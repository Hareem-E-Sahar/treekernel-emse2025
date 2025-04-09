package org.tzi.use.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

/**
 * Thread for capturing the Eval browser window to a image-file
 */
public class CaptureTheWindow extends Thread {

    Component fComponent;

    public CaptureTheWindow(Component comp) {
        fComponent = comp;
    }

    public void run() {
        try {
            Rectangle rec = fComponent.getBounds();
            Robot robot = new Robot();
            sleep(100);
            BufferedImage img = new Robot().createScreenCapture(rec);
            JFileChooser chooser = new JFileChooser(".");
            chooser.addChoosableFileFilter(new PngFilter());
            chooser.addChoosableFileFilter(new BmpFilter());
            chooser.addChoosableFileFilter(new JpgFilter());
            FileFilter[] filters = chooser.getChoosableFileFilters();
            chooser.setFileFilter(filters[0]);
            int returnVal = chooser.showSaveDialog(fComponent);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (!selectedFile.exists()) selectedFile.createNewFile();
                FileFilter ff = chooser.getFileFilter();
                String format = getExtention(selectedFile);
                if (format.equals("jpg") || format.equals("jpeg") || format.equals("png") || format.equals("bmp")) {
                    if (selectedFile.canWrite()) ImageIO.write(img, format, selectedFile.getAbsoluteFile()); else new ErrorFrame("IO Error on File " + selectedFile.getAbsoluteFile() + " occured");
                } else if (getExtention(selectedFile).equals("eps")) {
                    if (selectedFile.canWrite()) ImageIO.write(img, "eps", selectedFile.getAbsoluteFile()); else new ErrorFrame("IO Error on File " + selectedFile.getAbsoluteFile() + " occured");
                } else if (ff.getDescription().equals("All Files")) {
                    if (selectedFile.canWrite()) ImageIO.write(img, "png", new File(selectedFile.getAbsolutePath() + ".png")); else new ErrorFrame("IO Error on File " + selectedFile.getAbsoluteFile() + " occured");
                } else {
                    if (selectedFile.canWrite()) {
                        format = ff.getDescription().substring(2);
                        ImageIO.write(img, format, new File(selectedFile.getAbsoluteFile() + "." + format));
                    } else new ErrorFrame("IO Error on File " + selectedFile.getAbsoluteFile() + " occurred");
                }
            }
        } catch (Exception e) {
            new ErrorFrame("IO Error occurred while capturing the screen");
        }
    }

    class PngFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extention = getExtention(f);
            if (extention != null && (extention.equals("png"))) return true; else return false;
        }

        public String getDescription() {
            return "*.png";
        }
    }

    class JpgFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extention = getExtention(f);
            if (extention != null && (extention.equals("jpg") || extention.equals("jpeg"))) return true; else return false;
        }

        public String getDescription() {
            return "*.jpg";
        }
    }

    class EpsFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extention = getExtention(f);
            if (extention != null && (extention.equals("eps"))) return true; else return false;
        }

        public String getDescription() {
            return "*.eps";
        }
    }

    class BmpFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extention = getExtention(f);
            if (extention != null && (extention.equals("bmp"))) return true; else return false;
        }

        public String getDescription() {
            return "*.bmp";
        }
    }

    public String getExtention(File f) {
        String ext = "";
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * shows an error Frame for the user
     */
    public class ErrorFrame extends JFrame {

        public ErrorFrame(String labelTxt) {
            super("Error message");
            JLabel label = new JLabel(labelTxt);
            label.setHorizontalAlignment(JLabel.CENTER);
            getContentPane().add(label);
            setSize(300, 100);
            setVisible(true);
            adjustTopWidth(label, labelTxt, labelTxt);
        }

        public void adjustTopWidth(JLabel label, String text, String htmlText) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension displaySize = tk.getScreenSize();
            int maxWidth = (int) displaySize.getWidth();
            int windowSize = getWidth();
            if (windowSize < maxWidth) maxWidth = windowSize;
            FontMetrics fm = label.getFontMetrics(label.getFont());
            int topWidth = 0;
            int topHeight = fm.getHeight();
            Pattern p = Pattern.compile("\n");
            String s[] = p.split(text);
            for (int i = 0; i < s.length; i++) {
                if (topWidth < fm.stringWidth(s[i])) if (fm.stringWidth(s[i]) < maxWidth) topWidth = fm.stringWidth(s[i]); else topWidth = maxWidth;
                topHeight += (fm.getHeight() * (1 + (fm.stringWidth(s[i]) / maxWidth)));
            }
            label.setPreferredSize(new Dimension(topWidth, topHeight));
            label.setText(htmlText);
            label.setVisible(false);
            label.setVisible(true);
        }
    }
}
