package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView;
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
    private int mItemPosition = 0;
    private UnknownRootDataView mUnknownRootDataView;

    public UnknownsFileMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public Observable<MovieDataView> rematchMovieFile(MovieWrapper newWrapper) {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
                    VideoFile videoFile = mVideoFileDao.queryByVid(mUnknownRootDataView.connectedFileView.vid);
                    MovieHelper.addNewMovieInfo(getApplication(), newWrapper, videoFile);
                    MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(newWrapper.movie.movieId, newWrapper.movie.type.name(), ScraperSourceTools.getSource());
                    emitter.onNext(movieDataView);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MovieDataView> rematchMovieFile(MovieWrapper newWrapper,int season) {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
                    VideoFile videoFile = mVideoFileDao.queryByVid(mUnknownRootDataView.connectedFileView.vid);
                    videoFile.season=season;
                    MovieHelper.addNewMovieInfo(getApplication(), newWrapper, videoFile);
                    MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(newWrapper.movie.movieId, newWrapper.movie.type.name(), ScraperSourceTools.getSource());
                    emitter.onNext(movieDataView);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MovieDataView> rematchMovieFolder(MovieWrapper movieWrapper) {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
                    if (Constants.UnknownRootType.FOLDER.equals(mUnknownRootDataView.type)) {
                        String folder = mUnknownRootDataView.root;
                        List<VideoFile> videoFileList = mVideoFileDao.queryFileByFolder(folder + "%", folder + "%/%");
                        MovieHelper.manualSaveMovie(getApplication(), movieWrapper, videoFileList);
                        MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(movieWrapper.movie.movieId, movieWrapper.movie.type.name(), ScraperSourceTools.getSource());
                        emitter.onNext(movieDataView);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Exception());
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MovieDataView> rematchMovieFolder(MovieWrapper movieWrapper,int  season) {
        return Observable.create((ObservableOnSubscribe<com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView>) emitter -> {
                    if (Constants.UnknownRootType.FOLDER.equals(mUnknownRootDataView.type)) {
                        String folder = mUnknownRootDataView.root;
                        List<VideoFile> videoFileList = mVideoFileDao.queryFileByFolder(folder + "%", folder + "%/%");
                        for(VideoFile videoFile:videoFileList){
                            videoFile.season=season;
                        }
                        MovieHelper.manualSaveMovie(getApplication(), movieWrapper, videoFileList);
                        com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView movieDataView = mMovieDao.queryMovieDataViewByMovieId(movieWrapper.movie.movieId, movieWrapper.movie.type.name(), ScraperSourceTools.getSource());
                        emitter.onNext(movieDataView);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Exception());
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public int getItemPosition() {
        return mItemPosition;
    }

    public void setItemPosition(int itemPosition) {
        mItemPosition = itemPosition;
    }

    public UnknownRootDataView getUnknownRootDataView() {
        return mUnknownRootDataView;
    }

    public void setUnknownRootDataView(UnknownRootDataView unknownRootDataView) {
        mUnknownRootDataView = unknownRootDataView;
    }
}
