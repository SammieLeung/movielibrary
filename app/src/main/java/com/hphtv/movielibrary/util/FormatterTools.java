package com.hphtv.movielibrary.util;

import android.content.Context;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.view.CategoryView;

/**
 * author: Sam Leung
 * date:  2021/6/28
 */
public class FormatterTools {

    public static String getDeviceName(Context context,Device device){
        String name = device.name;
        switch (device.type) {
            case ConstData.DeviceType.DEVICE_TYPE_INTERNAL_STORAGE:
                return getString(context,R.string.device_internal_storage);
            case ConstData.DeviceType.DEVICE_TYPE_USB:
                return getString(context,R.string.device_udisk_name, device.name);
            case ConstData.DeviceType.DEVICE_TYPE_SDCARDS:
                return getString(context,R.string.device_sdcard_name, device.name);
            case ConstData.DeviceType.DEVICE_TYPE_HARD_DISK:
                return getString(context,R.string.device_harddisk_name, device.name);
            case ConstData.DeviceType.DEVICE_TYPE_PCIE:
                return getString(context,R.string.device_ssd_name, device.name);
        }
        return name;
    }

    public static String getString(Context context, int res, Object... args){
        return String.format(context.getString(res),args) ;
    }
}
