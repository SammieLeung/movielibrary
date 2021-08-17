package com.hphtv.movielibrary.viewmodel.fragment;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
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
public class PasswordDialogFragmentViewModel extends AndroidViewModel {
    public static final int IS_NOT_VAILD=1;        //0b0001 password is not vaild
    public static final int NOT_A_NEW_PASSWORD=2;    //0b0010 password not change
    public static final int PASSWORD_ERROR=4;    //0b0100 password error
    public static final int PASSWORD_INCONSISTENT=8;    //0b1000 password Inconsistent
    private String mPassword = "";
    private String mSharePreferenceSavePasswordKey=ConstData.SharePreferenceKeys.PASSWORD;

    public PasswordDialogFragmentViewModel(@NonNull @NotNull Application application) {
        super(application);
        mPassword = SharePreferencesTools.getInstance(application).readProperty(mSharePreferenceSavePasswordKey, "");
    }

    /**
     * 设置在SharePreference中保存的密码的键值
     * @param key
     */
    public void initSharePreferenceSavePasswordKey(String key){
        mSharePreferenceSavePasswordKey=key;
        mPassword = SharePreferencesTools.getInstance(getApplication()).readProperty(mSharePreferenceSavePasswordKey, "");
    }

    public void setPassword(String password) {
        if (!TextUtils.isEmpty(password)) {
            mPassword = password;
            SharePreferencesTools.getInstance(getApplication()).saveProperty(mSharePreferenceSavePasswordKey, password);
        }
    }

    public boolean isPasswordEmpty(){
        return TextUtils.isEmpty(mPassword);
    }

    /**
     * 密码是否有效
     *
     * @param password
     * @return
     */
    private boolean isValidityPassword(String password) {
        if (TextUtils.isEmpty(password))
            return false;
        else if (password.length() != 4)
            return false;
        else if (!TextUtils.isDigitsOnly(password))
            return false;
        else
            return true;
    }



    /**
     * 验证新密码是否和旧密码相同
     * @param password
     * @return
     */
    private boolean verifyNewPassword(String password) {
        boolean isVaild = isValidityPassword(password);
        if (isVaild) {
            return !password.equals(mPassword);
        }
        return false;
    }

    /**
     * 验证原密码是否正确
     * @param password
     * @return
     */
    private boolean verifyOriginPassword(String password) {
        boolean isVaild = isValidityPassword(password);
        if (isVaild)
            return password.equals(mPassword);
        return false;
    }


    /**
     * 验证密码是否一致
     * @param newPwd
     * @param dupPwd
     * @return
     */
    private boolean verifyConsistentPassowrd(String newPwd, String dupPwd) {
        boolean isVaildNewPwd = isValidityPassword(newPwd);
        boolean isVaildDupPwd = isValidityPassword(dupPwd);
        if (isVaildNewPwd && isVaildDupPwd) {
            if (newPwd.equals(dupPwd))
                return true;
        }
        return false;
    }

    public void verifyPasswords(List<Observable<CharSequence>> observableList, SimpleObserver<Integer> simpleObserver){
        int count=observableList.size();
        Observable.combineLatest(observableList, objects -> {
            //0001 password is not vaild
            //0010 password not change
            //0100 password error
            //1000 password Inconsistent
            //
            int flag = 0;
            switch (count) {
                case 1:
                    flag = isValidityPassword( objects[0].toString())
                            ? flag : flag | IS_NOT_VAILD;
                    flag = verifyOriginPassword( objects[0].toString())
                            ? flag : flag | PASSWORD_ERROR;
                    break;
                case 2:
                    flag = (isValidityPassword( objects[0].toString()) &&
                            isValidityPassword( objects[1].toString()))
                            ? flag : flag | IS_NOT_VAILD;
                    flag = verifyConsistentPassowrd( objects[0].toString(),  objects[1].toString())
                            ? flag : flag | PASSWORD_INCONSISTENT;
                    break;
                case 3:
                    flag = (isValidityPassword( objects[0].toString())
                            && isValidityPassword( objects[1].toString())
                            && isValidityPassword( objects[2].toString()))
                            ? flag : flag | IS_NOT_VAILD;
                    flag = verifyOriginPassword( objects[0].toString())
                            ? flag : flag | PASSWORD_ERROR;
                    flag = verifyNewPassword( objects[1].toString())
                            ? flag : flag | NOT_A_NEW_PASSWORD;
                    flag = verifyConsistentPassowrd( objects[1].toString(),  objects[2].toString())
                            ? flag : flag | PASSWORD_INCONSISTENT;
                    break;
            }
            return flag;
        }).subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(simpleObserver);
    }

}
