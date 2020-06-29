package com.explain.media.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
        System.loadLibrary("ffmpeg");
        System.loadLibrary("mp3lame");
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

    //开子线程调用native方法进行音视频处理
    public static void decode(final String filePath, final String newFilePath, final Handler handler) {
        Log.d("FFmpegCmd", filePath + " ," + newFilePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
//                //调用ffmpeg进行处理
                int code = audioDecode(filePath, newFilePath);
                if (handler != null) {
                    handler.sendEmptyMessage(code);
                }
                Log.d("FFmpegCmd", "解码完成");
            }
        }).start();
    }

    //获取avcodec版本号
    public static int getVersion() {
        return getAVCodecVersion();
    }

    private native static int getAVCodecVersion();
    private native static int pcm2aac(String filePath, String newFilePath);
    private native static int audioDecode(String filePath, String newFilePath);
}
