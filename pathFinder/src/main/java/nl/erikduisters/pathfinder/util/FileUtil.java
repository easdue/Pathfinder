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