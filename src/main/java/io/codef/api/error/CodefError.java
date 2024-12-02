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
    NULL_ORGANIZATION(
            "organization is required and cannot be null.",
            CodefReferenceUrl.DEV_GUIDE_REST_API
    ),
    OAUTH_UNAUTHORIZED(
            "Failed to authenticate with the Codef OAuth server (401 Unauthorized). Please verify your clientId and clientSecret values.",
            CodefReferenceUrl.KEY
    ),
    CODEF_API_UNAUTHORIZED(
            "Failed to authenticate with the Codef API server (401 Unauthorized). Please verify your clientId and clientSecret values.",
            CodefReferenceUrl.KEY
    ),
    OAUTH_INTERNAL_ERROR(
            "An error occurred on the Codef OAuth server. Please try again later, or contact support if the issue persists.",
            CodefReferenceUrl.KEY
    ),
    OAUTH_CONNECTION_ERROR(
            "The connection to the OAUTH server failed. Please check if `https://oauth.codef.io` is accessible.",
            CodefReferenceUrl.DEV_GUIDE_REST_API
    ),
    CODEF_API_SERVER_ERROR(
            "An error occurred during the request codef API Product. Please refer to the error message for details.",
            CodefReferenceUrl.DEV_GUIDE_REST_API
    ),
    RSA_ENCRYPTION_ERROR(
            "An error occurred on RSA Encryption. Please check your publicKey",
            CodefReferenceUrl.KEY
    ),
    NEED_TO_SECURE_WITH_METHOD(
            "To encrypt the parameters, you must call the following method: EasyCodefRequestBuilder.builder().secureWith(easyCodef).",
            CodefReferenceUrl.GITHUB
    ),
    NEED_TO_PATH_METHOD(
            "To request codef product, you must call the following method: EasyCodefRequestBuilder.builder().path(\"/v1/kr/***/***...\").",
            CodefReferenceUrl.GITHUB
    ),
    NEED_TO_ORGANIZATION_METHOD(
            "To request codef product, you must call the following method: EasyCodefRequestBuilder.builder().organization(\"0xxx\").",
            CodefReferenceUrl.GITHUB
    ),
    INVALID_PATH_REQUESTED(
            "The path should be requested in the following format: `/v1/kr/***/***/...`",
            CodefReferenceUrl.PRODUCT
    ),
    INTERNAL_SERVER_ERROR(
            "An error occurred on your request.",
            CodefReferenceUrl.DEV_GUIDE_REST_API
    ),
    PARSE_ERROR(
            "An exception occurred because the client could not parse the server response in the expected format, possibly due to incorrect headers or body format.",
            CodefReferenceUrl.TECH_INQUIRY
    ),
    IO_ERROR(
            "An error occurred because the request was either not sent properly or not received. Please check if the outbound port to IP: 211.55.34.5, PORT: 443 is open.",
            CodefReferenceUrl.TECH_INQUIRY
    ),
    SIMPLE_AUTH_FAILED(
            "No initial request data is saved for the specified transaction ID.",
            CodefReferenceUrl.TECH_INQUIRY
    ),
    NO_RESPONSE_RECEIVED(
            "No responses were received on multiple request",
            CodefReferenceUrl.MULTIPLE_REQUEST
    ),
    REQUEST_NULL(
            "Codef Request Entity is required and cannot be null.",
            CodefReferenceUrl.WIKI_003
    ),
    TRANSACTION_ID_NULL(
            "transactionId is required and cannot be null.",
            CodefReferenceUrl.WIKI_005
    ),
    KEY_CONFLICT_DEMO(
            "This token is for the demo version. EasycodefBuilder.clientType(CodefClientType.DEMO)",
            CodefReferenceUrl.WIKI_002
    ),
    KEY_CONFLICT_API(
            "This token is for the enterprise version. EasycodefBuilder.clientType(CodefClientType.API)",
            CodefReferenceUrl.WIKI_002
    );


    private static final String MESSAGE_FORMAT = "[EasyCodef] %s\n%s";
    private final String message;
    private final CodefReferenceUrl referenceUrl;

    CodefError(
            String message,
            CodefReferenceUrl referenceUrl
    ) {
        this.message = message;
        this.referenceUrl = referenceUrl;
    }

    public String getMessage() {
        return String.format(MESSAGE_FORMAT, message, referenceUrl.getUrl());
    }

    public String getRawMessage() {
        return message;
    }

    public CodefReferenceUrl getReferenceUrl() {
        return referenceUrl;
    }
    }