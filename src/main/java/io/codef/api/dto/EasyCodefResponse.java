package io.codef.api.dto;

public record EasyCodefResponse(
        Result result,
        Object data
) {
    public static final String RESULT = "result";
    public static final String DATA = "data";

    public record Result(
            String code,
            String extraMessage,
            String message,
            String transactionId
    ) {
    }
}