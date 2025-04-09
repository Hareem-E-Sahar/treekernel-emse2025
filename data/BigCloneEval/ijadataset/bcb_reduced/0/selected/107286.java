package ebgeo.maprequest;

import java.util.*;
import java.awt.image.*;
import java.io.*;
import java.awt.*;
import javax.imageio.*;
import javax.swing.*;
import org.apache.commons.logging.*;
import ebgeo.maprequest.cs.*;
import org.geotools.pt.*;
import org.geotools.cs.*;

/**
 * The super class for map services.
 */
public abstract class MapService implements Comparable {

    /** The coordinate system for this map. */
    protected ebgeo.maprequest.cs.MRCoordinateSystem cs;

    /** True if this map supports panning. */
    protected boolean supportsPanning = true;

    /** True if this map supports zooming. */
    protected boolean supportsZooming = true;

    /** The locale of the map (used for {@link #getAddText()}). */
    protected Locale locale = new Locale("en");

    /** Text to add to the bottom of a map. */
    protected String addText = null;

    /** The short name of this map. */
    private String shortName = null;

    /** The root of all layers. */
    protected Layer rootLayer = null;

    /** Creates a new instance of MapService */
    public MapService() {
    }

    /**
     * Construct a map service from some properties. This is the normal way
     * the system constructs a map service.
     *
     * @param props The properties for this service.
     **/
    public MapService(Properties props) {
    }

    /**
     * Get the description of this driver.
     *
     * @return Returns the description of this driver.
     **/
    public abstract String getDriverDescription();

    /**
     * Get the description of the map. Some drivers may be able to be used
     * for different maps, so this method identifies the map that the driver
     * is being used for.
     **/
    public abstract String getMapDescription();

    /**
     * Get a URL giving information about the map and/or web site.
     **/
    public abstract String getMapInfoURL();

    /**
     * Check if this service has been configured for the map.
     **/
    public abstract boolean isConfigured();

    /**
     * Instruct the service that it should configure itself for its map. The
     * service returns true if it suceeded in configuring itself, or false if
     * not.
     **/
    public abstract boolean configure();

    /**
     * Get the list of layers to display.
     **/
    public abstract Vector getLayers();

    /**
     * Set the list of layers to display.
     **/
    public void setLayers(Vector layers) {
        rootLayer = new Layer("ROOT", "ROOT", true);
        for (int loop = 0; loop < layers.size(); loop++) rootLayer.addSubLayer((Layer) layers.get(loop));
    }

    /**
     * Get the minimum valid X value for this map.
     **/
    public abstract double getMinXExtent();

    /**
     * Get the minimum valid Y value for this map.
     **/
    public abstract double getMinYExtent();

    /**
     * Get the maximum valid X value for this map.
     **/
    public abstract double getMaxXExtent();

    /**
     * Get the maximum valid Y value for this map.
     **/
    public abstract double getMaxYExtent();

    /**
     * Get the suggested initial minimum X value for this map.
     **/
    public abstract double getInitialMinX();

    /**
     * Get the suggested initial maximum X value for this map.
     **/
    public abstract double getInitialMaxX();

    /**
     * Get the suggested initial minimum Y value for this map.
     **/
    public abstract double getInitialMinY();

    /**
     * Get the suggested initial maximum Y value for this map.
     **/
    public abstract double getInitialMaxY();

    /**
     * Check that the map image parameters are valid for this map.
     *
     * @param params The requested map image parameters.
     * @return Returns the closest possible valid map image parameters to
     *   the parameters requested.
     **/
    public abstract MapImageParameters validateMapImageParameters(MapImageParameters params);

