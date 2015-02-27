package com.senta.dataextractor;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import com.senta.dataextractor.interfaces.ISentaSecurePrefs;
import com.senta.dataextractor.utils.Utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by mariosandreou on 29/09/14.
 */
public class ParamProcesses implements Constants {


    private static final String SCALE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private static final String CPU_INFO = "/proc/cpuinfo";

    private static final String PSTORE_PROCESSMANAGER = ParamProcesses.class.getSimpleName();


    private static final int MSG_REFRESH_PKG_LABEL = MSG_PRIVATE + 1;
    private static final int MSG_REFRESH_PKG_ICON = MSG_PRIVATE + 2;

    private static final String PREF_KEY_IGNORE_ACTION = "ignore_action"; //$NON-NLS-1$
    private static final String PREF_KEY_IGNORE_LIST = "ignore_list"; //$NON-NLS-1$
    private static final String PREF_KEY_SHOW_MEM = "show_mem"; //$NON-NLS-1$
    private static final String PREF_KEY_SHOW_CPU = "show_cpu"; //$NON-NLS-1$
    private static final String PREF_KEY_SHOW_SYS_PROC = "show_sys_proc"; //$NON-NLS-1$
    private static final String PREF_KEY_SHOW_KILL_WARN = "show_kill_warn"; //$NON-NLS-1$

    private static final int ORDER_TYPE_NAME = 0;
    private static final int ORDER_TYPE_IMPORTANCE = 1;
    private static final int ORDER_TYPE_MEM = 2;
    private static final int ORDER_TYPE_CPU = 3;

    private static final int ACTION_MENU = 0;
    private static final int ACTION_SWITCH = 1;
    private static final int ACTION_END = 2;
    private static final int ACTION_END_OTHERS = 3;
    private static final int ACTION_IGNORE = 4;
    private static final int ACTION_DETAILS = 5;

    private static final int IGNORE_ACTION_HIDDEN = 0;
    private static final int IGNORE_ACTION_PROTECTED = 1;


    ProcessCache procCache;
    long totalLoad, totalDelta, totalWork, workDelta;
    LinkedHashSet<String> ignoreList;
    private byte[] buf = new byte[512];

    private ArrayList<String> StaticCpuList;
    private ArrayList<String> DynamicProcessesList;

    public void ParamProcesses() {


    }

