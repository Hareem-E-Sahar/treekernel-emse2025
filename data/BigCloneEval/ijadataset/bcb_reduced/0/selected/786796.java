package org.matsim.utils.vis.kml;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class KMLWriter {

    private boolean useCompression;

    private String outFilename;

    private BufferedWriter out;

    private KML kml;

    public enum XMLNS {

        V_20("http://earth.google.com/kml/2.0"), V_21("http://earth.google.com/kml/2.1");

        private String xmlNSURL;

        private XMLNS(String xmlNS) {
            this.xmlNSURL = xmlNS;
        }

        @Override
        public String toString() {
            return this.xmlNSURL;
        }
    }

    private XMLNS xmlNS;

    public static final String DEFAULT_OUTDTD = "";

    public static final XMLNS DEFAULT_XMLNS = XMLNS.V_21;

    public static final boolean DEFAULT_COMPRESSION = false;

    public KMLWriter(KML kml, final String outFilename) {
        this(kml, outFilename, KMLWriter.DEFAULT_XMLNS, KMLWriter.DEFAULT_COMPRESSION);
    }

    /**
	 * @param kml The KML object.
	 * @param outFilename Name of the KML output file.
	 * @param xmlNS final XMLNS object that represents the KML version of the output file.
	 * @param useCompression
	 */
    public KMLWriter(KML kml, final String outFilename, final XMLNS xmlNS, final boolean useCompression) {
        this.kml = kml;
        this.outFilename = outFilename;
        this.xmlNS = xmlNS;
        this.useCompression = useCompression;
    }

    public void write() {
        try {
            if (!this.useCompression) {
                this.out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outFilename), "UTF8"));
            } else {
                this.outFilename = this.outFilename.substring(0, this.outFilename.length() - 4);
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(this.outFilename + ".kmz"));
                this.out = new BufferedWriter(new OutputStreamWriter(zipOut, "UTF8"));
                ZipEntry ze = new ZipEntry(this.outFilename + ".kml");
                ze.setMethod(ZipEntry.DEFLATED);
                zipOut.putNextEntry(ze);
            }
            this.writeXMLDeclaration(this.out);
            int offset = 0;
            String offsetString = "  ";
            this.kml.writeKML(this.out, this.xmlNS, offset, offsetString, this.xmlNS);
            this.out.flush();
            this.out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeXMLDeclaration(final BufferedWriter out) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.newLine();
    }
}
