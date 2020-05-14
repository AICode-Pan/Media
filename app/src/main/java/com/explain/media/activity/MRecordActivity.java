package com.explain.media.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.explain.media.audio.record.MAudioRecorder;
import com.explain.media.R;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/20
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class MRecordActivity extends Activity {
    private Button button;
    private MAudioRecorder mediaRecorder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(140,
                140);
        lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        lp.setMargins(0, 0, 0, 50);
        FrameLayout layout = new FrameLayout(this);
        button = new Button(this);
        button.setWidth(80);
        button.setHeight(80);
        button.setBackgroundResource(R.drawable.bg_record_btn);
        layout.addView(button);

        button.setLayoutParams(lp);

        setContentView(layout);

        mediaRecorder = new MAudioRecorder();

        button.setOnTouchListener(new TouchListener());
    }

    /**
     * Set TouchListener
     */
    private class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mediaRecorder.startRecord();
                    break;
                case MotionEvent.ACTION_UP:
                    mediaRecorder.stopRecord();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
            return false;
        }
    }
}
