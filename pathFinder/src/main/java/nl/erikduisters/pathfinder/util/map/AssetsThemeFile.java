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

import org.oscim.backend.AssetAdapter;
import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeUtils;
import org.oscim.theme.XmlRenderThemeMenuCallback;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Erik Duisters on 22-07-2018.
 */
public class AssetsThemeFile implements ExternalThemeFile {
    private final String themeName;
    private final String absFileName;
    private final String relativePathPrefix;
    private XmlRenderThemeMenuCallback menuCallback;

    public AssetsThemeFile(String themeFile) {
        this(themeFile, null);
    }

    public AssetsThemeFile(String absFileName, XmlRenderThemeMenuCallback menuCallback) {
        File themeFile = new File(absFileName);

        String name = themeFile.getName();
        themeName = name.substring(0, name.toLowerCase().indexOf(".xml"));
        this.absFileName = absFileName;
        relativePathPrefix = themeFile.getParent();
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
        return relativePathPrefix;
    }

    @Override
    public InputStream getRenderThemeAsStream() throws IRenderTheme.ThemeException {
        InputStream is = AssetAdapter.readFileAsStream(absFileName);

        if (is == null) throw new IRenderTheme.ThemeException(absFileName + " cannot be read from assets");

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
