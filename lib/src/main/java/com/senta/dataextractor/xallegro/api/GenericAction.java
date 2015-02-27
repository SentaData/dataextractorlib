package com.senta.dataextractor.xallegro.api;

import android.content.Context;

import com.senta.dataextractor.ParametrizedData;
import com.senta.dataextractor.R;

import org.apache.http.NameValuePair;

import java.util.List;

public class GenericAction implements ApiAction {

    private ApiRequestParams apiRequestParams;

    public GenericAction(String username, String password, String object, String event, List<NameValuePair> params, Context context) {
        final ParametrizedData data = new ParametrizedData(context, context.getString(R.string.WsGenericOperation));
        apiRequestParams = data.arrayDataForAuthentication(username, password, object, event, params);
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
        return serverResponce != null && !serverResponce.contains("<error>");
    }
}
