package com.senta.dataextractor;

/**
 * Created by alex on 14/10/2014.
 */

import android.content.Context;
import android.util.Log;

import com.senta.dataextractor.parameters.ParamSensors;
import com.senta.dataextractor.parameters.ParamStaticPhone;
import com.senta.dataextractor.xallegro.api.ApiRequestParams;
import com.senta.dataextractor.xallegro.api.StWsClient;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * Created by alex on 14/10/2014.
 */
public class StExtractStatic implements Runnable {

    private static final String TAG = StExtractStatic.class.getName();
    private final StWsClient stWsClient = new StWsClient();

    private final ParamStaticPhone objStaticPhone = new ParamStaticPhone();
    private final ParamSensors objSensor = new ParamSensors();
    private final ParamProcesses objProcess = new ParamProcesses();
    private final String username;
    private final String password;
    private HashSet<String> hs = new HashSet<String>();

    private Context MyContext;
    private String Url;


    public StExtractStatic(Context _MyContext, String _Url, String username, String password) {
        this.MyContext = _MyContext;
        this.Url = _Url;
        this.username = username;
        this.password = password;
    }


    @Override
    public void run() {
        ArrayList<String> param_list = createParamList();

        String XmlString = createXmlString(param_list, username, password);

        Log.d(TAG, "Senta-Secure-StaticParam" + XmlString);
        ApiRequestParams params = StWsClient.getDeviceDataForAuthentication(MyContext, param_list, XmlString);

        String ServerResponse = stWsClient.DeviceRegistration(Url, params.getStrCipherTextStrCTR(), params.getHexSymmetricKey(), params.getHexIv());

        if (ServerResponse.contains("<root><message>ok</message></root>")) {
            Log.d("PlainResponse", "in static data");
        }
    }


    public String createXmlString(List<String> param_list, String username, String password) {
        String xmlRequest = MyContext.getResources().getString(R.string.WsXmlStParams);
        Document doc = stWsClient.createXmlDoc(xmlRequest);

        doc = stWsClient.AddElementByXPath(doc, "/WsXmlData/login/username", username);
        doc = stWsClient.AddElementByXPath(doc, "/WsXmlData/login/password", password);

        String[] ElementNameArray = {"name", "value"};
        String[] ElementValueArray;

        for (int i = 0; i < param_list.size(); i++) {
            ElementValueArray = param_list.get(i).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            doc = stWsClient.AddParamElementByXPath(doc, "/WsXmlData/params", ElementNameArray, El);
        }
        ArrayList<String> sensor_list = objSensor.getParamSensors(MyContext);
        ArrayList<String> static_process_list = objProcess.getStaticCPUInfo();

        for (int i = 0; i < sensor_list.size(); i++) {
            ElementValueArray = sensor_list.get(i).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            doc = stWsClient.AddParamElementByXPath(doc, "/WsXmlData/sensors", ElementNameArray, El);
        }

        for (int i = 0; i < static_process_list.size(); i++) {
            ElementValueArray = static_process_list.get(i).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            doc = stWsClient.AddParamElementByXPath(doc, "/WsXmlData/cpu", ElementNameArray, El);
        }
        return stWsClient.getXmlToString(doc);
    }


    public ArrayList<String> createParamList() {
        ArrayList<String> param_list = new ArrayList<>();
        param_list = objStaticPhone.getBuildInfo(param_list, MyContext);
        param_list = objStaticPhone.getTelephonyInfo(param_list, MyContext);

        for (int i = 0; i < param_list.size(); i++) {
            hs.add(param_list.get(i));
        }
        param_list.clear();
        param_list.addAll(hs);
        return param_list;
    }
}

