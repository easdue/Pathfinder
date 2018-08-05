package nl.erikduisters.pathfinder.data.usecase;

import android.database.Cursor;
import android.support.annotation.StringRes;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.data.local.database.PathfinderMigration;

/**
 * Created by Erik Duisters on 16-06-2018.
 */
public class InitDatabase extends ProgressUseCase<Void, InitDatabase.Progress, Void> {
    private PathfinderDatabase database;

    public InitDatabase(Void requestInfo, ProgressUseCase.Callback<InitDatabase.Progress, Void> callback, PathfinderDatabase database) {
        super(requestInfo, callback);

        this.database = database;
    }

    @Override
    public void execute(Cancellable cancellable) {
        PathfinderMigration.setProgressCallback(callback);

        try {
            Cursor c = database.query("SELECT _id FROM track LIMIT 1", null);
            c.close();

            callback.onResult(null);
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    public static class Progress {
        public final int progress;
        public final @StringRes int message;

        public Progress(int progress, @StringRes int message) {
            this.progress = progress;
            this.message = message;
        }
    }
}
