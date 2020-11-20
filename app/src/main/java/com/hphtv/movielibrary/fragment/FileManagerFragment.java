package com.hphtv.movielibrary.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.activity.FolderManagerActivity;
import com.hphtv.movielibrary.activity.HomePageActivity;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.sqlite.bean.Directory;
import com.hphtv.movielibrary.service.MovieScanService;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.dao.DeviceDao;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;
import com.hphtv.movielibrary.util.Base64Helper;
import com.hphtv.movielibrary.util.Md5Util;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.hphtv.movielibrary.view.CustomSelectorDialogFragment;
import com.hphtv.movielibrary.view.PasswordDialogFragment;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by tchip on 17-12-4.
 */

public class FileManagerFragment extends Fragment {
    public static final String TAG = FileManagerFragment.class.getSimpleName();
    private RelativeLayout rv_device_manager;
    private RelativeLayout rv_folder_add;
    private RelativeLayout rv_encrypted_setting;
    private HomePageActivity context;
    private MovieApplication mApp;
    MovieScanService scanService;
    private MovieSharedPreferences preferences;
    private String md5Password;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_layout_setting, container, false);
        rv_device_manager = (RelativeLayout) view.findViewById(R.id.setting_device_manager);
        rv_folder_add = (RelativeLayout) view.findViewById(R.id.setting_folder_add);
        rv_encrypted_setting = (RelativeLayout) view.findViewById(R.id.private_setting);
        rv_device_manager.setOnClickListener(mOnClicklistener);
        rv_folder_add.setOnClickListener(mOnClicklistener);
        rv_encrypted_setting.setOnClickListener(mOnClicklistener);

        rv_device_manager.setOnFocusChangeListener(mOnFocusChangeListener);
        rv_folder_add.setOnFocusChangeListener(mOnFocusChangeListener);
        rv_encrypted_setting.setOnFocusChangeListener(mOnFocusChangeListener);
        context = (HomePageActivity) getActivity();
        mApp = (MovieApplication) context.getApplication();
        preferences = MovieSharedPreferences.getInstance();
        return view;
    }

    public static final int REQUEST_CODE_MANAGER = 1;
    public static final int REQUEST_CODE_ADD = 2;
    private boolean isFilePickerOn=false;
    View.OnClickListener mOnClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.setting_device_manager:
                    Intent intent = new Intent(getActivity(), FolderManagerActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_MANAGER);
                    break;
                case R.id.setting_folder_add:
                    if(!isFilePickerOn){
                        isFilePickerOn=!isFilePickerOn;
                        try {
                            Intent picker_intent = new Intent(ConstData.ACTION_FILE_PICKER);
                            startActivityForResult(picker_intent, REQUEST_CODE_ADD);
                        } catch (Exception e) {
                            e.printStackTrace();
                            isFilePickerOn=false;
                            Toast.makeText(context, "can't open the file picker!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    break;
                case R.id.private_setting:
                    md5Password = preferences.getPassword();
                    if (TextUtils.isEmpty(md5Password)) {
                        final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(context);
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
                                        md5Password = Md5Util.md5(psd2);
                                        preferences.setPassword(md5Password);

                                        Toast.makeText(context, getResources().getString(R.string.psw_set_success), Toast.LENGTH_SHORT).show();
                                        fragment.dismiss();
                                    }

                                }
                            }

                            @Override
                            public void onCancle() {

                            }
                        });
                        fragment.SetPassword().show(getFragmentManager(), TAG);
                    } else {
                        String text1 = ((MovieApplication) context.getApplication()).isShowEncrypted() ? getResources().getString(R.string.title_hide_private) : getResources().getString(R.string.title_show_private);
                        String text2 = getResources().getString(R.string.title_change_password);
                        final CustomSelectorDialogFragment customSelectFragment = CustomSelectorDialogFragment.newInstance(text1, text2);
                        customSelectFragment.setOnButtonClickListener(new CustomSelectorDialogFragment.OnButtonClickListener() {
                            @Override
                            public void onClick(int pos, final View v) {
                                customSelectFragment.dismiss();
                                final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(context);

                                switch (pos) {
                                    case 0:
                                        if (mApp.isShowEncrypted()) {
                                            mApp.setShowEncrypted(false);
                                            updateShowPrivateText((TextView) v, false);
                                            context.checkConnectedDevices();
                                            context.initMovie();
                                            Toast.makeText(context, context.getResources().getString(R.string.tips_for_hide_private_videos), Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        fragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
                                            @Override
                                            public void onConfirm(List<EditText> editTextList) {
                                                md5Password = preferences.getPassword();
                                                if (editTextList.size() == 1) {
                                                    fragment.hideTips();
                                                    String psd = editTextList.get(0).getText().toString();
                                                    if (psd.length() < 4) {
                                                        fragment.showTips(0);
                                                        editTextList.get(0).requestFocus();
                                                    } else if (!Md5Util.md5(psd).equals(md5Password)) {
                                                        fragment.showTips(1);
                                                        editTextList.get(0).requestFocus();
                                                    } else {//
                                                        mApp.setShowEncrypted(true);
                                                        updateShowPrivateText((TextView) v, true);
                                                        context.checkConnectedDevices();
                                                        context.initMovie();
                                                        Toast.makeText(context, context.getResources().getString(R.string.tips_for_show_private_videos), Toast.LENGTH_SHORT).show();
                                                        fragment.dismiss();
                                                    }


                                                }
                                            }

                                            @Override
                                            public void onCancle() {

                                            }
                                        });
                                        fragment.InputPassword().show(getFragmentManager(), TAG);
                                        break;
                                    case 1:
                                        fragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
                                            @Override
                                            public void onConfirm(List<EditText> editTextList) {
                                                if (editTextList.size() == 3) {
                                                    fragment.hideTips();
                                                    String old_psd = editTextList.get(0).getText().toString();
                                                    String new_psd_1 = editTextList.get(1).getText().toString();
                                                    String new_psd_2 = editTextList.get(2).getText().toString();
                                                    if (old_psd.length() < 4) {
                                                        fragment.showTips(0);
                                                        editTextList.get(0).requestFocus();
                                                    } else if (!Md5Util.md5(old_psd).equals(md5Password)) {
                                                        fragment.showTips(2);
                                                        editTextList.get(0).requestFocus();
                                                    } else if (new_psd_1.length() < 4) {
                                                        editTextList.get(1).requestFocus();
                                                        fragment.showTips(0);
                                                    } else if (new_psd_2.length() < 4) {
                                                        editTextList.get(2).requestFocus();
                                                        fragment.showTips(0);
                                                    } else if (!new_psd_1.equals(new_psd_2)) {
                                                        fragment.showTips(1);
                                                        editTextList.get(2).requestFocus();
                                                    } else {
                                                        md5Password = Md5Util.md5(new_psd_1);
                                                        preferences.setPassword(md5Password);
                                                        Log.v(TAG, "new psw=" + md5Password);
                                                        Toast.makeText(context, getResources().getString(R.string.psw_set_success), Toast.LENGTH_SHORT).show();
                                                        fragment.dismiss();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancle() {

                                            }
                                        });
                                        fragment.ChangePassword().show(getFragmentManager(), TAG);
                                        break;
                                }
                            }
                        });
                        customSelectFragment.setTitle(getResources().getString(R.string.title_encryption_setting)).show(getFragmentManager(), TAG);
                    }
                    break;
            }
        }
    };
    View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                ViewCompat.animate(v).scaleX(1.04f).scaleY(1.04f).translationZ(1.0f).start();
            } else {
                ViewCompat.animate(v).scaleX(1f).scaleY(1f).translationZ(0).start();
            }
        }
    };


    private void updateShowPrivateText(TextView v, boolean bool) {
        if (bool) {
            v.setText(context.getResources().getString(R.string.title_hide_private));
        } else {
            v.setText(context.getResources().getString(R.string.title_show_private));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult " + resultCode);
        if (requestCode == REQUEST_CODE_MANAGER && resultCode == RESULT_OK) {

        } else if (requestCode == REQUEST_CODE_ADD ) {
            isFilePickerOn=false;
            if(resultCode == RESULT_OK) {
                final Uri uri = data.getData();
                if (scanService == null)
                    scanService = context.getMovieSearchService();
                addFolderAndMatchMovie(uri);
            }
        }
    }

    private void addFolderAndMatchMovie(final Uri uri) {
        Log.d(TAG, "addFolderAndMatchMovie() uri=" + uri.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                Directory directory = new Directory();

                //获取uri基本信息
                String deviceTypeStr = uri.getPathSegments().get(0);//device Api str
                int deviceType = ConstData.DeviceType.DEVICE_TYPE_LOCAL;
                String base64Id = uri.getPathSegments().get(1);//dir id
                final int isEncrypted = Integer.valueOf(uri.getPathSegments().get(3));
                //base64解码
                String decodeData = Base64Helper.decode(base64Id);
                Log.v(TAG, "Decode Uri=[" + decodeData + "]");
                Log.v(TAG, "base64=" + base64Id);
                Log.v(TAG, "isEncrypted " + isEncrypted);

                String deviceName = null;//device name
                String devicePath = null;

                String dirName = null;
                String dirPath = null;
                String dirUri = uri.toString();
                int scanState = ConstData.DirectoryState.UNSCAN;

                String[] part;
                switch (deviceTypeStr) {
                    case ConstData.DeviceType.STR_LOCAL:
                        deviceType = ConstData.DeviceType.DEVICE_TYPE_LOCAL;

                        //Decode base64Id=[/storage/AD33-9C29/FaceDetect2.0.1/build/android-profile]
                        dirPath = decodeData;

                        if (decodeData.contains("/storage/emulated/0")) {
                            devicePath = "/storage/emulated/0";
                            deviceName = getResources().getString(R.string.local_storage);
                            if (decodeData.equals("/storage/emulated/0"))
                                dirName = deviceName;
                            else
                                dirName = dirPath.substring(dirPath.lastIndexOf("/") + 1);

                        } else {
                            devicePath = decodeData.indexOf("/", 9) > 0 ? decodeData.substring(0, decodeData.indexOf("/", 9)) : decodeData;
                            deviceName = devicePath.substring(9, devicePath.length());
                            dirName = dirPath.substring(dirPath.lastIndexOf("/") + 1);
                        }

                        break;
                    case ConstData.DeviceType.STR_DLNA:
                        deviceType = ConstData.DeviceType.DEVICE_TYPE_DLNA;

                        //Decode Uri=[d06142a6-38e9-c22e-ffff-ffffb717f98f:video_:My Videos]
                        part = decodeData.split(":", 3);
                        devicePath = part[1];
                        dirPath = part[2];

                        deviceName = part[0];
                        dirName = part[2];
                        break;
                    case ConstData.DeviceType.STR_SAMBA:
                        deviceType = ConstData.DeviceType.DEVICE_TYPE_SMB;

                        //  Decode Uri=[smb://:@ARCHLINUX/myshare/]
                        // Decode Uri=[smb://TCHIP/lxpshare/sc]
                        if (decodeData.endsWith("/")) {
                            decodeData = decodeData.substring(0, decodeData.length() - 1);

                            Log.v(TAG, "decodeData=" + decodeData);

                        }
                        decodeData = decodeData.substring(6, decodeData.length());//   TCHIP/lxpshare/sc
                        part = decodeData.split("/");
                        //                        devicePath = decodeData.substring(0, decodeData.indexOf(part[part.length - 1]));


                        deviceName = part[0];//parent 为完整路径
                        devicePath = part[0];

                        dirName = part[part.length - 1];
                        dirPath = decodeData;
                        break;
                }
                Log.v(TAG, "devicePath[" + devicePath + "]");
                Log.v(TAG, "dirPath[" + dirPath + "]");
                Log.v(TAG, "deviceName[" + deviceName + "]");
                Log.v(TAG, "dirName[" + dirName + "]");
                Log.v(TAG, "------------------------------------");

                DeviceDao deviceDao = new DeviceDao(context);
                DirectoryDao dirDao = new DirectoryDao(context);

                Device device = new Device();
                device.setType(deviceType);
                device.setName(deviceName);
                device.setPath(devicePath);
                device.setConnect_state(ConstData.DeviceConnectState.CONNECTED);

                Cursor deviceCusor = deviceDao.select(null, "path=? and type=?", new String[]{devicePath, String.valueOf(deviceType)});
                if (deviceCusor != null && deviceCusor.getCount() > 0) {
                    device = deviceDao.parseList(deviceCusor).get(0);
                } else {
                    long id = deviceDao.insert(deviceDao.parseContentValues(device));
                    device.setId(id);
                }

                directory.setIsEcrypted(isEncrypted);
                directory.setMatchedVideo(0);
                directory.setName(dirName);
                directory.setParentId(device.getId());
                directory.setScanState(scanState);
                directory.setUri(dirUri);
                directory.setPath(dirPath);
                directory.setVideoNumber(0);

                long rowId = -1;
                Cursor dirCusor = dirDao.select(null, "uri=?", new String[]{directory.getUri()});
                if (dirCusor != null && dirCusor.getCount() > 0) {
                    directory = dirDao.parseList(dirCusor).get(0);
                } else {
                    rowId = dirDao.insert(dirDao.parseContentValues(directory));
                    if(rowId>0)
                        directory.setId(rowId);
                }


                final Device t_device = device;
                final Directory t_dir = directory;

                if (isEncrypted == ConstData.EncryptState.ENCRYPTED) {
                    md5Password = preferences.getPassword();
                    if (!TextUtils.isEmpty(md5Password)) {
                        final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(context);
                        fragment.setOnClickListener(new PasswordDialogFragment.OnClickListener() {
                            @Override
                            public void onConfirm(List<EditText> editTextList) {
                                if (editTextList.size() == 1) {
                                    fragment.hideTips();
                                    String psd = editTextList.get(0).getText().toString();
                                    if (psd.length() < 4) {
                                        fragment.showTips(0);
                                        editTextList.get(0).requestFocus();
                                    } else if (!Md5Util.md5(psd).equals(md5Password)) {
                                        fragment.showTips(1);
                                        editTextList.get(0).requestFocus();
                                    } else {
                                        fragment.dismiss();
                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "开始扫描...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        mApp.setShowEncrypted(true);
//                                        scanService.initScanService();
//                                        scanService.addToScanQueue(t_device, t_dir, isEncrypted);
                                        Intent intent = new Intent(getActivity(), FolderManagerActivity.class);
                                        Bundle bundle=new Bundle();
                                        bundle.putSerializable("device",t_device);
                                        bundle.putSerializable("dir",t_dir);
                                        bundle.putInt("is_encrypted",isEncrypted);
                                        intent.putExtras(bundle);
                                        startActivityForResult(intent, REQUEST_CODE_MANAGER);
                                    }
                                }
                            }

                            @Override
                            public void onCancle() {
                            }

                        });
                        fragment.InputPassword().show(getFragmentManager(), TAG);
                    } else {
                        final PasswordDialogFragment fragment = PasswordDialogFragment.newInstance(context);
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
                                        md5Password = Md5Util.md5(psd2);
                                        preferences.setPassword(md5Password);
                                        Toast.makeText(context, getResources().getString(R.string.psw_set_success), Toast.LENGTH_SHORT).show();
                                        fragment.dismiss();
                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "开始扫描...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        mApp.setShowEncrypted(true);
//                                        scanService.initScanService();
//                                        scanService.addToScanQueue(t_device, t_dir, isEncrypted);
                                        Intent intent = new Intent(getActivity(), FolderManagerActivity.class);
                                        Bundle bundle=new Bundle();
                                        bundle.putSerializable("device",t_device);
                                        bundle.putSerializable("dir",t_dir);
                                        bundle.putInt("is_encrypted",isEncrypted);
                                        intent.putExtras(bundle);
                                        startActivityForResult(intent, REQUEST_CODE_MANAGER);
                                    }
                                }
                            }

                            @Override
                            public void onCancle() {
                            }
                        });
                        fragment.SetPassword().show(getFragmentManager(), TAG);
                    }
                } else {
//                    scanService.initScanService();
//                    scanService.addToScanQueue(t_device, t_dir, isEncrypted);
                    Intent intent = new Intent(getActivity(), FolderManagerActivity.class);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("device",t_device);
                    bundle.putSerializable("dir",t_dir);
                    bundle.putInt("is_encrypted",isEncrypted);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_CODE_MANAGER);

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "开始扫描...", Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        }).start();
    }
}
