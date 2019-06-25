package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;

/**
 * Created by tchip on 17-12-5.
 */

public class Device implements Serializable {
    private long id;
    private int type;//设备类型
    private String name;
    private String path;
    private int connect_state;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConnect_state() {
        return connect_state;
    }

    public void setConnect_state(int connect_state) {
        this.connect_state = connect_state;
    }
}
