package net.sf.excompcel.util.fileopener;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * Open Folder on a Linux System.
 * 
 * @author Detlev Struebig
 * @since v0.2
 * 
 */
public class FolderOpenerLinux implements FolderOpener {

    /** Logger. */
    private static Logger log = Logger.getLogger(FolderOpenerLinux.class);

    public synchronized void openFolder(final File folder) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(folder);
        } else {
            log.warn("Desktop.isDesktopSupported()=" + Desktop.isDesktopSupported());
        }
    }
}
