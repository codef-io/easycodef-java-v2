package io.codef.api.storage;

import io.codef.api.CodefValidator;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MultipleRequestStorage {

    private static final Logger log = LoggerFactory.getLogger(MultipleRequestStorage.class);
    private final ConcurrentHashMap<String, List<CompletableFuture<EasyCodefResponse>>> storage = new ConcurrentHashMap<>();

    public List<EasyCodefResponse> getRemainingResponses(
        String transactionId
    ) throws CodefException {
        log.info("Await Responses called By transactionId `{}`", transactionId);

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
                .collect(Collectors.toCollection(ArrayList::new));

            storage.remove(transactionId);

            log.info("Await Responses Count = {}", results.size());
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