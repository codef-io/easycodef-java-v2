package io.codef.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import java.util.Optional;

import static io.codef.api.dto.EasyCodefRequest.ACCESS_TOKEN;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;

public class ResponseHandler {
    private static final String UTF_8 = StandardCharsets.UTF_8.toString();

    /**
     * 토큰 응답 처리
     */
    public String handleTokenResponse(ClassicHttpResponse response) throws CodefException {
        HttpStatusHandler<String> handler = new HttpStatusHandler<>(
                response.getCode(),
                CodefError.OAUTH_UNAUTHORIZED,
                CodefError.OAUTH_INTERNAL_ERROR,
                this::parseAccessToken
        );

        return handleHttpResponse(response, handler, false);
    }

    /**
     * 상품 응답 처리
     */
    public EasyCodefResponse handleProductResponse(ClassicHttpResponse response) throws CodefException {
        HttpStatusHandler<EasyCodefResponse> handler = new HttpStatusHandler<>(
                response.getCode(),
                CodefError.CODEF_API_UNAUTHORIZED,
                CodefError.CODEF_API_SERVER_ERROR,
                this::parseProductResponse
        );

        return handleHttpResponse(response, handler, true);
    }

    /**
     * 공통 HTTP 응답 처리 로직
     */
    private <T> T handleHttpResponse(
            ClassicHttpResponse response,
            HttpStatusHandler<T> handler,
            boolean requireUrlDecoding
    ) throws CodefException {
        String responseBody = extractResponseBody(response, requireUrlDecoding);
        return handleStatusCode(responseBody, handler);
    }

    /**
     * HTTP 응답 본문 추출
     */
    private String extractResponseBody(
            ClassicHttpResponse response,
            boolean requiresDecoding
    ) {
        try {
            String responseBody = EntityUtils.toString(response.getEntity());
            return requiresDecoding ? URLDecoder.decode(responseBody, UTF_8) : responseBody;
        } catch (IOException ioException) {
            throw CodefException.of(CodefError.IO_ERROR, ioException);
        } catch (ParseException parseException) {
            throw CodefException.of(CodefError.PARSE_ERROR, parseException);
        }
    }

    /**
     * HTTP 상태 코드에 따른 처리
     */
    private <T> T handleStatusCode(String responseBody, HttpStatusHandler<T> handler) throws CodefException {
        return switch (handler.statusCode) {
            case HttpStatus.SC_OK -> handler.successHandler.parse(responseBody);
            case HttpStatus.SC_UNAUTHORIZED -> throw CodefException.of(handler.unauthorizedError, responseBody);
            default -> throw CodefException.of(handler.defaultError, responseBody);
        };
    }

    /**
     * 액세스 토큰 파싱
     */
    private String parseAccessToken(String responseBody) throws CodefException {
        try {
            return JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
        } catch (Exception exception) {
            throw CodefException.of(CodefError.PARSE_ERROR, exception);
        }
    }

    /**
     * 상품 응답 파싱
     */
    private EasyCodefResponse parseProductResponse(String responseBody) throws CodefException {
        JSONObject jsonResponse = JSON.parseObject(responseBody);

        EasyCodefResponse.Result result = Optional.ofNullable(jsonResponse.getJSONObject(RESULT))
                .map(object -> object.to(EasyCodefResponse.Result.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));

        Object data = Optional.ofNullable(jsonResponse.getJSONObject(DATA))
                .map(obj -> obj.to(Object.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));

        return new EasyCodefResponse(result, data);
    }


    /**
     * HTTP 응답 처리를 위한 공통 인터페이스
     */
    private interface ResponseParser<T> {
        T parse(String responseBody) throws CodefException;
    }

    /**
     * HTTP 응답 상태 코드에 따른 처리를 위한 레코드
     */
    private record HttpStatusHandler<T>(
            int statusCode,
            CodefError unauthorizedError,
            CodefError defaultError,
            ResponseParser<T> successHandler
    ) {
    }
}