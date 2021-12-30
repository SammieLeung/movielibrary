package com.hphtv.movielibrary.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public abstract class BaseAndroidViewModel extends AndroidViewModel {
    public BaseAndroidViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public String getString(int resId){
        return getApplication().getString(resId);
    }

}
