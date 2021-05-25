package com.hphtv.movielibrary.scraper.mtime;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.util.EditorDistance;
import com.hphtv.movielibrary.util.LogUtil;
import com.hphtv.movielibrary.util.OkHttpUtil;
import com.hphtv.movielibrary.util.retrofit.MtimeAPIRequest;
import com.hphtv.movielibrary.util.retrofit.MtimeDetailRespone;
import com.hphtv.movielibrary.util.retrofit.MtimeSearchRespone;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;

import org.jsoup.select.Elements;

import java.util.List;
import java.util.concurrent.ExecutorService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Response;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi2 {
    public static final String TAG = MtimeApi2.class.getSimpleName();


    public static Observable<MtimeSearchRespone> SearchAMovieByApi(String keyword) {
        keyword = keyword.trim();
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeSearchRespone> searchObservable = request.searchMovieByMtimeRx(keyword, 1);
        return searchObservable;
    }

    public static Observable<MtimeDetailRespone> getMovieDetail(String movieId) {
        MtimeAPIRequest request = RetrofiTools.createMtimeRequest();
        Observable<MtimeDetailRespone> detailResponeObservable = request.getMovieDetailByMtimeRx(movieId);
        return detailResponeObservable;
    }


}
