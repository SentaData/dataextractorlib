package com.senta.dataextractor.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class Utilities {

    public static boolean WrongPasswordUserName(String ServerResponse) {

        if (ServerResponse.contains("<WsXmlData><error>Username Or Password Error</error></WsXmlData>")) {
            return true;

        } else return false;
    }

    public static boolean validateUsername(Context context, String userName) {

        boolean valid = true;

        // check if any of the fields are vaccant
        if (userName.equals("")) {
            Toast.makeText(context.getApplicationContext(), "Username Vaccant", Toast.LENGTH_LONG).show();
            valid = false;
        }
        return valid;
    }

    public static boolean validatePassword(Context context, String password/*, String confirmPassword*/) {

        boolean valid = true;

        if (password.equals("") /*|| confirmPassword.equals("")*/) {
            Toast.makeText(context.getApplicationContext(), "Password Vaccant", Toast.LENGTH_LONG).show();
            valid = false;
        }
        // check if both password matches
        /*if (!password.equals(confirmPassword)) {
            Toast.makeText(getApplicationContext(), "Password Does Not Matches", Toast.LENGTH_LONG).show();
            valid =false;
        }*/
        return valid;
    }

    public static ArrayList<String> RemoveDuplicateValuesInList(ArrayList<String> Grid, int PreviousSize) {
        //Remove duplicate choosen parameters
        HashSet<String> hs = new HashSet<>();
        hs.addAll(Grid);
        Grid.clear();
        Grid.addAll(hs);

        return Grid;
    }


    static public boolean GetNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo[] netInfo = cm.getAllNetworkInfo();

        for (NetworkInfo ni : netInfo) {

            if (ni.getTypeName().equalsIgnoreCase("WIFI"))

                if (ni.isConnected())
                    haveConnectedWifi = true;

            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))

                if (ni.isConnected())
                    haveConnectedMobile = true;
        }

        return haveConnectedWifi || haveConnectedMobile;
    }

    public static String ReadTextFromResource(Context context, int resourceID) {

        InputStream raw = context.getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        int i;

        try {
            i = raw.read();
            while (i != -1) {

                stream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }


    public static boolean isSysProcess(String pkgName) {
        return pkgName != null &&
                (pkgName.startsWith("com.google.process") //$NON-NLS-1$
                        || pkgName.startsWith("com.android.phone") //$NON-NLS-1$
                        || pkgName.startsWith("android.process") //$NON-NLS-1$
                        || pkgName.startsWith("system") //$NON-NLS-1$
                        || pkgName.startsWith("com.android.inputmethod") //$NON-NLS-1$
                        || pkgName.startsWith("com.android.alarmclock")); //$NON-NLS-1$
    }
}


