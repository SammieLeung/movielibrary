package com.hphtv.movielibrary.data;


import androidx.databinding.ObservableBoolean;

import com.hphtv.movielibrary.MovieApplication;
import com.station.kit.util.SharePreferencesTools;

/**
 * author: Sam Leung
 * date:  2022/3/11
 */
public class Config {
    public static final String SYSTEM_PLAYER_PACKAGE="com.hph.videoplayer";
    public static final String CHILD_MODE="child_mode";
    public static final String CHILD_MODE_PSW="child_mode_psw";
    public static final String SHOW_TITLE="show_title";
    public static final String SHOW_POSTER="show_poster";
    public static final String SHOW_CORNER_MARK="show_cornermark";
    public static final String SHOW_RATING="show_rating";
    public static final String SHOW_LIKE="show_like";
    public static final String PLAYER_NAME ="player_name";
    public static final String PLAYER_PACKAGE="player_package";

    public static final String AUTO_SEARCH="auto_search";
    public static final String DEFAULT_SEARCH_MODE="default_search_mode";

    //默认密码
    public static final String DEAULT_PASSWORD="1111";

    private static boolean sChildMode;
    private static String sChildModePassword;
    private static boolean sTempCloseChildMode;
    private static ObservableBoolean sShowTitle;//显示标题
    private static ObservableBoolean sShowPoster;//显示海报
    private static ObservableBoolean sShowCornerMark;//显示角标
    private static ObservableBoolean sShowRating;//显示评分
    private static ObservableBoolean sShowLike;//显示收藏

    private static ObservableBoolean sAutoSearch;
    private static String sDefaultSearchMode;
    private static String sPlayerName;
    private static String sPlayerPackage;

    static {
        SharePreferencesTools tools=SharePreferencesTools.getInstance(MovieApplication.getInstance());
        sChildMode =tools.readProperty(CHILD_MODE,false);
        sTempCloseChildMode=false;
        sChildModePassword =tools.readProperty(CHILD_MODE_PSW,DEAULT_PASSWORD);

        sShowTitle=new ObservableBoolean(tools.readProperty(SHOW_TITLE,true));
        sShowPoster=new ObservableBoolean(tools.readProperty(SHOW_POSTER,true));
        sShowCornerMark=new ObservableBoolean(tools.readProperty(SHOW_CORNER_MARK,true));
        sShowRating=new ObservableBoolean(tools.readProperty(SHOW_RATING,true));
        sShowLike=new ObservableBoolean(tools.readProperty(SHOW_LIKE,true));

        sAutoSearch=new ObservableBoolean(tools.readProperty(AUTO_SEARCH,true));
        sDefaultSearchMode =tools.readProperty(DEFAULT_SEARCH_MODE, Constants.SearchType.auto.name());

        sPlayerName =tools.readProperty(PLAYER_NAME,"视频播放器");
        sPlayerPackage=tools.readProperty(PLAYER_PACKAGE,"com.hph.videoplayer");

    }

    /**
     * 保存儿童模式的启用状态
     * @param isChildMode
     */
    public static void setChildMode(boolean isChildMode){
        sChildMode =isChildMode;
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(CHILD_MODE,sChildMode);
    }

    /**
     * 获取儿童模式状态
     * @return
     */
    public static boolean isChildMode(){
        return sChildMode;
    }

//    public static boolean isTempCloseChildMode() {
//        return sTempCloseChildMode;
//    }

//    public static void setTempCloseChildMode(boolean tempCloseChildMode) {
//        sTempCloseChildMode = tempCloseChildMode;
//    }

    /**
     * 获取用于数据库查询的儿童模式相关语句
     * @return
     */
    public static String getSqlConditionOfChildMode() {
        /*
            儿童模式即需要筛选出有ALL_AGE的影片
            否则返回ALL_AGE+ADULT类型影片。
         */
        if(!sTempCloseChildMode&&sChildMode)
            return Constants.WatchLimit.ALL_AGE.name();
        return null;
    }

    /**
     * 获取儿童模式密码
     * @return
     */
    public static String getChildModePassword() {
        return sChildModePassword;
    }

    /**
     * 设置儿童模式密码>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     * @param childModePassword
     */
    public static void setChildModePassword(String childModePassword) {
        sChildModePassword = childModePassword;
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(CHILD_MODE_PSW, sChildModePassword);
    }

    // 海报设置相关属性>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    public static ObservableBoolean getShowTitle() {
        return sShowTitle;
    }

    public static void setShowTitle(boolean showTitle) {
        sShowTitle.set(showTitle);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(SHOW_TITLE, showTitle);
    }

    public static ObservableBoolean getShowPoster() {
        return sShowPoster;
    }

    public static void setShowPoster(boolean showPoster) {
        sShowPoster.set(showPoster);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(SHOW_POSTER, showPoster);
    }

    public static ObservableBoolean getShowCornerMark() {
        return sShowCornerMark;
    }

    public static void setShowCornerMark(boolean showCornerMark) {
        sShowCornerMark.set(showCornerMark);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(SHOW_CORNER_MARK, showCornerMark);
    }

    public static ObservableBoolean getShowRating() {
        return sShowRating;
    }

    public static void setShowRating(boolean showRating) {
        sShowRating.set(showRating);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(SHOW_RATING, showRating);
    }

    public static ObservableBoolean getShowLike() {
        return sShowLike;
    }

    public static void setShowLike(boolean showLike) {
        sShowLike.set(showLike);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(SHOW_LIKE, showLike);
    }

    //偏好设置>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    public static ObservableBoolean isAutoSearch() {
        return sAutoSearch;
    }

    public static void setAutoSearch(boolean autoSearch) {
        sAutoSearch.set(autoSearch);
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(AUTO_SEARCH,autoSearch);
    }

    public static String getDefaultSearchMode() {
        return sDefaultSearchMode;
    }

    public static void setDefaultSearchMode(Constants.SearchType type) {
        sDefaultSearchMode = type.name();
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(DEFAULT_SEARCH_MODE,type.name());
    }

    public static String getPlayerName() {
        return sPlayerName;
    }

    public static void setPlayerName(String playerName) {
        sPlayerName = playerName;
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(PLAYER_NAME, playerName);
    }

    public static String getPlayerPackage() {
        return sPlayerPackage;
    }

    public static void setPlayerPackage(String playerPackage) {
        sPlayerPackage = playerPackage;
        SharePreferencesTools.getInstance(MovieApplication.getInstance()).saveProperty(PLAYER_PACKAGE, playerPackage);
    }
}
