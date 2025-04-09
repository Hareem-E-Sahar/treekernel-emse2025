package com.goodcodeisbeautiful.archtea.io;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.goodcodeisbeautiful.archtea.config.DummyFolderConfig;
import com.goodcodeisbeautiful.test.util.CommonTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hata
 *                 2005/06/10 2006/06/12 2007/01/04 2007/01/19
 * Small: Time:    360 ms     440 ms     431 ms     401 ms
 * Small: Inc mem: 58KB       57KB       57KB       57KB
 * Huge:  Time:    34969 ms   14250 ms   13410 ms   13209 ms
 * Huge:  Inc mem: 2850KB     1698KB     1370KB     1223KB
 */
public class PerformanceTestCase extends CommonTestCase {

    private static final long INTERVAL_TIME = 10000;

    private static final int FLAT_FILE_ACCESS_COUNT = 1000;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(PerformanceTestCase.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        cleanWorkFiles();
    }

    /**
     * Constructor for PerformanceTestCase.
     * @param arg0
     */
    public PerformanceTestCase(String arg0) {
        super(arg0);
    }

    public void testPerformance() throws Exception {
        setupZipFile("small.zip", 2000, 20, 64);
        setupZipFile("mid.zip", 4000, 20, 256);
        File f = new File(getWorkDir());
        EntryManager mgr = new FileSystemEntryManager(new DummyFolderConfig(f.getPath()));
        assertNotNull(mgr);
        assertNotNull(mgr.getRoot());
        System.gc();
        long beginFreeMem = Runtime.getRuntime().freeMemory();
        Thread.sleep(1000);
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) accessToSubEntry(mgr.getRoot());
        long endTime = System.currentTimeMillis();
        Thread.sleep(1000);
        System.gc();
        long endFreeMem = Runtime.getRuntime().freeMemory();
        System.out.println("Small: Time:    " + (endTime - beginTime) + " ms");
        System.out.println("Small: Inc mem: " + (int) ((beginFreeMem - endFreeMem) / 1000) + "KB");
    }

    public void testPerformanceHuge() throws Exception {
        setupZipFile("large.zip", 8000, 40, 512);
        setupZipFile("huge.zip", 8000, 40, 4096);
        copyTo("huge.zip", "huge-foo.zip");
        copyTo("huge.zip", "huge-bar.zip");
        File f = new File(getWorkDir());
        EntryManager mgr = new FileSystemEntryManager(new DummyFolderConfig(f.getPath()));
        assertNotNull(mgr);
        assertNotNull(mgr.getRoot());
        System.gc();
        long beginFreeMem = Runtime.getRuntime().freeMemory();
        Thread.sleep(1000);
        long beginTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            System.out.print(".");
            System.out.flush();
            accessToSubEntry(mgr.getRoot());
        }
        System.out.println("");
        long endTime = System.currentTimeMillis();
        Thread.sleep(1000);
        System.gc();
        long endFreeMem = Runtime.getRuntime().freeMemory();
        System.out.println("Huge: Time:    " + (endTime - beginTime) + " ms");
        System.out.println("Huge: Inc mem: " + (int) ((endFreeMem - beginFreeMem) / 1000) + "KB");
    }

    public void testFlatFileAcces100() throws Exception {
        setupManyFiles(getWorkDir(), 1, "file", 100);
        System.gc();
        Thread.sleep(INTERVAL_TIME);
        repeatRandomAccess(getWorkDir(), FLAT_FILE_ACCESS_COUNT, "file", 100);
    }

    public void testFlatFileAccess1000() throws Exception {
        setupManyFiles(getWorkDir(), 1, "file", 1000);
        System.gc();
        Thread.sleep(INTERVAL_TIME);
        repeatRandomAccess(getWorkDir(), FLAT_FILE_ACCESS_COUNT, "file", 1000);
    }

    public void testFlatFileAccess10000() throws Exception {
        setupManyFiles(getWorkDir(), 1, "file", 10000);
        System.gc();
        Thread.sleep(INTERVAL_TIME);
        repeatRandomAccess(getWorkDir(), FLAT_FILE_ACCESS_COUNT, "file", 10000);
    }

    private void repeatRandomAccess(String rootDir, int maxLoop, String baseName, int fileCount) throws Exception {
        Random rand = new Random(Double.doubleToLongBits(Math.PI));
        File f = new File(rootDir);
        EntryManager mgr = null;
        try {
            mgr = new FileSystemEntryManager(new DummyFolderConfig(f.getPath()));
            Entry entry = mgr.getRoot();
            for (int i = 0; i < maxLoop; i++) entry.getEntry(baseName + rand.nextInt(fileCount));
        } finally {
            if (mgr != null) {
                mgr.close();
            }
        }
    }

    static int debugCount = 0;

    private void accessToSubEntry(Entry parent) throws Exception {
        if (parent == null) return;
        List subDir = parent.getEntries();
        if (subDir == null) return;
        Iterator it = subDir.iterator();
        while (it.hasNext()) {
            accessToSubEntry((Entry) it.next());
            if ((debugCount % 1000) == 0) System.out.print(".");
            if ((debugCount++ % 10000) == 0) System.out.println(".");
        }
    }

    /**
     * Create many files under path directory.
     * The filename start from zero .
     * @param path is a parent path to contain many files.
     * If there is not any "path" directory, then create it.
     * @param baseName is a new file's prefix part. For example,
     * the new file is "baseName"0, "baseName"1 ...
     * @param count is a number of files to be created.
     * 
     */
    protected void setupManyFiles(String path, int entrySize, String baseName, int count) throws Exception {
        Random rand = new Random(Double.doubleToLongBits(Math.PI));
        File f = new File(path);
        if (!f.exists()) f.mkdirs();
        if (!f.exists()) throw new Exception("Cannot create directory path=" + path);
        FileOutputStream fout = null;
        for (int i = 0; i < count; i++) {
            byte[] buff = new byte[entrySize];
            fillData(rand, buff);
            try {
                fout = new FileOutputStream(path + File.separator + baseName + i);
                fout.write(buff);
                fout.flush();
            } finally {
                if (fout != null) {
                    try {
                        fout.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ;
                    fout = null;
                }
            }
        }
    }

    protected void setupZipFile(String path, int entrySize, int filePerDir, int count) throws Exception {
        Random rand = new Random(Double.doubleToLongBits(Math.PI));
        if (filePerDir < 1) filePerDir = 10;
        String filePath = getWorkDir() + File.separator + path;
        File f = new File(filePath);
        if (!f.getParentFile().exists()) f.getParentFile().mkdirs();
        ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(filePath));
        int num = 0;
        byte[] dataBuff = new byte[entrySize];
        try {
            for (int i = 0; i < count; i++) {
                StringBuffer entryPath = new StringBuffer();
                num = i;
                do {
                    int a = num % filePerDir;
                    num = (int) (num / filePerDir);
                    if (entryPath.length() > 0) entryPath.append("/");
                    if (0 < num) entryPath.append("entry-dir-" + a); else entryPath.append("entry" + i);
                } while (0 < num);
                zo.putNextEntry(new ZipEntry(new String(entryPath)));
                fillData(rand, dataBuff);
                zo.write(dataBuff);
                zo.closeEntry();
            }
        } finally {
            if (zo != null) zo.close();
        }
    }

    /**
     * This function fill some ascii text into a buff.
     * The filled text is only to use fill the file. So, the text
     * doesn't have any meaning as text.
     * @param buff is a buffer to be filled.
     */
    static void fillData(Random rand, byte[] buff) {
        for (int i = 0; i < buff.length; i++) {
            buff[i] = (byte) (((int) (rand.nextDouble() * (0x7e - 0x21))) + 0x21);
            if (rand.nextInt(7) == 0) {
                buff[i] = 0x20;
            }
        }
    }
}
