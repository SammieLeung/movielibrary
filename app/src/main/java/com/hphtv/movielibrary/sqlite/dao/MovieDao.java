package com.hphtv.movielibrary.sqlite.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Review;
import com.hphtv.movielibrary.sqlite.MovieDBHelper;
import com.hphtv.movielibrary.util.StrUtils;

public class MovieDao  extends BaseDao<Movie>{


	public MovieDao(Context context) {
		super(context,MovieDBHelper.TABLE_MOVIE);
	}
	/**
	 * 数据库结果集转换MovieList
	 *
	 * @param cursor
	 * @return
	 */
	@Override
	public List<Movie> parseList(Cursor cursor){
		List<Movie> movies = new ArrayList<>();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				Movie movie = new Movie();
				long id=cursor.getInt(cursor.getColumnIndex("id"));
				String movie_id = cursor.getString(cursor.getColumnIndex("movie_id"));
				int wrapper_id=cursor.getInt(cursor.getColumnIndex("wrapper_id"));
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String otitle = cursor.getString(cursor
						.getColumnIndex("otitle"));
				String alt = cursor.getString(cursor.getColumnIndex("alt"));
				String rawimages = cursor.getString(cursor
						.getColumnIndex("images"));
				Images images = JSON.parseObject(rawimages, Images.class);
				String rawrating = cursor.getString(cursor
						.getColumnIndex("rating"));
				Rating rating = JSON.parseObject(rawrating, Rating.class);
				String rawpub_dates = cursor.getString(cursor
						.getColumnIndex("pub_dates"));
				String[] pub_dates = null;
				if (!StrUtils.isNull(rawpub_dates))
					pub_dates = JSON.parseArray(rawpub_dates, String.class)
							.toArray(new String[0]);

				String year = cursor.getString(cursor.getColumnIndex("year"));
				String subtype = cursor.getString(cursor
						.getColumnIndex("subtype"));
				String rawaka = cursor.getString(cursor.getColumnIndex("aka"));
				String[] aka = null;
				if (!StrUtils.isNull(rawaka)) {
					Log.v("json", "rawaka=" + rawaka + " " + (rawaka == null)
							+ " " + rawaka.equals("null"));
					aka = JSON.parseArray(rawaka, String.class).toArray(
							new String[0]);
				}
				String m_url = cursor.getString(cursor.getColumnIndex("m_url"));
				int r_count = cursor.getInt(cursor.getColumnIndex("r_count"));
				int w_count = cursor.getInt(cursor.getColumnIndex("w_count"));
				int c_count = cursor.getInt(cursor.getColumnIndex("c_count"));
				String rawdirectors = cursor.getString(cursor
						.getColumnIndex("directors"));
				Celebrity[] directors = null;
				if (!StrUtils.isNull(rawdirectors))
					directors = JSON.parseArray(rawdirectors, Celebrity.class)
							.toArray(new Celebrity[0]);
				String rawcasts = cursor.getString(cursor
						.getColumnIndex("casts"));
				Celebrity[] casts = null;
				if (!StrUtils.isNull(rawcasts))
					casts = JSON.parseArray(rawcasts, Celebrity.class).toArray(
							new Celebrity[0]);
				String rawwriters = cursor.getString(cursor
						.getColumnIndex("writers"));
				Celebrity[] writers = null;
				if (!StrUtils.isNull(rawwriters))
					writers = JSON.parseArray(rawwriters, Celebrity.class)
							.toArray(new Celebrity[0]);
				String website = cursor.getString(cursor
						.getColumnIndex("website"));
				String doubansite = cursor.getString(cursor
						.getColumnIndex("doubansite"));
				String rawlanguages = cursor.getString(cursor
						.getColumnIndex("languages"));
				String[] languages = null;
				if (!StrUtils.isNull(rawlanguages))
					languages = JSON.parseArray(rawlanguages, String.class)
							.toArray(new String[0]);
				String rawdurations = cursor.getString(cursor
						.getColumnIndex("durations"));
				String[] durations = null;
				if (!StrUtils.isNull(rawdurations))
					durations = JSON.parseArray(rawdurations, String.class)
							.toArray(new String[0]);
				String rawgenresids = cursor.getString(cursor
						.getColumnIndex("genres"));
				String[] genresids = null;
				if (!StrUtils.isNull(rawgenresids))
					genresids = JSON.parseArray(rawgenresids, String.class).toArray(
							new String[0]);
				String[] genres=getGenres(genresids);
				String rawcountries = cursor.getString(cursor
						.getColumnIndex("countries"));
				String[] countries = null;
				if (!StrUtils.isNull(rawcountries))
					countries = JSON.parseArray(rawcountries, String.class)
							.toArray(new String[0]);
				String summary = cursor.getString(cursor
						.getColumnIndex("summary"));
				int cm_count = cursor.getInt(cursor.getColumnIndex("cm_count"));
				int rw_count = cursor.getInt(cursor.getColumnIndex("rw_count"));
				String seasons_count = cursor.getString(cursor
						.getColumnIndex("seasons_count"));
				String current_season = cursor.getString(cursor
						.getColumnIndex("current_season"));
				String episodes=cursor.getString(cursor.getColumnIndex("episodes"));
				String schedule_url = cursor.getString(cursor
						.getColumnIndex("schedule_url"));
				String rawtrailer_urls = cursor.getString(cursor
						.getColumnIndex("trailer_urls"));
				String[] trailer_urls = null;
				if (!StrUtils.isNull(rawtrailer_urls))
					trailer_urls = JSON.parseArray(rawtrailer_urls,
							String.class).toArray(new String[0]);
				String rawphotos = cursor.getString(cursor
						.getColumnIndex("photos"));
				Photo[] photos = null;
				if (!StrUtils.isNull(rawphotos))
					photos = JSON.parseArray(rawphotos, Photo.class).toArray(
							new Photo[0]);
				String rawpop_rws = cursor.getString(cursor
						.getColumnIndex("pop_rws"));
				Review[] pop_rws = null;
				if (!StrUtils.isNull(rawpop_rws))
					pop_rws = JSON.parseArray(rawpop_rws, Review.class)
							.toArray(new Review[0]);
				String rawlocalPath=cursor.getString(cursor
						.getColumnIndex("local_path"));
				String[] localPaths=null;
				String uptime = cursor.getString(cursor
						.getColumnIndex("uptime"));
				String titlePinyin=cursor.getString(cursor.getColumnIndex("title_pinyin"));
				String addtime=cursor.getString(cursor.getColumnIndex("addtime"));
				int api_version=cursor.getInt(cursor.getColumnIndex("api"));

				movie.setId(id);
				movie.setMovieId(movie_id);
				movie.setWrapper_id(wrapper_id);
				movie.setTitle(title);
				movie.setOriginalTitle(otitle);
				movie.setAlt(alt);
				movie.setImages(images);
				movie.setRating(rating);
				movie.setPubDates(pub_dates);
				movie.setYear(year);
				movie.setSubtype(subtype);
				movie.setAka(aka);
				movie.setMobileUrl(m_url);
				movie.setRatingsCount(r_count);
				movie.setWishCount(w_count);
				movie.setCollectCount(c_count);
				movie.setDoCount(null);
				movie.setDirectors(directors);
				movie.setCasts(casts);
				movie.setWriters(writers);
				movie.setWebsite(website);
				movie.setDoubanSite(doubansite);
				movie.setLanguages(languages);
				movie.setDurations(durations);
				movie.setGenres(genres);
				movie.setCountries(countries);
				movie.setSummary(summary);
				movie.setCommentsCount(cm_count);
				movie.setReviewsCount(rw_count);
				movie.setSeasonsCount(seasons_count);
				movie.setCurrentSeason(current_season);
				movie.setEpisodes(episodes);
				movie.setScheduleUrl(schedule_url);
				movie.setTrailerUrls(trailer_urls);
				movie.setPhotos(photos);
				movie.setPopularReviews(pop_rws);
				movie.setUptime(uptime);
				movie.setTitlePinyin(titlePinyin);
				movie.setAddtime(addtime);
				movie.setApi(api_version);
				movies.add(movie);
			}
		}
		return movies;
	}



	public ContentValues parseContentValues(Movie movie) {
		ContentValues contentValues = new ContentValues();
		String movie_id = movie.getMovieId();
		long wrapper_id=movie.getWrapperId();
		String title = movie.getTitle();
		String otitle = movie.getOriginalTitle();
		String alt = movie.getAlt();
		Images oimages = movie.getImages();
		String images = JSON.toJSONString(oimages);
		Rating orating = movie.getRating();
		String rating = JSON.toJSONString(orating);
		String[] opub_dates = movie.getPubDates();
		String pub_dates = JSON.toJSONString(opub_dates);
		String year = movie.getYear();
		String subtype = movie.getSubtype();
		String[] oaka = movie.getAka();
		String aka = JSON.toJSONString(oaka);
		String m_url = movie.getMobileUrl();
		int r_count = movie.getRatingsCount();
		int w_count = movie.getWishCount();
		int c_count = movie.getCollectCount();
		Celebrity[] odirectors = movie.getDirectors();
		String directors = JSON.toJSONString(odirectors);
		Celebrity[] ocasts = movie.getCasts();
		String casts = JSON.toJSONString(ocasts);
		Celebrity[] owriters = movie.getWriters();
		String writers = JSON.toJSONString(owriters);
		String website = movie.getWebsite();
		String doubansite = movie.getDoubanSite();
		String[] olanguages = movie.getLanguages();
		String languages = JSON.toJSONString(olanguages);
		String[] odurations = movie.getDurations();
		String durations = JSON.toJSONString(odurations);
		String[] ogenres = movie.getGenres();
		String[] igenres = addGenres(ogenres);
		String genres = JSON.toJSONString(igenres);
		String[] ocountries = movie.getCountries();
		String countries = JSON.toJSONString(ocountries);
		String summary = movie.getSummary();
		int cm_count = movie.getCommentsCount();
		int rw_count = movie.getReviewsCount();
		String seasons_count = movie.getSeasonsCount();
		String current_season = movie.getCurrentSeason();
		String episodes=movie.getEpisodes();
		String schedule_url = movie.getScheduleUrl();
		String[] otrailer_urls = movie.getTrailerUrls();
		String trailer_urls = JSON.toJSONString(otrailer_urls);
		Photo[] ophotos = movie.getPhotos();
		String photos = JSON.toJSONString(ophotos);
		Review[] opop_rws = movie.getPopularReviews();
		String pop_rws = JSON.toJSONString(opop_rws);
		String uptime = movie.getUptime();
		String titlePinyin=movie.getTitlePinyin();
		String addtime=movie.getAddtime();
		int api_version=movie.getApi();

		contentValues.put("movie_id", movie_id);
		contentValues.put("wrapper_id",wrapper_id);
		contentValues.put("title", title);
		contentValues.put("otitle", otitle);
		contentValues.put("alt", alt);
		contentValues.put("images", images);
		contentValues.put("rating", rating);
		contentValues.put("pub_dates", pub_dates);
		contentValues.put("year", year);
		contentValues.put("subtype", subtype);
		contentValues.put("aka", aka);
		contentValues.put("r_count", r_count);
		contentValues.put("w_count", w_count);
		contentValues.put("c_count", c_count);
		contentValues.put("directors", directors);
		contentValues.put("casts", casts);
		contentValues.put("writers", writers);
		contentValues.put("website", website);
		contentValues.put("doubansite", doubansite);
		contentValues.put("languages", languages);
		contentValues.put("durations", durations);
		contentValues.put("genres", genres);
		contentValues.put("countries", countries);
		contentValues.put("summary", summary);
		contentValues.put("cm_count", cm_count);
		contentValues.put("rw_count", rw_count);
		contentValues.put("seasons_count", seasons_count);
		contentValues.put("current_season", current_season);
		contentValues.put("episodes",episodes);
		contentValues.put("schedule_url", schedule_url);
		contentValues.put("trailer_urls", trailer_urls);
		contentValues.put("photos", photos);
		contentValues.put("pop_rws", pop_rws);
		contentValues.put("uptime", uptime);
		contentValues.put("title_pinyin",titlePinyin);
		contentValues.put("addtime",addtime);
		contentValues.put("api",api_version);
		return contentValues;
	}

	public String[] addGenres(String[] genres) {
		if(genres==null){
			return null;
		}
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		List<String> genList = new ArrayList<String>();
		db.beginTransaction();
		for (int i = 0; i < genres.length; i++) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("name", genres[i]);
			try {
				Cursor cursor = db.query(MovieDBHelper.TABLE_GENRES, null,
						"name like ?", new String[] { "%" + genres[i] + "%" },
						null, null, null, null);

				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						String id = String.valueOf(cursor.getInt(cursor
								.getColumnIndex("id")));
						genList.add(id);
					}
				} else {
					long rowId = db.insertOrThrow(MovieDBHelper.TABLE_GENRES,
							null, contentValues);
					String id = String.valueOf(rowId);
					genList.add(id);
				}

			} catch (Exception e) {
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return genList.toArray(new String[0]);
	}

	public String[] getGenres(String[] genresids){
		if(genresids==null){
			return null;
		}
		int len=genresids.length;
		SQLiteDatabase	db = mDbHelper.getWritableDatabase();
		List<String> genList = new ArrayList<String>();
		StringBuffer sb=new StringBuffer();
		sb.append("id in (");
		for(int i=0;i<len;i++){
			sb.append("?,");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(")");
		db.beginTransaction();
		Cursor cursor = db.query(MovieDBHelper.TABLE_GENRES, null, sb.toString(), genresids,
				null, null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String name = String.valueOf(cursor.getString(cursor
						.getColumnIndex("name")));
				genList.add(name);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return genList.toArray(new String[0]);
	}

	public String getGenresId(String text){
		SQLiteDatabase	db = mDbHelper.getWritableDatabase();
		List<String> genList = new ArrayList<String>();
		db.beginTransaction();
		Cursor cursor = db.query(MovieDBHelper.TABLE_GENRES, null, "name=?", new String[]{text},
				null, null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String id = String.valueOf(cursor.getInt(cursor
						.getColumnIndex("id")));
				genList.add(id);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		if(genList.size()>0){
			return genList.get(0);
		}else{
			return "";
		}

	}
	public List<String> getAllGenres() {
		SQLiteDatabase	db = mDbHelper.getWritableDatabase();
		List<String> genList = new ArrayList<String>();
		db.beginTransaction();
		Cursor cursor = db.query(MovieDBHelper.TABLE_GENRES, null, null, null,
				null, null, "name", null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String name = String.valueOf(cursor.getString(cursor
						.getColumnIndex("name")));
				genList.add(name);
			}
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		return genList;
	}

}
