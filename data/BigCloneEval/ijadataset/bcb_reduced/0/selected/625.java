package uk.ac.rdg.resc.ncwms.graphics;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.ncwms.util.WmsUtils;
import uk.ac.rdg.resc.ncwms.wms.Layer;

/**
 * Creates KMZ files for importing into Google Earth.  Only one instance of this class
 * will ever be created, so this class contains no member variables to ensure
 * thread safety.
 * @todo Would this be better handled by a JSP?
 *
 * @author Jon Blower
 * $Revision$
 * $Date$
 * $Log$
 */
public class KmzFormat extends ImageFormat {

    private static final Logger logger = LoggerFactory.getLogger(KmzFormat.class);

    private static final String PICNAME = "frame";

    private static final String PICEXT = "png";

    private static final String COLOUR_SCALE_FILENAME = "legend.png";

    /**
     * Writes the given list of {@link java.awt.BufferedImage}s to the given
     * OutputStream.
     * @param frames List of BufferedImages to render into an image
     * @param out The OutputStream to which the image will be written
     * @param layer the Layer object representing the image(s)
     * @param tValues List of Strings representing the time values, one for each frame
     * @param zValue The elevation value representing the image(s)
     * @param bbox The bounding box of the image(s)
     * @param legend A legend image (this will be null unless this.requiresLegend()
     * returns true.
     * @throws IOException if there was an error writing to the output stream
     * @throws IllegalArgumentException if this ImageFormat cannot render all
     * of the given BufferedImages.
     */
    @Override
    public void writeImage(List<BufferedImage> frames, OutputStream out, Layer layer, List<String> tValues, String zValue, BoundingBox bbox, BufferedImage legend) throws IOException {
        StringBuffer kml = new StringBuffer();
        for (int frameIndex = 0; frameIndex < frames.size(); frameIndex++) {
            if (frameIndex == 0) {
                kml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                kml.append(System.getProperty("line.separator"));
                kml.append("<kml xmlns=\"http://earth.google.com/kml/2.0\">");
                kml.append("<Folder>");
                kml.append("<visibility>1</visibility>");
                kml.append("<name>" + layer.getDataset().getId() + ", " + layer.getId() + "</name>");
                kml.append("<description>" + layer.getDataset().getTitle() + ", " + layer.getTitle() + ": " + layer.getLayerAbstract() + "</description>");
                kml.append("<ScreenOverlay>");
                kml.append("<name>Colour scale</name>");
                kml.append("<Icon><href>" + COLOUR_SCALE_FILENAME + "</href></Icon>");
                kml.append("<overlayXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<screenXY x=\"0\" y=\"1\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<rotationXY x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("<size x=\"0\" y=\"0\" xunits=\"fraction\" yunits=\"fraction\"/>");
                kml.append("</ScreenOverlay>");
            }
            kml.append("<GroundOverlay>");
            String timestamp = null;
            String z = null;
            if (tValues.get(frameIndex) != null && !tValues.get(frameIndex).equals("")) {
                DateTime dt = WmsUtils.iso8601ToDateTime(tValues.get(frameIndex), layer.getChronology());
                timestamp = WmsUtils.dateTimeToISO8601(dt);
                kml.append("<TimeStamp><when>" + timestamp + "</when></TimeStamp>");
            }
            if (zValue != null && !zValue.equals("") && layer.getElevationValues() != null) {
                z = "";
                if (timestamp != null) z += "<br />";
                z += "Elevation: " + zValue + " " + layer.getElevationUnits();
            }
            kml.append("<name>");
            if (timestamp == null && z == null) {
                kml.append("Frame " + frameIndex);
            } else {
                kml.append("<![CDATA[");
                if (timestamp != null) kml.append("Time: " + timestamp);
                if (z != null) kml.append(z);
                kml.append("]]>");
            }
            kml.append("</name>");
            kml.append("<visibility>1</visibility>");
            kml.append("<Icon><href>" + getPicFileName(frameIndex) + "</href></Icon>");
            kml.append("<LatLonBox id=\"" + frameIndex + "\">");
            kml.append("<west>" + bbox.getMinX() + "</west>");
            kml.append("<south>" + bbox.getMinY() + "</south>");
            kml.append("<east>" + bbox.getMaxX() + "</east>");
            kml.append("<north>" + bbox.getMaxY() + "</north>");
            kml.append("<rotation>0</rotation>");
            kml.append("</LatLonBox>");
            kml.append("</GroundOverlay>");
        }
        kml.append("</Folder>");
        kml.append("</kml>");
        ZipOutputStream zipOut = new ZipOutputStream(out);
        logger.debug("Writing KML file to KMZ file");
        ZipEntry kmlEntry = new ZipEntry(layer.getDataset().getId() + "_" + layer.getId() + ".kml");
        kmlEntry.setTime(System.currentTimeMillis());
        zipOut.putNextEntry(kmlEntry);
        zipOut.write(kml.toString().getBytes());
        int frameIndex = 0;
        logger.debug("Writing frames to KMZ file");
        for (BufferedImage frame : frames) {
            ZipEntry picEntry = new ZipEntry(getPicFileName(frameIndex));
            frameIndex++;
            zipOut.putNextEntry(picEntry);
            ImageIO.write(frame, PICEXT, zipOut);
        }
        logger.debug("Constructing colour scale image");
        ZipEntry scaleEntry = new ZipEntry(COLOUR_SCALE_FILENAME);
        zipOut.putNextEntry(scaleEntry);
        logger.debug("Writing colour scale image to KMZ file");
        ImageIO.write(legend, PICEXT, zipOut);
        zipOut.close();
    }

    /**
     * @return the name of the picture file with the given index
     */
    private static final String getPicFileName(int frameIndex) {
        return PICNAME + frameIndex + "." + PICEXT;
    }

    @Override
    public String getMimeType() {
        return "application/vnd.google-earth.kmz";
    }

    @Override
    public boolean supportsMultipleFrames() {
        return true;
    }

    @Override
    public boolean supportsFullyTransparentPixels() {
        return true;
    }

    @Override
    public boolean supportsPartiallyTransparentPixels() {
        return true;
    }

    @Override
    public boolean requiresLegend() {
        return true;
    }
}
