import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MapDisplay extends JComponent implements MouseListener, Runnable {

    private NavigatorWindow navWindow;

    private SurfaceMap marsSurface;

    private SurfaceMap topoSurface;

    private boolean Wait;

    private Coordinates centerCoords;

    private Thread showThread;

    private boolean topo;

    private boolean recreate;

    private boolean labels;

    private Image mapImage;

    private Image vehicleSymbol;

    private Image topoVehicleSymbol;

    private Image settlementSymbol;

    private Image topoSettlementSymbol;

    public MapDisplay(NavigatorWindow navWindow) {
        super();
        setPreferredSize(new Dimension(300, 300));
        setMaximumSize(getPreferredSize());
        setMinimumSize(getPreferredSize());
        addMouseListener(this);
        marsSurface = new SurfaceMap("surface", this);
        topoSurface = new SurfaceMap("topo", this);
        centerCoords = new Coordinates(Math.PI / 2D, 0D);
        Wait = false;
        recreate = true;
        topo = false;
        labels = true;
        this.navWindow = navWindow;
        vehicleSymbol = (Toolkit.getDefaultToolkit()).getImage("VehicleSymbol.gif");
        topoVehicleSymbol = (Toolkit.getDefaultToolkit()).getImage("VehicleSymbolBlack.gif");
        settlementSymbol = (Toolkit.getDefaultToolkit()).getImage("SettlementSymbol.gif");
        topoSettlementSymbol = (Toolkit.getDefaultToolkit()).getImage("SettlementSymbolBlack.gif");
        showReal();
    }

    public void setLabels(boolean labels) {
        this.labels = labels;
    }

    public void showReal() {
        if (topo) {
            Wait = true;
            recreate = true;
        }
        topo = false;
        showMap(centerCoords);
    }

    public void showTopo() {
        if (!topo) {
            Wait = true;
            recreate = true;
        }
        topo = true;
        showMap(centerCoords);
    }

    public void showMap(Coordinates newCenter) {
        if (!centerCoords.equals(newCenter)) {
            Wait = true;
            recreate = true;
            centerCoords.setCoords(newCenter);
        }
        start();
    }

    public void start() {
        if ((showThread == null) || (!showThread.isAlive())) {
            showThread = new Thread(this, "Map");
            showThread.start();
        }
    }

    public void run() {
        while (true) {
            if (recreate) {
                if (topo) topoSurface.drawMap(centerCoords); else marsSurface.drawMap(centerCoords);
                recreate = false;
                repaint();
            } else {
                try {
                    showThread.sleep(2000);
                } catch (InterruptedException e) {
                }
                repaint();
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (Wait) {
            if (mapImage != null) g.drawImage(mapImage, 0, 0, this);
            g.setColor(Color.green);
            String message = new String("Preparing Map...");
            Font alertFont = new Font("TimesRoman", Font.BOLD, 30);
            FontMetrics alertMetrics = getFontMetrics(alertFont);
            int Height = alertMetrics.getHeight();
            int Width = alertMetrics.stringWidth(message);
            int x = (300 - Width) / 2;
            int y = (300 + Height) / 2;
            g.setFont(alertFont);
            g.drawString(message, x, y);
            Wait = false;
        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, 300, 300);
            boolean image_done = false;
            SurfaceMap tempMap;
            if (topo) tempMap = topoSurface; else tempMap = marsSurface;
            if (tempMap.image_done) {
                image_done = true;
                mapImage = tempMap.getMapImage();
                g.drawImage(mapImage, 0, 0, this);
            }
            if (topo) g.setColor(Color.black); else g.setColor(Color.green);
            g.setFont(new Font("Helvetica", Font.PLAIN, 9));
            UnitInfo[] vehicleInfo = navWindow.getMovingVehicleInfo();
            int counter = 0;
            for (int x = 0; x < vehicleInfo.length; x++) {
                if (centerCoords.getAngle(vehicleInfo[x].getCoords()) < .48587D) {
                    int[] rectLocation = getUnitRectPosition(vehicleInfo[x].getCoords());
                    int[] imageLocation = getUnitDrawLocation(rectLocation, vehicleSymbol);
                    if (topo) g.drawImage(topoVehicleSymbol, imageLocation[0], imageLocation[1], this); else g.drawImage(vehicleSymbol, imageLocation[0], imageLocation[1], this);
                    if (labels) {
                        int[] labelLocation = getLabelLocation(rectLocation, vehicleSymbol);
                        g.drawString(vehicleInfo[x].getName(), labelLocation[0], labelLocation[1]);
                    }
                    counter++;
                }
            }
            g.setFont(new Font("Helvetica", Font.PLAIN, 12));
            UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
            for (int x = 0; x < settlementInfo.length; x++) {
                if (centerCoords.getAngle(settlementInfo[x].getCoords()) < .48587D) {
                    int[] rectLocation = getUnitRectPosition(settlementInfo[x].getCoords());
                    int[] imageLocation = getUnitDrawLocation(rectLocation, settlementSymbol);
                    if (topo) g.drawImage(topoSettlementSymbol, imageLocation[0], imageLocation[1], this); else g.drawImage(settlementSymbol, imageLocation[0], imageLocation[1], this);
                    if (labels) {
                        int[] labelLocation = getLabelLocation(rectLocation, settlementSymbol);
                        g.drawString(settlementInfo[x].getName(), labelLocation[0], labelLocation[1]);
                    }
                }
            }
        }
    }

    public void mouseReleased(MouseEvent event) {
        Coordinates clickedPosition = centerCoords.convertRectToSpherical((double) event.getX() - 149D, (double) event.getY() - 149D);
        boolean unitsClicked = false;
        UnitInfo[] movingVehicleInfo = navWindow.getMovingVehicleInfo();
        for (int x = 0; x < movingVehicleInfo.length; x++) {
            if (movingVehicleInfo[x].getCoords().getDistance(clickedPosition) < 40D) {
                navWindow.openUnitWindow(movingVehicleInfo[x].getID());
                unitsClicked = true;
            }
        }
        UnitInfo[] settlementInfo = navWindow.getSettlementInfo();
        for (int x = 0; x < settlementInfo.length; x++) {
            if (settlementInfo[x].getCoords().getDistance(clickedPosition) < 90D) {
                navWindow.openUnitWindow(settlementInfo[x].getID());
                unitsClicked = true;
            }
        }
        if (!unitsClicked) navWindow.updateCoords(clickedPosition);
    }

    public void mousePressed(MouseEvent event) {
    }

    public void mouseClicked(MouseEvent event) {
    }

    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    private int[] getUnitRectPosition(Coordinates unitCoords) {
        double rho = 1440D / Math.PI;
        int half_map = 720;
        int low_edge = half_map - 150;
        int[] result = Coordinates.findRectPosition(unitCoords, centerCoords, rho, half_map, low_edge);
        return result;
    }

    private int[] getUnitDrawLocation(int[] unitPosition, Image unitImage) {
        int[] result = new int[2];
        result[0] = unitPosition[0] - Math.round(unitImage.getWidth(this) / 2);
        result[1] = unitPosition[1] - Math.round(unitImage.getHeight(this) / 2);
        return result;
    }

    private int[] getLabelLocation(int[] unitPosition, Image unitImage) {
        int[] result = new int[2];
        result[0] = unitPosition[0] + Math.round(unitImage.getWidth(this) / 2) + 10;
        result[1] = unitPosition[1] + Math.round(unitImage.getHeight(this) / 2);
        return result;
    }
}
