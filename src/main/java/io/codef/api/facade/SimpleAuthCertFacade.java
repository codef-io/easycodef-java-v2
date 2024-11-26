package io.codef.api.facade;

import static io.codef.api.constants.CodefResponseCode.CF_03002;
import static io.codef.api.constants.CodefResponseCode.CF_12872;
import static io.codef.api.dto.EasyCodefRequest.TRUE;

import com.alibaba.fastjson2.JSON;
import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.vo.CodefSimpleAuth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAuthCertFacade {

    private static final Logger log = LoggerFactory.getLogger(SimpleAuthCertFacade.class);

    private final SingleReqFacade singleReqFacade;
    private final SimpleAuthStorage simpleAuthStorage;
    private final MultipleRequestStorage multipleRequestStorage;

    public SimpleAuthCertFacade(
        SingleReqFacade singleReqFacade,
        SimpleAuthStorage simpleAuthStorage,
        MultipleRequestStorage multipleRequestStorage
    ) {
        this.singleReqFacade = singleReqFacade;
        this.simpleAuthStorage = simpleAuthStorage;
        this.multipleRequestStorage = multipleRequestStorage;
    }

    public EasyCodefResponse requestSimpleAuthCertification(String transactionId) throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);
        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);
        EasyCodefResponse response = singleReqFacade.requestProduct(enrichedRequest);

        simpleAuthStorage.updateIfRequired(
            simpleAuth.requestUrl(),
            enrichedRequest,
            response,
            transactionId
        );

        logResponseStatus(response, transactionId);
        return response;
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(
        String transactionId
    ) throws CodefException {
        EasyCodefResponse firstResponse = requestSimpleAuthCertification(transactionId);

        return isSuccessResponse(firstResponse)
            ? combineWithRemainingResponses(firstResponse, transactionId)
            : returnFirstResponse(firstResponse);
    }

    private void logAddAuthResponseStatus(
        EasyCodefResponse response,
        String transactionId,
        String resultStatusCode
    ) {
        if (resultStatusCode.equals(CF_03002)) {
            Object data = response.data();
            String addAuthMethod = JSON.parseObject(data.toString()).getString("method");

            log.warn("Additional authentication required | method : {}\n", addAuthMethod);
        } else if (resultStatusCode.equals(CF_12872)) {
            log.warn(
                "Retry limit for additional authentication exceeded. "
                + "Please restart the process from the initial request.\n"
            );
        }
    }

    private void logDefaultResponseStatus(String transactionId, String resultStatusCode) {
        log.info("Result Status Code : {}", resultStatusCode);
        log.info("Transaction Id : {}", transactionId);
    }

    private void logResponseStatus(
        EasyCodefResponse response,
        String transactionId
    ) {
        String resultStatusCode = response.code();
        logDefaultResponseStatus(transactionId, resultStatusCode);

        logAddAuthResponseStatus(response, transactionId, resultStatusCode);
    }

    private List<EasyCodefResponse> returnFirstResponse(EasyCodefResponse firstErrorResponse) {
        return List.of(firstErrorResponse);
    }

    private boolean isSuccessResponse(EasyCodefResponse response) {
        return CodefResponseCode.CF_00000.equals(response.code());
    }

    private boolean isAddAuthResponse(EasyCodefResponse response) {
        return CodefResponseCode.CF_03002.equals(response.code());
    }

    private boolean isFailureResponse(EasyCodefResponse response) {
        return !isSuccessResponse(response) && !isAddAuthResponse(response);
    }

    private EasyCodefRequest enrichRequestWithTwoWayInfo(CodefSimpleAuth simpleAuth) {
        EasyCodefRequest request = simpleAuth.request();
        Map<String, Object> body = request.requestBody();

        body.put(EasyCodefRequest.IS_TWO_WAY, true);
        body.put(EasyCodefRequest.SIMPLE_AUTH, TRUE);
        body.put(EasyCodefRequest.TWO_WAY_INFO, simpleAuth.response().data());

        return request;
    }

    private List<EasyCodefResponse> combineWithRemainingResponses(EasyCodefResponse firstResponse,
        String transactionId) throws CodefException {

        log.info("Await Responses called By transactionId `{}`", transactionId);
        List<EasyCodefResponse> responses = multipleRequestStorage.getRemainingResponses(
            transactionId);
        log.info("Await Responses Count = {}", responses.size());

        responses.add(firstResponse);
        List<String> allResponseCodes = responses.stream().map(EasyCodefResponse::code).toList();
        log.info("Total Responses Count = {}\n", responses.size());

        long successCount = responses.stream().filter(this::isSuccessResponse).count();
        long addAuthCount = responses.stream().filter(this::isAddAuthResponse).count();
        long failureCount = responses.stream().filter(this::isFailureResponse).count();

        log.info("Success Response Status [CF-00000] Count : {}", successCount);
        log.info("AddAuth Response Status [CF-03002] Count : {}", addAuthCount);
        log.warn("Failure Response Status [  Else  ] Count : {}", failureCount);

        if (failureCount > 0) {
            responses.stream()
                .filter(this::isFailureResponse)
                .map(EasyCodefResponse::code)
                .collect(Collectors.groupingBy(code -> code, Collectors.counting()))
                .forEach((code, count) -> log.warn("> Error code : {}, Count: {}", code, count));
        }

        return responses;
    }
}