package com.android.xlwlibrary.helper;

import android.content.Context;

/**
 * Created by xu on 2019/8/27.
 */
public class XlwForeignInterface {
    private Context mContext;
    public XFileHelper XFileHelper;
    public XGetTimeHelper XGetTimeHelper;
    private volatile static XlwForeignInterface xlwForeignInterface;
    /**
     * 构造器初始化
     */
    private XlwForeignInterface(Context context){
        this.mContext=context;
        XFileHelper =new XFileHelper();
        XGetTimeHelper =new XGetTimeHelper();
    }

    /**
     * 单例
     */
    public static XlwForeignInterface getInstance(Context context) {
        if (null == xlwForeignInterface) {
            synchronized (XlwForeignInterface.class) {
                if (null == xlwForeignInterface) {
                    xlwForeignInterface = new XlwForeignInterface(context);
                }
            }
        }
        return xlwForeignInterface;
    }
}
