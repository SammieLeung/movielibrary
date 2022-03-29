package com.hphtv.movielibrary.data;

import com.station.device.TokenHelper;

/**
 * author: Sam Leung
 * date:  2022/3/28
 */
public class AuthHelper {
    public static String sToken="";
    static {
        sToken=TokenHelper.getToken(TokenHelper.PRE_CN);
    }
}
