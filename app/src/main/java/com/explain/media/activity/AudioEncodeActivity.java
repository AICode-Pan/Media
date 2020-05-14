package com.explain.media.activity;

import android.app.Activity;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.explain.media.utils.SDFileUtil;
import com.explain.media.audio.encode.AudioEncode;

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

    private AudioEncode audioEncode;
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
                audioEncode = new AudioEncode();
                audioEncode.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
//                audioEncode.setIOPath(SDFileUtil.getSDPath() + "/gemgnzw.mp3", SDFileUtil.getAACPath());
                audioEncode.setIOPath(SDFileUtil.getSDPath() + "/ARecord.pcm", SDFileUtil.getSDPath() + "/AudioEncode.aac");
                audioEncode.prepare();
                audioEncode.startAsync();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioEncode.release();
    }
}
