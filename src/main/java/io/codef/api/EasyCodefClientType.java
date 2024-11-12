package io.codef.api;

public enum EasyCodefClientType {
    API("https://api.codef.io"),
    DEMO("https://development.codef.io");

    private final String uri;

    EasyCodefClientType(String uri) {
        this.uri = uri;
    }
}
