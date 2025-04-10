package com.nilo.plaf.nimrod;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public class NimRODPopupMenuUI extends BasicPopupMenuUI {

    private static Robot robot = null;

    private static Kernel kernel = null;

    private BufferedImage fondo = null;

    private BufferedImage blurFondo = null;

    private MiPL mipl;

    private static final int MATRIX = 3;

    public static ComponentUI createUI(JComponent c) {
        if (robot == null) {
            try {
                robot = new Robot();
            } catch (Exception ex) {
            }
        }
        if (kernel == null) {
            float[] elements = new float[MATRIX * MATRIX];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = .1f;
            }
            int mid = MATRIX / 2 + 1;
            elements[mid * mid] = .2f;
            kernel = new Kernel(MATRIX, MATRIX, elements);
        }
        return new NimRODPopupMenuUI();
    }

    public void installDefaults() {
        super.installDefaults();
        popupMenu.setBorder(NimRODBorders.getPopupMenuBorder());
        popupMenu.setOpaque(false);
    }

    public void uninstallDefaults() {
        super.uninstallDefaults();
        LookAndFeel.installBorder(popupMenu, "PopupMenu.border");
        popupMenu.setOpaque(true);
    }

    public void installListeners() {
        super.installListeners();
        mipl = new MiPL(popupMenu);
        popupMenu.addPopupMenuListener(mipl);
    }

    public void uninstallListeners() {
        super.uninstallListeners();
        popupMenu.removePopupMenuListener(mipl);
    }

    public void update(Graphics g, JComponent c) {
        if (blurFondo != null) {
            g.drawImage(blurFondo, 0, 0, null);
        }
        if (NimRODUtils.getMenuOpacity() > 5) {
            Color cFondo = new Color(c.getBackground().getRed(), c.getBackground().getGreen(), c.getBackground().getBlue(), NimRODUtils.getMenuOpacity());
            g.setColor(cFondo);
            g.fillRect(0, 0, c.getWidth() - 4, c.getHeight() - 4);
        }
    }

    /**
   * Este metodo esta aqui solo para **MINIMIZAR** el problema de usar la clase ROBOT. Esta clase tiene ciertas
   * restricciones de seguridad (a parte de que en el JDK de alguna distro de Linux de esas que van de guays el
   * programa nativo que hace el trabajo se instala sin permisos de ejecucion) que obligan a que el jar tenga
   * que ir firmado al usarse en applets.
   * Este metodo hace la llamada a la clase robot para capturar el fondo, y si salta una excepcion (en realidad
   * cualquier cosa pues se captura Throwable), se devuelve una imagen tan transparente como se le pida. Esto
   * se cargara el efecto de blur (blurrear algo liso es liso) de los menus, pero al menos habra cierta transparencia
   * y pintara una buena sombra si no se firma el applet o se usa una distro chapucera.
   * Por cierto, si el menu se sale de la ventana tendra un fondo opaco, y por tanto no habra transparencia y la
   * sombra quedara fatal
   * @param pop
   * @param rect
   * @param transparencia
   * @return
   */
    protected BufferedImage pillaFondo(JPopupMenu pop, Rectangle rect, int transparencia) {
        BufferedImage img = null;
        try {
            img = robot.createScreenCapture(rect);
        } catch (Throwable ex) {
            img = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setColor(NimRODUtils.getColorAlfa(pop.getBackground(), transparencia));
            g.fillRect(0, 0, rect.width, rect.height);
            g.dispose();
        }
        return img;
    }

    public Popup getPopup(JPopupMenu pop, int x, int y) {
        Dimension dim = pop.getPreferredSize();
        Rectangle rect = new Rectangle(x, y, dim.width, dim.height);
        fondo = pillaFondo(pop, rect, 0);
        if (NimRODUtils.getMenuOpacity() > 250) {
            blurFondo = fondo;
        } else {
            Rectangle rectAmp = new Rectangle(x - MATRIX, y - MATRIX, dim.width + 2 * MATRIX, dim.height + 2 * MATRIX);
            BufferedImage clearFondo = pillaFondo(pop, rectAmp, NimRODUtils.getMenuOpacity());
            blurFondo = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
            BufferedImage tempFondo = clearFondo.getSubimage(0, 0, clearFondo.getWidth(), clearFondo.getHeight());
            ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
            cop.filter(clearFondo, tempFondo);
            cop.filter(tempFondo, clearFondo);
            cop.filter(clearFondo, tempFondo);
            Graphics g = blurFondo.getGraphics();
            g.drawImage(fondo, 0, 0, null);
            g.drawImage(tempFondo.getSubimage(MATRIX, MATRIX, dim.width - 5, dim.height - 5), 0, 0, null);
        }
        return super.getPopup(pop, x, y);
    }

    private class MiPL implements PopupMenuListener {

        JPopupMenu papi;

        public MiPL(JPopupMenu pop) {
            papi = pop;
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent ev) {
            if (fondo == null) {
                return;
            }
            Graphics g = papi.getRootPane().getGraphics();
            Point p = papi.getLocationOnScreen();
            Point r = papi.getRootPane().getLocationOnScreen();
            g.drawImage(fondo, p.x - r.x, p.y - r.y, null);
            fondo = null;
        }

        public void popupMenuCanceled(PopupMenuEvent ev) {
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent ev) {
        }
    }
}
