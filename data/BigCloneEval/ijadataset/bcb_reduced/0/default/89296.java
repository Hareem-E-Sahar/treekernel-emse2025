import org.openscience.jmol.applet.*;
import org.openscience.jmol.DisplayControl;
import org.openscience.jmol.script.Eval;
import org.openscience.jmol.ui.JmolPopup;
import java.util.PropertyResourceBundle;
import java.util.MissingResourceException;
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import org.openscience.jmol.io.ReaderProgress;
import org.openscience.jmol.io.ReaderFactory;
import org.openscience.jmol.JmolStatusListener;
import org.openscience.jmol.FortranFormat;
import org.openscience.jmol.io.ChemFileReader;
import org.openscience.jmol.io.CMLReader;

public class JmolApplet extends Applet implements JmolStatusListener {

    AppletCanvas canvas;

    DisplayControl control;

    JmolPopup jmolpopup;

    private String defaultAtomTypesFileName = "Data/AtomTypes.txt";

    public String getAppletInfo() {
        return appletInfo;
    }

    private static String appletInfo = "Jmol Applet.  Part of the OpenScience project. " + "See jmol.sourceforge.net for more information";

    private static String[][] paramInfo = { { "bgcolor", "color", "Background color to HTML color name or #RRGGBB" }, { "style", "SHADED, QUICKDRAW or WIREFRAME", "One of the three possible rendering styles" }, { "label", "NONE, SYMBOL or NUMBER", "Select style for atom labels" }, { "atomTypes", "url", "URL of custom Atomtypes file, " + "or leave blank to use the default atom definitions" }, { "wireframeRotation", "ON or OFF", "Switch to wireframe during rotations for better performance" }, { "load", "url", "URL of the chemical data" }, { "loadInline", "fileformat", "Inline representation of chemical data" }, { "script", "string", "Inline RasMol/Chime script commands " + "separated by newlines or semicolons" } };

    public String[][] getParameterInfo() {
        return paramInfo;
    }

    public void init() {
        loadProperties();
        initWindows();
        initApplication();
    }

    public void initWindows() {
        String strJvmVersion = System.getProperty("java.version");
        canvas = new AppletCanvas();
        control = new DisplayControl(strJvmVersion, canvas);
        canvas.setDisplayControl(control);
        control.setJmolStatusListener(this);
        jmolpopup = new JmolPopup(control, canvas);
        control.setAppletContext(getDocumentBase(), getCodeBase(), getValue("JmolAppletProxy", null));
        setLayout(new java.awt.BorderLayout());
        add(canvas, "Center");
    }

    PropertyResourceBundle appletProperties = null;

    private void loadProperties() {
        URL codeBase = getCodeBase();
        try {
            URL urlProperties = new URL(codeBase, "JmolApplet.properties");
            appletProperties = new PropertyResourceBundle(urlProperties.openStream());
        } catch (Exception ex) {
            System.out.println("JmolApplet.loadProperties():" + "JmolApplet.properties not found/loaded");
        }
    }

    private String getValue(String propertyName, String defaultValue) {
        String stringValue = getParameter(propertyName);
        if (stringValue != null) return stringValue;
        if (appletProperties != null) {
            try {
                stringValue = appletProperties.getString(propertyName);
                return stringValue;
            } catch (MissingResourceException ex) {
            }
        }
        return defaultValue;
    }

