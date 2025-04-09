package fr.esrf.tangoatk.widget.jdraw;

import fr.esrf.Tango.DevFailed;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.core.AttributeList;
import fr.esrf.tangoatk.core.attribute.AttributeFactory;
import fr.esrf.tangoatk.core.attribute.AAttribute;
import fr.esrf.tangoatk.core.attribute.BooleanScalar;
import fr.esrf.tangoatk.core.attribute.DevStateScalar;
import fr.esrf.tangoatk.core.command.*;
import fr.esrf.tangoatk.widget.command.AnyCommandViewer;
import fr.esrf.tangoatk.widget.command.VoidVoidCommandViewer;
import fr.esrf.tangoatk.widget.util.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.io.*;
import java.util.regex.Pattern;
import fr.esrf.tangoatk.widget.util.jdraw.*;
import fr.esrf.tangoatk.widget.attribute.*;
import fr.esrf.tangoatk.widget.util.ErrorPopup;

/**
 * TangoSynopticHandler is the base class used to display and animate any
 * tango synoptic drawing made with the JDraw drawing tool "JDraw".
 *
 * The drawing file is browsed and a behaviour is attached to each drawing
 * component according to the tango object which is associated with.
 *
 * While brawsing the drawing file the name of the graphical component
 * determines the associated tango object:
 *
 * For example if inside the synoptic drawing a simple rectangle is given
 * the name "eas/test-api/1" it will be associated to the tango device
 * eas/test-api/1.
 *
 * The animation on the graphical component depends on the type of the
 * Tango object (device, attribute, command) and the type of JDraw graphical
 * component (simple graphical component, multi-state JDraw object, interactive JDraw object)
 *
 * Here are the default (state) animations provided :
 * <ul>
 * <li>Tango device or Tango state attribute - simple graphical component : the colour of the graphical
 *  component represents the state of the tango device (on, off, alarm, fault...)
 * <li>Tango device or Tango state attribute - Dynamic JDraw object (Dyno) : the Dyno is a group of JDobject where
 * each of them represents the drawing which should be visible for one or more tango state. This way
 * to each state (on, off, alarm, fault, ...) is associated a different form.
 * <li>Tango device command - interactive JDraw object : no state animation
 * <li>Tool Tip : A tooltip can be associated to any tango device. The tooltip can
 * display either the name of the device or it's state according to the tooltip mode used.
 * </ul>
 * <p>
 * In addition to the animation a default interaction behaviour is
 * provided (reaction to mouse clicks). Here are the default interactions :
 *
 * <ul>
 * <li>Tango device or Tango state attribute - simple graphical component : Click on the graphical component
 * will launch a java class whose name has been specified during the drawing phase.
 * If this class name is missing, the generic tool atkpanel is launched. If the class name is set
 * to the predefined string "noPanel" the atkpanel is not launched.
 * <li>Tango device - multi-state JDraw object : the same interaction model
 * <li>Tango device command - interactive JDraw object : Click on the JDraw object will display
 * an "input / output argument window" if the input is required or execute the
 * associated command on the tango device if no input is required.
 * </ul>
 *
 * There are 4 extensions parsed by default :
 * <ul>
 * <li>className: If the Jdraw component is asscoiated to a Tango device or to a
 * Tango state attribute this extension (className) represents the name of the java class
 * to instantiate when the Jdraw object is clicked. This allows to give the name
 * of the panel to be launched when the object is clicked.
 * If the className is not specified the default panel (atkpanel read-only) is launched.
 * If the className is set to "noPanel" nothing is launched.
 * <li>classParam: first argument (always a string) to be passed to the java panel.
 * If classParam is not specified the name associated to the JD component (device name or
 * state attribute name is passed as the String argument to the constructor.
 * <li>valueList: used by NumberScalarComboEditor and StringScalarComboEditor swing objects to acquire the values list
 * <li>shellCommand(*):It allows to launch any shell command or application (dir, xterm, jive, firefox, etc ...) from the synoptic.
 * If a JDObject is interactive and has the shellCommand extension its content will be executed at each valueExceedBounds event
 * The command is always executed in background. Streams redirection is not allowed (should be done inside an script).
 * Arguments can be passed to the shell command.
 * <li>qualityFactor: If the Jdraw component is asscoiated to a NumberScalarAttribute
 * and if this extension (qualityFactor) is set to "true" then the qualityFactor of the attribute
 * will be used to change the background color.
 * </ul>
 *
  * @author      Faranguiss  PONCET
  * @since       ATKWidget-1.9.7
  */
public class TangoSynopticHandler extends JDrawEditor implements IStateListener, IStatusListener, INumberScalarListener, IDevStateScalarListener, IBooleanScalarListener, IDevStateSpectrumListener, WindowListener {

    /** Does not display tooltip */
    public static final int TOOL_TIP_NONE = 0;

    /** Displays device state within tooltip only for Device objects */
    public static final int TOOL_TIP_STATE = 1;

    /** Displays device status within tooltip only for Device objects */
    public static final int TOOL_TIP_STATUS = 2;

    /** Displays the attribute name within tooltip */
    public static final int TOOL_TIP_NAME = 3;

    private static final int STATE_INDEX = 0;

    private static final int STATUS_INDEX = 1;

    private static final String STATE_NONE = "no status";

    private static Map<String, Integer> dynoState;

    private int toolTipMode;

    private String jdrawFileFullName = null;

    private AttributeFactory aFac = null;

    private CommandFactory cFac = null;

    private DeviceFactory dFac = null;

    private AttributeList allAttributes = null;

    private Map<String, List<JDObject>> jdHash;

    private Map<String, List<String>> stateCashHash;

    private AnyCommandViewer acv = null;

    private JFrame argFrame = null;

    private ErrorHistory errorHistWind = null;

    private ErrorPopup errPopup = null;

    private Vector<PanelItem> panelList = new Vector<PanelItem>();

    static {
        dynoState = new HashMap<String, Integer>();
        dynoState.put("ON", new Integer(fr.esrf.Tango.DevState._ON));
        dynoState.put("OFF", new Integer(fr.esrf.Tango.DevState._OFF));
        dynoState.put("CLOSE", new Integer(fr.esrf.Tango.DevState._CLOSE));
        dynoState.put("OPEN", new Integer(fr.esrf.Tango.DevState._OPEN));
        dynoState.put("INSERT", new Integer(fr.esrf.Tango.DevState._INSERT));
        dynoState.put("EXTRACT", new Integer(fr.esrf.Tango.DevState._EXTRACT));
        dynoState.put("MOVING", new Integer(fr.esrf.Tango.DevState._MOVING));
        dynoState.put("STANDBY", new Integer(fr.esrf.Tango.DevState._STANDBY));
        dynoState.put("FAULT", new Integer(fr.esrf.Tango.DevState._FAULT));
        dynoState.put("INIT", new Integer(fr.esrf.Tango.DevState._INIT));
        dynoState.put("RUNNING", new Integer(fr.esrf.Tango.DevState._RUNNING));
        dynoState.put("ALARM", new Integer(fr.esrf.Tango.DevState._ALARM));
        dynoState.put("DISABLE", new Integer(fr.esrf.Tango.DevState._DISABLE));
        dynoState.put("UNKNOWN", new Integer(fr.esrf.Tango.DevState._UNKNOWN));
    }

    /**
   * Construct a TangoSynopticHandler (A JDrawEditor in MODE_PLAY).
   * @see JDrawEditor#MODE_PLAY
   */
    public TangoSynopticHandler() {
        super(JDrawEditor.MODE_PLAY);
        toolTipMode = TOOL_TIP_NAME;
        jdrawFileFullName = null;
        aFac = AttributeFactory.getInstance();
        cFac = CommandFactory.getInstance();
        dFac = DeviceFactory.getInstance();
        errPopup = ErrorPopup.getInstance();
        allAttributes = new AttributeList();
    }

