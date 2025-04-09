package de.hsofttec.monitoring.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import de.hsofttec.monitoring.MonitorConfiguration;
import de.hsofttec.monitoring.MonitorContext;
import de.hsofttec.monitoring.host.HostContext;
import de.hsofttec.monitoring.host.IHostContext;
import de.hsofttec.monitoring.plugin.IPlugin;
import de.hsofttec.monitoring.util.MonitorTimer;

/**
 * @author <a href="mailto:shomburg@hsofttec.com">S.Homburg</a>
 * @version $Id: AbstractTestPlugin.java 2 2007-09-01 11:09:16Z shomburg $
 */
public class AbstractTestPlugin {

    private IPlugin _plugin;

    private static MonitorTimer _pluginTimer = new MonitorTimer(true);

    public void initIOC(Class pluginClass, String pluginConfig) {
        try {
            MonitorContext mContext = new MonitorContext(new MonitorConfiguration(new PropertiesConfiguration("./conf/monitor.properties")), _pluginTimer);
            HostContext hContext = new HostContext(mContext, new PropertiesConfiguration("./conf/hosts/ifxsrv.depot120.dpd.de.properties"));
            Constructor constructor = pluginClass.getConstructor(IHostContext.class, String.class);
            _plugin = (IPlugin) constructor.newInstance(hContext, pluginConfig);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public IPlugin getPlugin() {
        return _plugin;
    }
}
