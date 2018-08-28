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

package nl.erikduisters.pathfinder.util.map;

import android.support.annotation.NonNull;

import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeUtils;
import org.oscim.theme.XmlRenderThemeMenuCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Erik Duisters on 22-07-2018.
 */
public class StorageThemeFile implements ExternalThemeFile {
    private final String themeName;
    private final File renderTheme;
    private XmlRenderThemeMenuCallback menuCallback;

    public StorageThemeFile(File renderTheme) {
        this(renderTheme, null);
    }

    public StorageThemeFile(File renderTheme, XmlRenderThemeMenuCallback menuCallback) {
        String name = renderTheme.getName();

        themeName = name.substring(0, name.toLowerCase().indexOf(".xml"));
        this.renderTheme = renderTheme;
        this.menuCallback = menuCallback;
    }

    @Override
    public String getThemeName() {
        return themeName;
    }

    @Override
    public XmlRenderThemeMenuCallback getMenuCallback() {
        return menuCallback;
    }

    @Override
    public String getRelativePathPrefix() {
        return renderTheme.getParent();
    }

    @Override
    public InputStream getRenderThemeAsStream() throws IRenderTheme.ThemeException {
        InputStream is;

        try {
            is = new FileInputStream(renderTheme);
        } catch (FileNotFoundException e) {
            throw new IRenderTheme.ThemeException(e.getMessage());
        }

        return is;
    }

    @Override
    public boolean isMapsforgeTheme() {
        return ThemeUtils.isMapsforgeTheme(this);
    }

    @Override
    public void setMenuCallback(XmlRenderThemeMenuCallback menuCallback) {
        this.menuCallback = menuCallback;
    }

    @Override
    public int compareTo(@NonNull ExternalThemeFile o) {
        return themeName.compareTo(o.getThemeName());
    }
}
