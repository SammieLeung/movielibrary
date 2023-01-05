package com.hphtv.movielibrary.device;

/**
 * author: Sam Leung
 * date:  2023/1/5
 */
public class BindStateResponse {
    int code;
    String msg;
    long time;
    BindData data;

    class BindData{
        int bind;
    }
}
