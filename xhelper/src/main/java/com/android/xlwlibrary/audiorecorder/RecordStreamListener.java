package com.android.xlwlibrary.audiorecorder;

/**
 * Created by Administrator on 2018/12/19.
 */

public interface RecordStreamListener {
    void recordOfByte(byte[] data, int begin, int end);
    void setCallbackStatus(Status status);
}
