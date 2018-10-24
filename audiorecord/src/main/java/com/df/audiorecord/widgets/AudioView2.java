package com.df.audiorecord.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.df.audiorecord.R;
import com.df.audiorecord.record.AudioPlayManager;
import com.df.audiorecord.record.AudioRecordManager;
import com.df.audiorecord.record.OnPlayingListener;
import com.df.audiorecord.record.OnRecordingListener;
import com.df.audiorecord.utils.audio2.SamplePlayer2;
import com.df.audiorecord.utils.audio2.SoundFile2;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.df.audiorecord.widgets.AudioRecordButton.STATE_START_RECORD_FINISH_OR_STOP;

/**
 * Created by Danfeng on 2018/10/18.
 */

public class AudioView2 extends LinearLayout {
    private Context mContext;
    private AudioRecordButton mAudioBt;
    private TimeAndVolumeView mTimeVolume;
    private WaveformView2 mWaveFormView;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private OnRecordingListenerImp mOnRecordListener = new OnRecordingListenerImp();
    private OnPlayingListenerImp mOnPlayListener = new OnPlayingListenerImp();
    int mTimeCount = 30;
    Activity mActivity;

    public AudioView2(Context context) {
        this(context, null);
    }

    public AudioView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.audio_view_item_layout2, this);
        mAudioBt = view.findViewById(R.id.audio_bt);
        mTimeVolume = view.findViewById(R.id.time_volume);
        mWaveFormView = view.findViewById(R.id.wave_view);
        mWaveFormView.setLine_offset(0);
        mAudioBt.setOnRecordingListener(mOnRecordListener);
        mAudioBt.setOnPlayingListener(mOnPlayListener);
    }

    /**
     * 没有权限的情况下按钮没有任何反应
     * @param audioEnable
     */
    public void setAudioEnable(boolean audioEnable) {
        mAudioBt.setEnabled(audioEnable);
    }


    class OnRecordingListenerImp implements OnRecordingListener {
        @Override
        public void startRecord() {
            mTimeVolume.startRecord();
            mTimeCount = 0;
            startRecordTimeCount();
        }

        @Override
        public void tooShort() {
            releaseTimer();
            mTimeCount = 0;
            mTimeVolume.tooShortRecord();
        }

        @Override
        public void stopRecord() {
            releaseTimer();
            mTimeCount = 0;
            mTimeVolume.stopRecord();
            loadFromFile();
        }

        @Override
        public void reset() {
            //移出按钮时 取消计时器
            releaseTimer();
            mTimeCount = 0;
            mTimeVolume.reset();
        }
    }

    class OnPlayingListenerImp implements OnPlayingListener {

        @Override
        public void onStart() {
            mTimeVolume.startPlay();
            startPlayTimeCount();
        }

        @Override
        public void onStop() {
            //结束播放
            mTimeVolume.stopPlay();
            releaseTimer();
        }

        @Override
        public void onComplete() {
            //结束播放
            mTimeVolume.stopPlay();
            releaseTimer();
            mAudioBt.setState(STATE_START_RECORD_FINISH_OR_STOP);
        }
    }

    private void startPlayTimeCount() {
        final int timeLimit = (int) Math.ceil(AudioPlayManager.getInstance().getDuration(AudioRecordManager.getInstance().getmSaveWavPath()) / 1000.0f);
        releaseTimer();
        mTimeCount = 0;
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mTimeCount < timeLimit) {
                    ++mTimeCount;
                    mTimeVolume.setShowText(String.format("%2d:%02d", 0, mTimeCount));
                } else {
                    releaseTimer();
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    private void startRecordTimeCount() {
        releaseTimer();
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mTimeCount < 30) {
                    ++mTimeCount;
                    mTimeVolume.setShowText(String.format("%2d:%02d", 0, mTimeCount));
                } else {
                    releaseTimer();
                    mAudioBt.autoStopRecord();
                    mTimeVolume.stopRecord();
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 1000);
    }

    public void releaseTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void bindActivity(Activity activity) {

        mActivity = activity;
        mAudioBt.setmActivity(activity);
    }

    /******************************************波形相关代码*************************************************/


    Thread mLoadSoundFileThread;
    boolean mLoadingKeepGoing;
    File mFile;
    SoundFile2 mSoundFile;
    SamplePlayer2 mPlayer;

    /**
     * 载入wav文件显示波形
     */
    private void loadFromFile() {
        try {
            Thread.sleep(300);//让文件写入完成后再载入波形 适当的休眠下
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mFile = new File(AudioRecordManager.getInstance().getmSaveWavPath());
        mLoadingKeepGoing = true;
        // Load the sound file in a background thread
        mLoadSoundFileThread = new Thread() {
            public void run() {
                try {
                    mSoundFile = SoundFile2.create(mFile.getAbsolutePath(), null);
                    if (mSoundFile == null) {
                        return;
                    }
                    mPlayer = new SamplePlayer2(mSoundFile);
                } catch (final Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (mLoadingKeepGoing) {
                    finishOpeningSoundFile();
                    postInvalidate();
                }
            }
        };
        mLoadSoundFileThread.start();
    }

    float mDensity;

    /**
     * waveview载入波形完成
     */
    private void finishOpeningSoundFile() {
        mWaveFormView.setSoundFile(mSoundFile);
        DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;
        mWaveFormView.recomputeHeights(mDensity);
    }

}
