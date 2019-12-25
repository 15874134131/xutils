package com.android.xlwlibrary.helper;

import com.android.xlwlibrary.bean.Event;

import org.greenrobot.eventbus.EventBus;

//封装EventBus
public class XEventBusHelper {
    //此代码写在baseactivity 的oncreate() 生命周期中 ，判断用户是否注册了EventBus
	/*if (isRegisterEventBus()) {
        XEventBusHelper.register(this);
    }*/

    //写在baseactivity 中，isRegisterEventBus 判断是否注册，
    /*@Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBusCome(Event event) {
        if (event != null) {
            receiveEvent(event);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onStickyEventBusCome(Event event) {
        if (event != null) {
            receiveStickyEvent(event);
        }
    }
    *//**
     * 接收到分发到事件
     * @param event 事件
     *//*
    protected void receiveEvent(Event event) {
    }
    *//**
     * 接受到分发的粘性事件
     * @param event 粘性事件
     *//*
    protected void receiveStickyEvent(Event event) {
    }
    *//**
     * @return true绑定EventBus事件分发，默认不绑定，子类需要绑定的话复写此方法返回true.
     *//*
    protected boolean isRegisterEventBus() {
        return false;
    }*/

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }
    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }
    public static void sendEvent(Event event) {
        EventBus.getDefault().post(event);
    }
    public static void sendStickyEvent(Event event) {
        EventBus.getDefault().postSticky(event);
    }
}