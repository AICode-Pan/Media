package com.explain.media.activity.second;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.activity.base.BaseActivity;
import com.explain.media.utils.FFmpegCmd;
import com.explain.media.utils.MediaFile;

public class AudioDecodeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "AudioDecodeActivity";
    private TextView tvFilePath;
    private String filePath;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            hideProgressDialog();
            if (msg.what == 0) {
                Toast.makeText(AudioDecodeActivity.this, "解码成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AudioDecodeActivity.this, "解码失败", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音频解码");
        setContentView(R.layout.activity_audio_decode);
        findViewById(R.id.tv_select_file).setOnClickListener(this);
        findViewById(R.id.tv_start_decode).setOnClickListener(this);
        tvFilePath = findViewById(R.id.tv_filepath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
                break;
            case R.id.tv_start_decode:
                if (TextUtils.isEmpty(filePath)) {
                    Toast.makeText(this, "还没有选中文件地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                String type = filePath.substring(filePath.indexOf(".") + 1);
                String outFilePath = filePath.replace(type, "pcm");
                Log.i("Logger", TAG + ".onClick type=" + type);
                FFmpegCmd.decode(filePath, outFilePath, mHandler);
                break;
        }
    }

    @Override
    protected void onSelectedFile(String filePath) {
        super.onSelectedFile(filePath);
        if (!MediaFile.isAudioFileType(filePath)) {
            Toast.makeText(this, "文件格式错误，非音频文件", Toast.LENGTH_SHORT).show();
            return;
        }

        this.filePath = filePath;
        tvFilePath.setText("文件地址:" + filePath);
    }
}
