package ch.enterag.utils.zip;

import java.io.*;
import java.util.*;
import java.text.*;
import junit.framework.TestCase;

/** Tests zip64.
 @author Hartwig Thomas
 */
public class zip64Tester extends TestCase {

    /** small file size for test file */
    private static final int iSMALL_SIZE = 12345;

    /** user's home */
    private static final String sUSER_DIR = System.getProperty("user.dir");

    /** temp location */
    private static final String sTEMP_LOCATION = sUSER_DIR + "/Temp";

    /** directory for unzipped files */
    private static final String sFOLDER_UNZIP = sTEMP_LOCATION + "/unzip64";

    /** empty folder */
    private static final String sFOLDER_EMPTY = sFOLDER_UNZIP + "/empty";

    /** full folder */
    private static final String sFOLDER_FULL = sFOLDER_UNZIP + "/full";

    /** empty file */
    private static final String sFILE_EMPTY = sFOLDER_FULL + "/empty.txt";

    /** full file */
    private static final String sFILE_FULL = sFOLDER_FULL + "/full.txt";

    /** directory for zipped files */
    private static final String sFOLDER_ZIP = sTEMP_LOCATION + "/zip64";

    /** zip file */
    private static final String sFILE_ZIP = sFOLDER_ZIP + "/test.zip";

    /** directory for test files */
    private static final String sFOLDER_TEST = sTEMP_LOCATION + "/test";

    /** test zip file */
    private static final String sFILE_TEST = sFOLDER_TEST + "/test.zip";

    /** list file */
    private static final String sFILE_LIST = sTEMP_LOCATION + "/list.txt";

    /** global file comment */
    private static final String sZIP_COMMENT = "a global ZIP file comment";

    /** buffer size */
    private static final int iBUFFER_SIZE = 4096;

    /** date format of zip64 */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    /** size format of zip64 */
    private static final NumberFormat SIZE_FORMAT = new DecimalFormat("#,##0", new DecimalFormatSymbols());

    /** constructor
   * @param name of test.
   */
    public zip64Tester(String name) {
        super(name);
    }

    /** delete folder with all test files
   * @param file folder containing test files.
   * @return true, if folder could be deleted.
   */
    private boolean deleteAll(File file) {
        boolean bDeleted = true;
        if (file.isDirectory()) {
            File[] afile = file.listFiles();
            for (int iFile = 0; bDeleted && (iFile < afile.length); iFile++) bDeleted = deleteAll(afile[iFile]);
        }
        if (bDeleted) bDeleted = file.delete();
        return bDeleted;
    }

