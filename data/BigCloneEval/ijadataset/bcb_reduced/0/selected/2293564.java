package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;

class NimRODUtils {

    protected static Color rollColor;

    static final int THIN = 0;

    static final int FAT = 1;

    static final int MATRIX_FAT = 5;

    static Kernel kernelFat;

    static final int MATRIX_THIN = 3;

    static Kernel kernelThin;

    static Color getSombra() {
        return getColorAlfa(getColorTercio(NimRODLookAndFeel.getControlDarkShadow(), Color.black), 64);
    }

    static Color getBrillo() {
        return getColorAlfa(getColorTercio(NimRODLookAndFeel.getControlHighlight(), Color.white), 64);
    }

    static Color getSombraMenu() {
        return new Color(20, 20, 20, 50);
    }

    static Color getBrilloMenu() {
        return new Color(255, 255, 255, 64);
    }

    /**
   * Esta funcion se usa para inicializar los colores del tema segun los argumentos que se le pasen.
   */
    static NimRODTheme iniCustomColors(NimRODTheme nt, String selection, String background, String p1, String p2, String p3, String s1, String s2, String s3, String w, String b, String opMenu, String opFrame) {
        if (selection != null) {
            nt.setPrimary(Color.decode(selection));
        }
        if (background != null) {
            nt.setSecondary(Color.decode(background));
        }
        if (p1 != null) {
            nt.setPrimary1(Color.decode(p1));
        }
        if (p2 != null) {
            nt.setPrimary2(Color.decode(p2));
        }
        if (p3 != null) {
            nt.setPrimary3(Color.decode(p3));
        }
        if (s1 != null) {
            nt.setSecondary1(Color.decode(s1));
        }
        if (s2 != null) {
            nt.setSecondary2(Color.decode(s2));
        }
        if (s3 != null) {
            nt.setSecondary3(Color.decode(s3));
        }
        if (w != null) {
            nt.setWhite(Color.decode(w));
        }
        if (b != null) {
            nt.setBlack(Color.decode(b));
        }
        if (opMenu != null) {
            nt.setMenuOpacity(Integer.parseInt(opMenu));
        }
        if (opFrame != null) {
            nt.setFrameOpacity(Integer.parseInt(opFrame));
        }
        return nt;
    }

    static ImageIcon loadRes(String fich) {
        try {
            return new ImageIcon(Toolkit.getDefaultToolkit().createImage(readStream(NimRODLookAndFeel.class.getResourceAsStream(fich))));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("No se puede cargar el recurso " + fich);
            return null;
        }
    }

    static byte[] readStream(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int read;
        byte[] buffer = new byte[256];
        while ((read = input.read(buffer, 0, 256)) != -1) {
            bytes.write(buffer, 0, read);
        }
        return bytes.toByteArray();
    }

    /**
   * Esta funcion se usa para pintar la barra de seleccion de los menus. Esta aqui
   * para no repetirla en todas partes...
   */
    static void pintaBarraMenu(Graphics g, JMenuItem menuItem, Color bgColor) {
        ButtonModel model = menuItem.getModel();
        Color oldColor = g.getColor();
        int menuWidth = menuItem.getWidth();
        int menuHeight = menuItem.getHeight();
        if (menuItem.isOpaque()) {
            g.setColor(menuItem.getBackground());
            g.fillRect(0, 0, menuWidth, menuHeight);
        }
        if ((menuItem instanceof JMenu && !(((JMenu) menuItem).isTopLevelMenu()) && model.isSelected()) || model.isArmed()) {
            RoundRectangle2D.Float boton = new RoundRectangle2D.Float();
            boton.x = 1;
            boton.y = 0;
            boton.width = menuWidth - 3;
            boton.height = menuHeight - 1;
            boton.arcwidth = 8;
            boton.archeight = 8;
            GradientPaint grad = new GradientPaint(1, 1, getBrilloMenu(), 0, menuHeight, getSombraMenu());
            Graphics2D g2D = (Graphics2D) g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(bgColor);
            g2D.fill(boton);
            g.setColor(bgColor.darker());
            g2D.draw(boton);
            g2D.setPaint(grad);
            g2D.fill(boton);
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        }
        g.setColor(oldColor);
    }

    static void paintFocus(Graphics g, int x, int y, int width, int height, int r1, int r2, Color color) {
        paintFocus(g, x, y, width, height, r1, r2, 2.0f, color);
    }

