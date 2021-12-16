package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/1
 */
@Dao
public interface ShortcutDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertShortcut(Shortcut shortcut);

    @Query("SELECT * FROM " + TABLE.SHORTCUT)
    public List<Shortcut> queryAllShortcuts();

    @Query("SELECT * FROM " + TABLE.SHORTCUT + " WHERE device_type=:deviceType")
    public List<Shortcut> queryAllShortcutsByDevcietype(int deviceType);

    @Query("SELECT * FROM " + TABLE.SHORTCUT + " WHERE device_type <=5")
    public List<Shortcut> queryAllLocalShortcuts();

    @Query("SELECT * FROM " + TABLE.SHORTCUT + " WHERE device_type <=5 and device_path=:devicePath")
    public List<Shortcut> queryLocalShortcuts(String devicePath);

    @Query("SELECT * FROM " + TABLE.SHORTCUT + " WHERE uri=:uri")
    public Shortcut queryShortcutByUri(String uri);

    @Update
    public int updateShortcut(Shortcut shortcut);

    @Delete
    public int delete(Shortcut shortcut);
}
