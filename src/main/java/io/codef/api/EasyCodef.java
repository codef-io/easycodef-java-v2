package io.codef.api;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefException;
import io.codef.api.storage.MultipleRequestStorage;
import io.codef.api.storage.SimpleAuthStorage;
import io.codef.api.util.RsaUtil;
import java.security.PublicKey;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyCodef {

    private static final Logger log = LoggerFactory.getLogger(EasyCodef.class);

    private final SingleProductRequestor singleProductRequestor;
    private final MultipleProductRequestor multipleProductRequestor;
    private final SimpleAuthRequestor simpleAuthRequestor;
    private final PublicKey publicKey;

    protected EasyCodef(EasyCodefBuilder builder) {
        this.publicKey = RsaUtil.generatePublicKey(builder.getPublicKey());

        EasyCodefToken easyCodefToken = new EasyCodefToken(builder);
        SimpleAuthStorage simpleAuthStorage = new SimpleAuthStorage();
        MultipleRequestStorage multipleRequestStorage = new MultipleRequestStorage();
        CodefExecutorManager executorManager = CodefExecutorManager.create();

        this.singleProductRequestor = new SingleProductRequestor(
            easyCodefToken,
            simpleAuthStorage,
            builder.getClientType()
        );

        this.multipleProductRequestor = new MultipleProductRequestor(
            singleProductRequestor,
            multipleRequestStorage,
            executorManager
        );

        this.simpleAuthRequestor = new SimpleAuthRequestor(
            singleProductRequestor,
            simpleAuthStorage,
            multipleRequestStorage
        );

        logInitializeSuccessfully();
    }

    public EasyCodefResponse requestProduct(EasyCodefRequest request) throws CodefException {
        return singleProductRequestor.requestProduct(request);
    }

    public EasyCodefResponse requestMultipleProduct(List<EasyCodefRequest> requests) throws CodefException {
        return multipleProductRequestor.requestMultipleProduct(requests);
    }

    public EasyCodefResponse requestSimpleAuthCertification(String transactionId) throws CodefException {
        return simpleAuthRequestor.requestSimpleAuthCertification(transactionId);
    }

    public List<EasyCodefResponse> requestMultipleSimpleAuthCertification(String transactionId) throws CodefException {
        return simpleAuthRequestor.requestMultipleSimpleAuthCertification(transactionId);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    private void logInitializeSuccessfully() {
        log.info("===================================");
        log.info("EasyCodef successfully initialized!");
        log.info("===================================\n");
    }
}