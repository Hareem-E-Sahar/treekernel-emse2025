import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.*;
import java.beans.*;
import javax.swing.table.*;
import com.sun.image.codec.jpeg.*;
import java.net.*;
import javax.imageio.ImageIO;
import java.awt.geom.*;

/**
 * Class which handles allowing the user to draw points on the map.
 *
 */
class JMenuRadioButtonAddImages extends JMenuRadioButtonAction {

    static final long serialVersionUID = 42643948;

    /**
    * Constructor for creation of Radio button
    * @param registeredObject
    */
    public JMenuRadioButtonAddImages(GUI registeredObject) {
        super("Add Images", registeredObject);
    }

    /**
    * Run method for class.
    */
    public void run() {
        GUI gui = (GUI) registeredObject;
        GPane pane = gui.getTopPane();
        if (pane.getMouseRectanglePosition() != null) {
            String extension = "png";
            String filename = File.separator + extension;
            JFrame frame = new JFrame();
            JFileChooser fileChooser = new JFileChooser(new File(filename));
            fileChooser.showSaveDialog(frame);
            File outputFile = fileChooser.getSelectedFile();
            Rectangle selection = pane.getMouseRectanglePosition();
            Point upperLeftScreenPixel = pane.getUpperLeftPixel();
            Point upperLeft = new Point(upperLeftScreenPixel.x + selection.x, upperLeftScreenPixel.y + selection.y);
            Point lowerRight = new Point(upperLeft.x + selection.width, upperLeft.y + selection.height);
            BufferedImage loadedImage = LibGUI.loadImage(outputFile.getPath());
            if (outputFile != null && loadedImage != null) gui.getGMap().getGDraw().add(new GImage(new GPhysicalPoint(upperLeft, pane.getZoom()), new GPhysicalPoint(lowerRight, pane.getZoom()), loadedImage));
        }
        pane.setMode(GPane.IMAGE_MODE);
        super.setSelected(true);
        gui.getNotifier().firePaneEvent(this);
        gui.getProgressMeter().getPanel().repaint();
    }

    /**
    * Pane event method for class.
    */
    public void paneEvent(Object object) {
        GUI gui = (GUI) registeredObject;
        GPane pane = gui.getTopPane();
        if (pane == null) return;
        super.setSelected(pane.getMode() == GPane.IMAGE_MODE);
    }
}
