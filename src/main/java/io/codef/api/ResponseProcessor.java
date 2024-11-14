package io.codef.api;

import io.codef.api.error.CodefException;
import org.apache.hc.core5.http.ClassicHttpResponse;

@FunctionalInterface
public interface ResponseProcessor<T> {
    T process(ClassicHttpResponse response) throws CodefException;
}