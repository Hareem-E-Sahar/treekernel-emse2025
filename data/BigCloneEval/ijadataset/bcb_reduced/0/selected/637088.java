package net.myphpshop.admin.util;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.JButton;

public class ImageComponent extends JButton {

    private BufferedImage _image;

    public void paintComponent(Graphics g) {
        if (_image == null) return;
        int maxWidth = this.getWidth();
        int maxHeight = this.getHeight();
        int breite = _image.getWidth();
        int hoehe = _image.getHeight();
        double verhaeltnis = (double) breite / (double) hoehe;
        double breiteFaktor = (double) breite / maxWidth;
        double hoeheFaktor = (double) hoehe / maxHeight;
        int neueBreite = 0;
        int neueHoehe = 0;
        if (maxWidth > breite && maxHeight > hoehe) {
            neueBreite = breite;
            neueHoehe = hoehe;
        } else if (hoeheFaktor > breiteFaktor) {
            neueHoehe = maxHeight;
            neueBreite = (int) (neueHoehe * verhaeltnis);
        } else {
            neueBreite = maxWidth;
            neueHoehe = (int) (neueBreite / verhaeltnis);
        }
        g.drawImage(_image, 0, 0, neueBreite, neueHoehe, this);
    }

    public void setImage(File file) throws Exception {
        if (!file.canRead()) {
            throw new RuntimeException("Cant read Image " + file.getAbsolutePath());
        }
        _image = ImageIO.read(file);
        if (_image != null) {
            repaint();
        }
    }

    public byte[] getImageDataAsJPG() throws Exception {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(this._image, "jpg", bas);
        return bas.toByteArray();
    }

    public void setImageFromJPGData(byte[] bytes) throws Exception {
        ByteArrayInputStream bas = new ByteArrayInputStream(bytes);
        this._image = ImageIO.read(bas);
    }

    public int getSizeInBytes() throws Exception {
        return getSizeInBytes(this._image);
    }

    private int getSizeInBytes(BufferedImage image) throws Exception {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", bas);
        return bas.size();
    }

    public void shrinkTo(int maxHeight, int maxWidth, int countOfBytes, float percentTolerance) throws Exception {
        if (_image.getHeight() > maxHeight || _image.getWidth() > maxWidth) {
            int breite = _image.getWidth();
            int hoehe = _image.getHeight();
            double verhaeltnis = (double) breite / (double) hoehe;
            double breiteFaktor = (double) breite / maxWidth;
            double hoeheFaktor = (double) hoehe / maxHeight;
            int neueBreite = 0;
            int neueHoehe = 0;
            if (maxWidth > breite && maxHeight > hoehe) {
                neueBreite = breite;
                neueHoehe = hoehe;
            } else if (hoeheFaktor > breiteFaktor) {
                neueHoehe = maxHeight;
                neueBreite = (int) (neueHoehe * verhaeltnis);
            } else {
                neueBreite = maxWidth;
                neueHoehe = (int) (neueBreite / verhaeltnis);
            }
            Image image = _image.getScaledInstance(neueBreite, neueHoehe, Image.SCALE_AREA_AVERAGING);
            BufferedImage bufferedImage = new BufferedImage(neueBreite, neueHoehe, BufferedImage.TYPE_INT_BGR);
            Graphics2D tmpImg = bufferedImage.createGraphics();
            tmpImg.drawImage(image, 0, 0, null);
            _image = bufferedImage;
            tmpImg.dispose();
        }
        int size = getSizeInBytes();
        int range = (int) ((float) countOfBytes * (percentTolerance - 0.005F));
        if (size < countOfBytes + range) {
            return;
        }
        BufferedImage image = null;
        float upperLimit = 1.0F;
        float lowerLimit = 0.0F;
        int lastsize = 0;
        do {
            ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName("JPEG").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            float quality = (upperLimit + lowerLimit) / 2;
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            writer.setOutput(ImageIO.createImageOutputStream(bas));
            writer.write(null, new IIOImage(_image, null, null), param);
            writer.dispose();
            ByteArrayInputStream basIn = new ByteArrayInputStream(bas.toByteArray());
            image = ImageIO.read(basIn);
            size = getSizeInBytes(image);
            if (size > countOfBytes) {
                upperLimit = quality;
            } else {
                lowerLimit = quality;
            }
            range = (int) ((float) countOfBytes * (percentTolerance - 0.005F));
            if (size < countOfBytes + range && size > countOfBytes - range) {
                break;
            }
            if (lastsize == size) {
                throw new RuntimeException("Cant shrink image to the right size!");
            }
            lastsize = size;
        } while (true);
        _image = image;
    }

    public BufferedImage getImage() {
        return _image;
    }

    public void setImage(BufferedImage bufImage) {
        _image = bufImage;
    }
}
