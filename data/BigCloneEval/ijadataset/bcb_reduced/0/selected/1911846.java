package gov.nasa.worldwind.formats.shapefile;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.util.*;
import javax.xml.stream.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Parses an ESRI Shapefile (.shp) and provides access to its contents. For details on the Shapefile
 * format see the ESRI documentation at 
 * <a href="http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf">
 * http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf</a>.
 * <p/>
 * The Shapefile provides a streaming interface for parsing a Shapefile's contents. The streaming
 * interface enables applications to read Shapefiles that do not fit in memory. A typical usage 
 * pattern is as follows: 
 * <code>
 * <pre>
 * Object source = "MyShapefile.shp";
 * Shapefile sf = new Shapefile(source);
 * try
 * {
 *     while (sf.hasNext())
 *     {
 *         ShapefileRecord record = sf.nextRecord();
 *         // Interpret Shapefile record contents...
 *     }
 * }
 * finally
 * {
 *     WWIO.closeStream(sf, source);
 * }
 * </pre>
 * </code>
 * <p/>
 * The source Shapefile may be accompanied by an optional index file, attribute file, and projection
 * file. Shapefile constructors that accept a generic source such as {@link #Shapefile(Object)
 * expect accompanying files to be in the same logical folder as the Shapefile, have the same
 * filename as the Shapefile, and have suffixes ".shx", ".dbf", and ".prj" respectively. If any of 
 * these files do not exist, or cannot be read for any reason, the Shapefile opens without that 
 * information. Alternatively, the Shapefile can be constructed by providing a direct {@link
 * java.io.InputStream} to any of the accompanying sources by using the InputStream based
 * constructors, such as {@link #Shapefile(java.io.InputStream, java.io.InputStream, 
 * java.io.InputStream, java.io.InputStream)}.
 * <p/>
 * <h3>Coordinate System</h3>
 * <p/>
 * The Shapefile's coordinate system affects how the Shapefile's point coordinates are interpreted 
 * as follows: 
 * <ul>
 * <li>Unspecified - coordinates are not changed.</li> 
 * <li>Geographic - coordinates are validated during parsing.
 * Coordinates outside the standard range of +90/-90 latitude and +180/-180 longitude cause the Shapefile to throw an
 * exception during construction if the Shapefile's header contains an invalid coordinate, or in {@link
 * #readNextRecord()} if any of the Shapefile's records contain an invalid coordinate.</li> 
 * <li>
 * Universal Transverse
 * Mercator (UTM) - UTM coordinates are converted to geographic coordinates during parsing.
 * </li> 
 * <li>
 * Unsupported - the Shapefile throws a {@link gov.nasa.worldwind.exception.WWRuntimeException} 
 * during construction.
 * </li>
 * </ul>
 * <p/>
 * The Shapefile's coordinate system can be specified in either an accompanying projection file, 
 * or by specifying the coordinate system parameters in an {@link gov.nasa.worldwind.avlist.AVList} 
 * during Shapefile's construction. The Shapefile gives priority to the AVList if an accompanying 
 * projection file is available and AVList projection parameters are specified. If an accompanying 
 * projection file is available, the Shapefile attempts to parse the projection file as an OGC
 * coordinate system encoded in well-known text format. For details, see the OGC Coordinate
 * Transform Service (CT) specification at 
 * <a href="http://www.opengeospatial.org/standards/ct">
 * http://www.opengeospatial.org/standards/ct</a>.
 * The Shapefile expects the AVList specifying its coordinate system parameters to contain the
 * following properties:
 * <ul>
 * <li>
 * {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM} - 
 * either {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_GEOGRAPHIC}
 * or {@link gov.nasa.worldwind.avlist.AVKey#COORDINATE_SYSTEM_PROJECTED}.
 * </li>
 * <li>
 * {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_ZONE} -
 * the UTM zone (if coordinate system projection is UTM);
 * an integer in the range 1-60.
 * </li>
 * <li>
 * {@link gov.nasa.worldwind.avlist.AVKey#PROJECTION_HEMISPHERE} -
 * the UTM hemisphere (if coordinate system is UTM);
 * either {@link gov.nasa.worldwind.avlist.AVKey#NORTH}
 * or {@link gov.nasa.worldwind.avlist.AVKey#SOUTH}.
 * </li>
 * </ul>
 * <p/>
 * Subclasses can override how the Shapefile reads and interprets its coordinate system. Override {@link
 * #readCoordinateSystem()} and {@link #validateCoordinateSystem(gov.nasa.worldwind.avlist.AVList)} to change how the
 * Shapefile parses an accompanying projection file and validates the coordinate system parameters. Override {@link
 * #readBoundingRectangle(java.nio.ByteBuffer)} and {@link #readPoints(java.nio.ByteBuffer)} to change how the
 * Shapefile's point coordinates are interpreted according to its coordinate system.
 *
 * @author Patrick Murris
 * @version $Id: Shapefile.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class Shapefile extends AVListImpl implements Closeable, Exportable {

    protected static final int FILE_CODE = 0x0000270A;

    protected static final int HEADER_LENGTH = 100;

    protected static final String SHAPE_FILE_SUFFIX = ".shp";

    protected static final String INDEX_FILE_SUFFIX = ".shx";

    protected static final String ATTRIBUTE_FILE_SUFFIX = ".dbf";

    protected static final String PROJECTION_FILE_SUFFIX = ".prj";

    protected static final String[] SHAPE_CONTENT_TYPES = { "application/shp", "application/octet-stream" };

    protected static final String[] INDEX_CONTENT_TYPES = { "application/shx", "application/octet-stream" };

    protected static final String[] PROJECTION_CONTENT_TYPES = { "application/prj", "application/octet-stream", "text/plain" };

    public static final String SHAPE_NULL = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapeNull";

    public static final String SHAPE_POINT = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePoint";

    public static final String SHAPE_MULTI_POINT = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapeMultiPoint";

    public static final String SHAPE_POLYLINE = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolyline";

    public static final String SHAPE_POLYGON = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolygon";

    public static final String SHAPE_POINT_M = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePointM";

    public static final String SHAPE_MULTI_POINT_M = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapeMultiPointM";

    public static final String SHAPE_POLYLINE_M = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolylineM";

    public static final String SHAPE_POLYGON_M = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolygonM";

    public static final String SHAPE_POINT_Z = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePointZ";

    public static final String SHAPE_MULTI_POINT_Z = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapeMultiPointZ";

    public static final String SHAPE_POLYLINE_Z = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolylineZ";

    public static final String SHAPE_POLYGON_Z = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapePolygonZ";

    public static final String SHAPE_MULTI_PATCH = "gov.nasa.worldwind.formats.shapefile.Shapefile.ShapeMultiPatch";

    protected static List<String> measureTypes = new ArrayList<String>(Arrays.asList(Shapefile.SHAPE_POINT_M, Shapefile.SHAPE_POINT_Z, Shapefile.SHAPE_MULTI_POINT_M, Shapefile.SHAPE_MULTI_POINT_Z, Shapefile.SHAPE_POLYLINE_M, Shapefile.SHAPE_POLYLINE_Z, Shapefile.SHAPE_POLYGON_M, Shapefile.SHAPE_POLYGON_Z));

    protected static List<String> zTypes = new ArrayList<String>(Arrays.asList(Shapefile.SHAPE_POINT_Z, Shapefile.SHAPE_MULTI_POINT_Z, Shapefile.SHAPE_POLYLINE_Z, Shapefile.SHAPE_POLYGON_Z));

    protected static class Header {

        public int fileCode = FILE_CODE;

        public int fileLength;

        public int version;

        public String shapeType;

        public double[] boundingRectangle;

        public boolean normalizePoints;
    }

    protected Header header;

    protected int[] index;

    protected CompoundVecBuffer pointBuffer;

    protected ReadableByteChannel shpChannel;

    protected ReadableByteChannel shxChannel;

    protected ReadableByteChannel prjChannel;

    protected DBaseFile attributeFile;

    protected boolean open;

    /**
     * Indicates if the shapefile's point coordinates should be normalized. Defaults to false. This is used by Point
     * records to determine if its points should be normalized. MultiPoint, Polyline, and Polygon records use their
     * bounding rectangles to determine if they should be normalized, and therefore ignore this property.
     */
    protected boolean normalizePoints;

    protected int numRecordsRead;

    protected int numBytesRead;

    protected ByteBuffer recordHeaderBuffer;

    protected ByteBuffer recordContentBuffer;

    protected MappedByteBuffer mappedShpBuffer;

    /**
     * Opens an Shapefile from a general source. The source type may be one of the following: <ul> <li>{@link
     * java.io.InputStream}</li> <li>{@link java.net.URL}</li> <li>absolute {@link java.net.URI}</li><li>{@link
     * File}</li> <li>{@link String} containing a valid URL description or a file or resource name available on the
     * classpath.</li> </ul>
     * <p/>
     * The source Shapefile may be accompanied by an optional index file, attribute file, and projection file. To be
     * recognized by this Shapefile, accompanying files must be in the same logical folder as the Shapefile, have the
     * same filename as the Shapefile, and have suffixes ".shx", ".dbf", and ".prj" respectively. If any of these files
     * do not exist, or cannot be read for any reason, the Shapefile opens without that information.
     * <p/>
     * This throws an exception if the shapefile's coordinate system is unsupported.
     *
     * @param source the source of the shapefile.
     * @param params parameter list describing metadata about the Shapefile, such as its map projection.
     *
     * @throws IllegalArgumentException if the source is null or an empty string.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason, or if the shapefile's
     *                                  coordinate system is unsupported.
     */
    public Shapefile(Object source, AVList params) {
        if (source == null || WWUtil.isEmpty(source)) {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            this.setValue(AVKey.DISPLAY_NAME, source.toString());
            if (source instanceof File) this.initializeFromFile((File) source, params); else if (source instanceof URL) this.initializeFromURL((URL) source, params); else if (source instanceof InputStream) this.initializeFromStreams((InputStream) source, null, null, null, params); else if (source instanceof String) this.initializeFromPath((String) source, params); else {
                String message = Logging.getMessage("generic.UnrecognizedSourceType", source);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        } catch (Exception e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadShapefile", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Opens an Shapefile from a general source. The source type may be one of the following: <ul> <li>{@link
     * java.io.InputStream}</li> <li>{@link java.net.URL}</li> <li>{@link File}</li> <li>{@link String} containing a
     * valid URL description or a file or resource name available on the classpath.</li> </ul>
     * <p/>
     * The source Shapefile may be accompanied by an optional index file, attribute file, and projection file. To be
     * recognized by this Shapefile, accompanying files must be in the same logical folder as the Shapefile, have the
     * same filename as the Shapefile, and have suffixes ".shx", ".dbf", and ".prj" respectively. If any of these files
     * do not exist, or cannot be read for any reason, the Shapefile opens without that information.
     * <p/>
     * This throws an exception if the shapefile's coordinate system is unsupported, or if the shapefile's coordinate
     * system is unsupported.
     *
     * @param source the source of the shapefile.
     *
     * @throws IllegalArgumentException if the source is null or an empty string.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason.
     */
    public Shapefile(Object source) {
        this(source, ((AVList) null));
    }

    /**
     * Opens a Shapefile from an InputStream, and InputStreams to its optional resources.
     * <p/>
     * The source Shapefile may be accompanied optional streams to an index resource stream, an attribute resource
     * stream, and a projection resource stream. If any of these streams are null or cannot be read for any reason, the
     * Shapefile opens without that information.
     * <p/>
     * This throws an exception if the shapefile's coordinate system is unsupported.
     *
     * @param shpStream the shapefile geometry file stream.
     * @param shxStream the index file stream, can be null.
     * @param dbfStream the attribute file stream, can be null.
     * @param prjStream the projection file stream, can be null.
     * @param params    parameter list describing metadata about the Shapefile, such as its map projection.
     *
     * @throws IllegalArgumentException if the shapefile geometry stream <code>shpStream</code> is null.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason, or if the shapefile's
     *                                  coordinate system is unsupported.
     */
    public Shapefile(InputStream shpStream, InputStream shxStream, InputStream dbfStream, InputStream prjStream, AVList params) {
        if (shpStream == null) {
            String message = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            this.setValue(AVKey.DISPLAY_NAME, shpStream.toString());
            this.initializeFromStreams(shpStream, shxStream, dbfStream, prjStream, params);
        } catch (Exception e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadShapefile", shpStream);
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
    }

    /**
     * Opens a Shapefile from an InputStream, and InputStreams to its optional resources.
     * <p/>
     * The source Shapefile may be accompanied optional streams to an index resource stream, an attribute resource
     * stream, and a projection resource stream. If any of these streams are null or cannot be read for any reason, the
     * Shapefile opens without that information.
     * <p/>
     * This throws an exception if the shapefile's coordinate system is unsupported.
     *
     * @param shpStream the shapefile geometry file stream.
     * @param shxStream the index file stream, can be null.
     * @param dbfStream the attribute file stream, can be null.
     * @param prjStream the projection file stream, can be null.
     *
     * @throws IllegalArgumentException if the shapefile geometry stream <code>shpStream</code> is null.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason, or if the shapefile's
     *                                  coordinate system is unsupported.
     */
    public Shapefile(InputStream shpStream, InputStream shxStream, InputStream dbfStream, InputStream prjStream) {
        this(shpStream, shxStream, dbfStream, prjStream, null);
    }

    /**
     * Opens a Shapefile from an InputStream, and InputStreams to its optional resources.
     * <p/>
     * The source Shapefile may be accompanied optional streams to an index resource stream, and an attribute resource
     * stream. If any of these streams are null or cannot be read for any reason, the Shapefile opens without that
     * information.
     * <p/>
     * This throws an exception if the shapefile's coordinate system is unsupported.
     *
     * @param shpStream the shapefile geometry file stream.
     * @param shxStream the index file stream, can be null.
     * @param dbfStream the attribute file stream, can be null.
     * @param params    parameter list describing metadata about the Shapefile, such as its map projection.
     *
     * @throws IllegalArgumentException if the shapefile geometry stream <code>shpStream</code> is null.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason, or if the shapefile's
     *                                  coordinate system is unsupported.
     */
    public Shapefile(InputStream shpStream, InputStream shxStream, InputStream dbfStream, AVList params) {
        this(shpStream, shxStream, dbfStream, null, params);
    }

    /**
     * Opens a Shapefile from an InputStream, and InputStreams to its optional resources.
     * <p/>
     * The source Shapefile may be accompanied optional streams to an index resource stream, and an attribute resource
     * stream. If any of these streams are null or cannot be read for any reason, the Shapefile opens without that
     * information.
     *
     * @param shpStream the shapefile geometry file stream.
     * @param shxStream the index file stream, can be null.
     * @param dbfStream the attribute file stream, can be null.
     *
     * @throws IllegalArgumentException if the shapefile geometry stream <code>shpStream</code> is null.
     * @throws WWRuntimeException       if the shapefile cannot be opened for any reason.
     */
    public Shapefile(InputStream shpStream, InputStream shxStream, InputStream dbfStream) {
        this(shpStream, shxStream, dbfStream, null, null);
    }

    /**
     * Returns the shapefile's version field, or -1 if the Shapefile failed to open.
     *
     * @return the shapefile's version field, or -1 to denote the Shapefile failed to open.
     */
    public int getVersion() {
        return this.header != null ? this.header.version : -1;
    }

    /**
     * Returns the raw shapefile's length, or -1 if the Shapefile failed to open.
     *
     * @return the raw shapefile's length in bytes, or -1 to denote the Shapefile failed to open.
     */
    public int getLength() {
        return this.header != null ? this.header.fileLength : -1;
    }

    /**
     * Returns the shapefile's shape type: null if the Shapefile failed to open, otherwise one of
     * the following symbolic constants: 
     * <ul>
     * <li>{@link #SHAPE_NULL}</li>
     * <li>{@link #SHAPE_POINT}</li>
     * <li>{@link #SHAPE_MULTI_POINT}</li>
     * <li>{@link #SHAPE_POLYLINE}</li> 
     * <li>{@link #SHAPE_POLYGON}</li>
     * <li>{@link #SHAPE_POINT_M}</li>
     * <li>{@link #SHAPE_MULTI_POINT_M}</li>
     * <li>{@link #SHAPE_POLYLINE_M}</li>
     * <li>{@link #SHAPE_POLYGON_M}</li>
     * <li>{@link #SHAPE_POINT_Z}</li>
     * <li>{@link #SHAPE_MULTI_POINT_Z}</li>
     * <li>{@link #SHAPE_POLYLINE_Z}</li>
     * <li>{@link #SHAPE_POLYGON_Z}</li>
     * <li>{@link #SHAPE_MULTI_PATCH}</li> </ul>
     *
     * @return the shapefile's shape type: null if the Shapefile failed to open, 
     * othersise a symbolic constants denoting the type.
     */
    public String getShapeType() {
        return this.header != null ? this.header.shapeType : null;
    }

    /**
     * Returns a four-element array containing the shapefile's bounding rectangle, or null if the Shapefile failed to
     * open. The returned array is ordered as follows: minimum Y, maximum Y, minimum X, and maximum X. If the
     * Shapefile's coordinate system is geographic, the elements can be interpreted as angular degrees in the order
     * minimum latitude, maximum latitude, minimum longitude, and maximum longitude.
     *
     * @return the shapefile's bounding rectangle, or null to denote the Shapefile failed to open.
     */
    public double[] getBoundingRectangle() {
        return this.header != null ? this.header.boundingRectangle : null;
    }

    /**
     * Returns the number of records in the shapefile, or -1 if the number if records is unknown.
     *
     * @return the number of records in the shapefile, or -1 to denote an unknown number of records.
     */
    public int getNumberOfRecords() {
        return this.index != null ? this.index.length / 2 : -1;
    }

    /**
     * Get the underlying {@link CompoundVecBuffer} describing the shapefile's points.
     *
     * @return the underlying {@link CompoundVecBuffer}.
     */
    public CompoundVecBuffer getPointBuffer() {
        return this.pointBuffer;
    }

    /**
     * Returns <code>true</code> if the Shapefile has a more records, and <code>false</code> if all records have been
     * read.
     *
     * @return <code>true</code> if the Shapefile has a more records; <code>false</code> otherwise.
     */
    public boolean hasNext() {
        if (!this.open || this.header == null) return false;
        int contentLength = this.header.fileLength - HEADER_LENGTH;
        return this.numBytesRead < contentLength;
    }

    /**
     * Reads the Shapefile's next record and returns the result as a new {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     * The record's type depends on the Shapefile's type, and is one of the following: <ul> <li>{@link
     * gov.nasa.worldwind.formats.shapefile.ShapefileRecordPoint} if type is {@link #SHAPE_POINT}, {@link
     * #SHAPE_POINT_M} or {@link #SHAPE_POINT_Z}.</li> <li>{@link gov.nasa.worldwind.formats.shapefile.ShapefileRecordMultiPoint}
     * if type is {@link #SHAPE_MULTI_POINT}, {@link #SHAPE_MULTI_POINT_M} or {@link #SHAPE_MULTI_POINT_Z}.</li>
     * <li>{@link gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolyline} if type is {@link #SHAPE_POLYLINE},
     * {@link #SHAPE_POLYLINE_M} or {@link #SHAPE_POLYLINE_Z}.</li> <li>{@link gov.nasa.worldwind.formats.shapefile.ShapefileRecordPolygon}
     * if type is {@link #SHAPE_POLYGON}, {@link #SHAPE_POLYGON_M} or {@link #SHAPE_POLYGON_Z}.</li> </ul>
     * <p/>
     * This throws an exception if the JVM cannot allocate enough memory to hold the buffer used to store the record's
     * point coordinates.
     *
     * @return the Shapefile's next record.
     *
     * @throws IllegalStateException if the Shapefile is closed or if the Shapefile has no more records.
     * @throws WWRuntimeException    if an exception occurs while reading the record.
     * @see #getShapeType()
     */
    public ShapefileRecord nextRecord() {
        if (!this.open) {
            String message = Logging.getMessage("SHP.ShapefileClosed", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (this.header == null) {
            String message = Logging.getMessage("SHP.HeaderIsNull", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        int contentLength = this.header.fileLength - HEADER_LENGTH;
        if (contentLength <= 0 || this.numBytesRead >= contentLength) {
            String message = Logging.getMessage("SHP.NoRecords", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        ShapefileRecord record;
        try {
            record = this.readNextRecord();
        } catch (Exception e) {
            String message = Logging.getMessage("SHP.ExceptionAttemptingToReadShapefileRecord", this.getValue(AVKey.DISPLAY_NAME));
            Logging.logger().log(Level.SEVERE, message, e);
            throw new WWRuntimeException(message, e);
        }
        this.numRecordsRead++;
        return record;
    }

    /**
     * Closes the Shapefile, freeing any resources allocated during reading except the buffer containing the Shapefile's
     * points. This closes any {@link java.io.InputStream} passed to the Shapefile during construction. Subsequent calls
     * to {@link #nextRecord()} cause an IllegalStateException.
     * <p/>
     * After closing, the Shapefile's header information and point coordinates are still available. The following
     * methods are safe to call: <ul> <li>{@link #getVersion()}</li> <li>{@link #getLength()}</li> <li>{@link
     * #getShapeType()}</li> <li>{@link #getBoundingRectangle()}</li> <li>{@link #getNumberOfRecords()}</li> <li>{@link
     * #getPointBuffer()}</li> </ul>
     */
    public void close() {
        if (this.shpChannel != null) {
            WWIO.closeStream(this.shpChannel, null);
            this.shpChannel = null;
        }
        if (this.shxChannel != null) {
            WWIO.closeStream(this.shxChannel, null);
            this.shxChannel = null;
        }
        if (this.prjChannel != null) {
            WWIO.closeStream(this.prjChannel, null);
            this.prjChannel = null;
        }
        if (this.attributeFile != null) {
            this.attributeFile.close();
            this.attributeFile = null;
        }
        this.recordHeaderBuffer = null;
        this.recordContentBuffer = null;
        this.mappedShpBuffer = null;
        this.open = false;
    }

    /**
     * Returns whether the shapefile's point coordinates should be normalized.
     *
     * @return <code>true</code> if the shapefile's points should be normalized; <code>false</code> otherwise.
     */
    protected boolean isNormalizePoints() {
        return this.normalizePoints;
    }

    /**
     * Specifies if the shapefile's point coordinates should be normalized. Defaults to <code>false</code>.
     *
     * @param normalizePoints <code>true</code> if the shapefile's points should be normalized; <code>false</code>
     *                        otherwise.
     */
    protected void setNormalizePoints(boolean normalizePoints) {
        this.normalizePoints = normalizePoints;
    }

    protected void initializeFromFile(File file, AVList params) throws IOException {
        if (!file.exists()) {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new FileNotFoundException(message);
        }
        if (file.canRead() && file.canWrite()) {
            try {
                this.mappedShpBuffer = WWIO.mapFile(file, FileChannel.MapMode.PRIVATE);
                Logging.logger().finer(Logging.getMessage("SHP.MemoryMappingEnabled", file.getPath()));
            } catch (IOException e) {
                Logging.logger().log(Level.WARNING, Logging.getMessage("SHP.ExceptionAttemptingToMemoryMap", file.getPath()), e);
            }
        }
        if (this.mappedShpBuffer == null) this.shpChannel = Channels.newChannel(new BufferedInputStream(new FileInputStream(file)));
        InputStream shxStream = this.getFileStream(WWIO.replaceSuffix(file.getPath(), INDEX_FILE_SUFFIX));
        if (shxStream != null) this.shxChannel = Channels.newChannel(WWIO.getBufferedInputStream(shxStream));
        InputStream prjStream = this.getFileStream(WWIO.replaceSuffix(file.getPath(), PROJECTION_FILE_SUFFIX));
        if (prjStream != null) this.prjChannel = Channels.newChannel(WWIO.getBufferedInputStream(prjStream));
        this.setValue(AVKey.DISPLAY_NAME, file.getPath());
        this.initialize(params);
        File dbfFile = new File(WWIO.replaceSuffix(file.getPath(), ATTRIBUTE_FILE_SUFFIX));
        if (dbfFile.exists()) {
            try {
                this.attributeFile = new DBaseFile(dbfFile);
            } catch (Exception e) {
            }
        }
    }

    protected void initializeFromURL(URL url, AVList params) throws IOException {
        URLConnection connection = url.openConnection();
        String message = this.validateURLConnection(connection, SHAPE_CONTENT_TYPES);
        if (message != null) {
            throw new IOException(message);
        }
        this.shpChannel = Channels.newChannel(WWIO.getBufferedInputStream(connection.getInputStream()));
        URLConnection shxConnection = this.getURLConnection(WWIO.replaceSuffix(url.toString(), INDEX_FILE_SUFFIX));
        if (shxConnection != null) {
            message = this.validateURLConnection(shxConnection, INDEX_CONTENT_TYPES);
            if (message != null) Logging.logger().warning(message); else {
                InputStream shxStream = this.getURLStream(shxConnection);
                if (shxStream != null) this.shxChannel = Channels.newChannel(WWIO.getBufferedInputStream(shxStream));
            }
        }
        URLConnection prjConnection = this.getURLConnection(WWIO.replaceSuffix(url.toString(), PROJECTION_FILE_SUFFIX));
        if (prjConnection != null) {
            message = this.validateURLConnection(prjConnection, PROJECTION_CONTENT_TYPES);
            if (message != null) Logging.logger().warning(message); else {
                InputStream prjStream = this.getURLStream(prjConnection);
                if (prjStream != null) this.prjChannel = Channels.newChannel(WWIO.getBufferedInputStream(prjStream));
            }
        }
        this.setValue(AVKey.DISPLAY_NAME, url.toString());
        this.initialize(params);
        URL dbfURL = WWIO.makeURL(WWIO.replaceSuffix(url.toString(), ATTRIBUTE_FILE_SUFFIX));
        if (dbfURL != null) {
            try {
                this.attributeFile = new DBaseFile(dbfURL);
            } catch (Exception e) {
            }
        }
    }

    protected void initializeFromStreams(InputStream shpStream, InputStream shxStream, InputStream dbfStream, InputStream prjStream, AVList params) throws IOException {
        if (shpStream != null) this.shpChannel = Channels.newChannel(WWIO.getBufferedInputStream(shpStream));
        if (shxStream != null) this.shxChannel = Channels.newChannel(WWIO.getBufferedInputStream(shxStream));
        if (prjStream != null) this.prjChannel = Channels.newChannel(WWIO.getBufferedInputStream(prjStream));
        this.initialize(params);
        if (dbfStream != null) {
            try {
                this.attributeFile = new DBaseFile(dbfStream);
            } catch (Exception e) {
            }
        }
    }

    protected void initializeFromPath(String path, AVList params) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            this.initializeFromFile(file, params);
            return;
        }
        URL url = WWIO.makeURL(path);
        if (url != null) {
            this.initializeFromURL(url, params);
            return;
        }
        String message = Logging.getMessage("generic.UnrecognizedSourceType", path);
        Logging.logger().severe(message);
        throw new IllegalArgumentException(message);
    }

    /**
     * Prepares the Shapefile for reading. This reads the Shapefile's accompanying index and projection (if they're
     * available), validates the Shapefile's coordinate system, and reads the Shapefile's header.
     *
     * @param params arameter list describing metadata about the Shapefile, such as its map projection, or null to
     *               specify no additional parameters.
     *
     * @throws IOException if an error occurs while reading the Shapefile's header.
     */
    protected void initialize(AVList params) throws IOException {
        try {
            AVList csParams = this.readCoordinateSystem();
            if (csParams != null) this.setValues(csParams);
        } catch (IOException e) {
            Logging.logger().log(Level.WARNING, Logging.getMessage("SHP.ExceptionAttemptingToReadProjection", this.getValue(AVKey.DISPLAY_NAME)), e);
        }
        if (params != null) {
            this.setValues(params);
        }
        String message = this.validateCoordinateSystem(this);
        if (message != null) {
            throw new WWRuntimeException(message);
        }
        try {
            this.index = this.readIndex();
        } catch (IOException e) {
            Logging.logger().log(Level.WARNING, Logging.getMessage("SHP.ExceptionAttemptingToReadIndex", this.getValue(AVKey.DISPLAY_NAME)), e);
        }
        this.header = this.readHeader();
        this.open = true;
        this.setNormalizePoints(this.header.normalizePoints);
    }

    protected InputStream getFileStream(String path) {
        try {
            return new FileInputStream(path);
        } catch (Exception e) {
            return null;
        }
    }

    protected URLConnection getURLConnection(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.openConnection();
        } catch (Exception e) {
            return null;
        }
    }

    protected InputStream getURLStream(URLConnection connection) {
        try {
            return connection.getInputStream();
        } catch (Exception e) {
            return null;
        }
    }

    protected String validateURLConnection(URLConnection connection, String[] acceptedContentTypes) {
        try {
            if (connection instanceof HttpURLConnection && ((HttpURLConnection) connection).getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Logging.getMessage("HTTP.ResponseCode", ((HttpURLConnection) connection).getResponseCode(), connection.getURL());
            }
        } catch (Exception e) {
            return Logging.getMessage("URLRetriever.ErrorOpeningConnection", connection.getURL());
        }
        String contentType = connection.getContentType();
        if (WWUtil.isEmpty(contentType)) return null;
        for (String type : acceptedContentTypes) {
            if (contentType.trim().toLowerCase().startsWith(type)) return null;
        }
        return Logging.getMessage("HTTP.UnexpectedContentType", contentType, Arrays.toString(acceptedContentTypes));
    }

    /**
     * Reads the {@link Header} from this Shapefile. This file is assumed to have a header.
     *
     * @return a {@link Header} instance.
     *
     * @throws IOException if the header cannot be read for any reason.
     */
    protected Header readHeader() throws IOException {
        ByteBuffer buffer;
        if (this.mappedShpBuffer != null) {
            buffer = this.mappedShpBuffer;
        } else {
            buffer = ByteBuffer.allocate(HEADER_LENGTH);
            WWIO.readChannelToBuffer(this.shpChannel, buffer);
        }
        if (buffer.remaining() < HEADER_LENGTH) {
            throw new WWRuntimeException(Logging.getMessage("generic.InvalidFileLength", buffer.remaining()));
        }
        return this.readHeaderFromBuffer(buffer);
    }

    /**
     * Reads a {@link Header} instance from the given {@link java.nio.ByteBuffer};
     * <p/>
     * The buffer current position is assumed to be set at the start of the header and will be set to the end of the
     * header after this method has completed.
     *
     * @param buffer the Header @link java.nio.ByteBuffer} to read from.
     *
     * @return a {@link Header} instances.
     *
     * @throws IOException if the header cannot be read for any reason.
     */
    protected Header readHeaderFromBuffer(ByteBuffer buffer) throws IOException {
        Header header = null;
        int pos = buffer.position();
        try {
            buffer.order(ByteOrder.BIG_ENDIAN);
            int fileCode = buffer.getInt();
            if (fileCode != FILE_CODE) {
                throw new WWUnrecognizedException(Logging.getMessage("SHP.UnrecognizedShapefile", fileCode));
            }
            buffer.position(buffer.position() + 5 * 4);
            int lengthInWords = buffer.getInt();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int version = buffer.getInt();
            int type = buffer.getInt();
            BoundingRectangle rect = this.readBoundingRectangle(buffer);
            String shapeType = getShapeType(type);
            if (shapeType == null) {
                throw new WWRuntimeException(Logging.getMessage("SHP.UnsupportedShapeType", type));
            }
            header = new Header();
            header.fileLength = lengthInWords * 2;
            header.version = version;
            header.shapeType = shapeType;
            header.boundingRectangle = rect.coords;
            header.normalizePoints = rect.isNormalized;
        } finally {
            buffer.position(pos + HEADER_LENGTH);
        }
        return header;
    }

    /**
     * Reads the Shapefile's accompanying index file and return the indices as an array of integers. Each array element
     * represents the byte offset of the i'th record from the start of the Shapefile. This returns <code>null</code> if
     * this Shapefile has no accompanying index file, if the index file is empty, or if the JVM cannot allocate enough
     * memory to hold the index.
     *
     * @return the Shapefile's record offset index, or <code>null</code> if the Shapefile has no accompanying index
     *         file, if the index file is empty, or if the index cannot be allocated.
     *
     * @throws IOException if an exception occurs during reading.
     */
    protected int[] readIndex() throws IOException {
        if (this.shxChannel == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        WWIO.readChannelToBuffer(this.shxChannel, buffer);
        if (buffer.remaining() < HEADER_LENGTH) return null;
        Header indexHeader = this.readHeaderFromBuffer(buffer);
        int numRecords = (indexHeader.fileLength - HEADER_LENGTH) / 8;
        int numElements = 2 * numRecords;
        int indexLength = 8 * numRecords;
        int[] array;
        try {
            buffer = ByteBuffer.allocate(indexLength);
            array = new int[numElements];
        } catch (OutOfMemoryError e) {
            Logging.logger().log(Level.WARNING, Logging.getMessage("SHP.OutOfMemoryAllocatingIndex", this.getValue(AVKey.DISPLAY_NAME)), e);
            return null;
        }
        buffer.order(ByteOrder.BIG_ENDIAN);
        WWIO.readChannelToBuffer(this.shxChannel, buffer);
        buffer.asIntBuffer().get(array);
        for (int i = 0; i < numElements; i++) {
            array[i] *= 2;
        }
        return array;
    }

    /**
     * Reads the Shapefile's accompanying projection file as an OGC coordinate system encoded in well-known text format,
     * and returns the coordinate system parameters. This returns <code>null</code> if this Shapefile has no
     * accompanying projection file or if the projection file is empty. For details, see the OGC Coordinate Transform
     * Service (CT) specification at <a href="http://www.opengeospatial.org/standards/ct">http://www.opengeospatial.org/standards/ct</a>.
     *
     * @return coordinate system parameters parsed from the projection file, or <code>null</code> if this Shapefile has
     *         no accompanying projection file or the projection file is empty.
     *
     * @throws IOException if an exception occurs during reading.
     */
    protected AVList readCoordinateSystem() throws IOException {
        if (this.prjChannel == null) return null;
        String text = WWIO.readChannelToString(this.prjChannel, null);
        if (WWUtil.isEmpty(text)) return null;
        return WorldFile.decodeOGCCoordinateSystemWKT(text, null);
    }

    /**
     * Returns a string indicating an error with the Shapefile's coordinate system parameters, or null to indicate that
     * the coordinate parameters are valid.
     *
     * @param params the Shapefile's coordinate system parameters.
     *
     * @return a non-empty string if the coordinate system parameters are invalid; null otherwise.
     */
    protected String validateCoordinateSystem(AVList params) {
        Object o = params.getValue(AVKey.COORDINATE_SYSTEM);
        if (!this.hasKey(AVKey.COORDINATE_SYSTEM)) {
            Logging.logger().warning(Logging.getMessage("generic.UnspecifiedCoordinateSystem", this.getValue(AVKey.DISPLAY_NAME)));
            return null;
        } else if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(o)) {
            return null;
        } else if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(o)) {
            return this.validateProjection(params);
        } else {
            return Logging.getMessage("generic.UnsupportedCoordinateSystem", o);
        }
    }

    /**
     * Returns a string indicating an error with the Shapefile's projection parameters, or null to indicate that the
     * projection parameters are valid.
     *
     * @param params the Shapefile's projection parameters.
     *
     * @return a non-empty string if the projection parameters are invalid; null otherwise.
     */
    protected String validateProjection(AVList params) {
        Object proj = params.getValue(AVKey.PROJECTION_NAME);
        if (AVKey.PROJECTION_UTM.equals(proj)) {
            StringBuilder sb = new StringBuilder();
            Object o = params.getValue(AVKey.PROJECTION_ZONE);
            if (o == null) sb.append(Logging.getMessage("generic.ZoneIsMissing")); else if (!(o instanceof Integer) || ((Integer) o) < 1 || ((Integer) o) > 60) sb.append(Logging.getMessage("generic.ZoneIsInvalid", o));
            o = params.getValue(AVKey.PROJECTION_HEMISPHERE);
            if (o == null) sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("generic.HemisphereIsMissing")); else if (!o.equals(AVKey.NORTH) && !o.equals(AVKey.SOUTH)) sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("generic.HemisphereIsInvalid", o));
            return sb.length() > 0 ? sb.toString() : null;
        } else {
            return Logging.getMessage("generic.UnsupportedProjection", proj);
        }
    }

    /**
     * Reads the next {@link ShapefileRecord} instance from this Shapefile. This file is assumed to have one or more
     * remaining records available.
     *
     * @return a {@link ShapefileRecord} instance.
     *
     * @throws IOException if the record cannot be read for any reason.
     */
    protected ShapefileRecord readNextRecord() throws IOException {
        ByteBuffer buffer;
        if (this.mappedShpBuffer != null) {
            int pos = this.mappedShpBuffer.position();
            this.mappedShpBuffer.order(ByteOrder.BIG_ENDIAN);
            int contentLength = this.mappedShpBuffer.getInt(pos + 4) * 2;
            int recordLength = ShapefileRecord.RECORD_HEADER_LENGTH + contentLength;
            this.mappedShpBuffer.position(pos);
            this.mappedShpBuffer.limit(pos + recordLength);
            this.numBytesRead += recordLength;
            buffer = this.mappedShpBuffer;
        } else {
            if (this.recordHeaderBuffer == null) this.recordHeaderBuffer = ByteBuffer.allocate(ShapefileRecord.RECORD_HEADER_LENGTH);
            this.recordHeaderBuffer.clear();
            this.recordHeaderBuffer.order(ByteOrder.BIG_ENDIAN);
            WWIO.readChannelToBuffer(this.shpChannel, this.recordHeaderBuffer);
            int contentLength = this.recordHeaderBuffer.getInt(4) * 2;
            int recordLength = ShapefileRecord.RECORD_HEADER_LENGTH + contentLength;
            if (this.recordContentBuffer == null || this.recordContentBuffer.capacity() < recordLength) this.recordContentBuffer = ByteBuffer.allocate(recordLength);
            this.recordContentBuffer.limit(recordLength);
            this.recordContentBuffer.rewind();
            this.recordContentBuffer.put(this.recordHeaderBuffer);
            WWIO.readChannelToBuffer(this.shpChannel, this.recordContentBuffer);
            this.numBytesRead += recordLength;
            buffer = this.recordContentBuffer;
        }
        ShapefileRecord record;
        try {
            record = this.readRecordFromBuffer(buffer);
        } finally {
            if (this.mappedShpBuffer != null) this.mappedShpBuffer.limit(this.mappedShpBuffer.capacity());
        }
        return record;
    }

    /**
     * Reads a {@link ShapefileRecord} instance from the given {@link java.nio.ByteBuffer}, or null if the buffer
     * contains a null record.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the shapefile record {@link java.nio.ByteBuffer} to read from.
     *
     * @return a {@link ShapefileRecord} instance.
     */
    protected ShapefileRecord readRecordFromBuffer(ByteBuffer buffer) {
        ShapefileRecord record = this.createRecord(buffer);
        if (record != null) {
            if (this.attributeFile != null && this.attributeFile.hasNext()) {
                record.setAttributes(this.attributeFile.nextRecord());
            }
        }
        return record;
    }

    /**
     * Returns a new <code>{@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}</code> from the specified
     * buffer. The buffer's current position is assumed to be set at the start of the record and will be set to the
     * start of the next record after this method has completed.
     * <p/>
     * This returns an instance of of ShapefileRecord appropriate for the record's shape type. For example, if the
     * record's shape type is <code>SHAPE_POINT</code>, this returns a <code>ShapefileRecordPoint</code>, and if the
     * record's shape type is <code>SHAPE_NULL</code>, this returns <code>ShapefileRecordNull</code>.
     * <p/>
     * This returns <code>null</code> if the record's shape type is not one of the following types:
     * <code>SHAPE_POINT</code>, <code>SHAPE_POINT_M</code>, <code>SHAPE_POINT_Z</code>, <code>SHAPE_MULTI_POINT</code>,
     * <code>SHAPE_MULTI_POINT_M</code>, <code>SHAPE_MULTI_POINT_Z</code>, <code>SHAPE_NULL</code>,
     * <code>SHAPE_POLYGON</code>, <code>SHAPE_POLYGON_M</code>, <code>SHAPE_POLYGON_Z</code>,
     * <code>SHAPE_POLYLINE</code>, <code>SHAPE_POLYLINE_M</code>, <code>SHAPE_POLYLINE_Z</code>.
     *
     * @param buffer the buffer containing the record's content.
     *
     * @return a new {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} instance, <code>null</code> if the
     *         record's shape type is not one of the recognized types.
     */
    protected ShapefileRecord createRecord(ByteBuffer buffer) {
        String shapeType = this.readRecordShapeType(buffer);
        if (isPointType(shapeType)) {
            return this.createPoint(buffer);
        } else if (isMultiPointType(shapeType)) {
            return this.createMultiPoint(buffer);
        } else if (isPolylineType(shapeType)) {
            return this.createPolyline(buffer);
        } else if (isPolygonType(shapeType)) {
            return this.createPolygon(buffer);
        } else if (isNullType(shapeType)) {
            return this.createNull(buffer);
        }
        return null;
    }

    /**
     * Returns a new "null" {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} from the specified buffer.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the buffer containing the point record's content.
     *
     * @return a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     */
    protected ShapefileRecord createNull(ByteBuffer buffer) {
        return new ShapefileRecordNull(this, buffer);
    }

    /**
     * Returns a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} from the specified buffer.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the buffer containing the point record's content.
     *
     * @return a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     */
    protected ShapefileRecord createPoint(ByteBuffer buffer) {
        return new ShapefileRecordPoint(this, buffer);
    }

    /**
     * Returns a new multi-point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} from the specified
     * buffer.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the buffer containing the multi-point record's content.
     *
     * @return a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     */
    protected ShapefileRecord createMultiPoint(ByteBuffer buffer) {
        return new ShapefileRecordMultiPoint(this, buffer);
    }

    /**
     * Returns a new polyline {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} from the specified buffer.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the buffer containing the polyline record's content.
     *
     * @return a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     */
    protected ShapefileRecord createPolyline(ByteBuffer buffer) {
        return new ShapefileRecordPolyline(this, buffer);
    }

    /**
     * Returns a new polygon {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord} from the specified buffer.
     * <p/>
     * The buffer current position is assumed to be set at the start of the record and will be set to the start of the
     * next record after this method has completed.
     *
     * @param buffer the buffer containing the polygon record's content.
     *
     * @return a new point {@link gov.nasa.worldwind.formats.shapefile.ShapefileRecord}.
     */
    protected ShapefileRecord createPolygon(ByteBuffer buffer) {
        return new ShapefileRecordPolygon(this, buffer);
    }

    /**
     * Read and return a record's shape type from a record buffer.
     *
     * @param buffer the record buffer to read from.
     *
     * @return the record's shape type.
     */
    protected String readRecordShapeType(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int type = buffer.getInt(buffer.position() + 2 * 4);
        String shapeType = this.getShapeType(type);
        if (shapeType == null) {
            throw new WWRuntimeException(Logging.getMessage("SHP.UnsupportedShapeType", type));
        }
        return shapeType;
    }

    /**
     * Maps the integer shape type from the shapefile to the corresponding shape type defined above.
     *
     * @param type the integer shape type.
     *
     * @return the mapped shape type.
     */
    protected String getShapeType(int type) {
        switch(type) {
            case 0:
                return SHAPE_NULL;
            case 1:
                return SHAPE_POINT;
            case 3:
                return SHAPE_POLYLINE;
            case 5:
                return SHAPE_POLYGON;
            case 8:
                return SHAPE_MULTI_POINT;
            case 11:
                return SHAPE_POINT_Z;
            case 13:
                return SHAPE_POLYLINE_Z;
            case 15:
                return SHAPE_POLYGON_Z;
            case 18:
                return SHAPE_MULTI_POINT_Z;
            case 21:
                return SHAPE_POINT_M;
            case 23:
                return SHAPE_POLYLINE_M;
            case 25:
                return SHAPE_POLYGON_M;
            case 28:
                return SHAPE_MULTI_POINT_M;
            default:
                return null;
        }
    }

    /**
     * Add point coordinates to the Shapefile starting at the buffer's positions and ending at the specified number of
     * points, and returns an address to the point coordinates in the Shapefile's backing point buffer. Points are read
     * as (X,Y) pairs of 64-bit floating point numbers. This throws an exception if the JVM cannot allocate enough
     * memory to hold the Shapefile's backing point buffer.
     *
     * @param record    the record associated with the point coordinates, may be null.
     * @param buffer    the buffer to read points from.
     * @param numPoints the number of (X,Y) pairs to read.
     *
     * @return the point's address in the Shapefile's backing point buffer.
     */
    protected int addPoints(ShapefileRecord record, ByteBuffer buffer, int numPoints) {
        DoubleBuffer pointBuffer;
        int pos = buffer.position();
        int limit = buffer.position() + 2 * WWBufferUtil.SIZEOF_DOUBLE * numPoints;
        try {
            buffer.limit(limit);
            pointBuffer = this.readPoints(record, buffer);
        } finally {
            buffer.clear();
            buffer.position(limit);
        }
        if (this.mappedShpBuffer != null) {
            if (this.pointBuffer == null) {
                ByteBuffer buf = this.mappedShpBuffer.duplicate();
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf.clear();
                this.pointBuffer = new VecBufferBlocks(2, AVKey.FLOAT64, buf);
            }
            return ((VecBufferBlocks) this.pointBuffer).addBlock(pos, limit - 1);
        } else {
            if (this.pointBuffer == null) {
                int totalPointsEstimate = this.computeNumberOfPointsEstimate();
                DoubleBuffer doubleBuffer;
                try {
                    doubleBuffer = BufferUtil.newDoubleBuffer(2 * totalPointsEstimate);
                } catch (OutOfMemoryError e) {
                    throw new WWRuntimeException(Logging.getMessage("SHP.OutOfMemoryAllocatingPointBuffer", this.getValue(AVKey.DISPLAY_NAME)), e);
                }
                this.pointBuffer = new VecBufferSequence(new VecBuffer(2, new BufferWrapper.DoubleBufferWrapper(doubleBuffer)));
            }
            VecBuffer vecBuffer = new VecBuffer(2, new BufferWrapper.DoubleBufferWrapper(pointBuffer));
            return ((VecBufferSequence) this.pointBuffer).append(vecBuffer);
        }
    }

    /**
     * Estimate the number of points in a shapefile.
     *
     * @return a liberal estimate of the number of points in the shapefile.
     */
    @SuppressWarnings({ "StringEquality" })
    protected int computeNumberOfPointsEstimate() {
        final int numRecords = this.getNumberOfRecords();
        if (numRecords < 0) return (this.getLength() - HEADER_LENGTH) / 16;
        int overhead = HEADER_LENGTH + numRecords * 12;
        String shapeType = this.getShapeType();
        if (shapeType == SHAPE_POINT || shapeType == SHAPE_POINT_M) return (this.getLength() - overhead) / 16;
        if (shapeType == SHAPE_MULTI_POINT || shapeType == SHAPE_MULTI_POINT_M) return (this.getLength() - (overhead + numRecords * (32 + 4))) / 16;
        if (shapeType == SHAPE_POLYLINE || shapeType == SHAPE_POLYGON || shapeType == SHAPE_POLYLINE_M || shapeType == SHAPE_POLYGON_M) return (this.getLength() - (overhead + numRecords * (32 + 8))) / 16;
        if (shapeType == SHAPE_POINT_Z) return (this.getLength() - overhead) / 24;
        if (shapeType == SHAPE_MULTI_POINT_Z) return (this.getLength() - (overhead + numRecords * (48 + 4))) / 24;
        if (shapeType == SHAPE_POLYLINE_Z || shapeType == SHAPE_POLYGON_Z) return (this.getLength() - (overhead + numRecords * (48 + 8))) / 24;
        throw new WWRuntimeException(Logging.getMessage("SHP.UnsupportedShapeType", shapeType));
    }

    /**
     * Returns a {@link java.nio.DoubleBuffer} containing the (X,Y) tuples between the buffer's position and its limit.
     * This returns null if the buffer is null or if the buffer has no remaining elements. The returned coordinates are
     * interpreted according to the Shapefile's coordinate system. This throws a {@link
     * gov.nasa.worldwind.exception.WWRuntimeException} if the coordinate system is unsupported.
     * <p/>
     * The buffer current position is assumed to be set at the start of the point data and will be set to the end of the
     * point data after this method has completed.
     *
     * @param record the record associated with the point coordinates, may be null.
     * @param buffer the buffer to read point coordinates from.
     *
     * @return a buffer containing the point coordinates.
     *
     * @throws WWRuntimeException if the Shapefile's coordinate system is unsupported.
     */
    protected DoubleBuffer readPoints(ShapefileRecord record, ByteBuffer buffer) {
        if (buffer == null || !buffer.hasRemaining()) return null;
        Object o = this.getValue(AVKey.COORDINATE_SYSTEM);
        if (!this.hasKey(AVKey.COORDINATE_SYSTEM)) return this.readUnspecifiedPoints(record, buffer); else if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(o)) return this.readGeographicPoints(record, buffer); else if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(o)) return this.readProjectedPoints(record, buffer); else {
            throw new WWRuntimeException(Logging.getMessage("generic.UnsupportedCoordinateSystem", o));
        }
    }

    /**
     * Returns a {@link java.nio.DoubleBuffer} containing the (X,Y) tuples between the buffer's position and its limit.
     * The coordinates are assumed to be in an unspecified coordinate system and are not changed.
     *
     * @param record the record associated with the point coordinates, may be null.
     * @param buffer the buffer to read point coordinates from.
     *
     * @return a buffer containing the point coordinates.
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected DoubleBuffer readUnspecifiedPoints(ShapefileRecord record, ByteBuffer buffer) {
        return buffer.asDoubleBuffer();
    }

    /**
     * Returns a {@link java.nio.DoubleBuffer} containing the geographic (longitude, latitude) tuples between the
     * buffer's position and its limit. This normalizes the geographic coordinates to the range +-90 latitude and +-180
     * longitude if the record is non-null and {@link ShapefileRecord#isNormalizePoints()} returns <code>true</code>.
     *
     * @param record the record associated with the point coordinates, may be null.
     * @param buffer the buffer to read point coordinates from.
     *
     * @return a buffer containing the geographic point coordinates.
     */
    protected DoubleBuffer readGeographicPoints(ShapefileRecord record, ByteBuffer buffer) {
        DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
        if (record != null && record.isNormalizePoints()) {
            WWUtil.normalizeGeographicCoordinates(doubleBuffer);
            doubleBuffer.rewind();
        }
        return doubleBuffer;
    }

    /**
     * Returns a {@link java.nio.DoubleBuffer} containing the projected (X,Y) tuples between the buffer's position and
     * its limit, converted to geographic coordinates (latitude,longitude). The returned coordinates are interpreted
     * according to the Shapefile's projection. This throws a {@link gov.nasa.worldwind.exception.WWRuntimeException} if
     * the projection is unsupported.
     *
     * @param record the record associated with the point coordinates, may be null.
     * @param buffer the buffer to read point coordinates from.
     *
     * @return a buffer containing geographic point coordinates converted form projected coordinates.
     *
     * @throws WWRuntimeException if the Shapefile's projection is unsupported.
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected DoubleBuffer readProjectedPoints(ShapefileRecord record, ByteBuffer buffer) {
        Object o = this.getValue(AVKey.PROJECTION_NAME);
        if (AVKey.PROJECTION_UTM.equals(o)) {
            Integer zone = (Integer) this.getValue(AVKey.PROJECTION_ZONE);
            String hemisphere = (String) this.getValue(AVKey.PROJECTION_HEMISPHERE);
            DoubleBuffer doubleBuffer = buffer.asDoubleBuffer();
            WWUtil.convertUTMCoordinatesToGeographic(zone, hemisphere, doubleBuffer);
            doubleBuffer.rewind();
            return doubleBuffer;
        } else {
            throw new WWRuntimeException(Logging.getMessage("generic.UnsupportedProjection", o));
        }
    }

    /**
     * Stores a bounding rectangle's coordinates, and if the coordinates are normalized. If <code>isNormalized</code> is
     * <code>true</code>, this indicates that the original coordinate values are out of range and required
     * normalization. The shapefile and shapefile records use this to determine which records must have their point
     * coordinates normalized. Normalization is rarely needed, and this enables the shapefile to normalize only point
     * coordinates associated with records that require it.
     */
    protected static class BoundingRectangle {

        /** Four-element array of the bounding rectangle's coordinates, ordered as follows: (minY, maxY, minX, maxX). */
        public double[] coords;

        /** True if the coordinates are normalized, and false otherwise. */
        public boolean isNormalized;
    }

    /**
     * Returns a bounding rectangle from the specified buffer. This reads four doubles and interprets them as a bounding
     * rectangle in the following order: (minX, minY, maxX, maxY). The returned rectangle's coordinates are interpreted
     * according to the Shapefile's coordinate system. This throws a {@link gov.nasa.worldwind.exception.WWRuntimeException}
     * if the coordinate system is unsupported.
     *
     * @param buffer the buffer to read from.
     *
     * @return a bounding rectangle with coordinates from the specified buffer.
     */
    protected BoundingRectangle readBoundingRectangle(ByteBuffer buffer) {
        Object o = this.getValue(AVKey.COORDINATE_SYSTEM);
        if (!this.hasKey(AVKey.COORDINATE_SYSTEM)) return this.readUnspecifiedBoundingRectangle(buffer); else if (AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(o)) return this.readGeographicBoundingRectangle(buffer); else if (AVKey.COORDINATE_SYSTEM_PROJECTED.equals(o)) return this.readProjectedBoundingRectangle(buffer); else {
            throw new WWRuntimeException(Logging.getMessage("generic.UnsupportedCoordinateSystem", o));
        }
    }

    /**
     * Returns a bounding rectangle from the specified buffer. This reads four doubles and interprets them as a bounding
     * rectangle in the following order: (minX, minY, maxX, maxY). The coordinates are assumed to be in an unspecified
     * coordinate system and are not changed.
     *
     * @param buffer the buffer to read bounding rectangle coordinates from.
     *
     * @return a bounding rectangle with coordinates from the specified buffer. The rectangle's coordinates are ordered
     *         as follows: (minY, maxY, minX, maxX).
     */
    protected BoundingRectangle readUnspecifiedBoundingRectangle(ByteBuffer buffer) {
        BoundingRectangle rect = new BoundingRectangle();
        rect.coords = this.readBoundingRectangleCoordinates(buffer);
        return rect;
    }

    /**
     * Returns a bounding rectangle from the specified buffer. This reads four doubles and interprets them as a
     * Geographic bounding rectangle in the following order: (minLat, maxLat, minLon, maxLon). If any of the coordinates
     * are out of the range -90/+90 latitude and -180/+180 longitude, this normalizes the coordinates and sets the
     * rectangle's {@link gov.nasa.worldwind.formats.shapefile.Shapefile.BoundingRectangle#isNormalized} property to
     * <code>true</code>.
     *
     * @param buffer the buffer to read bounding rectangle coordinates from.
     *
     * @return a bounding rectangle with coordinates from the specified buffer. The rectangle's coordinates are ordered
     *         as follows: (minLat, maxLat, minLon, maxLon).
     */
    protected BoundingRectangle readGeographicBoundingRectangle(ByteBuffer buffer) {
        BoundingRectangle rect = new BoundingRectangle();
        rect.coords = this.readBoundingRectangleCoordinates(buffer);
        if (rect.coords[0] < -90) {
            double normalizedLat = Angle.normalizedLatitude(Angle.fromDegrees(rect.coords[0])).degrees;
            rect.coords[0] = 90;
            rect.isNormalized = true;
            if (rect.coords[1] < normalizedLat) rect.coords[1] = normalizedLat;
        }
        if (rect.coords[1] > 90) {
            double normalizedLat = Angle.normalizedLatitude(Angle.fromDegrees(rect.coords[1])).degrees;
            rect.coords[1] = 90;
            rect.isNormalized = true;
            if (rect.coords[0] > normalizedLat) rect.coords[0] = normalizedLat;
        }
        if (rect.coords[2] < -180 || rect.coords[3] > 180) {
            rect.coords[2] = -180;
            rect.coords[3] = 180;
            rect.isNormalized = true;
        }
        return rect;
    }

    /**
     * Returns a bounding rectangle from the specified buffer. This reads four doubles and interprets them as a
     * projected bounding rectangle in the following order: (minX, maxX, minY, maxY). The projected rectangle is
     * converted to geographic coordinates before the rectangle is returned. The returned coordinates are interpreted
     * according to the Shapefile's projection. This throws a {@link gov.nasa.worldwind.exception.WWRuntimeException} if
     * the projection is unsupported.
     *
     * @param buffer the buffer to read bounding rectangle coordinates from.
     *
     * @return a bounding rectangle with coordinates from the specified buffer. The rectangle's coordinates are ordered
     *         as follows: (minLat, maxLat, minLon, maxLon).
     *
     * @throws WWRuntimeException if the Shapefile's projection is unsupported.
     */
    protected BoundingRectangle readProjectedBoundingRectangle(ByteBuffer buffer) {
        Object o = this.getValue(AVKey.PROJECTION_NAME);
        if (AVKey.PROJECTION_UTM.equals(o)) {
            double[] coords = ShapefileUtils.readDoubleArray(buffer, 4);
            Integer zone = (Integer) this.getValue(AVKey.PROJECTION_ZONE);
            String hemisphere = (String) this.getValue(AVKey.PROJECTION_HEMISPHERE);
            Sector sector = Sector.fromUTMRectangle(zone, hemisphere, coords[0], coords[2], coords[1], coords[3]);
            BoundingRectangle rect = new BoundingRectangle();
            rect.coords = sector.toArrayDegrees();
            return rect;
        } else {
            throw new WWRuntimeException(Logging.getMessage("generic.UnsupportedProjection", o));
        }
    }

    /**
     * Reads a Shapefile bounding rectangle from the specified buffer. This reads four doubles and returns them as a
     * four-element array in the following order: (minY, maxY, minX, maxX). This ordering is consistent with the
     * ordering expected by {@link gov.nasa.worldwind.geom.Sector#fromDegrees(double[])}.
     *
     * @param buffer the buffer to read from.
     *
     * @return a four-element array ordered as follows: (minY, maxY, minX, maxX).
     */
    protected double[] readBoundingRectangleCoordinates(ByteBuffer buffer) {
        double minx = buffer.getDouble();
        double miny = buffer.getDouble();
        double maxx = buffer.getDouble();
        double maxy = buffer.getDouble();
        return new double[] { miny, maxy, minx, maxx };
    }

    /**
     * Indicates whether a specified shape type may contain optional measure values.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is one that may contain measure values.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isMeasureType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return measureTypes.contains(shapeType);
    }

    /**
     * Indicates whether a specified shape type contains Z values.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is one that contains Z values.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isZType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return zTypes.contains(shapeType);
    }

    /**
     * Indicates whether a specified shape type is {@link #SHAPE_NULL}.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is a null type.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isNullType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return shapeType.equals(Shapefile.SHAPE_NULL);
    }

    /**
     * Indicates whether a specified shape type is either {@link #SHAPE_POINT}, {@link #SHAPE_POINT_M} or {@link
     * #SHAPE_POINT_Z}.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is a point type.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isPointType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return shapeType.equals(Shapefile.SHAPE_POINT) || shapeType.equals(Shapefile.SHAPE_POINT_Z) || shapeType.equals(Shapefile.SHAPE_POINT_M);
    }

    /**
     * Indicates whether a specified shape type is either {@link #SHAPE_MULTI_POINT}, {@link #SHAPE_MULTI_POINT_M} or
     * {@link #SHAPE_MULTI_POINT_Z}.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is a mulit-point type.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isMultiPointType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return shapeType.equals(Shapefile.SHAPE_MULTI_POINT) || shapeType.equals(Shapefile.SHAPE_MULTI_POINT_Z) || shapeType.equals(Shapefile.SHAPE_MULTI_POINT_M);
    }

    /**
     * Indicates whether a specified shape type is either {@link #SHAPE_POLYLINE}, {@link #SHAPE_POLYLINE_M} or {@link
     * #SHAPE_POLYLINE_Z}.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is a polyline type.
     *
     * @throws IllegalArgumentException if <code>shapeType</code> is null.
     */
    public static boolean isPolylineType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return shapeType.equals(Shapefile.SHAPE_POLYLINE) || shapeType.equals(Shapefile.SHAPE_POLYLINE_Z) || shapeType.equals(Shapefile.SHAPE_POLYLINE_M);
    }

    /**
     * Indicates whether a specified shape type is either {@link #SHAPE_POLYGON}, {@link #SHAPE_POLYGON_M} or {@link
     * #SHAPE_POLYGON_Z}.
     *
     * @param shapeType the shape type to analyze.
     *
     * @return true if the shape type is a polygon type.
     */
    public static boolean isPolygonType(String shapeType) {
        if (shapeType == null) {
            String message = Logging.getMessage("nullValue.ShapeType");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return shapeType.equals(Shapefile.SHAPE_POLYGON) || shapeType.equals(Shapefile.SHAPE_POLYGON_Z) || shapeType.equals(Shapefile.SHAPE_POLYGON_M);
    }

    public String isExportFormatSupported(String mimeType) {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType)) return FORMAT_SUPPORTED;
        return Arrays.binarySearch(SHAPE_CONTENT_TYPES, mimeType) >= 0 ? FORMAT_SUPPORTED : FORMAT_NOT_SUPPORTED;
    }

    public void export(String mimeType, Object output) throws IOException, UnsupportedOperationException {
        if (mimeType == null) {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (output == null) {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            this.doExport(mimeType, output);
        } catch (XMLStreamException e) {
            Logging.logger().throwing(getClass().getName(), "export", e);
            throw new IOException(e);
        }
    }

    protected void doExport(String mimeType, Object output) throws IOException, XMLStreamException {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;
        if (output instanceof XMLStreamWriter) {
            xmlWriter = (XMLStreamWriter) output;
            closeWriterWhenFinished = false;
        } else if (output instanceof Writer) {
            xmlWriter = factory.createXMLStreamWriter((Writer) output);
        } else if (output instanceof OutputStream) {
            xmlWriter = factory.createXMLStreamWriter((OutputStream) output);
        }
        if (xmlWriter == null) {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }
        if (KMLConstants.KML_MIME_TYPE.equals(mimeType)) exportAsKML(xmlWriter); else exportAsXML(xmlWriter);
        xmlWriter.flush();
        if (closeWriterWhenFinished) xmlWriter.close();
    }

    protected void exportAsXML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
        xmlWriter.writeStartElement("Shapefile");
        xmlWriter.writeCharacters("\n");
        while (this.hasNext()) {
            try {
                ShapefileRecord nr = this.nextRecord();
                if (nr == null) continue;
                nr.exportAsXML(xmlWriter);
                xmlWriter.writeCharacters("\n");
            } catch (Exception e) {
                String message = Logging.getMessage("Export.Exception.ShapefileRecord");
                Logging.logger().log(Level.WARNING, message, e);
                continue;
            }
        }
        xmlWriter.writeEndElement();
    }

    protected void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException {
        while (this.hasNext()) {
            try {
                ShapefileRecord nr = this.nextRecord();
                if (nr == null) continue;
                nr.exportAsKML(xmlWriter);
            } catch (Exception e) {
                String message = Logging.getMessage("Export.Exception.ShapefileRecord");
                Logging.logger().log(Level.WARNING, message, e);
                continue;
            }
        }
    }

    public void printInfo(boolean printCoordinates) {
        while (this.hasNext()) {
            this.nextRecord().printInfo(printCoordinates);
        }
    }
}
