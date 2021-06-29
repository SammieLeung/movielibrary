package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.hphtv.movielibrary.viewmodel.HomepageViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/25
 */
public class UnrecognizeFileFragmentViewModel extends AndroidViewModel {
    public static final String UNRECOGNIZED_FILE = "unrecognizedFile";
    private ExecutorService mSingleThreadPool;
    private VideoFileDao mVideoFileDao;

    public UnrecognizeFileFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
    }

    public void prepareUnrecognizedFile(Callback callback) {
        Observable.just(UNRECOGNIZED_FILE)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(s -> {
                    List<UnrecognizedFileDataView> list = mVideoFileDao.queryUnrecognizedFiles();
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
        public void runOnUIThread(List<UnrecognizedFileDataView> unrecognizedFileDataViewList);
    }

}
