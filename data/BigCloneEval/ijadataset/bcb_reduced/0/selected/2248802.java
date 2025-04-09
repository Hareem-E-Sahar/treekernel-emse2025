package br.org.acessobrasil.portal.util;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Imagem {

    public static void criarScreenShot() {
        try {
            BufferedImage bi;
            bi = new Robot().createScreenCapture(new Rectangle(0, 0, 1024, 768));
            ImageIO.write(bi, "jpg", new File("suporte.jpg"));
        } catch (Exception e) {
        }
    }
}
