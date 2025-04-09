package org.pointrel.pointrel20090201;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ArchiveBackendUsingZipFile implements ArchiveBackend {

    String zipFilePath;

    public ArchiveBackendUsingZipFile(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getTransactionIdentifierList() {
        ArrayList<String> resultList = new ArrayList<String>();
        try {
            ZipFile zipFile = new ZipFile(new File(this.zipFilePath), ZipFile.OPEN_READ);
            Enumeration zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String name = entry.getName();
                resultList.add(ArchiveFileSupport.extractIdentifier(name));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public boolean retrieveDataForTransactionIdentifier(String transactionIdentifier, OutputStream outputStream) {
        try {
            ZipFile zipFile = new ZipFile(new File(this.zipFilePath), ZipFile.OPEN_READ);
            ZipEntry entry = zipFile.getEntry(ArchiveFileSupport.fileNameForTransactionIdentifier(transactionIdentifier));
            if (entry == null) {
                return false;
            } else {
                InputStream zipInputStream = zipFile.getInputStream(entry);
                ArchiveFileSupport.copyInputStreamToOutputStream(zipInputStream, outputStream);
                zipInputStream.close();
                zipFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean storeDataForTransactionIdentifier(String transactionIdentifier, InputStream inputStream, int inputLength) {
        try {
            if (true) {
                System.out.println("This does not work because Java 1.5 does not support appending to zip files");
                return false;
            }
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(this.zipFilePath, true));
            ZipEntry entry = new ZipEntry(ArchiveFileSupport.fileNameForTransactionIdentifier(transactionIdentifier));
            zipOutputStream.putNextEntry(entry);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, length);
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isReadOnly() {
        return true;
    }

    public boolean supportsTransactionObjects() {
        return false;
    }

    public Transaction retrieveTransactionForTransactionIdentifier(String transactionIdentifier) {
        return null;
    }

    public boolean storeTransactionForTransactionIdentifier(String transactionIdentifier, Transaction transaction) {
        return false;
    }
}
