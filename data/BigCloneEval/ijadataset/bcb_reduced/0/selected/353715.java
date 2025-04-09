package com.hme.tivo.videostream;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Stack;
import java.util.Comparator;
import com.tivo.hme.bananas.BApplication;
import com.tivo.hme.bananas.BView;

public class InitialScreen extends ScreenTemplate {

    public ScreenList list;

    public VText dirText;

    public VText message;

    public InitialScreen(BApplication app, Stack<String> topDir) {
        super(app);
        debug.print("CONSTRUCTOR: app=" + app + " topDir=" + topDir);
        dirText = new VText(getNormal(), SAFE_TITLE_H + 10, SAFE_TITLE_V + 40, 2, "small");
        dirText.setFlags(RSRC_VALIGN_TOP | RSRC_TEXT_WRAP);
        dirText.setValue("Top Level");
        message = new VText(getNormal(), SAFE_TITLE_H, (int) getHeight() / 2, 1, "");
        message.setFlags(RSRC_VALIGN_TOP | RSRC_HALIGN_CENTER);
        message.setValue("No entries in this directory");
        message.setVisible(false);
        int height = dirText.h + (int) (dirText.h / 2);
        int n = (int) (getHeight() - 2 * SAFE_TITLE_V - 80) / height;
        list = new ScreenList((BView) getNormal(), SAFE_TITLE_H + 10, SAFE_TITLE_V + 80, getWidth() - 2 * SAFE_TITLE_H, n * height, height);
        topDirList(topDir);
    }

    public void topDirList(Stack<String> topDir) {
        debug.print("topDir=" + topDir);
        if (list != null) {
            list.clear();
        }
        ViewScreen[] entries = new ViewScreen[topDir.size()];
        for (int i = 0; i < topDir.size(); i++) {
            entries[i] = new ViewScreen(getBApp(), GLOBAL.topDirName.get(topDir.get(i)));
        }
        if (entries.length > 0) {
            list.add(entries);
            list.setFocus(0, true);
        }
        dirText.setValue("Top Level");
    }

    public void updateFileList(String DIR) {
        debug.print("DIR=" + DIR);
        if (DIR == null) {
            return;
        }
        if (DIR.length() == 0) {
            return;
        }
        dirText.setValue("DIR: " + String.copyValueOf(DIR.toCharArray()));
        if (list != null) {
            list.clear();
        }
        File newDir = new File(DIR);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                for (int i = 0; i < GLOBAL.extList.length; i++) {
                    if (name.toLowerCase().endsWith(GLOBAL.extList[i].toLowerCase())) {
                        return true;
                    }
                }
                File d = new File(dir.getPath() + File.separator + name);
                if (d.isDirectory()) {
                    return true;
                }
                return false;
            }
        };
        String[] files = newDir.list(filter);
        String[] sortedFiles;
        if (GLOBAL.sortOrder.equals("alphanumeric")) {
            sortedFiles = getSortedByName(DIR, files);
        } else {
            sortedFiles = getSortedByDate(DIR, files);
        }
        ViewScreen[] entries = new ViewScreen[sortedFiles.length];
        for (int i = 0; i < sortedFiles.length; i++) {
            entries[i] = new ViewScreen(getBApp(), new GLOBAL().makeEntryName(DIR, sortedFiles[i]));
        }
        if (entries.length > 0) {
            list.add(entries);
            list.setFocus(0, true);
        }
        if (files.length == 0) {
            message.setVisible(true);
        } else {
            message.setVisible(false);
        }
    }

    public boolean focusOn(String entry) {
        debug.print("entry=" + entry);
        if (entry != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).toString().equals(entry)) {
                    list.setFocus(i, true);
                    return true;
                }
            }
        }
        return false;
    }

    public String[] getSortedByName(String DIR, String[] files) {
        debug.print("DIR=" + DIR + " files=" + files);
        File[] fileObjects = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            fileObjects[i] = new File(DIR + File.separator + files[i]);
        }
        Arrays.sort(fileObjects, new Comparator<Object>() {

            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                if (f1.isDirectory() && f2.isFile()) return -1;
                if (f1.isFile() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });
        for (int i = 0; i < fileObjects.length; i++) files[i] = fileObjects[i].getName();
        return files;
    }

    private String[] getSortedByDate(String DIR, String[] files) {
        debug.print("DIR=" + DIR + " files=" + files);
        File[] fileObjects = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            fileObjects[i] = new File(DIR + File.separator + files[i]);
        }
        Arrays.sort(fileObjects, new Comparator<Object>() {

            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                if (f1.isDirectory() && f2.isFile()) return -1;
                if (f1.isFile() && f2.isDirectory()) return 1;
                return (int) (f1.lastModified() - f2.lastModified());
            }
        });
        for (int i = 0; i < fileObjects.length; i++) files[i] = fileObjects[i].getName();
        return files;
    }

    public String toString() {
        return GLOBAL.TITLE;
    }

    public boolean handleKeyPress(int code, long rawcode) {
        return super.handleKeyPress(code, rawcode);
    }
}
