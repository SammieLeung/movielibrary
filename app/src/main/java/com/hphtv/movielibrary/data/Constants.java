package com.hphtv.movielibrary.data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by tchip on 18-3-1.
 */

public class Constants {

    public static final long GLIDE_CACHE_VERSION = System.currentTimeMillis() / 86400000;

    public static final String PACKAGE_FILE_PICKER = "com.firefly.filepicker";
    public static final String ACTION_FILE_PICKER = "com.firefly.FILE_PICKER";
    public static final String ACTION_FAVORITE_MOVIE_CHANGE = "action.favorite.movie.change";
    public static final String ACTION_APP_UPDATE_MOVIE = "action.app.update.movie";
    public static final String ACTION_APP_REMOVE_MOVIE = "action.app.remove.movie";
    public static final String ACTION_APPEND_USER_FAVORITE="action.append.user.favorite";

    public static final int ANIMATION_DURATION=200;

    //--------------HashMap keys------------
    //1.for LeftMenuListAdapter and HomePageActivity
    public static final String ICON = "left_menu_icon";
    public static final String TEXT = "left_menu_text";

    //3.--------------for DirectoryManagerAdapter--------------
    //3.1 for HashMap key
    /**
     * 设备对象
     */
    public static final String DEVICE = "device";
    public static final String DIRECTORY = "directory";

    /**
     * 搜刮器
     */

    public interface Scraper {
        String MTIME = "MTIME";
        String OMDB = "OMDB";
        String TMDB = "TMDB";
        String TMDB_EN="TMDB_EN";
    }



    public interface DeviceType {
        int DEVICE_TYPE_LOCAL = -1;
        /**
         * 内部存储
         */
        int DEVICE_TYPE_INTERNAL_STORAGE = 1;
        /**
         * usb设备
         */
        int DEVICE_TYPE_USB = 2;
        /**
         * sd卡
         */
        int DEVICE_TYPE_SDCARDS = 3;
        /**
         * pcie
         */
        int DEVICE_TYPE_PCIE = 4;
        /**
         * hardDisk
         */
        int DEVICE_TYPE_HARD_DISK = 5;

        int DEVICE_TYPE_DLNA = 6;
        int DEVICE_TYPE_SMB = 7;
    }

    public enum SearchType implements Serializable {
        auto,//智能匹配
        movie,//电影
        tv,//电视节目
//        VARIETY_SHOW,//综艺节目
//        CARTOON,//儿童节目
//        ANIMATE,//动画
//        OTHER,//其他
    }

    public enum VideoType implements Serializable{
        movie,
        tv,
        unknow,
        child,
        animate,
        variety_show,
        other,
        custom
    }

    public enum WatchLimit {
        ALL_AGE,
        ADULT
    }


    public enum UnknownRootType{
        FOLDER,
        FILE,
        BACK
    }

    public interface FileType {
        /*全部文件*/
        int OTHER = 0; // 全部文件
        /*音频文件*/
        int AUDIO = 1; // 音频文件
        /*图片文件*/
        int IMAGE = 2; // 图片文件
        /*文本文件*/
        int TEXT = 3; // 文本文件
        /*视频文件*/
        int VIDEO = 4; // 视频文件

    }

    // 视频格式后缀
    public static final String[] VIDEO_SUFFIX = {"avi", "wmv", "mp4", "rmvb", "kkv", "3gp", "ts", "mpeg", "mpg", "mkv", "m3u8", "mov",
            "m2ts", "flv", "m2t", "mts", "vob", /*"dat",*/ "m4v", "asf", "f4v", "3g2", "m1v", "m2v", "tp", "trp", "m2p", "rm",
            "avc", "dv", "divx", "mjpg", "mjpeg", "mpe", "mp2p", "mp2t", "mpg2", "mpeg2", "m4p", "mp4ps", "ogm", "hdmov",
            "qt", "iso", "webm"};

