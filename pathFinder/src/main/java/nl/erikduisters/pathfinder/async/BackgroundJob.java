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

package nl.erikduisters.pathfinder.async;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public abstract class BackgroundJob implements Cancellable, Runnable {
    protected volatile boolean canceled;
    protected BackgroundJobHandler backgroundJobHandler;

    void setBackgroundJobHandler(BackgroundJobHandler handler) {
        this.backgroundJobHandler = handler;
    }

    @Override
    public void cancel() {
        canceled = true;
        backgroundJobHandler.cancelJob(this);
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    abstract void onError(Throwable error);

    void finish() { backgroundJobHandler.onFinished(this); }
}
