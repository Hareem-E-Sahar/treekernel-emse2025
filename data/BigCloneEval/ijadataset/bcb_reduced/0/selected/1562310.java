package ch.jester.common.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Klasse für Zipping/Unzipping
 *
 */
public class ZipUtility {

    /**
	 * Lesen der ZipEntries
	 * @param file
	 * @param includeDirs
	 * @return Liste von ZipEntries
	 */
    public static List<String> getZipEntries(String file, boolean includeDirs) {
        try {
            List<String> entries = new ArrayList<String>();
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    if (includeDirs) {
                        entries.add(entry.getName());
                    }
                } else {
                    entries.add(entry.getName());
                }
            }
            zis.close();
            return entries;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Den ZipEntry als Inputstream lesen
	 * @param zipFile
	 * @param zipEntry
	 * @return den ZipEntryInputStream
	 */
    public static InputStream getZipEntry(String zipFile, String zipEntry) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(zipEntry)) {
                    InputStream instream = deflate(zis, entry);
                    zis.close();
                    return instream;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Unzippen
	 * @param pZipFile
	 * @param pZipEntry
	 * @param pOutputPath
	 * @return
	 */
    public static InputStream unzip(String pZipFile, String pZipEntry, String pOutputPath) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(pZipFile));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(pZipEntry)) {
                    InputStream instream = deflate(zis, entry);
                    zis.close();
                    return instream;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
	 * Zippen eines Directories
	 * @param zos
	 * @param files
	 * @param entry
	 * @throws IOException
	 */
    private static void zipDirectory(ZipOutputStream zos, File files, String entry) throws IOException {
        if (files.isDirectory()) {
            if (entry != null) {
                zos.putNextEntry(new ZipEntry(entry + "/"));
                entry = entry + "/";
            } else {
                zos.putNextEntry(new ZipEntry("/"));
                entry = "/";
            }
            String[] names = files.list();
            File[] files0 = files.listFiles();
            for (int i = 0; i < files0.length; i++) {
                zipDirectory(zos, files0[i], entry + names[i]);
            }
            zos.closeEntry();
        } else {
            if (entry != null) {
                zos.putNextEntry(new ZipEntry(entry));
            }
            byte[] data = new byte[1000];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(files), 1000);
            int count;
            while ((count = in.read(data, 0, 1000)) != -1) {
                zos.write(data, 0, count);
            }
            zos.closeEntry();
            in.close();
        }
    }

    /**
	 * Zippen eines Directories
	 * @param inFolder
	 * @param outFile
	 */
    public static void zipFolder(File inFolder, File outFile) {
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
            zipDirectory(out, inFolder, null);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Deflaten
	 * @param pZis
	 * @param pZipEntry
	 * @return
	 */
    public static InputStream deflate(ZipInputStream pZis, ZipEntry pZipEntry) {
        int size;
        byte[] buffer = new byte[2048];
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
        try {
            while ((size = pZis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(fos.toByteArray());
    }
}
