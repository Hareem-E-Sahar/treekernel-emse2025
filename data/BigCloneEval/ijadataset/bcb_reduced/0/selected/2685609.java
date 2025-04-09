package org.swixml;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;
import jmxm.gui.monitor.ChartFactory;
import jmxm.gui.monitor.TimeSeriesFactory;
import jmxm.gui.monitor.XTimeChart;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.swixml.converters.ConstraintsConverter;
import org.swixml.converters.LocaleConverter;
import org.swixml.converters.PrimitiveConverter;

/**
 * Singleton Parser to render XML for Swing Documents <p/> <img src="doc-files/swixml_1_0.png"
 * ALIGN="center">
 * </p>
 * 
 * @author <a href="mailto:wolf@paulus.com">Wolf Paulus</a>
 * @author <a href="mailto:fm@authentidate.de">Frank Meissner</a>
 * @version $Revision: 1.4 $
 * @see org.swixml.SwingTagLibrary
 * @see org.swixml.ConverterLibrary
 */
public class Parser {

    public static final String ATTR_CLASS = "class";

    /**
	 * Additional attribute to collect layout constrain information
	 */
    public static final String ATTR_CONSTRAINTS = "constraints";

    /**
	 * Additional attribute to collect information about the PLAF implementation
	 */
    public static final String ATTR_PLAF = "plaf";

    /**
	 * Additional attribute to collect layout constrain information
	 */
    public static final String ATTR_BUNDLE = "bundle";

    /**
	 * Additional attribute to collect layout constrain information
	 */
    public static final String ATTR_LOCALE = "locale";

    /**
	 * Allows to provides swixml tags with an unique id
	 */
    public static final String ATTR_ID = "id";

    /**
	 * Allows to provides swixml tags with an unique id
	 */
    public static final String ATTR_REFID = "refid";

    /**
	 * Allows to provides swixml tags with an unique id
	 */
    public static final String ATTR_INCLUDE = "include";

    /**
	 * Allows to provides swixml tags with a dynamic update class
	 */
    public static final String ATTR_INITCLASS = "initclass";

    /**
	 * Allows to provides swixml tags with a dynamic update class
	 */
    public static final String ATTR_ACTION = "action";

    /**
	 * Method name used with initclass - if this exit, the update class will no be instanced but
	 * getInstance is called
	 */
    public static final String GETINSTANCE = "getInstance";

    /**
	 * Localiced Attributes
	 */
    public static final Vector<Object> LOCALIZED_ATTRIBUTES = new Vector<Object>();

    /**
	 * the calling engine
	 */
    private SwingEngine engine;

    /**
	 * ConverterLib, to access COnverters, converting String in all kinds of things
	 */
    private ConverterLibrary cvtlib = ConverterLibrary.getInstance();

    /**
	 * map to store id-id components, needed to support labelFor attributes
	 */
    private Map lbl_map = new HashMap();

    /**
	 * map to store specific Mac OS actions mapping
	 */
    private Map mac_map = new HashMap();

    /**
	 * docoument, to be parsed
	 */
    private Document jdoc;

    /**
	 * Static Initializer adds Attribute Names into the LOCALIZED_ATTRIBUTES Vector Needs to be
	 * inserted all lowercase.
	 */
    static {
        LOCALIZED_ATTRIBUTES.add("accelerator");
        LOCALIZED_ATTRIBUTES.add("icon");
        LOCALIZED_ATTRIBUTES.add("iconimage");
        LOCALIZED_ATTRIBUTES.add("label");
        LOCALIZED_ATTRIBUTES.add("mnemonic");
        LOCALIZED_ATTRIBUTES.add("name");
        LOCALIZED_ATTRIBUTES.add("text");
        LOCALIZED_ATTRIBUTES.add("title");
        LOCALIZED_ATTRIBUTES.add("titleat");
        LOCALIZED_ATTRIBUTES.add("titles");
        LOCALIZED_ATTRIBUTES.add("tooltiptext");
    }

    /**
	 * Constructs a new SwixMl Parser for the provided engine.
	 * 
	 * @param engine
	 *            <code>SwingEngine</code>
	 */
    public Parser(SwingEngine engine) {
        this.engine = engine;
    }

