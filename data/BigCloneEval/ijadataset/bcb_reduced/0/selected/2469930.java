package org.kommando.filesystem.win32;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.kommando.core.util.Assert;
import org.kommando.filesystem.FileSystemBrowser;
import org.kommando.filesystem.FileSystemBrowserException;

/**
 * {@link FileSystemBrowser} implementation for Windows Explorer.
 * 
 * @author Peter De Bruycker
 */
public class WindowsExplorer implements FileSystemBrowser {

    private String grabSelectedFile;

    public WindowsExplorer(String grabSelectedFile) {
        this.grabSelectedFile = grabSelectedFile;
    }

    @Override
    public void reveal(String path) throws FileSystemBrowserException {
        Assert.argumentNotEmpty("path", path);
        try {
            new ProcessBuilder("explorer", "/e,", "/select,", path).start();
        } catch (IOException e) {
            throw new FileSystemBrowserException("Error while trying to reveal " + path, e);
        }
    }

    @Override
    public String getSelectedFile() throws FileSystemBrowserException {
        try {
            Process process = new ProcessBuilder(System.getenv("comspec"), "/c", grabSelectedFile, "|", "find", "/V", "\"\"").start();
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            reader.close();
            return line;
        } catch (IOException e) {
            throw new FileSystemBrowserException("Error while getting selected file", e);
        }
    }
}
