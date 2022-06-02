package com.hphtv.movielibrary.ui.detail;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.dao.MovieDao;
import com.hphtv.movielibrary.roomdb.entity.Genre;
import com.hphtv.movielibrary.roomdb.entity.Season;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.MovieHelper;
import com.hphtv.movielibrary.util.ScraperSourceTools;
import com.hphtv.movielibrary.util.StringTools;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.util.rxjava.SimpleObserver;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * author: Sam Leung
 * date:  2021/6/15
 */
public class MovieDetailViewModel extends BaseAndroidViewModel {
    private MovieDao mMovieDao;

    private ExecutorService mSingleThreadPool;
    private MovieWrapper mMovieWrapper;
    private List<MovieDataView> mRecommendList = new ArrayList<>();
    private List<List<VideoFile>> mEpisodeVideoFilesList = new ArrayList<>();
    private LinkedHashMap<String, List<List<VideoFile>>> mTabLayoutPaginationMap = new LinkedHashMap<>();
    private String mSource;
    private ObservableInt mLastPlayEpisodePos =new ObservableInt(-1);
    private ObservableField<String> mEpisodePlayBtnText=new ObservableField<>();
    private VideoFile mLastPlayVideoFile;

    public MovieDetailViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSingleThreadPool = Executors.newSingleThreadExecutor();
        mSource = ScraperSourceTools.getSource();
        mEpisodePlayBtnText.set(application.getString(R.string.btn_play));
        init();
    }

    private void init() {
        mMovieDao = MovieLibraryRoomDatabase.getDatabase(getApplication()).getMovieDao();
    }

    /**
     * 读取MovieWrapper
     *
     * @param id
     * @return
     */
    public Observable<MovieWrapper> loadMovieWrapper(long id, int season) {

        return Observable.just(id)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movie_id -> {
                    mMovieWrapper = mMovieDao.queryMovieWrapperById(movie_id, mSource);
                    mLastPlayEpisodePos.set(-1);
                    if (Constants.SearchType.tv.equals(mMovieWrapper.movie.type)) {
                        for (Season _season : mMovieWrapper.seasons) {
                            int num = _season.seasonNumber;
                            if (num == season) {
                                mMovieWrapper.season = _season;
                                break;
                            }
                        }
                        mTabLayoutPaginationMap.clear();
                        mEpisodeVideoFilesList.clear();
                        ArrayList<VideoFile> tmpVideoFileList = new ArrayList<>();
                        tmpVideoFileList.addAll(mMovieWrapper.videoFiles);

                        //按集数分配视频文件
                        for (int i = 0; i < mMovieWrapper.season.episodeCount + 1; i++) {
                            Iterator<VideoFile> videoFileIterator = tmpVideoFileList.iterator();
                            ArrayList<VideoFile> tmpList = new ArrayList<>();
                            mEpisodeVideoFilesList.add(tmpList);

                            while (videoFileIterator.hasNext()) {
                                VideoFile videoFile = videoFileIterator.next();
                                if(videoFile.season==mMovieWrapper.season.seasonNumber) {
                                    if (videoFile.lastPlayTime > 0) {
                                        if (mLastPlayVideoFile == null)
                                            mLastPlayVideoFile = videoFile;
                                        if (videoFile.lastPlayTime > mLastPlayVideoFile.lastPlayTime) {
                                            mLastPlayVideoFile = videoFile;
                                        }
                                    }
                                    if (videoFile.episode == i) {
                                        tmpList.add(videoFile);
                                        videoFileIterator.remove();
                                    }
                                }else{
                                    videoFileIterator.remove();
                                }
                            }
                        }

                        if(mLastPlayVideoFile!=null) {
                            mLastPlayEpisodePos.set(mLastPlayVideoFile.episode-1);//episode从1开始，所以索引应该是episode-1;
                            mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode, mLastPlayVideoFile.episode));
                        }
                        //按分页划分视频文件
                        int episodeSize = mEpisodeVideoFilesList.size();
                        if (episodeSize > 1) {
                            int part = (episodeSize - 1) / 10;//index 0是不能识别的剧集，所以需要长度-1.
                            int remaining = (episodeSize - 1) % 10;
                            if(remaining>0){
                                part+=1;
                            }
                            for (int i = 0; i < part; i++) {
                                int start = i * 10 + 1;
                                int end = (i + 1) * 10;
                                int last=(part - 1) * 10 + remaining;
                                if(remaining==0)
                                    last=part*10;
                                if (end > last)
                                    end = last;
                                String name = start + "-" + end;
                                if (start == end)
                                    name = String.valueOf(start);
                                mTabLayoutPaginationMap.put(name, mEpisodeVideoFilesList.subList(start, end + 1));
                            }
                            //TODO 暂时不处理更多剧集
//                            if (mEpisodeVideoFilesList.get(0).size() > 0)
//                                mTabLayoutPaginationMap.put(getApplication().getString(R.string.episode_pagination_others_title), mEpisodeVideoFilesList.subList(0, 1));
                        }

                    }

                    return mMovieWrapper;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * 获取推荐影片
     *
     * @return
     */
    public Observable<List<MovieDataView>> loadRecommend() {
        return Observable.just(mMovieWrapper.genres)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(list -> {
                    List<String> genreName = new ArrayList<>();
                    for (Genre genre : list) {
                        genreName.add(genre.name);
                    }
                    List<MovieDataView> dataViewList = new ArrayList<>();
                    dataViewList.addAll(mMovieDao.queryRecommand(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), genreName, mMovieWrapper.movie.id, 0, 10));
                    return dataViewList;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 获取影片分类标签
     *
     * @return
     */
    public Observable<List<String>> loadTags() {
        return Observable.just(mMovieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movieWrapper -> {
                    List<String> tagList = new ArrayList<>();
                    if (!TextUtils.isEmpty(movieWrapper.movie.region)) {
                        Locale locale = new Locale.Builder().setRegion(movieWrapper.movie.region).build();
                        tagList.add(locale.getDisplayName());
                    }
                    if (movieWrapper.genres.size() > 0) {
                        for (int i = 0; i < movieWrapper.genres.size() && i < 3; i++) {
                            tagList.add(movieWrapper.genres.get(i).name);
                        }
                    }
                    if (!TextUtils.isEmpty(movieWrapper.movie.year)) {
                        tagList.add(movieWrapper.movie.year);
                    }
                    return tagList;
                }).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 设置收藏
     *
     * @param isLike
     * @return
     */
    public Observable<Boolean> setLike(boolean isLike) {
        return Observable.just(mMovieWrapper)
                .map(wrapper -> {
                    boolean isFavorite = isLike;
                    String movieId = wrapper.movie.movieId;
                    mMovieDao.updateFavoriteStateByMovieId(isFavorite, movieId);
                    OnlineDBApiService.updateLike(movieId, isFavorite, ScraperSourceTools.getSource(), wrapper.movie.type.name());
                    mMovieWrapper.movie.isFavorite = isLike;
                    return isFavorite;
                }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * 切换收藏
     *
     * @return
     */
    public Observable<Boolean> toggleLike() {
        return setLike(!mMovieWrapper.movie.isFavorite);
    }

    public ObservableInt getLastPlayEpisodePos() {
        return mLastPlayEpisodePos;
    }

    public ObservableField<String> getEpisodePlayBtnText() {
        return mEpisodePlayBtnText;
    }

    public VideoFile getLastPlayVideoFile() {
        return mLastPlayVideoFile;
    }

    public void playingVideo(String path, String name) {
        MovieHelper.playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }

    public void playingEpisodeVideo(VideoFile videoFile){
        mLastPlayVideoFile=videoFile;
        mLastPlayEpisodePos.set(mLastPlayVideoFile.episode-1);//episode从1开始，所以索引应该是episode-1;
        mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode, mLastPlayVideoFile.episode));
        String path=videoFile.path;
        String name=videoFile.filename;
        MovieHelper.playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }


    public Observable<MovieWrapper> selectMovie(MovieWrapper wrapper) {
        return Observable.create((ObservableOnSubscribe<MovieWrapper>) emitter -> {
                    if (wrapper != null) {
//                String movie_id=wrapper.movie.movieId;
//                String last_movie_id = mMovieWrapper.movie.movieId;
//                boolean is_favoirte = mMovieWrapper.movie.isFavorite;
//                boolean is_watched = mMovieWrapper.movie.isWatched;
//                BroadcastHelper.sendBroadcastMovieUpdateSync(getApplication(), last_movie_id, movie_id, is_favoirte ? 1 : 0);//向手机助手发送电影更改的广播
                        MovieHelper.manualSaveMovie(getApplication(), wrapper, mMovieWrapper.videoFiles);
                    } else {
                        throw new Throwable();
                    }
                    emitter.onNext(wrapper);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.from(mSingleThreadPool))
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Observable<String> loadFileList() {
        return Observable.just(mMovieWrapper)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(wrapper -> {
                    List<VideoFile> list = wrapper.videoFiles;
                    StringBuffer sb = new StringBuffer();
                    for (VideoFile videoFile : list) {
                        sb.append(StringTools.hideSmbAuthInfo(videoFile.path) + "\n");
                    }
                    return sb.toString();
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public MovieWrapper getMovieWrapper() {
        return mMovieWrapper;
    }

    public List<MovieDataView> getRecommendList() {
        return mRecommendList;
    }

    public List<List<VideoFile>> getEpisodeVideoFilesList() {
        return mEpisodeVideoFilesList;
    }

    /**
     * 当没有播放记录时，自动播放第一个文件
     * @return
     */
    public VideoFile getFirstEnableEpisodeVideoFile(){
        for(List<VideoFile> list:mEpisodeVideoFilesList){
            if(list.size()>0)
                return list.get(0);
        }
        return null;
    }

    public HashMap<String, List<List<VideoFile>>> getTabLayoutPaginationMap() {
        return mTabLayoutPaginationMap;
    }

    public interface Callback2 {
        void runOnUIThread(Object... args);
    }

    public interface MovieWrapperCallback {
        void runOnUIThread(MovieWrapper movieWrapper);
    }

    public interface UnrecognizedFileCallback {
        void runOnUIThread(List<UnrecognizedFileDataView> unrecognizedFileDataViewList);
    }
}
