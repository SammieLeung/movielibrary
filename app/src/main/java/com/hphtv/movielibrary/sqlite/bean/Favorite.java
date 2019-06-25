package com.hphtv.movielibrary.sqlite.bean;

import java.io.Serializable;

/**
 * Created by tchip on 18-5-9.
 */

public class Favorite implements Serializable {
    private long id;
private long wrapper_id=-1;

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

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", wrapper_id=" + wrapper_id +
                '}';
    }
}
