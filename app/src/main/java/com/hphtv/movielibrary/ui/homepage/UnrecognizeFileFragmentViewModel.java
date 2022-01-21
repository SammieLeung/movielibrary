package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/25
 */
public class UnrecognizeFileFragmentViewModel extends BaseAndroidViewModel {
    public static final String UNRECOGNIZED_FILE = "unrecognizedFile";
    private ExecutorService mSingleThreadPool;
    private VideoFileDao mVideoFileDao;
    private String mSource;

    public UnrecognizeFileFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mSource = ScraperSourceTools.getSource();
    }

    public Observable<List<VideoFile>> playingVideo(UnrecognizedFileDataView unrecognizedFileDataView) {
        return Observable.just(unrecognizedFileDataView)
                .observeOn(Schedulers.io())
                .map(unrecognizedFileDataView1 -> {
                    List<VideoFile> list = new ArrayList<>();
                    VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getVideoFileDao();
                    list.addAll(videoFileDao.queryVideoFileListByKeyword(unrecognizedFileDataView1.keyword, mSource));
                    return list;
                });
    }

    public void prepareUnrecognizedFile(Callback callback) {
        Observable.just(UNRECOGNIZED_FILE)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(s -> {
                    List<UnrecognizedFileDataView> list = mVideoFileDao.queryUnrecognizedFiles(mSource);
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<UnrecognizedFileDataView>>() {
                    @Override
                    public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViewList) {
                        callback.runOnUIThread(unrecognizedFileDataViewList);
                    }
                });
    }

    public interface Callback {
        void runOnUIThread(List<UnrecognizedFileDataView> unrecognizedFileDataViewList);
    }

}
