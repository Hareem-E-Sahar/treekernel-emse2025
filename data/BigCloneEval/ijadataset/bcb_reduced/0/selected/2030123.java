package de.fhg.igd.earth.model.input;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import de.fhg.igd.earth.control.dialog.StatusDialog;
import de.fhg.igd.earth.model.graph.BranchGroup;
import de.fhg.igd.earth.model.graph.DetailGroup;
import de.fhg.igd.earth.model.graph.DistanceDetailGroup;
import de.fhg.igd.earth.model.graph.Group;
import de.fhg.igd.earth.model.graph.ModelGraph;
import de.fhg.igd.earth.model.graph.Text;
import de.fhg.igd.earth.model.graph.TransformGroup;
import de.fhg.igd.earth.model.input.shapefile.DBaseFile;
import de.fhg.igd.earth.model.input.shapefile.ESRIPointRecord;
import de.fhg.igd.earth.model.input.shapefile.ESRIPoly;
import de.fhg.igd.earth.model.input.shapefile.ESRIPolygonRecord;
import de.fhg.igd.earth.model.input.shapefile.ESRIRecord;
import de.fhg.igd.earth.model.input.shapefile.ShapeFile;
import de.fhg.igd.earth.utils.ExampleFileFilter;

/**
 * This "ModelLoader" loads shapefiles into the model.
 * The load method shows a dialog box where you can select the ".shp" file.
 * If a ".dbf" (DBase) file also exists this loader converts its data into the
 * infotable.
 *
 * Title        : Earth
 * Copyright    : Copyright (c) 2001
 * Organisation : IGD FhG
 * @author       : Werner Beutel
 * @version      : 1.0
 */
public class ShapeFileLoader extends ModelLoader {

    /**
     * default shapefile path
     */
    public static final String shapePath_ = "data/shapes/";

    /**
     * model to include into
     */
    private ModelGraph model_;

    /**
     * filename
     */
    private String filename_;

    /**
     * absolute .shp filename
     */
    private String filenameSHP_;

    /**
     * absolute .dbf filename
     */
    private String filenameDBF_;

    /**
     * plain filename wiithout extension
     */
    private String plainName_;

    /**
     * directory
     */
    private String directory_;

    /**
     * parent frame
     */
    private Frame parent_;

    /**
     * record names
     */
    private Vector recordNames_;

    /**
     * selection list
     */
    private Vector selected_;

    /**
     * color
     */
    private Color color_;

    /**
     * style (wire frame, solid, texture)
     */
    private int style_;

    /**
     * transparency (0.0-1.0)
     */
    private float transparency_;

    /**
     * x-offset
     */
    private double offsetX_;

    /**
     * y-offset
     */
    private double offsetY_;

    /**
     * z-offset
     */
    private double offsetZ_;

    /**
     * detail level (0=high detail, 1=low detail)
     */
    private int detailLevel_;

    /**
     * distance value
     */
    private float distance_;

    /**
     * create a LOD Group ?
     */
    private boolean lod_;

    /**
     * type of the shapefile
     */
    private int shapeFileType_;

    /**
     * type name of the shapefile
     */
    private String shapeFileTypeName_;

    /**
     * number of records
     */
    private int recordCounter_;

    /**
     * the please wait dialog
     */
    private StatusDialog pleaseWait_;

    /*************************************************************************
     * Creates an instance of this class.
     ************************************************************************/
    public ShapeFileLoader() {
        reset();
    }

    /*************************************************************************
     * Returns the identName for this class (ShapeFileLoader).
     * @return "ShapeFileLoader"
     ************************************************************************/
    public String getIdentName() {
        return "ShapeFileLoader";
    }

    /*************************************************************************
     * Initializes all variables.
     ************************************************************************/
    private void reset() {
        model_ = null;
        filename_ = "";
        directory_ = "";
        parent_ = null;
        recordNames_ = new Vector();
        selected_ = new Vector();
        shapeFileType_ = 0;
        shapeFileTypeName_ = "";
        recordCounter_ = 0;
    }

    /*************************************************************************
     * Loads a shapefile into the model.
     * @param model Model to load into
     * @param parent Parent frame
     * @return <code>true</code> on success.
     ************************************************************************/
    public boolean load(ModelGraph model, Frame parent) {
        reset();
        if (model == null || parent == null) return false;
        model_ = model;
        parent_ = parent;
        if (chooseFile() == false) return false;
        if (createRecordNames() == false) return false;
        if (showDialog() == false) return false;
        if (loadFile() == false) return false;
        return true;
    }

