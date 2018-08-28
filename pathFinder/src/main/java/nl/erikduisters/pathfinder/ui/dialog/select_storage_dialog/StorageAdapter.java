/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.ui.dialog.select_storage_dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.Storage;

/**
 * Created by Erik Duisters on 05-06-2018.
 */
class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> implements View.OnClickListener {
    private static final String KEY_SELECTED_STORAGE_POSITION = "SelectedStoragePosition";

    private static final long MEGABYTE = 1000*1000;
    private static final long GIGABYTE = 1000*1000*1000;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private int selectedStoragePos;
    @Nullable private OnItemClickListener onItemClickedListener;
    @Nullable private RecyclerView recyclerView;
    @NonNull private ArrayList<Storage> storageList;

    public StorageAdapter() {
        super();

        setHasStableIds(false);
        selectedStoragePos = -1;
        storageList = new ArrayList<>();
    }

    void setStorageList(@NonNull ArrayList<Storage> storageList, @Nullable Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);

        this.storageList = storageList;

        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        this.recyclerView = null;
    }

    void setOnItemClickListener(@Nullable OnItemClickListener listener) {
        onItemClickedListener = listener;
    }

    int getSelectedStoragePos() {
        return selectedStoragePos;
    }

    private String formatSize(long sizeInBytes) {
        if (sizeInBytes > GIGABYTE) {
            //return NumberFormat.getInstance().format((double)sizeInBytes/GIGABYTE) + "GB";
            return new DecimalFormat("#,###.0GB").format((double) sizeInBytes / GIGABYTE);
        } else {
            return new DecimalFormat("###MB").format(sizeInBytes / MEGABYTE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ssr_device) RadioButton device;
        @BindView(R.id.ssr_freeSpace) TextView freeSpace;
        @BindView(R.id.ssr_totalSize) TextView totalSpace;

        public ViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        public void bind(Storage item, boolean selected) {
            StringBuilder sb = new StringBuilder();
            sb.append(device.getContext().getString(item.getNameResId()));
            if (item.getSequenceNr() > 0) {
                sb.append(item.getSequenceNr());
            }

            device.setText(sb.toString());
            device.setChecked(selected);
            freeSpace.setText(item.getFreeSpace());
            totalSpace.setText(item.getTotalSpace());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_storage_row, parent, false);
        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh;

        int pos = recyclerView.getChildViewHolder(v).getAdapterPosition();

        if (pos == selectedStoragePos) {
            return;
        }

        if (selectedStoragePos != -1) {
            vh = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedStoragePos);
            vh.device.setChecked(false);
        }

        vh = (ViewHolder) recyclerView.getChildViewHolder(v);
        selectedStoragePos = vh.getAdapterPosition();

        vh.device.setChecked(true);

        if ( onItemClickedListener != null) {
            onItemClickedListener.onItemClick(selectedStoragePos);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(storageList.get(position), position == selectedStoragePos);
    }

    @Override
    public int getItemCount() {
        return storageList.size();
    }

    void saveInstanceState(Bundle outState) {
        outState.putInt(KEY_SELECTED_STORAGE_POSITION, selectedStoragePos);
    }

    private void restoreInstanceState(@Nullable Bundle state) {
        if (state == null) return;

        selectedStoragePos = state.getInt(KEY_SELECTED_STORAGE_POSITION);
    }
}
