package com.hphtv.movielibrary.roomdb.dao;


import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Transaction;
import androidx.room.Update;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.TABLE;
import com.hphtv.movielibrary.roomdb.VIEW;
import com.hphtv.movielibrary.roomdb.entity.Movie;
import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.relation.MovieWrapper;

import java.util.List;

/**
 * author: Sam Leung
 * date:  2021/5/25
 */
@Dao
public interface MovieDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long insertOrIgnoreMovie(Movie movie);

    @Update
    public int update(Movie movie);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id")
    public List<Movie> queryByMovieId(String movie_id);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id AND source=:source AND type=:type")
    public Movie queryByMovieIdAndType(String movie_id, String source, String type);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id AND source=:source AND type=:type")
    public MovieWrapper queryWrapperByMovieIdAndType(String movie_id, String source, String type);

    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE movie_id=:movie_id  AND type=:type")
    public List<Movie> queryByMovieIdAndType(String movie_id, String type);


    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id=(SELECT id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE path=:path AND source=:source)")
    public Movie queryByFilePath(String path, String source);

    @Query("UPDATE " + TABLE.MOVIE + " " +
            "SET is_favorite=:isFavorite " +
            "WHERE movie_id=:movie_id " +
            "AND (:videoType IS NULL OR type=:videoType)")
    public int updateFavoriteStateByMovieId(boolean isFavorite, String movie_id,String videoType);

    @Query("UPDATE " + TABLE.MOVIE + " " +
            "SET is_favorite=:isFavorite " +
            "WHERE movie_id=:movie_id " +
            "AND type=:type")
    public int updateFavoriteStateByMovieId(String movie_id, String type, boolean isFavorite);

    @Query("UPDATE " + TABLE.MOVIE +
            " SET last_playtime=:last_playtime" +
            " WHERE movie_id=:movie_id")
    public int updateLastPlaytime(String movie_id, long last_playtime);

    @Query("UPDATE " + TABLE.MOVIE +
            " SET ap=:ap " +
            " WHERE movie_id=:movie_id")
    public int updateAccessPermission(String movie_id, Constants.WatchLimit ap);

    @Query("SELECT is_favorite FROM " + TABLE.MOVIE + " WHERE id=:id")
    public boolean queryFavorite(long id);

