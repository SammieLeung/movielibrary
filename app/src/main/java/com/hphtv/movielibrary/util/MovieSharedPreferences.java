package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MovieSharedPreferences {
    public static final int DEFAULT_API = 2;

    public static final String YEAR = "year";
    public static final String GENRES = "genres";
    public static final String SORT_TYPE = "sortType";
    public static final String SORT_BY_ASC = "sortByAsc";
    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DIRECTORY_ID = "directoryId";
    public static final String SUB_TYPE = "subType";
    public static final String PASSWORD = "password";

    public static final String MODE = "mode";
    private static MovieSharedPreferences movieSharedPreferencesUtil = new MovieSharedPreferences();
    private SharedPreferences sharedPreferences;
    private int searchAPI;
    private String year;
    private String genres;
    private String sortType;
    private long deviceId;
    private String deviceName;
    private boolean isSortByAsc;
    private String subType;
    private String password;
    private long directoryId;

    private Context context;

    public static MovieSharedPreferences getInstance() {
        if (movieSharedPreferencesUtil == null) {
            movieSharedPreferencesUtil = new MovieSharedPreferences();
        }
        return movieSharedPreferencesUtil;
    }

    public void setContext(Context context) {
        this.context = context;
        sharedPreferences = context.getApplicationContext().getSharedPreferences("MovieLibrary", 0);
        this.searchAPI = sharedPreferences.getInt(MODE, DEFAULT_API);
        this.isSortByAsc = sharedPreferences.getBoolean(SORT_BY_ASC, true);
        this.year = sharedPreferences.getString(YEAR, "");
        this.genres = sharedPreferences.getString(GENRES, "");
        this.sortType = sharedPreferences.getString(SORT_TYPE, "");
        this.directoryId = sharedPreferences.getLong(DIRECTORY_ID, -1);
        this.deviceId = sharedPreferences.getLong(DEVICE_ID, -1);
        this.deviceName = sharedPreferences.getString(DEVICE_NAME, "");
        this.subType = sharedPreferences.getString(SUB_TYPE, "");
        this.password = sharedPreferences.getString(PASSWORD, "");
    }

    public void setSearchAPI(int searchAPI) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MODE, searchAPI);
        editor.commit();
        this.searchAPI = searchAPI;
    }

    public void setYear(String year) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(YEAR, year);
        editor.commit();
        this.year = year;
    }

    public void setGenres(String genres) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GENRES, genres);
        editor.commit();
        this.genres = genres;
    }

    public void setSortType(String sortType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SORT_TYPE, sortType);
        editor.commit();
        this.sortType = sortType;
    }

    public void setSortByAsc(boolean sortByAsc) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SORT_BY_ASC, sortByAsc);
        editor.commit();
        this.isSortByAsc = sortByAsc;
    }

    public void setDeviceId(long deviceId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DEVICE_ID, deviceId);
        editor.commit();
        this.deviceId = deviceId;
    }

    public void setDirectoryId(long directoryId) {
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong(DIRECTORY_ID,directoryId);
        editor.commit();
        this.directoryId=directoryId;
    }

    public void setSubType(String subType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SUB_TYPE, subType);
        editor.commit();
        this.subType = subType;
    }

    public void setDeviceName(String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_NAME, deviceName);
        editor.commit();
        this.deviceName = deviceName;
    }

    public void setPassword(String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PASSWORD, password);
        editor.commit();
        this.password = password;
    }

    public String getYear() {
        return year;
    }

    public String getGenres() {
        return genres;
    }

    public String getSortType() {
        return sortType;
    }

    public boolean isSortByAsc() {
        return isSortByAsc;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getSearchAPI() {
        return searchAPI;
    }

    public String getSubType() {
        return subType;
    }

    public String getPassword() {
        return password;
    }

    public long getDirectoryId(){return directoryId;}
}
