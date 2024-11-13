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

    public EasyCodefResponse requestProduct(
            EasyCodefRequest request,
            String path
    ) {
        final String requestUrl = clientType.getHost() + path;
        final EasyCodefToken easyCodefToken = this.easyCodefToken.validateAndRefreshToken();

        return EasyCodefConnector.requestProduct(request, easyCodefToken, requestUrl);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}