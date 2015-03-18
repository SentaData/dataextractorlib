package com.senta.dataextractor;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpManager {

    private static final String TAG = HttpManager.class.getName();
    private HttpClient httpClient = new DefaultHttpClient();

    public HttpManager() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        }, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    public String performRequest(String UrlString, String Base64Xml, String HexKey, String HexIv) {

        HttpPost httpPost = new HttpPost(UrlString);
        HttpEntity httpEntity;
        HttpResponse response;
        String return_response = "";
        try {
            List<NameValuePair> values = new ArrayList<>();
            values.add(new BasicNameValuePair("WsXml", "yes"));
            values.add(new BasicNameValuePair("Encoding", "B64"));
            values.add(new BasicNameValuePair("WsKey", HexKey));
            values.add(new BasicNameValuePair("WsIV", HexIv));
            values.add(new BasicNameValuePair("WsXmlData", Base64Xml));
            httpPost.setEntity(new UrlEncodedFormEntity(values));
            response = httpClient.execute(httpPost);
            httpEntity = response.getEntity();
            return_response = EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return return_response;
    }


    public String decodeAndDecrypt(String FileInXml, byte[] SymmetricKey, byte[] IV) {

        String resultNoSpaces = FileInXml.replaceAll("\\s", "");
        String clearTextStrCTR = null;
        try {
            byte[] bCipherTextStrCTR = Base64.decode(resultNoSpaces, Base64.NO_WRAP);
            byte[] bPlainTextStrCTR = SecurityUtilities.aesDecryptCTR(bCipherTextStrCTR, SymmetricKey, IV);
            clearTextStrCTR = SecurityUtilities.byteArrayToString(bPlainTextStrCTR);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "decodeAndDecrypt IllegalArgumentException: " + e.getMessage());
        }
        Log.d(TAG, "Decrypted message from server: " + clearTextStrCTR);
        return clearTextStrCTR;
    }
}
