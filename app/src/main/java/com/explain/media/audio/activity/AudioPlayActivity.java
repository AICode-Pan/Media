package com.explain.media.audio.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.explain.media.R;
import com.explain.media.Utils.SDFileUtil;

import java.io.File;
import java.io.IOException;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/21
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class AudioPlayActivity extends Activity implements View.OnClickListener {
    private static final String TAG = AudioPlayActivity.class.getSimpleName();
    private final int ACTIVITY_RESULT = 0x01;

    private EditText filePath;
    private TextView fileSize;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_audio_player);
        filePath = findViewById(R.id.edt_file_path);
        fileSize = findViewById(R.id.txt_file_size);

        filePath.setText(SDFileUtil.getSDPath());

        findViewById(R.id.btn_choose_file).setOnClickListener(this);
        findViewById(R.id.btn_audio_play).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose_file:
                File file = new File(filePath.getText().toString());
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setDataAndType(Uri.fromFile(file), "*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, ACTIVITY_RESULT);
                break;
            case R.id.btn_audio_play:
                try {
                    String path =filePath.getText().toString();
                    Log.i(TAG, TAG + ".path : " + path);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(path);
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
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_RESULT && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            //getPath()获取文件目录，例如返回值为/storage/emulated/0/com.explain.media/MRecord.m4a
            String path = SDFileUtil.getPath(AudioPlayActivity.this, treeUri);
            Log.i(TAG, TAG + ".uri : " + treeUri + " ,path : " + path);
            updateFileInfo(path);
        }
    }

    /**
     * 更新页面上的文件信息
     *
     * @param uri
     */
    private void updateFileInfo(String uri) {

        filePath.setText(uri);

        File file = new File(uri);
        if (!file.exists()) {
            Toast.makeText(AudioPlayActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        long size = file.length();
        fileSize.setText("文件大小:" + SDFileUtil.FormetFileSize(size));
    }


}
