package com.android.xlwlibrary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.android.xlwlibrary.callback.IntervalEdittextCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xu on 2019/12/11.
 * 该类主要用于输入手机号时的特殊UI要求,类似于 137 1354 1234
 */
public class XIntervalEdittext extends AppCompatEditText implements TextWatcher {
    // 特殊下标位置
    private static final int PHONE_INDEX_3 = 3;
    private static final int PHONE_INDEX_4 = 4;
    private static final int PHONE_INDEX_8 = 8;
    private static final int PHONE_INDEX_9 = 9;
    //private Paint mPaint;
    private IntervalEdittextCallback callback;

    public void setIntervalEdittextCallback(IntervalEdittextCallback intervalEdittextCallback){
        this.callback= intervalEdittextCallback;
    }

    public XIntervalEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        //mPaint = new Paint();
/*        mPaint.setStyle(Paint.Style.STROKE);
        // 你可以根据自己的具体需要在此处对画笔做更多个性化设置
        mPaint.setColor(ContextCompat.getColor(context, R.color.xian));
        mPaint.setStrokeWidth(3);*/
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画底线
       /* canvas.drawLine(0, this.getHeight() - 2, this.getWidth() - 2,
                this.getHeight() - 2, mPaint);*/
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    //当文本发生变化时
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        //如果文本为空
        if (callback!= null && (s == null || s.length() == 0)) {
            callback.onEdEmpty();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i != PHONE_INDEX_3 && i != PHONE_INDEX_8 && s.charAt(i) == ' ') {
                continue;
            } else {
                sb.append(s.charAt(i));
                if ((sb.length() == PHONE_INDEX_4 || sb.length() == PHONE_INDEX_9) && sb.charAt(sb.length() - 1) != ' ') {
                    sb.insert(sb.length() - 1, ' ');
                }
            }
        }
        if (callback!=null && replaceBlank(getText()+"").length()!=11){
            callback.onUnsatisfied();
        }
        if (!sb.toString().equals(s.toString())) {
            int index = start + 1;
            if (sb.charAt(start) == ' ') {
                if (before == 0) {
                    index++;
                } else {
                    index--;
                }
            } else {
                if (before == 1) {
                    index--;
                }
            }
            setText(sb.toString());
            setSelection(index);
        }else {
            if (callback!=null && replaceBlank(getText()+"").length()==11){
                callback.onEdSatisfy(replaceBlank(getText()+""));
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            if (m.find()) {
                dest = m.replaceAll("");
            }
        }
        return dest;
    }

    public void unIntervalEdittextCallback(){
        if (callback!=null){
            callback=null;
        }
    }
}
