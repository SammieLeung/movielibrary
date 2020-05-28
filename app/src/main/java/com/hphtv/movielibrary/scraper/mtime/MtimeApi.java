package com.hphtv.movielibrary.scraper.mtime;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.util.EditorDistance;
import com.hphtv.movielibrary.util.OkHttpUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import okhttp3.Response;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi {
    public static final String TAG = MtimeApi.class.getSimpleName();

    public static SimpleMovie SearchAMovieByApi(String keyword) {
        try {
            keyword=keyword.trim();
            JSONArray movieAry = SearchMovies(keyword);
            if (movieAry != null && movieAry.size() > 0) {
                int idx = 0;
                float maxSimilarity = 0;
                for (int i = 0; i < movieAry.size(); i++) {
                    JSONObject movieObj = movieAry.getJSONObject(i);
                    String name = movieObj.getString("titlecn");
                    String nameEn = movieObj.getString("titleen");
                    float similarity = EditorDistance.checkLevenshtein(name, keyword);
                    float similarityEn = EditorDistance.checkLevenshtein(nameEn, keyword);
                    float tmpSimilarity = Math.max(similarity, similarityEn);
                    if (tmpSimilarity == 1) {
                        idx = i;
                        break;
                    }
                    if (tmpSimilarity > maxSimilarity) {
                        idx = i;
                        maxSimilarity=tmpSimilarity;
                    }
                }
                JSONObject movieObj = movieAry.getJSONObject(idx);
                Rating rating = new Rating();
                rating.max = 10;
                rating.average = 0;
                String title = movieObj.getString("titlecn");
                String year = movieObj.getString("year");
                String cover = movieObj.getString("cover");
                String id = String.valueOf(movieObj.getInteger("id"));
                SimpleMovie simpleMovie = new SimpleMovie();
                simpleMovie.setId(id);
                simpleMovie.setRating(rating);
                simpleMovie.setTitle(title);
                simpleMovie.setYear(year);
                simpleMovie.setImages(new Images("", "", cover));
                return simpleMovie;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONArray SearchMovies(String keyword) {
        try {
            Response response = OkHttpUtil.getResponse(String.format(MtimeURL.SEARCH_NEW_URL, keyword));
            String content = response.body().string();
            JSONObject contentObj = JSON.parseObject(content);
            JSONArray movieAry = contentObj.getJSONArray("suggestions");

            return movieAry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray SearchMoviesByName(List<SimpleMovie> dataList, JSONArray jsonArray, String keyword, int offset, int limit) {
        JSONArray movie_array = null;
        if (jsonArray != null)
            movie_array = jsonArray;
        else
            movie_array = SearchMovies(keyword);
        if (movie_array != null && movie_array.size() > 0) {
            if (offset >= movie_array.size())
                return null;
            for (int i = offset; i < offset + limit && i < movie_array.size(); i++) {
                JSONObject movieObj = movie_array.getJSONObject(i);
                Rating rating = new Rating();
                rating.max = 10;
                rating.average = 0;
                String title = movieObj.getString("titlecn");
                String cover = movieObj.getString("cover");
                String id = String.valueOf(movieObj.getInteger("id"));
                String des1 = "";

                String subtypeStr = movieObj.getString("movieType");
                if (subtypeStr != null && subtypeStr.length() > 0) {
                    des1 += subtypeStr + " | ";
                }
                des1 += movieObj.getString("titleen");
                String des2 = "";
                String director = movieObj.getString("director");
                des2 += director;

                String[] abstracts = {des1, des2};
                SimpleMovie simpleMovie = new SimpleMovie();
                simpleMovie.setId(id);
                simpleMovie.setRating(rating);
                simpleMovie.setTitle(title);
                simpleMovie.setAbstracts(abstracts);
                simpleMovie.setRatingsCounts("none");
                simpleMovie.setImages(new Images("", "", cover));
                dataList.add(simpleMovie);
            }
        }
        return movie_array;
    }

    public static Movie parserMovieInfoFromHtmlById(String id) {
        Movie movie = parseMovie(id);
        movie.setAlt(String.format(MtimeURL.MOVIE_PAGE, id));
        movie.setMovieId(id);
        return movie;
    }

    private static Movie parseMovie(String id) {
        Movie movie = new Movie();
        //解析标题/别名/海报/评分/年份/剧情/分类/时长
        parseSubMovieInfo(id, movie);
        //解析剧照
        parseMoviePhoto(id, movie);
        movie.setApi(ConstData.Scraper.MTIME);
        return movie;
    }


    private static void parseSubMovieInfo(String id, Movie movie) {
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_DETAIL, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            String content = response.body().string();
            JSONObject movieObj = JSON.parseObject(content);

            movie.setImages(new Images(null, null, movieObj.getString("image")));
            movie.setTitle(movieObj.getString("titleCn"));
            movie.setOriginalTitle(movieObj.getString("titleEn"));
            Rating rating = new Rating();
            rating.average = Float.valueOf(movieObj.getString("rating") != null && movieObj.getString("rating").equals("") ? "-1" : movieObj.getString("rating"));
            movie.setRating(rating);
            movie.setRatingsCount(Integer.valueOf(movieObj.getString("scoreCount") != null && movieObj.getString("scoreCount").equals("") ? "0" : movieObj.getString("scoreCount")));
            movie.setYear(movieObj.getString("year"));
            movie.setSummary(movieObj.getString("content"));
            JSONArray typeArry = movieObj.getJSONArray("type");
            String[] genres = new String[typeArry.size()];
            for (int i = 0; i < genres.length; i++) {
                genres[i] = typeArry.getString(i);
            }
            movie.setGenres(genres);
            movie.setDurations(new String[]{movieObj.getString("runTime")});
            Celebrity[] celebrities = new Celebrity[1];
            Celebrity celebrity = new Celebrity();
            celebrity.setName(movieObj.getJSONObject("director").getString("directorName"));
            celebrities[0] = celebrity;
            movie.setDirectors(celebrities);

            JSONArray actorList = movieObj.getJSONArray("actorList");
            Celebrity[] actors = new Celebrity[actorList.size() > 10 ? 10 : actorList.size()];
            for (int i = 0; i < actorList.size() && i < 10; i++) {
                Celebrity actor = new Celebrity();
                actor.setName(actorList.getJSONObject(i).getString("actor"));
                actors[i] = actor;
            }
            movie.setCasts(actors);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void parseMoviePhoto(String id, Movie movie) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_POSTER_PAGE, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            content = response.body().string();
            //剧照
            JSONArray images = JSON.parseArray(content);

            Photo[] photo_set = new Photo[10];
            int count = 0;
            for (int i = 0; i < images.size(); i++) {
                if (count < 10) {
                    JSONObject _image = images.getJSONObject(i);
                    String src = _image.getString("image");
                    Photo photo = new Photo();
                    photo.setImageUrl(src);
                    photo.setThumb(src);
                    photo.setIcon(src);
                    photo_set[count] = photo;
                    count++;
                } else {
                    break;
                }
            }
            movie.setPhotos(photo_set);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void parseMovieTrailerInfo(long id, String movieId, List<MovieTrailer> list) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_TRAILER_PAGE, movieId));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            content = response.body().string();
            Log.v(TAG, "content= " + content);
            JSONObject jsonObject = JSON.parseObject(content);
            JSONArray videoList = jsonObject.getJSONArray("videoList");
            for (int i = 0; i < videoList.size(); i++) {
                JSONObject video = videoList.getJSONObject(i);
                MovieTrailer movieTrailer = new MovieTrailer();

                String href = video.getString("hightUrl");
                String duration = "";
                String title = video.getString("title");
                String photo = video.getString("image");
                int mId = video.getInteger("id");
                String pub_date = "";
                long movie_id = id;


                movieTrailer.setAlt(href);
                movieTrailer.setDuration(duration);
                movieTrailer.setId(String.valueOf(mId));
                movieTrailer.setPhoto(photo);
                movieTrailer.setPub_date(pub_date);
                movieTrailer.setTitle(title);
                movieTrailer.setMovie_id(movie_id);
                list.add(movieTrailer);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isNotEmpty(Elements eles) {
        return eles != null && eles.size() > 0;
    }
}
