package com.hphtv.movielibrary.ui.homepage;

import static com.hphtv.movielibrary.data.Constants.REQUEST_CODE_FROM_BASE_FILTER;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityNewHomepageBinding;
import com.hphtv.movielibrary.databinding.TabitemHomepageMenuBinding;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.ui.IRemoteRefresh;
import com.hphtv.movielibrary.ui.PermissionActivity;
import com.hphtv.movielibrary.ui.filterpage.FilterBoxDialogFragment;
import com.hphtv.movielibrary.ui.filterpage.OnFilterChangerListener;
import com.hphtv.movielibrary.ui.homepage.fragment.filter.FilterFragment;
import com.hphtv.movielibrary.ui.homepage.fragment.unknown.UnknownFileFragment;
import com.hphtv.movielibrary.ui.moviesearch.pinyin.PinyinSearchActivity;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragment;
import com.hphtv.movielibrary.ui.settings.PasswordDialogFragmentViewModel;
import com.hphtv.movielibrary.ui.settings.SettingsActivity;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity;
import com.orhanobut.logger.Logger;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.PackageTools;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class HomePageActivity extends PermissionActivity<HomePageViewModel, ActivityNewHomepageBinding> implements OnMovieChangeListener {
    private HomePageTabAdapter mNewHomePageTabAdapter;
    private Handler mHandler = new Handler();
    private Runnable mBottomMaskFadeInTask;
    private boolean isMouseEvent = false;
    ObjectAnimator mObjectAnimator;

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        private int mLastPos = 0;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (position != HomePageTabAdapter.CHILD && mLastPos == HomePageTabAdapter.CHILD) {
                if (mObjectAnimator != null) {
                    mObjectAnimator.cancel();
                    mObjectAnimator = null;
                }
                mObjectAnimator = ObjectAnimator.ofFloat(mBinding.viewBg2, View.ALPHA, 1);
                mObjectAnimator.setDuration(300);
                mObjectAnimator.start();
                mBinding.btnPinyinSearch.setBackgroundResource(R.drawable.circle_btn_bg);
                mBinding.btnShortcutmanager.setBackgroundResource(R.drawable.circle_btn_bg);
                mBinding.btnChildmode.setBackgroundResource(R.drawable.circle_btn_bg);
                mBinding.btnSettings.setBackgroundResource(R.drawable.circle_btn_bg);

                TextViewCompat.setCompoundDrawableTintList(mBinding.btnPinyinSearch, getColorStateList(R.color.circle_btn_color_list));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnShortcutmanager, getColorStateList(R.color.circle_btn_color_list));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnChildmode, getColorStateList(R.color.circle_btn_color_list));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnSettings, getColorStateList(R.color.circle_btn_color_list));
                //为child tabview设置特殊的背景
                ViewGroup viewGroup = (ViewGroup) mBinding.tabLayout.getChildAt(0);
                View childTab = viewGroup.getChildAt(HomePageTabAdapter.CHILD);
                ViewCompat.setBackground(childTab, AppCompatResources.getDrawable(childTab.getContext(), R.drawable.new_common_tab_bg_2));

            } else if (position == HomePageTabAdapter.CHILD) {
                if (mObjectAnimator != null) {
                    mObjectAnimator.cancel();
                    mObjectAnimator = null;
                }
                mObjectAnimator = ObjectAnimator.ofFloat(mBinding.viewBg2, View.ALPHA, 0);
                mObjectAnimator.setDuration(300);
                mObjectAnimator.start();
                mBinding.btnPinyinSearch.setBackgroundResource(R.drawable.circle_btn_child_bg);
                mBinding.btnShortcutmanager.setBackgroundResource(R.drawable.circle_btn_child_bg);
                mBinding.btnChildmode.setBackgroundResource(R.drawable.circle_btn_child_bg);
                mBinding.btnSettings.setBackgroundResource(R.drawable.circle_btn_child_bg);
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnPinyinSearch, ColorStateList.valueOf(Color.WHITE));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnShortcutmanager, ColorStateList.valueOf(Color.WHITE));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnChildmode, ColorStateList.valueOf(Color.WHITE));
                TextViewCompat.setCompoundDrawableTintList(mBinding.btnSettings, ColorStateList.valueOf(Color.WHITE));
                //为child tabview设置特殊的背景
                ViewGroup viewGroup = (ViewGroup) mBinding.tabLayout.getChildAt(0);
                View childTab = viewGroup.getChildAt(HomePageTabAdapter.CHILD);
                ViewCompat.setBackground(childTab, AppCompatResources.getDrawable(childTab.getContext(), R.drawable.new_common_tab_child_bg_2));


            }

            mLastPos = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MovieApplication.getInstance().isDeviceBound(true);
    }

    @Override
    public void firstPermissionGranted() {
        Log.i(TAG, "firstPermissionGranted: ");
        bindDatas();
        initView();
        autoScrollListener(mBinding.nsv);
        new Thread(() -> {
            if (!PackageTools.isServiceRunning(this, DeviceMonitorService.class.getName())) {
                Intent service = new Intent(this, DeviceMonitorService.class);
                this.startService(service);
            } else {
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
            if (!PackageTools.isServiceRunning(this, DeviceMonitorService.class.getName())) {
                Intent service = new Intent(this, DeviceMonitorService.class);
                this.startService(service);
            } else {
                autoSearch();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindBroadcastReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.viewpager.removeOnPageChangeListener(mOnPageChangeListener);
    }

    /**
     * 后台搜索结束后调用.
     */
    @Override
    protected void movieScrapeFinish() {
        super.movieScrapeFinish();
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
        mNewHomePageTabAdapter = new HomePageTabAdapter(mBinding.viewpager, getSupportFragmentManager());
        mBinding.viewpager.setAdapter(mNewHomePageTabAdapter);
        mBinding.viewpager.setOffscreenPageLimit(6);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewpager);
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.HOME).setCustomView(buildTabView(getString(R.string.tab_homepage)));
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.MOVIE).setCustomView(buildTabView(getString(R.string.video_type_movie)));
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.TV).setCustomView(buildTabView(getString(R.string.video_type_tv)));
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.UNKNOWN).setCustomView(buildTabView(getString(R.string.video_type_undefine)));
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.CHILD).setCustomView(buildTabView(getString(R.string.video_type_cartoon)));
        mBinding.tabLayout.getTabAt(HomePageTabAdapter.VARIETY_SHOW).setCustomView(buildTabView(getString(R.string.video_type_variety_show)));


        mBinding.viewpager.addOnPageChangeListener(mOnPageChangeListener);

        for (int i = 0; i < mBinding.tabLayout.getTabCount(); i++) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mBinding.tabLayout.getTabAt(i).view.getLayoutParams();//
            params.rightMargin = DensityUtil.dip2px(this, 32);
            mBinding.tabLayout.getTabAt(i).view.setLayoutParams(params);

            //设定tabview的左右焦点 （主页、电影、电视剧、...）
            if (mBinding.tabLayout.getTabCount() > 1) {
                View view = mBinding.tabLayout.getTabAt(i).view;
                if (i == 0) {
                    view.setId(View.generateViewId());
                    int nextRightId = View.generateViewId();
                    mBinding.tabLayout.getTabAt(i + 1).view.setId(nextRightId);
                    view.setNextFocusRightId(nextRightId);
                } else if (i == mBinding.tabLayout.getTabCount() - 1) {
                    view.setNextFocusLeftId(mBinding.tabLayout.getTabAt(i - 1).view.getId());
                } else {
                    view.setId(View.generateViewId());
                    int nextRightId = View.generateViewId();
                    mBinding.tabLayout.getTabAt(i + 1).view.setId(nextRightId);
                    view.setNextFocusRightId(nextRightId);
                    view.setNextFocusLeftId(mBinding.tabLayout.getTabAt(i - 1).view.getId());
                }
            }
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
        mBinding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos != HomePageTabAdapter.UNKNOWN && pos != HomePageTabAdapter.HOME) {
                    ((TextView) tab.getCustomView().findViewById(R.id.tabTextView)).setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.icon_filter_27dp), null);
                    //此时注册点击监听器,点击弹出筛选窗口
                    tab.view.setOnClickListener(v -> {
                        FilterBoxDialogFragment dialogFragment = FilterBoxDialogFragment.newInstance(pos, new OnFilterChangerListener() {
                            @Override
                            public void onFilterChange(@Nullable Shortcut shortcut, @Nullable String genre, @Nullable String startYear, @Nullable String endYear) {
                                ((FilterFragment) mNewHomePageTabAdapter.getItem(pos)).setFilter(shortcut, genre, startYear, endYear);
                            }

                            @Override
                            public void onOrderChange(int order, boolean isDesc) {
                                ((FilterFragment) mNewHomePageTabAdapter.getItem(pos)).setOrder(order, isDesc);
                            }
                        });
                        dialogFragment.show(getSupportFragmentManager(), "FilterBoxDialogFragment");
                    });
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView().findViewById(R.id.tabTextView)).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                tab.view.setOnClickListener(null);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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

    private void bindBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Constants.ACTION_NETWORK_AVAILABLE.equals(intent.getAction())) {
                    }
                }
            };
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_NETWORK_AVAILABLE);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unBindBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        //刷新显示儿童模式状态
//        mViewModel.getChildMode().set(!Config.isTempCloseChildMode() && Config.isChildMode());
        mViewModel.getChildMode().set(Config.isChildMode());
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

    public void forceRefreshFragment(int pos) {
        if (pos < mNewHomePageTabAdapter.mList.size()) {
            Fragment fragment = mNewHomePageTabAdapter.getItem(pos);
            if (fragment instanceof IActivityResult) {
                IActivityResult activityResult = (IActivityResult) fragment;
                activityResult.forceRefresh();
            }
        }
    }


    public void remoteUpdateMovieForFragment(int pos, long o_id, long n_id) {
        if (pos < mNewHomePageTabAdapter.mList.size()) {
            Fragment fragment = mNewHomePageTabAdapter.getItem(pos);
            if (fragment instanceof IRemoteRefresh) {
                IRemoteRefresh activityResult = (IRemoteRefresh) fragment;
                activityResult.remoteUpdateMovieNotify(o_id, n_id);
            }
        }
    }

    public void remoteRemoveMovieForFragment(int pos, String movie_id, String type) {
        if (pos < mNewHomePageTabAdapter.mList.size()) {
            Fragment fragment = mNewHomePageTabAdapter.getItem(pos);
            if (fragment instanceof IRemoteRefresh) {
                IRemoteRefresh activityResult = (IRemoteRefresh) fragment;
                activityResult.remoteRemoveMovieNotify(movie_id, type);
            }
        }
    }

    public Fragment getFragmentByPosition(int pos) {
        return mNewHomePageTabAdapter.getItem(pos);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchGenericMotionEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        startBottomMaskAnimate();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        startBottomMaskAnimate();
        return super.dispatchTouchEvent(ev);
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
    public void OnMovieRemove(String movie_id, String type, int pos) {
        for (Fragment fragment : mNewHomePageTabAdapter.mList) {
            if (fragment instanceof UnknownFileFragment) {
                UnknownFileFragment unknownFileFragment = (UnknownFileFragment) fragment;
                unknownFileFragment.refreshCurrentPage(pos);
                break;
            }
        }
    }

    @Override
    public void OnMovieInsert(MovieDataView movieDataView, int pos) {
    }

    @Override
    public void remoteUpdateMovieNotify(long o_id, long n_id) {
        int pos = mBinding.tabLayout.getSelectedTabPosition();
        remoteUpdateMovieForFragment(pos, o_id, n_id);
    }


    @Override
    public void remoteRemoveMovieNotify(String movie_id, String type) {
        int pos = mBinding.tabLayout.getSelectedTabPosition();
        remoteRemoveMovieForFragment(pos, movie_id, type);
    }

    @Override
    public void onBackPressed() {
        if (mBinding.viewpager.getCurrentItem() == HomePageTabAdapter.UNKNOWN) {
            if (mNewHomePageTabAdapter.getItem(HomePageTabAdapter.UNKNOWN) instanceof UnknownFileFragment) {
                UnknownFileFragment unknownFileFragment = (UnknownFileFragment) mNewHomePageTabAdapter.getItem(HomePageTabAdapter.UNKNOWN);
                boolean res = unknownFileFragment.OnBackPress();
                if (!res) {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }
}
