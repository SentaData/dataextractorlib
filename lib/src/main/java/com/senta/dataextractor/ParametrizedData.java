package com.senta.dataextractor;

import android.content.Context;

import com.senta.dataextractor.xallegro.api.ApiRequestParams;
import com.senta.dataextractor.xallegro.api.StWsClient;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;

public class ParametrizedData {

    private static final String TAG = ParametrizedData.class.getName();
    private Context MyContext;
    private String xmlStructure;
    private StWsClient wsClient = new StWsClient();

    public ParametrizedData(Context _MyContext, String _DynamicStructureXml) {
        this.MyContext = _MyContext;
        this.xmlStructure = _DynamicStructureXml;
    }

    private String createXmlRequest(String username, String password, String object, String event, List<NameValuePair> params) {
        Document dataDoc;
        dataDoc = docAddUsernamePass(username, password);
        dataDoc = docAddObjectEvent(object, event, dataDoc);
        dataDoc = docAddParams(params, dataDoc);
        return wsClient.getXmlToString(dataDoc);
    }

    private Document docAddParams(List<NameValuePair> params, Document dataDoc) {
        if (params != null) {
            for (NameValuePair param : params) {
                String[] ElementNameArray = {"name", "value"};
                String[] El = {param.getName(), param.getValue()};
                dataDoc = wsClient.AddParamElementByXPath(dataDoc, "/WsXmlData/params", ElementNameArray, El);
            }
        }
        return dataDoc;
    }

    private Document docAddObjectEvent(String object, String event, Document dataDoc) {
        if (object != null && event != null) {
            dataDoc = wsClient.AddElementByXPath(dataDoc, "/WsXmlData/operation/object", object);
            dataDoc = wsClient.AddElementByXPath(dataDoc, "/WsXmlData/operation/event", event);
        }
        return dataDoc;
    }

    private Document docAddUsernamePass(String username, String password) {
        Document dataDoc;
        dataDoc = StWsClient.createXmlDoc(xmlStructure);
        dataDoc = wsClient.AddElementByXPath(dataDoc, "/WsXmlData/login/username", username);
        dataDoc = wsClient.AddElementByXPath(dataDoc, "/WsXmlData/login/password", password);
        return dataDoc;
    }


    public ApiRequestParams arrayDataForAuthentication(String username, String password, String object, String event, List<NameValuePair> params) {
        String xmlString = createXmlRequest(username, password, object, event, params);
        List<String> paramList = nameValuePairListToStringList(params);
        return StWsClient.getDeviceDataForAuthentication(MyContext, paramList, xmlString);
    }

    private List<String> nameValuePairListToStringList(List<NameValuePair> params) {
        List<String> result = new ArrayList<>();
        if (params != null) {
            for (NameValuePair param : params) {
                result.add(param.getName() + ", " + param.getValue());
            }
        }
        return result;
    }


    public ApiRequestParams arrayDataForAuthentication(String username, String password) {
        String xmlString = createXmlRequest(username, password, null, null, null);
        return StWsClient.getDeviceDataForAuthentication(MyContext, new ArrayList<String>(), xmlString);
    }
}
