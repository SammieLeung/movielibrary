package com.hphtv.movielibrary.roomdb.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

    @Query("SELECT * FROM "+ TABLE.SHORTCUT)
    public List<Shortcut> queryAllShortcuts();

    @Query("SELECT * FROM "+TABLE.SHORTCUT+" WHERE type=:type")
    public List<Shortcut> queryAllShortcutsByType(int type);

    @Query("SELECT * FROM "+ TABLE.SHORTCUT +" WHERE uri=:uri")
    public Shortcut queryShortcutByUri(String uri);
}
