package com.explain.media.activity.second;

import android.app.Activity;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.activity.base.BaseActivity;
import com.explain.media.utils.FFmpegCmd;
import com.explain.media.utils.MediaFile;
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

public class AudioEncodeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = AudioEncodeActivity.class.getSimpleName();

    private TextView tvFilePath;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音频编码");
        setContentView(R.layout.activity_audio_encode);

        findViewById(R.id.tv_select_file).setOnClickListener(this);
        findViewById(R.id.tv_start_encode).setOnClickListener(this);
        tvFilePath = findViewById(R.id.tv_filepath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
                break;
            case R.id.tv_start_encode:
                if (TextUtils.isEmpty(filePath)) {
                    Toast.makeText(this, "还没有选中文件地址", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.i(TAG, "execute pcm to aac");
                String type = filePath.substring(filePath.indexOf(".") + 1);
                String outFilePath = filePath.replace(type, "aac");

//                AudioEncode audioEncode = new AudioEncode();
//                audioEncode.setEncodeType(MediaFormat.MIMETYPE_AUDIO_AAC);
//                audioEncode.setIOPath(filePath, outFilePath);
//                audioEncode.prepare();
//                audioEncode.startAsync();
                FFmpegCmd.encode(filePath, outFilePath);
                break;
        }
    }

    @Override
    protected void onSelectedFile(String filePath) {
        super.onSelectedFile(filePath);
        if (!MediaFile.isPCMFileType(filePath)) {
            Toast.makeText(this, "文件格式错误，非PCM文件", Toast.LENGTH_SHORT).show();
            return;
        }

        this.filePath = filePath;
        tvFilePath.setText("文件地址:" + filePath);
    }
}
