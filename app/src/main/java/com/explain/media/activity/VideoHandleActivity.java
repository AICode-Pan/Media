package com.explain.media.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.explain.media.R;

public class VideoHandleActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_handle);


        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
        findViewById(R.id.button7).setOnClickListener(this);
        findViewById(R.id.button8).setOnClickListener(this);
        findViewById(R.id.button9).setOnClickListener(this);
        findViewById(R.id.button10).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button1://视频转码
                break;
            case R.id.button2://视频剪切
                break;
            case R.id.button3://视频拼接
                break;
            case R.id.button4://视频截图
                break;
            case R.id.button5://合成GIF
                break;
            case R.id.button6://合成视频
                break;
            case R.id.button7://画面拼接
                break;
            case R.id.button8://倒放
                break;
            case R.id.button9://降噪
                break;
            case R.id.button10://画中画
                break;
        }
    }
}
