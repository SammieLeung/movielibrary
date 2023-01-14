package com.hphtv.movielibrary.util;

import android.util.Log;

import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/11/1
 */
public abstract class PaginatedDataLoader<T> {
    public static final String TAG = PaginatedDataLoader.class.getSimpleName();
    private final int mFirstLimit;
    private final int mLimit;
    private final AtomicBoolean mAtomicBooleanCanLoad = new AtomicBoolean(true);
    private final AtomicInteger mPage = new AtomicInteger(0);
    private Disposable mDisposable;


    public PaginatedDataLoader() {
        mLimit = getLimit();
        mFirstLimit = getFirstLimit();
    }

    public abstract int getLimit();

    public int getFirstLimit() {
        return getLimit();
    }

    ;

    protected List<T> reloadDataFromDB(int offset, int limit) {
        return loadDataFromDB(offset, limit);
    }

    protected abstract List<T> loadDataFromDB(int offset, int limit);

    protected void OnReloadResult(List<T> result) {
        OnLoadResult(result);
    }

    protected abstract void OnLoadResult(List<T> result);

    public int getPage() {
        return mPage.get();
    }

    public boolean canLoadMore() {
        return mAtomicBooleanCanLoad.get();
    }

    public void reset() {
        mPage.set(0);
        mAtomicBooleanCanLoad.set(true);
    }

    public void cancel(){
            RxJavaGcManager.getInstance().disposableActive(mDisposable);
    }


    public void reload() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    mPage.set(0);
                    mAtomicBooleanCanLoad.set(true);
                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
                    if (dataList.size() < mFirstLimit) {
                        mAtomicBooleanCanLoad.set(false);
                    }
                    emitter.onNext(dataList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<T>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mDisposable=d;
                    }

                    @Override
                    public void onAction(List<T> dataList) {
                        OnReloadResult(dataList);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable=null;
                    }
                });

    }

    public Observable<List<T>> rxReload() {
        return Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    mPage.set(0);
                    mAtomicBooleanCanLoad.set(true);
                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
                    if (dataList.size() < mFirstLimit) {
                        mAtomicBooleanCanLoad.set(false);
                    }
                    emitter.onNext(dataList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void load() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadMore()) {
                        int offset = mFirstLimit + (mPage.getAndIncrement()) * mLimit;

                        List<T> dataList = loadDataFromDB(offset, mLimit);
                        if (dataList.size() < mLimit) {
                            mAtomicBooleanCanLoad.set(false);
                        }
                        emitter.onNext(dataList);
                    } else {
                        Log.w(TAG, "Unable to load more data");
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<T>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mDisposable = d;
                    }
                    @Override
                    public void onAction(List<T> dataList) {
                        OnLoadResult(dataList);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                            mDisposable = null;
                    }
                });
    }

    public Observable<List<T>> rxLoad() {
        return Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadMore()) {
                        int offset = mFirstLimit + (mPage.getAndIncrement()) * mLimit;

                        List<T> dataList = loadDataFromDB(offset, mLimit);
                        if (dataList.size() < mLimit) {
                            mAtomicBooleanCanLoad.set(false);
                        }
                        emitter.onNext(dataList);
                    } else {
                        Log.w(TAG, "Unable to load more data");
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
