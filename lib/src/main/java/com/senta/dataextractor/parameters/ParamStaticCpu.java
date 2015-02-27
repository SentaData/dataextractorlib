package com.senta.dataextractor.parameters;

import android.os.Build;

import com.senta.dataextractor.xallegro.api.StWsClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class ParamStaticCpu {


    private static final String SCALE_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private static final String CPU_INFO = "/proc/cpuinfo";
    private volatile static boolean ThreadStopped = false;
    private static String url = "", B64XmlString = "";
    private static StWsClient ClientConn = new StWsClient();
    private static ParamStaticPhone a;

    public ParamStaticCpu() {
    }


    public static String timeZone() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                Locale.getDefault());
        Date currentLocalTime = calendar.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        String localTime = date.format(currentLocalTime);

        return localTime;
    }

    public ArrayList<String> getStaticCPUInfo() {

        //String strDate = "";
        ArrayList<String> info_list = new ArrayList<String>();

        /*Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        strDate = sdf.format(c.getTime());


        info_list.add("Timestamp," + strDate);
        info_list.add("TimeZone," + timeZone());*/

        String[] stat = getCpuState();

        if (stat != null && stat.length == 2) {
            info_list.add("StParameterStatic-Model," + stat[0]);
            if (stat[1] != null) {
                stat[1] = stat[1].replaceAll("\\s", "");
                info_list.add("StParameterStatic-CurrentFrequency," + stat[1]);
            }
        }

        String cpuMin = readFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq", true);
        String cpuMax = readFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", true);
        String scaleMin = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq", true);
        String scaleMax = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq", true);
        String governor = readFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", false);

        if (cpuMin != null && cpuMax != null) {
            info_list.add("StParameterStatic-CPU Frequency Range," + cpuMin + " - " + cpuMax);
        }

        if (scaleMin != null && scaleMax != null) {
            info_list.add("StParameterStatic-Scaling Range," + scaleMin + " - " + scaleMax);
        }

        if (governor != null) {
            info_list.add("StParameterStatic-Scaling Governor," + governor);
        }

        info_list.add("StParameterStatic-ABI," + Build.CPU_ABI);

        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.length() != 0) {
                        aLine = aLine.replace(":", ",");
                        aLine = aLine.replaceAll("\\s", "");
                        info_list.add("StParameterStatic-" + aLine);
                    }
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info_list;
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

}
