package powd;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.Cursor;

public class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener, Serializable {

    protected static final int LINE = 1, FREE_HAND = 6, ARROW = 2;

    protected static Vector vLine, vFreeHand, vArrow;

    private Color foreGroundColor, backGroundColor;

    private int alpha;

    private BasicStroke stroke;

    private Cursor c;

    private Image background;

    private Image imageBackground;

    private int x1, y1, x2, y2, linex1, linex2, liney1, liney2, drawMode = 0;

    public Properties properties;

    public CanvasPanel() {
        vLine = new Vector();
        vFreeHand = new Vector();
        vArrow = new Vector();
        properties = new Properties();
        loadProperties("config.properties");
        setStroke(6);
        alpha = 200;
        setForeGroundColor(new Color(200, 0, 0));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        addMouseListener(this);
        addMouseMotionListener(this);
        repaint();
    }

    public void mousePressed(MouseEvent event) {
        x1 = linex1 = linex2 = event.getX();
        y1 = liney1 = liney2 = event.getY();
    }

    public void captureImageBackground(GraphicsDevice grafica) {
        try {
            Robot rbt = new Robot();
            Rectangle bounds = grafica.getDefaultConfiguration().getBounds();
            imageBackground = rbt.createScreenCapture(bounds);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateBackground() {
        String propBackground = properties.getProperty("background");
        if (propBackground == null) propBackground = new String("image");
        if (propBackground.equalsIgnoreCase("image")) background = imageBackground; else if (propBackground.equalsIgnoreCase("color")) {
            background = null;
        } else {
            background = imageBackground;
        }
    }

    public void loadBackground() {
        String propBackground = properties.getProperty("background");
        if (propBackground == null) propBackground = new String("image");
        if (propBackground.equalsIgnoreCase("image")) background = imageBackground; else if (propBackground.equalsIgnoreCase("color")) {
            String propBackgroundColor = properties.getProperty("background-color");
            if (propBackgroundColor == null) propBackgroundColor = new String("0,0,0");
            String[] rgb = propBackgroundColor.split(",");
            Color bgColor = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
            background = null;
            this.setBackGroundColor(bgColor);
        } else {
            background = imageBackground;
        }
    }

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseMoved(MouseEvent event) {
    }

    public void mouseReleased(final MouseEvent event) {
        if (drawMode == LINE) {
            if (!(linex1 == 0 && liney1 == 0)) vLine.add(new Coordinate(x1, y1, event.getX(), event.getY(), foreGroundColor, stroke));
        }
        if (drawMode == ARROW) {
            if (!(linex1 == 0 && liney1 == 0)) vArrow.add(new Coordinate(x1, y1, event.getX(), event.getY(), foreGroundColor, stroke));
        }
        x1 = linex1 = x2 = linex2 = 0;
        y1 = liney1 = y2 = liney2 = 0;
    }

    public void mouseDragged(MouseEvent event) {
        x2 = event.getX();
        y2 = event.getY();
        if (drawMode == this.FREE_HAND) {
            linex1 = linex2;
            liney1 = liney2;
            linex2 = x2;
            liney2 = y2;
            if (!(linex1 == 0 && liney1 == 0)) vFreeHand.add(new Coordinate(linex1, liney1, linex2, liney2, foreGroundColor, stroke));
        }
        repaint();
    }

    public void mouseEntered(MouseEvent event) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void mouseExited(MouseEvent event) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);
        redrawVectorBuffer(g);
        g.setColor(foreGroundColor);
        g2.setStroke(stroke);
        if (drawMode == LINE) {
            g2.drawLine(x1, y1, x2, y2);
        }
        if (drawMode == ARROW) {
            paintArrow(g2, x1, y1, x2, y2);
        }
        if (drawMode == FREE_HAND) {
            g2.drawLine(linex1, liney1, linex2, liney2);
        }
    }

    public void setDrawMode(int mode) {
        drawMode = mode;
    }

    public int getDrawMode() {
        return drawMode;
    }

    public void setAlpha(int inalpha) {
        alpha = inalpha;
    }

    public void setForeGroundColor(Color inputColor) {
        foreGroundColor = new Color(inputColor.getRed(), inputColor.getGreen(), inputColor.getBlue(), alpha);
    }

    public void setStroke(BasicStroke s) {
        stroke = s;
    }

