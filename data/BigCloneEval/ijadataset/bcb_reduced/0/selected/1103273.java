package jgrx.graph;

import jgrx.iface.Context;
import jgrx.iface.Parametric;
import jgrx.iface.PlotList;
import jgrx.iface.Term;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.Observer;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import jgrx.iface.impl.util.MyObservable;

public class Graph extends JPanel implements Printable {

    private static boolean display = false;

    private double xMin, xMax, xStep, xScale;

    private double yMin, yMax, yStep, yScale;

    private double tangentLoc;

    private double realXMax;

    private double realYMax;

    private double a;

    private int pressedX, pressedY;

    private int clickedX, clickedY;

    /**reports whether resize to occur in x direction - should report true
     *if x coordinate of mouse click is in outer third of screen domain
     */
    private boolean direX = false;

    /**reports whether resize to occur in y direction - should report true
     *if y coordinate of mouse click is in outer third of screen range
     */
    private boolean direY = false;

    private boolean plotD, plotPts;

    private boolean center;

    private boolean ctrlDown = false;

    private MyObservable myObservable;

    private DecimalFormat myFormat;

    private ClickMode clickMode;

    private TreeMap<String, DrawInfo> fcts;

    private ArrayList<StatDrawInfo> plots;

    private Context context;

    private Color background;

    private Point2D centerPoint;

