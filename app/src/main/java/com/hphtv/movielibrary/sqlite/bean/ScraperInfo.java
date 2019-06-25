package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;

/**
 * @author lxp
 * @date 19-3-29
 */
public class ScraperInfo implements Serializable {
    private long id;
    private int api;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getApi() {
        return api;
    }

    public void setApi(int api) {
        this.api = api;
    }

    @Override
    public String toString() {
        return "ScraperInfo{" +
                "id=" + id +
                ", api=" + api +
                '}';
    }
}
