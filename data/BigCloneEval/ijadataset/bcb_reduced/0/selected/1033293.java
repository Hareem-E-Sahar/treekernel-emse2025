package de.johannesluderschmidt.throng.sharedGUIResources.mac;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EventObject;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import com.muchsoft.util.mac.Java14Handler;
import de.johannesluderschmidt.throng.util.Constants;

public class ThrongMacAboutHandler extends JOptionPane implements Java14Handler {

    private JFrame owner;

    private JLabel aboutTextPane;

    private JLabel lbljohannesLuderschmidt;

    private JPanel aboutPanel;

    public ThrongMacAboutHandler(JFrame owner) {
        setBounds(100, 100, 260, 165);
        aboutTextPane = new JLabel();
        aboutTextPane.setBounds(6, 6, 255, 100);
        aboutTextPane.setText("<html><center><strong>Throng</strong> " + Constants.THRONG_VERSION + "</center><br/>" + Constants.FULL_NAME + "<br/><br/>For help look in<html>");
        lbljohannesLuderschmidt = new JLabel("<html><a href=''>" + Constants.HOMEPAGE_TEXT + "</a></html>");
        lbljohannesLuderschmidt.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lbljohannesLuderschmidt.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                java.net.URI uri = null;
                try {
                    uri = new java.net.URI(Constants.HOMEPAGE_LINK);
                } catch (URISyntaxException e1) {
                    e1.printStackTrace();
                }
                if (java.awt.Desktop.isDesktopSupported()) {
                    try {
                        if (uri != null) {
                            java.awt.Desktop.getDesktop().browse(uri);
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            public void mouseEntered(MouseEvent arg0) {
            }

            public void mouseExited(MouseEvent arg0) {
            }

            public void mousePressed(MouseEvent arg0) {
            }

            public void mouseReleased(MouseEvent arg0) {
            }
        });
        lbljohannesLuderschmidt.setBounds(6, 93, 248, 40);
        aboutPanel = new JPanel();
        aboutPanel.add(aboutTextPane);
        aboutPanel.add(lbljohannesLuderschmidt);
    }

    public void handleAbout(EventObject arg0) {
        showMessageDialog(owner, aboutPanel, "", INFORMATION_MESSAGE);
    }

    public void handleOpenApplication(EventObject arg0) {
    }

    public void handleOpenFile(EventObject arg0, String arg1) {
    }

    public void handlePrefs(EventObject arg0) {
    }

    public void handlePrintFile(EventObject arg0, String arg1) {
    }

    public void handleQuit(EventObject arg0) {
    }

    public void handleReOpenApplication(EventObject arg0) {
    }
}
