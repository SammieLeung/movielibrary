package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.PaginatedDataLoader;
import com.hphtv.movielibrary.util.ScraperSourceTools;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */
public class UnknowFileViewModel extends BaseAndroidViewModel {
    public static final int TYPE_FOLDER=1;
    public static final int TYPE_FILE=2;
    public static final int TYPE_BACK=-1;
    private VideoFileDao mVideoFileDao;


    private boolean isPlayingVideo=false;

    private List<ConnectedFileDataView> mConnectedFileDataViews;

    public UnknowFileViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mConnectedFileDataViews = new ArrayList<>();

    }

    public Observable<List<ConnectedFileDataView>> reLoadUnknownFiles() {
        return mDataLoader.rxReload();
    }

    public Observable<List<ConnectedFileDataView>> loadMoreUnknownFiles() {
      return mDataLoader.rxLoad();
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

    private PaginatedDataLoader<ConnectedFileDataView> mDataLoader=new PaginatedDataLoader<ConnectedFileDataView>() {
        @Override
        public int getLimit() {
            return 10;
        }

        @Override
        public int getFirstLimit() {
            return 15;
        }

        @Override
        protected List<ConnectedFileDataView> loadDataFromDB(int offset, int limit) {
            return mVideoFileDao.queryUnrecognizedFiles(ScraperSourceTools.getSource(), offset, limit);
        }

        @Override
        protected void OnLoadResult(List<ConnectedFileDataView> result) {

        }
    };
}
