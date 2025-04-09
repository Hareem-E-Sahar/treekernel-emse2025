package swarm.gui;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;
import org.jfree.ui.RectangleInsets;
import swarm.SystemProperties;
import swarm.engine.Individual;
import swarm.engine.MetricsCalculator;
import swarm.engine.Parameters;
import swarm.engine.Population;
import swarm.engine.PopulationSimulator;
import static swarm.gui.GUI.*;

public class SwarmPopulationSimulatorPanel extends JPanel {

    private final class MouseEventHandler extends MouseAdapter implements MouseWheelListener, MouseMotionListener {

        private Point dragStart;

        private Double midPointStart;

        public void mouseClicked(MouseEvent me) {
            if (me.getModifiers() != InputEvent.BUTTON3_MASK) isSelected = !isSelected;
        }

        public void mouseEntered(MouseEvent me) {
            isMouseIn = true;
        }

        public void mouseExited(MouseEvent me) {
            isMouseIn = false;
        }

        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
            midPointStart = new Point2D.Double(currentMidX, currentMidY);
        }

        public void mouseDragged(MouseEvent e) {
            Point2D.Double p = new Point2D.Double(e.getX() - dragStart.x, e.getY() - dragStart.y);
            currentMidX = midPointStart.x - p.x / currentScalingFactor;
            currentMidY = midPointStart.y - p.y / currentScalingFactor;
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            final double f = Math.pow(1.7, notches);
            currentScalingFactor /= f;
        }

