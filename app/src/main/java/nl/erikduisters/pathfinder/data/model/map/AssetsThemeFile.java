package nl.erikduisters.pathfinder.data.model.map;

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
