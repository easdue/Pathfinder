package nl.erikduisters.pathfinder.util.map;

import org.oscim.theme.ThemeFile;

/**
 * Created by Erik Duisters on 22-07-2018.
 */
public interface ExternalThemeFile extends ThemeFile, Comparable<ExternalThemeFile> {
    String getThemeName();
}
