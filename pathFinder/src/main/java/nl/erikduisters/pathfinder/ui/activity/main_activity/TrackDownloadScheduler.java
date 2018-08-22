package nl.erikduisters.pathfinder.ui.activity.main_activity;

import nl.erikduisters.pathfinder.service.gpsies_service.GPSiesTrackImportService;

/**
 * Created by Erik Duisters on 20-08-2018.
 */
interface TrackDownloadScheduler {
    void scheduleTrackDownload(GPSiesTrackImportService.JobInfo jobInfo);
}
