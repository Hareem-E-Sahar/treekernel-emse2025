import java.util.*;
import java.io.*;

public class CVSDirDeletion {

    public static int numFile = 0;

    public static int numDir = 0;

    public static void main(String args[]) throws Exception {
        String rootPath = args[0];
        System.out.println("Root Path: " + rootPath);
        File rootDir = new File(rootPath);
        if (!rootDir.exists()) {
            System.out.println("Invalid Path!");
            return;
        }
        System.out.println("File List:");
        listDirectory(rootDir);
        System.out.println();
        System.out.println("Num of File: " + numFile);
        System.out.println("Num of Directory: " + numDir);
    }

    public static void listDirectory(File fileDir) {
        File[] fileList = fileDir.listFiles();
        for (int i = 0; i < fileList.length; ++i) {
            if (fileList[i].isDirectory()) {
                listDirectory(fileList[i].getAbsoluteFile());
                processDirectory(fileList[i]);
                ++numDir;
            }
            if (fileList[i].isFile()) {
                processFile(fileList[i]);
                ++numFile;
            }
        }
    }

    public static void processDirectory(File dir) {
        System.out.println("Directory: " + dir.getName());
        if ("CVS".equals(dir.getName())) {
            deleteDirectory(dir);
            dir.delete();
        }
    }

    public static void processFile(File file) {
    }

    public static void deleteDirectory(File fileDir) {
        File[] fileList = fileDir.listFiles();
        for (int i = 0; i < fileList.length; ++i) {
            if (fileList[i].isDirectory()) {
                deleteDirectory(fileList[i].getAbsoluteFile());
                fileList[i].delete();
            }
            if (fileList[i].isFile()) {
                fileList[i].delete();
            }
        }
    }
}