    /**
     * Create a map image for the requested map. When finished, this method
     * should call {@link MainWindow#mapDone()}, or
     * {@link MainWindow#mapFailed(String)} if there was an error.
     *
     * @param image The image to draw the map on.
     * @param tempDirectory A temporary directory that can be used
     *    to write temporary files to.
     * @param parentComponent A component to send repaint() requests to as
     *    each map segment is made available.
     * @param progress A progress bar to update.
     * @param main The main window. When the map has been requested, {@link MainWindow#mapDone()} is called.
     **/
    public abstract void buildUpMap(BufferedImage image, String tempDirectory, Component parentComponent, BoundedRangeModel progress, MainWindow main);

    /**
     * If this object is currently building up a map (because {@link
     * #buildUpMap(BufferedImage,String,Component,BoundedRangeModel,MainWindow)}
     * was called) then calling this method instructs the map service to stop
     * building up a map.
     **/
    public abstract void stopBuildUpMap();

    /**
     * Get the country that this map is from. Returns an empty string
     * if this map is from more than one country.
     **/
    public abstract String getCountry();

    /**
     * Get the state or province that this map is from. Returns an empty
     * string if this map is from more than one state or province, or if
     * this map is not from any state or province.
     **/
    public abstract String getState();

    /**
     * Get the width of the map image.
     **/
    public abstract int getImageWidth();

    /**
     * Get the height of the map image.
     **/
    public abstract int getImageHeight();

    /**
     * Set the area of map and size to draw.
     **/
    public abstract void setMapImageParameters(MapImageParameters imageParams);

    /**
     * Get the area of map and size to draw.
     **/
    public abstract MapImageParameters getMapImageParameters();

    /**
     * Check if any layers are selected.
     *
     * @return Returns true if one or more layers are selected (and thus a
     *   map could be drawn), or false if no layers are selected (and thus a
     *   map could not be drawn).
     **/
    public abstract boolean isAnyLayersSelected();

    /**
     * Write the map image to a file.
     **/
    public boolean writeImage(BufferedImage imageOut, String outputDirectory, String outputFilename) {
        boolean success = true;
        Log log = LogFactory.getLog(this.getClass());
        String format = outputFilename.substring(outputFilename.lastIndexOf(".") + 1, outputFilename.length());
        try {
            log.info("Writing output file " + outputFilename);
            String filename = outputDirectory + File.separator + outputFilename;
            File out = new File(filename);
            log.info("Writing a " + format + " image to " + filename);
            ImageIO.write(imageOut, format, out);
        } catch (Exception e) {
            log.error("Error writing image", e);
            success = false;
        }
        return success;
    }

    /**
     * Extract the whole number of degrees from a floating point amount.
     *
     * @return Returns the absolute whole number of degrees. e.g. -30.2 returns
     *   30. 45.1 returns 45.
     **/
    public int extractDegrees(double degrees) {
        if (degrees < 0.0) degrees *= (double) -1.0;
        return (int) degrees;
    }

    /**
     * Extract the number of minutes from a floating point amount of degrees.
     * 
     * @return Returns only the fractional amount of degrees expressed as
     *   a floating point amount of minutes. e.g. -30.5 degrees will return
     *   30.0 minutes.
     **/
    public double extractMinutes(double degrees) {
        if (degrees < 0.0) degrees *= (double) -1.0;
        degrees -= (double) extractDegrees(degrees);
        return (degrees * (double) 60.0);
    }

    /**
     * Write a calibration file for an external GIS system.
     *
     * @param system The system to write the calibration file to. At present
     *   only "oziexplorer" is supported.
     * @param directory The directory to write the file to.
     * @param imageFilename The file name of the associated map image.
     * @param mapFilename The name of the file to write the calibration
     *   information to.
     **/
    public boolean writeMapFile(String system, String directory, String imageFilename, String mapFilename) {
        if (system.equalsIgnoreCase("oziexplorer")) return writeOziMapFile(directory, imageFilename, mapFilename); else return false;
    }

    /**
     * Write a calibration file for the current image suitable for Ozi Explorer.
     *
     * @deprecated Use {@link #writeMapFile(String,String,String,String)
     *   writeMapFile("oziexplorer",directory,imageFilename,mapFilename)}
     *   instead.
     **/
    public boolean writeMapFile(String directory, String imageFilename, String mapFilename) {
        return writeMapFile("oziexplorer", directory, imageFilename, mapFilename);
    }