    /**
	 * Converts XML into a javax.swing object tree.
	 * 
	 * <pre>
	 *   Note: This parse method does not return a swing object but converts all &lt;b&gt;sub&lt;/b&gt; nodes
	 *   of the xml documents root into seing objects and adds those into the provided container.
	 *   This is useful when a JApplet for instance already exists and need to get some gui inserted.
	 * </pre>
	 * 
	 * @param jdoc
	 *            <code>Document</code> providing the XML document
	 * @param container
	 *            <code>Container</code> container for the XML root's children
	 * @throws Exception
	 */
    public void parse(Document jdoc_, Container container) throws Exception {
        this.jdoc = jdoc_;
        this.lbl_map.clear();
        this.mac_map.clear();
        getSwing(processCustomAttributes(jdoc.getRootElement()), container);
        linkLabels();
        this.lbl_map.clear();
        this.mac_map.clear();
    }

    /**
	 * Converts XML into a javax.swing object tree.
	 * 
	 * <pre>
	 *      Reads XML from the provied
	 * <code>
	 * Reader
	 * </code>
	 *   and builds an intermediate jdom document.
	 *      Tags and their attributes are getting converted into swing objects.
	 * </pre>
	 * 
	 * @param jdoc
	 *            <code>Document</code> providing the XML document
	 * @return <code>java.awt.Container</code> root object for the swing object tree
	 * @throws Exception
	 */
    public Object parse(Document jdoc_) throws Exception {
        this.jdoc = jdoc_;
        this.lbl_map.clear();
        Object obj = getSwing(processCustomAttributes(jdoc.getRootElement()), null);
        linkLabels();
        this.lbl_map.clear();
        this.mac_map.clear();
        return obj;
    }

    /**
	 * Looks for custom attributes to be proccessed.
	 * 
	 * @param element
	 *            <code>Element</code> custom attr. tag are looked for in this jdoc element
	 * @return <code>Element</code> - passed in (and maybe modified) element
	 * 
	 * <pre>
	 *            &lt;b&gt;Note:&lt;/b&gt;
	 * <br>
	 *  Successfully proccessed custom attributes will be removed from the jdoc element.
	 * </pre>
	 */
    private Element processCustomAttributes(Element element) throws Exception {
        Attribute locale = element.getAttribute(Parser.ATTR_LOCALE);
        if (locale != null && locale.getValue() != null) {
            engine.setLocale(LocaleConverter.conv(locale));
            element.removeAttribute(Parser.ATTR_LOCALE);
        }
        Attribute bundle = element.getAttribute(Parser.ATTR_BUNDLE);
        if (bundle != null && bundle.getValue() != null) {
            engine.getLocalizer().setResourceBundle(bundle.getValue());
            element.removeAttribute(Parser.ATTR_BUNDLE);
        }
        Attribute plaf = element.getAttribute(Parser.ATTR_PLAF);
        if (plaf != null && plaf.getValue() != null && 0 < plaf.getValue().length()) {
            element.removeAttribute(Parser.ATTR_PLAF);
        }
        return element;
    }

    /**
	 * Helper Method to Link Labels to InputFields etc.
	 */
    private void linkLabels() {
        Iterator it = lbl_map.keySet().iterator();
        while (it != null && it.hasNext()) {
            JLabel lbl = (JLabel) it.next();
            String id = lbl_map.get(lbl).toString();
            try {
                lbl.setLabelFor((Component) engine.getIdMap().get(id));
            } catch (ClassCastException e) {
            }
        }
    }

    ;

