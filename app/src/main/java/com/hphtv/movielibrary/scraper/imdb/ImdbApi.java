package com.hphtv.movielibrary.scraper.imdb;

/**
 * Created by tchip on 17-12-13.
 */

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.firelfy.util.LogUtil;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Celebrity;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Images;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Movie;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.Rating;
import com.hphtv.movielibrary.sqlite.bean.scraperBean.SimpleMovie;
import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.util.OkHttpUtil;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;

/**
 * http://www.theimdbapi.org/api/movie?movie_id=tt0418279
 * {
 * "title": "Transformers",
 * "content_rating": "PG-13",
 * "original_title": "",
 * "metadata": {
 * "languages": [
 * "English",
 * "Spanish"
 * ],
 * "asp_retio": "2.35 : 1",
 * "filming_locations": [
 * "Cesar E Chavez Avenue Viaduct",
 * "Downtown",
 * "Los Angeles",
 * "California",
 * "USA"
 * ],
 * "also_known_as": [
 * "Prime Directive"
 * ],
 * "countries": [
 * "USA"
 * ],
 * "gross": "$709,709,780",
 * "sound_mix": [
 * "SDDS",
 * "Dolby Digital",
 * "DTS"
 * ],
 * "budget": "$150,000,000            (estimated)"
 * },
 * "release_date": "2007-07-03",
 * "director": "Michael Bay",
 * "url": {
 * "url": "http://www.imdb.com/title/tt0418279"
 * },
 * "year": "2007",
 * "trailer": [
 * {
 * "mimeType": "video/mp4",
 * "definition": "480p",
 * "videoUrl": "https://video-http.media-imdb.com/MV5BNTMyMTFhYzgtMmY1Ny00ZTBlLWI4M2UtMDYxZTFhMTNkY2ZhXkExMV5BbXA0XkFpbWRiLWV0cy10cmFuc2NvZGU@.mp4?Expires=1513238319&Signature=TQkkOACjv936HNJjaQvxOJCGSUsRaqUXLbQ5BKZBzrO5-8Vbc7x6q2LP5cWVECwkeIkENg6ONtJBUgADo7bzVB9JPY0mXZI5AHFvxHIqlsZJiRBR-sPSi7NYX4b6hIyXVLgI7saGDlI23upFnLkZI~hDsRQJG0p45aFjGyK-9XQ_&Key-Pair-Id=APKAILW5I44IHKUN2DYA"
 * },
 * {
 * "mimeType": "video/webm",
 * "definition": "SD",
 * "videoUrl": "https://video-http.media-imdb.com/MV5BYzAxNWU5ZGItNDFjNi00YWIzLWFkOGEtMzM2MjdlNDk4OTM2XkExMV5Bd2VibV5BaW1kYi1ldHMtdHJhbnNjb2Rl.webm?Expires=1513238319&Signature=0Df14b4uAtZrrt8Leepb9AxMOBETFNQpxMcOjvqHz0K-jjYXqk4Wc9GFHACXSjToksMmHQYNyxFBroxYZhOFftiLo2bWHKX~30s8CLooPc8oE00tpRxHpAdvpF9usrw6PlE4Tx0CdQiS0LGtZ8Xfor51SgTdxUAQEwo4VIgSeYo_&Key-Pair-Id=APKAILW5I44IHKUN2DYA"
 * },
 * {
 * "mimeType": "video/mp4",
 * "definition": "SD",
 * "videoUrl": "https://video-http.media-imdb.com/MV5BNWMzZTc4NjgtMjUwZS00MThkLWIxZjItZjlkOGM4ZDliOGUzXkExMV5BbXA0XkFpbWRiLWV0cy10cmFuc2NvZGU@.mp4?Expires=1513238319&Signature=gk7cuJDm~LLuM~zJ33G36mnuLWvvJP3bIGiNgObmn2q0qTXMstessekqnv5yFEPwAGKxjZTxondp-xQbN5k2u4fzofdDUPzRPS3Y5ZHvLNMtzG5wZHCsVRftNSTADRIgMAYSlJZ-GphzeRJuEgL~UKi36pUQCxAsy~Yho6tMPrg_&Key-Pair-Id=APKAILW5I44IHKUN2DYA"
 * },
 * {
 * "mimeType": "video/mp4",
 * "definition": "SD",
 * "videoUrl": "https://video-http.media-imdb.com/MV5BMWJkYzM4ZjItMWVlNC00Zjc1LWJkOGQtZjEzZTlkNzdiZTNiXkExMV5BbXA0XkFpbWRiLWV0cy10cmFuc2NvZGU@.mp4?Expires=1513238319&Signature=alUG-g1tLMdXoKlOvIAIcV9oz80NH0Zd62jN0q5m7RvohqSKh5jIbzU-cTNohkjYiP4RsoiFGgyQhOHzyJ4CYGoEH4ruyB4wWnnJd~ZHrVcFdhlbZe-uQxXFtOFhDy-hGC0cNnBXirRPmzl9lXj2JXNimQjjwrc9qLb~2Ka8BFc_&Key-Pair-Id=APKAILW5I44IHKUN2DYA"
 * },
 * {
 * "mimeType": "video/webm",
 * "definition": "SD",
 * "videoUrl": "https://video-http.media-imdb.com/MV5BYjYwNTFhNmUtZmE2Yi00M2M0LWFlMGItMjA0MDU1ZWExNTA4XkExMV5Bd2VibV5BaW1kYi1ldHMtdHJhbnNjb2Rl.webm?Expires=1513238319&Signature=WYv3wcdjxZ0HuHZdiRLBjudMJPwgY-0DOXRzsY5xiu0cwxQiwST6axcyN5h4Ehda3KihysqCsb1FDtSxEwV9NIr7ATUsTtlEjtX8jc1oB287TPmmj8hEaLCQLcTGCD-avfyjhW44X3Xglj5qBZoOtSmlR5fpiSw7WefjhxXfLN0_&Key-Pair-Id=APKAILW5I44IHKUN2DYA"
 * }
 * ],
 * "length": "144",
 * "cast": [
 * {
 * "character": "Sam Witwicky",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMGI1NWY3MDAtYjY2YS00MTE4LTkxMDgtNzhkNzY1MWY1NmM2XkEyXkFqcGdeQXVyMjQwMDg0Ng@@._V1_UY44_CR1,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0479471/?ref_=tt_cl_t1",
 * "name": "Shia LaBeouf"
 * },
 * {
 * "character": "Mikaela Banes",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTc5MjgyMzk4NF5BMl5BanBnXkFtZTcwODk2OTM4Mg@@._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm1083271/?ref_=tt_cl_t2",
 * "name": "Megan Fox"
 * },
 * {
 * "character": "Captain Lennox",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMjE3MDUyNjM1N15BMl5BanBnXkFtZTcwMDc3NDY2Mg@@._V1_UY44_CR0,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0241049/?ref_=tt_cl_t3",
 * "name": "Josh Duhamel"
 * },
 * {
 * "character": "USAF Tech Sergeant Epps",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMjA3MjU1NzY4OF5BMl5BanBnXkFtZTgwMzU3MDQxNTE@._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0879085/?ref_=tt_cl_t4",
 * "name": "Tyrese Gibson"
 * },
 * {
 * "character": "Maggie Madsen",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMjEwMTk0OTM0OV5BMl5BanBnXkFtZTgwOTU2NTY2ODE@._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm1592225/?ref_=tt_cl_t5",
 * "name": "Rachael Taylor"
 * },
 * {
 * "character": "Glen Whitmann",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTQzMjk3MzM1NV5BMl5BanBnXkFtZTcwNDU1MDg0Mw@@._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0026364/?ref_=tt_cl_t6",
 * "name": "Anthony Anderson"
 * },
 * {
 * "character": "Defense Secretary John Keller",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTc2NTE3NDA3M15BMl5BanBnXkFtZTgwMDMyNTM1MjE@._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0000685/?ref_=tt_cl_t7",
 * "name": "Jon Voight"
 * },
 * {
 * "character": "Agent Simmons",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BNzYwNjgwMjMxMF5BMl5BanBnXkFtZTcwNjUwOTc3NQ@@._V1_UY44_CR2,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0001806/?ref_=tt_cl_t8",
 * "name": "John Turturro"
 * },
 * {
 * "character": "Tom Banacheck",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BNjUzMzI4MjE5Nl5BMl5BanBnXkFtZTcwNzU5NzQyNw@@._V1_UY44_CR5,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0642259/?ref_=tt_cl_t9",
 * "name": "Michael O'Neill"
 * },
 * {
 * "character": "Ron Witwicky",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTM1NTk4MTAwOF5BMl5BanBnXkFtZTcwODA4OTMwNA@@._V1_UY44_CR1,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0242656/?ref_=tt_cl_t10",
 * "name": "Kevin Dunn"
 * },
 * {
 * "character": "Judy Witwicky",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTg2MTMxMTgxNV5BMl5BanBnXkFtZTYwMDc2Mjk1._UY317_CR16,0,214,317_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0925033/?ref_=tt_cl_t11",
 * "name": "Julie White"
 * },
 * {
 * "character": "ACWO Jorge \"Fig\" Figueroa",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMTQ5NzQ4Njg3MF5BMl5BanBnXkFtZTgwODQ4NTUxNzE@._V1_UY44_CR11,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm1004774/?ref_=tt_cl_t12",
 * "name": "Amaury Nolasco"
 * },
 * {
 * "character": "First Sergeant Donnelly",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BMjA4ODk0MDMwM15BMl5BanBnXkFtZTcwODMxNDc4Nw@@._V1_UY44_CR11,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm0911933/?ref_=tt_cl_t13",
 * "name": "Zack Ward"
 * },
 * {
 * "character": "Ranger Team",
 * "image": "",
 * "link": "http://www.imdb.com/name/nm2698788/?ref_=tt_cl_t14",
 * "name": "Luis Echagarruga"
 * },
 * {
 * "character": "Trent DeMarco",
 * "image": "https://images-na.ssl-images-amazon.com/images/M/MV5BZTAxYmJiYmItM2U3Ny00ZjE4LWIxMTEtZDg0MDllNWRmMTdkXkEyXkFqcGdeQXVyMjQwNTI4ODI@._V1_UY44_CR2,0,32,44_AL_.jpg",
 * "link": "http://www.imdb.com/name/nm1670886/?ref_=tt_cl_t15",
 * "name": "Travis Van Winkle"
 * }
 * ],
 * "imdb_id": "tt0418279",
 * "rating": "7.1",
 * "genre": [
 * "Action",
 * "Adventure",
 * "Sci-Fi"
 * ],
 * "rating_count": "544,268",
 * "storyline": "A long time ago, far away on the planet of Cybertron, a war is being waged between the noble Autobots (led by the wise Optimus Prime) and the devious Decepticons (commanded by the dreaded Megatron) for control over the Allspark, a mystical talisman that would grant unlimited power to whoever possesses it. The Autobots managed to smuggle the Allspark off the planet, but Megatron blasts off in search of it. He eventually tracks it to the planet of Earth (circa 1850), but his reckless desire for power sends him right into the Arctic Ocean, and the sheer cold forces him into a paralyzed state. His body is later found by Captain Archibald Witwicky, but before going into a comatose state Megatron uses the last of his energy to engrave into the Captain's glasses a map showing the location of the Allspark, and to send a transmission to Cybertron. Megatron is then carried away aboard the Captain's ship. A century later, Captain Witwicky's grandson Sam Witwicky (nicknamed Spike by his friends) ...",
 * "description": "An ancient struggle between two Cybertronian races, the heroic Autobots and the evil Decepticons, comes to Earth, with a clue to the ultimate power held by a teenager.",
 * "writers": [
 * "Roberto Orci",
 * "Alex Kurtzman"
 * ],
 * "stars": [
 * "Shia LaBeouf",
 * "Megan Fox",
 * "Josh Duhamel"
 * ],
 * "poster": {
 * "large": "https://images-na.ssl-images-amazon.com/images/M/MV5BNDg1NTU2OWEtM2UzYi00ZWRmLWEwMTktZWNjYWQ1NWM1OThjXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_.jpg",
 * "thumb": "https://images-na.ssl-images-amazon.com/images/M/MV5BNDg1NTU2OWEtM2UzYi00ZWRmLWEwMTktZWNjYWQ1NWM1OThjXkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_UX182_CR0,0,182,268_AL_.jpg"
 * }
 * }
 */

