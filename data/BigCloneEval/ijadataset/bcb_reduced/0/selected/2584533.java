package ws4is.servlets.captcha;

import java.util.Random;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.servlet.ServletContext;

public class ImageProducer {

    private static String possible = "23456789bcdfghjkmnpqrstvwxyzBCDFGHJKMNPQRTVWXYZ";

    private static String font_name = "captcha.ttf";

    private int width;

    private int height;

    private int code_length;

    private String code;

    private Font font;

    private float font_size;

    private Color background_color;

    private Color noise_color;

    private Random generator;

    private Color text_color;

    private ServletContext servletContext;

    private int getRand(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    public Color generateRandomColor(Color mix) {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }
        Color color = new Color(red, green, blue);
        return color;
    }

    private String generateCode(int characters) {
        code = "";
        int i = 0;
        while (i < characters) {
            code = code + possible.charAt(getRand(0, possible.length() - 1));
            i++;
        }
        return code;
    }

    private Font getFont() {
        try {
            InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(font_name);
            if (fis == null) {
                String pack = this.getClass().getPackage().getName().replace(".", System.getProperty("file.separator"));
                String subdir = "WEB-INF.classes.".replace(".", System.getProperty("file.separator"));
                String path = servletContext.getRealPath("/");
                path = path.concat(subdir);
                path = path.concat(pack);
                path = path.concat(System.getProperty("file.separator"));
                File file = new File(path + font_name);
                fis = new FileInputStream(file);
            }
            font = Font.createFont(Font.TRUETYPE_FONT, fis);
            fis.close();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return font.deriveFont(this.font_size);
    }

    public ImageProducer(long i, ServletContext context) {
        this.servletContext = context;
        this.imageProducer3(i);
    }

    public void imageProducer3(long i) {
        generator = new Random(i);
        int c1 = getRand(0, 128);
        int c2 = getRand(0, 128);
        int c3 = getRand(0, 128);
        background_color = new Color(getRand(c1, 128), getRand(c2, 128), getRand(c3, 128));
        c1 = getRand(128, 255);
        c2 = getRand(128, 255);
        c3 = getRand(128, 255);
        text_color = new Color(getRand(c1, 255), getRand(c2, 255), getRand(c3, 255));
        noise_color = new Color(Math.abs((text_color.getRed() - background_color.getRed()) / 2), Math.abs((text_color.getGreen() - background_color.getGreen()) / 2), Math.abs((text_color.getBlue() - background_color.getBlue()) / 2));
    }

    public BufferedImage generateImage() {
        BufferedImage imageWithBackground = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graph = (Graphics2D) imageWithBackground.getGraphics();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        graph.setRenderingHints(hints);
        graph.setColor(background_color);
        graph.fillRect(0, 0, width, height);
        for (int i = 0; i < (width * height) / 3; i++) {
            graph.setColor(noise_color);
            graph.fillArc(generator.nextInt(width), generator.nextInt(height), 1, 1, 0, 360);
        }
        noise_color = new Color(getRand(0, 250), getRand(0, 250), getRand(0, 250));
        for (int i = 0; i < (width * height) / 150; i++) {
            graph.setColor(noise_color);
            graph.drawLine(getRand(0, width), getRand(0, height), getRand(0, width), getRand(0, height));
        }
        generateCode(code_length);
        graph.setColor(this.text_color);
        graph.setFont(this.getFont());
        Rectangle2D fm = graph.getFontMetrics().getStringBounds(code, graph);
        graph.drawString(code, (int) ((width - fm.getWidth()) / 2), (int) ((height / 4 + fm.getHeight() / 4)));
        return imageWithBackground;
    }

    public String getCode() {
        return code;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
        font_size = (float) (height * 0.75);
    }

    public int getCode_length() {
        return code_length;
    }

    public void setCode_length(int code_length) {
        this.code_length = code_length;
    }
}
