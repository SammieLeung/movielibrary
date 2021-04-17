package com.hphtv.movielibrary.data;

/**
 * Created by tchip on 18-3-1.
 */

public class ConstData {

    public static final String AUTHORITIES = "com.hphtv.movielibrary.fileprovider";

    //--------------HashMap keys------------
    //1.for LeftMenuListAdapter and HomePageActivity
    public static final String ICON = "left_menu_icon";
    public static final String TEXT = "left_menu_text";


    //2.--------------for Device--------------
    //2.1 Device Api
    public static final String LOCAL = "local";


    //3.--------------for DirectoryManagerAdapter--------------
    //3.1 for HashMap key
    /**
     * 设备对象
     */
    public static final String DEVICE = "device";
    public static final String DIRECTORY="directory";
    /**
     * 设备是否被选择
     */
    public static final String DEVICE_CHECK_STATUS = "device_check_status";

    public static final String DEVICE_IS_ENCRYPTED = "device_is_encrypted";

    public static final String DEVICE_MATCHED_VIDEO = "device_match_video";
    public static final String DEVICE_VIDEO_COUNT = "device_video_count";

    public static final int DEFAULT_UPDATE_TIME_7DAY = 3600 * 7 * 1000 * 24;
    public static final int DEFAULT_UPDATE_TIME_3DAY = 3600 * 3 * 1000 * 24;
    public static final int DEFAULT_UPDATE_TEST = 2000;

    /**
     * 电影搜索 模式
     */
    public interface SearchMode {
        int MODE_LIST = 0;// 搜索电影
        int MODE_INFO = 1;// 获取电影信息
    }

    /**
     * 搜刮器
     */
    public interface Scraper {
        int UNKNOW=-1;
        int DOUBAN = 0;
        int IMDB = 1;
        int MTIME = 2;
    }

    public interface DirectoryState {
        /*设备已被扫描*/
        int SCANNED = 0;
        /*设备正在扫描*/
        int SCANNING = 1;
        /*未扫描*/
        int UNSCAN = 2;
    }

    public interface VideoFile{
        int IS_MATCHED=1;
        int UN_MATCHED=0;
    }
    /**
     * 设备连接状态
     */
    public interface DeviceConnectState {
        int CONNECTED = 0;
        int DISCONNECTED = 1;
    }

    public interface DeviceType {
        /*本地设备*/
        String STR_LOCAL = "local";
        /*dlna设备*/
        String STR_DLNA = "dlna";
        /*设备*/
        String STR_SAMBA = "samba";


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
        int DEVICE_TYPE_DLNA = 4;
        int DEVICE_TYPE_SMB = 5;
    }

    public interface EncryptState {
        public static final int UNENCRYPTED = 0;
        public static final int ENCRYPTED = 1;
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


    //4.------------for filepicker---------------
    public static final String PACKAGE_FILE_PICKER = "com.firefly.filepicker";
    public static final String ACTION_FILE_PICKER = "com.firefly.FILE_PICKER";

    /*--------------uri prefix for get connected devices------------*/
    public static final String PREFIX_URI_CONNECTED_DEVICES = "content://com.firefly.filepicker/devices/";//content://com.firefly.filepicker/devices[/<deviceType>]


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
        int MODE_WRAPPER=4;
    }

    //6. --------------for movie and videofile-------------
    public interface MapKey {
        String KEY_MOVIE = "movie";
        String KEY_VIDEOFILE = "videofile";
        String KEY_TITLE = "title";
        String KEY_SUBTYPE = "subtype";

        String KEY_DEVICE_LIST = "key_devcie_list";

        //MovieScanService FileInfo
        String KEY_MNI = "key_mni";
        String KEY_FILE = "key_video_file";
        String KEY_PHRASENAME = "key_phrase_name";
    }


    //7.---------------for MovieSearcherHelper -----------
    public interface IntentKey {
        String IS_GET_MOVIE_LISTS = "MODE_LIST";

    }

    /**
     * 广播
     */
    public interface BoardCastMsg {
        String ACTION_DEVICE_LIST_REFRESH = "action_device_list_refresh";
    }

    //8.-----------for adapter-------------------
    public interface CardViewType {
        int VIEWTYPE_SIMPLE = 0;
        int VIEWTYPE_EXPEND = 1;
        int VIEWTYPE_SMALL = 2;
        int VIEWTYPE_HISTORY = 3;
        int VIEWTYPE_FAVORITE = 4;
    }

    public interface DeviceModel {
        String TRV9 = "trv9";
        String ROC_RK3399_PC = "ROC_RK3399_PC";
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

    /**
     * 设备信息
     */

    public interface DeviceMountMsg {
        String DEVICE_NAME = "device_name";
        String MOUNT_DEVICE_STATE = "device_state";
        String DEVICE_MOUNT_PATH = "device_mount_path";
        String DEVICE_NETWORKPATH = "device_networkpath";
        String MOUNT_TYPE = "device_type";
        String IS_FROM_NETWORK = "is_from_network";

        String DEVICE_ID = "device_id";
    }

    public interface MovieSubType {
        public static final String TV = "tv series";
        public static final String MOVIE = "movie";
        public static final String OTHERS = "others";
        public static final String CHILDREN = "children";
        public static final String SHOW = "show";
        public static final String PRIVATE = "private video";
        public static final String UNMATCHED = "unmatched";
        public static final String FAVORITE = "favorite";
        public static final String HISTORY = "history";
        public static final String ALL = "all";
    }


    /*---------------------本地广播-----------------------------*/
    public static final String ACTION_FAVORITE_MOVIE_CHANGE="action.favorite.movie.change";
}
