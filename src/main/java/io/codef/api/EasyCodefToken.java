package io.codef.api;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyCodefToken {

    private static final Logger log = LoggerFactory.getLogger(EasyCodefToken.class);

    private final String oauthToken;
    private String accessToken;
    private LocalDateTime expiresAt;

    protected EasyCodefToken(EasyCodefBuilder builder) {

        final int VALIDITY_PERIOD_DAYS = 7;
        final String DELIMITER = ":";

        String combinedKey = String.join(DELIMITER, builder.getClientId().toString(),
            builder.getClientSecret().toString());

        this.oauthToken = Base64.getEncoder().encodeToString(combinedKey.getBytes());
        log.info("Codef OAuth Token : {}", oauthToken);
        log.info("Codef OAuth Token successfully initialized.\n");

        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        log.info("Codef API AccessToken : {}", accessToken);

        this.expiresAt = LocalDateTime.now().plusDays(VALIDITY_PERIOD_DAYS);

        log.info(
            "Codef API AccessToken expiry at {} but, EasyCodef will handle automatic renewal",
            expiresAt
        );
        log.info("Codef API AccessToken successfully initialized.\n");
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
        log.info(
            "Codef API AccessToken expiry at {} so EasyCodef refresh token",
            expiresAt
        );
        this.accessToken = EasyCodefConnector.requestToken(oauthToken);
        log.info("Codef API AccessToken : {}", accessToken);

        this.expiresAt = LocalDateTime.now().plusDays(7);

        log.info(
            "AccessToken Refresh completed. Now, Codef accessToken expiry at {}.",
            expiresAt
        );
    }

    public String getAccessToken() {
        return accessToken;
    }
}
