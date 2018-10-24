package com.df.audiorecord.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.df.audiorecord.R;
import com.df.audiorecord.record.AudioRecordManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Danfeng on 2018/10/18.
 */

public class TimeAndVolumeView extends View {
    private Paint mTvPaint;
    private Paint mLinePaint;

    private int LINE_W;//默认矩形波纹的宽度，9像素, 原则上从layout的attr获得
    private int LINE_INC;//默认矩形波纹的宽度，9像素, 原则上从layout的attr获得
    private int MIN_WAVE_H = 2;//最小的矩形线高，是线宽的2倍，线宽从lineWidth获得
    private int MAX_WAVE_H = 7;//最高波峰，是线宽的4倍
    private int[] DEFAULT_WAVE_HEIGHT = {2, 2, 2, 2, 2, 2};
    private static final int UPDATE_INTERVAL_TIME = 100;//100ms更新一次

    private Context mContext;
    private String mDefaultString = "0:00";
    private String mShowText;
    private float mDefaulStrWidth;
    private LinkedList<Integer> mVolumeList = new LinkedList<>();//使用适合插入的linkedlist
    private LinkedList<Integer> mAllVolumeList = new LinkedList<>();//使用适合插入的linkedlist
    private boolean mIsRecording = false;
    private boolean mIsPlaying = false;
    private Timer mTimer;
    private TimerTask mTimerTask;


    public TimeAndVolumeView(Context context) {
        this(context, null);
    }

    public TimeAndVolumeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        resetList(mVolumeList, DEFAULT_WAVE_HEIGHT);
        LINE_W = (int) mContext.getResources().getDimension(R.dimen.x5);
        MIN_WAVE_H = (int) mContext.getResources().getDimension(R.dimen.y17);
        MAX_WAVE_H = (int) mContext.getResources().getDimension(R.dimen.y40);
        LINE_INC = (int) ((MAX_WAVE_H - MIN_WAVE_H) / 5.0f);
        //画笔
        mTvPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTvPaint.setColor(mContext.getResources().getColor(R.color.black_text_0_6));
        mTvPaint.setStyle(Paint.Style.FILL);
        mTvPaint.setTextAlign(Paint.Align.CENTER);
        mTvPaint.setTextSize(mContext.getResources().getDimension(R.dimen.text_size_14));
        mTvPaint.setFakeBoldText(true);

