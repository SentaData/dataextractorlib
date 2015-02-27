package com.senta.dataextractor.parameters;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.senta.dataextractor.R;

import java.util.ArrayList;

/**
 * Created by mariosandreou on 01/10/14.
 */
public class ParamBattery {

    private ArrayList<String> battery_data = new ArrayList<String>();

    public void setBatteryInfo(Context context, Context/*Activity*/ a) {

        battery_data.clear();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, ifilter);

        String action = intent.getAction();


        int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int level = -1;


        if (rawlevel >= 0 && scale > 0) {
            level = (rawlevel * 100) / scale;
        }

        String battery_level = String.valueOf(level) + '%';

        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_level) + "," + battery_level);

        //health of battery

        //int  health= intent.getIntExtra(BatteryManager.EXTRA_HEALTH,0);
        int health = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
        String battery_health = context.getString(R.string.unknown);

        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                battery_health = a.getString(R.string.good);
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                battery_health = a.getString(R.string.over_heat);
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                battery_health = a.getString(R.string.dead);
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                battery_health = a.getString(R.string.over_voltage);
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                battery_health = a.getString(R.string.failure);
                break;
        }

        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_health) + "," + battery_health);

        //Status of battery

        //int  status= intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
        int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
        String battery_status = context.getString(R.string.unknown);
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                battery_status = a.getString(R.string.charging);
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                battery_status = a.getString(R.string.discharging);
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                battery_status = a.getString(R.string.full);
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                battery_status = a.getString(R.string.not_charging);
                break;
        }
        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_status) + "," + battery_status);


        //Battery Technology
        String battery_technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_tech) + "," + battery_technology);

        //Battery Voltage
        //int  voltage= intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
        int volt = intent.getIntExtra("voltage", 0); //$NON-NLS-1$
        String battery_voltage = String.valueOf(volt) + "mV"; //$NON-NLS-1$
        battery_data.add("StParameterDynamic-" + (R.string.batt_voltage) + "," + battery_voltage);

        //Battery Temperature

        int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        //int temperature = intent.getIntExtra( "temperature", 0 ); //$NON-NLS-1$

        int tens = temperature / 10;

        String ct = Integer.toString(tens) + "." + (temperature - 10 * tens);

        tens = temperature * 18 / 100;

        String ft = Integer.toString(tens + 32) + "." + (temperature * 18 - 100 * tens);

        String battery_temperature = ct + "\u00B0C / " + ft + "\u00B0F"; //$NON-NLS-1$

        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_temp) + "," + battery_temperature);

        //Battery plugged

        int plugged = intent.getIntExtra("plugged", 0);

        String battery_plugged = a.getString(R.string.unknown);

        switch (plugged) {
            case 0:
                battery_plugged = a.getString(R.string.unplugged);
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                battery_plugged = "AC";
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                battery_plugged = "USB";
                break;
            case BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB:
                battery_plugged = "AC USB";
                break;
        }

        battery_data.add("StParameterDynamic-" + a.getString(R.string.batt_plugged) + "," + battery_plugged);

        int icon_small = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
        int lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);


        boolean present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT);
        int scl = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        battery_data.add("StParameterDynamic-Present," + Boolean.toString(present));
        battery_data.add("StParameterDynamic-Scale," + Integer.toString(scl));

        /*Log.e("Health: ", battery_health);
        Log.e("Icon Small:" , Integer.toString(icon_small));
        Log.e("Level: ", Integer.toString(lvl));
        Log.e("Plugged: ", battery_plugged);
        Log.e("Present: ", Boolean.toString(present));
        Log.e("Scale: ", Integer.toString(scl));
        Log.e("Status: ", battery_status);
        Log.e("Technology: ", battery_technology);
        Log.e("Temperature: ", battery_temperature);
        Log.e("Voltage: ", battery_voltage);*/
    }

    public ArrayList<String> getBatteryInfo() {

        return this.battery_data;
    }
}
