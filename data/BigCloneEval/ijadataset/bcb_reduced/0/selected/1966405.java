package ao.dd.desktop.model.display;

import ao.dd.desktop.model.area.Area;
import ao.dd.desktop.model.area.Areas;
import ao.dd.desktop.model.image.ArrayRgbGrid;
import ao.dd.desktop.model.image.RgbGrid;
import ao.dd.desktop.util.DesktopUtils;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Robot;

/**
 * User: 188952
 * Date: Apr 17, 2010
 * Time: 11:51:35 PM
 */
public class ScreenDisplay implements Display {

    private final GraphicsDevice screen;

    private final Robot bot;

    public ScreenDisplay(GraphicsDevice displayScreen) {
        screen = displayScreen;
        bot = DesktopUtils.newRobot(screen);
    }

    @Override
    public RgbGrid view() {
        return view(asRectangle());
    }

    @Override
    public RgbGrid view(Rectangle rectangle) {
        return new ArrayRgbGrid(bot.createScreenCapture(rectangle), "Screen");
    }

    @Override
    public Color colour(int x, int y) {
        return bot.getPixelColor(x, y);
    }

    @Override
    public boolean point(int x, int y) {
        bot.mouseMove(x, y);
        return true;
    }

    @Override
    public GraphicsDevice device() {
        return screen;
    }

    @Override
    public int width() {
        return bounds().width;
    }

    @Override
    public int height() {
        return bounds().height;
    }

    private Rectangle bounds() {
        return screen.getDefaultConfiguration().getBounds();
    }

    @Override
    public Area asArea() {
        return Areas.newInstance(this, asRectangle());
    }

    @Override
    public Rectangle asRectangle() {
        return new Rectangle(0, 0, width(), height());
    }

    @Override
    public String toString() {
        return "Screen: " + screen + ", Display: " + bot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScreenDisplay)) return false;
        ScreenDisplay that = (ScreenDisplay) o;
        return !(screen != null ? !screen.equals(that.screen) : that.screen != null);
    }

    @Override
    public int hashCode() {
        return screen != null ? screen.hashCode() : 0;
    }
}
