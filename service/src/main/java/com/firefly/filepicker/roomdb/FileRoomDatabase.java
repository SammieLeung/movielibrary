package com.firefly.filepicker.roomdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * author: Sam Leung
 * date:  2021/12/20
 */
@Database(entities = {Credential.class}, version = 1,exportSchema = false)
public abstract class FileRoomDatabase extends RoomDatabase {
    private static FileRoomDatabase sInstance;

    public abstract CredentialDao getCredentialDao();

    public static FileRoomDatabase getDatabase(final Context context) {
        if (sInstance == null) {
            synchronized (FileRoomDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            FileRoomDatabase.class, "filemanager_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return sInstance;
    }
}
