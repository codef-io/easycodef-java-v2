package io.codef.api;

public enum CodefError {
    INVALID_CLIENT_ID(
            "clientId must be a properly formatted UUID string. Please check your clientId and ensure it matches the UUID format.",
            EasyCodefReferenceUrl.KEY
    ),
    INVALID_CLIENT_SECRET(
            "clientSecret must be a properly formatted UUID string. Please check your clientSecret and ensure it matches the UUID format.",
            EasyCodefReferenceUrl.KEY
    ),
    INVALID_PUBLIC_KEY(
            "publicKey is required and cannot be null.",
            EasyCodefReferenceUrl.KEY
    ),
    NULL_CLIENT_ID(
            "clientId is required and cannot be null.",
            EasyCodefReferenceUrl.KEY
    ),
    NULL_CLIENT_SECRET(
            "clientSecret is required and cannot be null.",
            EasyCodefReferenceUrl.KEY
    ),
    NULL_PUBLIC_KEY(
            "publicKey is required and cannot be null.",
            EasyCodefReferenceUrl.KEY
    ),
    NULL_CLIENT_TYPE(
            "clientType is required and cannot be null.",
            EasyCodefReferenceUrl.KEY
    ),
    OAUTH_UNAUTHORIZED(
            "Failed to authenticate with the Codef OAuth server (401 Unauthorized). Please verify your clientId and clientSecret values.",
            EasyCodefReferenceUrl.KEY
    ),
    OAUTH_INTERNAL_ERROR(
            "An error occurred on the Codef OAuth server (500 Internal Server Error). Please try again later, or contact support if the issue persists.",
            EasyCodefReferenceUrl.KEY
    ),
    OAUTH_CONNECTION_ERROR(
            "The connection to the OAUTH server failed. Please check if `https://oauth.codef.io` is accessible.",
            EasyCodefReferenceUrl.DEV_GUIDE_REST_API
    );

    private final String message;
    private final EasyCodefReferenceUrl referenceUrl;

    CodefError(
            String message,
            EasyCodefReferenceUrl referenceUrl
    ) {
        this.message = message;
        this.referenceUrl = referenceUrl;
    }

    private static final String MESSAGE_FORMAT = "[EasyCodef] %s\n%s";

    public String getMessage() {
        return String.format(MESSAGE_FORMAT, message, referenceUrl.getUrl());
    }
}