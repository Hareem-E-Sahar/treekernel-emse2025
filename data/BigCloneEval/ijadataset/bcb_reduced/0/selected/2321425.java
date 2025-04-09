package org.openmolgrid.cli.resources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.openmolgrid.client.common.OMGTask;
import org.openmolgrid.client.common.OMGTaskProvider;
import org.openmolgrid.client.plugins.interfaces.IChainable;
import org.openmolgrid.client.plugins.interfaces.IResourceInfoProvider;
import org.openmolgrid.client.plugins.serviceRegistry.AbstractServiceReader;
import org.unicore.Vsite;
import org.unicore.resources.Application;

/**
 * This class collects instances of available client plugins and saves them into the 
 * <br>CLIServiceRegistry object.  
 * 
 * @author Lidia Kirtchakova
 *
 * @version $Id: CLIServiceReader.java,v 1.1.1.1 2005/03/02 13:09:15 bschuller Exp $
 * @see {@link CLIServiceRegistry}
 */
public class CLIServiceReader extends AbstractServiceReader {

    protected static Logger logger = Logger.getLogger("org.openmolgrid.cli");

    CLIServiceRegistry registry;

    private Application app;

    private Vsite vsite;

    private Properties props;

    private File pluginDir;

    public CLIServiceReader() {
        this(null);
    }

    public CLIServiceReader(Properties p) {
        super();
        props = p;
        app = new Application();
        vsite = new Vsite();
    }

    protected void add(OMGTaskProvider provider, OMGTask task) {
        registry.add(provider, task);
    }

    public void clear() {
    }

    protected void parsePluginInfo() {
        Vector plugins = getPluginsFromDir(pluginDir);
        for (int i = 0; i < plugins.size(); ++i) {
            try {
                Class contClass = (Class) plugins.elementAt(i);
                Constructor[] constructors = contClass.getConstructors();
                Object cont = new Object();
                for (int j = 0; j < constructors.length; ++j) {
                    if (constructors[j].getParameterTypes().length == 1) {
                        cont = constructors[j].newInstance(new Object[] { null });
                        break;
                    }
                }
                String name = cont.getClass().getName();
                logger.finer("Checking plugin container class " + name);
                if (cont instanceof IChainable) {
                    String[] taskNames = ((IChainable) cont).getSupportedTasks();
                    logger.fine("Container class " + contClass.getName() + " with IChainable interface found!");
                    registry.add(contClass, taskNames);
                }
                if (name.equals("com.pallas.unicore.client.plugins.script.ScriptContainer")) {
                    registry.add(contClass, new String[] { OMGTask.SCRIPT_TASK });
                }
            } catch (java.lang.NoClassDefFoundError t) {
                logger.severe("Error occured while retrieving information about supported tasks!");
                logger.severe(t.getMessage());
            } catch (Exception ex) {
                logger.severe("Error occured while retrieving information about supported tasks!");
                logger.severe(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void setRegistry(IResourceInfoProvider reg) {
        registry = (CLIServiceRegistry) reg;
    }

    /**
	 * 
	 * @param dirPath - absolute path to the plugin directory
	 * @return true, if specified directory exists, otherwise false 
	 */
    public boolean setPluginDirectory(String dirPath) {
        pluginDir = new File(dirPath);
        if (!pluginDir.exists() || pluginDir.isFile()) {
            logger.severe("No plugin directory found!");
            return false;
        }
        return true;
    }

    private Vector getPluginsFromDir(File dir) {
        logger.info("searching for plugins in the plugin directory " + dir.getAbsolutePath());
        String[] pluginNames = dir.list();
        if (pluginNames == null) {
            logger.severe("No plugins in the specified directory found!");
            return null;
        }
        Vector plugins = new Vector();
        for (int i = 0; i < pluginNames.length; i++) {
            String plugin = pluginNames[i];
            if (!plugin.toLowerCase().endsWith("plugin.jar")) {
                continue;
            }
            JarFile jar;
            try {
                jar = new JarFile(dir + File.separator + plugin);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not open plugin: " + plugin, e);
                continue;
            }
            logger.info("Found plugin jar: " + plugin);
            URL urls[] = new URL[1];
            URLClassLoader urlLoader = null;
            try {
                urls[0] = new URL("file:" + dir + File.separator + plugin);
                Class self = CLIServiceReader.class;
                urlLoader = new URLClassLoader(urls, self.getClassLoader());
            } catch (MalformedURLException mfue) {
                logger.severe("Cannot convert file name to URL: " + plugin);
                continue;
            }
            Class containerClass = getOMGContainerFromJar(jar, urlLoader);
            if (containerClass != null) plugins.add(containerClass);
        }
        return plugins;
    }

    private Class getOMGContainerFromJar(JarFile jar, URLClassLoader urlLoader) {
        boolean found = false;
        Enumeration entries = jar.entries();
        while (entries.hasMoreElements() && !found) {
            ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            String name = zipEntry.getName();
            if (!name.toLowerCase().endsWith(".class")) {
                continue;
            }
            try {
                int end = name.indexOf(".class");
                String className = name.substring(0, end).replace('/', '.');
                Class c = urlLoader.loadClass(className);
                if (className.equals("com.pallas.unicore.client.plugins.script.ScriptContainer")) {
                    return c;
                }
                Class test = c;
                while (!test.getName().equals("java.lang.Object")) {
                    Class[] interfaces;
                    try {
                        interfaces = test.getInterfaces();
                        for (int j = 0; j < interfaces.length; j++) {
                            if (interfaces[j].getName().endsWith("IChainable")) {
                                return c;
                            }
                        }
                        test = test.getSuperclass();
                    } catch (java.lang.NoClassDefFoundError err) {
                        logger.warning("error while reading plugins: " + err.getMessage() + ", scanning " + test.getName());
                    }
                }
            } catch (java.lang.NoClassDefFoundError t) {
                logger.warning("error while reading plugins: " + t.getMessage() + ", scanning " + name);
            } catch (Exception ex) {
                logger.severe("Error while reading plugins: ");
                ex.printStackTrace();
            }
        }
        return null;
    }
}
