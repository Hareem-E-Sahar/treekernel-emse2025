package ring.gui.vis2;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * 
 */
public class LabeledLineVisel extends LineVisel {

    /**
	 * 
	 */
    String label;

    /**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color
	 * @param location
	 */
    public LabeledLineVisel(double x1, double y1, double x2, double y2, Color color, int location, String label) {
        super(x1, y1, x2, y2, color, location);
        this.label = label;
    }

    public void paintVisel(Graphics2D g2) {
        super.paintVisel(g2);
        double labelX = (x + x2) / 2, labelY = (y + y2) / 2;
        g2.drawChars(label.toCharArray(), 0, label.length(), (int) labelX, (int) (labelY + 20));
    }
}
