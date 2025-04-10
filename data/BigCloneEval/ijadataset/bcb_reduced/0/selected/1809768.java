package net.sf.xpontus.plugins.browser;

import net.sf.xpontus.constants.XPontusConfigurationConstantsIF;
import net.sf.xpontus.controllers.impl.XPontusPluginManager;
import net.sf.xpontus.plugins.SimplePluginDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.java.plugin.registry.PluginDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * @author Yves Zoundi <yveszoundi at users dot sf dot net>
 */
public class AvailablePluginsResolver extends AbstractPluginsResolver {

    private Map<String, SimplePluginDescriptor> pluginsMap = new ConcurrentHashMap<String, SimplePluginDescriptor>();

    private List<String> installed = new Vector<String>();

    private PropertyDescriptor[] propertyDescriptors;

    public AvailablePluginsResolver() {
        init();
    }

    public PropertyDescriptor findDescriptor(String property) {
        for (PropertyDescriptor p : propertyDescriptors) {
            if (p.getName().equals(property)) {
                return p;
            }
        }
        return null;
    }

    public Map<String, SimplePluginDescriptor> getPluginDescriptorsMap() {
        return pluginsMap;
    }

    private void init() {
        try {
            BeanInfo bi = Introspector.getBeanInfo(SimplePluginDescriptor.class);
            propertyDescriptors = bi.getPropertyDescriptors();
        } catch (IntrospectionException ex) {
            Logger.getLogger(AvailablePluginsResolver.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collection<PluginDescriptor> descriptors = XPontusPluginManager.getPluginManager().getRegistry().getPluginDescriptors();
        for (PluginDescriptor pluginDescriptor : descriptors) {
            installed.add(pluginDescriptor.getId());
        }
    }

    private void setProperty(Object bean, PropertyDescriptor p, String value) {
        Class<?> propType = p.getPropertyType();
        PropertyEditor editor = PropertyEditorManager.findEditor(propType);
        if (editor == null) {
            throw new IllegalArgumentException("Not found: " + propType);
        }
        Method setter = p.getWriteMethod();
        editor.setAsText(value);
        Object result = editor.getValue();
        try {
            setter.invoke(bean, new Object[] { result });
        } catch (IllegalAccessException e) {
            throw new SecurityException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void resolvePlugins(String url) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(url));
        Node root = doc.getFirstChild();
        NodeList plugins = root.getChildNodes();
        final int total = plugins.getLength();
        for (int i = 0; i < total; i++) {
            Node node = plugins.item(i);
            if (node instanceof Element) {
                String id = node.getAttributes().getNamedItem("id").getNodeValue();
                if (!installed.contains(id)) {
                    SimplePluginDescriptor spd = new SimplePluginDescriptor();
                    setProperty(spd, findDescriptor("id"), id);
                    NodeList pluginInfo = node.getChildNodes();
                    for (int j = 0; j < pluginInfo.getLength(); j++) {
                        Node n = pluginInfo.item(j);
                        if (n instanceof Element) {
                            setProperty(spd, findDescriptor(n.getNodeName()), n.getTextContent());
                        }
                    }
                    if (spd.getAuthor() == null) {
                        spd.setAuthor("Yves Zoundi");
                    }
                    if (spd.getLicense() == null) {
                        spd.setLicense("UNKNOWN");
                    }
                }
            }
        }
    }

    public void resolvePlugins() {
        try {
            File cacheDir = XPontusConfigurationConstantsIF.XPONTUS_CACHE_DIR;
            File pluginsFile = new File(cacheDir, "plugins.xml");
            if (!pluginsFile.exists()) {
                URL pluginURL = new URL("http://xpontus.sourceforge.net/snapshot/plugins.xml");
                InputStream is = pluginURL.openStream();
                OutputStream os = FileUtils.openOutputStream(pluginsFile);
                IOUtils.copy(is, os);
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }
            resolvePlugins(pluginsFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
    }
}
