package com.explain.media.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import com.explain.media.R;
import com.explain.media.listener.RecordLisnter;
import com.explain.media.video.helper.CameraHelper;

/**
 * 视频实时处理
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("NewApi")
public class RecordHandleActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "RecordHandleActivity";

    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private CameraHelper cameraHelper;
    private ImageView ivRecord;
    private ImageView ivPicture;
    public static final int REQUEST_CAMERA_CODE = 100;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, TAG + ".onCreate");
        setContentView(R.layout.activity_record_handle);
        textureView = findViewById(R.id.textureview);
        ivRecord = findViewById(R.id.iv_record);
        ivPicture = findViewById(R.id.iv_picture);
        ivPicture.setOnClickListener(this);

        ivRecord.setOnClickListener(this);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, TAG + ".SurfaceTextureListener.onSurfaceTextureAvailable");
                surfaceTexture = surface;
                startPreview();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i(TAG, TAG + ".surfaceDestroyed");
                if (cameraHelper != null) {
                    cameraHelper.close();
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

    }

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPreview();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_record:
                if (cameraHelper != null) {
                    cameraHelper.takePicture();
                }
                break;
            case R.id.iv_picture:
                if (cameraHelper != null) {
                    cameraHelper.switchCamera();
                }
                break;
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        Log.i(TAG, TAG + ".startPreview");
        //检查CAMERA权限
        if (ActivityCompat.checkSelfPermission(RecordHandleActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CODE);
        } else {
            Display defaultDisplay = getWindowManager().getDefaultDisplay();
            Point point = new Point();
            defaultDisplay.getSize(point);
            int width = point.x;
            int height = point.y;
            Log.i(TAG, TAG + ".startPreview gRotation=" + defaultDisplay.getRotation());
            Log.i(TAG, TAG + ".startPreview screenWidth=" + width + " screenHeight=" + height);

            cameraHelper = new CameraHelper.Builder()
                    .with(RecordHandleActivity.this)
                    // 获取手机方向
                    .rotation(defaultDisplay.getRotation())
                    // 获取屏幕宽高
                    .screen(width, height)
                    .setSurface(new Surface(surfaceTexture))
                    .build();

            cameraHelper.setRecordLisnter(recordLisnter);
            cameraHelper.openCamera();
            Size size = cameraHelper.getSupportedPreviewSizes(surfaceTexture.getClass(), width, height);
            if (size != null) {
                Log.i(TAG, TAG + ".startPreview width=" + size.getWidth() + " height=" + size.getHeight());
                surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            }
        }
    }

    private RecordLisnter recordLisnter = new RecordLisnter() {
        @Override
        public void onTakePicture(Bitmap bitmap) {
            ivPicture.setVisibility(View.VISIBLE);
            ivPicture.setImageBitmap(bitmap);

        }
    };
}
