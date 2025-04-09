package org.proclos.etlcore;

import java.io.File;

public class DeleteClass {

    private void delete(File file) {
        if (file.isDirectory()) {
            File[] liste = file.listFiles();
            System.out.println("Dir: " + file.getName());
            try {
                for (int i = 0; i < liste.length; i++) {
                    delete(liste[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File: " + file.getName());
            if (file.getName().endsWith(".class")) file.delete();
        }
    }

    DeleteClass(String dirname) {
        delete(new File(dirname));
    }

    public static void main(String[] args) {
        new DeleteClass("/home/chris/workspace/jpalo/jars");
    }
}
