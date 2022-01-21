package com.hphtv.movielibrary;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.archos.filecorelibrary.filecorelibrary.jcifs.JcifsUtils;
import com.firefly.filepicker.utils.SambaAuthHelper;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.ui.homepage.HistoryFragmentViewModel;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.SharePreferencesTools;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieApplication extends Application {
    public static final boolean DEBUG = true;
    public static final String TAG = MovieApplication.class.getSimpleName();
    private boolean isShowEncrypted = false;
    private static MovieApplication sMovieApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sMovieApplication = this;
        init();
        initSambaAuthHelper();
    }

    private void init() {
        //友盟统计
        MobclickAgent.setScenarioType(sMovieApplication, MobclickAgent.EScenarioType.E_UM_NORMAL);
        Intent service = new Intent(sMovieApplication, DeviceMonitorService.class);
        startService(service);

    }


    private void initSambaAuthHelper() {
        Observable.just("")
                .observeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        JcifsUtils.getInstance(MovieApplication.this);
                        SambaAuthHelper.getInstance().init(MovieApplication.this);
                    }
                });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RxJavaGcManager.getInstance().clearDisposable();
    }


    public boolean isShowEncrypted() {
        return isShowEncrypted;
    }

    public void setShowEncrypted(boolean showEncrypted) {
        isShowEncrypted = showEncrypted;
    }

    public static MovieApplication getInstance() {
        return sMovieApplication;
    }

    public Observable<String> playingMovie(String path, String name) {
      return  Observable.just(path)
                .subscribeOn(Schedulers.io())
                //记录播放时间，作为播放记录
                .doOnNext(filepath -> {
                    VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(this).getVideoFileDao();
                    MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(this).getMovieDao();

                    long currentTime = System.currentTimeMillis();
                    videoFileDao.updateLastPlaytime(filepath, currentTime);
                    Movie movie = movieDao.queryByKeyword(filepath, ScraperSourceTools.getSource());
                    if (movie != null)
                        movieDao.updateLastPlaytime(movie.movieId, currentTime);
                    String poster = videoFileDao.getPoster(filepath,  ScraperSourceTools.getSource());
                    SharePreferencesTools.getInstance(this).saveProperty(Constants.SharePreferenceKeys.LAST_POTSER, poster);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(s -> VideoPlayTools.play(MovieApplication.this, path, name));
    }
}
