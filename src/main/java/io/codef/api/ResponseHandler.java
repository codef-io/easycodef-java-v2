package io.codef.api;

import static io.codef.api.dto.EasyCodefRequest.ACCESS_TOKEN;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class ResponseHandler {
    private static final String UTF_8 = StandardCharsets.UTF_8.toString();

    /**
     * 토큰 응답 처리
     */
    public String handleTokenResponse(ClassicHttpResponse response) throws CodefException {
        return handleHttpResponse(
            response,
            this::parseAccessToken,
            CodefError.OAUTH_UNAUTHORIZED,
            CodefError.OAUTH_INTERNAL_ERROR,
            false
        );
    }

    /**
     * 상품 응답 처리
     */
    public EasyCodefResponse handleProductResponse(ClassicHttpResponse response) throws CodefException {
        return handleHttpResponse(
            response,
            this::parseProductResponse,
            CodefError.CODEF_API_UNAUTHORIZED,
            CodefError.CODEF_API_SERVER_ERROR,
            true
        );
    }

    /**
     * 공통 HTTP 응답 처리 로직
     */
    private <T> T handleHttpResponse(
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
    private String extractResponseBody(ClassicHttpResponse response, boolean requiresDecoding) throws CodefException {
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
    private String parseAccessToken(String responseBody) throws CodefException {
        try {
            return JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
        } catch (Exception e) {
            throw CodefException.of(CodefError.PARSE_ERROR, e);
        }
    }

    /**
     * 상품 응답 파싱
     */
    private EasyCodefResponse parseProductResponse(String responseBody) throws CodefException {
        try {
            JSONObject jsonResponse = JSON.parseObject(responseBody);

            EasyCodefResponse.Result result = Optional.ofNullable(jsonResponse.getJSONObject(RESULT))
                .map(object -> object.to(EasyCodefResponse.Result.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));

            Object data = Optional.ofNullable(jsonResponse.getJSONObject(DATA))
                .map(obj -> obj.to(Object.class))
                .orElseThrow(() -> CodefException.from(CodefError.PARSE_ERROR));

            return new EasyCodefResponse(result, data);
        } catch (Exception e) {
            throw CodefException.of(CodefError.PARSE_ERROR, e);
        }
    }
}