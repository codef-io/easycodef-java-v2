package io.codef.api;

public enum EasyCodefServiceType {
    API("https://api.codef.io"),
    DEMO("https://development.codef.io");

    private final String uri;

    EasyCodefServiceType(String uri) {
        this.uri = uri;
    }
}
