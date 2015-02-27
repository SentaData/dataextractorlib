package com.senta.dataextractor.parameters;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

;

/**
 * Created by mariosandreou on 29/09/14.
 */
public class ParamSensors {


    public String getSensorInfo(int state, Activity a) {
        if (state == -1) {
            return "";//a.getString(R.string.info_not_available);
        }

        if (state > 1) {
            return "";//a.getString(R.string.sensor_info2, state);
        } else {
            return "";//a.getString(R.string.sensor_info, state);
        }
    }

    public int getSensorState(Context a) {
        SensorManager sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);

        if (sm != null) {
            List<Sensor> ss = sm.getSensorList(Sensor.TYPE_ALL);

            int c = 0;

            if (ss != null) {
                c = ss.size();
            }
            for (int i = 0; i < ss.size(); i++) {
                /*Log.e(Integer.toString(i), ss.get(i).getName());
                Log.e(Integer.toString(i), ss.get(i).getVendor());
                Log.e(Integer.toString(i), Integer.toString(ss.get(i).getType()));
                Log.e(Integer.toString(i), Float.toString(ss.get(i).getMaximumRange()));
                Log.e(Integer.toString(i), Float.toString(ss.get(i).getMaximumRange()));
                Log.e(Integer.toString(i), Float.toString(ss.get(i).getPower()));
                Log.e(Integer.toString(i), Float.toString(ss.get(i).getResolution()));*/
            }
            return c;
        }
        return -1;
    }

    public ArrayList<String> getParamSensors(Context/*Activity*/ a) {
        SensorManager sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
        ArrayList<String> param_list = new ArrayList<String>();
        if (sm != null) {
            List<Sensor> ss = sm.getSensorList(Sensor.TYPE_ALL);

            int c = 0;

            if (ss != null) {
                c = ss.size();
            }
            for (int i = 0; i < ss.size(); i++) {

                param_list.add("StDeviceSensor-Name," + ss.get(i).getName());
                param_list.add("StDeviceSensor-Vendor," + ss.get(i).getVendor());
                param_list.add("StDeviceSensor-Type," + Integer.toString(ss.get(i).getType()));
                param_list.add("StDeviceSensor-MaximumRange," + Float.toString(ss.get(i).getMaximumRange()));

                param_list.add("StDeviceSensor-Power," + Float.toString(ss.get(i).getPower()));
                param_list.add("StDeviceSensor-Resolution," + Float.toString(ss.get(i).getResolution()));

            }

        }
        return param_list;

    }

}
