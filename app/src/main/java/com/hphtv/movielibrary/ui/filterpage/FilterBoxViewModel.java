package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoTagDao;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoTag;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
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
    private VideoTagDao mVideoTagDao;

    private List<Shortcut> mLocalShortcutList;
    private List<Shortcut> mDLNAShortcutList;
    private List<Shortcut> mSMBShortcutList;
    private List<Object> mDeviceDataList;
    private List<String> mGenresList;
    private List<String> mYearsList;
    private List<String> mOrderList;
    private List<VideoTag> mVideoTagsList;
    private String mPresetGenreName;

    private ObservableInt mDevicePos = new ObservableInt(), mVideoTypePos = new ObservableInt(), mGenresPos = new ObservableInt(), mYearPos = new ObservableInt(), mFilterOrderPos = new ObservableInt();
    private ObservableBoolean mDesFlag = new ObservableBoolean();

    public FilterBoxViewModel(@NonNull @NotNull Application application) {
        super(application);
        mScanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(application).getScanDirectoryDao();
        mShortcutDao = MovieLibraryRoomDatabase.getDatabase(application).getShortcutDao();
        mGenreDao = MovieLibraryRoomDatabase.getDatabase(application).getGenreDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
        mVideoTagDao=MovieLibraryRoomDatabase.getDatabase(application).getVideoTagDao();
    }

    public void prepareDevices(FilterBoxDeviceAdapter adapter) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mLocalShortcutList = mShortcutDao.queryAllLocalShortcuts();
                    mDLNAShortcutList = mShortcutDao.queryAllShortcutsByDevcietype(Constants.DeviceType.DEVICE_TYPE_DLNA);
                    mSMBShortcutList = mShortcutDao.queryAllShortcutsByDevcietype(Constants.DeviceType.DEVICE_TYPE_SMB);
                    if (mDeviceDataList == null)
                        mDeviceDataList = new ArrayList<>();
                    mDeviceDataList.clear();
                    if (mLocalShortcutList != null && mLocalShortcutList.size() > 0) {
                        mDeviceDataList.add(getApplication().getString(R.string.filter_box_local_device));
                        mDeviceDataList.addAll(mLocalShortcutList);
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
                        adapter.addAll(dataList);
                    }
                });
    }

    public void prepareTypes(FilterBoxVideoTagAdapter adapter) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mVideoTagsList = mVideoTagDao.queryAllVideoTags();
                    return mVideoTagsList;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<VideoTag>>() {
                    @Override
                    public void onAction(List<VideoTag> list) {
                        adapter.addAll(list);
                    }
                });
    }

    public void prepareGenres(FilterBoxAdapter adapter) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mGenresList = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource());
                    return mGenresList;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> genresList) {
                        adapter.addAll(genresList);
                        if(!TextUtils.isEmpty(mPresetGenreName)) {
                            adapter.setCheckValue(mPresetGenreName);
                            mPresetGenreName=null;
                        }
                    }
                });
    }

    public void prepareYears(FilterBoxAdapter adapter) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mYearsList = mMovieDao.queryYearsGroup();
                    return mYearsList;
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<String>>() {
                    @Override
                    public void onAction(List<String> yearList) {
                        adapter.addAll(yearList);
                    }
                });
    }

    public void prepareOrders(FilterBoxOrderAdapter adapter) {
        Observable.just("")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    mOrderList = new ArrayList<>();
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
                        adapter.addAll(orderList);
                    }
                });
    }

    public ObservableInt getDevicePos() {
        return mDevicePos;
    }

    public ObservableInt getGenresPos() {
        return mGenresPos;
    }

    public ObservableInt getVideoTypePos() {
        return mVideoTypePos;
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

    public Object getRealShortCut() {
        if (mDevicePos.get() == 0)
            return null;
        return mDeviceDataList.get(mDevicePos.get() - 1);
    }

    public VideoTag getRealVideoTag() {
        if (mVideoTypePos.get() == 0)
            return null;
        return mVideoTagsList.get(mVideoTypePos.get() - 1);
    }

    public String getRealGenre() {
        if (mGenresPos.get() == 0)
            return null;
        return mGenresList.get(mGenresPos.get() - 1);
    }

    public String getRealYear() {
        if (mYearPos.get() == 0)
            return null;
        return mYearsList.get(mYearPos.get() - 1);
    }

    public void setPresetGenreName(String presetGenreName) {
        mPresetGenreName = presetGenreName;
    }

    private String getString(int resId) {
        return getApplication().getString(resId);
    }

}
