package net.sourceforge.magex.preparation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Rectangle represents one map division rectangle at run-time.
 */
public class Rectangle {

    /** Filename separator for map object data */
    private static final String OBJDATA_SEP = ".";

    /** Filename suffix for map object data */
    private static final String OBJDATA_SUFF = ".d";

    /** All the POIs that are contained within this Rectangle */
    public Vector<MapObject> pois;

    /** All the Polylines that are contained within this Rectangle */
    public Vector<MapCoords> polylines;

    /** All the Polygons that are contained within this Rectangle */
    public Vector<MapArea> polygons;

    /** The X-coordinate of the upper-left corner of this Rectangle (in 100000-multiply of the value in degrees) */
    int coordX;

    /** The Y-coordinate of the upper-left corner of this Rectangle (in 100000-multiply of the value in degrees) */
    int coordY;

    /** The depth level, to which this Rectangle should belong. */
    byte depthLevel;

    /** 
     * Creates an empty Rectangle, for the data to be added later.
     * 
     * @param depthLevel the zoom depth level of this Rectangle
     * @param coordX the X-coordinate of the upper left corner of this Rectangle
     * @param coordY the Y-coordinate of the upper left corner of this Rectangle
     */
    public Rectangle(byte depthLevel, int coordX, int coordY) {
        this.pois = new Vector<MapObject>();
        this.polylines = new Vector<MapCoords>();
        this.polygons = new Vector<MapArea>();
        this.depthLevel = depthLevel;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    /**
     * Creates a ZipEntry with the correct name and writes all the data into this 
     * ZIP output stream.
     *
     * @param mapId the id of the map for the entry name to be prefixed 
     * @param out the output stream to write into
     * @return the size of all the data written
     */
    public int writeAllData(int mapId, ZipOutputStream out) throws IOException {
        int bytesWritten = 0;
        DataOutputStream ds;
        String name;
        out.putNextEntry(new ZipEntry(name = Process.MAP_DIR_PREFIX + Integer.toString(mapId, 16) + Process.MAP_DIR_SEP + this.depthLevel + OBJDATA_SEP + Integer.toString(this.coordX, 16) + OBJDATA_SEP + Integer.toString(this.coordY, 16) + OBJDATA_SUFF));
        ds = new DataOutputStream(out);
        for (int i = 0; i < this.polygons.size(); ++i) {
            bytesWritten += this.polygons.elementAt(i).write(ds);
        }
        for (int i = 0; i < this.polylines.size(); ++i) {
            bytesWritten += this.polylines.elementAt(i).write(ds);
        }
        for (int i = 0; i < this.pois.size(); ++i) {
            bytesWritten += this.pois.elementAt(i).write(ds);
        }
        ds.flush();
        out.closeEntry();
        return bytesWritten;
    }

    /**
     * Adds an object to the rectangle. Sorts the objects by their data type and
     * adds them to the appropriate group. The MapArea objects are sorted by their
     * area.
     *
     * @param obj the object to be added
     */
    public void add(MapObject obj) {
        if (obj.dataType == MapObject.POI) {
            this.pois.add(obj);
        } else if (obj.dataType == MapObject.POLYLINE) {
            this.polylines.add((MapCoords) obj);
        } else {
            this.polygons.add((MapArea) obj);
            this.sortInsertedPolygon();
        }
    }

    /**
     * Returns a string representation of all the objects in this rectangle in Polish format,
     * mainly for debugging purposes.
     *
     * @return the contents of this Rectangle in Polish format
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.polygons.size(); ++i) {
            sb.append(this.polygons.elementAt(i).toString());
            sb.append("\n\n");
        }
        for (int i = 0; i < this.polylines.size(); ++i) {
            sb.append(this.polylines.elementAt(i).toString());
            sb.append("\n\n");
        }
        for (int i = 0; i < this.pois.size(); ++i) {
            sb.append(this.pois.elementAt(i).toString());
            sb.append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Sorts the last-inserted polygon into the polygons field, sorting by area
     * (biggest first).
     */
    private void sortInsertedPolygon() {
        int i = this.polygons.size() - 1;
        while (i > 0 && this.polygons.elementAt(i - 1).area < this.polygons.elementAt(i).area) {
            this.polygons.set(i, this.polygons.set(i - 1, this.polygons.get(i)));
            --i;
        }
    }
}
