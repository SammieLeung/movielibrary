package com.hphtv.movielibrary.data.pagination;

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
    private final AtomicBoolean canLoadNext = new AtomicBoolean(true);
    private final AtomicBoolean canLoadPre=new AtomicBoolean(false);
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

    protected List<T> reloadDataFromDB(int offset, int limit) {
        return loadDataFromDB(offset, limit);
    }

    protected abstract List<T> loadDataFromDB(int offset, int limit);

    protected void OnReloadResult(List<T> result) {
        OnLoadNextResult(result);
    }

    protected abstract void OnLoadNextResult(List<T> result);
    protected void OnLoadPreResult(List<T> result){}

    protected void OnLoadFinish() {
    }

    public int getPage() {
        return mPage.get();
    }

    public boolean canLoadMore() {
        return canLoadNext.get();
    }

    public void reset() {
        mPage.set(0);
        canLoadNext.set(true);
    }

    protected int nextOffset(){
        return mFirstLimit + (mPage.getAndIncrement()) * mLimit;
    }



    public void cancel() {
        RxJavaGcManager.getInstance().disposableActive(mDisposable);
    }
    public void forceReload() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    int limit = mFirstLimit + (mPage.get()) * mLimit;
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
                        if (!canLoadMore()) {
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
                    int limit = mFirstLimit + (mPage.get()) * mLimit;
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
                            if (!canLoadMore()) {
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
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    mPage.set(0);
                    canLoadNext.set(true);
                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
                    if (dataList.size() < mFirstLimit) {
                        canLoadNext.set(false);
                    }
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
                        if (!canLoadMore()) {
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

    public void reload(PaginationCallback callback) {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    mPage.set(0);
                    canLoadNext.set(true);
                    List<T> dataList = reloadDataFromDB(0, mFirstLimit);
                    if (dataList.size() < mFirstLimit) {
                        canLoadNext.set(false);
                    }
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
                            if (!canLoadMore()) {
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

    public void loadNext() {
        Observable.create((ObservableOnSubscribe<List<T>>) emitter -> {
                    if (canLoadMore()) {
//                        int offset = mFirstLimit + (mPage.getAndIncrement()) * mLimit;
                        int offset=nextOffset();
                        List<T> dataList = loadDataFromDB(offset, mLimit);
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
                        if (!canLoadMore()) {
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
                    if (canLoadMore()) {
//                        int offset = mFirstLimit + (mPage.getAndIncrement()) * mLimit;
                        int offset=nextOffset();
                        List<T> dataList = loadDataFromDB(offset, mLimit);
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
                            if (!canLoadMore()) {
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

    public void loadPre(){

    }

    public void loadPre(PaginationCallback callback){

    }
}
