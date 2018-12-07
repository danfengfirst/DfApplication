package com.df.expandableview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.df.expandableview.widget.ExpandableItemView;
import com.df.expandableview.widget.ExpandableView;

public class MainActivity extends AppCompatActivity {
    RadioGroup mRg;
    ExpandableItemView expandableview;
    LinearLayout mContentFl;
    View mAboutView;
    View mSymptomView;
    View mTreatmentView;
    RelativeLayout bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottom = findViewById(R.id.expandable_bottom_show);
        expandableview = findViewById(R.id.expandable_item);
        mRg = findViewById(R.id.comprehensive_rg);
        mContentFl = findViewById(R.id.content_fl);
        mAboutView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.about_content, null);
        mSymptomView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.symptom_content, null);
        mTreatmentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.treatment_content, null);
        mContentFl.removeAllViews();
        mContentFl.addView(mAboutView);
        bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableview.onClick();
            }
        });
        expandableview.setsHowBottomListener(new ExpandableItemView.SHowBottomListener() {
            @Override
            public void needShowBottom(boolean show) {
                Log.e("显示",""+show);
                bottom.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
        mRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.about_rbt:
                        mContentFl.removeAllViews();
                        mContentFl.addView(mAboutView);
                        expandableview.setmCollapsed(false);
                        expandableview.requestLayout();
                        break;
                    case R.id.symptom_rbt:
                        mContentFl.removeAllViews();
                        mContentFl.addView(mSymptomView);
                        expandableview.setmCollapsed(false);
                        expandableview.requestLayout();
                        break;
                    case R.id.treatment_rbt:
                        mContentFl.removeAllViews();
                        mContentFl.addView(mTreatmentView);
                        expandableview.setmCollapsed(false);
                        expandableview.requestLayout();
                        break;
                }
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
}
