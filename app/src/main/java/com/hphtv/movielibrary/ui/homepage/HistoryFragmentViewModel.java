package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/21
 */
public class HistoryFragmentViewModel extends BaseAndroidViewModel {
    private VideoFileDao mVideoFileDao;
    private String mSource;

    public HistoryFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSource= ScraperSourceTools.getSource();
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(application);
        mVideoFileDao = database.getVideoFileDao();
    }

    public void prepareHistory(Callback callback) {
        Observable.just("")
                .map(s -> {
                    List<HistoryMovieDataView> movieDataViewList = mVideoFileDao.queryHistoryMovieDataView(ScraperSourceTools.getSource());
                    return movieDataViewList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataViewList -> {
                    if (callback != null)
                        callback.runOnUIThread(dataViewList);
                });
    }

    public void playingVideo(String path, String name,Callback callback) {
        getApplication().playingMovie(path,name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        prepareHistory(callback);
                    }
                });
    }

    public interface Callback {
        void runOnUIThread(List<HistoryMovieDataView> dataViewList);
    }
}