    /**
     * Write a calibration file for the current image suitable for Ozi Explorer.
     **/
    protected boolean writeOziMapFile(String directory, String imageFilename, String mapFilename) {
        boolean success = true;
        MapImageParameters imageParams = getMapImageParameters();
        if (imageParams == null) return false;
        double minx = imageParams.getMinX();
        double miny = imageParams.getMinY();
        double maxx = imageParams.getMaxX();
        double maxy = imageParams.getMaxY();
        int width = imageParams.getWidth();
        int height = imageParams.getHeight();
        double unitMultiplier = 1.0d;
        boolean useLatLongForGrid = false;
        if (cs.getProjection().hasNameForRealm(MRCoordinateSystem.REALM_OZI_EXPLORER_UNIT_MULTIPLIER)) {
            unitMultiplier = Double.parseDouble(cs.getProjection().getName(MRCoordinateSystem.REALM_OZI_EXPLORER_UNIT_MULTIPLIER));
        }
        if (cs.getProjection().hasNameForRealm(MRCoordinateSystem.REALM_OZI_EXPLORER_USE_LATLONG_FOR_GRID)) {
            useLatLongForGrid = new Boolean(cs.getProjection().getName(MRCoordinateSystem.REALM_OZI_EXPLORER_USE_LATLONG_FOR_GRID)).booleanValue() && cs.isKnownCoordinateSystem();
        }
        try {
            FileWriter out = new FileWriter(directory + File.separator + mapFilename, false);
            out.write("OziExplorer Map Data File Version 2.2\r\n");
            out.write(imageFilename + "\r\n");
            out.write(directory + File.separator + imageFilename + "\r\n");
            out.write("1 ,Map Code,\r\n");
            out.write(cs.getDatum().getName(cs.REALM_OZI_EXPLORER) + "," + cs.getDatum().getName(cs.REALM_OZI_EXPLORER_CODE) + ",   0.0000,   0.0000," + cs.getDatum().getName(cs.REALM_OZI_EXPLORER_CODE) + "\r\n");
            out.write("Reserved 1\r\n");
            out.write("Reserved 2\r\n");
            out.write("Magnetic Variation,,,E\r\n");
            out.write("Map Projection," + cs.getProjection().getName(cs.REALM_OZI_EXPLORER) + ",PolyCal,No,AutoCalOnly,No,BSBUseWPX,No\r\n");
            CoordinatePoint wgs84 = null;
            double x;
            double y;
            CoordinatePoint point;
            if (cs.getProjection().isLatLong() || useLatLongForGrid) {
                if (useLatLongForGrid) {
                    point = cs.convertToLatLong(new CoordinatePoint(minx, maxy));
                    x = point.getOrdinate(0);
                    y = point.getOrdinate(1);
                } else {
                    x = minx;
                    y = maxy;
                }
                out.write("Point01,xy,0,0,in, deg," + extractDegrees(y) + "," + extractMinutes(y) + "," + ((y > 0) ? "N" : "S") + "," + extractDegrees(x) + "," + extractMinutes(x) + "," + ((x > 0) ? "E" : "W") + "," + "grid, , , ,\r\n");
                if (useLatLongForGrid) {
                    point = cs.convertToLatLong(new CoordinatePoint(maxx, miny));
                    x = point.getOrdinate(0);
                    y = point.getOrdinate(1);
                } else {
                    x = maxx;
                    y = miny;
                }
                out.write("Point02,xy," + (width - 1) + "," + (height - 1) + ",in, deg," + extractDegrees(y) + "," + extractMinutes(y) + "," + ((y > 0) ? "N" : "S") + "," + extractDegrees(x) + "," + extractMinutes(x) + "," + ((x > 0) ? "E" : "W") + "," + "grid, , , ,\r\n");
            } else {
                String zone;
                String hemisphere;
                point = new CoordinatePoint(minx, maxy);
                zone = cs.getProjection().getZone(point);
                if (cs.getProjection().getName().equalsIgnoreCase("bng")) {
                    x = BritishGrid.stripMajorDigit(minx);
                    y = BritishGrid.stripMajorDigit(maxy);
                } else {
                    x = minx;
                    y = maxy;
                }
                if (cs.isKnownCoordinateSystem()) {
                    wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(minx, maxy));
                    hemisphere = (wgs84.getOrdinate(1) >= 0.0) ? "N" : "S";
                } else {
                    hemisphere = cs.getProjection().getHemisphere();
                }
                out.write("Point01,xy,0,0,in, deg, , , , , , , grid," + zone + "," + (x * unitMultiplier) + "," + (y * unitMultiplier) + "," + hemisphere + "\r\n");
                point = new CoordinatePoint(maxx, miny);
                zone = cs.getProjection().getZone(point);
                if (cs.getProjection().getName().equalsIgnoreCase("bng")) {
                    x = BritishGrid.stripMajorDigit(maxx);
                    y = BritishGrid.stripMajorDigit(miny);
                } else {
                    x = maxx;
                    y = miny;
                }
                if (cs.isKnownCoordinateSystem()) {
                    wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(maxx, miny));
                    hemisphere = (wgs84.getOrdinate(1) >= 0.0) ? "N" : "S";
                } else {
                    hemisphere = cs.getProjection().getHemisphere();
                }
                out.write("Point02,xy," + (width - 1) + "," + (height - 1) + ",in, deg, , , , , , , grid," + zone + "," + (x * unitMultiplier) + "," + (y * unitMultiplier) + "," + hemisphere + "\r\n");
            }
            for (int loop = 3; loop <= 30; loop++) {
                out.write("Point" + ((loop < 10) ? "0" : "") + loop + ",xy,     ,     ,ex, deg,    ,        ,,    ,        ,, grid,   ,           ,           ,\r\n");
            }
            if (cs.getProjection().hasNameForRealm(MRCoordinateSystem.REALM_OZI_EXPLORER_PROJECTION_PARAMS)) {
                String projectionSetup = cs.getProjection().getName(MRCoordinateSystem.REALM_OZI_EXPLORER_PROJECTION_PARAMS);
                out.write("Projection Setup," + projectionSetup + "\r\n");
            } else {
                out.write("Projection Setup,,,,,,,,,,\r\n");
            }
            out.write("Map Feature = MF ; Map Comment = MC     These follow if they exist\r\n");
            out.write("Track File = TF      These follow if they exist\r\n");
            out.write("Moving Map Parameters = MM?    These follow if they exist\r\n");
            out.write("MM0,Yes\r\n");
            out.write("MMPNUM,4\r\n");
            out.write("MMPXY,1,0,0\r\n");
            out.write("MMPXY,2," + (width - 1) + ",0,\r\n");
            out.write("MMPXY,3," + (width - 1) + "," + (height - 1) + "\r\n");
            out.write("MMPXY,4,0," + (height - 1) + "\r\n");
            if (cs.isKnownCoordinateSystem()) {
                wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(minx, maxy));
                out.write("MMPLL,1," + wgs84.getOrdinate(0) + "," + wgs84.getOrdinate(1) + "\r\n");
                wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(maxx, maxy));
                out.write("MMPLL,2," + wgs84.getOrdinate(0) + "," + wgs84.getOrdinate(1) + "\r\n");
                wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(maxx, miny));
                out.write("MMPLL,3," + wgs84.getOrdinate(0) + "," + wgs84.getOrdinate(1) + "\r\n");
                wgs84 = cs.convertToLatLongWGS84(new CoordinatePoint(minx, miny));
                out.write("MMPLL,4," + wgs84.getOrdinate(0) + "," + wgs84.getOrdinate(1) + "\r\n");
                double midy = (miny + maxy) / 2;
                double xsize = (maxx - minx) / width;
                double midx = (minx + maxx) / 2;
                CoordinatePoint point1 = cs.convertToLatLongWGS84(new CoordinatePoint(midx, midy));
                CoordinatePoint point2 = cs.convertToLatLongWGS84(new CoordinatePoint(midx + xsize, midy));
                Ellipsoid wgs84ellipsoid = GeocentricCoordinateSystem.DEFAULT.getHorizontalDatum().getEllipsoid();
                double distance = wgs84ellipsoid.orthodromicDistance(point1.getOrdinate(0), point1.getOrdinate(1), point2.getOrdinate(0), point2.getOrdinate(1));
                out.write("MM1B," + distance + "\r\n");
            }
            out.write("MOP,Map Open Position,0,0\r\n");
            out.write("IWH,Map Image Width/Height," + width + "," + height + "\r\n");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }

    public int compareTo(Object o) {
        if (o instanceof MapService) {
            int result;
            MapService rhs = (MapService) o;
            if ((result = this.getCountry().compareTo(rhs.getCountry())) == 0) {
                if ((result = this.getState().compareTo(rhs.getState())) == 0) {
                    result = this.getMapDescription().compareTo(rhs.getMapDescription());
                }
            }
            return result;
        } else return 0;
    }

    /**
     * Get the coordinate system for this map.
     * @return Returns the coordinate system.
     */
    public ebgeo.maprequest.cs.MRCoordinateSystem getCs() {
        return cs;
    }

    /**
     * Set the coordinate system for this map.
     *
     * @param cs The new value for the coordinate system.
     */
    public void setCs(ebgeo.maprequest.cs.MRCoordinateSystem cs) {
        this.cs = cs;
    }

    /**
     * Set the coordinate system for this map.
     *
     * @param datum The name of a datum on which the coordinate system is based.
     * @param projection The name of a projection which the coordinate system
     *   uses.
     */
    public void setCs(String datum, String projection) {
        this.cs = MRCoordinateSystem.getCS(datum, projection);
    }

    /**
     * Check if this map supports panning.
     *
     * @return Returns true if this map supports panning, or false otherwise.
     **/
    public boolean isSupportsPanning() {
        return supportsPanning;
    }

    /** Setter for property supportsPanning.
     * @param supportsPanning New value of property supportsPanning.
     *
     */
    protected void setSupportsPanning(boolean supportsPanning) {
        this.supportsPanning = supportsPanning;
    }

    /**
     * Check if this map supports zooming.
     *
     * @return Returns true if this map supports zooming, or false otherwise.
     **/
    public boolean isSupportsZooming() {
        return supportsZooming;
    }

    /** Setter for property supportsZooming.
     * @param supportsZooming New value of property supportsZooming.
     *
     */
    protected void setSupportsZooming(boolean supportsZooming) {
        this.supportsZooming = supportsZooming;
    }

    /**
     * Get the locale for this map. This is used for any required text
     * formatting.
     */
    public java.util.Locale getLocale() {
        return locale;
    }

    /** Setter for property locale.
     * @param locale New value of property locale.
     *
     */
    protected void setLocale(java.util.Locale locale) {
        this.locale = locale;
    }

    /**
     * Get the text to add to the bottom of a map, with special characters
     * replaced by appropriatly formatted date/time symbols.
     *
     * @return Returns the text to add, or null if no text is to be added.
     * @see StringUtils#formatStringForDateTime(String,Locale)
     */
    public java.lang.String getAddText() {
        if (addText == null) return null; else return StringUtils.formatStringForDateTime(addText, getLocale());
    }

    /**
     * Set the text to add to the bottom of a map.
     *
     * @param addText The text to add, or null if no text is to be added.
     */
    protected void setAddText(java.lang.String addText) {
        this.addText = addText;
    }

    /**
     * Get the short name of this map.
     **/
    public String getShortName() {
        return shortName;
    }

    /**
     * Set the short name of this map.
     **/
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
