package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import org.oscim.core.MapPosition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.GpsManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.model.Track;
import nl.erikduisters.pathfinder.data.model.TrackActivityType;
import nl.erikduisters.pathfinder.service.gpsies_service.SearchTracks;
import nl.erikduisters.pathfinder.ui.dialog.MessageWithTitle;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.Group;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.Group.GroupType;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntry.GroupEntryType;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntryRadiogroup;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntrySeekbar;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntrySpinner;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntryTrackActivityTypes;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntryTrackLength;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntryTrackType;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.ImportLocalFilesGroup;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.SearchTracksOnGpsiesGroup;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewState.InitializedState;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsDialogViewState.ShowCancelMessageDialogState;
import nl.erikduisters.pathfinder.util.BoundingBox;
import nl.erikduisters.pathfinder.util.Coordinate;
import nl.erikduisters.pathfinder.util.FileUtil;
import nl.erikduisters.pathfinder.util.Units;
import nl.erikduisters.pathfinder.util.UnitsUtil;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 06-08-2018.
 */

@Singleton
public class ImportSettingsDialogViewModel extends ViewModel implements GpsManager.LocationListener {
    private final MutableLiveData<ImportSettingsDialogViewState> viewStateObservable;

    private final PreferenceManager preferenceManager;
    private final GpsManager gpsManager;

    @Inject
    ImportSettingsDialogViewModel(PreferenceManager preferenceManager, GpsManager gpsManager) {
        viewStateObservable = new MutableLiveData<>();

        this.preferenceManager = preferenceManager;
        this.gpsManager = gpsManager;
    }

    LiveData<ImportSettingsDialogViewState> getViewStateObservable() {
        if (viewStateObservable.getValue() == null) {
            viewStateObservable.setValue(new InitializedState(createOptionsMenu(), createAdapterData()));
        }

        return viewStateObservable;
    }

    private ImportSettingsAdapterData createAdapterData() {
        ImportSettingsAdapterData adapterData = new ImportSettingsAdapterData();

        adapterData.add(createSearchTracksOnGpsiesGroup());
        adapterData.add(createImportLocalFilesGroup());

        return adapterData;
    }

    private Group createSearchTracksOnGpsiesGroup() {
        SearchTracksOnGpsiesGroup group = new SearchTracksOnGpsiesGroup(R.string.search_tracks_on_gpsies, true);

        MyMenu menu = new MyMenu();
        menu.add(new MyMenuItem(R.id.menu_import_search_type_center_with_radius, true, true));
        menu.add(new MyMenuItem(R.id.menu_import_search_type_map_viewport, true, true));

        group.addChild(new GroupEntrySpinner(R.string.track_search_type, R.menu.import_search_type_menu, menu, R.id.menu_import_search_type_center_with_radius, true));

        addTrackSearchTypeCenterWithRadiusChildren(group);

        //group.addChild(new GroupEntrySeekbar(R.string.max_tracks_to_search_for, 0, 10, 10, 250, R.string.seekbar_value));
        group.addChild(new GroupEntryTrackActivityTypes(R.string.track_activity_types_to_include, true));
        group.addChild(new GroupEntryTrackType(R.string.track_type, true, false));

        //TODO: Maybe make these dependent on the track activity types included
        //TODO: Maybe make step, min and max configurable in settings but make sure to check that (max - min) % step == 0 and that min is a multiple of step or 0
        @Units int units = preferenceManager.getUnits();

        @StringRes int valueResId = units == Units.METRIC ? R.string.seekbar_value_km : R.string.seekbar_value_mi;
        int min = units == Units.METRIC ? 5 : 3;
        int max = units == Units.METRIC ? 500 : 300;
        int step = units == Units.METRIC ? 5 : 3;
        group.addChild(new GroupEntryTrackLength(R.string.track_length, R.string.track_length_minimum, R.string.track_length_maximum, step, min, max, valueResId));

        group.setCanExpand(true);

        return group;
    }

