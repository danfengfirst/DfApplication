package com.df.audiorecord.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Danfeng on 2018/10/17.
 */

public class AudioRecordManager {

    private volatile static AudioRecordManager mInstance;
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

    public static AudioRecordManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioRecordManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManager();
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
        //   构造一个AudioRecord对象，其中需要的最小录音缓存buffer大小可以通过getMinBufferSize方法得到。如果buffer容量过小，将导致对象构造的失败。
        mRecBufSize = AudioRecord.getMinBufferSize(FREQUENCY,
                CHANNELCONGIFIGURATION, AUDIOENCODING);// 录音组件
        mAudioRecord = new AudioRecord(AUDIO_SOURCE,// 指定音频来源，这里为麦克风
                FREQUENCY, // 16000HZ采样频率
                CHANNELCONGIFIGURATION,// 录制通道
                AUDIO_SOURCE,// 录制编码格式
                mRecBufSize);// 录制缓冲区大小 //先修改
    }

    @SuppressLint("NewApi")
    public void createAudioName(Context context) {
        String fileCommonName = UUID.randomUUID().toString() + System.currentTimeMillis();
        mSavePcmPath = context.getExternalCacheDir() + File.separator + "audio" + File.separator + fileCommonName + ".pcm";// 用于获取APP的cache目录 /data/data/<application package>/cache目录
        mSaveWavPath = context.getExternalCacheDir() + File.separator + "audio" + File.separator + fileCommonName + ".wav";
    }

    public void startRecord() {
        mIsWriting = true;
        isRecording = true;
        new Thread(new WriteRunnable()).start();
        new RecordTask().execute();
    }

    class RecordTask extends AsyncTask<Object, Object, Object> {
        public RecordTask() {
            inBuf.clear();// 清除  换缓冲区的数据
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                short[] buffer = new short[mRecBufSize];
                mAudioRecord.startRecording();// 开始录制
                while (isRecording) {
                    // 从MIC保存数据到缓冲区
                    readsize = mAudioRecord.read(buffer, 0,
                            mRecBufSize);
                    synchronized (inBuf) {
                        for (int i = 0; i < readsize; i += rateX) {
                            inBuf.add(buffer[i]);
                            calculateRealVolume(buffer, mRecBufSize);
                        }
                    }
                    publishProgress();
                    if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                        synchronized (write_data) {
                            byte bys[] = new byte[readsize * 2];
                            //因为arm字节序问题，所以需要高低位交换
                            for (int i = 0; i < readsize; i++) {
                                byte ss[] = getBytes(buffer[i]);
                                bys[i * 2] = ss[0];
                                bys[i * 2 + 1] = ss[1];
                            }
                            write_data.add(bys);
                        }
                    }
                }
                mIsWriting = false;
            } catch (Throwable t) {

            }
            return null;
        }
    }

    public byte[] getBytes(short s) {
        byte[] buf = new byte[2];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) (s & 0x00ff);
            s >>= 8;
        }
        return buf;
    }

    /**
     * 异步写文件
     *
     * @author cokus
     */
    class WriteRunnable implements Runnable {
        @Override
        public void run() {
            mAudioRecord.startRecording();
            try {
                FileOutputStream fos2wav = null;
                File file2wav = null;
                try {
                    file2wav = new File(mSavePcmPath);
                    if (file2wav.exists()) {
                        file2wav.delete();
                    }
                    fos2wav = new FileOutputStream(file2wav);// 建立一个可存取字节的文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (mIsWriting || write_data.size() > 0) {
                    byte[] buffer = null;
                    synchronized (write_data) {
                        if (write_data.size() > 0) {
                            buffer = write_data.get(0);
                            write_data.remove(0);
                        }
                    }
                    try {
                        if (buffer != null) {
                            fos2wav.write(buffer);
                            fos2wav.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                fos2wav.close();
                Pcm2Wav p2w = new Pcm2Wav();//将pcm格式转换成wav 其实就尼玛加了一个44字节的头信息
                p2w.convertAudioFiles(mSavePcmPath, mSaveWavPath);
            } catch (Throwable t) {
            }
        }
    }

    /**
     * 停止录音，释放资源
     */
    public void stopRecord() {
        if (isRecording) {
            isRecording = false;
            mAudioRecord.stop();
            mAudioRecord = null;
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
     *获取音量
     * @return
     */
    public float getMaxAmplitude() {
        //处理后的音量
        return mVolume / 70.0f;
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
