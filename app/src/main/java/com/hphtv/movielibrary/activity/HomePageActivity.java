package com.hphtv.movielibrary.activity;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
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
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.firelfy.util.DensityUtil;
import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.LeftMenuListAdapter;
import com.hphtv.movielibrary.adapter.QuickFragmentPageAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.databinding.ActivityHomepageBinding;
import com.hphtv.movielibrary.fragment.AboutsFragment;
import com.hphtv.movielibrary.fragment.FavoriteFragment;
import com.hphtv.movielibrary.fragment.FileManagerFragment;
import com.hphtv.movielibrary.fragment.HistoryFragment;
import com.hphtv.movielibrary.fragment.HomePageFragment;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.service.MovieScanService2;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.view.CategoryView;
import com.hphtv.movielibrary.view.CustomLoadingCircleViewFragment;
import com.hphtv.movielibrary.viewmodel.HomepageViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

public class HomePageActivity extends AppBaseActivity<HomepageViewModel, ActivityHomepageBinding> {
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

    // 筛选条件
    private String mFilterDeviceName;
    private String mFilterDeviceId;
    private String mFilterYear;// 年份
    private String mFilterGenres;// 类型
    private String mSortType;// 排列顺序
    private boolean isSortByAsc = true;// 排列顺序升序

//    private LinearLayout mSortBox;
//    private TextView mSortBoxDevice;
//    private TextView mTitle;
//    private ImageView mBtnMenu;
//    private Button mBtnSearch;
//    private PopupWindow mPopupWindow;
//    private CategoryView mCategoryView;
//    private ListView mLeftMenu;

    private FileManagerFragment mFileManagerFragment;
    private HomePageFragment mHomePageFragment;
    private AboutsFragment mAboutsFragment;
    private FavoriteFragment mFavoriteFragment;
    private HistoryFragment mHistoryFragment;

    private CustomLoadingCircleViewFragment mLoadingCircleViewDialogFragment;

    //    private DrawerLayout mDrawerLayout;
    private List<Fragment> mFramentList;

    private Device mCurrDevice;
    private QuickFragmentPageAdapter<Fragment> mPageAdapter;
    //    private FrameLayout mContentPanel;
    private LeftMenuListAdapter mLeftMenuAdapter;

    //    private ImageView mBackgroundImage;
    private PopupWindow mPopupWindow;
    private CategoryView mCategoryView;
    private MovieScanService2 mScanService;
    private DeviceMonitorService mDeviceMonitorService;


    private boolean isDevChange = false;

    private Boolean isExit = false;

    //当前选择的fragment的索引

    @Override
    protected int getContentViewId() {
        return R.layout.activity_homepage;
    }

