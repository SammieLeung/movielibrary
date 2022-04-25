package com.hphtv.movielibrary.data;

import com.hphtv.movielibrary.util.retrofit.RetrofiTools;
import com.station.device.TokenHelper;

/**
 * author: Sam Leung
 * date:  2022/3/28
 */
public class AuthHelper {
    public static String sToken = "";
    public static String sTokenEN = "";
    public static String sPreToken = "";
    public static String sPreTokenEN = "";
    public static String sTestToken = "";
    static {
        init();
    }

    public static synchronized boolean init() {
        switch (RetrofiTools.mode) {
            case RetrofiTools.TEST:
                try {
                    sTestToken = TokenHelper.getToken(TokenHelper.TEST);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }
                break;
            case RetrofiTools.PRE:
                try {
                    sPreToken = TokenHelper.getToken(TokenHelper.PRE_CN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    sPreTokenEN = TokenHelper.getToken(TokenHelper.PRE_EN);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                break;
            case RetrofiTools.RELEASE:
                try {
                    sToken = TokenHelper.getToken(TokenHelper.CN);
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
