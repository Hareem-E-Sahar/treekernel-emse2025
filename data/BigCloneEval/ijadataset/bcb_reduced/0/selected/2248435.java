package org.openconcerto.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 * Un écran de démarrage qui affiche l'image passée.
 * 
 * @author ILM Informatique 17 juin 2004
 */
public class Splash extends JFrame {

    private BufferedImage screenShot;

    private Image image;

    public Splash(String imageName) {
        super();
        this.setUndecorated(true);
        this.setBackground(Color.WHITE);
        URL imageURL = getClass().getResource("/" + imageName);
        ImageIcon icon = null;
        if (imageURL != null) icon = new ImageIcon(imageURL); else if (new File(imageName).exists()) icon = new ImageIcon(imageName);
        if (icon != null) {
            this.image = icon.getImage();
            int width = this.image.getWidth(null);
            int height = this.image.getHeight(null);
            this.setSize(width, height);
            this.setLocationRelativeTo(null);
            try {
                this.screenShot = new Robot().createScreenCapture(this.getBounds());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paint(Graphics g) {
        if (this.screenShot != null) g.drawImage(this.screenShot, 0, 0, null);
        g.drawImage(this.image, 0, 0, null);
    }
}
