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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
    private List<VideoFile> mVideoFileList = new ArrayList<>();
    private List<List<VideoFile>> mEpisodeList = new ArrayList<>();//总剧集列表
    private List<VideoFile> mUnknownEpisodeList = new ArrayList<>();//无法识别的剧集列表
    private LinkedHashMap<String, List<List<VideoFile>>> mTabLayoutPaginationMap = new LinkedHashMap<>();//剧集列表分页
    private String mSource;
    private ObservableInt mLastPlayEpisodePos = new ObservableInt(-1);
    private ObservableField<String> mEpisodePlayBtnText = new ObservableField<>();
    private VideoFile mLastPlayEpisodeVideoFile;

    private int mSeason;

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
        mSeason = season;
        return Observable.just(id)
                .subscribeOn(Schedulers.from(mSingleThreadPool))
                .map(movie_id -> {
                    mMovieWrapper = mMovieDao.queryMovieWrapperById(movie_id, mSource);
                    mLastPlayEpisodePos.set(-1);
                    mTabLayoutPaginationMap.clear();
                    mEpisodeList.clear();
                    mVideoFileList.clear();
                    mLastPlayEpisodeVideoFile = null;
                    mUnknownEpisodeList.clear();
                    //电视剧/综艺
                    if (Constants.SearchType.tv.equals(mMovieWrapper.movie.type)) {
                        for (Season _season : mMovieWrapper.seasons) {
                            int num = _season.seasonNumber;
                            if (num == mSeason) {
                                mMovieWrapper.season = _season;
                                break;
                            }
                        }
                        //无法匹配到合适的season，则当电影处理！
                        if (mMovieWrapper.season == null) {
                            return mMovieWrapper;
                        }

                        ArrayList<VideoFile> filterVideoFileList = new ArrayList<>();
                        //筛选与设置的播出季相同的文件
                        filterVideoFileList.addAll(mMovieWrapper.videoFiles
                                .stream()
                                .filter(videoFile ->
                                        videoFile.season == mMovieWrapper.season.seasonNumber)
                                .collect(Collectors.toList()));
                        mVideoFileList.addAll(filterVideoFileList);
                        //不能识别出播出季的文件<未知剧集>
                        mUnknownEpisodeList.addAll(mMovieWrapper.videoFiles
                                .stream()
                                .filter(videoFile ->
                                        videoFile.season == -1)
                                .collect(Collectors.toList()));

                        mUnknownEpisodeList.sort(Comparator.comparing(o -> o.path));
                        if (mMovieWrapper.containVideoTags(Constants.VideoType.variety_show)) {
//                            FIXME 未能识别出播出季的节目处理？
//                            filterVideoFileList.addAll(mUnknownEpisodeList);
                            filterVideoFileList.sort(Comparator.comparing(o -> o.aired));
                            //按集数分配视频文件，如 720P Ep1--->  第一集
                            //                  1080p EP1-↑
                            int lastPlayPos = -1;

                            for (int i = 0; i < filterVideoFileList.size(); i++) {
                                ArrayList<VideoFile> episodeFiles = new ArrayList<>();
                                mEpisodeList.add(episodeFiles);
                                VideoFile videoFile = filterVideoFileList.get(i);
                                episodeFiles.add(videoFile);
                                //获取上次播放的文件
                                if (videoFile.lastPlayTime > 0) {
                                    if (mLastPlayEpisodeVideoFile == null) {
                                        mLastPlayEpisodeVideoFile = videoFile;
                                        lastPlayPos = i;
                                    }
                                    if (videoFile.lastPlayTime > mLastPlayEpisodeVideoFile.lastPlayTime) {
                                        mLastPlayEpisodeVideoFile = videoFile;
                                        lastPlayPos = i;
                                    }
                                }
                            }
                            if (mLastPlayEpisodeVideoFile != null) {
                                mLastPlayEpisodePos.set(lastPlayPos);//episode从1开始，所以索引应该是episode-1;
                                mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode_2, mLastPlayEpisodeVideoFile.aired));
                            }
                        } else {
                            //根据处理海报信息先创建剧集
                            for (int i = 0; i < mMovieWrapper.season.episodeCount; i++) {
                                ArrayList<VideoFile> episodeFiles = new ArrayList<>();
                                mEpisodeList.add(episodeFiles);
                            }

                            for (int i = 0; i < filterVideoFileList.size(); i++) {
                                VideoFile filterVideoFile = filterVideoFileList.get(i);
                                //查找播放历史
                                if (filterVideoFile.lastPlayTime > 0) {
                                    if (mLastPlayEpisodeVideoFile == null)
                                        mLastPlayEpisodeVideoFile = filterVideoFile;
                                    if (filterVideoFile.lastPlayTime > mLastPlayEpisodeVideoFile.lastPlayTime) {
                                        mLastPlayEpisodeVideoFile = filterVideoFile;
                                    }
                                }
                                if (filterVideoFile.episode > 0) {
                                    mEpisodeList.get(filterVideoFile.episode - 1).add(filterVideoFile);
                                } else {
                                    mUnknownEpisodeList.add(filterVideoFile);
                                }
                            }

                            //上次播放的未分类剧集的位置
                            int lastPlayUnknownEpisodePos = 0;
                            if (mUnknownEpisodeList.size() > 0) {
                                mUnknownEpisodeList.sort(Comparator.comparing(o -> o.path));
                                int episodeSize=mEpisodeList.size();
                                //查找播放记录
                                for (int i=0;i<mUnknownEpisodeList.size();i++) {
                                    VideoFile videoFile=mUnknownEpisodeList.get(i);
                                    if (mLastPlayEpisodeVideoFile == null) {
                                        mLastPlayEpisodeVideoFile = videoFile;
                                        lastPlayUnknownEpisodePos=episodeSize+i;
                                    }
                                    if (videoFile.lastPlayTime > mLastPlayEpisodeVideoFile.lastPlayTime) {
                                        mLastPlayEpisodeVideoFile = videoFile;
                                        lastPlayUnknownEpisodePos=episodeSize+i;
                                    }
                                }
                            }

                            //根据上次播放的文件标记他集数列表中的位置
                            if (mLastPlayEpisodeVideoFile != null) {
                                if (mLastPlayEpisodeVideoFile.episode < 0) {
                                    mLastPlayEpisodePos.set(lastPlayUnknownEpisodePos);
                                    mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_resume));
                                } else {
                                    mLastPlayEpisodePos.set(mLastPlayEpisodeVideoFile.episode - 1);//episode从1开始，所以索引应该是episode-1;
                                    mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode, mLastPlayEpisodeVideoFile.episode));
                                }
                            }
                            //按分页划分视频文件合集
                            int episodeSize = mEpisodeList.size();
                            if (episodeSize > 0) {
                                int part = episodeSize / 10;//index 0是不能识别的剧集，所以需要长度-1.
                                int remaining = episodeSize % 10;
                                if (remaining > 0) {
                                    part += 1;
                                }
                                for (int i = 0; i < part; i++) {
                                    int start = i * 10;
                                    int end = (i + 1) * 10 - 1;
                                    int last = (part - 1) * 10 + remaining - 1;
                                    if (remaining == 0)
                                        last = part * 10 - 1;
                                    if (end > last)
                                        end = last;
                                    String name = (start + 1) + "-" + (end + 1);
                                    if (start == end)
                                        name = String.valueOf(start);
                                    mTabLayoutPaginationMap.put(name, mEpisodeList.subList(start, end + 1));
                                }
                                //TODO 暂时不处理更多剧集
                            }
                        }

                    } else {
                        mVideoFileList.addAll(mMovieWrapper.videoFiles);
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
                    dataViewList.addAll(mMovieDao.queryRecommend(ScraperSourceTools.getSource(), Config.getSqlConditionOfChildMode(), genreName, mMovieWrapper.movie.id, 0, 10));
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

    public VideoFile getLastPlayEpisodeVideoFile() {
        return mLastPlayEpisodeVideoFile;
    }

    public void playingVideo(String path, String name) {
        MovieHelper.playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }

    public void playingEpisodeVideo(VideoFile videoFile) {
        mLastPlayEpisodeVideoFile = videoFile;
        if (mMovieWrapper.containVideoTags(Constants.VideoType.variety_show)) {
            for (int i = 0; i < mEpisodeList.size(); i++) {
                VideoFile tmp = mEpisodeList.get(i).get(0);
                if (tmp.equals(videoFile)) {
                    mLastPlayEpisodePos.set(i);//episode从1开始，所以索引应该是episode-1;
                    mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode_2, mLastPlayEpisodeVideoFile.aired));
                    break;
                }
            }
        } else {
            if(mLastPlayEpisodeVideoFile.episode>0) {
                mLastPlayEpisodePos.set(mLastPlayEpisodeVideoFile.episode - 1);//episode从1开始，所以索引应该是episode-1;
                mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_episode, mLastPlayEpisodeVideoFile.episode));
            }else{
                //当最后播放文件没有剧集信息则在未分类中寻找匹配
                for(int i=0;i<mUnknownEpisodeList.size();i++){
                    VideoFile tmp=mUnknownEpisodeList.get(i);
                    if(tmp.equals(videoFile)){
                        mLastPlayEpisodePos.set(mEpisodeList.size()+i);
                        mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_resume));
                        break;
                    }
                }
            }
        }
        String path = videoFile.path;
        String name = videoFile.filename;
        MovieHelper.playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }

    /**
     * 播放未分类的电视剧
     *
     * @param videoFile
     */
    public void playingOtherEpisodeVideo(VideoFile videoFile) {
        mLastPlayEpisodeVideoFile = videoFile;
        for (int i = 0; i < mUnknownEpisodeList.size(); i++) {
            VideoFile tmp = mUnknownEpisodeList.get(i);
            if (tmp.equals(videoFile)) {
                mLastPlayEpisodePos.set(mEpisodeList.size() + i);//pos需要加上已分类剧集长度
                mEpisodePlayBtnText.set(getApplication().getString(R.string.btn_play_resume));
                break;
            }
        }
        String path = videoFile.path;
        String name = videoFile.filename;
        MovieHelper.playingMovie(path, name)
                .subscribe(new SimpleObserver<String>() {
                    @Override
                    public void onAction(String s) {
                    }
                });
    }


    public Observable<MovieWrapper> setMovie(MovieWrapper wrapper) {
        return Observable.create((ObservableOnSubscribe<MovieWrapper>) emitter -> {
                    if (wrapper != null) {
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
                    StringBuffer sb = new StringBuffer();
                    if (mVideoFileList.size() > 0) {
                        for (VideoFile videoFile : mVideoFileList) {
                            sb.append(StringTools.hideSmbAuthInfo(videoFile.path) + "\n");
                        }
                    } else {
                        for (VideoFile videoFile : mMovieWrapper.videoFiles) {
                            sb.append(StringTools.hideSmbAuthInfo(videoFile.path) + "\n");
                        }
                    }
                    return sb.toString();
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public List<VideoFile> getVideoFileList() {
        return mVideoFileList.size() > 0 ? mVideoFileList : mMovieWrapper.videoFiles;
    }

    public MovieWrapper getMovieWrapper() {
        return mMovieWrapper;
    }

    public List<MovieDataView> getRecommendList() {
        return mRecommendList;
    }

    public List<List<VideoFile>> getEpisodeList() {
        return mEpisodeList;
    }

    /**
     * 当没有播放记录时，自动播放第一个文件
     *
     * @return
     */
    public VideoFile getFirstEnableEpisodeVideoFile() {
        for (List<VideoFile> list : mEpisodeList) {
            if (list.size() > 0)
                return list.get(0);
        }
        return null;
    }

    public HashMap<String, List<List<VideoFile>>> getTabLayoutPaginationMap() {
        return mTabLayoutPaginationMap;
    }

    public List<VideoFile> getUnknownEpisodeList() {
        return mUnknownEpisodeList;
    }

    public int getSeason() {
        return mSeason;
    }

    public void setSeason(int season) {
        mSeason = season;
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
