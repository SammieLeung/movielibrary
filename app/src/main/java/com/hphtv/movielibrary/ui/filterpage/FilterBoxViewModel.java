package com.hphtv.movielibrary.ui.filterpage;

import android.app.Application;

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

import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
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

    private ObservableInt mDevicePos = new ObservableInt(), mVideoTypePos = new ObservableInt(), mGenresPos = new ObservableInt(), mYearPos = new ObservableInt(), mFilterOrderPos = new ObservableInt(FilterPageViewModel.DEFAULT_ORDER);
    private ObservableBoolean mDesFlag = new ObservableBoolean();

    public FilterBoxViewModel(@NonNull @NotNull Application application) {
        super(application);
        mScanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(application).getScanDirectoryDao();
        mShortcutDao = MovieLibraryRoomDatabase.getDatabase(application).getShortcutDao();
        mGenreDao = MovieLibraryRoomDatabase.getDatabase(application).getGenreDao();
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(application).getMovieDao();
        mVideoTagDao = MovieLibraryRoomDatabase.getDatabase(application).getVideoTagDao();
    }

    public void prepareDevices(FilterBoxDeviceAdapter adapter) {
        Observable.just("").subscribeOn(Schedulers.io()).map(s -> {
            mLocalShortcutList = mShortcutDao.queryAllConnectedLocalShortcuts();
            mDLNAShortcutList = mShortcutDao.queryAllShortcutsByDevcietype(Constants.DeviceType.DEVICE_TYPE_DLNA);
            mSMBShortcutList = mShortcutDao.queryAllShortcutsByDevcietype(Constants.DeviceType.DEVICE_TYPE_SMB);
            if (mDeviceDataList == null) mDeviceDataList = new ArrayList<>();
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
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<Object>>() {
            @Override
            public void onAction(List<Object> dataList) {
                adapter.addAll(dataList);
            }
        });
    }

    public void prepareGenres(FilterBoxAdapter adapter) {
        Observable.just("").subscribeOn(Schedulers.io()).map(s -> {
            mGenresList = mGenreDao.queryGenresBySource(ScraperSourceTools.getSource(), null);
            return mGenresList;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<String>>() {
            @Override
            public void onAction(List<String> genresList) {
                adapter.addAll(genresList);
            }
        });
    }

    public void prepareYears(FilterBoxAdapter adapter) {
        Observable.just("").subscribeOn(Schedulers.io()).map(s -> {
            mYearsList = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            for (int i = 0, j = year; i < 5; i++, j--) {
                mYearsList.add(String.valueOf(j));
            }

            String year_2010_x = "2010-" + (year - 5);
            String year_2000_2009 = getString(R.string.year_2000_2009);
            String year_90s = getString(R.string.year_90s);
            String year_80s = getString(R.string.year_80s);
            String year_earlier = getString(R.string.year_earlier);
            mYearsList.add(year_2010_x);
            mYearsList.add(year_2000_2009);
            mYearsList.add(year_90s);
            mYearsList.add(year_80s);
            mYearsList.add(year_earlier);
            return mYearsList;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<String>>() {
            @Override
            public void onAction(List<String> yearList) {
                adapter.addAll(yearList);
            }
        });
    }

    public void prepareOrders(FilterBoxOrderAdapter adapter) {
        Observable.just("").subscribeOn(Schedulers.io()).map(s -> {
            mOrderList = new ArrayList<>();
            mOrderList.add(getString(R.string.order_name));
            mOrderList.add(getString(R.string.order_score));
            mOrderList.add(getString(R.string.order_year));
            mOrderList.add(getString(R.string.order_addtime));
            mOrderList.add(getString(R.string.order_recently_played));
            return mOrderList;
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new SimpleObserver<List<String>>() {
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
        if (mDevicePos.get() == 0) return null;
        return mDeviceDataList.get(mDevicePos.get() - 1);
    }

    public VideoTag getRealVideoTag() {
        if (mVideoTypePos.get() == 0) return null;
        return mVideoTagsList.get(mVideoTypePos.get() - 1);
    }

    public String getRealGenre() {
        if (mGenresPos.get() == 0) return null;
        return mGenresList.get(mGenresPos.get() - 1);
    }

    public String getRealYear() {
        if (mYearPos.get() == 0) return null;
        return mYearsList.get(mYearPos.get() - 1);
    }

    public String getStartYear(String yearStr) {
        if (yearStr == null || getString(R.string.year_earlier).equalsIgnoreCase(yearStr)) {
            return null;
        } else if (NumberUtils.isDigits(yearStr)) {
            return yearStr;
        } else if (yearStr.contains("-")) {
            String[] years = yearStr.split("-");
            return years[0].trim();
        } else if (getString(R.string.year_90s).equals(yearStr)) {
            return "1990";
        } else if (getString(R.string.year_80s).equals(yearStr)) {
            return "1980";
        }
        return null;
    }

    public String getEndYear(String yearStr) {
        if (yearStr == null || NumberUtils.isDigits(yearStr)) {
            return null;
        } else if (getString(R.string.year_earlier).equalsIgnoreCase(yearStr)) {
            return "1979";
        } else if (yearStr.contains("-")) {
            String[] years = yearStr.split("-");
            return years[1].trim();
        } else if (getString(R.string.year_90s).equals(yearStr)) {
            return "1999";
        } else if (getString(R.string.year_80s).equals(yearStr)) {
            return "1989";
        }
        return null;
    }

    private String getString(int resId) {
        return getApplication().getString(resId);
    }

}
