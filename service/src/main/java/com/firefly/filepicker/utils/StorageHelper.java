package com.firefly.filepicker.utils;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by rany on 18-1-9.
 */

public class StorageHelper {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getVolumeState(StorageManager storageManager, String path){
        String result = "";

        if(null == storageManager || TextUtils.isEmpty(path)){
            return result;
        }

        try {
            Class clz = StorageManager.class;
            Method getVolumeList = clz.getMethod("getVolumeState", String.class);
            result = (String) getVolumeList.invoke(storageManager, path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static File getVolumePathFile(StorageVolume volume){
        try {
            Class clz = StorageVolume.class;
            Method getPathFile = clz.getMethod("getPathFile");
            return (File) getPathFile.invoke(volume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String getVolumePath(StorageVolume volume){
        try {
            Class clz = StorageVolume.class;
            Method getPathFile = clz.getMethod("getPath");
            return (String) getPathFile.invoke(volume);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static String[] getVolumePaths(StorageManager storageManager) {
        List<StorageVolume> volumes = storageManager.getStorageVolumes();
        int count = volumes.size();
        String[] paths = new String[count];

        for (int i = 0; i < count; i++) {
            paths[i] = getVolumePath(volumes.get(i));
        }

        return paths;
    }

    public static String[] getVolumePathsPreN(StorageManager storageManager) {
        String[] volumePaths = null;

        try {
            final Method method = storageManager.getClass().getMethod("getVolumePaths");

            if(null != method) {
                method.setAccessible(true);
                volumePaths = (String[]) method.invoke(storageManager);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return volumePaths;
    }

    public static StorageVolume[] getVolumeList(StorageManager storageManager){
        try {
            Class clz = StorageManager.class;
            Method getVolumeList = clz.getMethod("getVolumeList");
            return (StorageVolume[]) getVolumeList.invoke(storageManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            paths = StorageHelper.getVolumePaths(storageManager);
        } else {
            paths = StorageHelper.getVolumePathsPreN(storageManager);
        }

        return paths;
    }
}
