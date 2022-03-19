package com.hphtv.movielibrary.ui.settings;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.data.Config;
import com.station.kit.util.LogUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/3/14
 */
public class SettingsViewModel extends BaseAndroidViewModel {
    public static final int OK=0;
    public static final int PSW_ERROR=1;
    public static final int NEW_PSW_ERROR=2;
    public static final int PSW_NOT_CHANGE=4;
    public static final int CONFIRM_PSW_ERROR=8;

    private ObservableBoolean mFlagRefresh=new ObservableBoolean(false);

    private ObservableInt mSelectPos =new ObservableInt();
    private ObservableBoolean mChildModeState=new ObservableBoolean(Config.isChildMode());



    public SettingsViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSelectPos.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                LogUtil.v("onPropertyChanged "+sender.toString()+" "+propertyId);
            }
        });
    }

    public ObservableBoolean getFlagRefresh() {
        return mFlagRefresh;
    }

    public ObservableInt getSelectPos(){
        return mSelectPos;
    }

    //    儿童模式
    public ObservableBoolean getChildModeState() {
        return mChildModeState;
    }

    /**
     * 更改儿童模式状态
     * @param v
     */
    public void toggleChildMode(View v){
        mChildModeState.set(!mChildModeState.get());
        Config.setChildMode(mChildModeState.get());
        mFlagRefresh.set(true);
    }


    //  儿童模式->修改密码
    public int checkPasswords(String old_psw, String new_psw, String c_psw){
        String psw=Config.getChildModePassword();
        int flag=OK;
        if(psw==null||!psw.equals(old_psw))
            flag|=PSW_ERROR;
        if(new_psw==null||new_psw.length()<4)
            flag|=NEW_PSW_ERROR;
        if(psw.equals(new_psw))
            flag|=PSW_NOT_CHANGE;
        if(c_psw==null||!new_psw.equals(c_psw))
            flag|=CONFIRM_PSW_ERROR;
        return flag;
    }

    public void submitPassword(String psw){
        Config.setChildModePassword(psw);
    }

    //  海报设置


    public void toggleTitle(View v) {
        Config.setShowTitle(!Config.getShowTitle().get());
        mFlagRefresh.set(true);
    }

    public void togglePoster(View v) {
        Config.setShowPoster(!Config.getShowPoster().get());
        mFlagRefresh.set(true);
    }

    public void toggleLike(View v) {
        Config.setShowLike(!Config.getShowLike().get());
        mFlagRefresh.set(true);
    }

    public void toggleRating(View v) {
        Config.setShowRating(!Config.getShowRating().get());
        mFlagRefresh.set(true);
    }

    public void toggleCornermark(View v) {
        Config.setShowCornerMark(!Config.getShowCornerMark().get());
        mFlagRefresh.set(true);
    }
}
