package io.codef.api.util;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;

public final class HttpClientUtil {
    private HttpClientUtil() {
    }

    public static CloseableHttpClient createClient() {
        return HttpClientBuilder.create().build();
    }
}
