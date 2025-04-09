package mil.usmc.mgrs;

import mil.usmc.mgrs.objects.BoundingBox;
import mil.usmc.mgrs.objects.Grid;
import mil.usmc.mgrs.objects.Point;
import mil.usmc.mgrs.objects.Line;
import mil.usmc.mgrs.objects.GridCell;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mil.geotransj.GeoTransException;
import mil.geotransj.Geodetic;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create XML for grid lines.
 * @author Alex Pastel
 */
public class MgrsXmlResource {

    private static final Logger LOGGER = Logger.getLogger(MgrsXmlResource.class.getName());

    private static MgrsHelper mgrsHelper = new MgrsHelper();

    public static BoundingBox m_bbox;

    private static boolean UPDATE_XML_ON_DISK = true;

    private static final boolean ZONE_GRID_ON = true;

    private static final boolean HUNDRED_KM_GRID_ON = true;

    private static final boolean TEN_KM_GRID_ON = false;

    static final String ZONE_GRID = "ZoneGrid";

    static final String HUNDRED_KM_GRID = "HundredKmGrid";

    static final String TEN_KM_GRID = "TenKmGrid";

    private static final String BBOX_QUERY_PARAMETER_NAME = "BBOX";

    private ArrayList<GridCell> ZONE_CELLS = null;

    static GridlineXmlPathProvider MGRS_XML_PATH_PROVIDER = new GridlineXmlPathProvider() {

        @Override
        public File get() {
            return new File("/var/mgrs");
        }
    };

    interface GridlineXmlPathProvider {

        public File get();
    }

    public Document makeMgrsXml(double span) throws ParserConfigurationException, DOMException, GeoTransException {
        MgrsHelper.SUPPRESS_EXCEPTIONS = true;
        DocumentBuilder db = makeDocBuilder();
        Document doc = db.newDocument();
        Element bbox = doc.createElement("BBOX");
        bbox.setAttribute("value", m_bbox.toString());
        doc.appendChild(bbox);
        Element mgrs = doc.createElement("MGRS");
        bbox.appendChild(mgrs);
        if (span <= 125 && ZONE_GRID_ON) {
            createGridXml(doc, ZONE_GRID);
        }
        if (span <= 25 && HUNDRED_KM_GRID_ON) {
            createGridXml(doc, HUNDRED_KM_GRID);
        }
        if (span <= 2 && TEN_KM_GRID_ON) {
            createGridXml(doc, TEN_KM_GRID);
        }
        maybeWriteToDisk(doc);
        return doc;
    }

    private void createGridXml(Document doc, String gridName) throws DOMException {
        Element gridEl = doc.createElement(gridName);
        doc.getElementsByTagName("MGRS").item(0).appendChild(gridEl);
        Grid grid = null;
        grid = makeGrid(gridName);
        if (grid == null) {
            LOGGER.log(Level.SEVERE, "{0} is null", gridName);
            return;
        }
        Element latLineEl = doc.createElement("LatLines");
        Element lngLineEl = doc.createElement("LngLines");
        for (Line latLine : grid.getLatLines()) {
            Element lineEl = doc.createElement("Line");
            lineEl.setAttribute("start", latLine.getStart().toString());
            lineEl.setAttribute("end", latLine.getEnd().toString());
            if (latLine.getLabelPoint() != null) {
                lineEl.setAttribute("lblPnt", latLine.getLabelPoint().toString());
            }
            if (latLine.getLabelName() != null) {
                lineEl.setAttribute("lblName", latLine.getLabelName());
            }
            latLineEl.appendChild(lineEl);
        }
        for (Line lngLine : grid.getLngLines()) {
            if (lngLine == null || skipNorwayLines(lngLine)) {
                continue;
            }
            Element lineEl = doc.createElement("Line");
            lineEl.setAttribute("start", lngLine.getStart().toString());
            lineEl.setAttribute("end", lngLine.getEnd().toString());
            if (lngLine.getLabelPoint() != null) {
                lineEl.setAttribute("lblPnt", lngLine.getLabelPoint().toString());
            }
            if (lngLine.getLabelName() != null) {
                lineEl.setAttribute("lblName", lngLine.getLabelName());
            }
            lngLineEl.appendChild(lineEl);
        }
        gridEl.appendChild(latLineEl);
        gridEl.appendChild(lngLineEl);
    }

