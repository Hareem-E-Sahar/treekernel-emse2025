package testing;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TestCreateZipFile {

    /**
	 * @param args
	 * @throws IOException
	 */
    public static void main(String[] args) throws IOException {
        String inputFile = "/tmp/php_error.log";
        String outputFile = "/tmp/php_error.log.zip";
        String zipEntryName = "php_error.log.zip";
        byte[] fileContent = readFileContent(inputFile);
        byte[] zipFileContent = compress(fileContent, zipEntryName);
        writeFileContent(outputFile, zipFileContent);
    }

    private static byte[] readFileContent(String filename) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int bytesRead = fis.read(buff);
            while (bytesRead > 0) {
                baos.write(buff, 0, bytesRead);
                bytesRead = fis.read(buff);
            }
            return baos.toByteArray();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private static void writeFileContent(String filename, byte[] content) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filename);
            fos.write(content);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private static byte[] compress(byte[] bytes, String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        ZipOutputStream zos = new ZipOutputStream(baos);
        zos.setMethod(ZipEntry.DEFLATED);
        zos.setLevel(6);
        ZipEntry ze = new ZipEntry(name);
        ze.setSize(bytes.length);
        crc.update(bytes);
        ze.setCrc(crc.getValue());
        zos.putNextEntry(ze);
        zos.write(bytes);
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }
}
