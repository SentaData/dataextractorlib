package com.senta.dataextractor;

import android.content.Context;
import android.util.Log;

import com.senta.dataextractor.parameters.ParamDynamicMemory;
import com.senta.dataextractor.xallegro.api.StWsClient;

import java.util.ArrayList;


/**
 * Created by alex on 14/10/2014.
 */
public class StExtractFreeMemory implements Runnable {


    private Context MyContext;
    private StWsClient obj = new StWsClient();
    private ParamDynamicMemory MemObj = new ParamDynamicMemory();

    private String Url;


    public StExtractFreeMemory(Context _MyContext, String _Url) {

        this.MyContext = _MyContext;

        this.Url = _Url;
    }

    @Override
    public void run() {

        Log.d("START EXTRACTION", "Free Memory");

        //if (MainActivity.DeviceRegistered==true && MainActivity.NetConnStatus==1) {

        ArrayList<String> FreeMemory = MemObj.getFreeMemory();

           /*for(int i=0; i<FreeMemory.size(); i++) {
               Log.d("Free Memory2: ", FreeMemory.get(i));
           }*/

        Double TotalMemSize = Double.parseDouble(FreeMemory.get(0));
        Double FreeMemSize = Double.parseDouble(FreeMemory.get(1));
        Double ThreeQuarterMemSize = TotalMemSize * 0.75;
        //Double thresholdSize = TotalMemSize - FreeMemSize;
        if (FreeMemSize > ThreeQuarterMemSize) {
            //MainActivity.MemoryFree = false;
            Log.d("Memory is NOT ", "Free");
        } else {
            //MainActivity.MemoryFree = true;
            Log.d("Memory is", "Free");
        }

        /*} else {

            Log.d("Senta-Secure in dynamic Extraction", "DeviceNotRegistered or not Intenret");
        }*/
    }

}
