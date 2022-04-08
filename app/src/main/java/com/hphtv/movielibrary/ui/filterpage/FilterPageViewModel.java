package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/2/22
 */
public class FilterPageViewModel extends BaseAndroidViewModel {
    public static final int EP_NOT_EMPTY = 0;
    public static final int EP_NO_MOVIE = 1;
    public static final int EP_NO_RESULT = 2;
    public static final int LIMIT = 15;
    private AtomicInteger mPage = new AtomicInteger();
    private MovieDao mMovieDao;
    private Shortcut mShortcut;
    private int mTotal;
    private int mTotalRow;
    private VideoTag mVideoTag;
    private String mGenre, mYear;
    private int mOrder = 0;
    private boolean isDesc = false;

    private OnRefresh mOnRefresh;

    private ObservableInt mEmptyType = new ObservableInt(EP_NO_MOVIE);
    private ObservableField<String> mConditionStr = new ObservableField<>();
    private ObservableField<String> mMovieCount = new ObservableField<>();
    private ObservableField<String> mRowStr = new ObservableField<>();

   private ObservableFloat mBottomMaskAphla=new ObservableFloat(1);


    public FilterPageViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public void reloadMoiveDataViews() {
        Observable.just("")
                .map(_offset -> {
                    mPage.set(0);
                    String dir_uri = null;
                    long vtid = -1;
                    if (mShortcut != null)
                        dir_uri = mShortcut.uri;
                    if (mVideoTag != null)
                        vtid = mVideoTag.vtid;
                    mTotal = mMovieDao.countMovieDataView(dir_uri, vtid, mGenre, mYear, Config.getSqlConditionOfChildMode(), ScraperSourceTools.getSource());
                    mTotalRow = (int) Math.ceil(1.0f * mTotal / 5);
                    checkEmpty(mTotal);
                    return mMovieDao.queryMovieDataView(dir_uri, vtid, mGenre, mYear, mOrder, Config.getSqlConditionOfChildMode(), isDesc, ScraperSourceTools.getSource(), 0, LIMIT);
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

    public void loadMoiveDataViews() {
        Observable.just("")
                .map(_offset -> {
                    if ((mPage.get() + 1) * LIMIT < mTotal) {
                        int offset = mPage.incrementAndGet() * LIMIT;
                        String dir_uri = null;
                        long vtid = -1;
                        if (mShortcut != null)
                            dir_uri = mShortcut.uri;
                        if (mVideoTag != null)
                            vtid = mVideoTag.vtid;
                        return mMovieDao.queryMovieDataView(dir_uri, vtid, mGenre, mYear, mOrder, Config.getSqlConditionOfChildMode(), isDesc, ScraperSourceTools.getSource(), offset, LIMIT);
                    } else {
                        List<MovieDataView> emptyList = new ArrayList();
                        return emptyList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if (mOnRefresh != null)
                            mOnRefresh.appendMovieDataViews(movieDataViews);
                    }
                });
    }

    public void reOrderMovieDataViews() {
        Observable.just("")
                .map(_offset -> {
                    int limit = mPage.get() * LIMIT + LIMIT;
                    String dir_uri = null;
                    long vtid = -1;
                    if (mShortcut != null)
                        dir_uri = mShortcut.uri;
                    if (mVideoTag != null)
                        vtid = mVideoTag.vtid;
                    return mMovieDao.queryMovieDataView(dir_uri, vtid, mGenre, mYear, mOrder, Config.getSqlConditionOfChildMode(), isDesc, ScraperSourceTools.getSource(), 0, limit);
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


    public void onFilterChange(Shortcut shortcut, VideoTag videoTag, String genre, String year) {
        mShortcut = shortcut;
        mVideoTag = videoTag;
        mYear = year;
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


    public void checkEmpty(int total) {
        if (!hasFilter() && total == 0)
            mEmptyType.set(EP_NO_MOVIE);
        else if (hasFilter() && total == 0)
            mEmptyType.set(EP_NO_RESULT);
        else {
            mEmptyType.set(EP_NOT_EMPTY);
//            refreshResultStr(mTotal);
        }
    }

    private void refreshGenre(){
        if(mGenre==null)
            mConditionStr.set(getString(R.string.all));
        else
            mConditionStr.set(mGenre);
    }

    private void refreshResultStr(int total) {
        char l_quote = '“';
        char r_quote = '”';
        char comma = '、';
        if (ScraperSourceTools.getSource().equals(Constants.Scraper.TMDB)) {
            l_quote = '"';
            r_quote = '"';
            comma = ',';
        }
        mMovieCount.set(String.valueOf(total));
        StringBuffer sb = new StringBuffer();
        sb.append(l_quote);

        if (mShortcut != null) {
            if (TextUtils.isEmpty(mShortcut.friendlyName))
                sb.append(mShortcut.name + comma);
            else
                sb.append(mShortcut.friendlyName + comma);
        }
        if (mVideoTag != null) {
            sb.append(mVideoTag.toTagName(getApplication()) + comma);
        }
        if (mGenre != null)
            sb.append(mGenre + comma);
        if (mYear != null)
            sb.append(mYear + comma);
        if (sb.length() == 1) {
            sb.append(getString(R.string.all) + r_quote);
        } else {
            sb.replace(sb.lastIndexOf(String.valueOf(comma)), sb.length(), String.valueOf(r_quote));
        }
        mConditionStr.set(sb.toString());
        mRowStr.set("1/" + mTotalRow);
    }

    public void setGenre(String genre) {
        mGenre = genre;
        refreshGenre();
    }

    public String getGenre() {
        return mGenre;
    }


    public ObservableInt getEmptyType() {
        return mEmptyType;
    }

    public ObservableField<String> getConditionStr() {
        return mConditionStr;
    }

    public ObservableField<String> getMovieCount() {
        return mMovieCount;
    }

    public ObservableField<String> getRowStr() {
        return mRowStr;
    }

    public ObservableFloat getBottomMaskAphla() {
        return mBottomMaskAphla;
    }

    public int decreaseTotal() {
        mTotalRow = (int) Math.ceil(1.0f * --mTotal / 5);
        return mTotal;
    }

    public int increaseTotal() {
        mTotalRow = (int) Math.ceil(1.0f * ++mTotal / 5);
        return mTotal;
    }

    public void refreshRowStr(int position) {
        mRowStr.set((position / 5 + 1) + "/" + mTotalRow);
    }
}