    /** compare the files in two folders.
   * @param file1 first folder.
   * @param file2 second folder.
   * @return true, if files are identical.
   */
    private boolean compareAll(File file1, File file2) {
        boolean bEqual = false;
        if (file1.isDirectory() && file2.isDirectory()) {
            File[] afile1 = file1.listFiles();
            bEqual = (afile1.length == file2.listFiles().length);
            if (bEqual) {
                String sFolderName2 = file2.getAbsolutePath();
                if (sFolderName2.endsWith(".")) sFolderName2 = sFolderName2.substring(0, sFolderName2.length() - 1);
                if (sFolderName2.endsWith(File.separator)) sFolderName2 = sFolderName2.substring(0, sFolderName2.length() - 1);
                for (int iFile = 0; bEqual && (iFile < afile1.length); iFile++) {
                    File file = new File(sFolderName2 + File.separator + afile1[iFile].getName());
                    if (file.exists()) {
                        if (compareAll(afile1[iFile], file)) {
                            long l1 = 2000 * (long) Math.ceil(afile1[iFile].lastModified() / 2000.0);
                            long l2 = 2000 * (long) Math.ceil(file.lastModified() / 2000.0);
                            if (l1 != l2) {
                                fail("Time stamps of " + file1.getAbsolutePath() + " and " + file2.getAbsolutePath() + " are different!");
                                bEqual = false;
                            }
                        }
                    } else {
                        fail("File  " + file.getAbsolutePath() + " does not exist!");
                        bEqual = false;
                    }
                }
            }
        } else if (file1.isFile() && file2.isFile()) {
            if (file1.getName().equals(file2.getName())) {
                long l1 = 2000 * (long) Math.ceil(file1.lastModified() / 2000.0);
                long l2 = 2000 * (long) Math.ceil(file2.lastModified() / 2000.0);
                if (l1 == l2) {
                    if (file1.length() == file2.length()) {
                        try {
                            FileInputStream fis1 = new FileInputStream(file1);
                            FileInputStream fis2 = new FileInputStream(file2);
                            byte[] buf1 = new byte[iBUFFER_SIZE];
                            byte[] buf2 = new byte[iBUFFER_SIZE];
                            int iRead1 = fis1.read(buf1);
                            int iRead2 = fis2.read(buf2);
                            boolean bEOF = false;
                            while (bEqual && (iRead1 == iRead2) && (!bEOF)) {
                                for (int iByte = 0; bEqual && (iByte < iRead1); iByte++) {
                                    if (buf1[iByte] != buf2[iByte]) {
                                        fail("Contents of " + file1.getAbsolutePath() + " and " + file2.getAbsolutePath() + " are different!");
                                        bEqual = false;
                                    }
                                }
                                if (bEqual) {
                                    if (iRead1 == -1) bEOF = true; else {
                                        iRead1 = fis1.read(buf1);
                                        iRead2 = fis2.read(buf2);
                                    }
                                }
                            }
                            fis1.close();
                            fis2.close();
                        } catch (FileNotFoundException fnfe) {
                            fail(fnfe.getClass().getName() + ": " + fnfe.getMessage());
                        } catch (IOException ie) {
                            fail(ie.getClass().getName() + ": " + ie.getMessage());
                        }
                    } else {
                        fail("Sizes of " + file1.getAbsolutePath() + " and " + file2.getAbsolutePath() + " are different!");
                        bEqual = false;
                    }
                } else {
                    fail("Time stamps of " + file1.getAbsolutePath() + " and " + file2.getAbsolutePath() + " are different!");
                    bEqual = false;
                }
            } else {
                fail("Names of " + file1.getAbsolutePath() + " and " + file2.getAbsolutePath() + " are different!");
                bEqual = false;
            }
        } else fail("directory entries are not both files or both folders!");
        return bEqual;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File fileFolderUnzip = new File(sFOLDER_UNZIP);
        fileFolderUnzip.mkdirs();
        File fileFolderEmpty = new File(sFOLDER_EMPTY);
        fileFolderEmpty.mkdir();
        File fileFolderFull = new File(sFOLDER_FULL);
        fileFolderFull.mkdir();
        File fileFileEmpty = new File(sFILE_EMPTY);
        fileFileEmpty.createNewFile();
        File fileFileFull = new File(sFILE_FULL);
        FileOutputStream fos = new FileOutputStream(fileFileFull);
        int iLength = (int) Math.ceil(iSMALL_SIZE * Math.random());
        byte[] buffer = new byte[iLength];
        for (int i = 0; i < buffer.length; i++) buffer[i] = (byte) (32 + (int) Math.ceil(96 * Math.random()));
        fos.write(buffer);
        fos.close();
        File fileFolderZip = new File(sFOLDER_ZIP);
        fileFolderZip.mkdirs();
        File fileFileZip = new File(sFILE_ZIP);
        String[] asProg = new String[10];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + fileFolderUnzip.getAbsolutePath();
        asProg[5] = "-c";
        asProg[6] = "-q";
        asProg[7] = "-r";
        asProg[8] = "-z=" + sZIP_COMMENT;
        asProg[9] = fileFileZip.getAbsolutePath();
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            InputStream isStdOut = procZip64.getInputStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) System.out.print((char) c);
            isStdOut.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        File fileFolderUnzip = new File(sFOLDER_UNZIP);
        deleteAll(fileFolderUnzip);
        File fileFolderZip = new File(sFOLDER_ZIP);
        deleteAll(fileFolderZip);
    }

    /**
   * Test method for list.
   */
    public void testHelp() {
        System.out.println("testHelp");
        String[] asProg = new String[4];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "-h";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 4) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            if (!sbOutput.toString().startsWith("Usage:")) fail("Invalid output: " + sbOutput.toString());
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for list.
   */
    public void testList() {
        System.out.println("testList");
        String[] asProg = new String[4];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            String sOutput = sbOutput.toString();
            String sPrompt = "ZIP64 archive comment: ";
            int iPosition = sOutput.indexOf(sPrompt);
            sOutput = sOutput.substring(iPosition + sPrompt.length());
            if ((iPosition < 0) || (!sOutput.startsWith(sZIP_COMMENT))) fail("Incorrect ZIP file comment!");
            sOutput = sOutput.substring(sZIP_COMMENT.length());
            File fileEntry = null;
            int iEntries = 4;
            for (int iEntry = 0; iEntry < iEntries; iEntry++) {
                sOutput = sOutput.trim();
                iPosition = sOutput.indexOf(' ');
                String sEntry = sOutput.substring(0, iPosition).trim();
                sOutput = sOutput.substring(iPosition).trim();
                if (sEntry.equals("empty/") || sEntry.equals("full/") || sEntry.equals("full/empty.txt") || sEntry.equals("full/full.txt")) {
                    if (sEntry.equals("empty/")) fileEntry = new File(sFOLDER_EMPTY); else if (sEntry.equals("full/")) fileEntry = new File(sFOLDER_FULL); else if (sEntry.equals("full/empty.txt")) fileEntry = new File(sFILE_EMPTY); else if (sEntry.equals("full/full.txt")) fileEntry = new File(sFILE_FULL);
                    iPosition = sOutput.indexOf(": ");
                    sOutput = sOutput.substring(iPosition + 2);
                    @SuppressWarnings("null") String sFileDate = DATE_FORMAT.format(new Date(2000 * (long) Math.ceil(fileEntry.lastModified() / 2000.0)));
                    if (!sOutput.startsWith(sFileDate)) fail("Date of " + sEntry + " incorrect!");
                    iPosition = sOutput.indexOf(": ");
                    sOutput = sOutput.substring(iPosition + 2);
                    String sSize = "0";
                    if (sEntry.equals("full/full.txt")) sSize = SIZE_FORMAT.format(fileEntry.length());
                    if (!sOutput.startsWith(sSize)) fail("Size of " + sEntry + " incorrect!");
                    iPosition = sOutput.indexOf('\n');
                    sOutput = sOutput.substring(iPosition).trim();
                    if (sEntry.equals("full/full.txt") || sEntry.equals("full/empty.txt")) {
                        if (sOutput.startsWith("Compressed size")) {
                            iPosition = sOutput.indexOf(": ");
                            sOutput = sOutput.substring(iPosition + 2);
                            iPosition = sOutput.indexOf('\n');
                            try {
                                SIZE_FORMAT.parse(sOutput.substring(0, iPosition).trim());
                            } catch (ParseException pe) {
                                fail("Compressed size of " + sEntry + " could not be parsed!");
                            }
                            sOutput = sOutput.substring(iPosition);
                        }
                    }
                    iPosition = sOutput.indexOf(": ");
                    sOutput = sOutput.substring(iPosition + 2);
                    if (!sOutput.startsWith("0x")) fail("Invalid Crc!");
                    if (!sEntry.equals("full/full.txt")) {
                        if (!sOutput.startsWith("0x00000000")) fail("Invalid zero Crc!");
                    }
                    iPosition = sOutput.indexOf('\n');
                    sOutput = sOutput.substring(iPosition);
                }
            }
            sOutput = sOutput.trim();
            String sSize = SIZE_FORMAT.format(iEntries);
            if (!sOutput.startsWith(sSize)) fail("Invalid number of entries!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for basic injection case.
   */
    public void testInject() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testInject");
        String[] asProg = new String[6];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_TEST;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for basic injection case without replacement.
   */
    public void testInjectNoReplace() {
        System.out.println("testInjectNoReplace");
        String[] asProg = new String[6];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() <= 0) fail("Error must list unreplaced entries!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for basic injection case with replacement.
   */
    public void testInjectReplace() {
        System.out.println("testInjectReplace");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = "-r";
        asProg[6] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for injection of a single empty file.
   */
    public void testInjectFile() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testInjectFile");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_TEST;
        asProg[6] = "full/empty.txt";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for injection of a single full folder.
   */
    public void testInjectFolder() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testInjectFolder");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_TEST;
        asProg[6] = "full/*";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            if (sbOutput.indexOf("3 matching file entries injected") < 0) fail("Not 3 matching file entries injected!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for injection of a folder and a file.
   */
    public void testInjectSet() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testInjectSet");
        String[] asProg = new String[8];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_TEST;
        asProg[6] = "empty/*";
        asProg[7] = "full/full.txt";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            if (sbOutput.indexOf("2 matching file entries injected") < 0) fail("Not 2 matching file entries injected!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for injection of two files specified in a file list.
   */
    public void testInjectList() {
        File fileList = new File(sFILE_LIST);
        try {
            FileWriter fw = new FileWriter(fileList);
            fw.write("full/empty.txt\n");
            fw.write("full/full.txt\n");
            fw.close();
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testInjectSet");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "n";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_TEST;
        asProg[6] = "@" + sFILE_LIST;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            if (sbOutput.indexOf("2 matching file entries injected") < 0) fail("Not 2 matching file entries injected!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
        fileList.delete();
    }

    /**
   * Test method for basic extraction case.
   */
    public void testExtract() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testExtract");
        String[] asProg = new String[6];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_TEST;
        asProg[5] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            File fileFolderUnzip = new File(sFOLDER_UNZIP);
            if (!compareAll(fileFolderUnzip, fileFolderTest)) fail("Folder " + fileFolderUnzip.getAbsolutePath() + " is not equal to folder " + fileFolderTest.getAbsolutePath() + "!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for basic extraction case without replacement.
   */
    public void testExtractNoReplace() {
        System.out.println("testExtractNoReplace");
        String[] asProg = new String[6];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() <= 0) fail("Error must list unreplaced files!");
            if (sbOutput.indexOf("0 matching file entries extracted") < 0) fail("Not 0 matching file entries extracted!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for basic extraction case with replacement.
   */
    public void testExtractReplace() {
        System.out.println("testExtractReplace");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_UNZIP;
        asProg[5] = "-r";
        asProg[6] = sFILE_ZIP;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            if (sbOutput.indexOf("2 matching file entries extracted") < 0) fail("Not 2 matching file entries extracted!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
    }

    /**
   * Test method for extracting single (empty) file.
   */
    public void testExtractFile() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testExtractFile");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_TEST;
        asProg[5] = sFILE_ZIP;
        asProg[6] = "full/empty.txt";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            File fileFolderEmpty = new File(sFOLDER_EMPTY);
            fileFolderEmpty.delete();
            File fileFileFull = new File(sFILE_FULL);
            fileFileFull.delete();
            File fileFolderUnzip = new File(sFOLDER_UNZIP);
            if (!compareAll(fileFolderUnzip, fileFolderTest)) fail("Folder " + fileFolderUnzip.getAbsolutePath() + " is not equal to folder " + fileFolderTest.getAbsolutePath() + "!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for extracting single (full) folder.
   */
    public void testExtractFolder() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testExtractFolder");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_TEST;
        asProg[5] = sFILE_ZIP;
        asProg[6] = "full/*";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            File fileFolderEmpty = new File(sFOLDER_EMPTY);
            fileFolderEmpty.delete();
            File fileFolderUnzip = new File(sFOLDER_UNZIP);
            if (!compareAll(fileFolderUnzip, fileFolderTest)) fail("Folder " + fileFolderUnzip.getAbsolutePath() + " is not equal to folder " + fileFolderTest.getAbsolutePath() + "!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for extracting of a folder and a file.
   */
    public void testExtractSet() {
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testExtractSet");
        String[] asProg = new String[8];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_TEST;
        asProg[5] = sFILE_ZIP;
        asProg[6] = "empty/*";
        asProg[7] = "full/full.txt";
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            File fileFileEmpty = new File(sFILE_EMPTY);
            fileFileEmpty.delete();
            File fileFolderUnzip = new File(sFOLDER_UNZIP);
            if (!compareAll(fileFolderUnzip, fileFolderTest)) fail("Folder " + fileFolderUnzip.getAbsolutePath() + " is not equal to folder " + fileFolderTest.getAbsolutePath() + "!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
    }

    /**
   * Test method for extracting of two files specified in a file list.
   */
    public void testExtractList() {
        File fileList = new File(sFILE_LIST);
        try {
            FileWriter fw = new FileWriter(fileList);
            fw.write("full/empty.txt\n");
            fw.write("full/full.txt\n");
            fw.close();
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        File fileFolderTest = new File(sFOLDER_TEST);
        deleteAll(fileFolderTest);
        fileFolderTest.mkdir();
        System.out.println("testExtractList");
        String[] asProg = new String[7];
        asProg[0] = "java";
        asProg[1] = "-jar";
        asProg[2] = "lib/zip64.jar";
        asProg[3] = "x";
        asProg[4] = "-d=" + sFOLDER_TEST;
        asProg[5] = sFILE_ZIP;
        asProg[6] = "@" + sFILE_LIST;
        try {
            Process procZip64 = Runtime.getRuntime().exec(asProg);
            StringBuilder sbOutput = new StringBuilder();
            StringBuilder sbError = new StringBuilder();
            InputStream isStdOut = procZip64.getInputStream();
            InputStream isStdErr = procZip64.getErrorStream();
            for (int c = isStdOut.read(); c != -1; c = isStdOut.read()) sbOutput.append((char) c);
            for (int c = isStdErr.read(); c != -1; c = isStdErr.read()) sbError.append((char) c);
            isStdOut.close();
            isStdErr.close();
            int iExitCode = procZip64.waitFor();
            if (iExitCode != 0) fail("zip64 exit code: " + String.valueOf(iExitCode));
            procZip64.destroy();
            if (sbError.toString().length() > 0) fail("Invalid error: " + sbError.toString());
            File fileFolderEmpty = new File(sFOLDER_EMPTY);
            fileFolderEmpty.delete();
            File fileFolderUnzip = new File(sFOLDER_UNZIP);
            if (!compareAll(fileFolderUnzip, fileFolderTest)) fail("Folder " + fileFolderUnzip.getAbsolutePath() + " is not equal to folder " + fileFolderTest.getAbsolutePath() + "!");
        } catch (IOException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        } catch (InterruptedException ie) {
            fail(ie.getClass().getName() + ": " + ie.getMessage());
        }
        deleteAll(fileFolderTest);
        fileList.delete();
    }
}
