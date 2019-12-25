package com.android.xlwlibrary.helper;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Locale;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by xu on 2019/10/16.
 */
public class XSystemHelper {
    /**
     * 返回当前程序版本名,获取的是
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        int  versioncode=0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 获取当前应用的版本号
     * @param context
     * @return
     * @throws Exception
     */
    public static int getVersionCode(Context context) throws Exception
    {
        int versionCode=0;
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo pi = packageManager.getPackageInfo(context.getPackageName(),0);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.P){
                versionCode= (int) pi.getLongVersionCode();
            }else {
                versionCode = pi.versionCode;
            }
            Log.d("TAG","longVersionCode:"+versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e){
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 打开扬声器
     * @param mContext
     * @return
     */
    public static int  openSpeaker(Context mContext) {
        int currVolume=0;
        try{
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            currVolume=audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            if(!audioManager.isSpeakerphoneOn()) {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(true);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL ),
                        AudioManager.STREAM_VOICE_CALL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currVolume;
    }

    /**
     * 关闭扬声器
     * @param mContext
     */
    public static void closeSpeaker(Context mContext) {
        try {
            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            int current = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL );
            if(audioManager != null) {
                if(audioManager.isSpeakerphoneOn()) {
                    //把声音设定成Earpiece（听筒）出来，设定为正在通话中
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,current,
                            AudioManager.STREAM_VOICE_CALL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断服务是否在运行
     * @param context
     * @param serviceName
     * @return
     * 服务名称为全路径 例如com.ghost.WidgetUpdateService
     */
    public static boolean isRunService(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取CPU信息.
     *
     * @return "CPU核心个数 x CPU频率"
     */
    public static String getCpuInfo() {
        return getCpuCoreCount() + " x " + getCpuFrequency();
    }

    /**
     * 获取CPU核心个数.
     *
     * @return
     */
    private static int getCpuCoreCount() {
        int coreCount = 1;
        try {
            String cpuDiePath = "/sys/devices/system/cpu";
            File dir = new File(cpuDiePath);
            String[] cpuFiles = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return Pattern.matches("cpu\\d{1}", name);
                }
            });
            if (cpuFiles != null && cpuFiles.length > 0) {
                coreCount = cpuFiles.length;
            }
        } catch (Exception e) {
//			DebugUtils.error(e.getMessage(), e);
        }
        return coreCount;
    }

    /**
     * 获取CPU频率.
     *
     * @return
     */
    private static String getCpuFrequency() {
        String cpuFreq = "";
        BufferedReader bufferedReader = null;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            ProcessBuilder cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            cpuFreq = bufferedReader.readLine();
            // convert from Kb to Gb
            float tempFreq = Float.valueOf(cpuFreq.trim());
            cpuFreq = tempFreq / (1000 * 1000) + "Gb";
            return cpuFreq;
        } catch (Exception e) {
            return XStringHelper.isEmpty(cpuFreq) ? "N/A" : cpuFreq + "Kb";
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }
    }

    /**
     * 获得系统总内存大小.
     *
     * @param context
     * @return
     */
    public static String getSystemTotalMemory(Context context) {
        // 系统内存信息文件
        String memInfoFilePath = "/proc/meminfo";
        String firstLine;
        String[] arrayOfString;
        long initialMemory = 0;
        BufferedReader localBufferedReader = null;
        try {
            FileReader localFileReader = new FileReader(memInfoFilePath);
            localBufferedReader = new BufferedReader(
                    localFileReader, 10240);
            // 读取meminfo第一行, 系统总内存大小
            firstLine = localBufferedReader.readLine();
            arrayOfString = firstLine.split("\\s+");
            // 获得系统总内存, 单位是KB, 乘以1024转换为Byte
            initialMemory = Long.valueOf(arrayOfString[1].trim()) * 1024;
        } catch (Exception e) {
//			DebugUtils.error(e.getMessage(), e);
            // ignore.
        } finally {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        // 内存大小规格化, Byte转换为KB或者MB
        return Formatter.formatFileSize(context, initialMemory);
    }

    /**
     * 获取系统当前可用内存.
     *
     * @param context
     * @return
     */
    public static String getSystemAvailMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        // 内存大小规格化, Byte转换为KB或者MB
        return Formatter.formatFileSize(context, memoryInfo.availMem);
    }

    /**
     * 读取RAW文件内容
     *
     * @param resid
     * @param encoding
     * @return
     */
    public static String getRawFileContent(int resid, String encoding, Context mContext) {
        InputStream is = null;
        Context context = mContext;
        try {
            is = context.getResources().openRawResource(resid);
        } catch (Exception e) {
        }
        if (is != null) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int current = 0;

            try {
                while ((current = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, current);
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return "";
    }

    /**
     * 获取设备序列号.
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getDeviceId(Context context) {
        String deviceId="";
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deviceId=telephonyManager.getImei();
            if (deviceId.equals("")){
                telephonyManager.getMeid();
            }
        }else {
            deviceId=telephonyManager.getDeviceId();
        }

        return deviceId;
    }

    /**
     * 获取CPU序列号
     * @return CPU序列号(16位) 读取失败为"0000000000000000"
     */
    public static String getCPUSerial() {
        String str = "", strCPU = "", cpuAddress = "0000000000000000";
        try {
            // 读取CPU信息
            Process pp = Runtime. getRuntime().exec("cat/proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            // 查找CPU序列号
            for ( int i = 1; i < 100; i++) {
                str = input.readLine();
                if (str != null) {
                    // 查找到序列号所在行
                    if (str.indexOf( "Serial") > -1) {
                        // 提取序列号
                        strCPU = str.substring(str.indexOf(":" ) + 1, str.length());
                        // 去空格
                        cpuAddress = strCPU.trim();
                        break;
                    }
                } else {
                    // 文件结尾
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return cpuAddress;
    }
}
