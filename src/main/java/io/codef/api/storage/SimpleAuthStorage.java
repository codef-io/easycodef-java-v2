package io.codef.api.storage;

import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.vo.CodefSimpleAuth;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleAuthStorage {

    private final ConcurrentHashMap<String, CodefSimpleAuth> storage = new ConcurrentHashMap<>();

    public void storeIfRequired(
        EasyCodefRequest request,
        EasyCodefResponse response,
        String requestUrl
    ) {
        Optional.ofNullable(response.code())
            .filter(code -> code.equals(CodefResponseCode.CF_03002))
            .ifPresent(code -> {
                CodefSimpleAuth simpleAuth = new CodefSimpleAuth(requestUrl, request, response);
                storage.put(response.transactionId(), simpleAuth);
            });
    }

    public CodefSimpleAuth get(String transactionId) throws CodefException {
        return Optional.ofNullable(storage.get(transactionId))
            .orElseThrow(() -> CodefException.from(CodefError.SIMPLE_AUTH_FAILED));
    }

    public void updateIfRequired(
        String path,
        EasyCodefRequest request,
        EasyCodefResponse response,
        String transactionId
    ) {
        Optional.ofNullable(response.code())
            .filter(code -> code.equals(CodefResponseCode.CF_03002))
            .ifPresentOrElse(
                code -> {
                    CodefSimpleAuth newAuth = new CodefSimpleAuth(path, request, response);
                    storage.put(transactionId, newAuth);
                },
                () -> storage.remove(transactionId)
            );
    }
}