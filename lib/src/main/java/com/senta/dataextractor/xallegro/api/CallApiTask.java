package com.senta.dataextractor.xallegro.api;

import android.os.AsyncTask;
import android.util.Log;

import com.senta.dataextractor.HttpManager;
import com.senta.dataextractor.SecurityUtilities;
import com.senta.dataextractor.otto.BusProvider;
import com.senta.dataextractor.otto.events.ApiCallFinished;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class CallApiTask extends AsyncTask<Void, String, String> {

    private static final String TAG = CallApiTask.class.getName();
    private final String url;
    private static byte[] byteSymmetricKey;
    private final com.senta.dataextractor.xallegro.api.ApiAction apiAction;
    private HttpManager httpManager;

    public CallApiTask(ApiAction apiAction, String url) {
        this.apiAction = apiAction;
        this.url = url;
    }

    @Override
    protected String doInBackground(Void... unused) {
        final String hexKey = apiAction.getHexKey();
        byteSymmetricKey = SecurityUtilities.hexStringToByteArray(hexKey);
        String result = null;
        try {
            httpManager = new HttpManager();
            result = httpManager.performRequest(url, apiAction.getBase64Xml(), hexKey, apiAction.getHexIv());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return decodeAndDecrypt(result);
    }

    @Override
    protected void onPostExecute(String result) {
        Log.e(TAG, "Server Response:" + result);
        BusProvider.getInstance().post(new ApiCallFinished(result, apiAction.wasSuccessful(result)));
    }

    private String decodeAndDecrypt(String result) {
        return httpManager.decodeAndDecrypt(result, byteSymmetricKey, SecurityUtilities.IV);
    }
}