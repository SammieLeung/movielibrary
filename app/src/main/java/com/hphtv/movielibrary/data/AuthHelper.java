package com.hphtv.movielibrary.data;

import android.text.TextUtils;

import com.hphtv.movielibrary.util.retrofit.RetrofitTools;
import com.station.device.TokenHelper;
import com.station.kit.util.Tools;

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

    public static void requestTokenCN() {
        if (TextUtils.isEmpty(sTokenCN)) {
            synchronized (sTokenCN) {
                if (TextUtils.isEmpty(sTokenCN)) {
                    switch (RetrofitTools.mode) {
                        case RetrofitTools.PRE:
                            try {
                                sTokenCN = TokenHelper.getToken(TokenHelper.PRE_CN);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case RetrofitTools.RELEASE:
                            try {
                                sTokenCN = TokenHelper.getToken(TokenHelper.CN);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }
        }
    }

    public static void requestTokenEN() {
        if (TextUtils.isEmpty(sTokenEN)) {
            synchronized (sTokenEN) {
                switch (RetrofitTools.mode) {
                    case RetrofitTools.PRE:
                        try {
                            sTokenEN = TokenHelper.getToken(TokenHelper.PRE_EN);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case RetrofitTools.RELEASE:
                        try {
                            sTokenEN = TokenHelper.getToken(TokenHelper.EN);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }
    }
}
