package io.codef.api;

import io.codef.api.constants.CodefClientType;
import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.CodefSimpleAuth;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.util.RsaUtil;

import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.*;

import static io.codef.api.dto.EasyCodefRequest.SSO_ID;
import static io.codef.api.dto.EasyCodefRequest.TRUE;

public class EasyCodef {
    private final HashMap<String, CodefSimpleAuth> simpleAuthRequestStorage = new HashMap<>();
    private final HashMap<String, List<CompletableFuture<EasyCodefResponse>>> multipleSimpleAuthRequestStorage = new HashMap<>();

    private final PublicKey publicKey;
    private final CodefClientType clientType;
    private final EasyCodefToken easyCodefToken;

    protected EasyCodef(EasyCodefBuilder builder, EasyCodefToken easyCodefToken) {
        this.publicKey = RsaUtil.generatePublicKey(builder.getPublicKey());
        this.clientType = builder.getClientType();
        this.easyCodefToken = easyCodefToken;
    }

    public EasyCodefResponse requestProduct(EasyCodefRequest request) throws CodefException {
        final String requestUrl = clientType.getHost() + request.path();
        final EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        final EasyCodefResponse easyCodefResponse = EasyCodefConnector.requestProduct(request, validToken, requestUrl);

        storeIfSimpleAuthResponseRequired(request, easyCodefResponse, requestUrl);
        return easyCodefResponse;
    }

    public List<EasyCodefResponse> requestSimpleAuthCertification(String transactionId) throws CodefException {
        final CodefSimpleAuth codefSimpleAuth = simpleAuthRequestStorage.get(transactionId);
        CodefValidator.requireNonNullElseThrow(codefSimpleAuth, CodefError.SIMPLE_AUTH_FAILED);

        final String requestUrl = codefSimpleAuth.requestUrl();
        final EasyCodefRequest request = codefSimpleAuth.request();

        addTwoWayInfo(request, codefSimpleAuth);

        final EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();
        final EasyCodefResponse firstResponse = EasyCodefConnector.requestProduct(request, validToken, requestUrl);

        updateSimpleAuthResponseRequired(requestUrl, request, firstResponse, transactionId);

        if (firstResponse.code().equals(CodefResponseCode.CF_00000)) {
            List<EasyCodefResponse> remainingResponses = getRemainingResponse(transactionId);
            final List<EasyCodefResponse> responses = new ArrayList<>(remainingResponses);
            responses.add(firstResponse);

            return responses;
        }

        return List.of(firstResponse);
    }

    private List<EasyCodefResponse> getRemainingResponse(String transactionId) throws CodefException {
        final List<CompletableFuture<EasyCodefResponse>> completableFutures = multipleSimpleAuthRequestStorage.get(transactionId);

        if (completableFutures == null || completableFutures.isEmpty()) {
            throw CodefException.from(CodefError.SIMPLE_AUTH_FAILED);
        }

        try {
            CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));

            allDoneFuture.join();

            final List<EasyCodefResponse> result = completableFutures.stream().map(future -> {
                try {
                    return future.join();
                } catch (Exception exception) {
                    throw CodefException.of(CodefError.SIMPLE_AUTH_FAILED, exception);
                }
            }).filter(Objects::nonNull).filter(future -> !Objects.equals(future.transactionId(), transactionId)).toList();

            multipleSimpleAuthRequestStorage.remove(transactionId);

            return result;
        } catch (Exception e) {
            throw CodefException.from(CodefError.NO_RESPONSE_RECEIVED);
        }
    }

    public EasyCodefResponse requestMultipleProduct(List<EasyCodefRequest> requests) throws CodefException {
        final String uuid = UUID.randomUUID().toString();
        requests.forEach(request -> request.requestBody().put(SSO_ID, uuid));

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Executor executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

        List<CompletableFuture<EasyCodefResponse>> futures = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            final EasyCodefRequest request = requests.get(i);

            CompletableFuture<EasyCodefResponse> future = new CompletableFuture<>();
            scheduler.schedule(() -> {
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
            }, i * 700L, TimeUnit.MILLISECONDS);
            futures.add(future);
        }

        CompletableFuture<EasyCodefResponse> firstCompleted = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0])).thenApply(result -> (EasyCodefResponse) result);

        futures.remove(firstCompleted);

        final EasyCodefResponse result = firstCompleted.join();
        multipleSimpleAuthRequestStorage.put(result.transactionId(), futures);

        return result;
    }

    private void addTwoWayInfo(EasyCodefRequest request, CodefSimpleAuth codefSimpleAuth) {
        request.requestBody().put(EasyCodefRequest.IS_TWO_WAY, true);
        request.requestBody().put(EasyCodefRequest.SIMPLE_AUTH, TRUE);
        request.requestBody().put(EasyCodefRequest.TWO_WAY_INFO, codefSimpleAuth.response().data());
    }

    private void storeIfSimpleAuthResponseRequired(EasyCodefRequest request, EasyCodefResponse easyCodefResponse, String requestUrl) {
        Optional.ofNullable(easyCodefResponse.code()).filter(code -> code.equals(CodefResponseCode.CF_03002)).ifPresent(code -> {
            CodefSimpleAuth codefSimpleAuth = new CodefSimpleAuth(requestUrl, request, easyCodefResponse);
            simpleAuthRequestStorage.put(easyCodefResponse.transactionId(), codefSimpleAuth);
        });
    }

    private void updateSimpleAuthResponseRequired(String path, EasyCodefRequest request, EasyCodefResponse easyCodefResponse, String transactionId) {
        Optional.ofNullable(easyCodefResponse.code()).filter(code -> code.equals(CodefResponseCode.CF_03002)).ifPresentOrElse(code -> {
            CodefSimpleAuth newCodefSimpleAuth = new CodefSimpleAuth(path, request, easyCodefResponse);
            simpleAuthRequestStorage.put(transactionId, newCodefSimpleAuth);
        }, () -> simpleAuthRequestStorage.remove(transactionId));
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}