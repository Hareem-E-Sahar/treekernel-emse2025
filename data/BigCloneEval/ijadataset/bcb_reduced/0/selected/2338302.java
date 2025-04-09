package ao.dd.desktop.model.display;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * @author 188952
 */
public class ViewCache {

    private final long expiryTime;

    private final Robot bot;

    private final Rectangle outOf;

    private long lastFetched = 0;

    private BufferedImage view;

    public ViewCache(Robot bot, long expiryTime, Rectangle outOf) {
        this.bot = bot;
        this.expiryTime = expiryTime;
        this.outOf = outOf;
    }

    public BufferedImage view() {
        checkView();
        return view;
    }

    public BufferedImage view(Rectangle rectangle) {
        if (rectangle.equals(outOf)) {
            return view();
        }
        return bot.createScreenCapture(rectangle);
    }

    public Color colour(int x, int y) {
        return bot.getPixelColor(x, y);
    }

    private void checkView() {
        if (view == null || (System.currentTimeMillis() - lastFetched) > expiryTime) {
            view = bot.createScreenCapture(outOf);
            lastFetched = System.currentTimeMillis();
        }
    }
}
