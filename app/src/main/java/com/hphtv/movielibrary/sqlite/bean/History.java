package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;

/**
 * Created by tchip on 18-5-28.
 */

public class History implements Serializable {
    private long id;
    private long wrapper_id = -1;
    private String time;//播放时间戳
    private String last_play_time;//上次播放时间

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLast_play_time() {
        return last_play_time;
    }

    public void setLast_play_time(String last_play_time) {
        this.last_play_time = last_play_time;
    }

    public long getWrapper_id() {
        return wrapper_id;
    }

    public void setWrapper_id(long wrapper_id) {
        this.wrapper_id = wrapper_id;
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", wrapper_id=" + wrapper_id +
                ", time='" + time + '\'' +
                ", last_play_time='" + last_play_time + '\'' +
                '}';
    }
}
