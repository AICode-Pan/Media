package com.explain.media.video.stream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.lang.ref.WeakReference;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoStream {
    private CameraManager cameraManager;
    private String frontCameraId;
    private CameraCharacteristics frontCameraCharacteristics;
    private String backCameraId;
    private CameraCharacteristics backCameraCharacteristics;
    private CameraHandler cameraHandler;

    public VideoStream(Context context) {
        cameraHandler = new CameraHandler(this);
        try {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIds = cameraManager.getCameraIdList();
            for (int i = 0 ; i < cameraIds.length ; i++) {
                String cameraId = cameraIds[i];

                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = cameraId;
                    frontCameraCharacteristics = cameraCharacteristics;
                } else if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                    backCameraCharacteristics = cameraCharacteristics;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断相机的 Hardware Level 是否大于等于指定的 Level。
     */
    private Boolean isHardwareLevelSupported(int requiredLevel, int deviceLevel) {
        int[] sortedLevels = {CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3};
        if (requiredLevel == deviceLevel) {
            return true;
        }
        for (int i = 0; i < sortedLevels.length ; i++) {
            if (requiredLevel == sortedLevels[i]) {
                return true;
            } else if (deviceLevel == sortedLevels[i]) {
                return false;
            }
        }
        return false;
    }

    private static class CameraHandler extends Handler {
        private WeakReference<VideoStream> videoStreamWR;

        public CameraHandler(VideoStream vs) {
            videoStreamWR = new WeakReference<>(vs);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (videoStreamWR == null || videoStreamWR.get() == null)
                return;


        }
    }

    private class CameraStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
//            cameraDevice.createCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        String cameraId = backCameraId != null ? backCameraId : frontCameraId;
        if (cameraId != null) {
            try {
                cameraManager.openCamera(cameraId, new CameraStateCallback(), cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