//    /**
//     * 只查询当前以挂载的设备
//     *
//     * @return
//     */
//    @Transaction
//    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id IN (SELECT MOVIE__VF.id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " AS MOVIE__VF JOIN " +
//            "(SELECT VF.path from " + TABLE.VIDEOFILE + " AS VF JOIN " + TABLE.DEVICE + " AS DEV ON VF.device_id=DEV.id) AS VF__DEV " +
//            "ON MOVIE__VF.path=VF__DEV.path)")
//    public List<MovieWrapper> queryAll();

    /**
     * 按电影id查询电影wrapper
     *
     * @param id
     * @return
     */
    @Transaction
    @Query("SELECT * FROM "+TABLE.MOVIE+" WHERE id=:id AND source=:source")
    public MovieWrapper queryMovieWrapperById(long id, String source);

    /**
     * 按movie_id查询电影wrapper
     *
     * @param movie_id
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE source=:source and movie_id=:movie_id")
    public MovieWrapper queryMovieWrapperByMovieId(String movie_id, String source);

    /**
     * 按movie_id查询电影wrapper
     *
     * @param movie_id
     * @return
     */
    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE source=:source and movie_id=:movie_id and type=:type")
    public MovieWrapper queryMovieWrapperByMovieIdAndType(String movie_id, String source, String type);

    @Transaction
    @Query("SELECT * FROM " + TABLE.MOVIE + " WHERE id=(SELECT id FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE path=:path AND source=:source)")
    public MovieWrapper queryMovieWrapperByFilePath(String path, String source);

    /**
     * 按照来源分页查找
     *
     * @param source
     * @param offset
     * @param limit
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " GROUP BY id " +
            " ORDER BY " +
            " pinyin ASC " +
            " LIMIT :limit " +
            " OFFSET :offset "
    )
    public List<MovieDataView> queryMovieDataView(String source, int offset, int limit);

    /**
     * 按照电影id和来源
     *
     * @param movie_id
     * @param source
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source AND movie_id=:movie_id " +
            " AND (:type IS NULL OR type=:type) " +
            "GROUP BY id")
    public MovieDataView queryMovieDataViewByMovieId(String movie_id, String type, String source);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW +
            " WHERE id=:id")
    public MovieDataView queryMovieDataView(long id);

    /**
     * 根据条件返回符合条件电影数量。
     *
     * @param dir_uri
     * @param vtid
     * @param genre_name
     * @param year
     * @param year_2
     * @param source
     * @return
     */
    @Query("SELECT COUNT(*) FROM (" +
            "SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:dir_uri IS NULL OR dir_uri=:dir_uri)" +
            " AND ((:year IS NULL AND :year_2 IS NULL)" +
            " OR (:year_2 IS NULL AND year=:year ) " +
            " OR (:year IS NULL AND year <= :year_2)" +
            " OR (year>=:year AND year <=:year_2))" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name) " +
            " AND (:vtid IS -1 OR id IN (SELECT id FROM " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " WHERE vtid=:vtid))" +
            " GROUP BY id " +
            ")")
    public int countMovieDataView(@Nullable String dir_uri, @Nullable long vtid, @Nullable String genre_name, @Nullable String year, @Nullable String year_2, String ap, String source);

    /**
     * 根据条件返回符合条件电影
     * <p>
     * ① year和year2为空 : 全部
     * ② year2为空 : year为具体年份
     * ③ year为空 : year2为 更早
     * ④ year 和 year2不为空 : year <= 年份 <= year2
     *
     * @param dir_uri    设备id
     * @param year       起始年份
     * @param year_2     最高年份
     * @param genre_name 类型
     * @param order      排序方式
     * @param isDesc     是否倒序
     * @return
     */
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:dir_uri IS NULL OR dir_uri=:dir_uri)" +
            " AND ((:year IS NULL AND :year_2 IS NULL)" +
            " OR (:year_2 IS NULL AND year=:year ) " +
            " OR (:year IS NULL AND year <= :year_2)" +
            " OR (year>=:year AND year <=:year_2))" +
            " AND (:genre_name IS NULL OR genre_name=:genre_name) " +
            " AND (:vtid IS -1 OR id IN (SELECT id FROM " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " WHERE vtid=:vtid))" +
            " GROUP BY id,season " +
            " ORDER BY " +
            "CASE WHEN :order =0 AND :isDesc=0 THEN pinyin END ASC," +
            "CASE WHEN :order =1 AND :isDesc=0 THEN ratings END ASC," +
            "CASE WHEN :order =2 AND :isDesc=0 THEN year END ASC," +
            "CASE WHEN :order =3 AND :isDesc=0 THEN add_time END DESC," +
            "CASE WHEN :order =4 AND :isDesc=0 THEN last_playtime END ASC," +
            "CASE WHEN :order =0 AND :isDesc=1 THEN pinyin END DESC," +
            "CASE WHEN :order =1 AND :isDesc=1 THEN ratings END DESC," +
            "CASE WHEN :order =2 AND :isDesc=1 THEN year END DESC," +
            "CASE WHEN :order =3 AND :isDesc=1 THEN add_time END ASC," +
            "CASE WHEN :order =4 AND :isDesc=1 THEN last_playtime END DESC," +
            "CASE WHEN :order =5 THEN is_favorite END ASC " +
            "LIMIT :offset,:limit "
    )
    public List<MovieDataView> queryMovieDataView(@Nullable String dir_uri, @Nullable long vtid, @Nullable String genre_name, @Nullable String year, @Nullable String year_2, int order, @Nullable String ap, @Nullable boolean isDesc, String source, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW
            + " WHERE source=:source " +
//            " AND JULIANDAY('now') - JULIANDAY(DATE(add_time/1000,'UNIXEPOCH')) < 7 " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap))) " +
            " AND (:type IS NULL OR type=:type) " +
            " GROUP BY id " +
            " ORDER BY add_time DESC" +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryMovieDataViewForRecentlyAdded(String source, Constants.SearchType type, String ap, int offset, int limit);

    @Query("SELECT COUNT(*) FROM (" +
            "SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " AS M " +
            " JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCF " +
            " ON M.id=MVCF.id " +
            " JOIN " + TABLE.VIDEO_TAG + " AS VT " +
            " ON VT.vtid=MVCF.vtid " +
            " WHERE source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY M.id " +
            " ORDER BY add_time DESC" +
            ")"
    )
    public int countMovieDataViewForRecentlyAddedByVideoTag(String source, String video_tag, String ap);

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " AS M " +
            " JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCF " +
            " ON M.id=MVCF.id " +
            " JOIN " + TABLE.VIDEO_TAG + " AS VT " +
            " ON VT.vtid=MVCF.vtid " +
            " WHERE source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY M.id " +
            " ORDER BY add_time DESC" +
            " LIMIT :offset,:limit"
    )
    List<MovieDataView> queryMovieDataViewForRecentlyAddedByVideoTag(String source, String video_tag, String ap, int offset, int limit);


    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW +
            " WHERE source=:source " +
            " AND genre_name in (:genre_name) " +
            " AND id!=:id " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id " +
            " ORDER BY add_time DESC " +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryRecommend(String source, String ap, List<String> genre_name, long id, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW +
            " WHERE source=:source " +
            " AND genre_name IN (:genre_name) " +
            " AND id NOT IN (:ids) " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:type IS NULL OR type=:type)" +
            " GROUP BY id " +
            " ORDER BY add_time DESC " +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryRecommend(String source, String type, String ap, List<String> genre_name, List<Long> ids, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW +
            " WHERE source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:type IS NULL OR type=:type)" +
            " GROUP BY id " +
            " ORDER BY ratings DESC " +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryRecommend(String source, String type, String ap, int offset, int limit);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW +
            " WHERE source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " GROUP BY id ")
    public List<MovieDataView> queryAllMovieDataView(String source, String ap);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " WHERE is_favorite=1 AND source=:source" +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:type IS NULL OR type=:type) " +
            " GROUP BY id ORDER BY pinyin ASC" +
            " LIMIT :offset,:limit")
    public List<MovieDataView> queryFavoriteMovieDataView(String source, Constants.SearchType type, String ap, int offset, int limit);

    @Query("SELECT COUNT(*) FROM (" +
            " SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " AS M " +
            " JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCF " +
            " ON M.id=MVCF.id " +
            " JOIN " + TABLE.VIDEO_TAG + " AS VT " +
            " ON VT.vtid=MVCF.vtid " +
            " WHERE is_favorite=1 " +
            " AND source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY M.id " +
            " ORDER BY pinyin ASC" +
            ")")
    public int countFavoriteMovieDataViewByVideoTag(String source, String video_tag, String ap);

    @Query("SELECT * FROM " + VIEW.MOVIE_DATAVIEW + " AS M " +
            " JOIN " + TABLE.MOVIE_VIDEOTAG_CROSS_REF + " AS MVCF " +
            " ON M.id=MVCF.id " +
            " JOIN " + TABLE.VIDEO_TAG + " AS VT " +
            " ON VT.vtid=MVCF.vtid " +
            " WHERE is_favorite=1 " +
            " AND source=:source " +
            " AND (:ap IS NULL OR (ap=:ap OR (ap IS NULL AND s_ap=:ap)))" +
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY M.id " +
            " ORDER BY pinyin ASC" +
            " LIMIT :offset,:limit"
    )
    public List<MovieDataView> queryFavoriteMovieDataViewByVideoTag(String source, String video_tag, String ap, int offset, int limit);

    @Query(" SELECT M.id,M.movie_id,M.title,M.pinyin,M.poster,M.ratings,M.year,M.source,M.type,M.ap,M.add_time,M.last_playtime,M.is_favorite,M.is_watched,-1 AS season,0 AS episode_count " +
            " FROM "+TABLE.MOVIE+" AS M  " +
            " JOIN "+TABLE.MOVIE_VIDEOTAG_CROSS_REF+" AS MVCF  " +
            "  ON M.id=MVCF.id  " +
            " JOIN "+TABLE.VIDEO_TAG+" AS VT  " +
            " ON VT.vtid=MVCF.vtid  " +
            " JOIN "+TABLE.MOVIE_USER_FAVORITE_CROSS_REF+" AS MUF " +
            " ON M.movie_id=MUF.movie_id AND M.type=MUF.type " +
            " AND M.source=:source " +
            " AND (:ap IS NULL OR M.ap=:ap )" +
            " AND (:video_tag IS NULL OR VT.tag=:video_tag)" +
            " GROUP BY M.id  " +
            " ORDER BY pinyin ASC " +
            " LIMIT :offset,:limit")
    public List<MovieDataView> queryUserFavorite(String source,String video_tag,String ap,int offset,int limit);

    @Query("SELECT COUNT(*) FROM (SELECT * FROM " + TABLE.MOVIE_VIDEOFILE_CROSS_REF + " WHERE source=:source GROUP BY id)")
    public int queryTotalMovieCount(String source);

    /**
     * 查询年份
     *
     * @return
     */
    @Query("SELECT year FROM " + VIEW.MOVIE_DATAVIEW + " GROUP BY year ORDER BY year DESC")
    public List<String> queryYearsGroup();
}

