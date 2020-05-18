package com.explain.media.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.explain.media.R;
import com.explain.media.utils.FFmpegCmd;

/**
 * 音频处理
 */
public class AudioHandleActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_handle);

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
            case R.id.button1://PCM合并
                FFmpegCmd.execute("sss", "gaa");
                break;
            case R.id.button2://音频编码
                break;
            case R.id.button3://音频转码
                break;
            case R.id.button4://音频剪切
                break;
            case R.id.button5://音频合并
                break;
            case R.id.button6://音频混合
                break;
            case R.id.button7://AudioTrack播放
                break;
            case R.id.button8://OpenSL播放
                break;
        }
    }
}
