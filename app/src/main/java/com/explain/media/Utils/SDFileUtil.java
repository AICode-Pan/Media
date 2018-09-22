package com.explain.media.Utils;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.explain.media.CoreApplication;

import java.io.File;
import java.text.DecimalFormat;

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

    /**
     * 获取文件的绝对路径
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
