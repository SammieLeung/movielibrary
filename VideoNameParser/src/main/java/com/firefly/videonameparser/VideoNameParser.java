package com.firefly.videonameparser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.firefly.videonameparser.bean.AudioCodec;
import com.firefly.videonameparser.bean.Country;
import com.firefly.videonameparser.bean.Episode;
import com.firefly.videonameparser.bean.FileSize;
import com.firefly.videonameparser.bean.OtherItem;
import com.firefly.videonameparser.bean.Resolution;
import com.firefly.videonameparser.bean.SubTitle;
import com.firefly.videonameparser.bean.VideoCodec;
import com.firefly.videonameparser.bean.Year;
import com.firefly.videonameparser.utils.StringUtils;

//import com.google.common.collect.Collections2;
//import com.google.common.collect.Lists;

import android.text.TextUtils;
import android.util.Log;


public class VideoNameParser {
    private static final String TAG = "VideoNameParser";
    /*
     * 视频名称解析依据
     * * http://wiki.xbmc.org/index.php?title=Adding_videos_to_the_library/Naming_files/TV_shows
     * * http://wiki.xbmc.org/index.php?title=Advancedsettings.xml#.3Ctvshowmatching.3E
     */
    private final static int maxSegments = 3;
    private final static String[] movieKeywords = {
            "2160p",
            "1080p",
            "720p",
            "480p",
            "blurayrip",
            "brrip",
            "divx",
            "dvdrip",
            "hdrip",
            "hdtv",
            "tvrip",
            "xvid",
            "camrip",
            "hd",
            "4k",
            "2k",
            "bd"
    };
    private static final String[] SEASON_WORDS = {
            "s",
            "season",
            "saison",
            "seizoen",
            "serie",
            "seasons",
            "saisons",
            "series",
            "tem",
            "temp",
            "temporada",
            "temporadas",
            "stagione"
    };

    private static final String[] EPISODE_WORDS = {
            "e",
            "episode",
            "episodes",
            "eps",
            "ep",
            "episodio",
            "episodios",
            "capitulo",
            "capitulos"
    };
    private final static String SEGMENTS_SPLIT = "\\.| |-|;|_";
    private final static String MATCH_FILES = "/.mp4$|.mkv$|.avi$/";
    private final static int minYear = 1900, maxYear = 2060;
    private final static String[] excluded = {};


    private String simplifyName(String name) {
        if (name == null || name.length() == 0) return "";
        String[] arrays = name.toLowerCase()
                .trim()
                .replace("/\\([^\\(]+\\)$/", "") // remove brackets at end
                .replace("/&/g", "and")
                .replace("/[^0-9a-z ]+/g", " ") // remove special chars
                .split(" ");
        return join("", arrays);
        //.split(" ").filter(function(r){return r}).join(" ")
    }

    MovieNameInfo mInfo;

