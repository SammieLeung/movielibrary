package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.firefly.videonameparser.bean.Source;
import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.listener.OnMovieChangeListener;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideoTagCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.MovieVideofileCrossRefDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.reference.MovieVideoFileCrossRef;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieDataViewWithVdieoTags;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.MovieHelper;
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
    private VideoFileDao mVideoFileDao;

    private ObservableBoolean mMatchedFlag = new ObservableBoolean();

    private ObservableField<String> mAccessRights = new ObservableField<>();
    private ObservableBoolean mWatchedFlag = new ObservableBoolean(), mLikeFlag = new ObservableBoolean();
    private ObservableField<String> mTagString = new ObservableField<>();

    private boolean mRefreshFlag = false;
    private int mItemPosition=0;

    public PosterMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieVideoTagCrossRefDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieVideoTagCrossRefDao();
        mMovieVideofileCrossRefDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieVideofileCrossRefDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
        mVideoFileDao=MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
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
                            stringBuffer.append(videoTag.toTagName(getApplication())+",");
                        }

                        mTagString.set(stringBuffer.deleteCharAt(stringBuffer.length() - 1).toString());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<MovieDataView> reMatchMovie(MovieWrapper newWrapper){
        return Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            List<VideoFile> videoFileList=mVideoFileDao.queryVideoFilesById(mMovieDataView.id);
            MovieHelper.saveMatchedMovieWrapper(getApplication(),newWrapper,videoFileList);
            MovieDataView movieDataView=mMovieDao.queryMovieDataViewByMovieId(newWrapper.movie.movieId,newWrapper.movie.type.name(),ScraperSourceTools.getSource());
            emitter.onNext(movieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 切换观看权限
     */
    public void toggleAccessRights(OnMovieChangeListener listener) {
        Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            Constants.AccessPermission accessPermission = null;
            if (mMovieDataView.ap != null)
                accessPermission = mMovieDataView.ap;
            else
                accessPermission = mMovieDataView.s_ap;

//            boolean childMode=!Config.isTempCloseChildMode()&&Config.isChildMode();
            boolean childMode=Config.isChildMode();


            if (accessPermission.equals(Constants.AccessPermission.ADULT)) {
                accessPermission = Constants.AccessPermission.ALL_AGE;
                if(childMode)
                    listener.OnMovieInsert(mMovieDataView,mItemPosition);
            }else {
                accessPermission = Constants.AccessPermission.ADULT;
                if(childMode)
                    listener.OnMovieRemove(mMovieDataView.movie_id,mItemPosition);
            }
            mMovieDataView.ap = accessPermission;
            mMovieDao.updateAccessPermission(mMovieDataView.movie_id, mMovieDataView.ap);
            emitter.onNext(mMovieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
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

    public void toggleLike(OnMovieChangeListener listener) {
        Observable.create((ObservableOnSubscribe<MovieDataView>) emitter -> {
            mMovieDataView.is_favorite = !mMovieDataView.is_favorite;
            mMovieDao.updateFavoriteStateByMovieId(mMovieDataView.is_favorite, mMovieDataView.movie_id);
            OnlineDBApiService.updateLike(mMovieDataView.movie_id, mMovieDataView.is_favorite, ScraperSourceTools.getSource(), mMovieDataView.type.name());
            listener.OnMovieChange(mMovieDataView,mItemPosition);
            emitter.onNext(mMovieDataView);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single())
                .subscribe(movieDataView -> {
                    mLikeFlag.set(mMovieDataView.is_favorite);
                    mRefreshFlag = true;
                });
    }



    public MovieDataView getMovieDataView() {
        return mMovieDataView;
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

    public void setItemPosition(int itemPosition) {
        mItemPosition = itemPosition;
    }

    public boolean isRefreshFlag() {
        return mRefreshFlag;
    }

    public void setRefreshFlag(boolean refreshFlag) {
        mRefreshFlag = refreshFlag;
    }
}
