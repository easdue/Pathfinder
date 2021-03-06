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

package nl.erikduisters.pathfinder.data.local;

import android.support.annotation.NonNull;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.async.BackgroundJobHandler;
import nl.erikduisters.pathfinder.async.UseCaseJob;
import nl.erikduisters.pathfinder.data.usecase.RenderSvgView;
import nl.erikduisters.pathfinder.data.usecase.UseCase;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import timber.log.Timber;

/*
 * Created by Erik Duisters on 13-07-2018.
 */

@Singleton
public class SvgRenderer {
    private static final String CACHESUBDIR = "svg-cache/";

    private final BackgroundJobHandler backgroundJobHandler;
    private final PreferenceManager preferenceManager;
    private final int versionCode;

    @Inject
    public SvgRenderer(BackgroundJobHandler backgroundJobHandler, PreferenceManager preferenceManager, @Named("VersionCode") int versionCode) {
        this.backgroundJobHandler = backgroundJobHandler;
        this.preferenceManager = preferenceManager;
        this.versionCode = versionCode;
    }

    private boolean cancelPotentialWork(SvgView view) {
        UseCaseJob<SvgView, ?> job = view.getRenderJob();

        if (job != null) {
            RenderSvgView.RequestInfo requestInfo = ((RenderSvgView.RequestInfo)job.getUseCase().getRequestInfo());

            if (!job.isCancelled() && requestInfo.svgView.getSvgResourceId() != view.getSvgResourceId()) {
                Timber.d("Cancelling previous RenderJob");

                job.cancel();
            } else {
                return false;
            }
        }

        return true;
    }

    public void render(SvgView view) {
        /* Make sure the cachedir exists. If the user clears the cache android deletes
         * the entire cache directory
         */
        File f = new File(preferenceManager.getCacheDir(), CACHESUBDIR);
        if (!f.exists()) {
            f.mkdirs();
        }

        if (cancelPotentialWork(view)) {
            RenderSvgView.RequestInfo requestInfo = new RenderSvgView.RequestInfo(view, f.getAbsolutePath(), versionCode);
            RenderSvgView useCase = new RenderSvgView(requestInfo, new UseCase.Callback<SvgView>() {
                @Override
                public void onResult(@NonNull SvgView result) {
                    result.onRenderComplete();
                }

                @Override
                public void onError(@NonNull Throwable error) {
                    //TODO: Test if this works if not I need to change to onError callback to include the RequestInfo
                    requestInfo.svgView.onRenderFailed(error);
                }
            });

            UseCaseJob<SvgView, ?> job = useCase.getUseCaseJob();

            view.setRenderJob(job);

            backgroundJobHandler.runJob(job);
        }
    }
}
