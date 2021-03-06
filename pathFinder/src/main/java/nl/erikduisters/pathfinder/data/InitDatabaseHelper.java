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

package nl.erikduisters.pathfinder.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.async.BackgroundJobHandler;
import nl.erikduisters.pathfinder.data.local.database.PathfinderDatabase;
import nl.erikduisters.pathfinder.data.usecase.InitDatabase;

/**
 * Created by Erik Duisters on 16-06-2018.
 */
@Singleton
public class InitDatabaseHelper implements InitDatabase.Callback<InitDatabase.Progress, Void> {
    public interface InitDatabaseListener {
        void onDatabaseInitializationProgress(@NonNull InitDatabase.Progress progress);
        void onDatabaseInitializationComplete();
        void onDatabaseInitializationError(@NonNull Throwable error);
    }

    private boolean databaseInitialized;
    private final BackgroundJobHandler backgroundJobHandler;
    private final PathfinderDatabase database;
    private InitDatabaseListener listener;

    @Inject
    public InitDatabaseHelper(BackgroundJobHandler backgroundJobHandler, PathfinderDatabase database) {
        this.backgroundJobHandler = backgroundJobHandler;
        this.database = database;
        this.databaseInitialized = false;
    }

    public void initDatabase(InitDatabaseListener listener) {
        if (databaseInitialized) {
            listener.onDatabaseInitializationComplete();
            return;
        }

        if (this.listener != null) {
            throw new RuntimeException("The database can only be initialized once");
        }

        this.listener = listener;

        InitDatabase usecase = new InitDatabase(null,this, database);
        backgroundJobHandler.runJob(usecase.getUseCaseJob());
    }

    @Override
    public void onResult(@Nullable Void result) {
        databaseInitialized = true;
        listener.onDatabaseInitializationComplete();
        listener = null;
    }

    @Override
    public void onProgress(@NonNull InitDatabase.Progress progress) {
        listener.onDatabaseInitializationProgress(progress);
    }

    @Override
    public void onError(@NonNull Throwable error) {
        listener.onDatabaseInitializationError(error);
        listener = null;
    }
}
