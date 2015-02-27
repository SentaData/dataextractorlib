package com.senta.dataextractor;

/**
 * Created by mariosandreou on 29/09/14.
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.senta.dataextractor.parameters.ParamSensors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * SysInfoManager
 */
public final class SysInfoManager {

    static final String PSTORE_SYSINFOMANAGER = SysInfoManager.class.getSimpleName();

    private static final char[] CSV_SEARCH_CHARS = new char[]{
            ',', '"', '\r', '\n'
    };
    private static final char[] HTML_SEARCH_CHARS = new char[]{
            '<', '>', '&', '\'', '"', '\n'
    };

    private static final String F_SCALE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq"; //$NON-NLS-1$
    private static final String F_MEM_INFO = "/proc/meminfo"; //$NON-NLS-1$
    private static final String F_CPU_INFO = "/proc/cpuinfo"; //$NON-NLS-1$
    private static final String F_VERSION = "/proc/version"; //$NON-NLS-1$
    private static final String F_MOUNT_INFO = "/proc/mounts"; //$NON-NLS-1$

    private static final String HEADER_SPLIT = "========================================================================================\n"; //$NON-NLS-1$
    private static final String openFullRow = "<tr align=\"left\" valign=\"top\"><td colspan=5><small>"; //$NON-NLS-1$

    static final String openHeaderRow = "<tr align=\"left\" bgcolor=\"#E0E0FF\"><td><b>"; //$NON-NLS-1$
    static final String closeHeaderRow = "</b></td><td colspan=4/></tr>\n"; //$NON-NLS-1$
    static final String openRow = "<tr align=\"left\" valign=\"top\"><td nowrap><small>"; //$NON-NLS-1$
    static final String openTitleRow = "<tr bgcolor=\"#E0E0E0\" align=\"left\" valign=\"top\"><td><small>"; //$NON-NLS-1$
    static final String closeRow = "</small></td></tr>\n"; //$NON-NLS-1$
    static final String nextColumn = "</small></td><td><small>"; //$NON-NLS-1$
    static final String nextColumn4 = "</small></td><td colspan=4><small>"; //$NON-NLS-1$
    static final String emptyRow = "<tr><td>&nbsp;</td></tr>\n"; //$NON-NLS-1$

    static final String PREF_KEY_SHOW_INFO_ICON = "show_info_icon"; //$NON-NLS-1$
    static final String PREF_KEY_SHOW_TASK_ICON = "show_task_icon"; //$NON-NLS-1$
    static final String PREF_KEY_AUTO_START_ICON = "auto_start_icon"; //$NON-NLS-1$
    static final String PREF_KEY_DEFAULT_EMAIL = "default_email"; //$NON-NLS-1$
    static final String PREF_KEY_DEFAULT_TAB = "default_tab"; //$NON-NLS-1$
    static final String PREF_KEY_WIDGET_DISABLED = "widget_disabled"; //$NON-NLS-1$

    private static final String KEY_SD_STORAGE = "sd_storage"; //$NON-NLS-1$
    private static final String KEY_APP2SD_STORAGE = "app2sd_storage"; //$NON-NLS-1$
    private static final String KEY_INTERNAL_STORAGE = "internal_storage"; //$NON-NLS-1$
    private static final String KEY_SYSTEM_STORAGE = "system_storage"; //$NON-NLS-1$
    private static final String KEY_CACHE_STORAGE = "cache_storage"; //$NON-NLS-1$
    private static final String KEY_MEMORY = "memory"; //$NON-NLS-1$
    private static final String KEY_PROCESSOR = "processor"; //$NON-NLS-1$
    private static final String KEY_NET_ADDRESS = "net_address"; //$NON-NLS-1$
    private static final String KEY_BATTERY_LEVEL = "battery_level"; //$NON-NLS-1$
    private static final String KEY_SENSORS = "sensors"; //$NON-NLS-1$
    private static final String KEY_ACTIONS = "actions"; //$NON-NLS-1$
    private static final String KEY_REFRESH_STATUS = "refresh_status"; //$NON-NLS-1$
    private static final String KEY_VIEW_LOGS = "view_logs"; //$NON-NLS-1$
    private static final String KEY_SEND_REPORT = "send_report"; //$NON-NLS-1$
    private static final String KEY_MORE_INFO = "more_info"; //$NON-NLS-1$

    private static final int BASIC_INFO = 0;
    private static final int APPLICATIONS = 1;
    private static final int PROCESSES = 2;
    private static final int NETSTATES = 3;
    private static final int DMESG_LOG = 4;
    private static final int LOGCAT_LOG = 5;

