package libVideoKrowdix;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class Imagen {

    protected static void capturaPantalla(String path) {
        BufferedImage pantalla = obtenerCapturaPantalla();
        try {
            pantalla = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            File file = new File(path);
            ImageIO.write(pantalla, "jpg", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage obtenerCapturaPantalla() {
        BufferedImage pantalla;
        try {
            pantalla = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            return pantalla;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static void capturaPantallaRecortada(String path, int x, int y, int w, int h) {
        BufferedImage orig = obtenerCapturaPantalla(), fin = orig.getSubimage(x, y, w, h);
        File file = new File(path);
        try {
            ImageIO.write(fin, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
