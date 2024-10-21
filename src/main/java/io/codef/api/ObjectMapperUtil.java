package io.codef.api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {

    private ObjectMapperUtil() {
    }

    private static class ObjectMapperInitializer {
        private static final ObjectMapper INSTANCE = new ObjectMapper();
    }

    public static ObjectMapper getInstance() {
        return ObjectMapperInitializer.INSTANCE;
    }
}
