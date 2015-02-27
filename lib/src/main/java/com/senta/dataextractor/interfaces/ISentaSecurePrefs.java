package com.senta.dataextractor.interfaces;

import android.content.Context;

public interface ISentaSecurePrefs {
    boolean getBooleanOption(Context ctx, String name, String value);

    int getIntOption(Context ctx, String name, String value, int ignoreActionHidden);
}
