package com.android.xlwlibrary.helper;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/11/21.
 */

public class XToastHelper {

    public static void showShort(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLong(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
