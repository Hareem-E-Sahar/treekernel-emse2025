package cz.cvut.fel.mvod.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Decorator to java.io.File.
 * @author jakub
 */
class FileContainer extends File implements FileSystemUnit {

    private static final long serialVersionUID = 123648964987458498l;

    private String relativePath;

    private ZipEntry entry;

    /**
	 * Creates new instance of <code>FileContainer</code>.
	 * @param absolutePath system dependent path to file in file system
	 * @param relativePath path of file in zipe file
	 */
    public FileContainer(String absolutePath, String relativePath) {
        super(absolutePath);
        this.relativePath = (relativePath == null ? "" : relativePath);
    }

    /**
	 * Creates new instance of <code>FileContainer</code>.
	 * @param entry of this file in zip file
	 * @param pathToSaveEntry absolute path to directory, where will be this file saved
	 */
    public FileContainer(ZipEntry entry, String pathToSaveEntry) {
        super(pathToSaveEntry + "/" + entry.getName());
        this.entry = entry;
    }

    /**
	 * {@inheritDoc}
	 */
    public String getRelativePath() {
        return relativePath;
    }

    /**
	 * {@inheritDoc}
	 */
    public void write(ZipOutputStream out) throws IOException {
        FileInputStream in = null;
        try {
            out.putNextEntry(new ZipEntry(relativePath + getName()));
            in = new FileInputStream(this);
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            out.write(buffer, 0, buffer.length);
        } finally {
            try {
                out.closeEntry();
            } catch (IOException ex) {
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    /**
	 * Reads content of file from zip file and stores it into the file in specified location.
	 * @param in zip file
	 * @throws IOException if IO error occurs
	 */
    public void read(ZipInputStream in) throws IOException {
        FileOutputStream out = null;
        try {
            createNewFile();
            out = new FileOutputStream(this);
            byte[] buffer = new byte[(int) entry.getSize()];
            in.read(buffer);
            out.write(buffer);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
