package com.senta.dataextractor.parameters;

/**
 * Created by mariosandreou on 17/07/14.
 */

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ParamStaticPhone {


    private static final String F_MOUNT_INFO = "/proc/mounts";

    public ParamStaticPhone() {

    }

    public ArrayList<String> getBuildInfo(ArrayList<String> param_list, Context context) {

        param_list.add("StDevice-Brand," + Build.BRAND);

        param_list.add("StParameterStatic-Name," + Build.DEVICE);

        param_list.add("StParameterStatic-Product," + Build.PRODUCT);

        //param_list.add("Type," + Build.TYPE);

        param_list.add("StDevice-Model," + Build.MODEL);

        param_list.add("StParameterStatic-Manufacturer," + Build.MANUFACTURER);

        String android_id = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID.toString());
        if (android_id != null)
            param_list.add("StParameterStatic-AndroidId," + android_id);
        else
            param_list.add("StParameterStatic-AndroidId,NaN");

        if (Build.VERSION.SDK_INT >= 9) {
            String serial_number = Build.SERIAL;
            if (serial_number != null)
                param_list.add("StDevice-SerialNumber," + serial_number);
            else
                param_list.add("StDevice-SerialNumber,NaN");
        } else {
            param_list.add("StDevice-SerialNumber,NaN");
        }

        //Getting access to mac address
        /*WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String mac = wm.getConnectionInfo().getMacAddress();

        if (mac != null)
            param_list.add("MAC,mac");
        else
            param_list.add("MAC,mac");
         */
        return param_list;
    }


    public String getIMEINumber(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //Getting IMEI Number of Device
        String IMEI_Number = telephonyManager.getDeviceId();

        if (IMEI_Number != null)
            return IMEI_Number;
        else
            return IMEI_Number = "NaN";

    }

    public ArrayList<String> getTelephonyInfo(ArrayList<String> param_list, Context context) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //Getting IMEI Number of Device
        String IMEI_Number = telephonyManager.getDeviceId();

        //Getting Phone Number
        String mblNumber = telephonyManager.getLine1Number();

        //Getting the SIM card ID
        String simId = telephonyManager.getSimSerialNumber();

        //Get the phone type
        int phoneType = telephonyManager.getPhoneType();

        if (IMEI_Number != null)
            param_list.add("StDevice-Imei," + IMEI_Number);
        else
            param_list.add("StDevice-Imei," + "NaN");

        /*if(mblNumber!=null)
            param_list.add("StParameterStatic-PhoneNumber,"+mblNumber);
        else*/
        param_list.add("StParameterStatic-PhoneNumber," + "NaN");


        if (simId != null)
            param_list.add("StParameterStatic-SimCardNumber," + simId);
        else
            param_list.add("StParameterStatic-SimCardNumber," + "NaN");

        switch (phoneType) {
            case (TelephonyManager.PHONE_TYPE_CDMA):

                param_list.add("StParameterStatic-PhoneType," + "CDMA");
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):

                param_list.add("StParameterStatic-PhoneType," + "GSM");
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):

                param_list.add("StParameterStatic-PhoneType," + "NONE");
                break;
        }
        return param_list;
    }


    /**
     * Extracts memory parameters from Android device
     * and add them in a global arraylist.
     * <p/>
     * *
     */

    public static String timeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        String localTime = date.format(currentLocalTime);

        return localTime;
    }


    /*public ArrayList<String> getMemoryInfo() {

        String strDate = "";
        ArrayList<String> memory_list = new ArrayList<String>();

        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        strDate = sdf.format(c.getTime());

        Log.e("date", strDate + "  " + timeZone());
        memory_list.add("Timestamp," + strDate);
        memory_list.add("TimeZone," + timeZone());

        if (new File("/proc/meminfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/meminfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {

                    aLine = aLine.replace("kB", "");
                    aLine = aLine.replace(":", ",");
                    aLine = aLine.replaceAll("\\s", "");

                    memory_list.add("StParameterDynamic-" + aLine);
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return memory_list;
    }*/


    public ArrayList<String> getDynamicCpuInfo() {

        ArrayList<String> dynamic_cpu_list = new ArrayList<String>();

        Process p = null;
        try {
            p = Runtime.getRuntime().exec("top -m 15 -d 1 -n 1");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String aLine;
            while ((aLine = br.readLine()) != null) {
                if (aLine.length() > 0)
                    dynamic_cpu_list.add("CPU-" + aLine);
            }
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return dynamic_cpu_list;
    }
}