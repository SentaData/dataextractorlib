package com.senta.dataextractor;

import android.content.Context;

import com.senta.dataextractor.interfaces.ISentaSecurePrefs;
import com.senta.dataextractor.parameters.ParamBattery;
import com.senta.dataextractor.parameters.ParamDynamicMemory;
import com.senta.dataextractor.parameters.ParamStaticPhone;
import com.senta.dataextractor.utils.EncodingUtils;
import com.senta.dataextractor.xallegro.api.ApiRequestParams;
import com.senta.dataextractor.xallegro.api.StWsClient;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class DynamicData {

    private static final String TAG = DynamicData.class.getName();
    private final ISentaSecurePrefs sentaSecurePrefs;
    private Context MyContext;
    private String DynamicStructureXml;

    private StWsClient obj = new StWsClient();
    private ParamDynamicMemory MemObj = new ParamDynamicMemory();
    private ParamStaticPhone objStaticPhone = new ParamStaticPhone();
    private ParamProcesses objProcess = new ParamProcesses();
    private ParamBattery objBattery = new ParamBattery();

    private ArrayList<String> AllDynamic = new ArrayList<String>();
    String DynamicParamXmlString = "", DynamicParamB64XmlString = "";

    public DynamicData(Context _MyContext, String _DynamicStructureXml, ISentaSecurePrefs sentaSecurePrefs) {

        this.MyContext = _MyContext;
        this.DynamicStructureXml = _DynamicStructureXml;
        this.sentaSecurePrefs = sentaSecurePrefs;
    }

    public void extractDynamicData(String username, String password) {

        Document DynamicDataDoc;

        DynamicDataDoc = obj.createXmlDoc(DynamicStructureXml);
        DynamicDataDoc = obj.AddElementByXPath(DynamicDataDoc, "/WsXmlData/login/username", username);
        DynamicDataDoc = obj.AddElementByXPath(DynamicDataDoc, "/WsXmlData/login/password", password);

        String[] ElementNameArray = {"name", "value"};
        String[] ElementValueArray = null;

        ArrayList<String> MemInfo = MemObj.getMemoryInfo();
        String Imei = objStaticPhone.getIMEINumber(MyContext);
        MemInfo.add("StDevice-Imei," + Imei);

        for (int i = 0; i < MemInfo.size(); i++) {
            ElementValueArray = MemInfo.get(i).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            DynamicDataDoc = obj.AddParamElementByXPath(DynamicDataDoc, "/WsXmlData/params", ElementNameArray, El);
        }

        AllDynamic.addAll(MemInfo);

        ArrayList<ParamProcesses.ProcessItem> ListOfProcesses = objProcess.UpdateProcesses(MyContext, sentaSecurePrefs);
        for (int i = 0; i < ListOfProcesses.size() - 15; i++) {

            if (ListOfProcesses.get(i) != null) {
                ArrayList<String> EachProcessParameters = objProcess.getParametersForEachProcess(ListOfProcesses.get(i), /*MyActivity*/MyContext);

                for (int j = 0; j < EachProcessParameters.size(); j++) {
                    if (EachProcessParameters.get(j) != null) {

                        ElementValueArray = EachProcessParameters.get(j).split(",");

                        if (ElementValueArray.length < 2) {
                            String[] El = {ElementValueArray[0], "NoValue"};
                            //Log.e("Less than one",Integer.toString(ElementValueArray.length));
                            DynamicDataDoc = obj.AddParamElementByXPath(DynamicDataDoc, "/WsXmlData/cpu", ElementNameArray, El);
                        } else {
                            String[] El = {ElementValueArray[0], ElementValueArray[1]};
                            DynamicDataDoc = obj.AddParamElementByXPath(DynamicDataDoc, "/WsXmlData/cpu", ElementNameArray, El);
                        }
                    }
                    AllDynamic.addAll(EachProcessParameters);
                }
            }
        }
        objBattery.setBatteryInfo(MyContext, MyContext);
        ArrayList<String> BatteryList = objBattery.getBatteryInfo();

        for (int l = 0; l < BatteryList.size(); l++) {
            ElementValueArray = BatteryList.get(l).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            DynamicDataDoc = obj.AddParamElementByXPath(DynamicDataDoc, "/WsXmlData/battery", ElementNameArray, El);

        }
        AllDynamic.addAll(BatteryList);

        DynamicParamXmlString = obj.getXmlToString(DynamicDataDoc);
        DynamicParamB64XmlString = EncodingUtils.EncodeToBase64(DynamicParamXmlString);
    }

    public ApiRequestParams arrayDataForAuthentication(String username, String password) {
        extractDynamicData(username, password);
        return StWsClient.getDeviceDataForAuthentication(MyContext, AllDynamic, DynamicParamXmlString);
    }
}
