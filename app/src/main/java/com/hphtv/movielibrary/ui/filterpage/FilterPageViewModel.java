package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.PaginatedDataLoader;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageViewModel extends BaseAndroidViewModel {
    public static final String TAG = FilterPageViewModel.class.getSimpleName();

    public static final int EP_NOT_EMPTY = 0;
    public static final int EP_NO_MOVIE = 1;
    public static final int EP_NO_RESULT = 2;
    public static final int LIMIT = 15;

    public static final int DEFAULT_ORDER = 3;
    private MovieDao mMovieDao;
    private VideoTagDao mVideoTagDao;
    private Shortcut mShortcut;
    private VideoTag mVideoTag;
    private String mGenre, mYear, mYear2;
    private int mOrder = DEFAULT_ORDER;
    private boolean isDesc = false;

    private OnRefresh mOnRefresh;

    private ObservableInt mEmptyType = new ObservableInt(EP_NO_MOVIE);
    private ObservableField<String> mConditionStr = new ObservableField<>();

    private List<MovieDataView> mMovieDataViews = new ArrayList<>();


    public FilterPageViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
        mVideoTagDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoTagDao();
    }

    public void reloadMovieDataViews() {
        mMovieDataViewPaginatedDataLoader.reload();
    }

    public void loadMovieDataViews() {
        mMovieDataViewPaginatedDataLoader.load();
    }

    public void reOrderMovieDataViews() {
        Observable.just("")
                .map(_offset -> {
                    int page = mMovieDataViewPaginatedDataLoader.getPage();
                    int limit=mMovieDataViewPaginatedDataLoader.getFirstLimit()+page*mMovieDataViewPaginatedDataLoader.getLimit();
                    String dir_uri = null;
                    long vtid = -1;
                    if (mShortcut != null)
                        dir_uri = mShortcut.uri;
                    if (mVideoTag != null)
                        vtid = mVideoTag.vtid;
                    return mMovieDao.queryMovieDataView(dir_uri, vtid, mGenre, mYear, mYear2, mOrder, Config.getSqlConditionOfChildMode(), isDesc, ScraperSourceTools.getSource(), 0, limit);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (mOnRefresh != null)
                            mOnRefresh.newSearch(movieDataViews);
                    }
                });
    }

    public Observable<MovieDataView> getUpdatingFavorite(String movie_id, String type) {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
                    MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(movie_id, type, ScraperSourceTools.getSource());
                    emitter.onNext(movieDataView);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public void onFilterChange(Shortcut shortcut, VideoTag videoTag, String genre, String year) {
        mShortcut = shortcut;
        mVideoTag = videoTag;
        resolveYear(year);
        setGenre(genre);
    }

    public void onSortByChange(int order, boolean isDesc) {
        mOrder = order;
        this.isDesc = isDesc;
    }

    public interface OnRefresh {
        void newSearch(List<MovieDataView> newMovieDataView);

        void appendMovieDataViews(List<MovieDataView> movieDataViews);
    }

    public void setOnRefresh(OnRefresh onRefresh) {
        mOnRefresh = onRefresh;
    }


    public boolean hasFilter() {
        if (mShortcut == null && mVideoTag == null && mGenre == null && mYear == null)
            return false;
        return true;
    }


    public void checkEmpty() {
        int total = mMovieDataViews.size();
        if (!hasFilter() && total == 0)
            mEmptyType.set(EP_NO_MOVIE);
        else if (hasFilter() && total == 0)
            mEmptyType.set(EP_NO_RESULT);
        else {
            mEmptyType.set(EP_NOT_EMPTY);
        }
    }

    private void refreshGenre() {
        if (mGenre == null)
            mConditionStr.set(getString(R.string.all));
        else
            mConditionStr.set(mGenre);
    }

    private void refreshGenreAndVideoTag() {
        if (mGenre == null)
            mConditionStr.set(getString(R.string.all) + " (" + mVideoTag.toTagName(getApplication()) + ")");
        else
            mConditionStr.set(mGenre + " (" + mVideoTag.toTagName(getApplication()) + ")");
    }

    public void setGenre(String genre) {
        mGenre = genre;
        refreshGenre();
    }

    public void resolveYear(String yearStr) {
        if (yearStr == null) {
            mYear = null;
            mYear2 = null;
        } else if (NumberUtils.isDigits(yearStr)) {
            mYear = yearStr;
            mYear2 = null;
        } else if (getString(R.string.year_earlier).equalsIgnoreCase(yearStr)) {
            mYear2 = "1979";
            mYear = null;
        } else if (yearStr.contains("-")) {
            String[] years = yearStr.split("-");
            mYear = years[0].trim();
            mYear2 = years[1].trim();
        } else if (getString(R.string.year_90s).equals(yearStr)) {
            mYear = "1990";
            mYear2 = "1999";
        } else if (getString(R.string.year_80s).equals(yearStr)) {
            mYear = "1980";
            mYear2 = "1989";
        }
    }

    public void setGenreAndVideoTag(String genre, String tag) {
        Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .doOnNext(s -> {
                    mGenre = genre;
                    mVideoTag = mVideoTagDao.queryVtidByNormalTag(tag);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        refreshGenreAndVideoTag();
                        reloadMovieDataViews();
                    }
                });
    }

    public String getGenre() {
        return mGenre;
    }

    public VideoTag getVideoTag() {
        return mVideoTag;
    }

    public ObservableInt getEmptyType() {
        return mEmptyType;
    }

    public ObservableField<String> getConditionStr() {
        return mConditionStr;
    }

    public List<MovieDataView> getMovieDataViews() {
        return mMovieDataViews;
    }

    private PaginatedDataLoader<MovieDataView> mMovieDataViewPaginatedDataLoader=new PaginatedDataLoader<MovieDataView>() {
        @Override
        public int getLimit() {
            return 10;
        }

        @Override
        public int getFirstLimit() {
            return 15;
        }

        @Override
        protected List<MovieDataView> loadDataFromDB(int offset, int limit) {
            String dir_uri = null;
            long vtid = -1;
            if (mShortcut != null)
                dir_uri = mShortcut.uri;
            if (mVideoTag != null)
                vtid = mVideoTag.vtid;
            List<MovieDataView> dataViews = mMovieDao.queryMovieDataView(dir_uri, vtid, mGenre, mYear, mYear2, mOrder, Config.getSqlConditionOfChildMode(), isDesc, ScraperSourceTools.getSource(), offset, limit);
            return dataViews;
        }

        @Override
        protected void OnReloadResult(List<MovieDataView> result) {
            if (mOnRefresh != null) {
                mOnRefresh.newSearch(result);
                checkEmpty();
            }
        }

        @Override
        protected void OnLoadResult(List<MovieDataView> result) {
            if (mOnRefresh != null)
                mOnRefresh.appendMovieDataViews(result);
        }
    };
}
