import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Random;

public class CircleGame extends JPanel implements MouseListener, MouseMotionListener {

    public static final int DEFAULT_SEGMENTS = 2;

    private static final int DEFAULT_ATTEMPTS = 2;

    int segments;

    Arc2D.Double[] arcs;

    JTextArea textArea;

    static final String NEWLINE = System.getProperty("line.separator");

    static final Color INACTIVE_COLOR = Color.LIGHT_GRAY;

    static final Color ACTIVE_COLOR = Color.GRAY;

    static final Color WRONG_COLOR = Color.RED;

    static final Color CORRECT_COLOR = Color.GREEN;

    int correct = -1;

    int attempts = DEFAULT_ATTEMPTS;

    public CircleGame(int segments) {
        super();
        this.segments = segments;
        reset();
        initGui();
    }

    void reset() {
        arcs = new Arc2D.Double[segments];
        correct = new Random(System.currentTimeMillis()).nextInt(segments);
        attempts = DEFAULT_ATTEMPTS;
    }

    void initGui() {
        JFrame frame = new JFrame("Circle game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setJMenuBar(buildJMenuBar());
        Container content = frame.getContentPane();
        content.add(this, BorderLayout.NORTH);
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        content.add(scrollPane, BorderLayout.CENTER);
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(300, 300));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        content.addMouseListener(this);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Build application menu bar.
     * @return Initialized menu bar.
     */
    private JMenuBar buildJMenuBar() {
        JMenuBar mnuBar = new JMenuBar();
        JMenu mnuGame = new JMenu("Game");
        JMenuItem mnuGameAttempts = new JMenuItem("Attempts");
        JMenuItem mnuGameStat = new JMenuItem("Statistics");
        mnuBar.add(mnuGame);
        mnuGame.add(mnuGameAttempts);
        mnuGame.add(mnuGameStat);
        mnuGameAttempts.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        mnuGameStat.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
            }
        });
        return mnuBar;
    }

    /**
     * Outline and fill the shape.
     * @param shape
     * @param color
     */
    private void draw(Shape shape, Color color) {
        Graphics2D big = (Graphics2D) getGraphics();
        big.setColor(color);
        big.fill(shape);
        big.setColor(Color.BLACK);
        big.draw(shape);
    }

    public void draw(Graphics g) {
        Graphics2D big = (Graphics2D) g;
        int arc = 360 / segments;
        int n = 255 / segments;
        for (int i = 0; i < segments; i++) {
            int angle = i * arc;
            float hsb = i * n;
            arcs[i] = new Arc2D.Double(big.getClipBounds(), angle, arc, Arc2D.PIE);
            draw(arcs[i], INACTIVE_COLOR);
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    void eventOutput(String eventDescription, MouseEvent e) {
        textArea.append(eventDescription + " (" + e.getX() + "," + e.getY() + ")" + " detected on " + e.getComponent().getClass().getName() + NEWLINE);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    void eventOutput(String eventDescription) {
        textArea.append(eventDescription + NEWLINE);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    private int getActivatedArc(Point p) {
        for (int i = 0; i < arcs.length; i++) {
            if (arcs[i].contains(p)) return i;
        }
        return -1;
    }

    private void reveal(int n) {
        Graphics2D big = (Graphics2D) getGraphics();
        Color color = Color.RED;
        if (n == correct) {
            eventOutput("Correct! Arc " + correct + " is the right answer!");
        } else if (n != -1) {
            eventOutput("Weeeell... Arc " + n + " is not the one. Answer: " + correct);
        }
        for (int i = 0; i < arcs.length; i++) {
            if (i == correct) color = CORRECT_COLOR; else color = WRONG_COLOR;
            draw(arcs[i], color);
        }
    }

    public void mouseClicked(MouseEvent e) {
        int n = getActivatedArc(e.getPoint());
        attempts--;
        if (n == correct) {
            draw(arcs[n], CORRECT_COLOR);
            segments++;
            attempts = DEFAULT_ATTEMPTS;
            reset();
            this.repaint();
        } else if (n != correct && attempts > 0) {
            draw(arcs[n], WRONG_COLOR);
        } else if (n != correct && attempts == 0) {
            segments = DEFAULT_SEGMENTS;
            reveal(n);
            reset();
            this.repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    private static void testdb() throws Exception {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (Exception e) {
            System.out.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }
        Connection c = DriverManager.getConnection("jdbc:hsqldb:file:data/testdb", "sa", "");
        Statement st = c.createStatement();
        st.execute("insert into test(text, id) values('test', 10);");
        Statement st2 = c.createStatement();
        ResultSet rs = st2.executeQuery("select text from test;");
        if (rs.next()) System.out.println(rs.getString(1));
    }

    public static void main(String[] args) {
        final int segments;
        if (args.length > 0) segments = Integer.parseInt(args[0]); else segments = DEFAULT_SEGMENTS;
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new CircleGame(segments);
            }
        });
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        int n = getActivatedArc(e.getPoint());
        for (int i = 0; i < arcs.length; i++) {
            draw(arcs[i], INACTIVE_COLOR);
        }
        draw(arcs[n], ACTIVE_COLOR);
        if (n != -1) eventOutput("Arc " + n + " hovered over", e);
    }
}
