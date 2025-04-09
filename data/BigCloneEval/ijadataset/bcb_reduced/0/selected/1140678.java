package com.meterware.website;

import org.jmock.MockObjectTestCase;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author <a href="mailto:russgold@meterware.com">Russell Gold</a>
 */
public class QuickSiteTestCase extends MockObjectTestCase {

    private static File _temporaryDirectory;

    /**
     * Creates a temporary text file with the specified contents.
     * @param contents
     * @return the created file
     */
    protected static File createTextFile(String contents) throws IOException {
        File file = File.createTempFile("fragment", ".txt");
        if (_temporaryDirectory == null) _temporaryDirectory = file.getParentFile();
        return writeFileContents(file, contents);
    }

    /**
     * Creates a temporary text file with the specified contents.
     * @param contents
     * @return the created file
     */
    protected static File createTextFile(String directory, String contents) throws IOException {
        File file = createTextFile(contents);
        File path = new File(new File(file.getParentFile(), directory), file.getName());
        path.mkdir();
        file.renameTo(path);
        return path;
    }

    protected File createTextFile(File groupDir, String contents) throws IOException {
        File file = File.createTempFile("fragment", ".txt", groupDir);
        return writeFileContents(file, contents);
    }

    protected File createTextFile(File groupDir, String fileName, String contents) throws IOException {
        File file = new File(groupDir, fileName);
        return writeFileContents(file, contents);
    }

    private static File writeFileContents(File file, String contents) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(contents);
        writer.close();
        return file;
    }

    protected void clearDir(File dir) {
        File[] entries = dir.listFiles();
        for (int i = 0; i < entries.length; i++) {
            File entry = entries[i];
            if (!entry.exists()) continue;
            if (entry.isDirectory()) clearDir(entry);
            entry.delete();
        }
    }
}
