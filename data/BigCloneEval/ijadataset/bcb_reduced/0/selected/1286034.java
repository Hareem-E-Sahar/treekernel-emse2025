package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.vehicle.Vehicle;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** The MapDisplay class is the visual component for the surface map
 *  of Mars in the UI. It can show either the surface or topographical
 *  maps at a given point. It maintains two Map objects; one for the
 *  topographical map image, and one for the surface map image.
 *
 *  It will recenter the map on the location of a mouse click, or open
 *  a vehicle/settlement window if one of their icons is clicked.
 */
public class MapDisplay extends JComponent implements MouseListener, Runnable {

    private Mars mars;

    private UIProxyManager proxyManager;

    private NavigatorWindow navWindow;

    private Map surfMap;

    private Map usgsMap;

    private Map topoMap;

    private boolean wait;

    private Coordinates centerCoords;

    private Thread showThread;

    private boolean topo;

    private boolean recreate;

    private boolean labels;

    private Image mapImage;

    private boolean useUSGSMap;

    private int[] shadingArray;

    private boolean showDayNightShading;

    private boolean showVehicleTrails;

    private int width;

    private int height;

    private static final double HALF_PI = (Math.PI / 2D);

    private static final int HALF_MAP = 150;

    private static final double HALF_MAP_ANGLE_STANDARD = .48587D;

    private static final double HALF_MAP_ANGLE_USGS = .06106D;

    /** Constructs a MapDisplay object
     *  @param navWindow the navigator window pane
     *  @param proxyManager the UI proxy manager
     *  @param width the width of the map shown
     *  @param height the height of the map shown
     */
    public MapDisplay(NavigatorWindow navWindow, UIProxyManager proxyManager, int width, int height, Mars mars) {
        this.navWindow = navWindow;
        this.proxyManager = proxyManager;
        this.width = width;
        this.height = height;
        this.mars = mars;
        wait = false;
        recreate = true;
        topo = false;
        labels = true;
        centerCoords = new Coordinates(HALF_PI, 0D);
        shadingArray = new int[width * height];
        showDayNightShading = false;
        showVehicleTrails = true;
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        setBackground(Color.black);
        addMouseListener(this);
        topoMap = new TopoMarsMap(this);
        surfMap = new SurfMarsMap(this);
        usgsMap = new USGSMarsMap(this);
        useUSGSMap = false;
        showSurf();
    }

    /** Set USGS as surface map
     *  @param useUSGSMap true if using USGS map.
     */
    public void setUSGSMap(boolean useUSGSMap) {
        if (!topo && (this.useUSGSMap != useUSGSMap)) recreate = true;
        this.useUSGSMap = useUSGSMap;
    }

    /** Change label display flag
     *  @param labels true if labels are to be displayed
     */
    public void setLabels(boolean labels) {
        this.labels = labels;
    }

    /** Display real surface image */
    public void showSurf() {
        if (topo) {
            wait = true;
            recreate = true;
        }
        topo = false;
        showMap(centerCoords);
    }

    /** Display topographical map */
    public void showTopo() {
        if (!topo) {
            wait = true;
            recreate = true;
        }
        topo = true;
        showMap(centerCoords);
    }

    /** Display surface with new coords, regenerating image if necessary
     *  @param newCenter new center location for map
     */
    public void showMap(Coordinates newCenter) {
        if (!centerCoords.equals(newCenter)) {
            wait = true;
            recreate = true;
            centerCoords.setCoords(newCenter);
        }
        updateDisplay();
    }

    /** updates the current display */
    private void updateDisplay() {
        if ((showThread == null) || (!showThread.isAlive())) {
            showThread = new Thread(this, "Map");
            showThread.start();
        } else {
            showThread.interrupt();
        }
    }

    /** the run method for the runnable interface */
    public void run() {
        refreshLoop();
    }

    /** loop, refreshing the display when necessary */
    private void refreshLoop() {
        while (true) {
            if (recreate) {
                if (topo) topoMap.drawMap(centerCoords); else {
                    if (useUSGSMap) usgsMap.drawMap(centerCoords); else surfMap.drawMap(centerCoords);
                }
                recreate = false;
                repaint();
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                repaint();
            }
        }
    }

