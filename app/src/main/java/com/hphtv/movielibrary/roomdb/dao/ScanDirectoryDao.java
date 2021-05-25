package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/19
 */
@Dao
public interface ScanDirectoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertScanDirectories(ScanDirectory... scanDirectories);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long[] insertScanDirectories(List<ScanDirectory> scanDirectories);

    @Delete
    public void deleteScanDirectories(ScanDirectory... scanDirectories);


    @Delete
    public void deleteScanDirectories(List<ScanDirectory> scanDirectories);

    @Query("SELECT * FROM "+ TABLE.SCAN_DIRECTORY+" WHERE deviceId=:deviceId LIMIT 0,:limit")
    public List<ScanDirectory> queryScanDirectories(String deviceId,int limit);
}
