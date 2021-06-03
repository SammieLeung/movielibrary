package com.firefly.filepicker.picker.browse;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.Environment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.filepicker.R;
import com.firefly.filepicker.data.bean.FileItem;
import com.firefly.filepicker.data.bean.Node;

import org.fourthline.cling.support.model.item.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by rany on 18-2-28.
 */

public class FileAndDeviceListAdapter
        extends RecyclerView.Adapter<FileAndDeviceListAdapter.ViewHolder> {
    private static final String TAG = FileAndDeviceListAdapter.class.getSimpleName();

    private Context mContext;
    private ItemEventListener mListener;

    private List<Node> mData = new ArrayList<>();

    public FileAndDeviceListAdapter(Context context, ItemEventListener listener) {
        mContext = context;
        mListener = listener;

        setHasStableIds(true);
    }

    @Override
    public FileAndDeviceListAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_or_file_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            FileAndDeviceListAdapter.ViewHolder holder, int position) {
        final Node node = mData.get(position);
        FileItem fileItem = null;
        int iconId;
        String title = node.getTitle();

        switch (node.getType()) {
            case Node.DLNA_DEVICE:
                iconId = R.mipmap.dlna_focus;
                break;
            case Node.SAMBA_DEVICE:
                iconId = R.mipmap.net_focus;
                break;
            case Node.EXTERNAL_DEVICE:
                if (node.getId().equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    iconId = R.mipmap.local_storage_icon_focus;
                    title = mContext.getString(R.string.external_name);
                } else {
                    iconId = R.mipmap.flash_disk_icon;
                    title = String.format("%s (%s)", mContext.getString(R.string.udisk_name), title);
                }
                break;
            case Node.INTERNAL_DEVICE:
                iconId = R.mipmap.local_storage_icon_focus;
                break;
            default:
                if ("..".equals(node.getTitle())) {
                    iconId = R.drawable.ic_back_white;
                } else if (isFile(node)) {
                    iconId = R.drawable.ic_file_white;
                } else {
                    iconId = R.mipmap.folder;
                }
                break;
        }
        holder.mIcon.setImageResource(iconId);

        if (node.getItem() instanceof FileItem) {
            fileItem = (FileItem) node.getItem();
        }

        holder.mTitle.setText(title);
        holder.mDate.setText(fileItem != null ? fileItem.getDate() : "-");

        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.item_even_selector);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_odd_selector);
        }

        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                mListener.onFocusChange(node, true);
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(node);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!BrowsePathFragment.PARENT_NODE_ID.equals(node.getId())) {
                    mListener.onLongClick(node);
                    return true;
                }
                return false;
            }
        });
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mListener.onFocusChange(node, hasFocus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private boolean isFile(Node node) {
        if (node.getType() == Node.DLNA
                && node.getItem() instanceof Item) {
            return true;
        } else if (node.getType() == Node.EXTERNAL
                && ((File)node.getItem()).isFile()) {
            return true;
        } else if (node.getType() == Node.SAMBA) {
            final SmbFile smbFile = (SmbFile) node.getItem();
            final ConditionVariable conditionVariable = new ConditionVariable();
            final boolean[] isFile = {false};

            if (smbFile != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            isFile[0] = smbFile.isFile();
                        } catch (SmbException e) {
                            Log.d(TAG, e.getLocalizedMessage());
                        }
                        conditionVariable.open();
                    }
                }).start();
                conditionVariable.block();
                return isFile[0];
            }

            return false;
        }

        return false;
    }

    public void addData(List<Node> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void setData(List<Node> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mIcon;
        TextView mTitle;
        TextView mDate;

        ViewHolder(View itemView) {
            super(itemView);

            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.title);
            mDate = (TextView) itemView.findViewById(R.id.date);
        }
    }
}