    public Grid makeMgrsGrid(String gridName) {
        if (gridName != ZONE_GRID) {
            makeZoneGrid();
        }
        return makeGrid(gridName);
    }

    private Grid makeGrid(String gridName) {
        LOGGER.log(Level.INFO, "Create lines for {0}\n", gridName);
        if (gridName.equals(ZONE_GRID)) {
            return makeZoneGrid();
        }
        if (gridName.equals(HUNDRED_KM_GRID)) {
            return makeHundredKmGrid();
        }
        if (gridName.equals(TEN_KM_GRID)) {
            return makeTenKmGrid();
        }
        return null;
    }

    private Grid makeZoneGrid() {
        GridCell gridCell = null;
        ArrayList<GridCell> gridCells = new ArrayList<GridCell>();
        ZONE_CELLS = new ArrayList<GridCell>();
        double sLat = m_bbox.getMinLat();
        double wLng = m_bbox.getMinLon();
        double nLat = m_bbox.getMaxLat();
        double eLng = m_bbox.getMaxLon();
        if (wLng < 12 && eLng > 335) {
            wLng = -180;
            eLng = 180;
        }
        double x1 = (Math.floor((wLng / 6) + 1) * 6.0);
        double y1;
        if (sLat < -80) {
            y1 = -80;
        } else {
            y1 = (Math.floor((sLat / 8) + 1) * 8.0);
        }
        ArrayList<Double> lat_coords = new ArrayList<Double>();
        ArrayList<Double> lng_coords = new ArrayList<Double>();
        if (sLat < -80) {
            lat_coords.add(0, -80.0);
        } else {
            sLat = Math.floor(sLat / 8) * 8;
            lat_coords.add(0, sLat);
        }
        int j;
        double lat, lng;
        if (nLat > 80) {
            nLat = 80;
        }
        for (lat = y1, j = 1; lat < nLat; lat += 8, j++) {
            if (lat <= 72) {
                lat_coords.add(j, lat);
            } else if (lat <= 80) {
                lat_coords.add(j, 84.0);
            } else {
                j--;
            }
        }
        nLat = Math.ceil(nLat / 8) * 8;
        lat_coords.add(j, nLat);
        wLng = Math.floor(wLng / 6) * 6;
        lng_coords.add(0, wLng);
        if (wLng < eLng) {
            for (lng = x1, j = 1; lng < eLng; lng += 6, j++) {
                lng_coords.add(j, lng);
            }
        } else {
            for (lng = x1, j = 1; lng <= 180; lng += 6, j++) {
                lng_coords.add(j, lng);
            }
            for (lng = -180; lng < eLng; lng += 6, j++) {
                lng_coords.add(j, lng);
            }
        }
        eLng = Math.ceil(eLng / 6) * 6;
        lng_coords.add(j++, eLng);
        for (int i = 0; i < lat_coords.size() - 1; i++) {
            for (j = 0; j < lng_coords.size() - 1; j++) {
                double latA = lat_coords.get(i);
                double latB = lat_coords.get(i + 1);
                double lngA = lng_coords.get(j);
                double lngB = lng_coords.get(j + 1);
                Point sw = new Point(latA, lngA);
                Point se = new Point(latA, lngB);
                Point ne = new Point(latB, lngB);
                Point nw = new Point(latB, lngA);
                gridCell = new GridCell(sw, se, ne, nw);
                if (latA != latB) {
                    if (latA == 56 && lngA == 6) {
                        lngA = 3;
                    } else if (latA == 56 && lngA == 0) {
                        lngB = 3;
                    }
                    double centerLat = (latA + latB) / 2;
                    double centerLng = (lngA + lngB) / 2;
                    Point center = new Point(centerLat, centerLng);
                    String name = getGridLabelFromPoint(center);
                    if (name != null) {
                        gridCell.setLabel(center, name.substring(0, 3));
                    }
                }
                gridCells.add(gridCell);
                if (gridCell.isFullSized()) {
                    ZONE_CELLS.add(gridCell);
                }
            }
        }
        Grid grid = new Grid(gridCells);
        ArrayList<Line> lines = handleSpecialCases(lat_coords, lng_coords);
        grid.getLngLines().addAll(lines);
        return grid;
    }

