package com.explain.media.activity.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.activity.base.BaseActivity;
import com.explain.media.utils.MediaFile;

/**
 * 转码
 */
public class TranscodingActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "TranscodingActivity";

    private TextView tvFilePath;
    private String filePath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音频转码");

        setContentView(R.layout.activity_transcoding);
        findViewById(R.id.tv_select_file).setOnClickListener(this);
        findViewById(R.id.tv_start_transcode).setOnClickListener(this);
        tvFilePath = findViewById(R.id.tv_filepath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
                break;
            case R.id.tv_start_transcode:
                if (TextUtils.isEmpty(filePath)) {
                    Toast.makeText(this, "还没有选中文件地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                String type = filePath.substring(filePath.indexOf(".") + 1);
                String outFilePath = filePath.replace(type, "pcm");
                Log.i("Logger", TAG + ".onClick type=" + type);
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
