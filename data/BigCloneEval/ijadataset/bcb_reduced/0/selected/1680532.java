package org.matsim.utils.vis.kml;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import org.matsim.utils.vis.kml.KMLWriter.XMLNS;

/**
 * A writer for complex keyhole markup files used by Google Earth. It supports
 * packing multiple kml-files into one zip-compressed file which can directly be
 * read by Google Earth. The files will have the ending *.kmz.
 *
 * @author mrieser
 *
 */
public class KMZWriter {

    private static final Logger log = Logger.getLogger(KMZWriter.class);

    private KMLWriter.XMLNS xmlNS = null;

    private BufferedWriter out = null;

    private ZipOutputStream zipOut = null;

    private final Map<String, String> nonKmlFiles = new HashMap<String, String>();

    /**
	 * Creates a new kmz-file and a writer for it and opens the file for writing.
	 *
	 * @param outFilename
	 *          the location of the file to be written.
	 */
    public KMZWriter(final String outFilename) {
        this(outFilename, KMLWriter.DEFAULT_XMLNS);
    }

    /**
	 * Creates a new kmz-file with the specified namespace/version and opens the
	 * file for writing.
	 *
	 * @param outFilename
	 *          the location of the file to be written.
	 * @param xmlNS
	 *          the version in which to write the entries.
	 *
	 * @see KMLWriter.XMLNS
	 */
    public KMZWriter(final String outFilename, final XMLNS xmlNS) {
        String filename = outFilename;
        if (filename.endsWith(".kml") || filename.endsWith(".kmz")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        this.xmlNS = xmlNS;
        try {
            this.zipOut = new ZipOutputStream(new FileOutputStream(filename + ".kmz"));
            this.out = new BufferedWriter(new OutputStreamWriter(this.zipOut, "UTF8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        KML docKml = new KML();
        Document docDoc = new Document("link to main document");
        docKml.setFeature(docDoc);
        NetworkLink nl = new NetworkLink("mainlink", new Link("main.kml"));
        docKml.setFeature(nl);
        writeKml("doc.kml", docKml);
    }

    /**
	 * Adds the specified KML-object to the file.
	 *
	 * @param filename
	 *          The internal filename of this kml-object in the kmz-file. Other
	 *          kml-objects in the same kmz-file can reference this kml with the
	 *          specified filename.
	 * @param kml
	 *          The KML-object to store in the file.
	 */
    public void writeLinkedKml(final String filename, final KML kml) {
        if (filename.equals("doc.kml")) {
            throw new IllegalArgumentException("The filename 'doc.kml' is reserved for the primary kml.");
        }
        if (filename.equals("main.kml")) {
            throw new IllegalArgumentException("The filename 'main.kml' is reserved for the main kml.");
        }
        writeKml(filename, kml);
    }

    /**
	 * Writes the specified KML-object as the main kml into the file. The main kml
	 * is the one Google Earth reads when the file is opened. It should contain
	 * {@link NetworkLink NetworkLinks} to the other KMLs stored in the same file.
	 *
	 * @param kml
	 *          the KML-object that will be read by Google Earth when opening the
	 *          file.
	 */
    public void writeMainKml(final KML kml) {
        writeKml("main.kml", kml);
    }

    /**
	 * Closes this file for writing.
	 */
    public void close() {
        try {
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Adds a file to the kmz which is not a kml file
	 * @param filename the path to the file, relative or absolute
	 * @param inZipFilename the filename used for the file in the kmz file
	 * @throws IOException
	 */
    public void addNonKMLFile(final String filename, final String inZipFilename) throws IOException {
        if (this.nonKmlFiles.containsKey(filename) && (inZipFilename.compareTo(this.nonKmlFiles.get(filename)) == 0)) {
            log.warn("File: " + filename + " is already included in the kmz as " + inZipFilename);
            return;
        }
        this.nonKmlFiles.put(filename, inZipFilename);
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(filename);
            byte[] buffer = new byte[1024];
            int bytesRead;
            ZipEntry entry = new ZipEntry(inZipFilename);
            this.zipOut.putNextEntry(entry);
            while ((bytesRead = inStream.read(buffer)) != -1) {
                this.zipOut.write(buffer, 0, bytesRead);
            }
            log.info(entry.getName() + " added to kmz.");
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    /**
	 * Adds a file (in form of a byte array) to a kml file.
	 * @param file
	 * @param inZipFilename inZipFilename the filename used for the file in the kmz file
	 * @throws IOException
	 */
    public void addNonKMLFile(final byte[] file, final String inZipFilename) throws IOException {
        ZipEntry entry = new ZipEntry(inZipFilename);
        this.zipOut.putNextEntry(entry);
        this.zipOut.write(file);
        log.info(entry.getName() + " added to kmz.");
    }

    /**
	 * internal routine that does the real writing of the data
	 *
	 * @param filename
	 * @param kml
	 */
    private void writeKml(final String filename, final KML kml) {
        try {
            ZipEntry ze = new ZipEntry(filename);
            ze.setMethod(ZipEntry.DEFLATED);
            this.zipOut.putNextEntry(ze);
            this.writeXMLDeclaration(this.out);
            int offset = 0;
            String offsetString = "  ";
            kml.writeKML(this.out, this.xmlNS, offset, offsetString, this.xmlNS);
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeXMLDeclaration(final BufferedWriter out) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.newLine();
    }
}
