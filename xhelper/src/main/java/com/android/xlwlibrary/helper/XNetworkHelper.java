package com.android.xlwlibrary.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class XNetworkHelper {
    /**
     * 仅仅判断网络是否有连接,
     * @return
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo != null && mNetworkInfo.isConnected();
    }

    /**
     * 判断 wifi 数据是否可用
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isWifiAvailable(Context context) {
        return isWifiEnabled(context) && isAvailableByPing("www.baidu.com");
    }

    /**
     * 判断 wifi 是否连接
     * 仅仅判断是否连接，因为有些时候连接了，也不一定能访问网络
     * @return {@code true}: 连接<br>{@code false}: 未连接
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    public boolean isWifiConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWiFiNetworkInfo != null) {
            return mWiFiNetworkInfo.isConnected();
        }
        return false;
    }

    /**
     * 判断 wifi 是否打开
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isWifiEnabled(Context context) {
        @SuppressLint("WifiManagerLeak")
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


    /**
     * 打开或关闭 wifi
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     */
    @SuppressLint("MissingPermission")
    public void setWifiEnabled(boolean enabled,Context context) {
        @SuppressLint("WifiManagerLeak")
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enabled) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        } else {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
        }
    }

    /**
     * 判断MOBILE网络是否连接
     * 和上面WIFI开关打开之后不一定会有网络流通的原理是一样的，打开数据流量开关，但是不一定有网络流通，该方法有歧义，所以建议不采用
     * @param context
     * @return
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    public boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            @SuppressLint("MissingPermission") NetworkInfo mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isConnected();
            }
        }
        return false;
    }

    /**
     * 判断移动网络数据是否可用
     *原理跟上面的wifi 是一样的，
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public boolean isMobileDataAvailable(Context context) {
        return isMobileDataEnabled(context) && isAvailableByPing("www.baidu.com");
    }

    /**
     * 判断移动数据是否打开
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public boolean isMobileDataEnabled(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("getDataEnabled");
            if (getMobileDataEnabledMethod != null) {
                return (boolean) getMobileDataEnabledMethod.invoke(tm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 打开或关闭移动数据
     *
     * @param enabled {@code true}: 打开<br>{@code false}: 关闭
     * 测试之后，发现这个方法无效，看了源码之后发现反射的方法名和参数是没有问题的
     * 但是源码中多了一个“修改网络状态”的权限，这个权限在Android 5.0之后就升级为系统权限，
     * 普通的app是没有权限修改手机状态的，解决方案是，将app打上系统签名
     */
    public void setMobileDataEnabled(boolean enabled,Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method setMobileDataEnabledMethod = tm.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod) {
                setMobileDataEnabledMethod.invoke(tm, enabled);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取网络运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 运营商名称
     */
    public String getNetworkOperatorName(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getNetworkOperatorName() : null;
    }

    /**
     * 网络类型
     */
    public enum NetworkType {
        // wifi
        NETWORK_WIFI,
        // 4G 网
        NETWORK_4G,
        // 3G 网
        NETWORK_3G,
        // 2G 网
        NETWORK_2G,
        // 未知网络
        NETWORK_UNKNOWN,
        // 没有网络
        NETWORK_NO
    }
    /**
     * 获取当前网络类型
     * @return 网络类型
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    public static NetworkType getNetworkType(Context context) {
        NetworkType netType = NetworkType.NETWORK_NO;
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                netType = NetworkType.NETWORK_WIFI;
            } else if (mNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                switch (mNetworkInfo.getSubtype()) {
                    case TelephonyManager.NETWORK_TYPE_GSM:
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        netType = NetworkType.NETWORK_2G;
                        break;

                    case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        netType = NetworkType.NETWORK_3G;
                        break;
                    case TelephonyManager.NETWORK_TYPE_IWLAN:
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        netType = NetworkType.NETWORK_4G;
                        break;
                    default:

                        String subtypeName = mNetworkInfo.getSubtypeName();
                        //  中国移动 联通 电信 三种 3G 制式
                        if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
                            netType = NetworkType.NETWORK_3G;
                        } else {
                            netType = NetworkType.NETWORK_UNKNOWN;
                        }
                        break;
                }
            } else {
                netType = NetworkType.NETWORK_UNKNOWN;
            }
        }
        return netType;
    }

    /**
     * 通过 wifi 获取本地 IP 地址
     * @return IP 地址
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    @SuppressLint("MissingPermission")
    public String getIpAddressByWifi(Context context) {
        // 获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }
    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 获取 IP 地址
     *
     * @param useIPv4 是否用 IPv4
     * @return useIPv4为true返回IPv4，为false返回IPv6
     */
    public String getIPAddress(boolean useIPv4) {
        try {
            for (Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces(); nis.hasMoreElements(); ) {
                NetworkInterface ni = nis.nextElement();
                // 防止小米手机返回 10.0.2.15
                if (!ni.isUp()) continue;
                for (Enumeration<InetAddress> addresses = ni.getInetAddresses(); addresses.hasMoreElements(); ) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String hostAddress = inetAddress.getHostAddress();
                        boolean isIPv4 = hostAddress.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4) return hostAddress;
                        } else {
                            if (!isIPv4) {
                                int index = hostAddress.indexOf('%');
                                return index < 0 ? hostAddress.toUpperCase() : hostAddress.substring(0, index).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据域名获取IP
     * @param domain 域名
     * @return IP 地址
     * 注意，该方法一定要在子线程中运行，否则会报错
     */
    public String getDomainAddress(@Nullable String domain) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(domain);
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断网络是否可用
     * @param ipORdomain 传入网址，用于ping ，如果能ping 通，说明可以进行网络访问
     * @return
     */
    public static boolean isAvailableByPing(String ipORdomain) {
        if (TextUtils.isEmpty(ipORdomain)) {
            return false;
        }
        XShellHelper.CommandResult result = XShellHelper.execCmd(String.format("ping -c 1 %s", ipORdomain), false);
        boolean ret = result.result == 0;
        if (result.successMsg != null) {
            Log.d("yunchong", "isAvailableByPing() called" + result.successMsg);
        }
        if (result.errorMsg != null) {
            Log.d("yunchong", "isAvailableByPing() called" + result.errorMsg);
        }
        return ret;
    }

    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public static String getMacAddress(Context context) {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else {
            mac = getMacFromHardware();
        }
        return mac;
    }

    public static String getMAddress() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     * @param context
     * @return
     * 需要<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
     */
    @SuppressLint("MissingPermission")
    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }
}
