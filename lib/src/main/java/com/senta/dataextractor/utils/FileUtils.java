package com.senta.dataextractor.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.senta.dataextractor.interfaces.ISentaSecureApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = FileUtils.class.getName();

    public static String saveFile(String content, String name, Context context, ISentaSecureApp secureApp) {
        final File filesDir = context.getFilesDir();
        final File file = new File(filesDir, name);
        if (saveContentsToFile(content, file, secureApp)) return filesDir + "/" + name;
        return null;
    }


    public static File saveFileExternally(String content, String name, Context context, ISentaSecureApp secureApp) {
        final File filesDir = context.getExternalFilesDir(null);
        final File file = new File(filesDir, name);
        if (saveContentsToFile(content, file, secureApp)) return file;
        return null;
    }


    private static boolean saveContentsToFile(String content, File file, ISentaSecureApp secureApp) {
        byte[] decodedBytes = Base64.decode(content, 0);
        FileOutputStream os;
        try {
            os = new FileOutputStream(file, false);
            os.write(decodedBytes);
            os.flush();
            os.close();
            secureApp.addFileToBeDeleted(file);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "saveFile- IOException:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
