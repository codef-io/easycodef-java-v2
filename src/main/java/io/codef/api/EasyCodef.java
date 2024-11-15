package io.codef.api;

import io.codef.api.constants.CodefClientType;
import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.storage.entity.CodefSimpleAuth;
import io.codef.api.util.RsaUtil;

import java.security.PublicKey;
import java.util.HashMap;

public class EasyCodef {
    private final HashMap<String, CodefSimpleAuth> simpleAuthRequestStorage = new HashMap<>();
    private final PublicKey publicKey;
    private final CodefClientType clientType;
    private final EasyCodefToken easyCodefToken;

    protected EasyCodef(
            EasyCodefBuilder builder,
            EasyCodefToken easyCodefToken
    ) {
        this.publicKey = RsaUtil.generatePublicKey(builder.getPublicKey());
        this.clientType = builder.getClientType();
        this.easyCodefToken = easyCodefToken;
    }

    public EasyCodefResponse requestProduct(
            EasyCodefRequest request
    ) throws CodefException {
        final String requestUrl = clientType.getHost() + request.path();
        final EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        final EasyCodefResponse easyCodefResponse = EasyCodefConnector.requestProduct(request, validToken, requestUrl);

        if (easyCodefResponse.code().equals(CodefResponseCode.CF_03002)) {
            CodefSimpleAuth codefSimpleAuth = new CodefSimpleAuth(requestUrl, request, easyCodefResponse);
            simpleAuthRequestStorage.put(easyCodefResponse.transactionId(), codefSimpleAuth);
        }

        return easyCodefResponse;
    }

    public EasyCodefResponse requestSimpleAuthCertification(
            String transactionId
    ) throws CodefException {
        final CodefSimpleAuth codefSimpleAuth = simpleAuthRequestStorage.get(transactionId);
        CodefValidator.requireNonNullElseThrow(codefSimpleAuth, CodefError.SIMPLE_AUTH_FAILED);

        final String path = codefSimpleAuth.getPath();
        final EasyCodefRequest request = codefSimpleAuth.getRequest();

        request.requestBody().put("is2Way", true);
        request.requestBody().put("simpleAuth", "1");
        request.requestBody().put("twoWayInfo", codefSimpleAuth.getResponse().data());

        final EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();

        final EasyCodefResponse easyCodefResponse = EasyCodefConnector.requestProduct(request, validToken, path);

        if (easyCodefResponse.code().equals(CodefResponseCode.CF_03002)) {
            CodefSimpleAuth newCodefSimpleAuth = new CodefSimpleAuth(path, request, easyCodefResponse);
            simpleAuthRequestStorage.put(transactionId, newCodefSimpleAuth);
            final CodefSimpleAuth updatedSimpleAuth = simpleAuthRequestStorage.get(transactionId);
        } else {
            simpleAuthRequestStorage.remove(transactionId);
        }

        return easyCodefResponse;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}