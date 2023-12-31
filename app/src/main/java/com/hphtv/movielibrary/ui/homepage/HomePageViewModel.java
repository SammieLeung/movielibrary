package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/1/13
 */
public class HomePageViewModel extends AndroidViewModel {
    public ObservableBoolean mChildMode=new ObservableBoolean(Config.isChildMode());

    public HomePageViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public ObservableBoolean getChildMode() {
        return mChildMode;
    }


    public void toggleChildMode(){
        mChildMode.set(!mChildMode.get());
        Config.setChildMode(mChildMode.get());
        OnlineDBApiService.notifyChildMode(Constants.Scraper.TMDB);
        OnlineDBApiService.notifyChildMode(Constants.Scraper.TMDB_EN);
    }

}
