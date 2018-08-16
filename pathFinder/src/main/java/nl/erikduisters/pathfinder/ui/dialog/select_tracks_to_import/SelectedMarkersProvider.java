package nl.erikduisters.pathfinder.ui.dialog.select_tracks_to_import;

import android.support.annotation.Nullable;

import java.util.List;

import nl.erikduisters.pathfinder.data.model.Marker;

/**
 * Created by Erik Duisters on 16-08-2018.
 */
public interface SelectedMarkersProvider {
    interface OnSelectionChangedListener {
        void onSelectionChanged(int numSelected);
    }

    void setOnSelectionChangedListener(@Nullable OnSelectionChangedListener listener);
    List<Marker> getSelectedMarkers();
}