    private void addTrackSearchTypeCenterWithRadiusChildren(SearchTracksOnGpsiesGroup group) {
        boolean enabled = !preferenceManager.mapFollowsGps();
        group.addChild(1, new GroupEntryRadiogroup(R.string.track_search_center, R.string.track_search_center_gps,
                R.string.track_search_center_map, 1, false, false, enabled));

        int min = 5;
        int max = 20;
        @StringRes int unitResId = R.string.seekbar_value_km;

        if (preferenceManager.getUnits() == Units.IMPERIAL) {
            min = (int) UnitsUtil.kilometers2Miles(min);
            max = (int) UnitsUtil.kilometers2Miles(max);
            unitResId = R.string.seekbar_value_mi;
        }

        group.addChild(2, new GroupEntrySeekbar(R.string.track_search_radius, 0, 1, min, max, unitResId));

    }

    private Group createImportLocalFilesGroup() {
        //TODO: Handle .zip files
        File importDir = preferenceManager.getStorageImportDir();

        List<File> gpxFiles = FileUtil.getFilesByExtension(importDir, true, ".gpx");

        Group group = new ImportLocalFilesGroup(R.string.import_from_import_directory, !gpxFiles.isEmpty());

        int sequenceNumber = 0;

        for (File gpxFile : gpxFiles) {
            group.addChild(new ImportSettingsAdapterData.GroupEntryFile(sequenceNumber, gpxFile, false, true));
            sequenceNumber++;
        }

        if (gpxFiles.size() != 0) {
            group.setCanExpand(true);
        }

        return group;
    }

    private MyMenu createOptionsMenu() {
        MyMenu optionsMenu = new MyMenu();

        optionsMenu.add(new MyMenuItem(R.id.menu_search, false, true));
        optionsMenu.add(new MyMenuItem(R.id.menu_import, false, false));

        return optionsMenu;
    }

    @NonNull
    InitializedState getCurrentInitializedState() {
        ImportSettingsDialogViewState currentViewState = viewStateObservable.getValue();

        if (!(currentViewState instanceof InitializedState)) {
            throw new IllegalStateException("Expecting current viewState to be InitializedState but it is not");
        }

        return (InitializedState) currentViewState;
    }

    void onChanged(ImportSettingsAdapterData.GroupEntry changedGroupEntry) {
        InitializedState currentViewState = getCurrentInitializedState();

        if (changedGroupEntry.labelResId == R.string.track_search_type) {
            onChanged((GroupEntrySpinner) changedGroupEntry, currentViewState);
        } else if (changedGroupEntry.getGroupEntryType() == GroupEntryType.TYPE_TRACK_ACTIVITY_TYPES) {
            onChanged((GroupEntryTrackActivityTypes) changedGroupEntry, currentViewState);
        } else if (changedGroupEntry instanceof ImportSettingsAdapterData.GroupEntryFile) {
            //noinspection ConstantConditions
            onChanged((ImportLocalFilesGroup) currentViewState.importSettingsAdapterData
                    .getGroupOfType(GroupType.TYPE_IMPORT_LOCAL_FILES), currentViewState);
        }
    }

    private void onChanged(GroupEntrySpinner groupEntry, InitializedState currentViewState) {
        MyMenuItem selectedMenuItem = groupEntry.getMenu().findItem(groupEntry.selectedMenuItemId());

        ImportSettingsAdapterData importSettingsAdapterData = new ImportSettingsAdapterData(currentViewState.importSettingsAdapterData);

        SearchTracksOnGpsiesGroup oldGroup = (SearchTracksOnGpsiesGroup) importSettingsAdapterData.get(0);
        SearchTracksOnGpsiesGroup newGroup = new SearchTracksOnGpsiesGroup(oldGroup);

        if (selectedMenuItem.getId() == R.id.menu_import_search_type_center_with_radius) {
            addTrackSearchTypeCenterWithRadiusChildren(newGroup);
        } else {
            newGroup.getChildren().remove(1);
            newGroup.getChildren().remove(1);
        }

        importSettingsAdapterData.getGroups().set(0, newGroup);

        viewStateObservable.setValue(new InitializedState(currentViewState.optionsMenu, importSettingsAdapterData));
    }

