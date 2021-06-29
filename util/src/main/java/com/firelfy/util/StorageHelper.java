package com.firelfy.util;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import androidx.annotation.RequiresApi;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rany on 18-1-9.
 */

public class StorageHelper {
    public static final String TAG = StorageHelper.class.getSimpleName();

    private static Class<?> VolumeInfo;
    private static Class<?> DiskInfo;
    private static Method _getVolumes;
    private static Method _getType;
    private static Method _getDisk;
    private static Method _getVolumeState;
    private static Method _getVolumePaths;
    private static Method _getDirectory;
    private static Method _getPath;
    private static Field _TYPE_PUBLIC;
    private static Field _path;

    static {
        try {
            VolumeInfo = ClassLoader.getSystemClassLoader().loadClass("android.os.storage.VolumeInfo");
            DiskInfo = ClassLoader.getSystemClassLoader().loadClass("android.os.storage.DiskInfo");
            _getVolumes = ReflectionTools.getMethod(StorageManager.class, "getVolumes");
            _getVolumeState = ReflectionTools.getMethod(StorageManager.class, "getVolumeState", String.class);
            _getVolumePaths = ReflectionTools.getMethod(StorageManager.class, "getVolumePaths");
            _getDirectory = ReflectionTools.getMethod(StorageVolume.class, "getDirectory");
            _getPath = ReflectionTools.getMethod(StorageVolume.class, "getPath");
            _getType = ReflectionTools.getMethod(VolumeInfo, "getType");
            _getDisk = ReflectionTools.getMethod(VolumeInfo, "getDisk");
            _TYPE_PUBLIC = VolumeInfo.getDeclaredField("TYPE_PUBLIC");
            _TYPE_PUBLIC.setAccessible(true);
            _path = VolumeInfo.getDeclaredField("path");
            _path.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static String getVolumeState(StorageManager storageManager, String path) {
        String result = "";

        if (null == storageManager || TextUtils.isEmpty(path)) {
            return result;
        }

        try {
            result = (String) ReflectionTools.invoke(storageManager, _getVolumeState, path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public static String getVolumePath(StorageVolume volume) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                File file = (File)  ReflectionTools.invoke(volume,_getDirectory);
                if (file != null && file.exists()) {
                    return file.getPath();
                } else {
                    return "";
                }
            } else {
                return (String)  ReflectionTools.invoke(volume,_getPath);
            }
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


    public static String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            paths = StorageHelper.getVolumePaths(storageManager);
        }

        return paths;
    }


    public static List<String> getPaths(Context context, String method) {
        StorageManager storageManager = (StorageManager) (context.getSystemService(Context.STORAGE_SERVICE));
        List<String> paths = new ArrayList<String>();
        try {
            Method isDevice = DiskInfo.getDeclaredMethod(method);
            List<Object> volumes = (List<Object>) ReflectionTools.invoke(storageManager,_getVolumes);
            for (Object vol : volumes) {
                int type = (int) ReflectionTools.invoke(vol,_getType);
                int TYPE_PUBLIC = (int) _TYPE_PUBLIC.get(null);
                if (type == TYPE_PUBLIC) {
                    Object disk = ReflectionTools.invoke(vol,_getDisk);
                    if (disk != null) {
                        boolean isPcie = (boolean) ReflectionTools.invoke(disk,isDevice);
                        String path = (String) _path.get(vol);
                        if (isPcie && !TextUtils.isEmpty(path)) {
                            paths.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "get " + method + "->exception:" + e);
        } finally {
            return paths;
        }

    }


    public static String getFlashStoragePath(Context context) {
        StorageManager storageManager = (StorageManager) context
                .getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumes = storageManager.getStorageVolumes();
        int count = volumes.size();

        for (int i = 0; i < count; i++) {
            if (volumes.get(i).isPrimary())
                return getVolumePath(volumes.get(i));
        }

        return "";
    }

    /**
     * 获取Pcie路径
     *
     * @return
     */
    public static List<String> getPciePaths(Context context) {
        List<String> pciePaths = new ArrayList<String>();
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= Build.VERSION_CODES.M) {
            pciePaths.addAll(getPaths(context, "isPcie"));
        }
        return pciePaths;

    }

    /**
     * 获取HardDisk路径
     * sata/ usb sata /usb nvme
     *
     * @return
     */
    public static List<String> getHardDiskPaths(Context context) {
        List<String> hardDiskPaths = new ArrayList<String>();
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= Build.VERSION_CODES.M) {
            hardDiskPaths.addAll(getPaths(context, "isHardDisk"));
        }
        return hardDiskPaths;

    }


    /**
     * 获取SD card路径
     *
     * @return
     */
    public static List<String> getSdCardPaths(Context context) {
        List<String> sdPaths = new ArrayList<String>();
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= Build.VERSION_CODES.M) {
            sdPaths.addAll(getPaths(context, "isSd"));
        }
        return sdPaths;

    }

    /**
     * 获取USB路径
     *
     * @param context
     * @return
     */
    public static List<String> getUSBPaths(Context context) {
        List<String> usbPaths = new ArrayList<String>();
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        if (currentVersion >= Build.VERSION_CODES.M) {
            usbPaths.addAll(getPaths(context, "isUsb"));
        }
        return usbPaths;
    }

    /**
     * 判断挂载路径是否为USB设备
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountUsb(Context context, String path) {
        boolean isUsb = false;
        List<String> usbPaths = getUSBPaths(context);
        if (usbPaths != null && usbPaths.size() > 0 && usbPaths.contains(path))
            isUsb = true;
        return isUsb;
    }

    /**
     * 判断挂载路径是否为SD卡
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountSdCard(Context context, String path) {
        boolean isSDCard = false;
        List<String> sdCardPaths = getSdCardPaths(context);
        if (sdCardPaths != null && sdCardPaths.size() > 0 && sdCardPaths.contains(path))
            isSDCard = true;
        return isSDCard;
    }

    /**
     * 判断挂载路径是否为HardDisk(usb/sata)
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountHardDisk(Context context, String path) {
        List<String> paths = getHardDiskPaths(context);
        if (paths != null && paths.size() > 0 && paths.contains(path))
            return true;
        return false;
    }

    /**
     * 判断挂载路径是否为Pcie
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isMountPcie(Context context, String path) {
        List<String> paths = getPciePaths(context);
        if (paths != null && paths.size() > 0 && paths.contains(path))
            return true;
        return false;
    }

    /**
     * 通过执行df命令获取所有分区路径
     *
     * @return
     */
    public static List<String> getAllDfPaths() {
        List<String> paths = new ArrayList<String>();
        List<String> dfStrs = execdfCommond();
        if (dfStrs != null && dfStrs.size() > 0) {
            for (String item : dfStrs) {
                try {
                    paths.add(item.split("\\s+")[0]);
                } catch (Exception e) {

                }

            }
        }

        return paths;
    }

    /**
     * 执行df命令，获取磁盘分区的一些信息
     */
    public static List<String> execdfCommond() {
        List<String> list = new ArrayList<String>();
        try {
            Process su = Runtime.getRuntime().exec("df\n");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(su.getInputStream()));
            try {
                String line = bufferedReader.readLine();
                while (!TextUtils.isEmpty(line)) {
                    //Log.i(TAG, "line:" + line);
                    list.add(line);
                    line = bufferedReader.readLine();
                }
            } catch (Exception e) {
                //Log.i(TAG, "read:" + e);
            }

            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (Exception e) {
                //Log.i(TAG, "close:" + e);
            }
        } catch (Exception e) {
            //Log.i(TAG, "" + e);
        }

        return list;
    }
}
