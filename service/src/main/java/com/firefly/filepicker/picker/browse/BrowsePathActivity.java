package com.firefly.filepicker.picker.browse;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Environment;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.firefly.filepicker.DLNAService;
import com.firefly.filepicker.R;

import static com.firefly.filepicker.picker.browse.BrowsePathContract.Presenter.SELECT_DIR;

public class BrowsePathActivity extends AppCompatActivity {
    public final static String ARG_SELECT_TYPE = "selectType";
    public final static String ARG_FRAGMENT_TITLE = "title";
    public final static String ARG_SUPPORT_NET = "supportNet";
    public static final String ARG_ENABLE_CONFIRM_DIALOG = "enableConfrimDialog";

    private static final int REQUEST_CODE_CHECK_PERMISSION = 1000;
    private BrowsePathPresenter mPresenter;
    private BrowsePathFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_path);
        checkStorageManagerPermission();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showRequestPermissionRationale();
            } else {
                requestPermissions();
            }
        } else {
            init();
        }
    }

    private void showRequestPermissionRationale() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(R.string.notice_for_request_permission)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions();
            }
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_CODE_CHECK_PERMISSION);
    }

    private void init() {
        Intent intent = new Intent(this, DLNAService.class);
        startService(intent);

        int browseType = getIntent().getIntExtra(ARG_SELECT_TYPE, SELECT_DIR);
        String fragmentTitle = getIntent().getStringExtra(ARG_FRAGMENT_TITLE);
        boolean supportNet = getIntent().getBooleanExtra(ARG_SUPPORT_NET, true);
        boolean enableConfirm = getIntent().getBooleanExtra(ARG_ENABLE_CONFIRM_DIALOG, true);
        mFragment = new BrowsePathFragment();
        mFragment.setTitle(fragmentTitle);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment, mFragment)
                .commit();
        mPresenter = new BrowsePathPresenter(mFragment, this);
        mPresenter.setBrowseType(browseType);
        mPresenter.setSupportNet(supportNet);
        mPresenter.setEnableSelectConfirm(enableConfirm);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_CHECK_PERMISSION) {
            return;
        }

        for (int i = 0; i < permissions.length; ++i) {
            if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) &&
                    grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }

        init();
    }

    @Override
    public void onBackPressed() {
        if (!mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mFragment.isLoading()) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_BUTTON_B:
                case KeyEvent.KEYCODE_ESCAPE:
                    break;
                default:
                    return true;
            }
        }else{
           switch (event.getKeyCode()){
               case KeyEvent.KEYCODE_MENU:
               case KeyEvent.KEYCODE_BUTTON_SELECT:
               case KeyEvent.KEYCODE_BUTTON_START:
                   mFragment.dispatchKeyMenu();
                   break;
           }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mFragment.isLoading()) {
            mFragment.onBackPressed();
        }

        return super.dispatchTouchEvent(ev);
    }

    public void checkStorageManagerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}
