package net.sourceforge.processdash.ui.web.dash;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentOpenerJava16 implements DocumentOpener {

    Desktop desktop;

    private static final Logger log = Logger.getLogger(DocumentOpenerJava16.class.getName());

    public DocumentOpenerJava16() {
        desktop = getDesktop();
        if (desktop == null) throw new UnsupportedOperationException();
    }

    private Desktop getDesktop() {
        if (Desktop.isDesktopSupported() == false) return null;
        Desktop d = Desktop.getDesktop();
        if (d != null && d.isSupported(Action.OPEN)) return d; else return null;
    }

    public boolean openDocument(File doc) {
        try {
            desktop.open(doc);
            return true;
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to open file '" + doc.getPath() + "'", e);
            return false;
        }
    }
}
