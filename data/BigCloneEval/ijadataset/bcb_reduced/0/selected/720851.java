package org.rhwlab.image;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import net.sf.ij.jaiio.BufferedImageCreator;
import org.rhwlab.acetree.AceTree;
import org.rhwlab.acetree.AnnotInfo;
import org.rhwlab.acetree.NucUtils;
import org.rhwlab.help.AceTreeHelp;
import org.rhwlab.image.Image3D.SublineageDisplayProperty;
import org.rhwlab.image.Image3D.PropertiesTab.SublineageUI;
import org.rhwlab.snight.NucleiMgr;
import org.rhwlab.snight.Nucleus;
import org.rhwlab.tree.Cell;
import org.rhwlab.utils.C;
import org.rhwlab.utils.EUtils;

/**
 * Provides a JFrame window to contain the ImageJ ImagePlus object
 * 
 * @author biowolp
 * @version 1.0 January 25, 2005
 */
public class ImageWindow extends JFrame implements ActionListener, KeyListener, Runnable {

    ImageCanvas iImgCanvas;

    static ImagePlus iImgPlus;

    String iTitle;

    static Object[] iSpecialEffect;

    AceTree iAceTree;

    Vector iAnnotsShown;

    MouseHandler iMouseHandler;

    boolean iMouseEventHandled;

    int iImageTime;

    int iTimeInc;

    int iImagePlane;

    int iPlaneInc;

    boolean iIsMainImgWindow;

    boolean iIsRightMouseButton;

    boolean iSaveImage;

    boolean iSaveInProcess;

    String iSaveImageDirectory;

    boolean iUseRobot;

    boolean iNewConstruction;

    public static ColorSchemeDisplayProperty[] iDispProps;

    private JPanel iControlPanel;

    protected JMenuBar iMenuBar;

    protected JToolBar iToolBar;

    protected JButton iHelp;

    protected JButton iProperties;

    static boolean cAcbTree = false;

    static byte[] iRpix;

    static byte[] iGpix;

    static byte[] iBpix;

    public static String cZipTifFilePath;

    public static String cTifPrefix;

    public static String cTifPrefixR;

    public static int cUseZip;

    static ZipImage cZipImage;

    public static NucleiMgr cNucleiMgr;

    public static int cImageWidth;

    public static int cImageHeight;

    public static int cLineWidth;

    public static String cCurrentImageFile;

    public static String cCurrentImagePart;

    public static EditImage3 cEditImage3;

    /**
     * this is the constructor that is actually used
     * note that there are many static functions and class variables
     */
    public ImageWindow(String title, ImagePlus imgPlus) {
        super(title);
        iTitle = title;
        iImgPlus = imgPlus;
        ImageCanvas ic = new ImageCanvas(imgPlus);
        iImgCanvas = ic;
        iDispProps = getDisplayProps();
        iToolBar = new JToolBar();
        iToolBar.setLayout(new GridLayout(1, 0));
        iToolBar.setMinimumSize(new Dimension(ImageWindow.cImageWidth, 30));
        iToolBar.setPreferredSize(new Dimension(ImageWindow.cImageWidth, 30));
        iHelp = new JButton("Help");
        iHelp.addActionListener(this);
        iToolBar.add(iHelp);
        iProperties = new JButton("Properties");
        iProperties.addActionListener(this);
        iToolBar.add(iProperties);
        Container c = getContentPane();
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(iToolBar, BorderLayout.NORTH);
        jp.add(ic, BorderLayout.SOUTH);
        c.add(jp);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        WinEventMgr wem = new WinEventMgr();
        addWindowFocusListener(wem);
        addWindowListener(wem);
        iMouseHandler = new MouseHandler(this);
        iImgCanvas.addMouseMotionListener(iMouseHandler);
        iImgCanvas.addMouseListener(iMouseHandler);
        setImageTimeAndPlaneFromTitle();
        iAnnotsShown = new Vector();
        iIsRightMouseButton = false;
        iSaveImage = false;
        iSaveImageDirectory = null;
        iUseRobot = false;
        iImgCanvas.addKeyListener(this);
    }

    public static void setStaticParameters(String zipTifFilePath, String tifPrefix, int useZip) {
        cZipTifFilePath = zipTifFilePath;
        cTifPrefix = tifPrefix;
        cUseZip = useZip;
        if (cUseZip == 1) cZipImage = new ZipImage(cZipTifFilePath);
        cLineWidth = 2;
        String[] sa = cTifPrefix.split("/");
        if (sa.length > 1) cTifPrefixR = sa[0] + "R" + C.Fileseparator + sa[1];
        System.out.println("cZipTifFilePath, cTifPrefix, cTifPrefixR: " + cZipTifFilePath + CS + cTifPrefix + CS + cTifPrefixR);
    }

    public static void setNucleiMgr(NucleiMgr nucleiMgr) {
        cNucleiMgr = nucleiMgr;
    }