    public MovieNameInfo parseVideoName(String filePath) {
        if (StringUtils.checkChina(filePath)) {
            filePath = StringUtils.ChineseToEnglish(filePath);
        }
        mInfo = new MovieNameInfo();
        String[] segments = slice(reverse(filePath
                .replace("\\", "/") // Support Windows slashes, lol
                .split("/")), 0, maxSegments);
        String[] firstNameSplit = segments[0].split("\\.| |_");//file name
        mInfo.setExtension(firstNameSplit != null ? firstNameSplit[firstNameSplit.length - 1] : "");
        String firstName = segments[0].replaceAll("\\.| |_", "");
        for (String seg : segments) {
            parserYear(seg);
            parserAired(seg);
            parserEpisode(seg);
        }

        /*
         * This stamp must be tested before splitting (by dot)
         * a pattern of the kind [4.02]
         * This pattern should be arround characters which are not digits and not letters
         * */

        if (!(mInfo.saneSeason() && mInfo.saneEpisode()) && mInfo.getYear() == 0) {
            String[] dotStampMatch = matcher("[^\\da-zA-Z](\\d\\d?)\\.(\\d\\d?)[^\\da-zA-Z]", segments[0]);
            if (dotStampMatch != null && dotStampMatch.length >= 3) {
                //Log.v(TAG, "dotStampMatch:"+dotStampMatch[0]);
                mInfo.setSeason(Integer.parseInt(dotStampMatch[1]));
                ArrayList<Integer> tmp = new ArrayList<Integer>();
                tmp.add(Integer.parseInt(dotStampMatch[2]));
                mInfo.setEpisode(tmp);
            }
        }

        /*
         *  A stamp of the style "804", meaning season 8, episode 4
         * */
        if (!(mInfo.saneSeason() && mInfo.saneEpisode())) {
            String stamp = null;

            /* search from the end */
            for (String x : reverse(firstNameSplit)) {

                if (x.matches("\\d\\d\\d\\d?(e|E)"))// This is a weird case, but I've seen it: dexter.801e.720p.x264-kyr.mkv
                {
                    x = x.substring(0, x.length() - 1);
                } else if (x.matches("(s|S)\\d\\d\\d\\d?"))// This is a weird case, but I've seen it: dexter.s801.720p.x264-kyr.mkv
                {
                    x = x.substring(1);
                }
                //Log.v(TAG, "x:"+x);
                /* 4-digit only allowed if this has not been identified as a year */
                if (!TextUtils.isEmpty(x) && TextUtils.isDigitsOnly(x) && (x.length() == 3 || (mInfo.getYear() == 0 && x.length() == 4))) {
                    /* Notice how always the first match is choosen ; the second might be a part of the episode name (e.g. "Southpark - 102 - weight gain 4000");
                     * that presumes episode number/stamp comes before the name, which is where most human beings would put it */
                    stamp = x;
                    break;
                }
            }
            //Log.v(TAG, "stamp:"+stamp);
            /* Since this one is risky, do it only if we haven't matched a year (most likely not a movie)
             * or if year is BEFORE stamp, like: show.2014.801.mkv */
            if (!TextUtils.isEmpty(stamp) && TextUtils.isDigitsOnly(stamp) && (mInfo.getYear() == 0
                    || (mInfo.getYear() != 0 && (firstName.indexOf(stamp) < firstName.indexOf(mInfo.getYear()))))) {
                String episode = stamp.substring(stamp.length() - 2);
                String season = stamp.substring(0, stamp.length() - 2);
                //Log.v(TAG, "season:"+season+",episode:"+episode);

                mInfo.setSeason(Integer.parseInt(season));
                mInfo.setEpisode(Integer.parseInt(episode));
            }
        }

        /*
         * "season 1", "season.1", "season1"
         * */
        if (!mInfo.saneSeason()) {
            //Log.v(TAG,"segments:"+segments.length);
            String segments_str = join("/", segments);
            String[] seasonMatch = matcher("season(\\.| )?(\\d{1,2})", segments_str);
            if (seasonMatch != null && seasonMatch.length > 0) {
                String season = join("", matcher("\\d", seasonMatch[0]));
                mInfo.setSeason(Integer.parseInt(season));
                //Log.v(TAG,"season:"+season);
            }

            String[] seasonEpMatch = matcher("Season (\\d{1,2}) - (\\d{1,2})", segments_str);
            if (seasonEpMatch != null && seasonEpMatch.length > 0) {
                String season = seasonEpMatch[1];
                String episode = seasonEpMatch[2];
                mInfo.setSeason(Integer.parseInt(season));
                mInfo.setEpisode(Integer.parseInt(episode));
                //Log.v(TAG,"season:"+season+",episode:"+episode);
            }
        }
        /*
         * "episode 13", "episode.13", "episode13", "ep13", etc.
         * */
        if (!mInfo.saneEpisode()) {
            /* TODO: consider the case when a hyphen is used for multiple episodes ; e.g. e1-3*/
            String segments_str = join("/", segments);
            String[] episodeMatch = matcher("ep(isode)?(\\.| )?(\\d+)", segments_str);
            if (episodeMatch != null && episodeMatch.length > 0) {
                String episode = join("", matcher("\\d", episodeMatch[0]));
                mInfo.setEpisode(Integer.parseInt(episode));
                //Log.v(TAG,"episode:"+episode);
            }
        }

        /*
         * Which part (for mapsList which are split into .cd1. and .cd2., etc.. files)
         * TODO: WARNING: this assumes it's in the filename segment
         *
         * */
        String[] diskNumberMatch = matcher("[ _.-]*(?:cd|dvd|p(?:ar)?t|dis[ck]|d)[ _.-]*(\\d{1,2})[^\\d]*", segments[0]);/* weird regexp? */
        if (diskNumberMatch != null && diskNumberMatch.length > 0) {
            int diskNumber = Integer.parseInt(diskNumberMatch[1]);
            mInfo.setDiskNumber(diskNumber);
            //Log.v(TAG,"diskNumber:"+diskNumber);
        }


        /*
         * The name of the series / movie
         * TODO
         * */
        boolean isSample = false;
        for (String seg : segments) {
            //Log.v(TAG, "seg1:"+seg);
            /* Remove extension */
            if (seg.lastIndexOf(".") > -1) {
                seg = seg.substring(0, seg.lastIndexOf("."));//remove "."
            }

            String[] sourcePrefix = matcher("\\[(.*?)\\]", seg);
			
/*			 seg:[HYSUB]ONE PUNCH MAN[10][GB_MP4][1280X720].mp4
			 sourcePrefix[0]:[HYSUB]
			 sourcePrefix[1]:HYSUB
			 sourcePrefix[2]:[10]
			 sourcePrefix[3]:10
			 sourcePrefix[4]:[GB_MP4]
			 sourcePrefix[5]:GB_MP4
			 sourcePrefix[6]:[1280X720]
			 sourcePrefix[7]:1280X720
*/
            ArrayList<String> removeWords = new ArrayList<String>();
            if (sourcePrefix != null && sourcePrefix.length > 0) {
                for (int i = 0; i < (sourcePrefix.length) / 2; i++) {

                    String key = sourcePrefix[i * 2];
                    String value = sourcePrefix[i * 2 + 1];
                    //Log.v("sjfq", "key:"+key);

                    if (StringUtils.hasHttpUrl(value)) {
                        //Log.v("sjfq", "hasHttpUrl removeWords:"+key);
                        removeWords.add(key);
                        continue;
                    }

                    Country country = Country.parser(value);
                    if (country != null) {
                        //Log.v("sjfq", "setCountry removeWords:"+key);
                        mInfo.setCountry(country.code);
                        removeWords.add(key);
                        continue;
                    }

                    int year = Year.parser(value);
                    if (year > 0) {
                        //Log.v("sjfq", "Year removeWords:"+key);
                        mInfo.setYear(year);
                        removeWords.add(key);
                        continue;
                    }

                    Episode episode = Episode.parser(value);
                    if (episode != null) {
                        //Log.v("sjfq", "Episode removeWords:"+key);
                        mInfo.setEpisode(episode.episode);
                        mInfo.setSeason(episode.season);
                        removeWords.add(key);
                        continue;
                    }

                    Resolution resolution = Resolution.parser(value);
                    if (resolution != null) {
                        //Log.v("sjfq", "resolution removeWords:"+key);
                        mInfo.pushTag(resolution.tag);
                        removeWords.add(key);
                        continue;
                    }


                    VideoCodec videoCodec = VideoCodec.parser(value);
                    if (videoCodec != null) {
                        mInfo.setVideoCodec(videoCodec.codec);
                        removeWords.add(key);
                        continue;
                    }

                    AudioCodec audioCodec = AudioCodec.parser(value);
                    if (audioCodec != null) {
                        mInfo.setAudioCodec(audioCodec.codec);
                        removeWords.add(key);
                        continue;
                    }

                    FileSize fileSize = FileSize.parser(value);
                    if (fileSize != null) {
                        mInfo.setFileSize(fileSize.size);
                        removeWords.add(key);
                        continue;
                    }

                    if (SubTitle.parser(value)) {
                        //Log.v("sjfq", "SubTitle removeWords:"+key);
                        removeWords.add(key);
                        continue;
                    }

                    OtherItem otherItem = OtherItem.parser(value);
                    if (otherItem != null) {
                        mInfo.pushTag(otherItem.tag);
                        removeWords.add(key);
                        continue;
                    }
                }
            }

            //排除
            //      阳光电影www.ygdy8.com.
            //		阳光电影www.ygdy8.com
            //		阳光电影_www.ygdy8.com
            //		阳光电影-www.ygdy8.com
            //		阳光电影.www.ygdy8.com
            //		阳光电影|www.ygdy8.com
            if (!TextUtils.isEmpty(seg)) {
                String regex = "^[\u4e00-\u9fa5]+[-_\\|\\.]?(([a-z0-9]+\\.)+(com|net|cn)\\.|www\\.([a-z0-9]+\\.){2,6})";
//				String regex = "^[\u4e00-\u9fa5]+[-_\\|\\.]?"
//						+ "(((https|http|ftp|rtsp|mms)?://)"
//						+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@
//						+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 199.194.52.184
//						+ "|" // 允许IP和DOMAIN（域名）
//						+ "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.
//						+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名
//						+ "[a-z]{2,6})" // first level domain- .com or .museum
//						+ "(:[0-9]{1,4})?" // 端口- :80
//						+ "((/?)|" // a slash isn't required if there is no file name
//						+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?))\\.?";

                String[] httpPreTests = matcher(regex, seg);
                if (httpPreTests.length > 0) {
                    removeWords.add(httpPreTests[0]);
                }
            }

            seg = StringUtils.removeAll(seg, removeWords).trim();
            //Log.v("sjfq","seg:"+seg);

            sourcePrefix = matcher("\\[.*?\\]", seg);
            if (sourcePrefix != null && sourcePrefix.length > 1)// Keep only first title from this filepart, as other ones are most likely release group.
            {
                seg = seg.replace(sourcePrefix[sourcePrefix.length - 1], "");
            }

            //Log.v("sjfq","seg2:"+seg);



            /*
             * WARNING: we must change how this works in order to handle cases like
             * "the office[1.01]" as well as "the office [1.01]"; if we split those at '[' or ']', we will get the name "the office 1 10"
             * For now, here's a hack to fix this
             */
            int squareBracket = seg.indexOf("[");
            if (squareBracket > -1) {
                if (squareBracket == 0) {
                    if (seg.indexOf("]") == seg.length() - 1) { //[导火新闻线]
                        seg = seg.replaceAll("[\\[\\]]", " ").trim();
                    } else {//
                        if (seg.indexOf("]") > -1)
                            seg = seg.substring(seg.indexOf("]") + 1);
                    }
                }
//				else{
//					seg = seg.replaceAll("[\\[\\]]", " ").trim();
//				}
//				else{ //the office [1.01]
//					seg = seg.substring(0, squareBracket);
//				}
                seg = seg.replaceAll("[\\[\\]]", " ").trim();
            }


            //Log.v(TAG, "seg3:"+seg);

            //FooBar --> Foo Bar
            seg = seg.replaceAll("[A-Z]", " $0").trim();
            //String[] segSplit = seg.split("\\.| |-|;|_");
            String[] segSplit = seg.split(SEGMENTS_SPLIT);
            isSample = seg.matches("^sample") || seg.matches("^etrg");
            /* No need to go further;  */
            if (!TextUtils.isEmpty(mInfo.name))
                break;

            ArrayList<String> nameParts = new ArrayList<String>();
            int lastIndex = -1;
            for (int i = 0; i < segSplit.length; i++) {

                String word = segSplit[i];
                //Log.v(TAG, "word:"+word);
                lastIndex = i;
                /* words with basic punctuation and two-digit numbers; or numbers in the first position */
                String[] x = {"ep", "episode", "season"};
                if (!(isChinese(word) || word.matches("^[a-zA-Z,?!'&]*$") || (!isNaN(word) && word.length() <= 2) || (!isNaN(word) && i == 0))
                        //                || contain(excluded,word.toLowerCase())
                        || ((indexOf(x, word.toLowerCase()) > -1) && !isNaN(segSplit[i + 1])) || indexOf(movieKeywords, word.toLowerCase()) > -1) // TODO: more than that, match for stamp too
                    break;
                nameParts.add(word);
            }
            //Log.v(TAG, "nameParts.size():"+nameParts.size());
            if (nameParts.size() == 0)
                nameParts.add(seg);
//			if (nameParts.size() == 1 && !isNaN(nameParts.get(0))) break; /* Only a number: unacceptable */ 

            ArrayList<String> parts = new ArrayList<String>();
            for (String part : nameParts) {
                //Log.v(TAG, "part:"+part);
                if (part != null && part.length() > 0) {
                    parts.add(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
                }
            }
            String name = join(" ", parts.toArray(new String[0]));
            mInfo.setName(name);
        }

        isSample = isSample || (segments.length > 1 && !TextUtils.isEmpty(segments[1]) && segments[1].toLowerCase().equals("sample")); /* The directory where the file resides */

        boolean canBeMovie = mInfo.getYear() != 0
                || mInfo.getDiskNumber() != 0
                || checkMovieKeywords(join("/", segments));
        if (mInfo.hasName()) {
            if (mInfo.hasAired()) {
                mInfo.setType(MovieNameInfo.TYPE_SERIES);
            }

            if (mInfo.saneSeason() && mInfo.saneEpisode()) {
                mInfo.setType(MovieNameInfo.TYPE_SERIES);
            } else if (canBeMovie) {
                mInfo.setType(MovieNameInfo.TYPE_MOVIE);
            } else if (mInfo.getType() != null && mInfo.getType().equals(MovieNameInfo.TYPE_MOVIE) && mInfo.saneSeason())// Must be deprioritized compared to mapsList
            {
                mInfo.setType(MovieNameInfo.TYPE_EXTRAS);
            } else {
                mInfo.setType(MovieNameInfo.TYPE_OTHER);
            }
        } else {
            mInfo.setType(MovieNameInfo.TYPE_OTHER);
        }

        if (filePath.matches("(.*)1080(p|P)(.*)")) {
            mInfo.pushTag("hd");
            mInfo.pushTag("1080p");
        } else if (filePath.matches("(.*)720(p|P)(.*)")) {
            mInfo.pushTag("720p");
        } else if (filePath.matches("(.*)480[p|P](.*)")) {
            mInfo.pushTag("480p");
        }

        if (isSample) mInfo.pushTag("sample");

        return mInfo;
    }

    /*
     * Test for a year in the name
     * */
    private final static String MATCH_YEAR_REGEX = "[^\\d](19[0-9][0-9]|20[0-9][0-9])[^\\d]";//匹配４位数字,范围为1900-2099

    private void parserYear(String seg) {
        String[] numbers = matcher(MATCH_YEAR_REGEX, seg);
        if (numbers != null && numbers.length > 1) {
            //Log.v(TAG, "year:"+numbers[1]);
            mInfo.setYear(Integer.parseInt(numbers[1]));
        }
    }

    /*
     * Test for "aired" stamp; if aired stamp is there, we have a series
     */
    private final static String MATCH_AIRED_REGEX = "(19[0-9][0-9]|20[0-1][0-9])(\\.|-| )(\\d\\d)(\\.|-| )(\\d\\d)";//匹配４位数字,范围为1900-2099

    void parserAired(String seg) {
        String[] aired = matcher(MATCH_AIRED_REGEX, Pattern.CASE_INSENSITIVE, seg);
        if (aired != null && aired.length > 0) {
            //Log.v(TAG, "aired:"+aired[0]);
            //			String year = aired[1];
            //			String month = aired[3];
            //			String day = aired[5];
            mInfo.setAired(aired[0]);
        }
    }

    /*
     * A typical pattern - "s05e12", "S01E01", etc. ; can be only "E01"
     * Those are matched only in the file name
     *
     * TODO: this stamp may be in another segment (e.g. directory name)
     *
     * season,episode,
     * */
    private void parserEpisode(String seg) {
        String[] splits = seg.split("\\.| |_");
        for (String split : splits) {
            //Log.v(TAG, "split:"+split);
            String season_regex = "^" + build_or_pattern(SEASON_WORDS) + "(\\d{1,2})";//"S(\\d{1,2})"
            String[] seasonMatch = matcher(season_regex, Pattern.CASE_INSENSITIVE, split);
            if (seasonMatch != null && seasonMatch.length > 1) {
                mInfo.setSeason(Integer.parseInt(seasonMatch[1]));
            }
            String episode_regex = build_or_pattern(EPISODE_WORDS) + "(\\d{1,2})(?:-(\\d{1,2}))?";//"E(\\d{1,2})"
            String[] episodeMatch = matcher(episode_regex, Pattern.CASE_INSENSITIVE, split);
            if (episodeMatch != null && episodeMatch.length > 1) {
                ArrayList<Integer> episode = new ArrayList<Integer>();
                if (episodeMatch[0].contains("-")) {
                    if (episodeMatch.length == 3) {
                        for (int i = Integer.parseInt(episodeMatch[1]); i <= Integer.parseInt(episodeMatch[2]); i++) {
                            episode.add(i);
                        }
                    }
                } else {
                    for (int i = 1; i < episodeMatch.length; i++) {
                        if (episodeMatch[i] != null)
                            episode.add(Integer.parseInt(episodeMatch[i]));
                    }
                }
                mInfo.setEpisode(episode);
            }

            String[] xStampMatch = matcher("(\\d\\d?)x(\\d\\d?)", Pattern.CASE_INSENSITIVE, split);

        }
        String[] fullMatch = matcher("^([a-zA-Z0-9,-?!'& ]*) S(\\d{1,2})E(\\d{2})", seg.replace("\\.| |;|_", " "));
        //if (TextUtils.isEmpty(mInfo.getName()) && meta.season && meta.episode && fullMatch && fullMatch[1]) meta.name = fullMatch[1];
    }

    private String[] matcher(String regex, String input) {
        return matcher(regex, Pattern.CASE_INSENSITIVE, input);
    }

    private String[] matcher(String regex, int flag, String input) {
        Pattern pattern = Pattern.compile(regex, flag);
        Matcher matcher = pattern.matcher(input);
        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
            //Log.v(TAG, "matcher.groupCount():"+matcher.groupCount());
            for (int i = 0; i <= matcher.groupCount(); i++) {
                list.add(matcher.group(i));
                //Log.v(TAG, "matcher.group("+i+"):"+matcher.group(i));
            }
            //Log.v(TAG, "list:"+list.size());
            //			list.add(matcher.group());
            //			//Log.v(TAG, "matcher.group():"+matcher.group());
        }

        return list.toArray(new String[0]);
    }

