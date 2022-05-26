package com.hphtv.movielibrary;

import static org.junit.Assert.assertEquals;

import android.util.Log;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;
import com.station.kit.util.LogUtil;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testLocale(){
        Locale locale=new Locale("zh","TW");
        System.out.println(locale.getDisplayCountry());
    }

    @Test
    public void testCompare(){
        ArrayList<Integer> ep=new ArrayList<>();
        ep.add(1);
        ep.add(2);
        ep.add(6);
        ep.add(3);
        ep.add(8);
        Collections.sort(ep);
        System.out.println(ep.toString());
    }

}