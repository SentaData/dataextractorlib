package com.senta.dataextractor;

import android.content.Context;

import com.senta.dataextractor.utils.Utilities;

/**
 * Created by mariosandreou on 13/10/14.
 */
public class StWsServiceStatus implements Runnable {

    /*
     * 0= Not Connected
     * 1= Connected
    */
    private Context MyContext;

    public StWsServiceStatus(Context _MyContext) {

        this.MyContext = _MyContext;
    }

    @Override
    public void run() {

        Utilities.GetNetworkConnection(MyContext);

        if (Utilities.GetNetworkConnection(MyContext) == true) {

            //MainActivity.NetConnStatus=1;

        } else {

            //MainActivity.NetConnStatus=0;
        }
    }
}
