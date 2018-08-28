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

package nl.erikduisters.pathfinder.service.gpsies_service;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import okhttp3.OkHttpClient;

/**
 * Created by Erik Duisters on 12-08-2018.
 */
public abstract class Job<T extends Job.JobInfo> {
    @NonNull protected final T jobInfo;

    Job(@NonNull T jobInfo) {
        this.jobInfo = jobInfo;
    }

    abstract void execute(OkHttpClient okHttpClient, Callback callback);

    interface Callback {
        void onResult(Result result);
    }

    interface JobInfo extends Parcelable {
    }
}

