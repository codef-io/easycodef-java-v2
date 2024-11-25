package io.codef.api;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// 실행기 관리를 위한 클래스
public class CodefExecutorManager {
    private final ScheduledExecutorService scheduler;
    private final Executor virtualThreadExecutor;

    public CodefExecutorManager(ScheduledExecutorService scheduler, Executor virtualThreadExecutor) {
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
        SingleProductRequestor requestor
    ) {
        CompletableFuture<EasyCodefResponse> future = new CompletableFuture<>();

        scheduler.schedule(
            () -> executeRequest(request, requestor, future),
            delayMs,
            TimeUnit.MILLISECONDS
        );

        return future;
    }

    private void executeRequest(
        EasyCodefRequest request,
        SingleProductRequestor requestor,
        CompletableFuture<EasyCodefResponse> future
    ) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return requestor.requestProduct(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, virtualThreadExecutor).whenComplete((response, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                future.complete(response);
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
