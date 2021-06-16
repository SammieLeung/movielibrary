package com.hphtv.movielibrary.scraper.mtime;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.firelfy.util.EditorDistance;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.util.OkHttpUtil;

import org.jsoup.select.Elements;

import java.util.List;

import okhttp3.Response;

/**
 * Created by tchip on 18-11-9.
 */

public class MtimeApi {
    public static final String TAG = MtimeApi.class.getSimpleName();

    public static SimpleMovie SearchAMovieByApi(String keyword) {
        return SearchAMovieByApi(keyword, null);
    }

    public static SimpleMovie SearchAMovieByApi(String keyword, String originStr) {
        try {
            keyword = keyword.trim();
            JSONObject data = SearchMovies(keyword, 1);
            if (data != null) {
                JSONArray movieAry = data.getJSONArray("movies");
                if (movieAry != null && movieAry.size() > 0) {
                    int idx = 0;
                    float maxSimilarity = 0;
                    String compareStr = TextUtils.isEmpty(originStr) ? keyword : originStr;
                    for (int i = 0; i < movieAry.size(); i++) {
                        JSONObject movieObj = movieAry.getJSONObject(i);
                        String name = movieObj.getString("name");
                        String nameEn = movieObj.getString("nameEn");
                        float similarity = EditorDistance.checkLevenshtein(name, compareStr);
                        float similarityEn = EditorDistance.checkLevenshtein(nameEn, compareStr);
                        float tmpSimilarity = Math.max(similarity, similarityEn);
                        if (tmpSimilarity == 1) {
                            idx = i;
                            break;
                        }
                        if (tmpSimilarity > maxSimilarity) {
                            idx = i;
                            maxSimilarity = tmpSimilarity;
                        }
                    }
                    JSONObject movieObj = movieAry.getJSONObject(idx);
                    Rating rating = new Rating();
                    rating.max = 10;
                    rating.average = movieObj.getInteger("rating");
                    String title = movieObj.getString("name");
                    String year = movieObj.getString("year");
                    String cover = movieObj.getString("img");
                    String id = String.valueOf(movieObj.getInteger("movieId"));
                    SimpleMovie simpleMovie = new SimpleMovie();
                    simpleMovie.setId(id);
                    simpleMovie.setRating(rating);
                    simpleMovie.setTitle(title);
                    simpleMovie.setYear(year);
                    simpleMovie.setImages(new Images("", "", cover));
                    return simpleMovie;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JSONObject SearchMovies(String keyword, int offset) {
        try {
            Response response = OkHttpUtil.getResponse(String.format(MtimeURL.SEARCH_NEW_URL, keyword, offset));
            String content = response.body().string();
            JSONObject contentObj = JSON.parseObject(content);
            JSONObject data = contentObj.getJSONObject("data");

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray SearchMoviesByName(List<SimpleMovie> dataList, JSONArray jsonArray, String keyword, int pageIndex, int limit) {
        JSONArray movie_array = null;
        if (jsonArray != null)
            movie_array = jsonArray;
        else {
            JSONObject data = SearchMovies(keyword, pageIndex);
            if (data != null) {
                movie_array = data.getJSONArray("movies");
            }
        }
        if (movie_array != null && movie_array.size() > 0) {
            for (int i = 0; i < movie_array.size(); i++) {
                JSONObject movieObj = movie_array.getJSONObject(i);
                Rating rating = new Rating();
                rating.max = 10;
                rating.average = 0;
                String title = movieObj.getString("name");
                String cover = movieObj.getString("img");
                String id = String.valueOf(movieObj.getInteger("movieId"));
                String des1 = "";

                String subtypeStr = movieObj.getString("movieType");
                if (subtypeStr != null && subtypeStr.length() > 0) {
                    des1 += subtypeStr + " | ";
                }
                des1 += movieObj.getString("nameEn");
                String des2 = "";
                JSONArray directors = movieObj.getJSONArray("directors");
                for (int j = 0; j < directors.size(); j++) {
                    String director = (String) directors.get(j);
                    des2 += director + " ";
                }

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
            JSONObject contentJson=JSON.parseObject(content);
            JSONObject data=contentJson.getJSONObject("data");
            if(data==null)
                return;
            JSONObject movieObj = data.getJSONObject("basic");

            movie.setImages(new Images(null, null, movieObj.getString("img")));
            movie.setTitle(movieObj.getString("name"));
            movie.setOriginalTitle(movieObj.getString("nameEn"));
            Rating rating = new Rating();
            rating.average = Float.valueOf(movieObj.getString("overallRating") != null && movieObj.getString("overallRating").equals("") ? "-1" : movieObj.getString("overallRating"));
            movie.setRating(rating);
            movie.setRatingsCount(Integer.valueOf(movieObj.getString("personCount") != null && movieObj.getString("personCount").equals("") ? "0" : movieObj.getString("personCount")));
            movie.setYear(movieObj.getString("year"));
            movie.setSummary(movieObj.getString("story"));
            JSONArray typeArry = movieObj.getJSONArray("type");
            if(typeArry!=null) {
                String[] genres = new String[typeArry.size()];
                for (int i = 0; i < genres.length; i++) {
                    genres[i] = typeArry.getString(i);
                }
                movie.setGenres(genres);
            }
            movie.setDurations(new String[]{movieObj.getString("mins")});
            if(movieObj.getJSONObject("director")!=null) {
                Celebrity[] celebrities = new Celebrity[1];
                Celebrity celebrity = new Celebrity();
                celebrity.setName(movieObj.getJSONObject("director").getString("name"));
                celebrities[0] = celebrity;
                movie.setDirectors(celebrities);
            }

            JSONArray actorList = movieObj.getJSONArray("actors");
            Celebrity[] actors = new Celebrity[actorList.size() > 10 ? 10 : actorList.size()];
            for (int i = 0; i < actorList.size() && i < 10; i++) {
                Celebrity actor = new Celebrity();
                actor.setName(actorList.getJSONObject(i).getString("name"));
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
            JSONObject data=JSON.parseObject(content);
            JSONArray images = data.getJSONArray("imageInfos");
            if(images!=null) {
                Photo[] photo_set = new Photo[images.size()];
                for (int i = 0; i < images.size(); i++) {
                    JSONObject _image = images.getJSONObject(i);
                    String src = _image.getString("image");
                    Photo photo = new Photo();
                    photo.setImageUrl(src);
                    photo.setThumb(src);
                    photo.setIcon(src);
                    photo_set[i] = photo;
                }
                movie.setPhotos(photo_set);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void parseMovieTrailerInfo(long id, String movieId, List<MovieTrailer> list) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_DETAIL, movieId));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            content = response.body().string();
            Log.v(TAG, "content= " + content);
            JSONObject contentJson=JSON.parseObject(content);
            JSONObject data=contentJson.getJSONObject("data");
            JSONObject basic=data.getJSONObject("basic");
            JSONArray videoList = basic.getJSONArray("videos");
            for (int i = 0; i < videoList.size(); i++) {
                JSONObject video = videoList.getJSONObject(i);
                MovieTrailer movieTrailer = new MovieTrailer();

                String href = video.getString("hightUrl");
                String duration = "";
                String title = video.getString("title");
                String photo = video.getString("img");
                int mId = video.getInteger("videoId");
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