    public static ImagePlus makeImage(String s) {
        cCurrentImageFile = s;
        ImagePlus ip = null;
        switch(cUseZip) {
            case 0:
                ip = doMakeImageFromTif(s);
                break;
            case 1:
                ip = doMakeImageFromZip(s);
                break;
            default:
                ip = doMakeImageFromZip2(s);
                break;
        }
        if (ip != null) {
            cImageWidth = ip.getWidth();
            cImageHeight = ip.getHeight();
        }
        if (ip == null) return iImgPlus; else return ip;
    }

    public static ImagePlus doMakeImageFromZip(String s) {
        if (cZipImage == null) cZipImage = new ZipImage(cZipTifFilePath);
        ZipEntry ze = cZipImage.getZipEntry(s);
        ImagePlus ip;
        if (ze == null) {
            ip = new ImagePlus();
            ImageProcessor iproc = new ColorProcessor(cImageWidth, cImageHeight);
            ip.setProcessor(s, iproc);
        } else ip = cZipImage.readData(ze);
        return ip;
    }

    public static ImagePlus doMakeImageFromZip2(String s) {
        cZipImage = new ZipImage(cZipTifFilePath + "/" + s);
        int k1 = s.indexOf("/") + 1;
        String ss = s.substring(k1);
        int k2 = ss.indexOf(".");
        ss = ss.substring(0, k2);
        ZipEntry ze = null;
        if (cZipImage != null) ze = cZipImage.getZipEntry(ss + ".tif");
        ImagePlus ip;
        if (ze == null) {
            ip = new ImagePlus();
            ImageProcessor iproc = new ColorProcessor(cImageWidth, cImageHeight);
            ip.setProcessor(s, iproc);
        } else ip = cZipImage.readData(ze);
        ColorProcessor iprocColor = (ColorProcessor) ip.getProcessor();
        int[] all = (int[]) iprocColor.getPixels();
        byte[] R = new byte[all.length];
        byte[] G = new byte[all.length];
        byte[] B = new byte[all.length];
        iprocColor.getRGB(R, G, B);
        iRpix = R;
        iGpix = G;
        iBpix = B;
        return ip;
    }

    private static void showError(String fileName) {
        new Throwable().printStackTrace();
        String message = "Exiting: cannot find image\n";
        message += fileName;
        JOptionPane pane = new JOptionPane(message);
        JDialog dialog = pane.createDialog(null, "Error");
        dialog.show();
    }

    public static ImagePlus doMakeImageFromTif(String s) {
        cCurrentImagePart = s;
        FileInputStream fis;
        ImagePlus ip = null;
        String ss = cZipTifFilePath + C.Fileseparator + s;
        ip = new Opener().openImage(ss);
        if (ip != null) {
            cImageWidth = ip.getWidth();
            cImageHeight = ip.getHeight();
            ip = convertToRGB(ip);
        } else {
            ip = new ImagePlus();
            ImageProcessor iproc = new ColorProcessor(cImageWidth, cImageHeight);
            ip.setProcessor(s, iproc);
        }
        return ip;
    }

