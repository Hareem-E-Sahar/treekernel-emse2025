package net.walkingtools.j2se.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import net.walkingtools.Utilities;
import net.walkingtools.gpsTypes.*;
import net.walkingtools.gpsTypes.hiperGps.*;
import net.walkingtools.international.*;
import net.walkingtools.*;
import net.walkingtools.server.*;

/**
 * HiperMapPanel can display the contents of a GpsTypeContainer
 * @author Brett Stalbaum
 * @version 0.1.1
 * @since 0.0.4
 */
public class HiperMapPanel extends JPanel implements MouseMotionListener, MouseListener {

    /**
     * serialVersionUID for Serializable
     */
    public static final long serialVersionUID = 0x0a80be80;

    private Image logo = null;

    private GpsTypeContainer container = null;

    private Coordinates[] coords = null;

    int w = -1;

    int h = -1;

    int x_diff_saved = 0;

    int y_diff_saved = 0;

    int x_diff = 0;

    int y_diff = 0;

    int x_start = 0;

    int y_start = 0;

    private double centerLat = -1;

    private double centerLon = -1;

    private double metersPerPixel = 5;

    private static final int[] presetScales = { 50, 80, 100, 200, 500, 1000, 2000, 4000, 8000, 10000, 20000 };

    private int scale = 0;

    private Font f = null;

    private String scaleString = null;

    private Translation translation = null;

    private Color textColor = null;

    private Color radiusColor = null;

    private Color wptColor = null;

    private Color scaleColor = null;

    private Color backgroundColor = null;

