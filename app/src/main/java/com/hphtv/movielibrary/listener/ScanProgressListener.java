package com.hphtv.movielibrary.listener;

import android.support.annotation.Nullable;

import com.hphtv.movielibrary.sqlite.bean.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.others.ParseFile;

/**
 * @author lxp
 * @date 19-5-9
 */
public interface ScanProgressListener {
    public void onStart();
    public void onAddToScan(ParseFile parseFile);
    public void onGetFile();
    public void onSuccess(ParseFile parseFile,@Nullable MovieWrapper movieWrapper);
    public void onFailed(ParseFile parseFile, @Nullable MovieWrapper movieWrapper);
    public void onFinish();

}
