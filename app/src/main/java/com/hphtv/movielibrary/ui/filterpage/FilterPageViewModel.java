package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
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
    public static final int LIMIT = 15;
    private AtomicInteger mPage = new AtomicInteger();
    private int mTotal = 0;
    private MovieDao mMovieDao;
    private Shortcut mShortcut;
    private VideoTag mVideoTag;
    private String mGenre,mYear;
    private int mOrder=0;
    private boolean isDesc=false;

    private OnRefresh mOnRefresh;

    public FilterPageViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public void reloadMoiveDataViews() {
         Observable.just("")
                .map(_offset -> {
                    mPage.set(0);
                    String dir_uri = null;
                    long vtid=-1;
                    if(mShortcut!=null)
                        dir_uri=mShortcut.uri;
                    if(mVideoTag!=null)
                        vtid=mVideoTag.vtid;
                    mTotal = mMovieDao.countMovieDataView(dir_uri,vtid,mGenre,mYear,ScraperSourceTools.getSource());
                    return mMovieDao.queryMovieDataView2(dir_uri,vtid,mGenre,mYear,mOrder,isDesc,ScraperSourceTools.getSource(),0,LIMIT);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new SimpleObserver<List<MovieDataView>>() {
                     @Override
                     public void onAction(List<MovieDataView> movieDataViews) {
                         if(mOnRefresh!=null)
                             mOnRefresh.newSearch(movieDataViews);
                     }
                 });
    }

    public int getTotal(){
        return mTotal;
    }


    public void loadMoiveDataViews() {
         Observable.just("")
                .map(_offset -> {
                    if((mPage.get()+1)*LIMIT<mTotal) {
                       int offset=mPage.incrementAndGet()*LIMIT;
                        String dir_uri = null;
                        long vtid=-1;
                        if(mShortcut!=null)
                            dir_uri=mShortcut.uri;
                        if(mVideoTag!=null)
                            vtid=mVideoTag.vtid;
                        return mMovieDao.queryMovieDataView2(dir_uri,vtid,mGenre,mYear,mOrder,isDesc,ScraperSourceTools.getSource(),offset,LIMIT);

//                        return mMovieDao.queryAllMovieDataView(ScraperSourceTools.getSource(), offset, LIMIT);
                    }else{
                        List<MovieDataView> emptyList=new ArrayList();
                        return emptyList;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(new SimpleObserver<List<MovieDataView>>() {
                     @Override
                     public void onAction(List<MovieDataView> movieDataViews) {
                         if(mOnRefresh!=null)
                             mOnRefresh.appendMovieDataViews(movieDataViews);
                     }
                 });
    }
    
    public void reOrderMovieDataViews(){
        Observable.just("")
                .map(_offset -> {
                        int limit=mPage.get()*LIMIT+LIMIT;
                        String dir_uri = null;
                        long vtid=-1;
                        if(mShortcut!=null)
                            dir_uri=mShortcut.uri;
                        if(mVideoTag!=null)
                            vtid=mVideoTag.vtid;
                        return mMovieDao.queryMovieDataView2(dir_uri,vtid,mGenre,mYear,mOrder,isDesc,ScraperSourceTools.getSource(),0,limit);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<MovieDataView>>() {
                    @Override
                    public void onAction(List<MovieDataView> movieDataViews) {
                        if(mOnRefresh!=null)
                            mOnRefresh.newSearch(movieDataViews);
                    }
                });
    }

    public void onFilterChange(Shortcut shortcut, VideoTag videoTag, String genre,String year){
        mShortcut=shortcut;
        mVideoTag=videoTag;
        mGenre=genre;
        mYear=year;
    }

    public void onSortByChange(int order,boolean isDesc){
        mOrder=order;
        this.isDesc=isDesc;
    }

    public interface OnRefresh{
        void newSearch(List<MovieDataView> newMovieDataView);
        void appendMovieDataViews(List<MovieDataView> movieDataViews);
    }

    public void setOnRefresh(OnRefresh onRefresh) {
        mOnRefresh = onRefresh;
    }

    public void setGenre(String genre){
        mGenre=genre;
    }

    public String getGenre(){
        return mGenre;
    }
}
