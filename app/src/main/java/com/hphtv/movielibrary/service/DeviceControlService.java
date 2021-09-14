package com.hphtv.movielibrary.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DeviceControlService extends Service {
    public static final int CMD_PLAY_VIDEO = 0;
    public static final int CMD_SET_FAVORITE = 1;
    public static final int CMD_CHECK_MOVIEDB = 2;

    public static final String TAG = DeviceControlService.class.getSimpleName();
    private MyHandler mMyHandler = new MyHandler();
    VideoFileDao mVideoFileDao;
    MovieDao mMovieDao;

    @Override
    public void onCreate() {
        super.onCreate();
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(this);
        mVideoFileDao = database.getVideoFileDao();
        mMovieDao = database.getMovieDao();
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
                        e.printStackTrace();
                    }
                    break;
                case CMD_SET_FAVORITE:
                    LogUtil.v(TAG, "CMD_SET_FAVORITE");
                    msgToClient.what = CMD_SET_FAVORITE;
                    try {
                        Bundle bundle = msgFromClient.getData();
                        int isFavorite = bundle.getInt("favorite");
                        String movie_id=bundle.getString("movie_id");
                        setFavorite(movie_id, isFavorite==1?true:false);
                        msgToClient.arg1 = msgFromClient.arg1;
                        bundle.putBoolean("res", true);
                        msgToClient.setData(bundle);
                        msgFromClient.replyTo.send(msgToClient);
                    } catch (RemoteException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case CMD_CHECK_MOVIEDB:
                    boolean isMovieDbUpdate = SharePreferencesTools.getInstance(DeviceControlService.this).readProperty(Constants.SharePreferenceKeys.MOVIE_DB_UPDATE, false);
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
        VideoFile videoFile = mVideoFileDao.queryByVid(id);
        VideoPlayTools.play(this, videoFile.path, videoFile.filename);
    }

    private synchronized void setFavorite(String movie_id, boolean isFavorite) {
        Observable.just(movie_id)
                .observeOn(Schedulers.io())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        mMovieDao.updateFavoriteByMovieId(isFavorite, movie_id);
                    }
                });
    }

    private void sendRefreshFavoriteBroadcast(long id) {
        LogUtil.v(TAG, "sendRefreshFavoriteBroadcast " + id);
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_FAVORITE_MOVIE_CHANGE);
        intent.putExtra("id", id);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
