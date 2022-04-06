package com.firefly.videonameparser;

import android.text.TextUtils;

import com.firefly.videonameparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.Locale;

public class MovieNameInfo {
    public static String TYPE_SERIES = "series";
    public static String TYPE_MOVIE = "movie";
    public static String TYPE_EXTRAS = "extras";
    public static String TYPE_OTHER = "other";

    //file extension
    String extension;
    // parsed name, in lower case, allowed numbers/letters, no special symbols
    String name;
    //
    String name_cn;
    // can be movie, series or other - inferred from keywords / key phrases
    String type;
    // additional tags inferred from the name, e.g. 1080p
    ArrayList<String> tags;
    // - number of the season
    int season = 0;
    //- array of episode numbers, returned for episodes
    ArrayList<Integer> episodes;
    int diskNumber = 0;
    int year = 0;

    String aired;

    String country;

    String videoCodec;

    String audioCodec;

    String fileSize;


    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public void setEpisode(ArrayList<Integer> episodes) {
        this.episodes = episodes;
    }

    public void setEpisode(int episode) {
        if (episodes == null) episodes = new ArrayList<Integer>();
        episodes.add(episode);
    }

    public ArrayList<Integer> getEpisode() {
        return episodes;
    }

    public boolean saneEpisode() {
        return (episodes != null) && (episodes.size() > 0);
    }

    public String toEpisode(String prefix) {
        if (TextUtils.isEmpty(prefix))
            prefix = "0";
        if (saneEpisode()) {
            StringBuffer sb = new StringBuffer();
            if (episodes.size() <= 2) {
                for (int i : episodes) {
                    sb.append(prefix + i + "-");
                }
            } else {
                for (int i : episodes) {
                    sb.append(prefix + i + ",");
                }
            }
            sb.delete(sb.length() - 1,sb.length());
            return sb.toString();
        }
        return "";
    }


    public String getName() {
        if (Locale.getDefault().getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage()))
            return name_cn;
        else
            return name;
    }

    public void setName(String name) {
        String[] nameParts = name.split(" ");
        if (nameParts.length == 1) {
            this.name = name;
            this.name_cn = name;
        } else {
            StringBuffer sb_cn=new StringBuffer();
            if (StringUtils.hasGB2312(nameParts[0])) {
                for(int i=0;i<nameParts.length;i++){
                        sb_cn.append(StringUtils.getOnlyGB2312(nameParts[i]));
                }
                this.name_cn = sb_cn.toString();
                if(name.length()-1==name.indexOf(sb_cn.charAt(sb_cn.length()-1))){
                   this.name=name_cn;
                }else {
                    this.name = name.substring(name.indexOf(sb_cn.charAt(sb_cn.length() - 1))+1).trim();
                }
            } else {
                this.name = name;
                this.name_cn = name;
            }
        }
    }

    public String getAired() {
        return aired;
    }

    public void setAired(String aired) {
        this.aired = aired;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean saneSeason() {
        return season != 0;
    }

    public int getDiskNumber() {
        return diskNumber;
    }

    public void setDiskNumber(int diskNumber) {
        this.diskNumber = diskNumber;
    }

    public boolean hasName() {
        return name != null && name.length() > 0;
    }

    public boolean hasAired() {
        return aired != null && aired.length() > 0;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void pushTag(String tag) {
        if (tags == null) tags = new ArrayList<String>();
        tags.add(tag);
    }

    @Override
    public String toString() {
        return "MovieNameInfo{" +
                "extension='" + extension + '\'' +
                ", name='" + name + '\'' +
                ", name_cn='" + name_cn + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + tags +
                ", season=" + season +
                ", episodes=" + episodes +
                ", diskNumber=" + diskNumber +
                ", year=" + year +
                ", aired='" + aired + '\'' +
                ", country='" + country + '\'' +
                ", videoCodec='" + videoCodec + '\'' +
                ", audioCodec='" + audioCodec + '\'' +
                ", fileSize='" + fileSize + '\'' +
                '}';
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }


}
