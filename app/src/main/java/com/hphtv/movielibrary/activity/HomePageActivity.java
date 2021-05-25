package com.hphtv.movielibrary.activity;

import android.Manifest;
import android.app.Fragment;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.LeftMenuListAdapter;
import com.hphtv.movielibrary.adapter.QuickFragmentPageAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.fragment.AboutsFragment;
import com.hphtv.movielibrary.fragment.FavoriteFragment;
import com.hphtv.movielibrary.fragment.FileManagerFragment;
import com.hphtv.movielibrary.fragment.HistoryFragment;
import com.hphtv.movielibrary.fragment.HomePageFragment;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.util.DensityUtil;
import com.hphtv.movielibrary.util.LanguageUtil;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.hphtv.movielibrary.util.retrofit.MtimeSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;
import com.hphtv.movielibrary.util.retrofit.MtimeAPIRequest;
import com.hphtv.movielibrary.view.CategoryView;
import com.hphtv.movielibrary.view.CategoryView.OnClickCategoryListener;
import com.hphtv.movielibrary.view.CustomLoadingCircleViewFragment;
import com.hphtv.movielibrary.view.VerticalViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomePageActivity extends AppBaseActivity {
    public static final String TAG = HomePageActivity.class.getSimpleName();

    public String mTagAll;
    private String[] mTitleArr;
    public static final String DESC = "↓";
    public static final String ASC = "↑";
    public static final String SPLIT = "|";
    //筛选器条目顺序
    public static final int SORT_YEAR = 3;
    public static final int SORT_GENRES = 4;
    public static final int SORT_ORDER = 5;
    public static final int SORT_DIR = 2;
    public static final int SORT_DEVICE = 1;
    public static final int SORT_INIT = 0;

    public static final int HOME_PAGE_FRAGMENT = 0;
    public static final int HISTORY_FRAGMENT = 1;
    public static final int FAVORITE_FRAGMENT = 2;
    public static final int FILE_MANAGER_FRAGMENT = 3;
    public static final int ABOUTS_FRAGMENT = 4;

    //筛选条件数据
    private List<String> mGenresData;// 电影类型数据
    private List<String> mPubYearsData;
    private List<Device> mDeviceList;//
    private List<String> mSortTypeData;
    private List<Directory> mDirectoryList;
    // 筛选条件
    private String mFilterDeviceName;
    private long mFilterDeviceId;
    private long mFilterDirId;
    private String mFilterYear;// 年份
    private String mFilterGenres;// 类型
    private String mSortType;// 排列顺序
    private boolean isSortByAsc = true;// 排列顺序升序
    private Context mContext;

    private LinearLayout mSortBox;
    private TextView mSortBoxDevice;
    private TextView mTitle;
    private ImageView mBtnMenu;
    private Button mBtnSearch;
    private PopupWindow mPopupWindow;
    private CategoryView mCategoryView;
    private ListView mLeftMenu;

    private FileManagerFragment mFileManagerFragment;
    private HomePageFragment mHomePageFragment;
    private AboutsFragment mAboutsFragment;
    private FavoriteFragment mFavoriteFragment;
    private HistoryFragment mHistoryFragment;

    private CustomLoadingCircleViewFragment mLoadingCircleViewDialogFragment;

    private DrawerLayout mDrawerLayout;
    private List<Fragment> mFramentList;

    private Device mCurrDevice;
    private Directory mCurrDir;
    private QuickFragmentPageAdapter<Fragment> mPageAdapter;
    private VerticalViewPager mVViewPager;
    private FrameLayout mContentPanel;
    private LeftMenuListAdapter mLeftMenuAdapter;
    private ExecutorService mDevcieRefreshService;

    private ImageView mBackgroundImage;

    private MovieScanService mScanService;


    private DirectoryDao mDirectoryDao;
    private MovieDao mMovieDao;
    private boolean isDevChange = false;

    private Boolean isExit = false;

    //当前选择的fragment的索引
    MovieSharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.v(TAG, "onCreate");
        if (LanguageUtil.isLanguageChanged(HomePageActivity.this, HomePageActivity.class))
            LanguageUtil.restartApp(HomePageActivity.this, HomePageActivity.class);
        setContentView(R.layout.activity_homepage);
        mTagAll = getResources().getString(R.string.tx_all);
        mTitleArr = new String[]{getResources().getString(R.string.lb_title), getResources().getString(R.string.lb_sort_directory), getResources().getString(R.string.lb_setting)};
        mContext = this;
        mDevcieRefreshService = Executors.newSingleThreadExecutor();
        mPreferences = MovieSharedPreferences.getInstance();
        mPreferences.setContext(mContext);
        requestPermission();
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.v(TAG, "onResume()");
//
        Intent intent = new Intent(this, MovieScanService.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstData.ACTION_FAVORITE_MOVIE_CHANGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
        LogUtil.v(TAG, "onPause()");
    }

    /**
     * 刷新设备列表成功后调用
     *
     * @param deviceList
     */
    @Override
    public void OnDeviceChange(final List<Device> deviceList) {
        Log.v(TAG, "onDeviceChange");
        mDeviceList = deviceList;
        runOnUiThread(() -> {
            refreshDeviceAndDirectoryPopUpWindow();
            getSortCondition();
        });
//        mDevcieRefreshService.execute(new Runnable() {
//            @Override
//            public void run() {
//                Log.v(TAG, "Thread Begin");
//                synchronized (mDeviceList) {
//                    mDeviceList = deviceList;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.v(TAG, "refreshDeviceAndDirectoryPopUpWindow ");
//                            refreshDeviceAndDirectoryPopUpWindow();
//                            getSortCondition();
//                        }
//                    });
//
//                }
//            }
//        });

    }

    /**
     * 服务连接成功后检查连接设备。
     *
     * @param service
     */
    @Override
    public void OnDeviceMonitorServiceConnect(DeviceMonitorService service) {
        Log.v(TAG, "OnDeviceMonitorServiceConnect");
        mDeviceMonitorService = service;
        try {
            checkConnectedDevices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
        } else if (getCurrentFragment() != HOME_PAGE_FRAGMENT) {
            setCurrentFragment(HOME_PAGE_FRAGMENT);
        } else if (mScanService != null && mScanService.isRunning()) {
            getApp().moveToBack(HomePageActivity.this);
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


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                View view = getCurrentFocus();
                LogUtil.v(TAG, "dispatchKeyEvent");
                if (view instanceof ListView) {
                    mDrawerLayout.closeDrawer(Gravity.START);
                    mVViewPager.requestFocus();
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

    private void permissionGrant() {
        initView();
        if (mDeviceList == null)
            mDeviceList = new ArrayList<>();
        if (mDirectoryList == null)
            mDirectoryList = new ArrayList<>();
        setFilterData();
        initDevicePopUpWindow();
    }

    public void initMovie() {
        mHomePageFragment.initMovie();
        mHistoryFragment.initMovie();
        mFavoriteFragment.getFavoritMovie();
    }

    public MovieScanService getMovieSearchService() {
        return mScanService;
    }

    public Device getCurrDevice() {
        return mCurrDevice;
    }

    public List<Device> getDeviceList() {
        return mDeviceList;
    }

    public List<Directory> getDirectoryList() {
        return mDirectoryList;
    }

    public int getCurrentFragment() {
        return mVViewPager.getCurrentItem();
    }

    public String getFilterDeviceName() {
        return mFilterDeviceName;
    }

    public long getFilterDeviceId() {
        return mFilterDeviceId;
    }

    public long getFilterDirId() {
        return mFilterDirId;
    }

    public String getFilterYear() {
        return mFilterYear;
    }

    public String getFilterGenres() {
        return mFilterGenres;
    }

    public String getSortType() {
        return mSortType;
    }

    public int getPosSortType() {
        return mSortTypeData.indexOf(mSortType);
    }

    public String getOrderBySqlStr() {
        int pos_order = mSortTypeData.indexOf(mSortType);
        StringBuffer orderByBuffer = new StringBuffer();
        String asc = isSortByAsc ? "asc" : "desc";
        switch (pos_order) {
            case 0:
                orderByBuffer.append("title_pinyin " + asc + ",title " + asc + ",otitle " + asc);
                break;
            case 1:
                orderByBuffer.append("rating " + asc);
                break;
            case 2:
                orderByBuffer.append("genres " + asc);
                break;
            case 3:
                orderByBuffer.append("year " + asc);
                break;
            default:
                orderByBuffer.append("title_pinyin " + asc + ",title " + asc + ",otitle " + asc);

                break;
        }
        return orderByBuffer.toString();
    }

    public boolean isSortByAsc() {
        return isSortByAsc;
    }

    public boolean isShowEncrypted() {
        return getApp().isShowEncrypted();
    }

    public void startLoading() {
        LogUtil.v(TAG, "startLoading");
        if (mLoadingCircleViewDialogFragment == null) {
            mLoadingCircleViewDialogFragment = new CustomLoadingCircleViewFragment();
            mLoadingCircleViewDialogFragment.show(getFragmentManager(), TAG);
        }

    }

    public void stopLoading() {
        LogUtil.v(TAG, "stopLoading");
        if (mLoadingCircleViewDialogFragment != null) {
            mLoadingCircleViewDialogFragment.dismiss();
            mLoadingCircleViewDialogFragment = null;
        }
    }

    /**
     * 获取连接的设备列表
     */
    public void checkConnectedDevices() {
        if (mDeviceMonitorService != null)
            mDeviceMonitorService.checkDevices();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        LogUtil.v(TAG, "initView");
        mBackgroundImage = findViewById(R.id.home_page_background);
        mLeftMenu = findViewById(R.id.leftmenu_listview);
        mSortBox = findViewById(R.id.tv_sort_device_parent);
        mSortBoxDevice = findViewById(R.id.tv_sort_device);
        mTitle = findViewById(R.id.lb_title);
        mBtnMenu = findViewById(R.id.open_menu_btn);
        mDrawerLayout = findViewById(R.id.drawlayout);
        mContentPanel = findViewById(R.id.contentFrameLayout);
        mSortBox.setOnClickListener(v -> HomePageActivity.this.PopUpDeviceWindow(v));
        mBtnMenu.setOnClickListener(v -> {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.closeDrawer(Gravity.START);
            } else {
                LogUtil.v(TAG, "mBtnMenu onClick");
                mDrawerLayout.openDrawer(Gravity.START);
            }

        });
        mBtnMenu.setOnFocusChangeListener((v, hasFocus) -> {
            if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
                if (hasFocus) {
                    mDrawerLayout.openDrawer(Gravity.START);
                    mLeftMenu.requestFocus();
                }
            }
        });
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        //设置内容页面滑动监听
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float width = drawerView.getMeasuredWidth() * slideOffset;
                float rotation = 180 * slideOffset + 180;
                try {
                    mContentPanel.setTranslationX(width);
                    mBtnMenu.setRotation(rotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Button more_btn = (Button) mContentPanel.findViewById(R.id.more_btn);
                if (more_btn != null)
                    more_btn.setTranslationX(0);

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });


        initFragment();
        mFramentList = new ArrayList<>();
        mFramentList.add(mHomePageFragment);
        mFramentList.add(mHistoryFragment);
        mFramentList.add(mFavoriteFragment);
        mFramentList.add(mFileManagerFragment);
        mFramentList.add(mAboutsFragment);

        mPageAdapter = new QuickFragmentPageAdapter(getFragmentManager(), mFramentList, mTitleArr);
        mVViewPager = (VerticalViewPager) findViewById(R.id.viewpager);
        mVViewPager.setAdapter(mPageAdapter);
        mVViewPager.setOffscreenPageLimit(4);
        mVViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mLeftMenu.setSelection(position);
                if (position != HOME_PAGE_FRAGMENT && position != HISTORY_FRAGMENT && position != FAVORITE_FRAGMENT) {
                    mSortBox.setVisibility(View.GONE);
                } else {
                    mSortBox.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        initLeftMenu();
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        mBtnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MovieSearchActivity.class);
            startActivity(intent);
        });

        mBtnSearch.setOnHoverListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                ViewCompat.animate(v).scaleY(1.1f).scaleX(1.1f).start();
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                ViewCompat.animate(v).scaleY(1f).scaleX(1f).start();
            }
            return false;
        });
        mBtnSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ViewCompat.animate(v).scaleY(1.1f).scaleX(1.1f).start();
            } else {
                ViewCompat.animate(v).scaleY(1f).scaleX(1f).start();
            }
        });

    }

    /**
     * 初始化菜单
     */
    private void initLeftMenu() {
        List<HashMap<String, Object>> rawDataList = getLeftMenuData();
        mLeftMenuAdapter = new LeftMenuListAdapter(HomePageActivity.this, rawDataList);
        mLeftMenu.setAdapter(mLeftMenuAdapter);
        mLeftMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < mFramentList.size()) {
                    LogUtil.v(TAG, "[onItemSelected] position=" + position);
                    mVViewPager.setCurrentItem(position);
                    mTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[position].toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLeftMenu.setOnItemClickListener((parent, view, position, id) -> {
            LogUtil.v(TAG, "[onItemClick] position=" + position);
            if (position < mFramentList.size()) {
                mVViewPager.setCurrentItem(position);
                mTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[position].toString());
                mDrawerLayout.closeDrawer(Gravity.START);
            } else {
                finish();
            }
        });

        mLeftMenu.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                mDrawerLayout.closeDrawer(Gravity.START);
            }
            LogUtil.v(TAG, "[onItemClick] hasFocus=" + hasFocus);
        });

    }

    private List<HashMap<String, Object>> getLeftMenuData() {
        ArrayList<HashMap<String, Object>> menuList = new ArrayList<>();
        String[] itemGroup1 = getResources().getStringArray(R.array.menu_item_group_1);
        TypedArray ta_1 = getResources().obtainTypedArray(R.array.menu_item_icon_group_1);
        for (int i = 0; i < itemGroup1.length; i++) {
            HashMap<String, Object> tmpmap = new HashMap<>();
            tmpmap.put(ConstData.TEXT, itemGroup1[i]);
            tmpmap.put(ConstData.ICON, ta_1.getResourceId(i, 0));
            menuList.add(tmpmap);
        }
        return menuList;
    }

    /**
     * 初始化设备选择窗口数据
     */
    private void initDevicePopUpWindow() {

        if (mPopupWindow == null) {
            int heightpx = DensityUtil.dip2px(mContext, 482);
            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setHeight(heightpx);
            mPopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            mPopupWindow.setContentView(LayoutInflater.from(this).inflate(
                    R.layout.movie_library_sort_layout, null));
            /*隐藏标题组*/
//            View titleGroup = mPopupWindow.getContentView().findViewById(R.id.title_group);
//            titleGroup.setVisibility(View.GONE);
            if (mPubYearsData == null || mGenresData == null || mSortTypeData == null) {
                setFilterData();
            }

            int pos_years = mPubYearsData.indexOf(mFilterYear);
            int pos_genres = mGenresData.indexOf(mFilterGenres);
            int pos_order = mSortTypeData.indexOf(mSortType);
            int pos_device = getCurrentSelectDevicePosition();
            int pos_dir = getCurrentSelectDirectoryPosition();
            Log.v(TAG, "被选中设备的index=" + pos_device);
            mCategoryView = (CategoryView) (mPopupWindow.getContentView().findViewById(R.id.categoryview));
            mCategoryView.addRestButton();

            mCategoryView.addConditionForDeviceAt(mDeviceList, getResources().getString(R.string.lb_sort_device), pos_device, SORT_DEVICE);
            mCategoryView.addConditionForDirectoryAt(mDirectoryList, getResources().getString(R.string.lb_sort_directory), pos_dir, SORT_DIR);
            mCategoryView.addConditionAt(mPubYearsData, getResources().getString(R.string.lb_sort_year), pos_years, SORT_YEAR);
            mCategoryView.addConditionAt(mGenresData, getResources().getString(R.string.lb_sort_type), pos_genres, SORT_GENRES);
            mCategoryView.addOrderAt(mSortTypeData, pos_order, SORT_ORDER);
            mCategoryView
                    .setOnClickCategoryListener(mOnClickCategoryListener);

            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setFocusable(true);

        } else {
            if (mCategoryView == null) {
                mCategoryView = (CategoryView) mPopupWindow.getContentView().findViewById(R.id.categoryview);
            }
            Log.v(TAG, "updateCondition()");

            int checkPosForGenres = mGenresData.indexOf(mFilterGenres);
            int checkPosForDevice = getCurrentSelectDevicePosition();


            mCategoryView.addConditionAt(mGenresData, getResources().getString(R.string.lb_sort_type), checkPosForGenres, SORT_GENRES);
            mCategoryView.addConditionForDeviceAt(mDeviceList, getResources().getString(R.string.lb_sort_device), checkPosForDevice, SORT_DEVICE);


        }
    }

    /**
     * 刷新设备和目录分类
     */
    private void refreshDeviceAndDirectoryPopUpWindow() {
        if (mCategoryView != null) {
            int checkPosForDevice = getCurrentSelectDevicePosition();
            Log.v(TAG, "refreshDeviceAndDirectoryPopUpWindow +" + checkPosForDevice);

            mCategoryView.addConditionForDeviceAt(mDeviceList, getResources().getString(R.string.lb_sort_device), checkPosForDevice, SORT_DEVICE);
            if (checkPosForDevice == -1) {
                mDirectoryList = getAllDirectories();
            } else {
                mDirectoryList = getDirectoriesByDeviceId(mDeviceList.get(checkPosForDevice).getId());
            }
            int checkPosForDir = getCurrentSelectDirectoryPosition();
            mCategoryView.addConditionForDirectoryAt(mDirectoryList, getResources().getString(R.string.lb_sort_directory), checkPosForDir, SORT_DIR);

        }

    }

    /**
     * 刷新目录分类
     *
     * @param selectPosForDevice 目前选择的设备索引
     */
    private void refreshDirectoryPopUpWindow(int selectPosForDevice) {
        if (mCategoryView != null) {
            if (selectPosForDevice == -1) {
                mDirectoryList = getAllDirectories();
            } else {
                mDirectoryList = getDirectoriesByDeviceId(mDeviceList.get(selectPosForDevice).getId());
            }
            int checkPosForDir = getCurrentSelectDirectoryPosition();
            mCategoryView.addConditionForDirectoryAt(mDirectoryList, getResources().getString(R.string.lb_sort_directory), checkPosForDir, SORT_DIR);

        }
    }

    /**
     * 筛选菜单点击事件
     *
     * @param group
     * @param checkedId
     */
    private void CategeoryViewEvent(RadioGroup group, int checkedId) {
        LogUtil.v(TAG, "mOnClickCategoryListener");
        int gTag = (Integer) group.getTag();
        RadioButton button = (RadioButton) group
                .findViewById(checkedId);
        int index = 0;
        switch (gTag) {
            case CategoryView.RADIOGROUP_SORT_SPECIAL_DEVICE:
                index = group.indexOfChild(button);
                refreshDirectoryPopUpWindow(index - 1);
                break;
            case CategoryView.RADIOGROUP_SORT_SPECIAL_DIR:
                getSortCondition();
                if (isDevChange) {
                    initMovie();
                    isDevChange = false;
                }
                break;
            case CategoryView.RADIOGROUP_SORT_CONDITION:
                getSortCondition();
                mHomePageFragment.initMovie();
                break;
            case CategoryView.RADIOGROUP_SORT_ORDER:
                index = group.indexOfChild(button);//获取被点击排序条件的索引
                if (mSortType.equals(button.getText().toString().substring(0, button.getText().toString().length() - 1))) {
                    isSortByAsc = !isSortByAsc;
                }//点击同一个排序条件则只切换排序方式
                getSortWays(index);
//                getSortCondition();
                mHomePageFragment.initMovie();
                break;
        }
    }


    /**
     * 初始化Fragment
     */
    private void initFragment() {
        if (mFileManagerFragment == null) {
            mFileManagerFragment = new FileManagerFragment();
        }
        if (mHomePageFragment == null) {
            mHomePageFragment = new HomePageFragment();
        }

        if (mAboutsFragment == null) {
            mAboutsFragment = new AboutsFragment();
        }
        if (mFavoriteFragment == null) {
            mFavoriteFragment = new FavoriteFragment();
        }
        if (mHistoryFragment == null) {
            mHistoryFragment = new HistoryFragment();
        }
    }


    private void PopUpDeviceWindow(View v) {
        mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
    }


    /**
     * 设置筛选条件
     */
    private void getSortCondition() {
        //所属设备条件
        RelativeLayout device_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_DEVICE);
        RadioGroup device_group = (RadioGroup) device_ll
                .findViewById(R.id.container);
        RadioButton device_check_button = (RadioButton) device_ll
                .findViewById(device_group.getCheckedRadioButtonId());
        int device_index = device_group.indexOfChild(device_check_button);//获取备选条件的index
        if (device_index == 0 && device_check_button.getText().equals(mTagAll)) {//0为全部，不做处理
            mFilterDeviceName = null;
            mFilterDeviceId = -1;
            if (mCurrDevice != null)
                isDevChange = true;
            mCurrDevice = null;
        } else {//传递路径提供给数据库进行查询
            mFilterDeviceId = mDeviceList.get(device_index - 1).getId();
            mFilterDeviceName = mDeviceList.get(device_index - 1).getName();
            if (mCurrDevice == null || mCurrDevice.getId() != mDeviceList.get(device_index - 1).getId())
                isDevChange = true;
            mCurrDevice = mDeviceList.get(device_index - 1);

        }

        //目录条件
        RelativeLayout dir_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_DIR);
        RadioGroup dir_group = (RadioGroup) dir_ll
                .findViewById(R.id.container);
        RadioButton dir_check_button = (RadioButton) dir_ll
                .findViewById(dir_group.getCheckedRadioButtonId());
        int dir_index = dir_group.indexOfChild(dir_check_button);//获取备选条件的index
        if (dir_index == 0 && dir_check_button.getText().equals(mTagAll)) {//0为全部，不做处理
            mFilterDirId = -1;
            if (mCurrDir != null)
                isDevChange = true;
            mCurrDir = null;
        } else {//传递路径提供给数据库进行查询

            mFilterDirId = mDirectoryList.get(dir_index - 1).getId();
            if (mCurrDir == null || !mCurrDir.getUri().equals(mDirectoryList.get(dir_index - 1).getUri()))
                isDevChange = true;
            mCurrDir = mDirectoryList.get(dir_index - 1);
        }

        //年份条件
        RelativeLayout year_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_YEAR);
        RadioGroup year_group = (RadioGroup) year_ll
                .findViewById(R.id.container);
        RadioButton year_check_button = (RadioButton) year_ll
                .findViewById(year_group.getCheckedRadioButtonId());
        mFilterYear = year_check_button.getText().toString();

        RelativeLayout genres_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_GENRES);
        RadioGroup genres_group = (RadioGroup) genres_ll
                .findViewById(R.id.container);
        RadioButton genres_check_button = (RadioButton) genres_ll
                .findViewById(genres_group.getCheckedRadioButtonId());
        mFilterGenres = genres_check_button.getText().toString();

        mPreferences.setDeviceId(mFilterDeviceId);
        mPreferences.setDeviceName(mFilterDeviceName);
        mPreferences.setDirectoryId(mFilterDirId);
        mPreferences.setYear(mFilterYear);
        mPreferences.setGenres(mFilterGenres);
        //排序方式
        getSortWays();
    }

    private void getSortWays() {
        getSortWays(-1);
    }


    /**
     * 获取排序条件
     *
     * @param i RadioGroup中被touch的子组件的索引 -1:没有改变order时填-1
     */
    private void getSortWays(int i) {
        Log.v(TAG, "getSortWays(" + i + ")");
        if (i >= 0) {//获取新的排序条件
            mSortType = mSortTypeData.get(i);
            Log.v(TAG, "getSortTag(String s)==" + mSortType + " asc=" + isSortByAsc);
        } else {
            // 排序方式没有改变时执行
            RelativeLayout order_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_ORDER);
            RadioGroup order_group = (RadioGroup) order_ll
                    .findViewById(R.id.container);
            RadioButton order_check_button = (RadioButton) order_ll
                    .findViewById(order_group.getCheckedRadioButtonId());
            i = order_group.indexOfChild(order_check_button);// 获取被选择的radiobutton的索引
            if (i == -1) {
                i = 0;
            }
            mSortType = mSortTypeData.get(i);
        }


        mPreferences.setSortType(mSortType);
        mPreferences.setSortByAsc(isSortByAsc);
        updateSortText(i);
    }


    /**
     * 获取缓存分类条件和获取分类信息列表
     */
    private void setFilterData() {
        if (mPreferences != null) {
            isSortByAsc = mPreferences.isSortByAsc();
            mSortType = mPreferences.getSortType();
            mFilterGenres = mPreferences.getGenres();
            mFilterYear = mPreferences.getYear();
            mFilterDeviceId = mPreferences.getDeviceId();
            mFilterDeviceName = mPreferences.getDeviceName();
            mFilterDirId = mPreferences.getDirectoryId();
        }
        if (mSortTypeData == null) {
            mSortTypeData = new ArrayList<>();
            mSortTypeData.add(getResources().getString(R.string.order_name));
            mSortTypeData.add(getResources().getString(R.string.order_score));
            mSortTypeData.add(getResources().getString(R.string.order_type));
            mSortTypeData.add(getResources().getString(R.string.order_time));
        }
        if (mMovieDao == null)
            mMovieDao = new MovieDao(mContext);
        mGenresData = mMovieDao.getAllGenres();
        mPubYearsData = fillYearsFilter();
    }

    /**
     * 获取筛选年份
     *
     * @return
     */
    private List<String> fillYearsFilter() {
        List<String> years = new ArrayList<String>();

        Calendar c = Calendar.getInstance();
//        int cY = c.get(Calendar.YEAR);
        int cY = 2020;
        int tmp = 15;
        int count = ((cY - 1990 + 1) / 5);
        int remain = (cY - 1990 + 1) % 5;
        //最近5-9年;
        for (int i = 0; i < remain; i++) {
            years.add(String.valueOf(cY - i));
        }
        //最近15年 每5年一组
        for (int i = 0; i < 3; i++) {
            int startYear = cY - remain + 1 - (i + 1) * 5;
            int endYear = cY - remain + 1 - (i + 1) * 5 + 4;
            years.add(startYear + " - " + endYear);
        }

        if ((cY - remain - tmp +1) % 10 > 0) {
            int startYear = cY - remain + 1 - 4 * 5;
            int endYear = cY - remain + 1 - 4 * 5 + 4;
            years.add(startYear + " - " + endYear);
            tmp += 5;
        }

        //每10年一组
        for (int k = cY - remain - tmp; k >= 1990; k = k - 10) {
            int startYear = k - 9;
            int endYear = k;
            years.add(startYear + " - " + endYear);
        }

        return years;
    }

    /**
     * 更新筛选文字以及根据索引更新Order RadioGroup的子组件的文字.
     *
     * @param i
     */
    private void updateSortText(int i) {
        Log.v(TAG, "updateSortText");
        // 更新筛选条件文字.
        StringBuffer st = new StringBuffer();
        if (mFilterDirId == -1) {
            st.append(getResources().getString(R.string.tx_alldir) + SPLIT);
        } else {
            st.append(mCurrDir.getName() + SPLIT);
        }
        if (mFilterYear.equals(mTagAll) && mFilterGenres.equals(mTagAll)) {
            st.append(mTagAll + SPLIT);
        } else {
            if (!mFilterYear.equals(mTagAll))
                st.append(mFilterYear + SPLIT);
            if (!mFilterGenres.equals(mTagAll))
                st.append(mFilterGenres + SPLIT);
        }
        st.append(mSortType);
        if (isSortByAsc) {
            st.append(ASC);
        } else {
            st.append(DESC);
        }
        final String tagString = st.toString();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSortBoxDevice.setText(tagString);
            }
        });

        // 根据传入的order索引修改Text
        RelativeLayout order_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_ORDER);
        RadioGroup order_group = (RadioGroup) order_ll
                .findViewById(R.id.container);
        for (int j = 0; j < order_group.getChildCount(); j++) {
            ((RadioButton) order_group.getChildAt(j))
                    .setText(mSortTypeData.get(j));
        }//重置排序条件的文字（清除排序方式的箭头）
        RadioButton order_check_button = (RadioButton) order_group
                .getChildAt(i);
        order_check_button.setText(mSortType + (isSortByAsc ? ASC : DESC));

    }


    /**
     * 获取当前选择设备的index
     *
     * @return
     */
    private int getCurrentSelectDevicePosition() {
        int pos = -1;
        if (mFilterDeviceId == -1 || mFilterDeviceName == null) {
            mCurrDevice = null;
            return pos;
        }
        for (int i = 0; i < mDeviceList.size(); i++) {
            if (mDeviceList.get(i).getId() == mFilterDeviceId) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    private int getCurrentSelectDirectoryPosition() {
        int pos = -1;
        if (mFilterDirId == -1)
            return pos;
        for (int i = 0; i < mDirectoryList.size(); i++) {
            if (mDirectoryList.get(i).getId() == mFilterDirId) {
                pos = i;
                break;
            }
        }
        return pos;
    }


    /**
     * 切换Fragment
     *
     * @param pos
     */
    private void setCurrentFragment(int pos) {
        mVViewPager.setCurrentItem(pos, true);
        mTitle.setText(getResources().getTextArray(R.array.home_page_classified_title)[pos].toString());
    }

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

    private List<Directory> getDirectoriesByDeviceId(long dev_id) {
        if (mDirectoryDao == null)
            mDirectoryDao = new DirectoryDao(mContext);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("parent_id=?");
        boolean isShowPrivate = getApp().isShowEncrypted();
        if (!isShowPrivate)
            stringBuffer.append(" and is_encrypted=0");
        Cursor cursor = mDirectoryDao.select(stringBuffer.toString(), new String[]{String.valueOf(dev_id)}, null);
        if (cursor.getCount() > 0) {
            List<Directory> directories = mDirectoryDao.parseList(cursor);
            return directories;
        }
        return null;
    }

    private List<Directory> getAllDirectories() {
        if (mDeviceList.size() > 0) {
            if (mDirectoryDao == null)
                mDirectoryDao = new DirectoryDao(mContext);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("(");
            List<String> whereArgsList = new ArrayList<>();

            for (int i = 0; i < mDeviceList.size(); i++) {
                Device device = mDeviceList.get(i);
                stringBuffer.append("(parent_id=?) or");
                whereArgsList.add(String.valueOf(device.getId()));
            }
            stringBuffer.replace(stringBuffer.length() - 3, stringBuffer.length(), ")");
            boolean isShowPrivate = getApp().isShowEncrypted();

            if (!isShowPrivate)
                stringBuffer.append(" and is_encrypted=0");

            String whereClause = stringBuffer.toString();
            Cursor cursor = mDirectoryDao.select(whereClause, whereArgsList.toArray(new String[0]), null);
            if (cursor.getCount() > 0) {
                List<Directory> directories = mDirectoryDao.parseList(cursor);
                return directories;
            }
        }
        return new ArrayList<>();
    }


    /**
     * 升级设备文字
     */
    private void updateDeviceText() {
        String device;
        if (mFilterDeviceName == null) {
            device = getResources().getString(R.string.tx_all);
        } else {
            device = mFilterDeviceName;
        }
        mSortBoxDevice.setText(device);

    }

    OnClickCategoryListener mOnClickCategoryListener = new OnClickCategoryListener() {
        @Override
        public void click(RadioGroup group, int checkedId) {
            CategeoryViewEvent(group, checkedId);
        }

        @Override
        public void onKeyPress(KeyEvent event, CategoryView view) {
            if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    try {
                        RadioGroup parent = (RadioGroup) view.getChildAt(SORT_ORDER).findViewById(R.id.container);
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            RadioButton focus_button = (RadioButton) parent.getChildAt(i);
                            if (focus_button.isFocused()) {
                                CategeoryViewEvent(parent, focus_button.getId());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScanService = ((MovieScanService.ScanBinder) service).getService();
            LogUtil.v(TAG, "on servcie" + name + " connected!!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.v(TAG, "on scan servcie disconnected!!");
            mScanService = null;
        }
    };
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConstData.ACTION_FAVORITE_MOVIE_CHANGE)) {
                mFavoriteFragment.getFavoritMovie();
            }
        }
    };

}