    private static final int WIDGET_BAR = 0;
    private static final int WIDGET_INFO = 1;
    private static final int WIDGET_TASK = 2;


    LinkedHashMap<String, PrefItem> prefs;


    public LinkedHashMap<String, PrefItem> CreatePrefs(Activity a, Context context) {

        prefs = new LinkedHashMap<String, PrefItem>();

        prefs.put(KEY_SD_STORAGE, new PrefItem(KEY_SD_STORAGE,
                a.getString(R.string.sd_storage),
                false));

        prefs.put(KEY_APP2SD_STORAGE, new PrefItem(KEY_APP2SD_STORAGE,
                a.getString(R.string.a2sd_storage),
                false));

        prefs.put(KEY_INTERNAL_STORAGE, new PrefItem(KEY_INTERNAL_STORAGE,
                a.getString(R.string.internal_storage),
                false));

        prefs.put(KEY_SYSTEM_STORAGE, new PrefItem(KEY_SYSTEM_STORAGE,
                a.getString(R.string.system_storage),
                false));

        prefs.put(KEY_CACHE_STORAGE, new PrefItem(KEY_CACHE_STORAGE,
                a.getString(R.string.cache_storage),
                false));


        prefs.put(KEY_NET_ADDRESS, new PrefItem(KEY_NET_ADDRESS,
                a.getString(R.string.net_address)));

        prefs.put(KEY_SENSORS, new PrefItem(KEY_SENSORS,
                a.getString(R.string.sensors)));

        return prefs;
    }


    private void updateInfo(Activity a, Context ctx) {


        String[] si = getExternalStorageInfo(a);
        findPreference(KEY_SD_STORAGE).setSummary(si == null ? a.getString(R.string.info_not_available)
                : a.getString(R.string.storage_summary, si[0], si[1]));

        si = getA2SDStorageInfo(a);
        findPreference(KEY_APP2SD_STORAGE).setSummary(si == null ? a.getString(R.string.info_not_available)
                : a.getString(R.string.storage_summary, si[0], si[1]));

        si = getInternalStorageInfo(a);
        findPreference(KEY_INTERNAL_STORAGE).setSummary(si == null ? a.getString(R.string.info_not_available)
                : a.getString(R.string.storage_summary, si[0], si[1]));

        si = getSystemStorageInfo(a);
        findPreference(KEY_SYSTEM_STORAGE).setSummary(si == null ? a.getString(R.string.info_not_available)
                : a.getString(R.string.storage_summary, si[0], si[1]));

        si = getCacheStorageInfo(a);
        findPreference(KEY_CACHE_STORAGE).setSummary(si == null ? a.getString(R.string.info_not_available)
                : a.getString(R.string.storage_summary, si[0], si[1]));

        String nInfo = getNetAddressInfo();
        findPreference(KEY_NET_ADDRESS).setSummary(nInfo == null ? a.getString(R.string.info_not_available)
                : nInfo);
        findPreference(KEY_NET_ADDRESS).setEnabled(nInfo != null);

        ParamSensors sensorParam = new ParamSensors();

        int s = sensorParam.getSensorState(a);
        findPreference(KEY_SENSORS).setSummary(sensorParam.getSensorInfo(s, a));
        findPreference(KEY_SENSORS).setEnabled(s > 0);

    }


    PrefItem findPreference(String key) {
        if (prefs != null) {
            return prefs.get(key);
        }
        return null;
    }


