package com.senta.dataextractor.xallegro.api;

public interface ApiAction {
    String getBase64Xml();

    String getHexKey();

    String getHexIv();

    boolean wasSuccessful(String serverResponce);
}
