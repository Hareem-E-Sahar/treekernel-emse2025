package de.plugmail;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.apache.log4j.Logger;
import de.plugmail.plugins.BasePlugin;

public class PluginLoader {

    private Properties configuration;

    private Logger log;

    private Hashtable loadedPlugins;

    public PluginLoader(Properties configuration) {
        log = Logger.getLogger(this.getClass());
        loadedPlugins = new Hashtable();
        this.configuration = configuration;
    }

    public BasePlugin loadPlugin(String name) throws Exception {
        name = "plugMail." + name;
        BasePlugin plugin = null;
        String startUp = configuration.getProperty(name + ".startUp");
        if (startUp != null) {
            log.debug("found a loader!");
            String[] plugins = startUp.split(",");
            log.debug("we have to load the following plugin(s): " + Arrays.asList(plugins));
            for (int i = 0; i < plugins.length; i++) {
                Vector loadedPlugins = new Vector();
                Vector pluginClasses = new Vector();
                String pluginName = "plugMail." + plugins[i];
                if (plugins[i].indexOf("(") > -1) {
                    pluginName = "plugMail." + plugins[i].substring(0, plugins[i].indexOf("("));
                    String[] parameterPlugins = plugins[i].substring(plugins[i].indexOf("(") + 1, plugins[i].length() - 1).split("/");
                    for (int j = 0; j < parameterPlugins.length; j++) {
                        log.debug("loading plugin " + parameterPlugins[j]);
                        BasePlugin loadedPlugin = loadPlugin(parameterPlugins[j]);
                        loadedPlugins.add(loadedPlugin);
                        pluginClasses.add(loadedPlugin.getClass());
                    }
                }
                try {
                    log.debug("we have loaded the following parameter plugin classes: " + pluginClasses);
                    log.debug("we have loaded the following parameter plugins:        " + loadedPlugins);
                    Class[] classes = new Class[pluginClasses.size()];
                    for (int j = 0; j < pluginClasses.size(); j++) {
                        classes[j] = BasePlugin.class;
                    }
                    log.debug("created a classes array: " + classes);
                    plugin = loadPlugin(pluginName, classes, loadedPlugins.toArray());
                } catch (Exception e) {
                    log.error("Could not load plugin for property " + pluginName, e);
                    throw new Exception(e);
                }
            }
        } else {
            log.debug("found a simple plugin!");
            plugin = loadPlugin(name, new Class[0], new Object[0]);
        }
        return plugin;
    }

    public BasePlugin loadPlugin(String propertyname, Class[] classes, Object[] objects) throws Exception {
        BasePlugin plugin = null;
        log.debug("looking for classname for property " + propertyname);
        String classname = configuration.getProperty(propertyname);
        log.debug("loading plugin with name " + classname);
        try {
            plugin = (BasePlugin) Class.forName(classname).getConstructor(classes).newInstance(objects);
            log.debug("plugin loaded!");
            Enumeration keys = this.configuration.keys();
            String key;
            String value;
            String methodName;
            String pluginName;
            while (keys.hasMoreElements()) {
                key = (String) keys.nextElement();
                if (key.startsWith(propertyname + ".property")) {
                    value = configuration.getProperty(key);
                    key = key.substring(key.lastIndexOf(".") + 1);
                    log.debug(key + "/" + value);
                    methodName = "set" + key;
                    plugin.getClass().getMethod(methodName, new Class[] { String.class }).invoke(plugin, new Object[] { value });
                }
            }
            String callMethod = configuration.getProperty(propertyname + ".callMethod");
            if (callMethod != null) {
                String methods[] = callMethod.split(",");
                for (int i = 0; i < methods.length; i++) {
                    value = methods[i];
                    log.debug("callmethod " + value);
                    pluginName = "plugMail." + value.substring(0, value.lastIndexOf("."));
                    methodName = value.substring(value.lastIndexOf(".") + 1);
                    log.debug("callmethod " + pluginName + "." + methodName + "(BasePlugin)");
                    log.debug("loaded plugins: " + loadedPlugins);
                    BasePlugin targetPlugin = (BasePlugin) loadedPlugins.get(pluginName);
                    log.debug("what did we find? " + targetPlugin);
                    if (targetPlugin.acceptsPlugins()) {
                        Class clazz = plugin.getClass();
                        boolean found = false;
                        while (!found && (clazz != null)) {
                            try {
                                targetPlugin.getClass().getMethod(methodName, new Class[] { clazz }).invoke(targetPlugin, new Object[] { plugin });
                                found = true;
                            } catch (Exception e) {
                                log.debug("No method found for class " + clazz.getName());
                                clazz = clazz.getSuperclass();
                            }
                        }
                        if (!found) {
                            log.error("Tried to add a Plugin to a " + targetPlugin.getClass().getName() + " but method with name " + methodName + " not found!");
                        }
                    } else {
                        log.error("Tried to add a Plugin to a " + targetPlugin.getClass().getName());
                    }
                }
            }
            plugin.activate();
            log.debug("plugin activated!");
        } catch (Exception e) {
            log.error("Could not load plugin with name " + classname, e);
            throw new Exception(e);
        }
        loadedPlugins.put(propertyname, plugin);
        return plugin;
    }

    public final Hashtable getLoadedPlugins() {
        return this.loadedPlugins;
    }
}
