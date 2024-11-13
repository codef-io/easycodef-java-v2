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
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.net.URLDecoder;

import static org.apache.hc.client5.http.auth.StandardAuthScheme.BASIC;
import static org.apache.hc.client5.http.auth.StandardAuthScheme.BEARER;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

final class EasyCodefConnector {

    static String issueToken(String codefOAuthToken) {
        System.out.println("issue Token !!!\n\n");
        final String BASIC_TOKEN_FORMAT = BASIC + " %s";
        final String accessTokenParameter = "access_token";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(CodefHost.CODEF_OAUTH_SERVER + CodefPath.ISSUE_TOKEN);
            httpPost.addHeader(AUTHORIZATION, String.format(BASIC_TOKEN_FORMAT, codefOAuthToken));

            return httpClient.execute(httpPost, response -> {
                switch (response.getCode()) {
                    case 200:
                        break;
                    case 401:
                        throw CodefException.from(CodefError.OAUTH_UNAUTHORIZED);
                    case 500:
                    default:
                        throw CodefException.from(CodefError.OAUTH_INTERNAL_ERROR);
                }

                String httpResponse = EntityUtils.toString(response.getEntity());

                return JSON.parseObject(httpResponse).getString(accessTokenParameter);
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
        final String BEARER_TOKEN_FORMAT = BEARER + " %s";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(requestUrl);
            httpPost.addHeader(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, token.getAccessToken()));
            String rawRequest = JSON.toJSONString(request.requestParams());
            httpPost.setEntity(new StringEntity(rawRequest));

            return httpClient.execute(httpPost, response -> {
                String httpResponse = EntityUtils.toString(response.getEntity());
                String decodedResponse = URLDecoder.decode(httpResponse, "UTF-8");

                // TODO {"error":"invalid_token","error_description":"Cannot convert access token to JSON","code":"CF-09990","message":"OAUTH2.0 토큰 에러입니다. 메시지를 확인하세요."}
                JSONObject jsonResponseObject = JSON.parseObject(decodedResponse);

                EasyCodefResponse.Result resultResponse = jsonResponseObject.getJSONObject("result").to(EasyCodefResponse.Result.class);
                Object dataResponse = jsonResponseObject.getJSONObject("data").to(Object.class);
                return new EasyCodefResponse(resultResponse, dataResponse);
            });
        } catch (CodefException exception) {
            throw exception;
        } catch (Exception exception) {
            throw CodefException.of(CodefError.OAUTH_CONNECTION_ERROR, exception);
        }
    }
}
