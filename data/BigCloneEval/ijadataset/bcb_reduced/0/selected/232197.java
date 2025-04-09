package net.etherstorm.jopenrpg.swing.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import net.etherstorm.jopenrpg.ReferenceManager;
import net.etherstorm.jopenrpg.net.MapMessage;
import net.etherstorm.jopenrpg.swing.nodehandlers.PreferencesNode;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * MiniatureLayer is the class responsible for tracking, storing, and displaying
 * miniatures on the map
 *
 * @author $Author: tedberg $
 * @version $Revision: 1.26 $
 */
public class MiniatureLayer extends AbstractLayer implements PropertyChangeListener {

    public static final String ACTION_DELETE = "del";

    public static final String ACTION_NEW = "new";

    public static final String ACTION_UPDATE = "update";

    public static final String ATTRIBUTE_ACTION = "action";

    public static final String ATTRIBUTE_SERIAL_NUMBER = "serial";

    public static final String ELEMENT_MINIATURE = "miniature";

    public static final String ELEMENT_MINIATURES = "miniatures";

    int _zmax = 0;

    boolean autoLabel = true;

    int idCounter = 0;

    protected ArrayList _miniatureList;

    PreferencesNode prefs = new PreferencesNode();

    protected int serial;

    protected XMLOutputter xout;

    ZorderComparator zorderComparator = new ZorderComparator();

    public MiniatureLayer() {
    }

    /**
	 * Constructor declaration
	 *
	 *
	 * @param p
	 *
	 */
    public MiniatureLayer(JMapPanel p) {
        super(p);
        _miniatureList = new ArrayList();
        xout = new XMLOutputter();
        xout.setIndent("	");
    }