/**
 * http://imdbapi.net/api
 */
public class ImdbApi {

    public static final String TAG = ImdbApi.class.getName();

    public static SimpleMovie SearchAMovieByName(String keyword) {
        Elements tr_s = SearchMovie(keyword);
        if (tr_s != null && tr_s.size() > 0) {
            SimpleMovie simpleMovie = new SimpleMovie();
            Element a = tr_s.get(0).select("a").get(0);
            String imgurl = a.child(0).attr("src");
            if (!imgurl.contains("nopicture")) {
                Matcher matcher_1 = Pattern.compile(".*(U[X|Y][0-9]{2})_.*").matcher(imgurl);
                if (matcher_1.find()) {

                    Matcher matcher_2 = Pattern.compile("CR0,0,([0-9]{2}),([0-9]{2})_").matcher(imgurl);
                    if (matcher_2.find()) {

                        String srcStr_2 = matcher_2.group(1);
                        String srcStr_3 = matcher_2.group(2);
                        imgurl = imgurl.replace(srcStr_2, srcStr_2 + "0");
                        imgurl = imgurl.replace(srcStr_3, srcStr_3 + "0");
                    }
                }
            }
            String title = a.text();
            simpleMovie.setTitle(title);
            String href = a.attr("href");
            if (href != null) {
                Matcher matcher = Pattern.compile("title/(.*)/\\?.*").matcher(href);
                if (matcher.find()) {
                    String movieId = matcher.group(1);
                    simpleMovie.setId(movieId);
                }
            }
            return simpleMovie;
        }
        return null;
    }

