package com.hphtv.movielibrary.data;

import com.hphtv.movielibrary.util.retrofit.RetrofitTools;
import com.station.device.TokenHelper;

/**
 * author: Sam Leung
 * date:  2022/3/28
 */
public class AuthHelper {
    public static String sTokenCN = "";
    public static String sTokenEN = "";
    public static synchronized boolean init() {
        switch (RetrofitTools.mode) {
            case RetrofitTools.PRE:
                try {
                    sTokenCN = TokenHelper.getToken(TokenHelper.PRE_CN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    sTokenEN = TokenHelper.getToken(TokenHelper.PRE_EN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                break;
            case RetrofitTools.RELEASE:
                try {
                    sTokenCN = TokenHelper.getToken(TokenHelper.CN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    sTokenEN = TokenHelper.getToken(TokenHelper.EN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                break;
        }
        return true;
    }
}
