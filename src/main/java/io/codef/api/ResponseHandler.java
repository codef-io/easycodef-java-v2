package io.codef.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.codef.api.dto.EasyCodefRequest.ACCESS_TOKEN;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;

public class ResponseHandler {

    private static final String UTF_8 = StandardCharsets.UTF_8.toString();

    private ResponseHandler() {
    }

    public static boolean isSuccessResponse(EasyCodefResponse response) {
        return CodefResponseCode.CF_00000.equals(response.code());
    }

    public static boolean isAddAuthResponse(EasyCodefResponse response) {
        return CodefResponseCode.CF_03002.equals(response.code());
    }

    public static boolean isFailureResponse(EasyCodefResponse response) {
        return !isSuccessResponse(response) && !isAddAuthResponse(response);
    }

    /**
     * 토큰 응답 처리
     */
    public static String handleTokenResponse(ClassicHttpResponse response) throws CodefException {
        return handleHttpResponse(
                response,
                ResponseHandler::parseAccessToken,
                CodefError.OAUTH_UNAUTHORIZED,
                CodefError.OAUTH_INTERNAL_ERROR,
                false
        );
    }

    /**
     * 상품 응답 처리
     */
    public static EasyCodefResponse handleProductResponse(ClassicHttpResponse response)
            throws CodefException {
        return handleHttpResponse(
                response,
                ResponseHandler::parseProductResponse,
                CodefError.CODEF_API_UNAUTHORIZED,
                CodefError.CODEF_API_SERVER_ERROR,
                true
        );
    }

    /**
     * 상품 응답 파싱
     */
    private static EasyCodefResponse parseProductResponse(String responseBody) throws CodefException {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            EasyCodefResponse.Result result = parseResult(jsonResponse);
            Object data = parseData(jsonResponse);

            return new EasyCodefResponse(result, data);
        } catch (Exception exception) {
            throw CodefException.of(CodefError.PARSE_ERROR, exception);
        }
    }

    private static EasyCodefResponse.Result parseResult(JSONObject jsonResponse) throws CodefException {
        return Optional.ofNullable(jsonResponse.getJSONObject(RESULT))
                .map(object -> object.to(EasyCodefResponse.Result.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));
    }

    private static Object parseData(JSONObject jsonResponse) throws CodefException {
        try {
            return parseObjectData(jsonResponse);
        } catch (Exception e) {
            return parseArrayData(jsonResponse);
        }
    }

    private static Object parseObjectData(JSONObject jsonResponse) throws CodefException {
        return Optional.ofNullable(jsonResponse.getJSONObject(DATA))
                .map(obj -> obj.to(Object.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));
    }

    private static List<?> parseArrayData(JSONObject jsonResponse) throws CodefException {
        return Optional.ofNullable(jsonResponse.getJSONArray(DATA))
                .map(obj -> obj.to(List.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));
    }

    /**
     * 공통 HTTP 응답 처리 로직
     */
    private static <T> T handleHttpResponse(
            ClassicHttpResponse response,
            Function<String, T> parser,
            CodefError unauthorizedError,
            CodefError defaultError,
            boolean requireUrlDecoding
    ) throws CodefException {
        String responseBody = extractResponseBody(response, requireUrlDecoding);

        return switch (response.getCode()) {
            case HttpStatus.SC_OK -> parser.apply(responseBody);
            case HttpStatus.SC_UNAUTHORIZED -> throw CodefException.of(unauthorizedError, responseBody);
            default -> throw CodefException.of(defaultError, responseBody);
        };
    }

    /**
     * HTTP 응답 본문 추출
     */
    private static String extractResponseBody(ClassicHttpResponse response, boolean requiresDecoding)
            throws CodefException {
        try {
            String responseBody = EntityUtils.toString(response.getEntity());
            return requiresDecoding ? URLDecoder.decode(responseBody, UTF_8) : responseBody;
        } catch (IOException e) {
            throw CodefException.of(CodefError.IO_ERROR, e);
        } catch (ParseException e) {
            throw CodefException.of(CodefError.PARSE_ERROR, e);
        }
    }

    /**
     * 액세스 토큰 파싱
     */
    private static String parseAccessToken(String responseBody) throws CodefException {
        try {
            return JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
        } catch (Exception e) {
            throw CodefException.of(CodefError.PARSE_ERROR, e);
        }
    }
}