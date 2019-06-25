package com.firefly.filepicker.utils;

import android.content.Context;

import com.firefly.filepicker.data.bean.xml.SaveItem;
import com.firefly.filepicker.data.bean.xml.MediaDirs;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;

import java.io.File;
import java.util.List;

/**
 * Created by rany on 18-3-30.
 */

public class MediaDirHelper {
    private static MediaDirs MEDIA_DIRS = null;
    private static String SAVE_FILE = "media_dirs.xml";

    private static void init(Context context) {
        if (MEDIA_DIRS != null) {
            return;
        }

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        File file = new File(context.getFilesDir(), SAVE_FILE);

        try {
            MEDIA_DIRS = serializer.read(MediaDirs.class, file);
        } catch (Exception e) {
            MEDIA_DIRS = new MediaDirs();
        }
    }

    private static void save(Context context) throws Exception {
        init(context);

        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        File file = new File(context.getFilesDir(), SAVE_FILE);

        serializer.write(MEDIA_DIRS, file);
    }

    public static MediaDirs getMediaDirs(Context context) {
        init(context);

        return MEDIA_DIRS;
    }

    public static void saveMediaDirs(Context context) {
        try {
            save(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void add(Context context, SaveItem saveItem) {
        init(context);

        MEDIA_DIRS.addItem(saveItem);
    }

    public static void addAndSave(Context context, SaveItem saveItem) {
        add(context, saveItem);
        try {
            save(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<SaveItem> getDLNAs(Context context) {
        init(context);

        return MEDIA_DIRS.getDLNAs();
    }

    public static List<SaveItem> getLocals(Context context) {
        init(context);

        return MEDIA_DIRS.getLocals();
    }

    public static List<SaveItem> getSambas(Context context) {
        init(context);

        return MEDIA_DIRS.getSambas();
    }
}
