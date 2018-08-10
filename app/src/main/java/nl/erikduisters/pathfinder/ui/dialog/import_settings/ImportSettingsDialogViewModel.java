package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.Group;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.Group.GroupType;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntry.GroupEntryType;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntrySpinner;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.GroupEntryTrackActivityTypes;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.ImportLocalFilesGroup;
import nl.erikduisters.pathfinder.ui.dialog.import_settings.ImportSettingsAdapterData.SearchTracksOnGpsiesGroup;
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
public class ImportSettingsDialogViewModel extends ViewModel {
    private final MutableLiveData<ImportSettingsDialogViewState> viewStateObservable;

    private final PreferenceManager preferenceManager;

    @Inject
    ImportSettingsDialogViewModel(PreferenceManager preferenceManager) {
        viewStateObservable = new MutableLiveData<>();

        this.preferenceManager = preferenceManager;
    }

    LiveData<ImportSettingsDialogViewState> getViewStateObservable() {
        if (viewStateObservable.getValue() == null) {
            viewStateObservable.setValue(new ImportSettingsDialogViewState(createOptionsMenu(), createAdapterData()));
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

        group.addChild(new ImportSettingsAdapterData.GroupEntrySeekbar(R.string.max_tracks_to_search_for, 0, 10, 10, 250, R.string.seekbar_value));
        group.addChild(new GroupEntryTrackActivityTypes(R.string.track_activity_types_to_include, true));
        group.addChild(new ImportSettingsAdapterData.GroupEntryTrackType(R.string.track_type, true, false));

        //TODO: Maybe make these dependent on the track activity types included
        //TODO: Maybe make step, min and max configurable in settings
        @StringRes int valueResId = preferenceManager.getUnits() == Units.METRIC ? R.string.seekbar_value_km : R.string.seekbar_value_mi;
        group.addChild(new ImportSettingsAdapterData.GroupEntryTrackLength(R.string.track_length, R.string.track_length_minimum, R.string.track_length_maximum, 1, 1, 500, valueResId));

        group.setCanExpand(true);

        return group;
    }

    private void addTrackSearchTypeCenterWithRadiusChildren(SearchTracksOnGpsiesGroup group) {
        boolean enabled = !preferenceManager.mapFollowsGps();
        group.addChild(1, new ImportSettingsAdapterData.GroupEntryRadiogroup(R.string.track_search_center, R.string.track_search_center_gps,
                R.string.track_search_center_map, 1, false, false, enabled));

        int max = 50;
        @StringRes int unitResId = R.string.seekbar_value_km;

        if (preferenceManager.getUnits() == Units.IMPERIAL) {
            max = (int) UnitsUtil.kilometers2Miles(max);
            unitResId = R.string.seekbar_value_mi;
        }

        group.addChild(2, new ImportSettingsAdapterData.GroupEntrySeekbar(R.string.track_search_radius, 0, 1, 5, max, unitResId));

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
    ImportSettingsDialogViewState assertCurrentViewstateNotNull() {
        ImportSettingsDialogViewState currentViewState = viewStateObservable.getValue();

        if (currentViewState == null) {
            throw new IllegalStateException("onChanged called but current view state is null");
        }

        return currentViewState;
    }

    void onChanged(ImportSettingsAdapterData.GroupEntry changedGroupEntry) {
        ImportSettingsDialogViewState currentViewState = assertCurrentViewstateNotNull();

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

    private void onChanged(GroupEntrySpinner groupEntry, ImportSettingsDialogViewState currentViewState) {
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

        viewStateObservable.setValue(new ImportSettingsDialogViewState(currentViewState.optionsMenu, importSettingsAdapterData));
    }

    private void onChanged(GroupEntryTrackActivityTypes groupEntry, ImportSettingsDialogViewState currentViewState) {
        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);
        optionsMenu.findItem(R.id.menu_search).setEnabled(!groupEntry.areAllTrackActivityTypesExcluded());

        viewStateObservable.setValue(new ImportSettingsDialogViewState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    private void onChanged(ImportLocalFilesGroup group, ImportSettingsDialogViewState currentViewState) {
        boolean anyChecked = false;
        for (ImportSettingsAdapterData.Item child : group.getChildren()) {
            if (((ImportSettingsAdapterData.GroupEntryFile)child).isChecked()) {
                anyChecked = true;
                break;
            }
        }

        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);
        optionsMenu.findItem(R.id.menu_import).setEnabled(anyChecked);

        viewStateObservable.setValue(new ImportSettingsDialogViewState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    void onGroupExpanded(Group expandedGroup) {
        ImportSettingsDialogViewState currentViewState = assertCurrentViewstateNotNull();

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

        viewStateObservable.setValue(new ImportSettingsDialogViewState(optionsMenu, adapterData));
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
        ImportSettingsDialogViewState currentViewState = assertCurrentViewstateNotNull();
        MyMenu optionsMenu = new MyMenu(currentViewState.optionsMenu);

        for (MyMenuItem menuItem : optionsMenu.getMenuItems()) {
            if (menuItem.isVisible()) {
                menuItem.setEnabled(false);
            }
        }

        viewStateObservable.setValue(new ImportSettingsDialogViewState(optionsMenu, currentViewState.importSettingsAdapterData));
    }

    /*
         At this point I need to determine what to do: search for tracks or import tracks
         Searching has to be done using an intentService (udacity rubic)
            - Search result has to be presented to use so he/she can select which track to import
            - After user selects tracks those tracks need to be downloaded (intentService) and imported
            - Local .gpx files need to be imported

            GPSiesService extents IntentService
               Job
                 SearchTracks
                 DownloadTracks

            ImportService extends Service
                ImportGpxFiles
     */
    void onMenuItemSelected(MyMenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.id.menu_search:
                //TODO: Implement
                //TODO: Wait for GPS fix
                break;
        }
    }

    @Nullable
    public Parcelable onSaveInstanceState() {
        ImportSettingsDialogViewState currentState = viewStateObservable.getValue();

        if (currentState != null) {
            return currentState.importSettingsAdapterData.onSaveInstanceState();
        }

        return null;
    }

    public void onRestoreInstanceState(@Nullable Parcelable savedState) {
        if (savedState == null || viewStateObservable.getValue() != null) {
            Timber.d("onRestoreInstanceState: Not restoring state");
            return;
        }

        Timber.d("onRestoreInstanceState: Restoring state");

        ImportSettingsAdapterData adapterData = createAdapterData();
        adapterData.onRestoreInstanceState(savedState);

        SearchTracksOnGpsiesGroup searchTracksOnGpsiesGroup =
                (SearchTracksOnGpsiesGroup) adapterData.getGroupOfType(GroupType.TYPE_SEARCH_TRACKS_NEARBY);
        GroupEntrySpinner searchTypeGroup = (GroupEntrySpinner) searchTracksOnGpsiesGroup.getChild(0);
        GroupEntryTrackActivityTypes trackActivityTypesGroup = (GroupEntryTrackActivityTypes) searchTracksOnGpsiesGroup.getChild(4);

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

        viewStateObservable.setValue(new ImportSettingsDialogViewState(optionsMenu, adapterData));
    }



    @Override
    protected void onCleared() {
        super.onCleared();

        viewStateObservable.setValue(null);
    }
}
