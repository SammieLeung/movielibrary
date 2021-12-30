package com.hphtv.movielibrary.ui.shortcutmanager;


import android.os.Bundle;

import com.hphtv.movielibrary.databinding.ShortcutOptionsDialogLayoutBinding;
import com.hphtv.movielibrary.ui.BaseDialogFragment2;

/**
 * author: Sam Leung
 * date:  2021/12/30
 */
public class ShortcutOptionsDialog extends BaseDialogFragment2<ShortcutOptionsViewModel, ShortcutOptionsDialogLayoutBinding> {
    public static ShortcutOptionsDialog newInstance() {
        Bundle args = new Bundle();
        ShortcutOptionsDialog fragment = new ShortcutOptionsDialog();
        fragment.setArguments(args);
        return fragment;
    }
}
