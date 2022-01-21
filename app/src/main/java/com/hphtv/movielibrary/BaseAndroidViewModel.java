package com.hphtv.movielibrary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.roomdb.entity.Movie;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/21
 */
public class BaseAndroidViewModel extends AndroidViewModel {

    public BaseAndroidViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public String getString(int resId){
        return getApplication().getString(resId);
    }

    @NonNull
    @NotNull
    @Override
    public MovieApplication getApplication() {
        return super.getApplication();
    }
}
