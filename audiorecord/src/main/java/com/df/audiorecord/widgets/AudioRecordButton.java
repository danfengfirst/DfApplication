package com.df.audiorecord.widgets;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.df.audiorecord.MainActivity;
import com.df.audiorecord.R;
import com.df.audiorecord.record.AudioPlayManager;
import com.df.audiorecord.record.AudioRecordManager;
import com.df.audiorecord.record.OnPlayingListener;
import com.df.audiorecord.record.OnRecordingListener;
import com.df.audiorecord.utils.BitmapUtil;
import com.df.audiorecord.utils.ToastUitl;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.functions.Consumer;


/**
 * Created by Danfeng on 2018/10/17.
 */

public class AudioRecordButton extends View {
    public static final int STATE_START_RECORD = 0x111;//点击开始录制
    public static final int STATE_RECORDING = 0x112;//点击开始录制
    public static final int STATE_START_RECORD_FINISH_OR_STOP = 0x222;//录制完成或者开始录制
    public static final int STATE_PLAYING = 0x333;//正在播放
    private int mState = STATE_START_RECORD;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private Paint mBgCirclePaint;//圆形背景画笔
    private Paint mRecordingBgCirclePaint;//录音过程中圆形背景画笔
    private Paint mBorderCirclePaint;//圆形边框画笔
    private ValueAnimator mValueAnimator;//圆形边框动画
    private float mProgressing;

    private Bitmap mStartRecordBp;//正常录音按钮  录音图标
    private Bitmap mRecordingBp;//录音过程中按钮  方形
    private Bitmap mFinishRecordBp;//录音完成  三角
    private Bitmap mPlayingRecordBp;//zheng
    //中心matrix
    private Matrix mStartRecordMt;
    private Matrix mPlayingMt;
    private Matrix mFinishRecordMt;


    private int mDimen_x48;
    private int mDimen_x35;
    private int mDimen_y53;
    private int mDimen_y56;
    private int mDimen_X117;
    private int mDimen_border;
    private boolean mReady;
    private float mTime;
    private OnRecordingListener mRecordingListener;
    private OnPlayingListener mPlayingListener;
    //录音最短时间
    private Timer mRecordTime;
    private TimerTask mRecordTimeTask;

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public void init(Context context) {
        mContext = context;
        initPaint();
        initIconAndMt();
        initDimen();
        initListener();
    }

