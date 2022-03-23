package com.hphtv.movielibrary.ui.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.TabitemHomepageMenuBinding;
import com.hphtv.movielibrary.ui.moviesearch.PinyinSearchActivity;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel;
import com.hphtv.movielibrary.ui.settings.SettingsActivity;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class NewHomePageActivity extends PermissionActivity<NewHomePageViewModel, ActivityNewHomepageBinding> implements IAutofitHeight {
    private NewHomePageTabAdapter mNewHomePageTabAdapter;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void permissionGranted() {
        bindDatas();
        initView();
        autoScrollListener(mBinding.nsv);
        if(Config.isAutoSearch().get()){
            autoSearch();
        }
    }

    /**
     * 绑定数据
     */
    private void bindDatas(){
        mBinding.setChildmode(mViewModel.mChildMode);
        mBinding.setShowChildMode(mViewModel.mShowChildMode);
    }

    /**
     * 初始化UI
     */
    private void initView(){
        mBinding.btnPinyinSearch.setOnClickListener(this::startPinyinSearchPage);
        mBinding.btnShortcutmanager.setOnClickListener(this::startShortcutManager);
        mBinding.btnSettings.setOnClickListener(this::startSettings);
        mBinding.btnChildmode.setOnClickListener(this::toggleChildmode);
        initTab();
    }

    /**
     * 初始化TAB
     */
    private void initTab() {
        mNewHomePageTabAdapter = new NewHomePageTabAdapter(this, getSupportFragmentManager());
        mBinding.viewpager.setAdapter(mNewHomePageTabAdapter);
        mBinding.viewpager.setOffscreenPageLimit(2);
        mBinding.tablayout.setupWithViewPager(mBinding.viewpager);
        mBinding.tablayout.getTabAt(0).setCustomView(buildTabView(getString(R.string.tab_homepage)));
//        mBinding.tablayout.getTabAt(1).setCustomView(buildTabView(getString(R.string.tab_movie)));
//        mBinding.tablayout.getTabAt(2).setCustomView(buildTabView(getString(R.string.tab_tv)));

        for (int i = 0; i < mBinding.tablayout.getTabCount(); i++) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.tablayout.getTabAt(i).view.getLayoutParams();//
            params.rightMargin = DensityUtil.dip2px(this, 32);
            mBinding.tablayout.getTabAt(i).view.setLayoutParams(params);
        }
        mBinding.tablayout.getViewTreeObserver()
                .addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                    //实现Tablayout的焦点从其他非子布局到其子布局时，焦点始终在已选的TabView上。
                    if (oldFocus != null && !(oldFocus instanceof TabLayout.TabView) && newFocus != null && newFocus instanceof TabLayout.TabView) {
                        int pos = mBinding.tablayout.getSelectedTabPosition();
                        mBinding.tablayout.getTabAt(pos).view.requestFocus();
                    }
                    //实现焦点从一个TabView移动到其他TabView时，选中其他TabView。
                    if (oldFocus != null && oldFocus instanceof TabLayout.TabView && newFocus != null && newFocus instanceof TabLayout.TabView) {
                        TabLayout.TabView view = (TabLayout.TabView) newFocus;
                        view.getTab().select();
                    }
                });
    }

    /**
     * 自动搜索本地设备
     */
    private void autoSearch(){

    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        //刷新显示儿童模式状态
        mViewModel.getShowChildMode().set(Config.isChildMode());
        mViewModel.getChildMode().set(!Config.isTempCloseChildMode()&&Config.isChildMode());
        //刷新各个子Fragment状态
        for (Fragment fragment : mNewHomePageTabAdapter.mList) {
            if (fragment instanceof IActivityResult) {
                IActivityResult activityResult = (IActivityResult) fragment;
                activityResult.onActivityResult(result);
            }
        }
    }

    /**
     * 获取NoScrollAutofitHeightViewPager
     *
     * @return
     */
    @Override
    public NoScrollAutofitHeightViewPager getAutofitHeightViewPager() {
        return mBinding.viewpager;
    }

    /**
     * 打开搜索页
     *
     * @param view
     */
    private void startPinyinSearchPage(View view) {
        Intent intent = new Intent(this, PinyinSearchActivity.class);
        startActivityForResult(intent);
    }

    /**
     * 打开设备管理页
     *
     * @param v
     */
    private void startShortcutManager(View v) {
        Intent intent = new Intent(this, ShortcutManagerActivity.class);
        startActivityForResult(intent);
    }

    /**
     * 打开设置页面
     *
     * @param view
     */
    private void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent);
    }

    /**
     * 开关儿童，模式
     * @param v
     */
    private void toggleChildmode(View v) {
        if (mViewModel.getChildMode().get()) {
            mViewModel.getChildMode().notifyChange();
            showPasswordDialog();
        } else {
            mViewModel.toggleChildMode();
            mNewHomePageTabAdapter.refreshFragments();
        }
    }

    /**
     * 显示密码输入框
     */
    private void showPasswordDialog() {
        PasswordDialogFragmentViewModel viewModel = new ViewModelProvider(this).get(PasswordDialogFragmentViewModel.class);
        PasswordDialogFragment passwordDialogFragment = PasswordDialogFragment.newInstance();
        passwordDialogFragment.setDialogTitle(getString(R.string.childmode_temp_close));
        passwordDialogFragment.setOnConfirmListener(() -> {
            mViewModel.toggleChildMode();
            mNewHomePageTabAdapter.refreshFragments();
        });
        passwordDialogFragment.setViewModel(viewModel);
        passwordDialogFragment.show(getSupportFragmentManager(), "");
    }

    /**
     * 创建Tabview
     *
     * @param text
     * @return
     */
    private View buildTabView(String text) {
        TabitemHomepageMenuBinding homepageTabBinding = TabitemHomepageMenuBinding.inflate(getLayoutInflater());
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

        mBinding.nsv.smoothScrollBy(0, offset);
    }
}
