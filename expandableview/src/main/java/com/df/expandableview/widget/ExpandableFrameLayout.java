package com.df.expandableview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * Created by Danfeng on 2018/11/28.
 */

public class ExpandableFrameLayout extends LinearLayout {
    private int mHeight;

    public ExpandableFrameLayout(@NonNull Context context) {
        super(context);
    }

    public ExpandableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            // 测量每一个child的宽和高
            //    measureChildren(widthMeasureSpec, heightMeasureSpec);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            // 当前子空间实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            mHeight = mHeight + childHeight;
        }

        setMeasuredDimension(widthMeasureSpec, mHeight);
        Log.e("高度",""+mHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
