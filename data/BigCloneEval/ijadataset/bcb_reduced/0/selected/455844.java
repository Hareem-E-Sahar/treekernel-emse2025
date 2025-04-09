package translator;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class mozJarWriter extends Writer {

    private String zipPrefix;

    private String targetLanguage;

    private File zipFile;

    private FileOutputStream fos;

    private ZipOutputStream zos;

    private BufferedOutputStream bos;

    private String currentEntryFileName;

    private int fileType;

    private static final int FILETYPE_PROPERTIES = 1;

    private static final int FILETYPE_DTD = 7;

    private static final int FILETYPE_UNKNOWN = 8;

    private File tempFile;

    private FileOutputStream tempos;

    private OutputStreamWriter osw;

    private BufferedWriter bw;

    private Properties propsFile;

    private contentRdfWriter myContentRdfWriter;

    public mozJarWriter(String install, String destination) {
        super(install, destination);
        myContentRdfWriter = new contentRdfWriter();
        try {
            zipPrefix = "locale/" + destination.substring(destination.length() - 9, destination.length() - 4);
            zipFile = new File(destination);
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            bos = new BufferedOutputStream(zos);
        } catch (Exception e) {
            System.out.println("Error writing jar file ");
            System.exit(1);
        }
    }

    public String getTargetLanguageCode() {
        return targetLanguage;
    }

    public void setTargetLanguageCode(String trgtLanguageCode) {
        targetLanguage = trgtLanguageCode;
    }

    public void exportFile(TranslationFile file) {
        try {
            currentEntryFileName = zipPrefix + "/" + file.getComponentPath() + file.getFileName();
            tempFile = File.createTempFile("MTT_", null);
            tempFile.deleteOnExit();
            tempos = new FileOutputStream(tempFile);
            if (currentEntryFileName.endsWith(".properties")) {
                setupWritePropertiesFile();
            } else if (currentEntryFileName.endsWith(".dtd")) {
                setupWriteDTDFile();
            } else if (currentEntryFileName.endsWith(".rdf")) {
                createRDFFile(file);
            } else {
            }
        } catch (Exception e) {
            System.out.println("Error in exporting file");
            System.exit(1);
        }
    }

    public void exportPhrase(Phrase phrase) {
        if (currentEntryFileName.endsWith(".properties")) {
            writeToPropertiesFile(phrase);
        } else if (currentEntryFileName.endsWith(".dtd")) {
            writeToDTDFile(phrase);
        }
    }

    public void concludeExportFile() {
        try {
            if (currentEntryFileName.endsWith(".properties")) {
                propsFile.store(tempos, "");
            } else if (currentEntryFileName.endsWith(".dtd")) {
                bw.close();
            } else if (currentEntryFileName.endsWith(".rdf")) {
                bw.close();
            }
            copyFile(tempFile, currentEntryFileName);
            tempFile.delete();
        } catch (Exception e) {
            System.out.println("Exception in closing up export file");
            System.exit(1);
        }
    }

    public void prepareExportFile() {
    }

    public void close() {
        try {
            bos.close();
        } catch (Exception e) {
            System.out.println("Exception in closing");
            System.exit(1);
        }
    }

    private void setupWritePropertiesFile() {
        propsFile = new Properties();
    }

    private void setupWriteDTDFile() {
        try {
            osw = new OutputStreamWriter(tempos, "UTF-8");
            bw = new BufferedWriter(osw);
            bw.newLine();
        } catch (Exception e) {
            System.out.println("Exception occurred in setting up DTD file export");
            System.exit(1);
        }
    }

    private void writeToPropertiesFile(Phrase phrase) {
        propsFile.setProperty(phrase.getKey(), phrase.getText());
    }

    private void writeToDTDFile(Phrase phrase) {
        String line = "";
        try {
            if ((phrase.getText()).indexOf("\"") == -1) {
                line = "<!ENTITY " + phrase.getKey() + " \"" + phrase.getText() + "\">";
            } else {
                line = "<!ENTITY " + phrase.getKey() + " '" + phrase.getText() + "'>";
            }
            bw.write(line, 0, line.length());
            bw.newLine();
        } catch (Exception e) {
            System.out.println("Exception occured in writing dtd entry");
            System.exit(1);
        }
    }

    private void createRDFFile(TranslationFile file) {
        try {
            osw = new OutputStreamWriter(tempos, "UTF-8");
            bw = new BufferedWriter(osw);
            String rdfString = myContentRdfWriter.getContentRdfString(file);
            bw.write(rdfString);
        } catch (Exception e) {
            System.out.println("Exception occured in writing content rdf entry");
            System.exit(1);
        }
    }

    private void copyFile(File tempFile, String eName) throws IOException {
        ZipEntry ze;
        FileInputStream fis;
        BufferedInputStream bis;
        boolean theEnd;
        int trans;
        ze = new ZipEntry(eName);
        zos.putNextEntry(ze);
        fis = new FileInputStream(tempFile);
        bis = new BufferedInputStream(fis);
        theEnd = false;
        while (!theEnd) {
            trans = bis.read();
            if (trans == -1) {
                theEnd = true;
            } else {
                bos.write(trans);
            }
        }
        bis.close();
        bos.flush();
    }
}
