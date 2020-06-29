package com.explain.media.activity.base;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.explain.media.utils.SDFileUtil;
import com.explain.media.view.LoadingDialog;

public class BaseActivity extends AppCompatActivity {
    private final static int REQUEST_CODE = 1234;

    private final static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    /**
     * 选择文件
     */
    protected void selectFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivityForResult(intent, 123);
        } else {
            requestPermission(new String[] {permissions[0], permissions[1]});
        }
    }

    /**
     * 申请权限
     * @param permissions
     */
    private void requestPermission(String[] permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            String filePath = SDFileUtil.getPath(this, data.getData());
            Log.i("AudioHandleActivity", "filePath=" + filePath);
            onSelectedFile(filePath);
        }
    }

    protected void onSelectedFile(String filePath) { }

    private LoadingDialog loadingDialog;
    protected void showProgressDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.show();
    }

    protected void hideProgressDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
