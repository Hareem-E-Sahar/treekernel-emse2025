package org.drftpd.plugins.archive;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventSubscriber;
import org.drftpd.GlobalContext;
import org.drftpd.PluginInterface;
import org.drftpd.PropertyHelper;
import org.drftpd.event.ReloadEvent;
import org.drftpd.misc.CaseInsensitiveHashMap;
import org.drftpd.plugins.archive.archivetypes.ArchiveHandler;
import org.drftpd.plugins.archive.archivetypes.ArchiveType;
import org.drftpd.sections.SectionInterface;
import org.drftpd.util.CommonPluginUtils;
import org.drftpd.util.PluginObjectContainer;

/**
 * @author CyBeR
 * @version $Id: Archive.java 2521 2012-02-19 15:12:01Z cyber1331 $
 */
public class Archive implements PluginInterface {

    private static final Logger logger = Logger.getLogger(Archive.class);

    private Properties _props;

    private long _cycleTime;

    private HashSet<ArchiveHandler> _archiveHandlers = null;

    private TimerTask _runHandler = null;

    private CaseInsensitiveHashMap<String, Class<ArchiveType>> _typesMap;

    public Properties getProperties() {
        return _props;
    }

    public long getCycleTime() {
        return _cycleTime;
    }

    public ArchiveType getArchiveType(int count, String type, SectionInterface sec, Properties props) {
        ArchiveType archiveType = null;
        Class<?>[] SIG = { Archive.class, SectionInterface.class, Properties.class, int.class };
        if (!_typesMap.containsKey(type)) {
            logger.error("Archive Type: " + type + " wasn't loaded.");
        } else {
            if (!sec.getName().isEmpty()) {
                try {
                    Class<ArchiveType> clazz = _typesMap.get(type);
                    archiveType = clazz.getConstructor(SIG).newInstance(new Object[] { this, sec, props, count });
                } catch (Exception e) {
                    logger.error("Unable to load ArchiveType for section " + count + "." + type, e);
                }
            } else {
                logger.error("Unable to load Section for Archive " + count + "." + type);
            }
        }
        return archiveType;
    }

    public synchronized CaseInsensitiveHashMap<String, Class<ArchiveType>> getTypesMap() {
        return new CaseInsensitiveHashMap<String, Class<ArchiveType>>(_typesMap);
    }

    private void initTypes() {
        CaseInsensitiveHashMap<String, Class<ArchiveType>> typesMap = new CaseInsensitiveHashMap<String, Class<ArchiveType>>();
        try {
            List<PluginObjectContainer<ArchiveType>> loadedTypes = CommonPluginUtils.getPluginObjectsInContainer(this, "org.drftpd.plugins.archive", "ArchiveType", "ClassName", false);
            for (PluginObjectContainer<ArchiveType> container : loadedTypes) {
                String filterName = container.getPluginExtension().getParameter("TypeName").valueAsString();
                typesMap.put(filterName, container.getPluginClass());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Failed to load plugins for org.drftpd.plugins.archive.archivetypes extension point 'ArchiveType'", e);
        }
        _typesMap = typesMap;
    }

    private void reload() {
        initTypes();
        _props = GlobalContext.getGlobalContext().getPluginsConfig().getPropertiesForPlugin("archive.conf");
        _cycleTime = 60000 * Long.parseLong(PropertyHelper.getProperty(_props, "cycletime", "30").trim());
        if (_runHandler != null) {
            _runHandler.cancel();
            GlobalContext.getGlobalContext().getTimer().purge();
        }
        _runHandler = new TimerTask() {

            public void run() {
                int count = 1;
                String type;
                while ((type = PropertyHelper.getProperty(_props, count + ".type", null)) != null) {
                    type = type.trim();
                    SectionInterface sec = GlobalContext.getGlobalContext().getSectionManager().getSection(PropertyHelper.getProperty(_props, count + ".section", "").trim());
                    ArchiveType archiveType = getArchiveType(count, type, sec, _props);
                    if (archiveType != null) {
                        new ArchiveHandler(archiveType).start();
                    }
                    count++;
                }
            }
        };
        try {
            GlobalContext.getGlobalContext().getTimer().schedule(_runHandler, _cycleTime, _cycleTime);
        } catch (IllegalStateException e) {
        }
    }

    public synchronized boolean removeArchiveHandler(ArchiveHandler handler) {
        return _archiveHandlers.remove(handler);
    }

    public Collection<ArchiveHandler> getArchiveHandlers() {
        return Collections.unmodifiableCollection(_archiveHandlers);
    }

    public synchronized void addArchiveHandler(ArchiveHandler handler) throws DuplicateArchiveException {
        checkPathForArchiveStatus(handler.getArchiveType().getDirectory().getPath());
        _archiveHandlers.add(handler);
    }

    public synchronized void checkPathForArchiveStatus(String handlerPath) throws DuplicateArchiveException {
        for (Iterator<ArchiveHandler> iter = _archiveHandlers.iterator(); iter.hasNext(); ) {
            ArchiveHandler ah = iter.next();
            String ahPath = ah.getArchiveType().getDirectory().getPath();
            if (ahPath.length() > handlerPath.length()) {
                if (ahPath.startsWith(handlerPath)) {
                    throw new DuplicateArchiveException(ahPath + " is already being archived");
                }
            } else {
                if (handlerPath.startsWith(ahPath)) {
                    throw new DuplicateArchiveException(handlerPath + " is already being archived");
                }
            }
        }
    }

    @EventSubscriber
    public void onReloadEvent(ReloadEvent event) {
        reload();
    }

    public void startPlugin() {
        AnnotationProcessor.process(this);
        logger.info("Archive plugin loaded successfully");
        _archiveHandlers = new HashSet<ArchiveHandler>();
        reload();
    }

    public void stopPlugin(String reason) {
        if (_runHandler != null) {
            _runHandler.cancel();
            GlobalContext.getGlobalContext().getTimer().purge();
        }
        AnnotationProcessor.unprocess(this);
        logger.info("Archive plugin unloaded successfully");
    }
}
