package com.hphtv.movielibrary.data;

import com.station.device.TokenHelper;

/**
 * author: Sam Leung
 * date:  2022/3/28
 */
public class AuthHelper {
    public static String sToken="";
    public static String sTokenEN="";
    public static String sPreToken="";
    public static String sPreTokenEN="";
    static {
        sToken=TokenHelper.getToken(TokenHelper.CN);
        sTokenEN=TokenHelper.getToken(TokenHelper.EN);
        sPreToken=TokenHelper.getToken(TokenHelper.PRE_CN);
        sPreTokenEN=TokenHelper.getToken(TokenHelper.PRE_EN);
    }
}
