package cz.langteacher.plugin.impl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import cz.langteacher.LTCLassLoader;
import cz.langteacher.module.IMessanger;
import cz.langteacher.plugin.ILTPLugin;
import cz.langteacher.plugin.PluginLoader;
import cz.langteacher.plugin.iface.IDataLTSetting;
import cz.langteacher.plugin.iface.ILTServerFacade;

/**
 * Responsible for loading JARs from directory "plugins" to classpath. After that all plugin classes are initialized
 * @author libor
 *
 */
public class PluginManager implements ApplicationContextAware, IPluginManager {

    private Logger logger = Logger.getLogger(PluginManager.class);

    private ApplicationContext applicationContext;

    private List<ILTPLugin> plugins = null;

    @Autowired
    private ILTServerFacade pluginFacade;

    @Autowired
    private IMessanger messanger;

    @Autowired
    private IDataLTSetting ltSetting;

    public PluginManager() {
        super();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void initPlugins() {
        List<Class<? extends ILTPLugin>> pluginClasses = new PluginLoader(ltSetting, messanger).getPluginClasses((LTCLassLoader) applicationContext.getClassLoader());
        plugins = new ArrayList<ILTPLugin>();
        for (Class<? extends ILTPLugin> class1 : pluginClasses) {
            try {
                Constructor<ILTPLugin> con = (Constructor<ILTPLugin>) class1.getConstructor();
                ILTPLugin plugin = con.newInstance();
                if (isIDPluginAlreadyUsed(plugin)) {
                    String msg = "Plugin with ID=" + plugin.getPluginID() + " has been loaded already. Plugin won't be loaded. Plugin name:" + plugin.getName();
                    logger.error(msg);
                    messanger.addMessage(msg);
                    continue;
                }
                plugin.setServerFacade(pluginFacade);
                plugins.add(plugin);
            } catch (Exception e) {
                logger.fatal("Cannot make instance from " + class1);
            }
        }
        for (ILTPLugin plugin : plugins) {
            plugin.serverStarting();
        }
    }

    private boolean isIDPluginAlreadyUsed(ILTPLugin newPlugin) {
        for (ILTPLugin plugin : plugins) {
            if (plugin.getPluginID().equals(newPlugin.getPluginID())) {
                return true;
            }
        }
        return false;
    }

    public List<ILTPLugin> getPlugins() {
        if (plugins == null) {
            initPlugins();
        }
        return plugins;
    }

    public void fireServerStarted() {
        for (ILTPLugin plugin : getPlugins()) {
            plugin.serverStarted();
        }
    }

    public void cleanUpPlugins() {
        if (plugins != null) {
            for (ILTPLugin plugin : plugins) {
                plugin.serverShutDown();
            }
        }
    }
}
