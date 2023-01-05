package com.hphtv.movielibrary.util.rxjava;


import com.station.kit.util.LogUtil;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2021/6/8
 */
public abstract class SimpleObserver<T> implements Observer<T> {


    protected Disposable disposable;

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        RxJavaGcManager.getInstance().addDisposable(disposable);
    }

    @Override
    public void onNext(T t) {
        onAction(t);
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
    }

    /**
     * 下一步
     *
     * @param t 值
     */
    public abstract void onAction(T t);

}