    @Override
    protected void processLogic() {
        LogUtil.v(TAG, "processLogic==>OnCreate");
//        if (LanguageUtil.isLanguageChanged(HomePageActivity.this, HomePageActivity.class))
//            LanguageUtil.restartApp(HomePageActivity.this, HomePageActivity.class);
        mTagAll = getResources().getString(R.string.tx_all);
        mTitleArr = new String[]{getResources().getString(R.string.lb_title), getResources().getString(R.string.lb_sort_directory), getResources().getString(R.string.lb_setting)};
        requestPermission();
    }


    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.v(TAG, "onResume()");
        registerReceiver();//注册广播
        bindService();//绑定服务
    }

    @Override
    protected void onPause() {
        LogUtil.v(TAG, "onPause()");
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
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
        initView();

    }


    private void bindService() {
        Intent intent = new Intent(this, MovieScanService2.class);
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        Intent intent2 = new Intent();
        intent2.setClass(this, DeviceMonitorService.class);
        bindService(intent2, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConstData.ACTION_FAVORITE_MOVIE_CHANGE);
        intentFilter.addAction(ConstData.BroadCastMsg.DEVICE_UP);
        intentFilter.addAction(ConstData.BroadCastMsg.DEVICE_DOWN);
        intentFilter.addAction(ConstData.BroadCastMsg.RESCAN_DEVICE);
        intentFilter.addAction(ConstData.BroadCastMsg.MOVIE_SCRAP_FINISH);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void initMovie() {
        mHomePageFragment.initMovie();
        mHistoryFragment.initMovie();
        mFavoriteFragment.getFavoritMovie();
    }

    public MovieScanService2 getMovieSearchService() {
        return mScanService;
    }

    public Device getCurrDevice() {
        return mCurrDevice;
    }

    public List<Device> getDeviceList() {
        return null;
    }

    public int getCurrentFragment() {
        return mBinding.viewpager.getCurrentItem();
    }

    public String getFilterDeviceName() {
        return mFilterDeviceName;
    }

    public String getFilterDeviceId() {
        return mFilterDeviceId;
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

//    /**
//     * 获取连接的设备列表
//     */
//    public void checkConnectedDevices() {
//        if (mDeviceMonitorService != null)
//            mDeviceMonitorService.checkDevices();
//    }

    /**
     * 初始化组件
     */
    private void initView() {
        LogUtil.v(TAG, "initView");
        mBinding.viewSortbox.setOnClickListener(v -> PopUpDeviceWindow(v));

        prepareFragmentsAndViewpagers();
        prepareLeftMenu();
        //搜索按钮事件
        mBinding.btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, MovieSearchActivity.class);
            startActivity(intent);
        });
        mBinding.btnSearch.setOnHoverListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                ViewCompat.animate(v).scaleY(1.1f).scaleX(1.1f).start();
            } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                ViewCompat.animate(v).scaleY(1f).scaleX(1f).start();
            }
            return false;
        });
        mBinding.btnSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ViewCompat.animate(v).scaleY(1.1f).scaleX(1.1f).start();
            } else {
                ViewCompat.animate(v).scaleY(1f).scaleX(1f).start();
            }
        });
        initDevicePopUpWindow();

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
                    mBinding.contentPanel.setTranslationX(width);
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
     * 左侧菜单数据
     *
     * @return
     */
    private List<HashMap<String, Object>> prepareMenuDataSet() {
        ArrayList<HashMap<String, Object>> menuList = new ArrayList<>();
        String[] itemGroup1 = getResources().getStringArray(R.array.menu_item_group_1);
        TypedArray ta_1 = getResources().obtainTypedArray(R.array.menu_item_icon_group_1);
        for (int i = 0; i < itemGroup1.length; i++) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(ConstData.TEXT, itemGroup1[i]);
            map.put(ConstData.ICON, ta_1.getResourceId(i, 0));
            menuList.add(map);
        }
        return menuList;
    }

    /**
     * 初始化设备选择窗口数据
     */
    private void initDevicePopUpWindow() {

        if (mPopupWindow == null) {
            int heightpx = DensityUtil.dip2px(this, 482);
            mPopupWindow = new PopupWindow(this);
            mPopupWindow.setHeight(heightpx);
            mPopupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            mPopupWindow.setContentView(LayoutInflater.from(this).inflate(
                    R.layout.movie_library_sort_layout, null));

            mPopupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setFocusable(true);
        }
        mCategoryView = (CategoryView) (mPopupWindow.getContentView().findViewById(R.id.categoryview));
        mCategoryView.setOnClickCategoryListener(mOnClickCategoryListener);

    }

    private void updateCateGoryView() {
        mCategoryView.setDevices(mViewModel.getConditionDevices()).setYears(mViewModel.getConditionYears()).setGenres(mViewModel.getConditionGenres()).create();
    }

    /**
     * 初始化Fragment 和 Viewpager
     */
    private void prepareFragmentsAndViewpagers() {
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
        mFramentList = new ArrayList<>();
        mFramentList.add(mHomePageFragment);
//        mFramentList.add(mHistoryFragment);
//        mFramentList.add(mFavoriteFragment);
//        mFramentList.add(mFileManagerFragment);
//        mFramentList.add(mAboutsFragment);
        //设置ViewPager
        mPageAdapter = new QuickFragmentPageAdapter(getSupportFragmentManager(), mFramentList, mTitleArr);
        mBinding.viewpager.setAdapter(mPageAdapter);
        mBinding.viewpager.setOffscreenPageLimit(4);
        mBinding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mBinding.leftmenuListview.setSelection(position);
                if (position != HOME_PAGE_FRAGMENT && position != HISTORY_FRAGMENT && position != FAVORITE_FRAGMENT) {
                    mBinding.viewSortbox.setVisibility(View.GONE);
                } else {
                    mBinding.viewSortbox.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }


    private void PopUpDeviceWindow(View v) {
        mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
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
                mBinding.tvSortDevice.setText(tagString);
            }
        });

        // 根据传入的order索引修改Text
        RelativeLayout order_ll = (RelativeLayout) mCategoryView.getChildAt(SORT_ORDER);
        RadioGroup order_group = (RadioGroup) order_ll
                .findViewById(R.id.container);
