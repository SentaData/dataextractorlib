package com.senta.dataextractor.parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by mariosandreou on 26/09/14.
 */
public class ParamDynamicMemory {

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


    public ArrayList<String> getMemoryInfo() {

        String strDate = "";
        ArrayList<String> memory_list = new ArrayList<String>();

        Calendar c = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        strDate = sdf.format(c.getTime());

        //Log.e("date", strDate + "  " + timeZone());
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
    }

    public ArrayList<String> getFreeMemory() {

        ArrayList<String> free_total_list = new ArrayList<String>();

        if (new File("/proc/meminfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/meminfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {

                    aLine = aLine.replace("kB", "");
                    aLine = aLine.replace(":", ",");
                    aLine = aLine.replaceAll("\\s", "");

                    String arr[] = aLine.split(",");

                    if (arr[0].contains("MemFree")) {

                        free_total_list.add(arr[1]);

                    }
                    if (arr[0].contains("MemTotal")) {
                        free_total_list.add(arr[1]);


                    }
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return free_total_list;
    }


}
