package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.ui.detail.MovieDetailActivity;
import com.hphtv.movielibrary.ui.homepage.genretag.GenreTagItem;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/1
 */
public class NewPageFragmentViewModel extends BaseAndroidViewModel {
    public static final int LIMIT=10;
    private GenreDao mGenreDao;
    private VideoFileDao mVideoFileDao;
    private MovieDao mMovieDao;
    private Callback mGenreCallback;

    public NewPageFragmentViewModel(@NonNull @NotNull Application application) {
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
                    List<HistoryMovieDataView> movieDataViewList = mVideoFileDao.queryHistoryMovieDataView(ScraperSourceTools.getSource(),0,LIMIT);
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
        getApplication().playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        prepareHistory(callback);
                    }
                });
    }
    
    public void startDetailActivity(AppBaseActivity appBaseActivity, MovieDataView movieDataView){
        Intent intent=new Intent(appBaseActivity, MovieDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.Extras.MOVIE_ID, movieDataView.id);
        bundle.putInt(Constants.Extras.MODE, Constants.MovieDetailMode.MODE_WRAPPER);
        intent.putExtras(bundle);
        appBaseActivity.startActivityForResult(intent);
    }

    public void prepareGenreList() {
        Observable.just(3)
                .subscribeOn(Schedulers.io())
                .map(defalut_count -> {
                    //优先顺序 自定义>已有电影>固定排序
                    List<String> newCustomTags = new ArrayList<>();
                    List<String> customGenreTags = mGenreDao.queryGenreTagNameBySource(ScraperSourceTools.getSource());
                    if (customGenreTags.size() == 0) {
                        List<String> allMovieGenres = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource());
                        //已有电影分类数量不足
                        if (allMovieGenres.size() < defalut_count) {
                            List<String> genreArr = Arrays.asList(getApplication().getResources().getStringArray(R.array.genre_tags).clone());
                            newCustomTags.addAll(allMovieGenres);
                            for (int i = 0; newCustomTags.size() < defalut_count && i < genreArr.size(); i++) {
                                String newTag = genreArr.get(i);
                                //不添加重复的Tag
                                if (!newCustomTags.contains(newTag))
                                    newCustomTags.add(genreArr.get(i));
                            }
                        } else {
                            for (int i = 0; i < defalut_count; i++) {
                                String newTag = allMovieGenres.get(i);
                                if (!newCustomTags.contains(newTag))
                                    newCustomTags.add(newTag);
                            }
                        }
                    } else {
                        newCustomTags.addAll(customGenreTags);
                    }
                    return newCustomTags;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> genreTags) {
                        if (mGenreCallback != null)
                            mGenreCallback.runOnUIThread(genreTags);
                    }
                });
    }

    public void updateGenreTagList(List<GenreTag> genreTagItems){
        new Thread(() -> {
            mGenreDao.deleteAllGenreTags();
            mGenreDao.insertGenreTags(genreTagItems);
            prepareGenreList();
        }).start();
    }

    public void prepareRecentlyAddedMovie(Callback callback) {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(),0,LIMIT);
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (callback != null)
                            callback.runOnUIThread(movieDataViewList);
                    }
                });

    }

    public void prepareFavorite(Callback callback) {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            List<MovieDataView> movieDataViewList = mMovieDao.queryFavoriteMovieDataView(ScraperSourceTools.getSource(),0,LIMIT);
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (callback != null)
                            callback.runOnUIThread(movieDataViewList);
                    }
                });
    }

    public void prepareRecommand(Callback callback) {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            String source = ScraperSourceTools.getSource();
            List<HistoryMovieDataView> history = mVideoFileDao.queryHistoryMovieDataView(source,0,LIMIT);
            List<String> genreList = new ArrayList<>();
            List<Long> idList = new ArrayList<>();
            for (int i = 0; i < 3 && i < history.size(); i++) {
                MovieWrapper wrapper = mMovieDao.queryMovieWrapperByFilePath(history.get(i).path, source);
                for (Genre genre : wrapper.genres) {
                    if (genre.source.equals(source) && !genreList.contains(genre.name)) {
                        genreList.add(genre.name);
                    }
                }
                idList.add(wrapper.movie.id);
            }
            emitter.onNext(mMovieDao.queryRecommand(source, genreList, idList,0,LIMIT));
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (callback != null)
                            callback.runOnUIThread(movieDataViewList);
                    }
                });
    }

    public void setGenreCallback(Callback genreCallback) {
        mGenreCallback = genreCallback;
    }

    public interface Callback {
        void runOnUIThread(List<?> list);
    }
}
