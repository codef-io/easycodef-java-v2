package io.codef.api.constants;

public enum CodefClientType {
    API(CodefHost.CODEF_API),
    DEMO(CodefHost.CODEF_API_DEMO);

    private final String host;

    CodefClientType(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }
}