    private static String[] reverse(String[] Array) {
        String[] new_array = new String[Array.length];
        for (int i = 0; i < Array.length; i++) {
            // 反转后数组的第一个元素等于源数组的最后一个元素：
            new_array[i] = Array[Array.length - i - 1];
        }
        return new_array;
    }

    private static String[] slice(String[] Array, int start, int end) {
        if (end >= Array.length - 1) end = Array.length - 1;
        if (start < 0) start = 0;
        int length = end - start + 1;
        String[] new_array = new String[length];
        for (int i = 0; i < length; i++) {
            // 反转后数组的第一个元素等于源数组的最后一个元素：
            new_array[i] = Array[i]; //????
        }
        return new_array;
    }


    public static String join(String join, String[] strAry) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strAry.length; i++) {
            if (i == (strAry.length - 1)) {
                sb.append(strAry[i]);
            } else {
                sb.append(strAry[i]).append(join);
            }
        }
        return new String(sb);
    }

    public static boolean isNaN(String num) {
        if (!TextUtils.isEmpty(num) && TextUtils.isDigitsOnly(num)) {
            return false;
        }
        return true;
    }

    public static boolean isChinese(String word) {
        if (word == null)
            return false;
        for (char c : word.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FA5)    // 根据字节码判断
                return true;// 有一个中文字符就返回
        }
        return false;
    }

    public static boolean isOnlyChinese(String word) {
        if (word == null)
            return false;
        for (char c : word.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FA5)    // 根据字节码判断
                continue;
            else
                return false;
        }
        return true;
    }

    public static boolean contain(String[] array, String str) {
        if (array == null || array.length == 0) return false;
        List<String> list = Arrays.asList(array);
        return list.contains(str);
    }

    public static int indexOf(String[] array, String str) {
        if (array == null || array.length == 0) return -1;
        List<String> list = Arrays.asList(array);
        return list.indexOf(str);
    }

    public static boolean checkMovieKeywords(String str) {
        if (str == null || str.length() == 0) return false;
        for (String keyWord : movieKeywords) {
            if (str.toLowerCase().indexOf(keyWord) > -1) {
                return true;
            }
        }
        return false;
    }

    private static String build_or_pattern(String[]... patterns) {
        StringBuilder result = new StringBuilder();
        for (String[] strings : patterns) {
            for (String string : strings) {
                if (result.length() == 0) {
                    result.append("(?");
                    result.append(":");
                } else {
                    result.append("|");
                }
                result.append(StringUtils.escapeExprSpecialWord(string));
//					result.append(string);
                //result.append(String.format("(?:%s)",string));
            }

        }
        result.append(")");
        if (result.length() == 0) return null;
        return result.toString();
    }
}
