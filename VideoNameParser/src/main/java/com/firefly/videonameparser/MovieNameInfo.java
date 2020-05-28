package com.firefly.videonameparser;

import java.util.ArrayList;

public class MovieNameInfo {
    public static String TYPE_SERIES = "series";
    public static String TYPE_MOVIE = "movie";
    public static String TYPE_EXTRAS = "extras";
    public static String TYPE_OTHER = "other";

    //file extension
    String extension;
    // parsed name, in lower case, allowed numbers/letters, no special symbols
    String name;
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

    ;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
		return "MovieNameInfo [extension=" + extension + ", name=" + name
				+ ", type=" + type + ", tags=" + tags + ", season=" + season
				+ ", episodes=" + episodes + ", diskNumber=" + diskNumber
				+ ", year=" + year + ", aired=" + aired + ", country="
				+ country + ", videoCodec=" + videoCodec + ", audioCodec="
				+ audioCodec + ", fileSize=" + fileSize + "]";
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}


}