    public Graph(Context context, DecimalFormat df) {
        this.context = context;
        center = true;
        fcts = new TreeMap();
        plots = new ArrayList();
        DrawInfo info = new DrawInfo();
        info.setColor(Color.RED);
        background = Color.white;
        myObservable = new MyObservable();
        myFormat = df;
        centerPoint = new Point2D.Double(0, 0);
        clickMode = ClickMode.Move;
        setAngle(Math.PI / 6);
        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                realXMax = getSize().getWidth();
                realYMax = getSize().getHeight();
                xScale = realXMax / (xMax - xMin);
                yScale = realYMax / (yMax - yMin);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent e) {
                switch(clickMode) {
                    case Resize:
                        {
                            resizeAction(e.getX(), e.getY());
                        }
                        break;
                    case Tangent:
                        {
                            plotD = true;
                        }
                    default:
                        {
                            moveAction(e.getX(), e.getY());
                        }
                }
            }
        });
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                pressedX = e.getX();
                tangentLoc = cartX(pressedX);
                pressedY = e.getY();
                switch(clickMode) {
                    case Resize:
                        {
                            if (pressedX < getWidth() / 3) {
                                direX = true;
                            } else if (pressedX < getWidth() * 2 / 3) {
                                direX = false;
                            } else {
                                direX = true;
                            }
                            if (pressedY < getHeight() / 3) {
                                direY = true;
                            } else if (pressedY < getHeight() * 2 / 3) {
                                direY = false;
                            } else {
                                direY = true;
                            }
                        }
                        break;
                    case Follow:
                        {
                            followAction(e.getX());
                        }
                        break;
                    case Intersect:
                        {
                            intersectAction(e.getX());
                        }
                        break;
                }
            }

            public void mouseReleased(MouseEvent e) {
                clickedX = e.getX();
                clickedY = e.getY();
                switch(clickMode) {
                    case Tangent:
                        {
                            tangentAction(e.getX());
                        }
                        break;
                    default:
                        {
                            myObservable.change("click");
                            repaint();
                        }
                }
            }
        });
    }

    public void installCopyPopup() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ImageTransferable transferable = new ImageTransferable(snapshot());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
            }
        });
        menu.add(copy);
        setComponentPopupMenu(menu);
    }

    public BufferedImage snapshot() {
        BufferedImage bimage = new BufferedImage((int) realXMax, (int) realYMax, BufferedImage.TYPE_INT_RGB);
        paint(bimage.getGraphics());
        return bimage;
    }

    public void installPopup() {
        JPopupMenu menu = new JPopupMenu();
        JRadioButtonMenuItem drag_move = new JRadioButtonMenuItem("Move");
        JRadioButtonMenuItem drag_resize = new JRadioButtonMenuItem("Resize");
        JMenuItem properties = new JMenuItem("Properties");
        JMenuItem fct_ops = new JMenuItem("Function Options");
        drag_move.setSelected(true);
        ButtonGroup dragGroup = new ButtonGroup();
        dragGroup.add(drag_move);
        dragGroup.add(drag_resize);
        drag_move.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Graph.this.setClickMode(ClickMode.Move);
            }
        });
        drag_resize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Graph.this.setClickMode(ClickMode.Resize);
            }
        });
        properties.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        fct_ops.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                FctGraphEditor editor = new FctGraphEditor(fcts, Graph.this.context);
                editor.setVisible(true);
            }
        });
        menu.add(drag_move);
        menu.add(drag_resize);
        menu.add(new JSeparator());
        menu.add(fct_ops);
        menu.add(properties);
        setComponentPopupMenu(menu);
    }

    public void moveAction(double x, double y) {
        double dx = -((x - pressedX) / xScale);
        double dy = ((y - pressedY) / yScale);
        pressedX = (int) x;
        pressedY = (int) y;
        setXMin(xMin + dx);
        setXMax(xMax + dx);
        setYMin(yMin + dy);
        setYMax(yMax + dy);
        myObservable.change("move");
        repaint();
    }

    public void resizeAction(double x, double y) {
        double dx = 10 * Math.signum(x - pressedX) / xScale;
        double dy = 10 * Math.signum(y - pressedY) / yScale;
        if (x > realXMax * 2 / 3) dx *= -1;
        if (y > realYMax * 2 / 3) dy *= -1;
        if (direX) {
            setXMax(xMax + dx);
            setXMin(xMin - dx);
        }
        if (direY) {
            setYMax(yMax + dy);
            setYMin(yMin - dy);
        }
        if (!direX && !direY) {
            moveAction(x, y);
        } else {
            myObservable.change("resize");
            repaint();
        }
    }

    public void followAction(double x) {
        String s = clickMode.getTerm1();
        if (fcts.containsKey(s)) {
            Term term = context.getTerm(s);
            double cx = cartX(x);
            double cy = term.solve(cx);
            if (!Double.isNaN(cy) && !Double.isInfinite(cy)) {
                if (center) {
                    centerPoint(cx, cy);
                } else {
                    centerPoint = new Point2D.Double(cx, cy);
                }
                repaint();
            }
        } else {
            System.out.println("followAction: not contain: " + s);
        }
    }

    public void intersectAction(double x) {
        try {
            String s = clickMode.getTerm1();
            if (fcts.containsKey(s)) {
                Term term1 = context.getTerm(s);
                s = clickMode.getTerm2();
                Term term2 = context.getTerm(s);
                double cx = term1.sbt(term2).find_inverse(cartX(x));
                double cy = term1.solve(cx);
                if (!Double.isNaN(cy) && !Double.isInfinite(cy)) {
                    centerPoint(cx, cy);
                    myObservable.change("intersect");
                    repaint();
                }
            } else {
                System.out.println("intersectAction: not contain " + s);
            }
        } catch (StackOverflowError error) {
            System.out.println("overflow");
        }
    }

    public void tangentAction(double x) {
        String s = clickMode.getTerm1();
        if (fcts.containsKey(s)) {
            plotD = true;
            Term term1 = context.getTerm(s);
            double cx = cartX(x);
            double cy = term1.solve(cx);
            if (!Double.isNaN(cy) && !Double.isInfinite(cy)) {
                if (clickMode == ClickMode.Tangent) {
                    Term deriv = context.engine().tangent(term1, cx);
                    ClickMode.Tangent.setFct(deriv);
                }
                myObservable.change("tangent");
                if (center) {
                    centerPoint(cx, cy);
                    tangentLoc = centerPoint.getX();
                } else {
                    tangentLoc = cx;
                }
                repaint();
            }
        } else {
            plotD = false;
            System.out.println("tangentAction: not contain " + s);
        }
    }

    private void outport() {
        pl("called outport");
        myObservable.notifyObservers();
    }

    public void addTerm(String t) {
        DrawInfo tinfo = new DrawInfo();
        tinfo.setColor(new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)));
        fcts.put(t, tinfo);
        myObservable.change("termlist");
    }

    public void addTerm(String t, Parametric p) {
        DrawInfo pinfo = new DrawInfo();
        pinfo.setColor(new Color((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256)));
        fcts.put(t, pinfo);
        myObservable.change("parametric");
    }

    public void addPlot(StatDrawInfo sdf) {
        plots.add(sdf);
    }

    /**attempts to remove t from fcts and reports whether it did or not.
     */
    public boolean removeTerm(String t) {
        DrawInfo ifo = fcts.remove(t);
        myObservable.change("termlist");
        return ifo != null;
    }

    /**attempts to remove a plot containing x and y and reports whether it did or
     *not.
     */
    public boolean removePlot(String x, String y) {
        for (StatDrawInfo sdf : plots) {
            if (sdf.getXPlot().equals(x) && sdf.getYPlot().equals(y)) {
                plots.remove(sdf);
                return true;
            }
        }
        return false;
    }

    public TreeMap<String, DrawInfo> getGraphedFcts() {
        return fcts;
    }

    public void addObserver(Observer obs) {
        myObservable.addObserver(obs);
    }

    public double getClickedX() {
        return cartX(clickedX);
    }

    public int getClickedY() {
        return clickedY;
    }

    public double getXMin() {
        return xMin;
    }

    public double getXMax() {
        return xMax;
    }

    public double getXStep() {
        return xStep;
    }

    public double getYMin() {
        return yMin;
    }

    public double getYMax() {
        return yMax;
    }

    public double getYStep() {
        return yStep;
    }

    public void setXMin(double d) {
        xMin = d;
        xScale = realXMax / (xMax - xMin);
        context.engine().setMin(xMin);
    }

    public void setXMax(double d) {
        xMax = d;
        xScale = realXMax / (xMax - xMin);
        context.engine().setMax(xMax);
    }

    public void setXStep(double d) {
        xStep = d;
    }

    public void setYMin(double d) {
        yMin = d;
        yScale = realYMax / (yMax - yMin);
    }

    public void setYMax(double d) {
        yMax = d;
        yScale = realYMax / (yMax - yMin);
    }

    public void setYStep(double d) {
        yStep = d;
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex == 0) {
            paint(graphics);
            return Printable.PAGE_EXISTS;
        } else return Printable.NO_SUCH_PAGE;
    }

    public void paint(Graphics g) {
        clear(g);
        drawXaxis(g);
        drawYaxis(g);
        int y = 20;
        for (String f : fcts.keySet()) {
            if (context.get(f) instanceof Parametric) {
                plotParametric((Graphics2D) g, f, fcts.get(f));
            } else if (context.get(f) instanceof PlotList) {
            } else {
                Term term = context.getTerm(f);
                plotFunction((Graphics2D) g, term, fcts.get(f), plotPts, plotD);
                if (plotD && f.equals(ClickMode.Tangent.getTerm1())) plotDerivative(g, term, (tangentLoc));
                g.setColor(fcts.get(f).getColor());
                String s = f + term.varsToString() + " = " + term.toString();
                Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
                int d = g.getFontMetrics().getDescent();
                g.drawString(s, 20, y - d);
                y += (int) bounds.getHeight();
            }
        }
        if (centerPoint != null) {
            plotPoint(g, centerPoint.getX(), centerPoint.getY());
        }
    }

    private void clear(Graphics g) {
        g.setColor(background);
        g.fillRect(0, 0, (int) realXMax, (int) realYMax);
    }

    private void plotStat(Graphics g, String sdf) {
        PlotList plot = context.getList(sdf);
        for (int t = 0; t < plot.size(); t++) {
            plotPoint(g, plot.x().get(t), plot.y().get(t));
        }
    }

    private void plotFunction(Graphics2D g2, Term term, DrawInfo info, boolean pp, boolean pd) {
        double lastx = xMin, lasty = term.solve(lastx);
        g2.setColor(info.getColor());
        if (1 / xScale > 0) for (double i = xMin; i <= xMax; i += 3 / xScale) {
            double x = i;
            double y = term.solve(i);
            if (!Double.isNaN(y) && !Double.isInfinite(y) && !Double.isNaN(lasty) && !Double.isInfinite(lasty)) {
                if ((y >= yMin && y <= yMax) || (lasty >= yMin && lasty <= yMax)) g2.drawLine((int) scrX(x), (int) scrY(y), (int) scrX(lastx), (int) scrY(lasty));
            }
            lastx = x;
            lasty = y;
        }
        if (pp) plotVerteces(g2, term);
    }

    private void plotParametric(Graphics2D g, String f, DrawInfo info) {
        g.setColor(info.getColor());
        Parametric p = context.getParametric(f);
        Term xt = null;
        Term yt = null;
        double lastx = xt.solve(p.tMin()), lasty = yt.solve(p.tMin());
        if (p.tStep() > 0) for (double i = p.tMin(); i <= p.tMax(); i += p.tStep()) {
            double x = xt.solve(i);
            double y = yt.solve(i);
            if (!Double.isNaN(y) && !Double.isInfinite(y) && !Double.isNaN(lasty) && !Double.isInfinite(lasty)) {
                g.drawLine((int) scrX(x), (int) scrY(y), (int) scrX(lastx), (int) scrY(lasty));
            }
            lastx = x;
            lasty = y;
        }
    }

    public void plotVerteces(Graphics g2, Term term) {
        double[] verteces = term.getDerivative().solve_inverse(0);
        for (int i = 0; i < verteces.length; i++) {
            plotPoint(g2, verteces[i], term.solve(verteces[i]));
        }
    }

    private void rotateFunction(Graphics g2, Term term) {
        g2.setColor(Color.green);
        double cosa = Math.cos(getAngle());
        double sina = Math.sin(getAngle());
        String s = "f(x) = " + term.toString();
        g2.drawString(s, 20, 20);
        double lastx = cosa * xMin, lasty = sina * xMin - cosa * term.solve(lastx);
        for (double i = xMin; i <= xMax; i += 1 / xScale) {
            double f = term.solve(i);
            double x = cosa * i + f * sina;
            double y = sina * i - f * cosa;
            g2.drawLine((int) scrX(x), (int) scrY(y), (int) scrX(lastx), (int) scrY(lasty));
            lastx = x;
            lasty = y;
        }
    }

    private void plotDerivative(Graphics g, Term term, double cartX) {
        Term deriv = context.engine().tangent(term, cartX);
        g.setColor(Color.blue);
        double lastx = xMin, lasty = deriv.solve(lastx);
        for (double i = xMin; i <= xMax; i += 1 / xScale) {
            double x = i;
            double y = deriv.solve(i);
            g.drawLine((int) scrX(x), (int) scrY(y), (int) scrX(lastx), (int) scrY(lasty));
            lastx = x;
            lasty = y;
        }
    }

    private void drawXaxis(Graphics g) {
        boolean xAxisOnScreen = (yMax >= 0 && yMin <= 0);
        boolean yAxisOnScreen = (xMax >= 0 && xMin <= 0);
        double iStepM = Math.ceil(cartX(0) / xStep);
        while (iStepM * xStep < cartX(realXMax)) {
            if (iStepM != 0) {
                int x = (int) scrX(iStepM * xStep);
                g.setColor(Color.lightGray);
                g.drawLine(x, 0, x, (int) realYMax);
                String s = String.valueOf(context.engine().formatNumber(iStepM * xStep));
                Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
                int a = g.getFontMetrics().getAscent();
                int xDraw = x - (int) bounds.getWidth() / 2;
                int yDraw;
                if (xAxisOnScreen) {
                    double spaceUnder = realYMax - scrY(0);
                    if (spaceUnder > bounds.getHeight() + 10) {
                        yDraw = (int) scrY(0) + 10;
                    } else {
                        yDraw = (int) scrY(0) - 10 - (int) bounds.getHeight();
                    }
                } else {
                    yDraw = (int) realYMax - (int) bounds.getHeight();
                }
                g.setColor(this.background);
                g.fillRect(xDraw, yDraw, (int) bounds.getWidth(), (int) bounds.getHeight());
                g.setColor(Color.black);
                g.drawString(s, xDraw, yDraw + (int) bounds.getHeight() - a / 4);
            }
            iStepM++;
        }
        if (xAxisOnScreen) {
            g.setColor(Color.black);
            g.drawLine(0, (int) scrY(0), (int) realXMax, (int) scrY(0));
        }
    }

    private void drawYaxis(Graphics g) {
        boolean xAxisOnScreen = (yMax >= 0 && yMin <= 0);
        boolean yAxisOnScreen = (xMax >= 0 && xMin <= 0);
        double iStepM = Math.ceil(cartY(0) / yStep);
        while (iStepM * yStep > cartY(realYMax)) {
            if (iStepM != 0) {
                int y = (int) scrY(iStepM * yStep);
                g.setColor(Color.lightGray);
                g.drawLine(0, y, (int) realXMax, y);
                String s = String.valueOf(context.engine().formatNumber(iStepM * yStep));
                Rectangle2D bounds = g.getFontMetrics().getStringBounds(s, g);
                int a = g.getFontMetrics().getAscent();
                int xDraw;
                int yDraw = y - (int) bounds.getHeight() / 2;
                if (yAxisOnScreen) {
                    double spaceLeft = scrX(0);
                    if (spaceLeft > bounds.getWidth() + 10) {
                        xDraw = (int) scrX(0) - 10 - (int) bounds.getWidth();
                    } else {
                        xDraw = (int) scrX(0) + 10 + (int) bounds.getWidth();
                    }
                } else {
                    xDraw = 10;
                }
                g.setColor(this.background);
                g.fillRect(xDraw, yDraw, (int) bounds.getWidth(), (int) bounds.getHeight());
                g.setColor(Color.black);
                g.drawString(s, xDraw, yDraw + (int) bounds.getHeight() - a / 4);
            }
            iStepM--;
        }
        if (yAxisOnScreen) {
            g.setColor(Color.black);
            g.drawLine((int) scrX(0), 0, (int) scrX(0), (int) realYMax);
        }
    }

    public void plotPoint(Graphics g2, double cartX, double cartY) {
        double rx = scrX(cartX), ry = scrY(cartY);
        Color c = g2.getColor();
        g2.setColor(Color.pink);
        g2.fillOval((int) (rx - 2), (int) (ry - 2), 4, 4);
        g2.setColor(Color.blue);
        g2.drawOval((int) (rx - 2), (int) (ry - 2), 4, 4);
        g2.setColor(c);
    }

    public double scrX(double cartX) {
        double x = cartX - xMin;
        return x * xScale;
    }

    public double scrY(double cartY) {
        double y = yMax - cartY;
        return y * yScale;
    }

    public double cartX(double scrX) {
        double x = xMax - xMin;
        return x * scrX / realXMax + xMin;
    }

    public double cartY(double scrY) {
        double y = yMax - yMin;
        return yMax - y * scrY / realYMax;
    }

    public double getAngle() {
        return a;
    }

    public void setAngle(double a) {
        this.a = a;
    }

    public void usePoint(double cartX) {
        clickedX = (int) scrX(cartX);
        Graphics g = getGraphics();
        Term t = context.getTerm("$y");
        clear(g);
        drawXaxis(g);
        drawYaxis(g);
        if (plotD) plotDerivative(g, t, cartX);
        if (plotPts) plotPoint(g, cartX, t.solve(cartX));
    }

    public void setPlotPoints(boolean b) {
        plotPts = b;
    }

    public void setPlotD(boolean b) {
        plotD = b;
    }

    public void setTangentLocation(double cartX) {
        tangentLoc = cartX;
    }

    public double getTangentLocation() {
        return (tangentLoc);
    }

    /**centers the screen on the given point, maintaining scale and step.
     */
    public void centerPoint(double cartX, double cartY) {
        double w = xMax - xMin;
        setXMax(cartX + w / 2);
        setXMin(cartX - w / 2);
        double h = yMax - yMin;
        setYMax(cartY + h / 2);
        setYMin(cartY - h / 2);
        centerPoint = new Point2D.Double(cartX, cartY);
    }

    public Point2D getCenterPoint() {
        return centerPoint;
    }

    /**resets the x maximum and minimum so that the x scale is equivalent
     *to the y scale. center x coordinate on screen is conserved.
     */
    public void squareXStep() {
        double xRange = realXMax * (yMax - yMin) / realYMax;
        double x = (xMax + xMin) / 2;
        setXMax(x + xRange / 2);
        setXMin(x - xRange / 2);
    }

    /**resets the y maximum and minimum so that the y scale is equivalent to
     *the x scale. center y coordinate on screen is conserved.
     */
    public void squareYStep() {
        double yRange = realYMax * (xMax - xMin) / realXMax;
        double y = (yMax + yMin) / 2;
        setYMax(y + yRange / 2);
        setYMin(y - yRange / 2);
    }

    private static void pl(String s) {
        if (display) System.out.println(s);
    }

    public ClickMode getClickMode() {
        return clickMode;
    }

    public void setClickMode(ClickMode clickMode) {
        this.clickMode = clickMode;
        if (clickMode != ClickMode.Tangent) {
            plotD = false;
        }
    }

    public boolean getDoCenter() {
        return center;
    }

    public void setDoCenter(boolean c) {
        center = c;
        myObservable.change("do center");
    }
}
