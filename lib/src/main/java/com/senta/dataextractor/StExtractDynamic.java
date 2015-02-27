package com.senta.dataextractor;

import android.content.Context;
import android.util.Log;

import com.senta.dataextractor.interfaces.ISentaSecurePrefs;
import com.senta.dataextractor.parameters.ParamBattery;
import com.senta.dataextractor.parameters.ParamDynamicMemory;
import com.senta.dataextractor.parameters.ParamStaticPhone;
import com.senta.dataextractor.utils.EncodingUtils;
import com.senta.dataextractor.xallegro.api.ApiRequestParams;
import com.senta.dataextractor.xallegro.api.StWsClient;

import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Created by alex on 14/10/2014.
 */
public class StExtractDynamic implements Runnable {


    private static final String TAG = StExtractDynamic.class.getName();
    private Context MyContext;
    private String DynamicStructureXmlB64;
    private StWsClient obj = new StWsClient();
    private ParamDynamicMemory MemObj = new ParamDynamicMemory();
    private ParamStaticPhone objStaticPhone = new ParamStaticPhone();
    //private ParamSensors objSensor = new ParamSensors();
    private ParamProcesses objProcess = new ParamProcesses();

    private ParamBattery objBattery = new ParamBattery();
    //private Activity MyActivity;
    private String Url;

    ArrayList<String> AllDynamic = new ArrayList<String>();
    String DynamicParamXmlString = "", DynamicParamB64XmlString = "";
    private ISentaSecurePrefs sentaPrefs;

    public StExtractDynamic(Context _MyContext, String _DynamicStructureXmlB64, String _Url, ISentaSecurePrefs sentaPrefs) {

        this.MyContext = _MyContext;
        this.DynamicStructureXmlB64 = _DynamicStructureXmlB64;
        this.Url = _Url;
        this.sentaPrefs = sentaPrefs;
    }

    @Override
    public void run() {

        //if (MainActivity.DeviceRegistered==true && MainActivity.NetConnStatus==1 && MainActivity.MemoryFree==true) {
        Document dynamicDataDoc;
        dynamicDataDoc = StWsClient.createXmlDoc(DynamicStructureXmlB64);
        dynamicDataDoc = obj.AddElementByXPath(dynamicDataDoc, "/WsXmlData/login/username", "alex@xallegro.com"/*email*/);
        dynamicDataDoc = obj.AddElementByXPath(dynamicDataDoc, "/WsXmlData/login/password", /*password*/"abc");

        String[] ElementNameArray = {"name", "value"};
        String[] ElementValueArray;

        ArrayList<String> MemInfo = MemObj.getMemoryInfo();
        String Imei = objStaticPhone.getIMEINumber(MyContext);
        MemInfo.add("StDevice-Imei," + Imei);

        for (int i = 0; i < MemInfo.size(); i++) {
            ElementValueArray = MemInfo.get(i).split(",");
            String[] El = {ElementValueArray[0], ElementValueArray[1]};
            dynamicDataDoc = obj.AddParamElementByXPath(dynamicDataDoc, "/WsXmlData/params", ElementNameArray, El);
        }

        AllDynamic.addAll(MemInfo);

        ArrayList<ParamProcesses.ProcessItem> ListOfProcesses = objProcess.UpdateProcesses(MyContext, sentaPrefs);
        for (int i = 0; i < ListOfProcesses.size() - 15; i++) {

            if (ListOfProcesses.get(i) != null) {
                ArrayList<String> EachProcessParameters = objProcess.getParametersForEachProcess(ListOfProcesses.get(i), /*MyActivity*/MyContext);

                for (int j = 0; j < EachProcessParameters.size(); j++) {
                    if (EachProcessParameters.get(j) != null) {

                        ElementValueArray = EachProcessParameters.get(j).split(",");

                        if (ElementValueArray.length < 2) {
                            String[] El = {ElementValueArray[0], "NoValue"};
                            //Log.e("Less than one",Integer.toString(ElementValueArray.length));
                            dynamicDataDoc = obj.AddParamElementByXPath(dynamicDataDoc, "/WsXmlData/cpu", ElementNameArray, El);
                        } else {

                            String[] El = {ElementValueArray[0], ElementValueArray[1]};
                            dynamicDataDoc = obj.AddParamElementByXPath(dynamicDataDoc, "/WsXmlData/cpu", ElementNameArray, El);

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
            dynamicDataDoc = obj.AddParamElementByXPath(dynamicDataDoc, "/WsXmlData/battery", ElementNameArray, El);

        }

        AllDynamic.addAll(BatteryList);

        DynamicParamXmlString = obj.getXmlToString(dynamicDataDoc);
        DynamicParamB64XmlString = EncodingUtils.EncodeToBase64(DynamicParamXmlString);

        ApiRequestParams requestParams = StWsClient.getDeviceDataForAuthentication(MyContext, AllDynamic, DynamicParamXmlString);

        String ServerResponse = obj.DeviceRegistration(Url, requestParams.getStrCipherTextStrCTR(), requestParams.getHexSymmetricKey(), requestParams.getHexIv());


        //obj.DynamicParametersRegistration(Url,DynamicParamB64XmlString);
        Log.i("ServerResponse", ServerResponse);
        /*} else {

			Log.d("Senta-Secure in dynamic Extraction", "DeviceNotRegistered or not Intenret");
		}*/
    }
}
