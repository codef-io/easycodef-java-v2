package io.codef.api.facade;

import io.codef.api.ResponseHandler;
import io.codef.api.ResponseLogger;
import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.vo.CodefSimpleAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static io.codef.api.dto.EasyCodefRequest.TRUE;

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

    public EasyCodefResponse requestSimpleAuthCertification(String transactionId)
        throws CodefException {
        CodefSimpleAuth simpleAuth = simpleAuthStorage.get(transactionId);
        EasyCodefRequest enrichedRequest = enrichRequestWithTwoWayInfo(simpleAuth);
        EasyCodefResponse response = singleReqFacade.requestProduct(enrichedRequest);

        simpleAuthStorage.updateIfRequired(simpleAuth.requestUrl(), enrichedRequest, response);

        return response;
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(String transactionId)
        throws CodefException {
        EasyCodefResponse firstResponse = requestSimpleAuthCertification(transactionId);

        return ResponseHandler.isSuccessResponse(firstResponse)
            ? combineWithRemainingResponses(firstResponse, transactionId)
            : returnFirstResponse(firstResponse);
    }

    private List<EasyCodefResponse> returnFirstResponse(EasyCodefResponse firstErrorResponse) {
        return List.of(firstErrorResponse);
    }

    private EasyCodefRequest enrichRequestWithTwoWayInfo(CodefSimpleAuth simpleAuth) {
        EasyCodefRequest request = simpleAuth.request();
        Map<String, Object> body = request.requestBody();

        body.put(EasyCodefRequest.IS_TWO_WAY, true);
        body.put(EasyCodefRequest.SIMPLE_AUTH, TRUE);
        body.put(EasyCodefRequest.TWO_WAY_INFO, simpleAuth.response().data());

        return request;
    }

    private List<EasyCodefResponse> combineWithRemainingResponses(
        EasyCodefResponse firstResponse,
        String transactionId
    ) throws CodefException {
        List<EasyCodefResponse> responses = multipleRequestStorage.getRemainingResponses(transactionId);
        responses.add(firstResponse);
        ResponseLogger.logStatusSummary(responses);

        return responses;
    }
}