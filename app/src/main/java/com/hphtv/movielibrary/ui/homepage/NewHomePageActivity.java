package com.hphtv.movielibrary.ui.homepage;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.LayoutHomepageTabBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class NewHomePageActivity extends AppBaseActivity<NewHomePageViewModel, ActivityNewHomepageBinding> implements IAutofitHeight {
    private NewHomePageTabAdapter mNewHomePageTabAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ObjectAnimator mPageDownObjectAnimator;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewHomePageTabAdapter = new NewHomePageTabAdapter(this, getSupportFragmentManager());
        mBinding.viewpager.setAdapter(mNewHomePageTabAdapter);
        mBinding.tablayout.setupWithViewPager(mBinding.viewpager);
        initTab();
        autoScrollListener(mBinding.nsv);
    }

    private void initTab() {
//        mBinding.tablayout.getTabAt(0).setCustomView()
        mBinding.tablayout.getTabAt(0).setCustomView(buildTabView(getString(R.string.tab_homepage)));
        for (int i = 0; i < mBinding.tablayout.getTabCount(); i++) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.tablayout.getTabAt(i).view.getLayoutParams();//
            params.rightMargin = DensityUtil.dip2px(this, 32);
            mBinding.tablayout.getTabAt(i).view.setLayoutParams(params);
        }
    }

    /**
     * Viewpager+Scrollview自适应
     *
     * @return
     */
    @Override
    public NoScrollAutofitHeightViewPager getViewPager() {
        return mBinding.viewpager;
    }

    private View buildTabView(String text) {
        LayoutHomepageTabBinding homepageTabBinding = LayoutHomepageTabBinding.inflate(getLayoutInflater());
        homepageTabBinding.setText(text);
        return homepageTabBinding.getRoot();
    }

    /**
     * 焦点自动滚动到屏幕中间位置
     */
    public void autoScrollListener(View view) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        int widthPixel = outMetrics.widthPixels;
        int heightPixel = outMetrics.heightPixels;
        view.getViewTreeObserver()
                .addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                    LogUtil.v("focus change");
                    if (newFocus != null) {
                        int[] ints = new int[2];
                        newFocus.getLocationInWindow(ints);

                        int viewCenter = Math.round((newFocus.getHeight() >> 1) + ints[1]);//当前获取焦点的view中心
                        int screenCenter = Math.round(heightPixel >> 1);//屏幕中心

                        int i = viewCenter - screenCenter;//当前焦点view到中心的距离
                        if (i < 0) {
                            pageScroll(i);
                        } else {
                            pageScroll(i);
                        }
                        int[] mllInts = new int[2];
                        view.getLocationInWindow(mllInts);
                    }
                });
    }

    /**
     * 手动滚动页面
     */
    private void pageScroll(int offset) {
        LogUtil.v("pageScroll");

        mBinding.nsv.smoothScrollBy(0,offset);
    }
}
