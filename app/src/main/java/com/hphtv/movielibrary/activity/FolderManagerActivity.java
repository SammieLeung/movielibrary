package com.hphtv.movielibrary.activity;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.adapter.DirectoryManagerAdapter;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.listener.ScanProgressListener;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.sqlite.bean.others.ParseFile;
import com.hphtv.movielibrary.sqlite.dao.DeviceDao;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.Base64Helper;
import com.hphtv.movielibrary.util.Md5Util;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.hphtv.movielibrary.view.ConfirmDialogFragment;
import com.hphtv.movielibrary.view.PasswordDialogFragment;
import com.hphtv.movielibrary.view.RecyclerViewWithMouseScroll;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by tchip on 17-12-4.
 */

public class FolderManagerActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public static final String TAG = FolderManagerActivity.class.getSimpleName();
    private RecyclerViewWithMouseScroll mDeviceRecyclerView;
    private DirectoryManagerAdapter mDirectoryManagerAdapter;
    private List<HashMap<String, Object>> mHashMapList = new ArrayList<HashMap<String, Object>>();
    private Context mContext;

    private CheckBox mCheckBoxAll;
    private CheckBox mCheckBoxPrivate;

    private Button mBtnDel;
    private Button mBtnClean;
    private Button mBtnScan;

    private TextView mTextViewLocation;
    private TextView mTextViewScanResult;

    private RelativeLayout mFooterGroup;
    private RelativeLayout mFooterGroup2;

    private MovieScanService mScanService;

    private Set<Integer> mCheckedSet = new HashSet<Integer>();
    private MovieSharedPreferences mPreferences;
    private String mMd5EncodePwd;
    private boolean isConfirm = true;
    private boolean mIsShowEncryted = false;

    private DirectoryDao mDirectoryDao;
    private DeviceDao mDeviceDao;
    private VideoFileDao mVideoFileDao;
    private MovieWrapperDao mMovieWrapperDao;
    private MovieDao mMovieDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting);
        Log.v(TAG, "onCreate");

        this.mContext = FolderManagerActivity.this;
        mPreferences = MovieSharedPreferences.getInstance();
        mDirectoryDao = new DirectoryDao(mContext);
        mDeviceDao = new DeviceDao(mContext);
        mVideoFileDao = new VideoFileDao(mContext);
        mMovieWrapperDao=new MovieWrapperDao(mContext);
        mMovieDao=new MovieDao(mContext);

    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        Intent intent = new Intent(this, MovieScanService.class);
        bindService(intent, connection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");
        unbindService(connection);
        super.onPause();
    }


    private void initData() {
        Log.v(TAG, "initData()");
        mHashMapList.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mDeviceDao == null)
                    mDeviceDao = new DeviceDao(mContext);
                if (mDirectoryDao == null)
                    mDirectoryDao = new DirectoryDao(mContext);
                if (mVideoFileDao == null)
                    mVideoFileDao = new VideoFileDao(mContext);

                SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
                Log.v(TAG, "begin search connectdevice; " + format.format(System.currentTimeMillis()));
                Cursor dir_cursor;
                if (mCheckBoxPrivate.isChecked()) {
                    dir_cursor = mDirectoryDao.selectAll();
                } else {
                    dir_cursor = mDirectoryDao.select("is_encrypted=?", new String[]{String.valueOf(0)}, null);
                }
                List<Directory> dirList = mDirectoryDao.parseList(dir_cursor);
                Log.v(TAG, "begin search end; " + format.format(System.currentTimeMillis()));
                for (Directory dir : dirList) {
                    final HashMap<String, Object> map = new HashMap<String, Object>();
                    int videoCount = dir.getVideo_number();
                    int matchedCount = dir.getMatched_video();
                    Cursor dev_cursor = mDeviceDao.select("id=?", new String[]{String.valueOf(dir.getParent_id())}, null);
                    Device device = null;
                    if (dev_cursor != null && dev_cursor.getCount() > 0)
                        device = mDeviceDao.parseList(dev_cursor).get(0);

                    map.put(ConstData.DIRECTORY, dir);
                    map.put(ConstData.DEVICE, device);
                    map.put(ConstData.DEVICE_MATCHED_VIDEO, matchedCount);
                    map.put(ConstData.DEVICE_VIDEO_COUNT, videoCount);
                    map.put(ConstData.DEVICE_CHECK_STATUS, false);
                    map.put(ConstData.DEVICE_IS_ENCRYPTED, dir.getIsEncrypted());
                    mHashMapList.add(map);
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanService != null && mScanService.isRunning()) {

                            List<Directory> scanDirectories = mScanService.getScanDirectories();
                            for (int i = 0; i < scanDirectories.size(); i++) {
                                for (int j = 0; j < mHashMapList.size(); j++) {
                                    HashMap<String, Object> map = mHashMapList.get(j);
                                    Directory directory = (Directory) map.get(ConstData.DIRECTORY);
                                    if (directory.getId().equals(scanDirectories.get(i).getId())) {
                                        directory.setScanState(ConstData.DirectoryState.SCANNING);
                                        scanDirectories.remove(i);
                                        i = 0;
                                        break;
                                    }
                                }
                            }
                            mBtnScan.setText(getResources().getString(R.string.scan_stop));
//                            mTextViewScanResult.setText("[" + scanBinder.getmCurrScanDir().getName() + "]:" + getResources().getString(R.string.tx_scanning));
                            setWidgetEnableWhenScanning(false);
                            showFooterResult();
                        } else {
                            Log.v(TAG, "run1");
                            mBtnScan.setText(getResources().getString(R.string.scan_begin));
                            setWidgetEnableWhenScanning(true);
                            hideFooterResult();
                        }
                        mDirectoryManagerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private void initView() {
        mDeviceRecyclerView = (RecyclerViewWithMouseScroll) findViewById(R.id.device_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FolderManagerActivity.this);
        mDeviceRecyclerView.setLayoutManager(linearLayoutManager);
        mDirectoryManagerAdapter = new DirectoryManagerAdapter(mContext, mHashMapList);
        mDirectoryManagerAdapter.setOnItemCheckedListener(new DirectoryManagerAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemCheked(List<HashMap<String, Object>> dataSet, int pos, boolean isChecked) {
                if (isChecked) {
                    mCheckedSet.add(pos);
                } else {
                    mCheckedSet.remove(pos);
                }

                if (isConfirm == false) {
                    isConfirm = true;
                    hideFooterResult();
                }
            }
        });

        mDirectoryManagerAdapter.setOnItemFocusListener(new DirectoryManagerAdapter.OnItemFocusListener() {
            @Override
            public void OnItemFocus(Device device, Directory directory, boolean isHover) {

                String folder = directory.getName();
                String uri = device.getPath();
                String[] datas;
                String local;
                int devType = device.getType();
                String parent;
                switch (devType) {
                    case ConstData.DeviceType.DEVICE_TYPE_LOCAL:
                        local = uri.substring(0, uri.lastIndexOf("/"));
                        setFooter(folder, local);
                        break;
                    case ConstData.DeviceType.DEVICE_TYPE_DLNA:
                        local = Base64Helper.decode(Uri.parse(uri).getPathSegments().get(1));
                        setFooter(folder, local);
                        break;
                    case ConstData.DeviceType.DEVICE_TYPE_SMB:
                        local = uri.substring(0, uri.lastIndexOf("/"));
                        setFooter(folder, local);
                        break;
                }

                if (isConfirm == false && !isHover) {
                    isConfirm = true;
                    hideFooterResult();
                }
            }
        });

        mDeviceRecyclerView.setAdapter(mDirectoryManagerAdapter);

        mCheckBoxAll = (CheckBox) findViewById(R.id.all);
        mCheckBoxPrivate = (CheckBox) findViewById(R.id.privatedevice);

        mCheckBoxAll.setOnCheckedChangeListener(this);
        mCheckBoxPrivate.setOnCheckedChangeListener(this);

        mBtnClean = (Button) findViewById(R.id.clear);
        mBtnDel = (Button) findViewById(R.id.delete);
        mBtnScan = (Button) findViewById(R.id.scan);

        mBtnDel.setOnClickListener(this);
        mBtnClean.setOnClickListener(this);
        mBtnScan.setOnClickListener(this);

        mTextViewLocation = (TextView) findViewById(R.id.footer_locaction);
        mTextViewScanResult = (TextView) findViewById(R.id.footer_scan_result);
        mFooterGroup = (RelativeLayout) findViewById(R.id.statusbar_group_2);
        mFooterGroup2 = (RelativeLayout) findViewById(R.id.statusbar_group_3);
    }


    private void setWidgetEnableWhenScanning(Boolean bool) {
        mBtnClean.setEnabled(bool);
        mBtnClean.setFocusable(bool);
        mBtnDel.setEnabled(bool);
        mBtnDel.setFocusable(bool);
        mDeviceRecyclerView.setEnabled(bool);
        mDeviceRecyclerView.setFocusable(bool);
        mCheckBoxPrivate.setEnabled(bool);
        mCheckBoxPrivate.setFocusable(bool);
        mCheckBoxAll.setEnabled(bool);
        mCheckBoxAll.setFocusable(bool);
        for (int i = 0; i < mDeviceRecyclerView.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout) mDeviceRecyclerView.getChildAt(i);
            CheckBox cb = (CheckBox) ll.findViewById(R.id.cb_dm_select);
            cb.setEnabled(bool);
            ((View) cb.getParent()).setEnabled(bool);
            ((View) cb.getParent()).setFocusable(bool);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.all:
                for (int i = 0; i < mDeviceRecyclerView.getChildCount(); i++) {
                    LinearLayout ll = (LinearLayout) mDeviceRecyclerView.getChildAt(i);
                    CheckBox cb = (CheckBox) ll.findViewById(R.id.cb_dm_select);
                    cb.setChecked(isChecked);
                }
                break;
            case R.id.privatedevice:
                if (isChecked == true) {
                    mMd5EncodePwd = mPreferences.getPassword();
                    if (!TextUtils.isEmpty(mMd5EncodePwd)) {
                        final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(mContext);
                        fragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
                            @Override
                            public void onConfirm(List<EditText> editTextList) {
                                if (editTextList.size() == 1) {
                                    fragment.hideTips();
                                    String psd = editTextList.get(0).getText().toString();
                                    if (psd.length() < 4) {
                                        fragment.showTips(0);
                                        editTextList.get(0).requestFocus();
                                    } else if (!Md5Util.md5(psd).equals(mMd5EncodePwd)) {
                                        fragment.showTips(1);
                                        editTextList.get(0).requestFocus();
                                    } else {
                                        mCheckedSet.clear();
                                        initData();
                                        fragment.dismiss();
                                    }
                                }
                            }

                            @Override
                            public void onCancle() {
                                mCheckBoxPrivate.setChecked(false);
                            }
                        });
                        if (mIsShowEncryted) {
                            mCheckedSet.clear();
                            initData();
                        } else {
                            fragment.InputPassword().show(getFragmentManager(), TAG);
                        }

                    } else {
                        final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(mContext);
                        fragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
                            @Override
                            public void onConfirm(List<EditText> editTextList) {
                                if (editTextList.size() == 2) {
                                    fragment.hideTips();
                                    String psd1 = editTextList.get(0).getText().toString();
                                    String psd2 = editTextList.get(1).getText().toString();
                                    if (psd1.length() < 4) {
                                        fragment.showTips(0);
                                        editTextList.get(0).requestFocus();
                                    } else if (psd2.length() < 4) {
                                        fragment.showTips(0);
                                        editTextList.get(1).requestFocus();
                                    } else if (!psd1.equals(psd2)) {
                                        fragment.showTips(1);
                                        editTextList.get(1).requestFocus();
                                    } else {
                                        mMd5EncodePwd = Md5Util.md5(psd2);
                                        mPreferences.setPassword(mMd5EncodePwd);
                                        mCheckedSet.clear();
                                        initData();
                                        Toast.makeText(mContext, getResources().getString(R.string.psw_set_success), Toast.LENGTH_SHORT).show();
                                        fragment.dismiss();
                                    }

                                }
                            }

                            @Override
                            public void onCancle() {
                                mCheckBoxPrivate.setChecked(false);
                            }
                        });
                        fragment.SetPassword().show(getFragmentManager(), TAG);

                    }
                } else {
                    mCheckedSet.clear();
                    initData();
                }

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                if (mCheckedSet.size() > 0) {
                    showClearDialog();
                }
                break;
            case R.id.delete:
                if (mCheckedSet.size() > 0) {
                    showDeleteDialog();
                }
                break;
            case R.id.scan:
                if (mScanService != null && mScanService.isRunning()) {
                    showStopDialog();
                } else {
                    if (mCheckedSet.size() > 0) {
                        showDialog();
                    }
                }
                break;
        }
    }

    /**
     * 清除设备和封面信息
     */
    private void deleteDeviceAndVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator iterator = mCheckedSet.iterator();
                while (iterator.hasNext()) {
                    int i = (int) iterator.next();
                    //清除Directory记录
                    Directory directory = (Directory) mHashMapList.get(i).get(ConstData.DIRECTORY);
                    String id = directory.getId();
                    directory.setMatchedVideo(0);
                    directory.setVideoNumber(0);
                    ContentValues deviceValues = mDirectoryDao.parseContentValues(directory);
                    mDirectoryDao.update(deviceValues, "id=?", new String[]{id});
                    //wrapper去掉对应的videofile id
                    Cursor video_cursor = mVideoFileDao.select("dir_id=?", new String[]{id}, null);
                    if (video_cursor.getCount() > 0) {
                        List<VideoFile> videoFileList = mVideoFileDao.parseList(video_cursor);
                        for (VideoFile videoFile : videoFileList) {
                            long wrapper_id=videoFile.getWrapper_id();
                            if (wrapper_id != -1) {
                                Cursor wrapper_cursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, "0,1");
                                if (wrapper_cursor.getCount() > 0) {
                                    MovieWrapper wrapper = mMovieWrapperDao.parseList(wrapper_cursor).get(0);
                                    Long[] file_ids = wrapper.getFileIds();
                                    List<Long> file_id_list = new ArrayList<>();
                                    for (Long fid : file_ids) {
                                        if (fid != videoFile.getId())
                                            file_id_list.add(fid);
                                    }
                                    file_ids = file_id_list.toArray(new Long[0]);
                                    wrapper.setFileIds(file_ids);
                                    if (file_ids.length > 0) {
                                        wrapper.setFileIds(file_ids);
                                        ContentValues contentValues = mMovieWrapperDao.parseContentValues(wrapper);
                                        mMovieWrapperDao.update(contentValues, "id=?", new String[]{String.valueOf(wrapper_id)});
                                    }else{
                                        mMovieDao.delete("wrapper_id=?",new String[]{String.valueOf(wrapper_id)});
                                        mMovieWrapperDao.delete("id=?",new String[]{String.valueOf(wrapper_id)});
                                    }
                                }
                            }
                        }
                    }
                    //删除相关videofile
                    mVideoFileDao.delete("dir_id=?", new String[]{id});
                    mDirectoryDao.delete("id=?", new String[]{id});
                }
                mCheckedSet.clear();
                setResult(RESULT_OK);
                initData();
            }
        }).start();
    }

    /**
     * 清除封面信息
     */
    private void clearVideoInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator iterator = mCheckedSet.iterator();
                while (iterator.hasNext()) {
                    int i = (int) iterator.next();
                    //清除Directory记录
                    Directory directory = (Directory) mHashMapList.get(i).get(ConstData.DIRECTORY);
                    String id = directory.getId();
                    directory.setMatchedVideo(0);
                    directory.setVideoNumber(0);
                    directory.setScanState(ConstData.DirectoryState.UNSCAN);
                    ContentValues deviceValues = mDirectoryDao.parseContentValues(directory);
                    mDirectoryDao.update(deviceValues, "id=?", new String[]{id});
                    //wrapper去掉对应的videofile id
                    Cursor video_cursor = mVideoFileDao.select("dir_id=?", new String[]{id}, null);
                    if (video_cursor.getCount() > 0) {
                        List<VideoFile> videoFileList = mVideoFileDao.parseList(video_cursor);
                        for (VideoFile videoFile : videoFileList) {
                            long wrapper_id=videoFile.getWrapper_id();
                            if (wrapper_id != -1) {
                                Cursor wrapper_cursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, "0,1");
                                if (wrapper_cursor.getCount() > 0) {
                                    MovieWrapper wrapper = mMovieWrapperDao.parseList(wrapper_cursor).get(0);
                                    Long[] file_ids = wrapper.getFileIds();
                                    List<Long> file_id_list = new ArrayList<>();
                                    for (Long fid : file_ids) {
                                        if (fid != videoFile.getId())
                                            file_id_list.add(fid);
                                    }
                                    file_ids = file_id_list.toArray(new Long[0]);
                                    if (file_ids.length > 0) {
                                        wrapper.setFileIds(file_ids);
                                        ContentValues contentValues = mMovieWrapperDao.parseContentValues(wrapper);
                                        mMovieWrapperDao.update(contentValues, "id=?", new String[]{String.valueOf(wrapper_id)});
                                    }else{
                                        mMovieDao.delete("wrapper_id=?",new String[]{String.valueOf(wrapper_id)});
                                        mMovieWrapperDao.delete("id=?",new String[]{String.valueOf(wrapper_id)});
                                    }
                                }
                            }
                        }
                    }
                    //删除相关videofile
                    mVideoFileDao.delete("dir_id=?", new String[]{id});
                }
                mCheckedSet.clear();
                setResult(RESULT_OK);
                initData();
            }
        }).start();
    }


    /**
     *
     */
    public void beginScan() {
        setWidgetEnableWhenScanning(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Iterator iterator = mCheckedSet.iterator();
                mScanService.initScanService();
                while (iterator.hasNext()) {
                    int pos = (int) iterator.next();
                    Log.d(TAG, "beginScan pos " + pos);
                    Device device = (Device) mHashMapList.get(pos).get(ConstData.DEVICE);
                    Directory directory = (Directory) mHashMapList.get(pos).get(ConstData.DIRECTORY);
                    beginScanVideo(device, directory, directory.getIsEncrypted());
                }
            }
        }).start();
    }


    /**
     * 扫描视频
     */
    private void beginScanVideo(Device device, Directory directory, int isEncrypted) {

        if (mScanService != null) {
            mScanService.addToScanQueue(device, directory, isEncrypted);
        }
    }

    public void stopScanVideo() {
        if (mScanService != null) {
            mScanService.stopScan();
        }
    }

    /**
     * 搜索中断时调用
     */
    private void onScanFinished() {
        Log.v(TAG, "onScanFinished()");
        mBtnScan.setText(getResources().getString(R.string.scan_begin));
        setWidgetEnableWhenScanning(true);
        isConfirm = false;
        mTextViewScanResult.setText(getResources().getString(R.string.tx_scan_completed));
    }

    private void notifyMovieInfoDataChange(String dirId, int total, int match, int status) {
        for (int i = 0; i < mHashMapList.size(); i++) {
            HashMap<String, Object> map = mHashMapList.get(i);
            Directory directory = (Directory) map.get(ConstData.DIRECTORY);
            if (directory.getId().equals(dirId)) {
                directory.setScanState(status);
                map.put(ConstData.DEVICE_VIDEO_COUNT, total);
                map.put(ConstData.DEVICE_MATCHED_VIDEO, match);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDirectoryManagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showDeleteDialog() {
        ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment(FolderManagerActivity.this);
        dialogFragment.setPositiveButton(new ConfirmDialogFragment.OnPositiveListener() {
            @Override
            public void OnPositivePress(Button button) {
                deleteDeviceAndVideo();
            }
        }).setMessage(getResources().getString(R.string.setting_confirm_delete)).show(getFragmentManager(), TAG);
    }

    private void showClearDialog() {
        ConfirmDialogFragment dialogFragment = new ConfirmDialogFragment(FolderManagerActivity.this);
        dialogFragment.setPositiveButton(new ConfirmDialogFragment.OnPositiveListener() {
            @Override
            public void OnPositivePress(Button button) {
                clearVideoInfo();
            }
        }).setMessage(getResources().getString(R.string.setting_confirm_clear)).show(getFragmentManager(), TAG);
    }

    private void showDialog() {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(FolderManagerActivity.this);
        dialog.setPositiveButton(new ConfirmDialogFragment.OnPositiveListener() {
            @Override
            public void OnPositivePress(Button button) {
                beginScan();
            }
        }, true).setMessage(getResources().getString(R.string.dialog_begin_scan)).show(getFragmentManager(), TAG);
    }


    private void showStopDialog() {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment(FolderManagerActivity.this);
        dialog.setPositiveButton(new ConfirmDialogFragment.OnPositiveListener() {
            @Override
            public void OnPositivePress(Button button) {
                stopScanVideo();
            }
        }, true).setMessage(getResources().getString(R.string.dialog_stop_scan)).show(getFragmentManager(), TAG);

    }

    ScanProgressListener mScanProgressListener = new ScanProgressListener() {
        @Override
        public void onStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnScan.setText(getResources().getString(R.string.scan_stop));
                    setWidgetEnableWhenScanning(false);
                    showFooterResult();
                }
            });

        }

        @Override
        public void onAddToScan(ParseFile parseFile) {
            Directory directory = parseFile.getDirectory();
            for (int i = 0; i < mHashMapList.size(); i++) {
                HashMap<String, Object> map = mHashMapList.get(i);
                Directory dir = (Directory) map.get(ConstData.DIRECTORY);
                if (dir.getId().equals(directory.getId())) {
                    dir.setScanState(ConstData.DirectoryState.SCANNING);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDirectoryManagerAdapter.notifyDataSetChanged();
                }
            });


        }

        @Override
        public void onGetFile() {

        }

        @Override
        public void onSuccess(ParseFile parseFile, @Nullable MovieWrapper movieWrapper) {
            Directory directory = parseFile.getDirectory();
            notifyMovieInfoDataChange(directory.getId(), directory.getVideo_number(), directory.getMatched_video(), directory.getScan_state());
        }

        @Override
        public void onFailed(ParseFile parseFile, @Nullable MovieWrapper movieWrapper) {
            Directory directory = parseFile.getDirectory();
            notifyMovieInfoDataChange(directory.getId(), directory.getVideo_number(), directory.getMatched_video(), directory.getScan_state());
        }

        @Override
        public void onFinish() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onScanFinished();
                }
            });
        }
    };

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mScanService = ((MovieScanService.ScanBinder) service).getService();
            mScanService.setScanProgressListener(mScanProgressListener);
            Log.v(TAG, "on serviceIntent " + name + " connected");
