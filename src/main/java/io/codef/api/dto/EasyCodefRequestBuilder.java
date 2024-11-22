package io.codef.api.dto;

import static io.codef.api.dto.EasyCodefRequest.EASY_CODEF_JAVA_FLAG;
import static io.codef.api.dto.EasyCodefRequest.ORGANIZATION;
import static io.codef.api.dto.EasyCodefRequest.PATH_PREFIX;

import io.codef.api.CodefValidator;
import io.codef.api.EasyCodef;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import io.codef.api.util.RsaUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyCodefRequestBuilder {

    private static final Logger log = LoggerFactory.getLogger(EasyCodefRequestBuilder.class);

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

    private static void requireValidPathElseThrow(String path) {
        Optional.of(path)
            .filter(p -> p.startsWith(PATH_PREFIX))
            .orElseThrow(() -> CodefException.from(CodefError.INVALID_PATH_REQUESTED));
    }

    public EasyCodefRequestBuilder organization(Object value) {
        CodefValidator.requireNonNullElseThrow(value, CodefError.NULL_ORGANIZATION);
        generalRequestBody.put(ORGANIZATION, value);
        return this;
    }

    public EasyCodefRequestBuilder path(String path) {
        this.path = path;
        requireValidPathElseThrow(path);

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
        CodefValidator.requireNonNullElseThrow(path, CodefError.NEED_TO_PATH_METHOD);
        CodefValidator.requireNonNullElseThrow(
            generalRequestBody.get(ORGANIZATION), CodefError.NEED_TO_ORGANIZATION_METHOD
        );

        encryptSecureRequestBody();

        this.requestBody(EASY_CODEF_JAVA_FLAG, true);
        this.generalRequestBody.putAll(secureRequestBody);

        EasyCodefRequest easyCodefRequest = new EasyCodefRequest(path, generalRequestBody);

//        log.info("[EasyCodef] request object has been successfully built [ {} ]");
//        log.info(">> path = {}", path);
//        log.info(">> requestBody = {}", generalRequestBody);

        return easyCodefRequest;
    }

    private void encryptSecureRequestBody() {
        Optional.of(secureRequestBody)
            .filter(body -> !body.isEmpty())
            .ifPresent(body -> {
                CodefValidator.requireNonNullElseThrow(
                    easyCodef,
                    CodefError.NEED_TO_SECURE_WITH_METHOD
                );
                encryptRequestBodyValues(body);
            });
    }

    private void encryptRequestBodyValues(Map<String, String> body) {
        body.replaceAll((key, value) -> RsaUtil.encryptRSA(value, easyCodef.getPublicKey()));
    }
}
