package com.android.xlwlibrary.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.android.xlwlibrary.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by xu on 2019/12/20.
 */
public class XWaveSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mSurfaceHolder;
    private boolean isRun=true;
    private boolean isDrawAmplitude=false;
    private Canvas mCanvas;
    private int mWidth, mHeight;
    private Paint mPaint;
    private int MAX;
    private float amplitude = 0f;
    private float wAmplitude = 0.9f;
    private int[] COLORS = {R.color.green, R.color.blue, R.color.pink};
    private Random random;
    private List<Wave> waves;
    private int[] lineColors = new int[]{0xFF111111, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF111111};
    private float[] linepositions = new float[]{0f, 0.1f, 0.9f, 1};

    public XWaveSurfaceView(Context context) {
        super(context);
        init();
    }

    public XWaveSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs, 0);
        init();
    }

    public XWaveSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs, 0);
        init();
    }

    private void initTypedArray(Context context, AttributeSet attrs, int defStyleAttr){
       TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveSurfaceView, defStyleAttr, 0);
       currColor = typedArray.getColor(R.styleable.WaveSurfaceView_setBgColor,0);
    }

    public void init(){
        mPaint = new Paint();
        random = new Random();
        mPaint.setAntiAlias(true);
        mSurfaceHolder = this.getHolder();
        // 注册回调方法
        mSurfaceHolder.addCallback(this);
       // setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);//设置为透明
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        MAX = h * 2 / 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();
    }
    private Thread thread;
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread=new Thread(this);
        startAnim();
        onDrawLine();
        thread.start();
    }


    private int currColor=0;
    public void onDrawLine(){
        mCanvas = mSurfaceHolder.lockCanvas();
        if (mCanvas!=null){
            mCanvas.drawColor(currColor);
            drawLine(mCanvas);
        }
        mSurfaceHolder.unlockCanvasAndPost(mCanvas); //提交绘制内容
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this){
            if (Thread.currentThread()==thread){
                thread = null;
            }
        }
        isRun = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRun = false;
        thread = null;
    }

    //创建 waves 集合
    private void startAnim() {
        if (waves == null) {
            waves = new ArrayList<>();
        }
        waves.clear();
        for (int i = 0; i < 30; i++) {
            Wave wave = new Wave();
            //为每个对象都创建一个动画
            initAnimator(wave);
            waves.add(wave);
        }
        amplitude=0.1f;
        wAmplitude = 0.9f;
    }

    /**
     * 设置音量大小，用来控制波峰幅度
     * @param value
     */
    public void setVolem(int value){
        if (value<=3){
            amplitude=0.2f;
        }else if (value>4&&value<8){
            amplitude=0.6f;
        }else if (value>8&&value<11){
            amplitude=1f;
        }else if (value>10&&value<14){
            amplitude=1.3f;
        }else if (value>15&&value<18){
            amplitude=1.5f;
        }else if (value>18){
            amplitude=1.8f;
        }
    }

    //线程标记 ，控制线程是否运行
    public void onStopThread(){
        isRun=false;
    }

    //控制是否绘制
    public void onDrawAmplitude(boolean value){
        isDrawAmplitude=value;
    }
    //控制是否清除录音动画
    public void onDrawStopit(){
        if (!isDrawAmplitude){
            mCanvas = mSurfaceHolder.lockCanvas();
            if (mSurfaceHolder != null && mCanvas != null) {
                //清除之前的画布
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mCanvas.drawColor(currColor);
                drawLine(mCanvas);
            }
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void initAnimator(final Wave waveBean) {
        ValueAnimator animator = ValueAnimator.ofInt(0, waveBean.maxHeight);
        animator.setDuration(waveBean.duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveBean.waveHeight = (int) animation.getAnimatedValue();
                if (waveBean.waveHeight > waveBean.maxHeight / 2) {
                    waveBean.waveHeight = waveBean.maxHeight - waveBean.waveHeight;
                }
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
        animator.setRepeatMode(ValueAnimator.RESTART);//
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void run() {
//isRun 标记位
        while (isRun)
        {
            synchronized (this) {
                // 锁定画布
                if (isDrawAmplitude){
                    mCanvas = mSurfaceHolder.lockCanvas();
                    try {
                        if (mSurfaceHolder != null && mCanvas != null) {
                            //清除之前的画布
                            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            mCanvas.drawColor(currColor);
                            drawLine(mCanvas);
                            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
                            for (Wave wave : waves) {
                                wave.draw(mCanvas, mPaint);
                            }
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }finally{
                        //绘制结束后
                        // 将画布解锁
                        mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                    }
                }
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Path path = new Path();
    private Path pathN = new Path();
    class Wave {
        int maxHeight;
        int maxWidth;
        int color;
        double seed, open_class;
        int waveHeight;
        int duration;
        Paint mPaint;

        public Wave() {
            mHeight = getMeasuredHeight();
            mWidth = getMeasuredWidth();
            onInit();
        }
        //初始化本对象所处的位置，显示的时长，颜色，宽度跟高度
        private void onInit() {
            this.seed = Math.random();  // 位置
            maxWidth = (random.nextInt(mWidth / 16) + mWidth * 3 / 11);
            if (seed <= 0.2) {
                maxHeight = random.nextInt(MAX / 6) + MAX / 5;
                open_class = 2;
            } else if (seed <= 0.3 && seed > 0.2) {
                maxHeight = random.nextInt(MAX / 3) + MAX * 1 / 5;
                open_class = 3;
            } else if (seed > 0.3 && seed <= 0.7) {
                maxHeight = random.nextInt(MAX / 2) + MAX * 2 / 5;
                open_class = 3;
            } else if (seed > 0.7 && seed <= 0.8) {
                maxHeight = random.nextInt(MAX / 3) + MAX * 1 / 5;
                open_class = 3;
            } else if (seed > 0.8) {
                maxHeight = random.nextInt(MAX / 6) + MAX / 5;
                open_class = 2;
            }
            duration = random.nextInt(1000) + 700;
            color = COLORS[random.nextInt(3)];
        }

        double equation(double i) {
            i = Math.abs(i);
            double y = -1 * amplitude
                    * Math.pow(1 / (1 + Math.pow(open_class * i, 2)), 2);
            return y;
        }

        public void draw(Canvas canvas, Paint mPaint) {
            this.mPaint = mPaint;
            this._draw(canvas);
        }

        private void _draw(Canvas canvas) {
            path.reset();
            pathN.reset();
            path.moveTo(mWidth / 4, mHeight / 2);
            pathN.moveTo(mWidth / 4, mHeight / 2);
            double x_base = mWidth / 2 + (-mWidth / 4 + this.seed * (mWidth / 2));
            double y_base = mHeight / 2;

            double x, y, x_init = 0;
            double i = -1;
            while (i <= 1) {
                x = x_base + i * maxWidth * wAmplitude;
                double function = equation(i) * waveHeight;
                y = y_base + function;
                if (x_init > 0 || x > 0) {
                    x_init = mWidth / 4;
                }
                if (y > 0.1) {
                    path.lineTo((float) x, (float) y);
                    pathN.lineTo((float) x, (float) ((float) y_base - function));
                }
                i += 0.01;
            }
            mPaint.setColor(getResources().getColor(color));
            canvas.drawPath(path, mPaint);
            canvas.drawPath(pathN, mPaint);
        }
    }
    private LinearGradient shader;
    private PorterDuffXfermode porterDuffXfermode =new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);
    private void drawLine(Canvas canvas) {
        shader = new LinearGradient(mWidth / 40, 0,mWidth * 39 / 40, 0,
                lineColors,
                linepositions,
                Shader.TileMode.MIRROR);
        mPaint.setXfermode(porterDuffXfermode);
        mPaint.setShader(shader);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(mWidth / 40, mHeight / 2, mWidth * 39 / 40, mHeight / 2, mPaint);
        mPaint.setXfermode(null);
        mPaint.setShader(null);
        mPaint.clearShadowLayer();
    }
}
