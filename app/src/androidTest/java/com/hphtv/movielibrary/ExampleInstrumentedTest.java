package com.hphtv.movielibrary;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.fourthline.cling.support.model.Res;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;
import com.firefly.videonameparser.VideoNameParser2;
import com.firefly.videonameparser.bean.AudioCodec;
import com.firefly.videonameparser.bean.Episodes;
import com.firefly.videonameparser.bean.OtherItem;
import com.firefly.videonameparser.bean.Resolution;
import com.firefly.videonameparser.bean.Source;
import com.firefly.videonameparser.bean.SubTitle;
import com.firefly.videonameparser.bean.VideoCodec;
import com.station.kit.util.LogUtil;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static final String TAG="lxp";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.station.androidunittest", appContext.getPackageName());

    }

}