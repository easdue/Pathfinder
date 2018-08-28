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

package nl.erikduisters.pathfinder.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Erik Duisters on 04-06-2018.
 */
public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public static boolean createFile(File f) {
        if (f.exists()) {
            return true;
        } else {
            try {
                return f.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
    }

    public static boolean createDirectory(File file) {
        return file.isDirectory() || file.mkdirs();
    }

    public static class RegexFileFilter implements java.io.FileFilter {
        final Pattern pattern;

        public RegexFileFilter(String regex) {
            pattern = Pattern.compile(regex);
        }

        @Override
        public boolean accept(File f) {
            return pattern.matcher(f.getName()).find();
        }

    }

    public static List<File> getFilesByFilter(String directory, RegexFileFilter filter) {
        List<File> files = new ArrayList<>();

        File dir = new File(directory);

        File[] matchingFiles = dir.listFiles(filter);

        if (matchingFiles != null) {
            Collections.addAll(files, matchingFiles);
        }

        return files;
    }

    public static List<File> getFilesByExtension(File directory, boolean recursive, String extension) {
        List<File> files = new ArrayList<>();

        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                if (!recursive) {
                    continue;
                } else {
                    files.addAll(getFilesByExtension(f, recursive, extension));
                }
            }

            if (f.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                files.add(f);
            }
        }

        return files;
    }

    public static String removeEndSeparator(String path) {
        if (path.endsWith(File.separator)) {
            return path.substring(0, path.length()-1);
        }

        return path;
    }
}