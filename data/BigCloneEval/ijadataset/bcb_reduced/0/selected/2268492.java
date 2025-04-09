package eln.editors.util;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * Canvas used by ImageCapture to preview and select what to capture.
 * 
 * @author tara
 *
 */
public class ImageCanvas extends Canvas implements MouseListener, MouseMotionListener {

    Image image;

    BufferedImage underlyingImage;

    private double scale = 1;

    private int startX = 0;

    private int startY = 0;

    private int endX = 0;

    private int endY = 0;

    int width = 0;

    int height = 0;

    int cornerX = 0;

    int cornerY = 0;

    boolean needRefresh = false;

    Frame frame;

    public ImageCanvas(Frame f) {
        frame = f;
    }

    public void showImage(Image img) {
        MediaTracker media = new MediaTracker(this);
        image = img;
        media.addImage(image, 0);
        try {
            media.waitForID(0);
        } catch (InterruptedException e) {
            System.err.println("unable to load image " + " - " + e.getMessage());
        }
        checkStatus(media);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void setImage(BufferedImage img) {
        underlyingImage = img;
    }

    public BufferedImage getImage() {
        return underlyingImage;
    }

    public BufferedImage getSelectedImage() {
        if (underlyingImage == null) {
            return null;
        } else {
            int x = getXCorner();
            int y = getYCorner();
            int width = getWidth();
            int height = getHeight();
            Rectangle selectedArea = new Rectangle(x, y, width, height);
            Raster intermediateRaster = underlyingImage.getData(selectedArea);
            Raster newRaster = Raster.createRaster(intermediateRaster.getSampleModel(), intermediateRaster.getDataBuffer(), null);
            BufferedImage result = new BufferedImage(width, height, underlyingImage.getType());
            result.setData(newRaster);
            return result;
        }
    }

    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    private void checkStatus(MediaTracker mt) {
        int status = mt.statusAll(false);
        String s;
        switch(status) {
            case MediaTracker.LOADING:
                s = "LOADING";
                break;
            case MediaTracker.ABORTED:
                s = "ABORTED";
                break;
            case MediaTracker.ERRORED:
                s = "ERRORED";
                break;
            case MediaTracker.COMPLETE:
                s = "COMPLETE";
                break;
            default:
                throw new IllegalArgumentException("illegal media status: " + status);
        }
    }

    public void clear() {
        setVisible(false);
        dispose();
        frame.remove(this);
    }

    public void display() throws AWTException {
        display(frame.getWidth(), frame.getHeight());
    }

    public void display(int width, int height) throws AWTException {
        frame.setVisible(false);
        BufferedImage image;
        image = getImage();
        if (image == null) {
            Frame[] fs = Frame.getFrames();
            for (int i = 0; i < fs.length; i++) {
                Frame tempFrame = fs[i];
                if (tempFrame != null) {
                    tempFrame.paint(tempFrame.getGraphics());
                }
            }
            Rectangle selectedArea = new Rectangle(0, 0, width, height);
            Robot robot = new Robot();
            image = robot.createScreenCapture(selectedArea);
            underlyingImage = image;
        }
        int newWidth = new Double(image.getWidth() * scale).intValue();
        int newHeight = new Double(image.getHeight() * scale).intValue();
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        showImage(scaledImage);
        frame.add("Center", this);
        Dimension d = new Dimension();
        d.setSize(scaledImage.getWidth(this), scaledImage.getHeight(this));
        frame.setSize(d);
        frame.setVisible(true);
        needRefresh = false;
    }

    public void dispose() {
        frame.dispose();
    }

    public void mousePressed(MouseEvent evt) {
        if (needRefresh) {
            try {
                display();
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startX = evt.getX();
        startY = evt.getY();
    }

    public void mouseReleased(MouseEvent evt) {
        endX = evt.getX();
        endY = evt.getY();
        if (startX < endX) {
            cornerX = startX;
            width = endX - startX;
        } else {
            cornerX = endX;
            width = startX - endX;
        }
        if (startY < endY) {
            cornerY = startY;
            height = endY - startY;
        } else {
            cornerY = endY;
            height = startY - endY;
        }
        Graphics g = getGraphics();
        g.setColor(Color.RED);
        g.drawRect(startX, startY, width, height);
        needRefresh = true;
    }

    public void setScale(double d) {
        scale = d;
    }

    public int getWidth() {
        int tempwidth = frame.getWidth() - 5;
        if (width > 0) tempwidth = width;
        return new Double(tempwidth / scale).intValue();
    }

    public int getHeight() {
        int tempheight = frame.getHeight() - 54;
        if (height > 0) {
            tempheight = height;
        }
        return new Double(tempheight / scale).intValue();
    }

    public int getXCorner() {
        if (width == 0 || height == 0) return 0; else return new Double(cornerX / scale).intValue();
    }

    public int getYCorner() {
        if (width == 0 || height == 0) return 0; else return new Double(cornerY / scale).intValue();
    }

    public void mouseDragged(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }
}
