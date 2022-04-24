package com.hphtv.movielibrary.ui.pagination;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.ui.homepage.fragment.homepage.HomeFragmentViewModel;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/22
 */
public class PaginationViewModel extends BaseAndroidViewModel {
    public static final int LIMIT = 15;
    public static final int OPEN_RECENTLY_ADD = 1;
    public static final int OPEN_FAVORITE = 2;
    private GenreDao mGenreDao;
    private VideoFileDao mVideoFileDao;
    private MovieDao mMovieDao;
    private int mType;
    private ObservableField<String> mTitle = new ObservableField<>();
    private AtomicInteger mPage = new AtomicInteger(0);
    private AtomicInteger mTotal = new AtomicInteger(0);

    public PaginationViewModel(@NonNull @NotNull Application application) {
        super(application);
        MovieLibraryRoomDatabase movieLibraryRoomDatabase = MovieLibraryRoomDatabase.getDatabase(getApplication());
        mGenreDao = movieLibraryRoomDatabase.getGenreDao();
        mVideoFileDao = movieLibraryRoomDatabase.getVideoFileDao();
        mMovieDao = movieLibraryRoomDatabase.getMovieDao();
    }

    public void setType(int type) {
        mType = type;
        switch (mType) {
            case OPEN_RECENTLY_ADD:
                mTitle.set(getString(R.string.recently_added_list_title));
                break;
            case OPEN_FAVORITE:
                mTitle.set(getString(R.string.my_favorite_list_title));
                break;
        }
    }

    public ObservableField<String> getTitle() {
        return mTitle;
    }

    public void reload(){
        switch (mType){
            case OPEN_RECENTLY_ADD:
                reloadRecentlyAddedMovie();
                break;
            case OPEN_FAVORITE:
                reloadFavoriteMovie();
                break;
        }
    }

    public void loadMore(){
        switch (mType){
            case OPEN_RECENTLY_ADD:
                loadRecentlyAddedMovie();
                break;
            case OPEN_FAVORITE:
                loadFavoriteMovie();
                break;
        }
    }

    private void reloadRecentlyAddedMovie() {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            mPage.set(0);
            int count = mMovieDao.countMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode());
            mTotal.set(count);
            List<MovieDataView> movieDataViewList = mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), mPage.get(), LIMIT);
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (mOnRefresh != null)
                            mOnRefresh.newSearch(movieDataViewList);
                    }
                });

    }

    private void loadRecentlyAddedMovie() {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            if ((mPage.get() + 1) * LIMIT < mTotal.get()) {
                int offset = mPage.incrementAndGet() * LIMIT;
                List<MovieDataView> movieDataViewList = mMovieDao.queryMovieDataViewForRecentlyAdded(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), offset, LIMIT);
                emitter.onNext(movieDataViewList);
            }
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (mOnRefresh != null)
                            mOnRefresh.appendMovieDataViews(movieDataViewList);
                    }
                });
    }

    private void reloadFavoriteMovie() {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            mPage.set(0);
            int count = mMovieDao.countFavoriteMovieDataView(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode());
            mTotal.set(count);
            List<MovieDataView> movieDataViewList = mMovieDao.queryFavoriteMovieDataView(ScraperSourceTools.getSource(),Config.getSqlConditionOfChildMode(),0,LIMIT);
            emitter.onNext(movieDataViewList);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (mOnRefresh != null)
                            mOnRefresh.newSearch(movieDataViewList);
                    }
                });

    }

    private void loadFavoriteMovie() {
        Observable.create((ObservableOnSubscribe<List<MovieDataView>>) emitter -> {
            if ((mPage.get() + 1) * LIMIT < mTotal.get()) {
                int offset = mPage.incrementAndGet() * LIMIT;
                List<MovieDataView> movieDataViewList = mMovieDao.queryFavoriteMovieDataView(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), offset, LIMIT);
                emitter.onNext(movieDataViewList);
            }
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViewList) {
                        if (mOnRefresh != null)
                            mOnRefresh.appendMovieDataViews(movieDataViewList);
                    }
                });
    }

    private OnRefresh mOnRefresh;

    public void setOnRefresh(OnRefresh onRefresh) {
        mOnRefresh = onRefresh;
    }

    public interface OnRefresh {
        void newSearch(List<MovieDataView> newMovieDataView);

        void appendMovieDataViews(List<MovieDataView> movieDataViews);
    }
}
