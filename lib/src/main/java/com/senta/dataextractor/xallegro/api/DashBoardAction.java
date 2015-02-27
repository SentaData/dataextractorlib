package com.senta.dataextractor.xallegro.api;

import android.content.Context;

import com.senta.dataextractor.ParametrizedData;

public class DashBoardAction implements ApiAction {

    private final ApiRequestParams apiRequestParams;

    public DashBoardAction(String username, String password, Context context) {
        final ParametrizedData data = new ParametrizedData(context, "");// context.getString(R.string.WsXmlXaPages));
        apiRequestParams = data.arrayDataForAuthentication(username, password);
    }

    @Override
    public String getBase64Xml() {
        return apiRequestParams.getStrCipherTextStrCTR();
    }

    @Override
    public String getHexKey() {
        return apiRequestParams.getHexSymmetricKey();
    }

    @Override
    public String getHexIv() {
        return apiRequestParams.getHexIv();
    }

    @Override
    public boolean wasSuccessful(String serverResponce) {
        return serverResponce != null && serverResponce.startsWith("<!DOCTYPE html>");
    }
}
