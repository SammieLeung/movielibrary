package com.hphtv.movielibrary.ui.homepage;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.TabitemHomepageMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.ui.PermissionActivity;
import com.hphtv.movielibrary.ui.moviesearch.PinyinSearchActivity;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel;
import com.hphtv.movielibrary.ui.settings.SettingsActivity;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity;
import com.hphtv.movielibrary.ui.view.NoScrollAutofitHeightViewPager;
import com.station.kit.util.AppUtils;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;
import com.station.kit.util.PackageTools;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class HomePageActivity extends PermissionActivity<HomePageViewModel, ActivityNewHomepageBinding> implements IAutofitHeight, OnMovieChangeListener {
    private HomePageTabAdapter mNewHomePageTabAdapter;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void firstPermissionGranted() {
        Log.i(TAG, "firstPermissionGranted: ");
        bindDatas();
        initView();
        autoScrollListener(mBinding.nsv);
        new Thread(() -> {
            if(!PackageTools.isServiceRunning(this, DeviceMonitorService.class.getName())){
                Intent service = new Intent(this, DeviceMonitorService.class);
                this.startService(service);
            }else {
                autoSearch();
            }

        }).start();
    }

    @Override
    public void permissionGranted() {
        Log.i(TAG, "permissionGranted: ");
        bindDatas();
        initView();
        autoScrollListener(mBinding.nsv);
        //自动搜索文件夹
        new Thread(() -> {
            if(!PackageTools.isServiceRunning(this, DeviceMonitorService.class.getName())){
                Intent service = new Intent(this, DeviceMonitorService.class);
                this.startService(service);
            }else {
                autoSearch();
            }

        }).start();
    }

    /**
     * 后台搜索结束后调用.
     */
    @Override
    protected void movieScarpFinish() {
        super.movieScarpFinish();
        updateFragments();
    }

    /**
     * 绑定数据
     */
    private void bindDatas() {
        mBinding.setChildmode(mViewModel.mChildMode);
    }

    /**
     * 初始化UI
     */
    private void initView() {
        mBinding.btnPinyinSearch.setOnClickListener(this::startPinyinSearchPage);
        mBinding.btnShortcutmanager.setOnClickListener(this::startShortcutManager);
        mBinding.btnSettings.setOnClickListener(this::startSettings);
        mBinding.btnChildmode.setOnClickListener(this::toggleChildmode);
        mBinding.nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> startBottomMaskAnimate());
        initTab();
    }

    /**
     * 初始化TAB
     */
    private void initTab() {
        mNewHomePageTabAdapter = new HomePageTabAdapter(this, getSupportFragmentManager());
        mBinding.viewpager.setAdapter(mNewHomePageTabAdapter);
        mBinding.viewpager.setOffscreenPageLimit(2);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewpager);
        mBinding.tabLayout.getTabAt(0).setCustomView(buildTabView(getString(R.string.tab_homepage)));
        mBinding.tabLayout.getTabAt(1).setCustomView(buildTabView(getString(R.string.video_type_undefine)));
//        mBinding.tabLayout.getTabAt(2).setCustomView(buildTabView(getString(R.string.tab_tv)));

        for (int i = 0; i < mBinding.tabLayout.getTabCount(); i++) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.tabLayout.getTabAt(i).view.getLayoutParams();//
            params.rightMargin = DensityUtil.dip2px(this, 32);
            mBinding.tabLayout.getTabAt(i).view.setLayoutParams(params);
        }
        mBinding.tabLayout.getViewTreeObserver()
                .addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
                    //实现Tablayout的焦点从其他非子布局到其子布局时，焦点始终在已选的TabView上。
                    if (oldFocus != null && !(oldFocus instanceof TabLayout.TabView) && newFocus != null && newFocus instanceof TabLayout.TabView) {
                        int pos = mBinding.tabLayout.getSelectedTabPosition();
                        mBinding.tabLayout.getTabAt(pos).view.requestFocus();
                    }
                    //实现焦点从一个TabView移动到其他TabView时，选中其他TabView。
                    if (oldFocus != null && oldFocus instanceof TabLayout.TabView && newFocus != null && newFocus instanceof TabLayout.TabView) {
                        TabLayout.TabView view = (TabLayout.TabView) newFocus;
                        view.getTab().select();
                    }
                });
        mBinding.tabLayout.getTabAt(0).view.requestFocus();
    }

    /**
     * 自动搜索本地设备
     */
    private void autoSearch() {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION.RESCAN_ALL_FILES);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * 蒙版隱藏動畫
     */
    private void startBottomMaskAnimate() {
        mHandler.removeCallbacks(mBottomMaskFadeInTask);
        if (mBinding.bottomMask.getAlpha() > 0) {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 0).setDuration(200);
            objectAnimator.start();
        }
        mBottomMaskFadeInTask = () -> {
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mBinding.bottomMask, "alpha", mBinding.bottomMask.getAlpha(), 1).setDuration(500);
            objectAnimator.start();
        };
        mHandler.postDelayed(mBottomMaskFadeInTask, 800);
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        //刷新显示儿童模式状态
//        mViewModel.getChildMode().set(!Config.isTempCloseChildMode() && Config.isChildMode());
        mViewModel.getChildMode().set(Config.isChildMode());

        //刷新各个子Fragment状态
        if (result.getResultCode() == RESULT_OK)
            for (Fragment fragment : mNewHomePageTabAdapter.mList) {
                if (fragment instanceof IActivityResult) {
                    IActivityResult activityResult = (IActivityResult) fragment;
                    activityResult.forceRefresh();
                }
            }
    }

    /**
     * 强制刷新所有Fragment
     */
    private void updateFragments() {
        for (Fragment fragment : mNewHomePageTabAdapter.mList) {
            if (fragment instanceof IActivityResult) {
                IActivityResult activityResult = (IActivityResult) fragment;
                activityResult.forceRefresh();
            }
        }
    }

    public void refreshFragment(int pos) {
        if (pos < mNewHomePageTabAdapter.mList.size()) {
            Fragment fragment = mNewHomePageTabAdapter.getItem(pos);
            if (fragment instanceof IActivityResult) {
                IActivityResult activityResult = (IActivityResult) fragment;
                activityResult.forceRefresh();
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
    public void startShortcutManager(View v) {
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
     *
     * @param v
     */
    private void toggleChildmode(View v) {
        if (mViewModel.getChildMode().get()) {
            mViewModel.getChildMode().notifyChange();
            showPasswordDialog();
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

    @Override
    public void OnRematchPoster(MovieDataView movieDataView, int pos) {
        updateFragments();
    }

    @Override
    public void OnMovieChange(MovieDataView movieDataView, int pos) {
    }

    @Override
    public void OnMovieRemove(String movie_id, int pos) {
    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {
    }
}
