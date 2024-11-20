package io.codef.api.storage;

import io.codef.api.CodefValidator;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MultipleRequestStorage {

    private final Map<String, List<CompletableFuture<EasyCodefResponse>>> storage = new HashMap<>();

    public List<EasyCodefResponse> getRemainingResponses(
        String transactionId
    ) throws CodefException {
        final List<CompletableFuture<EasyCodefResponse>> futures = storage.get(transactionId);
        CodefValidator.requireNonNullElseThrow(futures, CodefError.SIMPLE_AUTH_FAILED);

        try {
            CompletableFuture<Void> allDone = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allDone.join();

            List<EasyCodefResponse> results = futures.stream()
                .map(this::safeJoin)
                .filter(Objects::nonNull)
                .filter(response -> !Objects.equals(response.transactionId(), transactionId))
                .toList();

            storage.remove(transactionId);
            return results;
        } catch (Exception e) {
            throw CodefException.from(CodefError.SIMPLE_AUTH_FAILED);
        }
    }

    private EasyCodefResponse safeJoin(CompletableFuture<EasyCodefResponse> future) {
        try {
            return future.join();
        } catch (Exception e) {
            throw CodefException.of(CodefError.SIMPLE_AUTH_FAILED, e);
        }
    }

    public void store(String transactionId, List<CompletableFuture<EasyCodefResponse>> futures) {
        storage.put(transactionId, futures);
    }
}