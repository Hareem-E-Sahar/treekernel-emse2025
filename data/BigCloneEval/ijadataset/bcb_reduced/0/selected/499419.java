package org.webstrips.core.bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.coffeeshop.io.Files;
import org.coffeeshop.io.Streams;
import org.webstrips.core.comic.ComicDescription;
import org.webstrips.core.comic.ComicException;

/**
 * The Class ComicBundle.
 */
public class ComicBundle {

    /**
	 * The Constant DESCRIPTION_NAME.
	 */
    public static final String DESCRIPTION_NAME = "description.ini";

    private static boolean extractEntry(ZipFile zip, String entry, File toDir, boolean overwrite) throws FileNotFoundException, IOException {
        File destFile = Files.join(toDir, entry);
        if (destFile.exists() && !overwrite) return false;
        InputStream in = zip.getInputStream(new ZipEntry(entry));
        if (in == null) throw new FileNotFoundException("File not found in archive:" + entry);
        FileOutputStream out = new FileOutputStream(destFile);
        Streams.copyStream(in, out);
        in.close();
        out.close();
        return true;
    }

    private static boolean addEntry(File src, ZipOutputStream zip, String entry) throws IOException {
        if (!src.exists()) return false;
        zip.putNextEntry(new ZipEntry(entry));
        FileInputStream in = new FileInputStream(src);
        Streams.copyStream(in, zip);
        in.close();
        zip.closeEntry();
        return true;
    }

    /**
	 * The description.
	 */
    private ComicDescription description;

    private File source;

    /**
	 * The Constructor.
	 * 
	 * @param bundleSource
	 *            the bundle source can be a directory or a zip file
	 * 
	 * @throws ComicException
	 *             the comic exception
	 */
    public ComicBundle(File bundleSource) throws ComicException {
        description = verifyBundle(bundleSource);
        this.source = bundleSource;
    }

    private ComicDescription verifyBundle(File f) throws ComicException {
        if (!f.exists()) throw new ComicBundleException(f + " does not exist");
        if (f.isDirectory()) {
            File description = new File(f, DESCRIPTION_NAME);
            if (!description.exists()) throw new ComicBundleException("Description file not found in " + f);
            return new ComicDescription(description);
        } else {
            try {
                ZipFile bundle = new ZipFile(f);
                InputStream din = bundle.getInputStream(new ZipEntry(DESCRIPTION_NAME));
                if (din == null) throw new ComicBundleException("Description file not found in " + f);
                return new ComicDescription(din);
            } catch (IOException e) {
                throw new ComicBundleException("Unable to open bundle " + f);
            }
        }
    }

    /**
	 * Gets the description.
	 * 
	 * @return the description
	 */
    public ComicDescription getDescription() {
        return description;
    }

    /**
	 * Unpack.
	 * 
	 * @param destination
	 *            the destination
	 * 
	 * @throws ComicBundleException
	 *             the comic bundle exception
	 */
    public String unpack(File destination) throws ComicBundleException {
        String dir = description.getShortName();
        if (!destination.isDirectory()) throw new ComicBundleException("Destination must be a directory", description);
        File destDir = Files.subdirectory(destination, dir, true);
        if (destDir == null) throw new ComicBundleException("Unable to unpack to nonexisting directory", description);
        String engine = "";
        switch(description.engineType()) {
            case NATIVE:
                engine = description.getShortName() + ".class";
                break;
            case JAVASCRIPT:
                engine = "comic.js";
                break;
        }
        if (source.isDirectory()) {
            try {
                Files.copy(new File(source, DESCRIPTION_NAME), new File(destDir, DESCRIPTION_NAME));
                Files.copy(new File(source, engine), new File(destDir, engine));
            } catch (IllegalArgumentException e) {
                throw new ComicBundleException("Unable to unpack: " + e.getMessage(), description);
            }
        } else {
            try {
                ZipFile zip = new ZipFile(source);
                extractEntry(zip, DESCRIPTION_NAME, destDir, true);
                extractEntry(zip, engine, destDir, true);
            } catch (IOException e) {
                throw new ComicBundleException("Unable to unpack: " + e.getMessage(), description);
            }
        }
        return destDir.getAbsolutePath();
    }

    public String pack(File destination) throws ComicBundleException {
        if (destination.exists() && destination.isDirectory()) {
            destination = new File(destination, description.getShortName() + ".comic");
        } else if (!destination.isFile() && destination.exists()) throw new ComicBundleException("Unable to pack to this destination", description);
        String engine = "";
        switch(description.engineType()) {
            case NATIVE:
                engine = description.getShortName() + ".class";
                break;
            case JAVASCRIPT:
                engine = "comic.js";
                break;
        }
        if (source.isDirectory()) {
            try {
                ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(destination));
                addEntry(new File(source, DESCRIPTION_NAME), zip, DESCRIPTION_NAME);
                addEntry(new File(source, engine), zip, engine);
                zip.close();
            } catch (IOException e) {
                throw new ComicBundleException("Unable to pack: " + e.getMessage(), description);
            }
        } else {
            throw new ComicBundleException("Copying from file to file currently unsupported", description);
        }
        return destination.getAbsolutePath();
    }
}
