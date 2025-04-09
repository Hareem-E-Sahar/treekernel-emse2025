package com.incendiaryblue.util.zip;

import java.io.*;
import java.util.zip.*;

/**
 *	Provides functionality for zipping a given input stream or file.
 */
public class Zipper {

    /**	File to be compressed. */
    private File inputFile;

    /**	Archive file. */
    private File outputFile;

    /**
	 *	Constructs a new Zipper object that compresses the contents of the
	 *	given input file and writes the results to the given output file.
	 *
	 *	@param	inputFile	File containing data to be compressed.
	 *	@param	outputFile	File to which compressed data will be written.
	 */
    public Zipper(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void compress() throws IOException {
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(this.outputFile));
        ZipEntry zipEntry = new ZipEntry(this.inputFile.getName());
        FileInputStream fileInputStream;
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1000];
        int numBytes;
        zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
        fileInputStream = new FileInputStream(this.inputFile);
        while ((numBytes = fileInputStream.read(bytes)) > -1) {
            crc.update(bytes, 0, numBytes);
        }
        fileInputStream.close();
        zipEntry.setSize(this.inputFile.length());
        zipEntry.setTime(this.inputFile.lastModified());
        zipEntry.setCrc(crc.getValue());
        zipEntry.setComment("Product File");
        zipOutputStream.putNextEntry(zipEntry);
        fileInputStream = new FileInputStream(this.inputFile);
        while ((numBytes = fileInputStream.read(bytes)) > -1) {
            zipOutputStream.write(bytes, 0, numBytes);
        }
        fileInputStream.close();
        zipOutputStream.closeEntry();
        zipOutputStream.finish();
    }

    /**
	 *	Command-line entry point. Zips the given file.
	 *	<P>
	 *	Arguments are:
	 *	<OL>
	 *	<LI>Name of the file to zipper</LI>
	 *	<LI>Name of the ZIP archive to be created</LI>
	 *	</OL>
	 */
    public static void main(String args[]) throws IOException {
        File inputFile = new File(args[0]);
        File outputFile = null;
        Zipper zipper;
        outputFile = new File(args[1]);
        zipper = new Zipper(inputFile, outputFile);
        zipper.compress();
    }
}