        //波纹线的画笔
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mContext.getResources().getColor(R.color.volume_wave));
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setStrokeWidth(LINE_W);
        //字符串长度
        mDefaulStrWidth = mTvPaint.measureText(mDefaultString);
        mShowText = "按下说话";
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int widthcentre = getWidth() / 2;
        int heightcentre = getHeight() / 2;
        canvas.drawText(mShowText, widthcentre, heightcentre - (mTvPaint.ascent() + mTvPaint.descent()) / 2, mTvPaint);
        if (mIsRecording) {
            //绘制左边的
            for (int i = 0; i < 6; i++) {
                canvas.drawLine(widthcentre - mDefaulStrWidth / 2 - LINE_W * 2 * (i + 1), (getHeight() - ((mVolumeList.get(i) - 2) * LINE_INC + MIN_WAVE_H)) / 2, widthcentre - mDefaulStrWidth / 2 - LINE_W * 2 * (i + 1), getHeight() - (getHeight() - ((mVolumeList.get(i) - 2) * LINE_INC + MIN_WAVE_H)) / 2, mLinePaint);
            }
            //绘制右边的
            for (int i = 0; i < 6; i++) {
                canvas.drawLine(widthcentre + mDefaulStrWidth / 2 + LINE_W * 2 * (i + 2), (getHeight() - ((mVolumeList.get(i) - 2) * LINE_INC + MIN_WAVE_H)) / 2, widthcentre + mDefaulStrWidth / 2 + LINE_W * 2 * (i + 2), getHeight() - (getHeight() - ((mVolumeList.get(i) - 2) * LINE_INC + MIN_WAVE_H)) / 2, mLinePaint);
            }
        }
        if (mIsPlaying) {
            for (int i = 0; i < 6; i++) {
//            Log.e("当前音量","总数"+mAllVolumeList.size()+"第几个"+i+"mPlayingCount"+mPlayingCount+"当前播放"+(mAllVolumeList.size() - (i + mPlayingCount+1)));
                canvas.drawLine(widthcentre - mDefaulStrWidth / 2 - LINE_W * 2 * (i + 1), (getHeight() - ((mAllVolumeList.get(mAllVolumeList.size() - (i + mPlayingCount+1)) - 2) * LINE_INC + MIN_WAVE_H)) / 2, widthcentre - mDefaulStrWidth / 2 - LINE_W * 2 * (i + 1), getHeight() - (getHeight() - ((mAllVolumeList.get(mAllVolumeList.size() - (i + mPlayingCount+1)) - 2) * LINE_INC + MIN_WAVE_H)) / 2, mLinePaint);
            }
            //绘制右边的
            for (int i = 0; i < 6; i++) {
                canvas.drawLine(widthcentre + mDefaulStrWidth / 2 + LINE_W * 2 * (i + 2), (getHeight() - ((mAllVolumeList.get(mAllVolumeList.size() - (i + mPlayingCount+1)) - 2) * LINE_INC + MIN_WAVE_H)) / 2, widthcentre + mDefaulStrWidth / 2 + LINE_W * 2 * (i + 2), getHeight() - (getHeight() - ((mAllVolumeList.get(mAllVolumeList.size() - (i + mPlayingCount+1)) - 2) * LINE_INC + MIN_WAVE_H)) / 2, mLinePaint);
            }
        }
    }

    public void refreshVolume() {
        float maxAmp = AudioRecordManager.getInstance().getMaxAmplitude();
        if (maxAmp>1){
            maxAmp=1;
        }
        if (maxAmp<0){
            maxAmp=0;
        }
        int waveH = 2 + Math.round(maxAmp * (7 - 2));//wave 在 2 ~ 7 之间
        mVolumeList.add(0, waveH);
        mVolumeList.removeLast();
        mAllVolumeList.add(0, waveH);
        postInvalidate();
    }

    public void setShowText(String text) {
        mShowText = text;
        postInvalidate();
    }

    private void resetList(List list, int[] array) {
        list.clear();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
    }

    public void startRecord() {
        mIsRecording = true;
        mShowText = mDefaultString;
        resetList(mVolumeList, DEFAULT_WAVE_HEIGHT);
        mAllVolumeList = new LinkedList<>();
        mAllVolumeList.addAll(mVolumeList);
        startRefreshVolumeTimer();
    }

    private void startRefreshVolumeTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mIsRecording) {
                    refreshVolume();
                } else {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 100);
    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0x11:
                    reset();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void tooShortRecord() {
        mShowText = "录音时间过短";
        mIsRecording = false;
        postInvalidate();
        mHandler.sendEmptyMessageDelayed(0x11,800);
    }

    public void stopRecord() {
        mIsRecording = false;
        postInvalidate();
    }

    public void reset() {
        mShowText = "按下说话";
        mIsRecording = false;
        resetList(mVolumeList, DEFAULT_WAVE_HEIGHT);
        postInvalidate();
    }

    public void startPlay() {
        mIsPlaying = true;
        mShowText = mDefaultString;
        postInvalidate();
        startPlayTimer();
    }

    int mPlayingCount;

    private void startPlayTimer() {
        mPlayingCount = 0;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mIsPlaying) {
                    ++mPlayingCount;
                    postInvalidate();
                } else {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        };
        mTimer.schedule(mTimerTask, 0, 100);
    }

    public void stopPlay() {
        mIsPlaying = false;
        postInvalidate();
    }
}
