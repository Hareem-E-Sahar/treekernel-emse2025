package com.iver.cit.gvsig.drivers.dwg;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.table.DefaultTableModel;
import com.hardcode.gdbms.driver.exceptions.CloseDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.data.driver.ObjectDriver;
import com.hardcode.gdbms.engine.data.edition.DataWare;
import com.hardcode.gdbms.engine.values.IntValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleMarkerSymbol;
import com.iver.cit.gvsig.fmap.drivers.AbstractCadMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.drivers.WithDefaultLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.VectorialUniqueValueLegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.AttrInTableLabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.jdwglib.dwg.DwgFile;
import com.iver.cit.jdwglib.dwg.DwgObject;
import com.iver.cit.jdwglib.dwg.DwgVersionNotSupportedException;
import com.iver.cit.jdwglib.dwg.IDwg2FMap;
import com.iver.cit.jdwglib.dwg.IDwg3DTestable;
import com.iver.cit.jdwglib.dwg.objects.DwgMText;
import com.iver.cit.jdwglib.dwg.objects.DwgText;
import com.iver.cit.jdwglib.util.AcadColor;

/**
 * Driver that allows gvSIG to read files in DWG format Using this driver, a
 * gvSIG user can manipulate part of the information contained in a DWG file
 * This driver load the Dwg file in memory This driver uses jdwglib
 *
 * @author jmorell
 */
public class DwgMemoryDriver extends AbstractCadMemoryDriver implements VectorialFileDriver, WithDefaultLegend {

    VectorialUniqueValueLegend defaultLegend;

    private File m_Fich;

    private String fileVersion;

    private DriverAttributes attr = new DriverAttributes();

    private boolean isInitialized = false;

    float heightText = 10;

    private boolean debug = false;

    private ILabelingStrategy labeler;

    /**
	 * entities of the dwg file (once applied many transformation,
	 * including block insertion) that are IDwgToFMap.
	 * */
    ArrayList entities = new ArrayList();

    /**
	 * Saves an original DWG entity associated to a FMap entity
	 * by their index.
	 * Only available in debug mode (if debug is false, doesnt save anything)
	 * */
    private void saveEntity(DwgObject entity) {
        if (debug) entities.add(entity);
    }

    public DwgObject getEntity(int i) {
        if (debug) return (DwgObject) entities.get(i); else return null;
    }

    public void open(File f) throws com.hardcode.gdbms.driver.exceptions.OpenDriverException {
        m_Fich = f;
    }

    /**
	 * Allows recovering of the DWG Drawing entity
	 * associated to the FMap feature whose position
	 * is index
	 * @param index position of the fmap feature whose
	 * dwg entity is required
	 *
	 * */
    public Object getCadSource(int index) {
        if (debug) return getEntity(index); else return null;
    }

