package com.firefly.filepicker.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/12/20
 */
@Dao
public interface CredentialDao {

    @Insert(onConflict= OnConflictStrategy.REPLACE)
    public long insertCredential(Credential credential);

    @Update
    public int updateCredential(Credential credential);

    @Query("DELETE FROM "+TABLE.CREDENTIAL+" WHERE server=:server AND share=:share")
    public int deleteCredential(String server,String share);

    @Query("SELECT * FROM "+TABLE.CREDENTIAL)
    public List<Credential> queryAllCredentials();
}
