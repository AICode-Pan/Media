package com.explain.media.audio.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.explain.media.R;
import com.explain.media.Utils.FFmpegCmd;
import com.explain.media.Utils.SDFileUtil;
import com.explain.media.audio.record.AudioRecorder;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/29
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class AudioEncodeActivity extends Activity {
    private static final String TAG = AudioEncodeActivity.class.getSimpleName();
    private Button button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(140,
                80);
        lp.setMargins(20, 20, 0, 0);
        FrameLayout layout = new FrameLayout(this);
        button = new Button(this);
        layout.addView(button);

        button.setLayoutParams(lp);

        setContentView(layout);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "execute pcm to aac");
                FFmpegCmd.execute(SDFileUtil.getSDPath() + "/ARecord.pcm" , SDFileUtil.getSDPath() + "/ATest.aac");
            }
        });
    }
}
