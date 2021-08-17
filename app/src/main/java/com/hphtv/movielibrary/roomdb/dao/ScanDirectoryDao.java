package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/19
 */
@Dao
public interface ScanDirectoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long[] insertScanDirectories(ScanDirectory... scanDirectories);

    @Delete
    public void deleteScanDirectories(List<ScanDirectory> scanDirectories);

    @Query("DELETE FROM " + TABLE.SCAN_DIRECTORY + " WHERE path=:path")
    public void deleteScanDirectory(String path);

    @Query("DELETE FROM " + TABLE.SCAN_DIRECTORY + " WHERE is_user_add=0")
    public void delectTmpScanDirectories();

    @Query("UPDATE " + TABLE.SCAN_DIRECTORY + " SET is_hidden=:isHidden WHERE path=:path")
    public void updateScanDirectoryHiddenState(String path,boolean isHidden);

    @Update
    public void updateScanDirectory(ScanDirectory scanDirectory);

    @Query("SELECT * FROM " + TABLE.SCAN_DIRECTORY + " WHERE path=:path")
    public ScanDirectory queryScanDirectoryByPath(String path);

    @Query("SELECT * FROM " + TABLE.SCAN_DIRECTORY + " WHERE device_path=:devicePath AND is_user_add=0 LIMIT 0,:limit")
    public List<ScanDirectory> queryTmpScanDirectories(String devicePath, int limit);

    @Query("SELECT * FROM " + TABLE.SCAN_DIRECTORY + " WHERE device_path=:devicePath AND is_user_add=1")
    public List<ScanDirectory> queryScanDirByDevicePath(String devicePath);

    @Query("SELECT * FROM " + TABLE.SCAN_DIRECTORY + " WHERE is_user_add=1 AND is_hidden=0")
    public List<ScanDirectory> queryAllNotHiddenScanDirectories();

    @Query("SELECT * FROM " + TABLE.SCAN_DIRECTORY + " WHERE is_user_add=1 AND is_hidden=1")
    public List<ScanDirectory> queryAllHiddenScanDirectories();

}
