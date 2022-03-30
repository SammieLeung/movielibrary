package com.hphtv.movielibrary.scraper.service;

import android.content.Context;

import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.api.StationMovieProtocol;
import com.hphtv.movielibrary.scraper.postbody.DeleteMovieRequestBody;
import com.hphtv.movielibrary.scraper.postbody.PostDetailRequetBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateHistoryRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateLikeRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateMovieRequestBody;
import com.hphtv.movielibrary.scraper.respone.BaseRespone;
import com.hphtv.movielibrary.scraper.respone.MovieDetailRespone;
import com.hphtv.movielibrary.util.FormatterTools;
import com.hphtv.movielibrary.util.retrofit.RetrofiTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.device.StationDeviceTool;
import com.station.kit.util.LogUtil;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class OnlineDBApiService {
    public static final String TAG = OnlineDBApiService.class.getSimpleName();

    public static void uploadMovie(MovieWrapper movieWrapper,List<VideoFile> videoFileList, String source) {
        Context context=MovieApplication.getInstance();
        StationMovieProtocol request = RetrofiTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createENRequest();
                break;
        }
        DeviceDao deviceDao= MovieLibraryRoomDatabase.getDatabase(context).getDeviceDao();
        Movie movie = movieWrapper.movie;
        String movieId = movie.movieId;
        String type = movie.type.name();
        for (VideoFile videoFile : videoFileList) {
            Device device=deviceDao.querybyMountPath(videoFile.devicePath);
            String path = videoFile.path;
            String keyword = videoFile.keyword;
            String filename = videoFile.filename;
            String storage = FormatterTools.getTypeName(context,device);
            String folder = videoFile.dirPath;
            String duration = "0";
            String current_point = "0";
            UpdateMovieRequestBody body = new UpdateMovieRequestBody(movieId, type, path, keyword, filename, storage, folder, duration, current_point);
            Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
            updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
                @Override
                public void onAction(BaseRespone baseRespone) {
                    LogUtil.w("{uploadMovie} "+baseRespone.code+": "+movieId+"<=>"+filename );
                }
            });
        }
    }

    public static void uploadMovie(Movie movie, VideoFile videoFile, String source) {
        StationMovieProtocol request = RetrofiTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createENRequest();
                break;
        }

        String movieId = movie.movieId;
        String type = movie.type.name();
        String path = videoFile.path;
        String keyword = videoFile.keyword;
        String filename = videoFile.filename;
        String storage = videoFile.devicePath;
        String folder = videoFile.dirPath;
        String duration = "0";
        String current_point = "0";
        UpdateMovieRequestBody body = new UpdateMovieRequestBody(movieId, type, path, keyword, filename, storage, folder, duration, current_point);
        Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
        updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{uploadMovie} "+baseRespone.code+": "+movieId+"<=>"+filename );
            }
        });
    }

    public static void deleteMovie(String movie_id, String source) {
        StationMovieProtocol request = RetrofiTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createENRequest();
                break;
        }

        DeleteMovieRequestBody body = new DeleteMovieRequestBody(movie_id);
        Observable<BaseRespone> deleteMovieRequest = request.deleteMovie(body);
        deleteMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{deleteMovie} "+baseRespone.code+": "+movie_id );
            }
        });
    }

    public static void updateHistory(String path, String source) {
        StationMovieProtocol request = RetrofiTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createENRequest();
                break;
        }

        UpdateHistoryRequestBody body = new UpdateHistoryRequestBody(StationDeviceTool.getDeviceSN().toLowerCase(), path);
        Observable<BaseRespone> updateHistoryRequest = request.updateHistory(body);
        updateHistoryRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{updateHistory} "+baseRespone.code+": "+path );
            }
        });
    }

    public static void updateLike(String movie_id, boolean isFavorite, String source) {
        StationMovieProtocol request = RetrofiTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofiTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofiTools.createENRequest();
                break;
        }
        UpdateLikeRequestBody body = new UpdateLikeRequestBody(movie_id, isFavorite ? "1" : "0", source);
        Observable<BaseRespone> updateLikeRequest = request.updateLike(body);
        updateLikeRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{updateLike} "+baseRespone.code+": "+movie_id );
            }
        });
    }
}