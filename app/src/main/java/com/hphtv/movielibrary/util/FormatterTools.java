package com.hphtv.movielibrary.util;

import android.content.Context;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.entity.Device;

/**
 * author: Sam Leung
 * date:  2021/6/28
 */
public class FormatterTools {

    public static String getDeviceName(Context context, Device device) {
        String name = device.name;
        switch (device.type) {
            case Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE:
                return getString(context, R.string.device_internal_storage);
            case Constants.DeviceType.DEVICE_TYPE_USB:
                return getString(context, R.string.device_udisk_name, device.name);
            case Constants.DeviceType.DEVICE_TYPE_SDCARDS:
                return getString(context, R.string.device_sdcard_name, device.name);
            case Constants.DeviceType.DEVICE_TYPE_HARD_DISK:
                return getString(context, R.string.device_harddisk_name, device.name);
            case Constants.DeviceType.DEVICE_TYPE_PCIE:
                return getString(context, R.string.device_ssd_name, device.name);
            case Constants.DeviceType.DEVICE_TYPE_DLNA:
                return getString(context, R.string.device_DLNA);
            case Constants.DeviceType.DEVICE_TYPE_SMB:
                return getString(context, R.string.device_smb);
        }

        return name;
    }

    public static String getTypeName(Context context,Device device,String dirPath){
        if(device!=null) {
            switch (device.type) {
                case Constants.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE:
                    return getString(context, R.string.device_internal_storage);
                case Constants.DeviceType.DEVICE_TYPE_USB:
                    return getString(context, R.string.device_udisk_name, device.name);
                case Constants.DeviceType.DEVICE_TYPE_SDCARDS:
                    return getString(context, R.string.device_sdcard_name, device.name);
                case Constants.DeviceType.DEVICE_TYPE_HARD_DISK:
                    return getString(context, R.string.device_harddisk_name, device.name);
                case Constants.DeviceType.DEVICE_TYPE_PCIE:
                    return getString(context, R.string.device_ssd_name, device.name);
                case Constants.DeviceType.DEVICE_TYPE_DLNA:
                    return getString(context, R.string.device_DLNA);
                case Constants.DeviceType.DEVICE_TYPE_SMB:
                    return getString(context, R.string.device_smb);
            }
            return getString(context, R.string.unknown);
        }else{
            if(dirPath.startsWith("http://")){
                return getString(context, R.string.device_DLNA);
            }else if(dirPath.startsWith("smb://")){
                return getString(context, R.string.device_smb);
            }else
                return getString(context, R.string.unknown);
        }
    };

    public static String getString(Context context, int res, Object... args) {
        return String.format(context.getString(res), args);
    }
}
