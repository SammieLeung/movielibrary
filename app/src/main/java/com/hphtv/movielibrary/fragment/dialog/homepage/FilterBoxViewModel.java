package com.hphtv.movielibrary.fragment.dialog.homepage;

import android.app.Application;
import android.telecom.Call;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/12/6
 */
public class FilterBoxViewModel extends AndroidViewModel {
    private ScanDirectoryDao mScanDirectoryDao;
    private ShortcutDao mShortcutDao;
    private GenreDao mGenreDao;
    private MovieDao mMovieDao;

    private List<ScanDirectory> mScanDirectoryList;
    private List<Shortcut> mDLNAShortcutList;
    private List<Shortcut> mSMBShortcutList;
    private List<Object> mDeviceDataList;
    private List<String> mGenresList;
    private List<String> mYearsList;
    private List<String> mOrderList;

    private ObservableInt mDevicePos = new ObservableInt(), mGenresPos = new ObservableInt(),mYearPos=new ObservableInt(),mFilterOrderPos=new ObservableInt();
    private ObservableBoolean mDesFlag=new ObservableBoolean();

    public FilterBoxViewModel(@NonNull @NotNull Application application) {
        super(application);
        mScanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(application).getScanDirectoryDao();
        mShortcutDao = MovieLibraryRoomDatabase.getDatabase(application).getShortcutDao();
        mGenreDao = MovieLibraryRoomDatabase.getDatabase(application).getGenreDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
    }

    public void prepareDevices(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mScanDirectoryList = mScanDirectoryDao.queryAllNotHiddenScanDirectories();
                    mDLNAShortcutList = mShortcutDao.queryAllShortcutsByType(Constants.DeviceType.DEVICE_TYPE_DLNA);
                    mSMBShortcutList = mShortcutDao.queryAllShortcutsByType(Constants.DeviceType.DEVICE_TYPE_SMB);
                    if(mDeviceDataList==null)
                        mDeviceDataList=new ArrayList<>();
                    mDeviceDataList.clear();
                    if (mScanDirectoryList != null && mScanDirectoryList.size() > 0) {
                        mDeviceDataList.add(getApplication().getString(R.string.filter_box_local_device));
                        mDeviceDataList.addAll(mScanDirectoryList);
                    }
                    if (mDLNAShortcutList != null && mDLNAShortcutList.size() > 0) {
                        mDeviceDataList.add(getApplication().getString(R.string.filter_box_dlna_device));
                        mDeviceDataList.addAll(mDLNAShortcutList);
                    }
                    if (mSMBShortcutList != null && mSMBShortcutList.size() > 0) {
                        mDeviceDataList.add(getApplication().getString(R.string.filter_box_smb_device));
                        mDeviceDataList.addAll(mSMBShortcutList);
                    }
                    return mDeviceDataList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<Object>>() {
                    @Override
                    public void onAction(List<Object> dataList) {
                        if (callback != null)
                            callback.runOnUIThread(dataList);
                    }
                });
    }

    public void prepareGenres(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mGenresList = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource());
                    return mGenresList;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> genresList) {
                        if (callback != null)
                            callback.runOnUIThread(genresList);
                    }
                });
    }

    public void prepareYears(Callback callback) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mYearsList = mMovieDao.qureyYearsGroup();
                    return mYearsList;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> yearList) {
                        if (callback != null)
                            callback.runOnUIThread(yearList);
                    }
                });
    }

    public void prepareOrders(Callback callback){
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mOrderList=new ArrayList<>();
                    mOrderList.add(getString(R.string.order_name));
                    mOrderList.add(getString(R.string.order_score));
                    mOrderList.add(getString(R.string.order_year));
                    mOrderList.add(getString(R.string.order_addtime));
                    mOrderList.add(getString(R.string.order_recently_played));
                    return mOrderList;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> orderList) {
                        if(callback!=null)
                            callback.runOnUIThread(orderList);
                    }
                });
    }

    public ObservableInt getDevicePos() {
        return mDevicePos;
    }

    public ObservableInt getGenresPos() {
        return mGenresPos;
    }

    public ObservableInt getYearPos() {
        return mYearPos;
    }

    public ObservableInt getFilterOrderPos() {
        return mFilterOrderPos;
    }

    public ObservableBoolean getDesFlag() {
        return mDesFlag;
    }

    public Object getRealShortCut(){
        if(mDevicePos.get()==0)
            return null;
        return mDeviceDataList.get(mDevicePos.get()-1);
    }

    public String getRealGenre(){
        if(mGenresPos.get()==0)
            return null;
        return mGenresList.get(mGenresPos.get()-1);
    }

    public String getRealYear(){
        if(mYearPos.get()==0)
            return null;
        return mYearsList.get(mYearPos.get()-1);
    }

    private String getString(int resId){
        return getApplication().getString(resId);
    }

    public interface Callback {
        void runOnUIThread(Object... args);
    }

}
