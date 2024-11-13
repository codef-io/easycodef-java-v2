package io.codef.api.dto;

import java.util.HashMap;

public record EasyCodefRequest(
        String path,
        HashMap<String, Object> requestParams
) {
}