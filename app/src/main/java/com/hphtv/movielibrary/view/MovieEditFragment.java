package com.hphtv.movielibrary.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hphtv.movielibrary.R;
import com.hphtv.movielibrary.util.StrUtils;

import java.util.HashMap;

/**
 * Created by tchip on 17-12-7.
 */

public class MovieEditFragment extends DialogFragment {
    public static final String TAG = "MovieEditFragment";
    private TextView  tv_genres, tv_score, tv_imdbid, tv_path;
    private EditText et_imdbid, et_filename;
    private ImageView iv_img;
    private Button btn_yes, btn_no;
    private View layout;
    public static final String TITLE = "title";
    public static final String IMG = "img";
    public static final String PATH = "path";
    public static final String GENRES = "genres";
    public static final String IMDBID = "imdbid";
    public static final String SCORE = "score";
    private String title;
    private String score;
    private String s_genres;
    private String s_path;
    private String filename;
    private String img;
    private String imdb_id;


    public MovieEditFragment() {
    }

    public static MovieEditFragment newInstance() {
        MovieEditFragment fragment = new MovieEditFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        layout = inflater.inflate(R.layout.component_my_edit_dialog_fragment, null);
        tv_genres = (TextView) layout.findViewById(R.id.movie_genres);
        tv_score = (TextView) layout.findViewById(R.id.movie_rate);
        tv_imdbid = (TextView) layout.findViewById(R.id.movie_imdb_id);
        tv_path = (TextView) layout.findViewById(R.id.movie_path);

        et_imdbid = (EditText) layout.findViewById(R.id.et_imdb_id);
        et_filename = (EditText) layout.findViewById(R.id.et_filename);

        iv_img = (ImageView) layout.findViewById(R.id.info_img);

        btn_no = (Button) layout.findViewById(R.id.btn_no);
        btn_yes = (Button) layout.findViewById(R.id.btn_yes);
        btn_yes.requestFocus();
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mPositiveListener != null)
                    mPositiveListener.OnPositivePress(v);
            }
        });

        et_imdbid.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(v.getApplicationWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    btn_yes.requestFocus();
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(layout);

        tv_genres.setText(s_genres);
        tv_score.setText(score);
        tv_path.setText(s_path);
        tv_imdbid.setText(imdb_id);
        et_filename.setText(title);
        et_imdbid.setText(imdb_id);

        try {
            if (img != null) {
                Glide.with(getActivity()).load(img).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(iv_img);
            } else {
                Glide.with(getActivity()).load(R.mipmap.ic_poster_default).apply(RequestOptions.placeholderOf(R.mipmap.ic_poster_default)).into(iv_img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

    public MovieEditFragment setInfo(HashMap<String, Object> map) {
        title = (String) map.get(TITLE);
        title=title.replaceAll("\\."," ");
        score = (String) map.get(SCORE);
        img = (String) map.get(IMG);
        String[] paths = (String[]) map.get(PATH);
        String[] genres = (String[]) map.get(GENRES);
        imdb_id = (String) map.get(IMDBID);
        filename = title;
        s_genres = StrUtils.arrayToString(genres);
        s_path = StrUtils.arrayToString(paths);
        return this;
    }


    public String getFilename() {
        return et_filename.getText().toString();
    }


    public String getImdbId() {
        return et_imdbid.getText().toString();
    }

    public interface PositiveListener {
        public void OnPositivePress(View v);
    }

    PositiveListener mPositiveListener;

    public void setPositiveListener(PositiveListener mPositiveListener) {
        this.mPositiveListener = mPositiveListener;
    }
}
