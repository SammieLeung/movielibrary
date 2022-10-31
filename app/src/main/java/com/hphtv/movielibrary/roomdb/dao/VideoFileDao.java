package com.hphtv.movielibrary.roomdb.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.dataview.ConnectedFileDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.HistoryMovieDataView;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/24
 */
@Dao
public interface VideoFileDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnore(VideoFile videoFile);

    @Update
    public int update(VideoFile videoFile);

    /**
     * 根据文件路径设置last_playtime
     *
     * @param path
     * @return
     */
    @Query("UPDATE " + TABLE.VIDEOFILE + " " +
            "SET last_playtime=:time " +
            "WHERE path=:path")
    public int updateLastPlaytime(String path, long time);

    @Query("SELECT poster FROM " + VIEW.MOVIE_DATAVIEW + " WHERE file_uri=:path AND source=:source")
    public String getPoster(String path, String source);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE)
    public List<VideoFile> queryAll();

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE keyword=:keyword AND path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source) ORDER BY filename ASC")
    public List<VideoFile> queryVideoFileListByKeyword(String keyword, String source);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE path IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE id=:id)")
    public List<VideoFile> queryVideoFilesById(long id);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE device_path <=5 AND is_scanned=0")
    public List<VideoFile> queryAllLocalUnScannedVideoFiles();

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE dir_path=:dirPath AND is_scanned=0")
    public List<VideoFile> queryUnScannedVideoFiles(String dirPath);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE path=:path")
    public VideoFile queryByPath(String path);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE vid=:vid")
    public VideoFile queryByVid(long vid);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE path in (:paths)")
    public List<VideoFile> queryByPaths(String... paths);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE path not in (:paths) and device_path=:device_path")
    public List<VideoFile> queryInvalidByPaths(List<String> paths, String device_path);


    @Query("SELECT * FROM " + VIEW.HISTORY_MOVIE_DATAVIEW +
            " WHERE (source=:source OR source IS NULL)" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:type IS NULL OR type=:type)" +
            " LIMIT :offset,:limit")
    public List<HistoryMovieDataView> queryHistoryMovieDataView(String source, @Nullable String ap, @Nullable Constants.SearchType type, int offset, int limit);

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM " + VIEW.HISTORY_MOVIE_DATAVIEW + " AS H " +
            "JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCRF " +
            "ON MVCRF.id=H._mid " +
            "JOIN " + TABLE.VIDEO_TAG + " AS VTG " +
            "ON VTG.vtid=MVCRF.vtid " +
            "WHERE VTG.tag=:video_tag" +
            " AND (source=:source OR source IS NULL)" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " LIMIT :offset,:limit")
    List<HistoryMovieDataView> queryHistoryMovieDataViewByVideoTag(String source, @Nullable String ap, String video_tag, int offset, int limit);

    @Query("DELETE FROM " + TABLE.VIDEOFILE + " WHERE device_path=:devicePath and path not in (:paths) ")
    public void deleteByDevice(String devicePath, List<String> paths);

    @Query("DELETE FROM " + TABLE.VIDEOFILE)
    public void deleteAll();

    @Query("DELETE FROM " + TABLE.VIDEOFILE + " WHERE dir_path=:dirPath")
    public void deleteVideoFilesOnShortcut(String dirPath);

    @Query("SELECT * FROM " + TABLE.VIDEOFILE + " WHERE dir_path=:dirPath")
    public List<VideoFile> queryVideoFilesOnShortcut(String dirPath);

    @Query("DELETE FROM " + TABLE.VIDEOFILE + " WHERE :currentTime - add_time > 604800000 ")
    public int deleteOutdated(long currentTime);

    @Query("SELECT * FROM " + VIEW.CONNECTED_FILE_DATAVIEW +
            " WHERE path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source)  GROUP BY keyword ")
    public List<ConnectedFileDataView> queryUnrecognizedFilesGroupByKeyword(String source);

    @Query("SELECT COUNT(*) FROM (SELECT * FROM " + VIEW.CONNECTED_FILE_DATAVIEW +
            " WHERE path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source) GROUP BY vid)")
    public int countUnrecognizedFiles(String source);

    @Query("SELECT * FROM " + VIEW.CONNECTED_FILE_DATAVIEW +
            " WHERE path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source) GROUP BY vid LIMIT :offset,:limit ")
    public List<ConnectedFileDataView> queryUnrecognizedFiles(String source, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.CONNECTED_FILE_DATAVIEW + " WHERE keyword=:keyword AND path NOT IN (SELECT path FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source)")
    public List<ConnectedFileDataView> queryUnrecognizedFilesByKeyword(String keyword, String source);
}
