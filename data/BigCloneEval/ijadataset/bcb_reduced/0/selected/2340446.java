package net.sf.webwarp.util.file;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.sf.webwarp.util.filemonitor.FileMonitor;

/**
 * Test/Demo thread for the FileMonitor. Used to execute series of creating, deleting and modifications on two test folders.
 *
 */
public class TwoFolderTestThread extends Thread {

    private int executionCount = 0;

    private int stopCount;

    private FileMonitor fileMonitor;

    private long interval;

    private File rootFolder1;

    private File rootFolder2;

    private File subFolder1 = null;

    private File subFolder2 = null;

    private File subFolder3 = null;

    private File file1 = null;

    private File file2 = null;

    private File file3 = null;

    private File file4 = null;

    private File file5 = null;

    private File file6 = null;

    private String subFolder1Name = "subFolder1";

    private String subFolder2Name = "subFolder2";

    private String subFolder3Name = "subFolder3";

    private String file1Name = "testFile1";

    private String file2Name = "testFile2";

    private String file3Name = "testFile3";

    private String file4Name = "testFile4";

    private String file5Name = "testFile5.txt";

    private String file6Name = "testFile6";

    /**
     * Creates a new TwoFolderTestThread.
     * 
     * @param folder1,
     *            the first folder used for testing/ demonstration
     * @param folder2,
     *            the second folder used for testing/ demonstration
     * @param fileMonitor,
     *            the file monitor used. (Only needed to shut it down at the end of the demo)
     * @param interval,
     *            the time between the different operations of this thread.
     */
    public TwoFolderTestThread(File folder1, File folder2, FileMonitor fileMonitor, long interval) {
        this.rootFolder1 = folder1;
        this.rootFolder2 = folder2;
        this.fileMonitor = fileMonitor;
        this.interval = interval;
    }

    public void run() {
        while (executionCount <= stopCount) {
            executionCount = modifiedFiles(executionCount);
            try {
                sleep(interval);
            } catch (InterruptedException e) {
            }
        }
    }

    private int modifiedFiles(int executionCount) {
        System.out.println(" ");
        if (executionCount == 1) {
            rootFolder1.mkdir();
            System.out.println("TestThread: " + executionCount + " -> created folder: " + rootFolder1.getAbsolutePath() + " Expecting to find him as NEW");
        }
        if (executionCount == 2) {
            subFolder1 = new File(rootFolder1, subFolder1Name);
            subFolder1.mkdir();
            file1 = new File(rootFolder1, file1Name);
            writeTextFile(file1, "contend1");
            System.out.println("TestThread: " + executionCount + " -> created folder: " + subFolder1.getAbsolutePath() + " and file: " + file1.getAbsolutePath() + " Expecting to find them as NEW");
        }
        if (executionCount == 3) {
            subFolder1.delete();
            writeTextFile(file1, "other contend");
            file2 = new File(rootFolder1, file2Name);
            writeTextFile(file2, "contend2");
            System.out.println("TestThread: " + executionCount + " -> folder: " + subFolder1.getAbsolutePath() + " deleted," + " file: " + file1.getAbsolutePath() + " modified," + " file: " + file2.getAbsolutePath() + " created." + " Expecting to find them as such");
        }
        if (executionCount == 4) {
            rootFolder2.mkdir();
            file5 = new File(rootFolder2, file5Name);
            writeTextFile(file5, "contend5");
            System.out.println("TestThread: " + executionCount + " -> created folder: " + rootFolder2.getAbsolutePath() + " and file:" + file5.getAbsolutePath() + " Expecting to find them as NEW");
        }
        if (executionCount == 5) {
            subFolder1 = new File(rootFolder1, subFolder1Name);
            subFolder1.mkdir();
            subFolder2 = new File(subFolder1, subFolder2Name);
            subFolder2.mkdir();
            subFolder3 = new File(subFolder2, subFolder3Name);
            subFolder3.mkdir();
            file3 = new File(subFolder3, file3Name);
            writeTextFile(file3, "contend3");
            System.out.println("TestThread: " + executionCount + " -> created folders: " + subFolder3.getAbsolutePath() + " and file:" + file3.getAbsolutePath() + " Expecting to find them as NEW");
        }
        if (executionCount == 6) {
            writeTextFile(file3, "other contend");
            file4 = new File(subFolder3, file4Name);
            writeTextFile(file4, "contend4");
            System.out.println("TestThread: " + executionCount + " ->" + " file: " + file3.getAbsolutePath() + " modified" + " file: " + file4.getAbsolutePath() + " created" + " Expecting to find them as such");
        }
        if (executionCount == 7) {
            file6 = new File(subFolder2, file6Name);
            writeTextFile(file6, "contend6");
            System.out.println("TestThread: " + executionCount + " ->" + " file: " + file6.getAbsolutePath() + " created" + " Expecting to find it as such");
        }
        if (executionCount == 8) {
            writeTextFile(file6, "other contend6");
            System.out.println("TestThread: " + executionCount + " ->" + " file: " + file6.getAbsolutePath() + " modified" + " Expecting to find it as such");
        }
        if (executionCount == 9) {
            file6.delete();
            System.out.println("TestThread: " + executionCount + " ->" + " file: " + file6.getAbsolutePath() + " deleted" + " Expecting to find it as such");
        }
        if (executionCount == 10) {
            deletFileRecursive(rootFolder1);
            deletFileRecursive(rootFolder2);
            System.out.println("TestThread: " + executionCount + " -> deleted all root folders " + " Expecting to find them as DELETED");
        }
        stopCount = 11;
        if (executionCount == stopCount) {
            System.out.println("TestThread: " + executionCount + " -> stopping FileMonitor");
            fileMonitor.stop();
        }
        System.out.println(" ");
        executionCount++;
        return executionCount;
    }

    private void writeTextFile(File file, String contend) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(contend);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ioe) {
            System.out.println("TestThread: Exception in altering test files / folder executionCount: " + executionCount);
            ioe.printStackTrace();
        }
    }

    private void deletFileRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (int i = 0, max = children.length; i < max; i++) {
                deletFileRecursive(children[i]);
            }
        }
        file.delete();
    }
}
