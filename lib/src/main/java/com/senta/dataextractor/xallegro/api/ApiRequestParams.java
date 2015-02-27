package com.senta.dataextractor.xallegro.api;

public class ApiRequestParams {

    private final String strCipherTextStrCTR;
    private final String hexSymmetricKey;
    private final String hexIv;

    public ApiRequestParams(String strCipherTextStrCTR, String hexSymmetricKey, String hexIv) {
        this.strCipherTextStrCTR = strCipherTextStrCTR;
        this.hexSymmetricKey = hexSymmetricKey;
        this.hexIv = hexIv;
    }

    public String getStrCipherTextStrCTR() {
        return strCipherTextStrCTR;
    }

    public String getHexSymmetricKey() {
        return hexSymmetricKey;
    }

    public String getHexIv() {
        return hexIv;
    }
}