    private int getValue(String propertyName, int defaultValue) {
        String stringValue = getValue(propertyName, null);
        if (stringValue != null) try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException ex) {
            System.out.println(propertyName + ":" + stringValue + " is not an integer");
        }
        return defaultValue;
    }

    private double getValue(String propertyName, double defaultValue) {
        String stringValue = getValue(propertyName, null);
        if (stringValue != null) try {
            return (new Double(stringValue)).doubleValue();
        } catch (NumberFormatException ex) {
            System.out.println(propertyName + ":" + stringValue + " is not a double");
        }
        return defaultValue;
    }

    public void initApplication() {
        control.pushHoldRepaint();
        {
            control.setShowBonds(true);
            control.setShowAtoms(true);
            control.setPercentVdwAtom(getValue("vdwPercent", 20));
            control.zoomToPercent(getValue("zoom", 100));
            control.setStyleBond(DisplayControl.SHADING);
            control.setStyleAtom(DisplayControl.SHADING);
            control.setColorBackground(getParameter("bgcolor"));
            setStyle(getValue("style", "shaded"));
            setLabelStyle(getValue("label", "none"));
            String wfr = getValue("wireframeRotation", "false");
            setWireframeRotation(wfr != null && (wfr.equalsIgnoreCase("on") || wfr.equalsIgnoreCase("true")));
            String pd = getValue("perspectiveDepth", "true");
            setPerspectiveDepth(pd == null || pd.equalsIgnoreCase("on") || pd.equalsIgnoreCase("true"));
            load(getValue("load", null));
            loadInline(getValue("loadInline", null));
            script(getValue("script", null));
        }
        control.popHoldRepaint();
    }

    public void notifyFileLoaded(String fullPathName, String fileName, String modelName, Object clientFile) {
        showStatus("File loaded:" + fileName);
    }

    public void notifyFileNotLoaded(String fullPathName, String errorMessage) {
        showStatus("File Error:" + errorMessage);
    }

    public void setStatusMessage(String statusMessage) {
        if (statusMessage != null) showStatus(statusMessage);
    }

    public void scriptEcho(String strEcho) {
    }

    public void scriptStatus(String strStatus) {
    }

    public void notifyScriptTermination(String errorMessage, int msWalltime) {
    }

    public void handlePopupMenu(MouseEvent e) {
        jmolpopup.show(e.getComponent(), e.getX(), e.getY());
    }

    /****************************************************************
   * These methods are intended for use from JavaScript via LiveConnect
   *
   * Note that there are some bug in LiveConnect implementations that
   * place some restrictions on the names of the functions in this file.
   * For example, LiveConnect on Netscape 4.7 will get confused if you
   * overload a method name with different parameter signatures ...
   * ... even if one of the methods is private
   * mth 2003 02
   ****************************************************************/
    private final String[] styleStrings = { "QUICKDRAW", "SHADED", "WIREFRAME" };

    private final byte[] styles = { DisplayControl.QUICKDRAW, DisplayControl.SHADING, DisplayControl.WIREFRAME };

    public void setStyle(String style) {
        for (int i = 0; i < styleStrings.length; ++i) {
            if (styleStrings[i].equalsIgnoreCase(style)) {
                control.setStyleAtom(styles[i]);
                control.setStyleBond(styles[i]);
                return;
            }
        }
    }

    private final String[] labelStyleStrings = { "NONE", "SYMBOL", "NUMBER" };

    private final byte[] labelStyles = { DisplayControl.NOLABELS, DisplayControl.SYMBOLS, DisplayControl.NUMBERS };

    public void setLabelStyle(String style) {
        for (int i = 0; i < labelStyles.length; ++i) {
            if (labelStyleStrings[i].equalsIgnoreCase(style)) {
                control.setStyleLabel(labelStyles[i]);
                return;
            }
        }
    }

    public void setPerspectiveDepth(boolean perspectiveDepth) {
        control.setPerspectiveDepth(perspectiveDepth);
    }

    public void setWireframeRotation(boolean wireframeRotation) {
        control.setWireframeRotation(wireframeRotation);
    }

    public void script(String script) {
        String strError = control.evalString(script);
        setStatusMessage(strError);
    }

    public void load(String modelName) {
        if (modelName != null) {
            String strError = control.openFile(modelName);
            setStatusMessage(strError);
        }
    }

    public void loadInline(String strModel) {
        if (strModel != null) {
            String strError = control.openStringInline(strModel);
            setStatusMessage(strError);
        }
    }
}
