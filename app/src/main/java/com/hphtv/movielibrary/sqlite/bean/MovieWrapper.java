package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author lxp
 * @date 19-3-29
 */
public class MovieWrapper implements Serializable {
    private long id;
    private ScraperInfo[] scraper_infos;
    private Long[] file_ids;
    private Long[] dev_ids;
    private Long[] dir_ids;
    private String title;
    private String poster;
    private String average;
    private String title_pinyin;

    public String getTitlePinyin() {
        return title_pinyin;
    }

    public void setTitlePinyin(String title_pinyin) {
        this.title_pinyin = title_pinyin;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ScraperInfo[] getScraperInfos() {
        return scraper_infos;
    }

    public void setScraperInfos(ScraperInfo[] scraper_infos) {
        this.scraper_infos = scraper_infos;
    }

    public Long[] getFileIds() {
        return file_ids;
    }

    public void setFileIds(Long[] file_ids) {
        this.file_ids = file_ids;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }



    public Long[] getDevIds() {
        return dev_ids;
    }

    public void setDevIds(Long[] devId) {
        this.dev_ids = devId;
    }

    public Long[] getDirIds() {
        return dir_ids;
    }

    public void setDirIds(Long[] dirId) {
        this.dir_ids = dirId;
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    @Override
    public String toString() {
        return "MovieWrapper{" +
                "id=" + id +
                ", scraper_infos=" + Arrays.toString(scraper_infos) +
                ", file_ids=" + Arrays.toString(file_ids) +
                ", dev_ids=" + Arrays.toString(dev_ids) +
                ", dir_ids=" + Arrays.toString(dir_ids) +
                ", title='" + title + '\'' +
                ", poster='" + poster + '\'' +
                ", average='" + average + '\'' +
                ", title_pinyin='" + title_pinyin + '\'' +
                '}';
    }
}
