package io.codef.api;

public class EasyCodef {
    private final String publicKey;
    private final EasyCodefClientType clientType;
    private final EasyCodefToken easyCodefToken;

    protected EasyCodef(
            EasyCodefBuilder builder,
            EasyCodefToken easyCodefToken
    ) {
        this.clientType = builder.getClientType();
        this.publicKey = builder.getPublicKey();
        this.easyCodefToken = easyCodefToken;
    }
}