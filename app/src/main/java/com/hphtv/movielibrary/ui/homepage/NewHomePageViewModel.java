package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Config;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class NewHomePageViewModel extends AndroidViewModel {
    public ObservableBoolean mChildMode=new ObservableBoolean(Config.isChildMode());
    public NewHomePageViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public ObservableBoolean getChildMode() {
        return mChildMode;
    }

    public void toggleChildMode(){
        mChildMode.set(!mChildMode.get());
    }

}
