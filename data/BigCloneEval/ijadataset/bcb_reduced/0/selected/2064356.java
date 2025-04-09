package game.engine;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import game.*;

/**
 * Fenêtre de jeu
 * 
 * @author SENG Daniel
 *
 */
public class WindowBase {

    protected Core core;

    protected boolean visible, open, closing;

    protected String nom;

    protected int x, y, tHeight, width, height, backOpacity;

    protected Font windowFont;

    protected String fontFamily;

    protected int fontStyle, fontSize;

    protected Color windowColor;

    public WindowBase(int x, int y, int width, int height) {
        this.core = Core.getInstance();
        this.visible = false;
        this.x = x;
        this.y = y;
        this.tHeight = height;
        this.width = width;
        this.height = 0;
        this.backOpacity = 50;
        this.open = false;
        this.closing = false;
        this.fontFamily = Vocab.defaultFont;
        this.fontStyle = Font.CENTER_BASELINE;
        this.fontSize = Vocab.defaultSize;
        this.windowFont = new Font(fontFamily, fontStyle, fontSize);
        this.windowColor = Vocab.windowColor;
        initialize();
    }

    public void initialize() {
        nom = "Primecia";
    }

    public void update() {
        if (visible) {
            Color c = new Color(windowColor.getRed(), windowColor.getGreen(), windowColor.getBlue(), backOpacity);
            core.contents().setClip(this.x - (Vocab.WLH / 2), this.y, width + Vocab.WLH, height + (Vocab.WLH / 2));
            if (!closing) open(); else close();
            drawRect(0, 0, width + Vocab.WLH, height + Vocab.WLH, c, 0);
            core.contents().setClip(null);
        }
    }

    public void open() {
        if (height >= tHeight) {
            height = tHeight;
            open = true;
        } else {
            if (height < tHeight) {
                height += tHeight / 10;
            }
        }
    }

    public void close() {
        if (height <= 0) {
            height = 0;
            open = false;
            visible = false;
        } else {
            if (height > 0) height -= tHeight / 5;
        }
    }

    public void setClose(boolean close) {
        this.closing = close;
    }

    public boolean getVisible() {
        return visible;
    }

    public boolean mouseIn() {
        Point mouse = core.getMousePosition();
        if (mouse == null) return false;
        return (mouse.x > this.x && mouse.x < this.x + this.width && mouse.y > this.y && mouse.y < this.y + this.height + (Vocab.WLH * 2));
    }

    public void setVisible(boolean state) {
        visible = state;
    }

    public int getBackOpacity() {
        return backOpacity;
    }

    public void setBackOpacity(int v) {
        backOpacity = v;
    }

    public void drawText(Rect rect, String chaine, Color color, int position) {
        drawText(rect.getX() - (Vocab.WLH / 2), rect.getY() - (Vocab.WLH / 2), chaine, color, position);
    }

    public void drawClassicText(Rect rect, String chaine, Color color, int position) {
        drawClassicText(rect.getX() - (Vocab.WLH / 2), rect.getY() - (Vocab.WLH / 2), chaine, color, position);
    }

    public void drawText(int x, int y, String chaine, Color color, int position) {
        if (visible && chaine != null) {
            if (color == null) color = defaultColor(0);
            int semiWLH = (Vocab.WLH / 2);
            core.contents().setClip(this.x + semiWLH, this.y + semiWLH, width, height);
            loadWindowFont();
            int isX, isY, addx, addy, sizex;
            int sizey = core.contents().getFontMetrics().getHeight() / 2;
            sizex = core.contents().getFontMetrics().stringWidth(chaine);
            core.contents().setColor(new Color(0, 0, 0));
            addx = 2;
            addy = 2;
            if (position == 1) isX = this.x + (this.width - sizex - semiWLH) / 2; else if (position == 2) isX = this.x + this.width - sizex - semiWLH; else isX = this.x + x + semiWLH;
            isY = this.y + y + +semiWLH + sizey;
            core.contents().drawString(chaine, isX + addx, isY + addy);
            core.contents().drawString(chaine, isX, isY + addy);
            core.contents().setColor(color);
            core.contents().drawString(chaine, isX, isY);
            core.setDefaultFont();
            core.contents().setClip(null);
        }
    }

    public void drawClassicText(int x, int y, String chaine, Color color, int position) {
        if (visible && chaine != null) {
            if (color == null) color = defaultColor(0);
            int semiWLH = Vocab.WLH / 2;
            core.contents().setClip(this.x, this.y, width, height);
            loadWindowFont();
            int isX, isY, addx, addy, sizex;
            int sizey = core.contents().getFontMetrics().getHeight();
            sizex = core.contents().getFontMetrics().stringWidth(chaine);
            core.contents().setColor(shadowColor(color));
            addx = 2;
            addy = 2;
            if (position == 1) isX = this.x + (this.width - sizex - semiWLH) / 2; else if (position == 2) isX = this.x + this.width - sizex - semiWLH; else isX = this.x + x + semiWLH;
            isY = this.y + y + sizey;
            core.contents().setColor(shadowColor(core.contents().getColor()));
            core.contents().drawString(chaine, isX + addx, isY + addy);
            core.contents().setColor(color);
            core.contents().drawString(chaine, isX, isY);
            core.setDefaultFont();
            core.contents().setClip(null);
        }
    }