    /**
   * Construct a TangoSynopticHandler (A JDrawEditor in MODE_PLAY).
   * @param jdFileName Filename of the JDraw (jdw) synptic to load.
   * @see #setSynopticFileName
   */
    public TangoSynopticHandler(String jdFileName) throws MissingResourceException, FileNotFoundException, IllegalArgumentException {
        this();
        setSynopticFileName(jdFileName);
    }

    /**
 * Construct a TangoSynopticHandler (A JDrawEditor in MODE_PLAY).
 * @param jdFileName Filename of the JDraw (jdw) synptic to load.
 * @param errh ErrorHistory window which will receive errors.
 * @see #setSynopticFileName
 * @see ErrorHistory
 */
    public TangoSynopticHandler(String jdFileName, ErrorHistory errh) throws MissingResourceException, FileNotFoundException, IllegalArgumentException {
        this();
        if (errh != null) errorHistWind = errh;
        setSynopticFileName(jdFileName);
    }

    /**
 * Construct a TangoSynopticHandler (A JDrawEditor in MODE_PLAY).
 * @param jdFileName Filename of the JDraw (jdw) synptic to load.
 * @param ttMode Tooltip mode
 * @see #TOOL_TIP_NONE
 * @see #TOOL_TIP_STATE
 * @see #TOOL_TIP_STATUS
 * @see #TOOL_TIP_NAME
 */
    public TangoSynopticHandler(String jdFileName, int ttMode) throws MissingResourceException, FileNotFoundException, IllegalArgumentException {
        this();
        if ((ttMode == TOOL_TIP_NONE) || (ttMode == TOOL_TIP_STATE) || (ttMode == TOOL_TIP_STATUS) || (ttMode == TOOL_TIP_NAME)) toolTipMode = ttMode;
        setSynopticFileName(jdFileName);
    }

    /**
 * Returns the current Tooltip Mode
 * @see #setToolTipMode
 */
    public int getToolTipMode() {
        return (toolTipMode);
    }

    /**
 * Sets the current tooltip mode (device object only)
 * @param ttMode Tooltip mode
 * @see #TOOL_TIP_NONE
 * @see #TOOL_TIP_STATE
 * @see #TOOL_TIP_STATUS
 * @see #TOOL_TIP_NAME
 */
    public void setToolTipMode(int ttMode) {
        if ((ttMode == TOOL_TIP_NONE) || (ttMode == TOOL_TIP_STATE) || (ttMode == TOOL_TIP_STATUS) || (ttMode == TOOL_TIP_NAME)) {
            if (toolTipMode != ttMode) {
                toolTipMode = ttMode;
            }
        }
    }

    /**
 * Returns the current error history window
 * @see #setErrorHistoryWindow
 */
    public ErrorHistory getErrorHistoryWindow() {
        return (errorHistWind);
    }

    /**
 * Sets the current error history window. Note that the error history window
 * should be set before the jdraw file is parsed (before the call to the setSynopticFileName)
 */
    public void setErrorHistoryWindow(ErrorHistory errh) {
        if (errh == null) return;
        if (jdrawFileFullName != null) return;
        errorHistWind = errh;
    }

    /**
 * Returns the current synoptic filename.
 * @see #getSynopticFileName
 */
    public String getSynopticFileName() {
        return (jdrawFileFullName);
    }

    /**
   * Returns a Handle to the global attribute list which is used
   * internaly to monitor attributes. This list is filled after
   * setSynopticFileName() is called. To add an error listener
   * to this list, you have to register it before loading a synoptic.
   * @see #setSynopticFileName
   */
    public AttributeList getAttributeList() {
        return allAttributes;
    }

    /** Load a jdraw grpahics input stream into the drawing area.The JLoox and Loox files formats are not supported. This
   *  method allows to load a synoptic which is not necessarily a file on disk. This
   *  method is particularly used when the synoptic jdraw file is pakaged into the
   *  application jarfile and cannot be accessed as a separate file on the disk.
   * @param InputStreamReader inp An InputStreamReader should be created by the application and passed to this method
   * @throws IOException Exception when the inputStream cannot be accessed.
   * @throws MissingResourceException when the "jdraw" inputStream cannot be parsed.
   */
    public void loadSynopticFromStream(InputStreamReader inp) throws IOException, MissingResourceException {
        jdHash = new HashMap<String, List<JDObject>>();
        stateCashHash = new HashMap<String, List<String>>();
        loadFromStream(inp);
        jdrawFileFullName = "InputStreamReader";
        if (getObjectNumber() == 0) throw new MissingResourceException("The Jdraw file has no component inside. First draw a Jdraw File.", "JDrawEditor", null);
        parseJdrawComponents();
        computePreferredSize();
        if (allAttributes.size() > 0) {
            for (int i = 0; i < allAttributes.size(); i++) ((AAttribute) allAttributes.elementAt(i)).refresh();
            allAttributes.startRefresher();
        }
    }

