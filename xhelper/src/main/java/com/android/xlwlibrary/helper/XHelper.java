package com.android.xlwlibrary.helper;

import android.content.Context;

import com.android.xlwlibrary.helper.disk.XDiskLruCacheHelper;

/**
 * Created by xu on 2020/3/3.
 */
public class XHelper {
    public  XBaseActivityHelper xBaseActivityHelper;
    public  XBitmapHelper xBitmapHelper;
    public XCrashHandlerHelper xCrashHandlerHelper;
    public XDiskLruCacheHelper xDiskLruCacheHelper;
    public XDisplayHelper xDisplayHelper;
    public XEventBusHelper xEventBusHelper;
    public XFileHelper xFileHelper;
    public XFragmentHelper xFragmentHelper;
    public XGetTimeHelper xGetTimeHelper;
    public XGsonHelper xGsonHelper;
    public XScreenHelper xScreenHelper;
    public XLanguageHelper xLanguageHelper;
    public XLogHelper xLogHelper;
    public XNetworkHelper xNetworkHelper;
    public XOperationHelper xOperationHelper;
    public XPcmToWav xPcmToWav;
    public XShellHelper xShellHelper;
    public XStringHelper xStringHelper;
    public XZipHelper xZipHelper;
    public XSystemHelper xSystemHelper;
    public XThreadPoolHelper xThreadPoolHelper;
    private static XHelper xHelper = null;
    private static Context mContext;

    public static void  install(Context context){
        if (mContext==null){
            throw new RuntimeException("Context is null,If you want to use this plug-in, context must not be empty");
        }
        mContext=context;
    }

    private XHelper(){
        xThreadPoolHelper=new XThreadPoolHelper();
        xSystemHelper=new XSystemHelper();
        xZipHelper=new XZipHelper();
        xStringHelper=new XStringHelper();
        xShellHelper=new XShellHelper();
        xPcmToWav=new XPcmToWav();
        xBaseActivityHelper= new XBaseActivityHelper();
        xBitmapHelper=new XBitmapHelper();
        xCrashHandlerHelper=new XCrashHandlerHelper(mContext);
        xDiskLruCacheHelper=new XDiskLruCacheHelper();
        xDisplayHelper=new XDisplayHelper(mContext);
        xEventBusHelper=new XEventBusHelper();
        xFileHelper=new XFileHelper(mContext);
        xFragmentHelper=new XFragmentHelper();
        xGetTimeHelper=new XGetTimeHelper();
        xGsonHelper=new XGsonHelper();
        xScreenHelper=new XScreenHelper();
        xLanguageHelper=new XLanguageHelper();
        xLogHelper=new XLogHelper();
        xNetworkHelper=new XNetworkHelper();
        xOperationHelper=new XOperationHelper();
    }

    public static XHelper defaultXHelper(){
        if (mContext==null){
            throw new RuntimeException("Context is null,If you want to use this plug-in, context must not be empty");
        }
        if (xHelper ==null){
            synchronized (XBaseActivityHelper.class){
                if (xHelper ==null){
                    xHelper =new XHelper();
                }
            }
        }
        return xHelper;
    }
}
