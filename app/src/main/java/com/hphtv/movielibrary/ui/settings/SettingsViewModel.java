package com.hphtv.movielibrary.ui.settings;

import android.app.Application;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;

import com.hphtv.movielibrary.BaseAndroidViewModel;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.data.Config;
import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.scraper.service.OnlineDBApiService;
import com.hphtv.movielibrary.util.VideoPlayTools;
import com.station.kit.util.AppUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * author: Sam Leung
 * date:  2022/3/14
 */
public class SettingsViewModel extends BaseAndroidViewModel {
    public static final String TAG = SettingsViewModel.class.getSimpleName();

    public static final int OK = 0;
    public static final int PSW_ERROR = 1;
    public static final int NEW_PSW_ERROR = 2;
    public static final int PSW_NOT_CHANGE = 4;
    public static final int CONFIRM_PSW_ERROR = 8;

    private ObservableBoolean mFlagRefresh = new ObservableBoolean(false);

    private ObservableInt mSelectPos = new ObservableInt(-1);
    private ObservableBoolean mChildModeState = new ObservableBoolean(Config.isChildMode());
    private ObservableInt mSelectPlayerPos = new ObservableInt(0);

    private ObservableBoolean mAutoSearchState = new ObservableBoolean(Config.isAutoSearch());
    private ObservableField<String> mDefaultSearchMode = new ObservableField<>();
    private ObservableField<String> mPlayerName = new ObservableField<>();
    private ObservableField<String> mRecentlyVideoAction=new ObservableField<>();
    private HashMap<String, String> mVideoPlayerMap;
    private List<String> mPlayerNames = new ArrayList<>();
    private List<String> mPlayerPackages = new ArrayList<>();

