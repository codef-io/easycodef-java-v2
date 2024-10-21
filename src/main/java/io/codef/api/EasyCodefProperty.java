package io.codef.api;

import java.util.UUID;

public class EasyCodefProperty {

    private String publicKey;
    private UUID clientId;
    private UUID clientSecret;

    public static EasyCodefProperty builder() {
        return new EasyCodefProperty();
    }

    public EasyCodefProperty publicKey(String publicKey) {
        this.publicKey = CodefValidator.requireNonNullElseThrow(publicKey, CodefError.INVALID_PUBLIC_KEY);
        return this;
    }

    public EasyCodefProperty clientId(String clientId) {
        this.clientId = parseUUID(clientId, CodefError.INVALID_CLIENT_ID);
        return this;
    }

    public EasyCodefProperty clientSecret(String clientSecret) {
        this.clientSecret = parseUUID(clientSecret, CodefError.INVALID_CLIENT_SECRET);
        return this;
    }

    public EasyCodef build() {
        CodefValidator.requireNonNullElseThrow(publicKey, CodefError.INVALID_PUBLIC_KEY);
        CodefValidator.requireNonNullElseThrow(clientId, CodefError.INVALID_CLIENT_ID);
        CodefValidator.requireNonNullElseThrow(clientSecret, CodefError.INVALID_CLIENT_SECRET);

        EasyCodefToken easyCodefToken = EasyCodefToken.of(this);
        return new EasyCodef(easyCodefToken, this);
    }

    private UUID parseUUID(
            String value,
            CodefError error
    ) {
        CodefValidator.requireNonNullElseThrow(value, error);
        CodefValidator.requireValidUUIDPattern(value, error);

        try {
            return UUID.fromString(value);
        } catch (Exception exception) {
            throw CodefException.of(error, exception);
        }
    }

    protected String getPublicKey() {
        return publicKey;
    }

    protected String getCombinedKey() {
        final String KEY_FORMAT = "%s:%s";
        return String.format(KEY_FORMAT, clientId, clientSecret);
    }
}