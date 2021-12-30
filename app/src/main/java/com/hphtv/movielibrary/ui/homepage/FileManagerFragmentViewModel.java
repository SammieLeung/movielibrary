package com.hphtv.movielibrary.ui.homepage;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.util.PackageUtil;

import org.jetbrains.annotations.NotNull;

/**
 * author: Sam Leung
 * date:  2021/8/31
 */
public class FileManagerFragmentViewModel extends AndroidViewModel {

    public FileManagerFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
    }

    public String getVersion() {
        StringBuffer versionbuffer = new StringBuffer();
        String versionname = PackageUtil.getVersionName(getApplication());
        versionbuffer.append(versionname);
        return versionbuffer.toString();
    }


}
