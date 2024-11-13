package io.codef.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.codef.api.constants.CodefHost;
import io.codef.api.constants.CodefPath;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static io.codef.api.dto.EasyCodefRequest.*;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

final class EasyCodefConnector {

    static String issueToken(String codefOAuthToken) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(CodefHost.CODEF_OAUTH_SERVER + CodefPath.ISSUE_TOKEN);
            httpPost.addHeader(AUTHORIZATION, String.format(BASIC_TOKEN_FORMAT, codefOAuthToken));

            return httpClient.execute(httpPost, response -> {
                final String responseBody = EntityUtils.toString(response.getEntity());

                switch (response.getCode()) {
                    case 200:
                        break;
                    case 401:
                        throw CodefException.of(CodefError.OAUTH_UNAUTHORIZED, responseBody);
                    default:
                        throw CodefException.of(CodefError.OAUTH_INTERNAL_ERROR, responseBody);
                }

                return JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
            });
        } catch (CodefException exception) {
            throw exception;
        } catch (Exception exception) {
            throw CodefException.of(CodefError.OAUTH_CONNECTION_ERROR, exception);
        }
    }

    static EasyCodefResponse requestProduct(
            EasyCodefRequest request,
            EasyCodefToken token,
            String requestUrl
    ) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.addHeader(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, token.getAccessToken()));
            String rawRequest = JSON.toJSONString(request.requestParams());
            httpPost.setEntity(new StringEntity(rawRequest));

            return httpClient.execute(httpPost, response -> {
                String httpResponse = EntityUtils.toString(response.getEntity());
                String decodedResponse = URLDecoder.decode(httpResponse, StandardCharsets.UTF_8);

                final int responseStatusCode = response.getCode();

                if (responseStatusCode != HttpStatus.SC_OK) {
                    throw CodefException.of(CodefError.CODEF_API_SERVER_ERROR, decodedResponse);
                }

                JSONObject jsonResponseObject = JSON.parseObject(decodedResponse);

                EasyCodefResponse.Result resultResponse = jsonResponseObject.getJSONObject(RESULT)
                        .to(EasyCodefResponse.Result.class);

                Object dataResponse = jsonResponseObject.getJSONObject(DATA)
                        .to(Object.class);

                return new EasyCodefResponse(resultResponse, dataResponse);
            });
        } catch (CodefException exception) {
            throw exception;
        } catch (Exception exception) {
            throw CodefException.of(CodefError.INTERNAL_SERVER_ERROR, exception);
        }
    }
}
