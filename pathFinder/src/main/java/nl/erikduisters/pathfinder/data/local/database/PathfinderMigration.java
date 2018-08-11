package nl.erikduisters.pathfinder.data.local.database;

import android.arch.persistence.room.migration.Migration;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.data.usecase.InitDatabase;
import nl.erikduisters.pathfinder.data.usecase.ProgressUseCase;

/**
 * Created by Erik Duisters on 16-06-2018.
 */
public abstract class PathfinderMigration extends Migration {
    private static ProgressUseCase.Callback<InitDatabase.Progress, Void> progressCallback;

    public PathfinderMigration(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    public static void setProgressCallback(ProgressUseCase.Callback<InitDatabase.Progress, Void> progressCallback) {
        PathfinderMigration.progressCallback = progressCallback;
    }

    protected void reportProgress(float progress, @StringRes int message) {
        if (progressCallback != null) {
            progressCallback.onProgress(new InitDatabase.Progress(Math.round(progress), message));
        }
    }
}
