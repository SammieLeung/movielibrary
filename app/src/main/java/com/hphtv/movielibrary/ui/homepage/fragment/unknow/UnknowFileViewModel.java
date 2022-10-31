package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknowFileViewModel extends BaseAndroidViewModel {
    public static final int LIMIT = 15;
    private VideoFileDao mVideoFileDao;
    private ExecutorService mSingleThreadPool;
    private AtomicInteger mPage = new AtomicInteger();
    private AtomicInteger mTotal = new AtomicInteger();

    private boolean isPlayingVideo=false;

    private List<ConnectedFileDataView> mConnectedFileDataViews;

    public UnknowFileViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mConnectedFileDataViews = new ArrayList<>();

    }

    public Observable<List<ConnectedFileDataView>> reLoadUnknownFiles() {
        return Observable.create((ObservableOnSubscribe<List<ConnectedFileDataView>>) emitter -> {
                    mPage.set(0);
                    mTotal.set(mVideoFileDao.countUnrecognizedFiles(ScraperSourceTools.getSource()));
                    List<ConnectedFileDataView> list = mVideoFileDao.queryUnrecognizedFiles(ScraperSourceTools.getSource(), 0, LIMIT);
                    emitter.onNext(list);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<List<ConnectedFileDataView>> loadMoreUnknowFiles() {
      return  Observable.create((ObservableOnSubscribe<List<ConnectedFileDataView>>) emitter -> {
            if ((mPage.get() + 1) * LIMIT < mTotal.get()) {
                int offset = mPage.incrementAndGet() * LIMIT;
                emitter.onNext(mVideoFileDao.queryUnrecognizedFiles(ScraperSourceTools.getSource(), offset, LIMIT));
            }
            emitter.onComplete();
        }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<String> playVideo(String path, String name) {
        return MovieHelper.playingMovie(path, name);
    }

    public List<ConnectedFileDataView> getUnrecognizedFileDataViewList() {
        return mConnectedFileDataViews;
    }

    public boolean isPlayingVideo() {
        return isPlayingVideo;
    }

    public void setPlayingVideo(boolean playingVideo) {
        isPlayingVideo = playingVideo;
    }
}
