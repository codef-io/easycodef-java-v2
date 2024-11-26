package io.codef.api;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.facade.SingleReqFacade;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// 실행기 관리를 위한 클래스
public class CodefExecutorManager implements AutoCloseable {
    private final ScheduledExecutorService scheduler;
    private final Executor virtualThreadExecutor;

    private CodefExecutorManager(ScheduledExecutorService scheduler, Executor virtualThreadExecutor) {
        this.scheduler = scheduler;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public static CodefExecutorManager create() {
        return new CodefExecutorManager(
            Executors.newScheduledThreadPool(1),
            Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())
        );
    }

    public CompletableFuture<EasyCodefResponse> scheduleRequest(
        EasyCodefRequest request,
        long delayMs,
        SingleReqFacade facade
    ) {
        return scheduleDelayedExecution(delayMs)
            .thenComposeAsync(
                ignored -> executeRequest(request, facade),
                virtualThreadExecutor
            );
    }

    private CompletableFuture<Void> scheduleDelayedExecution(long delayMs) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        scheduler.schedule(
            () -> future.complete(null),
            delayMs,
            TimeUnit.MILLISECONDS
        );
        return future;
    }

    private CompletableFuture<EasyCodefResponse> executeRequest(
        EasyCodefRequest request,
        SingleReqFacade facade
    ) {
        return CompletableFuture.supplyAsync(
            () -> facade.requestProduct(request),
            virtualThreadExecutor
        );
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}