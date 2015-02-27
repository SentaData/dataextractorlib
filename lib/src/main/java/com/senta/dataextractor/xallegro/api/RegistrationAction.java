package com.senta.dataextractor.xallegro.api;

import android.content.Context;

import com.senta.dataextractor.StExtractStatic;

import java.util.List;

public class RegistrationAction implements ApiAction {

    private static final String MESSAGE_OK = "<WsXmlData><message>ok</message></WsXmlData>";
    private final ApiRequestParams requestParams;

    public RegistrationAction(String username, String password, String url, Context context) {
        StExtractStatic stExtractStatic = new StExtractStatic(context, url, username, password);
        List<String> param_list = stExtractStatic.createParamList();
        String XmlString = stExtractStatic.createXmlString(param_list, username, password);
        requestParams = StWsClient.getDeviceDataForAuthentication(context, param_list, XmlString);
    }

    @Override
    public String getBase64Xml() {
        return requestParams.getStrCipherTextStrCTR();
    }

    @Override
    public String getHexKey() {
        return requestParams.getHexSymmetricKey();
    }

    @Override
    public String getHexIv() {
        return requestParams.getHexIv();
    }

    @Override
    public boolean wasSuccessful(String serverResponce) {
        return MESSAGE_OK.equals(serverResponce);
    }
}
