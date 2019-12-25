package com.android.xlwlibrary.bean;

/**
 * Created by xu on 2019/9/4.
 */
public class Event {
    private String code;
    private Object object;

    public Event(String code, Object object) {
        this.code = code;
        this.object = object;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