    public void drawRect(Rect rect, Color color, int position) {
        drawRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), color, position);
    }

    public void drawRect(int x, int y, int width, int height, Color color, int position) {
        if (visible) {
            core.contents().setClip(this.x, this.y, this.width + Vocab.WLH, this.height + Vocab.WLH);
            int isX, isY;
            isX = this.x + x;
            isY = this.y + y;
            if (position == 1) isX += (this.width - width) / 2;
            if (backOpacity != 0) {
                core.contents().setColor(windowColor);
                core.contents().drawRoundRect(isX + 1, isY + 1, width - 1, height - 1, 10, 10);
                core.contents().drawRoundRect(isX, isY, width, height, 10, 10);
            }
            core.contents().setColor(color);
            core.contents().fillRoundRect(isX, isY, width, height, 10, 10);
            core.contents().setClip(null);
        }
    }

    public void loadWindowFont() {
        loadWindowFont(windowFont);
    }

    public void loadWindowFont(Font font) {
        this.windowFont = font;
        core.setDefaultFont(windowFont);
    }

    public void setFontSize(int size) {
        this.fontSize = size;
        windowFont = new Font(windowFont.getFamily(), windowFont.getStyle(), size);
    }

    public void setFontStyle(int style) {
        this.fontStyle = style;
        windowFont = new Font(windowFont.getFamily(), style, windowFont.getSize());
    }

    public void setFontFamily(String family, boolean file) {
        if (file) try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, new File("Fonts/" + family));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(f);
            this.fontFamily = core.loadFont("Fonts/" + family).getFamily();
        } catch (FileNotFoundException e) {
            System.out.println("Fichier font introuvable");
            e.printStackTrace();
        } catch (FontFormatException e) {
            System.out.println("Format de font incorrect");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } else this.fontFamily = family;
        windowFont = new Font(fontFamily, windowFont.getStyle(), windowFont.getSize());
    }

    public Color lightColor() {
        int r, g, b;
        r = windowColor.getRed() + 200;
        g = windowColor.getGreen() + 200;
        b = windowColor.getBlue() + 200;
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        Color c = new Color(r, g, b);
        return c;
    }

    public Color slightColor() {
        int r, g, b;
        r = windowColor.getRed();
        g = windowColor.getGreen();
        b = windowColor.getBlue();
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        Color c = new Color(r, g, b, 150);
        return c;
    }

    public Color defaultColor(int id) {
        switch(id) {
            case 0:
                return new Color(183, 255, 255, 255);
            case 1:
                return new Color(183, 255, 185, 255);
            case 2:
                return new Color(255, 255, 183, 255);
            case 3:
                return new Color(255, 183, 183, 255);
            case 4:
                return new Color(215, 183, 255, 255);
            case 5:
                return new Color(255, 183, 253, 255);
            case 6:
                return new Color(255, 255, 255);
            case 7:
                return new Color(255, 255, 255, 150);
        }
        return new Color(0, 0, 0);
    }

    public Color itemColor(int i) {
        switch(i) {
            case 0:
                return new Color(255, 255, 255, 255);
            case 1:
                return new Color(255, 255, 135, 255);
            case 2:
                return new Color(255, 255, 0, 255);
            case 3:
                return new Color(255, 0, 0, 255);
            case 4:
                return new Color(167, 255, 135, 255);
            case 5:
                return new Color(135, 167, 255, 255);
            case 6:
                return new Color(217, 135, 255, 255);
            default:
                return new Color(255, 255, 255);
        }
    }

    public Color shadowColor(Color A) {
        int r, g, b, a;
        r = A.getRed() - 150;
        g = A.getGreen() - 150;
        b = A.getBlue() - 150;
        a = A.getAlpha();
        if (r < 0) r = 0;
        if (g < 0) g = 0;
        if (b < 0) b = 0;
        Color c = new Color(r, g, b, a);
        return c;
    }

    public Color disabledColor() {
        int r, g, b, a;
        r = windowColor.getRed();
        g = windowColor.getGreen();
        b = windowColor.getBlue();
        a = windowColor.getAlpha() - 200;
        if (a < 0) a = 0;
        Color c = new Color(r, g, b, a);
        return c;
    }

    public Color selectColor() {
        int r, g, b, a;
        r = windowColor.getRed() + 200;
        g = windowColor.getGreen() + 200;
        b = windowColor.getBlue() + 200;
        a = 80;
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        Color c = new Color(r, g, b, a);
        return c;
    }

    public String getNom() {
        return nom;
    }

    public boolean input(int e) {
        return core.getCurrKey(e);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.x = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