    private String[] getExternalStorageInfo(Activity a) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
                || Environment.MEDIA_MOUNTED.equals(state)) {
            return getStorageInfo(Environment.getExternalStorageDirectory(), a);
        }

        return null;
    }

    private String[] getA2SDStorageInfo(Activity a) {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
                || Environment.MEDIA_MOUNTED.equals(state)) {
            // here we just guess if it's app2sd enabled, this should work for
            // most app2sd enabled roms, but may not all.

            File f = new File("/dev/block/mmcblk0p2"); //$NON-NLS-1$

            if (f.exists()) {
                BufferedReader reader = null;
                String mountPoint = null;

                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(F_MOUNT_INFO)),
                            1024);

                    String line;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("/dev/block/mmcblk0p2 ")) //$NON-NLS-1$
                        {
                            // 21==length of the above string
                            int idx = line.indexOf(' ', 21);

                            if (idx != -1) {
                                mountPoint = line.substring(21, idx).trim();
                            }

                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(SysInfoManager.class.getName(),
                            e.getLocalizedMessage(),
                            e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                            reader = null;
                        } catch (IOException ie) {
                            Log.e(SysInfoManager.class.getName(),
                                    ie.getLocalizedMessage(),
                                    ie);
                        }
                    }
                }

                if (mountPoint != null) {
                    f = new File(mountPoint);

                    if (f.exists() && f.isDirectory()) {
                        return getStorageInfo(f, a);
                    }
                }
            }
        }

        return getSystemA2SDStorageInfo(a);
    }

    /**
     * This checks the built-in app2sd storage info supported since Froyo
     */
    private String[] getSystemA2SDStorageInfo(Activity a) {
        Activity ctx = a;
        final PackageManager pm = ctx.getPackageManager();
        List<ApplicationInfo> allApps = pm.getInstalledApplications(0);

        long total = 0;
        long free = 0;

        for (int i = 0, size = allApps.size(); i < size; i++) {
            ApplicationInfo info = allApps.get(i);

            if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                String src = info.sourceDir;

                if (src != null) {
                    File srcFile = new File(src);

                    if (srcFile.canRead()) {
                        try {
                            StatFs stat = new StatFs(srcFile.getAbsolutePath());
                            long blockSize = stat.getBlockSize();

                            total += stat.getBlockCount() * blockSize;
                            free += stat.getAvailableBlocks() * blockSize;
                        } catch (Exception e) {
                            Log.e(SysInfoManager.class.getName(),
                                    "Cannot access path: " //$NON-NLS-1$
                                            + srcFile.getAbsolutePath(),
                                    e);
                        }
                    }
                }
            }
        }

        if (total > 0) {
            String[] info = new String[2];
            info[0] = Formatter.formatFileSize(ctx, total);
            info[1] = Formatter.formatFileSize(ctx, free);

            return info;
        }

        return null;
    }

    private String[] getInternalStorageInfo(Activity a) {
        return getStorageInfo(Environment.getDataDirectory(), a);
    }

    private String[] getSystemStorageInfo(Activity a) {
        return getStorageInfo(Environment.getRootDirectory(), a);
    }

    private String[] getCacheStorageInfo(Activity a) {
        return getStorageInfo(Environment.getDownloadCacheDirectory(), a);
    }

    private String[] getStorageInfo(File path, Activity a) {
        if (path != null) {
            try {
                Activity ctx = a;

                StatFs stat = new StatFs(path.getAbsolutePath());
                long blockSize = stat.getBlockSize();

                String[] info = new String[2];
                info[0] = Formatter.formatFileSize(ctx, stat.getBlockCount()
                        * blockSize);
                info[1] = Formatter.formatFileSize(ctx,
                        stat.getAvailableBlocks() * blockSize);

                return info;
            } catch (Exception e) {
                Log.e(SysInfoManager.class.getName(), "Cannot access path: " //$NON-NLS-1$
                        + path.getAbsolutePath(), e);
            }
        }

        return null;
    }

    static String getNetAddressInfo() {
        try {
            StringBuffer sb = new StringBuffer();

            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String addr = inetAddress.getHostAddress();

                        if (!TextUtils.isEmpty(addr)) {
                            if (sb.length() == 0) {
                                sb.append(addr);
                            } else {
                                sb.append(", ").append(addr); //$NON-NLS-1$
                            }
                        }
                    }
                }
            }

            String netAddress = sb.toString();

            if (!TextUtils.isEmpty(netAddress)) {
                return netAddress;
            }
        } catch (SocketException e) {
            Log.e(SysInfoManager.class.getName(), e.getLocalizedMessage(), e);
        }

        return null;
    }


    static String getVersionName(PackageManager pm, String pkgName) {
        String ver = null;

        try {
            ver = pm.getPackageInfo(pkgName, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(SysInfoManager.class.getName(), e.getLocalizedMessage(), e);
        }

        if (ver == null) {
            ver = ""; //$NON-NLS-1$
        }

        return ver;
    }


    /**
     * PrefItem
     */
    public static final class PrefItem {

        String key;
        String title;
        String summary;
        boolean isHeader;
        boolean enabled = true;

        PrefItem(String key, String title) {
            this.key = key;
            this.title = title;
        }

        PrefItem(String key, String title, boolean enabled) {
            this.key = key;
            this.title = title;
            this.enabled = enabled;
        }

        String getKey() {
            return key;
        }

        void setSummary(String summary) {
            this.summary = summary;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * FormatItem
     */
    static final class FormatItem {

        String format;
        boolean compressed;

        FormatItem(String format) {
            this.format = format;
            this.compressed = false;
        }
    }


}
