package com.explain.media.utils;

import android.util.Log;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/29
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class FFmpegCmd {
    static{
        System.loadLibrary("media-handle");
    }

    //开子线程调用native方法进行音视频处理
    public static void execute(final String filePath, final String newFilePath) {
        Log.d("FFmpegCmd", filePath + " ," + newFilePath);
        new Thread(new Runnable() {
            @Override
            public void run() {

//                //调用ffmpeg进行处理
                int result = pcm2aac(filePath, newFilePath);
                Log.i("FFmpegCmd", "result : " + result);

            }
        }).start();
    }

    private native static int pcm2aac(String filePath, String newFilePath);
}
