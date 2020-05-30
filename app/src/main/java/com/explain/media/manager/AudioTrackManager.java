package com.explain.media.manager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AudioTrackManager {
    private static final String TAG = "AudioTrackManager";
    private AudioTrack audioTrack;
    private boolean isRecordPlaying = false;

    public void play(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(filePath);
                    InputStream is = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);

                    int minBufferSize = AudioTrack.getMinBufferSize(44100,
                            AudioFormat.CHANNEL_IN_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT);

                    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
                            AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                            minBufferSize, AudioTrack.MODE_STREAM);
                    Log.d(TAG, "Starting playback");
                    audioTrack.play();

                    int readResult = 0;
                    byte buffer[] = new byte[minBufferSize];
                    isRecordPlaying = true;

                    while (isRecordPlaying) {
                        readResult = bis.read(buffer, 0, minBufferSize);
                        Log.i(TAG, "result= " + readResult);
                        if (readResult != -1) {
                            audioTrack.write(buffer, 0, readResult);
                        } else {
                            isRecordPlaying = false;
                        }
                    }

                    audioTrack.stop();
                    bis.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }

    public void release() {
        if (audioTrack != null) {
            isRecordPlaying = false;
            audioTrack.stop();
            audioTrack.release();
        }
    }
}
