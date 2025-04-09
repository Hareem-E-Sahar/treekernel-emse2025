package net.sf.gateway.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import org.apache.axis.attachments.OctetStream;

/**
 * Utility methods for working with Zip files
 */
public final class ZipUtils {

    public static void main(String[] args) throws Exception {
        Map<String, byte[]> zipThis = new HashMap<String, byte[]>();
        zipThis.put("/xml/test.xml", "<A><B><C><D>test</D></C></B></A>".getBytes());
        zipThis.put("/manifest/manifest.xml", "<A><B><C><D>manifest</D></C></B></A>".getBytes());
        zipThis.put("/attachment/test.pdf", IOUtils.byteSafeRead("test.pdf"));
        byte[] inter = ZipUtils.zipFiles(zipThis);
        IOUtils.byteSafeWrite(inter, "nested.zip");
        Map<String, byte[]> zipThis2 = new HashMap<String, byte[]>();
        zipThis2.put("nested.zip", inter);
        byte[] finalZip = ZipUtils.zipFiles(zipThis2);
        IOUtils.byteSafeWrite(finalZip, "finalZip.zip");
        Map<String, byte[]> filesz = ZipUtils.unzipFiles(finalZip);
        Map<String, byte[]> filesz2 = ZipUtils.unzipFiles(filesz.get("nested.zip"));
        IOUtils.byteSafeWrite(filesz2.get("/attachment/test.pdf"), "test_out.pdf");
    }

    /**
     * Utility classes should not have a public or default constructor. -
     * checkstyle
     */
    private ZipUtils() {
    }

    public static OctetStream toZippedOctetStream(List<ZipEntry> entries, List<byte[]> files) throws java.io.IOException {
        if (entries.size() != files.size()) {
            return null;
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bytes);
        Iterator<ZipEntry> entriesItr = entries.iterator();
        Iterator<byte[]> filesItr = files.iterator();
        while (entriesItr.hasNext()) {
            byte[] file = filesItr.next();
            ZipEntry entry = entriesItr.next();
            zip.putNextEntry(entry);
            zip.write(file, 0, file.length);
        }
        zip.close();
        return new OctetStream(bytes.toByteArray());
    }

    public static List<ZipEntry> toZipEntryList(OctetStream octetStream) throws java.io.IOException {
        ArrayList<ZipEntry> entries = new ArrayList<ZipEntry>();
        ByteArrayInputStream bytes = new ByteArrayInputStream(octetStream.getBytes());
        ZipInputStream zip = new ZipInputStream(bytes);
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            entries.add(entry);
            entry = zip.getNextEntry();
        }
        zip.close();
        return entries;
    }

    public static List<byte[]> toByteArrayList(OctetStream octetStream) throws java.io.IOException {
        ArrayList<byte[]> files = new ArrayList<byte[]>();
        ByteArrayInputStream bytes = new ByteArrayInputStream(octetStream.getBytes());
        ZipInputStream zip = new ZipInputStream(bytes);
        int len;
        ByteArrayOutputStream file;
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            file = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while ((len = zip.read(buf, 0, 4096)) != -1) {
                file.write(buf, 0, len);
            }
            files.add(file.toByteArray());
            entry = zip.getNextEntry();
        }
        zip.close();
        return files;
    }

    /**
     * Takes a Map of strings to byte arrays (filenames to files). Creates a zip
     * file and returns it as another byte array
     * 
     * @param files
     *        a map containing strings mapped to files represented as byte
     *        arrays
     * @return a byte array representing the zipped data
     * @throws IOException
     */
    public static byte[] zipFiles(Map<String, byte[]> files) throws Exception {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte[] data = new byte[2048];
        Iterator<String> itr = files.keySet().iterator();
        while (itr.hasNext()) {
            String tempName = itr.next();
            byte[] tempFile = files.get(tempName);
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(tempFile);
            BufferedInputStream origin = new BufferedInputStream(bytesIn, 2048);
            ZipEntry entry = new ZipEntry(tempName);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, 2048)) != -1) {
                out.write(data, 0, count);
            }
            bytesIn.close();
            origin.close();
        }
        out.close();
        byte[] outBytes = dest.toByteArray();
        dest.close();
        return outBytes;
    }

    /**
     * Takes a Map of ZipEntry to byte arrays (zip metadata and file data). Creates a zip
     * file and returns it as another byte array
     * 
     * @param files
     *        a map containing zip metadata and file data.
     * @return a byte array representing the zipped data
     * @throws IOException
     */
    public static byte[] zipEntriesAndFiles(Map<ZipEntry, byte[]> files) throws Exception {
        ByteArrayOutputStream dest = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        byte[] data = new byte[2048];
        Iterator<ZipEntry> itr = files.keySet().iterator();
        while (itr.hasNext()) {
            ZipEntry entry = itr.next();
            byte[] tempFile = files.get(entry);
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(tempFile);
            BufferedInputStream origin = new BufferedInputStream(bytesIn, 2048);
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, 2048)) != -1) {
                out.write(data, 0, count);
            }
            bytesIn.close();
            origin.close();
        }
        out.close();
        byte[] outBytes = dest.toByteArray();
        dest.close();
        return outBytes;
    }

    /**
     * Takes a byte array representing a zip file and unzips all the contained
     * files into a map.
     * 
     * @param zipBytes
     *        zip file
     * @return Map<String, byte[]> (unzipped stuff)
     * @throws IOException
     */
    public static Map<String, byte[]> unzipFiles(byte[] zipBytes) throws IOException {
        InputStream bais = new ByteArrayInputStream(zipBytes);
        ZipInputStream zin = new ZipInputStream(bais);
        ZipEntry ze;
        Map<String, byte[]> extractedFiles = new HashMap<String, byte[]>();
        while ((ze = zin.getNextEntry()) != null) {
            ByteArrayOutputStream toScan = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = zin.read(buf)) > 0) {
                toScan.write(buf, 0, len);
            }
            byte[] fileOut = toScan.toByteArray();
            toScan.close();
            extractedFiles.put(ze.getName(), fileOut);
        }
        zin.close();
        bais.close();
        return extractedFiles;
    }
}
