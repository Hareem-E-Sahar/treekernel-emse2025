package org.drftpd.plugins;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.TimerTask;
import net.sf.drftpd.event.Event;
import net.sf.drftpd.event.FtpListener;
import org.apache.log4j.Logger;
import org.drftpd.GlobalContext;
import org.drftpd.PropertyHelper;
import org.drftpd.mirroring.ArchiveHandler;
import org.drftpd.mirroring.ArchiveType;
import org.drftpd.mirroring.DuplicateArchiveException;
import org.drftpd.sections.SectionInterface;

/**
 * @author zubov
 * @version $Id: Archive.java 1764 2007-08-04 02:01:21Z tdsoul $
 * This addon needs a little reworking, consider it and its
 * related packages unstable
 */
public class Archive extends FtpListener {

    private static final Logger logger = Logger.getLogger(Archive.class);

    private Properties _props;

    private long _cycleTime;

    private boolean _isStopped = false;

    private HashSet<ArchiveHandler> _archiveHandlers = new HashSet<ArchiveHandler>();

    private TimerTask _runHandler = null;

    public Archive() {
        logger.info("Archive plugin loaded successfully");
    }

    public Properties getProperties() {
        return _props;
    }

    public void actionPerformed(Event event) {
        if (event.getCommand().equals("RELOAD")) {
            reload();
            return;
        }
    }

    /**
     * @return the correct ArchiveType for the @section - it will return null if that section does not have an archiveType loaded for it
     */
    public ArchiveType getArchiveType(SectionInterface section) {
        ArchiveType archiveType = null;
        String name = null;
        try {
            name = PropertyHelper.getProperty(_props, section.getName() + ".archiveType");
        } catch (NullPointerException e) {
            return null;
        }
        Constructor constructor = null;
        Class[] classParams = { Archive.class, SectionInterface.class, Properties.class };
        Object[] objectParams = { this, section, _props };
        try {
            constructor = Class.forName("org.drftpd.mirroring.archivetypes." + name).getConstructor(classParams);
            archiveType = (ArchiveType) constructor.newInstance(objectParams);
        } catch (Exception e2) {
            logger.error("Unable to load ArchiveType for section " + section.getName(), e2);
        }
        return archiveType;
    }

    /**
     * Returns the getCycleTime setting
     */
    public long getCycleTime() {
        return _cycleTime;
    }

    public void init(GlobalContext gctx) {
        super.init(gctx);
        reload();
    }

    public void reload() {
        _props = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("conf/archive.conf");
            _props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("conf/archive.conf is missing, cannot continue", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    logger.error("Could not close the FileInputStream of conf/archive.conf", e);
                }
                fis = null;
            }
        }
        _cycleTime = 60000 * Long.parseLong(PropertyHelper.getProperty(_props, "cycleTime"));
        if (_runHandler != null) {
            _runHandler.cancel();
        }
        _runHandler = new TimerTask() {

            public void run() {
                Collection<SectionInterface> sectionsToCheck = getGlobalContext().getSectionManager().getSections();
                for (SectionInterface section : sectionsToCheck) {
                    ArchiveType archiveType = getArchiveType(section);
                    if (archiveType == null) {
                        continue;
                    }
                    new ArchiveHandler(archiveType).start();
                }
            }
        };
        getGlobalContext().getTimer().schedule(_runHandler, 0, _cycleTime);
    }

    public void unload() {
        if (_runHandler != null) {
            _runHandler.cancel();
        }
    }

    public synchronized boolean removeArchiveHandler(ArchiveHandler handler) {
        for (Iterator iter = _archiveHandlers.iterator(); iter.hasNext(); ) {
            ArchiveHandler ah = (ArchiveHandler) iter.next();
            if (ah == handler) {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    public Collection<ArchiveHandler> getArchiveHandlers() {
        return Collections.unmodifiableCollection(_archiveHandlers);
    }

    public synchronized void addArchiveHandler(ArchiveHandler handler) throws DuplicateArchiveException {
        checkPathForArchiveStatus(handler.getArchiveType().getDirectory().getPath());
        _archiveHandlers.add(handler);
    }

    public void checkPathForArchiveStatus(String handlerPath) throws DuplicateArchiveException {
        for (Iterator iter = _archiveHandlers.iterator(); iter.hasNext(); ) {
            ArchiveHandler ah = (ArchiveHandler) iter.next();
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
}
