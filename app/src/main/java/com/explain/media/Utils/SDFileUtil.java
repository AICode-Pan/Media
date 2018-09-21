package com.explain.media.Utils;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.explain.media.CoreApplication;

import java.io.File;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/21
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class SDFileUtil {
    private static final String TAG = SDFileUtil.class.getName();

    public static String getSDPath() {
        String sdDirPath = null;
        File sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
        Log.i(TAG, TAG + ".getSDPath.sdDir : " + sdDir.toString());
        if (sdDir.exists()) {
            sdDirPath = sdDir.toString();
        } else {
            sdDirPath = "/sdcard/";
        }
        sdDirPath = sdDirPath + "/" + CoreApplication.getContent().getPackageName();

        File file = new File(sdDirPath);
        if (file.exists()) {
            return sdDirPath;
        } else {
            file.mkdir();
            return file.getPath();
        }
    }
}
