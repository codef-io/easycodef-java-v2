package io.codef.api.facade;

import io.codef.api.EasyCodefConnector;
import io.codef.api.EasyCodefLogger;
import io.codef.api.EasyCodefToken;
import io.codef.api.constants.CodefClientType;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.storage.SimpleAuthStorage;

// 단일 요청 처리기
public class SingleReqFacade {
    private final EasyCodefToken easyCodefToken;
    private final SimpleAuthStorage simpleAuthStorage;
    private final CodefClientType clientType;

    public SingleReqFacade(
            EasyCodefToken easyCodefToken,
            SimpleAuthStorage simpleAuthStorage,
            CodefClientType clientType
    ) {
        this.easyCodefToken = easyCodefToken;
        this.simpleAuthStorage = simpleAuthStorage;
        this.clientType = clientType;
    }

    public EasyCodefResponse requestProduct(
            EasyCodefRequest request
    ) throws CodefException {
        String requestUrl = buildRequestUrl(request);
        EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        EasyCodefResponse response = EasyCodefConnector.requestProduct(request, validToken, requestUrl);

        if (response.code().equals("CF-00004")) {
            throw CodefException.from(CodefError.KEY_CONFLICT_DEMO);
        } else if (response.code().equals("CF-00005")) {
            throw CodefException.from(CodefError.KEY_CONFLICT_API);
        }

        simpleAuthStorage.storeIfAddAuthResponse(request, response, requestUrl);
        EasyCodefLogger.logResponseStatus(response);
        return response;
    }

    private String buildRequestUrl(EasyCodefRequest request) {
        return clientType.getHost() + request.path();
    }
}
