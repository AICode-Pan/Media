package com.explain.media.video.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.explain.media.listener.RecordLisnter;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 通过CameraHelper.Builder获取实例
 */
@SuppressLint("NewApi")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraHelper {
    private static final String TAG = "CameraHelper";

    private CameraManager mCameraManager;//摄像头管理器
    private Handler mainHandler;
    private int mCameraID;//摄像头Id 0 为后  1 为前
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    //CameraCharacteristics相机信息提供者
    //其内部携带大量的相机信息，包括代表相机朝向的 LENS_FACING；判断闪光灯是否可用的 FLASH_INFO_AVAILABLE；获取所有可用 AE 模式的 CONTROL_AE_AVAILABLE_MODES 等等
    private CameraCharacteristics cameraCharacteristics;

    private Context context;
    private RecordLisnter recordLisnter;
    private int rotation;
    private Surface surface;
    private int screenWidth;
    private int screenHeight;
    //ImageReader 是获取图像数据的一个重要途径，我们可以通过它获取各种各样格式的图像数据，例如 JPEG、YUV 和 RAW 等等。
    private ImageReader imageReader;

    private CameraHelper(Builder builder) {
        context = builder.context;
        rotation = builder.rotation;
        surface = builder.surface;
        screenWidth = builder.width;
        screenHeight = builder.height;

        initCamera();
    }

    /**
     * 初始化摄像头
     * CameraCharacteristics.LENS_FACING_FRONT 通常表示后置摄像头;
     * CameraCharacteristics.LENS_FACING_BACK 通常表示前置摄像头;
     */
    private void initCamera() {
        mainHandler = new Handler(context.getMainLooper());
        mCameraID = CameraCharacteristics.LENS_FACING_FRONT;//后摄像头

        //获取摄像头管理
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        initCameraCharacteristics();

        imageReader = ImageReader.newInstance(screenWidth, screenHeight, ImageFormat.JPEG, 1);
    }

    /**
     * 初始化CameraCharacteristics对象。
     */
    private void initCameraCharacteristics() {
        try {
            cameraCharacteristics = mCameraManager.getCameraCharacteristics(String.valueOf(mCameraID));
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            Log.i(TAG, "StateCallback.onOpened");
            mCameraDevice = camera;
            //开启预览
            takePreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {//关闭摄像头
            Log.i(TAG, "StateCallback.onDisconnected");
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {//发生错误
            Toast.makeText(context, "摄像头开启失败", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 设置录制监听
     *
     * @param lisnter
     */
    public void setRecordLisnter(RecordLisnter lisnter) {
        this.recordLisnter = lisnter;
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mCameraID == CameraCharacteristics.LENS_FACING_FRONT) {
            mCameraID = CameraCharacteristics.LENS_FACING_BACK;
        } else {
            mCameraID = CameraCharacteristics.LENS_FACING_FRONT;
        }

        closeCamera();
        initCameraCharacteristics();
        openCamera();
    }

    /**
     * 打开摄像头
     */
    public void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("Not has Permission Manifest.permission.CAMERA.");
            } else {
                //打开摄像头
                Log.i(TAG, "CameraManager.openCamera");
                mCameraManager.openCamera(String.valueOf(mCameraID), stateCallback, mainHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取可以支持的预览尺寸
     */
    public <T> Size getSupportedPreviewSizes(Class<T> tClass, int maxWidth, int maxHeight) {
        float aspectRatio = ((float) maxWidth) / maxHeight;
        Log.i(TAG, TAG + ".getSupportedPreviewSizes aspectRatio=" + aspectRatio);
        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        Size[] supportedSizes = streamConfigurationMap.getOutputSizes(tClass);
        Log.i(TAG, TAG + ".getSupportedPreviewSizes supportedSize=" + supportedSizes.length);
        for (int i = 0; i < supportedSizes.length; i++) {
            Size size = supportedSizes[i];
            Log.i(TAG, TAG + ".getSupportedPreviewSizes width=" + size.getWidth() + " height=" + size.getHeight());
            if (((float) size.getHeight()) / size.getWidth() == aspectRatio) {
                return size;
            }
        }
        return null;
    }

    /**
     * 放大缩小
     */
    public void handlerZoom() {

    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        if (null != mCameraDevice) {
            mCameraDevice.close();
        }
    }

    /**
     * 释放Camera资源
     */
    public void close() {
        closeCamera();
        mCameraDevice = null;

    }

    /**
     * 开始预览
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void takePreview() {
        Log.i(TAG, "Camera take preview.");
        try {
            // 创建预览需要的CaptureRequest.Builder
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            previewRequestBuilder.addTarget(surface);
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            mCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (null == mCameraDevice) return;
                    // 当摄像头已经准备好时，开始显示预览
                    mCameraCaptureSession = cameraCaptureSession;
                    try {
                        // 自动对焦
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // 打开闪光灯
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // 显示预览
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, mainHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(context, "配置失败", Toast.LENGTH_SHORT).show();
                }
            }, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     * setRepeatingRequest() 是重复请求获取图像数据，常用于预览或连拍
     * capture() 是获取一次，常用于单张拍照。
     */
    public void takePicture() {
        try {
            // 创建拍照需要的CaptureRequest.Builder
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // 将imageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(imageReader.getSurface());
            // 自动对焦
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // 自动曝光，闪光灯
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // 根据设备方向计算设置照片的方向
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(cameraCharacteristics, rotation));
            //拍照
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, mainHandler);

            //可以在这里处理拍照得到的临时照片 例如，写入本地
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
//                mCameraDevice.close();
                    // 拿到拍照照片数据
                    // 通过调用 Image.getPlanes() 方法获取所有的 Plane 对象的数组，最后通过 Plane.getBuffer() 获取每一个 Plane 里存储的图像数据。
                    Image image = reader.acquireNextImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);//由缓冲区存入字节数组
                    image.close();
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (recordLisnter != null) {
                        recordLisnter.onTakePicture(bitmap);
                    }
                }
            }, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 为了使照片竖直显示,获取图片的方向
     * 在进行图片方向矫正的时候，我们的目的是做到所见即所得，也就是用户在预览画面里看到的是什么样，输出的图片就是什么样。
     * 为了做到图片所见即所得，我们要同时考虑设备方向和摄像头传感器方向。
     * 下面是一段来自官方的图片矫正代码
     */
    private int getJpegOrientation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int myDeviceOrientation = deviceOrientation;
        if (myDeviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN) {
            return 0;
        }

        if (cameraCharacteristics == null) {
            return 0;
        }

        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        myDeviceOrientation = (myDeviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
//        boolean facingFront = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
//        if (facingFront) {
//            sensorOrientation = -sensorOrientation;
//        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + myDeviceOrientation + 360) % 360;
    }

    /**
     * 通过Builder获取CamerHelper对象
     */
    public static final class Builder {
        private Context context;
        private int rotation;
        private Surface surface;
        private int width = 1080;
        private int height = 1920;

        public Builder() {
        }

        public Builder with(Context context) {
            this.context = context;
            return this;
        }

        //获取手机方向
        public Builder rotation(int val) {
            this.rotation = val;
            return this;
        }

        public Builder setSurface(Surface surface) {
            this.surface = surface;
            return this;
        }

        public Builder screen(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public CameraHelper build() {
            return new CameraHelper(this);
        }
    }
}
