package com.hphtv.movielibrary.ui.homepage.fragment.unknow;

import android.app.Application;

import androidx.annotation.NonNull;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.adapter.UnknowFileItemListAdapter;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
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
    private UnknowFileItemListAdapter mUnknowsFileItemListAdapter;

    private List<UnrecognizedFileDataView> mUnrecognizedFileDataViews;

    public UnknowFileViewModel(@NonNull @NotNull Application application) {
        super(application);
        mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoFileDao();
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mUnrecognizedFileDataViews = new ArrayList<>();
        initRvAdapter();

    }

    private void initRvAdapter() {
        mUnknowsFileItemListAdapter = new UnknowFileItemListAdapter(getApplication(), mUnrecognizedFileDataViews);
    }

    public void reLoadUnknowFiles() {
        Observable.create((ObservableOnSubscribe<List<UnrecognizedFileDataView>>) emitter -> {
            mPage.set(0);
            mTotal.set(mVideoFileDao.countUnrecognizedFiles(ScraperSourceTools.getSource()));
            List<UnrecognizedFileDataView> list = mVideoFileDao.queryUnrecognizedFiles(ScraperSourceTools.getSource(), 0, LIMIT);
            emitter.onNext(list);
            emitter.onComplete();
        }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<UnrecognizedFileDataView>>() {
                    @Override
                    public void onAction(List<UnrecognizedFileDataView> unrecognizedFileDataViews) {
                        mUnknowsFileItemListAdapter.addAll(unrecognizedFileDataViews);
                    }
                });


    }

    public void loadMoreUnknowFiles() {
        Observable.create(new ObservableOnSubscribe<List<UnrecognizedFileDataView>>() {
            @Override
            public void subscribe(@io.reactivex.rxjava3.annotations.NonNull ObservableEmitter<List<UnrecognizedFileDataView>> emitter) throws Throwable {
                if ((mPage.get() + 1) * LIMIT < mTotal.get()) {
                    int offset = mPage.incrementAndGet() * LIMIT;
                    emitter.onNext(mVideoFileDao.queryUnrecognizedFiles(ScraperSourceTools.getSource(), offset, LIMIT));
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unrecognizedFileDataViews -> {
                    mUnknowsFileItemListAdapter.appendAll(unrecognizedFileDataViews);
                });
    }

    public Observable<String> playVideo(String path, String name) {
        return MovieHelper.playingMovie(path,name);
    }

    public int increaseTotal() {
        return mTotal.incrementAndGet();
    }

    public int decreaseTotal() {
        return mTotal.decrementAndGet();
    }

    public UnknowFileItemListAdapter getUnknownsFileItemListAdapter() {
        return mUnknowsFileItemListAdapter;
    }

    public List<UnrecognizedFileDataView> getUnrecognizedFileDataViewList() {
        return mUnrecognizedFileDataViews;
    }
}
