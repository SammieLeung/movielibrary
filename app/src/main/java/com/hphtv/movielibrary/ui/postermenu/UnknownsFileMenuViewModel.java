package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/6
 */
public class UnknownsFileMenuViewModel extends BaseAndroidViewModel {
    private VideoFileDao mVideoFileDao;
    private MovieDao mMovieDao;
    private int mItemPosition=0;
    private UnrecognizedFileDataView mUnrecognizedFileDataView;
    public UnknownsFileMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao= MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mMovieDao=MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public Observable<MovieDataView> reMatchMovie(MovieWrapper newWrapper){
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            VideoFile videoFile=mVideoFileDao.queryByVid(mUnrecognizedFileDataView.vid);
            MovieHelper.saveMovieWrapper(getApplication(),newWrapper,videoFile);
            MovieDataView movieDataView=mMovieDao.queryMovieDataViewByMovieId(newWrapper.movie.movieId,newWrapper.movie.type.name(), ScraperSourceTools.getSource());
            emitter.onNext(movieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public int getItemPosition() {
        return mItemPosition;
    }

    public void setItemPosition(int itemPosition) {
        mItemPosition = itemPosition;
    }

    public UnrecognizedFileDataView getUnrecognizedFileDataView() {
        return mUnrecognizedFileDataView;
    }

    public void setUnrecognizedFileDataView(UnrecognizedFileDataView unrecognizedFileDataView) {
        mUnrecognizedFileDataView = unrecognizedFileDataView;
    }
}
