package com.android.xlwlibrary.mvpbase;

public interface XIbaseView {
    /**
     * 显示正在加载 view
     */
    void showLoading();

    /**
     * 关闭正在加载 view
     */
    void hideLoading();
    /**
     * mvp 错误处理
     *
     * @param action 区分不同事件
     * @param code   错误码
     * @param msg    错误信息
     */
    void mvpError(String action, int code, String msg);
}
