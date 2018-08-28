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

import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.ThemeLoader;

import nl.erikduisters.pathfinder.async.Cancellable;

/**
 * Created by Erik Duisters on 12-07-2018.
 */
public class LoadRenderTheme extends UseCase<ThemeFile, IRenderTheme> {
    public LoadRenderTheme(@NonNull ThemeFile requestInfo, @NonNull Callback<IRenderTheme> callback) {
        super(requestInfo, callback);
    }

    @Override
    public void execute(Cancellable cancellable) {
        try {
            IRenderTheme renderTheme = ThemeLoader.load(requestInfo);
            callback.onResult(renderTheme);
        } catch(IRenderTheme.ThemeException e) {
            callback.onError(e);
        }
    }
}