    /**
	 * This method load the DWG file in memory. First, it will be necessary to
	 * create a DwgFile object with the DWG file path as the argument. Second,
	 * the method read of DwgFile allows to read the objects inside the DWG
	 * file. Third, it will be necessary to process some DWG objects like Layers
	 * or Polylines Fourth, applyExtrusions() can change the location of the DWG
	 * objects through the extrusion parameters. Fifth, the testDwg3D method
	 * test if this DWG has elevation informacion. Sixth, we can extract the
	 * objects contained inside the blocks through the blockManagement method.
	 * And finally, we can read the objects Vector, and convert this objects to
	 * the FMap object model.
	 */
    public void initialize() throws ReadDriverException {
        if (isInitialized) return; else isInitialized = true;
        attr.setLoadedInMemory(true);
        DwgFile dwg = new DwgFile(m_Fich.getAbsolutePath());
        try {
            dwg.read();
        } catch (DwgVersionNotSupportedException e1) {
            String autodeskUrl = "<a href=\"http://usa.autodesk.com/adsk/servlet/item?siteID=123112&id=7024151\">" + "http://usa.autodesk.com/adsk/servlet/item?siteID=123112&id=7024151</a>";
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileVersion = dwg.getDwgVersion();
        dwg.calculateGisModelDwgPolylines();
        dwg.blockManagement2();
        List dwgObjects = dwg.getDwgObjects();
        double[] extMin = (double[]) dwg.getHeader("MSPACE_EXTMIN");
        double[] extMax = (double[]) dwg.getHeader("MSPACE_EXTMAX");
        if (extMin != null && extMax != null) {
            if (extMin.length >= 2 && extMax.length >= 2) {
                double xmin = extMin[0];
                double ymin = extMin[1];
                double xmax = extMax[0];
                double ymax = extMax[1];
                Rectangle2D roi = new Rectangle2D.Double(xmin - 100, ymin - 100, (xmax - xmin) + 100, (ymax - ymin) + 100);
                addRegionOfInterest(roi);
            }
        }
        Value[] auxRow = new Value[10];
        ArrayList arrayFields = new ArrayList();
        arrayFields.add("ID");
        arrayFields.add("FShape");
        arrayFields.add("Entity");
        arrayFields.add("Layer");
        arrayFields.add("Color");
        arrayFields.add("Elevation");
        arrayFields.add("Thickness");
        arrayFields.add("Text");
        arrayFields.add("HeightText");
        arrayFields.add("RotationText");
        getTableModel().setColumnIdentifiers(arrayFields.toArray());
        boolean is3dFile = dwg.isDwg3DFile();
        for (int i = 0; i < dwgObjects.size(); i++) {
            try {
                DwgObject entity = (DwgObject) dwgObjects.get(i);
                if (entity instanceof IDwg2FMap) {
                    IDwg2FMap dwgEnt = (IDwg2FMap) entity;
                    IGeometry geometry = dwgEnt.toFMapGeometry(is3dFile);
                    if (geometry == null) continue;
                    if (!checkRois(geometry)) continue;
                    String fmapStr = dwgEnt.toFMapString(is3dFile);
                    auxRow[ID_FIELD_FSHAPE] = ValueFactory.createValue(fmapStr);
                    auxRow[ID_FIELD_ID] = ValueFactory.createValue(i);
                    auxRow[ID_FIELD_ENTITY] = ValueFactory.createValue(dwgEnt.toString());
                    String layerName = dwg.getLayerName(entity);
                    auxRow[ID_FIELD_LAYER] = ValueFactory.createValue(layerName);
                    int colorByLayer = dwg.getColorByLayer(entity);
                    int color = entity.getColor();
                    if (color < 0) color = Math.abs(color);
                    if (color > 255) color = colorByLayer;
                    auxRow[ID_FIELD_COLOR] = ValueFactory.createValue(color);
                    auxRow[ID_FIELD_HEIGHTTEXT] = ValueFactory.createValue(0.0);
                    auxRow[ID_FIELD_ROTATIONTEXT] = ValueFactory.createValue(0.0);
                    auxRow[ID_FIELD_TEXT] = ValueFactory.createNullValue();
                    auxRow[ID_FIELD_THICKNESS] = ValueFactory.createValue(0.0);
                    if (entity instanceof IDwg3DTestable) {
                        auxRow[ID_FIELD_ELEVATION] = ValueFactory.createValue(((IDwg3DTestable) entity).getZ());
                    }
                    if (entity instanceof DwgMText) {
                        DwgMText mtext = (DwgMText) entity;
                        String text = mtext.getText();
                        text = text.replace("\0", "");
                        String text2;
                        text2 = getTextFromMtext(fixUnicode(text));
                        auxRow[ID_FIELD_TEXT] = ValueFactory.createValue(text2);
                        auxRow[ID_FIELD_HEIGHTTEXT] = ValueFactory.createValue(mtext.getHeight());
                    } else if (entity instanceof DwgText) {
                        DwgText text = (DwgText) entity;
                        auxRow[ID_FIELD_TEXT] = ValueFactory.createValue(text.getText());
                        auxRow[ID_FIELD_THICKNESS] = ValueFactory.createValue(text.getThickness());
                        auxRow[ID_FIELD_HEIGHTTEXT] = ValueFactory.createValue(text.getHeight());
                        auxRow[ID_FIELD_ROTATIONTEXT] = ValueFactory.createValue(text.getRotationAngle());
                    }
                    addGeometry(geometry, auxRow);
                    if (debug) saveEntity(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        try {
            setSymbols();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Sets a symbol for each dwg entity's derived feature based in
	 * the DWG symbology info.
	 * @throws IOException
	 * @throws DriverException
	 * */
    private void setSymbols() throws IOException {
        labeler = new AttrInTableLabelingStrategy();
        ((AttrInTableLabelingStrategy) labeler).setTextFieldId(getTableModel().findColumn("Text"));
        ((AttrInTableLabelingStrategy) labeler).setHeightFieldId(getTableModel().findColumn("HeightText"));
        ((AttrInTableLabelingStrategy) labeler).setRotationFieldId(getTableModel().findColumn("RotationText"));
        ((AttrInTableLabelingStrategy) labeler).setUnit(1);
        defaultLegend = LegendFactory.createVectorialUniqueValueLegend(getShapeType());
        String[] names = { "Color" };
        int[] types = { Types.VARCHAR };
        defaultLegend.setClassifyingFieldNames(names);
        defaultLegend.setClassifyingFieldTypes(types);
        ObjectDriver rs = this;
        IntValue clave;
        MultiShapeSymbol theSymbol = null;
        try {
            for (long j = 0; j < rs.getRowCount(); j++) {
                clave = (IntValue) rs.getFieldValue(j, ID_FIELD_COLOR);
                if (defaultLegend.getSymbolByValue(clave) == null) {
                    theSymbol = (MultiShapeSymbol) SymbologyFactory.createDefaultSymbolByShapeType(FShape.MULTI);
                    theSymbol.setDescription(clave.toString());
                    ILineSymbol lineSymbol = new SimpleLineSymbol();
                    lineSymbol.setLineColor(AcadColor.getColor(clave.getValue()));
                    lineSymbol.setLineWidth(1);
                    lineSymbol.setUnit(-1);
                    IFillSymbol fillSymbol = new SimpleFillSymbol();
                    fillSymbol.setFillColor(AcadColor.getColor(clave.getValue()));
                    fillSymbol.setOutline(lineSymbol);
                    fillSymbol.setUnit(-1);
                    IMarkerSymbol markerSymbol = new SimpleMarkerSymbol();
                    markerSymbol.setColor(AcadColor.getColor(clave.getValue()));
                    markerSymbol.setSize(1);
                    markerSymbol.setUnit(-1);
                    theSymbol.setLineSymbol(lineSymbol);
                    theSymbol.setFillSymbol(fillSymbol);
                    theSymbol.setMarkerSymbol(markerSymbol);
                    defaultLegend.addSymbol(clave, theSymbol);
                }
            }
        } catch (ReadDriverException e) {
            throw new IOException("Error durante la asignacion de simbolos a los features dwg");
        }
    }

    /**
	 * checks if the given geometry intersects or its
	 * contained in one of the driver regions of interest.
	 *
	 * @param geometry feature geometry we want to check
	 * against the drivers rois.
	 *
	 *  @return true if feature is contained in any ROI.
	 *  false if not.
	 * */
    private boolean checkRois(IGeometry geometry) {
        Rectangle2D rect = geometry.getBounds2D();
        int numRois = this.getNumOfRois();
        for (int i = 0; i < numRois; i++) {
            Rectangle2D roi = getRegionOfInterest(i);
            if (checkIntersect(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY(), roi.getMinX(), roi.getMaxX(), roi.getMinY(), roi.getMaxY()) || checkContains(rect.getMinX(), rect.getMaxX(), rect.getMinY(), rect.getMaxY(), roi.getMinX(), roi.getMaxX(), roi.getMinY(), roi.getMaxY())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkContains(double x1, double x2, double y1, double y2, double ax1, double ax2, double ay1, double ay2) {
        boolean solution = (x1 >= ax1 && x2 <= ax2 && y1 >= ay1 && y2 <= ay2);
        return solution;
    }

    private boolean checkIntersect(double x1, double x2, double y1, double y2, double ax1, double ax2, double ay1, double ay2) {
        return !(x1 > ax2 || x2 < ax1 || y1 > ay2 || y2 < ay1);
    }

    public String getFileVersion() {
        return fileVersion;
    }

    private String formatString(String fmt, String[] params) {
        String ret = fmt;
        for (int i = 0; i < params.length; i++) {
            ret = ret.replaceFirst("%s", params[i]);
        }
        return ret;
    }

    /**
	 * Method that changes a Point2D array to a FPolygon2D. Is useful to convert
	 * a polygon given by it points to a FPolygon2D, a polygon in the FMap model
	 * object
	 *
	 * @param pts
	 *            Array of Point2D that defines the polygon that will be
	 *            converted in a FPolygon2D
	 * @return FPolygon2D This FPolygon2D is build using the array of Point2D
	 *         that is the argument of the method
	 */
    private FPolygon2D points2DToFPolygon2D(Point2D[] pts) {
        GeneralPathX genPathX = new GeneralPathX();
        genPathX.moveTo(pts[0].getX(), pts[0].getY());
        for (int i = 1; i < pts.length; i++) {
            genPathX.lineTo(pts[i].getX(), pts[i].getY());
        }
        genPathX.closePath();
        return new FPolygon2D(genPathX);
    }

    public boolean accept(File f) {
        return f.getName().toUpperCase().endsWith("DWG");
    }

    public int getShapeType() {
        return FShape.MULTI;
    }

    public String getName() {
        return "gvSIG DWG Memory Driver";
    }

    public ILegend getDefaultLegend() {
        return defaultLegend;
    }

    public DriverAttributes getDriverAttributes() {
        return attr;
    }

    public int[] getPrimaryKeys() throws ReadDriverException {
        return null;
    }

    public void write(DataWare arg0) throws ReadDriverException {
    }

    public void setDataSourceFactory(DataSourceFactory arg0) {
    }

    public void close() throws CloseDriverException {
    }

    public File getFile() {
        return m_Fich;
    }

    public boolean isWritable() {
        return m_Fich.canWrite();
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getFieldType(int i) {
        DefaultTableModel dtm = getTableModel();
        String columnName = dtm.getColumnName(i);
        if (columnName.equals("ID")) {
            return Types.INTEGER;
        } else if (columnName.equals("FShape")) {
            return Types.VARCHAR;
        } else if (columnName.equals("Entity")) {
            return Types.VARCHAR;
        } else if (columnName.equals("Layer")) {
            return Types.VARCHAR;
        } else if (columnName.equals("Color")) {
            return Types.INTEGER;
        } else if (columnName.equals("Elevation")) {
            return Types.DOUBLE;
        } else if (columnName.equals("Thickness")) {
            return Types.DOUBLE;
        } else if (columnName.equals("HeightText")) {
            return Types.DOUBLE;
        } else if (columnName.equals("RotationText")) {
            return Types.DOUBLE;
        } else if (columnName.equals("Text")) {
            return Types.VARCHAR;
        } else {
            return Types.VARCHAR;
        }
    }

    public ILabelingStrategy getDefaultLabelingStrategy() {
        ILabelingStrategy auxLabeler = new AttrInTableLabelingStrategy();
        ((AttrInTableLabelingStrategy) auxLabeler).setTextFieldId(((AttrInTableLabelingStrategy) labeler).getTextFieldId());
        ((AttrInTableLabelingStrategy) auxLabeler).setHeightFieldId(((AttrInTableLabelingStrategy) labeler).getHeightFieldId());
        ((AttrInTableLabelingStrategy) auxLabeler).setRotationFieldId(((AttrInTableLabelingStrategy) labeler).getRotationFieldId());
        ((AttrInTableLabelingStrategy) auxLabeler).setUnit(1);
        return auxLabeler;
    }

    /**
	 * Replace the Unicode characters formatted for AutoCAD
	 * by the Unicode character corresponding
	 *
	 * @param s
	 *         Multiline text formatted with Autocad.
	 *
	 * @return Text fixed
	 */
    private String fixUnicode(String s) {
        String s2 = "";
        String patron = "(\\\\[U][+])([0-9A-Fa-f]{4})";
        Pattern compiledPatron = Pattern.compile(patron);
        Matcher matcher = compiledPatron.matcher(s);
        int lastEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String code = matcher.group(2);
            String hexa = "0x" + code;
            int caracter = Integer.decode(hexa).intValue();
            s2 = s2 + s.substring(lastEnd, start) + (char) caracter;
            lastEnd = end;
        }
        s2 = s2 + s.substring(lastEnd);
        return s2;
    }

    /**
	 * Extracts the text of a multiline text formatted with Autocad
	 *
	 * @param mtext
	 *            Multiline text formatted with Autocad.
	 *            ACAD seems to add braces ({ }) and backslash-P's
	 *            to indicate paragraphs, as well as fonts and / or sizes.
	 *
	 * @return Text extracted
	 */
    private String getTextFromMtext(String mtext) {
        String text = "";
        String patron = "([^{]*)([{][^;]*[;])([^}]*)([}])";
        Pattern compiledPatron = Pattern.compile(patron);
        Matcher matcher = compiledPatron.matcher(mtext);
        int lastEnd = 0;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (i == 1) {
                    text = text + matcher.group(i);
                }
                if (i % 4 == 3) {
                    String grupo = matcher.group(i).replace("\\P", " \n");
                    if (grupo.contains(";")) {
                        String[] ss = grupo.split(";");
                        text = text + ss[ss.length - 1];
                    } else {
                        text = text + grupo;
                    }
                }
            }
            lastEnd = matcher.end();
        }
        text = text + mtext.substring(lastEnd);
        return text;
    }
}
