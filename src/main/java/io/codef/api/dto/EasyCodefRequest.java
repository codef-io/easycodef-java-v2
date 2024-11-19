package io.codef.api.dto;

import static org.apache.hc.client5.http.auth.StandardAuthScheme.BASIC;
import static org.apache.hc.client5.http.auth.StandardAuthScheme.BEARER;

import java.util.HashMap;

public record EasyCodefRequest(
    String path,
    HashMap<String, Object> requestBody
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
     * Request Body Key Constant
     */
    public static final String SSO_ID = "id";
    public static final String IS_TWO_WAY = "is2Way";
    public static final String SIMPLE_AUTH = "simpleAuth";
    public static final String TWO_WAY_INFO = "twoWayInfo";

    /**
     * Request Body Value Constant
     */
    public static final String TRUE = "1";

    /**
     * Header Format Constants
     */
    public static final String ORGANIZATION = "organization";
    public static final String EASY_CODEF_JAVA_FLAG = "easyCodefJavaV2";

    /**
     * Path Format Constants
     */
    public static final String PATH_PREFIX = "/v1";
}