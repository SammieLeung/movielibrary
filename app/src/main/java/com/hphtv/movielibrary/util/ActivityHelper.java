package com.hphtv.movielibrary.util;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

import com.hphtv.movielibrary.roomdb.entity.dataview.MovieDataView;
import com.hphtv.movielibrary.roomdb.entity.dataview.UnrecognizedFileDataView;
import com.hphtv.movielibrary.ui.homepage.HomePageActivity;
import com.hphtv.movielibrary.ui.postermenu.PosterMenuDialog;
import com.hphtv.movielibrary.ui.postermenu.UnknownsFileMenuDialog;

/**
 * author: Sam Leung
 * date:  2022/2/24
 */
public class ActivityHelper {

    public static void startHomePageActivity(Context context) {
        Intent intent = new Intent(context, HomePageActivity.class);
        context.startActivity(intent);
    }

    public static void showPosterMenuDialog(FragmentManager fragmentManager, int pos,MovieDataView movieDataView) {
        PosterMenuDialog dialog = PosterMenuDialog.newInstance(pos,movieDataView);
        dialog.show(fragmentManager, "");
    }

    public static void showUnknownsFileMenuDialog(FragmentManager fragmentManager, int pos, UnrecognizedFileDataView unrecognizedFileDataView){
        UnknownsFileMenuDialog dialog=UnknownsFileMenuDialog.newInstance(pos,unrecognizedFileDataView);
        dialog.show(fragmentManager, "");

    }


}
