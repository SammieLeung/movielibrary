package com.hphtv.movielibrary.ui.homepage.fragment.unknown;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.data.pagination.PaginationCallback;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnknownRootDataView;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.data.pagination.PaginatedDataLoader;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/4/2
 */

public class UnknownFileViewModel extends BaseAndroidViewModel {
    public static final String ROOT = "root";
    public static final String TAG = UnknownRootDataView.class.getSimpleName();
    public static final int LIMIT = 12;
    public static final int TYPE_FOLDER = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_BACK = -1;


    private VideoFileDao mVideoFileDao;


    private boolean isPlayingVideo = false;

    private List<UnknownRootDataView> mUnknownRootDataViews;
    private Stack<String> mParentStack = new Stack<>();

    private AtomicInteger mOffset = new AtomicInteger(0);
    private AtomicBoolean mAtomicBooleanCanLoad = new AtomicBoolean(true);
    private String mCurrentPath = ROOT;
    private boolean isLoadRoot = true;

    public UnknownFileViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mUnknownRootDataViews = new ArrayList<>();
    }

    public void reloadUnknownRoots(String root, PaginationCallback callback) {
        if (ROOT.equals(root)) {
            isLoadRoot = true;
            mParentStack.clear();
            mCurrentPath = root;
            mRootLoader.reload(callback);
        } else {
            isLoadRoot = false;
             reLoadUnknownFiles(root,callback);
        }
    }

    public void loadMoreUnknownRoots(PaginationCallback callback) {
        if (isLoadRoot)
             mRootLoader.loadNext(callback);
        else
             loadMoreUnknownFiles(callback);
    }

    private void reLoadUnknownFiles(String path,PaginationCallback callback) {
         Observable.create((ObservableOnSubscribe<List<UnknownRootDataView>>) emitter -> {
                    mOffset.set(0);
                    mAtomicBooleanCanLoad.set(true);
                    if (mParentStack.isEmpty() || !Objects.equals(mParentStack.peek(), path)) {
                        mParentStack.push(mCurrentPath);
                        mCurrentPath = path;
                    }
                    int limit = LIMIT - 1;
                    List<UnknownRootDataView> rootDataViewList = new ArrayList<>();
                    UnknownRootDataView rootDataView = new UnknownRootDataView();
                    rootDataView.root = getString(R.string.goback);
                    rootDataView.type = Constants.UnknownRootType.BACK;
                    rootDataViewList.add(rootDataView);

                    List<ConnectedFileDataView> connectedFileDataViews = mVideoFileDao.queryConnectedFileDataViewByParentPath(mCurrentPath + "%", mCurrentPath + "%/%", Config.getSqlConditionOfChildMode(), mOffset.get(), limit);
                    if (connectedFileDataViews.size() < limit) {
                        //设置数据无法读取标记
                        mAtomicBooleanCanLoad.set(false);
                    }

                    for (ConnectedFileDataView v : connectedFileDataViews) {
                        UnknownRootDataView dataDataView = new UnknownRootDataView();
                        dataDataView.type = Constants.UnknownRootType.FILE;
                        dataDataView.root = v.path;
                        dataDataView.s_ap = v.s_ap;
                        dataDataView.connectedFileView = v;
                        rootDataViewList.add(dataDataView);
                    }

                    mOffset.set(mOffset.get() + connectedFileDataViews.size());

                    emitter.onNext(rootDataViewList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<UnknownRootDataView>>() {
                    @Override
                    public void onAction(List<UnknownRootDataView> unknownRootDataViews) {
                        callback.onResult(unknownRootDataViews);
                    }
                });

    }

    private void loadMoreUnknownFiles(PaginationCallback callback) {
        Observable.create((ObservableOnSubscribe<List<UnknownRootDataView>>) emitter -> {
                    if (mAtomicBooleanCanLoad.get()) {
                        List<UnknownRootDataView> rootDataViewList = new ArrayList<>();
                        List<ConnectedFileDataView> connectedFileDataViews = mVideoFileDao.queryConnectedFileDataViewByParentPath(mCurrentPath + "%", mCurrentPath + "%/%", Config.getSqlConditionOfChildMode(), mOffset.get(), LIMIT);
                        if (connectedFileDataViews.size() < LIMIT) {
                            //设置数据无法读取标记
                            mAtomicBooleanCanLoad.set(false);
                        }

                        for (ConnectedFileDataView v : connectedFileDataViews) {
                            UnknownRootDataView dataDataView = new UnknownRootDataView();
                            dataDataView.type = Constants.UnknownRootType.FILE;
                            dataDataView.root = v.path;
                            dataDataView.s_ap = v.s_ap;
                            dataDataView.connectedFileView = v;
                            rootDataViewList.add(dataDataView);
                        }

                        mOffset.set(mOffset.get() + connectedFileDataViews.size());
                        emitter.onNext(rootDataViewList);
                    } else {
                        Log.w(TAG, "Unable to load more data");
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<UnknownRootDataView>>() {
                    @Override
                    public void onAction(List<UnknownRootDataView> unknownRootDataViews) {
                        callback.onResult(unknownRootDataViews);
                        if (!mAtomicBooleanCanLoad.get()) {
                            callback.loadFinish();
                        }
                    }
                });
    }


    public Observable<String> playVideo(String path) {
        String name = path.substring(path.lastIndexOf("/") + 1);
        return MovieHelper.playingMovie(path, name);
    }

    public List<UnknownRootDataView> getUnknownRootDataViews() {
        return mUnknownRootDataViews;
    }

    public boolean isPlayingVideo() {
        return isPlayingVideo;
    }

    public void setPlayingVideo(boolean playingVideo) {
        isPlayingVideo = playingVideo;
    }

    public String pop() {
        if (mParentStack != null && !mParentStack.empty())
            return mParentStack.pop();
        return null;
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }

    private PaginatedDataLoader<UnknownRootDataView> mRootLoader = new PaginatedDataLoader<UnknownRootDataView>() {
        @Override
        public int getLimit() {
            return LIMIT;
        }

        @Override
        protected List<UnknownRootDataView> loadDataFromDB(int offset, int limit) {
            List<UnknownRootDataView> rootDataViewList = mVideoFileDao.queryUnknownRoot(Config.getSqlConditionOfChildMode(), null, offset, limit);
            for (UnknownRootDataView rootDataView : rootDataViewList) {
                if (rootDataView.type == Constants.UnknownRootType.FILE) {
                    rootDataView.connectedFileView = mVideoFileDao.queryConnectedFileDataViewByPath(rootDataView.root);
                }
            }
            return rootDataViewList;
        }

        @Override
        protected void OnLoadNextResult(List<UnknownRootDataView> result) {

        }

        @Override
        protected void OnLoadFinish() {
            super.OnLoadFinish();
        }
    };

}
