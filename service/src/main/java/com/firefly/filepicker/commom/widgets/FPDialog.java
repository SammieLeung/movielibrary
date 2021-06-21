package com.firefly.filepicker.commom.widgets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firefly.filepicker.R;

/**
 * Created by rany on 18-3-7.
 */

public class FPDialog extends Dialog {
    private ViewGroup mContentPanel;
    private TextView mTitleView;
    private Button mPositiveButton;
    private Button mNegativeButton;

    private OnClickListener mPositiveListener;

    public FPDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.setContentView(R.layout.layout_dialog);

        mContentPanel = (ViewGroup) findViewById(R.id.content_panel);
        mTitleView = (TextView) findViewById(R.id.dialog_title);
        mPositiveButton = (Button) findViewById(R.id.btn_cancel);
        mNegativeButton = (Button) findViewById(R.id.negative_btn);

        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPositiveListener != null) {
                    mPositiveListener.onClick(FPDialog.this, v.getId());
                    return;
                }

                dismiss();
            }
        });
    }

    @Override
    public void setContentView(@NonNull View view) {
        mContentPanel.addView(view);
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = getLayoutInflater().inflate(layoutResID, mContentPanel, false);
        setContentView(view);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        setContentView(view);
    }

    @Override
    public void setTitle(int titleId) {
        mTitleView.setText(titleId);
    }

    @Override
    public void setTitle(@Nullable CharSequence title) {
        mTitleView.setText(title);
    }

    public void setPositiveButton(int textId, final OnClickListener listener) {
        mPositiveButton.setText(textId);
        mPositiveListener = listener;
    }

    public void setNegativeButton(int textId, final OnClickListener listener) {
        mNegativeButton.setText(textId);
        mNegativeButton.setVisibility(View.VISIBLE);
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(FPDialog.this, v.getId());
                }

                dismiss();
            }
        });
    }
}