    public static Elements SearchMoviesByName(List<SimpleMovie> dataList, Elements elements,String keyword,int offset,int limit) {
        Elements tr_s=null;
        if(elements!=null)
            tr_s   = elements;
        else
            tr_s=SearchMovie(keyword);
        if (tr_s != null && tr_s.size() > 0) {
            if(offset>=tr_s.size())
                return null;
            for (int i =offset;i<offset+limit&&i < tr_s.size(); i++) {
                SimpleMovie simpleMovie = new SimpleMovie();
                Element a = tr_s.get(i).select("a").get(0);
                Element td=tr_s.get(i).select("td.result_text").get(0);
                Element a_title= td.child(0);
                String descrption1=td.text();
                String imgurl = a.child(0).attr("src");
                if (!imgurl.contains("nopicture")) {
                    Matcher matcher_1 = Pattern.compile(".*(U[X|Y][0-9]{2})_.*").matcher(imgurl);
                    if (matcher_1.find()) {

                        Matcher matcher_2 = Pattern.compile("CR0,0,([0-9]{2}),([0-9]{2})_").matcher(imgurl);
                        if (matcher_2.find()) {

                            String srcStr_2 = matcher_2.group(1);
                            String srcStr_3 = matcher_2.group(2);
                            imgurl = imgurl.replace(srcStr_2, srcStr_2 + "0");
                            imgurl = imgurl.replace(srcStr_3, srcStr_3 + "0");
                        }
                    }
                }
                String title = a_title.text();
                simpleMovie.setTitle(title);
                String href = a.attr("href");
                if (href != null) {
                    Matcher matcher = Pattern.compile("title/(.*)/\\?.*").matcher(href);
                    if (matcher.find()) {
                        String movieId = matcher.group(1);
                        simpleMovie.setId(movieId);
                    }
                }
                Rating mRating = new Rating();
                mRating.max = 10;
                float fRating = 0;
                mRating.average = fRating;

                String[] abstracts = {descrption1,""};
                simpleMovie.setAbstracts(abstracts);
                Images images = new Images();
                images.setLarge(imgurl);
                simpleMovie.setImages(images);
                simpleMovie.setRating(mRating);
                simpleMovie.setRatingsCounts("none");
                dataList.add(simpleMovie);
            }
        }
        return tr_s;
    }

