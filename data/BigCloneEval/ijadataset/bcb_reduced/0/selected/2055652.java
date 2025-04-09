package com.htmli.compiler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class HTMLiDeployer {

    public static String version = "0.6";

    public static void main(String[] args) {
        try {
            System.out.println("Sync'ing..");
            sync();
            System.out.println("Jar'ing...");
            buildJar();
            System.out.println("Zip'ing...");
            buildZip();
            System.out.println("End");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private static void buildZip() throws IOException {
        OutputStream os = new FileOutputStream("build/htmli-" + version + ".zip");
        ZipOutputStream targetStream = new ZipOutputStream(os);
        targetStream.setMethod(ZipOutputStream.DEFLATED);
        addDirectoryToJar(new File("build/public/php"), targetStream, "htmli/php");
        addDirectoryToJar(new File("build/public/xsl"), targetStream, "htmli/xsl");
        addDirectoryToJar(new File("build/public/css"), targetStream, "htmli/css");
        addDirectoryToJar(new File("build/public/js"), targetStream, "htmli/js");
        addFileToJar(new File("build/README.TXT"), targetStream, "README.TXT");
        addFileToJar(new File("build/LICENSE-2.0.txt"), targetStream, "LICENSE-2.0.txt");
        targetStream.close();
    }

    private static void addFileToJar(File file, ZipOutputStream targetStream, String string) throws IOException {
        String dataFileName = string;
        InputStream is = new FileInputStream(file);
        BufferedInputStream sourceStream = new BufferedInputStream(is);
        ZipEntry theEntry = new ZipEntry(dataFileName);
        targetStream.putNextEntry(theEntry);
        byte[] data = new byte[1024];
        int bCnt;
        while ((bCnt = sourceStream.read(data, 0, 1024)) != -1) {
            targetStream.write(data, 0, bCnt);
        }
        targetStream.flush();
        targetStream.closeEntry();
        sourceStream.close();
    }

    private static void buildJar() throws IOException {
        OutputStream os = new FileOutputStream("build/htmli-" + version + ".jar");
        ZipOutputStream targetStream = new ZipOutputStream(os);
        targetStream.setMethod(ZipOutputStream.DEFLATED);
        addDirectoryToJar(new File("bin/com/htmli/compiler"), targetStream, "com/htmli/compiler");
        targetStream.close();
    }

    private static void addDirectoryToJar(File file, ZipOutputStream targetStream, String path) throws IOException {
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.getName().startsWith(".")) {
                continue;
            }
            if (f.isDirectory()) {
                addDirectoryToJar(f, targetStream, path + "/" + f.getName());
            } else {
                String dataFileName = path + "/" + f.getName();
                InputStream is = new FileInputStream(f);
                BufferedInputStream sourceStream = new BufferedInputStream(is);
                ZipEntry theEntry = new ZipEntry(dataFileName);
                targetStream.putNextEntry(theEntry);
                byte[] data = new byte[1024];
                int bCnt;
                while ((bCnt = sourceStream.read(data, 0, 1024)) != -1) {
                    targetStream.write(data, 0, bCnt);
                }
                targetStream.flush();
                targetStream.closeEntry();
                sourceStream.close();
            }
        }
    }

    private static void sync() throws IOException, ParserConfigurationException, TransformerException, SAXException {
        File htmliDir = new File("build/public");
        File jarDir = new File("src/com/htmli/public");
        BaseBuilder.sync(jarDir, htmliDir);
        BaseBuilder baseBuilder = new BaseBuilder(htmliDir);
        baseBuilder.build();
        LibraryBuilder builder = new LibraryBuilder(htmliDir, "std");
        builder.build();
    }
}
