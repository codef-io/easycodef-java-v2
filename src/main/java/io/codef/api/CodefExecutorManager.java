package io.codef.api;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.facade.SingleReqFacade;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CodefExecutorManager {

    private final Executor virtualThreadExecutor;

    private CodefExecutorManager(Executor virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public static CodefExecutorManager create() {
        return new CodefExecutorManager(
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory()));
    }

    public CompletableFuture<EasyCodefResponse> executeRequest(
        EasyCodefRequest request,
        SingleReqFacade facade
    ) {
        return CompletableFuture.supplyAsync(
            () -> facade.requestProduct(request),
            virtualThreadExecutor
        );
    }
}