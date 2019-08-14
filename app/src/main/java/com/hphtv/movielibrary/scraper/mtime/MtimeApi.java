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
            JSONArray movieAry = SearchMovies(keyword);
            if (movieAry != null && movieAry.size() > 0) {
                int idx=0;
                for(int i=0;i<movieAry.size();i++){
                    JSONObject movieObj=movieAry.getJSONObject(i);
                    String name=movieObj.getString("name");
                    String nameEn=movieObj.getString("nameEn");
                    if(name.contains(keyword)||nameEn.contains(keyword)){
                        idx=i;
                        if(name.equalsIgnoreCase(keyword)||nameEn.equalsIgnoreCase(keyword)){
                            break;
                        }
                    }
                }
                JSONObject movieObj = movieAry.getJSONObject(idx);
                Rating rating = new Rating();
                rating.max = 10;
                rating.average = Float.valueOf(movieObj.getString("rating").equals("") ? "-1" : movieObj.getString("rating"));
                String title = movieObj.getString("name");
                String year = movieObj.getString("year");
                String cover = movieObj.getString("img");
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
            Response response = OkHttpUtil.getResponse(String.format(MtimeURL.SEARCH_URL, keyword));
            String content = response.body().string();
            JSONObject contentObj = JSON.parseObject(content);
            JSONArray movieAry = contentObj.getJSONArray("movies");

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
                rating.average = Float.valueOf(movieObj.getString("rating").equals("") ? "-1" : movieObj.getString("rating"));
                String title = movieObj.getString("name");
                String cover = movieObj.getString("img");
                String id = String.valueOf(movieObj.getInteger("id"));
                String des1 = "";

                String subtypeStr = movieObj.getString("movieType");
                if (subtypeStr != null && subtypeStr.length() > 0) {
                    des1 += subtypeStr + " | ";
                }
                des1 += movieObj.getString("nameEn");
                String des2 = "";
                JSONArray casts = movieObj.getJSONArray("actors");
                if (casts != null && casts.size() > 0) {
                    for (int j = 0; j < casts.size(); j++) {
                        des2 += casts.getString(j) + " | ";
                    }
                }
                if (des2.length() > 4)
                    des2 = des2.substring(0, des2.length() - 4);
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

    public static Movie parserMovieInfoById(String id) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_PAGE, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
                return null;
            }
            content = response.body().string();
            Movie movie = parseMovie(content, id);
            movie.setAlt(String.format(MtimeURL.MOVIE_PAGE, id));
            movie.setMovieId(id);
            return movie;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Movie parseMovie(String content, String id) {
        Movie movie = new Movie();

        // 电影解析部分代码
        Document doc = Jsoup.parse(content);

        //解析 时长、类型、上映时间
        Elements othersGroup = doc.select("div[pan=M14_Movie_Overview_MovieTypeAndRuntimeAndVersion]");
        if (isNotEmpty(othersGroup)) {
            Element otherEle = othersGroup.first();
            if (otherEle.text().contains("电视系列剧")) {
                movie.setSubtype(ConstData.MovieSubType.TV);
            } else {
                movie.setSubtype(ConstData.MovieSubType.MOVIE);
            }
            //解析上映时间
            Elements pubdateEles = otherEle.select("a[property=v:initialReleaseDate]");
            if (isNotEmpty(pubdateEles)) {
                String pubdate = pubdateEles.first().text();
                movie.setPubDates(new String[]{pubdate});
            }
        }
        //解析导演
        Elements directorGroup = doc.select("div[pan=M14_Movie_Overview_BaseInfo]");
        if (isNotEmpty(directorGroup)) {
            Elements directorsGroup = directorGroup.get(0).select("a[rel=v:directedBy]");
            if (isNotEmpty(directorsGroup)) {
                int size = directorGroup.size();
                Celebrity[] directors = new Celebrity[size];
                for (int i = 0; i < size; i++) {
                    Celebrity director = new Celebrity();
                    director.setName(directorGroup.get(i).text());
                    director.setAlt(directorGroup.get(i).attr("href"));
                    directors[i] = director;
                }
                movie.setDirectors(directors);
            }
        }
        //解析国家
        Elements baseEles = doc.select("dd[pan=M14_Movie_Overview_BaseInfo]");
        if (isNotEmpty(baseEles)) {

            for (int i = 0; i < baseEles.size(); i++) {
                Element label = baseEles.get(i).select("strong").get(0);
                if (label.text().equals("国家地区")) {
                    Elements elem = baseEles.get(i).select("a");
                    if (isNotEmpty(elem)) {
                        int size = elem.size();
                        String[] country = new String[size];
                        for (int j = 0; j < size; j++) {
                            country[j] = elem.get(j).text();
                        }
                        movie.setCountries(country);
                    }
                }

            }

        }
        //解析标题/别名/海报/评分/年份/剧情/分类/时长
        parseSubMovieInfo(id, movie);
        //解析剧照
        parseMoviePhoto(id, movie);
        //解析剧情
        parseMovieSummery(id, movie);
        //解析演员
        parseCast(id, movie);
        movie.setApi(ConstData.Scraper.MTIME);
        return movie;
    }

    private static void parseCast(String id, Movie movie) {
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_DETAIL, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            String content = response.body().string();
            JSONObject jsonObject = JSON.parseObject(content);
            JSONArray actorList = jsonObject.getJSONArray("actorList");
            Celebrity[] celebrities = new Celebrity[actorList.size() > 10 ? 10 : actorList.size()];
            for (int i = 0; i < actorList.size() && i < 10; i++) {
                Celebrity celebrity = new Celebrity();
                celebrity.setName(actorList.getJSONObject(i).getString("actor"));
                celebrities[i] = celebrity;
            }
            movie.setCasts(celebrities);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseSubMovieInfo(String id, Movie movie) {
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_DETAIL, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            String content = response.body().string();
            JSONObject subInfoObj = JSON.parseObject(content);

            movie.setImages(new Images(null, null, subInfoObj.getString("image")));
            movie.setTitle(subInfoObj.getString("titleCn"));
            movie.setOriginalTitle(subInfoObj.getString("titleEn"));
            Rating rating = new Rating();
            rating.average = Float.valueOf(subInfoObj.getString("rating") != null && subInfoObj.getString("rating").equals("") ? "-1" : subInfoObj.getString("rating"));
            movie.setRating(rating);
            movie.setRatingsCount(Integer.valueOf(subInfoObj.getString("scoreCount") != null && subInfoObj.getString("scoreCount").equals("") ? "0" : subInfoObj.getString("scoreCount")));
            movie.setYear(subInfoObj.getString("year"));
            movie.setSummary(subInfoObj.getString("content"));
            JSONArray typeArry = subInfoObj.getJSONArray("type");
            String[] genres = new String[typeArry.size()];
            for (int i = 0; i < genres.length; i++) {
                genres[i] = typeArry.getString(i);
            }
            movie.setGenres(genres);
            movie.setDurations(new String[]{subInfoObj.getString("runTime")});
            Celebrity[] celebrities = new Celebrity[1];
            Celebrity celebrity = new Celebrity();
            celebrity.setName(subInfoObj.getJSONObject("director").getString("directorName"));
            celebrities[0] = celebrity;
            movie.setDirectors(celebrities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void parseMovieSummery(String id, Movie movie) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(String.format(MtimeURL.MOVIE_PLOTS_PAGE, id));
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
            }
            content = response.body().string();
            JSONArray plotsArray = JSON.parseArray(content);
            if (plotsArray != null && plotsArray.size() > 0) {
                String text = plotsArray.getJSONObject(0).getString("content");
                if (text != null) {
                    movie.setSummary(text);
                }
            }

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


    public static void parseMovieTrailerInfo(long id,String movieId, List<MovieTrailer> list) {
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
