package io.codef.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

public class EasyCodefToken {
    private static final Logger log = LoggerFactory.getLogger(EasyCodefToken.class);

    private final String oauthToken;
    private String accessToken;
    private LocalDateTime expiresAt;

    protected EasyCodefToken(EasyCodefBuilder builder) {
        this.oauthToken = createOAuthToken(builder);
        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        this.expiresAt = calculateExpiryDateTime();

        EasyCodefLogger.logAccessTokenCreation(accessToken, expiresAt);
    }

    private String createOAuthToken(EasyCodefBuilder builder) {
        final String DELIMITER = ":";
        String combinedKey = String.join(
                DELIMITER,
                builder.getClientId().toString(),
                builder.getClientSecret().toString()
        );

        String oauthToken = Base64.getEncoder().encodeToString(combinedKey.getBytes());
        EasyCodefLogger.logOAuthTokenCreation(oauthToken);
        return oauthToken;
    }

    private LocalDateTime calculateExpiryDateTime() {
        final int VALIDITY_PERIOD_DAYS = 7;
        return LocalDateTime.now().plusDays(VALIDITY_PERIOD_DAYS);
    }

    public EasyCodefToken validateAndRefreshToken() {
        Optional.of(expiresAt).filter(this::isTokenExpiringSoon)
                .ifPresent(expiry -> refreshToken());
        return this;
    }

    private boolean isTokenExpiringSoon(LocalDateTime expiry) {
        return expiry.isBefore(LocalDateTime.now().plusHours(24));
    }

    private void refreshToken() {
        EasyCodefLogger.logTokenRefreshStart(expiresAt);

        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        this.expiresAt = calculateExpiryDateTime();

        EasyCodefLogger.logTokenRefreshCompletion(accessToken, expiresAt);
    }

    public String getAccessToken() {
        return accessToken;
    }
}