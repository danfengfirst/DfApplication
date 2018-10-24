package com.df.audiorecord.record;

/**
 * Created by Danfeng on 2018/10/17.
 */

public interface OnRecordingListener {
    void startRecord();
    void tooShort();
    void stopRecord();
    void reset();
}