//        for (int j = 0; j < order_group.getChildCount(); j++) {
//            ((RadioButton) order_group.getChildAt(j))
//                    .setText(mSortTypes.get(j));
//        }//重置排序条件的文字（清除排序方式的箭头）
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
        if (mFilterDeviceId.equals("") || mFilterDeviceName == null) {
            mCurrDevice = null;
            return pos;
        }
//        for (int i = 0; i < mConditionDevices.size(); i++) {
//            if (mConditionDevices.get(i).id.equals(mFilterDeviceId)) {
//                pos = i;
//                break;
//            }
//        }
        return pos;
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
     */
    private void updateDeviceText() {
        String device;
        if (mFilterDeviceName == null) {
            device = getResources().getString(R.string.tx_all);
        } else {
            device = mFilterDeviceName;
        }
        mBinding.tvSortDevice.setText(device);

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
        }
//        else if (mScanService != null && mScanService.isRunning()) {
//            getApp().moveToBack(HomePageActivity.this);
//        }
        else {
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
                if (view instanceof ListView) {
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

    CategoryView.OnClickCategoryListener mOnClickCategoryListener = new CategoryView.OnClickCategoryListener() {
        @Override
        public void onSortChange(String sortType, boolean isDesc) {

        }

        @Override
        public void onConditionChange(Device device, String year, String genre) {

        }

    };

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (name.getClassName().equalsIgnoreCase(MovieScanService2.class.getCanonicalName())) {
                mScanService = ((MovieScanService2.ScanBinder) service).getService();

            } else if (name.getClassName().equalsIgnoreCase(DeviceMonitorService.class.getCanonicalName())) {
                mDeviceMonitorService = ((DeviceMonitorService.MonitorBinder) service).getService();
                mDeviceMonitorService.scanDevices();
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
                case ConstData.ACTION_FAVORITE_MOVIE_CHANGE:
                    break;
                case ConstData.BroadCastMsg.DEVICE_UP:
                    String mountPath = intent.getStringExtra(ConstData.DeviceMountMsg.DEVICE_MOUNT_PATH);
                    mViewModel.prepareSortDeviceConditions(mScanService, mountPath, () -> updateCateGoryView());
                    break;
                case ConstData.BroadCastMsg.DEVICE_DOWN:
                    mViewModel.prepareSortDeviceConditions(mScanService,() -> updateCateGoryView());
                    break;
                case ConstData.BroadCastMsg.RESCAN_DEVICE:
                    break;
                case ConstData.BroadCastMsg.MOVIE_SCRAP_FINISH:
                    mViewModel.prepareOtherConditions(() -> updateCateGoryView());
                    break;
                case ConstData.BroadCastMsg.MOVIE_GET_CATEGORY_FINISH:
                    break;
            }
        }
    };


}
