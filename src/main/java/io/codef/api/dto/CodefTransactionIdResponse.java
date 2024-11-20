package io.codef.api.dto;

public record CodefTransactionIdResponse(
    String transactionId
) {

    public static CodefTransactionIdResponse from(String transactionId) {
        return new CodefTransactionIdResponse(transactionId);
    }
}
