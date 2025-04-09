package com.wateray.ipassbook.ui.widget;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.wateray.ipassbook.util.ImageManager;

public class CaptureScreen extends JFrame implements ActionListener {

    /**
         *
         */
    private static final long serialVersionUID = 1L;

    private JButton startCaptureButton;

    private JButton cancelButton;

    private JButton saveAllButton;

    private JPanel contentPanel;

    private JTabbedPane pictureContainerTabbedPane;

    /** screen Image.*/
    private BufferedImage screenImage;

    /** captureImage is a subImage of screenImage.*/
    private BufferedImage captureImage;

    private int imageIndex;

    private JRadioButton metalLookAndFeelRadioButton;

    private JRadioButton systemLookAndFeelRadioButton;

    /** The fixed position capturer.*/
    private JButton fixedPositionCaptureButton;

    /** The last position data. */
    private Rectangle lastPostionRectangle;

    /** The close all button. */
    private JButton closeAllButton;

    /**desktop color.*/
    private static Color desktopColor = Color.LIGHT_GRAY;

    /** Robot.*/
    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /** Creates a new instance of CaptureScreen */
    public CaptureScreen() {
        super("屏幕截取软件(第三版)");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        initWindow();
        initOther();
        initTrayIcon();
    }

    private void initOther() {
        pictureContainerTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        pictureContainerTabbedPane.setBackground(desktopColor);
        pictureContainerTabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (pictureContainerTabbedPane.getSelectedIndex() != -1) {
                    doCopy(((PicPanel) pictureContainerTabbedPane.getSelectedComponent()).getImage(), false);
                }
            }
        });
    }

    private void initWindow() {
        startCaptureButton = new JButton("开始截取");
        saveAllButton = new JButton("保存所有");
        saveAllButton.setEnabled(false);
        cancelButton = new JButton("退出");
        startCaptureButton.addActionListener(this);
        saveAllButton.addActionListener(this);
        cancelButton.addActionListener(this);
        JPanel buttonJP = new JPanel();
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(desktopColor);
        JLabel jl = new JLabel("屏幕截取", JLabel.CENTER);
        JLabel jl1 = new JLabel("<Html><Font size=5 color=white>作者：千里冰封<br>" + "QQ:24325142<br><br><br></Font></html>", JLabel.CENTER);
        jl.setFont(new Font("黑体", Font.BOLD, 40));
        jl.setForeground(Color.RED);
        jl1.setForeground(Color.BLUE);
        contentPanel.add(jl, BorderLayout.CENTER);
        contentPanel.add(jl1, BorderLayout.SOUTH);
        buttonJP.add(startCaptureButton);
        buttonJP.add(getFixedPositionCaptureButton());
        buttonJP.add(saveAllButton);
        buttonJP.add(getCloseAllButton());
        buttonJP.add(cancelButton);
        buttonJP.setBorder(BorderFactory.createTitledBorder("公共操作区"));
        JPanel jp = new JPanel();
        jp.add(metalLookAndFeelRadioButton = new JRadioButton("java界面"));
        jp.add(systemLookAndFeelRadioButton = new JRadioButton("系统界面", true));
        metalLookAndFeelRadioButton.addActionListener(this);
        systemLookAndFeelRadioButton.addActionListener(this);
        jp.setBorder(BorderFactory.createTitledBorder("界面风格"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(metalLookAndFeelRadioButton);
        bg.add(systemLookAndFeelRadioButton);
        JPanel all = new JPanel();
        all.add(buttonJP);
        this.getContentPane().add(contentPanel, BorderLayout.CENTER);
        this.getContentPane().add(all, BorderLayout.SOUTH);
        this.setSize(550, 400);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setIconImage(ImageManager.getImageByShortName("69.png"));
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent we) {
                CaptureScreen.this.setVisible(false);
            }
        });
    }

    private JButton getFixedPositionCaptureButton() {
        if (fixedPositionCaptureButton == null) {
            fixedPositionCaptureButton = new JButton("固定截取");
            fixedPositionCaptureButton.setEnabled(false);
            fixedPositionCaptureButton.addActionListener(this);
        }
        return fixedPositionCaptureButton;
    }

    private JButton getCloseAllButton() {
        if (closeAllButton == null) {
            closeAllButton = new JButton("关闭所有");
            closeAllButton.setEnabled(false);
            closeAllButton.addActionListener(this);
        }
        return closeAllButton;
    }

    private void initTrayIcon() {
        try {
            SystemTray st = SystemTray.getSystemTray();
            PopupMenu pm = new PopupMenu("POP Menu");
            pm.add(new MenuItem("About")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    JOptionPane.showMessageDialog(CaptureScreen.this, "<html><Font color=red><center><h2>关于</h2></center></Font>" + "这是一款纯JAVA的屏幕截取程序<br>在以前的基础上增加了一些常用的功能,<br>" + "比如,批量保存,多幅截取,复制到系统粘帖板<br>" + "在使用过程中有任何问题,欢迎联系.<br>" + "<Font size=5 color=blue>作者:千里冰封<br>" + "<i>QQ:24325142</i><br></Font></html>");
                }
            });
            pm.addSeparator();
            pm.add(new MenuItem("Show Window")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    CaptureScreen.this.setVisible(true);
                }
            });
            pm.add(new MenuItem("Capture")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    doStart();
                }
            });
            pm.add(new MenuItem("Exit")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    System.exit(0);
                }
            });
            TrayIcon ti = new TrayIcon(ImageManager.getImageByShortName("69.png"), "JAVA SCreen Capture", pm);
            st.add(ti);
            ti.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    CaptureScreen.this.setVisible(true);
                }
            });
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    private void updates() {
        this.setVisible(true);
        if (captureImage != null) {
            if (imageIndex == 0) {
                contentPanel.removeAll();
                contentPanel.add(pictureContainerTabbedPane, BorderLayout.CENTER);
            } else {
            }
            PicPanel pic = new PicPanel(captureImage);
            pictureContainerTabbedPane.addTab("图片" + (++imageIndex), pic);
            pictureContainerTabbedPane.setSelectedComponent(pic);
            SwingUtilities.updateComponentTreeUI(contentPanel);
            doCopy(captureImage, false);
            saveAllButton.setEnabled(true);
            getCloseAllButton().setEnabled(true);
        }
    }

    private void doStart() {
        Dimension screen;
        Rectangle rect;
        try {
            this.setVisible(false);
            Thread.sleep(500);
            Toolkit tk = Toolkit.getDefaultToolkit();
            screen = tk.getScreenSize();
            rect = new Rectangle(0, 0, screen.width, screen.height);
            screenImage = robot.createScreenCapture(rect);
            JFrame jf = new JFrame();
            Temp temp = new Temp(jf, screenImage, screen.width, screen.height);
            jf.getContentPane().add(temp, BorderLayout.CENTER);
            jf.setUndecorated(true);
            jf.setSize(screen);
            jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jf.setVisible(true);
            jf.setAlwaysOnTop(true);
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            screen = null;
            rect = null;
            if (screenImage != null) {
                screenImage.flush();
                screenImage = null;
            }
        }
    }

    /**
         * Fixed position capture.
         * */
    private void doFixedPositionCapture() {
        Dimension screen;
        Rectangle rect;
        try {
            this.setVisible(false);
            Thread.sleep(50);
            Toolkit tk = Toolkit.getDefaultToolkit();
            screen = tk.getScreenSize();
            rect = new Rectangle(0, 0, screen.width, screen.height);
            if (lastPostionRectangle != null) {
                captureImage = robot.createScreenCapture(lastPostionRectangle);
                updates();
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        } finally {
            screen = null;
            rect = null;
            screenImage = null;
        }
    }

    /**
         *公共方法,处理保存所有的图片
         */
    public void doSaveAll() {
        if (pictureContainerTabbedPane.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "图片不能为空!!", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser jfc = new JFileChooser(".");
        jfc.addChoosableFileFilter(new GIFfilter());
        jfc.addChoosableFileFilter(new BMPfilter());
        jfc.addChoosableFileFilter(new JPGfilter());
        jfc.addChoosableFileFilter(new PNGfilter());
        int i = jfc.showSaveDialog(this);
        if (i == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            String about = "PNG";
            String ext = file.toString().toLowerCase();
            javax.swing.filechooser.FileFilter ff = jfc.getFileFilter();
            if (ff instanceof JPGfilter) {
                about = "JPG";
            } else if (ff instanceof PNGfilter) {
                about = "PNG";
            } else if (ff instanceof BMPfilter) {
                about = "BMP";
            } else if (ff instanceof GIFfilter) {
                about = "GIF";
            }
            if (ext.endsWith(about.toLowerCase())) {
                ext = ext.substring(0, ext.lastIndexOf(about.toLowerCase()));
            }
            new SaveAllThread(ext, about).setVisible(true);
        }
    }

    public void doCloseAll() {
        for (Component componet : pictureContainerTabbedPane.getComponents()) {
            componet = null;
        }
        pictureContainerTabbedPane.removeAll();
        saveAllButton.setEnabled(false);
        getCloseAllButton().setEnabled(false);
        imageIndex = 0;
        System.gc();
    }

    private class SaveAllThread extends JDialog implements Runnable {

        private String name;

        private String ext;

        private JProgressBar jpb;

        private JLabel info;

        private int allTask, doneTask;

        public SaveAllThread(String name, String ext) {
            super(CaptureScreen.this, "保存", true);
            this.name = name;
            this.ext = ext;
            initWindow();
        }

        private void initWindow() {
            jpb = new JProgressBar();
            allTask = pictureContainerTabbedPane.getTabCount();
            jpb.setMaximum(allTask);
            jpb.setMinimum(0);
            jpb.setValue(0);
            jpb.setStringPainted(true);
            setProgressBarString();
            info = new JLabel("正在保存到:");
            this.getContentPane().setBackground(Color.CYAN);
            this.add(info, BorderLayout.NORTH);
            this.add(jpb, BorderLayout.SOUTH);
            this.setUndecorated(true);
            this.setSize(300, 100);
            this.setLocationRelativeTo(CaptureScreen.this);
            new Thread(this).start();
        }

        private void setProgressBarString() {
            jpb.setString("" + doneTask + "/" + allTask);
        }

        public void run() {
            try {
                for (int i = 0; i < allTask; i++) {
                    PicPanel pp = (PicPanel) pictureContainerTabbedPane.getComponentAt(i);
                    BufferedImage image = pp.getImage();
                    File f = new File(name + (doneTask + 1) + "." + ext.toLowerCase());
                    info.setText("<html><b>正在保存到:</b><br>" + f.toString() + "</html>");
                    ImageIO.write(image, ext, f);
                    doneTask++;
                    jpb.setValue(doneTask);
                    setProgressBarString();
                    Thread.sleep(500);
                }
                JOptionPane.showMessageDialog(this, "保存完毕!!");
                this.dispose();
            } catch (Exception exe) {
                exe.printStackTrace();
                this.dispose();
            }
        }
    }

    /**
         *公用的处理保存图片的方法 这个方法不再私有了
         */
    public void doSave(BufferedImage get) {
        try {
            if (get == null) {
                JOptionPane.showMessageDialog(this, "图片不能为空!!", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JFileChooser jfc = new JFileChooser(".");
            jfc.addChoosableFileFilter(new GIFfilter());
            jfc.addChoosableFileFilter(new BMPfilter());
            jfc.addChoosableFileFilter(new JPGfilter());
            jfc.addChoosableFileFilter(new PNGfilter());
            int i = jfc.showSaveDialog(this);
            if (i == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                String about = "PNG";
                String ext = file.toString().toLowerCase();
                javax.swing.filechooser.FileFilter ff = jfc.getFileFilter();
                if (ff instanceof JPGfilter) {
                    about = "JPG";
                    if (!ext.endsWith(".jpg")) {
                        String ns = ext + ".jpg";
                        file = new File(ns);
                    }
                } else if (ff instanceof PNGfilter) {
                    about = "PNG";
                    if (!ext.endsWith(".png")) {
                        String ns = ext + ".png";
                        file = new File(ns);
                    }
                } else if (ff instanceof BMPfilter) {
                    about = "BMP";
                    if (!ext.endsWith(".bmp")) {
                        String ns = ext + ".bmp";
                        file = new File(ns);
                    }
                } else if (ff instanceof GIFfilter) {
                    about = "GIF";
                    if (!ext.endsWith(".gif")) {
                        String ns = ext + ".gif";
                        file = new File(ns);
                    }
                }
                if (ImageIO.write(get, about, file)) {
                    JOptionPane.showMessageDialog(this, "保存成功！");
                } else JOptionPane.showMessageDialog(this, "保存失败！");
            }
        } catch (Exception exe) {
            exe.printStackTrace();
        }
    }

    /**
         *公共的处理把当前的图片加入剪帖板的方法
         */
    public void doCopy(final BufferedImage image) {
        doCopy(image, true);
    }

    public void doCopy(final BufferedImage image, boolean showMsgFlg) {
        try {
            if (captureImage == null) {
                JOptionPane.showMessageDialog(this, "图片不能为空!!", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Transferable trans = new Transferable() {

                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[] { DataFlavor.imageFlavor };
                }

                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return DataFlavor.imageFlavor.equals(flavor);
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if (isDataFlavorSupported(flavor)) {
                        return image;
                    }
                    throw new UnsupportedFlavorException(flavor);
                }
            };
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
            if (showMsgFlg) JOptionPane.showMessageDialog(this, "已复制到系统粘帖板!!");
        } catch (Exception exe) {
            exe.printStackTrace();
            JOptionPane.showMessageDialog(this, "复制到系统粘帖板出错!!", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doClose(Component c) {
        pictureContainerTabbedPane.remove(c);
        c = null;
        if (pictureContainerTabbedPane.getTabCount() < 1) {
            saveAllButton.setEnabled(false);
            getCloseAllButton().setEnabled(false);
            imageIndex = 0;
        }
        System.gc();
    }

    public void actionPerformed(ActionEvent ae) {
        Object source = ae.getSource();
        if (source == startCaptureButton) {
            doStart();
        } else if (source == cancelButton) {
            System.exit(0);
        } else if (source == metalLookAndFeelRadioButton) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        } else if (source == systemLookAndFeelRadioButton) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception exe) {
                exe.printStackTrace();
            }
        } else if (source == saveAllButton) {
            doSaveAll();
        } else if (source == getFixedPositionCaptureButton()) {
            doFixedPositionCapture();
        } else if (source == getCloseAllButton()) {
            doCloseAll();
        }
    }

    private class PicPanel extends JPanel implements ActionListener {

        JButton save, copy, close;

        BufferedImage get;

        public PicPanel(BufferedImage get) {
            super(new BorderLayout());
            this.get = get;
            initPanel();
        }

        public BufferedImage getImage() {
            return get;
        }

        private void initPanel() {
            save = new JButton("保存(S)");
            copy = new JButton("复制到剪帖板(C)");
            close = new JButton("关闭(X)");
            save.setMnemonic('S');
            copy.setMnemonic('C');
            close.setMnemonic('X');
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(copy);
            buttonPanel.add(save);
            buttonPanel.add(close);
            JLabel icon = new JLabel(new ImageIcon(get));
            this.add(new JScrollPane(icon), BorderLayout.CENTER);
            this.add(buttonPanel, BorderLayout.SOUTH);
            save.addActionListener(this);
            copy.addActionListener(this);
            close.addActionListener(this);
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == save) {
                doSave(get);
            } else if (source == copy) {
                doCopy(get);
            } else if (source == close) {
                get = null;
                doClose(this);
            }
        }
    }

    private class BMPfilter extends javax.swing.filechooser.FileFilter {

        public BMPfilter() {
        }

        public boolean accept(File file) {
            if (file.toString().toLowerCase().endsWith(".bmp") || file.isDirectory()) {
                return true;
            } else return false;
        }

        public String getDescription() {
            return "*.BMP(BMP图像)";
        }
    }

    private class JPGfilter extends javax.swing.filechooser.FileFilter {

        public JPGfilter() {
        }

        public boolean accept(File file) {
            if (file.toString().toLowerCase().endsWith(".jpg") || file.isDirectory()) {
                return true;
            } else return false;
        }

        public String getDescription() {
            return "*.JPG(JPG图像)";
        }
    }

    private class GIFfilter extends javax.swing.filechooser.FileFilter {

        public GIFfilter() {
        }

        public boolean accept(File file) {
            if (file.toString().toLowerCase().endsWith(".gif") || file.isDirectory()) {
                return true;
            } else return false;
        }

        public String getDescription() {
            return "*.GIF(GIF图像)";
        }
    }

    private class PNGfilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File file) {
            if (file.toString().toLowerCase().endsWith(".png") || file.isDirectory()) {
                return true;
            } else return false;
        }

        public String getDescription() {
            return "*.PNG(PNG图像)";
        }
    }

    private class Temp extends JPanel implements MouseListener, MouseMotionListener {

        private BufferedImage bi;

        private int width, height;

        private int startX, startY, endX, endY, tempX, tempY;

        private JFrame jf;

        private Rectangle select = new Rectangle(0, 0, 0, 0);

        private Cursor cs = new Cursor(Cursor.CROSSHAIR_CURSOR);

        private States current = States.DEFAULT;

        private Rectangle[] rec;

        public static final int START_X = 1;

        public static final int START_Y = 2;

        public static final int END_X = 3;

        public static final int END_Y = 4;

        private int currentX, currentY;

        private Point p = new Point();

        private boolean showTip = true;

        public Temp(JFrame jf, BufferedImage bi, int width, int height) {
            this.jf = jf;
            this.bi = bi;
            this.width = width;
            this.height = height;
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            this.setDoubleBuffered(false);
            initRecs();
        }

        private void initRecs() {
            rec = new Rectangle[8];
            for (int i = 0; i < rec.length; i++) {
                rec[i] = new Rectangle();
            }
        }

        public void paintComponent(Graphics g) {
            g.drawImage(bi, 0, 0, width, height, this);
            g.setColor(Color.RED);
            g.drawLine(startX, startY, endX, startY);
            g.drawLine(startX, endY, endX, endY);
            g.drawLine(startX, startY, startX, endY);
            g.drawLine(endX, startY, endX, endY);
            int x = startX < endX ? startX : endX;
            int y = startY < endY ? startY : endY;
            select = new Rectangle(x, y, Math.abs(endX - startX), Math.abs(endY - startY));
            int x1 = (startX + endX) / 2;
            int y1 = (startY + endY) / 2;
            g.fillRect(x1 - 2, startY - 2, 5, 5);
            g.fillRect(x1 - 2, endY - 2, 5, 5);
            g.fillRect(startX - 2, y1 - 2, 5, 5);
            g.fillRect(endX - 2, y1 - 2, 5, 5);
            g.fillRect(startX - 2, startY - 2, 5, 5);
            g.fillRect(startX - 2, endY - 2, 5, 5);
            g.fillRect(endX - 2, startY - 2, 5, 5);
            g.fillRect(endX - 2, endY - 2, 5, 5);
            rec[0] = new Rectangle(x - 5, y - 5, 10, 10);
            rec[1] = new Rectangle(x1 - 5, y - 5, 10, 10);
            rec[2] = new Rectangle((startX > endX ? startX : endX) - 5, y - 5, 10, 10);
            rec[3] = new Rectangle((startX > endX ? startX : endX) - 5, y1 - 5, 10, 10);
            rec[4] = new Rectangle((startX > endX ? startX : endX) - 5, (startY > endY ? startY : endY) - 5, 10, 10);
            rec[5] = new Rectangle(x1 - 5, (startY > endY ? startY : endY) - 5, 10, 10);
            rec[6] = new Rectangle(x - 5, (startY > endY ? startY : endY) - 5, 10, 10);
            rec[7] = new Rectangle(x - 5, y1 - 5, 10, 10);
            if (showTip) {
                g.setColor(Color.CYAN);
                g.fillRect(p.x, p.y, 170, 20);
                g.setColor(Color.RED);
                g.drawRect(p.x, p.y, 170, 20);
                g.setColor(Color.BLACK);
                g.drawString("请按住鼠标左键不放选择截图区", p.x, p.y + 15);
            }
        }

        private void initSelect(States state) {
            switch(state) {
                case DEFAULT:
                    currentX = 0;
                    currentY = 0;
                    break;
                case EAST:
                    currentX = (endX > startX ? END_X : START_X);
                    currentY = 0;
                    break;
                case WEST:
                    currentX = (endX > startX ? START_X : END_X);
                    currentY = 0;
                    break;
                case NORTH:
                    currentX = 0;
                    currentY = (startY > endY ? END_Y : START_Y);
                    break;
                case SOUTH:
                    currentX = 0;
                    currentY = (startY > endY ? START_Y : END_Y);
                    break;
                case NORTH_EAST:
                    currentY = (startY > endY ? END_Y : START_Y);
                    currentX = (endX > startX ? END_X : START_X);
                    break;
                case NORTH_WEST:
                    currentY = (startY > endY ? END_Y : START_Y);
                    currentX = (endX > startX ? START_X : END_X);
                    break;
                case SOUTH_EAST:
                    currentY = (startY > endY ? START_Y : END_Y);
                    currentX = (endX > startX ? END_X : START_X);
                    break;
                case SOUTH_WEST:
                    currentY = (startY > endY ? START_Y : END_Y);
                    currentX = (endX > startX ? START_X : END_X);
                    break;
                default:
                    currentX = 0;
                    currentY = 0;
                    break;
            }
        }

        public void mouseMoved(MouseEvent me) {
            doMouseMoved(me);
            initSelect(current);
            if (showTip) {
                p = me.getPoint();
                repaint();
            }
        }

        private void doMouseMoved(MouseEvent me) {
            if (select.contains(me.getPoint())) {
                this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                current = States.MOVE;
            } else {
                States[] st = States.values();
                for (int i = 0; i < rec.length; i++) {
                    if (rec[i].contains(me.getPoint())) {
                        current = st[i];
                        this.setCursor(st[i].getCursor());
                        return;
                    }
                }
                this.setCursor(cs);
                current = States.DEFAULT;
            }
        }

        public void mouseExited(MouseEvent me) {
            System.out.println("mouseExited" + me.getButton());
        }

        public void mouseEntered(MouseEvent me) {
            System.out.println("mouseEntered" + me.getButton());
        }

        public void mouseDragged(MouseEvent me) {
            System.out.println("mouseDragged" + me.getButton());
            int x = me.getX();
            int y = me.getY();
            if (current == States.MOVE) {
                startX += (x - tempX);
                startY += (y - tempY);
                endX += (x - tempX);
                endY += (y - tempY);
                tempX = x;
                tempY = y;
            } else if (current == States.EAST || current == States.WEST) {
                if (currentX == START_X) {
                    startX += (x - tempX);
                    tempX = x;
                } else {
                    endX += (x - tempX);
                    tempX = x;
                }
            } else if (current == States.NORTH || current == States.SOUTH) {
                if (currentY == START_Y) {
                    startY += (y - tempY);
                    tempY = y;
                } else {
                    endY += (y - tempY);
                    tempY = y;
                }
            } else if (current == States.NORTH_EAST || current == States.NORTH_EAST || current == States.SOUTH_EAST || current == States.SOUTH_WEST) {
                if (currentY == START_Y) {
                    startY += (y - tempY);
                    tempY = y;
                } else {
                    endY += (y - tempY);
                    tempY = y;
                }
                if (currentX == START_X) {
                    startX += (x - tempX);
                    tempX = x;
                } else {
                    endX += (x - tempX);
                    tempX = x;
                }
            } else {
                startX = tempX;
                startY = tempY;
                endX = me.getX();
                endY = me.getY();
            }
            this.repaint();
        }

        public void mousePressed(MouseEvent me) {
            if (me.getButton() == 3) return;
            System.out.println("mousePressed" + me.getButton());
            showTip = false;
            tempX = me.getX();
            tempY = me.getY();
        }

        public void mouseReleased(MouseEvent me) {
            if (me.getButton() == 3) return;
            System.out.println("mouseReleased" + me.getButton());
            if (me.isPopupTrigger()) {
                if (current == States.MOVE) {
                    showTip = true;
                    p = me.getPoint();
                    startX = 0;
                    startY = 0;
                    endX = 0;
                    endY = 0;
                    repaint();
                } else {
                    jf.dispose();
                    updates();
                }
            } else {
                System.out.println("else" + me.getButton());
            }
        }

        public void mouseClicked(MouseEvent me) {
            System.out.println("me.getButton() =" + me.getButton());
            if (me.getButton() != 1) {
                jf.dispose();
                jf = null;
                if (bi != null) {
                    bi.flush();
                    bi = null;
                }
                return;
            }
            if (me.getClickCount() == 2) {
                Point p = me.getPoint();
                if (select.contains(p)) {
                    if (select.x + select.width < this.getWidth() && select.y + select.height < this.getHeight()) {
                        captureImage = bi.getSubimage(select.x, select.y, select.width, select.height);
                    } else {
                        int wid = select.width, het = select.height;
                        if (select.x + select.width >= this.getWidth()) {
                            wid = this.getWidth() - select.x;
                        }
                        if (select.y + select.height >= this.getHeight()) {
                            het = this.getHeight() - select.y;
                        }
                        captureImage = bi.getSubimage(select.x, select.y, wid, het);
                    }
                    jf.dispose();
                    jf = null;
                    updates();
                    if (bi != null) {
                        bi.flush();
                        bi = null;
                    }
                    lastPostionRectangle = select;
                    if (lastPostionRectangle != null && !getFixedPositionCaptureButton().isEnabled()) {
                        getFixedPositionCaptureButton().setEnabled(true);
                    }
                }
            }
        }
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new CaptureScreen();
            }
        });
    }
}

enum States {

    NORTH_WEST(new Cursor(Cursor.NW_RESIZE_CURSOR)), NORTH(new Cursor(Cursor.N_RESIZE_CURSOR)), NORTH_EAST(new Cursor(Cursor.NE_RESIZE_CURSOR)), EAST(new Cursor(Cursor.E_RESIZE_CURSOR)), SOUTH_EAST(new Cursor(Cursor.SE_RESIZE_CURSOR)), SOUTH(new Cursor(Cursor.S_RESIZE_CURSOR)), SOUTH_WEST(new Cursor(Cursor.SW_RESIZE_CURSOR)), WEST(new Cursor(Cursor.W_RESIZE_CURSOR)), MOVE(new Cursor(Cursor.MOVE_CURSOR)), DEFAULT(new Cursor(Cursor.DEFAULT_CURSOR));

    private Cursor cs;

    States(Cursor cs) {
        this.cs = cs;
    }

    public Cursor getCursor() {
        return cs;
    }
}
