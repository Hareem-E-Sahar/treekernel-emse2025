package org.ea;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Jarcrypt_cli {

    private static String outputFile = null;

    private static String inputFile = null;

    /**
	 * This program will with help of the java zip library and
	 * javax.crypt create a jar file with an encrypted datafile included
	 * inside. The file should be able to decrypt by itself.
	 * 
	 * @param args
	 */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("JarCrypt 0.1");
            System.out.println("");
            System.out.println("Usage: java -jar jarcrypt.jar [output jar] [input file]");
            System.exit(0);
        }
        outputFile = args[0];
        inputFile = args[1];
        ZipOutputStream myZip = new ZipOutputStream(new FileOutputStream(outputFile));
        myZip.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        OutputStreamWriter osw = new OutputStreamWriter(myZip);
        osw.write("Manifest-Version: 1.0\n");
        osw.write("Main-Class: org.ea.Extractor\n");
        osw.write("Created-By: Woden\n");
        osw.flush();
        myZip.closeEntry();
        writeJarFile(myZip);
        writeDataFile(myZip, inputFile);
        myZip.close();
    }

    private static void writeDataFile(ZipOutputStream myZip, String inputFile) throws Exception {
        final int BUFFER = 4096;
        int count;
        byte data[] = new byte[BUFFER];
        ByteArrayOutputStream myBuffer = new ByteArrayOutputStream();
        ZipOutputStream myDataZip = new ZipOutputStream(myBuffer);
        myDataZip.putNextEntry(new ZipEntry(inputFile));
        FileInputStream myDataFile = new FileInputStream(inputFile);
        while ((count = myDataFile.read(data, 0, BUFFER)) != -1) {
            myDataZip.write(data, 0, count);
        }
        myDataZip.closeEntry();
        myDataZip.close();
        myZip.putNextEntry(new ZipEntry("data"));
        ByteArrayInputStream myBufferReader = new ByteArrayInputStream(myBuffer.toByteArray());
        int j = 0;
        while ((j = myBufferReader.read()) != -1) {
            myZip.write(j);
        }
        myZip.closeEntry();
    }

    private static void writeJarFile(ZipOutputStream myZip) throws Exception {
        myZip.putNextEntry(new ZipEntry("org/ea/Extractor.class"));
        String p = System.getProperty("java.class.path");
        ZipInputStream myJar = new ZipInputStream(new FileInputStream(p));
        ZipEntry ze = null;
        while ((ze = myJar.getNextEntry()) != null) {
            if (ze.getName().compareTo("org/ea/Extractor.class") == 0) {
                int i = 0;
                while ((i = myJar.read()) != -1) {
                    myZip.write(i);
                }
            }
        }
        myJar.close();
        myZip.closeEntry();
    }
}