    private Grid makeHundredKmGrid() {
        ArrayList<Line> latLines = new ArrayList<Line>();
        ArrayList<Line> lngLines = new ArrayList<Line>();
        for (GridCell zoneCell : ZONE_CELLS) {
            double sLat = zoneCell.m_sw.getLat();
            double wLng = zoneCell.m_sw.getLng();
            double nLat = zoneCell.m_ne.getLat();
            double eLng = zoneCell.m_ne.getLng();
            String mgrsSW = mgrsHelper.geoToMgrs(sLat, wLng, MgrsHelper.MAX_PRECISION);
            String mgrsSE = mgrsHelper.geoToMgrs(sLat, eLng, MgrsHelper.MAX_PRECISION);
            String mgrsNW = mgrsHelper.geoToMgrs(nLat, wLng, MgrsHelper.MAX_PRECISION);
            String mgrsNE = mgrsHelper.geoToMgrs(nLat, eLng, MgrsHelper.MAX_PRECISION);
            if (mgrsSW == null || mgrsSE == null || mgrsNW == null || mgrsNE == null) {
                continue;
            }
            String mgrsStart = mgrsSW;
            String mgrsEnd = mgrsSE;
            char gridRow = mgrsSW.charAt(2);
            boolean flag = true;
            while (mgrsHelper.isValid(mgrsStart)) {
                mgrsHelper.setMgrs(mgrsStart);
                mgrsHelper.setGridSquareRow(mgrsHelper.getNextGridSquareRow());
                mgrsHelper.setNorthing(0);
                mgrsStart = mgrsHelper.toString();
                mgrsHelper.setMgrs(mgrsEnd);
                mgrsHelper.setGridSquareRow(mgrsHelper.getNextGridSquareRow());
                mgrsHelper.setNorthing(0);
                mgrsEnd = mgrsHelper.toString();
                if (!mgrsHelper.isValid(mgrsStart) || !mgrsHelper.isValid(mgrsEnd)) {
                    break;
                }
                if (mgrsStart == null) {
                    break;
                }
                mgrsHelper.setMgrs(mgrsStart);
                Geodetic geo = mgrsHelper.mgrsToGeo(mgrsHelper.toString());
                double lat;
                if (geo == null) {
                    break;
                }
                lat = geo.getLatitude();
                String easting = mgrsHelper.geoToMgrs(lat, wLng, MgrsHelper.MAX_PRECISION);
                if (easting == null) {
                    break;
                }
                easting = easting.substring(5, 10);
                mgrsHelper.setEasting(Integer.parseInt(easting));
                geo = mgrsHelper.mgrsToGeo(mgrsHelper.toString());
                if (geo == null) {
                    break;
                }
                if ((gridRow == 'J' || gridRow == 'F') && Integer.parseInt(easting) > 90000 && flag) {
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getPreviousGridSquareColumn());
                } else if ((gridRow == 'R' && geo.getLatitude() > 26 && flag) || (gridRow == 'U' && geo.getLatitude() > 54 && flag)) {
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                }
                mgrsStart = mgrsHelper.toString();
                if (mgrsEnd == null) {
                    break;
                }
                mgrsHelper.setMgrs(mgrsEnd);
                geo = mgrsHelper.mgrsToGeo(mgrsHelper.toString());
                if (geo != null) {
                    lat = geo.getLatitude();
                } else {
                    continue;
                }
                easting = mgrsHelper.geoToMgrs(lat, eLng, MgrsHelper.MAX_PRECISION);
                if (easting != null) {
                    easting = easting.substring(5, 10);
                } else {
                    continue;
                }
                mgrsHelper.setEasting(Integer.parseInt(easting));
                geo = mgrsHelper.mgrsToGeo(mgrsHelper.toString());
                if (geo == null) {
                    continue;
                }
                if ((gridRow == 'J' || gridRow == 'F') && Integer.parseInt(easting) > 90000 && flag) {
                    flag = false;
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getPreviousGridSquareColumn());
                } else if ((gridRow == 'R' && geo.getLatitude() > 26 && flag) || (gridRow == 'U' && geo.getLatitude() > 54 && flag)) {
                    flag = false;
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                }
                mgrsEnd = mgrsHelper.toString();
                mgrsHelper.setMgrs(mgrsStart);
                while (true) {
                    String start = mgrsHelper.toString();
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                    mgrsHelper.setEasting(0);
                    String end = mgrsHelper.toString();
                    geo = mgrsHelper.mgrsToGeo(end);
                    double endLng = Double.MIN_NORMAL;
                    if (geo != null) {
                        endLng = geo.getLongitude();
                    }
                    if (!mgrsHelper.isValid(end) || endLng > eLng) {
                        geo = mgrsHelper.mgrsToGeo(mgrsStart);
                        if (geo == null) {
                            break;
                        }
                        end = mgrsHelper.geoToMgrs(geo.getLatitude(), eLng, MgrsHelper.MAX_PRECISION);
                        if (end == null) {
                            break;
                        }
                        Line line = new Line(start, end);
                        Point label = line.calculateLabelPoint();
                        String name = getGridLabelFromPoint(label);
                        if (name != null) {
                            line.setLabel(label, name.substring(3, 5));
                        }
                        latLines.add(line);
                        break;
                    }
                    Line line = new Line(start, end);
                    Point label = line.calculateLabelPoint();
                    String name = getGridLabelFromPoint(label);
                    if (name != null) {
                        line.setLabel(label, name.substring(3, 5));
                    }
                    latLines.add(line);
                }
            }
            mgrsStart = mgrsSW;
            mgrsEnd = mgrsNW;
            flag = true;
            boolean xFlag = true;
            while (true) {
                boolean flag2 = true;
                String temp = mgrsStart;
                mgrsHelper.setMgrs(mgrsStart);
                mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                mgrsHelper.setEasting(0);
                mgrsStart = mgrsHelper.toString();
                if (!xFlag) {
                    break;
                }
                if (gridRow <= 'M' && gridRow != 'C') {
                    if (gridRow == 'J') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 1));
                    }
                    mgrsStart = mgrsHelper.toString();
                }
                if (!mgrsHelper.isValid(mgrsStart)) {
                    break;
                }
                if ((gridRow == 'R' || gridRow == 'U' || gridRow == 'X') && flag) {
                    flag = false;
                    Line line = constructMergeLineN(mgrsStart, gridRow, wLng, eLng, true);
                    lngLines.add(line);
                    continue;
                }
                mgrsHelper.setMgrs(mgrsEnd);
                mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                mgrsHelper.setEasting(0);
                mgrsEnd = mgrsHelper.toString();
                if (gridRow <= 'M') {
                    if (mgrsEnd.charAt(2) == 'J') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 1));
                    }
                    mgrsEnd = mgrsHelper.toString();
                }
                if (!mgrsHelper.isValid(mgrsEnd)) {
                    break;
                }
                if ((gridRow == 'J' || gridRow == 'F' || gridRow == 'C') && flag) {
                    flag = false;
                    flag2 = false;
                    Line line = constructMergeLineSLeft(mgrsEnd, gridRow, wLng);
                    lngLines.add(line);
                    mgrsStart = temp;
                } else if (xFlag) {
                    Line line = new Line(mgrsStart, mgrsEnd);
                    lngLines.add(line);
                }
                if (gridRow == 'X') {
                    xFlag = false;
                }
                if (gridRow <= 'M') {
                    if (flag2) {
                        mgrsHelper.setMgrs(mgrsStart);
                        if (mgrsStart.charAt(2) == 'H') {
                            mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 2));
                        } else {
                            mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 1));
                        }
                        mgrsStart = mgrsHelper.toString();
                    }
                    mgrsHelper.setMgrs(mgrsEnd);
                    if (mgrsEnd.charAt(2) == 'H') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 1));
                    }
                    mgrsEnd = mgrsHelper.toString();
                }
            }
            if (gridRow == 'R' || gridRow == 'U' || gridRow == 'X') {
                Line line = constructMergeLineN(mgrsStart, gridRow, wLng, eLng, false);
                lngLines.add(line);
            } else if (gridRow == 'J' || gridRow == 'F' || gridRow == 'C') {
                Line line = constructMergeLineSRight(mgrsEnd, gridRow, eLng);
                lngLines.add(line);
            }
        }
        return new Grid(latLines, lngLines);
    }

    private Grid makeTenKmGrid() {
        ArrayList<Line> latLines = new ArrayList<Line>();
        ArrayList<Line> lngLines = new ArrayList<Line>();
        for (GridCell zoneCell : ZONE_CELLS) {
            double sLat = zoneCell.m_sw.getLat();
            double wLng = zoneCell.m_sw.getLng();
            double nLat = zoneCell.m_ne.getLat();
            double eLng = zoneCell.m_ne.getLng();
            String mgrsSW = mgrsHelper.geoToMgrs(sLat, wLng, MgrsHelper.MAX_PRECISION);
            String mgrsSE = mgrsHelper.geoToMgrs(sLat, eLng, MgrsHelper.MAX_PRECISION);
            String mgrsNW = mgrsHelper.geoToMgrs(nLat, wLng, MgrsHelper.MAX_PRECISION);
            String mgrsNE = mgrsHelper.geoToMgrs(nLat, eLng, MgrsHelper.MAX_PRECISION);
            if (mgrsSW == null || mgrsSE == null || mgrsNW == null || mgrsNE == null) {
                continue;
            }
            String mgrsStart = mgrsSW;
            String mgrsEnd = mgrsSE;
            char gridRow = mgrsSW.charAt(2);
            boolean f = true;
            while (mgrsHelper.isValid(mgrsStart)) {
                mgrsHelper.setMgrs(mgrsStart);
                int northing = mgrsHelper.getNorthing();
                if (northing % 10000 == 0) {
                    if (northing < 90000) {
                        northing += 10000;
                    } else {
                        northing = 0;
                        mgrsHelper.setGridSquareRow(mgrsHelper.getNextGridSquareRow());
                    }
                } else {
                    if (northing < 90000) {
                        northing = ((int) Math.ceil(northing / 10000.0)) * 10000;
                    }
                }
                mgrsHelper.setNorthing(northing);
                mgrsStart = mgrsHelper.toString();
                mgrsHelper.setMgrs(mgrsEnd);
                northing = mgrsHelper.getNorthing();
                if (northing % 10000 == 0) {
                    if (northing < 90000) {
                        northing += 10000;
                    } else {
                        northing = 0;
                        mgrsHelper.setGridSquareRow(mgrsHelper.getNextGridSquareRow());
                    }
                } else {
                    if (northing < 90000) {
                        northing = ((int) Math.ceil(northing / 10000.0)) * 10000;
                    }
                }
                mgrsHelper.setNorthing(northing);
                mgrsEnd = mgrsHelper.toString();
                if (!mgrsHelper.isValid(mgrsStart) || !mgrsHelper.isValid(mgrsEnd)) {
                    break;
                }
                mgrsHelper.setMgrs(mgrsStart);
                double lat = mgrsHelper.mgrsToGeo(mgrsHelper.toString()).getLatitude();
                String easting = mgrsHelper.geoToMgrs(lat, wLng, MgrsHelper.MAX_PRECISION).substring(5, 10);
                if (gridRow == 'J' && Integer.parseInt(easting) > 90000 && f) {
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getPreviousGridSquareColumn());
                }
                mgrsHelper.setEasting(Integer.parseInt(easting));
                mgrsStart = mgrsHelper.toString();
                mgrsHelper.setMgrs(mgrsEnd);
                lat = mgrsHelper.mgrsToGeo(mgrsHelper.toString()).getLatitude();
                easting = mgrsHelper.geoToMgrs(lat, eLng, MgrsHelper.MAX_PRECISION).substring(5, 10);
                if (gridRow == 'J' && Integer.parseInt(easting) > 90000 && f) {
                    mgrsHelper.setGridSquareColumn(mgrsHelper.getPreviousGridSquareColumn());
                }
                mgrsHelper.setEasting(Integer.parseInt(easting));
                mgrsEnd = mgrsHelper.toString();
                Line line = new Line(mgrsStart, mgrsEnd);
                latLines.add(line);
            }
            mgrsStart = mgrsSW;
            mgrsEnd = mgrsNW;
            boolean flag = true;
            while (true) {
                boolean flag2 = true;
                mgrsHelper.setMgrs(mgrsStart);
                int easting = mgrsHelper.getEasting();
                if (easting % 10000 == 0) {
                    if (easting < 90000) {
                        easting += 10000;
                    } else {
                        easting = 0;
                        mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                    }
                } else {
                    if (easting < 90000) {
                        easting = ((int) Math.ceil(easting / 10000.0)) * 10000;
                    } else {
                        easting = 0;
                        mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                    }
                }
                mgrsHelper.setEasting(easting);
                mgrsStart = mgrsHelper.toString();
                if (gridRow <= 'M' && gridRow != 'C') {
                    if (gridRow == 'J') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 1));
                    }
                    mgrsStart = mgrsHelper.toString();
                }
                if (!mgrsHelper.isValid(mgrsStart)) {
                    break;
                }
                mgrsHelper.setMgrs(mgrsEnd);
                easting = mgrsHelper.getEasting();
                if (easting % 10000 == 0) {
                    if (easting < 90000) {
                        easting += 10000;
                    } else {
                        easting = 0;
                        mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                    }
                } else {
                    if (easting < 90000) {
                        easting = ((int) Math.ceil(easting / 10000.0)) * 10000;
                    } else {
                        easting = 0;
                        mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
                    }
                }
                mgrsHelper.setEasting(easting);
                mgrsEnd = mgrsHelper.toString();
                if (gridRow <= 'M') {
                    if (mgrsEnd.charAt(2) == 'J') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() - 1));
                    }
                    mgrsEnd = mgrsHelper.toString();
                }
                if (!mgrsHelper.isValid(mgrsEnd)) {
                    break;
                }
                Line line = new Line(mgrsStart, mgrsEnd, null, null);
                lngLines.add(line);
                if (gridRow <= 'M') {
                    if (flag2) {
                        mgrsHelper.setMgrs(mgrsStart);
                        if (mgrsStart.charAt(2) == 'H') {
                            mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 2));
                        } else {
                            mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 1));
                        }
                        mgrsStart = mgrsHelper.toString();
                    }
                    mgrsHelper.setMgrs(mgrsEnd);
                    if (mgrsEnd.charAt(2) == 'H') {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 2));
                    } else {
                        mgrsHelper.setGridRow((char) (mgrsHelper.getGridRow() + 1));
                    }
                    mgrsEnd = mgrsHelper.toString();
                }
            }
        }
        return new Grid(latLines, lngLines);
    }

    private Line constructMergeLineN(String mgrsStart, char gridRow, double wLng, double eLng, boolean left) {
        double lat;
        switch(gridRow) {
            case 'X':
                lat = 72.6;
                break;
            case 'U':
                lat = 53.25;
                break;
            case 'R':
                lat = 26.2;
                break;
            default:
                lat = 0;
        }
        Geodetic geo = mgrsHelper.mgrsToGeo(mgrsStart);
        if (geo == null) {
            return new Line();
        }
        Point start = new Point(geo.getLatitude(), geo.getLongitude());
        double lng = (left) ? wLng : eLng;
        Point end = new Point(lat, lng);
        return new Line(start, end);
    }

    private Line constructMergeLineSLeft(String mgrsStart, char gridRow, double wLng) {
        double lat;
        switch(gridRow) {
            case 'J':
                lat = -26.2;
                break;
            case 'F':
                lat = -53.25;
                break;
            case 'C':
                lat = -72.6;
                break;
            default:
                lat = 0;
        }
        Geodetic geo = mgrsHelper.mgrsToGeo(mgrsStart);
        if (geo == null) {
            return new Line();
        }
        Point start = new Point(geo.getLatitude(), geo.getLongitude());
        Point end = new Point(lat, wLng);
        return new Line(start, end);
    }

    private Line constructMergeLineSRight(String mgrsStart, char gridRow, double eLng) {
        mgrsHelper.setMgrs(mgrsStart);
        mgrsHelper.setGridRow(mgrsHelper.getPreviousGridRow());
        mgrsHelper.setGridSquareColumn(mgrsHelper.getNextGridSquareColumn());
        mgrsStart = mgrsHelper.toString();
        double lat;
        switch(gridRow) {
            case 'J':
                lat = -26.2;
                break;
            case 'F':
                lat = -53.25;
                break;
            case 'C':
                lat = -72.6;
                break;
            default:
                lat = 0;
        }
        Geodetic geo = mgrsHelper.mgrsToGeo(mgrsStart);
        if (geo == null) {
            return new Line();
        }
        Point start = new Point(geo.getLatitude(), geo.getLongitude());
        Point end = new Point(lat, eLng);
        return new Line(start, end);
    }

    private String getGridLabelFromPoint(Point center) {
        String result = mgrsHelper.geoToMgrs(center.getLat(), center.getLng(), MgrsHelper.MAX_PRECISION);
        return result;
    }

    private ArrayList<Line> handleSpecialCases(ArrayList<Double> latcoords, ArrayList<Double> lngcoords) {
        ArrayList<Point> points1 = new ArrayList<Point>();
        ArrayList<Point> points2 = new ArrayList<Point>();
        ArrayList<Point> points3 = new ArrayList<Point>();
        ArrayList<Point> points4 = new ArrayList<Point>();
        ArrayList<Point> points5 = new ArrayList<Point>();
        ArrayList<Point> points6 = new ArrayList<Point>();
        ArrayList<Point> points7 = new ArrayList<Point>();
        for (int i = 1; i < lngcoords.size(); i++) {
            if (lngcoords.get(i) == 6) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 56) {
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i)));
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                    } else if (latcoords.get(j) < 56 || latcoords.get(j) > 64 && latcoords.get(j) < 72) {
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) > 56 && latcoords.get(j) < 64) {
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                    } else if (latcoords.get(j) == 64) {
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                        points1.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) == 72) {
                        points2.add(new Point(latcoords.get(j), lngcoords.get(i)));
                        points2.add(new Point(latcoords.get(j), lngcoords.get(i) + 3));
                    } else if (latcoords.get(j) < 72) {
                        points2.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) > 72) {
                        points2.add(new Point(latcoords.get(j), lngcoords.get(i) + 3));
                    } else {
                        points2.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                    }
                }
            } else if (lngcoords.get(i) == 12) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 72) {
                        points3.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) < 72) {
                        points3.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    }
                }
            } else if (lngcoords.get(i) == 18) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 72) {
                        points4.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) < 72) {
                        points4.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    }
                }
            } else if (lngcoords.get(i) == 24) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 72) {
                        points5.add(new Point(latcoords.get(j), lngcoords.get(i)));
                        points5.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                    } else if (latcoords.get(j) < 72) {
                        points5.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) > 72) {
                        points5.add(new Point(latcoords.get(j), lngcoords.get(i) - 3));
                    }
                }
            } else if (lngcoords.get(i) == 30) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 72) {
                        points6.add(new Point(latcoords.get(j), lngcoords.get(i)));
                        points6.add(new Point(latcoords.get(j), lngcoords.get(i) + 3));
                    } else if (latcoords.get(j) < 72) {
                        points6.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) > 72) {
                        points6.add(new Point(latcoords.get(j), lngcoords.get(i) + 3));
                    }
                }
            } else if (lngcoords.get(i) == 36) {
                for (int j = 0; j < latcoords.size(); j++) {
                    if (latcoords.get(j) == 72) {
                        points7.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    } else if (latcoords.get(j) < 72) {
                        points7.add(new Point(latcoords.get(j), lngcoords.get(i)));
                    }
                }
            }
        }
        ArrayList<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < points1.size() - 1; i++) {
            Line line = new Line(points1.get(i), points1.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points2.size() - 1; i++) {
            Line line = new Line(points2.get(i), points2.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points3.size() - 1; i++) {
            Line line = new Line(points3.get(i), points3.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points4.size() - 1; i++) {
            Line line = new Line(points4.get(i), points4.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points5.size() - 1; i++) {
            Line line = new Line(points5.get(i), points5.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points6.size() - 1; i++) {
            Line line = new Line(points6.get(i), points6.get(i + 1));
            lines.add(line);
        }
        for (int i = 0; i < points7.size() - 1; i++) {
            Line line = new Line(points7.get(i), points7.get(i + 1));
            lines.add(line);
        }
        return lines;
    }

    public boolean skipNorwayLines(Line lngLine) {
        double startLat = lngLine.getStart().getLat();
        double startLng = lngLine.getStart().getLng();
        double endLat = lngLine.getEnd().getLat();
        double endLng = lngLine.getEnd().getLng();
        boolean skipNorway = ((startLat == 56 && startLng == 6) && (endLat == 64 && endLng == 6) || (startLat == 64 && startLng == 6) && (endLat == 56 && endLng == 6) || (startLat > 56 && startLat < 64) && startLng == 6 || (endLat > 56 && endLat < 64) && endLng == 6);
        boolean skipSvalbard = ((startLat == 72 && startLng == 6) && (endLat == 84 && endLng == 6) || (startLat == 84 && startLng == 6) && (endLat == 72 && endLng == 6) || (startLat > 72 && startLat < 84) && startLng == 6 || (endLat > 72 && endLat < 84) && endLng == 6);
        boolean skipSvalbard2 = ((startLat == 72 && startLng == 12) && (endLat == 84 && endLng == 12) || (startLat == 84 && startLng == 12) && (endLat == 72 && endLng == 12) || (startLat > 72 && startLat < 84) && startLng == 12 || (endLat > 72 && endLat < 84) && endLng == 12);
        boolean skipSvalbard3 = ((startLat == 72 && startLng == 18) && (endLat == 84 && endLng == 18) || (startLat == 84 && startLng == 18) && (endLat == 72 && endLng == 18) || (startLat > 72 && startLat < 84) && startLng == 18 || (endLat > 72 && endLat < 84) && endLng == 18);
        boolean skipSvalbard4 = ((startLat == 72 && startLng == 24) && (endLat == 84 && endLng == 24) || (startLat == 84 && startLng == 24) && (endLat == 72 && endLng == 24) || (startLat > 72 && startLat < 84) && startLng == 24 || (endLat > 72 && endLat < 84) && endLng == 24);
        boolean skipSvalbard5 = ((startLat == 72 && startLng == 30) && (endLat == 84 && endLng == 30) || (startLat == 84 && startLng == 30) && (endLat == 72 && endLng == 30) || (startLat > 72 && startLat < 84) && startLng == 30 || (endLat > 72 && endLat < 84) && endLng == 30);
        boolean skipSvalbard6 = ((startLat == 72 && startLng == 36) && (endLat == 84 && endLng == 36) || (startLat == 84 && startLng == 36) && (endLat == 72 && endLng == 36) || (startLat > 72 && startLat < 84) && startLng == 36 || (endLat > 72 && endLat < 84) && endLng == 36);
        return skipNorway || skipSvalbard || skipSvalbard2 || skipSvalbard3 || skipSvalbard4 || skipSvalbard5 || skipSvalbard6;
    }

    private void maybeWriteToDisk(Document doc) {
        if (UPDATE_XML_ON_DISK) {
            try {
                LOGGER.log(Level.INFO, "Writing grid xml to disk");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Transformer tf = TransformerFactory.newInstance().newTransformer();
                tf.setOutputProperty(OutputKeys.INDENT, "yes");
                tf.transform(new DOMSource(doc), new StreamResult(bos));
                byte[] gridxmlbytes = bos.toByteArray();
                updateXmlOnDisk(gridxmlbytes, "grid.xml");
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "failed", ex);
            } catch (TransformerException ex) {
                LOGGER.log(Level.SEVERE, "failed", ex);
            }
        }
    }

    private void updateXmlOnDisk(byte[] xmlbytes, String filename) throws IOException {
        FileOutputStream fos = null;
        try {
            File dir = MGRS_XML_PATH_PROVIDER.get();
            dir.mkdirs();
            File file = new File(dir, filename);
            fos = new FileOutputStream(file);
            fos.write(xmlbytes);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to update xml on disk", ex);
        } finally {
            fos.close();
        }
    }

    public void setBoundingBox(BoundingBox bbox) {
        MgrsXmlResource.m_bbox = bbox;
    }

    public BoundingBox calculateBoundingBox(MultivaluedMap<String, String> map) {
        String bboxString = stripBrackets(map.get(BBOX_QUERY_PARAMETER_NAME).toString());
        try {
            MgrsXmlResource.m_bbox = BoundingBox.valueOf(bboxString);
            return MgrsXmlResource.m_bbox;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to get value of bounding box", ex);
        }
        return new BoundingBox();
    }

    private DocumentBuilder makeDocBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder();
    }

    private String stripBrackets(String str) {
        return str.replaceAll("\\[|\\]", "");
    }
}
