package my.img;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import my.cache.CacheManager;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 图形验证码
 * @author liudong
 */
public class ImageCaptchaService {

    private static final String CACHE_REGION = "session";

    private static final String COOKIE_NAME = "_reg_key_";

    private static int WIDTH = 120;

    private static int HEIGHT = 40;

    private static int LENGTH = 5;

    private static final Random random = new Random();

    /**
     * 画随机码图
     * @param text
     * @param out
     * @param width
     * @param height
     * @throws java.io.IOException
     */
    private static void _Render(String text, OutputStream out, int width, int height) throws IOException {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < 10; i++) {
            g.setColor(_GetRandColor(150, 250));
            g.drawOval(random.nextInt(110), random.nextInt(24), 5 + random.nextInt(10), 5 + random.nextInt(10));
        }
        Font mFont = new Font("Arial", Font.ITALIC, 28);
        g.setFont(mFont);
        g.setColor(_GetRandColor(10, 240));
        g.drawString(text, 10, 30);
        ImageIO.write(bi, "png", out);
    }

    private static Color _GetRandColor(int fc, int bc) {
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public static void main(String[] args) throws IOException {
        String code = RandomStringUtils.randomAlphanumeric(LENGTH).toUpperCase();
        code = code.replace('0', 'W');
        code = code.replace('o', 'R');
        code = code.replace('I', 'E');
        code = code.replace('1', 'T');
        FileOutputStream out = new FileOutputStream("e:\\aa.jpg");
        _Render(code, out, 120, 40);
    }
}
