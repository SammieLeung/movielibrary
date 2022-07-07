package com.hphtv.movielibrary.scraper.service;

import android.content.Context;

import com.google.gson.Gson;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.scraper.api.StationMovieProtocol;
import com.hphtv.movielibrary.scraper.postbody.DeleteMovieRequestBody;
import com.hphtv.movielibrary.scraper.postbody.DeviceConfigRequestBody;
import com.hphtv.movielibrary.scraper.postbody.RemoveFolderRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateHistoryRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateLikeRequestBody;
import com.hphtv.movielibrary.scraper.postbody.UpdateMovieRequestBody;
import com.hphtv.movielibrary.scraper.respone.BaseRespone;
import com.hphtv.movielibrary.util.FormatterTools;
import com.hphtv.movielibrary.util.retrofit.RetrofitTools;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;
import com.station.device.StationDeviceTool;
import com.station.kit.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * author: Sam Leung
 * date:  2022/3/29
 */
public class OnlineDBApiService {
    public static final String TAG = OnlineDBApiService.class.getSimpleName();

    public static void uploadMovie(MovieWrapper movieWrapper, List<VideoFile> videoFileList, String source) {
        Context context = MovieApplication.getInstance();
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        DeviceDao deviceDao = MovieLibraryRoomDatabase.getDatabase(context).getDeviceDao();
        Movie movie = movieWrapper.movie;
        String movieId = movie.movieId;
        String type = movie.type.name();
        for (VideoFile videoFile : videoFileList) {
            Device device = deviceDao.querybyMountPath(videoFile.devicePath);//TODO smb/dlna设备为空，处理。
            String path = videoFile.path;
            String keyword = videoFile.keyword;
            String filename = videoFile.filename;
            String storage = FormatterTools.getTypeName(context, device,path);
            String folder = videoFile.dirPath;
            String duration = "0";
            String current_point = "0";
            String watch_limit = movieWrapper.movie.ap!=null&&movieWrapper.movie.ap.equals(Constants.WatchLimit.ADULT) ? "1" : "0";
            UpdateMovieRequestBody body = new UpdateMovieRequestBody(movieId, type, path, keyword, filename, storage, folder, duration, current_point, watch_limit);
            Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
            updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
                @Override
                public void onAction(BaseRespone baseRespone) {
                    LogUtil.w("{uploadMovie:" + source + "} " + baseRespone.code + ": " + movieId + "<=>" + filename);
                }
            });
        }
    }

    public static void uploadMovie(Movie movie, VideoFile videoFile, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
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
        String watch_limit =  movie.ap!=null&&movie.ap.equals(Constants.WatchLimit.ADULT) ? "1" : "0";
        UpdateMovieRequestBody body = new UpdateMovieRequestBody(movieId, type, path, keyword, filename, storage, folder, duration, current_point, watch_limit);
        Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
        updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{uploadMovie:" + source + "} " + baseRespone.code + ": " + movieId + "<=>" + filename);
            }
        });
    }

    public static void uploadFile(VideoFile videoFile, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        String path = videoFile.path;
        String keyword = videoFile.keyword;
        String filename = videoFile.filename;
        String storage = videoFile.devicePath;
        String folder = videoFile.dirPath;
        String duration = "0";
        String current_point = "0";
        String watch_limit = "0";
        UpdateMovieRequestBody body = new UpdateMovieRequestBody("0", null, path, keyword, filename, storage, folder, duration, current_point, watch_limit);
        Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
        updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{uploadFile} " + Thread.currentThread().getName() + ":" + baseRespone.code + ": " + path);
            }
        });
    }

    public static void uploadWatchLimit(String path, Constants.WatchLimit watchLimit, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        String watch_limit = watchLimit.equals(Constants.WatchLimit.ADULT) ? "1" : "0";
        UpdateMovieRequestBody body = new UpdateMovieRequestBody(null, null, path, null, null, null, null, null, null, watch_limit);
        Observable<BaseRespone> updateMovieRequest = request.updateMovie(body);
        updateMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{uploadWatchLimit} " + Thread.currentThread().getName() + ":" + baseRespone.code + ": " + path);
            }
        });
    }

    public static void deleteMovie(String movie_id, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }

        DeleteMovieRequestBody body = new DeleteMovieRequestBody(movie_id);
        Observable<BaseRespone> deleteMovieRequest = request.deleteMovie(body);
        deleteMovieRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{deleteMovie} " + baseRespone.code + ": " + movie_id);
            }
        });
    }

    public static void updateHistory(String path, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }

        UpdateHistoryRequestBody body = new UpdateHistoryRequestBody(StationDeviceTool.getDeviceSN().toLowerCase(), path);
        Observable<BaseRespone> updateHistoryRequest = request.updateHistory(body);
        updateHistoryRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{updateHistory} " + baseRespone.code + ": " + path);
            }
        });
    }

    public static void updateLike(String movie_id, boolean isFavorite, String source, String type) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        UpdateLikeRequestBody body = new UpdateLikeRequestBody(movie_id, isFavorite ? "1" : "0", type);
        Observable<BaseRespone> updateLikeRequest = request.updateLike(body);
        updateLikeRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{updateLike} " + baseRespone.code + ": " + movie_id);
            }
        });
    }

    public static void removeFolder(String dir_path, String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        RemoveFolderRequestBody body = new RemoveFolderRequestBody(dir_path);
        Observable<BaseRespone> removeFolderRequest = request.removeShortcut(body);
        removeFolderRequest.subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{removeFolder} " + Thread.currentThread().getName() + " " + baseRespone.code + ": " + dir_path);
            }
        });
    }

    public static void notifyChildMode(String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        DeviceConfigRequestBody body = new DeviceConfigRequestBody();
        body.child_model = Config.isChildMode() ? "1" : "0";
        request.changeConfig(body).subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{notifyChildMode} " + baseRespone.code );
            }
        });
    }

    public static void notifyShortcuts(List<Shortcut> shortcutList,String source) {
        StationMovieProtocol request = RetrofitTools.createRequest();
        switch (source) {
            case Constants.Scraper.TMDB:
                request = RetrofitTools.createRequest();
                break;
            case Constants.Scraper.TMDB_EN:
                request = RetrofitTools.createENRequest();
                break;
        }
        DeviceConfigRequestBody body = new DeviceConfigRequestBody();
        List<String> pathList=new ArrayList<>();
        for(Shortcut shortcut:shortcutList){
            pathList.add(shortcut.uri);
        }
        body.movie_folder = pathList.toArray(new String[0]);
        request.changeConfig(body).subscribe(new SimpleObserver<BaseRespone>() {
            @Override
            public void onAction(BaseRespone baseRespone) {
                LogUtil.w("{notifyShortcuts} " + baseRespone.code+new Gson().toJson(body.movie_folder));
            }

        });
    }

}
