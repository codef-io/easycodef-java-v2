package io.codef.api;

import io.codef.api.constants.CodefClientType;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.util.RsaUtil;

import java.security.PublicKey;

public class EasyCodef {
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

    public EasyCodefResponse requestProduct(EasyCodefRequest request) {
        final String requestUrl = generateRequestUrl(request);
        final EasyCodefToken validToken = easyCodefToken.validateAndRefreshToken();
        return EasyCodefConnector.requestProduct(request, validToken, requestUrl);
    }

    private String generateRequestUrl(EasyCodefRequest request) {
        return clientType.getHost() + request.path();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}