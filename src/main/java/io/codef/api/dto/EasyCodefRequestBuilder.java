package io.codef.api.dto;

import io.codef.api.EasyCodef;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.util.RsaUtil;

import java.util.HashMap;

public class EasyCodefRequestBuilder {

    private final HashMap<String, Object> generalRequestBody;
    private final HashMap<String, String> secureRequestBody;
    private String path;
    private EasyCodef easyCodef;

    private EasyCodefRequestBuilder() {
        this.generalRequestBody = new HashMap<>();
        this.secureRequestBody = new HashMap<>();
    }

    public static EasyCodefRequestBuilder builder() {
        return new EasyCodefRequestBuilder();
    }

    public EasyCodefRequestBuilder organization(Object value) {
        generalRequestBody.put("organization", value);
        return this;
    }

    public EasyCodefRequestBuilder path(String path) {
        this.path = path;

        if(!path.startsWith("/v1")) {
            throw CodefException.from(CodefError.INVALID_PATH_REQUESTED);
        }
        return this;
    }

    public EasyCodefRequestBuilder requestBody(
            String param,
            Object value
    ) {
        generalRequestBody.put(param, value);
        return this;
    }

    public EasyCodefRequestBuilder secureRequestBody(
            String param,
            String value
    ) {
        secureRequestBody.put(param, value);
        return this;
    }

    public EasyCodefRequestBuilder secureWith(EasyCodef easyCodef) {
        this.easyCodef = easyCodef;
        return this;
    }

    public EasyCodefRequest build() {
        final HashMap<String, Object> requests = new HashMap<>();
        final String EASY_CODEF_JAVA_FLAG = "easyCodefJavaV2";

        if (!secureRequestBody.isEmpty()) {
            if (easyCodef == null) {
                throw CodefException.from(CodefError.NEED_TO_SECURE_WITH_METHOD);
            } else {
                secureRequestBody.forEach((key, value) -> {
                    String encryptedValue = RsaUtil.encryptRSA(value, easyCodef.getPublicKey());
                    secureRequestBody.put(key, encryptedValue);
                });
            }
        }

        if (path == null) {
            throw CodefException.from(CodefError.NEED_TO_PATH_METHOD);
        }

        if (generalRequestBody.get("organization") == null) {
            throw CodefException.from(CodefError.NEED_TO_ORGANIZATION_METHOD);
        }

        this.requestBody(EASY_CODEF_JAVA_FLAG, true);
        this.generalRequestBody.putAll(secureRequestBody);
        return new EasyCodefRequest(path, generalRequestBody);
    }
}