    /**
	 * Recursively converts <code>org.jdom.Element</code>s into <code>javax.swing</code> or
	 * <code>java.awt</code> objects
	 * 
	 * @param element
	 *            <code>org.jdom.Element</code> XML tag
	 * @param obj
	 *            <code>Object</code> if not null, only this elements children will be processed,
	 *            not the element itself
	 * @return <code>java.awt.Container</code> representing the GUI impementation of the XML tag.
	 * @throws Exception
	 */
    Object getSwing(Element element, Object obj) throws Exception {
        Factory factory = engine.getTaglib().getFactory(element.getName());
        String id = element.getAttribute(Parser.ATTR_ID) != null ? element.getAttribute(Parser.ATTR_ID).getValue().trim() : null;
        boolean unique = !engine.getIdMap().containsKey(id);
        boolean constructed = false;
        if (!unique) {
            throw new IllegalStateException("id already in use: " + id + " : " + engine.getIdMap().get(id).getClass().getName());
        }
        if (factory == null) {
            throw new Exception("Unknown TAG, implementation class not defined: " + element.getName());
        }
        if (element.getAttribute(Parser.ATTR_INCLUDE) != null) {
            StringTokenizer st = new StringTokenizer(element.getAttribute(Parser.ATTR_INCLUDE).getValue(), "#");
            element.removeAttribute(Parser.ATTR_INCLUDE);
            Document doc = new org.jdom.input.SAXBuilder().build(this.engine.getClassLoader().getResourceAsStream(st.nextToken()));
            Element xelem = find(doc.getRootElement(), st.nextToken());
            if (xelem != null) {
                moveContent(xelem, element);
            }
        }
        if (element.getAttribute(Parser.ATTR_REFID) != null) {
            element = (Element) element.clone();
            cloneAttributes(element);
            element.removeAttribute(Parser.ATTR_REFID);
        }
        List attributes = element.getAttributes();
        if (obj == null) {
            Object initParameter = null;
            if (element.getAttribute(Parser.ATTR_INITCLASS) != null) {
                StringTokenizer st = new StringTokenizer(element.getAttributeValue(Parser.ATTR_INITCLASS), "( )");
                element.removeAttribute(Parser.ATTR_INITCLASS);
                try {
                    if (st.hasMoreTokens()) {
                        Class initClass = Class.forName(st.nextToken());
                        try {
                            Method factoryMethod = initClass.getMethod(Parser.GETINSTANCE, null);
                            if (Modifier.isStatic(factoryMethod.getModifiers())) {
                                initParameter = factoryMethod.invoke(null, null);
                            }
                        } catch (NoSuchMethodException nsme) {
                        }
                        if (initParameter == null && st.hasMoreTokens()) {
                            try {
                                Constructor ctor = initClass.getConstructor(new Class[] { String.class });
                                String pattern = st.nextToken();
                                initParameter = ctor.newInstance(new Object[] { pattern });
                            } catch (NoSuchMethodException e) {
                            } catch (SecurityException e) {
                            } catch (InstantiationException e) {
                            } catch (IllegalAccessException e) {
                            } catch (IllegalArgumentException e) {
                            } catch (InvocationTargetException e) {
                            }
                        }
                        if (initParameter == null) {
                            initParameter = initClass.newInstance();
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (SecurityException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (IllegalAccessException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (IllegalArgumentException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (InvocationTargetException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (InstantiationException e) {
                    System.err.println(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage() + e);
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new Exception(Parser.ATTR_INITCLASS + " not instantiated : " + e.getLocalizedMessage(), e);
                }
            }
            if (element.getAttribute(Parser.ATTR_CLASS) != null) {
                Class customComponent = Class.forName(element.getAttributeValue(Parser.ATTR_CLASS), true, engine.getClassLoader());
                factory = new DefaultFactory(customComponent);
            }
            if (element.getName().toLowerCase().equals("timechart")) {
                factory = new ChartFactory(element);
            } else if (element.getName().toLowerCase().equals("graph")) {
                factory = new TimeSeriesFactory(element);
            }
            obj = initParameter != null ? factory.newInstance(new Object[] { initParameter }) : factory.newInstance();
            constructed = true;
            if (id != null) {
                engine.getIdMap().put(id, obj);
            }
        }
        Attribute actionAttr = element.getAttribute("Action");
        if (actionAttr != null) {
            element.removeAttribute(actionAttr);
            attributes.add(0, actionAttr);
        }
        if (element.getAttribute("Text") == null && 0 < element.getTextTrim().length()) {
            attributes.add(new Attribute("Text", element.getTextTrim()));
        }
        List remainingAttrs = applyAttributes(obj, factory, attributes);
        LayoutManager layoutMgr = obj instanceof Container ? ((Container) obj).getLayout() : null;
        Iterator it = element.getChildren().iterator();
        while (it != null && it.hasNext()) {
            Element child = (Element) it.next();
            if ("buttongroup".equalsIgnoreCase(child.getName())) {
                int k = JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItemCount() : ((Container) obj).getComponentCount();
                getSwing(child, obj);
                int n = JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItemCount() : ((Container) obj).getComponentCount();
                ButtonGroup btnGroup = new ButtonGroup();
                if (null != child.getAttribute(Parser.ATTR_ID)) {
                    engine.getIdMap().put(child.getAttribute(Parser.ATTR_ID).getValue(), btnGroup);
                }
                while (k < n) {
                    putIntoBtnGrp(JMenu.class.isAssignableFrom(obj.getClass()) ? ((JMenu) obj).getItem(k++) : ((Container) obj).getComponent(k++), btnGroup);
                }
                continue;
            }
            Attribute constrnAttr = child.getAttribute(Parser.ATTR_CONSTRAINTS);
            Object constrains = null;
            if (constrnAttr != null && layoutMgr != null) {
                child.removeAttribute(Parser.ATTR_CONSTRAINTS);
                constrains = ConstraintsConverter.convert(layoutMgr.getClass(), constrnAttr);
            }
            Element grandchild = child.getChild("gridbagconstraints");
            if (grandchild != null) {
                addChild(obj, getSwing(child, null), getSwing(grandchild, null));
            } else if (!child.getName().equals("gridbagconstraints")) {
                addChild(obj, getSwing(child, null), constrains);
            }
        }
        if (remainingAttrs != null && 0 < remainingAttrs.size()) {
            remainingAttrs = applyAttributes(obj, factory, remainingAttrs);
            if (remainingAttrs != null) {
                it = remainingAttrs.iterator();
                while (it != null && it.hasNext()) {
                    Attribute attr = (Attribute) it.next();
                    if (JComponent.class.isAssignableFrom(obj.getClass())) {
                        ((JComponent) obj).putClientProperty(attr.getName(), attr.getValue());
                        if (SwingEngine.DEBUG_MODE) {
                            System.out.println("ClientProperty put: " + obj.getClass().getName() + "(" + id + "): " + attr.getName() + "=" + attr.getValue());
                        }
                    } else {
                        if (SwingEngine.DEBUG_MODE) {
                            System.err.println(attr.getName() + " not applied for tag: <" + element.getName() + ">");
                        }
                    }
                }
            }
        }
        return (constructed ? obj : null);
    }

    /**
	 * Creates an object and sets properties based on the XML tag's attributes
	 * 
	 * @param obj
	 *            <code>Object</code> object representing a tag found in the SWIXML descriptor
	 *            document
	 * @param factory
	 *            <code>Factory</code> factory to instantiate a new object
	 * @param attributes
	 *            <code>List</code> attribute list
	 * @return <code>List</code> - list of attributes that could not be applied.
	 * @throws Exception
	 * 
	 * <pre>
	 *                   <ol>
	 *                   <li>
	 *  For every attribute, createContainer() 1st tries to find a setter in the given factory.
	 * <br>
	 *                     if a setter can be found and converter exists to convert the parameter string into a type that fits
	 *                     the setter method, the setter gets invoked.
	 * </li>
	 *                   <li>
	 *  Otherwise, createContainer() looks for a public field with a matching name.
	 * </li>
	 *                   </ol>
	 *                   </pre><pre>
	 *                     &lt;b&gt;Example:&lt;/b&gt;
	 * <br>
	 *                   <br>
	 *  1.) try to create a parameter obj using the ParameterFactory: i.e.
	 * <br>
	 * background = &quot;FFC9AA&quot; = container.setBackground(new Color(attr.value))
	 * <br>
	 *  2.) try to find a simple setter taking a primitive or String:  i.e.
	 * <br>
	 *  width=&quot;25&quot; container.setWidth( new Interger( attr. getIntValue() ) )
	 * <br>
	 *  3.) try to find a public field,
	 * <br>
	 * container.BOTTOM_ALIGNMENT
	 * </pre>
	 */
    private List applyAttributes(Object obj, Factory factory, List attributes) throws Exception {
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attr = (Attribute) attributes.get(i);
            if (Parser.ATTR_ACTION.equalsIgnoreCase(attr.getName())) {
                attributes.remove(i);
                attributes.add(0, attr);
                break;
            }
        }
        Iterator it = attributes.iterator();
        List list = new ArrayList();
        while (it != null && it.hasNext()) {
            Attribute attr = (Attribute) it.next();
            if (Parser.ATTR_ID.equals(attr.getName())) continue;
            if (Parser.ATTR_REFID.equals(attr.getName())) continue;
            if (JLabel.class.isAssignableFrom(obj.getClass()) && attr.getName().equalsIgnoreCase("LabelFor")) {
                lbl_map.put(obj, attr.getValue());
                continue;
            }
            Method method = factory.guessSetter(attr.getName());
            if (method != null) {
                Class paraType = method.getParameterTypes()[0];
                Converter converter = cvtlib.getConverter(paraType);
                if (converter != null) {
                    Object para = null;
                    try {
                        if (Action.class.equals(paraType)) {
                            para = engine.getClient().getClass().getField(attr.getValue()).get(engine.getClient());
                        } else {
                            para = converter.convert(paraType, attr, engine.getLocalizer());
                        }
                        method.invoke(obj, new Object[] { para });
                    } catch (NoSuchFieldException e) {
                        if (SwingEngine.DEBUG_MODE) {
                            System.err.println("Action '" + attr.getValue() + "' not set. Public Action '" + attr.getValue() + "' not found in " + engine.getClient().getClass().getName());
                        }
                    } catch (InvocationTargetException e) {
                        if (obj instanceof RootPaneContainer) {
                            Container rootpane = ((RootPaneContainer) obj).getContentPane();
                            Factory f = engine.getTaglib().getFactory(rootpane.getClass());
                            Method m = f.guessSetter(attr.getName());
                            try {
                                m.invoke(rootpane, new Object[] { para });
                            } catch (Exception ex) {
                                list.add(attr);
                            }
                        } else {
                            list.add(attr);
                        }
                    } catch (Exception e) {
                        throw new Exception(e + ":" + method.getName() + ":" + para, e);
                    }
                    continue;
                }
                if (paraType.equals(Object.class)) {
                    try {
                        String s = attr.getValue();
                        if (Parser.LOCALIZED_ATTRIBUTES.contains(attr.getName().toLowerCase()) && attr.getAttributeType() == Attribute.CDATA_TYPE) {
                            s = engine.getLocalizer().getString(s);
                        }
                        method.invoke(obj, new Object[] { s });
                    } catch (Exception e) {
                        list.add(attr);
                    }
                    continue;
                }
                if (paraType.isPrimitive()) {
                    try {
                        method.invoke(obj, new Object[] { PrimitiveConverter.conv(paraType, attr, engine.getLocalizer()) });
                    } catch (Exception e) {
                        list.add(attr);
                    }
                    continue;
                }
                list.add(attr);
                continue;
            } else {
                try {
                    Field field = obj.getClass().getField(attr.getName());
                    if (field != null) {
                        Converter converter = cvtlib.getConverter(field.getType());
                        if (converter != null) {
                            Object fieldValue = converter.convert(field.getType(), attr, null);
                            if (String.class.equals(converter.convertsTo())) {
                                fieldValue = engine.getLocalizer().getString((String) fieldValue);
                            }
                            field.set(obj, fieldValue);
                        } else {
                            list.add(attr);
                        }
                    } else {
                        list.add(attr);
                    }
                } catch (Exception e) {
                    list.add(attr);
                }
            }
        }
        return list;
    }

    /**
	 * Copies attributes that element doesn't have yet form element[id]
	 * 
	 * @param target
	 *            <code>Element</code> target to receive more attributes
	 */
    private void cloneAttributes(Element target) {
        Element source = null;
        if (target.getAttribute(Parser.ATTR_REFID) != null) {
            source = find(jdoc.getRootElement(), target.getAttribute(Parser.ATTR_REFID).getValue().trim());
        }
        if (source != null) {
            Iterator it = source.getAttributes().iterator();
            while (it != null && it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                String name = attr.getName().trim();
                if (!Parser.ATTR_ID.equals(name) && target.getAttribute(name) == null) {
                    Attribute attrcln = (Attribute) attr.clone();
                    attrcln.detach();
                    target.setAttribute(attrcln);
                }
            }
        }
    }

    /**
	 * Adds a child component to a parent component considering many differences between the Swing
	 * containers
	 * 
	 * @param parent
	 *            <code>Component</code>
	 * @param component
	 *            <code>Component</code> child to be added to the parent
	 * @param constrains
	 *            <code>Object</code> contraints
	 * @return <code>Component</code> - the passed in component
	 */
    private static Object addChild(Object parent2, Object component2, Object constrains) {
        if (component2 == null) return null;
        Container parent = null;
        if (parent2 instanceof Container) {
            parent = (Container) parent2;
        }
        Component component = null;
        if (component2 instanceof Component) {
            component = (Container) component2;
        }
        if (component != null && component instanceof JMenuBar) {
            try {
                Method m = parent.getClass().getMethod("setJMenuBar", new Class[] { JMenuBar.class });
                m.invoke(parent, new Object[] { component });
            } catch (NoSuchMethodException e) {
                parent.add(component);
            } catch (Exception e) {
            }
        } else if (parent != null && parent instanceof RootPaneContainer) {
            RootPaneContainer rpc = (RootPaneContainer) parent;
            if (component instanceof LayoutManager) {
                rpc.getContentPane().setLayout((LayoutManager) component);
            } else {
                rpc.getContentPane().add(component, constrains);
            }
        } else if (parent != null && parent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) parent;
            scrollPane.setViewportView(component);
        } else if (parent != null && parent instanceof JSplitPane) {
            JSplitPane splitPane = (JSplitPane) parent;
            if (splitPane.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                if (splitPane.getTopComponent() == null) {
                    splitPane.setTopComponent(component);
                } else {
                    splitPane.setBottomComponent(component);
                }
            } else {
                if (splitPane.getLeftComponent() == null) {
                    splitPane.setLeftComponent(component);
                } else {
                    splitPane.setRightComponent(component);
                }
            }
        } else if (parent != null && parent instanceof JMenuBar && component instanceof JMenu) {
            JMenuBar menuBar = (JMenuBar) parent;
            menuBar.add(component, constrains);
        } else if (component != null && JSeparator.class.isAssignableFrom(component.getClass())) {
            Container con = parent;
            if (JToolBar.class.isAssignableFrom(parent.getClass())) ((JToolBar) parent).addSeparator(); else if (JPopupMenu.class.isAssignableFrom(parent.getClass())) ((JPopupMenu) parent).addSeparator(); else if (JMenu.class.isAssignableFrom(parent.getClass())) ((JMenu) parent).addSeparator(); else if (constrains != null) con.add(component, constrains); else con.add(component);
        } else {
            if (parent2 instanceof XTimeChart) {
                XTimeChart chart = (XTimeChart) parent2;
                chart.add((TimeSeries) component2);
            } else if (parent instanceof ChartPanel) {
                ChartPanel chartPanel = (ChartPanel) parent;
                chartPanel.add((JFreeChart) component2);
            } else {
                if (constrains == null) {
                    parent.add(component);
                } else {
                    parent.add(component, constrains);
                }
            }
        }
        return component;
    }

    /**
	 * Moves the content from the source into the traget <code>Element</code>
	 * 
	 * @param source
	 *            <code>Element</code> Content provider
	 * @param target
	 *            <code>Element</code> Content receiver
	 */
    private static void moveContent(Element source, Element target) {
        List list = source.getContent();
        while (!list.isEmpty()) {
            Object obj = list.remove(0);
            target.getContent().add(obj);
        }
    }

    /**
	 * Recursive element by id finder
	 * 
	 * @param element
	 *            <code>Element</code> start node
	 * @param id
	 *            <code>String</code> id to look for
	 * @return <code>Element</code> - with the given id in the id attribute or null if not found
	 */
    private static Element find(Element element, String id) {
        Element elem = null;
        Attribute attr = element.getAttribute(Parser.ATTR_ID);
        if (attr != null && id.equals(attr.getValue().trim())) {
            elem = element;
        } else {
            Iterator it = element.getChildren().iterator();
            while (it != null && it.hasNext() && elem == null) {
                elem = find((Element) it.next(), id.trim());
            }
        }
        return elem;
    }

    /**
	 * Recursively adds AbstractButtons into the given
	 * 
	 * @param obj
	 *            <code>Object</code> should be an AbstractButton or JComponent containing
	 *            AbstractButtons
	 * @param grp
	 *            <code>ButtonGroup</code>
	 */
    private static void putIntoBtnGrp(Object obj, ButtonGroup grp) throws Exception {
        if (AbstractButton.class.isAssignableFrom(obj.getClass())) {
            grp.add((AbstractButton) obj);
        } else if (JComponent.class.isAssignableFrom(obj.getClass())) {
            JComponent jp = (JComponent) obj;
            for (int i = 0; i < jp.getComponentCount(); i++) {
                putIntoBtnGrp(jp.getComponent(i), grp);
            }
        }
    }
}
