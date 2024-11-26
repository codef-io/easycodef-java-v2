package io.codef.api;

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

import com.alibaba.fastjson2.JSON;
import io.codef.api.constants.CodefHost;
import io.codef.api.constants.CodefPath;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.util.AuthorizationUtil;
import io.codef.api.util.HttpClientUtil;
import io.codef.api.util.JsonUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyCodefConnector {

    private static final Logger log = LoggerFactory.getLogger(EasyCodefConnector.class);
    private static final ResponseHandler responseHandler = new ResponseHandler();

    private EasyCodefConnector() {
    }

    /**
     * 상품 요청
     */
    public static EasyCodefResponse requestProduct(
        EasyCodefRequest request,
        EasyCodefToken token,
        String requestUrl
    ) throws CodefException {
        HttpPost httpRequest = createProductRequest(request, token, requestUrl);
        return executeRequest(httpRequest, responseHandler::handleProductResponse);
    }

    /**
     * 액세스 토큰 요청
     */
    public static String requestToken(
        String codefOAuthToken
    ) throws CodefException {
        HttpPost request = createTokenRequest(codefOAuthToken);
        return executeRequest(request, responseHandler::handleTokenResponse);
    }


    private static HttpPost createTokenRequest(String codefOAuthToken) {
        HttpPost httpPost = new HttpPost(CodefHost.CODEF_OAUTH_SERVER + CodefPath.ISSUE_TOKEN);
        String basicToken = AuthorizationUtil.createBasicAuth(codefOAuthToken);
        httpPost.addHeader(AUTHORIZATION, basicToken);
        return httpPost;
    }

    private static HttpPost createProductRequest(
        EasyCodefRequest request,
        EasyCodefToken token,
        String requestUrl
    ) {
        HttpPost httpPost = new HttpPost(requestUrl);
        String accessToken = AuthorizationUtil.createBearerAuth(token.getAccessToken());
        httpPost.addHeader(AUTHORIZATION, accessToken);

        String rawRequest = JSON.toJSONString(request.requestBody());
        httpPost.setEntity(new StringEntity(rawRequest, StandardCharsets.UTF_8));

        return httpPost;
    }

    private static <T> T executeRequest(
        HttpPost request,
        ResponseProcessor<T> processor
    ) throws CodefException {
        logRequest(request);

        try (CloseableHttpClient httpClient = HttpClientUtil.createClient()) {
            return httpClient.execute(request, response -> {
                T result = processor.process(response);
                logResponse(request.hashCode(), response, result);
                return result;
            });
        } catch (IOException exception) {
            throw CodefException.of(CodefError.IO_ERROR, exception);
        }
    }

    private static void logRequest(HttpPost request) {
        log.info("[{}] Codef API Request", request.hashCode());
        log.info("> Request Host: {}://{}",
            request.getScheme(),
            request.getAuthority()
        );
        log.info("> Request URI: {}\n", request.getRequestUri());
    }

    private static void logResponse(
        int requestHashCode,
        ClassicHttpResponse response,
        Object result
    ) {
        log.info("[{}] Codef API Response", requestHashCode);
        log.info("> Response Status: {}", response.getCode());
        log.info("> Response → \n{}\n", JsonUtil.toPrettyJson(result));
    }

    @FunctionalInterface
    private interface ResponseProcessor<T> {

        T process(ClassicHttpResponse response) throws CodefException;
    }
}