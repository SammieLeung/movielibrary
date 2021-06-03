package com.firelfy.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by rany on 18-3-1.
 */

public class Base64Helper {
    public static String encode(String plain) {
        byte[] b = plain.getBytes(StandardCharsets.UTF_8);
        String encodeStr = Base64.encodeToString(b, Base64.DEFAULT);

        try {
            return URLEncoder.encode(encodeStr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String decode(String cipher) {
        return new String(Base64.decode(cipher, Base64.DEFAULT), StandardCharsets.UTF_8);
    }
}
