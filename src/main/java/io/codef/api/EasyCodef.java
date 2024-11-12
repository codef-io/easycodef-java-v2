package io.codef.api;

import io.codef.api.constants.EasyCodefClientType;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.util.RsaUtil;

import java.security.PublicKey;

public class EasyCodef {
    private final PublicKey publicKey;
    private final EasyCodefClientType clientType;
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
            String endpoint
    ) {
        return EasyCodefConnector.requestProduct(request, endpoint, clientType);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}