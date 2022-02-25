package com.hphtv.movielibrary.ui.homepage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.ui.AppBaseActivity;
import com.station.kit.util.LogUtil;

/**
 * author: Sam Leung
 * date:  2022/2/24
 */
public abstract class PermissionActivity<VM extends AndroidViewModel, VDB extends ViewDataBinding> extends AppBaseActivity<VM,VDB> {
    public static final String[] sRequestPermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermission();
    }

    /**
     * 动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                //第一次请求权限的时候返回false,第二次shouldShowRequestPermissionRationale返回true
                //如果用户选择了“不再提醒”永远返回false。
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //请求权限
                    LogUtil.v(TAG, "showRequest true");
                } else {
                    LogUtil.v(TAG, "showRequest false");
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            } else {
                permissionGranted();
            }
        }
    }

    public abstract void permissionGranted();

    private void checkPermissions(){
        for(String permission:sRequestPermissions){
            if (ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(permission)){

                }else{
                }
                requestPermissions(new String[]{permission},1000);
                return;
            }
        }
        permissionGranted();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1000&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            checkPermissions();
        }
    }

}
