package com.firefly.filepicker.picker.browse;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.filepicker.R;
import com.firefly.filepicker.data.bean.Node;

import java.util.List;

/**
 * Created by rany on 18-2-27.
 */

public class DeviceTypeListAdapter extends RecyclerView.Adapter<DeviceTypeListAdapter.ViewHolder> {
    private Context mContext;
    private List<Node> mDeviceTypeNodes;

    private ItemEventListener mListener;

    private View mSelectedItemView;

    public DeviceTypeListAdapter(Context context, Node root, ItemEventListener listener) {
        mContext = context;
        mDeviceTypeNodes = root.getChildren();
        mListener = listener;
    }

    @Override
    public DeviceTypeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_type_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DeviceTypeListAdapter.ViewHolder holder, int position) {
        final Node node = mDeviceTypeNodes.get(position);
        int normalIcon = -1;

        switch (node.getType()) {
            case Node.EXTERNAL_CATEGORY:
                normalIcon = R.drawable.local_storage_icon;
                break;
            case Node.DLNA_CATEGORY:
                normalIcon = R.drawable.dlna;
                break;
            case Node.SAMBA_CATEGORY:
                holder.mAddButton.setVisibility(View.VISIBLE);
                holder.mInfoView.setNextFocusRightId(R.id.add_button);
            default:
                normalIcon = R.drawable.net_icon;
                break;
        }

        holder.mIconView.setImageResource(normalIcon);
        holder.mTitle.setText(node.getTitle());

//        if (position % 2 == 0) {
//            holder.itemView.setBackgroundResource(R.drawable.item_odd_selector);
//        } else {
//            holder.itemView.setBackgroundResource(R.drawable.item_even_selector);
//        }

        if (mSelectedItemView == null && position == 0) {
            mSelectedItemView = holder.itemView;
            mSelectedItemView.setSelected(true);
            mSelectedItemView.requestFocus();

            mListener.onClick(node);

        }

        holder.mInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(node);

                setSelected(holder);
            }
        });

        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelected(holder);

                if (mListener != null) {
                    mListener.onAddButtonClick(node);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDeviceTypeNodes != null ? mDeviceTypeNodes.size() : 0;
    }

    private void setSelected(ViewHolder holder) {
        mSelectedItemView.setSelected(false);
        mSelectedItemView = holder.itemView;
        mSelectedItemView.setSelected(true);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mIconView;
        TextView mTitle;
        View mInfoView;
        ImageButton mAddButton;

        ViewHolder(View itemView) {
            super(itemView);

            mIconView = (ImageView) itemView.findViewById(R.id.icon);
            mTitle = (TextView) itemView.findViewById(R.id.tv_title);
            mInfoView = itemView.findViewById(R.id.row_info);
            mAddButton = (ImageButton) itemView.findViewById(R.id.add_button);
        }
    }
}
