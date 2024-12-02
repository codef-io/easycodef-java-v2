package io.codef.api;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.facade.MultipleReqFacade;
import io.codef.api.facade.SimpleAuthCertFacade;
import io.codef.api.facade.SingleReqFacade;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.util.RsaUtil;

import java.security.PublicKey;
import java.util.List;

public class EasyCodef {

    private final SingleReqFacade singleReqFacade;
    private final MultipleReqFacade multipleReqFacade;
    private final SimpleAuthCertFacade simpleAuthCertFacade;

    private final PublicKey publicKey;

    protected EasyCodef(EasyCodefBuilder easyCodefBuilder) {
        this.publicKey = RsaUtil.generatePublicKey(easyCodefBuilder.getPublicKey());

        EasyCodefToken easyCodefToken = new EasyCodefToken(easyCodefBuilder);
        SimpleAuthStorage simpleAuthStorage = new SimpleAuthStorage();
        MultipleRequestStorage multipleRequestStorage = new MultipleRequestStorage();
        CodefExecutorManager executorManager = CodefExecutorManager.create();

        this.singleReqFacade = new SingleReqFacade(
            easyCodefToken,
            simpleAuthStorage,
            easyCodefBuilder.getClientType()
        );

        this.multipleReqFacade = new MultipleReqFacade(
            singleReqFacade,
            multipleRequestStorage,
            executorManager
        );

        this.simpleAuthCertFacade = new SimpleAuthCertFacade(
            singleReqFacade,
            simpleAuthStorage,
            multipleRequestStorage
        );

        EasyCodefLogger.logInitializeSuccessfully();
    }

    public String encryptRSA(
            String requestParam
    ) throws CodefException {
        return RsaUtil.encryptRSA(requestParam, publicKey);
    }

    public EasyCodefResponse requestProduct(
            EasyCodefRequest request
    ) throws CodefException {
        return singleReqFacade.requestProduct(request);
    }

    public EasyCodefResponse requestMultipleProduct(
            List<EasyCodefRequest> requests
    ) throws CodefException {
        return multipleReqFacade.requestMultipleProduct(requests);
    }

    public EasyCodefResponse requestSimpleAuthCertification(
            String transactionId
    ) throws CodefException {
        return simpleAuthCertFacade.requestSimpleAuthCertification(transactionId);
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(
            String transactionId
    ) throws CodefException {
        return simpleAuthCertFacade.requestMultipleSimpleAuthCertification(transactionId);
    }
}