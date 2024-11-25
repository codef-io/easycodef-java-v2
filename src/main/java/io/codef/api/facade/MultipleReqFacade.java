package io.codef.api.facade;

import static io.codef.api.dto.EasyCodefRequest.SSO_ID;

import io.codef.api.CodefExecutorManager;
import io.codef.api.CodefValidator;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

// 다중 요청 처리기
public class MultipleReqFacade {
    private static final long REQUEST_DELAY_MS = 700L;

    private final SingleReqFacade singleReqFacade;
    private final MultipleRequestStorage multipleRequestStorage;
    private final CodefExecutorManager executorManager;

    public MultipleReqFacade(
        SingleReqFacade singleReqFacade,
        MultipleRequestStorage multipleRequestStorage,
        CodefExecutorManager executorManager
    ) {
        this.singleReqFacade = singleReqFacade;
        this.multipleRequestStorage = multipleRequestStorage;
        this.executorManager = executorManager;
    }

    public EasyCodefResponse requestMultipleProduct(List<EasyCodefRequest> requests)
        throws CodefException {
        validateRequests(requests);
        assignSsoId(requests, UUID.randomUUID().toString());

        try {
            List<CompletableFuture<EasyCodefResponse>> futures =
                scheduleRequests(requests);

            CompletableFuture<EasyCodefResponse> firstCompleted =
                CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(result -> (EasyCodefResponse) result);

            EasyCodefResponse result = firstCompleted.join();
            multipleRequestStorage.store(result.transactionId(), futures);

            return result;
        } finally {
            executorManager.shutdown();
        }
    }

    private void validateRequests(List<EasyCodefRequest> requests) {
        requests.forEach(
            request -> CodefValidator.requireNonNullElseThrow(request, CodefError.REQUEST_NULL));
    }

    private void assignSsoId(List<EasyCodefRequest> requests, String uuid) {
        requests.forEach(request -> request.requestBody().put(SSO_ID, uuid));
    }

    private List<CompletableFuture<EasyCodefResponse>> scheduleRequests(
        List<EasyCodefRequest> requests
    ) {
        return IntStream.range(0, requests.size())
            .mapToObj(i -> executorManager.scheduleRequest(
                requests.get(i),
                i * REQUEST_DELAY_MS,
                singleReqFacade
            ))
            .toList();
    }
}
