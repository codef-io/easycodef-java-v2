package io.codef.api;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import static org.apache.hc.client5.http.auth.StandardAuthScheme.BASIC;
import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

final class EasyCodefConnector {

    static String issueToken(String codefOAuthToken) {
        final String BASIC_TOKEN_FORMAT = BASIC + " %s";

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
                return ObjectMapperUtil.getInstance().readTree(httpResponse).path("access_token").asText();
            });
        } catch (Exception exception) {
            throw CodefException.of(CodefError.OAUTH_CONNECTION_ERROR, exception);
        }
    }
}
