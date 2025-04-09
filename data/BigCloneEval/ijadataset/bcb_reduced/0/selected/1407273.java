package com.empower.utils.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipManagerImpl implements ZipManager {

    private static final String fileSeparator = System.getProperty("file.separator");

    public ZipManagerImpl() {
    }

    public void createZippedFile(String dirName, String outputZippedFileName) throws Exception {
        File dirHandle = new File(dirName);
        if (dirHandle.exists() && dirHandle.isDirectory()) {
            String[] dirEntryList = dirHandle.list();
            this.createZippedFile(dirEntryList, dirName, outputZippedFileName);
        } else {
            throw new Exception("Specified dirName either does not exist or is not a directory!");
        }
    }

    public void createZippedFile(String[] fileNameList, String dirName, String outputZippedFileName) throws Exception {
        ZipOutputStream zipOutput = null;
        FileInputStream inputFile = null;
        try {
            if (fileNameList.length <= 0) {
                throw new Exception("Passed fileNameList is empty!");
            }
            FileOutputStream out = new FileOutputStream(new File(outputZippedFileName));
            zipOutput = new ZipOutputStream(out);
            ZipEntry tmpZipEntry = null;
            CRC32 crc = new CRC32();
            for (int i = 0; i < fileNameList.length; i++) {
                String tmpFile = fileNameList[i];
                File tmpFileHandle = new File(dirName + fileSeparator + tmpFile);
                inputFile = new FileInputStream(tmpFileHandle);
                if (!tmpFileHandle.exists()) {
                    throw new Exception("One or more files specified in fileNameList do not exist!");
                }
                if (!tmpFileHandle.isFile()) {
                    throw new Exception("One or more files specified in fileNameList is not a normal file!");
                }
                if (!tmpFileHandle.canRead()) {
                    throw new Exception("One or more files specified in fileNameList is not readable!");
                }
                int fileSize = (int) tmpFileHandle.length();
                byte[] b = new byte[fileSize];
                crc.reset();
                int bytesRead = 0;
                while (fileSize > 0 && ((bytesRead = inputFile.read(b)) != -1)) {
                    crc.update(b, 0, bytesRead);
                }
                tmpZipEntry = new ZipEntry(tmpFile);
                tmpZipEntry.setMethod(ZipEntry.STORED);
                tmpZipEntry.setCompressedSize(tmpFileHandle.length());
                tmpZipEntry.setSize(tmpFileHandle.length());
                tmpZipEntry.setCrc(crc.getValue());
                zipOutput.putNextEntry(tmpZipEntry);
                zipOutput.write(b);
                zipOutput.flush();
                inputFile.close();
            }
            zipOutput.close();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (zipOutput != null) {
                zipOutput.close();
            }
            if (inputFile != null) {
                inputFile.close();
            }
        }
    }

    public ArrayList<String> unzipFile(String fileName, String outputDirectory) throws Exception {
        File zippedFileHandle = null;
        ZipFile zipFile = null;
        InputStream inputStream = null;
        FileOutputStream foutStream = null;
        ArrayList<String> returnFileNameArray = new ArrayList<String>();
        try {
            zippedFileHandle = new File(fileName);
            if (!zippedFileHandle.exists()) {
                throw new Exception("File : " + fileName + " does not exist!");
            }
            if (!zippedFileHandle.isFile()) {
                throw new Exception("The fileName : " + fileName + " is not a normal file!");
            }
            if (!zippedFileHandle.canRead()) {
                throw new Exception("The fileName : " + fileName + " is not readable!");
            }
            zipFile = new ZipFile(zippedFileHandle);
            if (zipFile == null) {
                throw new Exception("Failed to open zipped file : " + fileName + " for reading!");
            }
            int numEntries = zipFile.size();
            if (numEntries <= 0) {
                throw new Exception("No entries found in the zipped file : " + fileName);
            }
            for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                String name = e.nextElement().toString();
                ZipEntry zipEntry = zipFile.getEntry(name);
                String outputFilename = name.substring(name.lastIndexOf('/') + 1);
                File outDir = new File(outputDirectory + fileSeparator);
                outDir.mkdirs();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                inputStream = zipFile.getInputStream(zipEntry);
                outputFilename = outDir.getAbsolutePath() + fileSeparator + outputFilename;
                foutStream = new FileOutputStream(outputFilename);
                int fileSize = (int) zipEntry.getSize();
                byte[] b = new byte[fileSize];
                int byteRead = 0;
                while (byteRead != -1) {
                    byteRead = inputStream.read(b, 0, fileSize - byteRead);
                }
                foutStream.write(b);
                foutStream.flush();
                foutStream.close();
                returnFileNameArray.add(outputFilename);
            }
            return returnFileNameArray;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (foutStream != null) {
                foutStream.close();
            }
        }
    }

    public ArrayList<String> unzipFileMyWay(String fileName, String outputDirectory) throws Exception {
        File zippedFileHandle = null;
        ZipFile zipFile = null;
        InputStream inputStream = null;
        FileOutputStream foutStream = null;
        ArrayList<String> returnFileNameArray = new ArrayList<String>();
        try {
            zippedFileHandle = new File(fileName);
            if (!zippedFileHandle.exists()) {
                throw new Exception("File : " + fileName + " does not exist!");
            }
            if (!zippedFileHandle.isFile()) {
                throw new Exception("The fileName : " + fileName + " is not a normal file!");
            }
            if (!zippedFileHandle.canRead()) {
                throw new Exception("The fileName : " + fileName + " is not readable!");
            }
            zipFile = new ZipFile(zippedFileHandle);
            if (zipFile == null) {
                throw new Exception("Failed to open zipped file : " + fileName + " for reading!");
            }
            int numEntries = zipFile.size();
            if (numEntries <= 0) {
                throw new Exception("No entries found in the zipped file : " + fileName);
            }
            for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
                String name = e.nextElement().toString();
                ZipEntry zipEntry = zipFile.getEntry(name);
                String outputFilename = name;
                File outDir = new File(outputDirectory);
                outDir.mkdirs();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                inputStream = zipFile.getInputStream(zipEntry);
                outputFilename = outDir.getAbsolutePath() + fileSeparator + outputFilename;
                foutStream = new FileOutputStream(outputFilename);
                int fileSize = (int) zipEntry.getSize();
                byte[] b = new byte[fileSize];
                int byteRead = 0;
                while (byteRead != -1) {
                    byteRead = inputStream.read(b, 0, fileSize - byteRead);
                }
                foutStream.write(b);
                foutStream.flush();
                foutStream.close();
                returnFileNameArray.add(outputFilename);
            }
            return returnFileNameArray;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (foutStream != null) {
                foutStream.close();
            }
        }
    }
}
