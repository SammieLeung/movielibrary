package com.hphtv.movielibrary.ui.settings;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.kit.util.LogUtil;
import com.station.kit.util.SharePreferencesTools;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/9/4
 */
public class PasswordDialogFragmentViewModel extends BaseAndroidViewModel {

    private ObservableField<String> mInputPassword=new ObservableField<>();
    public PasswordDialogFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public boolean checkPassword(){
        if(Config.getChildModePassword().equals(mInputPassword.get()))
            return true;
        return false;
    }

    public ObservableField<String> getInputPassword() {
        return mInputPassword;
    }

    public void setInputPassword(String password) {
        mInputPassword.set(password);
    }
}
