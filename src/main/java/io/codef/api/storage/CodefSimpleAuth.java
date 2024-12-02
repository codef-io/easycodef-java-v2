package io.codef.api.storage;

import io.codef.api.dto.EasyCodefRequest;
import io.codef.api.dto.EasyCodefResponse;

public record CodefSimpleAuth(
    String requestUrl,
    EasyCodefRequest request,
    EasyCodefResponse response
) {

}


