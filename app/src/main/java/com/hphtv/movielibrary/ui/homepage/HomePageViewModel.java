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
public class HomePageViewModel extends AndroidViewModel {
//    public ObservableBoolean mChildMode=new ObservableBoolean(!Config.isTempCloseChildMode()&&Config.isChildMode());
    public ObservableBoolean mChildMode=new ObservableBoolean(Config.isChildMode());

    public ObservableBoolean mShowChildMode=new ObservableBoolean(Config.isChildMode());
    public HomePageViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public ObservableBoolean getChildMode() {
        return mChildMode;
    }

    public ObservableBoolean getShowChildMode(){return mShowChildMode;}

    public void toggleChildMode(){
        mChildMode.set(!mChildMode.get());
//        Config.setTempCloseChildMode(!Config.isTempCloseChildMode());
        Config.setChildMode(mChildMode.get());
    }

}
