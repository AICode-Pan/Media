package com.explain.media.audio.record;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import com.explain.media.Utils.SDFileUtil;

import java.io.File;
import java.io.IOException;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/20
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class MAudioRecorder {
    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private MediaRecorder mRecorder;
    //AudioName裸音频数据文件
    private static final String AudioName = SDFileUtil.getAACPath();//不推荐这么写，可以用Enviroment.

    public void startRecord() {
        try {
            // 创建AudioRecord对象
            mRecorder = new MediaRecorder();
            // 设置录音的声音来源
            mRecorder.setAudioSource(audioSource);
            // 设置录制的声音的输出格式（必须在设置声音编码格式之前设置）
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            // 设置声音编码格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
            //所有android系统都支持的适中采样的频率
            mRecorder.setAudioSamplingRate(sampleRateInHz);
            File file = new File(AudioName);
            if (file.exists()) {
                file.delete();
            }
            mRecorder.setOutputFile(file.getAbsolutePath());

            mRecorder.prepare();

            // 开始录音
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();//释放资源
            mRecorder = null;
        }
    }

    private class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();//往文件中写入裸数据
        }
    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private void writeDateTOFile() {
        int readsize = 0;


    }
}
