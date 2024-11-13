package io.codef.api;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.codef.api.constants.CodefHost;
import io.codef.api.constants.CodefPath;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.util.HttpClientUtil;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static io.codef.api.dto.EasyCodefRequest.*;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

@FunctionalInterface
interface ResponseProcessor<T> {
    T process(ClassicHttpResponse response) throws IOException, ParseException;
}

public final class EasyCodefConnector {
    private static final ResponseHandler responseHandler = new ResponseHandler();

    private EasyCodefConnector() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * OAuth 토큰을 발급받습니다.
     */
    public static String requestToken(String codefOAuthToken) {
        HttpPost request = createTokenRequest(codefOAuthToken);
        return executeRequest(request, responseHandler::handleTokenResponse);
    }

    /**
     * CODEF 상품 API를 호출합니다.
     */
    public static EasyCodefResponse requestProduct(
            EasyCodefRequest request,
            EasyCodefToken token,
            String requestUrl
    ) {
        HttpPost httpRequest = createProductRequest(request, token, requestUrl);
        return executeRequest(httpRequest, responseHandler::handleProductResponse);
    }

    private static HttpPost createTokenRequest(String codefOAuthToken) {
        HttpPost httpPost = new HttpPost(CodefHost.CODEF_OAUTH_SERVER + CodefPath.ISSUE_TOKEN);
        httpPost.addHeader(AUTHORIZATION, String.format(BASIC_TOKEN_FORMAT, codefOAuthToken));
        return httpPost;
    }

    private static HttpPost createProductRequest(
            EasyCodefRequest request,
            EasyCodefToken token,
            String requestUrl
    ) {
        HttpPost httpPost = new HttpPost(requestUrl);
        httpPost.addHeader(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, token.getAccessToken()));

        String rawRequest = JSON.toJSONString(request.requestBody());
        httpPost.setEntity(new StringEntity(rawRequest, StandardCharsets.UTF_8));

        return httpPost;
    }

    private static <T> T executeRequest(HttpPost request, ResponseProcessor<T> processor) {
        try (CloseableHttpClient httpClient = HttpClientUtil.createClient()) {
            return httpClient.execute(request, processor::process);
        } catch (CodefException e) {
            throw e;
        } catch (Exception e) {
            throw CodefException.of(CodefError.INTERNAL_SERVER_ERROR, e);
        }
    }
}

final class ResponseHandler {
    String handleTokenResponse(ClassicHttpResponse response) throws IOException, ParseException {
        final String responseBody = EntityUtils.toString(response.getEntity());

        return switch (response.getCode()) {
            case HttpStatus.SC_OK -> JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
            case HttpStatus.SC_UNAUTHORIZED -> throw CodefException.of(CodefError.OAUTH_UNAUTHORIZED, responseBody);
            default -> throw CodefException.of(CodefError.OAUTH_INTERNAL_ERROR, responseBody);
        };
    }

    EasyCodefResponse handleProductResponse(ClassicHttpResponse response) throws IOException, ParseException {
        String httpResponse = EntityUtils.toString(response.getEntity());
        String decodedResponse = URLDecoder.decode(httpResponse, StandardCharsets.UTF_8);

        if (response.getCode() != HttpStatus.SC_OK) {
            throw CodefException.of(CodefError.CODEF_API_SERVER_ERROR, decodedResponse);
        }

        return parseProductResponse(decodedResponse);
    }

    private EasyCodefResponse parseProductResponse(String decodedResponse) {
        JSONObject jsonResponseObject = JSON.parseObject(decodedResponse);

        EasyCodefResponse.Result resultResponse = jsonResponseObject.getJSONObject(RESULT)
                .to(EasyCodefResponse.Result.class);

        Object dataResponse = jsonResponseObject.getJSONObject(DATA)
                .to(Object.class);

        return new EasyCodefResponse(resultResponse, dataResponse);
    }
}