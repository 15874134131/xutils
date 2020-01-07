package com.android.xlwlibrary.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.xlwlibrary.listener.CustomCameraListener;
import com.android.xlwlibrary.listener.CustomCameraImgListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import static android.content.Context.CAMERA_SERVICE;

/**
 * 这是一个camera 的控制类，一般的步骤为在摄像头初始化成功之后，根据自己的需要进行摄像头的调用，但是本人这里考虑了一个情况 ，
 * 当你频繁的打开和关闭摄像头设备的时候 ，内部的一些配置其实来不及销毁。特别是一些图像的回调。
 * 所以我这里的思想是，摄像头在一开始就初始化，并且进行了预览，然后调用stopRepeating（）停止预览 ，在当前界面，如果需要的地方，调用restartPreview 重新预览。
 * 使用方式：在oncreate() 中创建控制类 ，
 * TextureView的SurfaceTextureListener的onSurfaceTextureAvailable 中进行初始化
 *      try {
 *             customCamera.setupCamera(width, height);
 *         } catch (CameraAccessException e) {
 *             e.printStackTrace();
 *         }
 *         customCamera.openCamera();
 *         customCamera.startPreview();
 *         customCamera.stopRepeating();
 */
public class XCustomCamera {
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private Size mPreviewSize;
    private Size mCaptureSize;
    private String mCameraId;
    private Activity mContext;
    private ImageReader mImageReader;
    private Handler mCameraHandler;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mPreviewSession;
    private TextureView tvShowphoto;
    private CameraManager cameraManager;
    private CustomCameraListener mCustomCameraListener;
    private CustomCameraImgListener customCameraImgListener;

    //该回调用于回调摄像机是否成功打开
    public void steCustomCameraImgCallback(CustomCameraImgListener cameraImgCallback){
        this.customCameraImgListener =cameraImgCallback;
    }

