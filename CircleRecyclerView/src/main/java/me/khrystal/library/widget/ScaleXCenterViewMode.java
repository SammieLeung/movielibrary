package me.khrystal.library.widget;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import me.khrystal.library.R;

/**
 * Created by tchip on 18-4-28.
 */

public class ScaleXCenterViewMode implements ItemViewMode {
    @Override
    public void applyToView(View v, RecyclerView parent) {
        float scale = 1.27f;
        boolean isCenter = (boolean) v.getTag(R.string.tag_is_center);
        if (isCenter) {
            ViewCompat.setScaleX(v, scale);
            ViewCompat.setScaleY(v, scale);
            ViewCompat.setTranslationZ(v, 1);
            if (v.isShown())
                v.requestFocus();
        } else {
            ViewCompat.setScaleX(v, 1f);
            ViewCompat.setScaleY(v, 1f);
            ViewCompat.setTranslationZ(v, 0);
        }
    }
}
