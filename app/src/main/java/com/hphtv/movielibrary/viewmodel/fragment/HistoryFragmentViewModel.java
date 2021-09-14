package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/21
 */
public class HistoryFragmentViewModel extends AndroidViewModel {
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
                    List<UnrecognizedFileDataView> movieDataViewList = mVideoFileDao.queryHistoryMovieDataView();
                    return movieDataViewList;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataViewList -> {
                    if (callback != null)
                        callback.runOnUIThread(dataViewList);
                });
    }

    public void playingVideo(String path, String name) {
        Observable.just(path)
                .subscribeOn(Schedulers.io())
                //记录播放时间，作为播放记录
                .doOnNext(filepath -> {
                    mVideoFileDao.updateLastPlaytime(filepath, System.currentTimeMillis());
                    String poster=mVideoFileDao.getPoster(filepath,mSource);
                    SharePreferencesTools.getInstance(getApplication()).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String path) {
                        VideoPlayTools.play(getApplication(), path, name);
                    }
                });
    }

    public interface Callback {
        void runOnUIThread(List<UnrecognizedFileDataView> dataViewList);
    }
}
