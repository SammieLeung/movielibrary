package com.hphtv.movielibrary.ui.shortcutmanager.options;

import androidx.lifecycle.ViewModelStoreOwner;

/**
 * author: Sam Leung
 * date:  2022/1/8
 */
public interface DialogAction {
    void show();
    void hide();
    ViewModelStoreOwner getOwner();
}
