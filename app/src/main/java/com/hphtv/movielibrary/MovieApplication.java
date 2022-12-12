package com.hphtv.movielibrary;

import android.app.Application;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.archos.filecorelibrary.filecorelibrary.jcifs.JcifsUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.stetho.Stetho;
import com.firefly.filepicker.utils.SambaAuthHelper;
import com.hphtv.movielibrary.data.AuthHelper;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieUserFavoriteCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieUserFavoriteCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.respone.BaseRespone;
import com.hphtv.movielibrary.scraper.respone.GetUserFavoriteResponse;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.service.DeviceMonitorService;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.SharePreferencesTools;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Function3;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieApplication extends Application {
    private boolean isUpdatedUserFavorite;
    public static final boolean DEBUG = true;
    public static final String TAG = MovieApplication.class.getSimpleName();
    private boolean isShowEncrypted = false;
    private static MovieApplication sMovieApplication;
    private ConnectivityManager mConnectivityManager;

    private ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);

            ObservableSource<String> getCNAuthToken = observer -> {
                if (TextUtils.isEmpty(AuthHelper.sTokenCN)) {
                    AuthHelper.requestTokenCN();
                }
                observer.onNext(AuthHelper.sTokenCN);
                observer.onComplete();
            };

            ObservableSource<String> getEnAuthToken = observer -> {
                if (TextUtils.isEmpty(AuthHelper.sTokenEN)) {
                    AuthHelper.requestTokenEN();
                }
                observer.onNext(AuthHelper.sTokenEN);
                observer.onComplete();
            };

            Observable.zip(getCNAuthToken, getEnAuthToken, (cn, en) -> ScraperSourceTools.getSource()).subscribeOn(Schedulers.newThread())
                    .subscribe(new SimpleObserver<String>() {
                        @Override
                        public void onAction(String source) {
                            updateUserFavorites(source,1,10);
                        }
                    });
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sMovieApplication = this;
        init();
        registerNetworkCallback();
    }

    public void registerNetworkCallback() {
        if (mConnectivityManager == null)
            mConnectivityManager = getSystemService(ConnectivityManager.class);
        mConnectivityManager.registerDefaultNetworkCallback(mNetworkCallback);
    }

    public void unregisterNetworkCallback() {
        if (mConnectivityManager != null) {
            mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
            mConnectivityManager = null;
        }
    }

    private void init() {

        Observable.just("")
                .observeOn(Schedulers.newThread())
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                        //动图库
                        Fresco.initialize(getApplicationContext());
                        //初始化stetho
                        Stetho.initializeWithDefaults(getBaseContext());
                        //友盟统计
                        MobclickAgent.setScenarioType(sMovieApplication, MobclickAgent.EScenarioType.E_UM_NORMAL);
                        JcifsUtils.getInstance(MovieApplication.this);
                        SambaAuthHelper.getInstance().init(MovieApplication.this);

                        //设备TOKEN
//                        AuthHelper.init();
                    }
                });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        RxJavaGcManager.getInstance().clearDisposable();
        unregisterNetworkCallback();
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

    /**
     * 获取用户严选库收藏
     *
     * @param source
     */
    public void updateUserFavorites(String source, int page, int limit) {
        Observable.zip(Observable.just(source), Observable.just(page), Observable.just(limit),
                        OnlineDBApiService::getUserFavorites)
                .subscribeOn(Schedulers.newThread())
                .flatMap((Function<Observable<GetUserFavoriteResponse>, ObservableSource<GetUserFavoriteResponse>>) favoriteResponseObservable -> favoriteResponseObservable)
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "updateUserFavorites: "+throwable.getMessage() );
                    return new GetUserFavoriteResponse();
                })
                .subscribe(new SimpleObserver<GetUserFavoriteResponse>() {
                    @Override
                    public void onAction(GetUserFavoriteResponse getUserFavoriteResponse) {
                        MovieUserFavoriteCrossRefDao movieUserFavoriteCrossRefDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getMovieUserFavoriteCrossRefDao();
                        MovieDao movieDao = MovieLibraryRoomDatabase.getDatabase(getBaseContext()).getMovieDao();

                        List<MovieWrapper> movieList = getUserFavoriteResponse.toEntity();
                        int count=movieList.size();
                        if (count > 0) {
                            for (MovieWrapper movieWrapper : movieList) {
                                Movie movie = movieWrapper.movie;
                                if (movie != null) {
                                    MovieUserFavoriteCrossRef movieUserFavoriteCrossRef = movieUserFavoriteCrossRefDao.query(movie.movieId, movie.type.name(), movie.source);
                                    if (movieUserFavoriteCrossRef == null) {
                                        if (movieDao.queryByMovieIdAndType(movie.movieId, movie.source, movie.type.name()) == null) {
                                            MovieHelper.saveBaseInfo(getBaseContext(), movieWrapper);
                                        }
                                        movieUserFavoriteCrossRef = new MovieUserFavoriteCrossRef();
                                        movieUserFavoriteCrossRef.movie_id = movie.movieId;
                                        movieUserFavoriteCrossRef.source = movie.source;
                                        movieUserFavoriteCrossRef.type = movie.type;
                                        movieUserFavoriteCrossRefDao.insertOrIgnore(movieUserFavoriteCrossRef);
                                    }
                                }
                            }
                            Intent intent=new Intent(Constants.ACTION_APPEND_USER_FAVORITE);
                            intent.putExtra("count",page-1*limit+count);
                            LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                            if (movieList.size() == limit)
                                updateUserFavorites(source, page + 1, limit);
                        }
                    }
                });
    }

}
