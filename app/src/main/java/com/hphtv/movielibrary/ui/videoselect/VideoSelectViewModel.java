package com.hphtv.movielibrary.ui.videoselect;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/1/21
 */
public class VideoSelectViewModel extends BaseAndroidViewModel {
    private VideoFileDao mVideoFileDao;

    public VideoSelectViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
    }

    public Observable<List<VideoFile>> getVideoList(String keyword) {
        return Observable.just(keyword)
                .observeOn(Schedulers.io())
                .map(ky -> {
                    List<VideoFile> list = new ArrayList<>();
                    list.addAll(mVideoFileDao.queryVideoFileListByKeyword(ky, ScraperSourceTools.getSource()));
                    return list;
                }).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> playVideo(String path,String name){
        return MovieHelper.playingMovie(path,name);
    }
}
