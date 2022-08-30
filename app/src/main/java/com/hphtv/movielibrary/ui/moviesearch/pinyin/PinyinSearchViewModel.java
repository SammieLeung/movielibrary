package com.hphtv.movielibrary.ui.moviesearch.pinyin;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.PinyinParseAndMatchTools;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/8/19
 */
public class PinyinSearchViewModel extends AndroidViewModel {

    private MovieDao mMovieDao;

    private List<MovieDataView> mMovieDataViewList;
    private String mSource;

    private ObservableBoolean isEmpty=new ObservableBoolean(true);
    private ObservableBoolean isShowTab=new ObservableBoolean(false);

    public PinyinSearchViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase database = MovieLibraryRoomDatabase.getDatabase(application);
        mMovieDao = database.getMovieDao();
        mSource= ScraperSourceTools.getSource();
    }

    public void init() {
        Observable.just("")
                .observeOn(Schedulers.io())
                .subscribe(s -> mMovieDataViewList = mMovieDao.queryAllMovieDataView(mSource, Config.getSqlConditionOfChildMode()));
    }

    public void search(String keyword, Callback<MovieDataView> callback) {
        Observable.just(keyword)
                .map(s -> PinyinParseAndMatchTools.getInstance().match(mMovieDataViewList, keyword))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataViewList -> {
                    if (callback != null)
                        callback.runOnUIThread(dataViewList);
                });
    }

    public ObservableBoolean getIsEmpty() {
        return isEmpty;
    }

    public ObservableBoolean getIsShowTab() {
        return isShowTab;
    }

    public interface Callback<T> {
        void runOnUIThread(List<T> data);
    }
}
