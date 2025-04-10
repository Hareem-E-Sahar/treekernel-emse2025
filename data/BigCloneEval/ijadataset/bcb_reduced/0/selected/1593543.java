package fm.last.commons.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import fm.last.commons.test.LastTestCase;
import fm.last.commons.test.TestAssert;

/**
 * Test case for custom FileUtils class.
 */
public class LastFileUtilsTest extends LastTestCase {

    /**
   * Tests requesting a tail that is bigger than the file, should read in entire file.
   * 
   * @throws IOException
   */
    @Test
    public void testTailBiggerThanFile() throws IOException {
        File file = new File(testDataFolder, "3805bytes.log");
        String tail = LastFileUtils.tail(file, 50000);
        assertEquals(FileUtils.readFileToString(file), tail);
    }

    /**
   * Tests requesting a tail that is the same size as the file, should read in entire file.
   * 
   * @throws IOException
   */
    @Test
    public void testTailSameAsFile() throws IOException {
        File file = new File(testDataFolder, "3805bytes.log");
        String tail = LastFileUtils.tail(file, 3805);
        assertEquals(FileUtils.readFileToString(file), tail);
    }

    /**
   * Tests requesting a tail that is smaller than the file, only the last specified bytes should be read.
   * 
   * @throws IOException
   */
    @Test
    public void testTailSmallerThanFile() throws IOException {
        File file = new File(testDataFolder, "3805bytes.log");
        String tail = LastFileUtils.tail(file, 143);
        assertEquals("2008-01-14 18:25:54,757 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.sh", tail);
    }

    /**
   * Tests requesting a tail that is smaller than the file, that line breaks are preserved.
   * 
   * @throws IOException
   */
    @Test
    public void testTailLineBreaks() throws IOException {
        File file = new File(testDataFolder, "3805bytes.log");
        String tail = LastFileUtils.tail(file, 287);
        assertEquals("2008-01-14 18:25:54,756 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.bat\n2008-01-14 18:25:54,757 fm.last.citrine.jobs.syscommand.RollingFileSysCommandObserver.sysOut(RollingFileSysCommandObserver.java:72) version.sh", tail);
    }

    @Test(expected = java.io.FileNotFoundException.class)
    public void testGetFile_NonExistent() throws FileNotFoundException {
        LastFileUtils.getFile("non-existent-file", this.getClass());
    }

    @Test
    public void testGetFile_OnPath() throws FileNotFoundException {
        File buildXML = LastFileUtils.getFile("build.xml", this.getClass());
        assertTrue(buildXML.exists());
    }

    /**
   * Note: for this to work, test/conf must be on your classpath.
   * 
   * @throws FileNotFoundException
   */
    @Test
    public void testGetFile_OnClasspath() throws FileNotFoundException {
        File checkstyleXML = LastFileUtils.getFile("checkstyle.xml", this.getClass());
        assertTrue(checkstyleXML.exists());
    }

    @Test
    public void testWriteToFile() throws IOException {
        File inputFile = new File(testDataFolder, "3805bytes.log");
        File outputFile = new File(testTempFolder, "copy.log");
        InputStream inputStream = new FileInputStream(inputFile);
        LastFileUtils.writeToFile(inputStream, outputFile);
        assertEquals(FileUtils.readFileToString(inputFile), FileUtils.readFileToString(outputFile));
    }

    @Test
    public void testMoveFileSafely() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        File movedFile = new File(testTempFolder, "copy.log");
        LastFileUtils.moveFileSafely(inputFile, movedFile);
        assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
        assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
        assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
    }

    /**
   * Tests that trying to move a non-existent file that has no parent results in a FileNotFoundException and not a
   * NullPointerException.
   * 
   * @throws IOException
   */
    @Test(expected = FileNotFoundException.class)
    public void testMoveFileSafely_NullParent() throws IOException {
        LastFileUtils.moveFileSafely(new File("noparent"), new File(testTempFolder, "output"));
    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveFileSafely_NonExistent() throws IOException {
        LastFileUtils.moveFileSafely(new File(testTempFolder, "nonexistent"), new File(testTempFolder, "output"));
    }

    @Test
    public void testMoveFileToDirectorySafely() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        File newDir = new File(testTempFolder, "FileUtilsTest");
        assertTrue(newDir.mkdirs());
        assertTrue(newDir.exists());
        LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, false);
        assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
        File movedFile = new File(newDir, inputFile.getName());
        assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
        assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
    }

    @Test
    public void testMoveFileToDirectorySafely_CreateDir() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        File newDir = new File(testTempFolder, "FileUtilsTest");
        assertFalse(newDir.exists());
        LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, true);
        assertFalse(inputFile.getAbsolutePath() + " exists", inputFile.exists());
        File movedFile = new File(newDir, inputFile.getName());
        assertTrue(movedFile.getAbsolutePath() + " doesn't exist", movedFile.exists());
        assertEquals(FileUtils.readFileToString(originalFile), FileUtils.readFileToString(movedFile));
    }

    @Test(expected = FileNotFoundException.class)
    public void testMoveFileToDirectorySafely_NoCreateDir() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        File newDir = new File(testTempFolder, "FileUtilsTest");
        assertFalse(newDir.exists());
        LastFileUtils.moveFileToDirectorySafely(inputFile, newDir, false);
    }

    @Test(expected = NullPointerException.class)
    public void testMoveFileToDirectorySafely_NullDir() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        LastFileUtils.moveFileToDirectorySafely(inputFile, null, false);
    }

    @Test(expected = NullPointerException.class)
    public void testMoveFileToDirectorySafely_NullDir_CreateDir() throws IOException {
        File originalFile = new File(testDataFolder, "3805bytes.log");
        FileUtils.copyFileToDirectory(originalFile, testTempFolder);
        File inputFile = new File(testTempFolder, originalFile.getName());
        assertTrue(inputFile.getAbsolutePath() + " not found", inputFile.exists());
        LastFileUtils.moveFileToDirectorySafely(inputFile, null, true);
    }

    @Test
    public void testAppend1File() throws IOException {
        File destination = new File(testTempFolder, "merged");
        File file1 = new File(testDataFolder, "file1.txt");
        LastFileUtils.appendFiles(destination, file1);
        TestAssert.assertFileEquals(file1, destination);
    }

    @Test
    public void testAppend2Files() throws IOException {
        File destination = new File(testTempFolder, "merged");
        File file1 = new File(testDataFolder, "file1.txt");
        File file2 = new File(testDataFolder, "file2.txt");
        LastFileUtils.appendFiles(destination, file1, file2);
        TestAssert.assertFileEquals(new File(testDataFolder, "file1-2.txt"), destination);
    }

    @Test
    public void testAppend3Files() throws IOException {
        File destination = new File(testTempFolder, "merged");
        File file1 = new File(testDataFolder, "file1.txt");
        File file2 = new File(testDataFolder, "file2.txt");
        File file3 = new File(testDataFolder, "file3.txt");
        LastFileUtils.appendFiles(destination, file1, file2, file3);
        TestAssert.assertFileEquals(new File(testDataFolder, "file1-2-3.txt"), destination);
    }

    @Test
    public void testAppend3Files_List() throws IOException {
        File destination = new File(testTempFolder, "merged");
        List<File> files = new ArrayList<File>();
        files.add(new File(testDataFolder, "file1.txt"));
        files.add(new File(testDataFolder, "file2.txt"));
        files.add(new File(testDataFolder, "file3.txt"));
        LastFileUtils.appendFiles(destination, files);
        TestAssert.assertFileEquals(new File(testDataFolder, "file1-2-3.txt"), destination);
    }
}
