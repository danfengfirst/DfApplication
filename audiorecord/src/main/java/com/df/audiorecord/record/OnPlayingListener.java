package com.df.audiorecord.record;

public interface OnPlayingListener {
    void onStart();

    void onStop();

    void onComplete();
}