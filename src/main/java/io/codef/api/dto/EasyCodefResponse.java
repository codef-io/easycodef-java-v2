package io.codef.api.dto;

public record EasyCodefResponse(
        Result result,
        Object data
) {

    public record Result(
            String code,
            String extraMessage,
            String message,
            String transactionId
    ) {
    }
}