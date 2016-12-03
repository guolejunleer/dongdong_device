package com.dongdong.prompt;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.dongdong.utils.DDLog;

/**
 * Launcher界面状态发生变化后的播放状态声音管理者
 * author leer （http://www.dd121.com）
 * created at 2016/8/24 16:24
 */
public class MediaMusicOfCall {

    private final static MediaPlayer mMediaPlayer = new MediaPlayer();

    public static void intPlayData(Context context) {
        try {
            AssetFileDescriptor openFd = context.getAssets().openFd("calling.MP3");
            DDLog.i("MediaMusicOfCall.clazz--->>>construct openFd :" + openFd);
            mMediaPlayer.setDataSource(openFd.getFileDescriptor(),
                    openFd.getStartOffset(), openFd.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void playMusic() {
        try {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void stopMusic() {
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void release() {
        try {
            mMediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
