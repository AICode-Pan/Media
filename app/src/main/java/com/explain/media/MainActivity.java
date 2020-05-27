package com.explain.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.explain.media.activity.AudioHandleActivity;
import com.explain.media.activity.second.MRecordActivity;
import com.explain.media.activity.RecordHandleActivity;
import com.explain.media.activity.VideoHandleActivity;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/3
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class MainActivity extends Activity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.button1:
                intent.setClass(MainActivity.this, AudioHandleActivity.class);
                startActivity(intent);
                break;
            case R.id.button2:
                intent.setClass(MainActivity.this, MRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.button3:
                intent.setClass(MainActivity.this, VideoHandleActivity.class);
                startActivity(intent);
                break;
            case R.id.button4:
                intent.setClass(MainActivity.this, RecordHandleActivity.class);
                startActivity(intent);
                break;
        }
    }
}
