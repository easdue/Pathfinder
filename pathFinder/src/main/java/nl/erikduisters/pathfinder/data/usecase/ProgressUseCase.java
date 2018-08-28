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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.async.ProgressUseCaseJob;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public abstract class ProgressUseCase<I, P, R> {
    I requestInfo;
    Callback<P, R> callback;

    ProgressUseCase(@Nullable I requestInfo, @NonNull Callback<P, R> callback) {
        this.requestInfo = requestInfo;
        this.callback = callback;
    }

    public I getRequestInfo() { return requestInfo; }

    public Callback<P, R> getCallback() {
        return callback;
    }

    public void setCallback(@NonNull Callback<P, R> callback) {
        this.callback = callback;
    }

    public interface Callback<P, R> extends UseCase.Callback<R> {
        void onResult(@Nullable R result);
        void onProgress(@NonNull P progress);
        void onError(@NonNull Throwable error);
    }

    abstract public void execute(Cancellable cancellable);

    public ProgressUseCaseJob<P, R, ?> getUseCaseJob() {
        return new ProgressUseCaseJob<>(this);
    }
}
