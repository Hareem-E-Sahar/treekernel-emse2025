package fr.n7.khome.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import fr.n7.khome.core.Fuzzy;
import fr.n7.khome.util.FuzzyFormatException;
import fr.n7.khome.util.InvalidFuzzyValueException;

public class JFuzzy extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private static final long serialVersionUID = -5267948464878688246L;

    private Fuzzy fuzzy;

    protected JRequestField field;

    JCheckBox positiveInfiniteButton;

    JCheckBox negativeInfiniteButton;

    protected boolean drawLabels;

    protected boolean editable;

    protected int f1;

    protected int f2;

    protected int f3;

    protected int f4;

    protected int ph;

    protected int ph0;

    protected int ph1;

    protected Color oneColor;

    protected Color zeroColor;

    protected float lastFiniteA;

    protected float lastFiniteB;

    protected int mouseX;

    protected Vector<ChangeListener> listeners;

    public JFuzzy(boolean editable) {
        this(editable, Fuzzy.whateverFuzzy());
    }

    public JFuzzy(boolean editable, Fuzzy fuzzy, boolean drawLabels) {
        this(editable, fuzzy);
        this.drawLabels = drawLabels;
    }

    public JFuzzy(boolean editable, boolean drawLabels) {
        this(editable);
        this.drawLabels = drawLabels;
    }

    public JFuzzy(boolean editable, Fuzzy fuzzy) {
        super(new BorderLayout());
        this.editable = true;
        this.fuzzy = fuzzy;
        drawLabels = true;
        this.field = new JRequestField() {

            private static final long serialVersionUID = -2451332686481483782L;

            public void paintComponent(Graphics g) {
                paintComponent2(g);
            }
        };
        field.setDoubleBuffered(true);
        if (editable) {
            field.addMouseListener(this);
            field.addMouseMotionListener(this);
            field.setLayout(new BorderLayout());
            JPanel buttonsPanel = new JPanel(new BorderLayout());
            positiveInfiniteButton = new JCheckBox("+∞");
            positiveInfiniteButton.addActionListener(this);
            negativeInfiniteButton = new JCheckBox("-∞");
            negativeInfiniteButton.addActionListener(this);
            buttonsPanel.add(positiveInfiniteButton, BorderLayout.EAST);
            buttonsPanel.add(negativeInfiniteButton, BorderLayout.WEST);
            add(buttonsPanel, BorderLayout.NORTH);
        }
        lastFiniteA = 1;
        lastFiniteB = 10;
        listeners = new Vector<ChangeListener>();
        zeroColor = new Color(255, 55, 30);
        oneColor = new Color(75, 255, 50);
        add(field, BorderLayout.CENTER);
    }

    public void setEditable(boolean editable) {
        if (this.editable != editable) {
            this.editable = editable;
            if (editable) {
                field.addMouseListener(this);
                field.addMouseMotionListener(this);
            } else {
                field.removeMouseListener(this);
                field.removeMouseMotionListener(this);
            }
        }
    }

    public Fuzzy getFuzzy() {
        return fuzzy;
    }

    public void setFuzzy(float a, float b, float alpha, float beta, boolean notify) {
        if (Float.compare(0, a) != 0 && !Float.isInfinite(a)) {
            alpha = a * Fuzzy.INCREMENT / 100 * Math.round(alpha / a / Fuzzy.INCREMENT * 100);
        }
        if (Float.compare(0, b) != 0 && !Float.isInfinite(b)) {
            beta = b * Fuzzy.INCREMENT / 100 * Math.round(beta / b / Fuzzy.INCREMENT * 100);
        }
        try {
            fuzzy = new Fuzzy(a, b, alpha, beta);
        } catch (InvalidFuzzyValueException e) {
            e.printStackTrace();
        }
        boolean finiteA = Float.compare(a, Float.NEGATIVE_INFINITY) != 0;
        boolean finiteB = Float.compare(b, Float.POSITIVE_INFINITY) != 0;
        if (negativeInfiniteButton != null && positiveInfiniteButton != null) {
            negativeInfiniteButton.setSelected(!finiteA);
            positiveInfiniteButton.setSelected(!finiteB);
        }
        if (finiteA) {
            lastFiniteA = a;
        }
        if (finiteB) {
            lastFiniteB = b;
        }
        if (notify) notifyListenersWith(fuzzy);
        repaint();
    }

    public void setFuzzy(Fuzzy f, boolean notify) {
        setFuzzy(f.getA(), f.getB(), f.getAlpha(), f.getBeta(), notify);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public void notifyListenersWith(Fuzzy value) {
        for (ChangeListener changeListener : listeners) {
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    enum Handle {

        Alpha, A, B, Beta, None
    }

    ;

    enum Mode {

        Normal, AInfinite, BInfinite, BothInfinite, singleValue
    }

    ;

    private Fuzzy oldFuzzy;

    private Handle holdHandle;

    private Mode mode;

    @Override
    public void mousePressed(MouseEvent e) {
        oldFuzzy = fuzzy;
        float alpha = oldFuzzy.getAlpha();
        float a = oldFuzzy.getA();
        float b = oldFuzzy.getB();
        float beta = oldFuzzy.getBeta();
        int w0 = field.w0;
        int w = field.w;
        Point p = e.getPoint();
        if (Float.compare(a, b) == 0 && Float.compare(beta, alpha) == 0 && Float.compare(beta, 0.0f) == 0) {
            mode = Mode.singleValue;
            if (p.getX() < (int) (w0 + w * 0.45)) {
                holdHandle = Handle.Alpha;
            } else if (p.getX() < (int) (w0 + w * 0.50)) {
                holdHandle = Handle.A;
            } else if (p.getX() < (int) (w0 + w * 0.55)) {
                holdHandle = Handle.B;
            } else {
                holdHandle = Handle.Beta;
            }
            fu1 = w0 + w / 2;
            fu2 = fu1;
            fu3 = w0 + w / 2;
            fu4 = fu3;
        } else {
            mode = Mode.Normal;
            if (p.getX() < f2 - (f2 - f1) / 2) {
                holdHandle = Handle.Alpha;
            } else if (p.getX() < f3 - (f3 - f2) / 2) {
                holdHandle = Handle.A;
            } else if (p.getX() < f4 - (f4 - f3) / 2) {
                holdHandle = Handle.B;
            } else {
                holdHandle = Handle.Beta;
            }
            boolean negativeInfinity = Float.compare(a, Float.NEGATIVE_INFINITY) == 0;
            boolean positiveInfinity = Float.compare(b, Float.POSITIVE_INFINITY) == 0;
            double coreWidth = b - a;
            double supportWidth = coreWidth + beta + alpha;
            double widthProportion;
            double displayedProportion = field.displayedProportion;
            widthProportion = w * displayedProportion / supportWidth;
            if (negativeInfinity) {
                mode = Mode.AInfinite;
                widthProportion = w * displayedProportion / (b + beta);
                fu1 = w0;
                fu2 = fu1;
            } else {
                fu1 = (int) (w * (1 - displayedProportion) / 2);
                fu2 = fu1 + (int) (widthProportion * alpha);
            }
            if (positiveInfinity) {
                if (negativeInfinity) {
                    mode = Mode.BothInfinite;
                } else {
                    mode = Mode.BInfinite;
                }
                widthProportion = w * displayedProportion / (a + alpha);
                fu3 = w;
                fu4 = fu3;
            } else {
                fu4 = (int) (w - (w * (1 - displayedProportion) / 2));
                fu3 = fu4 - (int) (widthProportion * beta);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        holdHandle = Handle.None;
        repaint();
    }

    @Override
    public void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        field.addMouseListener(l);
    }

    @Override
    public void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        field.removeMouseListener(l);
    }

    private int fu1, fu2, fu3, fu4;

    @Override
    public void mouseDragged(MouseEvent e) {
        Point p = e.getPoint();
        float alpha = oldFuzzy.getAlpha();
        float a = oldFuzzy.getA();
        float b = oldFuzzy.getB();
        float beta = oldFuzzy.getBeta();
        double coreWidth = b - a;
        double supportWidth = coreWidth + beta + alpha;
        double widthProportion = 1;
        int w0 = field.w0;
        int w = field.w;
        double displayedProportion = field.displayedProportion;
        if (mode == Mode.AInfinite) {
            widthProportion = w * displayedProportion / (b == 0 ? 1 : b);
        } else if (mode == Mode.BInfinite) {
            widthProportion = w * displayedProportion / (a == 0 ? 1 : a);
        } else {
            widthProportion = w * displayedProportion / supportWidth;
        }
        mouseX = (int) p.getX();
        float speedPower = 0.3f;
        if (mode == Mode.BothInfinite) {
        } else if (mode == Mode.singleValue && (holdHandle == Handle.A || holdHandle == Handle.B)) {
            alpha = a * Fuzzy.INCREMENT / 100;
            beta = b * Fuzzy.INCREMENT / 100;
            int x = (int) (p.getX()) - (w0 + w / 2);
            if (x < 0) {
                widthProportion = w * displayedProportion / (a == 0 ? 1 : a);
                double newA = Math.max(Math.round(Math.min(a + x * Math.pow(Math.abs(x), speedPower) / widthProportion, b)), 0);
                double newAlpha = Math.abs(alpha / a * newA);
                setFuzzy((float) newA, b, (float) newAlpha, beta, true);
            } else {
                widthProportion = w * displayedProportion / (a == 0 ? 1 : a);
                double newB = Math.max(Math.round(Math.max(b + x * Math.pow(Math.abs(x), speedPower) / widthProportion, a)), 0);
                double newBeta = Math.abs(beta / b * newB);
                setFuzzy(a, (float) newB, alpha, (float) newBeta, true);
            }
        } else if (holdHandle == Handle.Alpha) {
            int x = -(int) (p.getX()) + fu1;
            double newAlpha = Math.max(alpha + x * Math.pow(Math.abs(x), speedPower) / (w * displayedProportion / a), 0);
            setFuzzy(a, b, (float) newAlpha, beta, true);
        } else if (holdHandle == Handle.A) {
            int x = (int) (p.getX()) - fu2;
            double newA = Math.max(Math.round(Math.min(a + x * Math.pow(Math.abs(x), speedPower) / widthProportion, b)), 0);
            double newAlpha = a == 0 ? alpha : Math.abs(alpha / a * newA);
            setFuzzy((float) newA, b, (float) newAlpha, beta, true);
        } else if (holdHandle == Handle.B) {
            int x = (int) (p.getX()) - fu3;
            double newB = Math.max(Math.round(Math.max(b + x * Math.pow(Math.abs(x), speedPower) / widthProportion, a)), 0);
            double newBeta = b == 0 ? beta : Math.abs(beta / b * newB);
            setFuzzy(a, (float) newB, alpha, (float) newBeta, true);
        } else if (holdHandle == Handle.Beta) {
            int x = (int) (p.getX()) - fu4;
            double newBeta = Math.max(beta + x * Math.pow(Math.abs(x), speedPower) / (w * displayedProportion / b), 0);
            setFuzzy(a, b, alpha, (float) newBeta, true);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        float a = fuzzy.getA();
        float alpha = fuzzy.getAlpha();
        float b = fuzzy.getB();
        float beta = fuzzy.getBeta();
        if (negativeInfiniteButton.isSelected()) {
            if (Float.compare(a, Float.NEGATIVE_INFINITY) != 0) {
                a = Float.NEGATIVE_INFINITY;
            }
        } else {
            if (Float.compare(a, Float.NEGATIVE_INFINITY) == 0) {
                a = Math.min(lastFiniteA, b);
                if (Float.compare(alpha, 0f) == 0) alpha = a / 10;
            }
        }
        if (positiveInfiniteButton.isSelected()) {
            if (Float.compare(b, Float.POSITIVE_INFINITY) != 0) {
                b = Float.POSITIVE_INFINITY;
            }
        } else {
            if (Float.compare(b, Float.POSITIVE_INFINITY) == 0) {
                b = Math.max(lastFiniteB, a);
                if (Float.compare(beta, 0f) == 0) beta = b / 10;
            }
        }
        try {
            setFuzzy(new Fuzzy(a, b, alpha, beta), true);
        } catch (InvalidFuzzyValueException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void repaint() {
        super.repaint();
        if (field != null) field.repaint();
    }

    public void paintComponent2(Graphics graphics) {
        paintComponent(graphics);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        field.initializePaintComponent(graphics);
        int w = field.w;
        int w0 = field.w0;
        double displayedProportion = field.displayedProportion;
        int h0 = field.h0;
        int h = field.h;
        double heightProportion = field.heightProportion;
        h0 = drawLabels ? (graphics.getFontMetrics().getHeight()) : h0;
        h = (int) (field.getHeight() - 2 * h0);
        ph = (int) (h * heightProportion);
        ph0 = (h - ph) / 2;
        ph1 = (h + ph) / 2;
        ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean negativeInfinity = Float.compare(fuzzy.getA(), Float.NEGATIVE_INFINITY) == 0;
        boolean positiveInfinity = Float.compare(fuzzy.getB(), Float.POSITIVE_INFINITY) == 0;
        if (Float.compare(fuzzy.getA(), fuzzy.getB()) == 0 && Float.compare(fuzzy.getBeta(), fuzzy.getAlpha()) == 0 && Float.compare(fuzzy.getBeta(), 0.0f) == 0) {
            graphics.setColor(zeroColor);
            graphics.fillRect(w0, h0 + ph0, w, ph);
            graphics.setColor(Color.black);
            graphics.drawRect(w0, h0 + ph0, w, ph);
            double m = w / 2;
            graphics.setColor(oneColor);
            graphics.fillRect((int) (w0 + m * 0.9), h0, (int) (m * 0.2), h);
            graphics.setColor(Color.black);
            graphics.drawRect((int) (w0 + m * 0.9), h0, (int) (m * 0.2), h);
            graphics.setFont(new Font(graphics.getFont().getFontName(), Font.BOLD, 16));
            field.drawLabel(w / 2, graphics, "" + fuzzy.getA(), h0 + h / 2, -.5f, new Color(220, 220, 220), 1);
        } else {
            if (negativeInfinity && positiveInfinity) {
                graphics.setColor(oneColor);
                graphics.fillRect(w0, h0, w, h);
                graphics.setColor(Color.black);
                graphics.drawLine(w0, h0, w0, h0 + h);
                graphics.drawLine(w0, h0, w0 + w, h0);
                graphics.drawLine(w0, h0 + h, w0 + w, h0 + h);
                graphics.drawLine(w0 + w, h0, w0 + w, h0 + h);
                graphics.setFont(new Font(graphics.getFont().getFontName(), Font.BOLD, 16));
                field.drawLabel(w / 2, graphics, " ? ", h0 + h / 2, -.5f, new Color(220, 220, 220), 1);
            } else {
                double coreWidth = fuzzy.getB() - fuzzy.getA();
                double supportWidth = coreWidth + fuzzy.getBeta() + fuzzy.getAlpha();
                double widthProportion;
                if (negativeInfinity) {
                    widthProportion = w * displayedProportion / (fuzzy.getB() + fuzzy.getBeta());
                } else if (positiveInfinity) {
                    widthProportion = w * displayedProportion / (fuzzy.getA() + fuzzy.getAlpha());
                } else {
                    widthProportion = w * displayedProportion / supportWidth;
                }
                if (negativeInfinity) {
                    f1 = w0;
                    f2 = f1;
                } else {
                    f1 = (int) (w * (1 - displayedProportion) / 2);
                    f2 = f1 + (int) (widthProportion * fuzzy.getAlpha());
                }
                if (positiveInfinity) {
                    f3 = w;
                    f4 = f3;
                } else {
                    f4 = (int) (w - (w * (1 - displayedProportion) / 2));
                    f3 = f4 - (int) (widthProportion * fuzzy.getBeta());
                }
                if (!negativeInfinity) {
                    graphics.setColor(zeroColor);
                    graphics.fillRect(w0, h0 + ph0, f1, ph);
                    graphics.setColor(Color.black);
                    graphics.drawLine(w0, h0 + ph0, w0 + f1, h0 + ph0);
                    graphics.drawLine(w0, h0 + ph0, w0, h0 + ph1);
                    graphics.drawLine(w0, h0 + ph1, w0 + f1, h0 + ph1);
                    double val;
                    double alpha = f2 - f1;
                    double ra = (oneColor.getRed() - zeroColor.getRed()) / alpha;
                    double ga = (oneColor.getGreen() - zeroColor.getGreen()) / alpha;
                    double ba = (oneColor.getBlue() - zeroColor.getBlue()) / alpha;
                    double aa = (oneColor.getAlpha() - zeroColor.getAlpha()) / alpha;
                    int rb = zeroColor.getRed();
                    int gb = zeroColor.getGreen();
                    int bb = zeroColor.getBlue();
                    int ab = zeroColor.getAlpha();
                    for (int i = 0; i < alpha; i++) {
                        val = i / alpha;
                        graphics.setColor(new Color(rb + (int) (i * ra), gb + (int) (i * ga), bb + (int) (i * ba), ab + (int) (i * aa)));
                        graphics.drawLine(w0 + f1 + i, h0 + (int) ((1 - val) * ph0), w0 + f1 + i, h0 + ph1 + (int) (val * ph0));
                    }
                    graphics.setColor(Color.black);
                    graphics.drawLine(w0 + f1, h0 + ph0, w0 + f2, h0);
                    graphics.drawLine(w0 + f1, h0 + ph1, w0 + f2, h0 + ph0 + ph1);
                }
                graphics.setColor(oneColor);
                graphics.fillRect(w0 + f2, h0, f3 - f2, h);
                graphics.setColor(Color.black);
                graphics.drawLine(w0 + f2, h0, w0 + f3, h0);
                graphics.drawLine(w0 + f2, h0 + ph0 + ph1, w0 + f3, h0 + ph0 + ph1);
                if (!positiveInfinity) {
                    double beta = f4 - f3;
                    double ra = (oneColor.getRed() - zeroColor.getRed()) / beta;
                    double ga = (oneColor.getGreen() - zeroColor.getGreen()) / beta;
                    double ba = (oneColor.getBlue() - zeroColor.getBlue()) / beta;
                    double aa = (oneColor.getAlpha() - zeroColor.getAlpha()) / beta;
                    int rb = oneColor.getRed();
                    int gb = oneColor.getGreen();
                    int bb = oneColor.getBlue();
                    int ab = oneColor.getAlpha();
                    for (int i = 0; i < beta; i++) {
                        double val = i / beta;
                        graphics.setColor(new Color(rb - (int) (i * ra), gb - (int) (i * ga), bb - (int) (i * ba), ab - (int) (i * aa)));
                        graphics.drawLine(w0 + f3 + i, h0 + (int) ((val) * ph0), w0 + f3 + i, h0 + ph1 + (int) ((1 - val) * ph0));
                    }
                    graphics.setColor(Color.black);
                    graphics.drawLine(w0 + f3, h0, w0 + f4, h0 + ph0);
                    graphics.drawLine(w0 + f3, h0 + ph0 + ph1, w0 + f4, h0 + ph1);
                    graphics.setColor(zeroColor);
                    graphics.fillRect(w0 + f4, h0 + ph0, w - f4, ph);
                    graphics.setColor(Color.black);
                    graphics.drawLine(w0 + f4, h0 + ph0, w0 + w, h0 + ph0);
                    graphics.drawLine(w0 + f4, h0 + ph1, w0 + w, h0 + ph1);
                    graphics.drawLine(w0 + w, h0 + ph0, w0 + w, h0 + ph1);
                }
                graphics.setColor(Color.black);
                if (!negativeInfinity) {
                    graphics.fill3DRect(w0 + f1 - 2, h0 + ph0, 4, ph, true);
                    graphics.fill3DRect(w0 + f2 - 2, h0, 4, h, true);
                }
                if (!positiveInfinity) {
                    graphics.fill3DRect(w0 + f3 - 2, h0, 4, h, true);
                    graphics.fill3DRect(w0 + f4 - 2, h0 + ph0, 4, ph, true);
                }
                boolean equal = Float.compare(fuzzy.getA(), fuzzy.getB()) == 0;
                if (drawLabels) {
                    String aString;
                    if (Float.compare(fuzzy.getA(), Float.NEGATIVE_INFINITY) == 0) {
                        aString = "-∞";
                    } else {
                        aString = "" + fuzzy.getA();
                    }
                    field.drawTopBottomLabel(f2, graphics, aString, false);
                    float alpha = Math.round(Math.abs(1000 * fuzzy.getAlpha() / fuzzy.getA())) / 10;
                    field.drawTopBottomLabel(f1, graphics, alpha + "%", true, new Color(255, 255, 255, 100));
                    float beta = Math.round(Math.abs(1000 * fuzzy.getBeta() / fuzzy.getB())) / 10;
                    field.drawTopBottomLabel(f4, graphics, beta + "%", false, new Color(255, 255, 255, 100));
                    if (!equal) {
                        String bString;
                        if (Float.compare(fuzzy.getB(), Float.POSITIVE_INFINITY) == 0) {
                            bString = "+∞";
                        } else {
                            bString = "" + fuzzy.getB();
                        }
                        field.drawTopBottomLabel(f3, graphics, bString, true);
                    }
                }
            }
            if (holdHandle != Handle.None) {
                if (holdHandle == Handle.A || holdHandle == Handle.B) {
                    graphics.setColor(new Color(0, 0, 0, 50));
                    graphics.fillRect(mouseX - 2, h0, 4, h);
                } else {
                    graphics.setColor(new Color(0, 0, 0, 50));
                    graphics.fillRect(mouseX - 2, h0 + ph0, 4, ph);
                }
            }
        }
    }

    public static void main(String[] args) throws FuzzyFormatException, InvalidFuzzyValueException {
        JFrame f = new JFrame();
        f.pack();
        f.setSize(new Dimension(600, 300));
        f.setVisible(true);
        JFuzzy jf = new JFuzzy(true, Fuzzy.parse("~5-~~10"));
        f.add(jf);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
