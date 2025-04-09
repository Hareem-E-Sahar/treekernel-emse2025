package org.vspirit.doveide.project.builder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author codekitten
 */
public class DoveFileParser {

    private String mProjectDir;

    private ArrayList<FileObject> mFileArray;

    public DoveFileParser(String projectDir) {
        mProjectDir = projectDir;
        mFileArray = new ArrayList<FileObject>(50);
    }

    public ArrayList<FileObject> getSrcFiles() {
        listFiles(new File(mProjectDir + "/src"));
        return mFileArray;
    }

    public void clean() throws IOException {
        File file = new File(mProjectDir + "/obj/Debug");
        deleteFiles(file);
        file.mkdirs();
        file = new File(mProjectDir + "/obj/Release");
        deleteFiles(file);
        file.mkdirs();
        file = new File(mProjectDir + "/bin/Debug");
        deleteFiles(file);
        file.mkdirs();
        file = new File(mProjectDir + "/bin/Release");
        deleteFiles(file);
        file.mkdirs();
    }

    private void listFiles(File folder) {
        File[] array = folder.listFiles();
        for (int i = 0; i < array.length; i++) {
            if (array[i].isFile()) {
                if (array[i].isFile()) mFileArray.add(FileUtil.toFileObject(array[i]));
            } else {
                listFiles(array[i]);
            }
        }
    }

    private void deleteFiles(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFiles(files[i]);
            }
        }
        file.delete();
    }
}
