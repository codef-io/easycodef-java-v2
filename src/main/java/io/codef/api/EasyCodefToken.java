package io.codef.api;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

public class EasyCodefToken {
    private final String oauthToken;
    private String accessToken;
    private LocalDateTime expiresAt;

    protected EasyCodefToken(EasyCodefBuilder builder) {
        final int VALIDITY_PERIOD_DAYS = 7;
        final String DELIMITER = ":";

        String combinedKey = String.join(DELIMITER, builder.getClientId().toString(), builder.getClientSecret().toString());
        this.oauthToken = Base64.getEncoder().encodeToString(combinedKey.getBytes());
        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        this.expiresAt = LocalDateTime.now().plusDays(VALIDITY_PERIOD_DAYS);
    }

    public EasyCodefToken validateAndRefreshToken() {
        Optional.of(expiresAt)
                .filter(this::isTokenExpiringSoon)
                .ifPresent(expiry -> refreshToken());
        
        return this;
    }

    private boolean isTokenExpiringSoon(LocalDateTime expiry) {
        return expiry.isBefore(LocalDateTime.now().plusHours(24));
    }

    private void refreshToken() {
        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }

    public String getAccessToken() {
        return accessToken;
    }
}
