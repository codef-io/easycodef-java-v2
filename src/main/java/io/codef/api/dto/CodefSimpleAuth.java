package io.codef.api.dto;

public record CodefSimpleAuth(
        String requestUrl,
        EasyCodefRequest request,
        EasyCodefResponse response
) {
}


