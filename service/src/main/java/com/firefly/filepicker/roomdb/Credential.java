package com.firefly.filepicker.roomdb;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * author: Sam Leung
 * date:  2021/12/17
 */
@Entity(tableName = TABLE.CREDENTIAL, primaryKeys = {"server", "share"})
public class Credential implements Serializable {
    @ColumnInfo(name = "server")
    @NonNull
    public String mServer;
    @ColumnInfo(name = "share")
    @NonNull
    public String mShare;
    @ColumnInfo(name = "user_name")
    public String mUsername;
    @ColumnInfo(name = "password")
    public String mPassword;

    @ColumnInfo(name = "domain")
    public String mDomain;
    @Ignore
    public boolean isTemporary = true;

    public Credential(String server, String share, String domain, String username, String password) {
        mUsername = username;
        mPassword = password;
        mServer = server==null?"":server;
        mShare = share==null?"":share;
        mDomain = domain;
    }

    public String getServer() {
        return mServer;
    }


    @NonNull
    public String getShare() {
        return mShare;
    }

    public void setShare(@NonNull String share) {
        mShare = share;
    }

    public void setServer(String server) {
        mServer = server;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getDomain() {
        return mDomain;
    }

    public void setDomain(String domain) {
        mDomain = domain;
    }

    public boolean isTemporary() {
        return isTemporary;
    }

    public void setTemporary(boolean temporary) {
        isTemporary = temporary;
    }

    public boolean isAnonymous(){
        return TextUtils.isEmpty(mUsername);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credential that = (Credential) o;
        return Objects.equals(mUsername, that.mUsername) &&
                Objects.equals(mPassword, that.mPassword) &&
                mServer.equals(that.mServer) &&
                Objects.equals(mDomain, that.mDomain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUsername, mPassword, mServer, mDomain);
    }
}
