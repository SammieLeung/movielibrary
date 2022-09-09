package com.hphtv.movielibrary.util;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.webkit.MimeTypeMap;


import com.hphtv.movielibrary.R;
import com.station.kit.util.FileUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
/**
 * author: Sam Leung
 * date:  2022/9/9
 */
public class MimeTypes {
    private static MimeTypes mimeTypes;

    private Map<String, String> mExtensionsToTypes = new HashMap<>();
    private Map<String, Integer> mTypesToIcons = new HashMap<>();

    public static MimeTypes getInstance() {
        if (mimeTypes == null) {
            throw new IllegalStateException("MimeTypes must be initialized with newInstance");
        }
        return mimeTypes;
    }

    /**
     * Use this instead of the default constructor to get a prefilled object.
     */
    public static void initInstance(Context c) {
        MimeTypeParser mtp = null;
        try {
            mtp = new MimeTypeParser(c, c.getPackageName());
        } catch (NameNotFoundException e) {
            // Should never happen
        }

        XmlResourceParser in = c.getResources().getXml(R.xml.mimetypes);

        try {
            mimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public void put(String extension, String type, int icon) {
        put(extension, type);
        mTypesToIcons.put(type, icon);
    }

    public void put(String extension, String type) {
        // Convert extensions to lower case letters for easier comparison
        extension = extension.toLowerCase(Locale.ROOT);
        mExtensionsToTypes.put(extension, type);
    }

    public String getMimeType(String filename) {
        String extension = FileUtils.getFileExt(filename);

        // Let's check the official map first. Webkit has a nice extension-to-MIME map.
        // Be sure to remove the first character from the extension, which is the "." character.
        if (extension.length() > 0) {
            String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));

            if (webkitMimeType != null) {
                // Found one. Let's take it!
                return webkitMimeType;
            }
        }

        // Convert extensions to lower case letters for easier comparison
        extension = extension.toLowerCase();

        String mimetype = mExtensionsToTypes.get(extension);

        if (mimetype == null) {
            mimetype = "*/*";
        }

        return mimetype;
    }

    public int getIcon(String mimetype) {
        Integer iconResId = mTypesToIcons.get(mimetype);
        if (iconResId == null)
            return 0; // Invalid identifier
        return iconResId;
    }
}
