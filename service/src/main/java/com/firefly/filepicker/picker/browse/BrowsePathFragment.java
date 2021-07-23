package com.firefly.filepicker.picker.browse;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firefly.filepicker.R;
import com.firefly.filepicker.commom.widgets.FPDialog;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;
import com.firefly.filepicker.utils.SmbFileHelper;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * A placeholder fragment containing a simple view.
 */
public class BrowsePathFragment extends Fragment
        implements BrowsePathContract.View, ItemEventListener {
    private static final String TAG = BrowsePathFragment.class.getSimpleName();
    public static final String PARENT_NODE_ID = "-1";

    private Node mRoot;
    private Node mCurrentNode;
    // 临时添加的Node,如在选择文件的时候输入url进行浏览
    private Node mTmpNode;

    private BrowsePathContract.Presenter mPresenter;

    private RecyclerView mTreeContainer;
    private RecyclerView mTypeContainer;
    private TextView mFocusedDirView;
    private TextView mFocusedLastModifiedView;
    private ImageButton mIbtnBack;
    private ProgressBar mProgressBar;
    private RelativeLayout mEmptyView;
    private boolean mEnableProgressBar = true;

    private String mTitle = null;

    private FileAndDeviceListAdapter mFileAndDeviceListAdapter;
    private DeviceTypeListAdapter mDeviceTypeListAdapter;

    public BrowsePathFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_path, container, false);

        mTreeContainer = (RecyclerView) view.findViewById(R.id.container);
        mTypeContainer = (RecyclerView) view.findViewById(R.id.type_container);
        mFocusedDirView = (TextView) view.findViewById(R.id.focused_dir);
        mIbtnBack = view.findViewById(R.id.ibtn_back);
        mFocusedLastModifiedView = (TextView) view.findViewById(R.id.last_modified);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mEmptyView = (RelativeLayout) view.findViewById(R.id.empty_view);

        if (!TextUtils.isEmpty(mTitle)) {
            TextView titleView = (TextView) view.findViewById(R.id.title);
            titleView.setText(mTitle);
        }

        initView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPresenter.init();
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.deinit();
    }

    @Override
    public void setPresenter(BrowsePathContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void updateTreeView(Node root) {
        if ((mCurrentNode != null && (mCurrentNode.equals(root)) || root.equals(mTmpNode))) {
            createChildrenNode(root);
        } else if (mRoot == null) {
            createTypeView(root);
        } else {
            mRoot.replaceChild(mRoot, root);
        }

        if (root.getType() == Node.ROOT) {
            mRoot = root;
        }

        setEmptyViewVisible(false);
        setLoadingViewVisible(false);
    }

    @Override
    public void setLoadingViewVisible(boolean show) {
        if (show) {
            mEnableProgressBar = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mEnableProgressBar)
                        mProgressBar.setVisibility(View.VISIBLE);
                }
            }, 200);
        } else {
            mEnableProgressBar = false;
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(final String msg) {
        if (getActivity() != null) {
            setLoadingViewVisible(false);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            setEmptyViewVisible(true);
        }
    }

    @Override
    public void setResult(int resultCode, Bundle data) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(data.getString("data")));

        getActivity().setResult(resultCode, intent);
        getActivity().finish();
    }

    @Override
    public void showAuthDialog(@NonNull final Node node,
                               @NonNull final Bundle data) {
        setLoadingViewVisible(true);
        FPDialog dialog = new FPDialog(getActivity());
        // 是否为手动输入url添加设备的判断标志
        final boolean isAddNew = node.isCategory() || !TextUtils.isEmpty(data.getString("url"));
        String title;

        if (isAddNew) {
            title = getString(R.string.add_smb);
        } else {
            title = getString(R.string.samba_access_to,
                    node.getTitle().replace('/', ' '));
        }

        dialog.setTitle(title);
        dialog.setContentView(R.layout.layout_user_auth);

        final EditText urlView = (EditText) dialog.findViewById(R.id.smb_url);
        final TextView usernameView = (TextView) dialog.findViewById(R.id.auth_username);
        final TextView passwordView = (TextView) dialog.findViewById(R.id.auth_password);
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.anonymous_checkbox);

        if (isAddNew) {
            urlView.setVisibility(View.VISIBLE);
            urlView.requestFocus();
            if (!TextUtils.isEmpty(data.getString("url"))) {
                urlView.setText(data.getString("url"));
            }
        }

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    usernameView.setFocusable(false);
                    usernameView.setEnabled(false);
                    passwordView.setFocusable(false);
                    passwordView.setEnabled(false);
                } else {
                    usernameView.setFocusable(true);
                    usernameView.setEnabled(true);
                    usernameView.setFocusableInTouchMode(true);
                    passwordView.setFocusable(true);
                    passwordView.setEnabled(true);
                    passwordView.setFocusableInTouchMode(true);
                    usernameView.requestFocus();
                }
            }
        });

        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = usernameView.getText().toString();
                String password = passwordView.getText().toString();
                String url = urlView.getText().toString();

                if (!checkBox.isChecked() && username.isEmpty()) {
                    Toast.makeText(getActivity(),
                            R.string.username_cannot_empty, Toast.LENGTH_LONG).show();
                    return;
                }

                if (isAddNew && TextUtils.isEmpty(url)) {
                    Toast.makeText(getActivity(),
                            R.string.url_cannot_empty, Toast.LENGTH_LONG).show();
                    return;
                }

                if (isAddNew) {
                    // 此参数可用作判断是否为输入url添加设备
                    data.putString("url", url);
                }

                data.putString("username", username);
                data.putString("password", password);
                data.putBoolean("anonymous", checkBox.isChecked());

                Node n = node;
                if (isAddNew) {
                    SmbFile smbFile;
                    try {
                        smbFile = new SmbFile(url);
                    } catch (MalformedURLException e) {
                        Toast.makeText(getActivity(), R.string.url_is_invalid, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        return;
                    }

                    n = new Node(url, SmbFileHelper.getName(smbFile), Node.SAMBA, smbFile);
                    mTmpNode = n;
                }

                mPresenter.checkAuthData(n, data);
                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setLoadingViewVisible(false);
                if (!isAddNew) {
                    fallBackParent(node);
                }
                dialog.dismiss();
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setLoadingViewVisible(false);
                if (!isAddNew) {
                    fallBackParent(node);
                }
            }
        });

        if (data.containsKey("anonymous") && !data.getBoolean("anonymous")) {
            checkBox.setChecked(false);
            usernameView.setText(data.getString("username"));
            passwordView.setText(data.getString("password"));
        }

        dialog.show();
    }

    @Override
    public void fallBackParent(Node preNode) {
        Node parent = mCurrentNode.getParent();
        if (preNode == mCurrentNode
                && parent != null
                && parent.getType() != Node.ROOT) {
            mCurrentNode = parent;
        }
    }

    @Override
    public void showOnSelectConfirm(final Node node) {
        FPDialog dialog = new FPDialog(getActivity());
        dialog.setTitle(R.string.add_directory_dilog_title);
        dialog.setContentView(R.layout.dialog_add_directory_notice);

        ((TextView) dialog.findViewById(R.id.selected_dir)).setText(node.getTitle());
        final CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.private_checkbox);
        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checkBox.isChecked()) {
                    mPresenter.onSelect(node, true, true);
                } else {
                    mPresenter.onSelect(node, false, true);
                }

                dialog.dismiss();
            }
        });
        dialog.setNegativeButton(android.R.string.cancel, null);
        dialog.show();
    }

    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mTreeContainer.setLayoutManager(layoutManager);

        mFileAndDeviceListAdapter = new FileAndDeviceListAdapter(getActivity(), this);
        mTreeContainer.setAdapter(mFileAndDeviceListAdapter);

        mFocusedDirView.setText(getString(R.string.selected, "-"));
        mFocusedLastModifiedView.setText(getString(R.string.last_modify_date, "-"));
        mIbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void createChildrenNode(Node node) {
        List<Node> children = node.getChildren();

        if (children == null) {
            children = new ArrayList<>();
        }

        int type = node.getType();
        Node firstNode = null;

        try {
            firstNode = children.get(0);
        } catch (IndexOutOfBoundsException ignored) {
        }

        if ((firstNode == null || !PARENT_NODE_ID.equals(firstNode.getId()))
                && !node.isCategory() && !node.equals(mTmpNode)) {
            Node parentNode = new Node(PARENT_NODE_ID, "..", -100, null);
            children.add(0, parentNode);
        }

        mFileAndDeviceListAdapter.setData(children);
    }

    private void createTypeView(Node root) {
        mTypeContainer.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mTypeContainer.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mDeviceTypeListAdapter = new DeviceTypeListAdapter(getActivity(), root, this);
        mTypeContainer.setAdapter(mDeviceTypeListAdapter);
    }

    @Override
    public void onClick(Node node) {
        if (PARENT_NODE_ID.equals(node.getId())) {
            if (mCurrentNode.getParent() != null) {
                mCurrentNode = mCurrentNode.getParent();
            }
        } else {
            mCurrentNode = node;
        }

        if (mCurrentNode.getChildren() == null
                || mCurrentNode.getChildren().isEmpty()
                || mCurrentNode.getType() == Node.SAMBA_CATEGORY
                || mCurrentNode.getType() == Node.SAMBA_DEVICE) {
            mPresenter.getChildren(mCurrentNode);
        } else {
            updateTreeView(mCurrentNode);
        }
    }

    @Override
    public void onAddButtonClick(Node node) {
        showAuthDialog(node, true);
    }

    @Override
    public void onLongClick(final Node node) {
        onSelectItem(node);
    }

    @Override
    public void onFocusChange(Node node, boolean focus) {
        String lastModified = "-";

        if (node.getItem() instanceof FileItem) {
            FileItem fileItem = (FileItem) node.getItem();
            lastModified = fileItem.getDate();
        }

        String text;
        if (node.getItem() instanceof File) {
            File file = (File) node.getItem();
            text = node.getTitle();
            if (file != null && file.isDirectory())
                text = file.getPath();
        } else {
            text = node.getTitle();
        }

        mFocusedDirView.setText(getString(R.string.selected, focus ? text : "-"));
        mFocusedDirView.setSelected(true);
        mFocusedLastModifiedView.setText(
                getString(R.string.last_modify_date, lastModified));
    }

    public boolean onBackPressed() {
        if (isLoading()) {
            mPresenter.cancelTask(mCurrentNode);
            setLoadingViewVisible(false);
            return true;
        }

        Node node = mCurrentNode.getParent();

        if (node == null || node.getType() == Node.ROOT) {
            return false;
        }

        if (mEmptyView.getVisibility() == View.VISIBLE) {
            setEmptyViewVisible(false);
            return true;
        }

        mCurrentNode = mCurrentNode.getParent();
        updateTreeView(mCurrentNode);

        return true;
    }

    public boolean isLoading() {
        return mEnableProgressBar;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    private void setEmptyViewVisible(boolean show) {
        if (show) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTreeContainer.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mTreeContainer.setVisibility(View.VISIBLE);
        }
    }


    public void dispatchKeyMenu(){

    }

    private void onSelectItem(Node node) {
        if (node.getType() == Node.SAMBA) {
            if (node.getItem() instanceof SmbFile) {
                SmbFile smbFile = (SmbFile) node.getItem();

                try {
                    if (smbFile.getType() == SmbFile.TYPE_SHARE
                            && !mPresenter.isAlreadyAuth(node)) {
                        showAuthDialog(node, true);
                    } else {
                        mPresenter.onSelect(node, false, false);
                    }
                } catch (SmbException e) {
                    e.printStackTrace();
                }
            }
        } else if (node.getType() != Node.SAMBA_CATEGORY
                && node.getType() != Node.SAMBA_DEVICE) {
            mPresenter.onSelect(node, false, false);
        }
    }

    private void showAuthDialog(Node node, boolean checkOnly) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("checkOnly", checkOnly);
        showAuthDialog(node, bundle);
    }



}
