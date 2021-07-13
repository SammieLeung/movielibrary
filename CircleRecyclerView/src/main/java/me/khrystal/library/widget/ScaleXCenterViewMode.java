package me.khrystal.library.widget;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

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
            v.setScaleX(scale);
            v.setScaleY(scale);
            ViewCompat.setTranslationZ(v, 1);
            if (v.isShown())
                v.requestFocus();
        } else {
            v.setScaleX(1f);
            v.setScaleY(1f);
            ViewCompat.setTranslationZ(v, 0);
        }
    }
}
