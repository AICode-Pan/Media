package com.explain.media.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.explain.media.R;
import com.explain.media.activity.second.AudioDecodeActivity;
import com.explain.media.activity.second.AudioPlayActivity;
import com.explain.media.activity.second.PCMPlayActivity;
import com.explain.media.activity.second.TranscodingActivity;
import com.explain.media.utils.FFmpegCmd;

/**
 * 音频处理
 */
public class AudioHandleActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_handle);

        findViewById(R.id.button0).setOnClickListener(this);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button0://音频解码
                Intent intent0 = new Intent();
                intent0.setClass(this, AudioDecodeActivity.class);
                startActivity(intent0);
                break;
            case R.id.button1://PCM合并
                FFmpegCmd.execute("sss", "gaa");
                break;
            case R.id.button2://音频编码
                break;
            case R.id.button3://音频转码
                Intent intent3 = new Intent();
                intent3.setClass(this, TranscodingActivity.class);
                startActivity(intent3);
                break;
            case R.id.button4://音频剪切
                break;
            case R.id.button5://音频合并
                break;
            case R.id.button6://音频混合
                break;
            case R.id.button7://音频播放
                Intent intent7 = new Intent();
                intent7.setClass(this, AudioPlayActivity.class);
                startActivity(intent7);
                break;
            case R.id.button8://PCM文件播放
                Intent intent8 = new Intent();
                intent8.setClass(this, PCMPlayActivity.class);
                startActivity(intent8);
                break;
        }
    }
}
