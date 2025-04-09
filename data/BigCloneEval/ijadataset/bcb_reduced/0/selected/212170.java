package be.fedict.eid.applet.service.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A "light" implementation of OGC standard KML 2.2
 * This class merely creates the KMZ zip package
 *
 * @see http://www.opengeospatial.org/standards/kml/
 * @author Bart Hanssens
 */
public class KmlLight {

    public static final String MIME_TYPE = "application/vnd.google-earth.kmz";

    private ZipOutputStream kmz;

    /**
     * Add an image (photo) to the KMZ zip
     *
     * @param image
     * @throws IOException
     */
    public void addImage(byte[] image) throws IOException {
        ZipEntry zImage = new ZipEntry("photo.jpg");
        kmz.putNextEntry(zImage);
        kmz.write(image);
        kmz.closeEntry();
    }

    /**
     * Add the KML file to the KMZ zip
     *
     * @param doc KML document
     * @throws IOException
     */
    public void addKmlFile(byte[] doc) throws IOException {
        ZipEntry zKml = new ZipEntry("data.kml");
        kmz.putNextEntry(zKml);
        kmz.write(doc);
        kmz.closeEntry();
    }

    /**
     * Close the KMZ zip file
     *
     * @throws IOException
     */
    public void close() throws IOException {
        kmz.close();
    }

    /**
     * Constructor
     *
     * @param outStream
     */
    public KmlLight(OutputStream outStream) {
        kmz = new ZipOutputStream(outStream);
    }
}
