package com.hphtv.movielibrary.util;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageList {
    private static final String TAG = "StorageList";
    private static StorageList sStorageList;
    private StorageManager mStorageManager;
    private Method mMethodGetPaths;
    private Method mMethodGetPathsState;

    private Method mMethodGetVolumes;
    private Method mMethodGetType;
    private Method mMethodGetDisk;
    private Method mMethodIsSd;
    private Method mMethodIsUsb;

    private Field _TYPE_PUBLIC;
    private Field mFieldPath;

    public static StorageList getInstance(){
        if(sStorageList==null)
            sStorageList=new StorageList();
        return sStorageList;
    }

    public void init(Context context) {
        if (context != null) {
            try {
                Class clazzVolumeInfo = Class.forName("android.os.storage.VolumeInfo");
                Class clazzDiskInfo = Class.forName("android.os.storage.DiskInfo");
                mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                mMethodGetPaths = ReflectionTools.getMethod(StorageManager.class, "getVolumePaths");
                //通过调用类的实例mStorageManager的getClass()获取StorageManager类对应的Class对象
                //getMethod("getVolumePaths")返回StorageManager类对应的Class对象的getVolumePaths方法，这里不带参数
                //getDeclaredMethod()----可以不顾原方法的调用权限
                mMethodGetPathsState = ReflectionTools.getMethod(StorageManager.class, "getVolumeState", String.class);
                mMethodGetVolumes = ReflectionTools.getMethod(StorageManager.class, "getVolumes");
                mMethodGetType = ReflectionTools.getMethod(clazzVolumeInfo, "getType");
                mMethodGetDisk = ReflectionTools.getMethod(clazzVolumeInfo, "getDisk");
                mMethodIsSd = ReflectionTools.getMethod(clazzDiskInfo, "isSd");
                mMethodIsUsb = ReflectionTools.getMethod(clazzDiskInfo, "isUsb");

                _TYPE_PUBLIC = clazzVolumeInfo.getDeclaredField("TYPE_PUBLIC");
                mFieldPath = clazzVolumeInfo.getDeclaredField("path");
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] getVolumnPaths() {
        String[] paths = null;
        try {
            paths = (String[]) ReflectionTools.invoke(mStorageManager, mMethodGetPaths);
            if (paths == null) {
                Log.d(TAG, "mMethodGetPaths Failed!!!");
                return null;
            }

//            for (String path : paths) {
//                Log.d(TAG, "Storage'paths:" + path + " state:" + getVolumeState(path));
//            }

        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return paths;
    }

    public String getVolumeState(String mountPoint) {
        //mountPoint是挂载点名Storage'paths[1]:/mnt/extSdCard不是/mnt/extSdCard/
        //不同手机外接存储卡名字不一样。/mnt/sdcard
        String status = null;
        try {
            status = (String) ReflectionTools.invoke(mStorageManager, mMethodGetPathsState, mountPoint);
            if (status == null)
                Log.d(TAG, "mMethodGetPathsState Failed!!!");
            //调用该方法，mStorageManager是主调，mountPoint是实参数
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return status;
    }

    public boolean isVolumeMounted(String mountPoint) {
        if (Environment.MEDIA_MOUNTED.equals(getVolumeState(mountPoint))) {
            return true;
        }
        return false;
    }

    public String getFlashStoragePath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public List<String> getUSBPaths() {
        List<String> paths = new ArrayList<>();
        try {
            //类型常量
            int TYPE_PULIC = _TYPE_PUBLIC.getInt(Class.forName("android.os.storage.VolumeInfo"));
            List<Object> volumeInfos = (List<Object>) mMethodGetVolumes.invoke(mStorageManager);
            for (Object volumeInfo : volumeInfos) {
                int type = (int) mMethodGetType.invoke(volumeInfo);
                if (type == TYPE_PULIC) {
                    Object _diskInfo = mMethodGetDisk.invoke(volumeInfo);

                    if (_diskInfo != null) {
                        if ((boolean) mMethodIsUsb.invoke(_diskInfo)) {
                            String path = mFieldPath.get(volumeInfo).toString();
                            paths.add(path);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return paths;
    }

    public List<String> getSdCardPaths() {
        List<String> paths = new ArrayList<>();
        try {
            //类型常量
            int TYPE_PULIC = _TYPE_PUBLIC.getInt(Class.forName("android.os.storage.VolumeInfo"));
            List<Object> volumeInfos = (List<Object>) mMethodGetVolumes.invoke(mStorageManager);
            for (Object volumeInfo : volumeInfos) {
                int type = (int) mMethodGetType.invoke(volumeInfo);
                if (type == TYPE_PULIC) {
                    Object _diskInfo = mMethodGetDisk.invoke(volumeInfo);

                    if (_diskInfo != null) {
                        if ((boolean) mMethodIsSd.invoke(_diskInfo)) {
                            String path = mFieldPath.get(volumeInfo).toString();
                            paths.add(path);
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return paths;
    }

    /**
     * 判断挂载路径是否为USB设备
     *
     * @param path
     * @return
     */
    public boolean isMountUsb(String path) {
        boolean isUsb = false;
        List<String> usbPaths = getUSBPaths();
        if (usbPaths != null && usbPaths.size() > 0 && usbPaths.contains(path))
            isUsb = true;
        return isUsb;
    }

    /**
     * 判断挂载路径是否为SD卡
     *
     * @param path
     * @return
     */
    public boolean isMountSdCard(String path) {
        boolean isSDCard = false;
        List<String> sdCardPaths = getSdCardPaths();
        if (sdCardPaths != null && sdCardPaths.size() > 0 && sdCardPaths.contains(path))
            isSDCard = true;
        return isSDCard;
    }

}