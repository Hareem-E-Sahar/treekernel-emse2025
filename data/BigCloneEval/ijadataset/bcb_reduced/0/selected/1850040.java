package boogiepants.instruments;

import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.media.j3d.Locale;
import javax.media.j3d.BranchGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import boogiepants.display.ControlPanel;
import boogiepants.display.FileShape;
import boogiepants.display.BoogiepantsDisplayWindow;
import boogiepants.display.PelvicCircleEditor;
import boogiepants.display.ScaleEditor;
import boogiepants.display.ShapeEditor;
import boogiepants.display.ToggleEditor;
import boogiepants.model.InstrumentContainer;
import boogiepants.model.Locatable;
import boogiepants.model.PelvicCircleInstrument;
import boogiepants.model.ScaleLineInstrument;
import boogiepants.model.StrikeInstrument;
import boogiepants.model.ToggleInstrument;
import boogiepants.wiiInput.WiiInputDevice;

/**
 * @author jstoner
 * 
 *         <P>
 *         This class manages the transient state associated with .pants files. It 
 *         can generate an InstrumentContainer object containing a full
 *         suite of Instrument groups.</P>
 *         
 *         <p>it also manages the cache of imported shapes</P>
 *         <P>It's basically a loader and an editor for .pants
 *         files.</P>
 * 
 *         It can take a .pants file as input. It can also generate a .pants
 *         file, using the testbed method.
 * 
 *         WARNING: the format of this file WILL change. It should not be
 *         considered stable.
 */
public class InstrumentManager {

    private static final String INSTRUMENT_CONTAINER_NAME = "##PANTS##";

    protected static final String FILE_SUFFIX = ".pants";

    private boolean editMode;

    private InstrumentContainer instrumentContainer;

    private ShapeCache shapes;

    private JFileChooser fileChooser;

    private String currentFilename;

    private JFileChooser shapeChooser;

    private static InstrumentManager instance = null;

    /**
     * @return
     */
    public static InstrumentManager getInstance() {
        return instance;
    }

    /**
     * @return
     */
    public InstrumentContainer getInstrumentContainer() {
        return instrumentContainer;
    }

    /**
     * Core control of edit mode.
     */
    public void toggleEditMode() {
        editMode = !editMode;
        instrumentContainer.toggle(instrumentContainer.getLastPushed());
        BoogiepantsDisplayWindow.getInstance().editDisplay(editMode);
        ControlPanel.getInstance().editDisplay(editMode);
    }

    /**
     * tells whether boogiepants is in edit mode or not.
     * 
     * @return
     */
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * creates InstrumentContainer object directly from a .pants file
     * 
     * <P>
     * can be easily modified to call testbed method to create objects directly
     * </P>
     * 
     * @return InstrumentManager
     */
    public InstrumentManager() {
        shapes = new ShapeCache();
        instance = this;
        this.instrumentContainer = new InstrumentContainer();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(FILE_SUFFIX);
            }

