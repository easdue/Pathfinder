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

package nl.erikduisters.pathfinder.ui.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.XpPreferenceFragment;
import android.view.View;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.SwitchPreference;

import org.oscim.theme.IRenderTheme;
import org.oscim.theme.VtmThemes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.local.ExternalRenderThemeManager;
import nl.erikduisters.pathfinder.data.local.PreferenceManager;
import nl.erikduisters.pathfinder.ui.activity.map_download.MapDownloadActivity;
import nl.erikduisters.pathfinder.ui.activity.settings.SettingsActivity;
import nl.erikduisters.pathfinder.ui.preference.ListPreferenceWithButton;
import nl.erikduisters.pathfinder.ui.preference.ListPreferenceWithButtonDialogFragment;
import nl.erikduisters.pathfinder.util.map.ExternalThemeFile;
import nl.erikduisters.pathfinder.util.map.OnlineMap;
import nl.erikduisters.pathfinder.util.map.ScaleBarType;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 18-07-2018.
 */

//TODO: Call FirebaseAnalytics SetCurrentScreen
public class SettingsFragment
        extends XpPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, ListPreferenceWithButtonDialogFragment.ButtonClickListener {
    private static final String TAG_LIST_PREFERENCE_WITH_BUTTON_DIALOG = "ListPreferenceWithButtonDialog";

    @Inject
    PreferenceManager preferenceManager;
    @Inject
    ExternalRenderThemeManager externalRenderThemeManager;

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    public static SettingsFragment newInstance(String rootKey) {
        SettingsFragment settingsFragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, rootKey);
        settingsFragment.setArguments(args);

        return settingsFragment;
    }

    @Override
    public void onAttach(Context context) {
        Timber.d("%s.onAttach()", getClass().getSimpleName());

        AndroidSupportInjection.inject(this);

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager.registerOnSharedPreferenceChangeListener(this);

        if (savedInstanceState != null) {
            ListPreferenceWithButtonDialogFragment fragment =
                    (ListPreferenceWithButtonDialogFragment) getFragmentManager().findFragmentByTag(TAG_LIST_PREFERENCE_WITH_BUTTON_DIALOG);

            if (fragment != null) {
                fragment.setButtonClickListener(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        preferenceManager.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceItemDecoration divider = new PreferenceItemDecoration(getContext())
                .drawBottom(false)
                .drawBetweenItems(false)
                .drawBetweenCategories(true)
                .drawTop(false);

        getListView().addItemDecoration(divider);
        getListView().setFocusable(false);
        setDivider(null);
    }

    @Override
    public void onCreatePreferences2(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        setupPreferenceScreen();
    }

    private void setupPreferenceScreen() {
        PreferenceScreen screen = getPreferenceScreen();

        /*
        if (screen.getTitle().equals(getString(R.string.prefs_settings))) {
        }
        */

        if (screen.getTitle().equals(getString(R.string.prefs_map_preferences))) {
            setupMapPreferences(screen);
        }

    }

    private void setupMapPreferences(PreferenceScreen screen) {
        setupMapSourcePreferences(screen);
        setupRenderThemePreferences(screen);
        setupScaleBarPreference(screen);
    }

    private void setupMapSourcePreferences(PreferenceScreen screen) {
        setupOnlineMapPreference(screen);
        setupOfflineMapPreference(screen);
        setupUseOnlineMapPreference(screen);
    }

    private void setupOnlineMapPreference(PreferenceScreen screen) {
        ListPreference listPreference = (ListPreference) screen.findPreference(getString(R.string.key_map_online_map));

        if (listPreference.getEntries() == null) {
            listPreference.setEntries(getEntries(OnlineMap.class, false));
            listPreference.setEntryValues(getEntries(OnlineMap.class, true));
            listPreference.setValue(preferenceManager.getOnlineMap().name());
        }

        listPreference.setSummary(listPreference.getEntry());
    }

    private void setupOfflineMapPreference(PreferenceScreen screen) {
        ListPreferenceWithButton preference = (ListPreferenceWithButton) screen.findPreference(getString(R.string.key_map_offline_map));

        if (preference.getEntries() == null || preference.getEntries().length == 0) {
            String[] mapNames = getOfflineMapNames();
            preference.setEntries(mapNames);
            preference.setEntryValues(mapNames);

            String currentMap = preferenceManager.getOfflineMap();

            if (arrayContains(mapNames, currentMap)) {
                preference.setValue(currentMap);
            } else if (mapNames.length > 0) {
                preference.setValue(mapNames[0]);
            } else {
                preference.setValue("");
            }
        }

        preference.setSummary(preference.getEntry());
    }

    private void setupUseOnlineMapPreference(PreferenceScreen screen) {
        SwitchPreference switchPreference = (SwitchPreference) screen.findPreference(preferenceManager.KEY_USE_ONLINE_MAP);

        boolean offlineMapConfigured = !preferenceManager.getOfflineMap().isEmpty();
        switchPreference.setChecked(!offlineMapConfigured || preferenceManager.useOnlineMap());
        switchPreference.setEnabled(offlineMapConfigured);
    }

    private String[] getOfflineMapNames() {
        File externalMapDir = preferenceManager.getStorageMapDir();

        File[] mapFiles = externalMapDir.listFiles(pathname -> (pathname.isFile() && pathname.getName().endsWith(".map")));

        String[] mapFileNames = new String[mapFiles.length];

        int index = 0;
        for (File mapFile : mapFiles) {
            mapFileNames[index] = mapFile.getName();
            index++;
        }

        Arrays.sort(mapFileNames);

        return mapFileNames;
    }

    private boolean arrayContains(String[] array, String element) {
        for (String curElement : array) {
            if (curElement.equals(element)) {
                return true;
            }
        }

        return false;
    }

    private void setupRenderThemePreferences(PreferenceScreen screen) {
        boolean enabled = !(preferenceManager.useOnlineMap() && (preferenceManager.getOnlineMap().isBitmapTileSource()));
        setupInternalThemePreferences(screen, enabled);
        setupExternalThemePreferences(screen, enabled);
        setupUseInternalRenderThemePreference(screen, enabled);
    }

    private void setupInternalThemePreferences(PreferenceScreen screen, boolean enabled) {
        ListPreference listPreference = (ListPreference) screen.findPreference(getString(R.string.key_map_internal_render_theme));

        boolean nextzenMapSourceActive = preferenceManager.useOnlineMap() && preferenceManager.getOnlineMap() == OnlineMap.NEXTZEN_MVT;

        if (listPreference.getEntries() == null ||
                (nextzenMapSourceActive && listPreference.getEntries().length > 1) ||
                (!nextzenMapSourceActive && listPreference.getEntries().length == 1)) {

            ArrayList<VtmThemes> excludedThemes = new ArrayList<>();

            if (nextzenMapSourceActive) {
                for (VtmThemes vtmTheme : VtmThemes.values()) {
                    if (vtmTheme != VtmThemes.MAPZEN) {
                        excludedThemes.add(vtmTheme);
                    }
                }
            } else {
                excludedThemes.add(VtmThemes.MAPZEN);
            }

            VtmThemes[] excludedThemesArray = excludedThemes.toArray(new VtmThemes[excludedThemes.size()]);

            listPreference.setEntries(getEntries(VtmThemes.class, false, excludedThemesArray));
            listPreference.setEntryValues(getEntries(VtmThemes.class, true, excludedThemesArray));
            listPreference.setValue(getInternalRenderThemeName(nextzenMapSourceActive));
        }

        listPreference.setSummary(listPreference.getEntry());
        listPreference.setEnabled(enabled);
    }

    private String getInternalRenderThemeName(boolean nextzenMapSourceActive) {
        if (nextzenMapSourceActive) {
            return VtmThemes.MAPZEN.name();
        }

        VtmThemes currentTheme = preferenceManager.getInternalRenderTheme();

        if (!nextzenMapSourceActive && currentTheme == VtmThemes.MAPZEN) {
            return VtmThemes.DEFAULT.name();
        } else {
            return currentTheme.name();
        }
    }

    private void setupExternalThemePreferences(PreferenceScreen screen, boolean enabled) {
        ListPreference listPreference = (ListPreference) screen.findPreference(getString(R.string.key_map_external_render_theme));

        if (listPreference.getEntries() == null) {
            CharSequence[] themeNames = externalRenderThemeManager.getThemeNames();
            listPreference.setEntries(themeNames);
            listPreference.setEntryValues(themeNames);

            try {
                ExternalThemeFile themeFile = externalRenderThemeManager.getThemeWithName(preferenceManager.getExternalRenderThemeName());
                listPreference.setValue(themeFile.getThemeName());
            } catch (IRenderTheme.ThemeException e) {
                listPreference.setValue(themeNames[0].toString());
            }
        }

        listPreference.setSummary(listPreference.getEntry());
        listPreference.setEnabled(enabled);
    }

    private void setupUseInternalRenderThemePreference(PreferenceScreen screen, boolean enabled) {
        SwitchPreference switchPreference = (SwitchPreference) screen.findPreference(preferenceManager.KEY_USE_INTERNAL_RENDER_THEME);

        boolean nextzenMapSourceActive = preferenceManager.useOnlineMap() && preferenceManager.getOnlineMap() == OnlineMap.NEXTZEN_MVT;
        switchPreference.setChecked(nextzenMapSourceActive || preferenceManager.useInternalRenderTheme());
        switchPreference.setEnabled(!nextzenMapSourceActive && enabled);
    }

    private void setupScaleBarPreference(PreferenceScreen screen) {
        ListPreference listPreference = (ListPreference) screen.findPreference(getString(R.string.key_map_scale_bar_type));

        if (listPreference.getEntries() == null) {
            listPreference.setEntries(ScaleBarType.getEntries(getContext()));
            listPreference.setEntryValues(ScaleBarType.getEntryValues());

            listPreference.setValue(preferenceManager.getScaleBarType().name());
        }

        listPreference.setSummary(listPreference.getEntry());
    }

    private <T extends Enum<T>> CharSequence[] getEntries(Class<T> enumType, boolean isEntryValues, T... exclude) {
        T[] enumConstants = enumType.getEnumConstants();
        CharSequence[] names = new CharSequence[enumConstants.length - exclude.length];

        int index = 0;

        StringBuilder sb = new StringBuilder(25);

        for (T enumConstant : enumType.getEnumConstants()) {
            if (isExcluded(enumConstant, exclude)) {
                continue;
            }

            String name = enumConstant.name();

            if (!isEntryValues) {
                name = name.toLowerCase();

                String[] parts = name.split("_");
                sb.setLength(0);

                for (int i = 0, size=parts.length; i < size; i++) {
                    if (i > 0) {
                        sb.append(" ");
                    }

                    if (parts[i].equals("and")) {
                        sb.append(parts[i]);
                    } else {
                        sb.append(parts[i].substring(0, 1).toUpperCase()).append(parts[i].substring(1));
                    }
                }

                name = sb.toString();
            }

            names[index] = name;
            index++;
        }

        return names;
    }

    private <T extends Enum<T>> boolean isExcluded(T item, T... excludes) {
        for (T currentExclude : excludes) {
            if (currentExclude == item) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ListPreferenceWithButton) {
            ListPreferenceWithButtonDialogFragment fragment =
                    ListPreferenceWithButtonDialogFragment.newInstance(preference.getKey(), R.string.download_map, R.string.no_offline_maps_available);

            fragment.setTargetFragment(this, 0);
            fragment.setButtonClickListener(this);
            fragment.show(getFragmentManager(), TAG_LIST_PREFERENCE_WITH_BUTTON_DIALOG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ((SettingsActivity) getActivity()).setToolbarTitle(getPreferenceScreen().getTitle());
        setupPreferenceScreen();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Timber.d("onSharedPreferenceChanged: %s", key);
        setupPreferenceScreen();
    }

    @Override
    public void onButtonClicked(ListPreferenceWithButton preference) {
        Intent intent = new Intent(getContext(), MapDownloadActivity.class);

        startActivity(intent);
    }

    public void onMapAvailable() {
        if (getPreferenceScreen().getTitle().equals(getString(R.string.prefs_map_preferences))) {
            setupOfflineMapPreference(getPreferenceScreen());
        }
    }
}
