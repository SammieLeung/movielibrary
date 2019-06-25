package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;

/**
 * Created by tchip on 18-3-26.
 */

public class VideoFile implements Serializable {
    private long id;
    private long wrapper_id =-1;
    private String uri;
    private String filename;
    private String search_name;
    private String thumbnail;
    private String thumbnail_s;
    private String dir_id;
    private int is_matched=0;//1已匹配 0未匹配
    private String title_pinyin;


    public int isMatched() {
        return is_matched;
    }

    public void setMatched(int matched) {
        is_matched = matched;
    }

    public String getSearchName() {
        return search_name;
    }

    public void setSearchName(String search_name) {
        this.search_name = search_name;
    }

    public String getTitlePinyin() {
        return title_pinyin;
    }

    public void setTitlePinyin(String title_pinyin) {
        this.title_pinyin = title_pinyin;
    }

    @Override
    public String toString() {
        return "VideoFile{" +
                "id=" + id +
                ", wrapper_id=" + wrapper_id +
                ", uri='" + uri + '\'' +
                ", filename='" + filename + '\'' +
                ", search_name='" + search_name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", thumbnail_s='" + thumbnail_s + '\'' +
                ", dir_id='" + dir_id + '\'' +
                ", is_matched=" + is_matched +
                ", title_pinyin='" + title_pinyin + '\'' +
                ", dev_id=" + dev_id +
                '}';
    }

    public String getDir_id() {
        return dir_id;
    }

    public void setDir_id(String dir_id) {
        this.dir_id = dir_id;
    }

    public long getDev_id() {
        return dev_id;
    }

    public void setDev_id(long dev_id) {
        this.dev_id = dev_id;
    }

    private long dev_id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWrapper_id() {
        return wrapper_id;
    }

    public void setWrapper_id(long wrapper_id) {
        this.wrapper_id = wrapper_id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnail_s() {
        return thumbnail_s;
    }

    public void setThumbnail_s(String thumbnail_s) {
        this.thumbnail_s = thumbnail_s;
    }


}