    static void paintFocus(Graphics g, int x, int y, int width, int height, int r1, int r2, float grosor, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(grosor));
        if (r1 == 0 && r2 == 0) {
            g.drawRect(x, y, width, height);
        } else {
            g.drawRoundRect(x, y, width - 1, height - 1, r1, r2);
        }
        g2d.setStroke(oldStroke);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
    }

    static Color getRolloverColor() {
        if (rollColor == null) {
            rollColor = getColorAlfa(UIManager.getColor("Button.focus"), 40);
        }
        return rollColor;
    }

    static Color getColorAlfa(Color col, int alfa) {
        return new Color(col.getRed(), col.getGreen(), col.getBlue(), alfa);
    }

    static Color getColorMedio(Color a, Color b) {
        return new Color(propInt(a.getRed(), b.getRed(), 2), propInt(a.getGreen(), b.getGreen(), 2), propInt(a.getBlue(), b.getBlue(), 2));
    }

    static ColorUIResource getColorTercio(Color a, Color b) {
        return new ColorUIResource(propInt(a.getRed(), b.getRed(), 3), propInt(a.getGreen(), b.getGreen(), 3), propInt(a.getBlue(), b.getBlue(), 3));
    }

    private static int propInt(int a, int b, int prop) {
        return b + ((a - b) / prop);
    }

    static void paintShadowTitleFat(Graphics g, String title, int x, int y, Color frente) {
        paintShadowTitle(g, title, x, y, frente, Color.black, 1, FAT, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleFat(Graphics g, String title, int x, int y, Color frente, Color shadow) {
        paintShadowTitle(g, title, x, y, frente, shadow, 1, FAT, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleFat(Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
        paintShadowTitle(g, title, x, y, frente, shadow, desp, FAT, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleThin(Graphics g, String title, int x, int y, Color frente) {
        paintShadowTitle(g, title, x, y, frente, Color.black, 1, THIN, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleThin(Graphics g, String title, int x, int y, Color frente, Color shadow) {
        paintShadowTitle(g, title, x, y, frente, shadow, 1, THIN, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleThin(Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
        paintShadowTitle(g, title, x, y, frente, shadow, desp, THIN, SwingConstants.HORIZONTAL);
    }

    static void paintShadowTitleFatV(Graphics g, String title, int x, int y, Color frente) {
        paintShadowTitle(g, title, x, y, frente, Color.black, 1, FAT, SwingConstants.VERTICAL);
    }

    static void paintShadowTitleFatV(Graphics g, String title, int x, int y, Color frente, Color shadow) {
        paintShadowTitle(g, title, x, y, frente, shadow, 1, FAT, SwingConstants.VERTICAL);
    }

    static void paintShadowTitleFatV(Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
        paintShadowTitle(g, title, x, y, frente, shadow, desp, FAT, SwingConstants.VERTICAL);
    }

    static void paintShadowTitleThinV(Graphics g, String title, int x, int y, Color frente) {
        paintShadowTitle(g, title, x, y, frente, Color.black, 1, THIN, SwingConstants.VERTICAL);
    }

    static void paintShadowTitleThinV(Graphics g, String title, int x, int y, Color frente, Color shadow) {
        paintShadowTitle(g, title, x, y, frente, shadow, 1, THIN, SwingConstants.VERTICAL);
    }

    static void paintShadowTitleThinV(Graphics g, String title, int x, int y, Color frente, Color shadow, int desp) {
        paintShadowTitle(g, title, x, y, frente, shadow, desp, THIN, SwingConstants.VERTICAL);
    }

    static void paintShadowTitle(Graphics g, String title, int x, int y, Color frente, Color shadow, int desp, int tipo, int orientation) {
        Font f = g.getFont();
        if (orientation == SwingConstants.VERTICAL) {
            AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2);
            f = f.deriveFont(rotate);
        }
        if (shadow != null) {
            int matrix = (tipo == THIN ? MATRIX_THIN : MATRIX_FAT);
            Rectangle2D rect = g.getFontMetrics().getStringBounds(title, g);
            int w, h;
            if (orientation == SwingConstants.HORIZONTAL) {
                w = (int) rect.getWidth() + 6 * matrix;
                h = (int) rect.getHeight() + 6 * matrix;
            } else {
                h = (int) rect.getWidth() + 6 * matrix;
                w = (int) rect.getHeight() + 6 * matrix;
            }
            BufferedImage iTitulo = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            BufferedImage iSombra = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = iTitulo.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(f);
            g2.setColor(shadow);
            g2.drawString(title, 3 * matrix, 3 * matrix);
            ConvolveOp cop = new ConvolveOp((tipo == THIN ? kernelThin : kernelFat), ConvolveOp.EDGE_NO_OP, null);
            cop.filter(iTitulo, iSombra);
            g.drawImage(iSombra, x - 3 * matrix + desp, y - 3 * matrix + desp, null);
        }
        if (frente != null) {
            g.setFont(f);
            g.setColor(frente);
            g.drawString(title, x, y);
        }
    }

    static Icon reescala(Icon ic, int maxW, int maxH) {
        if (ic == null) {
            return null;
        }
        if (ic.getIconHeight() == maxH && ic.getIconWidth() == maxW) {
            return ic;
        }
        BufferedImage bi = new BufferedImage(ic.getIconHeight(), ic.getIconWidth(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        ic.paintIcon(null, g, 0, 0);
        g.dispose();
        Image bf = bi.getScaledInstance(maxW, maxH, Image.SCALE_SMOOTH);
        return new ImageIcon(bf);
    }

    static int getOpacity() {
        return getMenuOpacity();
    }

    static int getMenuOpacity() {
        try {
            NimRODTheme th = (NimRODTheme) NimRODLookAndFeel.theme;
            return th.getMenuOpacity();
        } catch (Throwable ex) {
            return NimRODTheme.DEFAULT_MENU_OPACITY;
        }
    }

    static float getMenuOpacityFloat() {
        return getMenuOpacity() / 255f;
    }

    static int getFrameOpacity() {
        try {
            NimRODTheme th = (NimRODTheme) NimRODLookAndFeel.theme;
            return th.getFrameOpacity();
        } catch (Throwable ex) {
            return NimRODTheme.DEFAULT_FRAME_OPACITY;
        }
    }

    static float getFrameOpacityFloat() {
        return getFrameOpacity() / 255f;
    }
}
