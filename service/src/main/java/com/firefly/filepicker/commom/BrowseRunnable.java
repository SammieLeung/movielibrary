package com.firefly.filepicker.commom;

import com.firefly.filepicker.data.Constants;

import org.fourthline.cling.support.contentdirectory.callback.Browse;

/**
 * Created by rany on 18-1-19.
 */

public class BrowseRunnable implements Runnable {
    private Browse mBrowse;

    public BrowseRunnable(Browse browse) {
        mBrowse = browse;
    }

    @Override
    public void run() {
        Constants.upnpService.getControlPoint().execute(mBrowse);
    }
}