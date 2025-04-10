package org.frameworkset.spi.remote;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * 
 * <p>Title: PropertyConfigurator.java</p> 
 * <p>Description: </p>
 * <p>bboss workgroup</p>
 * <p>Copyright (c) 2007</p>
 * @Date Apr 24, 2009 10:49:02 PM
 * @author biaoping.yin
 * @version 1.0
 */
public class PropertyConfigurator {

    private static Logger logger_ = Logger.getLogger(PropertyConfigurator.class);

    private Object objToConfigure_;

    private static final String ROOT = "mbean";

    private static final String ATTR = "attribute";

    private static final String SUB_ATTR = "config";

    private static final String NAME = "name";

    public PropertyConfigurator() {
    }

    protected Element loadDocument(String location) throws ConfigureException {
        URL url = null;
        try {
            url = new URL("file", "", 80, location);
            logger_.debug("URL location is " + url.toString());
            return loadDocument(url.openStream());
        } catch (java.net.MalformedURLException mfx) {
            logger_.error("Configurator error: " + mfx);
            mfx.printStackTrace();
        } catch (java.io.IOException e) {
            logger_.error("Configurator error: " + e);
            e.printStackTrace();
        }
        return null;
    }

    protected Element loadDocument(InputStream is) throws ConfigureException {
        Document doc = null;
        try {
            InputSource xmlInp = new InputSource(is);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = docBuilderFactory.newDocumentBuilder();
            doc = parser.parse(xmlInp);
            Element root = doc.getDocumentElement();
            root.normalize();
            return root;
        } catch (SAXParseException err) {
            logger_.error("Configurator SAXParse error: " + err.getMessage());
            err.printStackTrace();
        } catch (SAXException e) {
            logger_.error("Configurator SAX error: " + e);
            e.printStackTrace();
        } catch (Exception pce) {
            logger_.error("Configurator general error: " + pce);
            pce.printStackTrace();
        }
        return null;
    }

    protected Element getMBeanElement(Element root) throws ConfigureException {
        NodeList list = root.getElementsByTagName(ROOT);
        if (list == null) throw new ConfigureException("Can't find " + ROOT + " tag");
        if (list.getLength() > 1) throw new ConfigureException("Has multiple " + ROOT + " tag");
        Node node = list.item(0);
        Element element = null;
        if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            element = (Element) node;
        } else {
            throw new ConfigureException("Can't find " + ROOT + " element");
        }
        return element;
    }

    /**
	    * Configure the object based on the config xml file.
	    *
	    * @param objToConfigure Object that needs configuration.
	    * @param configFile     xml file name that exists in the class path.
	    * @throws ConfigureException when the configuration attempt fails.
	    */
    public void configure(Object objToConfigure, String configFile) throws ConfigureException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream(configFile);
        if (is == null) {
            try {
                is = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (is == null) throw new ConfigureException("could not find resource " + configFile);
        configure(objToConfigure, is);
    }

    /**
	    * Configure the object based on the config xml file.
	    *
	    * @param objToConfigure Object that needs configuration.
	    * @param is             InputStream for the configuration xml file.
	    * @throws ConfigureException when the configuration attempt fails.
	    */
    public void configure(Object objToConfigure, InputStream is) throws ConfigureException {
        objToConfigure_ = objToConfigure;
        if (is == null) throw new ConfigureException("input stream is null for property xml");
        Element root = loadDocument(is);
        Element mbeanElement = getMBeanElement(root);
        NodeList list = mbeanElement.getElementsByTagName(ATTR);
        logger_.info("attribute size: " + list.getLength());
        Class objClass = objToConfigure_.getClass();
        Method[] methods = objClass.getMethods();
        Class[] string_sig = new Class[] { String.class };
        for (int loop = 0; loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) continue;
            Element element = (Element) node;
            String name = element.getAttribute(NAME);
            String valueStr = getElementContent(element, true);
            Element valueObj = null;
            if (valueStr.length() == 0) {
                valueObj = getSubElementObject(element);
            }
            String methodName = "set" + name;
            Object value = null;
            Method method = null;
            try {
                method = objClass.getMethod(methodName, string_sig);
            } catch (Exception e) {
                ;
            }
            if (method != null) {
                try {
                    logger_.debug("setting attribute " + name + " to " + valueStr);
                    method.invoke(objToConfigure_, new Object[] { valueStr });
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new ConfigureException("configure(): can't invoke " + methodName + " to configure " + "TreeCache properties. Exception: " + ex);
                }
            }
            for (int i = 0; i < methods.length; i++) {
                if (methodName.equals(methods[i].getName())) {
                    method = methods[i];
                    Class[] clz = method.getParameterTypes();
                    if (clz.length != 1) throw new ConfigureException("Parameter size of " + methodName + " is not 1 but " + clz.length);
                    Class classParam = clz[0];
                    PropertyEditor editor = PropertyEditorManager.findEditor(classParam);
                    if (editor == null) {
                        String str = "Could not find PropertyEditor for type class " + classParam;
                        if (classParam.isAssignableFrom(Element.class)) {
                            value = valueObj;
                        } else {
                            continue;
                        }
                    } else {
                        if (valueObj != null) {
                            editor.setValue(valueObj);
                        } else {
                            editor.setAsText(valueStr);
                        }
                        value = editor.getValue();
                    }
                    logger_.debug("Invoking setter method: " + method + " with parameter \"" + value + "\" of type " + value.getClass());
                    try {
                        method.invoke(objToConfigure_, new Object[] { value });
                        break;
                    } catch (Exception ex) {
                        throw new ConfigureException("can't invoke " + methodName + " to configure TreeCache", ex);
                    }
                }
            }
        }
    }

    private Element getSubElementObject(Element element) {
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && SUB_ATTR.equals(((Element) node).getTagName())) {
                return (Element) node;
            }
        }
        logger_.debug("getSubElementObject(): element object. Does not exist for " + SUB_ATTR);
        return null;
    }

    private String getElementContent(Element element, boolean trim) {
        NodeList nl = element.getChildNodes();
        String attributeText = "";
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n instanceof Text) {
                attributeText += ((Text) n).getData();
            }
        }
        if (trim) attributeText = attributeText.trim();
        return attributeText;
    }

    public static void main(String[] args) {
        try {
            PropertyConfigurator config = new PropertyConfigurator();
            JGroupConfig jconfig = new JGroupConfig();
            config.configure(jconfig, "etc/META-INF/replSync-service-aop.xml");
            System.out.println(jconfig.getClusterProperties());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
