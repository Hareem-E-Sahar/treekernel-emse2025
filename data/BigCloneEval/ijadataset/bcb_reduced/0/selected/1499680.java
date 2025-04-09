package de.fhluebeck.oop.gps.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * Beschreibung:<br>Controller für die AboutView.
 *
 * @author Ogün Bilge, Leif Hitzschke
 * @version 1.02, 13.11.2009
 * 
 */
public class AboutController implements ActionListener, MouseListener {

    AboutView _obj = null;

    public AboutController(AboutView aboutView) {
        _obj = aboutView;
    }

    /**
	 * �berpr�ft, ob Standardbrowser ge�ffnet werden kann und �ffnet diesen
	 */
    public void getBrowser() {
        if (Desktop.isDesktopSupported()) {
            Desktop desk = Desktop.getDesktop();
            if (desk.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desk.browse(new URI("http://code.google.com/p/gpsnutzung/"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "cmdOK") _obj.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.getBrowser();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this._obj.link.setForeground(Color.RED);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this._obj.link.setForeground(Color.black);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}
