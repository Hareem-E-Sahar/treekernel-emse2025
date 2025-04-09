package org.dbreplicator.replication.zip;

import java.io.*;
import java.util.zip.*;
import org.dbreplicator.replication.*;

/**
 * This class is used to make zip file of the XML file,BLOB.lob and CLOB.lob
 * so that compacted files can be transferred over the network. Besides it, this
 * class contains method for unzipping it.
 */
public class ZipHandler {

    /**
     * makes a zip file named <xmlFileName> from <xmlURL> at path <zipURL>
     * @param zipURL
     * @param xmlURL
     * @param xmlFileName
     */
    public static void makeZip(String zipURL, String xmlURL, String xmlFileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(zipURL));
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(xmlURL);
        zos.putNextEntry(new ZipEntry(xmlFileName + ".xml"));
        writeInOutputStream(fis, zos);
        String bpath = PathHandler.getBLobFilePathForClient();
        FileInputStream fisBLOB = createInputStream(bpath);
        zos.putNextEntry(new ZipEntry("blob.lob"));
        writeInOutputStream(fisBLOB, zos);
        zos.closeEntry();
        String cpath = PathHandler.getCLobFilePathForClient();
        FileInputStream fisCLOB = createInputStream(cpath);
        zos.putNextEntry(new ZipEntry("clob.lob"));
        writeInOutputStream(fisCLOB, zos);
        zos.closeEntry();
        fis.close();
        fisCLOB.close();
        fisBLOB.close();
        zos.close();
        fos.close();
    }

    /**
     * unzipps a zip file placed at <zipURL> to path <xmlURL>
     * @param zipURL
     * @param xmlURL
     */
    public static void unZip(String zipURL, String xmlURL) throws IOException {
        FileOutputStream fosBLOB = new FileOutputStream(PathHandler.getBLobFilePathForClient());
        FileOutputStream fosCLOB = new FileOutputStream(PathHandler.getCLobFilePathForClient());
        FileInputStream fis = new FileInputStream(new File(zipURL));
        ZipInputStream zis = new ZipInputStream(fis);
        FileOutputStream fos = new FileOutputStream(xmlURL);
        ZipEntry ze = zis.getNextEntry();
        ExtractZip(zis, fos, fosBLOB, fosCLOB, ze);
        ze = zis.getNextEntry();
        ExtractZip(zis, fos, fosBLOB, fosCLOB, ze);
        ze = zis.getNextEntry();
        ExtractZip(zis, fos, fosBLOB, fosCLOB, ze);
        fos.flush();
        fis.close();
        fos.close();
        fosCLOB.close();
        fosBLOB.close();
        zis.close();
    }

    private static void ExtractZip(ZipInputStream zis, FileOutputStream fos, FileOutputStream fosBLOB, FileOutputStream fosCLOB, ZipEntry ze) throws IOException {
        if (ze.getName().equalsIgnoreCase("blob.lob")) writeInOutputStream(zis, fosBLOB); else if (ze.getName().equalsIgnoreCase("clob.lob")) writeInOutputStream(zis, fosCLOB); else writeInOutputStream(zis, fos);
    }

    private static void writeInOutputStream(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
    }

    /**
     * makes a zip file named <xmlFileName> from <xmlURL> at path <zipURL>
     * @param zipURL
     * @param xmlURL
     * @param xmlFileName
     */
    public static void makeStructZip(String zipURL, String xmlURL, String xmlFileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(zipURL));
        ZipOutputStream zos = new ZipOutputStream(fos);
        FileInputStream fis = new FileInputStream(xmlURL);
        zos.putNextEntry(new ZipEntry(xmlFileName + ".xml"));
        writeInOutputStream(fis, zos);
        zos.closeEntry();
        fis.close();
        zos.close();
    }

    /**
     * unzipps a zip file placed at <zipURL> to path <xmlURL>
     * @param zipURL
     * @param xmlURL
     */
    public static void unStructZip(String zipURL, String xmlURL) throws IOException {
        FileInputStream fis = new FileInputStream(new File(zipURL));
        ZipInputStream zis = new ZipInputStream(fis);
        FileOutputStream fos = new FileOutputStream(xmlURL);
        ZipEntry ze = zis.getNextEntry();
        writeInOutputStream(zis, fos);
        fos.flush();
        fis.close();
        fos.close();
        zis.close();
    }

    private static FileInputStream createInputStream(String bpath) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(bpath);
        } catch (FileNotFoundException ex) {
            FileOutputStream temp = new FileOutputStream(bpath);
            fis = new FileInputStream(bpath);
        }
        return fis;
    }
}
