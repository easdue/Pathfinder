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

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.data.usecase.UseCase;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public class UseCaseJob<R, U extends UseCase<?, R>> extends BackgroundJob implements UseCase.Callback<R> {
    private final U useCase;
    private UseCase.Callback<R> callback;

    public UseCaseJob(@NonNull U usecase) {
        Timber.d("New %s created %s", usecase.getClass().getSimpleName(), this);

        this.useCase = usecase;
        this.callback = usecase.getCallback();

        useCase.setCallBack(this);
    }

    @Override
    public void run() {
        Timber.d("%s@%s.run()", useCase.getClass().getSimpleName(), this);
        useCase.execute(this);

        if (canceled) {
            finish();
        }
    }

    public U getUseCase() {
        return useCase;
    }

    @Override
    public void onResult(@NonNull final R result) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onResult(result);
            }
        });
        finish();
    }

    @Override
    public void onError(@NonNull final Throwable error) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onError(error);
            }
        });
        finish();
    }

    @Override
    void finish() {
        super.finish();

        Timber.d("%s@%s Finished", useCase.getClass().getSimpleName(), this);
    }

    @Override
    public void cancel() {
        super.cancel();

        Timber.d("%s@%s Cancelled", getClass().getSimpleName(), this);
    }
}
