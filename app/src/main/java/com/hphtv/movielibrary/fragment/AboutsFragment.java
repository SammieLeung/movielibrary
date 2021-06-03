package com.hphtv.movielibrary.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.PackageUtil;

/**
 * Created by tchip on 18-3-7.
 */

public class AboutsFragment extends Fragment {
    public static final String TAG = AboutsFragment.class.getSimpleName();

    private TextView tv_version;
    private Spinner spinner;
    private boolean isFirstStart = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_layout_abouts, container, false);
        tv_version = (TextView) view.findViewById(R.id.tv_version);
        String version = getVersion();
        tv_version.setText(version);
        return view;
    }

    private String getVersion() {
        StringBuffer versionbuffer = new StringBuffer();
        String versionname = PackageUtil.getVersionName(getActivity());
        versionbuffer.append(versionname);
        return versionbuffer.toString();
    }

}
