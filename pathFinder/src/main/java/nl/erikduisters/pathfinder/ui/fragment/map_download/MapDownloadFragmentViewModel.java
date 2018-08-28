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

package nl.erikduisters.pathfinder.ui.fragment.map_download;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.data.local.StorageHelper;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewState.DisplayMessageState;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewState.ScheduleMapDownloadState;
import nl.erikduisters.pathfinder.ui.fragment.map_download.MapDownloadFragmentViewState.ShowWebsiteState;
import nl.erikduisters.pathfinder.util.StringProvider;

/**
 * Created by Erik Duisters on 26-07-2018.
 */

//TODO: Create a launchPage (String) that shows the user a number of possible download sites as buttons
//TODO: Re-populate downloadingMapUrls from DownloadManager but every map url will get a redirect to http://ftp.gwdg.de ==> just remember the map name and if I ever support more than 1 download site rename the map (eg. Belgium-OpenAndroMaps.zip)

@Singleton
public class MapDownloadFragmentViewModel extends ViewModel {
    private static final String OPENANDROMAPSURL = "https://www.openandromaps.org/en/downloads/countrys-and-regions";

    private final MutableLiveData<MapDownloadFragmentViewState> viewStateObservable;
    private final MutableLiveData<ScheduleMapDownloadState> scheduleMapDownloadStateObservable;

    private final List<String> downloadingMapUrls;
    private final StorageHelper storageHelper;
    private final PreferenceManager preferenceManager;

    @Override
    protected void onCleared() {
        super.onCleared();

        downloadingMapUrls.clear();
    }

    @Inject
    MapDownloadFragmentViewModel(StorageHelper storageHelper, PreferenceManager preferenceManager) {
        viewStateObservable = new MutableLiveData<>();
        scheduleMapDownloadStateObservable = new MutableLiveData<>();
        downloadingMapUrls = new ArrayList<>();
        this.storageHelper = storageHelper;
        this.preferenceManager = preferenceManager;
    }

    LiveData<MapDownloadFragmentViewState> getViewStateObservable() { return viewStateObservable; }
    LiveData<ScheduleMapDownloadState> getScheduleMapDownloadStateObservable() { return scheduleMapDownloadStateObservable; }

    void start() {
        viewStateObservable.setValue(new ShowWebsiteState(OPENANDROMAPSURL));
    }

    void onWebSiteShown() {
        viewStateObservable.setValue(null);
    }

    void onMessageDisplayed() {
        viewStateObservable.setValue(null);
    }

