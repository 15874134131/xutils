package com.android.xlwlibrary.helper;


/**
 * Created by xu on 2018/3/26.
 * 设置LEVEL 等级，需要显示什么级别的log 则设置LEVEL
 */
public  class XLogHelper {
    private static final int VERBOSE=1;
    private static final int DEBUG=2;
    private static final int INFO=3;
    private static final int WARN=4;
    private static final int ERROR=6;
    private static final int NOTHING=7;
    private static int LEVEL=ERROR;

    public void setLEVEL(int level){
        this.LEVEL=level;
    }

    public static void v(String tag, String msg){
        if (LEVEL<=VERBOSE){
            android.util.Log.v(tag,msg);
        }
    }

    public static void d(String tag, String msg){
        if (LEVEL<=DEBUG){
            android.util.Log.d(tag,msg);
        }
    }
    public static void i(String tag, String msg){
        if (LEVEL<=INFO){
            android.util.Log.i(tag,msg);
        }
    }
    public static void w(String tag, String msg){
        if (LEVEL<=WARN){
            android.util.Log.w(tag,msg);
        }
    }
    public static void e(String tag, String msg){
        if (LEVEL<=ERROR){
            android.util.Log.e(tag,msg);
        }
    }
}
