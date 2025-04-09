package org.openfrag.OpenCDS.core.plugin;

import javax.management.ListenerNotFoundException;
import org.openfrag.OpenCDS.core.exceptions.*;
import org.openfrag.OpenCDS.core.util.VectorHelper;
import org.openfrag.OpenCDS.core.logging.*;
import java.util.*;
import java.io.*;
import java.util.jar.*;
import java.net.URLClassLoader;
import java.net.URL;

/**
 * The plugin manager can install, start, stop, or remove plugins. It handles
 *  all plugins available.
 *
 * @author  Lars 'Levia' Wesselius
*/
public class PluginManager extends VectorHelper<Plugin> {

    private List<PluginListener> m_PluginListeners = new ArrayList<PluginListener>();

    private PluginInterface m_PluginInterface;

    /**
     * The PluginManager constructor.
     *
     * @param   pluginInterface The interface plugins will operate upon.
    */
    public PluginManager(PluginCommunication pluginInterface) {
        pluginInterface.setPluginManager(this);
        m_PluginInterface = pluginInterface;
    }

    /**
     * Get a plugin by name, or filename
     *
     * @param   name    The name or filename of the plugin.
     * \return  The plugin with the name.
    */
    public Plugin getPlugin(String name) {
        for (Enumeration entries = m_Vector.elements(); entries.hasMoreElements(); ) {
            Plugin plugin = (Plugin) entries.nextElement();
            if (plugin.getName().equals(name) || plugin.getFileName().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    /**
     * Adds all files from the plugins folder to the classpath.
     *
     * @param   classLoader The classloader.
     * \return  A new classloader.
    */
    public ClassLoader addAllToClassPath(ClassLoader classLoader) {
        File directory = new File("plugins");
        if (!directory.exists()) {
            directory.mkdir();
        }
        if (!directory.isDirectory()) {
            return classLoader;
        }
        File[] fileList = directory.listFiles();
        for (int j = 0; j < fileList.length; j++) {
            try {
                if (classLoader instanceof URLClassLoader) {
                    URL[] old = ((URLClassLoader) classLoader).getURLs();
                    URL[] new_urls = new URL[old.length + 1];
                    System.arraycopy(old, 0, new_urls, 1, old.length);
                    new_urls[0] = fileList[j].toURL();
                    return classLoader = new URLClassLoader(new_urls, classLoader);
                } else {
                    return classLoader = new URLClassLoader(new URL[] { fileList[j].toURL() }, classLoader);
                }
            } catch (Exception e) {
                return classLoader;
            }
        }
        return classLoader;
    }

    /**
     * Adds all files from the plugins folder to the classpath.
     *
     * @param   classLoader The classloader.
     * @param   file        The file to add.
     * \return  A new classloader.
    */
    public ClassLoader addToClassPath(ClassLoader classLoader, File file) {
        if (file.exists() && !file.isDirectory() && file.getName().endsWith(".jar")) {
            try {
                if (classLoader instanceof URLClassLoader) {
                    URL[] old = ((URLClassLoader) classLoader).getURLs();
                    URL[] new_urls = new URL[old.length + 1];
                    System.arraycopy(old, 0, new_urls, 1, old.length);
                    new_urls[0] = file.toURL();
                    return classLoader = new URLClassLoader(new_urls, classLoader);
                } else {
                    return classLoader = new URLClassLoader(new URL[] { file.toURL() }, classLoader);
                }
            } catch (Exception e) {
                return classLoader;
            }
        }
        return classLoader;
    }

    /**
     * Scans for plugins, and adds them if not already existing.
    */
    public void scanForPlugins() {
        File pluginDir = new File("plugins");
        if (pluginDir.exists() && pluginDir.isDirectory()) {
            File[] files = pluginDir.listFiles();
            for (int i = 0; i != files.length; ++i) {
                File entry = files[i];
                if (entry.getName().endsWith(".jar")) {
                    createPlugin(entry);
                }
            }
        }
    }

    /**
     * Creates a plugin. Calls the constructor of the plugin class, but does
     *  not initialize it yet.
     *
     * @param   fileName    The filename of the jar file.
     * \return  A pointer to the Plugin class created and installed.
    */
    public Plugin createPlugin(String fileName) throws PluginException {
        try {
            return createPlugin(new File(fileName));
        } catch (PluginException e) {
            throw e;
        }
    }

    /**
     * Creates a plugin. Calls the constructor of the plugin class, but does
     *  not initialize it yet.
     *
     * @param   fileName    The filename of the jar file.
     * \return  A pointer to the Plugin class created and installed.
    */
    public Plugin createPlugin(File fileName) throws PluginException {
        try {
            if (!fileName.exists() || fileName.isDirectory() || !fileName.getName().endsWith(".jar")) {
                throw new PluginException("The given file is not of supported format. Failed loading plugin: " + fileName.getName());
            }
            JarFile file = new JarFile(fileName);
            for (Enumeration entries = file.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) entries.nextElement();
                if (entry.getName().equals("plugin.properties")) {
                    Properties p = new Properties();
                    p.load(file.getInputStream(entry));
                    String pluginClass = (String) p.get("plugin.class");
                    if (pluginClass == null) {
                        throw new PluginException("There was no 'plugin.class' property. Failed loading plugin: " + fileName.getName());
                    }
                    ClassLoader classLoader = PluginManager.class.getClassLoader();
                    classLoader = addToClassPath(classLoader, fileName);
                    Class c = classLoader.loadClass(pluginClass);
                    Plugin plugin = (Plugin) c.newInstance();
                    if ((String) p.get("plugin.name") == null || (String) p.get("plugin.version") == null) {
                        throw new PluginException("A needed property was not found. Failed loading plugin: " + fileName.getName());
                    }
                    plugin.create((String) p.get("plugin.name"), fileName.getCanonicalPath(), (String) p.get("plugin.version"));
                    if (!m_Vector.contains(plugin)) {
                        m_Vector.add(plugin);
                    } else {
                        throw new PluginException("A plugin with the name '" + plugin.getName() + "' already exists.");
                    }
                    Logger.getInstance().log("Loaded plugin: " + plugin.getName());
                    firePluginCreated(plugin);
                    return plugin;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Run a plugin.
     *
     * @param   plugin  The plugin to run.
    */
    public void runPlugin(final Plugin plugin) {
        if (exists(plugin) && plugin.isCreated()) {
            Thread thread = new Thread(new Runnable() {

                public void run() {
                    try {
                        plugin.initialize(m_PluginInterface);
                    } catch (Exception e) {
                        Logger.getInstance().log(Logger.LOG_WARNING, "Failed loading plugin: '" + plugin.getName() + "'.");
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        Logger.getInstance().log(Logger.LOG_WARNING, sw.toString());
                        return;
                    } catch (AbstractMethodError err) {
                        Logger.getInstance().log(Logger.LOG_WARNING, "Failed loading plugin: '" + plugin.getName() + "', no initialize method.");
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        err.printStackTrace(pw);
                        Logger.getInstance().log(Logger.LOG_WARNING, sw.toString());
                    }
                }
            });
            plugin.setThread(thread);
            thread.start();
            firePluginRan(plugin);
            Logger.getInstance().log("Started plugin: " + plugin.getName());
        } else {
            throw new PluginException("The plugin was not ready to be started.");
        }
    }

    /**
     * Run a plugin.
     *
     * @param   name    The name of the plugin to run.
    */
    public void runPlugin(String name) {
        runPlugin(getPlugin(name));
    }

    /**
     * Run all plugins.
    */
    public void runAll() {
        for (Enumeration entries = m_Vector.elements(); entries.hasMoreElements(); ) {
            Plugin plugin = (Plugin) entries.nextElement();
            runPlugin(plugin);
        }
    }

    /**
     * Stop a plugin. Does the same as <i>removePlugin</i>
     *
     * @param   plugin  The plugin to stop.
    */
    public void stopPlugin(Plugin plugin) {
        removePlugin(plugin);
    }

    /**
     * Remove a plugin from the system. This method also calls the <i>destroy</i>
     *  method on the plugin.
     *
     * @param   plugin  The plugin to destroy.
    */
    public void removePlugin(Plugin plugin) throws PluginException {
        if (exists(plugin) && plugin.isCreated()) {
            try {
                plugin.destroy();
            } catch (AbstractMethodError err) {
                Logger.getInstance().log(Logger.LOG_WARNING, "Failed destroying plugin: '" + plugin.getName() + "', no destroy method.");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                err.printStackTrace(pw);
                Logger.getInstance().log(Logger.LOG_WARNING, sw.toString());
            }
            m_Vector.remove(plugin);
            firePluginRemoved(plugin);
            Logger.getInstance().log("Removed plugin: " + plugin.getName());
        } else {
            if (plugin.isCreated()) {
                throw new PluginException("The plugin with the name '" + plugin.getName() + "' was not initialized yet.");
            } else {
                throw new PluginException("The plugin was not created yet.");
            }
        }
    }

    /**
     * Add a plugin listener.
     *
     * @param   pluginListener  The PluginListener to add.
    */
    public void addListener(PluginListener pluginListener) {
        m_PluginListeners.add(pluginListener);
    }

    /**
     * Remove a plugin listener.
     *
     * @param   pluginListener  The PluginListener to remove.
    */
    public void removeListener(PluginListener pluginListener) {
        m_PluginListeners.remove(pluginListener);
    }

    /**
     * Fires an event.
     *
     * @param   plugin  The plugin to which this event effects.
    */
    public void firePluginCreated(Plugin plugin) {
        for (Iterator it = m_PluginListeners.iterator(); it.hasNext(); ) {
            PluginListener listener = (PluginListener) it.next();
            listener.pluginCreated(plugin);
        }
    }

    /**
     * Fires an event.
     *
     * @param   plugin  The plugin to which this event effects.
    */
    public void firePluginRan(Plugin plugin) {
        for (Iterator it = m_PluginListeners.iterator(); it.hasNext(); ) {
            PluginListener listener = (PluginListener) it.next();
            listener.pluginRan(plugin);
        }
    }

    /**
     * Fires an event.
     *
     * @param   plugin  The plugin to which this event effects.
    */
    public void firePluginRemoved(Plugin plugin) {
        for (Iterator it = m_PluginListeners.iterator(); it.hasNext(); ) {
            PluginListener listener = (PluginListener) it.next();
            listener.pluginRemoved(plugin);
        }
    }
}