            public String getDescription() {
                return FILE_SUFFIX + " files";
            }
        });
        shapeChooser = new JFileChooser();
        shapeChooser.setDialogTitle("import shape for instruments");
        shapeChooser.setFileFilter(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".obj");
            }

            public String getDescription() {
                return ".obj files";
            }
        });
    }

    /**
     * writes .pants file--basically the imported shape files, followed by a
     * serialized and compressed InstrumentContainer
     * 
     * @param instrumentContainer
     * @param filename
     */
    public void writeInstruments(String filename) {
        ObjectOutputStream out = null;
        ZipOutputStream zipout = null;
        try {
            zipout = new ZipOutputStream(new FileOutputStream(filename));
            zipout.setLevel(0);
            for (String i : shapes.keySet()) {
                FileShape shape = shapes.importShape(i);
                zipout.putNextEntry(new ZipEntry(shape.getFilename()));
                shape.saveShapeToOutputStream(zipout);
                zipout.closeEntry();
            }
            zipout.putNextEntry(new ZipEntry(INSTRUMENT_CONTAINER_NAME));
            out = new ObjectOutputStream(zipout);
            out.writeObject(instrumentContainer);
            out.flush();
            zipout.closeEntry();
            zipout.finish();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zipout != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * reads .pants file--basically the compressed shape files, followed by a
     * serialized and compressed InstrumentContainer.
     * 
     * @param filename
     * @return
     */
    private InstrumentContainer readInstruments(String filename) {
        ObjectInputStream in = null;
        ZipInputStream zipin = null;
        InstrumentContainer ic = null;
        try {
            zipin = new ZipInputStream(new FileInputStream(filename));
            ZipEntry ze = zipin.getNextEntry();
            while (!ze.getName().equals(INSTRUMENT_CONTAINER_NAME)) {
                shapes.importShape(zipin, ze.getName());
                zipin.closeEntry();
                ze = zipin.getNextEntry();
            }
            in = new ObjectInputStream(zipin);
            ic = (InstrumentContainer) in.readObject();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ic;
    }

    /**
     * creates InstrumentContainer object tree directly. Useful for development
     * purposes
     * 
     * @param locale
     * @return
     */
    public InstrumentContainer testbed() {
        String home = System.getProperty("user.home") + "/boogiepants";
        instrumentContainer = new InstrumentContainer();
        StrikeInstrument instrument = new StrikeInstrument("/1/right_hip", home + "/resources/geometry/icosahedron.obj", new Point3d(-.5, -.5, 0), null, null, new Color3f(.2f, 1f, .3f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_ONE, instrument);
        instrument = new StrikeInstrument("/1/left_hip", home + "/resources/geometry/dodecahedron.obj", new Point3d(.5, -.5, 0), new Vector3d(.3, .3, .7), null, new Color3f(.2f, .3f, 1f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_ONE, instrument);
        instrument = new StrikeInstrument("/1/front", home + "/resources/geometry/cube.obj", new Point3d(0, -.75, .45), new Vector3d(.3, .3, .3), null, new Color3f(0f, .6f, 1f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_ONE, instrument);
        instrument = new StrikeInstrument("/1/back", home + "/resources/geometry/cube.obj", new Point3d(0, -.75, -.45), new Vector3d(.3, .3, .3), null, new Color3f(1f, 0f, .6f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_ONE, instrument);
        instrument = new StrikeInstrument("/2/h1", home + "/resources/geometry/icosahedron.obj", new Point3d(-.65, -.65, 0), null, null, new Color3f(1f, .2f, .2f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h2", home + "/resources/geometry/icosahedron.obj", new Point3d(-.45, -.50, .3), null, null, new Color3f(.8f, .4f, .2f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h4", home + "/resources/geometry/icosahedron.obj", new Point3d(0, -.40, .4), null, null, new Color3f(.6f, .6f, .2f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h6", home + "/resources/geometry/icosahedron.obj", new Point3d(.45, -.50, .3), null, null, new Color3f(.4f, .8f, .2f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h3", home + "/resources/geometry/icosahedron.obj", new Point3d(.35, -.50, -.35), null, null, new Color3f(.2f, 1f, .4f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h5", home + "/resources/geometry/icosahedron.obj", new Point3d(-.35, -.50, -.35), null, null, new Color3f(.2f, .8f, .4f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        instrument = new StrikeInstrument("/2/h7", home + "/resources/geometry/icosahedron.obj", new Point3d(.65, -.65, 0), null, null, new Color3f(.2f, .8f, .6f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_TWO, instrument);
        Matrix3d m = new Matrix3d();
        m.rotX(-Math.PI / 4.0);
        instrument = new StrikeInstrument("/plus/1", home + "/resources/geometry/cube.obj", new Point3d(0, -.5, .7), new Vector3d(.3, .3, .8), m, new Color3f(1f, 0f, .8f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_PLUS, instrument);
        m = new Matrix3d();
        m.rotZ(Math.PI / 4.0);
        instrument = new StrikeInstrument("/plus/2", home + "/resources/geometry/cube.obj", new Point3d(-.7, -.5, 0), new Vector3d(.3, .8, .3), m, new Color3f(1f, 0f, .8f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_PLUS, instrument);
        m = new Matrix3d();
        m.rotX(Math.PI / 4.0);
        instrument = new StrikeInstrument("/plus/3", home + "/resources/geometry/cube.obj", new Point3d(0, -.5, -.7), new Vector3d(.3, .3, .8), m, new Color3f(1f, 0f, 0.8f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_PLUS, instrument);
        m = new Matrix3d();
        m.rotZ(-Math.PI / 4.0);
        instrument = new StrikeInstrument("/plus/4", home + "/resources/geometry/cube.obj", new Point3d(.7, -.5, 0), new Vector3d(.3, .8, .3), m, new Color3f(1f, 0f, .8f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_PLUS, instrument);
        ScaleLineInstrument scratcher = new ScaleLineInstrument("/minus", new Point3d(0, -.6, .4), new Point3d(0, -.6, -.4));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_MINUS, scratcher);
        PelvicCircleInstrument circle = new PelvicCircleInstrument("/a", new Color3f(1f, .5f, 0f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_A, circle);
        ToggleInstrument toggle = new ToggleInstrument("/track/playpause", new Color3f(.5f, .5f, 0f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_DOWN, toggle);
        PelvicCircleInstrument scratch = new PelvicCircleInstrument("/track/scratch", new Color3f(.5f, .5f, 0f));
        instrumentContainer.add(WiiInputDevice.BUTTON_INDEX_LEFT, scratch);
        return instrumentContainer;
    }

    /**
     * empties out the cache of imported shapes--called on 'new'
     */
    public void resetImportedShapes() {
        this.shapes.clear();
    }

    /**
     * creates a new blank .pants file
     */
    public void newPantsFile() {
        resetImportedShapes();
        instrumentContainer = new InstrumentContainer();
        BoogiepantsDisplayWindow bpw = BoogiepantsDisplayWindow.getInstance();
        bpw.resetInstrumentView();
    }

    /**
     * opens previously created .pants file
     */
    public void openPantsFile() {
        int retValue = fileChooser.showOpenDialog(null);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            currentFilename = fileChooser.getSelectedFile().getAbsolutePath();
            resetImportedShapes();
            instrumentContainer = this.readInstruments(currentFilename);
            System.out.println("after read " + instrumentContainer);
            BoogiepantsDisplayWindow bpw = BoogiepantsDisplayWindow.getInstance();
            bpw.resetInstrumentView();
        }
    }

    /**
     * saves edited .pants file
     */
    public void savePantsFile() {
        if (currentFilename == null) {
            saveAsPantsFile();
        } else {
            this.writeInstruments(currentFilename);
        }
    }

    /**
     * saves current .pants file, with new name
     */
    public void saveAsPantsFile() {
        int retValue = fileChooser.showSaveDialog(null);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            currentFilename = fileChooser.getSelectedFile().getAbsolutePath();
            if (!currentFilename.endsWith(FILE_SUFFIX)) {
                currentFilename += FILE_SUFFIX;
            }
            this.writeInstruments(currentFilename);
        }
    }

    /**
     * displays the file chooser to import a shape
     * @return
     */
    public int importShapeDialog() {
        int retValue = shapeChooser.showOpenDialog(null);
        if (retValue == JFileChooser.APPROVE_OPTION) {
            String filename = shapeChooser.getSelectedFile().getAbsolutePath();
            shapes.importShape(filename);
        }
        return retValue;
    }

    public Object[] getImportedShapeNames() {
        return shapes.keySet().toArray();
    }

    /**
     * adds a new Strike Instrument to the current instrument group
     */
    public void addStrikeInstrument() {
        int retValue = JFileChooser.APPROVE_OPTION;
        String key = null;
        StrikeInstrument instrument = new StrikeInstrument();
        if (shapes.isEmpty()) {
            retValue = importShapeDialog();
            key = (String) shapes.keySet().toArray()[0];
            instrument.setShapeName(key);
        }
        if (retValue == JFileChooser.APPROVE_OPTION) {
            Object[] keys = shapes.keySet().toArray();
            int retcode = ShapeEditor.makeDialog((JFrame) BoogiepantsDisplayWindow.getInstance(), keys, instrument);
            if (retcode == ShapeEditor.SUCCESS) {
                instrumentContainer.add(instrumentContainer.getLastPushed(), instrument);
                instrumentContainer.displayNew();
                instrument.setRelocate();
            }
        }
    }

    /**
     * adds a new Scale Instrument to the current instrument group
     */
    public void addScaleInstrument() {
        ScaleLineInstrument instrument = new ScaleLineInstrument();
        int retcode = ScaleEditor.makeDialog((JFrame) BoogiepantsDisplayWindow.getInstance(), instrument);
        if (retcode == ScaleEditor.SUCCESS) {
            instrumentContainer.add(instrumentContainer.getLastPushed(), instrument);
            instrumentContainer.displayNew();
            instrument.setRelocate();
        }
    }

    /**
     * adds a new Pelvic Circle Instrument to the current instrument group
     */
    public void addPelvicCircleInstrument() {
        PelvicCircleInstrument instrument = new PelvicCircleInstrument();
        int retcode = PelvicCircleEditor.makeDialog((JFrame) BoogiepantsDisplayWindow.getInstance(), instrument);
        if (retcode == PelvicCircleEditor.SUCCESS) {
            instrumentContainer.add(instrumentContainer.getLastPushed(), instrument);
            instrumentContainer.displayNew();
        }
    }

    /**
     * adds a new Toggle Instrument to the current instrument group
     */
    public void addToggleInstrument() {
        ToggleInstrument instrument = new ToggleInstrument();
        int retcode = ToggleEditor.makeDialog((JFrame) BoogiepantsDisplayWindow.getInstance(), instrument);
        if (retcode == ToggleEditor.SUCCESS) {
            instrumentContainer.add(instrumentContainer.getLastPushed(), instrument);
            instrumentContainer.displayNew();
        }
    }

    public ShapeCache getShapes() {
        return shapes;
    }
}
