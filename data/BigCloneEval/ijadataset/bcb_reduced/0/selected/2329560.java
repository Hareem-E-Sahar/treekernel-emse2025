package org.igr.gps;

import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.Sprite;
import org.igr.gps.location.Location;
import org.igr.gps.location.LocationProvider;

public class TraceView extends Canvas implements CommandListener {

    public static final Command cmdClose = new Command("Close", Command.EXIT, 100);

    public static final Command cmdStartTrace = new Command("Start trace", Command.SCREEN, 5);

    public static final Command cmdStopTrace = new Command("Stop trace", Command.STOP, 10);

    public static final Command cmdStatus = new Command("GPS status", Command.BACK, 50);

    private CommandListener owner;

    private FileChoice fileChoice;

    private Tracer tracer = null;

    private Logger logger = null;

    private Location[] route = new Location[0];

    private double minLatitude;

    private double maxLatitude;

    private double minLongitude;

    private double maxLongitude;

    private double distance;

    public TraceView(CommandListener owner) {
        this.owner = owner;
        setFullScreenMode(true);
        addCommand(cmdStartTrace);
        addCommand(cmdStopTrace);
        addCommand(cmdStatus);
        addCommand(cmdClose);
        setCommandListener(this);
    }

    public synchronized void update() {
        if (tracer == null) {
            return;
        }
        route = tracer.getRoute();
        minLatitude = tracer.getMinLatitude();
        maxLatitude = tracer.getMaxLatitude();
        minLongitude = tracer.getMinLongitude();
        maxLongitude = tracer.getMaxLongitude();
        distance = tracer.getDistance();
        repaint();
    }

    private double cLong;

    private double cLat;

    private double cScrX;

    private double cScrY;

    private double mPerLong;

    private double mPerLat;

    private double mPerPixel;

    private double cx, cy;

    private boolean autoZoom = true;

    private void drawLocation(Graphics g, Location location, int color1, int color2) {
        try {
            int margin = g.getFont().getHeight();
            mPerLong = LocationProvider.distance(location.latitude, location.longitude, location.latitude, location.longitude);
            mPerLat = LocationProvider.distance(location.latitude, location.longitude, location.latitude + 1, location.longitude);
            if (autoZoom) mPerPixel = Math.max(mPerLat * (maxLatitude - minLatitude) / (getHeight() - 2 * margin), mPerLong * (maxLongitude - minLongitude) / (getWidth() - 2 * margin));
            mPerPixel = Math.max(mPerPixel, 1);
            cx = cScrX + mPerLong * (location.longitude - cLong) / mPerPixel;
            cy = cScrY - mPerLat * (location.latitude - cLat) / mPerPixel;
            double r2 = location.horizontalAccuracy / mPerPixel;
            double r1 = 1.5 * r2;
            g.setColor(color2);
            g.fillArc((int) (cx - r1 - 0.5), (int) (cy - r1 - 0.5), (int) (2 * r1 + 1.5), (int) (2 * r1 + 1.5), 0, 360);
            g.setColor(color1);
            g.fillArc((int) (cx - r2 - 0.5), (int) (cy - r2 - 0.5), (int) (2 * r2 + 1.5), (int) (2 * r2 + 1.5), 0, 360);
        } catch (Exception e) {
            GPStatus.log("TraceView.drawLocation", e);
        }
    }

