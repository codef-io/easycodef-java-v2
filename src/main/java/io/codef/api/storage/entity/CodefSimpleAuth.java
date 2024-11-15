package io.codef.api.storage.entity;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;

public class CodefSimpleAuth {
    private final String path;
    private final EasyCodefRequest request;
    private final EasyCodefResponse response;

    public CodefSimpleAuth(
            String path,
            EasyCodefRequest request,
            EasyCodefResponse response
    ) {
        this.path = path;
        this.request = request;
        this.response = response;
    }

    public String getPath() {
        return path;
    }

    public EasyCodefRequest getRequest() {
        return request;
    }

    public EasyCodefResponse getResponse() {
        return response;
    }
}


