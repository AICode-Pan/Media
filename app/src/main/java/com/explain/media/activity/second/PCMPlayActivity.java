package com.explain.media.activity.second;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.activity.base.BaseActivity;
import com.explain.media.manager.AudioTrackManager;
import com.explain.media.utils.MediaFile;

public class PCMPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = PCMPlayActivity.class.getSimpleName();

    private TextView tvSelectFile, tvFilePath, tvPlay;
    private TextView tvFileInfo;
    private String filePath;
    private AudioTrackManager audioTrackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("PCM文件播放");

        setContentView(R.layout.activity_audio_player);
        tvSelectFile = findViewById(R.id.tv_select_file);
        tvFilePath = findViewById(R.id.tv_filepath);
        tvPlay = findViewById(R.id.btn_audio_play);
        tvFileInfo = findViewById(R.id.tv_file_info);

        tvSelectFile.setOnClickListener(this);
        findViewById(R.id.btn_audio_play).setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
                break;
            case R.id.btn_audio_play:
                play();
                break;
        }
    }

    @Override
    protected void onSelectedFile(String filePath) {
        super.onSelectedFile(filePath);
        if (!MediaFile.isPCMFileType(filePath)) {
            Toast.makeText(this, "文件格式错误，不是PCM文件", Toast.LENGTH_SHORT).show();
            return;
        }

        this.filePath = filePath;
        tvFilePath.setText("文件地址:" + filePath);
    }


    /**
     * 更新页面上的文件信息
     *
     * @param uri
     */
    private void updateFileInfo(String uri) {
        switch (0) {
            case 0:
                setTitle("AudioTrack播放");
                break;
            case 1:
                setTitle("MediaPlayer播放");
                break;
            case 2:
                setTitle("OpenSL播放");
                break;
        }
    }

    private void play() {
        if (!TextUtils.isEmpty(filePath)) {
            audioTrackManager = new AudioTrackManager();
            audioTrackManager.play(filePath);
        }
    }

    private void pause() {
        if (audioTrackManager != null) {
            audioTrackManager.release();
        }
    }
}
