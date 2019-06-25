package com.hphtv.movielibrary.util;


import com.firefly.videonameparser.MovieNameInfo;
import com.firefly.videonameparser.VideoNameParser;

import java.util.HashMap;

/**
 * Created by tchip on 17-10-20.
 */
public class FileScanUtil {
    public static final String TAG=FileScanUtil.class.getSimpleName();
    public static final String VIDEO_FILE = "file";
    public static final String FILE_INFO = "file_info";
    public static final String FILE_PARSE_NAME = "file_parse_name";
    public static String[] file_extensions = new String[]
            {
                    "mp4", "mov", "avi",
                    "wmv", "flv", "mkv",
                    "rmvb", "rm", "iso"
            };

    public static MovieNameInfo simpleParse(String fileName) {
        MovieNameInfo mni = (new VideoNameParser()).parseVideoName(
                fileName);
        String fileParseName = mni.getName();//文件名解析结果
        LogUtil.v(TAG, "文件名解析结果 fileParseName:" + fileParseName);
        return mni;

    }

    /**
     * 判断是否是音乐文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isMusic(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        if (ext.equals("mp3") || ext.equals("m4a") || ext.equals("wav") || ext.equals("amr") || ext.equals("awb") ||
                ext.equals("aac") || ext.equals("flac") || ext.equals("mid") || ext.equals("midi") ||
                ext.equals("xmf") || ext.equals("rtttl") || ext.equals("rtx") || ext.equals("ota") ||
                ext.equals("wma") || ext.equals("ra") || ext.equals("mka") || ext.equals("m3u") || ext.equals("pls")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是图像文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isPhoto(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        if (ext.endsWith("jpg") || ext.endsWith("jpeg") || ext.endsWith("gif") || ext.endsWith("png") ||
                ext.endsWith("bmp") || ext.endsWith("wbmp")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是视频文件
     *
     * @param extension 后缀名
     * @return
     */
    public static boolean isVideo(String extension) {
        if (extension == null)
            return false;

        final String ext = extension.toLowerCase();
        for (String file_ext : file_extensions) {
            if (ext.endsWith(file_ext))
                return true;
            else
                continue;
        }
        return false;

    }


}
