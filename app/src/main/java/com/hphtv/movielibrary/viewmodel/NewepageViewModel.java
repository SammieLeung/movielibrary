package com.hphtv.movielibrary.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/1
 */
public class NewepageViewModel extends AndroidViewModel {
    private GenreDao mGenreDao;
    private VideoFileDao mVideoFileDao;
    private MovieDao mMovieDao;

    public NewepageViewModel(@NonNull @NotNull Application application) {
        super(application);

        initDao();
    }


    /**
     * 初始化Dao类
     */
    private void initDao() {
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mGenreDao = movieLibraryRoomDatabase.getGenreDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
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

    public void playingVideo(String path, String name, Callback callback) {
        Observable.just(path)
                .subscribeOn(Schedulers.io())
                //记录播放时间，作为播放记录
                .doOnNext(filepath -> {
                    mVideoFileDao.updateLastPlaytime(filepath, System.currentTimeMillis());
                    String poster = mVideoFileDao.getPoster(filepath, ScraperSourceTools.getSource());
                    SharePreferencesTools.getInstance(getApplication()).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String path) {
                        VideoPlayTools.play(getApplication(), path, name);
                        prepareHistory(callback);
                    }
                });
    }

    public void prepareGenreList(Callback callback) {
        Observable.just(3)
                .subscribeOn(Schedulers.io())
                .map(defalut_count -> {
                    List<String> allGenres = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource());
                    if (allGenres != null && allGenres.size() > 0) {
                        List<String> genreTags = mGenreDao.queryGenreTagBySource(ScraperSourceTools.getSource());
                        for (String tag : genreTags) {
                            if (!allGenres.contains(tag))
                                genreTags.remove(tag);
                            else
                                allGenres.remove(tag);
                        }
                        while (genreTags.size() < defalut_count && allGenres.size() > 0) {
                            int size = allGenres.size();
                            int index = new Random().nextInt(size);
                            GenreTag newTag = new GenreTag();
                            newTag.name = allGenres.get(index);
                            newTag.source = ScraperSourceTools.getSource();
                            long res = mGenreDao.insertGenreTag(newTag);
                            if (res > 0) {
                                genreTags.add(newTag.name);
                                allGenres.remove(index);
                            }
                        }
                        return genreTags;
                    }
                    return new ArrayList<String>();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> genreTags) {
                        if (callback != null)
                            callback.runOnUIThread(genreTags);
                    }
                });

    }

    public void prepareRecentlyAddedMovie(Callback callback) {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource());
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if(callback!=null)
                            callback.runOnUIThread(movieDataViewList);
                    }
                });

    }

    public interface Callback {
        void runOnUIThread(List<?> list);
    }
}
