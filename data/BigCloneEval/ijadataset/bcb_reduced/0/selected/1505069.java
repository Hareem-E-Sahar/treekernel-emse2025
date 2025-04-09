package net.sf.force4maven.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.DirectoryWalker;

public class PackageArchiver extends DirectoryWalker {

    protected File packageDirectory;

    protected ByteArrayOutputStream data;

    protected ZipOutputStream output;

    protected String name;

    public PackageArchiver(File packageDirectory, String name) {
        this.packageDirectory = packageDirectory;
        this.name = name;
        data = new ByteArrayOutputStream();
    }

    public byte[] archive() throws IOException {
        walk(packageDirectory, null);
        return data.toByteArray();
    }

    /**
	 * @see org.apache.commons.io.DirectoryWalker#handleEnd(java.util.Collection)
	 */
    @Override
    protected void handleEnd(Collection results) throws IOException {
        super.handleEnd(results);
        output.flush();
        output.close();
    }

    /**
	 * @see org.apache.commons.io.DirectoryWalker#handleFile(java.io.File, int, java.util.Collection)
	 */
    @Override
    protected void handleFile(File file, int depth, Collection results) throws IOException {
        String path = "";
        File parent = file.getParentFile();
        for (int i = 0; i < depth - 1; i++) {
            path = parent.getName() + "/" + path;
            parent = parent.getParentFile();
        }
        ZipEntry entry = new ZipEntry(path + file.getName());
        output.putNextEntry(entry);
        int size = (int) file.length();
        byte[] buffer = new byte[size];
        FileInputStream fis = new FileInputStream(file);
        fis.read(buffer);
        output.write(buffer);
    }

    /**
	 * @see org.apache.commons.io.DirectoryWalker#handleStart(java.io.File, java.util.Collection)
	 */
    @Override
    protected void handleStart(File startDirectory, Collection results) throws IOException {
        super.handleStart(startDirectory, results);
        this.output = new ZipOutputStream(data);
    }
}
