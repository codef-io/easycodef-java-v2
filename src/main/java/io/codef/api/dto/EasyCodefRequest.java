package io.codef.api.dto;

import java.util.HashMap;

import static org.apache.hc.client5.http.auth.StandardAuthScheme.BASIC;
import static org.apache.hc.client5.http.auth.StandardAuthScheme.BEARER;

public record EasyCodefRequest(
        String path,
        HashMap<String, Object> requestParams
) {
    /**
     * Header Format Constants
     */
    public static final String BEARER_TOKEN_FORMAT = BEARER + " %s";
    public static final String BASIC_TOKEN_FORMAT = BASIC + " %s";

    /**
     * Header Format Constants
     */
    public static final String ACCESS_TOKEN = "access_token";

    /**
     * Header Format Constants
     */
    public static final String ORGANIZATION = "organization";
    public static final String EASY_CODEF_JAVA_FLAG = "easyCodefJavaV2";
}