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

package nl.erikduisters.pathfinder.ui.fragment.track_list;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.MinimalTrack;
import nl.erikduisters.pathfinder.data.model.TrackType;
import nl.erikduisters.pathfinder.glide.CropHeightGraphTransformation;
import nl.erikduisters.pathfinder.glide.GlideApp;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.Distance;

/**
 * Created by Erik Duisters on 24-08-2018.
 */
//TODO: DiffUtils
public class MinimalTrackAdapter extends RecyclerView.Adapter<MinimalTrackAdapter.ViewHolder> implements SvgView.SVGViewLoadedListener, View.OnClickListener, View.OnLongClickListener {
    public interface Listener {
        void onItemClicked(MinimalTrack minimalTrack);
        void onItemLongClicked(MinimalTrack minimalTrack);
    }

    private @NonNull List<MinimalTrack> minimalTracks;

    private SvgView roundTripSvgView;
    private SvgView oneWaySvgView;
    private SvgView totalAscentSvgView;
    private SvgView totalDescentSvgView;
    private int numSvgViewsToLoad;
    private final int heightChartWidth;
    private final int heightChartHeight;

    private @Nullable RecyclerView recyclerView;
    private @Nullable Listener listener;
    private int selectedMinimalTrackPosition;

    MinimalTrackAdapter(Context context) {
        minimalTracks = new ArrayList<>(0);
        setHasStableIds(true);

        heightChartWidth = context.getResources().getDimensionPixelSize(R.dimen.height_chart_width);
        heightChartHeight = context.getResources().getDimensionPixelSize(R.dimen.height_chart_height);

        int width = context.getResources().getDimensionPixelSize(R.dimen.marker_list_row_svg_width);
        int height = context.getResources().getDimensionPixelSize(R.dimen.marker_list_row_svg_height);

        numSvgViewsToLoad = 0;

        roundTripSvgView = new SvgView(context);
        oneWaySvgView = new SvgView(context);
        totalAscentSvgView = new SvgView(context);
        totalDescentSvgView = new SvgView(context);

        setupSvgView(roundTripSvgView, width, height, R.raw.ic_round_trip);
        setupSvgView(oneWaySvgView, width, height, R.raw.ic_one_way);
        setupSvgView(totalAscentSvgView, width, height, R.raw.ic_trending_up);
        setupSvgView(totalDescentSvgView, width, height, R.raw.ic_trending_down);

        selectedMinimalTrackPosition = -1;
    }

    private void setupSvgView(SvgView svgView, int width, int height, @RawRes int rawResId) {
        svgView.setSize(width, height);
        svgView.setSvgResourceId(rawResId);
        svgView.setListener(this);

        numSvgViewsToLoad++;
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    public void setMinimalTracks(@NonNull List<MinimalTrack> minimalTracks) {
        this.minimalTracks = minimalTracks;
        this.selectedMinimalTrackPosition = -1;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.minimal_track_row, parent, false);

        v.setOnClickListener(this);
        v.setOnLongClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(minimalTracks.get(position));

        holder.itemView.setSelected(position == selectedMinimalTrackPosition);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            ViewHolder viewHolder = (ViewHolder) recyclerView.getChildViewHolder(v);
            int position = viewHolder.getAdapterPosition();

            listener.onItemClicked(minimalTracks.get(position));
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (listener != null) {
            ViewHolder viewHolder = (ViewHolder) recyclerView.getChildViewHolder(v);
            int position = viewHolder.getAdapterPosition();

            listener.onItemLongClicked(minimalTracks.get(position));
        }

        return true;
    }

    public void setSelected(MinimalTrack minimalTrack) {
        int position = minimalTracks.indexOf(minimalTrack);

        if (selectedMinimalTrackPosition >= 0 && position != selectedMinimalTrackPosition) {
            notifyItemChanged(selectedMinimalTrackPosition);
        }

        selectedMinimalTrackPosition = position;
        notifyItemChanged(selectedMinimalTrackPosition);
    }

    @Override
    public int getItemCount() {
        return minimalTracks.size();
    }

    @Override
    public long getItemId(int position) {
        return minimalTracks.get(position).id;
    }

    @Override
    public void onSvgRendered(SvgView v) {
        numSvgViewsToLoad--;

        if (numSvgViewsToLoad == 0) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onSvgRenderingFailed(Throwable e) {
        //TODO: Display error?
        onSvgRendered(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements RequestListener<Drawable> {
        @BindView(R.id.checkBox) CheckBox checkBox;
        @BindView(R.id.trackName) TextView trackName;
        @BindView(R.id.progressBar) ProgressBar progressBar;
        @BindView(R.id.heightChart) ImageView heightChart;
        @BindView(R.id.author) TextView author;
        @BindView(R.id.length) TextView length;
        @BindView(R.id.trackTypeImageView) ImageView trackTypeImageView;
        @BindView(R.id.totalAscentImageView) ImageView totalAscentImageView;
        @BindView(R.id.totalAscent) TextView totalAscent;
        @BindView(R.id.totalDescentImageView) ImageView totalDescentImageView;
        @BindView(R.id.totalDescent) TextView totalDescent;

        public ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            //I am tired of RecyclerView returning null on calls to getChildViewHolder
            heightChart.setTag(R.id.viewHolderTag, this);
        }

        void bind(MinimalTrack minimalTrack) {
            checkBox.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);
            itemView.setSelected(checkBox.isChecked());

            trackName.setText(minimalTrack.name);

            GlideApp.with(itemView.getContext())
                    .load(GPSiesService.getHeightChartUri(minimalTrack.gpsiesFileId, heightChartWidth, heightChartHeight))
                    .transform(new CropHeightGraphTransformation(itemView.getContext()))
                    .override(Target.SIZE_ORIGINAL)
                    .error(R.drawable.vector_drawable_ic_no_image)
                    .listener(this)
                    .into(heightChart);

            author.setText(minimalTrack.author);
            length.setText(Distance.getDistance(itemView.getContext(), minimalTrack.length, 2));

            SvgView trackTypeSvgView = minimalTrack.type == TrackType.ONE_WAY ? oneWaySvgView : roundTripSvgView;

            if (trackTypeSvgView.isRendered()) {
                trackTypeImageView.setImageBitmap(trackTypeSvgView.getBitmap());
            }

            String contentDescription = trackTypeSvgView.getContext().getString(minimalTrack.type == TrackType.ONE_WAY ? R.string.track_type_one_way : R.string.track_type_round_trip);
            trackTypeImageView.setContentDescription(contentDescription);

            if (totalAscentSvgView.isRendered()) {
                totalAscentImageView.setImageBitmap(totalAscentSvgView.getBitmap());
            }

            totalAscent.setText(Distance.getDistance(itemView.getContext(), minimalTrack.totalAscent, 0));

            if (totalDescentSvgView.isRendered()) {
                totalDescentImageView.setImageBitmap(totalDescentSvgView.getBitmap());
            }

            totalDescent.setText(Distance.getDistance(itemView.getContext(), minimalTrack.totalDescent, 0));
        }

        private ViewHolder getViewHolder(Target<Drawable> target) {
            ImageView imageView = ((DrawableImageViewTarget)target).getView();
            return (ViewHolder) imageView.getTag(R.id.viewHolderTag);
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            ViewHolder viewHolder = getViewHolder(target);

            viewHolder.progressBar.setVisibility(View.GONE);

            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            ViewHolder viewHolder = getViewHolder(target);

            viewHolder.progressBar.setVisibility(View.GONE);

            return false;
        }
    }
}
