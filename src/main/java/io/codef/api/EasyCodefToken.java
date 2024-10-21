package io.codef.api;

import java.util.Base64;

public class EasyCodefToken {

    private String codefOAuthToken;
    private String codefAccessToken;
    private EasyCodefServiceType serviceType;

    private EasyCodefToken(
            EasyCodefProperty property
    ) {
        this.codefOAuthToken = generateBase64OAuthToken(property);
        this.codefAccessToken = EasyCodefConnector.issueToken(codefOAuthToken);
    }

    protected static EasyCodefToken of(EasyCodefProperty property) {
        return new EasyCodefToken(property);
    }

    private static String generateBase64OAuthToken(EasyCodefProperty property) {
        String combinedKey = property.getCombinedKey();
        return Base64.getEncoder().encodeToString(combinedKey.getBytes());
    }
}
