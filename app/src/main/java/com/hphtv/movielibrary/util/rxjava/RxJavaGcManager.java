package com.hphtv.movielibrary.util.rxjava;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author: Sam Leung
 * date:  2021/6/8
 */
public class RxJavaGcManager {
    private static RxJavaGcManager instance;
    private CompositeDisposable compositeDisposable;

    private RxJavaGcManager() {
    }

    public static RxJavaGcManager getInstance() {
        if (instance == null) {
            instance = new RxJavaGcManager();
        }
        return instance;
    }

    public void addDisposable(Disposable disposable) {
        if (compositeDisposable == null) {
            compositeDisposable
                    = new CompositeDisposable();
        }
        compositeDisposable.add(disposable);
    }

    private void deleteDisposable(Disposable disposable) {
        if (compositeDisposable != null) {
            compositeDisposable.delete(disposable);
        }
    }

    /**
     * 清除所有
     */
    public void clearDisposable() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
    }

    public void disposableActive(Disposable disposable) {
        synchronized (this) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                deleteDisposable(disposable);
                disposable = null;
            }
        }
    }

}