    /** Overrides paintComponent method.  Displays map image or
     *  "Preparing Map..." message.
     *  @param g graphics context
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (wait) {
            if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
            if (topo) g.setColor(Color.black); else g.setColor(Color.green);
            String message = "Generating Map";
            if (useUSGSMap) message = "Downloading Map";
            Font messageFont = new Font("SansSerif", Font.BOLD, 25);
            FontMetrics messageMetrics = getFontMetrics(messageFont);
            int msgHeight = messageMetrics.getHeight();
            int msgWidth = messageMetrics.stringWidth(message);
            int x = (width - msgWidth) / 2;
            int y = (height + msgHeight) / 2;
            g.setFont(messageFont);
            g.drawString(message, x, y);
            wait = false;
        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, width, height);
            Map map = null;
            if (topo) map = topoMap; else {
                if (useUSGSMap) map = usgsMap; else map = surfMap;
            }
            if (map.isImageDone()) {
                mapImage = map.getMapImage();
                g.drawImage(mapImage, 0, 0, this);
            }
            if (!topo && showDayNightShading) drawShading(g);
            if (showVehicleTrails) drawVehicleTrails(g);
            drawUnits(g);
        }
    }

    /** Draws the day/night shading on the map.
     *  @param g graphics context
     */
    protected void drawShading(Graphics g) {
        int centerX = width / 2;
        int centerY = width / 2;
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double rho = 1440D / Math.PI;
        if (useUSGSMap) rho = 11458D / Math.PI;
        boolean nightTime = true;
        boolean dayTime = true;
        Coordinates location = new Coordinates(0D, 0D);
        for (int x = 0; x < width; x += 2) {
            for (int y = 0; y < height; y += 2) {
                centerCoords.convertRectToSpherical(x - centerX, y - centerY, rho, location);
                int sunlight = mars.getSurfaceFeatures().getSurfaceSunlight(location);
                int shadeColor = ((127 - sunlight) << 24) & 0xFF000000;
                shadingArray[x + (y * width)] = shadeColor;
                shadingArray[x + 1 + (y * width)] = shadeColor;
                if (y < height - 1) {
                    shadingArray[x + ((y + 1) * width)] = shadeColor;
                    shadingArray[x + 1 + ((y + 1) * width)] = shadeColor;
                }
                if (sunlight > 0) nightTime = false;
                if (sunlight < 127) dayTime = false;
            }
        }
        if (nightTime) {
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, width, height);
        } else if (!dayTime) {
            Image shadingMap = this.createImage(new MemoryImageSource(width, height, shadingArray, 0, width));
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(shadingMap, 0);
            try {
                mt.waitForID(0);
            } catch (InterruptedException e) {
                System.out.println("MapDisplay - ShadingMap interrupted: " + e);
            }
            g.drawImage(shadingMap, 0, 0, this);
        }
    }

    /** Draws units on map
     *  @param g graphics context
     */
    private void drawUnits(Graphics g) {
        Iterator i = proxyManager.getUIProxies();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy.isMapDisplayed()) {
                Coordinates unitCoords = proxy.getUnit().getCoordinates();
                double angle = 0D;
                if (useUSGSMap && !topo) angle = HALF_MAP_ANGLE_USGS; else angle = HALF_MAP_ANGLE_STANDARD;
                if (centerCoords.getAngle(unitCoords) < angle) {
                    IntPoint rectLocation = getUnitRectPosition(unitCoords);
                    Image positionImage = proxy.getSurfMapIcon().getImage();
                    IntPoint imageLocation = getUnitDrawLocation(rectLocation, positionImage);
                    if (topo) {
                        g.drawImage(proxy.getTopoMapIcon().getImage(), imageLocation.getiX(), imageLocation.getiY(), this);
                    } else {
                        g.drawImage(proxy.getSurfMapIcon().getImage(), imageLocation.getiX(), imageLocation.getiY(), this);
                    }
                    if (labels) {
                        if (topo) g.setColor(proxy.getTopoMapLabelColor()); else g.setColor(proxy.getSurfMapLabelColor());
                        g.setFont(proxy.getMapLabelFont());
                        IntPoint labelLocation = getLabelLocation(rectLocation, positionImage);
                        g.drawString(proxy.getUnit().getName(), labelLocation.getiX() + labelHorizOffset, labelLocation.getiY());
                    }
                }
            }
        }
    }

    /**
     * Draws vehicle trails.
     * @param g graphics context
     */
    private void drawVehicleTrails(Graphics g) {
        if (topo) g.setColor(Color.black); else g.setColor(new Color(0, 96, 0));
        double angle = 0D;
        if (useUSGSMap && !topo) angle = HALF_MAP_ANGLE_USGS; else angle = HALF_MAP_ANGLE_STANDARD;
        Iterator i = proxyManager.getUIProxies();
        while (i.hasNext()) {
            Object proxy = i.next();
            if (proxy instanceof VehicleUIProxy) {
                VehicleUIProxy vehicleProxy = (VehicleUIProxy) proxy;
                Vehicle vehicle = (Vehicle) vehicleProxy.getUnit();
                IntPoint oldSpot = null;
                Iterator j = (new ArrayList(vehicle.getTrail())).iterator();
                while (j.hasNext()) {
                    Coordinates trailSpot = (Coordinates) j.next();
                    if (centerCoords.getAngle(trailSpot) < angle) {
                        IntPoint spotLocation = getUnitRectPosition(trailSpot);
                        if ((oldSpot == null)) g.drawRect(spotLocation.getiX(), spotLocation.getiY(), 1, 1); else if (!spotLocation.equals(oldSpot)) g.drawLine(oldSpot.getiX(), oldSpot.getiY(), spotLocation.getiX(), spotLocation.getiY());
                        oldSpot = spotLocation;
                    }
                }
            }
        }
    }

    /** MouseListener methods overridden. Perform appropriate action
     *  on mouse click. */
    public void mouseClicked(MouseEvent event) {
        double rho;
        if (useUSGSMap && !topo) rho = 11458D / Math.PI; else rho = 1440D / Math.PI;
        Coordinates clickedPosition = centerCoords.convertRectToSpherical((double) (event.getX() - HALF_MAP - 1), (double) (event.getY() - HALF_MAP - 1), rho);
        boolean unitsClicked = false;
        Iterator i = proxyManager.getUIProxies();
        while (i.hasNext()) {
            UnitUIProxy proxy = (UnitUIProxy) i.next();
            if (proxy.isMapDisplayed()) {
                Coordinates unitCoords = proxy.getUnit().getCoordinates();
                double clickRange = unitCoords.getDistance(clickedPosition);
                double unitClickRange = proxy.getMapClickRange();
                if (useUSGSMap && !topo) unitClickRange *= .1257D;
                if (clickRange < unitClickRange) {
                    navWindow.openUnitWindow(proxy);
                    unitsClicked = true;
                }
            }
        }
        if (!unitsClicked) navWindow.updateCoords(clickedPosition);
    }

    public void mousePressed(MouseEvent event) {
    }

    public void mouseReleased(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    /** Returns unit x, y position on map panel
     *  @param unitCoords location of unit
     *  @return display point on map
     */
    private IntPoint getUnitRectPosition(Coordinates unitCoords) {
        double rho;
        int half_map;
        if (useUSGSMap && !topo) {
            rho = 11458D / Math.PI;
            half_map = 11458 / 2;
        } else {
            rho = 1440D / Math.PI;
            half_map = 1440 / 2;
        }
        int low_edge = half_map - 150;
        return Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
    }

    /** Returns unit image draw position on map panel
     *  @param unitPosition absolute unit position
     *  @param unitImage unit's map image
     *  @return draw position for unit image
     */
    private IntPoint getUnitDrawLocation(IntPoint unitPosition, Image unitImage) {
        return new IntPoint(unitPosition.getiX() - (unitImage.getWidth(this) / 2), unitPosition.getiY() - (unitImage.getHeight(this) / 2));
    }

    private static final int labelHorizOffset = 2;

    /** Returns label draw postion on map panel
     *  @param unitPosition absolute unit position
     *  @param unitImage unit's map image
     *  @return draw position for unit label
     */
    private IntPoint getLabelLocation(IntPoint unitPosition, Image unitImage) {
        return new IntPoint(unitPosition.getiX() + (unitImage.getWidth(this) / 2) + labelHorizOffset, unitPosition.getiY() + (unitImage.getHeight(this) / 2));
    }

    /** Sets day/night tracking to on or off.
     *  @param showDayNightShading true if map is to use day/night tracking.
     */
    public void setDayNightTracking(boolean showDayNightShading) {
        this.showDayNightShading = showDayNightShading;
    }

    /**
     * Sets the vehicle trails flag.
     * @param showVehicleTrails true if vehicle trails are to be displayed.
     */
    public void setVehicleTrails(boolean showVehicleTrails) {
        this.showVehicleTrails = showVehicleTrails;
    }
}
