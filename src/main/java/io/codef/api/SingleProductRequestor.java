package io.codef.api;

import io.codef.api.constants.CodefClientType;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.SimpleAuthStorage;

// 단일 요청 처리기
public class SingleProductRequestor {
    private final EasyCodefToken easyCodefToken;
    private final SimpleAuthStorage simpleAuthStorage;
    private final CodefClientType clientType;

    public SingleProductRequestor(
        EasyCodefToken easyCodefToken,
        SimpleAuthStorage simpleAuthStorage,
        CodefClientType clientType
    ) {
        this.easyCodefToken = easyCodefToken;
        this.simpleAuthStorage = simpleAuthStorage;
        this.clientType = clientType;
    }

    public EasyCodefResponse requestProduct(EasyCodefRequest request) throws CodefException {
        String requestUrl = buildRequestUrl(request);
        EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        EasyCodefResponse response =
            EasyCodefConnector.requestProduct(request, validToken, requestUrl);

        simpleAuthStorage.storeIfRequired(request, response, requestUrl);
        return response;
    }

    private String buildRequestUrl(EasyCodefRequest request) {
        return clientType.getHost() + request.path();
    }
}
