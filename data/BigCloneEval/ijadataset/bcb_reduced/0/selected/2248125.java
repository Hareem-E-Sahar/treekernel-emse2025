package frond.fractals;

import frond.maths.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class TestFractal extends Fractal {

    public TestFractal() {
        super("testFractal");
    }

    /**
	* Fractal drawer for the gradient Fractal
	*/
    protected void drawFractal(BufferedImage image, Complex start, Complex end) {
        double xcoord;
        int sizeX = image.getWidth();
        int sizeY = image.getHeight();
        double tendency;
        Graphics2D graphics = image.createGraphics();
        for (int j = 0; j < sizeX; j++) {
            xcoord = start.getReal() + j * (end.getReal() - start.getReal()) / sizeX;
            if (xcoord < -1) {
                tendency = 0;
            } else if (xcoord > 1) {
                tendency = 1;
            } else {
                tendency = (xcoord + 1) / 2;
            }
            graphics.setColor(colourScheme.getColourForTendency(tendency));
            graphics.drawLine(j, 0, j, sizeY);
        }
    }
}
