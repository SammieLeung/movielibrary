package com.hphtv.movielibrary.scraper.douban;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hphtv.movielibrary.MovieApplication;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.MovieTrailer;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Photo;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.scraper.Scraper;
import com.hphtv.movielibrary.util.OkHttpUtil;
import com.hphtv.movielibrary.util.StrUtils;

import android.text.TextUtils;
import android.util.Log;

public class DoubanApi {

    private static final String TAG = "DoubanApi";
    private static String DOUBAN_API_KEY = "0b0f2e2fa4180db6212d8a8eec482922";
    private static final Boolean IS_PARSE_CELEBRITY = false;


    /**
     * String title,
     String year, String cover, String rating, String ratingCounts,
     String descrption1, String descrption2, String href, String id
     * @param name
     */
    public static SimpleMovie SearchMovieByApi(String name,int start,int count){
        try {
              Response response= OkHttpUtil.getResponseFromServer(String.format(DoubanURL.API_BASE_V2,name,start,count));
               String content = response.body().string();
                JSONObject jsonObj = JSON.parseObject(content);
                JSONArray subjectsArray=jsonObj.getJSONArray("subjects");
              if(subjectsArray!=null&&subjectsArray.size()>0){
                  JSONObject subjectChildObj=  subjectsArray.getJSONObject(0);
                  Rating rating=new Rating();
                  rating.max=10;
                  rating.average=Float.valueOf(subjectChildObj.getJSONObject("rating").getFloat("average"));
                  String title=subjectChildObj.getString("title");
                  String year=subjectChildObj.getString("year");
                  String cover=subjectChildObj.getJSONObject("images").getString("large");
                  String ratingCounts=subjectChildObj.getString("collect_count");
                  String href=subjectChildObj.getString("alt");
                  String id=subjectChildObj.getString("id");
                  SimpleMovie simpleMovie=new SimpleMovie();
                  simpleMovie.setId(id);
                  simpleMovie.setAlt(href);
                  simpleMovie.setRatingsCounts(ratingCounts);
                  simpleMovie.setRating(rating);
                  simpleMovie.setTitle(title);
                  simpleMovie.setYear(year);
                  simpleMovie.setImages(new Images("","",cover));
                  return simpleMovie;
              }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
    }

    /**
     * 解析电影列表数据
     *
     * @param search_data_list ListView关联的datalist
     * @param data             搜索页面body标签下的内容
     * @param movieLimit       每次搜索的数量
     * @return 搜索到的电影数量
     */
    public static int parserSearchMoiveLists(
            List<SimpleMovie> search_data_list, String data,
            int movieLimit) {
        try {
            Document doc = Jsoup.parse(data);
            // Douban解析部分
            Element root = doc.getElementById("root");
            // 电影条目的class为sc-ifAKCX
            Elements es_list = root.select(".cover-link");

            if (es_list.size() > 0) {
                int len = es_list.size() < movieLimit ? es_list.size() : movieLimit;
                if (MovieApplication.DEBUG)
                    LogUtil.v(TAG,
                            "======================电影信息======================");
                    for (int i = 0; i < len; i++) {
                        Element eMovie = es_list.get(i).parent();
                        // 电影详细信息链接
                        String href = eMovie.getElementsByTag("a").get(0).attr("href");
                        Matcher movieMatch = Pattern.compile(".*/subject.*").matcher(href);
                        if (!movieMatch.find()) {
                            len++;
                            continue;
                        }
                        // 电影id
                        String[] contents = href.split("/");
                        String id = contents[contents.length - 1];
                        // 电影标题(主标题+副标题+(年份))
                        String title = eMovie.select(".detail .title a").text();
                        Log.v(TAG, "[Movie] " + title);
                        // 年份
                        Matcher title_matcher = Pattern.compile("\\((.*)\\)").matcher(
                                title);
                        String year = null;
                        if (title_matcher.find())
                            year = title_matcher.group(1);
                        // 评分
                        String rating = eMovie.select(
                                ".detail .rating span.rating_nums").text();
                        // 评分人数
                        String ratingCounts = eMovie.select(".detail .rating span.pl")
                                .text();
                        Matcher rn_matcher = Pattern.compile("\\((.*)\\)").matcher(
                                ratingCounts);
                        if (rn_matcher.find())
                            ratingCounts = rn_matcher.group(1);
                        // 描述1
                        String descrption1 = eMovie.select(".detail .abstract").text();
                        // 描述2
                        String descrption2 = eMovie.select(".detail .abstract_2")
                                .text();
                        // 封面
                        String cover = eMovie.getElementsByClass("cover").attr("src");
                        if (MovieApplication.DEBUG) {
                            LogUtil.v(TAG, "\n");
                            LogUtil.v(TAG, "herf:" + href);
                            LogUtil.v(TAG, "id:" + id);
                            LogUtil.v(TAG, "title:" + title);
                            LogUtil.v(TAG, "cover:" + cover);
                            LogUtil.v(TAG,
                                    "---------------------------------------------------------------------------\n");
                        }
                        Scraper.addSearchMovieData(search_data_list, title, year, cover,
                                rating, ratingCounts, descrption1, descrption2, href, id);
                    }
                if (MovieApplication.DEBUG)
                    LogUtil.v(TAG,
                            "======================信息结束======================");
                return len;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }


    }



    public static Movie parserBaseMovieInfo(SimpleMovie simpleMovie) {
//        Log.v(TAG, "time=>DoubanApi.parserBaseMovieInfo===>start");
        if (simpleMovie == null)
            return null;
        Movie movie = new Movie();
        movie.setMovieId(simpleMovie.getId());
        movie.setAlt(simpleMovie.getAlt());
        movie.setYear(simpleMovie.getYear());
        Images images = new Images();
        images.setMedium(simpleMovie.getImages().getLarge());
        movie.setImages(images);
        movie.setRating(simpleMovie.getRating());
        try {
            // 获取评价人数字符串（不确定格式）
            String ratingCounts = simpleMovie.getRatingsCounts();
            // 正则匹配数字串
            Matcher number = Pattern.compile("([0-9]+)[^0-9]*").matcher(
                    ratingCounts);
            if (number.find()) {
                int ratingscount = Integer.valueOf(number.group(1));
                movie.setRatingsCount(ratingscount);
            } else {
                movie.setRatingsCount(-1);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            movie.setRatingsCount(-1);
        }

        return movie;
    }

    public static Movie parserMovieInfo(Movie baseMovie) {
        if (baseMovie == null)
            return new Movie();
        Movie movie = baseMovie;
        String content;
        Images images = movie.getImages();
        // 一定要初始化,否则报错会崩溃
        images.setLarge("");
        String url = movie.getAlt();
        try {
            Response response = OkHttpUtil.getResponseFromServer(url);
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
                return null;
            }
            content = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // 电影解析部分代码
        Document doc = Jsoup.parse(content, "https://movie.douban.com/");
        // 电影解析公共部分
        parseCommonMovieInfo(doc, movie, images);


        // 解析评分信息
        Rating rating = movie.getRating();
        // 评分人数
        if (movie.getRatingsCount() < 0) {
            rating.stars = "暂无评分";
            rating.average = -1;
            movie.setRating(rating);
        }
        return movie;
    }

    /**
     * 根据url解析电影页面
     *
     * @param url 电影介绍url
     * @return
     */
    public static Movie parserMovieInfo(String url) {
//        Log.v(TAG, "time=>DoubanApi.parserMovieInfo===>start");
//        Log.v(TAG, "simpleParserMovieHtml  url:" + url);
        Movie movie = new Movie();
        Matcher match = Pattern.compile("/([0-9]+)/").matcher(url);
        if (match.find()) {
            String id = match.group(1);
            movie.setMovieId(id);
        }
        movie.setAlt(url);
        String content;
        Images images = new Images();
        // 一定要初始化,否则报错会崩溃
        images.setLarge("");
        try {
            Response response = OkHttpUtil.getResponseFromServer(url);
            if (!response.isSuccessful()) {
                Log.w(TAG, "httpget failed!");
                return null;
            }
            content = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // 电影解析部分代码
        Document doc = Jsoup.parse(content, "https://movie.douban.com/");
        // 电影解析公共部分
        parseCommonMovieInfo(doc, movie, images);
        //解析年份

        // 解析评分信息
        Element interest_sectl = doc.getElementById("interest_sectl");
        Elements rating_nums = interest_sectl.select(".rating_num");
        Element rating_num = null;
        // 评分分数
        if (rating_nums != null && rating_nums.size() > 0)
            rating_num = rating_nums.get(0);
        Rating rating = new Rating();
        rating.max = 10;
        try {
            rating.average = Float.valueOf(rating_num.text());
        } catch (Exception e) {
            rating.average = -1;
        }
        Elements ratingCounts = interest_sectl
                .select(".rating_sum span[property=v:votes]");
        Element ratingCountsNumber = null;
        // 评分人数
        if (ratingCounts != null && ratingCounts.size() > 0)
            ratingCountsNumber = ratingCounts.get(0);
        if (ratingCountsNumber != null) {
            movie.setRatingsCount(Integer.valueOf(ratingCountsNumber.text()));
        }
        movie.setRating(rating);
        //解析年份信息
        Elements years = doc.getElementsByClass("year");
        if (years != null) {
            String year = years.get(0).text();
            Matcher yearMatch = Pattern.compile("\\(([0-9]+)\\)").matcher(year);
            if (yearMatch.find()) {
                year = yearMatch.group(1);
            }
//            Log.v(TAG, "year " + Integer.valueOf(year));
        }

//        Log.v(TAG, "time=>DoubanApi.parserMovieInfo===>end");
        return movie;
    }


    private static void parseCommonMovieInfo(Document doc, Movie movie, Images images) {


        // 解析电影名
        String doc_title = doc.title();
        if (doc_title.matches("(.*) \\(豆瓣\\)")) {
            String[] matchs = StrUtils.matcher("(.*) \\(豆瓣\\)", doc_title);
            if (matchs != null && matchs.length > 1) {
                String title = matchs[1];
                movie.setTitle(title);
            }
        }

        // 解析剧照信息
        Element poster = doc.select(".related-pic-bd").first();
        if (poster != null) {
            Elements poster_a = poster.getElementsByTag("a");
            ArrayList<Photo> list = new ArrayList<Photo>();
            for (Element element : poster_a) {
                if ("related-pic-video".equals(element.className()))// 为预告片，暂时不做解析
                {
                    continue;
                }
                String herf = element.attr("href");
                if (herf.matches("https://movie.douban.com/photos/photo/([0-9]+)/")) {
                    String[] matchs = StrUtils.matcher(
                            "https://movie.douban.com/photos/photo/([0-9]+)/",
                            herf);
                    if (matchs != null && matchs.length > 1) {
                        String id = matchs[1];
//                        LogUtil.v(
//                                TAG,
//                                "poster IMG_PHOTO:"
//                                        + String.format(DoubanURL.IMG_PHOTO, id));

                        Photo photo = new Photo();
                        photo.setId(id);
                        photo.setImageUrl(String
                                .format(DoubanURL.IMG_PHOTO, id));
                        photo.setThumb(String.format(DoubanURL.IMG_PHOTO_ICON,
                                id));
                        photo.setIcon(String.format(DoubanURL.IMG_PHOTO_ICON,
                                id));
                        list.add(photo);
                    }
                }
            }

            movie.setPhotos(list.toArray(new Photo[0]));
        }
        // 解析封面信息
        Element mainpic = doc.getElementById("mainpic");
        if (mainpic != null) {
            String main_poster = mainpic.select("img").first().attr("src");
            main_poster = main_poster.replace("s_ratio_poster", "l");
//            Log.v(TAG,"main_poster="+main_poster);
            images.setLarge(main_poster);
            movie.setImages(images);
        }
        movie.setSubtype(ConstData.MovieSubType.OTHERS);
        // 解析电影信息
        Element info = doc.getElementById("info");// doc.select("div#info").first();
        Elements spans = null;
        if (info != null)
            spans = info.select("span.pl");
        if (spans != null)
            for (Element span : spans) {
                String text = span.text();
                if ("IMDb链接:".equals(text)) {
                    Element imdb = span.nextElementSibling();
                    if (imdb != null) {
//                    Log.d(TAG, "imdb url = " + imdb.attr("href"));
//                    Log.d(TAG, "imdb id = " + imdb.text());
//            TODO            movie.setImdbId(imdb.text());
//                        movie.setImdbUrl(imdb.attr("href"));
                    }
                } else if ("语言:".equals(text)) {
                    String lang = span.nextSibling().toString();
                    if (lang != null && lang.length() > 0) {
                        String[] languages = lang.trim().toString().split("/\\s+");
                        movie.setLanguages(languages);
                    }
                } else if (text.contains("制片国家") || text.contains("地区")) {
                    String area = span.nextSibling().toString();
                    if (area != null && area.length() > 0) {
                        String[] areas = area.trim().toString().split("/\\s+");
                        movie.setCountries(areas);
                    }
                } else if ("上映日期:".equals(text) || "首播:".equals(text)) {
                    Element data = span;
                    ArrayList<String> list = new ArrayList<String>();
                    while (true) {
                        data = data.nextElementSibling();
                        if (data == null
                                || !"v:initialReleaseDate".equals(data
                                .attr("property"))) {
                            break;
                        } else {
                            list.add(data.text());
                        }
                    }
                    movie.setPubDates(list.toArray(new String[0]));
                    if ("上映日期:".equals(text)) {
                        movie.setSubtype(ConstData.MovieSubType.MOVIE);
                    } else {
                        movie.setSubtype(ConstData.MovieSubType.TV);
                    }
                } else if ("片长:".equals(text)) {
                    Element duration = span.nextElementSibling();
                    if (duration != null
                            && "v:runtime".equals(duration.attr("property"))) {
                        StringBuilder sb = new StringBuilder(duration.text().trim());
                        Node node = duration.nextSibling();
                        while (node != null && !node.toString().equals("<br>")) {
                            sb.append(node.toString().trim());
                            node = node.nextSibling();
                        }
                        String[] durations = sb.toString().split("/\\s+");
                        movie.setDurations(durations);
                    }
                    movie.setSubtype(ConstData.MovieSubType.MOVIE);
                } else if ("单集片长:".equals(text)) {
                    String duration = span.nextSibling().toString().trim();
                    if (duration != null && duration.length() > 0) {
                        movie.setDurations(new String[]{duration});
                    }
                    movie.setSubtype(ConstData.MovieSubType.TV);
                } else if ("季数:".equals(text)) {
                    Element select = span.nextElementSibling();
                    if (select != null && select.id().equals("season")) {
                        Elements options = select.getAllElements();
                        String seassoncount = String.valueOf(options.size());
                        movie.setSeasonsCount(seassoncount);
                        Elements selectOption = select.getElementsByAttributeValue("selected", "selected");
                        if (selectOption != null) {
                            String currentSeason = selectOption.get(0).text();
                            movie.setCurrentSeason(currentSeason);
                        }
                    }
                } else if ("集数:".equals(text)) {
                    String ep = span.nextSibling().toString().trim();
                    if (ep != null && ep.length() > 0) {
                        movie.setEpisodes(ep);
                    }
                    movie.setSubtype(ConstData.MovieSubType.TV);
                } else if ("编剧".equals(text)) {
                    Elements screenwriters = (span.nextElementSibling())
                            .getElementsByTag("a");
                    ArrayList<Celebrity> list = new ArrayList<Celebrity>();
                    for (Element screenwriter : screenwriters) {

                        String absUrl = screenwriter.absUrl("href");

                        if (absUrl != null
                                && absUrl.indexOf("movie.douban.com/celebrity") != -1) {
                            Celebrity celebrity = null;
                            if (IS_PARSE_CELEBRITY) {
                                celebrity = parserCelebrityHtml(absUrl,
                                        screenwriter.text());

                            }
                            if (celebrity != null) {
                                list.add(celebrity);
                            } else {
                                list.add(new Celebrity(screenwriter.text(), absUrl));
                            }
                        } else {
                            list.add(new Celebrity(screenwriter.text(), null));
                        }
                    }
                    movie.setWriters(list.toArray(new Celebrity[0]));
                } else if ("主演".equals(text)) {
                    Elements casts = (span.nextElementSibling()).select("a");
                    ArrayList<Celebrity> list = new ArrayList<Celebrity>();
                    for (int i = 0; i < ((casts.size() > 4) ? 4 : casts.size()); i++) {
                        Element cast = casts.get(i);
//                    Log.d(TAG, "cast  = " + cast.text());
                        String absUrl = cast.absUrl("href");

                        if (absUrl != null
                                && absUrl.indexOf("movie.douban.com/celebrity") != -1) {
                            Celebrity celebrity = null;
                            if (IS_PARSE_CELEBRITY) {
                                celebrity = parserCelebrityHtml(absUrl,
                                        cast.text());
                            }
                            if (celebrity != null) {
                                list.add(celebrity);
                            } else {
                                list.add(new Celebrity(cast.text(), absUrl));
                            }
                        } else {
                            list.add(new Celebrity(cast.text(), null));
                        }
                    }
                    movie.setCasts(list.toArray(new Celebrity[0]));
                } else if ("类型:".equals(text)) {
                    ArrayList<String> arrayList = new ArrayList<String>();
                    String[] genres = null;
                    Element type_span = span.nextElementSibling();
                    for (int i = 0; ; i++) {
                        if (type_span.hasAttr("property")) {
                            arrayList.add(type_span.text());
                            type_span = type_span.nextElementSibling();
                        } else {
                            break;
                        }
                    }
                    genres = arrayList.toArray(new String[0]);
                    movie.setGenres(genres);
                }
            }
        // 解析剧情简介
        Elements sum_all = doc.select("span.all.hidden");
        if (sum_all != null && sum_all.size() > 0) {
            movie.setSummary(sum_all.get(0).text());
        } else {
            Elements summarys = doc.select("span[property=v:summary]");
            Element summary = null;
            if (summarys != null && summarys.size() > 0)
                summary = summarys.get(0);
            if (summary != null)
                movie.setSummary(summary.text());
        }

    }


    public static void parseMovieTrailerInfo(Movie movie, List<MovieTrailer> list) {
        String alt = movie.getAlt();
        String trailer_alt = alt + DoubanURL.TRAILER;
        String content;
        List<MovieTrailer> movieTrailers = list;
        try {
            Response response = OkHttpUtil.getResponseFromServer(trailer_alt);
            if (!response.isSuccessful()) {
                return;
            }
            content = response.body().string();
        } catch (IOException e) {
            Log.v(TAG, "return error:" + trailer_alt);
            return;
        }
        Document doc = Jsoup.parse(content, "https://movie.douban.com/");
        Element head = doc.getElementById("trailer");
        if (head != null) {
            Element videolist = head.parent()
                    .nextElementSibling();
            Elements trailers = videolist.select("li");
            if (MovieApplication.DEBUG)
//            LogUtil.v(TAG, "==================预告片======================");
                for (int i = 0; i < trailers.size(); i++) {
                    Element trailer = trailers.get(i);
                    MovieTrailer movieTrailer = new MovieTrailer();
                    Element a = trailer.child(0);
                    Element img = a.select("img").get(0);
                    Element eTime = a.select("strong em").get(0);
                    Element p1 = trailer.child(1).select("a").get(0);
                    Element p2 = trailer.child(2).select("span").get(0);

                    String title = p1.text();
                    String pub_date = p2.text();
                    String photo = img.attr("src");
                    String duration = eTime.text();
                    String href = a.attr("href");
                    String id = null;
                    long movie_id = movie.getId();
                    Matcher matcher = Pattern.compile(".*trailer/(.*)/.*")
                            .matcher(href);
                    if (matcher.find()) {
                        id = matcher.group(1);
                    }
//            if (MovieApplication.DEBUG) {
//                LogUtil.v(TAG, "title=>" + title);
//                LogUtil.v(TAG, "pub_date=>" + pub_date);
//                LogUtil.v(TAG, "photo=>" + photo);
//                LogUtil.v(TAG, "duration=>" + duration);
//                LogUtil.v(TAG, "href=>" + href);
                    LogUtil.v(TAG, "id=>" + id);
                    LogUtil.v(TAG, "movie_id=>" + movie_id);
                    LogUtil.v(TAG, "-------------------------------------------\n\n");
//            }
                    movieTrailer.setAlt(href);
                    movieTrailer.setDuration(duration);
                    movieTrailer.setId(id);
                    movieTrailer.setPhoto(photo);
                    movieTrailer.setPub_date(pub_date);
                    movieTrailer.setTitle(title);
                    movieTrailer.setMovie_id(movie_id);
                    movieTrailers.add(movieTrailer);
                }
//        if (MovieApplication.DEBUG)
//            LogUtil.v(TAG, "==================end======================");

        }


    }

    public static String parseTrailerUrl(String pageUrl) {
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(pageUrl);
            if (!response.isSuccessful()) {
                return null;
            }
            content = response.body().string();
        } catch (IOException e) {
            Log.v(TAG, "return error:" + pageUrl);
            return null;
        }

        Document doc = Jsoup.parse(content, "https://movie.douban.com/");
        Element video = doc.select("source").get(0);
//        LogUtil.v(TAG, "video url=" + video.toString());
        String url = video.attr("src");
        return url;
    }


    /**
     * 解析演员页面
     *
     * @param alt
     * @param name
     * @return
     */
    public static Celebrity parserCelebrityHtml(String alt, String name) {
        if (TextUtils.isEmpty(alt))
            return null;
        String content;
        try {
            Response response = OkHttpUtil.getResponseFromServer(alt);
            // content = OkHttpUtil.getStringFromServer(search_url);
            if (!response.isSuccessful()) {
                return null;
            }

            content = response.body().string();
//            Log.v(TAG, "parserCelebrityHtml  content:" + content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Celebrity celebrity = new Celebrity();
        celebrity.setAlt(alt);

        Document doc = Jsoup.parse(content);
        Element headline = doc.getElementById("headline");
        if (headline != null) {
            Element img = headline.select(".pic").select(".nbg").first();
            if (img != null) {
                String url = img.absUrl("href");
                String large = url.replaceAll("/(large|medium|small)/",
                        "/large/");
                String medium = url.replaceAll("/(large|medium|small)/",
                        "/medium/");
                String small = url.replaceAll("/(large|medium|small)/",
                        "/small/");

                Images images = new Images(small, medium, large);
                celebrity.setAvatars(images);

                if (!TextUtils.isEmpty(name))
                    celebrity.setName(name);
                else {
//                    Log.v(TAG, "title:" + img.attr("title"));
                    celebrity.setName(img.attr("title"));
                }

            }
            Elements spans = headline.select("div.info  > ul > li > span");
            for (Element span : spans) {
//                Log.v(TAG, "li:" + span.text());
                String text = span.text();
                if ("性别".equals(text)) {
                    String sex = span.nextSibling().toString()
                            .replaceAll(":", "").trim();
                    if (sex != null) {
                        celebrity.setGender(sex);
//                        Log.d(TAG, "sex  = " + sex);
                    }
                } else if ("星座".equals(text)) {
                    String constellation = span.nextSibling().toString()
                            .replaceAll(":", "").trim();
                    if (constellation != null) {
                        celebrity.setConstellation(constellation);
                    }
                } else if ("出生日期".equals(text)) {
                    String birthday = span.nextSibling().toString()
                            .replaceAll(":", "").trim();
                    if (birthday != null) {
                        celebrity.setBirthday(birthday);
                    }
                } else if ("职业".equals(text)) {
                    String[] professions = span.nextSibling().toString()
                            .replaceAll(":", "").trim().split("/");
                    if (professions != null && professions.length > 0) {
                        celebrity.setProfessions(professions);
                    }
                } else if ("更多外文名".equals(text)) {
                    String[] aka_en = span.nextSibling().toString()
                            .replaceAll(":", "").trim().split("/");
                    if (aka_en != null && aka_en.length > 0) {
                        celebrity.setAka_en(aka_en);
                    }
                } else if ("imdb编号".equals(text)) {
                    Element imdb = span.nextElementSibling();
                    if (imdb != null) {
                        String imdb_id = imdb.text();
                        String imdb_url = imdb.attr("href");
                        celebrity.setImdb_id(imdb_id);
                        celebrity.setImdb_url(imdb_url);
                    }

                }
            }

        }

        return celebrity;
    }



}