    private void onChanged(GroupEntryTrackActivityTypes groupEntry, InitializedState currentViewState) {
        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);
        optionsMenu.findItem(R.id.menu_search).setEnabled(!groupEntry.areAllTrackActivityTypesExcluded());

        viewStateObservable.setValue(new InitializedState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    private void onChanged(ImportLocalFilesGroup group, InitializedState currentViewState) {
        boolean anyChecked = false;
        for (ImportSettingsAdapterData.Item child : group.getChildren()) {
            if (((ImportSettingsAdapterData.GroupEntryFile)child).isChecked()) {
                anyChecked = true;
                break;
            }
        }

        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);
        optionsMenu.findItem(R.id.menu_import).setEnabled(anyChecked);

        viewStateObservable.setValue(new InitializedState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    void onGroupExpanded(Group expandedGroup) {
        InitializedState currentViewState = getCurrentInitializedState();

        ImportSettingsAdapterData adapterData = currentViewState.importSettingsAdapterData;
        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);

        if (expandedGroup.getGroupType() == GroupType.TYPE_SEARCH_TRACKS_NEARBY) {
            ImportLocalFilesGroup importLocalFilesGroup = (ImportLocalFilesGroup) adapterData.getGroupOfType(GroupType.TYPE_IMPORT_LOCAL_FILES);

            if (importLocalFilesGroup.isExpanded()) {
                adapterData = new ImportSettingsAdapterData(adapterData);

                ImportLocalFilesGroup newGroup = new ImportLocalFilesGroup(importLocalFilesGroup);
                newGroup.setIsExpanded(false);
                adapterData.getGroups().set(adapterData.getGroups().indexOf(importLocalFilesGroup), newGroup);
            }
        } else {
            SearchTracksOnGpsiesGroup searchTracksOnGpsiesGroup = (SearchTracksOnGpsiesGroup) adapterData.getGroupOfType(GroupType.TYPE_SEARCH_TRACKS_NEARBY);

            if (searchTracksOnGpsiesGroup.isExpanded()) {
                adapterData = new ImportSettingsAdapterData(adapterData);

                SearchTracksOnGpsiesGroup newGroup = new SearchTracksOnGpsiesGroup(searchTracksOnGpsiesGroup);
                newGroup.setIsExpanded(false);
                adapterData.getGroups().set(adapterData.getGroups().indexOf(searchTracksOnGpsiesGroup), newGroup);
            }
        }

        updateOptionsMenuForExpandedGroup(expandedGroup, optionsMenu);

        viewStateObservable.setValue(new InitializedState(optionsMenu, adapterData));
    }

    private void updateOptionsMenuForExpandedGroup(Group expandedGroup, MyMenu optionsMenu) {
        MyMenuItem menuItem;

        if (expandedGroup.getGroupType() == GroupType.TYPE_SEARCH_TRACKS_NEARBY) {
            GroupEntryTrackActivityTypes groupEntry = expandedGroup.findGroupEntryByLabel(R.string.track_activity_types_to_include);

            menuItem = optionsMenu.findItem(R.id.menu_search);
            menuItem.setVisible(true);
            menuItem.setEnabled(!groupEntry.areAllTrackActivityTypesExcluded());

            optionsMenu.findItem(R.id.menu_import).setVisible(false);
        } else {
            boolean anyChecked = false;

            for (ImportSettingsAdapterData.Item child : expandedGroup.getChildren()) {
                ImportSettingsAdapterData.GroupEntryFile groupEntry = (ImportSettingsAdapterData.GroupEntryFile) child;

                if (groupEntry.isChecked()) {
                    anyChecked = true;
                    break;
                }
            }

            menuItem = optionsMenu.findItem(R.id.menu_import);
            menuItem.setVisible(true);
            menuItem.setEnabled(anyChecked);

            optionsMenu.findItem(R.id.menu_search).setVisible(false);
        }
    }

    void onGroupCollapsed(Group group) {
        //Now both must be collapsed, find visible menu item and disable
        InitializedState currentViewState = getCurrentInitializedState();
        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);

        for (MyMenuItem menuItem : optionsMenu.getMenuItems()) {
            if (menuItem.isVisible()) {
                menuItem.setEnabled(false);
            }
        }

        viewStateObservable.setValue(new InitializedState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    void onMenuItemSelected(MyMenuItem menuItem) {
        InitializedState currentState = getCurrentInitializedState();
        ImportSettingsAdapterData adapterData = currentState.importSettingsAdapterData;

        switch (menuItem.getId()) {
            case R.id.menu_search:
                handleSearchTracksOnGpsies(adapterData);
                break;
            case R.id.menu_import:
                //TODO: Implement
                break;
        }
    }

    private void handleSearchTracksOnGpsies(ImportSettingsAdapterData adapterData) {
        SearchTracksOnGpsiesGroup group = (SearchTracksOnGpsiesGroup) adapterData.getGroupOfType(GroupType.TYPE_SEARCH_TRACKS_NEARBY);

        GroupEntrySpinner searchTypeGroupEntry = group.findGroupEntryByLabel(R.string.track_search_type);

        if (searchTypeGroupEntry.selectedMenuItemId() == R.id.menu_import_search_type_center_with_radius) {
            if (((GroupEntryRadiogroup) group.findGroupEntryByLabel(R.string.track_search_center)).selectedButton() == 1 && !gpsManager.gpsHasFix()) {
                setShowCancelDialogState(getCurrentInitializedState());

                return;
            }
        }

        SearchTracks.JobInfo jobInfo = createSearchJobInfo((SearchTracksOnGpsiesGroup) adapterData.getGroupOfType(GroupType.TYPE_SEARCH_TRACKS_NEARBY));
        viewStateObservable.setValue(new ImportSettingsDialogViewState.DismissDialogState.ReportGpsiesServiceJobState(jobInfo));
    }

    private void setShowCancelDialogState(InitializedState initializedState) {
        MessageWithTitle messageWithTitle = new MessageWithTitle(R.string.no_gps_fix_dialog_title, R.string.no_gps_fix_dialog_message);

        viewStateObservable.setValue(new ShowCancelMessageDialogState(initializedState, messageWithTitle));

        gpsManager.addLocationListener(this);
    }

    void onCancelMessageDialogDismissed() {
        gpsManager.removeLocationListener(this);

        InitializedState currentState = getCurrentInitializedState();

        viewStateObservable.setValue(new InitializedState(currentState.optionsMenu, currentState.importSettingsAdapterData));
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (gpsManager.gpsHasFix()) {
            gpsManager.removeLocationListener(this);

            InitializedState currentState = getCurrentInitializedState();

            handleSearchTracksOnGpsies(currentState.importSettingsAdapterData);
        }
    }

    private SearchTracks.JobInfo createSearchJobInfo(SearchTracksOnGpsiesGroup group) {
        GroupEntrySpinner searchTypeGroupEntry = group.findGroupEntryByLabel(R.string.track_search_type);

        SearchTracks.JobInfo.Builder builder = new SearchTracks.JobInfo.Builder();

        if (searchTypeGroupEntry.selectedMenuItemId() == R.id.menu_import_search_type_center_with_radius) {
            builder.withBoundingBox(createBoundingBoxForCenterAndRadiusSearch(group));
        } else {
            builder.withBoundingBox(preferenceManager.getMapBoundingBox());
        }

        //int maxTracks = ((GroupEntrySeekbar) group.findGroupEntryByLabel(R.string.max_tracks_to_search_for)).getValue();

        List<TrackActivityType> includedTrackActivityTypes = ((GroupEntryTrackActivityTypes) group
                .findGroupEntryByLabel(R.string.track_activity_types_to_include)).getIncludedTrackActivityTypes();

        @Track.Type List<Integer> trackTypes = new ArrayList<>();

        GroupEntryTrackType groupEntryTrackType = group.findGroupEntryByLabel(R.string.track_type);

        if (groupEntryTrackType.isRoundTripChecked()) {
            trackTypes.add(Track.Type.ROUND_TRIP);
        }

        if (groupEntryTrackType.isOneWayChecked()) {
            trackTypes.add(Track.Type.ONE_WAY);
        }

        GroupEntryTrackLength groupEntryTrackLength = group.findGroupEntryByLabel(R.string.track_length);
        int minLength = groupEntryTrackLength.getMinValue();
        int maxLength = groupEntryTrackLength.getMaxValue();

        return builder
                //.withMaxTracks(maxTracks)
                .withTrackActivityTypes(includedTrackActivityTypes)
                .withTrackTypes(trackTypes)
                .withMinTrackLength(minLength)
                .withMaxTrackLength(maxLength)
                .build();
    }

    private BoundingBox createBoundingBoxForCenterAndRadiusSearch(SearchTracksOnGpsiesGroup group) {
        Coordinate center;

        if (((GroupEntryRadiogroup) group.findGroupEntryByLabel(R.string.track_search_center)).selectedButton() == 1) {
            Location location = gpsManager.getLastKnowLocation();
            center = new Coordinate(location.getLatitude(), location.getLongitude());
        } else {
            MapPosition mapPosition = preferenceManager.getMapPosition();
            center = new Coordinate(mapPosition.getLatitude(), mapPosition.getLongitude());
        }

        int radiusKm = ((GroupEntrySeekbar) group.findGroupEntryByLabel(R.string.track_search_radius)).getValue();

        return new BoundingBox(center, radiusKm * 1000);
    }

    @Nullable
    public Parcelable onSaveInstanceState() {
        ImportSettingsDialogViewState currentState = viewStateObservable.getValue();

        if (currentState != null && currentState instanceof InitializedState) {
            SavedState savedState = new SavedState();

            savedState.adapterDataState = ((InitializedState) currentState).importSettingsAdapterData.onSaveInstanceState();
            savedState.showingCancelDialog = currentState instanceof ShowCancelMessageDialogState;

            return savedState;
        }

        return null;
    }

    public void onRestoreInstanceState(@Nullable Parcelable savedState) {
        if (savedState == null || viewStateObservable.getValue() != null) {
            Timber.d("onRestoreInstanceState: Not restoring state");
            return;
        }

        Timber.d("onRestoreInstanceState: Restoring state");

        SavedState state = (SavedState) savedState;

        ImportSettingsAdapterData adapterData = createAdapterData();
        adapterData.onRestoreInstanceState(state.adapterDataState);

        SearchTracksOnGpsiesGroup searchTracksOnGpsiesGroup =
                (SearchTracksOnGpsiesGroup) adapterData.getGroupOfType(GroupType.TYPE_SEARCH_TRACKS_NEARBY);
        GroupEntrySpinner searchTypeGroup = (GroupEntrySpinner) searchTracksOnGpsiesGroup.getChild(0);

        if (searchTypeGroup.selectedMenuItemId() == R.id.menu_import_search_type_map_viewport) {
            searchTracksOnGpsiesGroup.getChildren().remove(1);
            searchTracksOnGpsiesGroup.getChildren().remove(1);
        }

        MyMenu optionsMenu = createOptionsMenu();

        for (Group group : adapterData.getGroups()) {
            if (group.isExpanded()) {
                updateOptionsMenuForExpandedGroup(group, optionsMenu);
            }
        }

        InitializedState initializedState = new InitializedState(optionsMenu, adapterData);

        if (state.showingCancelDialog) {
            setShowCancelDialogState(initializedState);
        } else {
            viewStateObservable.setValue(initializedState);
        }
    }

    static class SavedState implements Parcelable {
        ImportSettingsAdapterData.SavedState adapterDataState;
        boolean showingCancelDialog;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.adapterDataState, flags);
            dest.writeByte(this.showingCancelDialog ? (byte) 1 : (byte) 0);
        }

        public SavedState() {
        }

        protected SavedState(Parcel in) {
            this.adapterDataState = in.readParcelable(ImportSettingsAdapterData.SavedState.class.getClassLoader());
            this.showingCancelDialog = in.readByte() != 0;
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

    @Override
    protected void onCleared() {
        super.onCleared();

        viewStateObservable.setValue(null);
    }
}
