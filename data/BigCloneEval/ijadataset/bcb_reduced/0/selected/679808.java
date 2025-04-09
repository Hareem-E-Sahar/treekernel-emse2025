package org.designerator.media.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.core.resources.IResource;

public class SortUtil {

    static void sortFiles(Object[] files) {
        sortBlock(files, 0, files.length - 1, new File[files.length]);
    }

    private static void sortBlock(Object[] files, int start, int end, Object[] mergeTemp) {
        final int length = end - start + 1;
        if (length < 8) {
            for (int i = end; i > start; --i) {
                for (int j = end; j > start; --j) {
                    if (compareFiles(files[j - 1], files[j]) > 0) {
                        final Object temp = files[j];
                        files[j] = files[j - 1];
                        files[j - 1] = temp;
                    }
                }
            }
            return;
        }
        final int mid = (start + end) / 2;
        sortBlock(files, start, mid, mergeTemp);
        sortBlock(files, mid + 1, end, mergeTemp);
        int x = start;
        int y = mid + 1;
        for (int i = 0; i < length; ++i) {
            if ((x > mid) || ((y <= end) && compareFiles(files[x], files[y]) > 0)) {
                mergeTemp[i] = files[y++];
            } else {
                mergeTemp[i] = files[x++];
            }
        }
        for (int i = 0; i < length; ++i) files[i + start] = mergeTemp[i];
    }

    private static int compareFiles(Object a, Object b) {
        if (a instanceof File) {
            int compare = ((File) a).getName().compareToIgnoreCase(((File) b).getName());
            if (compare == 0) compare = ((File) a).getName().compareTo(((File) b).getName());
            return compare;
        } else if (a instanceof IResource) {
            int compare = ((IResource) a).getName().compareToIgnoreCase(((IResource) b).getName());
            if (compare == 0) compare = ((IResource) a).getName().compareTo(((IResource) b).getName());
            return compare;
        }
        return 0;
    }

    public static void jdkSort(Object[] files) {
        Comparator c = new Comparator() {

            public int compare(Object a, Object b) {
                int compare = ((IResource) a).getName().compareToIgnoreCase(((IResource) b).getName());
                if (compare == 0) compare = ((IResource) a).getName().compareTo(((IResource) b).getName());
                return compare;
            }
        };
        Arrays.sort(files, c);
    }
}
