package de.miethxml.toolkit.io;

/**
 * @author simon
 */
public class FileModelByNameSorter implements FileModelSorter {

    public void sort(FileModel[] list) {
        FileModel pivot;
        int sortLength = list.length;
        boolean next = true;
        int lastDirectory = list.length - 1;
        for (int i = 0; i < sortLength; i++) {
            if (list[i].isFile()) {
                int x = list.length - 1;
                while (list[x].isFile() && (x > i)) {
                    x--;
                }
                if ((x == i) && list[i].isFile()) {
                    quickSortByName(list, i, (list.length - 1));
                    return;
                } else {
                    pivot = list[x];
                    list[x] = list[i];
                    list[i] = pivot;
                    lastDirectory = x;
                }
            }
            if (!list[i].isFile()) {
                int y = 0;
                next = true;
                while ((y < i) && !list[y].isFile()) {
                    if (list[y].getName().compareTo(list[i].getName()) >= 0) {
                        pivot = list[y];
                        list[y] = list[i];
                        list[i] = pivot;
                    }
                    y++;
                }
            }
        }
        quickSortByName(list, lastDirectory, (list.length - 1));
    }

    private void quickSortByName(FileModel[] list, int left, int right) {
        if (right <= left) {
            return;
        } else if (left == (right - 1)) {
            if (list[left].getName().compareTo(list[right].getName()) > 0) {
                FileModel pivot = list[left];
                list[left] = list[right];
                list[right] = pivot;
                return;
            }
        }
        int center = (right + left) / 2;
        FileModel pivot = list[center];
        int i = left;
        int j = right;
        do {
            while ((list[i].getName().compareTo(pivot.getName()) < 0) && (i < right)) {
                i++;
            }
            while ((list[j].getName().compareTo(pivot.getName()) > 0) && (j > left)) {
                j--;
            }
            if (i <= j) {
                FileModel cache = list[i];
                list[i] = list[j];
                list[j] = cache;
                i++;
                j--;
            }
        } while (i <= j);
        if (j < left) {
            list[center] = list[left];
            list[left] = pivot;
            i++;
            j++;
        }
        quickSortByName(list, left, j);
        quickSortByName(list, i, right);
    }
}
