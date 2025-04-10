package org.drftpd.plugins.trafficmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.drftpd.GlobalContext;
import org.drftpd.PluginInterface;
import org.drftpd.PropertyHelper;
import org.drftpd.commands.dataconnection.event.SlowTransferEvent;
import org.drftpd.event.ReloadEvent;
import org.drftpd.misc.CaseInsensitiveHashMap;
import org.drftpd.util.CommonPluginUtils;
import org.drftpd.util.PluginObjectContainer;

/**
 * @author CyBeR
 * @version $Id: TrafficManager.java 1925 2009-06-15 21:46:05Z CyBeR $
 */
public class TrafficManager implements PluginInterface {

    private static final Logger logger = Logger.getLogger(TrafficManager.class);

    private CaseInsensitiveHashMap<String, Class<TrafficType>> _typesMap;

    private ArrayList<TrafficType> _traffictypes;

    @Override
    public void startPlugin() {
        AnnotationProcessor.process(this);
        loadConf();
        logger.debug("Strated TrafficManager Plugin");
    }

    @Override
    public void stopPlugin(String reason) {
        AnnotationProcessor.unprocess(this);
        _traffictypes = new ArrayList<TrafficType>();
    }

    @EventSubscriber
    public void onReloadEvent(ReloadEvent event) {
        loadConf();
    }

    public static TrafficManager getTrafficManager() {
        for (PluginInterface plugin : GlobalContext.getGlobalContext().getPlugins()) {
            if (plugin instanceof TrafficManager) {
                return (TrafficManager) plugin;
            }
        }
        throw new RuntimeException("TrafficManager plugin is not loaded.");
    }

    private TrafficType getTrafficType(int count, String type, Properties props) {
        TrafficType trafficType = null;
        Class<?>[] SIG = { Properties.class, int.class, String.class };
        if (!_typesMap.containsKey(type)) {
            logger.error("Traffic Type: " + type + " wasn't loaded.");
        } else {
            try {
                Class<TrafficType> clazz = _typesMap.get(type);
                trafficType = clazz.getConstructor(SIG).newInstance(new Object[] { props, count, type.toLowerCase() });
            } catch (Exception e) {
                logger.error("Unable to load TrafficType for section " + count + ".type=" + type, e);
            }
        }
        return trafficType;
    }

    private void initTypes() {
        CaseInsensitiveHashMap<String, Class<TrafficType>> typesMap = new CaseInsensitiveHashMap<String, Class<TrafficType>>();
        try {
            List<PluginObjectContainer<TrafficType>> loadedTypes = CommonPluginUtils.getPluginObjectsInContainer(this, "org.drftpd.plugins.trafficmanager", "TrafficType", "ClassName", false);
            for (PluginObjectContainer<TrafficType> container : loadedTypes) {
                String filterName = container.getPluginExtension().getParameter("TypeName").valueAsString();
                typesMap.put(filterName, container.getPluginClass());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Failed to load plugins for org.drftpd.plugins.trafficmanager extension point 'TrafficType'", e);
        }
        _typesMap = typesMap;
    }

    public void loadConf() {
        initTypes();
        _traffictypes = new ArrayList<TrafficType>();
        Properties _props = GlobalContext.getGlobalContext().getPluginsConfig().getPropertiesForPlugin("trafficmanager.conf");
        int count = 1;
        String type;
        while ((type = PropertyHelper.getProperty(_props, count + ".type", null)) != null) {
            TrafficType trafficType = getTrafficType(count, type, _props);
            if (trafficType != null) {
                _traffictypes.add(trafficType);
            }
            count++;
        }
    }

    public ArrayList<TrafficType> getTrafficTypes() {
        return new ArrayList<TrafficType>(_traffictypes);
    }

    @EventSubscriber
    public void onSlowTransferEvent(SlowTransferEvent event) {
        for (TrafficType trafficType : getTrafficTypes()) {
            if ((event.isStor() && trafficType.getUpload()) || (!event.isStor() && trafficType.getDownload())) {
                if ((trafficType.checkInclude(event.getFile().getParent().getPath())) && (!trafficType.checkExclude(event.getFile().getParent().getPath())) && (trafficType.getPerms().check(event.getUser()))) {
                    trafficType.doAction(event.getUser(), event.getFile(), event.isStor(), event.getMinSpeed(), event.getSpeed(), event.getTransfered(), event.getConn(), event.getSlaveName());
                    return;
                }
            }
        }
    }
}
