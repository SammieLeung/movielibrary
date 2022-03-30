package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.firefly.videonameparser.bean.Source;
import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideoTagCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieDataViewWithVdieoTags;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuViewModel extends BaseAndroidViewModel {
    private MovieDataView mMovieDataView;
    private MovieVideoTagCrossRefDao mMovieVideoTagCrossRefDao;
    private MovieVideofileCrossRefDao mMovieVideofileCrossRefDao;
    private MovieDao mMovieDao;

    private ObservableBoolean mMatchedFlag=new ObservableBoolean();

    private ObservableField<String> mAccessRights = new ObservableField<>();
    private ObservableBoolean mWatchedFlag = new ObservableBoolean(), mLikeFlag = new ObservableBoolean();
    private ObservableField<String> mTagString = new ObservableField<>();

    private boolean mRefreshFlag = false;

    public PosterMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieVideoTagCrossRefDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieVideoTagCrossRefDao();
        mMovieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieVideofileCrossRefDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    /**
     * 读取movieDataView信息
     *
     * @param movieDataView
     * @return
     */
    public Observable<MovieDataView> loadMovieProperty(MovieDataView movieDataView) {
        return Observable.just(movieDataView)
                .doOnNext(new Consumer<MovieDataView>() {
                    @Override
                    public void accept(MovieDataView movieDataView) throws Throwable {
                        mMatchedFlag.set(true);
                        mMovieDataView = movieDataView;
                        if (mMovieDataView.ap != null) {
                            switch (mMovieDataView.ap) {
                                case ALL_AGE:
                                    mAccessRights.set(getString(R.string.shortcut_access_all_ages));
                                    break;
                                case ADULT:
                                    mAccessRights.set(getString(R.string.shortcut_access_adult_only));
                                    break;
                            }
                        } else if (mMovieDataView.s_ap != null) {
                            switch (mMovieDataView.s_ap) {
                                case ALL_AGE:
                                    mAccessRights.set(getString(R.string.shortcut_access_all_ages));
                                    break;
                                case ADULT:
                                    mAccessRights.set(getString(R.string.shortcut_access_adult_only));
                                    break;
                            }
                        }
                        mWatchedFlag.set(movieDataView.is_watched);
                        mLikeFlag.set(movieDataView.is_favorite);

                        MovieDataViewWithVdieoTags movieDataViewWithVdieoTags = mMovieVideoTagCrossRefDao.queryMovieDataViewWithVideoTags(movieDataView.id);
                        StringBuffer stringBuffer = new StringBuffer();
                        for (VideoTag videoTag : movieDataViewWithVdieoTags.mVideoTagList) {
                            Constants.VideoType type = videoTag.tag;
                            switch (type) {
                                case movie:
                                    stringBuffer.append(getString(R.string.video_type_movie) + ",");
                                    break;
                                case tv:
                                    stringBuffer.append(getString(R.string.video_type_tv) + ",");
                                    break;
                                case child:
                                    stringBuffer.append(getString(R.string.video_type_cartoon) + ",");
                                    break;
                                case custom:
                                    stringBuffer.append(videoTag.tagName + ",");
                                    break;
                            }
                        }

                        mTagString.set(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 切换观看权限
     */
    public void toggleAccessRights() {
        Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            Constants.AccessPermission accessPermission = null;
            if (mMovieDataView.ap != null)
                accessPermission = mMovieDataView.ap;
            else
                accessPermission = mMovieDataView.s_ap;
            if (accessPermission.equals(Constants.AccessPermission.ADULT))
                accessPermission = Constants.AccessPermission.ALL_AGE;
            else
                accessPermission = Constants.AccessPermission.ADULT;
            mMovieDataView.ap = accessPermission;
            mMovieDao.updateAccessPermission(mMovieDataView.movie_id, mMovieDataView.ap);

            emitter.onNext(mMovieDataView);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.single())
                .subscribe(movieDataView -> {
                    switch (mMovieDataView.ap) {
                        case ALL_AGE:
                            mAccessRights.set(getString(R.string.shortcut_access_all_ages));
                            break;
                        case ADULT:
                            mAccessRights.set(getString(R.string.shortcut_access_adult_only));
                            break;
                    }
                    mRefreshFlag = true;
                });
    }

    public void toggleLike() {
        Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            mMovieDataView.is_favorite = !mMovieDataView.is_favorite;
            mMovieDao.updateFavoriteStateByMovieId(mMovieDataView.is_favorite, mMovieDataView.movie_id);
            OnlineDBApiService.updateLike(mMovieDataView.movie_id,mMovieDataView.is_favorite,ScraperSourceTools.getSource(),mMovieDataView.type.name());
            emitter.onNext(mMovieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .subscribe(movieDataView -> {
                    mLikeFlag.set(mMovieDataView.is_favorite);
                    mRefreshFlag = true;
                });
    }

    public Observable<MovieDataView> clearMovieInfo() {
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            mMatchedFlag.set(false);
            List<Movie> movieList = mMovieDao.queryByMovieId(mMovieDataView.movie_id);
            for (Movie movie : movieList) {
                mMovieVideofileCrossRefDao.deleteById(movie.id);
            }
            mMovieDao.updateFavoriteStateByMovieId(false, mMovieDataView.movie_id);//电影的收藏状态在删除时要设置为false
            OnlineDBApiService.deleteMovie(mMovieDataView.movie_id,ScraperSourceTools.getSource());
            mRefreshFlag = true;
            emitter.onNext(mMovieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public ObservableBoolean getMatchedFlag() {
        return mMatchedFlag;
    }

    public ObservableField<String> getAccessRights() {
        return mAccessRights;
    }

    public ObservableBoolean getWatchedFlag() {
        return mWatchedFlag;
    }

    public ObservableBoolean getLikeFlag() {
        return mLikeFlag;
    }

    public ObservableField<String> getTagString() {
        return mTagString;
    }
}
