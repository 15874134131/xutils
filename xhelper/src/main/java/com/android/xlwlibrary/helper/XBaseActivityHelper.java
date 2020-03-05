package com.android.xlwlibrary.helper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by computer on 2017/7/31.
 */

public class XBaseActivityHelper {
    private List<Activity> activityList=new ArrayList<>();
    public XBaseActivityHelper() {
        /**
         * 这里面写一些需要执行初始化的工作
         */
    }

    private static Stack<Activity> activityStack;

    //添加新的activity
    public void addActivity(Activity activity){
        activityList.add(activity);
    }

    //关闭单个
    public void removeActivity(Activity activity){
        if (activity!=null) {
            activityList.remove(activity);
            activity.finish();
        }
    }


    public void exit(Class<?> cls){
        for (Activity activity:activityList){
            if (activity!=null){
                if (cls==activity.getClass()){
                }else {
                    activity.finish();
                }
            }
        }
    }

    //关闭所有
    public void exitAll(){
        for (Activity activity:activityList){
            if (activity!=null){
                activity.finish();
            }
        }
    }

    //结束指定名称的activity
    public void finishActivity(Class<?> cls ){
        if (activityList!=null){
            for (Activity activity:activityList){
                if (activity.getClass().equals(cls)){
                    activityList.remove(cls);
                    removeActivity(activity);
                    break;
                }
            }
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity(){
        Activity activity=activityList.get(activityList.size() - 1);
        return activity;
    }
}
