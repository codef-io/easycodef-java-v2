package io.codef.api.constants;

public enum CodefReferenceUrl {
    KEY("https://codef.io/account/keys"),
    DEV_GUIDE_REST_API("https://developer.codef.io/common-guide/rest-api"),
    GITHUB("https://github.com/codef-io/easycodef-java-v2"),
    PRODUCT("https://developer.codef.io/product/api"),
    TECH_INQUIRY("https://codef.io/cs/inquiry"),
    MULTIPLE_REQUEST("https://developer.codef.io/common-guide/multiple-requests"),

    WIKI_003("https://github.com/codef-io/easycodef-java-v2/wiki/003.-%EC%83%81%ED%92%88-%EC%9A%94%EC%B2%AD-%EA%B0%9D%EC%B2%B4-%EC%83%9D%EC%84%B1");

    private static final String MESSAGE_FORMAT = "→ For detailed information, please visit '%s'";
    private final String url;

    CodefReferenceUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return String.format(MESSAGE_FORMAT, url);
    }
}