    private void initListener() {
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (AudioRecordManager.getInstance().checkAudioPermission(mContext)) {
                    if (mState == STATE_PLAYING) {
                        AudioPlayManager.getInstance().release();
                    }
                    mReady = true;
                    setState(STATE_RECORDING);
                    AudioRecordManager.getInstance().prepare(mContext);
                    AudioRecordManager.getInstance().startRecord();
                    if (mRecordingListener != null) {
                        mRecordingListener.startRecord();
                    }
                    mTime = 0;
                    startTimeThread();

                }

                return false;
            }
        });

        setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                switch (mState) {
                    case STATE_RECORDING:
                        setState(STATE_START_RECORD_FINISH_OR_STOP);
                        break;
                    case STATE_START_RECORD_FINISH_OR_STOP:
                        //如果录制完毕则进行播放,重新播放
                        AudioPlayManager.getInstance().init(mContext);
                        AudioPlayManager.getInstance().setOnplayingListener(mPlayingListener);
                        AudioPlayManager.getInstance().startPlay(AudioRecordManager.getInstance().getmSaveWavPath());
                        mProgressing = 0;
                        startValueAnimator(AudioPlayManager.getInstance().getDuration(AudioRecordManager.getInstance().getmSaveWavPath()));
                        setState(STATE_PLAYING);
                        break;
                    case STATE_PLAYING:
                        //如果是正在播放
                        AudioPlayManager.getInstance().release();
                        setState(STATE_START_RECORD_FINISH_OR_STOP);
                        break;
                }
            }
        });
    }

    private void initDimen() {
        //不同情况下，尺寸
        mDimen_x48 = (int) mContext.getResources().getDimension(R.dimen.x48);
        mDimen_x35 = (int) mContext.getResources().getDimension(R.dimen.x35);
        mDimen_y53 = (int) mContext.getResources().getDimension(R.dimen.y53);
        mDimen_y56 = (int) mContext.getResources().getDimension(R.dimen.y56);
        mDimen_X117 = (int) mContext.getResources().getDimension(R.dimen.x117);
    }

    private void initIconAndMt() {
        //三种不同状态的图片
        mStartRecordBp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.start_record);
        mFinishRecordBp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.record_finish);
        mPlayingRecordBp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.playing);
        mRecordingBp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.recording);
        mStartRecordMt = new Matrix();
        mPlayingMt = new Matrix();
        mFinishRecordMt = new Matrix();
    }

    private void initPaint() {
        //正常情况下背景圆
        mBgCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgCirclePaint.setColor(mContext.getResources().getColor(R.color.audio_record_circle_bg));
        mBgCirclePaint.setStyle(Paint.Style.FILL);
        //录音时背景圆
        mRecordingBgCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRecordingBgCirclePaint.setColor(mContext.getResources().getColor(R.color.audio_recording_circle_bg));
        mRecordingBgCirclePaint.setStyle(Paint.Style.FILL);
        //边缘
        mBorderCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderCirclePaint.setColor(mContext.getResources().getColor(R.color.audio_record_circle_border));
        mDimen_border = (int) mContext.getResources().getDimension(R.dimen.x6);
        mBorderCirclePaint.setStrokeWidth(mDimen_border);
        mBorderCirclePaint.setStyle(Paint.Style.STROKE);
        mBorderCirclePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 开启计算录制最短时间
     */
    private void startTimeThread() {
        mRecordTime = new Timer();
        mRecordTimeTask = new TimerTask() {
            @Override
            public void run() {
                if (mReady) {
                    mTime = mTime + 0.1f;
                } else {
                    if (mRecordTime != null) {
                        mRecordTime.cancel();
                        mRecordTime = null;
                    }
                }
            }
        };
        mRecordTime.schedule(mRecordTimeTask, 0, 100);
    }


    /**
     * 播放音频文件时进度得动画
     *
     * @param totalDuration
     */
    private void startValueAnimator(int totalDuration) {
        mValueAnimator = ValueAnimator.ofFloat(mProgressing, 360);
        mValueAnimator.setDuration(totalDuration);
        mValueAnimator.setTarget(mProgressing);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgressing = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.start();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (mState) {
            case STATE_START_RECORD:
                canvas.drawCircle(mWidth / 2.0f, mHeight / 2.0f, mWidth / 2.0f, mBgCirclePaint);
                // 48 56
                canvas.drawBitmap(mStartRecordBp, mStartRecordMt, mBgCirclePaint);
                break;
            case STATE_RECORDING:
                canvas.drawCircle(mWidth / 2.0f, mHeight / 2.0f, mWidth / 2.0f, mRecordingBgCirclePaint);
                canvas.drawBitmap(mRecordingBp, mStartRecordMt, mBgCirclePaint);
                canvas.drawCircle(mWidth / 2.0f, mHeight / 2.0f, mWidth / 2.0f - mDimen_border / 2, mBorderCirclePaint);
                break;
            case STATE_START_RECORD_FINISH_OR_STOP:
                canvas.drawCircle(mWidth / 2.0f, mHeight / 2.0f, mWidth / 2.0f, mBgCirclePaint);
                //35 53
                canvas.drawBitmap(mFinishRecordBp, mFinishRecordMt, mBgCirclePaint);
                break;
            case STATE_PLAYING:
                canvas.drawCircle(mWidth / 2.0f, mHeight / 2.0f, mWidth / 2.0f, mBgCirclePaint);
                //48 48
                canvas.drawBitmap(mPlayingRecordBp, mPlayingMt, mBgCirclePaint);
                canvas.drawArc(mDimen_border / 2, mDimen_border / 2, mWidth - mDimen_border / 2, mHeight - mDimen_border / 2, -90, mProgressing, false, mBorderCirclePaint);
                break;
        }
    }

    /**
     * 解决滑动冲突，处理滑动事件拦截，否则当外层是scrollview的时候onFingerMove()方法将不会执行
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        this.setEnabled(checkPermission());
        return super.dispatchTouchEvent(event);
    }

    float mDowPointY;
    float mDowPointX;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDowPointY = event.getY();
                mDowPointX = event.getX();
                if (!checkPermission()) {
                    return super.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onFingerMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (!mReady) {
                    return super.onTouchEvent(event);
                }
                if (mReady && mTime < 0.6f) {
                    if (mRecordingListener != null) {
                        mRecordingListener.tooShort();
                    }
                    AudioRecordManager.getInstance().stopRecord();
                    //删除文件
                    AudioRecordManager.getInstance().deleteAudioFile();
                    setState(STATE_START_RECORD);
                    mReady = false;
                } else {
                    //录制完成
                    AudioRecordManager.getInstance().stopRecord();
                    if (mRecordingListener != null) {
                        mRecordingListener.stopRecord();
                    }
                    mReady = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mReady) {
                    return super.onTouchEvent(event);
                } else {
                    AudioRecordManager.getInstance().stopRecord();
                    //删除文件
                    AudioRecordManager.getInstance().deleteAudioFile();
                    setState(STATE_START_RECORD);
                    mReady = false;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void autoStopRecord() {
        AudioRecordManager.getInstance().stopRecord();
        if (mRecordingListener != null) {
            mRecordingListener.stopRecord();
        }
        mReady = false;
    }

    private void onFingerMove(MotionEvent event) {
        float currentY = event.getY();
        float currentX = event.getX();
        boolean isCanceledX = checkCancel(currentX);
        boolean isCanceledY = checkCancel(currentY);
        if (isCanceledX || isCanceledY) {
            AudioRecordManager.getInstance().stopRecord();
            //删除文件
            AudioRecordManager.getInstance().deleteAudioFile();
            setState(STATE_START_RECORD);
            mReady = false;
            if (mRecordingListener != null) {
                mRecordingListener.reset();
            }
        }
    }

    private boolean checkCancel(float currentY) {
        return Math.abs(mDowPointY - currentY) >= (mDimen_X117 / 3.0f * 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = getDefaultSize(getMeasuredWidth(), widthMeasureSpec);
        mHeight = getDefaultSize(getMeasuredHeight(), heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 48 56
        mStartRecordMt = getCorrectMatrix(mStartRecordMt, mStartRecordBp, mDimen_x48, mDimen_y56);
        //35 53
        mFinishRecordMt = getCorrectMatrix(mFinishRecordMt, mFinishRecordBp, mDimen_x35, mDimen_y53);
        //48 48
        mPlayingMt = getCorrectMatrix(mPlayingMt, mPlayingRecordBp, mDimen_x48, mDimen_x48);
    }

    public Matrix getCorrectMatrix(Matrix matrix, Bitmap bitmap, int width, int height) {
        int bw = bitmap.getWidth();
        int bh = bitmap.getHeight();
        float scale = Math.min(1f * width / bw, 1f * height / bh);
        bitmap = BitmapUtil.scaleBitmap(bitmap, scale);
        // compute init left, top
        int bbw = bitmap.getWidth();
        int bbh = bitmap.getHeight();
        Point center = new Point(mWidth / 2, mHeight / 2);
        Point bmpCenter = new Point(bbw / 2, bbh / 2);
        matrix.postScale(1, 1, center.x, center.y); // 中心点参数是有用的
        matrix.postTranslate(center.x - bmpCenter.x, center.y - bmpCenter.y); // 移动到当前view 的中心
        return matrix;
    }

    public void setState(int state) {
        mState = state;
        invalidate();
    }

    private Activity mActivity;

    public void setmActivity(Activity mActivity) {
        this.mActivity = mActivity;
    }

    /**
     * 录音与文件独写权限检测
     *
     * @return
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            if (AudioRecordManager.getInstance().checkAudioPermission(mContext)) {
                return true;
            } else {
                ToastUitl.showShort(mContext, "请前往设置中开启录音权限");
                return false;
            }
        } else {
            final boolean[] record_permissiont = {false};
            final boolean[] write_permissiont = {false};
            RxPermissions permissions = new RxPermissions(mActivity);
            permissions.requestEach(Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Consumer<Permission>() {
                @Override
                public void accept(Permission permission) throws Exception {
                    if (permission.name == Manifest.permission.RECORD_AUDIO) {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            record_permissiont[0] = true;
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            record_permissiont[0] = false;

                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUitl.showShort(mContext, "请前往设置中开启" + permission.name + "权限");
                            record_permissiont[0] = false;
                        }
                    } else {
                        if (permission.granted) {
                            // 用户已经同意该权限
                            write_permissiont[0] = true;
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            write_permissiont[0] = false;
                        } else {
                            // 用户拒绝了该权限，并且选中『不再询问』
                            ToastUitl.showShort(mContext, "请前往设置中开启" + permission.name + "权限");
                            write_permissiont[0] = false;
                        }
                    }
                }
            });
            return record_permissiont[0] && write_permissiont[0];
        }
    }


    public void setOnRecordingListener(OnRecordingListener li) {
        mRecordingListener = li;
    }

    public void setOnPlayingListener(OnPlayingListener onPlayingListener) {
        mPlayingListener = onPlayingListener;
    }
}
