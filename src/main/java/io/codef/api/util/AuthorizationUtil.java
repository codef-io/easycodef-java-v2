package io.codef.api.util;

public  class AuthorizationUtil {
    private static final String BASIC_FORMAT = "Basic %s";
    private static final String BEARER_FORMAT = "Bearer %s";

    public static String createBasicAuth(String token) {
        return String.format(BASIC_FORMAT, token);
    }

    public static String createBearerAuth(String token) {
        return String.format(BEARER_FORMAT, token);
    }
}
