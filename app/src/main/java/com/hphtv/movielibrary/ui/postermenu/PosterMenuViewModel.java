package com.hphtv.movielibrary.ui.postermenu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.MovieVideoTagCrossRefDao;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieDataViewWithVdieoTags;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/25
 */
public class PosterMenuViewModel extends BaseAndroidViewModel {
    private MovieDataView mMovieDataView;
    private MovieVideoTagCrossRefDao mMovieVideoTagCrossRefDao;

    private ObservableField<String> mAccessRights = new ObservableField<>();
    private ObservableBoolean mWatchedFlag = new ObservableBoolean(), mLikeFlag = new ObservableBoolean();
    private ObservableField<String> mTagString = new ObservableField<>();

    public PosterMenuViewModel(@NonNull @NotNull Application application) {
        super(application);
        mMovieVideoTagCrossRefDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieVideoTagCrossRefDao();
    }

    public Observable<MovieDataView> loadMovieProperty(MovieDataView movieDataView) {
       return Observable.just(movieDataView)
                .doOnNext(new Consumer<MovieDataView>() {
                    @Override
                    public void accept(MovieDataView movieDataView) throws Throwable {
                        mMovieDataView = movieDataView;
                        if(mMovieDataView.ap!=null) {
                            switch (mMovieDataView.ap) {
                                case ALL_AGE:
                                    mAccessRights.set(getString(R.string.shortcut_access_all_ages));
                                    break;
                                case ADULT:
                                    mAccessRights.set(getString(R.string.shortcut_access_adult_only));
                                    break;
                            }
                        }else if(mMovieDataView.s_ap!=null){
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

                        mTagString.set(stringBuffer.deleteCharAt(stringBuffer.length()-1).toString());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
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
