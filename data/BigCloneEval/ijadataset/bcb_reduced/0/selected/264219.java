package eu.keep.uphec.mainwindow.menubar;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 * The ViewTOTEMListener class defines the activities following the firing of the totem_button button 
 * in the viewMenu menu
 * 
 * @author Antonio Ciuffreda 
 */
public class ViewTOTEMListener implements ActionListener {

    private static final Logger logger = Logger.getLogger(eu.keep.uphec.mainwindow.menubar.ViewTOTEMListener.class.getName());

    private URI address;

    private JFrame mainWindowFrame;

    public ViewTOTEMListener(JFrame mainWindowFrame) {
        this.mainWindowFrame = mainWindowFrame;
    }

    public void actionPerformed(ActionEvent arg0) {
        try {
            address = new URI("http://www.keep-totem.co.uk");
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        System.out.println(Desktop.isDesktopSupported());
        try {
            Desktop.getDesktop().browse(address);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainWindowFrame.getRootPane(), "An I/O error occurred: the TOTEM metadata database could not be accessed");
            logger.warn("A I/O error occurred: the TOTEM metadata database could not be accessed.");
        }
    }
}
