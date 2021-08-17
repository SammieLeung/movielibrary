package com.hphtv.movielibrary.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.hphtv.movielibrary.R;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by tchip on 18-4-13.
 */

public class CustomSelectorDialogFragment extends DialogFragment {

    private View view;
    private LinearLayout contentPanel;
    String title;

    public static CustomSelectorDialogFragment newInstance(String... strs) {

        Bundle args = new Bundle();
        CustomSelectorDialogFragment fragment = new CustomSelectorDialogFragment();
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (String s : strs) {
            stringArrayList.add(s);
        }
        args.putStringArrayList("text", stringArrayList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.component_button_list_dialog_fragment,null);
        contentPanel = (LinearLayout) view.findViewById(R.id.view_content);
        TextView titleView = (TextView) view.findViewById(R.id.tv_title);
        titleView.setText(title);
        List<String> list = getArguments().getStringArrayList("text");
        if (list != null) {
            for (String text : list) {
                Button button = (Button) inflater.inflate(R.layout.button_item,contentPanel,false);
                button.setText(text);
                button.setOnClickListener(onClickListener);

                contentPanel.addView(button);
            }
        }
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(view);
        return dialog;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = contentPanel.indexOfChild(v);
            listener.onClick(position, v);
        }
    };


    public interface OnButtonClickListener {
        public void onClick(int pos, View v);
    }

    OnButtonClickListener listener;

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public CustomSelectorDialogFragment setTitle(String text) {
        title = text;
        return this;
    }
}
