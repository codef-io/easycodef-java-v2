package io.codef.api;

public enum EasyCodefReferenceUrl {
    KEY("https://codef.io/account/keys"),
    DEV_GUIDE_REST_API("https://developer.codef.io/common-guide/rest-api");

    private final String url;

    EasyCodefReferenceUrl(String url) {
        this.url = url;
    }

    private static final String MESSAGE_FORMAT = "→ For detailed information, please visit '%s'";

    public String getUrl() {
        return String.format(MESSAGE_FORMAT, url);
    }
}
