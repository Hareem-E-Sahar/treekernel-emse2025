package net.ambre.ant.check_file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.TreeSet;
import org.apache.tools.ant.Task;

public abstract class CheckFile extends Task {

    private String extension;

    private Collection<String> errors;

    private Collection<String> warnings;

    public Collection<String> getWarnings() {
        return warnings;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public void init() {
        errors = new TreeSet<String>();
        warnings = new TreeSet<String>();
    }

    public abstract void checkFile(File file);

    protected ByteBuffer getFileAsBytes(File file) throws IOException {
        byte buffer[] = new byte[2048];
        int n;
        InputStream input = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        while ((n = input.read(buffer)) != -1) {
            outputStream.write(buffer, 0, n);
        }
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    protected void registerError(String file, String message) {
        errors.add("Error in file: " + file + ": " + message);
    }

    protected void registerWarning(String file, String message) {
        warnings.add("Warning in file: " + file + ": " + message);
    }
}
