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

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.async.UseCaseJob;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public abstract class UseCase<I, R> {
    protected I requestInfo;
    protected Callback<R> callback;

    public UseCase(@NonNull I requestInfo, @NonNull Callback<R> callback) {
        this.requestInfo = requestInfo;
        this.callback = callback;
    }

    public I getRequestInfo() {
        return requestInfo;
    }

    public Callback<R> getCallback() {
        return callback;
    }

    public void setCallBack(@NonNull Callback<R> callback) {
        this.callback = callback;
    }

    public interface Callback<R> {
        void onResult(@NonNull R result);
        void onError(@NonNull Throwable error);
    }

    abstract public void execute(Cancellable cancellable);

    public UseCaseJob<R, ?> getUseCaseJob() {
        return new UseCaseJob<>(this);
    }
}
