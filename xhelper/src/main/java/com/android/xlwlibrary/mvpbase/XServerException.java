package com.android.xlwlibrary.mvpbase;

/**
 * 自定义服务器错误
 *
 * @author xu
 */
public class XServerException extends RuntimeException {
    private int code;
    private String msg;

    public XServerException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