    public void setStroke(float s) {
        stroke = new BasicStroke(s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
    }

    public BasicStroke getStroke() {
        return stroke;
    }

    public Color getForeGroundColor() {
        return foreGroundColor;
    }

    public void setBackGroundColor(Color inputColor) {
        backGroundColor = inputColor;
        this.setBackground(backGroundColor);
    }

    public Color getBackGroundColor() {
        return backGroundColor;
    }

    public void clearCanvas() {
        vFreeHand.removeAllElements();
        vLine.removeAllElements();
        vArrow.removeAllElements();
        repaint();
    }

    private class Coordinate implements Serializable {

        private int x1, y1, x2, y2;

        private Color foreColor;

        private BasicStroke stroke;

        public Coordinate(int inx1, int iny1, int inx2, int iny2, Color color, BasicStroke instroke) {
            x1 = inx1;
            y1 = iny1;
            x2 = inx2;
            y2 = iny2;
            foreColor = color;
            stroke = instroke;
        }

        public Color colour() {
            return foreColor;
        }

        public BasicStroke getStroke() {
            return stroke;
        }

        public int getX1() {
            return x1;
        }

        public int getX2() {
            return x2;
        }

        public int getY1() {
            return y1;
        }

        public int getY2() {
            return y2;
        }
    }

    private void redrawVectorBuffer(Graphics g) {
        Coordinate co;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Point pos = new Point(0, 0);
        Point offset = new Point(-pos.x, -pos.y);
        g.drawImage(background, offset.x, offset.y, null);
        int tam = vFreeHand.size();
        for (int i = 0; i < tam; i++) {
            co = (Coordinate) vFreeHand.elementAt(i);
            g.setColor(co.colour());
            g2.setStroke(co.getStroke());
            g2.drawLine(co.getX1(), co.getY1(), co.getX2(), co.getY2());
        }
        tam = vLine.size();
        for (int i = 0; i < tam; i++) {
            g.setColor(((Coordinate) vLine.elementAt(i)).colour());
            g2.setStroke(((Coordinate) vLine.elementAt(i)).getStroke());
            g2.drawLine(((Coordinate) vLine.elementAt(i)).getX1(), ((Coordinate) vLine.elementAt(i)).getY1(), ((Coordinate) vLine.elementAt(i)).getX2(), ((Coordinate) vLine.elementAt(i)).getY2());
        }
        tam = vArrow.size();
        for (int i = 0; i < tam; i++) {
            g.setColor(((Coordinate) vArrow.elementAt(i)).colour());
            g2.setStroke(((Coordinate) vArrow.elementAt(i)).getStroke());
            paintArrow(g2, ((Coordinate) vArrow.elementAt(i)).getX1(), ((Coordinate) vArrow.elementAt(i)).getY1(), ((Coordinate) vArrow.elementAt(i)).getX2(), ((Coordinate) vArrow.elementAt(i)).getY2());
        }
    }

    public void paintArrow(Graphics2D g2, int x0, int y0, int x1, int y1) {
        int deltaX = x1 - x0;
        int deltaY = y1 - y0;
        double frac = 0.08;
        g2.drawLine(x0, y0, x1, y1);
        g2.drawLine(x0 + (int) ((1 - frac) * deltaX + frac * deltaY), y0 + (int) ((1 - frac) * deltaY - frac * deltaX), x1, y1);
        g2.drawLine(x0 + (int) ((1 - frac) * deltaX - frac * deltaY), y0 + (int) ((1 - frac) * deltaY + frac * deltaX), x1, y1);
    }

    public int getPosMouseX() {
        return x1;
    }

    public int getPosMouseY() {
        return y1;
    }

    private void loadProperties(String file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            if (is != null) properties.load(is);
        } catch (IOException ioe) {
        }
    }

    public int getMonitorIndex() {
        int monitor;
        String prop;
        prop = properties.getProperty("monitor");
        if (prop != null) monitor = Integer.parseInt(prop); else monitor = 0;
        return monitor;
    }

    @Override
    public Image createImage(int width, int height) {
        BufferedImage capture = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_BGR);
        Graphics graphics = capture.getGraphics();
        graphics.setColor(this.getBackGroundColor());
        graphics.fillRect(0, 0, width, height);
        redrawVectorBuffer(graphics);
        return capture;
    }
}
