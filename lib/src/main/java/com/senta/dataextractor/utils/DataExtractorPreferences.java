package com.senta.dataextractor.utils;

import android.content.Context;

import com.senta.dataextractor.interfaces.ISentaSecurePrefs;

public class DataExtractorPreferences implements ISentaSecurePrefs {

    @Override
    public boolean getBooleanOption( /*Activity*/Context ac, String prefName, String key) {
        return getBooleanOption(ac, prefName, key, true);
    }

    public static boolean getBooleanOption(Context ac, String prefName, String key, boolean defValue) {
        return ac.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getBoolean(key, defValue);
    }

    @Override
    public int getIntOption(Context ac, String prefName, String key, int defValue) {
        return ac.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                .getInt(key, defValue);
    }
}
