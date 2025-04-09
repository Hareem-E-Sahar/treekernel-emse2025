package ru.sitekeeper.cpn.gui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import javax.swing.AbstractAction;

public class ActionOpenWebsite extends AbstractAction {

    public static final String HOMEPAGE = "http://sourceforge.net/projects/cpnightmare";

    private static final long serialVersionUID = 1L;

    public ActionOpenWebsite(String name) {
        super(name);
    }

    public void actionPerformed(ActionEvent evt) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(HOMEPAGE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
