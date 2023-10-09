package com.hphtv.movielibrary.data.pagination;

import android.util.Log;

import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.orhanobut.logger.Logger;

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
    private final AtomicBoolean canLoadNext = new AtomicBoolean(true);
    private final AtomicBoolean canLoadPre = new AtomicBoolean(false);
    private final AtomicInteger mNextPage = new AtomicInteger(0);
    private final AtomicInteger mPrePage = new AtomicInteger(0);
    private Disposable mDisposable;


    public PaginatedDataLoader() {
        mLimit = getLimit();
        mFirstLimit = getFirstLimit();
    }

    public abstract int getLimit();

    public int getFirstLimit() {
        return getLimit();
    }

    protected List<T> reloadDataFromDB(int offset, int limit) {
        return loadDataFromDB(offset, limit);
    }

    protected abstract List<T> loadDataFromDB(int offset, int limit);

    protected void OnReloadResult(List<T> result) {
        OnLoadNextResult(result);
    }

    protected abstract void OnLoadNextResult(List<T> result);

    protected void OnLoadPreResult(List<T> result) {
    }

    protected void OnLoadFinish() {
    }

    public int getPage() {
        return mNextPage.get();
    }

    public boolean canLoadNext() {
        return canLoadNext.get();
    }

    public boolean canLoadPre() {
        return canLoadPre.get();
    }

    public void reset() {
        mNextPage.set(0);
        canLoadNext.set(true);
    }

    protected int nextOffset() {
        return mNextPage.getAndIncrement() * mLimit;
    }

    protected int preOffset() {
        return mPrePage.getAndDecrement() * mLimit;
    }


    public void cancel() {
        RxJavaGcManager.getInstance().disposableActive(mDisposable);
    }

    public void forceReload() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    int limit = mFirstLimit + (mNextPage.get()) * mLimit;
                    canLoadNext.set(true);
                    List<T> dataList = reloadDataFromDB(0, limit);
                    emitter.onNext(dataList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<T>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mDisposable = d;
                    }

                    @Override
                    public void onAction(List<T> dataList) {
                        OnReloadResult(dataList);
                        if (!canLoadNext()) {
                            OnLoadFinish();
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }

    public void forceReload(PaginationCallback callback) {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    int limit = mFirstLimit + (mNextPage.get()) * mLimit;
                    canLoadNext.set(true);
                    List<T> dataList = reloadDataFromDB(0, limit);
                    emitter.onNext(dataList);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<T>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mDisposable = d;
                    }

                    @Override
                    public void onAction(List<T> dataList) {
                        if (callback != null) {
                            callback.onResult(dataList);
                            if (!canLoadNext()) {
                                callback.loadFinish();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }

    public void reload() {
        reload(0);
    }

    public void reload(int pos) {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    int realPage = pos / mLimit;
                    mNextPage.set(realPage);
                    mPrePage.set(realPage-1);
                    canLoadNext.set(true);
                    canLoadPre.set(true);
                    if(realPage == 0) {
                        canLoadPre.set(false);
                        List<T> dataList = reloadDataFromDB(nextOffset(), mLimit * 3);
                        nextOffset();
                        nextOffset();
                        if (dataList.size() < mLimit * 3) {
                            canLoadNext.set(false);
                        }
                        emitter.onNext(dataList);
                        emitter.onComplete();
                    }else{
                        List<T> dataList = reloadDataFromDB(preOffset(), mLimit * 3);
                        nextOffset();
                        if (dataList.size() < mLimit * 3) {
                            canLoadNext.set(false);
                        }
                        emitter.onNext(dataList);
                        emitter.onComplete();
                    }

                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<T>>() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        mDisposable = d;
                    }

                    @Override
                    public void onAction(List<T> dataList) {
                        OnReloadResult(dataList);
                        if (!canLoadNext()) {
                            OnLoadFinish();
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }


//    public void reload() {
//        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
//                    mNextPage.set(0);
//                    canLoadNext.set(true);
//                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
//                    if (dataList.size() < mFirstLimit) {
//                        canLoadNext.set(false);
//                    }
//                    emitter.onNext(dataList);
//                    emitter.onComplete();
//                }).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleObserver<List<T>>() {
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        super.onSubscribe(d);
//                        mDisposable = d;
//                    }
//
//                    @Override
//                    public void onAction(List<T> dataList) {
//                        OnReloadResult(dataList);
//                        if (!canLoadMore()) {
//                            OnLoadFinish();
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                        mDisposable = null;
//                    }
//                });
//
//    }

    public void reload(PaginationCallback callback) {
//        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
//                    mNextPage.set(0);
//                    canLoadNext.set(true);
//                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
//                    if (dataList.size() < mFirstLimit) {
//                        canLoadNext.set(false);
//                    }
//                    emitter.onNext(dataList);
//                    emitter.onComplete();
//                }).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new SimpleObserver<List<T>>() {
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        super.onSubscribe(d);
//                        mDisposable = d;
//                    }
//
//                    @Override
//                    public void onAction(List<T> dataList) {
//                        if (callback != null) {
//                            callback.onResult(dataList);
//                            if (!canLoadNext()) {
//                                callback.loadFinish();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        super.onComplete();
//                        mDisposable = null;
//                    }
//                });

    }

    public void loadNext() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadNext()) {
                        List<T> dataList = loadDataFromDB(nextOffset(), mLimit);
                        if (dataList.size() < mLimit) {
                            canLoadNext.set(false);
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
                        OnLoadNextResult(dataList);
                        if (!canLoadNext()) {
                            OnLoadFinish();
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }

    public void loadNext(PaginationCallback callback) {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadNext()) {
                        List<T> dataList = loadDataFromDB(nextOffset(), mLimit);
                        if (dataList.size() < mLimit) {
                            canLoadNext.set(false);
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
                        if (callback != null) {
                            callback.onResult(dataList);
                            if (!canLoadNext()) {
                                callback.loadFinish();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }


    public void loadPre() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadPre()) {
                        int offset = preOffset();
                        if(offset<0) {
                            canLoadPre.set(false);
                        }else {
                            List<T> dataList = loadDataFromDB(offset, mLimit);
                            canLoadPre.set(mPrePage.get() >= 0);
                            emitter.onNext(dataList);
                        }
                    } else {
                        Log.w(TAG, "Unable to load pre data");
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
                        OnLoadPreResult(dataList);
                        if (!canLoadPre()) {
                            OnLoadFinish();
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mDisposable = null;
                    }
                });
    }
}
