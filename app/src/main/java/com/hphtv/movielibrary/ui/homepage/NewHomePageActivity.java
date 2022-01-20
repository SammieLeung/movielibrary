package com.hphtv.movielibrary.ui.homepage;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.LayoutHomepageTabBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.DensityUtil;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class NewHomePageActivity extends AppBaseActivity<NewHomePageViewModel, ActivityNewHomepageBinding> implements IAutofitHeight {
    NewHomePageTabAdapter mNewHomePageTabAdapter;
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNewHomePageTabAdapter=new NewHomePageTabAdapter(this,getSupportFragmentManager());
        mBinding.viewpager.setAdapter(mNewHomePageTabAdapter);
        mBinding.tablayout.setupWithViewPager(mBinding.viewpager);
        initTab();
    }

    private void initTab(){
//        mBinding.tablayout.getTabAt(0).setCustomView()
        mBinding.tablayout.getTabAt(0).setCustomView(buildTabView(getString(R.string.tab_homepage)));
        for(int i=0;i<mBinding.tablayout.getTabCount();i++){
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.tablayout.getTabAt(i).view.getLayoutParams();//
            params.rightMargin= DensityUtil.dip2px(this,32);
            mBinding.tablayout.getTabAt(i).view.setLayoutParams(params);
        }
    }

    /**
     * Viewpager+Scrollview自适应
     * @return
     */
    @Override
    public NoScrollAutofitHeightViewPager getViewPager() {
        return mBinding.viewpager;
    }

    private View buildTabView(String text) {
        LayoutHomepageTabBinding homepageTabBinding=LayoutHomepageTabBinding.inflate(getLayoutInflater());
        homepageTabBinding.setText(text);
        return homepageTabBinding.getRoot();
    }
}
