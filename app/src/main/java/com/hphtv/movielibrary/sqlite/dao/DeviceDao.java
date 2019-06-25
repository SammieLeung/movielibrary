package com.hphtv.movielibrary.sqlite.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.hphtv.movielibrary.sqlite.bean.Device;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 17-12-5.
 */

public class DeviceDao extends BaseDao<Device> {


    public DeviceDao(Context context) {
        super(context,MovieDBHelper.TABLE_DEVICE);
    }


    @Override
    public List<Device> parseList(Cursor cursor) {
        List<Device> device_list = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Device device = new Device();
                long id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                int state = cursor.getInt(cursor.getColumnIndex("connect_state"));
                String path=cursor.getString(cursor.getColumnIndex("path"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                device.setId(id);
                device.setName(name);
                device.setConnect_state(state);
                device.setPath(path);
                device.setType(type);
                device_list.add(device);
            }
        }
        return device_list;
    }

    @Override
    public ContentValues parseContentValues(Device device) {
        ContentValues contentValues = new ContentValues();
        String name = device.getName();
        int status = device.getConnect_state();
        int type = device.getType();
        String path=device.getPath();
        contentValues.put("name", name);
        contentValues.put("connect_state", status);
        contentValues.put("type", type);
        contentValues.put("path",path);
        return contentValues;
    }



}
