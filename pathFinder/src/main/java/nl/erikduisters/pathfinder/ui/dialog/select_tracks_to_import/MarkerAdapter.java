package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import nl.erikduisters.pathfinder.data.model.Marker;
import nl.erikduisters.pathfinder.data.model.TrackType;
import nl.erikduisters.pathfinder.glide.CropHeightGraphTransformation;
import nl.erikduisters.pathfinder.glide.GlideApp;
import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesService;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.Distance;

/**
 * Created by Erik Duisters on 14-08-2018.
 */
public class MarkerAdapter
        extends RecyclerView.Adapter<MarkerAdapter.ViewHolder>
        implements View.OnClickListener, SvgView.SVGViewLoadedListener, SelectedMarkersProvider {

    @NonNull private List<Marker> markers;
    private SvgView roundTripSvgView;
    private SvgView oneWaySvgView;
    private SvgView totalAscentSvgView;
    private SvgView totalDescentSvgView;
    private int numSvgViewsToLoad;
    private SparseBooleanArray selectedMarkers;

    private final int heightChartWidth;
    private final int heightChartHeight;

    @Nullable RecyclerView recyclerView;

    @Nullable private OnSelectionChangedListener listener;

    MarkerAdapter(Context context) {
        markers = new ArrayList<>();
        selectedMarkers = new SparseBooleanArray();

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
    }

    @Override
    public void setOnSelectionChangedListener(@Nullable OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public List<Marker> getSelectedMarkers() {
        List<Marker> out = new ArrayList<>(selectedMarkers.size());

        for (int i = 0; i < selectedMarkers.size(); i++) {
            out.add(markers.get(selectedMarkers.keyAt(i)));
        }

        return out;
    }

    private void setupSvgView(SvgView svgView, int width, int height, @RawRes int rawResId) {
        svgView.setSize(width, height);
        svgView.setSvgResourceId(rawResId);
        svgView.setListener(this);

        numSvgViewsToLoad++;
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

    void setMarkers(List<Marker> markers) {
        this.markers = markers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.marker_list_row, parent, false);

        v.setOnClickListener(this);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(markers.get(position), position);
    }

    @Override
    public int getItemCount() {
        return markers.size();
    }

    @Override
    public void onClick(View v) {
        //TODO: Open map dialog/popupWindow showing track instead of calling checkBox.performClick();
        ViewHolder viewHolder = (ViewHolder) recyclerView.getChildViewHolder(v);

        viewHolder.checkBox.performClick();
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

    class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, RequestListener<Drawable> {
        @BindView(R.id.checkBox) CheckBox checkBox;
        @BindView(R.id.trackName) TextView trackName;
        @BindView(R.id.progressBar) ProgressBar progressBar;
        @BindView(R.id.heightChart) ImageView heightChart;
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

        void bind(Marker marker, int position) {
            checkBox.setOnCheckedChangeListener(this);
            checkBox.setChecked(selectedMarkers.get(position, false));
            checkBox.setClickable(false); //TODO: Remove when showing track preview on map has been implemented

            progressBar.setVisibility(View.VISIBLE);
            itemView.setSelected(checkBox.isChecked());

            trackName.setText(marker.filename);

            GlideApp.with(itemView.getContext())
                    .load(GPSiesService.getHeightChartUri(marker.fileId, heightChartWidth, heightChartHeight))
                    .transform(new CropHeightGraphTransformation(itemView.getContext()))
                    .override(Target.SIZE_ORIGINAL)
                    .error(R.drawable.vector_drawable_ic_no_image)
                    .listener(this)
                    .into(heightChart);

            length.setText(Distance.getDistance(itemView.getContext(), marker.trackLengthKilometers * 1000f, 2));

            SvgView trackTypeSvgView = marker.property == TrackType.ONE_WAY ? oneWaySvgView : roundTripSvgView;

            if (trackTypeSvgView.isRendered()) {
                trackTypeImageView.setImageBitmap(trackTypeSvgView.getBitmap());
            }

            String contentDescription = trackTypeSvgView.getContext().getString(marker.property == TrackType.ONE_WAY ? R.string.track_type_one_way : R.string.track_type_round_trip);
            trackTypeSvgView.setContentDescription(contentDescription);

            if (totalAscentSvgView.isRendered()) {
                totalAscentImageView.setImageBitmap(totalAscentSvgView.getBitmap());
            }

            totalAscent.setText(Distance.getDistance(itemView.getContext(), marker.totalAscentMeters, 0));

            if (totalDescentSvgView.isRendered()) {
                totalDescentImageView.setImageBitmap(totalDescentSvgView.getBitmap());
            }

            totalDescent.setText(Distance.getDistance(itemView.getContext(), marker.totalDescentMeters, 0));
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //This is not called when manually calling Checkbox.setChecked();
            itemView.setSelected(isChecked);

            int position = getAdapterPosition();

            if (isChecked) {
                selectedMarkers.put(position, true);
            } else {
                selectedMarkers.delete(position);
            }

            if (listener != null) {
                listener.onSelectionChanged(selectedMarkers.size());
            }
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

    Parcelable onSaveInstanceState() {
        return new SavedState(selectedMarkers);
    }

    void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;

        selectedMarkers = savedState.selectedMarkers;

        if (selectedMarkers.size() > 0 && listener != null) {
            listener.onSelectionChanged(selectedMarkers.size());
        }
    }

    private static class SavedState implements Parcelable {
        SparseBooleanArray selectedMarkers;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSparseBooleanArray(this.selectedMarkers);
        }

        public SavedState(SparseBooleanArray selectedMarkers) {
            this.selectedMarkers = selectedMarkers;
        }

        protected SavedState(Parcel in) {
            this.selectedMarkers = in.readSparseBooleanArray();
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