    static {
        Arrays.sort(VIDEO_SUFFIX);
    }


    //5.---------------for MovieDetailActivity----------------------
    public interface MovieDetailMode {
        /*更新电影信息*/
        int MODE_EDIT = 1;

        /*普通模式 文件id*/
        int NORMAL_FOR_V_ID = 0;
        /*普通模式 电影id*/
        int NORMAL_FOR_M_ID = 3;
        /*只查看电影信息但不保存*/
        int MODE_OUTSIDE = 2;
        /*通过MovieWrapper打开*/
        int MODE_WRAPPER = 4;
        int MODE_UNRECOGNIZEDFILE = 5;
    }


    public interface Extras {
        //详情页
        String MODE = "mode";
        String MOVIE_ID = "movie_id";
        String SEASON="season";
        String SUCCESS_COUNT ="success_count";
        String SCANNED_COUNT="scanned_count";
        String TOTAL ="total";
        String SHORTCUT ="shortcut";
        String UNRECOGNIZE_FILE_KEYWORD = "keyword";

        String CURRENT_FRAGMENT = "current_fragment";
        //设备信息
        String DEVICE_NAME = "device_name";
        String DEVICE_STATE = "device_state";
        String DEVICE_MOUNT_PATH = "device_mount_path";
        String DEVICE_NETWORKPATH = "device_networkpath";
        String MOUNT_TYPE = "device_type";
        String QUERY_URI ="query_uri";
        String QUERY_SHORTCUT ="query_shortcut";
        String QUICKMODE="query_quick_mode";
        String SEARCH_TYPE ="search_type";
        String NETWORK_DIR_PATH="network_dir_path";
        String IS_FROM_NETWORK = "is_from_network";
        String DEVICE_ID = "device_id";

        String TYPE="type";

    }

    /**
     * 设备挂在状态
     */
    public interface DeviceMountState {
        String MOUNTED_TEXT = "mounted";
        String UNMOUNTED_TEXT = "unmounted";

        int MOUNTED = 1;
        int UNMOUNTED = 0;
    }



    public interface ACTION {
        String FILE_SCANNING="file_scannig";
        /**
         * 设备挂载/卸载广播
         */
        String DEVICE_RE_INIT="action.devices.re.init";
        String DEVICE_INIT="action.devices.init";
        String DEVICE_MOUNTED = "action_mounted";
        String DEVICE_UNMOUNTED = "action_unmounted";
        String ADD_LOCAL_SHORTCUT = "action.local_shortcut.add";
        String ADD_NETWORK_SHORTCUT ="action.network_uri.add";

        /**
         * 重新扫描设备
         */
        String RESCAN_ALL_FILES = "com.station.rescan_all";

        String MOVIE_SCRAP_START= "action.movie.scrap.start";
        String MOVIE_SCRAP_STOP = "action.movie.scrap.stop";
        String MOVIE_SCRAP_STOP_AND_REFRESH="action.movie.scrap.stop.and.refresh";
        String SHORTCUT_SCRAP_START="action.shortcut.scrap.start";
        String SHORTCUT_SCRAP_STOP="action.shortcut.scrap.finish";
        String SHORTCUT_INFO_UPDATE="action.shortcut.info_update";
        String SHORTCUT_REMOVE="action.shortcut.remove";

        String MATCHED_MOVIE="action.movie.matched";
        String MATCHED_MOVIE_FAILED="action.movie.matched.failed";


    }


    public interface SharePreferenceKeys {
        String DEVICE = "sort_device";
        String YEAR = "sort_year";
        String GENRE = "sort_genre";
        String SORTTYPE = "sort_type";
        String SORT_BY_DESC = "sort_desc";

        String PASSWORD = "password";
        String MOVIE_DB_UPDATE = "movie_db_update";

        String LAST_POTSER = "last_poster";
    }

    public enum RecentlyVideoAction{
        playNow,
        openDetail
    }

}
