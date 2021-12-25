package com.hphtv.movielibrary.data;

import com.hphtv.movielibrary.roomdb.entity.Device;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tchip on 18-3-1.
 */

public class Constants {

    public static final long GLIDE_CACHE_VERSION = System.currentTimeMillis() / 86400000;

    public static final String PACKAGE_FILE_PICKER = "com.firefly.filepicker";
    public static final String ACTION_FILE_PICKER = "com.firefly.FILE_PICKER";
    public static final String ACTION_FAVORITE_MOVIE_CHANGE = "action.favorite.movie.change";
    /**
     * 设备路径与id映射
     */
    public static final ConcurrentHashMap<String, String> connectDeviceIds = new ConcurrentHashMap<>();

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

    public enum FolderType {
        MOVIE,//电影
        TV_SERIES,//电视节目
        VARIETY_SHOW,//综艺节目
        ANIMATE,//动画
        OTHER,//其他
    }

    public enum AccessPermission{
        NORMAL,
        CHILDREN,
        ADULT
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
        String UNRECOGNIZE_FILE_KEYWORD = "keyword";

        String CURRENT_FRAGMENT = "current_fragment";
        //设备信息
        String DEVICE_NAME = "device_name";
        String DEVICE_STATE = "device_state";
        String DEVICE_MOUNT_PATH = "device_mount_path";
        String DEVICE_NETWORKPATH = "device_networkpath";
        String MOUNT_TYPE = "device_type";
        String NETWORK_DIR_URI="network_dir_uri";
        String IS_FROM_NETWORK = "is_from_network";
        String DEVICE_ID = "device_id";

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



    public interface BroadCastMsg {
        String FILE_SCANNING="file_scannig";
        /**
         * 设备挂载/卸载广播
         */
        String DEVICE_UP = "action_mounted";
        String DEVICE_DOWN = "action_unmounted";
        String POSTER_PAIRING = "action.poster_pairing";
        String POSTER_PAIRING_FOR_NETWORK_URI ="action.network_uri.poster_pairing";

        /**
         * 重新扫描设备
         */
        String RESCAN_DEVICE = "com.rockchips.mediacenter.rescan_device";

        String MOVIE_SCRAP_START= "action.movie.scrap.start";
        String MOVIE_SCRAP_FINISH = "action.movie.scrap.finish";
        String MATCHED_MOVIE="action.movie.matched";
        String FOLDER_REFRESH = "action.movie.folder.refresh";
        String START_LOADING = "action.home.startloading";
        String STOP_LOADING = "action.home.stoploading";


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

}
