package nl.erikduisters.pathfinder.ui.activity.main_activity;

import nl.erikduisters.pathfinder.service.track_import.ImportJob;

/**
 * Created by Erik Duisters on 20-08-2018.
 */
interface TrackImportScheduler {
    void scheduleTrackDownload(ImportJob.JobInfo jobInfo);
}
