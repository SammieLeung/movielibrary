package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.Device;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */

@Dao
public interface DeviceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertDevices(Device... devices);


    @Update
    public void updateDevice(Device... devices);

    @Query("SELECT * FROM " + TABLE.DEVICE + " WHERE path =:mountpath")
    public Device querybyMountPath(String mountpath);

    @Query("SELECT * FROM "+TABLE.DEVICE)
    public List<Device> qureyAll();

    @Query("DELETE FROM " + TABLE.DEVICE)
    public void deleteAll();

    @Delete
    public void deleteDevices(Device... devices);
}
