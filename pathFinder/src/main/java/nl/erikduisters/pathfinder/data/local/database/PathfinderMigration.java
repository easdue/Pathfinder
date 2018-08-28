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