    public synchronized void paint(Graphics g) {
        try {
            g.setClip(0, 0, getWidth(), getHeight());
            g.setColor(0x00e8e8e8);
            g.fillRect(0, 0, getWidth(), getHeight());
            Font font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(font);
            if (autoZoom) {
                cLong = (minLongitude + maxLongitude) / 2;
                cLat = (minLatitude + maxLatitude) / 2;
            }
            cScrX = getWidth() / 2;
            cScrY = getHeight() / 2;
            if (route.length != 0) {
                for (int i = 1; i < route.length - 1; i++) drawLocation(g, route[i], 0x00606060, 0x00808080);
                Location firstCoordinates = route[0];
                Location lastCoordinates = route[route.length - 1];
                drawLocation(g, firstCoordinates, 0x000620C9, 0x00808080);
                drawLocation(g, lastCoordinates, 0x00C97D7D, 0x00808080);
                g.setColor(0x00E01010);
                g.setStrokeStyle(Graphics.DOTTED);
                g.drawLine((int) (cx), (int) (cy - 20), (int) (cx), getHeight() - font.getBaselinePosition());
                g.drawLine((int) (cx - 20), (int) (cy), getWidth() - font.getBaselinePosition(), (int) (cy));
                g.setColor(0x00010101);
                Utils.transformText(g, Utils.formatLongitude(lastCoordinates.longitude, lastCoordinates.horizontalAccuracy), Sprite.TRANS_NONE, (int) cx, getHeight(), Graphics.HCENTER | Graphics.BOTTOM);
                Utils.transformText(g, Utils.formatLatitude(lastCoordinates.latitude, lastCoordinates.horizontalAccuracy), Sprite.TRANS_ROT270, getWidth(), (int) cy, Graphics.RIGHT | Graphics.VCENTER);
                Utils.transformText(g, "Total time: " + Utils.formatTimeDiff(route[route.length - 1].timestamp - route[0].timestamp) + "  distance: " + (int) distance + "m", Sprite.TRANS_NONE, (int) (cScrX), 0, Graphics.HCENTER | Graphics.TOP);
            }
        } catch (Exception e) {
            GPStatus.log("TraceView.paint", e);
        }
    }

    protected synchronized void keyPressed(int keyCode) {
        try {
            if (keyCode == getKeyCode(LEFT)) {
                cLong -= getWidth() / 2 * mPerPixel / mPerLong;
                autoZoom = false;
            } else if (keyCode == getKeyCode(RIGHT)) {
                cLong += getWidth() / 2 * mPerPixel / mPerLong;
                autoZoom = false;
            } else if (keyCode == getKeyCode(UP)) {
                cLat += getHeight() / 2 * mPerPixel / mPerLat;
                autoZoom = false;
            } else if (keyCode == getKeyCode(DOWN)) {
                cLat -= getHeight() / 2 * mPerPixel / mPerLat;
                autoZoom = false;
            } else if (keyCode == KEY_NUM0) {
                mPerPixel *= 1.5;
                autoZoom = false;
            } else if (keyCode == KEY_POUND) {
                mPerPixel /= 1.5;
                if (mPerPixel < 1.0) mPerPixel = 1;
                autoZoom = false;
            } else if (keyCode == KEY_STAR) {
                autoZoom = true;
            }
            repaint();
        } catch (Exception e) {
            GPStatus.log("TraceView.keyPressed", e);
        }
    }

    public void commandAction(Command command, Displayable d) {
        try {
            if (d == this) {
                if (command == cmdStartTrace) {
                    tracer = new Tracer(this);
                    fileChoice = new FileChoice(this);
                    fileChoice.setTitle("Log to file (GPX)");
                    GPStatus.setScreen(fileChoice);
                } else if (command == cmdStopTrace) {
                    if (tracer != null) {
                        tracer.stop();
                        tracer = null;
                    }
                    if (logger != null) {
                        logger.stop();
                        logger = null;
                    }
                } else {
                    if (command == cmdClose) {
                        if (tracer != null) {
                            tracer.stop();
                            tracer = null;
                        }
                        if (logger != null) {
                            logger.stop();
                            logger = null;
                        }
                    }
                    owner.commandAction(command, d);
                }
            } else if (d == fileChoice) {
                if (command == FileChoice.openCmd) {
                    FileConnection file = ((FileChoice) d).getFile();
                    if (file != null) {
                        logger = new Logger(file);
                    }
                }
                GPStatus.setScreen(this);
                fileChoice = null;
            }
        } catch (Exception e) {
            GPStatus.log("TraceView.commandAction", e);
        }
    }
}