    private static Elements SearchMovie(String keyword) {
        try {
            Response response = OkHttpUtil.getResponse(String.format(ImdbURL.SEARCH_PAGE, keyword));
            if (!response.isSuccessful()) {
                return null;
            }
            String content = response.body().string();
            Document doc = Jsoup.parse(content);
            Elements findLists = doc.getElementsByClass("findList");
            if (findLists != null & findLists.size() > 0) {
                Element findList = findLists.get(0);
                Elements tr_s = findList.getElementsByClass("findResult");
                if (tr_s != null && findLists.size() > 0) {
                    return tr_s;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Movie parserMovieInfoByTitle(String keyword) {
        try {

            org.json.JSONObject postParamJsonObject = new org.json.JSONObject();
            try {
                postParamJsonObject.put("key", ImdbURL.API_KEY);
                postParamJsonObject.put("title", keyword);
                postParamJsonObject.put("api", "json");
                postParamJsonObject.put("page", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Response response = OkHttpUtil.getPostResponseFromServer(ImdbURL.API_BASE, OkHttpUtil.buildPostRequestBody(postParamJsonObject));
//            Response response=OkHttpUtil.getResponse("https://m.imdb.com/find?q=Iron+Man+3&ref_=m_nv_sr_fn");
            if (!response.isSuccessful()) {
                return null;
            }
            String content = response.body().string();
            Log.v(TAG, "imdb=" + content);
            JSONObject jsonObj = JSON.parseObject(content);
            Movie movie = parseMovie(jsonObj);
            return movie;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Movie parserMovieInfoById(String id) {
        try {

            org.json.JSONObject postParamJsonObject = new org.json.JSONObject();
            try {
                postParamJsonObject.put("key", ImdbURL.API_KEY);
                postParamJsonObject.put("id", id);
                postParamJsonObject.put("api", "json");
                postParamJsonObject.put("year", "");
                postParamJsonObject.put("page", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Response response = OkHttpUtil.getPostResponseFromServer(ImdbURL.API_BASE, OkHttpUtil.buildPostRequestBody(postParamJsonObject));

            if (!response.isSuccessful()) {
                return null;
            }
            String content = response.body().string();
            LogUtil.v(TAG,"content="+content);
            JSONObject jsonObj = JSON.parseObject(content);
            return parseMovie(jsonObj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Movie parseMovie(JSONObject jsonObj) {
        String genresStr = jsonObj.getString("genre");
        String castsStr = jsonObj.getString("actors");
        String directorStr = jsonObj.getString("director");
        String writersStr = jsonObj.getString("writers");
        String type = jsonObj.getString("api");
        String countryStr = jsonObj.getString("country");
        String languageStr = jsonObj.getString("language");
        String images_large = jsonObj.getString("poster");
        String ratingStr = jsonObj.getString("rating");


        String movie_id = jsonObj.getString("imdb_id");
        String title = jsonObj.getString("title");
        String year = jsonObj.getString("year");
        String pub_dates = jsonObj.getString("released");
        String summery = jsonObj.getString("plot");
        String subtype = type==null?"others":type.equals("movie") ? "movie" : "tv series";
        String r_count = jsonObj.getString("votes");

        Movie movie = new Movie();
        movie.setGenres(genresStr.split(","));

        String[] castsArr = castsStr.split(",");
        Celebrity[] castCeleArr = new Celebrity[castsArr.length];
        for (int i = 0; i < castCeleArr.length; i++) {
            Celebrity cele = new Celebrity();
            cele.setName(castsArr[i]);
            castCeleArr[i] = cele;
        }
        movie.setCasts(castCeleArr);
        String[] directorsArr = directorStr.split(",");
        Celebrity[] directorCeleArr = new Celebrity[directorsArr.length];
        for (int i = 0; i < directorCeleArr.length; i++) {
            Celebrity cele = new Celebrity();
            cele.setName(directorsArr[i]);
            directorCeleArr[i] = cele;
        }
        movie.setDirectors(directorCeleArr);
        String[] writersArr = writersStr.split(",");
        Celebrity[] writersCeleArr = new Celebrity[writersArr.length];
        for (int i = 0; i < writersCeleArr.length; i++) {
            Celebrity cele = new Celebrity();
            cele.setName(writersArr[i]);
            writersCeleArr[i] = cele;
        }
        movie.setCasts(writersCeleArr);
        movie.setCountries(countryStr.split(","));
        movie.setLanguages(languageStr.split(","));
        movie.setImages(new Images(images_large, images_large, images_large));
        Rating rating = new Rating();
        rating.max = 10;
        rating.average = Float.valueOf(ratingStr);
        movie.setRating(rating);
        movie.setMovieId(movie_id);
        movie.setTitle(title);
        movie.setYear(year);
        movie.setPubDates(pub_dates.split(","));
        movie.setSummary(summery);
        movie.setSubtype(subtype);
        movie.setRatingsCount(Integer.valueOf(r_count.replaceAll(",", "")));
        movie.setApi(ConstData.Scraper.IMDB);//0为豆瓣,1为imdb
        return movie;
    }


}