//            if (mScanService.isRunning() && mScanService.getmCurrScanDir().getEncrypted() == 1) {
//                initView();
//                mCheckBoxPrivate.setChecked(true);
//            } else {
            initView();
            initData();
//            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "onServiceDisconnected()");
        }
    };

    private void setFooter(String folder, String location) {
        if (mScanService != null && !mScanService.isRunning()) {
            mTextViewLocation.setText(getResources().getString(R.string.device_manager_footer_bar_path) + " " + location);
        }
    }

    /**
     * 显示底部扫描结果
     */
    private void showFooterResult() {
        mFooterGroup2.setVisibility(View.VISIBLE);
        mFooterGroup.setVisibility(View.GONE);
    }

    /**
     * 隐藏底部扫描结果
     */
    private void hideFooterResult() {
        mFooterGroup2.setVisibility(View.GONE);
        mFooterGroup.setVisibility(View.VISIBLE);
    }


    private void appendToScanProgress(String text) {
        if (mScanService.isRunning()) {
            showFooterResult();
        }
//        if (scanBinder.getmCurrScanDir().getEncrypted() == 0) {
//            mTextViewScanResult.setText("[" + scanBinder.getmCurrScanDir().getName() + "]:" + text);
//        } else {
//            mTextViewScanResult.setText("[" + scanBinder.getmCurrScanDir().getName() + "]:" + getResources().getString(R.string.tx_scanning));
//        }
        mTextViewScanResult.setText(text);
    }

