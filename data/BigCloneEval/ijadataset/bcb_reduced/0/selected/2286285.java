package pobr;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Image2Process {

    BufferedImage image;

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void setHSL(int x, int y, int H, int S, int L) {
        float r;
        float g;
        float b;
        int R;
        int G;
        int B;
        float h = (float) H / 360;
        float s = (float) S / 100;
        float l = (float) L / 100;
        if (s == 0) {
            R = (int) (l * 255);
            G = (int) (l * 255);
            B = (int) (l * 255);
            setColor(x, y, (new Color(R, G, B)).getRGB());
            return;
        }
        float temp1 = 0;
        float temp2 = 0;
        float Rtemp3 = 0;
        float Gtemp3 = 0;
        float Btemp3 = 0;
        if (l < 0.5) temp2 = l * (1.0f + s);
        if (l >= 0.5) temp2 = l + s - l * s;
        temp1 = 2.0f * l - temp2;
        Rtemp3 = h + 1.0f / 3.0f;
        Gtemp3 = h;
        Btemp3 = h - 1.0f / 3.0f;
        if (Rtemp3 < 0) Rtemp3 = Rtemp3 + 1.0f;
        if (Rtemp3 > 1) Rtemp3 = Rtemp3 - 1.0f;
        if (Gtemp3 < 0) Gtemp3 = Gtemp3 + 1.0f;
        if (Gtemp3 > 1) Gtemp3 = Gtemp3 - 1.0f;
        if (Btemp3 < 0) Btemp3 = Btemp3 + 1.0f;
        if (Btemp3 > 1) Btemp3 = Btemp3 - 1.0f;
        if (6.0 * Rtemp3 < 1) r = temp1 + (temp2 - temp1) * 6.0f * Rtemp3; else if (2.0f * Rtemp3 < 1) r = temp2; else if (3.0f * Rtemp3 < 2) r = temp1 + (temp2 - temp1) * ((2.0f / 3.0f) - Rtemp3) * 6.0f; else r = temp1;
        if (6.0 * Gtemp3 < 1) g = temp1 + (temp2 - temp1) * 6.0f * Gtemp3; else if (2.0f * Gtemp3 < 1) g = temp2; else if (3.0f * Gtemp3 < 2) g = temp1 + (temp2 - temp1) * ((2.0f / 3.0f) - Gtemp3) * 6.0f; else g = temp1;
        if (6.0 * Btemp3 < 1) b = temp1 + (temp2 - temp1) * 6.0f * Btemp3; else if (2.0f * Btemp3 < 1) b = temp2; else if (3.0f * Btemp3 < 2) b = temp1 + (temp2 - temp1) * ((2.0f / 3.0f) - Btemp3) * 6.0f; else b = temp1;
        R = (int) (r * 255);
        G = (int) (g * 255);
        B = (int) (b * 255);
        setColor(x, y, (new Color(R, G, B)).getRGB());
    }

    public int getH(int x, int y) {
        int H = 0;
        int S = 0;
        int L = 0;
        float fH = 0;
        float fS = 0;
        float fL = 0;
        float r = (float) getRed(x, y) / 255;
        float g = (float) getGreen(x, y) / 255;
        float b = (float) getBlue(x, y) / 255;
        float maxcolor = r > g ? r : g;
        maxcolor = b > maxcolor ? b : maxcolor;
        float mincolor = r < g ? r : g;
        mincolor = b < mincolor ? b : mincolor;
        fL = (maxcolor + mincolor) / 2;
        L = (int) (fL * 100);
        if (maxcolor == mincolor) {
            S = 0;
            H = 0;
            return H;
        }
        if (fL < 0.5) fS = (maxcolor - mincolor) / (maxcolor + mincolor); else if (fL >= 0.5) fS = (maxcolor - mincolor) / (2.0f - maxcolor - mincolor);
        if (r == maxcolor) fH = (g - b) / (maxcolor - mincolor);
        if (g == maxcolor) fH = 2.0f + (b - r) / (maxcolor - mincolor);
        if (b == maxcolor) fH = 4.0f + (r - g) / (maxcolor - mincolor);
        S = (int) (fS * 100);
        H = (int) (fH * 60);
        if (H < 0) {
            H += 360;
        }
        return H;
    }

    public int getS(int x, int y) {
        int H = 0;
        int S = 0;
        int L = 0;
        float fH = 0;
        float fS = 0;
        float fL = 0;
        float r = (float) getRed(x, y) / 255;
        float g = (float) getGreen(x, y) / 255;
        float b = (float) getBlue(x, y) / 255;
        float maxcolor = r > g ? r : g;
        maxcolor = b > maxcolor ? b : maxcolor;
        float mincolor = r < g ? r : g;
        mincolor = b < mincolor ? b : mincolor;
        fL = (maxcolor + mincolor) / 2;
        L = (int) (fL * 100);
        if (maxcolor == mincolor) {
            S = 0;
            H = 0;
            return S;
        }
        if (fL < 0.5) fS = (maxcolor - mincolor) / (maxcolor + mincolor); else if (fL >= 0.5) fS = (maxcolor - mincolor) / (2.0f - maxcolor - mincolor);
        if (r == maxcolor) fH = (g - b) / (maxcolor - mincolor);
        if (g == maxcolor) fH = 2.0f + (b - r) / (maxcolor - mincolor);
        if (b == maxcolor) fH = 4.0f + (r - g) / (maxcolor - mincolor);
        S = (int) (fS * 100);
        H = (int) (fH * 60);
        if (H < 0) {
            H += 360;
        }
        return S;
    }

    public int getL(int x, int y) {
        int H = 0;
        int S = 0;
        int L = 0;
        float fH = 0;
        float fS = 0;
        float fL = 0;
        float r = (float) getRed(x, y) / 255;
        float g = (float) getGreen(x, y) / 255;
        float b = (float) getBlue(x, y) / 255;
        float maxcolor = r > g ? r : g;
        maxcolor = b > maxcolor ? b : maxcolor;
        float mincolor = r < g ? r : g;
        mincolor = b < mincolor ? b : mincolor;
        fL = (maxcolor + mincolor) / 2;
        L = (int) (fL * 100);
        return L;
    }

    public int getRed(int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        return color.getRed();
    }

    public int getGreen(int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        return color.getGreen();
    }

    public int getBlue(int x, int y) {
        Color color = new Color(image.getRGB(x, y));
        return color.getBlue();
    }

    public void setColor(int x, int y, int rgb) {
        image.setRGB(x, y, rgb);
    }

    public void setBlue(int x, int y, int blue) {
        Color color = new Color(image.getRGB(x, y));
        color = new Color(color.getRed(), color.getGreen(), blue);
        image.setRGB(x, y, color.getRGB());
    }

    public void setGreen(int x, int y, int green) {
        Color color = new Color(image.getRGB(x, y));
        color = new Color(color.getRed(), green, color.getBlue());
        image.setRGB(x, y, color.getRGB());
    }

    public void setRed(int x, int y, int red) {
        Color color = new Color(image.getRGB(x, y));
        color = new Color(red, color.getGreen(), color.getBlue());
        image.setRGB(x, y, color.getRGB());
    }

    public Image2Process() {
    }

    public Image2Process(File file) throws Exception {
        try {
            image = ImageIO.read(file);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading file " + file.getName() + " !");
            throw e;
        }
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
