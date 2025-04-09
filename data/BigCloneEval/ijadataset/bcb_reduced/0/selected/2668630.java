package net.codebuilders.desktop.imagetools;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeSupport;
import java.util.LinkedList;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Carl Marcum
 */
public class ImageToolsModel {

    private String dirPath = "";

    private String fileName = "";

    private String fileExt = "";

    private String text = "XXX";

    private String fontName = Font.SANS_SERIF;

    private int fontSize = 14;

    private float textBoxWidth = (float) 200.0;

    private Color color = Color.RED;

    Robot robot = null;

    private BufferedImage image = null;

    public LinkedList<BufferedImage> undoQueue = null;

    public LinkedList<BufferedImage> redoQueue = null;

    public PropertyChangeSupport pcs = null;

    /** Creates new form ImageToolsModel */
    public ImageToolsModel() {
        undoQueue = new LinkedList<BufferedImage>();
        redoQueue = new LinkedList<BufferedImage>();
        pcs = new PropertyChangeSupport(this);
    }

    String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public void capture() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        tk.sync();
        Rectangle ecran = new Rectangle(tk.getScreenSize());
        try {
            robot = new Robot();
        } catch (java.awt.AWTException awte) {
            awte.printStackTrace();
        }
        robot.setAutoDelay(0);
        robot.setAutoWaitForIdle(false);
        robot.delay(4000);
        setImage(robot.createScreenCapture(ecran));
    }

    /**
     * @return the image
     */
    public BufferedImage getImage() {
        BufferedImage image = this.image;
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(BufferedImage image) {
        System.out.println("entering Model setImage");
        BufferedImage old = this.image;
        this.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.image = image;
        if (old != null) {
            this.undoQueue.push(old);
            System.out.println("image added to undoQueue");
            System.out.println("undoQueue has " + this.undoQueue.size());
        }
        if (!this.redoQueue.isEmpty()) {
            this.redoQueue.clear();
            System.out.println("Redo Queue cleared");
        }
        System.out.println("Model firePropertyChange");
        if (old != null) {
            System.out.println(old.toString());
        }
        System.out.println(this.image.toString());
        pcs.firePropertyChange("image", old, this.image);
        if (pcs.hasListeners("image")) {
            System.out.println("image has listeners");
        } else {
            System.out.println("image has no listeners");
        }
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        String oldText = getText();
        this.text = text;
        pcs.firePropertyChange("text", oldText, this.text);
    }

    /**
     * @return the fontSize
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @param fontSize the fontSize to set
     */
    public void setFontSize(int fontSize) {
        int oldSize = this.getFontSize();
        this.fontSize = fontSize;
        pcs.firePropertyChange("fontSize", oldSize, fontSize);
    }

    public void undo() {
        if (this.undoQueue.isEmpty()) {
            return;
        } else {
            this.redoQueue.push(this.getImage());
            this.image = this.undoQueue.pop();
            System.out.println("undoQueue has " + this.undoQueue.size());
            System.out.println("redoQueue has " + this.redoQueue.size());
        }
    }

    public void redo() {
        if (this.redoQueue.isEmpty()) {
            return;
        } else {
            this.undoQueue.push(this.getImage());
            this.image = this.redoQueue.pop();
            System.out.println("undoQueue has " + this.undoQueue.size());
            System.out.println("redoQueue has " + this.redoQueue.size());
        }
    }

    /**
     * @return the textBoxWidth
     */
    public float getTextBoxWidth() {
        return textBoxWidth;
    }

    /**
     * @param textBoxWidth the textBoxWidth to set
     */
    public void setTextBoxWidth(float textBoxWidth) {
        this.textBoxWidth = textBoxWidth;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @return the fontName
     */
    public String getFontName() {
        return fontName;
    }

    /**
     * @param fontName the fontName to set
     */
    public void setFontName(String fontName) {
        String oldFontName = this.getFontName();
        this.fontName = fontName;
        pcs.firePropertyChange("fontName", oldFontName, this.fontName);
    }
}
