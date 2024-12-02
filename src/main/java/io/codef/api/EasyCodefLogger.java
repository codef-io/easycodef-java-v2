package io.codef.api;

import com.alibaba.fastjson2.JSON;
import io.codef.api.dto.EasyCodefResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static io.codef.api.constants.CodefResponseCode.*;

public class EasyCodefLogger {
    private static final Logger log = LoggerFactory.getLogger(EasyCodefLogger.class);

    private EasyCodefLogger() {
    }


    public static void logResponseStatus(EasyCodefResponse response) {
        logBasicInfo(response);

        switch (response.code()) {
            case CF_00000:
                logSuccessResponse();
                break;

            case CF_03002:
                logAddAuthRequired(response);
                break;

            case CF_12872:
                logAuthExceeded();
                break;

            default:
                logError(response);
                break;
        }
    }

    public static void logStatusSummary(List<EasyCodefResponse> responses) {
        long successCount = responses.stream().filter(ResponseHandler::isSuccessResponse).count();
        long addAuthCount = responses.stream().filter(ResponseHandler::isAddAuthResponse).count();
        long failureCount = responses.stream().filter(ResponseHandler::isFailureResponse).count();

        log.info("Total Responses Count = {}\n", responses.size());
        logStatus(successCount, addAuthCount, failureCount);
    }

    private static void logBasicInfo(EasyCodefResponse response) {
        log.info("Response Status Code : {}", response.code());
        log.info("Transaction Id : {}", response.transactionId());
    }

    private static void logSuccessResponse() {
        log.info("Successfully returned Value from Codef API\n");
    }

    private static void logAddAuthRequired(EasyCodefResponse response) {
        Object data = response.data();
        String addAuthMethod = JSON.parseObject(data.toString()).getString("method");
        log.warn("Additional authentication required | method : {}\n", addAuthMethod);
    }

    private static void logAuthExceeded() {
        log.error("Retry limit for additional authentication exceeded. "
                + "Please restart the process from the initial request.\n");
    }

    private static void logError(EasyCodefResponse response) {
        log.error("Failed to get proper scrapping response. Check the Error errorMessage And StatusCode");
        log.error("> message : {}", response.result().message());
        log.error("> extraMessage : {}", response.result().extraMessage());
    }

    private static void logStatus(
            long successCount,
            long addAuthCount,
            long failureCount
    ) {
        Optional.of(successCount)
                .filter(EasyCodefLogger::isExist)
                .ifPresent(count -> log.info("Success Response Status [ {} ] Count : {}", CF_00000, count));

        Optional.of(addAuthCount)
                .filter(EasyCodefLogger::isExist)
                .ifPresent(count -> log.info("AddAuth Response Status [ {} ] Count : {}", CF_03002, count));

        Optional.of(failureCount)
                .filter(EasyCodefLogger::isExist)
                .ifPresentOrElse(
                        count -> log.warn("Failure Response Status [   Else   ] Count : {}\n", count),
                        () -> log.info("No Failure Responses\n")
                );
    }

    private static boolean isExist(Long count) {
        return count > 0;
    }

    static void logInitializeSuccessfully() {
        log.info("""
            
            
            ------.                        ,-----.          ,--.       ,---.\s
            |  .---' ,--,--. ,---.,--. ,--.'  .--./ ,---.  ,-|  |,---. /  .-'\s
            |  `--, ' ,-.  |(  .-' \\  '  / |  |    | .-. |' .-. | .-. :|  `-,\s
            |  `---.\\ '-'  |.-'  `) \\   '  '  '--'\\' '-' '\\ `-' \\   --.|  .-'\s
            `------' `--`--'`----'.-'  /    `-----' `---'  `---' `----'`--'     \s
            
            > EasyCodef v2.0.0-beta-005 Successfully Initialized! Hello worlds!
            """
        );
    }
}
