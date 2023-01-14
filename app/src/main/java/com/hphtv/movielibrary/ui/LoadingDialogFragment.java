package com.hphtv.movielibrary.ui;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hphtv.movielibrary.R;


public class LoadingDialogFragment extends DialogFragment {
    public static final String TAG = "LOADING_VIEW";
    private DialogInterface.OnCancelListener mOnCancelListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_loading, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (mOnCancelListener != null)
            getDialog().setOnCancelListener(mOnCancelListener);
        return view;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        mOnCancelListener = onCancelListener;
        if (getDialog() != null)
            getDialog().setOnCancelListener(mOnCancelListener);
    }
}