    public static ImagePlus readData(FileInputStream fis, boolean bogus) {
        if (fis == null) return null;
        int byteCount;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        try {
            InputStream is = (InputStream) fis;
            byte data[] = new byte[DATA_BLOCK_SIZE];
            while ((byteCount = is.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                out.write(data, 0, byteCount);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return openTiff(new ByteArrayInputStream(out.toByteArray()), true);
    }

    public static ImagePlus readData(FileInputStream fis) {
        if (fis == null) return null;
        byte[] ba = readByteArray(fis);
        return openTiff(new ByteArrayInputStream(ba), true);
    }

    public static byte[] readByteArray(FileInputStream fis) {
        if (fis == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteCount;
        byte[] buf = new byte[4096];
        try {
            InputStream is = (InputStream) fis;
            byte data[] = new byte[DATA_BLOCK_SIZE];
            while ((byteCount = is.read(data, 0, DATA_BLOCK_SIZE)) != -1) {
                out.write(data, 0, byteCount);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return out.toByteArray();
    }

    /** Attempts to open the specified inputStream as a
    TIFF, returning an ImagePlus object if successful. */
    public static ImagePlus openTiff(InputStream in, boolean convertToRGB) {
        if (in == null) return null;
        FileInfo[] info = null;
        try {
            TiffDecoder td = new TiffDecoder(in, null);
            info = td.getTiffInfo();
        } catch (FileNotFoundException e) {
            IJ.error("TiffDecoder", "File not found: " + e.getMessage());
            return null;
        } catch (Exception e) {
            IJ.error("TiffDecoder", "" + e);
            return null;
        }
        ImagePlus imp = null;
        if (IJ.debugMode) IJ.log(info[0].info);
        FileOpener fo = new FileOpener(info[0]);
        imp = fo.open(false);
        if (info[0].getBytesPerPixel() == 1 && convertToRGB) {
            imp = convertToRGB(imp);
        }
        return imp;
    }

    /**
     * If the images in the zip archive are 8 bit tiffs,
     * we use that as the green plane of an RGB image processor
     * so the program is always showing RGB images
     * 
     * @param ip an Image processor obtained from the image file
     * @return
     */
    private static ImagePlus convertToRGB(ImagePlus ip) {
        ImageProcessor iproc = ip.getProcessor();
        byte[] bpix = (byte[]) iproc.getPixels();
        byte[] R = new byte[bpix.length];
        byte[] G = new byte[bpix.length];
        byte[] B = new byte[bpix.length];
        ColorProcessor iproc3 = new ColorProcessor(iproc.getWidth(), iproc.getHeight());
        iproc3.getRGB(R, G, B);
        G = bpix;
        R = getRedChannel(R);
        iRpix = R;
        iGpix = G;
        iBpix = B;
        return buildImagePlus(ip);
    }

    private static ImagePlus buildImagePlus(ImagePlus ip) {
        ImageProcessor iproc = ip.getProcessor();
        ColorProcessor iproc3 = new ColorProcessor(iproc.getWidth(), iproc.getHeight());
        iproc3.setRGB(iRpix, iGpix, iBpix);
        ip.setProcessor("test", iproc3);
        return ip;
    }

    protected static ImagePlus makeRedImagePlus(ImagePlus ip) {
        ImageProcessor iproc = ip.getProcessor();
        ColorProcessor iproc3 = new ColorProcessor(iproc.getWidth(), iproc.getHeight());
        iproc3.setRGB(iRpix, new byte[iRpix.length], new byte[iRpix.length]);
        ip.setProcessor("test", iproc3);
        return ip;
    }

    protected static ImagePlus makeGreenImagePlus(ImagePlus ip) {
        ImageProcessor iproc = ip.getProcessor();
        ColorProcessor iproc3 = new ColorProcessor(iproc.getWidth(), iproc.getHeight());
        iproc3.setRGB(new byte[iRpix.length], iGpix, new byte[iRpix.length]);
        ip.setProcessor("test", iproc3);
        return ip;
    }

    protected static ImagePlus makePlainImagePlus(ImagePlus ip) {
        ImageProcessor iproc = ip.getProcessor();
        ColorProcessor iproc3 = new ColorProcessor(iproc.getWidth(), iproc.getHeight());
        if (cAcbTree) {
            iproc3.setRGB(iRpix, iRpix, iRpix);
        } else {
            iproc3.setRGB(new byte[iRpix.length], new byte[iRpix.length], new byte[iRpix.length]);
        }
        ip.setProcessor("test", iproc3);
        return ip;
    }

    private static byte[] getRedChannel(byte[] R) {
        String fileName = makeRedChannelName();
        File f = new File(fileName);
        if (f.exists()) {
            FileInputStream fis;
            ImagePlus ip = null;
            ip = new Opener().openImage(fileName);
            if (ip != null) {
                ByteProcessor bproc = (ByteProcessor) ip.getProcessor();
                R = (byte[]) bproc.getPixels();
            } else {
                System.out.println("getRedChannel, Opener returned null ip");
            }
        } else {
        }
        return R;
    }

    private static String makeRedChannelName() {
        String s = cCurrentImageFile;
        String ss = cCurrentImagePart;
        ss = ss.substring(3);
        s = cZipTifFilePath + C.Fileseparator + "/tifR/" + ss;
        return s;
    }

    public ImageWindow() {
    }

    /**
     * this constructor for test purposes only
     */
    public ImageWindow(String title, ImagePlus imgPlus, boolean test) {
        super(title);
        iTitle = title;
        iImgPlus = imgPlus;
        ImageCanvas ic = new ImageCanvas(imgPlus);
        iImgCanvas = ic;
        getContentPane().add(ic);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public class ColorSchemeDisplayProperty {

        public String iName;

        public int iLineageNum;

        public ColorSchemeDisplayProperty(String name, int lineageNum) {
            iName = name;
            iLineageNum = lineageNum;
        }
    }

    public class ColorSchemeUI {

        public JPanel iPanel;

        public JTextField iTF;

        public JComboBox iCB;

        public JLabel iLabel;

        public ColorSchemeUI(int i) {
            iPanel = new JPanel();
            iPanel.setLayout(new GridLayout(1, 2));
            iTF = new JTextField(iDispProps[i].iName, WIDTH);
            iLabel = new JLabel(iDispProps[i].iName);
            String[] list;
            list = COLORS;
            if (i == 5) list = SIZES;
            iCB = new JComboBox(list);
            iCB.setSelectedIndex(iDispProps[i].iLineageNum);
            iPanel.add(iLabel);
            iPanel.add(iCB);
            iPanel.setMaximumSize(new Dimension(200, 10));
        }

        private String[] COLORS = { "red", "blue", "green", "yellow", "cyan", "magenta   ", "pink", "gray", "white" };

        private String[] SIZES = { "1", "2", "3" };
    }

    public class PropertiesTab implements ActionListener {

        JPanel iPanel;

        ColorSchemeUI[] iCSUI;

        public PropertiesTab() {
            Border blackline = BorderFactory.createLineBorder(Color.black);
            iDispProps = getDisplayProps();
            iCSUI = new ColorSchemeUI[iDispProps.length];
            iPanel = new JPanel();
            iPanel.setLayout(new BorderLayout());
            iPanel.setBorder(blackline);
            JPanel lineagePanel = new JPanel();
            JPanel dummyPanel = new JPanel();
            JPanel topPart = new JPanel();
            topPart.setLayout(new GridLayout(1, 2));
            lineagePanel.setLayout(new GridLayout(0, 1));
            lineagePanel.setBorder(blackline);
            topPart.add(lineagePanel);
            topPart.add(dummyPanel);
            JPanel[] testPanel = new JPanel[iDispProps.length];
            JTextField textField;
            JComboBox cb;
            JPanel labelPanel = new JPanel();
            JLabel sublineage = new JLabel("item");
            JLabel color = new JLabel("color");
            labelPanel.setLayout(new GridLayout(1, 2));
            labelPanel.add(sublineage);
            labelPanel.add(color);
            lineagePanel.add(labelPanel);
            for (int i = 0; i < iDispProps.length; i++) {
                iCSUI[i] = new ColorSchemeUI(i);
                lineagePanel.add(iCSUI[i].iPanel);
            }
            lineagePanel.setMaximumSize(new Dimension(200, 200));
            iPanel.add(topPart, BorderLayout.NORTH);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 3));
            JButton reset = new JButton("Reset");
            JButton apply = new JButton("Apply");
            JButton cancel = new JButton("Cancel");
            buttonPanel.add(apply);
            reset.addActionListener(this);
            apply.addActionListener(this);
            cancel.addActionListener(this);
            buttonPanel.add(reset);
            buttonPanel.add(apply);
            buttonPanel.add(cancel);
            JPanel botPart = new JPanel();
            botPart.setLayout(new GridLayout(5, 1));
            botPart.add(new JPanel());
            botPart.add(buttonPanel);
            botPart.add(new JPanel());
            botPart.add(new JPanel());
            botPart.add(new JPanel());
            iPanel.add(botPart, BorderLayout.CENTER);
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Reset")) {
                iDispProps = getDisplayProps();
                for (int i = 0; i < iDispProps.length; i++) {
                    iCSUI[i].iLabel.setText(iDispProps[i].iName);
                    iCSUI[i].iCB.setSelectedIndex(iDispProps[i].iLineageNum);
                }
            } else if (command.equals("Apply")) {
                for (int i = 0; i < iDispProps.length; i++) {
                    String name = iCSUI[i].iTF.getText();
                    if (name.length() == 0) name = "-";
                    int num = iCSUI[i].iCB.getSelectedIndex();
                    iDispProps[i].iName = name;
                    iDispProps[i].iLineageNum = num;
                }
            }
        }

        public JPanel getPanel() {
            return iPanel;
        }

        private String[] COLORS = { "red", "blue", "green", "yellow", "cyan", "magenta   ", "pink", "gray", "white" };

        private String[] SIZES = { "1", "2", "3" };

        private static final int WIDTH = 15;
    }

    public ColorSchemeDisplayProperty[] getDisplayProps() {
        ColorSchemeDisplayProperty[] dispProps = { new ColorSchemeDisplayProperty("normal centroid", 1), new ColorSchemeDisplayProperty("selected centroid", 8), new ColorSchemeDisplayProperty("annotations", 8), new ColorSchemeDisplayProperty("upper sister", 4), new ColorSchemeDisplayProperty("lower sister", 5), new ColorSchemeDisplayProperty("line size", 1) };
        return dispProps;
    }

    private int getLineageNumber(String name) {
        int num = iDispProps.length;
        for (int i = 0; i < iDispProps.length; i++) {
            if (name.indexOf(iDispProps[i].iName) >= 0) {
                num = iDispProps[i].iLineageNum;
                break;
            }
        }
        return num;
    }

    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = null;
        JMenuItem test = null;
        menu = new JMenu("dummy");
        menuBar.add(menu);
        test = new JMenuItem("dummy");
        menu.add(test);
        return menuBar;
    }

    public void setAceTree(AceTree aceTree) {
        iAceTree = aceTree;
    }

    public AceTree getAceTree() {
        return iAceTree;
    }

    public ImagePlus refreshDisplay(String imageName) {
        if (imageName == null) imageName = iTitle; else {
            if (imageName.indexOf(cTifPrefix) == -1) {
                imageName = cTifPrefix + imageName;
            }
            iTitle = imageName;
            setTitle(iTitle);
        }
        if (iIsMainImgWindow) {
            iTimeInc = iAceTree.getTimeInc();
            iPlaneInc = iAceTree.getPlaneInc();
            iImageTime = iAceTree.getImageTime();
            iImagePlane = iAceTree.getImagePlane();
        } else {
            iTimeInc = 0;
            iPlaneInc = 0;
            setImageTimeAndPlaneFromTitle();
        }
        String random = RANDOMT;
        if (cUseZip > 0) random = RANDOMF;
        int k = imageName.indexOf(random);
        if (k > -1) imageName = imageName.substring(0, k + random.length() - 1);
        ImagePlus ip = null;
        ip = makeImage(imageName);
        if (ip == null) {
            iAceTree.pausePlayerControl();
            System.out.println("no ip for: " + iTitle);
        }
        switch(iAceTree.getColor()) {
            case 1:
                ip = makeGreenImagePlus(ip);
                break;
            case 2:
                ip = makeRedImagePlus(ip);
                break;
            case 3:
                ip = makePlainImagePlus(ip);
                break;
            default:
        }
        if (ip != null) iImgPlus.setProcessor(imageName, ip.getProcessor());
        if (iIsMainImgWindow && iAceTree.isTracking()) iAceTree.addMainAnnotation();
        if (iAceTree.getShowCentroids()) showCentroids();
        if (iAceTree.getShowAnnotations()) showAnnotations();
        if (iSpecialEffect != null) showSpecialEffect();
        iImgCanvas.repaint();
        return iImgPlus;
    }

    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch(code) {
            case KeyEvent.VK_UP:
                iAceTree.actionPerformed(new ActionEvent(this, 0, AceTree.UP));
                break;
            case KeyEvent.VK_DOWN:
                iAceTree.actionPerformed(new ActionEvent(this, 0, AceTree.DOWN));
                break;
            case KeyEvent.VK_LEFT:
                iAceTree.actionPerformed(new ActionEvent(this, 0, AceTree.PREV));
                break;
            case KeyEvent.VK_RIGHT:
                iAceTree.actionPerformed(new ActionEvent(this, 0, AceTree.NEXTT));
                break;
            default:
                return;
        }
    }

    public void quickRefresh() {
        iImgCanvas.repaint();
    }

    public void setSpecialEffect(Object[] specialEffect) {
        iSpecialEffect = specialEffect;
    }

    protected void showSpecialEffect() {
        if (!iAceTree.isTracking()) return;
        int x1 = ((Integer) iSpecialEffect[0]).intValue();
        int y1 = ((Integer) iSpecialEffect[1]).intValue();
        int z1 = ((Integer) iSpecialEffect[2]).intValue();
        int x2 = ((Integer) iSpecialEffect[3]).intValue();
        int y2 = ((Integer) iSpecialEffect[4]).intValue();
        int r2 = ((Integer) iSpecialEffect[5]).intValue();
        int z2 = ((Integer) iSpecialEffect[6]).intValue();
        String s = (String) iSpecialEffect[7];
        int offset = r2 + 4;
        if (y2 < y1) offset = -offset;
        ImageProcessor iproc = getImagePlus().getProcessor();
        iproc.setColor(COLOR[iDispProps[LOWERSIS].iLineageNum]);
        if (z2 <= z1) iproc.setColor(COLOR[iDispProps[UPPERSIS].iLineageNum]);
        iproc.setLineWidth(cLineWidth);
        iproc.drawLine(x1, y1, x2, y2);
        iproc.drawPolygon(EUtils.pCircle((int) x2, (int) y2, (int) r2));
        iproc.drawString("    " + s + "(" + z2 + ")", x2, y2 + offset);
    }

    private void redrawMe() {
        iImgCanvas.repaint();
    }

    protected void setImageTimeAndPlaneFromTitle() {
        int k = iTitle.lastIndexOf(DASHT) + DASHT.length();
        if (k <= 1) {
            iImageTime = 1;
            iImagePlane = 15;
            iTimeInc = 0;
            iPlaneInc = 0;
            String random = RANDOMT;
            if (cUseZip > 0) random = RANDOMF;
            iIsMainImgWindow = iTitle.indexOf(random) == -1;
            return;
        }
        System.out.println("setImage..: " + k);
        String time = iTitle.substring(k, k + 3);
        iImageTime = Integer.parseInt(time);
        String s = iTitle.substring(k);
        k = s.indexOf(DASHP) + DASHP.length();
        String plane = s.substring(k, k + 2);
        iImagePlane = Integer.parseInt(plane);
        iTimeInc = 0;
        iPlaneInc = 0;
        String random = RANDOMT;
        if (cUseZip > 0) random = RANDOMF;
        iIsMainImgWindow = iTitle.indexOf(random) == -1;
    }

    public ImageCanvas getCanvas() {
        return iImgCanvas;
    }

    public ImagePlus getImagePlus() {
        return iImgPlus;
    }

    public void addAnnotation(int mx, int my, boolean dontRemove) {
        if (iIsMainImgWindow) {
            iTimeInc = iAceTree.getTimeInc();
            iImageTime = iAceTree.getImageTime();
            iPlaneInc = iAceTree.getPlaneInc();
        } else {
            iTimeInc = 0;
            iPlaneInc = 0;
        }
        double x, y, r;
        boolean g;
        Nucleus n = cNucleiMgr.findClosestNucleus(mx, my, iImagePlane + iPlaneInc, iImageTime + iTimeInc);
        if (cNucleiMgr.hasCircle(n, (double) (iImagePlane + iPlaneInc))) {
            AnnotInfo ai = new AnnotInfo(n.identity, n.x, n.y);
            boolean itemRemoved = false;
            boolean itemAlreadyPresent = false;
            String test = n.identity;
            AnnotInfo aiTest = null;
            for (int k = 0; k < iAnnotsShown.size(); k++) {
                aiTest = (AnnotInfo) iAnnotsShown.elementAt(k);
                if (aiTest.iName.equals(test)) {
                    itemAlreadyPresent = true;
                    if (!dontRemove) {
                        iAnnotsShown.remove(k);
                        itemRemoved = true;
                    }
                    break;
                }
            }
            if (!itemRemoved && !itemAlreadyPresent) {
                iAnnotsShown.add(ai);
            }
            if (iIsRightMouseButton && iIsMainImgWindow) {
                iIsRightMouseButton = false;
            }
        }
    }

    protected void showCentroids() {
        int time = iImageTime + iTimeInc;
        if (time < 0) {
            iImageTime = 1;
            iTimeInc = 0;
        }
        Vector v = (Vector) cNucleiMgr.getNucleiRecord().elementAt(iImageTime + iTimeInc - 1);
        ImageProcessor iproc = getImagePlus().getProcessor();
        iproc.setColor(COLOR[iDispProps[NCENTROID].iLineageNum]);
        iproc.setLineWidth(WIDTHS[iDispProps[LINEWIDTH].iLineageNum]);
        Polygon p = null;
        Enumeration e = v.elements();
        Cell currentCell = iAceTree.getCurrentCell();
        while (e.hasMoreElements()) {
            Nucleus n = (Nucleus) e.nextElement();
            if (n.status < 0) continue;
            double x = cNucleiMgr.nucDiameter(n, (double) (iImagePlane + iPlaneInc));
            if (x > 0) {
                if (currentCell != null && n.hashKey != null && n.hashKey.equals(currentCell.getHashKey()) && iAceTree.isTracking()) {
                    iproc.setColor(COLOR[iDispProps[SCENTROID].iLineageNum]);
                }
                iproc.drawPolygon(EUtils.pCircle(n.x, n.y, (int) (x / 2.)));
                iproc.setColor(COLOR[iDispProps[NCENTROID].iLineageNum]);
            }
        }
    }

    private void drawRoi(int plane, Nucleus c, ImageProcessor iproc) {
        double d = cNucleiMgr.nucDiameter(c, plane);
        float fxx = c.x;
        float fyy = c.y;
        fxx -= d / 2;
        fyy -= d / 2;
        int xx = (int) (fxx + 0.5);
        int yy = (int) (fyy + 0.5);
        int dd = (int) (d + 0.5);
        OvalRoi oRoi = new OvalRoi(xx, yy, dd, dd);
        iproc.setColor(new Color(0, 0, 255));
        oRoi.drawPixels(iproc);
        Rectangle r = oRoi.getBounds();
        int width = iproc.getWidth();
        int offset, i;
        for (int y = r.y; y < (r.y + r.height); y++) {
            offset = y * width;
            for (int x = r.x; x <= (r.x + r.width); x++) {
                i = offset + x;
                if (oRoi.contains(x, y)) {
                    int k = iproc.getPixel(x, y);
                    int m = k & -16711936;
                }
            }
        }
    }

    protected void showAnnotations() {
        Vector v = (Vector) cNucleiMgr.getNucleiRecord().elementAt(iImageTime + iTimeInc - 1);
        int size = v.size();
        int[] x = new int[size];
        int[] y = new int[size];
        Vector annots = new Vector();
        Enumeration e = v.elements();
        while (e.hasMoreElements()) {
            AnnotInfo ai = null;
            Nucleus n = (Nucleus) e.nextElement();
            if (n.status >= 0 && (isInList(n.identity) != null)) {
                ai = new AnnotInfo(n.identity, n.x, n.y);
                if (cNucleiMgr.hasCircle(n, (double) (iImagePlane + iPlaneInc))) {
                    annots.add(ai);
                }
            }
        }
        drawStrings(annots, this);
    }

    private void drawStrings(Vector annots, ImageWindow imgWin) {
        ImagePlus imgPlus = imgWin.getImagePlus();
        ImageProcessor imgProc = imgPlus.getProcessor();
        ImageCanvas imgCan = imgWin.getCanvas();
        imgProc.setColor(COLOR[iDispProps[ANNOTATIONS].iLineageNum]);
        imgProc.setFont(new Font("SansSerif", Font.BOLD, 13));
        Enumeration e = annots.elements();
        while (e.hasMoreElements()) {
            AnnotInfo ai = (AnnotInfo) e.nextElement();
            imgProc.moveTo(imgCan.offScreenX(ai.iX), imgCan.offScreenY(ai.iY));
            imgProc.drawString(ai.iName);
        }
        imgPlus.updateAndDraw();
    }

    private void showWhichAnnotations() {
        for (int i = 0; i < iAnnotsShown.size(); i++) {
            System.out.println((AnnotInfo) iAnnotsShown.elementAt(i));
        }
    }

    public void updateCurrentCellAnnotation(Cell newCell, Cell old, int time) {
        AnnotInfo ai = null;
        if (old != null) ai = isInList(old.getName());
        if (ai != null) iAnnotsShown.remove(ai);
        if (time == -1) time = newCell.getTime();
        String s = newCell.getHashKey();
        Nucleus n = null;
        if (s != null) {
            n = cNucleiMgr.getNucleusFromHashkey(newCell.getHashKey(), time);
        }
        if ((n != null) && (isInList(newCell.getName()) == null)) {
            ai = new AnnotInfo(newCell.getName(), n.x, n.y);
            iAnnotsShown.add(ai);
        }
    }

    public void clearAnnotations() {
        iAnnotsShown.clear();
    }

    public void addAnnotation(String name, int x, int y) {
        AnnotInfo ai = new AnnotInfo(name, x, y);
        iAnnotsShown.add(ai);
    }

    protected AnnotInfo isInList(String name) {
        AnnotInfo aiFound = null;
        Enumeration e = iAnnotsShown.elements();
        while (e.hasMoreElements()) {
            AnnotInfo ai = (AnnotInfo) e.nextElement();
            boolean is = ((String) ai.iName).equals(name);
            if (is) {
                aiFound = ai;
                break;
            }
        }
        return aiFound;
    }

    public void saveImageIfEnabled() {
        if (iSaveImage) {
            while (iSaveInProcess) ;
            new Thread(this).start();
        }
    }

    public void run() {
        iSaveInProcess = true;
        int k = 1000;
        if (iNewConstruction) {
            k = 5000;
            iNewConstruction = false;
        }
        try {
            Thread.sleep(k);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        saveImage();
    }

    public void saveImage() {
        String title = makeTitle();
        if (title == null) {
            cancelSaveOperations();
            return;
        }
        Rectangle screenRect = this.getBounds();
        int topAdjust = 23;
        int y = screenRect.y;
        screenRect.y += topAdjust;
        int height = screenRect.height;
        screenRect.height -= topAdjust;
        Robot robot = null;
        BufferedImage image = null;
        if (iUseRobot) {
            try {
                robot = new Robot();
            } catch (AWTException e) {
                println("EXCEPTION -- NO ROBOT -- NOT SAVING");
                iSaveInProcess = false;
                iSaveImage = false;
                iAceTree.iAceMenuBar.resetSaveState();
                return;
            }
            image = robot.createScreenCapture(screenRect);
        } else {
            image = BufferedImageCreator.create((ColorProcessor) iImgPlus.getProcessor());
        }
        try {
            ImageIO.write(image, "jpeg", new File(title));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("file: " + title + " written");
        iSaveInProcess = false;
    }

    public void cancelSaveOperations() {
        println("WARNING: NO IMAGE SAVE PATH -- NOT SAVING!");
        iSaveInProcess = false;
        iSaveImage = false;
        iAceTree.iAceMenuBar.resetSaveState();
        return;
    }

    public String getSaveImageDirectory() {
        if (iSaveImageDirectory != null) return iSaveImageDirectory;
        try {
            Class.forName("net.sf.ij.jaiio.BufferedImageCreator");
        } catch (ClassNotFoundException e) {
            iUseRobot = true;
            println("USING ROBOT FOR IMAGE2D SAVING");
        }
        {
            JFileChooser fc = new JFileChooser("");
            fc.setDialogTitle("Save images to: ");
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = fc.showOpenDialog(null);
            String path = null;
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                path = fc.getSelectedFile().getPath();
                iSaveImageDirectory = path;
                System.out.println("Saving images to: " + path);
            }
            return path;
        }
    }

    private String makeTitle() {
        if (iSaveImageDirectory == null) {
            String dir = getSaveImageDirectory();
            iSaveImageDirectory = dir;
            if (dir == null) return null;
        }
        String s = iTitle;
        int j = s.lastIndexOf(C.Fileseparator) + 1;
        int k = s.lastIndexOf(".");
        s = s.substring(j, k) + ".jpeg";
        s = iSaveImageDirectory + "/" + s;
        return s;
    }

    public void setSaveImageState(boolean saveIt) {
        iSaveImage = saveIt;
    }

    public Object[] getSpecialEffect() {
        return iSpecialEffect;
    }

    private class WinEventMgr extends WindowAdapter {

        public void windowGainedFocus(WindowEvent e) {
            refreshDisplay(null);
        }

        public void windowClosing(WindowEvent e) {
            System.out.println("windowClosing: " + iIsMainImgWindow);
            if (!iIsMainImgWindow) dispose();
        }
    }

    class MouseHandler extends MouseInputAdapter {

        public MouseHandler(ImageWindow iw) {
            super();
        }

        public void mouseMoved(MouseEvent e) {
            iAceTree.mouseMoved(e);
        }

        public void mouseClicked(MouseEvent e) {
            int button = e.getButton();
            if (button == 3) {
                iIsRightMouseButton = true;
            } else {
                iIsRightMouseButton = false;
            }
            if (button == 3) {
                Nucleus n = cNucleiMgr.findClosestNucleus(e.getX(), e.getY(), iImagePlane + iPlaneInc, iImageTime + iTimeInc);
                if (n == null) return;
                Cell c = iAceTree.getCellByName(n.identity);
                iAceTree.setCurrentCell(c, iImageTime + iTimeInc, AceTree.RIGHTCLICKONIMAGE);
            } else if (button == 1) {
                addAnnotation(e.getX(), e.getY(), false);
                refreshDisplay(null);
            }
            iAceTree.cellAnnotated(getClickedCellName(e.getX(), e.getY()));
            if (cEditImage3 != null) cEditImage3.processEditMouseEvent(e);
        }
    }

    private String getClickedCellName(int x, int y) {
        int timeInc = 0;
        int planeInc = 0;
        if (iIsMainImgWindow) {
            timeInc = iAceTree.getTimeInc();
            planeInc = iAceTree.getPlaneInc();
        }
        String name = "";
        Nucleus n = cNucleiMgr.findClosestNucleus(x, y, iImageTime + iTimeInc);
        if (cNucleiMgr.hasCircle(n, (double) (iImagePlane + iPlaneInc))) {
            name = n.identity;
        }
        return name;
    }

    protected static final String RANDOMF = ".zip0", RANDOMT = ".tif0", DASHT = "-t", DASHP = "-p";

    public static void main(String[] args) {
        System.out.println("ImageWindow main");
        test3();
    }

    public static void test3() {
        FileInputStream fis;
        ImagePlus ip = null;
        String ss = "/nfs/waterston1/images/bao/081505/tif/081505_L1-t050-p15.tif";
        try {
            fis = new FileInputStream(ss);
            byte[] ba = readByteArray(fis);
            ip = openTiff(new ByteArrayInputStream(ba), false);
        } catch (IOException ioe) {
            System.out.println("ImageWindow.test3 exception ");
            System.out.println(ioe);
        }
        int width = ip.getWidth();
        int height = ip.getHeight();
        System.out.println("width, height: " + width + CS + height);
        ByteProcessor bproc = new ByteProcessor(width, height);
        ImagePlus ip2 = new ImagePlus("newtest3", bproc);
        new ImageWindow("test", ip2);
    }

    public static void test2() {
        ImageWindow.setStaticParameters("", "", 0);
        String s = "/home/biowolp/AncesTree/temp2/images/050405-t050-p15.tif";
        ImagePlus ip = ImageWindow.makeImage(s);
        System.out.println("handleImage: " + ip + CS + s);
        ij.gui.ImageWindow iImgWin = new ij.gui.ImageWindow(ip);
        ColorProcessor cproc = (ColorProcessor) ip.getProcessor();
        int[] pix = (int[]) cproc.getPixels();
        byte[] R = new byte[pix.length];
        byte[] G = new byte[pix.length];
        ;
        byte[] B = new byte[pix.length];
        ;
        cproc.getRGB(R, G, B);
        ByteProcessor bproc = new ByteProcessor(cproc.getWidth(), cproc.getHeight());
        bproc.setPixels(R);
        test(bproc);
        R = (byte[]) bproc.getPixels();
        cproc.setRGB(R, G, B);
        ImagePlus ip2 = new ImagePlus("newtest2", cproc);
        FileSaver fs = new FileSaver(ip2);
        fs.saveAsTiff();
    }

    public static void test(ImageProcessor ip) {
        byte[] pixels = (byte[]) ip.getPixels();
        int width = ip.getWidth();
        OvalRoi oRoi = new OvalRoi(560, 130, 55, 55);
        ip.setRoi(oRoi);
        Rectangle r = ip.getRoi();
        int offset, i;
        for (int y = r.y; y < (r.y + r.height); y++) {
            offset = y * width;
            for (int x = r.x; x < (r.x + r.width); x++) {
                i = offset + x;
                if (oRoi.contains(x, y)) {
                    pixels[i] = (byte) (128. * Math.random());
                }
            }
        }
    }

    public static final Integer ANTERIOR = new Integer(1), POSTERIOR = new Integer(2), NONE = new Integer(0);

    private static final String CS = ", ";

    private static final int DATA_BLOCK_SIZE = 2048;

    public static final int NCENTROID = 0, SCENTROID = 1, ANNOTATIONS = 2, UPPERSIS = 3, LOWERSIS = 4, LINEWIDTH = 5;

    public static final Color[] COLOR = { Color.RED, new Color(140, 70, 255), Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.PINK, Color.LIGHT_GRAY, Color.WHITE };

    public static final int[] WIDTHS = { 1, 2, 3 };

    private static void println(String s) {
        System.out.println(s);
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == iProperties) {
            new ImageParmsDialog(this);
        } else if (o == iHelp) {
            String item = "/org/rhwlab/help/html/ImageWindowToolbarHelp.html";
            new AceTreeHelp(item);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }
}