    /*************************************************************************
     * Load directly without a dialog window (DEBUG).
     * @param model Model to load into
     * @param name Name of the shapefile (plain)
     * @param color Color
     * @param style Style
     * @param transparency Transparency
     * @param offsetX,Y,Z Translation
     * @param detailLevel (0=high detail, 1=low detail)
     * @param distance Distance (threshold between high and low detail)
     * @param lod Create a LODGroup ?
     * @return <code>true</code> on success.
     ************************************************************************/
    public boolean load(ModelGraph model, String name, Color color, int style, float transparency, double offsetX, double offsetY, double offsetZ, int detailLevel, float distance, boolean lod) {
        char c;
        reset();
        if (model == null) return false;
        model_ = model;
        color_ = color;
        style_ = style;
        transparency_ = transparency;
        offsetX_ = offsetX;
        offsetY_ = offsetY;
        offsetZ_ = offsetZ;
        detailLevel_ = detailLevel;
        distance_ = distance;
        lod_ = lod;
        filenameSHP_ = datapath_ + shapePath_ + name + ".shp";
        filenameDBF_ = datapath_ + shapePath_ + name + ".dbf";
        plainName_ = name;
        return loadFile();
    }

    /*************************************************************************
     * Shows a dialog where you can choose a shapefile.
     * @return true on OK, false on CANCEL
     ************************************************************************/
    private boolean chooseFile() {
        File dir;
        JFileChooser chooser;
        ExampleFileFilter filter;
        int returnVal;
        char c;
        dir = new File(datapath_ + "data/shapes");
        chooser = new JFileChooser();
        chooser.setDialogTitle("Import Shapefile");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        filter = new ExampleFileFilter();
        filter.addExtension("shp");
        filter.setDescription("SHP shape");
        chooser.setFileFilter(filter);
        chooser.setCurrentDirectory(dir);
        returnVal = chooser.showOpenDialog(parent_);
        if (returnVal != JFileChooser.APPROVE_OPTION) return false;
        directory_ = chooser.getCurrentDirectory().getAbsolutePath();
        filename_ = chooser.getSelectedFile().getName();
        if (!directory_.endsWith(File.separator)) directory_ += File.separator;
        c = filename_.charAt(filename_.length() - 4);
        if (c != '.') return false;
        filenameSHP_ = directory_ + filename_.substring(0, filename_.length() - 3) + "shp";
        filenameDBF_ = directory_ + filename_.substring(0, filename_.length() - 3) + "dbf";
        plainName_ = filename_.substring(0, filename_.length() - 4);
        return true;
    }

    /*************************************************************************
     * Shows the dialog box to select the records of the shapefile.
     * @return true on OK, false on CANCEL
     ************************************************************************/
    private boolean showDialog() {
        ShapeFileLoaderDialog sd;
        sd = new ShapeFileLoaderDialog(parent_, "InputFilter", true, directory_, filename_, recordNames_, selected_, shapeFileTypeName_, recordCounter_);
        sd.show();
        if (sd.getResult() == false) return false;
        color_ = sd.getColor();
        style_ = sd.getStyle();
        transparency_ = sd.getTransparency();
        offsetX_ = sd.getOffsetX();
        offsetY_ = sd.getOffsetY();
        offsetZ_ = sd.getOffsetZ();
        detailLevel_ = sd.getDetailLevel();
        distance_ = sd.getDistance();
        lod_ = sd.getLOD();
        return true;
    }

    /*************************************************************************
     * Loads a shapefile directly without a dialog box (simple version)
     * @param model Model to load into
     * @param name Name of the shapefile (plain)
     * @return <code>true</code> on success.
     ************************************************************************/
    public boolean load(ModelGraph model, String name) {
        return load(model, name, Color.white, de.fhg.igd.earth.model.graph.Polygon.STYLE_WIRE_FRAME, 0.2f, 0.0, 0.0, 0.0, 0, 0.0f, true);
    }

