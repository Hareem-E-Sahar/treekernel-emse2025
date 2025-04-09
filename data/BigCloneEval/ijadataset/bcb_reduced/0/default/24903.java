import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import java.awt.print.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import java.text.*;
import javax.imageio.*;
import java.awt.image.*;
import javax.swing.border.*;
import com.sun.image.codec.jpeg.*;
import com.sun.image.codec.*;

public class GhinPaint extends JFrame implements ActionListener {

    public GhinPaintPanel panel;

    public GhinPaintControls controls;

    public MyToolbar toolbar;

    public JPopupMenu popup;

    public JMenuItem endPop, clearPop, savePop;

    private static Properties ghinprops;

    private static String sfile = "Ghin.properties";

    final JTextField PathTextfield = new JTextField(20);

    final JTextField StrokeTextfield = new JTextField(20);

    public Canvas fontcanvas = new Canvas();

    public Canvas fontbgcanvas = new Canvas();

    protected JFileChooser my_chooser;

    private Color cf = Color.black;

    private Color cb = Color.white;

    private int pointsz = 0;

    public String sethomedir;

    public Toolkit kit;

    public GhinPaint() {
        super("GhinPaint Ver.1");
        kit = Toolkit.getDefaultToolkit();
        Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
        setIconImage(image);
        setLayout(new BorderLayout());
        ghinprops = new Properties();
        try {
            FileInputStream in = new FileInputStream(sfile);
            ghinprops.load(in);
            sethomedir = ghinprops.getProperty("MainPath");
            cf = new Color(Integer.valueOf(ghinprops.getProperty("Foreground")));
            cb = new Color(Integer.valueOf(ghinprops.getProperty("Background")));
            pointsz = Integer.valueOf(ghinprops.getProperty("StrokeSize"));
        } catch (Exception ex) {
        }
        panel = new GhinPaintPanel(cf, cb, pointsz);
        panel.home = sethomedir;
        controls = new GhinPaintControls(panel);
        toolbar = new MyToolbar();
        for (int i = 0; i < toolbar.imageName.length; i++) {
            toolbar.button[i].addActionListener(this);
        }
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);
        popup = new JPopupMenu();
        popup.add(endPop = new JMenuItem("End"));
        endPop.addActionListener(new menuListener());
        popup.add(clearPop = new JMenuItem("Clear"));
        clearPop.addActionListener(new menuListener());
        popup.add(savePop = new JMenuItem("Save"));
        savePop.addActionListener(new menuListener());
        panel.addMouseListener(new MousePopupListener());
        add("North", toolbar);
        add("Center", panel);
        add("South", controls);
        pack();
        setVisible(true);
        setSize(650, 400);
    }

    public JPanel JPanelSetting(final JFrame j) {
        final JPanel MyPanel = new JPanel();
        MyPanel.setLayout(new BorderLayout());
        MyPanel.setBorder(new TitledBorder(new EtchedBorder(), "Option"));
        JFileChooser filechooser;
        JPanel MyLabelPanel = new JPanel();
        JLabel PathLabel = new JLabel(" MainPath : ");
        JLabel ForegroundLabel = new JLabel(" Foreground : ");
        JLabel BackgroundLabel = new JLabel(" Background : ");
        JLabel StrokeLabel = new JLabel(" StrokeSize: ");
        JPanel MyTextFieldPanel = new JPanel();
        JPanel MyButtondPanel = new JPanel();
        JButton FontColorButton;
        JButton FontBackgroundButton;
        JButton DirSelectButton;
        MyLabelPanel.setLayout(new GridLayout(4, 1, 5, 5));
        MyLabelPanel.add(PathLabel);
        MyLabelPanel.add(ForegroundLabel);
        MyLabelPanel.add(BackgroundLabel);
        MyLabelPanel.add(StrokeLabel);
        MyPanel.add("West", MyLabelPanel);
        MyTextFieldPanel.setLayout(new GridLayout(4, 1, 5, 5));
        MyTextFieldPanel.add(PathTextfield);
        fontcanvas = new Canvas();
        fontbgcanvas = new Canvas();
        MyTextFieldPanel.add(fontcanvas);
        MyTextFieldPanel.add(fontbgcanvas);
        MyTextFieldPanel.add(StrokeTextfield);
        MyPanel.add("Center", MyTextFieldPanel);
        JPanel ButtonPanel = new JPanel();
        ButtonPanel.setLayout(new GridLayout(1, 4, 5, 5));
        MyButtondPanel.setLayout(new GridLayout(6, 1, 5, 5));
        DirSelectButton = new JButton("..");
        DirSelectButton.addActionListener(this);
        FontColorButton = new JButton("..");
        FontColorButton.addActionListener(this);
        FontBackgroundButton = new JButton("..");
        FontBackgroundButton.addActionListener(this);
        JLabel jl1 = new JLabel("");
        JLabel jl2 = new JLabel("");
        JLabel jl3 = new JLabel("");
        MyButtondPanel.add(DirSelectButton);
        MyButtondPanel.add(jl1);
        MyButtondPanel.add(FontColorButton);
        MyButtondPanel.add(jl2);
        MyButtondPanel.add(FontBackgroundButton);
        MyButtondPanel.add(jl3);
        MyPanel.add("East", MyButtondPanel);
        JButton ApplyButton = new JButton("Apply");
        ApplyButton.addActionListener(this);
        JButton DefaultButton = new JButton("Default");
        DefaultButton.addActionListener(this);
        JButton ResetButton = new JButton("Reset");
        ResetButton.addActionListener(this);
        JButton CloseButton = new JButton("Close");
        CloseButton.addActionListener(this);
        ButtonPanel.add(ApplyButton);
        ButtonPanel.add(DefaultButton);
        ButtonPanel.add(ResetButton);
        ButtonPanel.add(CloseButton);
        MyPanel.add("South", ButtonPanel);
        ApplyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                saveSetting();
                panel.setForeground(new Color(cf.getRGB()));
                panel.setBackground(new Color(cb.getRGB()));
                panel.setGhinStrokeSize(Integer.valueOf(StrokeTextfield.getText()));
                panel.home = PathTextfield.getText();
                controls.bgini = new Color(cb.getRGB());
                controls.repaint();
                panel.repaint();
            }
        });
        DefaultButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                loadSetting();
            }
        });
        ResetButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PathTextfield.setText(".");
                fontcanvas.setBackground(Color.black);
                fontbgcanvas.setBackground(Color.white);
                StrokeTextfield.setText("1");
                repaint();
            }
        });
        CloseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                j.dispose();
            }
        });
        DirSelectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                File file = new File(".");
                if (chooser.showDialog(MyPanel, "Select") == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    String dir = file.getPath();
                    PathTextfield.setText(dir);
                }
            }
        });
        FontColorButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cf = JColorChooser.showDialog(((Component) null), "set Foreground Color", Color.blue);
                fontcanvas.setBackground(cf);
                if (cf == null) fontcanvas.setBackground(new Color(Integer.valueOf(ghinprops.getProperty("Foreground"))));
            }
        });
        FontBackgroundButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cb = JColorChooser.showDialog(((Component) null), "set Background Color", Color.blue);
                fontbgcanvas.setBackground(cb);
                if (cb == null) fontbgcanvas.setBackground(new Color(Integer.valueOf(ghinprops.getProperty("Background"))));
            }
        });
        loadSetting();
        return MyPanel;
    }

    public void loadSetting() {
        try {
            ghinprops = new Properties();
            FileInputStream in = new FileInputStream(sfile);
            ghinprops.load(in);
            PathTextfield.setText(ghinprops.getProperty("MainPath"));
            fontcanvas.setBackground(new Color(Integer.valueOf(ghinprops.getProperty("Foreground"))));
            fontbgcanvas.setBackground(new Color(Integer.valueOf(ghinprops.getProperty("Background"))));
            StrokeTextfield.setText(ghinprops.getProperty("StrokeSize"));
        } catch (Exception ex) {
            warnme("" + ex.getMessage());
        }
    }

    public void saveSetting() {
        try {
            FileOutputStream outfl = new FileOutputStream(sfile);
            ghinprops.setProperty("MainPath", PathTextfield.getText());
            ghinprops.setProperty("Foreground", String.valueOf(cf.getRGB()));
            ghinprops.setProperty("Background", String.valueOf(cb.getRGB()));
            ghinprops.setProperty("StrokeSize", StrokeTextfield.getText());
            ghinprops.save(outfl, "Ghin Ver 1.0 Pekanbaru 2007 - Properties File ");
            outfl.close();
        } catch (IOException ex) {
            warnme("" + ex.getMessage());
        }
    }

    public void warnme(String message) {
        JOptionPane.showMessageDialog(new JFrame(), message, "Warning", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void onSaveImage(String savename) {
        String input = (String) JOptionPane.showInputDialog(this, "Your Image File name is...", "Save as PNG File", JOptionPane.INFORMATION_MESSAGE, new ImageIcon("logo.gif"), null, savename);
        if (savename.endsWith("png")) {
            try {
                Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle region = getCenteredRectangle(d);
                d = panel.getSize();
                region = getCenteredRectangle(d);
                panel.tulisDrawing(panel.ambilDrawing(panel, region), input);
            } catch (Exception ex) {
            }
        }
        System.out.println("Saved " + input + " image done.");
    }

    private static Rectangle getCenteredRectangle(Dimension d) {
        int x = 0;
        int y = 0;
        int width = d.width;
        int height = d.height;
        return new Rectangle(x, y, width, height);
    }

    protected JMenuBar createMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        JMenu mFile = new JMenu("File");
        mFile.setMnemonic('f');
        JMenu mCommand = new JMenu("Edit");
        mCommand.setMnemonic('e');
        JMenu mTool = new JMenu("Tool");
        mTool.setMnemonic('t');
        JMenu mAbout = new JMenu("Help");
        mAbout.setMnemonic('h');
        JMenuItem item = new JMenuItem("About GhinPaint 1.0");
        item.setMnemonic('a');
        ActionListener lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("About GhinPaint 1.0");
                ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("logo.gif"));
                JLabel label1 = new JLabel(icon);
                frame.add("North", label1);
                JLabel label2 = new JLabel("<html><li>GhinPaint 1.0� " + "</li><li><p>Ver# 1.0 </li>" + "<li><p>Develop by: Goen-Ghin</li><li><p>JavaGeo Technology Advance System</li><li>" + "<p>Copyright<font size=\"2\">�</font> Juli 2007 @Pekanbaru-Riau</li></html>");
                label2.setFont(new Font("Tahoma", Font.PLAIN, 11));
                frame.add(label2);
                Dimension d1 = frame.getSize();
                Dimension d2 = getSize();
                int xv = Math.max((d2.width / 2 - d1.width) / 2, 0);
                int yv = Math.max((d2.height / 2 - d1.height) / 2, 0);
                frame.setBounds(xv + frame.getX(), yv + frame.getY(), d1.width, d1.height);
                Toolkit kit = Toolkit.getDefaultToolkit();
                Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
                frame.setIconImage(image);
                frame.setSize(240, 150);
                frame.setVisible(true);
            }
        };
        item.addActionListener(lst);
        mAbout.add(item);
        item = new JMenuItem("Open Point");
        item.setMnemonic('o');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    panel.readPoint();
                    panel.repaint();
                    panel.OKdraw = true;
                } catch (Exception ex) {
                    System.out.println("" + ex);
                }
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Save Point");
        item.setMnemonic('s');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                panel.savePoint();
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Option");
        item.setMnemonic('n');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("Set Configuration");
                frame.setContentPane(JPanelSetting(frame));
                Toolkit kit = Toolkit.getDefaultToolkit();
                Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
                frame.setIconImage(image);
                frame.setSize(360, 240);
                frame.setVisible(true);
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        mFile.addSeparator();
        item = new JMenuItem("Save As...");
        item.setMnemonic('a');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                my_chooser = new JFileChooser(sethomedir);
                SimpleFilter filter = new SimpleFilter("png", "PNG Image Files");
                my_chooser.setFileFilter(filter);
                if (my_chooser.showSaveDialog(GhinPaint.this) != JFileChooser.APPROVE_OPTION) return;
                File fChoosen = my_chooser.getSelectedFile();
                if (fChoosen != null && fChoosen.exists()) {
                    String message = "File " + fChoosen.getName() + " already exists. Override?";
                    int result = JOptionPane.showConfirmDialog(GhinPaint.this, message, getTitle(), JOptionPane.YES_NO_OPTION);
                    if (result != JOptionPane.YES_OPTION) return;
                }
                onSaveImage(fChoosen.toString());
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Print");
        item.setMnemonic('P');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    new PrintImage();
                } catch (Exception ex) {
                }
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Exit");
        item.setMnemonic('x');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        item.addActionListener(lst);
        mFile.add(item);
        item = new JMenuItem("Foreground");
        item.setMnemonic('f');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Color c = JColorChooser.showDialog(((Component) null), "set Foregound Color", Color.blue);
                if (c != null) panel.setForeground(c);
            }
        };
        item.addActionListener(lst);
        mCommand.add(item);
        item = new JMenuItem("Background");
        item.setMnemonic('b');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Color c = JColorChooser.showDialog(((Component) null), "set Background Color", Color.blue);
                if (c != null) panel.setBackground(c);
            }
        };
        item.addActionListener(lst);
        mCommand.add(item);
        item = new JMenuItem("Clear");
        item.setMnemonic('c');
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                panel.clearDraw = true;
                panel.repaint();
                panel.OKdraw = false;
            }
        };
        item.addActionListener(lst);
        mCommand.add(item);
        item = new JMenuItem("Load Image");
        item.setMnemonic('l');
        mTool.add(item);
        lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                panel.OKdraw = true;
                panel.openFile();
                panel.OKdraw = false;
                panel.update();
                GhinPaint.this.getContentPane().repaint();
            }
        };
        item.addActionListener(lst);
        mTool.add(item);
        menuBar.add(mFile);
        menuBar.add(mCommand);
        menuBar.add(mTool);
        menuBar.add(mAbout);
        return menuBar;
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == toolbar.button[0]) {
            try {
                panel.readPoint();
                panel.repaint();
                panel.OKdraw = true;
            } catch (Exception ex) {
                System.out.println("" + ex);
            }
        }
        if (ae.getSource() == toolbar.button[1]) {
            panel.savePoint();
        }
        if (ae.getSource() == toolbar.button[2]) {
            Color c = JColorChooser.showDialog(((Component) null), "set Background Color", Color.blue);
            if (c != null) panel.setBackground(c);
        }
        if (ae.getSource() == toolbar.button[3]) {
            Color c = JColorChooser.showDialog(((Component) null), "set Foregound Color", Color.blue);
            if (c != null) panel.setForeground(c);
        }
        if (ae.getSource() == toolbar.button[4]) {
            panel.clearDraw = true;
            panel.repaint();
            panel.OKdraw = false;
        }
        if (ae.getSource() == toolbar.button[5]) {
            panel.OKdraw = true;
            panel.openFile();
            panel.OKdraw = false;
            panel.update();
            GhinPaint.this.getContentPane().repaint();
        }
        if (ae.getSource() == toolbar.button[6]) {
            JFrame frame = new JFrame("Set Configuration");
            frame.setContentPane(JPanelSetting(frame));
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image image = kit.getImage(ClassLoader.getSystemResource("logo.gif"));
            frame.setIconImage(image);
            frame.setSize(360, 240);
            frame.setVisible(true);
        }
        if (ae.getSource() == toolbar.button[7]) {
            Thread runner6 = new Thread() {

                public void run() {
                    try {
                        System.exit(0);
                    } catch (Exception e) {
                    }
                }
            };
            runner6.start();
        }
    }

    public static void main(String args[]) {
        GhinPaint yp = new GhinPaint();
        yp.show();
    }

    class MyToolbar extends JToolBar {

        public JButton[] button;

        public String[] imageName = { "tampilkan.png", "rumah.png", "maju.png", "mundur.png", "endrumah.png", "cetak.png", "atur.png", "keluar.png" };

        public String[] tipText = { "Open Point File", "Save Point File", "Set Background ", "Set Foreground", "Clear", "Open Image", "Option", "Exit" };

        public MyToolbar() {
            button = new JButton[8];
            for (int i = 0; i < imageName.length; i++) {
                add(button[i] = new JButton(new ImageIcon(ClassLoader.getSystemResource(imageName[i]))));
                button[i].setToolTipText(tipText[i]);
            }
        }
    }

    class MousePopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            checkPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            checkPopup(e);
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(panel, e.getX(), e.getY());
            }
        }
    }

    class menuListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            panel.OKdraw = false;
            if (e.getActionCommand() == "End") {
                Thread runner1 = new Thread() {

                    public void run() {
                        try {
                            panel.OKdraw = false;
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        ;
                    }
                };
                runner1.start();
            }
            if (e.getActionCommand() == "Clear") {
                Thread runner1 = new Thread() {

                    public void run() {
                        try {
                            panel.clearDraw = true;
                            panel.OKdraw = false;
                            panel.repaint();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        ;
                    }
                };
                runner1.start();
            }
            if (e.getActionCommand() == "Save") {
                Thread runner2 = new Thread() {

                    public void run() {
                        panel.OKdraw = false;
                        panel.savePoint();
                    }
                };
                runner2.start();
            }
        }
    }

    class PrintImage implements Printable {

        private Font fnt = new Font("Helvetica", Font.PLAIN, 24);

        private Paint pnt = new GradientPaint(100f, 100f, Color.red, 136f, 100f, Color.green, true);

        PrintImage() {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            if (job.printDialog()) {
                try {
                    job.print();
                } catch (Exception e) {
                    System.out.println("error" + e);
                    System.exit(1);
                }
            }
        }

        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            AffineTransform at = new AffineTransform();
            at.translate(0, 0);
            Dimension drawku = kit.getScreenSize();
            double xScale = pageFormat.getImageableWidth() / drawku.getWidth();
            double yScale = pageFormat.getImageableHeight() / drawku.getHeight();
            double aspectScale = Math.min(xScale, yScale);
            try {
                g2d.drawRenderedImage(panel.ambilDrawing(panel), at);
            } catch (Exception ex) {
            }
            return Printable.PAGE_EXISTS;
        }
    }
}

