/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.df.audiorecord.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.df.audiorecord.R;
import com.df.audiorecord.utils.audio2.SoundFile2;

import java.util.ArrayList;
import java.util.List;


/**
 * 这个在原来的基础上做了处理  录音最多30s，屏幕显示的波形也是30s的波形，没有数据的地方不绘制波形
 * SoudFile2的getSamplesPerFrame方法之前返回的是1024改成了1 ，从而获取原始数据，并对原始数据做进一步处理
 */
public class WaveformView2 extends View {
    // Colors
    private int line_offset;
    Paint paintLine;
    private int playFinish;

    private SoundFile2 mSoundFile;
    private int[] mHeightsAtThisZoomLevel;
    private int mSampleRate;
    private int mOffset;
    private int mSelectionStart;
    private boolean mInitialized;
    float mDensity;
    private int state = 0;
    List<Double> heights;
    int x2;

    public WaveformView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        init(context);

    }

    private void init(Context context) {
        x2 = (int) getResources().getDimension(R.dimen.x2);
        paintLine = new Paint();
        paintLine.setStrokeWidth(x2);
        paintLine.setColor(getResources().getColor(R.color.black_text_0_6));
        mSoundFile = null;
        mOffset = 0;
        mInitialized = false;
    }


    public boolean hasSoundFile() {
        return mSoundFile != null;
    }

    public void setSoundFile(SoundFile2 soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        computeDoublesForAllZoomLevels2();
        mHeightsAtThisZoomLevel = null;
    }

    public int getStart() {
        return mSelectionStart;
    }

    public int getOffset() {
        return mOffset;
    }


    public void recomputeHeights(float density) {
        mHeightsAtThisZoomLevel = null;
        mDensity = density;
        postInvalidate();
    }


    protected void drawWaveformLine(Canvas canvas,
                                    int x, int y0, int y1,
                                    Paint paint) {
        canvas.drawLine((int) x, y0, (int) x, y1, paint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (state == 1) {
            mSoundFile = null;
            state = 0;
            return;
        }

        if (mSoundFile == null) {
            return;
        }
        if (mHeightsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel();
        // Draw waveform
        int start = mOffset;
        int width = mHeightsAtThisZoomLevel.length - start;
        int ctr = measuredHeight / 2;
        if (width > measuredWidth)
            width = measuredWidth;
        // Draw waveform
        for (int i = 0; i < heights.size(); i++) {
            drawWaveformLine(
                    canvas, i * 2 * x2,
                    (ctr - 2 - mHeightsAtThisZoomLevel[start + i]),
                    (ctr + 2 + mHeightsAtThisZoomLevel[start + i]),
                    paintLine);

        }
    }

    private void computeDoublesForAllZoomLevels2() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        List<Double> smoothedGains = new ArrayList<>();
        //getMeasuredWidth() / (x2 * 2): 将屏幕的宽度 除以 (画笔宽度 +间隔的宽度(这里间隔宽度与画笔宽度相同))
        // mSampleRate * 30: 在audioRecorder中设置的采样频率为16000， 30s的采样总数据点应该是 30*采样频率
        //mSampleRate * 30 / (getMeasuredWidth() / (x2 * 2)): 每条线包含了多少个数据点
        int sampleCount = mSampleRate * 30 / (getMeasuredWidth() / (x2 * 2));
        int total = 0;
        double sum = 0;
        for (int i = 0; i < numFrames; i++) {
            total++;
            sum += frameGains[i];
            if (total > sampleCount) {
                //对sampleCount个数据点求平均，作为一条线的数据
                smoothedGains.add(sum / sampleCount);
                sum = 0;
                total = 0;
            }
        }
        //找到所有数据中最大值
        double maxGain = 1.0;
        for (int i = 0; i < smoothedGains.size(); i++) {
            if (smoothedGains.get(i) > maxGain) {
                maxGain = smoothedGains.get(i);
            }
        }
        // 以最大值作为基准
        heights = new ArrayList<>();
        for (int i = 0; i < smoothedGains.size(); i++) {
            double value = smoothedGains.get(i) / maxGain;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights.add(value);
        }

        mInitialized = true;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {
        int halfHeight = (getMeasuredHeight() / 2) - 1;
        mHeightsAtThisZoomLevel = new int[heights.size()];
        for (int i = 0; i < heights.size(); i++) {
            mHeightsAtThisZoomLevel[i] = (int) (heights.get(i) * halfHeight);
        }
    }


    public int getPlayFinish() {
        return playFinish;
    }

    public void setPlayFinish(int playFinish) {
        this.playFinish = playFinish;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getLine_offset() {
        return line_offset;
    }

    public void setLine_offset(int line_offset) {
        this.line_offset = line_offset;
    }

}
