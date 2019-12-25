package com.android.xlwlibrary.threadpool;

import java.util.Comparator;

/**
 * Created by Administrator on 2018/10/15.
 */

public class ComparePriority<T extends RunWithPriority> implements Comparator<T> {

    @Override
    public int compare(T lhs, T rhs) {
        return rhs.getPriority() - lhs.getPriority();
    }
}
