package io.codef.api.constants;

public enum EasyCodefClientType {
    API("https://api.codef.io"),
    DEMO("https://development.codef.io");

    private final String uri;

    EasyCodefClientType(String uri) {
        this.uri = uri;
    }
}
