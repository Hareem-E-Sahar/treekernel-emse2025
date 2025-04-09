package jrbt.bots;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class Captura {

    public BufferedImage imagenPosicion;

    public BufferedImage imagenVida;

    private Dimension screenSize;

    private int ancho;

    private int alto;

    private Point p;

    private String tipo = "jpg";

    private String path = "c:/";

    public int getAlto() {
        return alto;
    }

    public void setAlto(int alto) {
        this.alto = alto;
    }

    public int getAncho() {
        return ancho;
    }

    public void setAncho(int ancho) {
        this.ancho = ancho;
    }

    public void setpath(String f) {
        this.path = f;
    }

    public void setdimension(Dimension d) {
        this.screenSize = d;
    }

    public void setpoint(Point p) {
        this.p = p;
    }

    public void settipo(String t) {
        this.tipo = t;
    }

    public Captura() {
        this.screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.p = new Point(0, 0);
        this.tipo = "jpg";
        this.path = "C:/default";
    }

    public void captureScreen() {
        try {
            Rectangle screenRectangle = new Rectangle(p, screenSize);
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRectangle);
            ImageIO.write(image, tipo, new File(path + "." + tipo));
        } catch (IOException ex) {
            Logger.getLogger(Captura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AWTException ex) {
            Logger.getLogger(Captura.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
