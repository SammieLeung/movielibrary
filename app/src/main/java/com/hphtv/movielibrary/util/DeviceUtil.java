package com.hphtv.movielibrary.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.dao.DeviceDao;
import com.hphtv.movielibrary.sqlite.dao.DirectoryDao;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 17-12-6.
 */

public class DeviceUtil {
    public static final String TAG = "DeviceUtil";


    private static Method mMethodGetPaths;
    private static Method mMethodGetPathsState;
    private static Method mMethodGetStorageVolume;
    private static StorageManager mStorageManager;
    public static final String USB_LABEL = "label";
    public static final String USB_PATH = "path";
    public static final String PROGRESS_BAR = "bar";

    /**
     * 打开mStorageManager
     * 初始化Method
     *
     * @param context
     */
    public static void openStorage(Context context) {
        if (mStorageManager == null)
            mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            mMethodGetPaths = mStorageManager.getClass().
                    getMethod("getVolumePaths");

            mMethodGetStorageVolume = mStorageManager.getClass().getMethod("getStorageVolume", File.class);
            //通过调用类的实例mStorageManager的getClass()获取StorageManager类对应的Class对象
            //getMethod("getVolumePaths")返回StorageManager类对应的Class对象的getVolumePaths方法，这里不带参数
            //getDeclaredMethod()----可以不顾原方法的调用权限
            mMethodGetPathsState = mStorageManager.getClass().
                    getMethod("getVolumeState", String.class);//String.class形参列表
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    public static void closeStorage() {
        mStorageManager = null;
        mMethodGetPaths = null;
        mMethodGetPathsState = null;
        mMethodGetStorageVolume = null;
    }

    private static StorageVolume getStorageVolume(File file) {
        StorageVolume storagevolume = null;
        if (mMethodGetStorageVolume != null && mStorageManager != null) {
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    storagevolume = (StorageVolume) mMethodGetStorageVolume.invoke(mStorageManager, file);
                }
                if (storagevolume == null) {
                    Log.d(TAG, "mMethodGetStorageVolume Failed!!!");
                    return null;
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return storagevolume;

    }

    public static String[] getVolumnPaths() {
        String[] paths = null;
        if (mMethodGetPaths != null && mStorageManager != null) {
            try {
                paths = (String[]) mMethodGetPaths.invoke(mStorageManager);//调用该方法
                if (paths == null) {
                    Log.d(TAG, "mMethodGetPaths Failed!!!");
                    return null;
                }


            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        return paths;
    }

    public static String getVolumeState(String mountPoint) {
        //mountPoint是挂载点名Storage'paths[1]:/mnt/extSdCard不是/mnt/extSdCard/
        //不同手机外接存储卡名字不一样。/mnt/sdcard
        String status = null;
        if (mMethodGetPathsState != null && mStorageManager != null) {
            try {
                status = (String) mMethodGetPathsState.invoke(mStorageManager, mountPoint);
                //调用该方法，mStorageManager是主调，mountPoint是实参数
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        Log.v(TAG, "getVolumeState " + mountPoint + " " + status);
        return status;
    }


    public static List<Device> getConnectedDevices(Context context, int isEncrypted) {
        String uriPrefix = ConstData.PREFIX_URI_CONNECTED_DEVICES;
        String[] types = new String[]{ConstData.DeviceType.STR_LOCAL, ConstData.DeviceType.STR_DLNA, ConstData.DeviceType.STR_SAMBA};
        List<Device> devices = new ArrayList<>();
        DeviceDao devDao = new DeviceDao(context);
        for (int i = 0; i < types.length; i++) {
            Uri uri = Uri.parse(uriPrefix + types[i]);//设备列表（对应各种类型);
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(
                        uri,
                        null,
                        null,
                        null,
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }

            StringBuffer whereClauseBuffer = new StringBuffer();
            List<String> whereArgsList = new ArrayList<>();

            if (cursor!=null&&cursor.getCount() > 0) {
//                if (isEncrypted == ConstData.EncryptState.UNENCRYPTED) {
//                    whereClauseBuffer.append("isEncrypted=? and ");
//                    whereArgsList.add(String.valueOf(isEncrypted));
//                }
                whereClauseBuffer.append("(");
                while (cursor != null && cursor.moveToNext()) {
                    whereClauseBuffer.append(" (path like ?) or");
//                    Log.d(TAG, "device_id=" + cursor.getString(cursor.getColumnIndexOrThrow("device_id")));
//                    Log.d(TAG, "device_name=" + cursor.getString(cursor.getColumnIndexOrThrow("device_name")));
//                    Log.d(TAG, "device_type=" + cursor.getString(cursor.getColumnIndexOrThrow("device_type")));
//                    Log.d(TAG, "----------------------------------- \n");
                    String device_id = cursor.getString(cursor.getColumnIndexOrThrow("device_id"));
                    String device_type = cursor.getString(cursor.getColumnIndexOrThrow("device_type"));

                    if (types[i].equalsIgnoreCase(ConstData.DeviceType.STR_SAMBA))
                        device_id = device_id.substring(6, device_id.length());
                    whereArgsList.add("%" + device_id + "%");
                }
                whereClauseBuffer.delete(whereClauseBuffer.length() - 2, whereClauseBuffer.length());
                whereClauseBuffer.append(")");
            } else {
                continue;
            }

            String whereClasue = whereClauseBuffer.toString();
            String[] whereArgs = whereArgsList.toArray(new String[0]);
            Cursor connectDevicesCursor = devDao.select(whereClasue, whereArgs, null);
            List<Device> connectDevices = devDao.parseList(connectDevicesCursor);//连接状态的设备列表
            for (Device device : connectDevices) {
                device.setConnect_state(ConstData.DeviceConnectState.CONNECTED);
                ContentValues values = devDao.parseContentValues(device);
                devDao.update(values, "id=?", new String[]{String.valueOf(device.getId())});
            }
            devices.addAll(connectDevices);
        }
        return devices;
    }


}