class GhinPaintPanel extends JPanel implements MouseListener, MouseMotionListener {

    public static final int LINES = 0;

    public static final int POINTS = 1;

    public static final int ARBITRARY = 2;

    public static double SCALE = 1;

    public static int SIZESPOINTS = 1;

    public JFileChooser my_chooser;

    int mode = LINES;

    Vector lines = new Vector();

    Vector colors = new Vector();

    Vector linesfile = new Vector();

    public BufferedImage open_img;

    public Image offScreen = null;

    public Graphics offScreenGraphics = null;

    public static final String formatNameDefault = "png";

    Color colorFile, bgini, fgini, sizeini;

    int x1, y1;

    int x2, y2;

    boolean OKdraw = false;

    boolean clearDraw = false;

    boolean shapeActive = true;

    boolean openimgFile = false;

    public Graphics2D graph = null;

    public String home = ".";

    public GhinPaintPanel(Color fg, Color bg, int sizepoint) {
        bgini = bg;
        fgini = fg;
        setBackground(bgini);
        setForeground(fgini);
        SIZESPOINTS = sizepoint;
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    public void setGhinPaintMode(int mode) {
        switch(mode) {
            case LINES:
            case POINTS:
                this.mode = mode;
                break;
            case ARBITRARY:
                this.mode = mode;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setGhinStrokeSize(int size) {
        this.SIZESPOINTS = size;
        this.repaint();
    }

    public void setGhinSkala(double skala) {
        this.SCALE = skala;
        this.repaint();
    }

    public void mouseDragged(MouseEvent e) {
        e.consume();
        switch(mode) {
            case LINES:
                x2 = e.getX();
                y2 = e.getY();
                break;
            case POINTS:
            default:
                colors.addElement(getForeground());
                lines.addElement(new Rectangle(x1, y1, e.getX(), e.getY()));
                x1 = e.getX();
                y1 = e.getY();
                break;
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        if (OKdraw == true) {
            e.consume();
            switch(mode) {
                case LINES:
                    x2 = e.getX();
                    y2 = e.getY();
                    if (this.open_img != null) {
                        drawing(graph);
                        repaint();
                    }
                    break;
                case POINTS:
                    x2 = e.getX();
                    y2 = e.getY();
                    repaint();
                    break;
                case ARBITRARY:
                    x1 = e.getX();
                    y1 = e.getY();
                    colors.addElement(getForeground());
                    lines.addElement(new Rectangle(x1, y1, x2, y2));
                    x2 = x1;
                    y2 = y1;
                    if (this.open_img != null) {
                        drawing(graph);
                        repaint();
                    }
                    repaint();
                    break;
                default:
                    break;
            }
            repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
        clearDraw = false;
        e.consume();
        switch(mode) {
            case LINES:
                OKdraw = false;
                x1 = e.getX();
                y1 = e.getY();
                x2 = -1;
                if (this.open_img != null) {
                    drawing(graph);
                    repaint();
                }
                repaint();
                break;
            case POINTS:
            case ARBITRARY:
                OKdraw = true;
                x1 = e.getX();
                y1 = e.getY();
                x2 = -1;
                break;
            default:
                colors.addElement(getForeground());
                lines.addElement(new Rectangle(e.getX(), e.getY(), -1, -1));
                x1 = e.getX();
                y1 = e.getY();
                if (this.open_img != null) {
                    drawing(graph);
                    repaint();
                }
                repaint();
                break;
        }
    }

    public void mouseReleased(MouseEvent e) {
        e.consume();
        switch(mode) {
            case LINES:
                colors.addElement(getForeground());
                lines.addElement(new Rectangle(x1, y1, e.getX(), e.getY()));
                x2 = -1;
                if (this.open_img != null) {
                    drawing(graph);
                    repaint();
                }
                repaint();
                break;
            case POINTS:
            case ARBITRARY:
                colors.addElement(getForeground());
                lines.addElement(new Rectangle(x1, y1, e.getX(), e.getY()));
                x2 = -1;
                if (this.open_img != null) {
                    drawing(graph);
                    repaint();
                }
                repaint();
                break;
            default:
                break;
        }
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void update() {
        repaint();
    }

    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        super.paintComponent(gr);
        if (this.open_img != null) {
            graph = this.open_img.createGraphics();
            graph.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        drawing(g);
        if (clearDraw == true) {
            lines.removeAllElements();
            colors.removeAllElements();
            linesfile.removeAllElements();
            cleardraw(g);
            if (open_img != null) clearImage(g);
        }
        if (shapeActive == true) {
            clearDraw = false;
            drawPointFile(g);
        }
        if (openimgFile == true) {
            openImage(g);
        }
        ;
    }

    public void segitiga(Graphics2D gi) {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        gi.setBackground(getBackground());
        gi.clearRect(0, 0, 10, 10);
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(0, 0);
        p1.lineTo(5, 10);
        p1.lineTo(10, 0);
        p1.closePath();
        gi.setColor(Color.lightGray);
        gi.fill(p1);
        TexturePaint triangles = new TexturePaint(bi, new Rectangle(0, 0, 10, 10));
        gi.setPaint(triangles);
        gi.setPaint(triangles);
    }

    public BufferedImage ambilDrawing(JComponent jcomponent) throws AWTException, SecurityException {
        Dimension d = jcomponent.getSize();
        Rectangle region = new Rectangle(0, 0, d.width, d.height);
        return ambilDrawing(jcomponent, region);
    }

    public BufferedImage ambilDrawing(Rectangle region) throws IllegalArgumentException, AWTException, SecurityException {
        if (region == null) throw new IllegalArgumentException("region == null");
        return new Robot().createScreenCapture(region);
    }

    public static BufferedImage ambilDrawing(JComponent jcomponent, Rectangle region) throws IllegalArgumentException {
        if (jcomponent == null) throw new IllegalArgumentException("jcomponent == null");
        if (region == null) throw new IllegalArgumentException("region == null");
        boolean opaquenessOriginal = jcomponent.isOpaque();
        Graphics2D g2d = null;
        try {
            jcomponent.setOpaque(true);
            BufferedImage image = new BufferedImage(region.width, region.height, BufferedImage.TYPE_4BYTE_ABGR);
            g2d = image.createGraphics();
            g2d.translate(-region.x, -region.y);
            g2d.setClip(region);
            jcomponent.paint(g2d);
            return image;
        } finally {
            jcomponent.setOpaque(opaquenessOriginal);
            if (g2d != null) g2d.dispose();
        }
    }

    public static void tulisDrawing(BufferedImage image, String fileName) throws IllegalArgumentException, IOException {
        if (image == null) throw new IllegalArgumentException("image == null");
        if (fileName == null) throw new IllegalArgumentException("fileName == null");
        if (fileName.trim().length() == 0) throw new IllegalArgumentException("fileName is all whitespace");
        int index = fileName.lastIndexOf(".");
        String formatName = (index != -1) ? fileName.substring(index + 1) : formatNameDefault;
        File file = new File(fileName);
        saveImage(image, formatName, file);
    }

    public static void saveImage(BufferedImage image, String formatName, File file) throws IllegalArgumentException, IOException {
        if (image == null) throw new IllegalArgumentException("image == null");
        if (formatName == null) throw new IllegalArgumentException("formatName == null");
        if (formatName.trim().length() == 0) throw new IllegalArgumentException("formatName is all whitespace");
        if (file == null) throw new IllegalArgumentException("file == null");
        ImageIO.write(image, formatName, file);
    }

    public void openImage(Graphics2D g) {
        if (offScreen == null) {
            offScreen = createImage(this.getWidth(), this.getHeight());
            offScreenGraphics = offScreen.getGraphics();
            offScreenGraphics.setColor(getBackground());
        }
        g.drawImage(open_img, 0, 0, this);
    }

    public void cleardraw(Graphics2D g) {
        g.clearRect(0, 0, this.getWidth(), this.getHeight());
        setBackground(getBackground());
        g.dispose();
        this.repaint();
    }

    public void clearImage(Graphics g) {
        open_img = null;
        g.setColor(getBackground());
        g.drawImage(open_img, 0, 0, this);
        g.dispose();
    }

    public void drawing(Graphics2D g) {
        int np = lines.size();
        g.scale(this.SCALE, this.SCALE);
        g.setStroke(new BasicStroke(SIZESPOINTS));
        for (int i = 0; i < np; i++) {
            Rectangle p = (Rectangle) lines.elementAt(i);
            g.setColor((Color) colors.elementAt(i));
            if (p.width != -1) {
                g.drawLine(p.x, p.y, p.width, p.height);
            } else {
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }

    public void drawPointFile(Graphics2D g) {
        int np = linesfile.size();
        g.scale(this.SCALE, this.SCALE);
        g.setStroke(new BasicStroke(SIZESPOINTS));
        GeneralPath pt = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        for (int i = 0; i < np; i++) {
            Rectangle p = (Rectangle) linesfile.elementAt(i);
            if (p.width != -1) {
                g.drawLine(p.x, p.y, p.width, p.height);
                g.fill(pt);
                pt.moveTo(p.x, p.y);
                pt.lineTo(p.x, p.y * this.getHeight());
                if (i == np) pt.lineTo(p.width, p.height * this.getHeight());
            } else {
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
        g.fill(pt);
        g.draw(pt);
    }

    protected void openFile() {
        my_chooser = new JFileChooser(".");
        SimpleFilter filter = new SimpleFilter("png", "PNG Image Files");
        my_chooser.setFileFilter(filter);
        if (my_chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final File fChoosen = my_chooser.getSelectedFile();
        if (fChoosen == null || !fChoosen.exists()) return;
        if (this.offScreenGraphics != null) this.offScreenGraphics.dispose();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        Thread runner = new Thread() {

            public void run() {
                try {
                    if (fChoosen.toString().endsWith("jpg") || fChoosen.toString().endsWith("JPG") || fChoosen.toString().endsWith("jpeg") || fChoosen.toString().endsWith("JPEG")) {
                        FileInputStream in = new FileInputStream(fChoosen);
                        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
                        open_img = decoder.decodeAsBufferedImage();
                        in.close();
                    } else if (fChoosen.toString().endsWith("png") || fChoosen.toString().endsWith("PNG")) {
                        openimgFile = true;
                        open_img = ImageIO.read(fChoosen);
                        repaint();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println("openFile: " + ex.toString());
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
        runner.start();
    }

    public void savePoint() {
        try {
            my_chooser = new JFileChooser(home);
            if (my_chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File fChoosen = my_chooser.getSelectedFile();
            if (fChoosen != null && fChoosen.exists()) {
                String message = "File " + fChoosen.getName() + " already exists. Override?";
                int result = JOptionPane.showConfirmDialog(this, message, "save drawing point", JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION) return;
            }
            FileWriter fw = new FileWriter(fChoosen.toString(), true);
            int np = lines.size();
            for (int i = 0; i < np; i++) {
                Rectangle p = (Rectangle) lines.elementAt(i);
                fw.write(p.x + "|" + p.y + "|" + p.width + "|" + p.height + "|" + "\n");
            }
            fw.write("" + getForeground().getRed() + "|" + getForeground().getGreen() + "|" + getForeground().getBlue() + "|" + -999 + "|" + "\n");
            lines.removeAllElements();
            colors.removeAllElements();
            fw.close();
        } catch (Exception ex) {
        }
    }

    public void readPoint() throws java.io.IOException, java.text.ParseException {
        try {
            int pointX;
            int pointY;
            int pointXW;
            int pointYH;
            String rowLine;
            String token;
            my_chooser = new JFileChooser(home);
            my_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (my_chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File fChoosen = my_chooser.getSelectedFile();
            File inFile = new File(fChoosen.toString());
            BufferedReader bf = new BufferedReader(new FileReader(inFile));
            rowLine = bf.readLine();
            while (rowLine != null) {
                StringTokenizer tk = new StringTokenizer(rowLine, "|", true);
                NumberFormat nf = NumberFormat.getInstance();
                pointX = nf.parse(tk.nextToken()).intValue();
                token = tk.nextToken();
                pointY = nf.parse(tk.nextToken()).intValue();
                token = tk.nextToken();
                pointXW = nf.parse(tk.nextToken()).intValue();
                token = tk.nextToken();
                pointYH = nf.parse(tk.nextToken()).intValue();
                token = tk.nextToken();
                rowLine = bf.readLine();
                linesfile.addElement(new Rectangle(pointX, pointY, pointXW, pointYH));
                lines.addElement(new Rectangle(pointX, pointY, pointXW, pointYH));
                if (pointYH == -999) setColorfile(pointX, pointY, pointXW);
                colors.addElement(colorFile);
                repaint();
            }
        } catch (Exception ex) {
        }
    }

    public void setColorfile(int r, int g, int b) {
        colorFile = new Color(r, g, b);
    }
}

class GhinPaintControls extends JPanel implements ItemListener {

    GhinPaintPanel target;

    Color bgini;

    public GhinPaintControls(GhinPaintPanel target) {
        this.target = target;
        setLayout(new FlowLayout());
        setBackground(Color.lightGray);
        CheckboxGroup group = new CheckboxGroup();
        Checkbox b;
        add(b = new Checkbox("White", group, false));
        b.addItemListener(this);
        b.setForeground(Color.white);
        add(b = new Checkbox("Black", group, true));
        b.addItemListener(this);
        b.setForeground(Color.black);
        add(b = new Checkbox("Color", group, true));
        b.addItemListener(this);
        b.setForeground(target.getBackground());
        bgini = target.getBackground();
        Choice shapes = new Choice();
        shapes.addItemListener(this);
        shapes.addItem("Lines");
        shapes.addItem("Points");
        shapes.addItem("Arbitrary");
        shapes.setBackground(Color.white);
        add(shapes);
        Choice strokesize = new Choice();
        strokesize.addItemListener(this);
        strokesize.addItem("1");
        strokesize.addItem("2");
        strokesize.addItem("4");
        strokesize.addItem("6");
        strokesize.addItem("8");
        strokesize.addItem("10");
        strokesize.setBackground(Color.white);
        add(strokesize);
        Choice imgscale = new Choice();
        imgscale.addItemListener(this);
        imgscale.addItem("10%");
        imgscale.addItem("20%");
        imgscale.addItem("30%");
        imgscale.addItem("50%");
        imgscale.addItem("100%");
        imgscale.setBackground(Color.white);
        add(imgscale);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.lightGray);
        int n = getComponentCount();
        for (int i = 0; i < n; i++) {
            Component comp = getComponent(i);
            if (comp instanceof Checkbox) {
                Point loc = comp.getLocation();
                Dimension d = comp.getSize();
                g.drawRect(loc.x - 1, loc.y - 1, d.width + 1, d.height + 1);
            }
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() instanceof Checkbox) {
            String ckbox = (String) e.getItem();
            if (ckbox.equals("White")) {
                target.setBackground(Color.white);
            } else if (ckbox.equals("Black")) {
                target.setBackground(Color.black);
            } else if (ckbox.equals("Color")) {
                target.setBackground(bgini);
            }
        } else if (e.getSource() instanceof Choice) {
            String choice = (String) e.getItem();
            if (choice.equals("Lines")) {
                target.setGhinPaintMode(GhinPaintPanel.LINES);
            } else if (choice.equals("Points")) {
                target.setGhinPaintMode(GhinPaintPanel.POINTS);
            } else if (choice.equals("Arbitrary")) {
                target.setGhinPaintMode(GhinPaintPanel.ARBITRARY);
            }
            if (choice.equals("10")) {
                target.setGhinStrokeSize(10);
            } else if (choice.equals("1")) {
                target.setGhinStrokeSize(1);
            } else if (choice.equals("2")) {
                target.setGhinStrokeSize(2);
            } else if (choice.equals("4")) {
                target.setGhinStrokeSize(4);
            } else if (choice.equals("6")) {
                target.setGhinStrokeSize(6);
            } else if (choice.equals("8")) {
                target.setGhinStrokeSize(8);
            }
            if (choice.equals("10%")) {
                target.setGhinSkala(1);
            } else if (choice.equals("20%")) {
                target.setGhinSkala(1.2);
            } else if (choice.equals("30%")) {
                target.setGhinSkala(2);
            } else if (choice.equals("50%")) {
                target.setGhinSkala(3);
            } else if (choice.equals("100%")) {
                target.setGhinSkala(4);
            }
        }
    }
}

class SimpleFilter extends javax.swing.filechooser.FileFilter {

    private String gdescription = null;

    private String gextension = null;

    public SimpleFilter(String extension, String description) {
        gdescription = description;
        gextension = "." + extension.toLowerCase();
    }

    public String getDescription() {
        return gdescription;
    }

    public boolean accept(File f) {
        if (f == null) return false;
        if (f.isDirectory()) return true;
        return f.getName().toLowerCase().endsWith(gextension);
    }
}