    //该回调为成功预览时的回调。，因为基于我之前的设想 ，在第一次初始化成功后，我们执行了然后调用stopRepeating（）停止预览 ，
    // 因此在初始化时并没有返回成功预览的回调，所以本例只在重新预览时返回
    public void setCustomCameraCallback(CustomCameraListener customCameraListener){
        this.mCustomCameraListener = customCameraListener;
    }

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    public XCustomCamera(Activity context, Handler handler, TextureView textureView){
        this.mContext=context;
        this.mCameraHandler=handler;
        this.tvShowphoto=textureView;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cameraManager = (CameraManager) mContext.getSystemService(CAMERA_SERVICE);
        }
    }

    //设置相机参数
    public  void setupCamera(int width, int height) throws CameraAccessException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            String cameraId ="";
            switch (cameraManager.getCameraIdList().length){
                case 0:
                    throw new NullPointerException("CameraIdList was null");
                case 2:
                    cameraId=cameraManager.getCameraIdList()[1];
                    break;
                    default:
                        cameraId=cameraManager.getCameraIdList()[0];
                        break;
            }
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //根据TextureView的尺寸设置预览尺寸
            mPreviewSize = getOptimalPreviewSize(Arrays.asList(map.getOutputSizes(SurfaceTexture.class)), width, height);
            //设置拍照时照片的尺寸和格式
            mCaptureSize= Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });
            //该方法主要为了获取预览的帧数生成照片
            setupImageReader();
            //获得相机ID
            mCameraId = cameraId;
        }
    }



    //获取预览帧数据
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupImageReader() {
        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据，本例的2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
                //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                mCameraHandler.post(new ImageSaver(data,mContext));
                image.close();
            }
        }, mCameraHandler);
    }



    //打开相机
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void openCamera(){
        try {
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            cameraManager.openCamera(mCameraId,stateCallback , mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void transformImage (int width, int height) {
        if (mPreviewSize == null || tvShowphoto == null) {
            return;
        }
        Matrix matrix = new Matrix();
        int rotation = this.mContext.getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centery = textureRectF.centerY();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centery - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / mPreviewSize.getWidth(), (float) height / mPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centery);
            matrix.postRotate(90 * (rotation - 2), centerX, centery);
            tvShowphoto.setTransform(matrix);
        }
    }

    //启动预览
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startPreview() {
        SurfaceTexture mSurfaceTexture = tvShowphoto.getSurfaceTexture();
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //获取Surface显示预览数据
        Surface previewSurface = new Surface(mSurfaceTexture);
        if (mCameraDevice!=null) {
            try {
                //创建CaptureRequestBuilder，TEMPLATE_PREVIEW表示预览请求
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //自动对焦
                mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //设置Surface作为预览数据的显示界面
                mCaptureRequestBuilder.addTarget(previewSurface);
                //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，这里我们除了预览用的previewSurface，还有一个拍照，或者说是连续获取帧数据的mImageReader
                // 第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，
                // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
                //
                mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        try {
                            if (null == mCameraDevice) {
                                return;
                            }
                            //创建捕获请求，定义输出缓冲区以及显示界面
                            mCaptureRequest = mCaptureRequestBuilder.build();
                            //获得捕获会话
                            mPreviewSession = session;
                            //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
                            mPreviewSession.setRepeatingRequest(mCaptureRequest, mSessionCaptureCallback, mCameraHandler);

                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                    }
                }, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    // 判断mPreviewSession 是否存在
    public CameraCaptureSession onCameraCaptureSession(){
        return mPreviewSession;
    }

    //该回调是当预览一直有数据显示时的回调
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    CameraCaptureSession.CaptureCallback mSessionCaptureCallback=new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    //打开摄像头的回调
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            //打开摄像头成功，开启预览
            //startPreview();
            customCameraImgListener.callBackImgPath(true);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
            customCameraImgListener.callBackImgPath(false);
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
            customCameraImgListener.callBackImgPath(false);
        }
    };

    private int rotation=0;
    //拍照
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void capture() {
        try {
            //首先我们创建请求拍照的CaptureRequest
            final CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //设置CaptureRequest输出到mImageReader
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            //获取屏幕方向
            rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
            //设置拍照方向
            mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            //这个回调接口用于拍照结束时重启预览，因为拍照会导致预览停止
            CameraCaptureSession.CaptureCallback mImageSavedCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    //重启预览
                    //restartPreview();
                }
            };
            //停止预览
            mPreviewSession .stopRepeating();
            //开始拍照，然后回调上面的接口重启预览，因为mCaptureBuilder设置ImageReader作为target，所以会自动回调ImageReader的onImageAvailable()方法保存图片
            mPreviewSession .capture(mCaptureBuilder.build(), mImageSavedCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //重新预览
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void restartPreview() {
        try {
            //执行setRepeatingRequest方法就行了，注意mCaptureRequest是之前开启预览设置的请求
            mPreviewSession .setRepeatingRequest(mCaptureRequest, mSessionCaptureCallback, mCameraHandler);
            if (mCustomCameraListener != null) {
                mCustomCameraListener.setPreviewSuccess();
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopRepeating(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mPreviewSession!=null){
                //停止预览
                try {
                    mPreviewSession .stopRepeating();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //根据TextureView的尺寸设置预览尺寸
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = 0;
            ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.getHeight() - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.getHeight() - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    //图片处理
    private class ImageSaver implements Runnable {
        File mImageFile;
        private byte[] mData;
        private Context context;
        public ImageSaver(byte[] bytes, Context context) {
            mData = bytes;
            this.context=context;
        }
        @Override
        public void run() {
            mImageFile = new File(context.getExternalFilesDir("Camera").getPath()+"/camera.jpg");
            mJpegOrientation=getJpegOrientation(getNaturalOrientation(mData),rotation);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=2;
            Bitmap thumb = BitmapFactory.decodeByteArray(mData,0,mData.length,options);
            Matrix matrix = new Matrix();
            matrix.postRotate(mJpegOrientation);
            Bitmap newThumb = Bitmap.createBitmap(thumb, 0, 0, thumb.getWidth(), thumb.getHeight(), matrix, true);
            FileOutputStream fos = null;
            newThumb.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            try {
                fos = new FileOutputStream(mImageFile);
                fos.write(baos.toByteArray(), 0 ,baos.toByteArray().length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImageFile = null;
                if (fos != null) {
                    try {
                        fos.close();
                        fos = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void closeCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                if (null != mPreviewSession) {

                    mPreviewSession.stopRepeating();
                    mPreviewSession.close();
                    mPreviewSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
                if (null != mImageReader) {
                    mImageReader.close();
                    mImageReader = null;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } finally {
                mCameraOpenCloseLock.release();
            }
        }
    }
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private  int mJpegOrientation;
    /**
     * 从底层返回的数据拿到对应的图片的角度，有些手机hal层会对手机拍出来的照片作相应的旋转，有些手机不会(比如三星手机)
     * @param jpeg
     * @return
     */
    private  int getNaturalOrientation(byte[] jpeg) {
        if (jpeg == null) {
            return 0;
        }

        int offset = 0;
        int length = 0;

        // ISO/IEC 10918-1:1993(E)
        while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
            int marker = jpeg[offset] & 0xFF;

            // Check if the marker is a padding.
            if (marker == 0xFF) {
                continue;
            }
            offset++;

            // Check if the marker is SOI or TEM.
            if (marker == 0xD8 || marker == 0x01) {
                continue;
            }
            // Check if the marker is EOI or SOS.
            if (marker == 0xD9 || marker == 0xDA) {
                break;
            }

            // Get the length and check if it is reasonable.
            length = pack(jpeg, offset, 2, false);
            if (length < 2 || offset + length > jpeg.length) {
                return 0;
            }

            // Break if the marker is EXIF in APP1.
            if (marker == 0xE1 && length >= 8 &&
                    pack(jpeg, offset + 2, 4, false) == 0x45786966 &&
                    pack(jpeg, offset + 6, 2, false) == 0) {
                offset += 8;
                length -= 8;
                break;
            }

            // Skip other markers.
            offset += length;
            length = 0;
        }

        // JEITA CP-3451 Exif Version 2.2
        if (length > 8) {
            // Identify the byte order.
            int tag = pack(jpeg, offset, 4, false);
            if (tag != 0x49492A00 && tag != 0x4D4D002A) {

                return 0;
            }
            boolean littleEndian = (tag == 0x49492A00);

            // Get the offset and check if it is reasonable.
            int count = pack(jpeg, offset + 4, 4, littleEndian) + 2;
            if (count < 10 || count > length) {
                return 0;
            }
            offset += count;
            length -= count;

            // Get the count and go through all the elements.
            count = pack(jpeg, offset - 2, 2, littleEndian);
            while (count-- > 0 && length >= 12) {
                // Get the tag and check if it is orientation.
                tag = pack(jpeg, offset, 2, littleEndian);
                if (tag == 0x0112) {
                    // We do not really care about type and count, do we?
                    int orientation = pack(jpeg, offset + 8, 2, littleEndian);
                    switch (orientation) {
                        case 1:
                            return 0;
                        case 3:
                            return 180;
                        case 6:
                            return 90;
                        case 8:
                            return 270;
                    }
                    return 0;
                }
                offset += 12;
                length -= 12;
            }
        }
        return 0;
    }

    private  int pack(byte[] bytes, int offset, int length,
                            boolean littleEndian) {
        int step = 1;
        if (littleEndian) {
            offset += length - 1;
            step = -1;
        }

        int value = 0;
        while (length-- > 0) {
            value = (value << 8) | (bytes[offset] & 0xFF);
            offset += step;
        }
        return value;
    }

    private   int getJpegOrientation(int naturalJpegOrientation, int deviceOrientation) {
        return (naturalJpegOrientation+ deviceOrientation) % 360;
    }
}
