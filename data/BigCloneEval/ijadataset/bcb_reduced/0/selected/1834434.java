package org.nakedobjects.system;

import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.NakedObjectsComponent;
import org.nakedobjects.system.install.StartupException;
import org.nakedobjects.utility.AboutNakedObjects;
import org.nakedobjects.utility.configuration.ComponentLoader;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.PropertiesFileLoader;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class StartUp {

    private static final Logger LOG = Logger.getLogger(StartUp.class);

    private final Hashtable components = new Hashtable();

    public static void main(final String[] args) {
        String configurationFile = args.length > 0 ? args[0] : "nakedobjects.properties";
        StartUp startup = new StartUp();
        startup.start(configurationFile);
    }

    protected void start(final String name) {
        LogManager.getRootLogger().setLevel(Level.OFF);
        PropertiesConfiguration configuration = new PropertiesConfiguration(new PropertiesFileLoader(name, true));
        PropertyConfigurator.configure(configuration.getProperties("log4j"));
        AboutNakedObjects.logVersion();
        LOG.debug("Configuring system using " + name);
        String componentNames = configuration.getString("nakedobjects.components");
        StringTokenizer st = new StringTokenizer(componentNames, ",;/:");
        Properties properties = configuration.getProperties("nakedobjects");
        Vector components = new Vector();
        while (st.hasMoreTokens()) {
            String componentName = ((String) st.nextToken()).trim();
            String componentClass = properties.getProperty("nakedobjects." + componentName);
            LOG.debug("loading core component " + componentName + ": " + componentClass);
            if (componentClass == null) {
                throw new StartupException("No component specified for nakedobjects." + componentName);
            }
            NakedObjectsComponent component = (NakedObjectsComponent) ComponentLoader.loadComponent(componentClass, NakedObjectsComponent.class);
            if (component instanceof NakedObjects) {
                ((NakedObjects) component).setConfiguration(configuration);
            }
            setProperties(component, "nakedobjects." + componentName, properties);
            component.init();
        }
        for (Enumeration e = components.elements(); e.hasMoreElements(); ) {
            ((NakedObjectsComponent) e.nextElement()).init();
        }
    }

    private void setProperties(final Object object, final String prefix, final Properties properties) {
        LOG.debug("looking for properties starting with " + prefix);
        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(prefix) && key.length() > prefix.length() && key.substring(prefix.length() + 1).indexOf('.') == -1) {
                LOG.debug("  property " + key + " (of prefix " + prefix + ")");
                String className = properties.getProperty(key).trim();
                Object value;
                if (className.indexOf(',') > 0) {
                    StringTokenizer st = new StringTokenizer(className, ",");
                    Object[] elements = new Object[st.countTokens()];
                    int i = 0;
                    while (st.hasMoreTokens()) {
                        String cls = st.nextToken();
                        elements[i++] = load(cls, key, properties);
                    }
                    value = elements;
                } else {
                    if (className.equalsIgnoreCase("true")) {
                        value = Boolean.TRUE;
                    } else if (className.equalsIgnoreCase("false")) {
                        value = Boolean.FALSE;
                    } else {
                        value = load(className, key, properties);
                    }
                }
                setProperty(object, key, value);
            }
        }
    }

    private void setProperty(final Object object, final String fieldName, final Object value) {
        String field = fieldName.substring(fieldName.lastIndexOf(".") + 1);
        field = Character.toUpperCase(field.charAt(0)) + field.substring(1);
        LOG.debug("    setting " + field + " on " + object);
        Class c = object.getClass();
        LOG.debug("    getting set method for " + field);
        Method setter = null;
        try {
            PropertyDescriptor property = new PropertyDescriptor(field, c, null, "set" + field);
            setter = property.getWriteMethod();
            Class cls = setter.getParameterTypes()[0];
            if (cls.isArray()) {
                int length = Array.getLength(value);
                Object[] array = (Object[]) Array.newInstance(cls.getComponentType(), length);
                System.arraycopy(value, 0, array, 0, length);
                setter.invoke(object, new Object[] { array });
            } else {
                setter.invoke(object, new Object[] { value });
            }
            LOG.debug("  set " + field + " with " + value.getClass());
        } catch (SecurityException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e) {
            throw new StartupException(e.getMessage() + ": can't invoke " + setter.getName() + " with instance of " + value.getClass().getName());
        } catch (IllegalAccessException e) {
            throw new StartupException(e.getMessage() + ": can't access " + setter.getName());
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    private Object load(final String className, final String name, final Properties properties) {
        if (className.startsWith("ref:")) {
            String referencedName = className.substring(4);
            if (components.containsKey(referencedName)) {
                return components.get(referencedName);
            } else {
                throw new StartupException("Could not reference the object names " + referencedName);
            }
        }
        LOG.debug("loading component " + className + " for " + name);
        Object object = ComponentLoader.loadComponent(className);
        components.put(name, object);
        setProperties(object, name, properties);
        return object;
    }
}
