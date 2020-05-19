package com.explain.media.activity.second;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.activity.BaseActivity;
import com.explain.media.utils.MediaFile;
import com.explain.media.utils.SDFileUtil;

/**
 * 转码
 */
public class TranscodingActivity extends BaseActivity implements View.OnClickListener {
    private TextView tvFilePath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("音频转码");

        setContentView(R.layout.activity_transcoding);
        findViewById(R.id.tv_select_file).setOnClickListener(this);
        tvFilePath = findViewById(R.id.tv_filepath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
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

        tvFilePath.setText("文件地址:" + filePath);
    }
}
