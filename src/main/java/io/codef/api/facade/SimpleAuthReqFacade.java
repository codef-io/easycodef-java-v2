package io.codef.api.facade;

import static io.codef.api.constants.CodefResponseCode.CF_03002;
import static io.codef.api.constants.CodefResponseCode.CF_12872;
import static io.codef.api.dto.EasyCodefRequest.TRUE;

import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.CodefSimpleAuth;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAuthReqFacade {

    private static final Logger log = LoggerFactory.getLogger(SimpleAuthReqFacade.class);
    private final SingleReqFacade singleReqFacade;
    private final SimpleAuthStorage simpleAuthStorage;
    private final MultipleRequestStorage multipleRequestStorage;

    public SimpleAuthReqFacade(
        SingleReqFacade singleReqFacade,
        SimpleAuthStorage simpleAuthStorage,
        MultipleRequestStorage multipleRequestStorage
    ) {
        this.singleReqFacade = singleReqFacade;
        this.simpleAuthStorage = simpleAuthStorage;
        this.multipleRequestStorage = multipleRequestStorage;
    }

    public EasyCodefResponse requestSimpleAuthCertification(String transactionId)
        throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);
        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);

        EasyCodefResponse response = singleReqFacade.requestProduct(enrichedRequest);

        simpleAuthStorage.updateIfRequired(
            simpleAuth.requestUrl(),
            enrichedRequest,
            response,
            transactionId
        );

        handleResponseStatus(response, transactionId);
        return response;
    }

    private void handleResponseStatus(EasyCodefResponse response, String transactionId) {
        String resultStatusCode = response.code();
        log.info("Result Status Code : {}", resultStatusCode);

        if (resultStatusCode.equals(CF_03002)) {
            log.warn("Transaction Id : {}", transactionId);
            log.warn(
                "The end user has not completed the additional authentication. " +
                    "Recheck the end user's simple authentication status."
            );
        } else if (resultStatusCode.equals(CF_12872)) {
            log.warn("Transaction Id : {}", transactionId);
            log.warn(
                "Retry limit for additional authentication exceeded. " +
                    "Please restart the process from the initial request."
            );
        }
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(String transactionId)
        throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);
        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);

        EasyCodefResponse firstResponse =
            singleReqFacade.requestProduct(enrichedRequest);

        simpleAuthStorage.updateIfRequired(
            simpleAuth.requestUrl(),
            enrichedRequest,
            firstResponse,
            transactionId
        );

        return isSuccessful(firstResponse)
            ? combineWithRemainingResponses(firstResponse, transactionId)
            : returnFirstErrorResponse(firstResponse, transactionId);
    }

    private boolean isSuccessful(EasyCodefResponse response) {
        return CodefResponseCode.CF_00000.equals(response.code());
    }

    private EasyCodefRequest enrichRequestWithTwoWayInfo(CodefSimpleAuth simpleAuth) {
        EasyCodefRequest request = simpleAuth.request();
        Map<String, Object> body = request.requestBody();

        body.put(EasyCodefRequest.IS_TWO_WAY, true);
        body.put(EasyCodefRequest.SIMPLE_AUTH, TRUE);
        body.put(EasyCodefRequest.TWO_WAY_INFO, simpleAuth.response().data());

        return request;
    }

    private List<EasyCodefResponse> returnFirstErrorResponse(
        EasyCodefResponse firstResponse,
        String transactionId
    ) {
        String resultStatusCode = firstResponse.code();
        log.info("Result Status Code : {}", resultStatusCode);
        if (resultStatusCode.equals(CF_03002)) {
            log.warn("Transaction Id : {}", transactionId);
            log.warn(
                "The end user has not completed the additional authentication. Recheck the end user's simple authentication status."
            );
        } else if (resultStatusCode.equals(CF_12872)) {
            log.warn("Transaction Id : {}", transactionId);
            log.warn(
                " Retry limit for additional authentication exceeded. Please restart the process from the initial request."
            );
        }
        return List.of(firstResponse);
    }


    private List<EasyCodefResponse> combineWithRemainingResponses(
        EasyCodefResponse firstResponse,
        String transactionId
    ) throws CodefException {

        log.info("Await Responses called By transactionId `{}`", transactionId);

        List<EasyCodefResponse> remainingResponses =
            multipleRequestStorage.getRemainingResponses(transactionId);

        log.info("Await Responses Count = {}", remainingResponses.size());
        List<EasyCodefResponse> allResponses = new ArrayList<>(remainingResponses);
        allResponses.add(firstResponse);

        log.info("Total Responses Count = {}", allResponses.size());
        log.info("Result Status Codes : {}",
            allResponses.stream().map(EasyCodefResponse::code).toList());

        return allResponses;
    }
}