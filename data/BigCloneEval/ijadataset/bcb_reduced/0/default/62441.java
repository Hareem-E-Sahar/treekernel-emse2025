import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

public class Chopper {

    static double wthresh = 8.2;

    public static void main(String args[]) {
        try {
            if (args.length != 1) throw new Exception("usage: java Chopper <input>");
            File od = new File("chopped");
            if (!od.exists()) {
                od.mkdir();
            } else if (!od.isDirectory()) {
                throw new Exception("output directory obstructed");
            } else {
                File list[] = od.listFiles();
                for (File f : list) {
                    f.delete();
                }
            }
            Chopper c = new Chopper(ImageIO.read(new File(args[0])));
            c.dump();
        } catch (Throwable t) {
            System.err.println(t);
            System.exit(1);
        }
    }

    BufferedImage image;

    LinkedList<Rectangle> rl;

    Chopper(BufferedImage _image) {
        image = _image;
        rl = new LinkedList<Rectangle>();
        chop();
    }

    void chop() {
        int y;
        boolean region;
        Rectangle linerect = new Rectangle(0, 0, image.getWidth(), 0);
        for (y = 0, region = false; y < image.getHeight(); y++) {
            if (region) {
                if (isLineWhite(y)) {
                    region = false;
                    linerect.height = y - linerect.y;
                    chop(linerect);
                }
            } else {
                if (!isLineWhite(y)) {
                    region = true;
                    linerect.y = y;
                }
            }
        }
    }

    void chop(Rectangle linerect) {
        int x, y;
        boolean region;
        Rectangle r = new Rectangle(0, linerect.y, 0, linerect.height);
        for (x = 0, region = false; x < image.getWidth(); x++) {
            if (region) {
                if (isSubcolWhite(x, linerect.y, linerect.height)) {
                    region = false;
                    r.width = x - r.x;
                    rl.add(new Rectangle(r));
                }
            } else {
                if (!isSubcolWhite(x, linerect.y, linerect.height)) {
                    region = true;
                    r.x = x;
                }
            }
        }
    }

    boolean isLineWhite(int y) {
        int rgb;
        int w = image.getWidth();
        double dw = 0.0;
        for (int x = 0; x < w; x++) {
            rgb = image.getRGB(x, y);
            int rd = 0xff - ((rgb & 0xff0000) >> 16);
            int gd = 0xff - ((rgb & 0xff00) >> 8);
            int bd = 0xff - (rgb & 0xff);
            dw += Math.sqrt(rd * rd + gd * gd + bd * bd) / (double) w;
        }
        return dw < wthresh;
    }

    boolean isSubcolWhite(int x, int y, int h) {
        int rgb;
        double dw = 0.0;
        for (int y1 = y; y1 < y + h; y1++) {
            rgb = image.getRGB(x, y1);
            int rd = 0xff - ((rgb & 0xff0000) >> 16);
            int gd = 0xff - ((rgb & 0xff00) >> 8);
            int bd = 0xff - (rgb & 0xff);
            dw += Math.sqrt(rd * rd + gd * gd + bd * bd) / (double) h;
        }
        return dw < wthresh;
    }

    void dump() throws Throwable {
        int counter = 0;
        int digits = (int) Math.floor(Math.log10((double) rl.size())) + 1;
        for (Rectangle r : rl) {
            ImageIO.write(image.getSubimage(r.x, r.y, r.width, r.height), "PNG", new File("chopped" + File.separator + lzpad(Integer.toString(counter++), digits) + ".png"));
        }
    }

    String lzpad(String x, int y) {
        String pad = "";
        for (int i = 0; i < y - x.length(); i++) {
            pad += '0';
        }
        return pad + x;
    }
}
