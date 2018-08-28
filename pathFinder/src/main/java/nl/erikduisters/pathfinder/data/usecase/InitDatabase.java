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