    /**
 * Reads the Jdraw file, browses and parses the synoptic components.
 * The main purpose of this function is to attach Tango entity model to
 * JDraw component.
 * @param jdFileName Filename of the JDraw (jdw) synptic to load.
 */
    public void setSynopticFileName(String jdFileName) throws MissingResourceException, FileNotFoundException, IllegalArgumentException {
        if (jdrawFileFullName != null) clearSynopticFileModel();
        jdHash = new HashMap<String, List<JDObject>>();
        stateCashHash = new HashMap<String, List<String>>();
        try {
            loadFile(jdFileName);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        if (getObjectNumber() == 0) throw new MissingResourceException("The Jdraw file has no component inside. First draw a Jdraw File.", "JDrawEditor", null);
        jdrawFileFullName = jdFileName;
        parseJdrawComponents();
        computePreferredSize();
        if (allAttributes.size() > 0) {
            for (int i = 0; i < allAttributes.size(); i++) ((AAttribute) allAttributes.elementAt(i)).refresh();
            allAttributes.startRefresher();
        }
    }

    private void clearAllPanels() {
        Iterator<PanelItem> it = panelList.iterator();
        while (it.hasNext()) {
            PanelItem pi = it.next();
            removePanel(pi);
        }
        panelList.clear();
        panelList = new Vector<PanelItem>();
    }

    private void removeAllListeners() {
        if (allAttributes == null) return;
        if (allAttributes.isEmpty()) return;
        for (int i = 0; i < allAttributes.size(); i++) {
            Object obj = allAttributes.get(i);
            if (obj instanceof IDevStateScalar) {
                IDevStateScalar idss = (IDevStateScalar) obj;
                idss.removeDevStateScalarListener(this);
                continue;
            }
            if (obj instanceof INumberScalar) {
                INumberScalar ins = (INumberScalar) obj;
                ins.removeNumberScalarListener(this);
                continue;
            }
            if (obj instanceof IBooleanScalar) {
                IBooleanScalar ibs = (IBooleanScalar) obj;
                ibs.removeBooleanScalarListener(this);
                continue;
            }
            if (obj instanceof IDevStateSpectrum) {
                IDevStateSpectrum idss = (IDevStateSpectrum) obj;
                idss.removeDevStateSpectrumListener(this);
                continue;
            }
        }
    }

    public void clearSynopticFileModel() {
        removeAllListeners();
        clearAllPanels();
        if (allAttributes != null) {
            allAttributes.stopRefresher();
            if (allAttributes.size() > 0) allAttributes.clear();
            allAttributes = new AttributeList();
        }
        if (jdHash != null) {
            if (!jdHash.isEmpty()) jdHash.clear();
            jdHash = null;
        }
        if (stateCashHash != null) {
            if (!stateCashHash.isEmpty()) stateCashHash.clear();
            stateCashHash = null;
        }
        jdrawFileFullName = null;
    }

    /**
    * Parses JDraw components , detects tango entity name and attatch a model.
    * This method does not recurse group and use isDevice() , isAttribute()
    * and isDevice() to detect entity name.
    * @see #isDevice
    * @see #isAttribute
    * @see #isCommand
    */
    protected void parseJdrawComponents() {
        for (int i = 0; i < getObjectNumber(); i++) {
            JDObject jdObj = getObjectAt(i);
            String s = jdObj.getName();
            if (isDevice(s)) {
                addDevice(jdObj, s);
            } else {
                if (isAttribute(s)) {
                    addAttribute(jdObj, s);
                } else {
                    if (isSpectrumAttElement(s)) {
                        addSpectrumAttElement(jdObj, s);
                    } else {
                        if (isCommand(s)) {
                            addCommand(jdObj, s);
                        } else {
                            if (jdObj.isInteractive() && jdObj.hasExtendedParam("shellCommand")) {
                                addShellCmdButton(jdObj);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addShellCmdButton(JDObject jdo) {
        String shellCmd = jdo.getExtendedParam("shellCommand");
        if (shellCmd == null) return;
        if (shellCmd.length() == 0) return;
        jdo.addValueListener(new JDValueListener() {

            public void valueChanged(JDObject src) {
            }

            public void valueExceedBounds(JDObject src) {
                String compName = src.getName();
                String command = src.getExtendedParam("shellCommand");
                System.out.println("Interactive Object " + compName + " value Exceed Bounds");
                if (JOptionPane.showConfirmDialog(null, "This Shell Command is going to be executed, Are you sure?\n>" + command, compName + ": Shell Command", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    try {
                        System.out.println("The osName is " + System.getProperty("os.name"));
                        if (command.endsWith("&")) command = command.substring(0, command.length() - 1);
                        System.out.println("ExecutionAccepted:" + command);
                        Runtime rt = Runtime.getRuntime();
                        rt.exec(command);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                } else System.out.println("ExecutionRejected");
            }
        });
    }

    /**
   * Return true only if the given name matches a Tango attribute name.
    * <p>Attribute name allowed syntax ( Can be preceded by tango: ):<p>
    * <pre>
    *   Full syntax: //hostName:portNumber/domain/family/member/attName
    *   Full syntax: //ipAddress:portNumber/domain/family/member/attName
    *   Short syntax: domain/family/member/attName
    * </pre>
    * @param s Attribute name
    */
    protected boolean isAttribute(String s) {
        String attDevName, attName;
        int lastSlash;
        boolean isdev;
        lastSlash = s.lastIndexOf("/");
        if ((lastSlash <= 0) || (lastSlash >= s.length())) return false;
        try {
            attDevName = s.substring(0, lastSlash);
            isdev = isDevice(attDevName);
            if (isdev == false) {
                if (s.toLowerCase().endsWith("/state")) return aFac.isConnectionLessAttribute(s); else return false;
            }
            attName = s.substring(lastSlash, s.length());
            boolean attPattern;
            attPattern = Pattern.matches("/[a-zA-Z_0-9[-]]+", attName);
            if (attPattern == false) {
                return false;
            } else {
                return aFac.isAttribute(s);
            }
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
   * Return true only if the given name matches a Tango attribute name followed by [index].
    * <p>Spectrum Attribute element allowed syntax ( Can be preceded by tango: ):<p>
    * <pre>
    *   Full syntax: //hostName:portNumber/domain/family/member/attName[xx]
    *   Full syntax: //ipAddress:portNumber/domain/family/member/attName[xx]
    *   Short syntax: domain/family/member/attName[xx]
    * </pre>
    * @param s Spectrum Attribute element
    */
    protected boolean isSpectrumAttElement(String s) {
        String attDevName, attName;
        int lastSlash;
        boolean isdev;
        lastSlash = s.lastIndexOf("/");
        if ((lastSlash <= 0) || (lastSlash >= s.length())) return false;
        try {
            attDevName = s.substring(0, lastSlash);
            isdev = isDevice(attDevName);
            if (isdev == false) return false;
            attName = s.substring(lastSlash, s.length());
            boolean attPattern;
            attPattern = Pattern.matches("/[a-zA-Z_0-9[-]]+\\[[0-9]+\\]", attName);
            if (attPattern == true) {
                int leftBracket = s.lastIndexOf("[");
                if ((leftBracket > 0) || (leftBracket < s.length())) {
                    String spectrumAttName = s.substring(0, leftBracket);
                    return aFac.isAttribute(spectrumAttName);
                }
            }
            return false;
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
   * Return true only if the given name matches a Tango command name.
   * <p>Command name allowed syntax ( Can be preceded by tango: ):<p>
   * <pre>
   *   Full syntax: //hostName:portNumber/domain/family/member/cmdName
   *   Full syntax: //ipAddress:portNumber/domain/family/member/cmdName
   *   Short syntax: domain/family/member/cmdName
   * </pre>
   * @param s Command name
   */
    protected boolean isCommand(String s) {
        String cmdDevName, cmdName;
        int lastSlash;
        boolean isdev;
        lastSlash = s.lastIndexOf("/");
        if ((lastSlash <= 0) || (lastSlash >= s.length())) return false;
        try {
            cmdDevName = s.substring(0, lastSlash);
            isdev = isDevice(cmdDevName);
            if (isdev == false) return false;
            cmdName = s.substring(lastSlash, s.length());
            boolean cmdPattern;
            cmdPattern = Pattern.matches("/[a-zA-Z_0-9[-]]+", cmdName);
            if (cmdPattern == false) return false; else return cFac.isCommand(s);
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }

    /**
   * Return true only if the given name matches a Tango device name.
   * <p>Device name allowed syntax ( Can be preceded by tango: ):<p>
   * <pre>
   *   Full syntax: //hostName:portNumber/domain/family/member
   *   Full syntax: //ipAddress:portNumber/domain/family/member
   *   Short syntax: domain/family/member
   * </pre>
   * @param devName Device name
   */
    protected boolean isDevice(String devName) {
        if (!isDeviceName(devName)) return false;
        return dFac.isDevice(devName);
    }

    /**
   * Return true if the given name has a correct tango syntax.
   * @param devName Device name to check
   */
    private boolean isDeviceName(String devName) {
        boolean devNamePattern;
        String s = new String(devName);
        if (s.startsWith("tango:")) s = s.substring(6);
        devNamePattern = Pattern.matches("//[a-zA-Z_0-9]+:[0-9]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+", s);
        if (devNamePattern == false) devNamePattern = Pattern.matches("[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[[a-zA-Z_0-9][-]]+", s);
        if (devNamePattern == false) devNamePattern = Pattern.matches("taco:[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+", s);
        if (devNamePattern == false) devNamePattern = Pattern.matches("taco://[a-zA-Z_0-9]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+", s);
        if (devNamePattern == false) devNamePattern = Pattern.matches("//[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+/[a-zA-Z_0-9[-]]+", s);
        return devNamePattern;
    }

    private void addDevice(JDObject jdObj, String s) {
        try {
            Device dev = dFac.getDevice(s);
            if (dev == null) return;
            if (!dev.areDevPropertiesLoaded()) dev.loadDevProperties();
            addDeviceListener(dev);
            mouseifyDevice(jdObj);
            stashComponent(s, jdObj);
            addDevToStateCashHashMap(s);
            refreshStateJDObj(jdObj, dev);
        } catch (ConnectionException connectionexception) {
            System.out.println("Couldn't load device " + s + " " + connectionexception);
        }
    }

    private void refreshStateJDObj(JDObject jdo, Device dev) {
        List<String> stateStatusCash = null;
        String stateCash = null;
        stateStatusCash = stateCashHash.get(dev.getName());
        if (stateStatusCash != null) {
            try {
                stateCash = stateStatusCash.get(STATE_INDEX);
            } catch (IndexOutOfBoundsException iob) {
            }
        }
        if ((stateCash == null) || (stateCash.equalsIgnoreCase(STATE_NONE))) return;
        if (jdo.isProgrammed()) {
            jdo.setValue(getDynoState(stateCash));
            jdo.refresh();
        } else {
            changeJDobjColor(jdo, ATKConstant.getColor4State(stateCash, dev.getInvertedOpenClose(), dev.getInvertedInsertExtract()));
        }
    }

    private void addDeviceListener(Device device) {
        System.out.println("connecting to a device : " + device);
        device.addStateListener(this);
        device.addStatusListener(this);
        if (errorHistWind != null) device.addErrorListener(errorHistWind);
    }

    private void mouseifyDevice(JDObject jdObj) {
        jdObj.addMouseListener(new JDMouseAdapter() {

            public void mousePressed(JDMouseEvent e) {
                deviceClicked(e);
            }

            public void mouseEntered(JDMouseEvent e) {
                devDisplayToolTip(e);
            }

            public void mouseExited(JDMouseEvent e) {
                devRemoveToolTip();
            }
        });
    }

    private void mouseifyAttribute(JDObject jdObj) {
        jdObj.addMouseListener(new JDMouseAdapter() {

            public void mousePressed(JDMouseEvent e) {
                attributeClicked(e);
            }

            public void mouseEntered(JDMouseEvent e) {
                attDisplayToolTip(e);
            }

            public void mouseExited(JDMouseEvent e) {
                attRemoveToolTip();
            }
        });
    }

    private void mouseifyStateAttribute(JDObject jdObj) {
        jdObj.addMouseListener(new JDMouseAdapter() {

            public void mousePressed(JDMouseEvent e) {
                stateAttributeClicked(e);
            }

            public void mouseEntered(JDMouseEvent e) {
                devDisplayToolTip(e);
            }

            public void mouseExited(JDMouseEvent e) {
                devRemoveToolTip();
            }
        });
    }

    private void mouseifyStateSpectrumElement(JDObject jdObj, IDevStateSpectrum stateSpecAtt) {
        final IDevStateSpectrum att = stateSpecAtt;
        final int elemIndex = getIndexFromName(jdObj.getName());
        jdObj.addMouseListener(new JDMouseAdapter() {

            public void mousePressed(JDMouseEvent e) {
                stateSpectrumElementClicked(e);
            }

            public void mouseEntered(JDMouseEvent e) {
                stateSpectrumElementDisplayToolTip(e, att, elemIndex);
            }

            public void mouseExited(JDMouseEvent e) {
                stateSpectrumElementRemoveToolTip();
            }
        });
    }

    private void deviceClicked(JDMouseEvent evt) {
        JDObject comp = (JDObject) evt.getSource();
        String devName = comp.getName();
        launchPanel(comp, devName, true);
    }

    private void attributeClicked(JDMouseEvent evt) {
        JDObject comp = (JDObject) evt.getSource();
        String attName = comp.getName();
        launchPanel(comp, attName, false);
    }

    private void stateAttributeClicked(JDMouseEvent evt) {
        JDObject comp = (JDObject) evt.getSource();
        String attName = comp.getName();
        int i = attName.lastIndexOf('/');
        if (i > 0) {
            String devName = attName.substring(0, i);
            launchPanel(comp, devName, true);
        }
    }

    private boolean isNoPanel(String panelName) {
        if (panelName.equalsIgnoreCase("noPanel")) return true; else if (panelName.equalsIgnoreCase("no panel")) return true;
        return false;
    }

    private void launchPanel(JDObject comp, String compName, boolean isAdevice) {
        if (comp.hasExtendedParam("className")) {
            String pname = comp.getExtendedParam("className");
            if (isNoPanel(pname)) return;
        }
        if (!comp.hasExtendedParam("className")) {
            if (isAdevice) {
                Window w = getPanel("atkpanel.MainPanel", compName);
                if (w == null) {
                    showDefaultPanel(compName);
                } else {
                    showPanelWindow(w);
                }
            }
            return;
        }
        String clName = comp.getExtendedParam("className");
        String constParam = comp.getExtendedParam("classParam");
        if (constParam.length() == 0) constParam = null;
        System.out.println("clName = " + clName + "  constParam = " + constParam + " compName = " + compName);
        Class panelCl;
        Constructor panelClNew;
        Class[] paramCls = new Class[1];
        Object[] params = new Object[1];
        if (constParam == null) {
            params[0] = compName;
        } else {
            params[0] = constParam;
        }
        System.out.println("params[0]= " + params[0]);
        Window w = getPanel(clName, (String) params[0]);
        if (w != null) {
            showPanelWindow(w);
            return;
        }
        try {
            panelCl = Class.forName(clName);
            paramCls[0] = compName.getClass();
            panelClNew = panelCl.getConstructor(paramCls);
        } catch (ClassNotFoundException clex) {
            showErrorMsg("The panel class : " + clName + " not found; ignored.\n");
            return;
        } catch (Exception e) {
            showErrorMsg("Failed to load the constructor " + clName + "( String ) for the panel class.\n");
            return;
        }
        try {
            Object obj = panelClNew.newInstance(params);
            PanelItem newPanel = addNewPanel(obj, clName, (String) params[0]);
            if (newPanel != null) showPanelWindow(newPanel.parent);
        } catch (InstantiationException instex) {
            showErrorMsg("Failed to instantiate 1 the panel class : " + clName + ".\n");
        } catch (IllegalAccessException accesex) {
            showErrorMsg("Failed to instantiate 2 the panel class : " + clName + ".\n");
        } catch (IllegalArgumentException argex) {
            showErrorMsg("Failed to instantiate 3 the panel class : " + clName + ".\n");
        } catch (InvocationTargetException invoqex) {
            showErrorMsg("Failed to instantiate 4 the panel class : " + clName + ".\n");
            System.out.println(invoqex);
            System.out.println(invoqex.getMessage());
            invoqex.printStackTrace();
        } catch (Exception e) {
            showErrorMsg("Got an exception when instantiate the panel class : " + clName + ".\n");
        }
    }

    private void showErrorMsg(String msg) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException intExcept) {
        }
        JOptionPane.showMessageDialog(null, msg);
    }

    private void showPanelWindow(Window pw) {
        if (pw == null) return;
        if (pw instanceof JFrame) ATKGraphicsUtils.centerFrame(this, (JFrame) pw);
        try {
            Thread.sleep(100);
        } catch (InterruptedException intExcept) {
        }
        if (pw.isVisible()) {
            pw.toFront();
        }
    }

    private Window getPanel(String className, String param) {
        boolean found = false;
        int i = 0;
        PanelItem panel = null;
        while (i < panelList.size() && !found) {
            panel = panelList.get(i);
            found = (panel.className.equals(className)) && (panel.param.equals(param));
            if (!found) i++;
        }
        if (found) return panel.parent;
        return null;
    }

    private PanelItem addNewPanel(Object obj, String className, String param) {
        PanelItem panel = new PanelItem(obj, className, param);
        if (panel.parent != null) {
            panelList.add(panel);
            panel.parent.addWindowListener(this);
            return panel;
        } else return null;
    }

    private void removePanel(PanelItem pi) {
        Window pw = pi.parent;
        if (pw != null) {
            pw.removeWindowListener(this);
            if (pw instanceof JFrame) {
                JFrame jf = (JFrame) pw;
                pw.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(pw, WindowEvent.WINDOW_CLOSING));
            }
        }
    }

    public void windowClosed(WindowEvent e) {
        boolean found = false;
        int i = 0;
        PanelItem panel = null;
        while (i < panelList.size() && !found) {
            panel = panelList.get(i);
            found = (panel.parent == e.getSource());
            if (!found) i++;
        }
        if (found) {
            panelList.remove(i);
        }
    }

    public void windowOpened(WindowEvent e) {
    }

    ;

    public void windowClosing(WindowEvent e) {
    }

    ;

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    private void showDefaultPanel(String devName) {
        Class atkpanelCl;
        Constructor atkpanelClNew;
        Class[] atkpanelParamCls = new Class[5];
        Object[] params = new Object[5];
        System.out.println("showDefaultPanel called");
        try {
            atkpanelCl = Class.forName("atkpanel.MainPanel");
        } catch (ClassNotFoundException clex) {
            showErrorMsg("showDefaultPanel : atkpanel.MainPanel not found; ignored.\n");
            return;
        }
        try {
            atkpanelParamCls[0] = devName.getClass();
            atkpanelParamCls[1] = Class.forName("java.lang.Boolean");
            atkpanelParamCls[2] = atkpanelParamCls[1];
            atkpanelParamCls[3] = atkpanelParamCls[1];
            atkpanelParamCls[4] = atkpanelParamCls[1];
            atkpanelClNew = atkpanelCl.getConstructor(atkpanelParamCls);
        } catch (ClassNotFoundException clex) {
            showErrorMsg("showDefaultPanel :java.lang.Boolean not found; ignored.\n");
            return;
        } catch (Exception e) {
            showErrorMsg("showDefaultPanel : Failed to load the constructor (five arguments) for atkpanel read-only.\n");
            return;
        }
        params[0] = devName;
        params[1] = Boolean.FALSE;
        params[2] = Boolean.TRUE;
        params[3] = Boolean.FALSE;
        params[4] = Boolean.TRUE;
        try {
            Object obj = atkpanelClNew.newInstance(params);
            PanelItem newPanel = addNewPanel(obj, "atkpanel.MainPanel", devName);
            if (newPanel != null) showPanelWindow(newPanel.parent);
        } catch (InstantiationException instex) {
            showErrorMsg("Failed to instantiate 1 the atkpanel read-only.\n");
        } catch (IllegalAccessException accesex) {
            showErrorMsg("Failed to instantiate 2 the atkpanel read-only.\n");
        } catch (IllegalArgumentException argex) {
            showErrorMsg("Failed to instantiate 3 the atkpanel read-only.\n");
        } catch (InvocationTargetException invoqex) {
            showErrorMsg("Failed to instantiate 4 the atkpanel read-only.\n");
            System.out.println(invoqex);
            System.out.println(invoqex.getMessage());
            invoqex.printStackTrace();
        } catch (Exception e) {
            showErrorMsg("Got an exception when instantiate the default panel : atkpanel readonly.\n");
        }
    }

    private void stashComponent(String s, JDObject jdObj) {
        List<JDObject> list = jdHash.get(s);
        if (list == null) list = new Vector<JDObject>();
        list.add(jdObj);
        jdHash.put(s, list);
    }

    private void addDevToStateCashHashMap(String s) {
        List<String> list;
        String str;
        list = stateCashHash.get(s);
        if (list != null) return;
        list = new Vector<String>();
        str = new String(STATE_NONE);
        list.add(STATE_INDEX, str);
        list.add(STATUS_INDEX, str);
        stateCashHash.put(s, list);
    }

    private void addCommand(JDObject jdObj, String s) {
        ICommand cmd = null;
        try {
            cmd = cFac.getCommand(s);
            if (cmd != null) {
                if (jdObj instanceof JDSwingObject) {
                    JComponent swingComp = ((JDSwingObject) jdObj).getComponent();
                    if (swingComp instanceof VoidVoidCommandViewer) {
                        ((VoidVoidCommandViewer) swingComp).setModel(cmd);
                    }
                } else {
                    if (jdObj.isInteractive()) mouseifyCommand(jdObj, cmd);
                }
                cmd.addErrorListener(this);
            }
        } catch (ConnectionException connectionexception) {
            System.out.println("Couldn't load device for command" + s + " " + connectionexception);
        } catch (DevFailed dfEx) {
            System.out.println("Couldn't find the command" + s + " " + dfEx);
        }
    }

    private void mouseifyCommand(JDObject jdpb, ICommand devCmd) {
        final ICommand cmd = devCmd;
        jdpb.addValueListener(new JDValueListener() {

            public void valueChanged(JDObject jdObject) {
            }

            public void valueExceedBounds(JDObject jdObject) {
                System.out.println("Acommand is " + cmd);
                commandClicked(cmd);
            }
        });
    }

    private void commandClicked(ICommand ic) {
        if (ic instanceof InvalidCommand) {
            javax.swing.JOptionPane.showMessageDialog(this, ic.getName() + " is not supported. ", "Error", 1);
            return;
        }
        if (ic instanceof VoidVoidCommand) {
            ic.execute();
            return;
        }
        if (acv == null) {
            argFrame = new JFrame();
            acv = new AnyCommandViewer();
            argFrame.getContentPane().add(acv);
        }
        acv.initialize(ic);
        acv.setBorder(null);
        acv.setInputVisible(true);
        acv.setDeviceButtonVisible(true);
        acv.setDescriptionVisible(true);
        acv.setInfoButtonVisible(true);
        if (!ic.takesInput()) {
            ic.execute();
        }
        argFrame.setTitle(ic.getName());
        argFrame.pack();
        argFrame.setVisible(true);
    }

    private void addStateScalarAttribute(JDObject jdObj, IDevStateScalar model) {
        String attName = model.getName();
        System.out.println("Connecting to a StateScalar attribute : " + attName);
        mouseifyStateAttribute(jdObj);
        stashComponent(attName, jdObj);
        addDevToStateCashHashMap(attName);
        if (!model.areAttPropertiesLoaded()) model.loadAttProperties();
        model.addDevStateScalarListener(this);
        allAttributes.add(model);
        if (errorHistWind != null) model.addErrorListener(errorHistWind);
        refreshStateJDObj(jdObj, model);
    }

    private void refreshStateJDObj(JDObject jdo, IDevStateScalar stateAtt) {
        List<String> stateStatusCash = null;
        String stateCash = null;
        stateStatusCash = stateCashHash.get(stateAtt.getName());
        if (stateStatusCash != null) {
            try {
                stateCash = stateStatusCash.get(STATE_INDEX);
            } catch (IndexOutOfBoundsException iob) {
            }
        }
        if ((stateCash == null) || (stateCash.equalsIgnoreCase(STATE_NONE))) return;
        if (jdo.isProgrammed()) {
            jdo.setValue(getDynoState(stateCash));
            jdo.refresh();
        } else {
            changeJDobjColor(jdo, ATKConstant.getColor4State(stateCash, stateAtt.getInvertedOpenClose(), stateAtt.getInvertedInsertExtract()));
        }
    }

    private void addBooleanScalarAttribute(JDObject jdObj, IBooleanScalar model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof BooleanScalarCheckBoxViewer) {
                ((BooleanScalarCheckBoxViewer) atkObj).setAttModel(model);
                allAttributes.add(model);
                model.addSetErrorListener(errPopup);
                if (errorHistWind != null) model.addErrorListener(errorHistWind);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept IBooleanScalar model");
            }
        } else {
            mouseifyAttribute(jdObj);
            String attName = model.getName();
            System.out.println("connecting to a BooleanScalar attribute : " + attName);
            allAttributes.add(model);
            model.addBooleanScalarListener(this);
            stashComponent(model.getName(), jdObj);
        }
    }

    private void addNumberScalarAttribute(JDObject jdObj, INumberScalar model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof SimpleScalarViewer) {
                ((SimpleScalarViewer) atkObj).setModel(model);
                ((SimpleScalarViewer) atkObj).setHasToolTip(true);
                allAttributes.add(model);
            } else if (atkObj instanceof DigitalNumberScalarViewer) {
                ((DigitalNumberScalarViewer) atkObj).setModel(model);
                ((DigitalNumberScalarViewer) atkObj).setHasToolTip(true);
                allAttributes.add(model);
            } else if (atkObj instanceof NumberScalarWheelEditor) {
                ((NumberScalarWheelEditor) atkObj).setModel(model);
                allAttributes.add(model);
                model.addSetErrorListener(errPopup);
            } else if (atkObj instanceof NumberScalarComboEditor) {
                String valList = jdObj.getExtendedParam("valueList");
                if (valList != null) {
                    if (valList.length() != 0) {
                        double[] possVals = parsePossNumberValues(valList);
                        if (possVals != null) if (possVals.length != 0) model.setPossibleValues(possVals);
                    }
                }
                ((NumberScalarComboEditor) atkObj).setNumberModel(model);
                allAttributes.add(model);
                model.addSetErrorListener(errPopup);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept INumberScalar model");
            }
        } else {
            mouseifyAttribute(jdObj);
            String attName = model.getName();
            System.out.println("connecting to a NumberScalar attribute : " + attName);
            allAttributes.add(model);
            model.addNumberScalarListener(this);
            stashComponent(model.getName(), jdObj);
        }
        if (errorHistWind != null) model.addErrorListener(errorHistWind);
    }

    private void addStateSpectrumAttributeElement(JDObject jdObj, IDevStateSpectrum model) {
        System.out.println("Connecting to a DevStateSpectrum element : " + jdObj.getName());
        if (!model.areAttPropertiesLoaded()) model.loadAttProperties();
        mouseifyStateSpectrumElement(jdObj, model);
        stashComponent(jdObj.getName().toLowerCase(), jdObj);
        model.addDevStateSpectrumListener(this);
        allAttributes.add(model);
        if (errorHistWind != null) model.addErrorListener(errorHistWind);
        model.refresh();
    }

    private double[] parsePossNumberValues(String vals) {
        String[] c = vals.split(",");
        if (c.length == 0) return null;
        if (c.length < 0) {
            return null;
        } else {
            double[] dvals = new double[c.length];
            int j = 0;
            for (int i = 0; i < c.length; i++) {
                try {
                    double dval = Double.parseDouble(c[i]);
                    dvals[j] = dval;
                    j++;
                } catch (Exception ex) {
                    continue;
                }
            }
            if (j <= 0) return null;
            if (j != c.length) {
                double[] retVals = new double[j];
                for (int i = 0; i < j; i++) retVals[i] = dvals[i];
                return retVals;
            } else return dvals;
        }
    }

    private void addNumberSpectrumAttribute(JDObject jdObj, INumberSpectrum model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof NumberSpectrumViewer) {
                NumberSpectrumViewer nsv = (NumberSpectrumViewer) atkObj;
                nsv.setModel(model);
                allAttributes.add(model);
                if (errorHistWind != null) model.addErrorListener(errorHistWind);
                if (nsv.getManageXaxis()) if (model.hasMinxMaxxAttributes()) addSpectMinxMaxxAttributes(nsv, model);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept INumberSpectrum model");
            }
        }
    }

    private void addSpectMinxMaxxAttributes(NumberSpectrumViewer nsv, INumberSpectrum ins) {
        String attFullName;
        IAttribute iatt = null;
        INumberScalar minAtt = null, maxAtt = null;
        if ((nsv == null) || (ins == null)) return;
        if ((ins.getMinxAttName() == null) || (ins.getMaxxAttName() == null)) return;
        try {
            attFullName = ins.getDevice() + "/" + ins.getMinxAttName();
            iatt = null;
            iatt = aFac.getAttribute(attFullName);
        } catch (Exception ex) {
        }
        if (iatt != null) if (iatt instanceof INumberScalar) minAtt = (INumberScalar) iatt;
        if (minAtt == null) return;
        try {
            attFullName = ins.getDevice() + "/" + ins.getMaxxAttName();
            iatt = null;
            iatt = aFac.getAttribute(attFullName);
        } catch (Exception ex) {
        }
        if (iatt != null) if (iatt instanceof INumberScalar) maxAtt = (INumberScalar) iatt;
        if (maxAtt == null) return;
        if (minAtt == maxAtt) return;
        allAttributes.add(minAtt);
        allAttributes.add(maxAtt);
        nsv.setXaxisModels(minAtt, maxAtt);
    }

    private void addNumberImageAttribute(JDObject jdObj, INumberImage model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof NumberImageViewer) {
                ((NumberImageViewer) atkObj).setModel(model);
                allAttributes.add(model);
                if (errorHistWind != null) model.addErrorListener(errorHistWind);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept INumberImage model");
            }
        }
    }

    private void addStringScalarAttribute(JDObject jdObj, IStringScalar model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof SimpleScalarViewer) {
                ((SimpleScalarViewer) atkObj).setModel(model);
                allAttributes.add(model);
                if (errorHistWind != null) model.addErrorListener(errorHistWind);
            } else if (atkObj instanceof StringScalarComboEditor) {
                String valList = jdObj.getExtendedParam("valueList");
                if (valList != null) {
                    if (valList.length() != 0) {
                        String[] possStrVals = parsePossStringValues(valList);
                        if (possStrVals != null) if (possStrVals.length != 0) model.setPossibleValues(possStrVals);
                    }
                }
                ((StringScalarComboEditor) atkObj).setStringModel(model);
                allAttributes.add(model);
                model.addSetErrorListener(errPopup);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept IStringScalar model");
            }
        }
    }

    private void addEnumScalarAttribute(JDObject jdObj, IEnumScalar model) {
        if (jdObj instanceof JDSwingObject) {
            JComponent atkObj = ((JDSwingObject) jdObj).getComponent();
            if (atkObj instanceof SimpleEnumScalarViewer) {
                ((SimpleEnumScalarViewer) atkObj).setModel(model);
                allAttributes.add(model);
                if (errorHistWind != null) model.addErrorListener(errorHistWind);
            } else if (atkObj instanceof EnumScalarComboEditor) {
                ((EnumScalarComboEditor) atkObj).setEnumModel(model);
                allAttributes.add(model);
                model.addSetErrorListener(errPopup);
            } else {
                System.out.println(atkObj.getClass().getName() + " does not accept IStringScalar model");
            }
        }
    }

    private String[] parsePossStringValues(String vals) {
        String[] c;
        try {
            c = vals.split(",");
        } catch (Exception ex) {
            c = null;
        }
        return c;
    }

    private void addAttribute(JDObject jddg, String s) {
        IAttribute att = null;
        try {
            att = aFac.getAttribute(s);
            if (att != null) {
                if (att instanceof IDevStateScalar) {
                    addStateScalarAttribute(jddg, (IDevStateScalar) att);
                    return;
                }
                if (att instanceof BooleanScalar) {
                    addBooleanScalarAttribute(jddg, (IBooleanScalar) att);
                    return;
                }
                if (att instanceof INumberScalar) {
                    addNumberScalarAttribute(jddg, (INumberScalar) att);
                    return;
                }
                if (att instanceof IStringScalar) {
                    addStringScalarAttribute(jddg, (IStringScalar) att);
                    return;
                }
                if (att instanceof IEnumScalar) {
                    addEnumScalarAttribute(jddg, (IEnumScalar) att);
                    return;
                }
                if (att instanceof INumberSpectrum) {
                    addNumberSpectrumAttribute(jddg, (INumberSpectrum) att);
                    return;
                }
                if (att instanceof INumberImage) {
                    addNumberImageAttribute(jddg, (INumberImage) att);
                    return;
                }
            }
        } catch (ConnectionException connectionexception) {
            System.out.println("Couldn't load device for attribute" + s + " " + connectionexception);
        } catch (DevFailed dfEx) {
            System.out.println("Couldn't find the attribute" + s + " " + dfEx);
        }
    }

    private void addSpectrumAttElement(JDObject jdo, String s) {
        String spectrumAttName = null;
        IAttribute att = null;
        int leftBracket = s.lastIndexOf("[");
        if ((leftBracket <= 0) || (leftBracket >= s.length())) return;
        spectrumAttName = s.substring(0, leftBracket);
        try {
            att = aFac.getAttribute(spectrumAttName);
            if (att == null) return;
            if (att instanceof IDevStateSpectrum) {
                addStateSpectrumAttributeElement(jdo, (IDevStateSpectrum) att);
                return;
            }
        } catch (ConnectionException connectionexception) {
            System.out.println("Couldn't load device for attribute" + spectrumAttName + " " + connectionexception);
        } catch (DevFailed dfEx) {
            System.out.println("Couldn't find the attribute" + spectrumAttName + " " + dfEx);
        }
    }

    public void numberScalarChange(NumberScalarEvent evt) {
        JDObject jdObj;
        INumberScalar ins;
        double value = evt.getValue();
        ins = null;
        ins = evt.getNumberSource();
        String s = ins.getName();
        if (ins != null) {
            List list = jdHash.get(s);
            if (list == null) return;
            int nbJdObjs = list.size();
            int i;
            for (i = 0; i < nbJdObjs; i++) {
                jdObj = null;
                jdObj = (JDObject) list.get(i);
                if (jdObj != null) {
                    if (jdObj.isProgrammed()) {
                        int jdValue = 0;
                        if (!Double.isNaN(value)) jdValue = (int) Math.rint(value);
                        if (jdObj.getValue() != jdValue) {
                            jdObj.preRefresh();
                            jdObj.setValue(jdValue);
                            jdObj.refresh();
                        }
                    }
                    if (jdObj instanceof JDBar) {
                        JDBar bar = (JDBar) jdObj;
                        if (bar.getBarValue() != value) {
                            bar.setBarValue(value);
                            jdObj.refresh();
                        }
                    } else if (jdObj instanceof JDSlider) {
                        JDSlider slider = (JDSlider) jdObj;
                        if (slider.getSliderValue() != value) {
                            jdObj.preRefresh();
                            ((JDSlider) jdObj).setSliderValue(value);
                            jdObj.refresh();
                        }
                    }
                }
            }
        }
    }

    public void booleanScalarChange(BooleanScalarEvent evt) {
        IBooleanScalar boolAtt = (IBooleanScalar) evt.getSource();
        boolean newValue = evt.getValue();
        String attName = boolAtt.getName();
        List<JDObject> jdobjList = jdHash.get(attName);
        if (jdobjList == null) return;
        if (jdobjList.isEmpty()) return;
        Iterator<JDObject> jdoIt = jdobjList.iterator();
        while (jdoIt.hasNext()) {
            JDObject jdobj = jdoIt.next();
            if (jdobj.isProgrammed()) {
                if (newValue) jdobj.setValue(1); else jdobj.setValue(0);
                jdobj.refresh();
            }
        }
    }

    public void devStateSpectrumChange(DevStateSpectrumEvent evt) {
        String[] newStates = evt.getValue();
        IDevStateSpectrum att = (IDevStateSpectrum) evt.getSource();
        if (newStates == null) setAllStateElements(att, IDevice.UNKNOWN); else if (newStates.length == 0) setAllStateElements(att, IDevice.UNKNOWN); else updateAllStateElements(att, newStates);
    }

    private void setAllStateElements(IDevStateSpectrum att, String newState) {
        Set<String> synopticKeySet = jdHash.keySet();
        ;
        if (synopticKeySet.isEmpty()) return;
        String attName = att.getName().toLowerCase();
        Iterator<String> it = synopticKeySet.iterator();
        while (it.hasNext()) {
            String synopticKey = it.next();
            if (synopticKey.startsWith(attName)) {
                List<JDObject> jdobjList = jdHash.get(synopticKey);
                int elemIndex = getIndexFromName(synopticKey);
                Iterator<JDObject> jdoIt = jdobjList.iterator();
                while (jdoIt.hasNext()) {
                    JDObject jdobj = jdoIt.next();
                    manageStateSpectrumElementChange(jdobj, elemIndex, att, newState);
                }
            }
        }
    }

    private void updateAllStateElements(IDevStateSpectrum att, String[] newStates) {
        Set<String> synopticKeySet = jdHash.keySet();
        ;
        if (synopticKeySet.isEmpty()) return;
        String attName = att.getName().toLowerCase();
        Iterator<String> it = synopticKeySet.iterator();
        while (it.hasNext()) {
            String synopticKey = it.next();
            if (synopticKey.startsWith(attName)) {
                int elemIndex = getIndexFromName(synopticKey);
                String newElemState;
                if ((elemIndex < 0) || (elemIndex >= newStates.length)) newElemState = IDevice.UNKNOWN; else newElemState = newStates[elemIndex];
                List<JDObject> jdobjList = jdHash.get(synopticKey);
                Iterator<JDObject> jdoIt = jdobjList.iterator();
                while (jdoIt.hasNext()) {
                    JDObject jdobj = jdoIt.next();
                    manageStateSpectrumElementChange(jdobj, elemIndex, att, newElemState);
                }
            }
        }
    }

    private void manageStateSpectrumElementChange(JDObject jdo, int elemIndex, IDevStateSpectrum att, String newState) {
        if (jdo.isProgrammed()) {
            jdo.setValue(getDynoState(newState));
            jdo.refresh();
        } else changeJDobjColor(jdo, ATKConstant.getColor4State(newState, att.getInvertedOpenCloseForElement(elemIndex), att.getInvertedInsertExtractForElement(elemIndex)));
    }

    /** change the color of the jdobj (bg or fg) according to the quality factor
       of the attribute If and Only If the JDobject is not a JDBar, or JDSlider
       and no color mapper is set for the JDobject
       and the "extension" QualityFactor is true **/
    public void stateChange(AttributeStateEvent evt) {
        JDObject jdObj;
        IAttribute iatt;
        String quality = evt.getState();
        iatt = null;
        iatt = (IAttribute) evt.getSource();
        String s = iatt.getName();
        if (iatt != null) {
            List list = jdHash.get(s);
            if (list == null) return;
            int nbJdObjs = list.size();
            int i;
            for (i = 0; i < nbJdObjs; i++) {
                jdObj = null;
                jdObj = (JDObject) list.get(i);
                if (!(jdObj instanceof JDBar) && !(jdObj instanceof JDSlider)) {
                    if ((!jdObj.hasBackgroundMapper()) && (!jdObj.hasForegroundMapper())) if (jdObj.hasExtendedParam("qualityFactor")) {
                        String qfString = jdObj.getExtendedParam("qualityFactor");
                        if (Boolean.valueOf(qfString)) {
                            changeJDobjColourForQuality(jdObj, quality);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void errorChange(ErrorEvent event) {
        Object source = event.getSource();
        if (source instanceof IDevStateScalar) {
            IDevStateScalar src = (IDevStateScalar) event.getSource();
            String state = Device.UNKNOWN;
            manageStateChange(src.getName(), state, source);
            return;
        }
        if (source instanceof INumberScalar) {
            NumberScalarEvent e = new NumberScalarEvent((INumberScalar) source, Double.NaN, event.getTimeStamp());
            numberScalarChange(e);
            return;
        }
        if (source instanceof ICommand) {
            ICommand src = ((ICommand) source);
            ErrorPane.showErrorMessage(this, src.getName(), (ATKException) event.getError());
        }
        if (source instanceof IBooleanScalar) {
            IBooleanScalar src = (IBooleanScalar) event.getSource();
            BooleanScalarEvent e = new BooleanScalarEvent(src, false, event.getTimeStamp());
            booleanScalarChange(e);
            return;
        }
        if (source instanceof IDevStateSpectrum) {
            IDevStateSpectrum src = (IDevStateSpectrum) event.getSource();
            setAllStateElements(src, IDevice.UNKNOWN);
            return;
        }
    }

    private void manageStateChange(String entityName, String state, Object sourceEntity) {
        List<String> stateStatusCash = null;
        stateStatusCash = stateCashHash.get(entityName);
        if (stateStatusCash != null) {
            String stateCash = null;
            try {
                stateCash = stateStatusCash.get(STATE_INDEX);
                if (stateCash != null) {
                    if (stateCash.equals(state)) return;
                }
                stateCash = new String(state);
                stateStatusCash.set(STATE_INDEX, stateCash);
                stateCashHash.put(entityName, stateStatusCash);
            } catch (IndexOutOfBoundsException iob) {
            }
        }
        JDObject jdObj;
        List<JDObject> list = jdHash.get(entityName);
        if (list == null) return;
        int nbJdObjs = list.size();
        int i;
        for (i = 0; i < nbJdObjs; i++) {
            jdObj = list.get(i);
            if (jdObj.isProgrammed()) {
                jdObj.setValue(getDynoState(state));
                jdObj.refresh();
            } else {
                if (sourceEntity instanceof IDevice) {
                    IDevice dev = (IDevice) sourceEntity;
                    changeJDobjColor(jdObj, ATKConstant.getColor4State(state, dev.getInvertedOpenClose(), dev.getInvertedInsertExtract()));
                } else if (sourceEntity instanceof IDevStateScalar) {
                    IDevStateScalar stateAtt = (IDevStateScalar) sourceEntity;
                    changeJDobjColor(jdObj, ATKConstant.getColor4State(state, stateAtt.getInvertedOpenClose(), stateAtt.getInvertedInsertExtract()));
                } else changeJDobjColor(jdObj, ATKConstant.getColor4State(state));
            }
        }
    }

    public void devStateScalarChange(DevStateScalarEvent event) {
        IDevStateScalar src = (IDevStateScalar) event.getSource();
        String state = event.getValue();
        manageStateChange(src.getName(), state, src);
    }

    public void stateChange(StateEvent event) {
        Device device = (Device) event.getSource();
        String state = event.getState();
        manageStateChange(device.getName(), state, device);
    }

    private void changeJDobjColor(JDObject jdo, Color stateColor) {
        if (jdo instanceof JDGroup) {
            changeJDgroupColor(jdo, stateColor);
            return;
        }
        if (jdo.getFillStyle() == JDObject.FILL_STYLE_NONE) jdo.setForeground(stateColor); else jdo.setBackground(stateColor);
        jdo.refresh();
    }

    private void changeJDgroupColor(JDObject jdo, Color stateColor) {
        JDGroup jdg = null;
        int nbChild = 0;
        int idx;
        JDObject currChild = null;
        if (!(jdo instanceof JDGroup)) return;
        if (jdo.getName().equalsIgnoreCase("IgnoreRepaint")) return;
        jdg = (JDGroup) jdo;
        nbChild = jdg.getChildrenNumber();
        for (idx = 0; idx < nbChild; idx++) {
            currChild = jdg.getChildAt(idx);
            if (currChild == null) continue;
            if (currChild.getName().equalsIgnoreCase("IgnoreRepaint")) continue;
            if (currChild instanceof JDGroup) {
                changeJDgroupColor(currChild, stateColor);
                continue;
            }
            if (currChild.getFillStyle() == JDObject.FILL_STYLE_NONE) currChild.setForeground(stateColor); else currChild.setBackground(stateColor);
            currChild.refresh();
        }
    }

    private void changeJDobjColourForQuality(JDObject jdo, String quality) {
        if (jdo instanceof JDGroup) {
            changeJDgroupForQuality(jdo, quality);
            return;
        }
        if (jdo.getFillStyle() == JDObject.FILL_STYLE_NONE) jdo.setForeground(ATKConstant.getColor4Quality(quality)); else jdo.setBackground(ATKConstant.getColor4Quality(quality));
        jdo.refresh();
    }

    private void changeJDgroupForQuality(JDObject jdo, String quality) {
        JDGroup jdg = null;
        int nbChild = 0;
        int idx;
        JDObject currChild = null;
        if (!(jdo instanceof JDGroup)) return;
        if (jdo.getName().equalsIgnoreCase("IgnoreRepaint")) return;
        jdg = (JDGroup) jdo;
        nbChild = jdg.getChildrenNumber();
        for (idx = 0; idx < nbChild; idx++) {
            currChild = jdg.getChildAt(idx);
            if (currChild == null) continue;
            if (currChild.getName().equalsIgnoreCase("IgnoreRepaint")) continue;
            if (currChild instanceof JDGroup) {
                changeJDgroupForQuality(currChild, quality);
                continue;
            }
            if (currChild.getFillStyle() == JDObject.FILL_STYLE_NONE) currChild.setForeground(ATKConstant.getColor4Quality(quality)); else currChild.setBackground(ATKConstant.getColor4Quality(quality));
            currChild.refresh();
        }
    }

    public void statusChange(StatusEvent event) {
        Device device = (Device) event.getSource();
        String s = device.getName();
        List<String> stateStatusCash = null;
        stateStatusCash = stateCashHash.get(s);
        if (stateStatusCash != null) {
            String statusCash = null;
            try {
                statusCash = stateStatusCash.get(STATUS_INDEX);
                if (statusCash != null) {
                    if (statusCash.equals(event.getStatus())) return;
                }
                statusCash = new String(event.getStatus());
                stateStatusCash.set(STATUS_INDEX, statusCash);
                stateCashHash.put(s, stateStatusCash);
            } catch (IndexOutOfBoundsException iob) {
                return;
            }
        }
    }

    private int getDynoState(String deviceState) {
        Integer intObj;
        intObj = (Integer) dynoState.get(deviceState);
        return (intObj.intValue());
    }

    private void devDisplayToolTip(JDMouseEvent e) {
        String devName;
        JDObject jdObj;
        List stateStatusCash = null;
        String stateCash = null;
        if (toolTipMode == TOOL_TIP_NONE) {
            setToolTipText(null);
            return;
        }
        if (toolTipMode == TOOL_TIP_NAME) {
            jdObj = (JDObject) e.getSource();
            devName = jdObj.getName();
            setToolTipText(devName);
            return;
        }
        if (toolTipMode == TOOL_TIP_STATE) {
            jdObj = (JDObject) e.getSource();
            devName = jdObj.getName();
            stateStatusCash = (List) stateCashHash.get(devName);
            if (stateStatusCash == null) {
                setToolTipText(null);
                return;
            }
            try {
                stateCash = (String) stateStatusCash.get(STATE_INDEX);
                if (stateCash == null) {
                    setToolTipText(null);
                    return;
                }
                setToolTipText(stateCash);
                return;
            } catch (IndexOutOfBoundsException iob) {
                setToolTipText(null);
                return;
            }
        }
        setToolTipText(null);
    }

    private void devRemoveToolTip() {
        setToolTipText(null);
    }

    private void attDisplayToolTip(JDMouseEvent e) {
        String attName;
        JDObject jdObj;
        if (toolTipMode != TOOL_TIP_NAME) {
            setToolTipText(null);
            return;
        }
        jdObj = (JDObject) e.getSource();
        attName = jdObj.getName();
        setToolTipText(attName);
    }

    private void attRemoveToolTip() {
        setToolTipText(null);
    }

    private void stateSpectrumElementClicked(JDMouseEvent evt) {
        JDObject comp = (JDObject) evt.getSource();
        String attElemName = comp.getName().toLowerCase();
        launchPanel(comp, attElemName, false);
    }

    private void stateSpectrumElementDisplayToolTip(JDMouseEvent e, IDevStateSpectrum att, int elemIndex) {
        String[] stateLabels = null;
        JDObject jdObj;
        if (toolTipMode != TOOL_TIP_NAME) {
            setToolTipText(null);
            return;
        }
        jdObj = (JDObject) e.getSource();
        if (elemIndex < 0) {
            setToolTipText(jdObj.getName());
            return;
        }
        stateLabels = att.getStateLabels();
        if ((stateLabels == null) || (elemIndex >= stateLabels.length)) {
            setToolTipText(jdObj.getName());
            return;
        }
        setToolTipText(stateLabels[elemIndex]);
    }

    private void stateSpectrumElementRemoveToolTip() {
        setToolTipText(null);
    }

    private int getIndexFromName(String elemName) {
        int index = -1;
        int leftBracket = elemName.lastIndexOf("[");
        int rightBracket = elemName.lastIndexOf("]");
        if ((leftBracket < 0) || (leftBracket >= elemName.length())) return -1;
        if ((rightBracket < 0) || (rightBracket >= elemName.length())) return -1;
        if (leftBracket >= rightBracket) return -1;
        String indexNumber = elemName.substring(leftBracket + 1, rightBracket);
        try {
            index = Integer.parseInt(indexNumber);
        } catch (NumberFormatException nfe) {
        }
        return index;
    }

    public static void main(String args[]) {
        TangoSynopticHandler tsh = null;
        try {
            tsh = new TangoSynopticHandler("/users/poncet/JDRAW_FILES/sy_klyst.jdw", TOOL_TIP_NAME);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Prog Aborted.");
            System.exit(-1);
        }
        JFrame jf = new JFrame();
        jf.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        jf.setContentPane(tsh);
        jf.pack();
        jf.setVisible(true);
    }
}

/**
 * Class which handle panel unicity.
 */
class PanelItem {

    Window parent = null;

    String className = "";

    String param = "";

    PanelItem(Object parent, String className, String param) {
        if (parent instanceof Window) {
            this.parent = (Window) parent;
        } else {
            System.out.println("Warning, " + className + "is not a Window");
        }
        this.className = className;
        this.param = param;
    }
}
