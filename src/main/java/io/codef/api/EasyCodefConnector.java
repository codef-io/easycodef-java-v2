package io.codef.api;

import com.alibaba.fastjson2.JSON;
import io.codef.api.constants.CodefUri;
import io.codef.api.constants.CodefUrl;
import io.codef.api.constants.EasyCodefClientType;
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
        final String BASIC_TOKEN_FORMAT = BASIC + " %s";
        final String accessTokenParameter = "access_token";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(CodefUrl.CODEF_OAUTH_SERVER + CodefUri.ISSUE_TOKEN);
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
            String endpoint,
            EasyCodefClientType clientType
    ) {
        final String BEARER_TOKEN_FORMAT = BEARER + " %s";

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost httpPost = new HttpPost(CodefUrl.CODEF_API_DEMO + endpoint);
            httpPost.addHeader(AUTHORIZATION, String.format(BEARER_TOKEN_FORMAT, "token"));
            String rawRequest = JSON.toJSONString(request.requestParams());
            System.out.println("rawRequest = " + rawRequest);
            httpPost.setEntity(new StringEntity(rawRequest));

            return httpClient.execute(httpPost, response -> {
                String httpResponse = EntityUtils.toString(response.getEntity());
                String decodedResponse = URLDecoder.decode(httpResponse, "UTF-8");

                // TODO {"error":"invalid_token","error_description":"Cannot convert access token to JSON","code":"CF-09990","message":"OAUTH2.0 토큰 에러입니다. 메시지를 확인하세요."}
                // -> 형식에 맞지 않는 에러 반환(Status 검증 필요)
                System.out.println("decodedResponse = " + decodedResponse);
                EasyCodefResponse easyCodefResponse = JSON.parseObject(decodedResponse).to(EasyCodefResponse.class);
                System.out.println("data!!! = " + easyCodefResponse.getData());
                return easyCodefResponse;
            });
        } catch (CodefException exception) {
            throw exception;
        } catch (Exception exception) {
            throw CodefException.of(CodefError.OAUTH_CONNECTION_ERROR, exception);
        }
    }
}