    public SettingsViewModel(@NonNull @NotNull Application application) {
        super(application);
        mSelectPlayerPos.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Config.setPlayerPackage(mPlayerPackages.get(mSelectPlayerPos.get()));
                Config.setPlayerName(mPlayerNames.get(mSelectPlayerPos.get()));
                readDefaultPlayer();
            }
        });


        mVideoPlayerMap = VideoPlayTools.getVideoPlayers(application);
        if (mVideoPlayerMap != null) {
            mVideoPlayerMap.keySet().stream().sorted((o1, o2) -> {
                if (o1.equals(Config.SYSTEM_PLAYER_PACKAGE))
                    return -1;
                return 0;
            }).forEach(s -> {
                String name = mVideoPlayerMap.get(s);
//                if (s.equals(Config.SYSTEM_PLAYER_PACKAGE)) {
//                    name = getString(R.string.settings_preference_system_player);
//                }
                mPlayerPackages.add(s);
                mPlayerNames.add(name);
            });
            readDefaultPlayer();
            readRecentlyVideoAction();
        }
    }

    public ObservableBoolean getFlagRefresh() {
        return mFlagRefresh;
    }

    public ObservableInt getSelectPos() {
        return mSelectPos;
    }

    //    儿童模式
    public ObservableBoolean getChildModeState() {
        return mChildModeState;
    }

    /**
     * 更改儿童模式状态
     *
     * @param v
     */
    public void toggleChildMode(View v) {
        mChildModeState.set(!mChildModeState.get());
        Config.setChildMode(mChildModeState.get());
//        Config.setTempCloseChildMode(false);
        mFlagRefresh.set(true);
        OnlineDBApiService.notifyChildMode(Constants.Scraper.TMDB);
        OnlineDBApiService.notifyChildMode(Constants.Scraper.TMDB_EN);
    }


    //  儿童模式->修改密码
    public int checkPasswords(String old_psw, String new_psw, String c_psw) {
        String psw = Config.getChildModePassword();
        int flag = OK;
        if (psw == null || !psw.equals(old_psw))
            flag |= PSW_ERROR;
        if (new_psw == null || new_psw.length() < 4)
            flag |= NEW_PSW_ERROR;
        if (psw.equals(new_psw))
            flag |= PSW_NOT_CHANGE;
        if (c_psw == null || !new_psw.equals(c_psw))
            flag |= CONFIRM_PSW_ERROR;
        return flag;
    }

    public void submitPassword(String psw) {
        Config.setChildModePassword(psw);
    }

    //  海报设置
    public void toggleTitle(View v) {
        Config.setShowTitle(!Config.getShowTitle().get());
        mFlagRefresh.set(true);
    }

    public void togglePoster(View v) {
        Config.setShowPoster(!Config.getShowPoster().get());
        mFlagRefresh.set(true);
    }

    public void toggleLike(View v) {
        Config.setShowLike(!Config.getShowLike().get());
        mFlagRefresh.set(true);
    }

    public void toggleRating(View v) {
        Config.setShowRating(!Config.getShowRating().get());
        mFlagRefresh.set(true);
    }

    public void toggleCornermark(View v) {
        Config.setShowCornerMark(!Config.getShowCornerMark().get());
        mFlagRefresh.set(true);
    }

    // 偏好设置

    public ObservableField<String> getDefaultSearchMode() {
        return mDefaultSearchMode;
    }

    public ObservableField<String> getRecentlyVideoAction() {
        return mRecentlyVideoAction;
    }

    public ObservableField<String> getPlayerName() {
        return mPlayerName;
    }

    public void toggleAutoSearchState(View v) {
        Config.setAutoSearch(!Config.isAutoSearch().get());
    }


    public void changeDefaultSearchState(View v) {
        Constants.SearchType type = Constants.SearchType.valueOf(Config.getDefaultSearchMode());
        if (type == Constants.SearchType.auto) {
            Config.setDefaultSearchMode(Constants.SearchType.movie);
        } else if (type == Constants.SearchType.movie) {
            Config.setDefaultSearchMode(Constants.SearchType.tv);
        } else if (type == Constants.SearchType.tv) {
            Config.setDefaultSearchMode(Constants.SearchType.auto);
        }
        readDefaultSearchModeString();
    }

    public void changeRecentlyVideoAction(View v){
        Constants.RecentlyVideoAction action=Constants.RecentlyVideoAction.valueOf(Config.getRecentlyVideoAction());

        if (action == Constants.RecentlyVideoAction.playNow) {
            Config.setRecentlyVideoAction(Constants.RecentlyVideoAction.openDetail);
        } else if (action == Constants.RecentlyVideoAction.openDetail) {
            Config.setRecentlyVideoAction(Constants.RecentlyVideoAction.playNow);
        }
        readRecentlyVideoAction();
    }

    /**
     * 刷新默认搜索模式的值
     */
    public void readDefaultSearchModeString() {
        Constants.SearchType type = Constants.SearchType.valueOf(Config.getDefaultSearchMode());
        if (type == Constants.SearchType.auto) {
            mDefaultSearchMode.set(getString(R.string.shortcut_type_auto));
        } else if (type == Constants.SearchType.movie) {
            mDefaultSearchMode.set(getString(R.string.shortcut_type_movie));
        } else if (type == Constants.SearchType.tv) {
            mDefaultSearchMode.set(getString(R.string.shortcut_type_tv));
        }
    }

    /**
     * 重新读取默认播放器名称。
     */
    public void readDefaultPlayer() {
//        if (Config.getPlayerPackage().equals(Config.SYSTEM_PLAYER_PACKAGE)) {
//            mPlayerName.set(getApplication().getString(R.string.settings_preference_system_player));
//        } else {
        mPlayerName.set(Config.getPlayerName());
//        }
        if (mPlayerPackages.contains(Config.getPlayerPackage())) {
            mSelectPlayerPos.set(mPlayerPackages.indexOf(Config.getPlayerPackage()));
        }
    }

    public void readRecentlyVideoAction(){
        Constants.RecentlyVideoAction action=Constants.RecentlyVideoAction.valueOf(Config.getRecentlyVideoAction());
        switch (action){
            case playNow:
                mRecentlyVideoAction.set(getString(R.string.settings_preference_homepage_recently_played_action_play_now));
                break;
            case openDetail:
                mRecentlyVideoAction.set(getString(R.string.settings_preference_homepage_recently_played_action_open_video_details));
                break;
        }
    }

    public void setDefaultPlayer(int pos) {
        mSelectPlayerPos.set(pos);
    }

    public ObservableInt getSelectPlayerPos() {
        return mSelectPlayerPos;
    }

    public String getVersionName() {
        return AppUtils.getVersionName(getApplication());
    }

    public List<String> getPlayerNames() {
        return mPlayerNames;
    }

    public List<String> getPlayerPackages() {
        return mPlayerPackages;
    }
}
