package io.codef.api.dto;

import java.util.HashMap;

public class EasyCodefEncryptedRequest {
    private final HashMap<String, Object> request;

    protected EasyCodefEncryptedRequest(HashMap<String, Object> request) {
        this.request = request;
    }

    public HashMap<String, Object> getRequest() {
        return request;
    }
}
