package de.hsofttec.monitoring.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import de.hsofttec.monitoring.IMonitorContext;
import de.hsofttec.monitoring.host.IHost;
import de.hsofttec.monitoring.host.IHostContext;
import de.hsofttec.monitoring.plugin.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:shomburg@hsofttec.com">shomburg</a>
 * @version $Id: PluginUtils.java 2 2007-09-01 11:09:16Z shomburg $
 */
public class PluginUtils {

    private static Logger _logger = LoggerFactory.getLogger(PluginUtils.class);

    /**
     * instantiert das Plugin.
     *
     * @param classLoader         the classloader
     * @param hostContext         host context
     * @param pluginConfiguration plugin configuration
     *
     * @return
     */
    public static IPlugin instantiatePlugin(ClassLoader classLoader, IHostContext hostContext, String pluginConfiguration) {
        try {
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream(pluginConfiguration);
            properties.load(fis);
            Class pluginClass = classLoader.loadClass((String) properties.get("class"));
            fis.close();
            Constructor pluginConstructor = pluginClass.getConstructor(IHostContext.class, String.class);
            return (IPlugin) pluginConstructor.newInstance(hostContext, pluginConfiguration);
        } catch (ClassNotFoundException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (NoSuchMethodException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (IllegalAccessException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (InvocationTargetException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (InstantiationException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (FileNotFoundException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        } catch (IOException e) {
            if (_logger.isErrorEnabled()) _logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * get a list of oll configured plugins for a host.
     *
     * @param classLoader
     * @param host
     *
     * @return
     */
    public static List<IPlugin> getHostPlugins(ClassLoader classLoader, IHost host) {
        List<IPlugin> pluginList = Collections.synchronizedList(new ArrayList<IPlugin>());
        IMonitorContext monitorContext = host.getHostContext().getMonitorContext();
        IPlugin plugin = null;
        for (int i = 0; i < 256; i++) {
            String pluginConfigurationFileName = host.getHostContext().getConfiguration().getString("check.plugin." + i);
            if (pluginConfigurationFileName == null) continue;
            pluginConfigurationFileName = monitorContext.getConfiguration().getConfiguration().getString("plugins.directory", "./conf/plugins") + "/" + pluginConfigurationFileName;
            File propertiesFile = new File(pluginConfigurationFileName);
            if (!propertiesFile.exists()) {
                if (_logger.isErrorEnabled()) _logger.error(String.format("plugin configuration '%s' dont exists, ignored", pluginConfigurationFileName));
                continue;
            }
            plugin = PluginUtils.instantiatePlugin(classLoader, host.getHostContext(), pluginConfigurationFileName);
            if (plugin != null) pluginList.add(plugin);
        }
        return pluginList;
    }
}
