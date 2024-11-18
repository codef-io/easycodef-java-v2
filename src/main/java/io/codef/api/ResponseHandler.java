package io.codef.api;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.codef.api.dto.EasyCodefResponse;
import io.codef.api.error.CodefError;
import io.codef.api.error.CodefException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static io.codef.api.dto.EasyCodefRequest.ACCESS_TOKEN;
import static io.codef.api.dto.EasyCodefResponse.DATA;
import static io.codef.api.dto.EasyCodefResponse.RESULT;

public class ResponseHandler {
    public ResponseHandler() {
    }

    public String handleTokenResponse(ClassicHttpResponse response) {
        try {
            final String responseBody = EntityUtils.toString(response.getEntity());
            final int httpStatusCode = response.getCode();

            return switch (httpStatusCode) {
                case HttpStatus.SC_OK -> parseAccessToken(responseBody);
                case HttpStatus.SC_UNAUTHORIZED -> throw CodefException.of(CodefError.OAUTH_UNAUTHORIZED, responseBody);
                default -> throw CodefException.of(CodefError.OAUTH_INTERNAL_ERROR, responseBody);
            };
        } catch (IOException exception) {
            throw CodefException.of(CodefError.IO_ERROR, exception);
        } catch (ParseException exception) {
            throw CodefException.of(CodefError.PARSE_ERROR, exception);
        }
    }

    public EasyCodefResponse handleProductResponse(
            ClassicHttpResponse response
    ) throws CodefException {
        try {
            final String httpResponse = EntityUtils.toString(response.getEntity());
            final int httpStatusCode = response.getCode();

            return switch (httpStatusCode) {
                case HttpStatus.SC_OK -> {
                    final var decodedResponse = URLDecoder.decode(httpResponse, StandardCharsets.UTF_8);
                    yield parseProductResponse(decodedResponse);
                }
                case HttpStatus.SC_UNAUTHORIZED -> throw CodefException.of(CodefError.API_UNAUTHORIZED, httpResponse);
                default -> throw CodefException.of(CodefError.CODEF_API_SERVER_ERROR, httpResponse);
            };
        } catch (IOException exception) {
            throw CodefException.of(CodefError.IO_ERROR, exception);
        } catch (ParseException exception) {
            throw CodefException.of(CodefError.PARSE_ERROR, exception);
        }
    }


    private String parseAccessToken(String responseBody) {
        return JSON.parseObject(responseBody).getString(ACCESS_TOKEN);
    }

    private EasyCodefResponse parseProductResponse(String decodedResponse) {
        JSONObject jsonResponseObject = JSON.parseObject(decodedResponse);

        EasyCodefResponse.Result resultResponse = jsonResponseObject.getJSONObject(RESULT).to(EasyCodefResponse.Result.class);

        Object dataResponse = jsonResponseObject.getJSONObject(DATA).to(Object.class);

        return new EasyCodefResponse(resultResponse, dataResponse);
    }
}