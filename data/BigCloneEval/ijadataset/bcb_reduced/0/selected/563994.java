package com.onehao.io;

import java.io.File;

public class MyFileTest9 {

    public void deleteSubFile(File file) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            for (File f : fileList) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteSubFile(f);
                    f.delete();
                }
            }
        }
    }

    public static void main(String[] args) {
    }
}
