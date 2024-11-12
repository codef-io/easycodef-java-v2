package io.codef.api.dto;

import com.alibaba.fastjson2.JSONObject;

import java.util.HashMap;

public record EasyCodefResponse(
        Result result,
        JSONObject data
) {

    public record Result(
            String code,
            String extraMessage,
            String message,
            String transactionId
    ) {
    }

    public HashMap<String, Object> getData() {
        return new HashMap<>(data);
    }
}