package com.firefly.filepicker.picker.browse;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.firefly.filepicker.commom.BasePresenter;
import com.firefly.filepicker.commom.BaseView;
import com.firefly.filepicker.data.bean.Node;

import java.util.Map;

/**
 * Created by rany on 18-1-10.
 */

public interface BrowsePathContract {
    interface View extends BaseView<Presenter> {
        void updateTreeView(Node root);
        void setLoadingViewVisible(boolean show);
        void showError(String msg);
        void setResult(int resultCode, Bundle data);
        void showAuthDialog(@NonNull Node node, Bundle data);
        void fallBackParent(Node preNode);
        void showOnSelectConfirm(Node node);
    }

    interface Presenter extends BasePresenter {
        @IntDef({SELECT_DIR, SELECT_FILE})
        @interface SelectType {}
        int SELECT_DIR = 0;
        int SELECT_FILE = 1;

        void getChildren(Node node);
        void checkAuthData(Node node, Bundle data);
        boolean isAlreadyAuth(Node node);
        void onSelect(Node node, boolean isPrivate, boolean confirm);
        void setBrowseType(int type);
        void setSupportNet(boolean supportNet);
        void setEnableSelectConfirm(boolean enableSelectConfirm);
        void cancelTask(Node node);
    }
}
