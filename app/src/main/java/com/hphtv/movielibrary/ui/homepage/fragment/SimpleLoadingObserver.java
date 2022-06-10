package com.hphtv.movielibrary.ui.homepage.fragment;


import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.hphtv.movielibrary.util.rxjava.RxJavaGcManager;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2021/6/8
 */
public abstract class SimpleLoadingObserver<T> implements Observer<T> {
    public ILoadingState mLoadingState;

    public SimpleLoadingObserver(ILoadingState loadingState) {
        mLoadingState=loadingState;
    }

    private Disposable disposable;

    @Override
    public void onSubscribe(Disposable d) {
        disposable = d;
        RxJavaGcManager.getInstance().addDisposable(disposable);
        if (mLoadingState != null)
            mLoadingState.startLoading();
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
        if (mLoadingState != null)
            mLoadingState.finishLoading();
    }

    /**
     * 下一步
     *
     * @param t 值
     */
    public abstract void onAction(T t);

}
