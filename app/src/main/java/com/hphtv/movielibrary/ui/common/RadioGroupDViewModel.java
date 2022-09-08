package com.hphtv.movielibrary.ui.common;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2022/9/8
 */
public class RadioGroupDViewModel extends BaseAndroidViewModel {
    private ObservableInt mCheckPos;

    public RadioGroupDViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public ObservableInt getCheckPos() {
        return mCheckPos;
    }

    public void setCheckPos(ObservableInt checkPos) {
        mCheckPos = checkPos;
    }
}