    /*
       http[s]://www.openandromaps.org/en/legend/elevate-mountain-hike-theme  Install Rendertheme
       https://www.openandromaps.org/en/downloads/general-maps
       https://www.openandromaps.org/wp-content/users/tobias/Elevate4.zip
       http://download.openandromaps.org/maps/europe/Alps.zip
       http://download.openandromaps.org/pois/europe/Alps.poi.zip
       http://download.openandromaps.org/mapsV4/europe/Alps.zip - V4 multilingual
       orux-map://download.openandromaps.org/mapsV4/europe/Alps.zip
       locus-actions://https/www.openandromaps.org/wp-content/library/xml/Alps.xml
       backcountrynav-action-map://download.openandromaps.org/maps/europe/Alps.zip
       bikecomputer-map://download.openandromaps.org/maps/europe/Alps.zip
       mf-v3-map://download.openandromaps.org/maps/europe/Alps.zip
       mf-v4-map://download.openandromaps.org/mapsV4/europe/Alps.zip
       https://www.openandromaps.org/en/oam-forums/
       https://www.openandromaps.org/wp-content/images/maps/europe/Alps.jpg

       https://www.openandromaps.org/downloads/laender-und-regionen
       https://www.openandromaps.org/downloads/europa
       http://www.openandromaps.org/kartenlegende/elevation-hike-theme

       http://ftp.gwdg.de/pub/misc/openstreetmap/openandromaps//maps/europe/Alps.zip
    */
    boolean shouldOverrideUrlLoading(String url) {
        //return false to have the url loaded

        try {
            Uri uri = Uri.parse(url);

            String scheme = uri.getScheme();

            if (!scheme.equals("http") && !scheme.equals("https")) {
                viewStateObservable.setValue(new DisplayMessageState(R.string.please_use_the_main_download_link));
                return true;
            }

            if (!uri.getHost().endsWith("openandromaps.org")) {
                return true;
            }

            if (uri.getPath().endsWith("elevate-mountain-hike-theme")) {
                viewStateObservable.setValue(new DisplayMessageState(R.string.the_render_theme_is_already_installed));
                return true;
            }

            if (uri.getPath().endsWith("downloads/general-maps")) {
                viewStateObservable.setValue(new DisplayMessageState(R.string.world_maps_are_not_supported));
                return true;
            }

            if (uri.getPath().startsWith("/wp-content/images/maps")) {
                return false;
            }

            if (uri.getPath().startsWith("/downloads/") || uri.getPath().startsWith("/en/downloads/")) {
                return false;
            }

            if (uri.getHost().startsWith("download")) {
                if (uri.getPath().startsWith("/maps/" ) && uri.getPath().endsWith(".zip")) {
                    String[] parts = uri.getPath().split("/");
                    String mapName = parts[parts.length - 1];

                    if (downloadingMapUrls.contains(url)) {
                        viewStateObservable.setValue(new DisplayMessageState(R.string.already_downloading_map, mapName));
                    } else {
                        viewStateObservable.setValue(new DisplayMessageState(R.string.downloading_map, mapName));
                        downloadingMapUrls.add(url);

                        setScheduleMapDownloadState(uri, mapName);
                    }
                    return true;
                } else if (uri.getPath().startsWith("/pois/")) {
                    viewStateObservable.setValue(new DisplayMessageState(R.string.pois_are_not_supported));
                    return true;
                } else {
                    viewStateObservable.setValue(new DisplayMessageState(R.string.please_use_the_main_download_link));
                    return true;
                }
            }
        } catch (NullPointerException e) {
            return true;
        }

        return true;
    }

    void setScheduleMapDownloadState(Uri uri, String mapName) {
        ScheduleMapDownloadState.Builder builder = new ScheduleMapDownloadState.Builder();

        builder.withTitle(new StringProvider(mapName))
                .withDescription(new StringProvider(R.string.notification_summary))
                .withMapUri(uri)
                .withDestinationUri(getDestinationUri(mapName));

        scheduleMapDownloadStateObservable.setValue(builder.build());
    }

    private Uri getDestinationUri(String mapName) {
        StringBuilder sb = new StringBuilder();
        sb.append("file:");

        boolean externalStorageIsEmulated = storageHelper.isStorageEmulated(storageHelper.getExternalFilesDir());
        boolean storageDirectoryIsInternal = preferenceManager.getStorageDir().equals(storageHelper.getFilesDir().getAbsolutePath());

        if (storageDirectoryIsInternal) {
            if (externalStorageIsEmulated) {
                sb.append(storageHelper.getExternalFilesDir());
            } else {
                return null;
            }
        } else if (externalStorageIsEmulated && Build.VERSION.SDK_INT > 26) {
            /* TODO: Check behaviour on real device when using external storage or cache directory
                     On emulator API 27 download fails with ERROR_UNKNOWN
                     On emulator API 28 download remains Queued forever
             */
            sb.append(storageHelper.getExternalFilesDir());
        } else {
            sb.append(preferenceManager.getStorageDir());
        }

        if (sb.charAt(sb.length()-1) != File.separatorChar) {
            sb.append(File.separatorChar);
        }

        sb.append(Environment.DIRECTORY_DOWNLOADS);

        if (sb.charAt(sb.length()-1) != File.separatorChar) {
            sb.append(File.separatorChar);
        }

        sb.append(mapName);

        return Uri.parse(sb.toString());
    }

    public void onMapDownloadScheduled() {
        scheduleMapDownloadStateObservable.setValue(null);
    }
}