    /**
	 * @param mini
	 */
    protected void add(Miniature mini) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            if (((Miniature) iter.next()).getId().equals(mini.getId())) {
                System.out.println("MiniatureLayer.add()");
                System.out.println("Attempted to add Miniature " + mini.toString());
                return;
            }
        }
        if ((mini.getId()) == null || (mini.getId().trim().equals(""))) mini.setId(genId());
        if (mini.getZorder() < 1) {
            _zmax += 1;
            mini.setZorder(_zmax);
        }
        mini.setMiniAction(Miniature.NEW);
        mini.setMapPanel(getMapPanel());
        getMiniatures().add(mini);
        sortMinis();
        mini.addPropertyChangeListener(this);
        logger.debug("Added miniature to list " + mini);
    }

    /**
	 * @param xml
	 */
    public void addMiniature(Element xml) {
        MapMessage mm = MapMessage.createMapMessage();
        Element e = (Element) xml.clone();
        e.setAttribute("id", genId());
        mm.getMessage().getChild(ELEMENT_MINIATURES).addContent(e);
        mm.send();
    }

    /**
	 * @param url
	 * @param count
	 * @return
	 */
    public Miniature[] addMiniature(URL url, int count) {
        Miniature[] list = new Miniature[count];
        for (int loop = 0; loop < count; loop++) {
            DnDMiniature mini = new DnDMiniature();
            mini.setPath(url.toString());
            mini.setMiniAction(Miniature.NEW);
            if (autoLabel) {
                String name = url.toString().substring(url.toString().lastIndexOf('/') + 1, url.toString().lastIndexOf('.'));
                mini.setLabel(name);
            }
            list[loop] = mini;
        }
        addMiniatures(list);
        return list;
    }

    /**
	 * @param url
	 * @return
	 */
    public Miniature addMiniature(java.net.URL url) {
        DnDMiniature mini = new DnDMiniature();
        mini.setPath(url.toString());
        mini.setMiniAction(Miniature.NEW);
        if (autoLabel) {
            String name = url.toString().substring(url.toString().lastIndexOf('/') + 1, url.toString().lastIndexOf('.'));
            mini.setLabel(name);
        }
        addMiniature(mini);
        return mini;
    }

    /**
	 * @param list
	 */
    public void addMiniatures(Miniature[] list) {
        deselectAll();
        MapMessage mm = MapMessage.createMapMessage();
        for (int loop = 0; loop < list.length; loop++) {
            add(list[loop]);
            list[loop].setSelected(true);
            mm.getMessage().getChild(ELEMENT_MINIATURES).addContent(list[loop].toXml());
        }
        mm.send();
    }

    public void addMiniatures(ArrayList list) {
        addMiniatures((Miniature[]) list.toArray(new Miniature[] { new Miniature() }));
    }

    /**
	 * @param mini
	 */
    public void addMiniature(Miniature mini) {
        add(mini);
        mini.setSelected(true);
        MapMessage mm = MapMessage.createMapMessage();
        mm.getMessage().getChild(ELEMENT_MINIATURES).addContent(mini.toXml());
        mm.send();
    }

    /**
	 * Removes all miniatures from the layer.
	 *
	 *
	 */
    public void clear() {
        reset();
    }

    /**
	 * 
	 * @see net.etherstorm.jopenrpg.swing.map.AbstractLayer#clearDelta()
	 */
    public void clearDelta() {
        super.clearDelta();
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) ((Miniature) iter.next()).clearDelta();
    }

    /**
	 * 
	 */
    public void deselectAll() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            ((Miniature) iter.next()).setSelected(false);
        }
    }

    /**
	 * @return
	 */
    public int firstSelectedIndex() {
        int index = -1;
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            index++;
            if (((Miniature) iter.next()).isSelected()) return index;
        }
        return -1;
    }

    /**
	 * @return
	 */
    protected String genId() {
        try {
            return java.text.MessageFormat.format("{0}-{1}", new Object[] { ReferenceManager.getInstance().getCore().getId(), String.valueOf(idCounter++) });
        } catch (Exception ex) {
            return "xx-xx";
        }
    }

    public Miniature getClosestSelectedMiniToPoint(Point p) {
        int dist = Integer.MAX_VALUE;
        Miniature result = null;
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) {
                int d = (int) Math.sqrt(Math.pow(p.x - mini.getPosx(), 2) + Math.pow(p.y - mini.getPosy(), 2));
                if (d < dist) {
                    dist = d;
                    result = mini;
                }
            }
        }
        return result;
    }

    /**
	 * @return
	 * @see net.etherstorm.jopenrpg.swing.map.AbstractLayer#getDelta()
	 */
    public Element getDelta() {
        getDelta(ELEMENT_MINIATURES);
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            try {
                Miniature mini = (Miniature) iter.next();
                if (mini.hasDelta()) delta.addContent(mini.getDelta());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return delta;
    }

    public int getIdCounter() {
        return idCounter;
    }

    public ArrayList getMiniatures() {
        return _miniatureList;
    }

    public int getSelectedCount() {
        int selcount = 0;
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            if (((Miniature) iter.next()).isSelected()) selcount++;
        }
        return selcount;
    }

    /**
	 * Returns the current serial number.
	 *
	 *
	 * @return serial
	 *
	 */
    public int getSerial() {
        return this.serial;
    }

    public boolean hasDelta() {
        boolean result = super.hasDelta();
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) if (((Miniature) iter.next()).hasDelta()) result = true;
        return result;
    }

    public boolean hasSelected() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            if (((Miniature) iter.next()).isSelected()) return true;
        }
        return false;
    }

    public boolean isAutolabel() {
        return autoLabel;
    }

    public void keyPadMove(KeyEvent evt) {
        if (prefs.getKeypadMoveStyle() == 1) {
            keyPadMoveStyle2(evt);
        } else keyPadMoveStyle1(evt);
    }

    /**
	 * @param evt
	 */
    protected void keyPadMoveStyle1(KeyEvent evt) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) switch(evt.getKeyCode()) {
                case KeyEvent.VK_A:
                case KeyEvent.VK_NUMPAD4:
                    {
                        int tmp = mini.getFace() - 1;
                        if (tmp < Miniature.FACING_NORTH) tmp = Miniature.FACING_NORTHWEST;
                        mini.setFace(tmp);
                        break;
                    }
                case KeyEvent.VK_D:
                case KeyEvent.VK_NUMPAD6:
                    {
                        int tmp = mini.getFace() + 1;
                        if (tmp > Miniature.FACING_NORTHWEST) tmp = Miniature.FACING_NORTH;
                        mini.setFace(tmp);
                        break;
                    }
                case KeyEvent.VK_W:
                case KeyEvent.VK_NUMPAD8:
                    {
                        mini.stepForward(stepSize(evt));
                        break;
                    }
                case KeyEvent.VK_S:
                case KeyEvent.VK_NUMPAD5:
                    {
                        mini.stepBackward(stepSize(evt));
                        break;
                    }
            }
        }
    }

    /**
	 * @param evt
	 */
    protected void keyPadMoveStyle2(KeyEvent evt) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) {
                switch(evt.getKeyCode()) {
                    case KeyEvent.VK_E:
                    case KeyEvent.VK_NUMPAD9:
                        {
                            mini.setFace(Miniature.FACING_NORTHEAST);
                            break;
                        }
                    case KeyEvent.VK_W:
                    case KeyEvent.VK_NUMPAD8:
                        {
                            mini.setFace(Miniature.FACING_NORTH);
                            break;
                        }
                    case KeyEvent.VK_Q:
                    case KeyEvent.VK_NUMPAD7:
                        {
                            mini.setFace(Miniature.FACING_NORTHWEST);
                            break;
                        }
                    case KeyEvent.VK_D:
                    case KeyEvent.VK_NUMPAD6:
                        {
                            mini.setFace(Miniature.FACING_EAST);
                            break;
                        }
                    case KeyEvent.VK_S:
                    case KeyEvent.VK_NUMPAD5:
                        {
                            mini.setFace(Miniature.FACING_NONE);
                            break;
                        }
                    case KeyEvent.VK_A:
                    case KeyEvent.VK_NUMPAD4:
                        {
                            mini.setFace(Miniature.FACING_WEST);
                            break;
                        }
                    case KeyEvent.VK_C:
                    case KeyEvent.VK_NUMPAD3:
                        {
                            mini.setFace(Miniature.FACING_SOUTHEAST);
                            break;
                        }
                    case KeyEvent.VK_X:
                    case KeyEvent.VK_NUMPAD2:
                        {
                            mini.setFace(Miniature.FACING_SOUTH);
                            break;
                        }
                    case KeyEvent.VK_Z:
                    case KeyEvent.VK_NUMPAD1:
                        {
                            mini.setFace(Miniature.FACING_SOUTHWEST);
                            break;
                        }
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_NUMPAD0:
                        {
                            mini.stepForward(stepSize(evt));
                            break;
                        }
                    default:
                        {
                        }
                }
            }
        }
    }

    public void makeMinisNew() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) ((Miniature) iter.next()).setMiniAction(ACTION_NEW);
    }

    public void moveToGhosts() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) mini.moveToOffset();
        }
    }

    /**
	 * Returns the next serial number in the progression.
	 *
	 *
	 * @return ++serial
	 *
	 */
    public int nextSerial() {
        return ++serial;
    }

    public void offsetGhostsBy(int dx, int dy) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) {
                if (getMapPanel().isGridSnap()) {
                    if (mini.getAlign() == Miniature.SNAP_ALIGN_CENTER) {
                        Point p = panel.getSnapPoint(mini.getCenterX() + dx, mini.getCenterY() + dy);
                        mini.offsetBy(p.x, p.y);
                    } else {
                        Point p = panel.getSnapPoint(mini.getPosx() + dx, mini.getPosy() + dy);
                        mini.offsetBy(p.x, p.y);
                    }
                } else {
                    mini.offsetBy(dx, dy);
                }
            }
        }
    }

    public void offsetGhostsTo(int x, int y) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) {
                if (getMapPanel().isGridSnap()) {
                    Point p = getMapPanel().getSnapPoint(x, y);
                    int dx = p.x - mini.getPosx();
                    int dy = p.y - mini.getPosy();
                    if (mini.getAlign() == Miniature.SNAP_ALIGN_CENTER) {
                        mini.offsetBy(p.x - mini.getCenterX(), p.y - mini.getCenterY());
                    } else mini.offsetBy(dx, dy);
                } else {
                    mini.offsetBy(x, y);
                }
            }
        }
    }

    /**
	 * Calls Miniature.paint on each miniature in the list.
	 *
	 * @param g
	 * @see net.etherstorm.jopenrpg.swing
	 * @see Miniature#paint(Graphics2D)
	 */
    public void paint(Graphics g) {
        int size = getMiniatures().size() - 1;
        for (int loop = size; loop > -1; loop--) ((Miniature) getMiniatures().get(loop)).paint((Graphics2D) g);
    }

    /**
	 * Assigns the list of &lt;miniature /&gt; Elements new id attributes
	 */
    public void reIdMinis(java.util.List minis) {
        Iterator iter = minis.iterator();
        while (iter.hasNext()) {
            ((Element) iter.next()).setAttribute("id", genId());
        }
    }

    public void removeMiniature(Miniature mini) {
        mini.setMiniAction(Miniature.DELETE);
        getMiniatures().remove(mini);
    }

    public void removeSelectedMiniatures() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.isSelected()) {
                mini.setMiniAction(Miniature.DELETE);
                iter.remove();
            }
        }
    }

    /**
	* 
	*/
    public void reset() {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            ((Miniature) iter.next()).setMiniAction(Miniature.DELETE);
            iter.remove();
        }
        panel.getInspector().clear();
        _zmax = 0;
    }

    /**
	 * @param p
	 * @return
	 */
    public boolean selectAt(Point p) {
        Iterator iter = getMiniatures().iterator();
        boolean test = false;
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.contains(p)) {
                mini.setSelected(!mini.isSelected());
                test = true;
                break;
            }
        }
        return test;
    }

    /**
	 * @param p
	 * @return
	 */
    public Miniature getMiniatureAt(Point p) {
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            if (mini.contains(p)) return mini;
        }
        return null;
    }

    /**
	 * @param poly
	 */
    public void selectIn(Polygon poly) {
        ReferenceManager.getInstance().getInspector().setBatching(true);
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) {
            Miniature mini = (Miniature) iter.next();
            mini.setSelected(poly.intersects(mini.getBoundingRect().getBounds2D()));
        }
        ReferenceManager.getInstance().getInspector().setBatching(false);
    }

    public void selectNext() {
        try {
            int index = firstSelectedIndex();
            index++;
            deselectAll();
            logger.debug("Selecting mini :" + index);
            if (index > getMiniatures().size() - 1) index = 0;
            Miniature mini = (Miniature) getMiniatures().get(index);
            mini.setSelected(true);
            getMapPanel().centerOnPoint(mini.getCenterX(), mini.getCenterY());
        } catch (IndexOutOfBoundsException ioobe) {
        } catch (Exception ex) {
            net.etherstorm.jopenrpg.util.ExceptionHandler.handleException(ex);
        }
    }

    public void setAutolabel(boolean value) {
        autoLabel = value;
    }

    public void setIdCounter(int val) {
        idCounter = val;
    }

    public void setMiniatures(ArrayList al) {
        _miniatureList = al;
    }

    /**
	 * Sets the current serial number.
	 *
	 *
	 * @param serial
	 *
	 */
    public void setSerial(int serial) {
        this.serial = serial;
    }

    public void sortMinis() {
        Collections.sort(getMiniatures(), zorderComparator);
    }

    /**
	 * @param evt
	 * @return
	 */
    protected int stepSize(KeyEvent evt) {
        int step = panel.getGridSize();
        if ((evt.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) step = 1;
        return step;
    }

    public Element toXml() {
        Element e = new Element(ELEMENT_MINIATURES);
        e.setAttribute(ATTRIBUTE_SERIAL_NUMBER, String.valueOf(getSerial()));
        Iterator iter = getMiniatures().iterator();
        while (iter.hasNext()) e.addContent(((Miniature) iter.next()).toXml());
        return e;
    }

    /**
	 *
	 */
    public void updateProperties(Element elem) {
        beginUpdate();
        Element e = elem.getChild(ELEMENT_MINIATURES);
        if (e == null) {
            return;
        }
        Iterator iter = e.getAttributes().iterator();
        while (iter.hasNext()) {
            try {
                Attribute attrib = (Attribute) iter.next();
                if (attrib.getName().equals(ATTRIBUTE_SERIAL_NUMBER)) {
                    setSerial(attrib.getIntValue());
                }
            } catch (Exception ex) {
            }
        }
        logger.debug(xout.outputString(e));
        iter = e.getChildren(ELEMENT_MINIATURE).iterator();
        while (iter.hasNext()) {
            Element mini = (Element) iter.next();
            if (mini.getAttributeValue(ATTRIBUTE_ACTION).equals(ACTION_NEW)) {
                Miniature miniature = null;
                try {
                    Class k = getClass().getClassLoader().loadClass(mini.getAttributeValue(MapConstants.MINIATURE_TYPE, Miniature.class.getName()));
                    Constructor cons = k.getConstructor(new Class[] {});
                    miniature = (Miniature) cons.newInstance(new Object[] {});
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                } catch (SecurityException se) {
                    miniature = new Miniature();
                    se.printStackTrace();
                } catch (NoSuchMethodException nsme) {
                    miniature = new Miniature();
                    nsme.printStackTrace();
                } catch (IllegalArgumentException iae) {
                    miniature = new Miniature();
                    iae.printStackTrace();
                } catch (InstantiationException ie) {
                    miniature = new Miniature();
                    ie.printStackTrace();
                } catch (IllegalAccessException iarge) {
                    miniature = new Miniature();
                    iarge.printStackTrace();
                } catch (InvocationTargetException ite) {
                    miniature = new Miniature();
                    ite.printStackTrace();
                }
                miniature.fromXML(mini);
                add(miniature);
            } else if (mini.getAttributeValue(ATTRIBUTE_ACTION).equals(ACTION_UPDATE)) {
                for (int loop = 0; loop < getMiniatures().size(); loop++) {
                    Miniature m = (Miniature) getMiniatures().get(loop);
                    if (m.getId().equals(mini.getAttributeValue(Miniature.ATTRIBUTE_ID))) {
                        m.fromXML(mini);
                        break;
                    }
                }
            } else if (mini.getAttributeValue(ATTRIBUTE_ACTION).equals(ACTION_DELETE)) {
                for (int loop = 0; loop < getMiniatures().size(); loop++) {
                    Miniature m = (Miniature) getMiniatures().get(loop);
                    if (m.getId().equals(mini.getAttributeValue(Miniature.ATTRIBUTE_ID))) {
                        getMiniatures().remove(m);
                        break;
                    }
                }
            }
        }
        endUpdate();
    }

    /**
	 * @param evt
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Miniature.ATTRIBUTE_ZORDER)) {
            sortMinis();
        }
    }
}

class ZorderComparator implements Comparator {

    /**
	 * @param o1
	 * @param o2
	 * @return
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        try {
            Miniature m1 = (Miniature) o1;
            Miniature m2 = (Miniature) o2;
            if (m1.getZorder() > m2.getZorder()) return -1;
            if (m1.getZorder() < m2.getZorder()) return 1;
        } catch (Exception ex) {
            return 0;
        }
        return 0;
    }
}