        public void mouseMoved(MouseEvent arg0) {
        }
    }

    PopulationSimulator simulator;

    public int originalWidth, originalHeight;

    public boolean isSelected;

    private int mouseX, mouseY;

    private boolean isMouseIn;

    private double currentMidX, currentMidY, currentScalingFactor;

    private double swarmRadius = 3;

    private double swarmDiameter;

    private boolean tracking;

    private Map<Integer, Point2D.Double> markers = new HashMap<Integer, Point2D.Double>();

    private Collection<Point2D.Double> registrationMarks = Collections.emptyList();

    public SwarmPopulationSimulatorPanel(int spaceSize, PopulationSimulator _simulator) {
        super();
        initialize(spaceSize, _simulator);
        final MouseEventHandler mouseListener = new MouseEventHandler();
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        addMouseMotionListener(mouseListener);
        mouseX = mouseY = -100;
        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent me) {
                mouseX = me.getX();
                mouseY = me.getY();
            }

            public void mouseMoved(MouseEvent me) {
                mouseX = me.getX();
                mouseY = me.getY();
            }
        });
    }

    public void initialize(int spaceSize, PopulationSimulator _simulator) {
        simulator = _simulator;
        isSelected = false;
        currentMidX = 0;
        currentMidY = 0;
        currentScalingFactor = 0.3;
        swarmDiameter = swarmRadius * 2;
        originalHeight = originalWidth = spaceSize;
        repaint();
    }

    public void paint(Graphics g) {
        g.drawImage(displayStates(), 0, 0, getWidth(), getHeight(), this);
    }

    public synchronized Image displayStates() {
        final int width = getWidth(), height = getHeight();
        final int margin = (int) (Math.min(width, height) * getMarginFraction());
        final boolean lightOnDark = getLightOnDark();
        Image backBuffer = createImage(width, height);
        Graphics2D g = (Graphics2D) backBuffer.getGraphics();
        Individual ag, ag2;
        int max, x, y;
        double minX, maxX, minY, maxY, tempX, tempY, midX, midY, scalingFactor;
        double averageInterval;
        int tempRadius, tempDiameter;
        if (lightOnDark) g.setColor(Color.black); else g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        if (isSelected) {
            g.setColor(Color.red);
            Stroke stroke = g.getStroke();
            g.setStroke(new BasicStroke(5));
            g.drawRect(1, 1, width - 2, height - 2);
            g.setStroke(stroke);
        }
        Population population = simulator.getPopulation();
        max = population.size();
        List<Individual> swarmInXOrder = simulator.getSwarmInXOrder();
        List<Individual> swarmInYOrder = simulator.getSwarmInYOrder();
        minX = swarmInXOrder.get(0).getX();
        maxX = swarmInXOrder.get(max - 1).getX();
        minY = swarmInYOrder.get(0).getY();
        maxY = swarmInYOrder.get(max - 1).getY();
        if (tracking && max > 10) {
            averageInterval = 0;
            for (int i = 0; i < max - 1; i++) {
                ag = swarmInXOrder.get(i);
                ag2 = swarmInXOrder.get(i + 1);
                averageInterval += ag2.getX() - ag.getX();
            }
            averageInterval /= max - 1;
            for (int i = 0; i < max - 10; i++) {
                ag = swarmInXOrder.get(i);
                ag2 = swarmInXOrder.get(i + 10);
                if (ag2.getX() - ag.getX() < averageInterval * getIntervalCoefficient()) {
                    minX = ag.getX();
                    break;
                }
            }
            for (int i = max - 1; i >= 10; i--) {
                ag = swarmInXOrder.get(i - 10);
                ag2 = swarmInXOrder.get(i);
                if (ag2.getX() - ag.getX() < averageInterval * getIntervalCoefficient()) {
                    maxX = ag2.getX();
                    break;
                }
            }
            tempX = (maxX - minX) * 0.1;
            minX -= tempX;
            maxX += tempX;
            averageInterval = 0;
            for (int i = 0; i < max - 1; i++) {
                ag = swarmInYOrder.get(i);
                ag2 = swarmInYOrder.get(i + 1);
                averageInterval += ag2.getY() - ag.getY();
            }
            averageInterval /= max - 1;
            for (int i = 0; i < max - 10; i++) {
                ag = swarmInYOrder.get(i);
                ag2 = swarmInYOrder.get(i + 10);
                if (ag2.getY() - ag.getY() < averageInterval * getIntervalCoefficient()) {
                    minY = ag.getY();
                    break;
                }
            }
            for (int i = max - 1; i >= 10; i--) {
                ag = swarmInYOrder.get(i - 10);
                ag2 = swarmInYOrder.get(i);
                if (ag2.getY() - ag.getY() < averageInterval * getIntervalCoefficient()) {
                    maxY = ag2.getY();
                    break;
                }
            }
            tempY = (maxY - minY) * 0.1;
            minY -= tempY;
            maxY += tempY;
        }
        if (maxX - minX < (double) originalWidth) maxX = (minX = (minX + maxX - (double) originalWidth) / 2) + (double) originalWidth;
        if (maxY - minY < (double) originalHeight) maxY = (minY = (minY + maxY - (double) originalHeight) / 2) + (double) originalHeight;
        midX = (minX + maxX) / 2;
        midY = (minY + maxY) / 2;
        if ((maxX - minX) * height > (maxY - minY) * width) scalingFactor = ((double) (width - 2 * margin)) / (maxX - minX); else scalingFactor = ((double) (height - 2 * margin)) / (maxY - minY);
        scalingFactor *= getZoomFactor();
        if (tracking) {
            if (currentScalingFactor == 0) {
                currentMidX = midX;
                currentMidY = midY;
                currentScalingFactor = scalingFactor;
            } else {
                currentMidX += (midX - currentMidX) * getTrackingFilterStrength();
                currentMidY += (midY - currentMidY) * getTrackingFilterStrength();
                currentScalingFactor += (scalingFactor - currentScalingFactor) * getScalingFilterStrength();
            }
        }
        if (getGridEnabled()) {
            if (lightOnDark) g.setColor(Color.darkGray.darker()); else g.setColor(Color.lightGray);
            final double gridInterval = getGridInterval();
            for (tempX = Math.floor((-((double) width) / 2 / currentScalingFactor + currentMidX) / gridInterval) * gridInterval; tempX < ((double) width) / 2 / currentScalingFactor + currentMidX; tempX += gridInterval) g.drawLine((int) ((tempX - currentMidX) * currentScalingFactor) + width / 2, 0, (int) ((tempX - currentMidX) * currentScalingFactor) + width / 2, height);
            for (tempY = Math.floor((-((double) height) / 2 / currentScalingFactor + currentMidY) / gridInterval) * gridInterval; tempY < ((double) height) / 2 / currentScalingFactor + currentMidY; tempY += gridInterval) g.drawLine(0, (int) ((tempY - currentMidY) * currentScalingFactor) + height / 2, width, (int) ((tempY - currentMidY) * currentScalingFactor) + height / 2);
        }
        tempRadius = (int) (swarmRadius * currentScalingFactor);
        tempDiameter = (int) (swarmDiameter * currentScalingFactor);
        if (tempDiameter < 3) tempDiameter = 3;
        for (int i = 0; i < max; i++) {
            ag = population.get(i);
            x = (int) ((ag.getX() - currentMidX) * currentScalingFactor) + width / 2;
            y = (int) ((ag.getY() - currentMidY) * currentScalingFactor) + height / 2;
            Color displayColor = ag.getDisplayColor();
            if (lightOnDark) {
                displayColor = new Color(255 - displayColor.getRed(), 255 - displayColor.getGreen(), 255 - displayColor.getBlue());
            }
            g.setColor(displayColor);
            g.fillOval(x - tempRadius, y - tempRadius, tempDiameter, tempDiameter);
        }
        if (getMarksEnabled()) {
            g.setColor(Color.orange);
            for (Map.Entry<Integer, Point2D.Double> entry : markers.entrySet()) {
                final Double p = entry.getValue();
                x = (int) ((p.x - currentMidX) * currentScalingFactor) + width / 2;
                y = (int) ((p.y - currentMidY) * currentScalingFactor) + height / 2;
                g.drawString(entry.getKey().toString(), x, y);
                g.drawOval(x - 12, y - 12, 24, 24);
            }
            g.setColor(Color.BLUE);
            for (Point2D.Double p : registrationMarks) {
                x = (int) ((p.x - currentMidX) * currentScalingFactor) + width / 2;
                y = (int) ((p.y - currentMidY) * currentScalingFactor) + height / 2;
                g.fillOval(x - 12, y - 12, 24, 24);
            }
        }
        return backBuffer;
    }

    public PopulationSimulator getSimulator() {
        return simulator;
    }

    public void setSimulator(PopulationSimulator simulator) {
        this.simulator = simulator;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isTracking() {
        return tracking;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

    public int getMouseX() {
        return (int) (((double) (mouseX - getWidth() / 2)) / currentScalingFactor + currentMidX);
    }

    public int getMouseY() {
        return (int) (((double) (mouseY - getHeight() / 2)) / currentScalingFactor + currentMidY);
    }

    public boolean isMouseIn() {
        return isMouseIn;
    }

    public void clearMarkers() {
        markers.clear();
    }

    public void setMarker(int i, Point2D.Double p) {
        if (p == null) {
            markers.remove(i);
        } else {
            markers.put(i, p);
        }
    }

    public void setMarkers(Map<Integer, Point2D.Double> m) {
        markers.clear();
        markers.putAll(m);
    }

    public void setRegistrationMarks(Collection<Point2D.Double> registrationMarks) {
        this.registrationMarks = new ArrayList<Point2D.Double>(registrationMarks);
    }
}
