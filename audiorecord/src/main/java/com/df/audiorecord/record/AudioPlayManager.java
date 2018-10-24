package com.df.audiorecord.record;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.text.TextUtils;

import java.io.IOException;

/**
 * Created by Danfeng on 2018/10/17.
 */

public class AudioPlayManager implements MediaPlayer.OnCompletionListener {
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private MediaPlayer mMediaPlayer;
    private OnPlayingListener mOnplayingListener;
    private static AudioPlayManager mInstance;

    public static AudioPlayManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioPlayManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioPlayManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "SCREEN_ON");
        mWakeLock.setReferenceCounted(false);
    }

    public int getDuration(String path) {
        if (TextUtils.isEmpty(path))
            return -1;
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            int duration = player.getDuration();
            player.release();
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void startPlay(String path) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                    if (mOnplayingListener!=null){
                        mOnplayingListener.onStart();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void resume() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void release() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mOnplayingListener!=null){
            mOnplayingListener.onStop();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mOnplayingListener != null) {
            mOnplayingListener.onComplete();
        }
    }

    public void setOnplayingListener(OnPlayingListener listener) {
        mOnplayingListener = listener;
    }
}
