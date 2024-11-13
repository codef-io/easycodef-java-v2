package io.codef.api.error;

import io.codef.api.constants.CodefReferenceUrl;

public enum CodefError {
    INVALID_CLIENT_ID(
            "clientId must be a properly formatted UUID string. Please check your clientId and ensure it matches the UUID format.",
            CodefReferenceUrl.KEY
    ),
    INVALID_CLIENT_SECRET(
            "clientSecret must be a properly formatted UUID string. Please check your clientSecret and ensure it matches the UUID format.",
            CodefReferenceUrl.KEY
    ),
    INVALID_PUBLIC_KEY(
            "publicKey is required and cannot be null.",
            CodefReferenceUrl.KEY
    ),
    NULL_CLIENT_ID(
            "clientId is required and cannot be null.",
            CodefReferenceUrl.KEY
    ),
    NULL_CLIENT_SECRET(
            "clientSecret is required and cannot be null.",
            CodefReferenceUrl.KEY
    ),
    NULL_PUBLIC_KEY(
            "publicKey is required and cannot be null.",
            CodefReferenceUrl.KEY
    ),
    NULL_CLIENT_TYPE(
            "clientType is required and cannot be null.",
            CodefReferenceUrl.KEY
    ),
    OAUTH_UNAUTHORIZED(
            "Failed to authenticate with the Codef OAuth server (401 Unauthorized). Please verify your clientId and clientSecret values.",
            CodefReferenceUrl.KEY
    ),
    OAUTH_INTERNAL_ERROR(
            "An error occurred on the Codef OAuth server (500 Internal Server Error). Please try again later, or contact support if the issue persists.",
            CodefReferenceUrl.KEY
    ),
    OAUTH_CONNECTION_ERROR(
            "The connection to the OAUTH server failed. Please check if `https://oauth.codef.io` is accessible.",
            CodefReferenceUrl.DEV_GUIDE_REST_API
    ),
    RSA_ENCRYPTION_ERROR(
            "An error occurred on RSA Encryption. Please check your publicKey",
            CodefReferenceUrl.KEY
    ),
    NEED_TO_SECURE_WITH_METHOD(
            "To encrypt the parameters, you must call the following method: EasyCodefRequestBuilder.builder().secureWith(easyCodef).",
            CodefReferenceUrl.GITHUB
    );

    private final String message;
    private final CodefReferenceUrl referenceUrl;

    CodefError(
            String message,
            CodefReferenceUrl referenceUrl
    ) {
        this.message = message;
        this.referenceUrl = referenceUrl;
    }

    private static final String MESSAGE_FORMAT = "[EasyCodef] %s\n%s";

    public String getMessage() {
        return String.format(MESSAGE_FORMAT, message, referenceUrl.getUrl());
    }
}