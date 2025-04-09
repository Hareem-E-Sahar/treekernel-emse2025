package screencompare.screen;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.imageio.ImageIO;
import screencompare.Screencompare;

public class Screen {

    BufferedImage _img;

    ScreenInfo _info;

    public static final int TRANSPARENT = 0x00000000;

    public static final int BLACK = 0xFF000000;

    public static enum Alignment {

        TOP_LEFT, TOP_CENTER, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public Screen(ScreenInfo info) {
        _info = info;
        try {
            Rectangle size = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            Robot robot = new Robot();
            BufferedImage screen = robot.createScreenCapture(size);
            _img = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < screen.getHeight(); y++) {
                for (int x = 0; x < screen.getWidth(); x++) {
                    _img.setRGB(x, y, screen.getRGB(x, y));
                }
            }
        } catch (AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public Screen(BufferedImage img, ScreenInfo info) {
        _img = img;
        _info = info;
    }

    public Screen(String file, ScreenInfo info) {
        try {
            String path = Screencompare.getDataDir() + "screens/" + file;
            _img = ImageIO.read(new File(path));
            _info = info;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean hasInfo() {
        if (_info != null) {
            return true;
        }
        return false;
    }

    public ScreenInfo getInfo() {
        return _info;
    }

    public void save(String file) {
        try {
            Screencompare.mkDataDirs("screens" + File.separator);
            String path = Screencompare.getDataPath("screens" + File.separator + file);
            ImageIO.write(_img, "png", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void save() {
        save(getInfo().getFilename());
    }

    public Screen clip(Rectangle rect) {
        BufferedImage imgNew = _img.getSubimage(rect.x, rect.y, rect.width, rect.height);
        return new Screen(imgNew, getInfo());
    }

    public Screen clip(Alignment align, int width, int height) {
        if (width > _img.getWidth() || height > _img.getHeight()) {
            return this;
        }
        Rectangle rect = new Rectangle(0, 0, width, height);
        switch(align) {
            case TOP_LEFT:
                break;
            case TOP_CENTER:
                rect.x = (_img.getWidth() - width) / 2;
                break;
            case TOP_RIGHT:
                rect.x = _img.getWidth() - width;
                break;
            case BOTTOM_LEFT:
                rect.y = _img.getHeight() - height;
                break;
            case BOTTOM_CENTER:
                rect.y = _img.getHeight() - height;
                rect.x = (_img.getWidth() - width) / 2;
                break;
            case BOTTOM_RIGHT:
                rect.y = _img.getHeight() - height;
                rect.x = _img.getWidth() - width;
                break;
        }
        return clip(rect);
    }

    public Screen mask(Color color) {
        BufferedImage result = new BufferedImage(_img.getWidth(), _img.getHeight(), _img.getType());
        int c = color.getRGB();
        for (int y = 0; y < _img.getHeight(); y++) {
            for (int x = 0; x < _img.getWidth(); x++) {
                if (_img.getRGB(x, y) == c) {
                    result.setRGB(x, y, TRANSPARENT);
                } else {
                    result.setRGB(x, y, _img.getRGB(x, y));
                }
            }
        }
        return new Screen(result, getInfo());
    }

    public Rectangle findViewport(Color matchColor) {
        int w = _img.getWidth();
        int h = _img.getHeight();
        int color = matchColor.getRGB();
        Rectangle rect = new Rectangle(w / 2, h / 2, 1, 1);
        if (_img.getRGB(rect.x, rect.y) != color) {
            System.err.println("Cannot detect viewport: Pixel " + rect.x + "," + rect.y + " doesn't match " + matchColor);
            System.exit(1);
        }
        expand: while (rect.x > 0) {
            if (_img.getRGB(rect.x - 1, rect.y) != color) break expand;
            rect.x--;
            rect.width++;
        }
        expand: while (rect.x + rect.width < w) {
            if (_img.getRGB(rect.x + rect.width, rect.y) != color) break expand;
            rect.width++;
        }
        expand: while (rect.y > 0) {
            for (int x = rect.x; x < rect.width; x++) if (_img.getRGB(x, rect.y - 1) != color) break expand;
            rect.y--;
            rect.height++;
        }
        expand: while (rect.y + rect.height < h) {
            for (int x = rect.x; x < rect.width; x++) if (_img.getRGB(x, rect.y + rect.height) != color) break expand;
            rect.height++;
        }
        return rect;
    }

    public float diffVal(Screen screen, Alignment align) {
        BufferedImage diffImg = diff(screen, align)._img;
        int diffCount = 0;
        int sumCount = 0;
        for (int y = 0; y < diffImg.getHeight(); y++) {
            for (int x = 0; x < diffImg.getWidth(); x++) {
                int px = diffImg.getRGB(x, y);
                if (px != TRANSPARENT) {
                    sumCount++;
                    if (px != BLACK) {
                        diffCount++;
                    }
                }
            }
        }
        return (float) diffCount / sumCount;
    }

    public Screen diff(Screen screen, Alignment align) {
        if (align == null) align = Alignment.TOP_LEFT;
        int minWidth = Math.min(_img.getWidth(), screen._img.getWidth());
        int minHeight = Math.min(_img.getHeight(), screen._img.getHeight());
        Screen screen1 = this.clip(align, minWidth, minHeight);
        Screen screen2 = screen.clip(align, minWidth, minHeight);
        BufferedImage diff = new BufferedImage(minWidth, minHeight, screen1._img.getType());
        for (int y = 0; y < minHeight; y++) {
            for (int x = 0; x < minWidth; x++) {
                int px1 = screen1._img.getRGB(x, y);
                int px2 = screen2._img.getRGB(x, y);
                int value = 0xFF000000 + Math.abs((px1 & 0x00FF0000) - (px2 & 0x00FF0000)) + Math.abs((px1 & 0x0000FF00) - (px2 & 0x0000FF00)) + Math.abs((px1 & 0x000000FF) - (px2 & 0x000000FF));
                if ((px1 & 0xFF000000) == 0 || (px2 & 0xFF000000) == 0) {
                    value = TRANSPARENT;
                }
                diff.setRGB(x, y, value);
            }
        }
        ScreenInfo info = new ScreenInfo("diff", null, null);
        return new Screen(diff, info);
    }

    public void diffReport(Screen screen, Alignment align) {
        Screen diff = diff(screen, align);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        String timestamp = sdf.format(Calendar.getInstance().getTime());
        Screencompare.mkDataDirs("reports" + File.separator);
        ScreenReport report = new ScreenReport(Screencompare.getDataPath("reports" + File.separator + timestamp + ".pdf"));
        report.create(this, screen, diff);
    }
}