    /*************************************************************************
     * Creates the record names
     * @return <code>true</code> on success.
     ************************************************************************/
    private boolean createRecordNames() {
        boolean nameSet;
        ShapeFile inshape;
        DBaseFile dbase;
        boolean dbaseExists;
        int i;
        File testFile;
        String fieldName;
        String fieldData;
        String checkName;
        boolean fileSupported;
        showPleaseWait(true);
        testFile = new File(filenameSHP_);
        if (testFile == null || !testFile.exists()) {
            JOptionPane.showMessageDialog(null, filenameSHP_, "shape file not found", JOptionPane.INFORMATION_MESSAGE);
            showPleaseWait(false);
            return false;
        }
        dbaseExists = false;
        testFile = new File(filenameDBF_);
        if (testFile != null) dbaseExists = testFile.exists();
        dbase = null;
        fileSupported = false;
        if (!dbaseExists) {
            JOptionPane.showMessageDialog(null, "no metadata will be added", "dbase file not found", JOptionPane.INFORMATION_MESSAGE);
        }
        try {
            inshape = new ShapeFile(filenameSHP_);
            if (dbaseExists) dbase = new DBaseFile(filenameDBF_);
            recordCounter_ = 0;
            shapeFileTypeName_ = "Unknown";
            shapeFileType_ = inshape.getShapeType();
            switch(shapeFileType_) {
                case ShapeFile.SHAPE_TYPE_MULTIPOINT:
                    shapeFileTypeName_ = "Multipoint";
                    fileSupported = false;
                    break;
                case ShapeFile.SHAPE_TYPE_NULL:
                    shapeFileTypeName_ = "Null";
                    fileSupported = false;
                    break;
                case ShapeFile.SHAPE_TYPE_POINT:
                    shapeFileTypeName_ = "Point";
                    fileSupported = true;
                    break;
                case ShapeFile.SHAPE_TYPE_POLYGON:
                    shapeFileTypeName_ = "Polygon";
                    fileSupported = true;
                    break;
                case ShapeFile.SHAPE_TYPE_POLYLINE:
                    shapeFileTypeName_ = "Polyline";
                    fileSupported = true;
                    break;
            }
            ESRIRecord rec;
            rec = (ESRIRecord) inshape.getNextRecord();
            if (dbaseExists) dbase.getNextRecord();
            while (rec != null && fileSupported) {
                nameSet = false;
                checkName = "";
                fieldData = "";
                if (dbaseExists) {
                    for (i = 0; i < dbase.numRows(); i++) {
                        fieldName = dbase.getFieldName(i);
                        fieldData = dbase.getFieldData(i);
                        if (fieldData.length() > 2 && Character.isLetter(fieldData.charAt(0)) && checkName.indexOf(fieldData) == -1) {
                            checkName += fieldData + "| ";
                            nameSet = true;
                        }
                    }
                }
                if (nameSet == false) checkName = "unknown";
                recordNames_.add(checkName);
                selected_.add(new Boolean(true));
                rec = (ESRIRecord) inshape.getNextRecord();
                if (dbaseExists) dbase.getNextRecord();
                recordCounter_++;
            }
            if (dbaseExists) dbase.close();
            inshape.close();
        } catch (Exception ex) {
            System.out.println(ex);
            showPleaseWait(false);
            return false;
        }
        showPleaseWait(false);
        if (!fileSupported) {
            JOptionPane.showMessageDialog(null, shapeFileTypeName_ + " not supported", "wrong file type", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    /*************************************************************************
     * Opens the shapefile and loads the data into the model.
     ************************************************************************/
    public boolean loadFile() {
        File testFile;
        Hashtable infoTable;
        BranchGroup bg;
        Group dummygroup;
        DetailGroup lod;
        DistanceDetailGroup dlod;
        Group group;
        Group recordGroup;
        String fieldName, fieldData;
        TransformGroup tg;
        TransformGroup tg2;
        Text text;
        ESRIRecord rec;
        ESRIPoly[] polygons;
        int i;
        boolean nameSet;
        float[] distances;
        ShapeFile inshape;
        DBaseFile dbase;
        de.fhg.igd.earth.model.graph.Polygon pol;
        int nPolys;
        int len;
        float[] pts;
        boolean dbaseExists;
        showPleaseWait(true);
        bg = new BranchGroup();
        bg.setName("Shape: " + plainName_);
        group = bg;
        if (lod_ == true) {
            lod = new DetailGroup();
            lod.setName("LoD");
            bg.addChild(lod);
            group = lod;
        }
        if (distance_ > 0) {
            distances = new float[1];
            distances[0] = distance_;
            dlod = new DistanceDetailGroup(distances);
            dlod.setName("DLoD " + String.valueOf(distance_) + "%");
            group.addChild(dlod);
            if (detailLevel_ == 1) {
                dummygroup = new Group();
                dummygroup.setName("dummy");
                dlod.addChild(dummygroup);
            }
            group = dlod;
        }
        tg = new TransformGroup();
        tg.setName("Data");
        group.addChild(tg);
        testFile = new File(filenameSHP_);
        if (testFile == null || !testFile.exists()) {
            JOptionPane.showMessageDialog(null, filenameSHP_, "shape file not found", JOptionPane.INFORMATION_MESSAGE);
            showPleaseWait(false);
            return false;
        }
        dbaseExists = false;
        testFile = new File(filenameDBF_);
        if (testFile != null) dbaseExists = testFile.exists();
        dbase = null;
        try {
            int counter = 0;
            inshape = new ShapeFile(filenameSHP_);
            if (dbaseExists) dbase = new DBaseFile(filenameDBF_);
            shapeFileType_ = inshape.getShapeType();
            rec = (ESRIRecord) inshape.getNextRecord();
            if (dbaseExists) dbase.getNextRecord();
            while (rec != null) {
                if (selected_.size() == 0 || (((Boolean) selected_.elementAt(counter)).booleanValue()) == true) {
                    recordGroup = new Group();
                    recordGroup.setPickable(true);
                    recordGroup.setName("Record");
                    nameSet = false;
                    if (dbaseExists) {
                        infoTable = recordGroup.getInfoTable();
                        for (i = 0; i < dbase.numRows(); i++) {
                            fieldName = dbase.getFieldName(i);
                            fieldData = dbase.getFieldData(i);
                            if (nameSet == false) {
                                String test = fieldName.toUpperCase();
                                int p = test.indexOf("NAME");
                                if (p >= 0) {
                                    recordGroup.setName(fieldData);
                                    nameSet = true;
                                }
                            }
                            infoTable.put(fieldName, fieldData);
                        }
                        recordGroup.setInfoTable(infoTable);
                    }
                    switch(shapeFileType_) {
                        case ShapeFile.SHAPE_TYPE_MULTIPOINT:
                            break;
                        case ShapeFile.SHAPE_TYPE_NULL:
                            break;
                        case ShapeFile.SHAPE_TYPE_POINT:
                            double xm, ym, x0, x1, y0, y1;
                            xm = ((ESRIPointRecord) rec).getBoundingBox().min.x;
                            ym = ((ESRIPointRecord) rec).getBoundingBox().min.y;
                            x0 = xm - 0.05;
                            x1 = xm + 0.05;
                            y0 = ym - 0.05;
                            y1 = ym + 0.05;
                            pol = new de.fhg.igd.earth.model.graph.Polygon();
                            pol.setStyle(style_);
                            pol.setTransparency(transparency_);
                            pol.setColor(color_);
                            pol.addPoint(x0 + offsetX_, y0 + offsetY_, offsetZ_, 0, 0, 1);
                            pol.addPoint(x1 + offsetX_, y0 + offsetY_, offsetZ_, 0, 0, 1);
                            pol.addPoint(x1 + offsetX_, y1 + offsetY_, offsetZ_, 0, 0, 1);
                            pol.addPoint(x0 + offsetX_, y1 + offsetY_, offsetZ_, 0, 0, 1);
                            pol.addPoint(x0 + offsetX_, y0 + offsetY_, offsetZ_, 0, 0, 1);
                            recordGroup.addChild(pol);
                            break;
                        case ShapeFile.SHAPE_TYPE_POLYGON:
                            polygons = ((ESRIPolygonRecord) rec).polygons;
                            nPolys = polygons.length;
                            for (i = 0; i < nPolys; i++) {
                                pts = ((ESRIPoly.ESRIFloatPoly) polygons[i]).getDecimalDegrees();
                                len = pts.length;
                                pol = new de.fhg.igd.earth.model.graph.Polygon();
                                pol.setStyle(style_);
                                pol.setTransparency(transparency_);
                                pol.setColor(color_);
                                for (int j = 0; j < len; j += 2) pol.addPoint(pts[j + 1] + offsetX_, pts[j] + offsetY_, offsetZ_, 0, 0, 1);
                                recordGroup.addChild(pol);
                            }
                            break;
                        case ShapeFile.SHAPE_TYPE_POLYLINE:
                            polygons = ((ESRIPolygonRecord) rec).polygons;
                            nPolys = polygons.length;
                            for (i = 0; i < nPolys; i++) {
                                pts = ((ESRIPoly.ESRIFloatPoly) polygons[i]).getDecimalDegrees();
                                len = pts.length;
                                pol = new de.fhg.igd.earth.model.graph.Polygon();
                                pol.setStyle(de.fhg.igd.earth.model.graph.Polygon.STYLE_WIRE_FRAME);
                                pol.setTransparency(transparency_);
                                pol.setColor(color_);
                                for (int j = 0; j < len; j += 2) pol.addPoint(pts[j + 1] + offsetX_, pts[j] + offsetY_, offsetZ_, 0, 0, 1);
                                recordGroup.addChild(pol);
                            }
                            break;
                    }
                    tg.addChild(recordGroup);
                }
                counter++;
                rec = (ESRIRecord) inshape.getNextRecord();
                if (dbaseExists) dbase.getNextRecord();
            }
            inshape.close();
            if (dbaseExists) dbase.close();
        } catch (Exception ex) {
            System.out.println(ex);
            showPleaseWait(false);
            return false;
        }
        model_.addChild(bg);
        showPleaseWait(false);
        return true;
    }

    /*************************************************************************
     * Shows the "please wait" window.
     * @param on true=Show false=hide
     ************************************************************************/
    private void showPleaseWait(boolean on) {
        if (on) {
            pleaseWait_ = new StatusDialog(this.parent_, "please wait ...", false);
            pleaseWait_.show();
        } else pleaseWait_.hide();
    }
}
