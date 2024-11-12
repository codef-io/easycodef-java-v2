package io.codef.api;

import io.codef.api.constants.EasyCodefClientType;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;

import java.util.UUID;

public class EasyCodefBuilder {
    private String publicKey;
    private UUID clientId;
    private UUID clientSecret;
    private EasyCodefClientType clientType;

    public static EasyCodefBuilder builder() {
        return new EasyCodefBuilder();
    }

    public EasyCodefBuilder publicKey(String publicKey) {
        this.publicKey = CodefValidator.requireNonNullElseThrow(publicKey, CodefError.INVALID_PUBLIC_KEY);
        return this;
    }

    public EasyCodefBuilder clientId(String clientId) {
        this.clientId = parseUUID(clientId, CodefError.INVALID_CLIENT_ID);
        return this;
    }

    public EasyCodefBuilder clientSecret(String clientSecret) {
        this.clientSecret = parseUUID(clientSecret, CodefError.INVALID_CLIENT_SECRET);
        return this;
    }

    public EasyCodefBuilder clientType(EasyCodefClientType clientType) {
        this.clientType = clientType;
        return this;
    }

    public EasyCodef build() {
        validatePropertyArguments();
        EasyCodefToken easyCodefToken = new EasyCodefToken(this);

        return new EasyCodef(this, easyCodefToken);
    }

    private void validatePropertyArguments() {
        CodefValidator.requireNonNullElseThrow(publicKey, CodefError.NULL_PUBLIC_KEY);
        CodefValidator.requireNonNullElseThrow(clientId, CodefError.NULL_CLIENT_ID);
        CodefValidator.requireNonNullElseThrow(clientSecret, CodefError.NULL_CLIENT_SECRET);
        CodefValidator.requireNonNullElseThrow(clientType, CodefError.NULL_CLIENT_TYPE);
    }

    private UUID parseUUID(String value, CodefError error) {
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

    protected UUID getClientId() {
        return clientId;
    }

    protected UUID getClientSecret() {
        return clientSecret;
    }

    protected EasyCodefClientType getClientType() {
        return clientType;
    }
}