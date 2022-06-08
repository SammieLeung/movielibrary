package com.hphtv.movielibrary.ui.homepage.genretag;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.GenreTag;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2022/3/7
 */
public class AddGenreDialogViewModel extends BaseAndroidViewModel {
    private GenreDao mGenreDao;
    private List<GenreTagItem> mGenreTagItemList = new ArrayList<>();
    private List<GenreTagItem> mGenreTagItemSortList = new ArrayList<>();

    private List<String> mAllGenres;
    ObservableInt mCheckPos = new ObservableInt();

    public AddGenreDialogViewModel(@NonNull @NotNull Application application) {
        super(application);
        mGenreDao = MovieLibraryRoomDatabase.getDatabase(application).getGenreDao();
        mAllGenres = Arrays.asList(getApplication().getResources().getStringArray(R.array.genre_tags).clone());
    }

    public Observable<List<GenreTagItem>> prepareGenreList() {
        return Observable.create((ObservableOnSubscribe<List<GenreTagItem>>) emitter -> {
                    mGenreTagItemList.clear();
                    String source = ScraperSourceTools.getSource();
                    List<GenreTag> genreTags = mGenreDao.queryGenreTagBySource(source);
                    int count = 0;
                    for (GenreTag tag : genreTags) {
                        GenreTagItem item = new GenreTagItem(tag.name, true);
                        mGenreTagItemSortList.add(item);
                        mGenreTagItemList.add(item);
                    }
                    for (String genre_name : mAllGenres) {
                        GenreTagItem item = new GenreTagItem(genre_name, false);
                        if (!mGenreTagItemSortList.contains(item)) {
                            mGenreTagItemList.add(item);
                        }
                    }


                    emitter.onNext(mGenreTagItemList);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public List<GenreTagItem> getGenreTagItemList() {
        return mGenreTagItemList;
    }

    public List<GenreTagItem> getGenreTagItemSortList() {
        return mGenreTagItemSortList;
    }

    public Observable<String> saveGenreTagList() {
        return Observable.just("")
                .subscribeOn(Schedulers.newThread())
                .doOnNext(s -> {
                    mGenreDao.deleteAllGenreTags();
                    mGenreDao.insertGenreTags(toGenreTagList());
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public List<GenreTag> toGenreTagList() {
        int weight = 0;
        List<GenreTag> genreTags = new ArrayList<>();
        for (GenreTagItem tagItem : mGenreTagItemSortList) {
            if (tagItem.isChecked().get()) {
                GenreTag tag = new GenreTag();
                tag.weight = weight++;
                tag.name = tagItem.getName();
                tag.source = ScraperSourceTools.getSource();
                genreTags.add(tag);
            }
        }
        return genreTags;
    }

}
