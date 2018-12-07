package com.df.audiorecord.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Danfeng on 2018/10/17.
 */

public class AudioRecordManager2 {

    private volatile static AudioRecordManager2 mInstance;
    private MediaRecorder mMediaRecorder;
    private String mAudioFileName;
    private boolean isRecording = false;
    private int readsize;
    public int rateX = 100;//控制多少帧取一帧
    private static final int FREQUENCY = 16000;// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static final int CHANNELCONGIFIGURATION = AudioFormat.CHANNEL_IN_MONO;// 设置单声道声道
    private static final int AUDIOENCODING = AudioFormat.ENCODING_PCM_16BIT;// 音频数据格式：每个样本16位
    public final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;// 音频获取源
    private int mRecBufSize;// 录音最小buffer大小
    private AudioRecord mAudioRecord;
    private String mSavePcmPath;
    private String mSaveWavPath;
    private ArrayList<Short> inBuf = new ArrayList<Short>();//缓冲区数据
    private ArrayList<byte[]> write_data = new ArrayList<byte[]>();//写入文件数据
    private boolean mIsWriting;
    private int mVolume;
    BufferedOutputStream out;

    public static AudioRecordManager2 getInstance() {
        if (mInstance == null) {
            synchronized (AudioRecordManager2.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManager2();
                }
            }
        }
        return mInstance;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void prepare(Context context) {
        createAudioName(context);
        try {
            File file_pcm = new File(mSavePcmPath);
            if (!file_pcm.getParentFile().exists()) {
                file_pcm.getParentFile().mkdirs();
            }
            file_pcm.createNewFile();
            File file_wav = new File(mSaveWavPath);
            file_wav.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initAudioRecorder();
    }

    private void initAudioRecorder() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioChannels(1);
        mMediaRecorder.setAudioSamplingRate(FREQUENCY);
        mMediaRecorder.setOutputFile(mSaveWavPath);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public void createAudioName(Context context) {
        String fileCommonName = UUID.randomUUID().toString() + System.currentTimeMillis();
        mSavePcmPath = context.getExternalCacheDir() + File.separator + "audio" + File.separator + fileCommonName + ".pcm";// 用于获取APP的cache目录 /data/data/<application package>/cache目录
        mSaveWavPath = context.getExternalCacheDir() + File.separator + "audio" + File.separator + fileCommonName + ".aac";
    }

    public void startRecord() {
        mIsWriting = true;
        isRecording = true;
        mMediaRecorder.start();
    }


    /**
     * 停止录音，释放资源
     */
    public void stopRecord() {
        if (isRecording) {
            isRecording = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }

    }

    /**
     * 删除资源文件
     */
    public void deleteAudioFile() {
        if (mSaveWavPath != null) {
            File file = new File(mSaveWavPath);
            if (file.exists()) {
                file.delete();
            }
            mSaveWavPath = null;
        }
        if (mSavePcmPath != null) {
            File file = new File(mSavePcmPath);
            if (file.exists()) {
                file.delete();
            }
            mSavePcmPath = null;
        }
    }

    /*
     *获取音量 0-32767
     * @return
     */
    public float getMaxAmplitude() {
        //处理后的音量
        if (mMediaRecorder != null) {
         float   volume=mMediaRecorder.getMaxAmplitude() * 1.0f / 5000;
            Log.e("录音",  volume+ "");
            return volume;
        }
            return 0.0f;

//        return mVolume / 70.0f;
    }

    public String getmSavePcmPath() {
        if (!TextUtils.isEmpty(mSavePcmPath)) return mSavePcmPath;
        return mSavePcmPath;
    }

    public String getmSaveWavPath() {
        if (!TextUtils.isEmpty(mSaveWavPath)) return mSaveWavPath;
        return mSavePcmPath;
    }

    public String getAudioFileName() {
        if (!TextUtils.isEmpty(mAudioFileName)) return mAudioFileName;
        return "";
    }


    protected void calculateRealVolume(short[] buffer, int readSize) {
        double sumVolume = 0.0;
        double avgVolume = 0.0;
        double volume = 0.0;
        for (short b : buffer) {
            sumVolume += Math.abs(b);
        }
        avgVolume = sumVolume / buffer.length;
        mVolume = (int) (Math.log10(1 + avgVolume) * 10);
    }

    /**
     * 5.0及一下判断是是否有录音权限
     */
    public boolean checkAudioPermission(final Context context) {
        initAudioRecorder();
        //开始录制音频
        try {
            // 防止某些手机崩溃，例如联想
            mAudioRecord.startRecording();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING
                && mAudioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
            return false;
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            //如果短时间内频繁检测，会造成audioRecord还未销毁完成，此时检测会返回RECORDSTATE_STOPPED状态，再去read，会读到0的size，可以更具自己的需求返回true或者false
            return false;
        }

        byte[] bytes = new byte[1024];
        int readSize = mAudioRecord.read(bytes, 0, 1024);
        if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize <= 0) {
            return false;
        }
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        return true;
    }
}
