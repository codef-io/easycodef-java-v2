package io.codef.api.facade;

import static io.codef.api.constants.CodefResponseCode.CF_03002;
import static io.codef.api.constants.CodefResponseCode.CF_12872;
import static io.codef.api.dto.EasyCodefRequest.TRUE;

import io.codef.api.constants.CodefResponseCode;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.vo.CodefSimpleAuth;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAuthCertFacade {

    private static final Logger log = LoggerFactory.getLogger(SimpleAuthCertFacade.class);

    private final SingleReqFacade singleReqFacade;
    private final SimpleAuthStorage simpleAuthStorage;
    private final MultipleRequestStorage multipleRequestStorage;

    public SimpleAuthCertFacade(SingleReqFacade singleReqFacade,
        SimpleAuthStorage simpleAuthStorage, MultipleRequestStorage multipleRequestStorage) {
        this.singleReqFacade = singleReqFacade;
        this.simpleAuthStorage = simpleAuthStorage;
        this.multipleRequestStorage = multipleRequestStorage;
    }

    public EasyCodefResponse requestSimpleAuthCertification(
        String transactionId
    ) throws CodefException {
        return executeCertification(transactionId);
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(
        String transactionId
    ) throws CodefException {
        EasyCodefResponse firstResponse = executeCertification(transactionId);

        return isSuccessful(firstResponse)
            ? combineWithRemainingResponses(firstResponse, transactionId)
            : returnFirstResponse(firstResponse);
    }


    private EasyCodefResponse executeCertification(String transactionId) {
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

    private void logResponseStatus(
        EasyCodefResponse response,
        String transactionId
    ) {
        String resultStatusCode = response.code();
        log.info("Result Status Code : {}", resultStatusCode);
        log.info("Transaction Id : {}", transactionId);

        if (resultStatusCode.equals(CF_03002)) {
            log.warn("The end user has not completed the additional authentication. "
                + "Recheck the end user's simple authentication status.");
        } else if (resultStatusCode.equals(CF_12872)) {
            log.warn("Transaction Id : {}", transactionId);
            log.warn("Retry limit for additional authentication exceeded. "
                + "Please restart the process from the initial request.");
        }
    }

    private List<EasyCodefResponse> returnFirstResponse(EasyCodefResponse firstErrorResponse) {
        return List.of(firstErrorResponse);
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

    private List<EasyCodefResponse> combineWithRemainingResponses(EasyCodefResponse firstResponse,
        String transactionId) throws CodefException {

        log.info("Await Responses called By transactionId `{}`", transactionId);
        List<EasyCodefResponse> awaitResponses = multipleRequestStorage.getRemainingResponses(transactionId);
        log.info("Await Responses Count = {}", awaitResponses.size());
        awaitResponses.add(firstResponse);
        List<String> allResponseCodes = awaitResponses.stream().map(EasyCodefResponse::code).toList();

        log.info("Total Responses Count = {}", awaitResponses.size());
        log.info("Result Status Codes : {}", allResponseCodes);

        return awaitResponses;
    }
}