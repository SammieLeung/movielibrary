package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;

import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.sqlite.bean.Favorite;
import com.hphtv.movielibrary.sqlite.bean.History;
import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.PosterProviderBean;
import com.hphtv.movielibrary.sqlite.bean.VideoFile;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.dao.FavoriteDao;
import com.hphtv.movielibrary.sqlite.dao.HistoryDao;
import com.hphtv.movielibrary.sqlite.dao.MovieWrapperDao;
import com.hphtv.movielibrary.sqlite.dao.PosterProviderDao;
import com.hphtv.movielibrary.sqlite.dao.VideoFileDao;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.MovieSharedPreferences;
import com.hphtv.movielibrary.util.VideoPlayTools;

public class DeviceControlService extends Service {
    public static final int CMD_PLAY_VIDEO = 0;
    public static final int CMD_SET_FAVORITE = 1;
    public static final int CMD_CHECK_MOVIEDB = 2;

    public static final String TAG = DeviceControlService.class.getSimpleName();
    private MyHandler mMyHandler = new MyHandler();
    VideoFileDao mVideoFileDao;
    MovieWrapperDao mMovieWrapperDao;
    PosterProviderDao mPosterProviderDao;
    HistoryDao mHistoryDao;
    FavoriteDao mFavoriteDao;

    @Override
    public void onCreate() {
        super.onCreate();
        mVideoFileDao = new VideoFileDao(this);
        mMovieWrapperDao = new MovieWrapperDao(this);
        mPosterProviderDao = new PosterProviderDao(this);
        mHistoryDao = new HistoryDao(this);
        mFavoriteDao = new FavoriteDao(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(mMyHandler).getBinder();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msgFromClient) {
            Message msgToClient = Message.obtain(msgFromClient);
            switch (msgFromClient.what) {
                case CMD_PLAY_VIDEO:
                    LogUtil.v(TAG, "CMD_PLAY_VIDEO");
                    msgToClient.what = CMD_PLAY_VIDEO;
                    try {
                        playVideo(msgFromClient.arg1);
                        msgFromClient.replyTo.send(msgToClient);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case CMD_SET_FAVORITE:
                    LogUtil.v(TAG, "CMD_SET_FAVORITE");
                    msgToClient.what = CMD_SET_FAVORITE;
                    try {
                        Bundle bundle = msgFromClient.getData();
                        int isFavorite = bundle.getInt("favorite");
                        boolean res = setFavorite(msgFromClient.arg1, isFavorite);
                        msgToClient.arg1 = msgFromClient.arg1;
                        bundle.putBoolean("res", res);
                        msgToClient.setData(bundle);
                        msgFromClient.replyTo.send(msgToClient);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case CMD_CHECK_MOVIEDB:
                    MovieSharedPreferences movieSharedPreferences = MovieSharedPreferences.getInstance();
                    movieSharedPreferences.setContext(DeviceControlService.this);
                    boolean isMovieDbUpdate = movieSharedPreferences.isMovieDBUpdate();
                    if (isMovieDbUpdate) {
                        msgToClient.what = CMD_CHECK_MOVIEDB;
                        try {
                            msgFromClient.replyTo.send(msgToClient);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    public synchronized void playVideo(long id) {
        Cursor cursor = mVideoFileDao.select("id=?", new String[]{String.valueOf(id)}, null);
        final VideoFile file = (mVideoFileDao).parseList(cursor).get(0);
        long wrapper_id = file.getWrapper_id();
        if (wrapper_id > 0) {
            Cursor m_cursor = mMovieWrapperDao.select("id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (m_cursor != null && m_cursor.getCount() > 0) {
                MovieWrapper wrapper = mMovieWrapperDao.parseList(m_cursor).get(0);
                String poster = wrapper.getPoster();
                if (poster != null) {
                    mPosterProviderDao.deleteAll();
                    PosterProviderBean posterProviderBean = new PosterProviderBean();
                    posterProviderBean.setPoster(poster);
                    ContentValues values = mPosterProviderDao.parseContentValues(posterProviderBean);
                    mPosterProviderDao.insert(values);
                }
            }

            Cursor historyCursor = mHistoryDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (historyCursor.getCount() > 0) {
                long currentTime = System.currentTimeMillis();
                History history = mHistoryDao.parseList(historyCursor).get(0);
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = mHistoryDao.parseContentValues(history);
                mHistoryDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
            } else {
                long currentTime = System.currentTimeMillis();
                History history = new History();
                history.setWrapper_id(file.getId());
                history.setTime("0");
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = mHistoryDao.parseContentValues(history);
                mHistoryDao.insert(contentValues);
            }
        } else {
            String poster = file.getThumbnail();
            if (poster != null) {
                mPosterProviderDao.deleteAll();
                PosterProviderBean posterProviderBean = new PosterProviderBean();
                posterProviderBean.setPoster(poster);
                ContentValues values = mPosterProviderDao.parseContentValues(posterProviderBean);
                mPosterProviderDao.insert(values);
            }

            Cursor historyCursor = mHistoryDao.select("wrapper_id=?", new String[]{String.valueOf(wrapper_id)}, null);
            if (historyCursor.getCount() > 0) {
                long currentTime = System.currentTimeMillis();
                History history = mHistoryDao.parseList(historyCursor).get(0);
                history.setLast_play_time(String.valueOf(currentTime));
                ContentValues contentValues = mHistoryDao.parseContentValues(history);
                mHistoryDao.update(contentValues, "id=?", new String[]{String.valueOf(history.getId())});
            } else {
                long currentTime = System.currentTimeMillis();
                History history = new History();
                history.setTime("0");
                history.setLast_play_time(String.valueOf(currentTime));
                history.setWrapper_id(wrapper_id);
                ContentValues contentValues = mHistoryDao.parseContentValues(history);
                mHistoryDao.insert(contentValues);
            }
        }
        VideoPlayTools.play(this, file);
    }

    private synchronized boolean setFavorite(long id, int isFavorite) {
        Cursor cursor = mFavoriteDao.select("wrapper_id=?", new String[]{String.valueOf(id)}, null);

        if (cursor != null) {
            try {
                if (cursor.getCount() > 0 && isFavorite == 0) {
                    int res = mFavoriteDao.delete("wrapper_id=?", new String[]{String.valueOf(id)});
                    if (res > 0) {
                        return true;
                    }
                } else if (cursor.getCount() == 0 && isFavorite == 1) {
                    Favorite favorite = new Favorite();
                    favorite.setWrapper_id(id);
                    ContentValues values = mFavoriteDao.parseContentValues(favorite);
                    long row = mFavoriteDao.insert(values);
                    if (row > 0)
                        return true;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                sendRefreshFavoriteBroadcast(id);
                cursor.close();
                return true;
            }
        }
        return false;
    }

    private void sendRefreshFavoriteBroadcast(long id) {
        LogUtil.v(TAG, "sendRefreshFavoriteBroadcast " + id);
        Intent intent = new Intent();
        intent.setAction(ConstData.ACTION_FAVORITE_MOVIE_CHANGE);
        intent.putExtra("id", id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
