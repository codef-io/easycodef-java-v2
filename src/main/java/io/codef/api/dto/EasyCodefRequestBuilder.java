package io.codef.api.dto;

import io.codef.api.CodefValidator;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static io.codef.api.dto.EasyCodefRequest.EASY_CODEF_JAVA_FLAG;
import static io.codef.api.dto.EasyCodefRequest.PATH_PREFIX;

public class EasyCodefRequestBuilder {
    private static final Logger log = LoggerFactory.getLogger(EasyCodefRequestBuilder.class);

    private final Map<String, Object> requestBody;
    private String path;

    private EasyCodefRequestBuilder() {
        this.requestBody = new HashMap<>();
    }

    public static EasyCodefRequestBuilder builder() {
        return new EasyCodefRequestBuilder();
    }

    private static void requireValidPathElseThrow(String path) {
        Optional.of(path)
                .filter(p -> p.startsWith(PATH_PREFIX))
                .orElseThrow(() -> CodefException.from(CodefError.INVALID_PATH_REQUESTED));
    }


    public EasyCodefRequestBuilder path(String path) {
        this.path = path;
        requireValidPathElseThrow(path);
        return this;
    }

    public EasyCodefRequestBuilder requestBody(String param, Object value) {
        requestBody.put(param, value);
        return this;
    }

    public EasyCodefRequestBuilder requestObject(
            String key,
            Consumer<Map<String, Object>> objectBuilder
    ) {
        CodefValidator.requireNonNullElseThrow(key, CodefError.REQUEST_NULL);

        Map<String, Object> object = new HashMap<>();
        objectBuilder.accept(object);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objectList =
                (List<Map<String, Object>>) requestBody.computeIfAbsent(key, k -> new ArrayList<>());

        objectList.add(object);
        return this;
    }

    public EasyCodefRequest build() {
        CodefValidator.requireNonNullElseThrow(path, CodefError.NEED_TO_PATH_METHOD);
        this.requestBody(EASY_CODEF_JAVA_FLAG, true);

        return new EasyCodefRequest(path, requestBody);
    }
}