    public HiperMapPanel() {
        translation = new English();
        w = getWidth();
        h = getHeight();
        f = new Font("Dialog", Font.BOLD, 12);
        backgroundColor = Color.BLACK;
        setBackground(backgroundColor);
        setEnabled(true);
        logo = Utilities.loadImage("res/img/default_fb500dd0-5b81-11de-8a39-0800200c9a66.png", this);
        resetScale();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /**
     * Creates a HiperMapPanel
     * @param translation the initial translation object for this object
     */
    public HiperMapPanel(Translation translation) {
        this.translation = translation;
        setEnabled(true);
        w = getWidth();
        h = getHeight();
        f = new Font("Dialog", Font.BOLD, 12);
        backgroundColor = Color.BLACK;
        setBackground(backgroundColor);
        logo = Utilities.loadImage("res/img/default_fb500dd0-5b81-11de-8a39-0800200c9a66.png", this);
        resetScale();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /**
     * Sets the current waypoint container that will be drawn in the display
     * @param container the WaypointContainer to draw
     */
    public void setWayPointContainer(GpsTypeContainer container) {
        this.container = container;
        coords = container.getCoordinates();
    }

    /**
     * Sets the translation
     * @param translation
     */
    public void setTranslation(Translation translation) {
        this.translation = translation;
        repaint();
    }

    /**
     * Resets the view, making all data visible on the canvas and resizing to fit
     */
    public void resetView() {
        int latish = (int) Math.abs((container.getMinlat() - container.getMaxlat()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int longish = (int) Math.abs((container.getMinlon() - container.getMaxlon()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int largest = (latish > longish) ? latish : longish;
        for (int i = 0; i < presetScales.length - 1; i++) {
            scale = i + 1;
            if (presetScales[i] >= largest) {
                break;
            }
        }
        x_diff_saved = 0;
        y_diff_saved = 0;
        x_diff = 0;
        y_diff = 0;
        calculateCenter();
        resetScale();
        repaint();
    }

    /**
     * Determines if all of the data can be shown on the Panel at the present size and resoluton
     */
    private boolean dataFitsOnPanel() {
        if (container == null) {
            return true;
        }
        int latish = (int) Math.abs((container.getMinlat() - container.getMaxlat()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int longish = (int) Math.abs((container.getMinlon() - container.getMaxlon()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int largest = (latish > longish) ? latish : longish;
        if (largest < presetScales[scale]) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets the view, no rescaling
     */
    public void resetViewNoScale() {
        int latish = (int) Math.abs((container.getMinlat() - container.getMaxlat()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int longish = (int) Math.abs((container.getMinlon() - container.getMaxlon()) / WalkingtoolsInformation.GEOGRAPHICMETER);
        int largest = (latish > longish) ? latish : longish;
        for (int i = 0; i < presetScales.length - 1; i++) {
            scale = i + 1;
            if (presetScales[i] >= largest) {
                break;
            }
        }
        repaint();
    }

    /**
     * Resets the scale
     */
    private void resetScale() {
        if (presetScales[scale] >= 1000) {
            scaleString = translation.translate("Scale: ") + presetScales[scale] / 1000 + "K";
        } else {
            scaleString = translation.translate("Scale: ") + presetScales[scale] + "M";
        }
        if (!dataFitsOnPanel()) {
            scaleString += translation.translate(" (Data Off Scale)");
        }
        metersPerPixel = ((float) presetScales[scale]) / w;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            radiusColor = Color.DARK_GRAY;
            wptColor = Color.ORANGE;
            scaleColor = Color.YELLOW;
            textColor = Color.CYAN;
        } else {
            radiusColor = Color.BLACK;
            wptColor = Color.GRAY;
            scaleColor = Color.GRAY;
            textColor = Color.LIGHT_GRAY;
        }
    }

    @Override
    public void paint(Graphics g) {
        g.setFont(f);
        w = getWidth();
        h = getHeight();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(textColor);
        if (container != null) {
            g.translate((w / 2) - x_diff, (h / 2) - y_diff);
            if (container.getGpsType() == GpsTypeContainer.WAYPOINTS) {
                for (int i = 0; i < container.getSize(); i++) {
                    HiperWaypoint wpt = (HiperWaypoint) coords[i];
                    int r = wpt.getRadius();
                    int mpp = (int) metersPerPixel;
                    Dimension pts = processCoordinates(wpt.getLatitude(), wpt.getLongitude());
                    int x = pts.width;
                    int y = pts.height;
                    g.setColor(radiusColor);
                    if (mpp == 0) {
                        g.fillArc(x - (r * 2), y - (r * 2), r * 4, r * 4, 0, 360);
                    } else if (mpp == 1) {
                        g.fillArc(x - r, y - r, r * 2, r * 2, 0, 360);
                    } else {
                        g.fillArc(x - r / mpp, (y - r / mpp), ((r * 2) / mpp), ((r * 2) / mpp), 0, 360);
                    }
                }
                for (int i = 0; i < container.getSize(); i++) {
                    HiperWaypoint wpt = (HiperWaypoint) coords[i];
                    int r = wpt.getRadius();
                    int mpp = (int) metersPerPixel;
                    Dimension pts = processCoordinates(wpt.getLatitude(), wpt.getLongitude());
                    int x = pts.width;
                    int y = pts.height;
                    g.setColor(textColor);
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x, y - 4);
                    triangle.addPoint(x + 4, y + 4);
                    triangle.addPoint(x - 4, y + 4);
                    g.fillPolygon(triangle);
                    g.drawString((wpt).getName(), x + 4, y);
                }
            } else if (container.getGpsType() == GpsTypeContainer.ROUTE) {
                int old_x = -1000000000;
                int old_y = -1000000000;
                for (int i = 0; i < container.getSize(); i++) {
                    HiperWaypoint wpt = (HiperWaypoint) coords[i];
                    int r = wpt.getRadius();
                    int mpp = (int) metersPerPixel;
                    Dimension pts = processCoordinates(wpt.getLatitude(), wpt.getLongitude());
                    int x = pts.width;
                    int y = pts.height;
                    g.setColor(radiusColor);
                    if (mpp == 0) {
                        g.fillArc(x - (r * 2), y - (r * 2), r * 4, r * 4, 0, 360);
                    } else if (mpp == 1) {
                        g.fillArc(x - r, y - r, r * 2, r * 2, 0, 360);
                    } else {
                        g.fillArc(x - r / mpp, (y - r / mpp), ((r * 2) / mpp), ((r * 2) / mpp), 0, 360);
                    }
                    g.setColor(wptColor);
                    if (i > 0) {
                        g.drawLine(x, y, old_x, old_y);
                    }
                    old_x = x;
                    old_y = y;
                }
                for (int i = 0; i < container.getSize(); i++) {
                    HiperWaypoint wpt = (HiperWaypoint) coords[i];
                    int r = wpt.getRadius();
                    int mpp = (int) metersPerPixel;
                    Dimension pts = processCoordinates(wpt.getLatitude(), wpt.getLongitude());
                    int x = pts.width;
                    int y = pts.height;
                    g.setColor(textColor);
                    Polygon triangle = new Polygon();
                    triangle.addPoint(x, y - 4);
                    triangle.addPoint(x + 4, y + 4);
                    triangle.addPoint(x - 4, y + 4);
                    g.fillPolygon(triangle);
                    g.drawString((i + 1) + ": " + (wpt).getName(), x + 4, y);
                }
            } else if (container.getGpsType() == GpsTypeContainer.TRACKLOG) {
                g.drawString("TRACKLOG not supported yet", w / 2 - (logo.getWidth(this) / 2), h / 2 - (logo.getHeight(this) / 2));
            } else {
                g.drawString("EMPTY", w / 2 - (logo.getWidth(this) / 2), h / 2 - (logo.getHeight(this) / 2));
            }
            g.setColor(scaleColor);
            g.drawString(scaleString, -(w / 2) + 3 + x_diff, -(h / 2) + 15 + y_diff);
        } else {
            FontMetrics fm = g.getFontMetrics(f);
            String message = "CALIT2";
            g.drawString(message, w / 2 - (fm.stringWidth(message) / 2), h / 15 + (fm.getHeight()));
            message = "CRCA";
            g.drawString(message, w / 2 - (fm.stringWidth(message) / 2), h / 15 + (fm.getHeight() * 2));
            message = "File Labo";
            g.drawString(message, w / 2 - (fm.stringWidth(message) / 2), h / 15 + (fm.getHeight() * 3));
            message = "UCSD Visual Arts";
            g.drawString(message, w / 2 - (fm.stringWidth(message) / 2), h / 15 + (fm.getHeight() * 4));
            g.drawImage(logo, w / 2 - (logo.getWidth(this) / 2), h / 2 - (logo.getHeight(this) / 2), this);
        }
    }

    /**
     * Clears the panel
     */
    public void clear() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        container = null;
        repaint();
    }

    private void calculateCenter() {
        if (container == null) {
            return;
        }
        double min = container.getMinlat();
        double max = container.getMaxlat();
        centerLat = (min + max) / 2;
        min = container.getMinlon();
        max = container.getMaxlon();
        centerLon = (min + max) / 2;
    }

    private Dimension processCoordinates(double lon, double lat) {
        int x = (int) (((lat - centerLon) / WalkingtoolsInformation.GEOGRAPHICMETER) / metersPerPixel);
        int y = -(int) (((lon - centerLat) / WalkingtoolsInformation.GEOGRAPHICMETER) / metersPerPixel);
        return new Dimension(x, y);
    }

    /** specify the meters per pixel to display
     * @param metersPerPixel must be a positive, non-zero number
     */
    public void setScale(float metersPerPixel) {
        if (metersPerPixel <= 0) {
            return;
        }
        this.metersPerPixel = metersPerPixel;
    }

    /**
     * Zooms out (displays smaller scale)
     */
    public void zoomOut() {
        if ((scale < (presetScales.length - 1))) {
            scale++;
        }
        metersPerPixel = ((float) presetScales[scale]) / w;
        resetScale();
        repaint();
    }

    /**
     * Zooms in (displays larger scale)
     */
    public void zoomIn() {
        if (scale > 0) {
            scale--;
        }
        metersPerPixel = ((float) presetScales[scale]) / w;
        resetScale();
        repaint();
    }

    public void mouseDragged(MouseEvent evt) {
        int x = evt.getPoint().x;
        int y = evt.getPoint().y;
        x_diff = +x_diff_saved + x_start - x;
        y_diff = y_diff_saved + y_start - y;
        repaint();
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mousePressed(MouseEvent evt) {
        x_start = evt.getPoint().x;
        y_start = evt.getPoint().y;
    }

    public void mouseReleased(MouseEvent evt) {
        x_diff_saved = x_diff;
        y_diff_saved = y_diff;
        x_start = 0;
        y_start = 0;
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }
}
