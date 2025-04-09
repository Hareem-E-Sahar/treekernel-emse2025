package fr.cephb.lindenb.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.event.MouseInputAdapter;
import org.lindenb.io.PreferredDirectory;
import org.lindenb.lang.ThrowablePane;
import org.lindenb.swing.ObjectAction;
import org.lindenb.swing.SwingUtils;
import org.lindenb.util.Compilation;

/**
 * Graph4NCBI
 * @author lindenb
 *
 */
public class Graph4NCBI extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final int ICON_SIZE = 50;

    /**
 * A Node in the drawing area
 * @author lindenb
 *
 */
    private class Icon {

        Point2D.Double goal = randomPoint();

        ;

        Point2D.Double current = randomPoint();

        ;

        Node node;

        int countLinks = -1;

        Icon(Node node) {
            this.node = node;
        }

        int getPartnerCount() {
            if (countLinks == -1) countLinks = getNode().getPartners().size();
            return countLinks;
        }

        Node getNode() {
            return this.node;
        }

        String getTitle() {
            return getNode().getTitle();
        }

        boolean isMoving() {
            return goal.distance(current) >= 1.5;
        }

        public boolean isVisible() {
            return true;
        }

        public Shape getShape() {
            return new Rectangle((int) (current.x - (ICON_SIZE / 2)), (int) (current.y - (ICON_SIZE / 2)), ICON_SIZE, ICON_SIZE);
        }

        public void paint(Graphics2D g) {
            Shape shape = getShape();
            switch(getNode().getDatabase()) {
                case pubmed:
                    g.setColor(Color.CYAN);
                    break;
                case snp:
                    g.setColor(Color.PINK);
                    break;
                case gene:
                    g.setColor(Color.LIGHT_GRAY);
                    break;
                case omim:
                    g.setColor(Color.GREEN);
                    break;
                case unigene:
                    g.setColor(Color.ORANGE);
                    break;
                default:
                    g.setColor(Color.WHITE);
                    break;
            }
            g.fill(shape);
            if (visitedIcons.contains(this)) {
                Stroke stroke = g.getStroke();
                g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(Color.MAGENTA);
                g.draw(shape);
                g.setStroke(stroke);
            } else {
                g.setColor(Color.BLUE);
                g.draw(shape);
            }
            g.setFont(new Font("Courier", Font.PLAIN, 9));
            String s = getTitle();
            if (s.length() > 10) {
                s = s.substring(0, 10) + "...";
            }
            g.setColor(Color.YELLOW);
            paintBanner(g, s, current.x, current.y);
            g.setColor(Color.WHITE);
            paintBanner(g, getNode().getURI(), current.x, current.y + ICON_SIZE / 2);
        }

        public void paintBanner(Graphics2D g, String s, double cx, double cy) {
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(s) + 10;
            int h = fm.getHeight() + 4;
            g.fillRect((int) cx - w / 2, (int) cy - h / 2, w, h);
            g.setColor(Color.black);
            g.drawRect((int) cx - w / 2, (int) cy - h / 2, w - 1, h - 1);
            g.drawString(s, (int) cx - (w - 10) / 2, (int) (cy - (h - 4) / 2) + fm.getAscent());
        }

        public boolean contains(int x, int y) {
            return getShape().contains(x, y);
        }

        @Override
        public int hashCode() {
            return getNode().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            return getNode().equals(Icon.class.cast(obj).getNode());
        }

        public URL getPage() {
            return getNode().getIdentifier().getURL();
        }
    }

    class LayoutWorker extends SwingWorker<Object, Object> {

        @Override
        protected Object doInBackground() throws Exception {
            final double step = 0.001;
            boolean indimoving = true;
            int loop = 0;
            while (indimoving) {
                if (isDone()) break;
                indimoving = false;
                ++loop;
                for (Icon icon : icons) {
                    if (isDone()) break;
                    if (!icon.isMoving()) {
                        continue;
                    }
                    indimoving = true;
                    icon.current.setLocation(icon.current.x + (step * (icon.goal.x - icon.current.x)), icon.current.y + (step * (icon.goal.y - icon.current.y)));
                    if (icon.goal.distance(icon.current) < 1.5) {
                        icon.current.x = icon.goal.x;
                        icon.current.y = icon.goal.y;
                    }
                }
                drawingArea.repaint();
                if (!indimoving) break;
            }
            for (Icon icon : icons) {
                icon.current.x = icon.goal.x;
                icon.current.y = icon.goal.y;
            }
            return null;
        }

        @Override
        protected void done() {
            drawingArea.repaint();
        }
    }

    private JPanel drawingArea;

    private Vector<Graph> graphs = new Vector<Graph>();

    private Vector<Icon> icons = new Vector<Icon>();

    private HashMap<Node, Icon> node2icon = new HashMap<Node, Icon>();

    /** history stack */
    private Vector<Icon> historyStack;

    /** position in history */
    private int position_in_history = -1;

    /** backward History Action */
    private AbstractAction backwardHistoryAction;

    /** forward History Action */
    private AbstractAction forwardHistoryAction;

    /** offscreen image */
    private BufferedImage offscreenImage = null;

    /** offscreen graphics */
    private Graphics2D offscreenGraphics = null;

    /** animation worker */
    private LayoutWorker relaxer = new LayoutWorker();

    /** label */
    private JLabel mainLabel;

    /** visited */
    private HashSet<Icon> visitedIcons = new HashSet<Icon>();

    /**
	 * Graph4NCBI
	 */
    private Graph4NCBI(Vector<Graph> graphs) throws ClassNotFoundException {
        super("Graph4NCBI");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.graphs.addAll(graphs);
        for (Graph g : this.graphs) {
            for (Node n : g.getNodes()) {
                Icon icn = new Icon(n);
                this.icons.addElement(icn);
                this.node2icon.put(n, icn);
            }
        }
        JPanel contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                SwingUtils.center(e.getWindow(), 100);
                focusGraph(Graph4NCBI.this.graphs.firstElement());
            }

            @Override
            public void windowClosing(WindowEvent e) {
                doMenuClose();
            }
        });
        JPanel topPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        contentPane.add(topPane, BorderLayout.NORTH);
        topPane.add(new JButton(this.backwardHistoryAction = new AbstractAction("Back") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                setHistoryIndex(Graph4NCBI.this.position_in_history - 1);
            }
        }));
        topPane.add(new JButton(this.forwardHistoryAction = new AbstractAction("Next") {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                setHistoryIndex(Graph4NCBI.this.position_in_history + 1);
            }
        }));
        this.historyStack = new Vector<Icon>();
        this.forwardHistoryAction.setEnabled(false);
        this.backwardHistoryAction.setEnabled(false);
        this.mainLabel = new JLabel();
        this.mainLabel.setFont(new Font("Helvetica", Font.BOLD, 18));
        this.mainLabel.setForeground(Color.BLUE);
        topPane.add(this.mainLabel);
        JMenuBar bar = new JMenuBar();
        setJMenuBar(bar);
        JMenu menu = new JMenu("File");
        bar.add(menu);
        menu.add(new AbstractAction("About") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(Graph4NCBI.this, Compilation.getLabel() + " Pierre Lindenbaum PhD 2008. plindenbaum@yahoo.fr");
            }
        });
        menu.add(new JSeparator());
        menu.add(new AbstractAction("Quit") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                doMenuClose();
            }
        });
        menu = new JMenu("Graph");
        bar.add(menu);
        menu.add(new AbstractAction("Show All") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                focusGraph(null);
            }
        });
        menu.add(new JSeparator());
        for (Graph graph : this.graphs) {
            menu.add(new ObjectAction<Graph>(graph, String.valueOf(graph.getId())) {

                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    focusGraph(getObject());
                }
            });
        }
        JPanel pane1 = new JPanel(new GridLayout(1, 0, 5, 5));
        contentPane.add(pane1, BorderLayout.CENTER);
        this.drawingArea = new JPanel(true) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                paintDrawingArea(Graphics2D.class.cast(g));
            }

            public String getToolTipText(MouseEvent e) {
                Icon icon = findIconAt(e.getX(), e.getY());
                if (icon != null) return icon.getTitle();
                return null;
            }
        };
        this.drawingArea.setToolTipText("");
        this.drawingArea.setOpaque(true);
        this.drawingArea.setBackground(Color.BLACK);
        this.drawingArea.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                focusGraph(null);
                e.getComponent().removeComponentListener(this);
            }
        });
        MouseInputAdapter mouseListener = new MouseInputAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                Icon icon = findIconAt(e.getX(), e.getY());
                if (icon == null) return;
                JPopupMenu pop = new JPopupMenu();
                AbstractAction action = new ObjectAction<Icon>(icon, "Open URL ") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Desktop.getDesktop().browse(getObject().getNode().getURL().toURI());
                        } catch (Exception e1) {
                            ThrowablePane.show(drawingArea, e1);
                        }
                    }
                };
                if (icon == null || !Desktop.isDesktopSupported()) {
                    action.setEnabled(false);
                }
                pop.add(action);
                pop.show(e.getComponent(), e.getX(), e.getY());
            }

            public void mouseClicked(MouseEvent e) {
                Icon icon = findIconAt(e.getX(), e.getY());
                if (icon == null) return;
                if (e.getClickCount() < 2 && e.isShiftDown()) {
                    try {
                        Desktop.getDesktop().browse(icon.getNode().getURL().toURI());
                    } catch (Exception e1) {
                        ThrowablePane.show(drawingArea, e1);
                    }
                    return;
                }
                setHistory(icon);
            }
        };
        this.drawingArea.addMouseListener(mouseListener);
        this.drawingArea.addMouseMotionListener(mouseListener);
        pane1.add(new JScrollPane(this.drawingArea));
    }

    private static Point2D.Double randomPoint() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int L = Math.max(screen.width, screen.height) * 2;
        double a = Math.PI * 2.0 * Math.random();
        return new Point2D.Double(screen.width / 2.0 + L * Math.cos(a), screen.height / 2.0 + L * Math.sin(a));
    }

    private void paintDrawingArea(Graphics2D g) {
        if (this.offscreenImage == null || this.offscreenImage.getWidth(this) != this.getWidth() || this.offscreenImage.getHeight(this) != this.getHeight()) {
            if (this.offscreenGraphics != null) this.offscreenGraphics.dispose();
            this.offscreenImage = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
            this.offscreenGraphics = this.offscreenImage.createGraphics();
        }
        this.offscreenGraphics.setColor(drawingArea.getBackground());
        this.offscreenGraphics.fillRect(0, 0, getWidth(), getHeight());
        for (Icon icn : this.icons) {
            paintIcon(this.offscreenGraphics, icn);
        }
        g.drawImage(this.offscreenImage, 0, 0, this);
    }

    private void paintIcon(Graphics2D g, Icon id) {
        id.paint(g);
    }

    private void focusGraph(Graph g) {
        HashSet<Icon> set = new HashSet<Icon>();
        if (g == null) {
            mainLabel.setText("");
            set.addAll(this.icons);
        } else {
            mainLabel.setText("Graph   :  " + g.getId());
            for (Node n : g.getNodes()) {
                set.add(node2icon.get(n));
            }
        }
        layoutIcons(set);
    }

    private void focusIcon(Icon focused) {
        URL page = null;
        HashSet<Icon> icon2beshown = new HashSet<Icon>();
        if (focused == null) {
            icon2beshown.addAll(this.icons);
            mainLabel.setText("");
        } else {
            for (Node n : focused.getNode().getPartners()) {
                icon2beshown.add(this.node2icon.get(n));
            }
            mainLabel.setText(focused.getTitle());
            page = focused.getPage();
        }
        if (page == null) {
        } else {
        }
        this.visitedIcons.add(focused);
        layoutIcons(icon2beshown);
    }

    private synchronized void layoutIcons(Set<Icon> icon2beshown) {
        Collections.sort(this.icons, new Comparator<Icon>() {

            @Override
            public int compare(Icon o1, Icon o2) {
                return o2.getPartnerCount() - o1.getPartnerCount();
            }
        });
        int width = this.drawingArea.getSize().width;
        if (width < 50) width = Toolkit.getDefaultToolkit().getScreenSize().width - 200;
        int half = ICON_SIZE / 2;
        int margin = ICON_SIZE + half;
        int x = 0;
        int y = half + ICON_SIZE;
        for (Icon icn : this.icons) {
            if (icon2beshown.contains(icn)) {
                x += ICON_SIZE + half;
                if (x + half > width) {
                    x = margin;
                    y += ICON_SIZE + half;
                }
                icn.goal.x = x;
                icn.goal.y = y;
            } else {
                icn.goal = randomPoint();
            }
        }
        start();
    }

    private synchronized void start() {
        if (this.relaxer != null) {
            this.relaxer.cancel(true);
        }
        this.relaxer = new LayoutWorker();
        relaxer.execute();
    }

    private void setHistory(Icon icn) {
        ++this.position_in_history;
        this.historyStack.setSize(this.position_in_history + 1);
        this.historyStack.setElementAt(icn, this.position_in_history);
        this.forwardHistoryAction.setEnabled(false);
        this.backwardHistoryAction.setEnabled(this.position_in_history > 0);
        focusIcon(icn);
    }

    private void setHistoryIndex(int index) {
        if (index < 0 || index >= historyStack.size()) return;
        this.position_in_history = index;
        backwardHistoryAction.setEnabled(this.position_in_history != 0);
        forwardHistoryAction.setEnabled(this.position_in_history + 1 < historyStack.size());
        focusIcon(historyStack.elementAt(this.position_in_history));
    }

    private Icon findIconAt(int x, int y) {
        for (Icon id : this.icons) {
            if (id.contains(x, y)) return id;
        }
        return null;
    }

    private void doMenuClose() {
        this.setVisible(false);
        this.dispose();
        if (this.relaxer != null) this.relaxer.cancel(true);
    }

    /**
 * main
 * @param args
 */
    public static void main(String[] args) {
        try {
            int optind = 0;
            while (optind < args.length) {
                if (args[optind].equals("-h")) {
                    System.err.println(Compilation.getLabel());
                    System.err.println(" -h this screen");
                    return;
                } else if (args[optind].equals("--")) {
                    optind++;
                    break;
                } else if (args[optind].startsWith("-")) {
                    System.err.println("Unknown option " + args[optind]);
                } else {
                    break;
                }
                ++optind;
            }
            Vector<Graph> graphs = new Vector<Graph>();
            if (optind == args.length) {
                JFileChooser chooser = new JFileChooser(PreferredDirectory.getPreferredDirectory());
                chooser.setMultiSelectionEnabled(true);
                if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
                for (File f : chooser.getSelectedFiles()) {
                    PreferredDirectory.setPreferredDirectory(f);
                    graphs.addAll(Graph.parse(f));
                }
            } else {
                while (optind < args.length) {
                    graphs.addAll(Graph.parse(new File(args[optind++])));
                }
            }
            if (graphs.isEmpty()) {
                System.err.println("No Input");
            }
            JFrame.setDefaultLookAndFeelDecorated(true);
            JPopupMenu.setDefaultLightWeightPopupEnabled(true);
            Graph4NCBI app = new Graph4NCBI(graphs);
            SwingUtils.show(app);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
