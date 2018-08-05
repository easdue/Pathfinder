package nl.erikduisters.pathfinder.data.local;

import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import org.oscim.theme.IRenderTheme;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import nl.erikduisters.pathfinder.data.model.map.AssetsThemeFile;
import nl.erikduisters.pathfinder.data.model.map.ExternalThemeFile;
import nl.erikduisters.pathfinder.data.model.map.StorageThemeFile;

/**
 * Created by Erik Duisters on 22-07-2018.
 */

//TODO: Maybe make this a LifeCycleObserver so it can automatically scan for external render themes
@Singleton
public class ExternalRenderThemeManager {
    private final AssetManager assetManager;
    private PreferenceManager preferenceManager;
    @NonNull private List<ExternalThemeFile> themeFiles;

    @Inject
    ExternalRenderThemeManager(AssetManager assetManager, PreferenceManager preferenceManager) {
        this.assetManager = assetManager;
        this.preferenceManager = preferenceManager;
        themeFiles = new ArrayList<>();

        getAssetsThemes();
        getStorageThemes();

        Collections.sort(themeFiles);

        setPreferredExternalRenderTheme();
    }

    private void setPreferredExternalRenderTheme() {
        String currentThemeName = preferenceManager.getExternalRenderThemeName();

        if (themeFiles.size() > 0) {
            if (currentThemeName.equals("") || !hasThemeWithName(currentThemeName)) {
                if (hasThemeWithName("Elevate4")) {
                    preferenceManager.setExternalRenderThemeName("Elevate4");
                } else {
                    preferenceManager.setExternalRenderThemeName(themeFiles.get(0).getThemeName());
                }
            }
        } else {
            preferenceManager.setExternalRenderThemeName("");
        }
    }

    private void getAssetsThemes() {
        String renderThemeBaseDir = preferenceManager.getStorageRenderThemeSubDir();

        try {
            String[] renderThemeDirs = assetManager.list(renderThemeBaseDir);

            for (String renderThemeDir : renderThemeDirs) {
                getAssetsThemeFiles(renderThemeBaseDir + File.separator + renderThemeDir);
            }
        } catch (IOException e) {
            //To bad
        }
    }

    private void getAssetsThemeFiles(String renderThemeDir) {
        try {
            String[] dirEntries = assetManager.list(renderThemeDir);

            for (String dirEntry : dirEntries) {
                if (dirEntry.toLowerCase().endsWith(".xml")) {
                    String absThemeFile = renderThemeDir + File.separator + dirEntry;

                    InputStream is = assetManager.open(absThemeFile);
                    is.close();

                    themeFiles.add(new AssetsThemeFile(absThemeFile));
                }
            }
        } catch (IOException e) {
            //To bad
        }
    }

    private void getStorageThemes() {
        File renderThemeDir = preferenceManager.getStorageRenderThemeDir();

        File[] dirEntries = renderThemeDir.listFiles(pathname -> {
            if (pathname.isDirectory()) {
                return true;
            } else
                return pathname.isFile() && pathname.canRead() && pathname.getName().toLowerCase().endsWith(".xml");

        });

        for (File dirEntry : dirEntries) {
            if (dirEntry.isFile()) {
                themeFiles.add(new StorageThemeFile(dirEntry));
            } else {
                getStorageThemeFiles(dirEntry);
            }
        }
    }

    private void getStorageThemeFiles(File renderThemeDir) {
        File[] xmlFiles = renderThemeDir.listFiles(pathname ->
                pathname.isFile() && pathname.canRead() && pathname.getName().toLowerCase().endsWith(".xml"));

        for (File xmlFile : xmlFiles) {
            themeFiles.add(new StorageThemeFile(xmlFile));
        }
    }

    public CharSequence[] getThemeNames() {
        String[] names = new String[themeFiles.size()];

        int index = 0;

        for (ExternalThemeFile themeFile : themeFiles) {
            names[index] = themeFile.getThemeName();
            index++;
        }

        return names;
    }

    public @NonNull ExternalThemeFile getThemeWithName(String themeName) throws IRenderTheme.ThemeException {
        for (ExternalThemeFile themeFile : themeFiles) {
            if (themeFile.getThemeName().equals(themeName)) {
                return themeFile;
            }
        }

        throw new IRenderTheme.ThemeException("There is no theme with name: " + themeName);
    }

    private boolean hasThemeWithName(String themeName) {
        for (ExternalThemeFile themeFile : themeFiles) {
            if (themeFile.getThemeName().equals(themeName)) {
                return true;
            }
        }

        return false;
    }
}
