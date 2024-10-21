package io.codef.api;


public class EasyCodef {

    private EasyCodefToken easyCodefToken;
    private EasyCodefProperty property;

    protected EasyCodef(
            EasyCodefToken easyCodefToken,
            EasyCodefProperty property
    ) {
        this.easyCodefToken = easyCodefToken;
        this.property = property;
    }
}