    /*
        @Override
        protected void finalize() throws Throwable {
            Log.d("END PROCESS EXTRACTION","PROCESS");
            super.finalize();
        }
    */
    public ArrayList<String> getStaticCPUInfo() {
        StaticCpuList = new ArrayList<String>();

        String[] stat = getCpuState();

        if (stat != null && stat.length == 2) {
            StaticCpuList.add("StParameterStatic-Model," + stat[0]);
            if (stat[1] != null) {
                stat[1] = stat[1].replaceAll("\\s", "");
                StaticCpuList.add("StParameterStatic-CurrentFrequency," + stat[1]);
            }
        }

        String cpuMin = readFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq", true);
        String cpuMax = readFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", true);
        String scaleMin = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq", true);
        String scaleMax = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq", true);
        String governor = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", false);

        if (cpuMin != null && cpuMax != null) {
            StaticCpuList.add("StParameterStatic-CPUFrequencyRange," + cpuMin + " - " + cpuMax);
        }

        if (scaleMin != null && scaleMax != null) {
            StaticCpuList.add("StParameterStatic-ScalingRange," + scaleMin + " - " + scaleMax);
        }

        if (governor != null) {
            StaticCpuList.add("StParameterStatic-ScalingGovernor," + governor);
        }

        StaticCpuList.add("StParameterStatic-ABI," + Build.CPU_ABI);

        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.length() != 0) {
                        aLine = aLine.replace(":", ",");
                        aLine = aLine.replaceAll("\\s", "");
                        StaticCpuList.add("StParameterStatic-" + aLine);
                    }
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return StaticCpuList;
    }

    static String[] getCpuState() {
        BufferedReader reader = null;
        try {
            String line;
            String processor = null;
            String mips = null;
            String model = null;

            File f = new File(SCALE_FREQ);

            if (f.exists()) {
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 32);

                    line = reader.readLine();

                    if (line != null) {
                        long freq = Long.parseLong(line.trim());
                        mips = String.valueOf(freq / 1000);
                    }
                } catch (Exception e) {
                    //Log.e( SysInfoManager.class.getName( ),
                    //	 e.getLocalizedMessage( ),
                    //	 e );
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                            reader = null;
                        } catch (IOException ie) {
                            //Log.e( SysInfoManager.class.getName( ),
                            //	 ie.getLocalizedMessage( ),
                            //	 ie );
                        }
                    }
                }
            } else {
                //Log.d( SysInfoManager.class.getName( ),
                //	 "No scaling found, using BogoMips instead" );
            }

            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(CPU_INFO))), 1024);

            while ((line = reader.readLine()) != null) {
                if (processor == null && line.startsWith("Processor")) {
                    processor = line;
                } else if (mips == null && line.startsWith("BogoMIPS")) {
                    mips = line;
                }
                if (model == null && line.startsWith("model name")) {
                    model = line;
                }

                if (model != null || (processor != null && mips != null)) {
                    break;
                }
            }

            if (model != null) {
                int idx = model.indexOf(':');
                if (idx != -1) {
                    return new String[]{model.substring(idx + 1).trim(), null};
                } else {
                    //Log.e( SysInfoManager.class.getName( ),
                    //	 "Unexpected processor format: " + model ); //$NON-NLS-1$
                }
            } else if (processor != null && mips != null) {
                int idx = processor.indexOf(':');
                if (idx != -1) {
                    processor = processor.substring(idx + 1).trim();

                    idx = mips.indexOf(':');

                    if (idx != -1) {
                        mips = mips.substring(idx + 1).trim();
                    }

                    return new String[]{processor, mips + "MHz"};
                } else {
                    //Log.e( SysInfoManager.class.getName( ),
                    //	 "Unexpected processor format: " + processor ); //$NON-NLS-1$
                }
            } else {
                //Log.e( SysInfoManager.class.getName( ),
                //	 "Incompatible cpu format" ); //$NON-NLS-1$
            }
        } catch (Exception e) {
            //Log.e( SysInfoManager.class.getName( ), e.getLocalizedMessage( ), e );
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ie) {
                    //Log.e( SysInfoManager.class.getName( ),
                    //	 ie.getLocalizedMessage( ),
                    //	 ie );
                }
            }
        }

        return null;
    }

    private static String readFile(String fname, boolean freq) {
        File f = new File(fname);

        if (f.exists() && f.isFile() && f.canRead()) {
            BufferedReader reader = null;
            String line;

            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)), 32);

                line = reader.readLine();

                if (line != null) {
                    if (freq) {
                        return String.valueOf(Long.parseLong(line.trim()) / 1000)
                                + "MHz";
                    } else {
                        return line.trim();
                    }
                }
            } catch (Exception e) {
                //Log.e( CpuInfoActivity.class.getName( ),
                //	 e.getLocalizedMessage( ),
                //	 e );
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                        reader = null;
                    } catch (IOException ie) {
                        //	Log.e( CpuInfoActivity.class.getName( ),
                        //	 ie.getLocalizedMessage( ),
                        //	 ie );
                    }
                }
            }
        } else {
            //Log.d( CpuInfoActivity.class.getName( ),
            //	 "Cannot read file: " + fname ); //$NON-NLS-1$
        }
        return null;
    }

    public ArrayList<ProcessItem> UpdateProcesses(/*Activity a,*/ Context context, ISentaSecurePrefs sentaSecurePrefs) {

        procCache = new ProcessCache();

        ignoreList = new LinkedHashSet<String>();

        ArrayList<String> list = getIgnoreList( /*a*/context.getSharedPreferences(PSTORE_PROCESSMANAGER,
                Context.MODE_PRIVATE));

        if (list != null) {
            ignoreList.addAll(list);
        }

        StringBuffer sb = new StringBuffer();
        ActivityManager am = (ActivityManager) /*a*/context.getSystemService(context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> procs = am.getRunningAppProcesses();

        if (procs != null) {
            PackageManager pm = context/*a*/.getPackageManager();

            for (int i = 0, size = procs.size(); i < size; i++) {
                RunningAppProcessInfo proc = procs.get(i);

                sb.append('<')
                        .append(getImportance(proc))
                        .append("> [") //$NON-NLS-1$
                        .append(proc.pid)
                        .append("]\t:\t"); //$NON-NLS-1$

                sb.append(proc.processName);

                try {
                    ApplicationInfo ai = pm.getApplicationInfo(proc.processName,
                            0);

                    if (ai != null) {
                        CharSequence label = pm.getApplicationLabel(ai);

                        if (label != null
                                && !label.equals(proc.processName)) {
                            sb.append(" ( ") //$NON-NLS-1$
                                    .append(label)
                                    .append(" )"); //$NON-NLS-1$
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // ignore this error
                }

                sb.append('\n');
            }
        }

        sb.append('\n');
        String ProcessesList = sb.toString();

        updateProcess(procs, context, sentaSecurePrefs);

        ArrayList<ProcessItem> localList;
        synchronized (procCache) {
            localList = procCache.procList;
        }


        /*synchronized ( procCache ) {
            ArrayList<ProcessItem> localList = procCache.procList;

            for (int i = 0; i < localList.size(); i++) {
                if(localList.get(i)!=null) {

                    String ProcessList = getParametersForEachProcess(localList.get(i), a);
                    for(int j=0; j<DynamicProcessesList.size(); j++) {
                        Log.e(Integer.toString(j), DynamicProcessesList.get(j));
                    }
                    }

            }
        }*/

        return localList;

    }


    public ArrayList<String> getParametersForEachProcess(ProcessItem rap, Context a/*Activity a*/) {

        Context/*Activity*/ ctx = a;
        DynamicProcessesList = new ArrayList<String>();
        String[] status = readProcStatus(rap.procInfo.pid);
        /*String strDate = "";

        strDate = getDate();
        //Log.e("date", strDate + "  " + timeZone());
        DynamicProcessesList.add("Timestamp," + strDate);
        DynamicProcessesList.add("TimeZone," + timeZone());*/


        DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.proc_name) + "," + rap.procInfo.processName);
        DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.pid) + "," + rap.procInfo.pid);
        DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.show_memory_usage) + "," + rap.mem);
        DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.show_cpu_usage) + "," + rap.cputime);
        if (status == null)
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.uid) + "," + "");
        else
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.uid) + "," + status[1]);

        if (status == null)
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.gid) + "," + "");
        else
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.gid) + "," + status[2]);

        if (status == null)
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.state) + "," + "");
        else
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.state) + "," + status[0]);

        if (status == null)
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.threads) + "," + "");

        else
            DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.threads) + "," + status[3]);

        DynamicProcessesList.add("StParameterDynamic-" + a.getString(R.string.importance) + "," + rap.procInfo.importance);

        /*StringBuffer sb = new StringBuffer( ).append( "<small>" )
                .append( a.getString( R.string.proc_name ) )
                .append( ": " )
                .append( rap.procInfo.processName )
                .append( "<br>" )
                .append( a.getString( R.string.pid ) )
                .append( ": " )
                .append( rap.procInfo.pid )
                .append( "<br>" )
                .append( a.getString(R.string.show_memory_usage ))
                .append( ": " )
                .append( rap.mem )
                .append( "<br>" )
                .append( a.getString(R.string.show_cpu_usage ))
                .append( ": " )
                .append( rap.cputime )
                .append( "<br>" )
                .append( a.getString( R.string.uid ) )
                .append( ": " )
                .append( status == null ? "" : status[1] )
                .append( "<br>" )
                .append( a.getString( R.string.gid ) )
                .append( ": " )
                .append( status == null ? "" : status[2] )
                .append("<br>")
                .append( a.getString( R.string.state ) )
                .append( ": " )
                .append( status == null ? "" : status[0] )
                .append( "<br>" )
                .append( a.getString( R.string.threads ) )
                .append( ": " )
                .append(status == null ? "" : status[3])
                .append( "<br>" )
                .append( a.getString( R.string.importance ) )
                .append( ": " )
                .append( rap.procInfo.importance )
                .append( "<br>LRU: " )
                .append( rap.procInfo.lru )
                .append( "<br>" )
                .append( a.getString( R.string.pkg_name ) )
                .append(": ");

        if ( rap.procInfo.pkgList != null )
        {
            int i = 0;

            for ( String pkg : rap.procInfo.pkgList )
            {
                if ( pkg != null )
                {
                    if ( i > 0 )
                    {

                        sb.append( ", " );
                    }
                    sb.append( pkg );
                    //DynamicProcessesList.add("StParameterDynamicProcess LRU-"+ a.getString( R.string.pkg_name)+","+pkg);


                    i++;
                }
            }
        }
*/
        return DynamicProcessesList;

    }


    static ArrayList<String> getIgnoreList(SharedPreferences sp) {
        if (sp == null) {
            return null;
        }

        String listVal = sp.getString(PREF_KEY_IGNORE_LIST, null);

        if (listVal == null || listVal.length() == 0) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(listVal);
        ArrayList<String> list = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }

        return list.size() == 0 ? null : list;
    }

    public String getImportance(RunningAppProcessInfo proc) {
        String impt = "Empty"; //$NON-NLS-1$

        switch (proc.importance) {
            case RunningAppProcessInfo.IMPORTANCE_BACKGROUND:
                impt = "Background"; //$NON-NLS-1$
                break;
            case RunningAppProcessInfo.IMPORTANCE_FOREGROUND:
                impt = "Foreground"; //$NON-NLS-1$
                break;
            case RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE:
                impt = "Perceptible"; //$NON-NLS-1$
                break;
            case RunningAppProcessInfo.IMPORTANCE_SERVICE:
                impt = "Service"; //$NON-NLS-1$
                break;
            case RunningAppProcessInfo.IMPORTANCE_VISIBLE:
                impt = "Visible"; //$NON-NLS-1$
                break;
        }

        return impt;
    }


    /**
     * @return [State, UID, GID, Threads]
     */
    private static String[] readProcStatus(int pid) {
        BufferedReader reader = null;
        String s = "/proc/" + pid + "/status";

        File f = new File(s);
        if (f.exists() && !f.isDirectory()) {
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/"
                        + pid
                        + "/status")),
                        1024);

                String line;
                String stateMsg = "";
                String uidMsg = "";
                String gidMsg = "";
                String threadsMsg = "";

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("State:")) //$NON-NLS-1$
                    {
                        if (line.length() > 6) {
                            stateMsg = line.substring(6).trim();
                        }
                    } else if (line.startsWith("Uid:")) {
                        if (line.length() > 4) {
                            uidMsg = line.substring(4).trim();

                            int idx = uidMsg.indexOf('\t');
                            if (idx != -1) {
                                uidMsg = uidMsg.substring(0, idx);
                            } else {
                                idx = uidMsg.indexOf(' ');
                                if (idx != -1) {
                                    uidMsg = uidMsg.substring(0, idx);
                                }
                            }
                        }
                    } else if (line.startsWith("Gid:")) //$NON-NLS-1$
                    {
                        if (line.length() > 4) {
                            gidMsg = line.substring(4).trim();

                            int idx = gidMsg.indexOf('\t');
                            if (idx != -1) {
                                gidMsg = gidMsg.substring(0, idx);
                            } else {
                                idx = gidMsg.indexOf(' ');
                                if (idx != -1) {
                                    gidMsg = gidMsg.substring(0, idx);
                                }
                            }
                        }
                    } else if (line.startsWith("Threads:")) //$NON-NLS-1$
                    {
                        if (line.length() > 8) {
                            threadsMsg = line.substring(8).trim();
                        }
                    }
                }

                return new String[]{
                        stateMsg, uidMsg, gidMsg, threadsMsg
                };
            } catch (Exception e) {
                Log.e(ParamProcesses.class.getName(), e.getLocalizedMessage(), e);
                Log.e("Error", "ReadStatus");
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ie) {
                        Log.e(ParamProcesses.class.getName(),
                                ie.getLocalizedMessage(),
                                ie);
                    }
                }
            }
        }
        return null;
    }


    void updateProcess(List<RunningAppProcessInfo> list, /*Activity*/Context a, ISentaSecurePrefs sentaSecurePrefs) {
        /*Activity*/
        Context ctx = a;

        boolean showCpu = sentaSecurePrefs.getBooleanOption(ctx,
                PSTORE_PROCESSMANAGER,
                PREF_KEY_SHOW_CPU);

        if (showCpu) {
            long[] loads = readCpuLoad();

            long newload = loads == null ? 0 : (loads[0] + loads[1]);

            if (totalLoad != 0) {
                totalDelta = newload - totalLoad;
            }

            totalLoad = newload;

            long newWork = loads == null ? 0 : loads[0];

            if (totalWork != 0) {
                workDelta = newWork - totalWork;
            }

            totalWork = newWork;
        }

        synchronized (procCache) {
            procCache.procList.clear();

            if (list != null) {
                int ignoreAction = sentaSecurePrefs.getIntOption(ctx,
                        PSTORE_PROCESSMANAGER,
                        PREF_KEY_IGNORE_ACTION,
                        IGNORE_ACTION_HIDDEN);
                boolean showMem = sentaSecurePrefs.getBooleanOption(ctx,
                        PSTORE_PROCESSMANAGER,
                        PREF_KEY_SHOW_MEM);
                boolean showSys = sentaSecurePrefs.getBooleanOption(ctx,
                        PSTORE_PROCESSMANAGER,
                        PREF_KEY_SHOW_SYS_PROC);

                String name;
                boolean isSys;

                for (int i = 0, size = list.size(); i < size; i++) {
                    RunningAppProcessInfo rap = list.get(i);

                    name = rap.processName;

                    isSys = Utilities.isSysProcess(name);

                    if (isSys && !showSys) {
                        continue;
                    }

                    if (ignoreAction == IGNORE_ACTION_HIDDEN
                            && ignoreList.contains(name)) {
                        continue;
                    }

                    ProcessItem pi = procCache.resCache.get(name);

                    if (pi == null) {
                        pi = new ProcessItem();
                        pi.procInfo = rap;
                        pi.sys = isSys;
                    } else {
                        pi.procInfo = rap;
                        pi.sys = isSys;
                        pi.lastcputime = pi.cputime;
                    }

                    if (rap.pid != 0 && (showMem || showCpu)) {

                        readProcessStat(ctx, buf, pi, showMem, showCpu);
                    }

                    procCache.procList.add(pi);
                }

                procCache.reOrder(sentaSecurePrefs.getIntOption(ctx,
                        PSTORE_PROCESSMANAGER,
                        PREF_KEY_SORT_ORDER_TYPE,
                        ORDER_TYPE_NAME), sentaSecurePrefs.getIntOption(ctx,
                        PSTORE_PROCESSMANAGER,
                        PREF_KEY_SORT_DIRECTION,
                        ORDER_ASC));
            }
        }
    }


    /**
     * @return [worktime, idletime]
     */
    static long[] readCpuLoad() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")), //$NON-NLS-1$
                    256);

            String line = reader.readLine();

            if (line != null && line.startsWith("cpu ")) //$NON-NLS-1$
            {
                line = line.substring(3).trim();

                StringTokenizer tokens = new StringTokenizer(line);

                long totaltime = 0, idletime = 0;
                int i = 0;
                String tk;

                while (tokens.hasMoreTokens() && i < 7) {
                    tk = tokens.nextToken();

                    if (i == 3) {
                        idletime = Long.parseLong(tk);
                    } else {
                        totaltime += Long.parseLong(tk);
                    }
                    i++;
                }

                return new long[]{
                        totaltime, idletime
                };
            }
        } catch (Exception ingored) {

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ingored) {

                }
            }
        }

        return null;
    }


    private static void readProcessStat(Context ctx, byte[] buf,
                                        ProcessItem pi, boolean showMem, boolean showCpu) {

        File f = new File("/proc/" + pi.procInfo.pid + "/stat");
        if (f.exists() && !f.isDirectory()) {

            InputStream is = null;
            try {

                is = new FileInputStream("/proc/" + pi.procInfo.pid + "/stat");


                ByteArrayOutputStream output = new ByteArrayOutputStream();

                int len;

                while ((len = is.read(buf)) != -1) {
                    output.write(buf, 0, len);
                }

                output.close();

                String line = output.toString();

                if (line != null) {
                    line = line.trim();

                    int idx = line.lastIndexOf(')');

                    if (idx != -1) {
                        line = line.substring(idx + 1).trim();

                        StringTokenizer tokens = new StringTokenizer(line);

                        String rss = null;
                        String utime = null;
                        String stime = null;

                        long nrss;
                        int i = 0;
                        String tk;

                        // [11,12,21] for [utime,stime,rss]
                        while (tokens.hasMoreTokens()) {
                            tk = tokens.nextToken();

                            if (i == 11) {
                                utime = tk;
                            } else if (i == 12) {
                                stime = tk;
                            } else if (i == 21) {
                                rss = tk;
                            }

                            if (rss != null) {
                                break;
                            }

                            i++;
                        }

                        if (showCpu) {
                            if (utime != null) {
                                pi.cputime = Long.parseLong(utime);
                            }

                            if (stime != null) {
                                pi.cputime += Long.parseLong(stime);
                            }
                        }

                        if (showMem && rss != null) {
                            nrss = Long.parseLong(rss);

                            if (pi.rss != nrss || pi.mem == null) {
                                pi.rss = nrss;

                                pi.mem = Formatter.formatFileSize(ctx,
                                        pi.rss * 4 * 1024);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(ParamProcesses.class.getName(), e.getLocalizedMessage(), e);
                Log.e("error", "readprocess");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.e("Not found", "readprocess");
                    }
                }
            }
        }
    }


    /**
     * ProcessItem
     */
    public static final class ProcessItem {

        RunningAppProcessInfo procInfo;

        String label;

        Drawable icon;

        boolean sys;

        long rss;

        String mem;

        long cputime;

        long lastcputime;

        ProcessItem() {

        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ProcessItem)) {
                return false;
            }

            return this.procInfo.pid == ((ProcessItem) o).procInfo.pid;
        }
    }


    /**
     * ProcessCache
     */
    private static final class ProcessCache {

        HashMap<String, ProcessItem> resCache;

        ArrayList<ProcessItem> procList;

        ProcessCache() {
            resCache = new HashMap<String, ProcessItem>();
            procList = new ArrayList<ProcessItem>();
        }

        synchronized void clear() {
            resCache.clear();
            procList.clear();
        }

        synchronized ArrayList<ProcessItem> generateLocalList() {
            ArrayList<ProcessItem> local = new ArrayList<ProcessItem>();

            local.addAll(procList);

            return local;
        }

        synchronized void reOrder(int type, final int direction) {
            switch (type) {
                case ORDER_TYPE_NAME:
                    Collections.sort(procList, new Comparator<ProcessItem>() {

                        Collator clt = Collator.getInstance();

                        public int compare(ProcessItem obj1, ProcessItem obj2) {
                            String lb1 = obj1.label == null ? obj1.procInfo.processName
                                    : obj1.label;
                            String lb2 = obj2.label == null ? obj2.procInfo.processName
                                    : obj2.label;

                            return clt.compare(lb1, lb2) * direction;
                        }
                    });
                    break;
                case ORDER_TYPE_IMPORTANCE:
                    Collections.sort(procList, new Comparator<ProcessItem>() {

                        public int compare(ProcessItem obj1, ProcessItem obj2) {
                            // result should be reversed
                            return (obj2.procInfo.importance - obj1.procInfo.importance)
                                    * direction;

                        }
                    });
                    break;
                case ORDER_TYPE_MEM:
                    Collections.sort(procList, new Comparator<ProcessItem>() {

                        public int compare(ProcessItem obj1, ProcessItem obj2) {
                            return (obj1.rss == obj2.rss ? 0
                                    : (obj1.rss < obj2.rss ? -1 : 1))
                                    * direction;
                        }
                    });
                    break;
                case ORDER_TYPE_CPU:
                    Collections.sort(procList, new Comparator<ProcessItem>() {

                        public int compare(ProcessItem obj1, ProcessItem obj2) {
                            long c1 = obj1.lastcputime == 0 ? 0
                                    : (obj1.cputime - obj1.lastcputime);
                            long c2 = obj2.lastcputime == 0 ? 0
                                    : (obj2.cputime - obj2.lastcputime);
                            return (c1 == c2 ? 0 : (c1 < c2 ? -1 : 1))
                                    * direction;
                        }
                    });
                    break;
            }
        }
    }

}


