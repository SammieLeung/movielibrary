package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
/**
 * author: Sam Leung
 * date:  2022/9/9
 */
public class MimeTypeParser {

    public static final String TAG_MIMETYPES = "MimeTypes";
    public static final String TAG_TYPE = "type";
    public static final String ATTR_EXTENSION = "extension";
    public static final String ATTR_MIMETYPE = "mimetype";
    public static final String ATTR_ICON = "icon";
    private static final String LOG_TAG = "MimeTypeParser";
    private XmlPullParser mXpp;
    private MimeTypes mMimeTypes;
    private Resources resources;
    private String packagename;

    public MimeTypeParser(Context ctx, String packagename) throws NameNotFoundException {
        this.packagename = packagename;
        resources = ctx.getPackageManager().getResourcesForApplication(packagename);
    }

    public MimeTypes fromXmlResource(XmlResourceParser in)
            throws XmlPullParserException, IOException {
        mXpp = in;

        return parse();
    }

    public MimeTypes parse()
            throws XmlPullParserException, IOException {

        mMimeTypes = new MimeTypes();

        int eventType = mXpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tag = mXpp.getName();

            if (eventType == XmlPullParser.START_TAG) {
                if (tag.equals(TAG_MIMETYPES)) {

                } else if (tag.equals(TAG_TYPE)) {
                    addMimeTypeStart();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (tag.equals(TAG_MIMETYPES)) {

                }
            }

            eventType = mXpp.next();
        }

        return mMimeTypes;
    }

    private void addMimeTypeStart() {
        String extension = mXpp.getAttributeValue(null, ATTR_EXTENSION);
        String mimetype = mXpp.getAttributeValue(null, ATTR_MIMETYPE);
        String icon = mXpp.getAttributeValue(null, ATTR_ICON);

        if (icon != null) {
            int id = resources.getIdentifier(icon.substring(1) /* to cut the @ */, null, packagename);
            if (id > 0) {
                mMimeTypes.put(extension, mimetype, id);
                return;
            }
        }

        mMimeTypes.put(extension, mimetype);
    }
}