//    private final BroadcastReceiver mMovieInfoReceiver = new BroadcastReceiver() {
//        public void onReceive(Context mContext, Intent intent) {
//            String action = intent.getAction();
//            Log.v(TAG, "Receive action=[" + action + "]");
//            if (MovieScanService.ScanBinder.ACTION_SCAN_BEGIN.equals(action)) {
//
//                String deviceId = intent.getStringExtra("deviceId");
//
//                for (int i = 0; i < mHashMapList.size(); i++) {
//                    HashMap<String, Object> map = mHashMapList.get(i);
//                    Device device = (Device) map.get(ConstData.DEVICE);
//                    if (device.getId().equals(deviceId)) {
//                        device.setConnect_state(ConstData.DirectoryState.SCANNING);
//                    }
//                }
//
//                mDirectoryManagerAdapter.notifyDataSetChanged();
//                mBtnScan.setText(getResources().getString(R.string.scan_stop));
//                setWidgetEnableWhenScanning(false);
//                showFooterResult();
//            }
//            if (MovieScanService.ScanBinder.ACTION_SCAN_FINISHED.equals(action)) {
//                int file_count = intent.getIntExtra("filecount", 0);
//
//                if (file_count != 0) {
//                    appendToScanProgress(getResources().getString(R.string.tx_p_begin_match));
//                }
//            }
//            if (MovieScanService.ScanBinder.ACTION_MATCH_FINISHED.equals(action) || MovieScanService.ScanBinder.ACTION_MATCH_FAILED.equals(action)) {
//                String deviceId = intent.getStringExtra("deviceId");
//                int matched_count = intent.getIntExtra("matched", 0);
//                int unmatched_count = intent.getIntExtra("unmatched", 0);
//                int total = intent.getIntExtra("total", 0);
//                String fileName = intent.getStringExtra("fileName");
//                String title = intent.getStringExtra("title");
//
//                notifyMovieInfoDataChange(deviceId, total, matched_count, ConstData.DirectoryState.SCANNING);
//                appendToScanProgress("[" + fileName + "]" + "(" + matched_count + "/" + (total) + ") 扫描完成:" + (matched_count + unmatched_count));
//            }
//            if (MovieScanService.ScanBinder.ACTION_SCAN_END.equals(action)) {
//                onScanFinished(intent);
//            }
//
//        }
//    };
}
