package com.hphtv.movielibrary.ui;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Sam Leung
 * date:  2022/6/10
 */
public interface ILoadingState {
    void startLoading();
    void finishLoading();
}
