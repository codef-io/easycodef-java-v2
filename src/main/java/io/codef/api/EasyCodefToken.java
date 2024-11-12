package io.codef.api;

import java.time.LocalDateTime;
import java.util.Base64;

public class EasyCodefToken {
     private final String oauthToken;
     private String accessToken;
     private LocalDateTime expiresAt;

    protected EasyCodefToken(EasyCodefBuilder builder) {
        String combinedKey = String.join(":", builder.getClientId().toString(), builder.getClientSecret().toString());
        this.oauthToken = Base64.getEncoder().encodeToString(combinedKey.getBytes());
        this.accessToken = EasyCodefConnector.issueToken(oauthToken);
        this.expiresAt = LocalDateTime.now();
    }
}
