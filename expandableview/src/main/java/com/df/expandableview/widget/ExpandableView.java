

package com.df.expandableview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.df.expandableview.R;

public class ExpandableView extends LinearLayout {
    private Context mContext;
    private static final int DEFAULT_ANIM_DURATION = 300;

    private static final float DEFAULT_ANIM_ALPHA_START = 0f;
    private static final boolean DEFAULT_SHOW = true;
    private boolean mCollapsed = true; // Show short version as default.

    private Drawable mExpandDrawable;

    private Drawable mCollapseDrawable;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;
    private ExpandableItemView mExpandableItem;
    private RelativeLayout mBottomLl;

    /* Listener for callback */
    private int mHeight;//需要改变的高度
    private int mMinHeight;//最小距离
    private boolean addFlag = false;

    public ExpandableView(Context context) {
        this(context, null);
    }

    public ExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableView);
        mAnimationDuration = typedArray.getInt(R.styleable.ExpandableView_animDuration, DEFAULT_ANIM_DURATION);
        mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
        mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableView_expandDrawable);
        mCollapseDrawable = typedArray.getDrawable(R.styleable.ExpandableView_collapseDrawable);

        if (mExpandDrawable == null) {
            mExpandDrawable = getResources().getDrawable(R.mipmap.more_icon);
        }
        if (mCollapseDrawable == null) {
            mCollapseDrawable = getResources().getDrawable(R.mipmap.appoint_less);
        }
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        findView();
    }

    private void findView() {
        mExpandableItem = findViewById(R.id.expandable_item);
        mBottomLl = findViewById(R.id.expandable_bottom_show);
        mBottomLl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableItem.onClick();
            }
        });
    }

    public void refresh() {
        if (mExpandableItem != null) {
            mExpandableItem.requestLayout();
            mExpandableItem.invalidate();
        }
    }
}