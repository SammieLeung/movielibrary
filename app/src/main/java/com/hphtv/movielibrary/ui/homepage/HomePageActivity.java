package com.hphtv.movielibrary.ui.homepage;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.LeftMenuListAdapter;
import com.hphtv.movielibrary.adapter.QuickFragmentPageAdapter;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.databinding.ActivityHomepageBinding;
import com.hphtv.movielibrary.databinding.MovieLibraryMovieFiltersLayoutBinding;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.moviesearch.PinyinSearchActivity;
import com.hphtv.movielibrary.ui.filterpage.FilterBoxDialogFragment;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.ui.shortcutmanager.ShortcutManagerActivity;
import com.hphtv.movielibrary.ui.view.CategoryView;
import com.station.kit.util.DensityUtil;
import com.station.kit.util.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomePageActivity extends AppBaseActivity<HomepageViewModel, ActivityHomepageBinding> {
    public static final String TAG = HomePageActivity.class.getSimpleName();

    public String mTagAll;
    private String[] mTitleArr;
    private boolean needRefresh = true;
    //筛选器条目顺序

    public static final int HOME_PAGE_FRAGMENT = 0;
    public static final int UNRECOGNIZED = 1;
    public static final int HISTORY_FRAGMENT = 2;
    public static final int FAVORITE_FRAGMENT = 3;
    public static final int FOLDER_MANAGER_FRAGMENT = 4;

    // 筛选条件
    private FileManagerFragment mFileManagerFragment;
    private HomePageFragment mHomePageFragment;
    private UnrecognizedFileFragement mUnrecognizedFileFragement;
    private FavoriteFragment mFavoriteFragment;
    private HistoryFragment mHistoryFragment;


    private List<Fragment> mFramentList;

    private QuickFragmentPageAdapter<Fragment> mPageAdapter;
    private LeftMenuListAdapter mLeftMenuAdapter;

    private PopupWindow mPopupWindow;
    private MovieScanService mScanService;
    private DeviceMonitorService mDeviceMonitorService;

    private Boolean isExit = false;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    MovieLibraryMovieFiltersLayoutBinding mFilterBinding;

    //当前选择的fragment的索引

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.v(TAG, "onResume()");
        registerReceiver();//注册广播
        bindService();//绑定服务
        Log.d(TAG, "onResume: "+mViewModel.toString());
    }


    @Override
    protected void onPause() {
        LogUtil.v(TAG, "onPause()");
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
        mHandler.removeCallbacksAndMessages(null);
    }


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.v(TAG, "processLogic==>OnCreate");
        mTagAll = getResources().getString(R.string.tx_all);
        mTitleArr = new String[]{getResources().getString(R.string.lb_title), getResources().getString(R.string.lb_sort_directory), getResources().getString(R.string.lb_setting)};
        requestPermission();
    }

    @Override
    protected void onActivityResultCallback(ActivityResult result) {
        switch (result.getResultCode()) {
            case RESULT_OK:
                needRefresh = true;
                break;
            default:
                needRefresh = false;
                break;
        }
        if (needRefresh)
            postDelayMovieRefresh(0);
    }


    /**
     * 动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                //第一次请求权限的时候返回false,第二次shouldShowRequestPermissionRationale返回true
                //如果用户选择了“不再提醒”永远返回false。
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //请求权限
                    LogUtil.v(TAG, "showRequest true");
                } else {
                    LogUtil.v(TAG, "showRequest false");
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {
                permissionGrant();
            }
        }
    }

    /**
     * 权限获取后调用
     */
    private void permissionGrant() {
        init();
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(this, DeviceMonitorService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        intentFilter.addAction(Constants.BroadCastMsg.DEVICE_UP);
        intentFilter.addAction(Constants.BroadCastMsg.DEVICE_DOWN);
        intentFilter.addAction(Constants.BroadCastMsg.RESCAN_ALL);
        intentFilter.addAction(Constants.BroadCastMsg.MOVIE_SCRAP_START);
        intentFilter.addAction(Constants.BroadCastMsg.MOVIE_SCRAP_STOP);
        intentFilter.addAction(Constants.BroadCastMsg.MATCHED_MOVIE);
        intentFilter.addAction(Constants.BroadCastMsg.START_LOADING);
        intentFilter.addAction(Constants.BroadCastMsg.STOP_LOADING);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }


    /**
     * 初始化
     */
    private void init() {
        LogUtil.v(TAG, "initView");
        prepareFragmentsAndViewpagers();
        prepareLeftMenu();
        initButtons();
        initMovieFiltersView();
    }

    /**
     * 初始化其他按钮
     */
    private void initButtons() {
        //弹出筛选窗口
//        mBinding.btnFilter.setOnClickListener(v -> mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0));
        mBinding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilterBoxDialogFragment filterBoxDialogFragment=FilterBoxDialogFragment.newInstance();
                filterBoxDialogFragment.show(getSupportFragmentManager(),"");
            }
        });
        //本地搜索按钮
        mBinding.btnPinyinSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, PinyinSearchActivity.class);
            startActivity(intent);
        });
        mBinding.btnShortcutmanager.setOnClickListener(v->{
            Intent intent = new Intent(this, ShortcutManagerActivity.class);
            startActivityForResult(intent);
        });
    }

    /**
     * 初始化菜单
     */
    private void prepareLeftMenu() {
        //注册开始菜单事件
        mBinding.openMenuBtn.setOnClickListener(v -> {
            if (mBinding.drawlayout.isDrawerOpen(GravityCompat.START)) {
                mBinding.drawlayout.closeDrawer(GravityCompat.START);
            } else {
                mBinding.drawlayout.openDrawer(GravityCompat.START);
            }
        });
        mBinding.openMenuBtn.setOnFocusChangeListener((v, hasFocus) -> {
            if (!mBinding.drawlayout.isDrawerOpen(GravityCompat.START)) {
                if (hasFocus) {
                    mBinding.drawlayout.openDrawer(GravityCompat.START);
                    mBinding.leftmenuListview.requestFocus();
                }
            }
        });
        //设置左侧菜单运动时，按钮的运动
        mBinding.drawlayout.setScrimColor(Color.TRANSPARENT);
        mBinding.drawlayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float width = drawerView.getMeasuredWidth() * slideOffset;
                float rotation = 180 * slideOffset + 180;
                try {
                    mBinding.viewContent.setTranslationX(width);
                    mBinding.openMenuBtn.setRotation(rotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        //填充左侧菜单
        mLeftMenuAdapter = new LeftMenuListAdapter(HomePageActivity.this, prepareMenuDataSet());
        mBinding.leftmenuListview.setAdapter(mLeftMenuAdapter);
        //遥控焦点事件
        mBinding.leftmenuListview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < mFramentList.size()) {
                    mBinding.viewpager.setCurrentItem(position);
                    mBinding.lbTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[position].toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //鼠标/触屏事件
        mBinding.leftmenuListview.setOnItemClickListener((parent, view, position, id) -> {
            if (position < mFramentList.size()) {
                mBinding.viewpager.setCurrentItem(position);
                mBinding.lbTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[position].toString());
                mBinding.drawlayout.closeDrawer(GravityCompat.START);
            } else {
                finish();
            }
        });

        mBinding.leftmenuListview.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                mBinding.drawlayout.closeDrawer(GravityCompat.START);
            }
            LogUtil.v(TAG, "[onItemClick] hasFocus=" + hasFocus);
        });

    }

    /**
     * 初始化Fragment 和 Viewpager
     */
    private void prepareFragmentsAndViewpagers() {
        if (mFileManagerFragment == null) {
            mFileManagerFragment = FileManagerFragment.newInstance(FOLDER_MANAGER_FRAGMENT);
        }
        if (mHomePageFragment == null) {
            mHomePageFragment = HomePageFragment.newInstance(HOME_PAGE_FRAGMENT);
        }
        if (mFavoriteFragment == null) {
            mFavoriteFragment = FavoriteFragment.newInstance(FAVORITE_FRAGMENT);
        }
        if (mHistoryFragment == null) {
            mHistoryFragment = HistoryFragment.newInstance(HISTORY_FRAGMENT);
        }
        if (mUnrecognizedFileFragement == null) {
            mUnrecognizedFileFragement = UnrecognizedFileFragement.newInstance(UNRECOGNIZED);
        }
        mFramentList = new ArrayList<>();
        mFramentList.add(mHomePageFragment);
        mFramentList.add(mUnrecognizedFileFragement);
        mFramentList.add(mHistoryFragment);
        mFramentList.add(mFavoriteFragment);
//        mFramentList.add(mFileManagerFragment);
        //设置ViewPager
        mPageAdapter = new QuickFragmentPageAdapter(getSupportFragmentManager());
        mPageAdapter.addAllFragments(mFramentList);
        mPageAdapter.addAllTitles(Arrays.asList(mTitleArr));
        mBinding.viewpager.setAdapter(mPageAdapter);
        mBinding.viewpager.setOffscreenPageLimit(3);
        mBinding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBinding.leftmenuListview.setSelection(position);
                if (position != HOME_PAGE_FRAGMENT) {
                    mBinding.btnFilter.setVisibility(View.GONE);
                } else {
                    mBinding.btnFilter.setVisibility(View.VISIBLE);
                }
                LogUtil.v("onPageSelected " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mViewModel.getCurrentFragmentPos().observe(this, page -> setCurrentFragment(page));
    }

    /**
     * 初始化左侧菜单数据
     *
     * @return
     */
    private List<HashMap<String, Object>> prepareMenuDataSet() {
        ArrayList<HashMap<String, Object>> menuList = new ArrayList<>();
        String[] itemGroup1 = getResources().getStringArray(R.array.menu_item_group_1);
        TypedArray ta_1 = getResources().obtainTypedArray(R.array.menu_item_icon_group_1);
        for (int i = 0; i < itemGroup1.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.TEXT, itemGroup1[i]);
            map.put(Constants.ICON, ta_1.getResourceId(i, 0));
            menuList.add(map);
        }
        return menuList;
    }

    /**
     * 初始化影片过滤部件
     */
    private void initMovieFiltersView() {
        if (mPopupWindow == null) {
            mFilterBinding = MovieLibraryMovieFiltersLayoutBinding.inflate(LayoutInflater.from(this));
            int heightpx = DensityUtil.dip2px(this, 482);
            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setHeight(heightpx);
            mPopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            mPopupWindow.setContentView(mFilterBinding.getRoot());

            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setFocusable(true);
            mFilterBinding.categoryview.setOnClickCategoryListener(mOnClickCategoryListener);
        }
    }

    /**
     * 更新UI分类
     */
    private void updateCateGoryView() {
        mFilterBinding.categoryview.setDevices(mViewModel.getConditionDevices()).setYears(mViewModel.getConditionYears()).setGenres(mViewModel.getConditionGenres()).create();
//        updateDeviceText();//TODO
        notifyAllFragmentsUpdate();
    }

    /**
     * 一定时间后更新筛选分类和电影
     *
     * @param delay 毫秒
     */
    private void postDelayMovieRefresh(long delay) {
        startLoading();
        mHandler.removeCallbacks(mMovieRefreshTask);
        mHandler.postDelayed(mMovieRefreshTask, delay);
    }

    /**
     * 1000毫秒后更新筛选分类和电影
     */
    private void postDelayMovieRefresh() {
        this.postDelayMovieRefresh(800);
    }

    /**
     * 切换Fragment
     *
     * @param pos
     */
    private void setCurrentFragment(int pos) {
        mBinding.viewpager.setCurrentItem(pos, true);
        mBinding.lbTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[pos].toString());
    }


    private void exitBy2Click() {
        Timer tExit = null;

        if (isExit == false) {

            isExit = true; // 准备退出
            Toast.makeText(HomePageActivity.this, "再按一次返回键退出程序",
                    Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            finish();
            // System.exit(0);直接 System.exit 导致destory函数未被调用
        }
    }


    /**
     * 升级设备文字
     * TODO
     */
//    private void updateDeviceText() {
//
//        Device device = mFilterBinding.categoryview.getDevice();
//        String year = mFilterBinding.categoryview.getYear();
//        String genre = mFilterBinding.categoryview.getGenre();
//
//        StringBuffer buffer = new StringBuffer();
//        if (device != null)
//            buffer.append(FormatterTools.getDeviceName(this, device));
//        if (!TextUtils.isEmpty(year))
//            buffer.append("|" + year);
//        if (!TextUtils.isEmpty(genre))
//            buffer.append("|" + genre);
//        if (buffer.length() == 0)
//            buffer.append(mTagAll);
//        if (buffer.toString().startsWith("|"))
//            buffer.replace(0, 1, "");
//
////        mBinding.tvSortDevice.setText(buffer.toString());
//    }

    public void notifyAllFragmentsUpdate() {
        notifyHomePage();
        mUnrecognizedFileFragement.notifyUpdate();
        mHistoryFragment.notifyUpdate();
        mFavoriteFragment.notifyUpdate();
    }

    public void notifyHomePage(){
        int sorttype = mFilterBinding.categoryview.getSortTypePos();
        Device device = mFilterBinding.categoryview.getDevice();
        String genre = mFilterBinding.categoryview.getGenre();
        String year = mFilterBinding.categoryview.getYear();
        boolean isDesc = mFilterBinding.categoryview.isDesc();
        LogUtil.v("2 sotType " + sorttype + " isDesc " + isDesc);
        mHomePageFragment.notifyUpdate(device, year, genre, sorttype, isDesc);
    }

    public MovieScanService getMovieSearchService() {
        return mScanService;
    }


    public int getCurrentFragment() {
        return mBinding.viewpager.getCurrentItem();
    }

    public boolean isShowEncrypted() {
        return getApp().isShowEncrypted();
    }

    public HomepageViewModel getViewModel() {
        return mViewModel;
    }


    /**
     * 退出
     */
    @SuppressLint("WrongConstant")
    @Override
    public void onBackPressed() {
        if (mBinding.drawlayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawlayout.closeDrawer(GravityCompat.START);
        } else if (getCurrentFragment() != HOME_PAGE_FRAGMENT) {
            setCurrentFragment(HOME_PAGE_FRAGMENT);
        } else {
            exitBy2Click();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogUtil.v(TAG, "onRequestPermissionsResult()");
        if (requestCode == 1) {
            if (permissions.length > 0 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGrant();
            } else {//没有获得到权限
                finish();
                LogUtil.v(TAG, "granted failed");
            }
        }
    }


    @SuppressLint("WrongConstant")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                View view = getCurrentFocus();
                LogUtil.v(TAG, "dispatchKeyEvent");
                if (view instanceof ListView && view.getId() == R.id.leftmenu_listview) {
                    mBinding.drawlayout.closeDrawer(GravityCompat.START);
                    mBinding.viewpager.requestFocus();
                    if (getCurrentFragment() == HOME_PAGE_FRAGMENT) {
//                        mHomePageFragment.movieRequestFocus();
                    }
                    return true;
                }
            }
        }
        LogUtil.v(TAG, getCurrentFocus() != null ? getCurrentFocus().toString() : "null");
        return super.dispatchKeyEvent(event);
    }

    /**
     * 更新筛选任务
     */
    Runnable mMovieRefreshTask = () -> {
        LogUtil.v("movieTaskRefresh");
        mViewModel.prepareConditions(args -> updateCateGoryView());
    };

    CategoryView.OnClickCategoryListener mOnClickCategoryListener = new CategoryView.OnClickCategoryListener() {

        @Override
        public void onConditionChange(Device device, String year, String genre, int sortType, boolean isDesc) {
            if (mHomePageFragment != null) {
                LogUtil.v(" sotType " + sortType + " isDesc " + isDesc);

                startLoading();
//                updateDeviceText();//TODO
                notifyHomePage();
            }

//            mViewModel.prepareMovies(device, year, genre,sortType,isDesc, args -> {
//
//                List<MovieDataView> movieDataViews = (List<MovieDataView>) args[0];
//                mHomePageFragment.updateMovie(movieDataViews);
//                stopLoading();
//
//            });
        }

    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (name.getClassName().equalsIgnoreCase(MovieScanService.class.getCanonicalName())) {
                mScanService = ((MovieScanService.ScanBinder) service).getService();
            } else if (name.getClassName().equalsIgnoreCase(DeviceMonitorService.class.getCanonicalName())) {
                mDeviceMonitorService = ((DeviceMonitorService.MonitorBinder) service).getService();
                if (needRefresh) {
                    needRefresh = false;
                    postDelayMovieRefresh(0);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.v(HomePageActivity.class.getSimpleName(), "mBroadcastReceiver action:" + action);
            switch (action) {
                case Constants.BroadCastMsg.DEVICE_DOWN:
                    mFilterBinding.categoryview.resetDevicePos();
                    postDelayMovieRefresh();
                    break;
                case Constants.BroadCastMsg.DEVICE_UP:
                case Constants.BroadCastMsg.MOVIE_SCRAP_START:
                    startLoading();
                    break;
                case Constants.BroadCastMsg.START_LOADING:
                    int cur_start = intent.getIntExtra(Constants.Extras.CURRENT_FRAGMENT, 0);
                    if (mBinding.viewpager.getCurrentItem() == cur_start) {
                        LogUtil.v("response " + cur_start + " startLoading");
                        startLoading();
                    }
                    break;
                case Constants.BroadCastMsg.STOP_LOADING:
                    int cur_stop = intent.getIntExtra(Constants.Extras.CURRENT_FRAGMENT, 0);
                    if (mBinding.viewpager.getCurrentItem() == cur_stop) {
                        LogUtil.v("response " + cur_stop + " stopLoading");
                        stopLoading();
                    }
                    break;
                case Constants.BroadCastMsg.MATCHED_MOVIE:
                    String mid=intent.getStringExtra(Constants.Extras.MOVIE_ID);
                    if(!TextUtils.isEmpty(mid)){
                        mHomePageFragment.addMovie(mid);
                    }
                    break;
                case Constants.BroadCastMsg.MOVIE_SCRAP_STOP:
                    postDelayMovieRefresh(0);

                    break;
                case Constants.ACTION_FAVORITE_MOVIE_CHANGE:
                    mFavoriteFragment.notifyUpdate();
                    break;
            }
        }
    };

}
