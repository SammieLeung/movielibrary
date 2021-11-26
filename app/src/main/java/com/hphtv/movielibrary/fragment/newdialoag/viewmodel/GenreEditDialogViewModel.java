package com.hphtv.movielibrary.fragment.newdialoag.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.hphtv.movielibrary.fragment.newdialoag.entity.GenreTagItem;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.GenreDao;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/11/13
 */
public class GenreEditDialogViewModel extends AndroidViewModel {
    private GenreDao mGenreDao;
    public GenreEditDialogViewModel(@NonNull @NotNull Application application) {
        super(application);
        mGenreDao= MovieLibraryRoomDatabase.getDatabase(application).getGenreDao();
    }

    public void prepareGenreList(Callback callback){
        Observable.create((ObservableOnSubscribe<List<GenreTagItem>>) emitter -> {
            List<GenreTagItem> genreTagItems=new ArrayList<>();
            String source=ScraperSourceTools.getSource();
            List<String> allGenres=mGenreDao.queryGenresBySource(source);
            List<String> genre=mGenreDao.queryGenreTagBySource(source);
            int count=0;
            for(String genre_name:allGenres){
                GenreTagItem item=new GenreTagItem(genre_name,false);
                if(genre.size()>count&&genre.contains(genre_name)){
                    count++;
                    item.setChecked(true);
                }
                genreTagItems.add(item);
            }
            emitter.onNext(genreTagItems);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleObserver<List<GenreTagItem>>() {
                    @Override
                    public void onAction(List<GenreTagItem> genreTagItems) {
                        if(callback!=null)
                            callback.runOnUIThread(genreTagItems);
                    }
                });
    }


    public interface Callback{
        void runOnUIThread(List<GenreTagItem> genreTagItemList);
    }

}
