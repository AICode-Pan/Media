package com.explain.media.activity.second;

import android.media.MediaPlayer;
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

import java.io.IOException;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/21
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class AudioPlayActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = AudioPlayActivity.class.getSimpleName();
    private int type = 0;

    private TextView tvSelectFile, tvFilePath, tvPlay;
    private TextView tvFileInfo;
    private MediaPlayer mediaPlayer;
    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra("type", 0);
        if (type == 0) {
            setTitle("AudioTrack播放");
        } else if (type == 1) {
            setTitle("OpenSL播放");
        }

        setContentView(R.layout.activity_audio_player);
        tvSelectFile = findViewById(R.id.tv_select_file);
        tvFilePath = findViewById(R.id.tv_filepath);
        tvPlay = findViewById(R.id.btn_audio_play);
        tvFileInfo = findViewById(R.id.tv_file_info);

        tvSelectFile.setOnClickListener(this);
        findViewById(R.id.btn_audio_play).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_select_file:
                selectFile();
                break;
            case R.id.btn_audio_play:
                audioPlay();
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

    /**
     * 更新页面上的文件信息
     *
     * @param uri
     */
    private void updateFileInfo(String uri) {

    }

    private void audioPlay() {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                Log.i(TAG, TAG + ".path : " + filePath);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(TAG, TAG + ".onCompletion");
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
