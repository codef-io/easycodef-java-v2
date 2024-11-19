package io.codef.api;

import static io.codef.api.dto.EasyCodefRequest.SSO_ID;
import static io.codef.api.dto.EasyCodefRequest.TRUE;

import io.codef.api.constants.CodefClientType;
import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.CodefSimpleAuth;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.util.RsaUtil;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class EasyCodef {

    private static final long REQUEST_DELAY_MS = 700L;
    private final SimpleAuthStorage simpleAuthStorage;
    private final MultipleRequestStorage multipleRequestStorage;
    private final PublicKey publicKey;
    private final CodefClientType clientType;
    private final EasyCodefToken easyCodefToken;

    protected EasyCodef(EasyCodefBuilder builder, EasyCodefToken easyCodefToken) {
        this.publicKey = RsaUtil.generatePublicKey(builder.getPublicKey());
        this.clientType = builder.getClientType();
        this.easyCodefToken = easyCodefToken;
        this.simpleAuthStorage = new SimpleAuthStorage();
        this.multipleRequestStorage = new MultipleRequestStorage();
    }

    /**
     * 단일 상품 요청
     */
    public EasyCodefResponse requestProduct(EasyCodefRequest request) throws CodefException {
        String requestUrl = buildRequestUrl(request);
        EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        EasyCodefResponse response = EasyCodefConnector.requestProduct(request, validToken,
            requestUrl);
        simpleAuthStorage.storeIfRequired(request, response, requestUrl);

        return response;
    }

    /**
     * 다중 상품 요청
     */
    public EasyCodefResponse requestMultipleProduct(List<EasyCodefRequest> requests)
        throws CodefException {
        validateRequests(requests);
        assignSsoId(requests, UUID.randomUUID().toString());

        var executors = createExecutors();
        try {
            return processMultipleRequests(requests, executors);
        } finally {
            cleanupExecutors(executors);
        }
    }

    /**
     * 단건 간편인증 완료 요청
     */
    public EasyCodefResponse requestSimpleAuthCertification(String transactionId)
        throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);

        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);
        EasyCodefResponse response = executeSimpleAuthRequest(enrichedRequest,
            simpleAuth.requestUrl());

        simpleAuthStorage.updateIfRequired(
            simpleAuth.requestUrl(),
            enrichedRequest,
            response,
            transactionId
        );

        return response;
    }

    /**
     * 다건 간편인증 완료 요청
     */
    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(String transactionId)
        throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);

        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);
        EasyCodefResponse firstResponse = executeSimpleAuthRequest(enrichedRequest,
            simpleAuth.requestUrl());

        simpleAuthStorage.updateIfRequired(
            simpleAuth.requestUrl(),
            enrichedRequest,
            firstResponse,
            transactionId
        );

        return isSuccessful(firstResponse)
            ? combineWithRemainingResponses(firstResponse, transactionId)
            : List.of(firstResponse);
    }

    // Private helper methods

    private String buildRequestUrl(EasyCodefRequest request) {
        return clientType.getHost() + request.path();
    }

    private EasyCodefRequest enrichRequestWithTwoWayInfo(CodefSimpleAuth simpleAuth) {
        EasyCodefRequest request = simpleAuth.request();
        Map<String, Object> body = request.requestBody();

        body.put(EasyCodefRequest.IS_TWO_WAY, true);
        body.put(EasyCodefRequest.SIMPLE_AUTH, TRUE);
        body.put(EasyCodefRequest.TWO_WAY_INFO, simpleAuth.response().data());

        return request;
    }

    private EasyCodefResponse executeSimpleAuthRequest(EasyCodefRequest request, String requestUrl)
        throws CodefException {
        EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();
        return EasyCodefConnector.requestProduct(request, validToken, requestUrl);
    }

    private boolean isSuccessful(EasyCodefResponse response) {
        return CodefResponseCode.CF_00000.equals(response.code());
    }

    private List<EasyCodefResponse> combineWithRemainingResponses(
        EasyCodefResponse firstResponse,
        String transactionId
    ) throws CodefException {
        List<EasyCodefResponse> remainingResponses = multipleRequestStorage.getRemainingResponses(
            transactionId);
        List<EasyCodefResponse> allResponses = new ArrayList<>(remainingResponses);
        allResponses.add(firstResponse);
        return allResponses;
    }

    private void validateRequests(List<EasyCodefRequest> requests) {
        requests.forEach(
            request -> CodefValidator.requireNonNullElseThrow(request, CodefError.REQUEST_NULL));
    }

    private void assignSsoId(List<EasyCodefRequest> requests, String uuid) {
        requests.forEach(request -> request.requestBody().put(SSO_ID, uuid));
    }

    private CodefExecutors createExecutors() {
        return new CodefExecutors(
            Executors.newScheduledThreadPool(1),
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())
        );
    }

    private EasyCodefResponse processMultipleRequests(
        List<EasyCodefRequest> requests,
        CodefExecutors codefExecutors
    ) throws CodefException {
        List<CompletableFuture<EasyCodefResponse>> futures = scheduleRequests(requests,
            codefExecutors);

        CompletableFuture<EasyCodefResponse> firstCompleted = CompletableFuture.anyOf(
            futures.toArray(new CompletableFuture[0])
        ).thenApply(result -> (EasyCodefResponse) result);

        EasyCodefResponse result = firstCompleted.join();
        multipleRequestStorage.store(result.transactionId(), futures);

        return result;
    }

    private List<CompletableFuture<EasyCodefResponse>> scheduleRequests(
        List<EasyCodefRequest> requests,
        CodefExecutors codefExecutors
    ) {
        return IntStream.range(0, requests.size())
            .mapToObj(i -> scheduleRequest(requests.get(i), i * REQUEST_DELAY_MS, codefExecutors))
            .toList();
    }

    private CompletableFuture<EasyCodefResponse> scheduleRequest(
        EasyCodefRequest request,
        long delayMs,
        CodefExecutors codefExecutors
    ) {
        CompletableFuture<EasyCodefResponse> future = new CompletableFuture<>();

        codefExecutors.scheduler.schedule(
            () -> executeRequest(request, codefExecutors.virtualThreadExecutor, future),
            delayMs,
            TimeUnit.MILLISECONDS
        );

        return future;
    }

    private void executeRequest(
        EasyCodefRequest request,
        Executor executor,
        CompletableFuture<EasyCodefResponse> future
    ) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return requestProduct(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor).whenComplete((response, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                future.complete(response);
            }
        });
    }

    private void cleanupExecutors(CodefExecutors codefExecutors) {
        codefExecutors.scheduler.shutdown();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private record CodefExecutors(
        ScheduledExecutorService scheduler,
        Executor virtualThreadExecutor
    ) {

    }
}