package com.df.expandableview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.df.expandableview.R;

/**
 * Created by Danfeng on 2018/11/27.
 */

public class ExpandableItemView extends LinearLayout {
    private Context mContext;
    private static final int DEFAULT_ANIM_DURATION = 300;

    private static final float DEFAULT_ANIM_ALPHA_START = 0f;
    private static final boolean DEFAULT_SHOW = true;
    private boolean mCollapsed = false; //false：收缩 true：展开

    private Drawable mExpandDrawable;

    private Drawable mCollapseDrawable;

    private int mAnimationDuration;

    private float mAnimAlphaStart;

    private boolean mAnimating;

    /* Listener for callback */
//    private ExpandableView.OnExpandStateChangeListener mListener;
    private int mHeight;//需要改变的高度
    private int mMinHeight = 400;//最小距离
    private boolean addFlag = false;

    public ExpandableItemView(Context context) {
        this(context, null);
    }

    public ExpandableItemView(Context context, AttributeSet attrs) {
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
            mExpandDrawable = getDrawable(getContext(), R.mipmap.more_icon);
        }
        if (mCollapseDrawable == null) {
            mCollapseDrawable = getDrawable(getContext(), R.mipmap.appoint_less);
        }
        setOrientation(LinearLayout.VERTICAL);
    }

    @Override
    public void setOrientation(int orientation) {
        if (LinearLayout.HORIZONTAL == orientation) {
            throw new IllegalArgumentException("ExpandableTextView only supports Vertical Orientation.");
        }
        super.setOrientation(orientation);
    }


    public void onClick() {
        if (!mAnimating) {
            mCollapsed = !mCollapsed;
            mAnimating = true;
//        Log.i("hah:",mCollapsed+"=getHeight="+getHeight()+"=mHeight="+mHeight+"=mMinHeight="+mMinHeight);
            Animation animation;
            if (mCollapsed) {
                //true 展开
                animation = new ExpandCollapseAnimation(this, mMinHeight, mHeight);//kuo
            } else {
                //false 收缩
                animation = new ExpandCollapseAnimation(this, getHeight(), mMinHeight);//suo
            }

            animation.setFillAfter(true);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    mAnimating = false;

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            clearAnimation();
            startAnimation(animation);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setmCollapsed(boolean mCollapsed) {
        this.mCollapsed = mCollapsed;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getVisibility() == View.GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int heightMode = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.UNSPECIFIED);
        super.onMeasure(widthMeasureSpec, heightMode);
        mHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            // 当前子空间实际占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            // 当前子空间实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            mHeight = mHeight + childHeight;
        }
        if (sHowBottomListener!=null){
            sHowBottomListener.needShowBottom(mHeight>mMinHeight?true:false);
        }
        Log.e("EX高度",""+mHeight);
        if (mCollapsed) {
            setMeasuredDimension(widthMeasureSpec, mHeight);
            return;
        }
//        //如果关闭
        if (!mCollapsed) {
            int height = mHeight;
            // Saves the collapsed height of this ViewGroup
            if (height > mMinHeight) {
                height = mMinHeight;
            }
            setMeasuredDimension(widthMeasureSpec, height);
        }
    }

    public  interface SHowBottomListener{
        void needShowBottom(boolean show);
    }
    private SHowBottomListener sHowBottomListener;

    public void setsHowBottomListener(SHowBottomListener li) {
        this.sHowBottomListener = li;
    }

    private static boolean isPostHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    private static boolean isPostLolipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void applyAlphaAnimation(View view, float alpha, int duration) {
        if (isPostHoneycomb()) {
            view.setAlpha(1);
        } else {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, alpha);
            // make it instant
            alphaAnimation.setDuration(duration);
            alphaAnimation.setFillAfter(true);
            view.startAnimation(alphaAnimation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        Resources resources = context.getResources();
        if (isPostLolipop()) {
            return resources.getDrawable(resId, context.getTheme());
        } else {
            return resources.getDrawable(resId);
        }
    }


    class ExpandCollapseAnimation extends Animation {
        private final View mTargetView;
        private final int mStartHeight;
        private final int mEndHeight;

        public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
            mTargetView = view;
            mStartHeight = startHeight;
            mEndHeight = endHeight;
            setDuration(mAnimationDuration);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
            setMinimumHeight(newHeight);
            mTargetView.getLayoutParams().height = newHeight;
            mTargetView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface OnExpandStateChangeListener {
        /**
         * Called when the expand/collapse animation has been finished
         *
         * @param textView   - TextView being expanded/collapsed
         * @param isExpanded - true if the TextView has been expanded
         */
        void onExpandStateChanged(LinearLayout textView, boolean isExpanded);
    }

}
