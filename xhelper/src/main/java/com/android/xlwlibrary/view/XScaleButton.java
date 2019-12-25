package com.android.xlwlibrary.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.android.xlwlibrary.R;

/**
 * Button放大综小动画,也可继承layout
 */
@SuppressLint("AppCompatCustomView")
public class XScaleButton extends Button {

    private Context context;

    public XScaleButton(Context context) {
        this(context, null);
        this.context = context;
    }

    public XScaleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public XScaleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                beginScale(R.anim.zoom_in);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                beginScale(R.anim.zoom_out);
                break;
            case MotionEvent.ACTION_CANCEL:
                beginScale(R.anim.zoom_out);
                break;
        }
        return true;
    }

    private synchronized void beginScale(int animation) {
        Animation an = AnimationUtils.loadAnimation(context, animation);
        an.setDuration(500);
        an.setFillAfter(true);
        this.startAnimation(an);
    }
}
