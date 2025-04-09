package name.huzhenbo.java.io;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

public class FileTest {

    @Test
    public void should_create_a_file() throws IOException {
        File file = new File("res/file.test");
        if (!file.exists()) {
            file.createNewFile();
        }
        assertTrue(file.isFile());
        assertEquals("file.test", file.getName());
        assertEquals("res\\file.test", file.getPath());
        assertEquals("file.test", file.getAbsoluteFile().getName());
        assertTrue(file.getAbsolutePath().endsWith("\\res\\file.test"));
        assertEquals("file.test", file.getCanonicalFile().getName());
        assertTrue(file.getCanonicalPath().endsWith("\\res\\file.test"));
        assertEquals("res", file.getParent());
        assertEquals("res", file.getParentFile().getName());
        file.delete();
    }

    @Test
    public void should_make_directory() {
        File file = new File("res/dirtest");
        if (!file.exists()) {
            file.mkdir();
        }
        assertTrue(file.isDirectory());
        file.delete();
    }

    @Test
    public void should_make_directories() {
        File file = new File("res/dir/test");
        if (!file.exists()) {
            file.mkdirs();
        }
        assertTrue(file.isDirectory());
        assertEquals("test", file.getName());
        assertEquals("dir", file.getParentFile().getName());
        file.delete();
        file.getParentFile().delete();
    }

    @Test
    public void should_list_roots() throws IOException {
        for (File dir : File.listRoots()) {
            assertTrue(dir.getPath().endsWith(":\\"));
        }
    }

    @Test
    public void should_list_files() throws IOException {
        File dir = new File("res/dir/test");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        assertTrue(dir.isDirectory());
        File file = new File("res/dir/test.file");
        if (!file.exists()) {
            file.createNewFile();
        }
        assertTrue(file.isFile());
        File file2 = new File("res/dir/test.file2");
        if (!file2.exists()) {
            file2.createNewFile();
        }
        assertTrue(file2.isFile());
        File parentDir = dir.getParentFile();
        assertEquals(3, parentDir.list().length);
        assertEquals(3, parentDir.listFiles().length);
        FilenameFilter filenameFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith("file");
            }
        };
        assertEquals(1, parentDir.list(filenameFilter).length);
        dir.delete();
        file.delete();
        file2.delete();
        parentDir.delete();
    }

    @Test
    public void should_rename() throws IOException {
        File file = new File("res/test.file");
        if (!file.exists()) {
            file.createNewFile();
        }
        assertEquals("test.file", file.getName());
        File newFile = new File("res/newTest.file");
        assertTrue(file.renameTo(newFile));
        assertFalse(file.exists());
        assertEquals("newTest.file", newFile.getName());
        newFile.delete();
    }
}
