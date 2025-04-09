package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.junit.Assert;

public class FindFiles {

    public static FilenameFilter getFilter(final String extension) {
        return new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                File file = new File(dir, name);
                if (name.endsWith(extension)) {
                    accept = true;
                } else if (file.isDirectory() && name.equals("..") == false && name.equals(".") == false) {
                    accept = true;
                }
                return accept;
            }
        };
    }

    public static void findFiles(List<String> list, File directory, FilenameFilter filter) {
        String[] files = directory.list(filter);
        for (int i = 0; i < files.length; i++) {
            File file = new File(directory, files[i]);
            if (file.isDirectory()) {
                findFiles(list, file, filter);
            } else {
                list.add(file.getPath());
            }
        }
    }

    public static void delete(File file) {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                if (!file.delete()) {
                    Assert.fail("Could not delete file: " + file.getPath());
                }
            } else {
                for (File entry : file.listFiles()) {
                    if (entry.isDirectory()) {
                        delete(entry);
                    } else {
                        if (!entry.delete()) {
                            Assert.fail("Could not delete file: " + entry.getPath());
                        }
                    }
                }
                if (!file.delete()) {
                    Assert.fail("Could not delete directory: " + file.getPath());
                }
            }
        }
    }

    public static void delete(File[] files) {
        for (File file : files) {
            delete(file);
        }
    }
}
