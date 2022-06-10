package com.hphtv.movielibrary.ui.homepage.fragment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * author: Sam Leung
 * date:  2022/6/10
 */
public interface ILoadingState {
    public AtomicInteger atomicState=new AtomicInteger();
    void startLoading();
    void finishLoading();
}
