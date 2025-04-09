package org.pointrel.pointrel20090201;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

class ResourceFileSpecification {

    File file;

    String filePath;

    byte[] bytes;

    ResourceFileSpecification(File file) {
        if (file == null) System.out.println("ResourceFileSpecification: file should not be null");
        this.file = file;
        this.filePath = null;
        this.bytes = null;
    }

    ResourceFileSpecification(String filePath) {
        if (filePath == null) System.out.println("ResourceFileSpecification: filePath should not be null");
        this.file = null;
        this.filePath = filePath;
        this.bytes = null;
    }

    ResourceFileSpecification(byte[] bytes) {
        if (bytes == null) System.out.println("ResourceFileSpecification: bytes should not be null");
        this.file = null;
        this.filePath = null;
        this.bytes = bytes;
    }

    public InputStream getInputStream() {
        try {
            if (file != null) {
                return new FileInputStream(file);
            } else if (filePath != null) {
                if (ArchiveFileSupport.isZipFileReference(filePath)) {
                    System.out.println("Reference to file in a zip file");
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    boolean foundAndCopied = ArchiveFileSupport.copyFromZipFileEntryToStream(filePath, byteArrayOutputStream);
                    if (!foundAndCopied) return null;
                    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                } else {
                    return new FileInputStream(filePath);
                }
            } else if (bytes != null) {
                return new ByteArrayInputStream(bytes);
            } else {
                System.out.println("Problem with ResourceFileSpecification; all fields null");
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean retrieveResourceFile(OutputStream outputStream) {
        try {
            if (file != null) {
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, outputStream);
                } finally {
                    inputStream.close();
                }
            } else if (filePath != null) {
                if (ArchiveFileSupport.isZipFileReference(filePath)) {
                    System.out.println("Reference to file in a zip file");
                    boolean foundAndCopied = ArchiveFileSupport.copyFromZipFileEntryToStream(filePath, outputStream);
                    if (!foundAndCopied) return false;
                } else {
                    FileInputStream inputStream = new FileInputStream(filePath);
                    try {
                        ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, outputStream);
                    } finally {
                        inputStream.close();
                    }
                }
            } else if (bytes != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                ArchiveFileSupport.copyInputStreamToOutputStream(byteArrayInputStream, outputStream);
            } else {
                System.out.println("Problem with ResourceFileSpecification; all fields null");
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] getBytes() {
        try {
            if (file != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                FileInputStream inputStream = new FileInputStream(file);
                try {
                    ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, outputStream);
                } finally {
                    inputStream.close();
                }
                outputStream.close();
                return outputStream.toByteArray();
            } else if (filePath != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                if (ArchiveFileSupport.isZipFileReference(filePath)) {
                    System.out.println("Reference to file in a zip file");
                    boolean foundAndCopied = ArchiveFileSupport.copyFromZipFileEntryToStream(filePath, outputStream);
                    if (!foundAndCopied) return null;
                } else {
                    FileInputStream inputStream = new FileInputStream(filePath);
                    try {
                        ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, outputStream);
                    } finally {
                        inputStream.close();
                    }
                }
                outputStream.close();
                return outputStream.toByteArray();
            } else if (bytes != null) {
                return bytes;
            } else {
                System.out.println("Problem with ResourceFileSpecification; all fields null");
                return null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    boolean copyTo(File destinationFile, boolean updateResourceFiles) {
        try {
            if (file != null) {
                ArchiveFileSupport.copyFromFileToFileUsingNIO(file, destinationFile);
            } else if (filePath != null) {
                if (ArchiveFileSupport.isZipFileReference(filePath)) {
                    System.out.println("Reference to file in a zip file");
                    OutputStream outputStream = new FileOutputStream(destinationFile);
                    boolean foundAndCopied = false;
                    try {
                        foundAndCopied = ArchiveFileSupport.copyFromZipFileEntryToStream(filePath, outputStream);
                    } finally {
                        outputStream.close();
                    }
                    if (!foundAndCopied) return false;
                } else {
                    ArchiveFileSupport.copyFromFileToFileUsingNIO(new File(filePath), destinationFile);
                }
            } else if (bytes != null) {
                InputStream inputStream = new ByteArrayInputStream(bytes);
                OutputStream outputStream = new FileOutputStream(destinationFile);
                try {
                    ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, outputStream);
                } finally {
                    outputStream.close();
                }
                inputStream.close();
            } else {
                System.out.println("Problem with ResourceFileSpecification; all fields null");
                return false;
            }
            if (updateResourceFiles) file = destinationFile;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean copyTo(ZipOutputStream zipOutputStream, boolean updateResourceFiles) {
        try {
            if (updateResourceFiles) {
                this.bytes = this.getBytes();
                if (this.bytes == null) return false;
                this.file = null;
                this.filePath = null;
                InputStream inputStream = new ByteArrayInputStream(bytes);
                ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, zipOutputStream);
                return true;
            } else {
                if (file != null) {
                    FileInputStream inputStream = new FileInputStream(file);
                    ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, zipOutputStream);
                    inputStream.close();
                } else if (filePath != null) {
                    if (ArchiveFileSupport.isZipFileReference(filePath)) {
                        System.out.println("Reference to file in a zip file");
                        boolean foundAndCopied = ArchiveFileSupport.copyFromZipFileEntryToStream(filePath, zipOutputStream);
                        if (!foundAndCopied) return false;
                    } else {
                        FileInputStream inputStream = new FileInputStream(filePath);
                        ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, zipOutputStream);
                        inputStream.close();
                    }
                } else if (bytes != null) {
                    InputStream inputStream = new ByteArrayInputStream(bytes);
                    ArchiveFileSupport.copyInputStreamToOutputStream(inputStream, zipOutputStream);
                    inputStream.close();
                } else {
                    System.out.println("Problem with ResourceFileSpecification; all fields null");
                    System.out.println("Empty file would be written to zipped stream");
                    return false;
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

public class Transaction {

    static String currentVersion = "20090201.0.1";

    public String uuid;

    public String timestamp;

    public ArrayList<Triple> triples;

    public String fileName;

    public boolean isStored;

    public HashMap<String, ResourceFileSpecification> resourceFiles;

    public Transaction() {
        uuid = null;
        timestamp = null;
        triples = new ArrayList<Triple>();
        isStored = false;
        this.resetResourceFiles();
    }

    public void resetResourceFiles() {
        resourceFiles = new HashMap<String, ResourceFileSpecification>();
    }

    public void makeUUIDAndTimestamp() {
        uuid = Standards.newUUID();
        timestamp = Standards.getCurrentTimestamp();
    }

    public void print() {
        System.out.print("Transaction( ");
        System.out.println(uuid);
        System.out.println(timestamp);
        for (Triple triple : triples) {
            triple.print();
        }
        System.out.println(")");
    }

    public void search(Query query) {
        Iterator<Triple> iterator = triples.iterator();
        while (iterator.hasNext()) {
            Triple triple = iterator.next();
            if (query.isMatchForFilterTriple(triple)) query.addMatchingTriple(triple);
        }
    }

    public Triple addTripleForFields(String contextType, String contextData, String entityType, String entityData, String attributeType, String attributeData, String valueType, String valueData) {
        Triple triple = new Triple(this.uuid);
        triple.setFields(null, null, contextType, contextData, entityType, entityData, attributeType, attributeData, valueType, valueData);
        triples.add(triple);
        return triple;
    }

    public Triple addTransactionMetadata(String attributeData, String valueType, String valueData) {
        return addTripleForFields("PointrelTransactionMetadata", uuid, "PointrelTransaction", uuid, "PointrelTransactionMetadata", attributeData, valueType, valueData);
    }

    public Triple addTimestampToTransaction(String timestamp) {
        return addTransactionMetadata("timestamp", Standards.timestampType, timestamp);
    }

    public Triple addAuthorToTransaction(String authorReferenceType, String authorReference) {
        return addTransactionMetadata("author", authorReferenceType, authorReference);
    }

    public Triple addLicenseGrantToTransaction(String licenseReferenceType, String licenseReference) {
        return addTransactionMetadata("license", licenseReferenceType, licenseReference);
    }

    public Triple addTripleMetadata(String tripleUUID, String attributeData, String valueType, String valueData) {
        return addTripleForFields("PointrelTripleMetadata", tripleUUID, "PointrelTriple", tripleUUID, "PointrelTripleMetadata", attributeData, valueType, valueData);
    }

    public Triple addResourceFileMetadata(String transactionResourceFileReference, String attributeData, String valueType, String valueData) {
        return addTripleForFields("PointrelTransactionResourceFileMetadata", transactionResourceFileReference, "PointrelTransactionResourceFile", transactionResourceFileReference, "PointrelTransactionResourceFileMetadata", attributeData, valueType, valueData);
    }

    public String addResourceFile(String relativeFileName, File file) {
        String transactionResourceFileReference = ArchiveFileSupport.transactionResourceFileReference(this.uuid, relativeFileName);
        this.resourceFiles.put(transactionResourceFileReference, new ResourceFileSpecification(file));
        return transactionResourceFileReference;
    }

    public String addResourceFile(String relativeFileName, String filePath) {
        String transactionResourceFileReference = ArchiveFileSupport.transactionResourceFileReference(this.uuid, relativeFileName);
        this.resourceFiles.put(transactionResourceFileReference, new ResourceFileSpecification(filePath));
        return transactionResourceFileReference;
    }

    public String addResourceFile(String relativeFileName, byte[] bytes) {
        String transactionResourceFileReference = ArchiveFileSupport.transactionResourceFileReference(this.uuid, relativeFileName);
        this.resourceFiles.put(transactionResourceFileReference, new ResourceFileSpecification(bytes));
        return transactionResourceFileReference;
    }

    public void clearResourceFiles() {
        this.resetResourceFiles();
    }

    public boolean readTransactionMetadataAndTriplesFromUTF8Stream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        String line = null;
        try {
            line = reader.readLine();
            if (!line.equals("PointrelTransaction")) {
                System.out.println("expected PointrelTransaction");
                return false;
            }
            line = reader.readLine();
            if (!line.startsWith("version: ")) {
                System.out.println("expected version");
                return false;
            }
            String version = line.substring("version: ".length());
            if (!version.equals(currentVersion)) {
                System.out.println("expected version to match");
                return false;
            }
            line = reader.readLine();
            if (!line.startsWith("uuid: ")) {
                System.out.println("expected uuid: " + line);
                return false;
            }
            uuid = line.substring("uuid: ".length());
            line = reader.readLine();
            if (!line.startsWith("timestamp: ")) {
                System.out.println("expected timestamp: " + line);
                return false;
            }
            timestamp = line.substring("timestamp: ".length());
            line = reader.readLine();
            if (!line.equals("")) {
                System.out.println("expected blank line");
                return false;
            }
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    System.out.println("missing EndTransaction");
                    break;
                }
                if (line.equals("EndTransaction")) {
                    break;
                }
                if (!line.equals("Triple")) {
                    System.out.println("Expected Triple, got: " + line);
                    break;
                }
                Triple triple = new Triple(this.uuid);
                if (triple.readFromUTF8Stream(reader)) {
                    triples.add(triple);
                } else {
                    System.out.println("Problem reading triple");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean writeTransactionMetadataAndTriplesToUTF8Stream(OutputStream outputStream) {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
        try {
            writer.write("PointrelTransaction\n");
            writer.write("version: ");
            writer.write(currentVersion);
            writer.write("\n");
            writer.write("uuid: ");
            writer.write(uuid);
            writer.write("\n");
            writer.write("timestamp: ");
            String timestamp = this.timestamp;
            writer.write(timestamp);
            writer.write("\n");
            writer.write("\n");
            Iterator<Triple> iterator = triples.iterator();
            while (iterator.hasNext()) {
                Triple triple = iterator.next();
                triple.writeToUTF8Stream(writer);
            }
            writer.write("EndTransaction\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean retrieveTransactionFromArchiveDirectory(String transactionUUID, String archiveDirectory) {
        String filePath = new File(archiveDirectory, ArchiveFileSupport.fileNameInSubdirectoryForTransactionIdentifier(transactionUUID)).getAbsolutePath();
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            try {
                if (!this.readTransactionMetadataAndTriplesFromUTF8Stream(inputStream)) {
                    System.out.println("Something went wrong reading transaction metadata and triples");
                    return false;
                }
            } finally {
                inputStream.close();
            }
            ArrayList<String> files = ArchiveFileSupport.getVisibleFileListRecursively(archiveDirectory, transactionUUID, true);
            for (String transactionResourceFileReference : files) {
                File localFile = ArchiveFileSupport.localFileReferenceInArchiveDirectoryFromTransactionResourceFileReference(archiveDirectory, transactionResourceFileReference);
                this.resourceFiles.put(transactionResourceFileReference, new ResourceFileSpecification(localFile));
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean storeTransactionToArchiveDirectory(String archiveDirectory, boolean updateResourceFiles) {
        File transactionDataFile = new File(archiveDirectory, ArchiveFileSupport.fileNameInSubdirectoryForTransactionIdentifier(this.uuid));
        System.out.println("writing transaction to: " + transactionDataFile.getName());
        File parent = transactionDataFile.getParentFile();
        if (parent.exists()) {
            System.out.println("Transaction directory already exists: " + parent.getAbsolutePath());
            return false;
        }
        if (parent.mkdirs()) {
        }
        try {
            FileOutputStream transactionDataFileOutputStream = new FileOutputStream(transactionDataFile);
            try {
                this.writeTransactionMetadataAndTriplesToUTF8Stream(transactionDataFileOutputStream);
            } finally {
                transactionDataFileOutputStream.close();
            }
            for (String transactionResourceFileReference : this.resourceFiles.keySet()) {
                File destinationFile = ArchiveFileSupport.localFileReferenceInArchiveDirectoryFromTransactionResourceFileReference(archiveDirectory, transactionResourceFileReference);
                if (destinationFile.exists()) {
                    System.out.println("Transaction resource file already exists: " + destinationFile.getAbsolutePath());
                    continue;
                }
                destinationFile.getParentFile().mkdirs();
                ResourceFileSpecification resourceFileSpecification = this.resourceFiles.get(transactionResourceFileReference);
                resourceFileSpecification.copyTo(destinationFile, updateResourceFiles);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FileUtils.deleteQuietly(parent);
            this.resetResourceFiles();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            FileUtils.deleteQuietly(parent);
            this.resetResourceFiles();
            return false;
        }
        return true;
    }

    public boolean retrieveTransactionFromZippedStream(String transactionUUID, InputStream inputStream) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                if (!zipEntry.isDirectory()) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ArchiveFileSupport.copyInputStreamToOutputStream(zipInputStream, byteArrayOutputStream);
                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    this.resourceFiles.put(zipEntry.getName(), new ResourceFileSpecification(bytes));
                    if (zipEntry.getName().equals(ArchiveFileSupport.fileNameInSubdirectoryForTransactionIdentifier(transactionUUID))) {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                        if (!this.readTransactionMetadataAndTriplesFromUTF8Stream(byteArrayInputStream)) {
                            System.out.println("Something went wrong reading triples");
                            return false;
                        }
                        break;
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean storeTransactionToZippedStream(OutputStream outputStream, boolean updateResourceFiles) {
        System.out.println("Writing as a zipped transaction: " + this.uuid);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        try {
            zipOutputStream.setLevel(Deflater.DEFAULT_COMPRESSION);
            String relativeFileName = ArchiveFileSupport.fileNameInSubdirectoryForTransactionIdentifier(this.uuid);
            ZipEntry zipEntry = new ZipEntry(relativeFileName);
            try {
                zipOutputStream.putNextEntry(zipEntry);
                if (!this.writeTransactionMetadataAndTriplesToUTF8Stream(zipOutputStream)) {
                    return false;
                }
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            for (String transactionResourceFileReference : this.resourceFiles.keySet()) {
                ResourceFileSpecification resourceFileSpecification = this.resourceFiles.get(transactionResourceFileReference);
                zipEntry = new ZipEntry(transactionResourceFileReference);
                zipOutputStream.putNextEntry(zipEntry);
                if (!resourceFileSpecification.copyTo(zipOutputStream, updateResourceFiles)) {
                    return false;
                }
                zipOutputStream.closeEntry();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            this.resetResourceFiles();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            this.resetResourceFiles();
            return false;
        } finally {
            try {
                zipOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public byte[] bytesForResourceFileReference(String transactionResourceFileReference) {
        if (!this.resourceFiles.containsKey(transactionResourceFileReference)) {
            System.out.println("resource file reference not found in transaction: " + transactionResourceFileReference);
            return null;
        }
        ResourceFileSpecification resourceFileSpecification = this.resourceFiles.get(transactionResourceFileReference);
        return resourceFileSpecification.getBytes();
    }

    public boolean retrieveResourceFile(String transactionResourceFileReference, OutputStream outputStream) {
        if (!this.resourceFiles.containsKey(transactionResourceFileReference)) {
            System.out.println("resource file reference not found in transaction: " + transactionResourceFileReference);
            return false;
        }
        ResourceFileSpecification resourceFileSpecification = this.resourceFiles.get(transactionResourceFileReference);
        return resourceFileSpecification.retrieveResourceFile(outputStream);
    }

    public InputStream getInputStreamForResourceFile(String transactionResourceFileReference) {
        if (!this.resourceFiles.containsKey(transactionResourceFileReference)) {
            System.out.println("resource file reference not found in transaction: " + transactionResourceFileReference);
            return null;
        }
        ResourceFileSpecification resourceFileSpecification = this.resourceFiles.get(transactionResourceFileReference);
        return resourceFileSpecification.getInputStream();
    }
}
