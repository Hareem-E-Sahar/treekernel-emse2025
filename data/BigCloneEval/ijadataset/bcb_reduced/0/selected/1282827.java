package org.rhwlab.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.media.j3d.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GraphicsContext3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.rhwlab.acetree.AceTree;
import org.rhwlab.snight.Config;
import org.rhwlab.snight.NucleiMgr;
import org.rhwlab.snight.Nucleus;
import org.rhwlab.tree.Cell;
import org.rhwlab.tree.CellData;
import qdxml.DocHandler;
import qdxml.QDParser;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * @author biowolp
 *
 */
public class Image3D2 extends MouseAdapter implements ActionListener, Runnable, DocHandler {

    private PickCanvas iPickCanvas;

    private AceTree iAceTree;

    private NucleiMgr iNucleiMgr;

    public BranchGroup iBG;

    private Background iBackground;

    private JFrame iFrame;

    private SimpleUniverse iUniverse;

    private Canvas3D iCanvas;

    private String iTitle;

    boolean iNewConstruction;

    Thread iThread;

    boolean iSaveInProcess;

    static boolean iSaveImage;

    boolean fakeit1;

    boolean fakeit2;

    JTextField iAngle;

    JTextField iScale;

    JLabel iPick;

    private int iXA;

    private int iYA;

    private float iZA;

    private Transform3D iRotate;

    private TransformGroup iRotGroup;

    private TransformGroup iTranslateGroup;

    private Matrix4d iMatrix;

    private JPanel iImagePanel;

    private JPanel iControlPanel;

    private JTabbedPane iTabbedPane;

    public SublineageDisplayProperty[] iDispProps2;

    public PropertiesTab2 iPT2;

    private int iMinRed;

    private int iMaxRed;

    private boolean iUseExpression;

    private boolean iUseExpressionColors;

    private boolean iShowNonExpressing;

    JTextField iAngX;

    JTextField iAngXInc;

    JButton iXUp;

    JButton iXDn;

    JTextField iAngY;

    JTextField iAngYInc;

    JButton iYUp;

    JButton iYDn;

    JTextField iAngZ;

    JTextField iAngZInc;

    JButton iZUp;

    JButton iZDn;

    JTextField iPosIncr;

    JTextField iPos;

    JButton iPIn;

    JButton iPOut;

    JButton iRestore;

    JButton iUndoButton;

    Vector iUndo;

    JButton iLoadButton;

    JButton iSaveButton;

    String iCurrentRotDir;

    int iLineageCount;

    JButton iSaveImageButton;

    String iSaveImageAsDir;

    String iLastSaveAsName;

    BranchGroup iBGT;

    Indicator3D iIndicator;

    public class Nuclei3D {

        private boolean iShowIt;

        public Nuclei3D() {
            AceTree a = iAceTree;
            iBG = new BranchGroup();
            Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
            Color3f sColor = new Color3f(1.0f, 1.0f, 1.0f);
            Material m = new Material(eColor, eColor, sColor, sColor, 100.0f);
            m.setLightingEnable(true);
            Appearance app = new Appearance();
            app.setMaterial(m);
            addNuclei();
        }

        private Appearance setColor(Color3f color) {
            Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
            Color3f sColor = color;
            Material m = new Material(eColor, eColor, sColor, sColor, 100.0f);
            m.setLightingEnable(true);
            Appearance app = new Appearance();
            app.setMaterial(m);
            return app;
        }

        private Appearance getLineageColor(int k) {
            Appearance app = null;
            switch(k) {
                case 0:
                    app = setColor(ColorConstants.red);
                    break;
                case 1:
                    app = setColor(ColorConstants.blue);
                    break;
                case 2:
                    app = setColor(ColorConstants.green);
                    break;
                case 3:
                    app = setColor(ColorConstants.yellow);
                    break;
                case 4:
                    app = setColor(ColorConstants.cyan);
                    break;
                case 5:
                    app = setColor(ColorConstants.magenta);
                    break;
                case 6:
                    app = setColor(ColorConstants.pink);
                    break;
                case 7:
                    app = setColor(ColorConstants.gray);
                    break;
                case 8:
                    app = setColor(ColorConstants.white);
                    break;
                default:
                    app = null;
            }
            return app;
        }

        private Appearance getExpressionColor(Nucleus n) {
            Cell.setMinRed(iMinRed);
            Cell.setMaxRed(iMaxRed);
            int k = Cell.getDiscrete(n.rweight);
            Color color = Cell.getTheColor(k);
            color = getColor(n);
            Color3f c3f = new Color3f(color);
            Appearance app = setColor(c3f);
            return app;
        }

        private Color getColor(Nucleus n) {
            int rweight = n.rweight;
            int red = rweight;
            float frac = ((float) red - (float) Cell.cMin) / ((float) Cell.cMax - (float) Cell.cMin);
            frac = Math.min(frac, 1f);
            frac = Math.max(frac, 0f);
            float iHue = 0;
            float hue = iHue;
            float sat = 1;
            int col = Color.HSBtoRGB(hue, sat, frac);
            return new Color(col);
        }

        private Vector copyNuclei(Vector nuclei) {
            Vector newNuclei = new Vector();
            Enumeration e = nuclei.elements();
            Nucleus n = null;
            while (e.hasMoreElements()) {
                n = (Nucleus) e.nextElement();
                newNuclei.add(n.copy());
            }
            Collections.sort(newNuclei, n);
            return newNuclei;
        }

        private void addNuclei() {
            int count = 0;
            int falsePos = 0;
            int falseNeg = 0;
            NucleiMgr nucleiMgr = iAceTree.getNucleiMgr();
            int time = iAceTree.getImageTime() + iAceTree.getTimeInc();
            Vector nuclei = (Vector) nucleiMgr.getNucleiRecord().elementAt(time - 1);
            nuclei = copyNuclei(nuclei);
            getCenter(nuclei);
            Nucleus n = null;
            float xf, yf, z, rf;
            int width = ImageWindow.cImageWidth;
            int height = ImageWindow.cImageHeight;
            float scale = width / 2;
            float xoff = iXA;
            float yoff = iYA;
            float zoff = iZA;
            float nx, ny, nz, nr;
            for (int j = 0; j < nuclei.size(); j++) {
                n = (Nucleus) nuclei.elementAt(j);
                if (n.status < 0) continue;
                xf = (float) ((n.x - xoff) / scale);
                yf = (float) ((n.y - yoff) / scale);
                yf = -yf;
                z = (float) iNucleiMgr.getZPixRes() * (n.z - zoff) / scale;
                z = -z;
                rf = (float) ((n.size / 2) / scale);
                Appearance app = new Appearance();
                TransparencyAttributes tran = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.0f);
                TransparencyAttributes tran2 = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.5f);
                tran.setTransparency(0.8f);
                int k = getLineageNumber(n.identity);
                iShowIt = true;
                if (k < iDispProps2.length - 2) app = getLineageColor(k); else {
                    int m = iDispProps2[iDispProps2.length - 2].iLineageNum;
                    switch(m) {
                        case 0:
                            iShowIt = false;
                            break;
                        case 1:
                            app.setTransparencyAttributes(tran);
                            break;
                        default:
                            app = getLineageColor(8);
                    }
                }
                if (iUseExpression) {
                    if (iUseExpressionColors) app = getExpressionColor(n);
                    if (n.rweight < iMinRed) {
                        app = setColor(ColorConstants.white);
                        app.setTransparencyAttributes(tran);
                        iShowIt = iShowNonExpressing;
                    }
                } else if (iDispProps2[iDispProps2.length - 3].iName.indexOf("Special") == 0) {
                    app = special(n);
                }
                if (iShowIt && app != null) {
                    iBG.addChild(makeNamedSphere(n.identity, xf, yf, z, rf, app));
                    if (app.getTransparencyAttributes() != tran) count++;
                }
            }
        }

        private boolean inSCAList(Nucleus n) {
            String[] theList = { "ABaraaappaa", "ABalpaappa", "ABaraaappap", "ABaraaapaaa", "ABaraaappp", "MSaaaaaa", "ABalpaapppa", "ABprpapppp", "ABalpaapppp" };
            for (int i = 0; i < theList.length; i++) {
                if (n.identity.equals(theList[i])) return true;
            }
            return false;
        }

        private Appearance special(Nucleus n) {
            TransparencyAttributes faint = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.8f);
            TransparencyAttributes invisible = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.f);
            TransparencyAttributes solid = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.f);
            Appearance app = null;
            String name = n.identity;
            Appearance appRest = null;
            app = appRest;
            iShowIt = true;
            if (name.indexOf("E") == 0) {
                app = setColor(ColorConstants.yellow);
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("MSaa") == 0) {
                app = setColor(ColorConstants.magenta);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("MSaaaaaa") == 0 || name.indexOf("MSaappp") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("MSpa") == 0) {
                app = setColor(ColorConstants.cyan);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("MSpapp") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("ABalpaaa") == 0 || name.indexOf("ABalpaapa") == 0 || name.indexOf("ABalpapp") == 0) {
                app = setColor(ColorConstants.pink);
                ;
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("ABaraaaa") == 0 || name.indexOf("ABaraaapa") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("ABaraaapaaa") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("ABaraap") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("ABarapa") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("ABarapapapa") == 0) {
                    app = appRest;
                }
            }
            if (app == appRest) {
                iShowIt = true;
                app = setColor(ColorConstants.white);
                app.setTransparencyAttributes(faint);
            }
            return app;
        }

        private Appearance special(Nucleus n, boolean bogus) {
            TransparencyAttributes faint = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.8f);
            TransparencyAttributes invisible = new TransparencyAttributes(TransparencyAttributes.BLENDED, 1.f);
            TransparencyAttributes solid = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.f);
            Appearance app = null;
            String name = n.identity;
            Appearance appRest = null;
            app = appRest;
            iShowIt = true;
            if (name.indexOf("E") == 0) {
                app = setColor(ColorConstants.green);
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("MSaa") == 0) {
                app = setColor(ColorConstants.magenta);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("MSaaaaaa") == 0 || name.indexOf("MSaappp") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("MSpa") == 0) {
                app = setColor(ColorConstants.cyan);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("MSpapp") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("ABalpaaa") == 0 || name.indexOf("ABalpaapa") == 0 || name.indexOf("ABalpapp") == 0) {
                app = setColor(ColorConstants.red);
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("ABaraaaa") == 0 || name.indexOf("ABaraaapa") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("ABaraaapaaa") == 0) {
                    app = appRest;
                }
            }
            if (name.indexOf("ABaraap") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
            }
            if (name.indexOf("ABarapa") == 0) {
                app = setColor(ColorConstants.blue);
                app.setTransparencyAttributes(solid);
                if (name.indexOf("ABarapapapa") == 0) {
                    app = appRest;
                }
            }
            if (app == appRest) {
                iShowIt = true;
                app = setColor(ColorConstants.white);
                app.setTransparencyAttributes(faint);
            }
            return app;
        }

        private void getCenter(Vector nuclei) {
            iXA = 0;
            iYA = 0;
            iZA = 0.f;
            int count = 0;
            Enumeration e = nuclei.elements();
            while (e.hasMoreElements()) {
                Nucleus n = (Nucleus) e.nextElement();
                if (n.status == Nucleus.NILLI) continue;
                iXA += n.x;
                iYA += n.y;
                iZA += n.z;
                count++;
            }
            iXA /= count;
            iYA /= count;
            iZA /= count;
        }

        private TransformGroup makeNamedSphere(String name, float x, float y, float z, float r, Appearance a) {
            Transform3D translate = new Transform3D();
            translate.set(new Vector3f(x, y, z));
            NamedSphere sph = new NamedSphere(name, r, a);
            TransformGroup tg = new TransformGroup(translate);
            tg.addChild(sph);
            return tg;
        }

        private TransformGroup makeSphere(float x, float y, float z, float r, Appearance a) {
            Transform3D translate = new Transform3D();
            translate.set(new Vector3f(x, y, z));
            Sphere sph = new Sphere(r, a);
            TransformGroup tg = new TransformGroup(translate);
            tg.addChild(sph);
            return tg;
        }

        public BranchGroup getBG() {
            return iBG;
        }
    }

    public Image3D2(AceTree aceTree, String title) {
        iAceTree = aceTree;
        iNucleiMgr = iAceTree.getNucleiMgr();
        iFrame = new JFrame(title);
        iTitle = title;
        iNewConstruction = true;
        iMinRed = MINRED;
        iMaxRed = MAXRED;
        iUseExpression = false;
        iFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        WinEventMgr wem = new WinEventMgr();
        iFrame.addWindowListener(wem);
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        iCanvas = new Canvas3D(config);
        iCanvas.setSize(ImageWindow.cImageWidth, ImageWindow.cImageHeight);
        iUniverse = new SimpleUniverse(iCanvas);
        iUniverse.getViewingPlatform().setNominalViewingTransform();
        ViewingPlatform viewingPlatform = iUniverse.getViewingPlatform();
        iTranslateGroup = viewingPlatform.getViewPlatformTransform();
        iMatrix = new Matrix4d();
        Transform3D t3d = new Transform3D();
        iTranslateGroup.getTransform(t3d);
        Matrix4d m4d = new Matrix4d();
        buildOutUI();
        iTabbedPane = new JTabbedPane();
        iTabbedPane.addTab("Image", null, iImagePanel, "View 3D image");
        Object dispProps = iAceTree.getDispProps3D2();
        if (dispProps == null) iAceTree.setDispProps3D2((Object) getDisplayProps());
        iDispProps2 = (SublineageDisplayProperty[]) iAceTree.getDispProps3D2();
        iPT2 = new PropertiesTab2(this);
        iControlPanel = iPT2.getPanel();
        iTabbedPane.addTab("Properties", null, iControlPanel, "Set color scheme");
        iFrame.getContentPane().add(iTabbedPane);
        iCanvas.addMouseListener(this);
        iFrame.pack();
        iFrame.show();
        iUndo = new Vector();
        insertContent(iTitle);
        iSaveImageAsDir = "";
        iLastSaveAsName = "";
    }

    private void buildOutUI() {
        iPick = new JLabel("pick");
        iImagePanel = new JPanel();
        iImagePanel.setLayout(new BorderLayout());
        iImagePanel.add(iCanvas, "Center");
        JPanel secondPanel = new JPanel(new BorderLayout());
        JPanel newPanel = new JPanel();
        newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.PAGE_AXIS));
        JPanel rotatePanels = new JPanel();
        rotatePanels.setLayout(new GridLayout(0, 1));
        JPanel rotatePanel = new JPanel();
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        rotatePanel.add(iPick);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        rotatePanel.add(new JLabel("angX"));
        iAngXInc = new JTextField("30", 5);
        iAngX = new JTextField("0", 10);
        iXUp = new JButton("up");
        iXDn = new JButton("dn");
        rotatePanel.add(iAngXInc);
        rotatePanel.add(iAngX);
        rotatePanel.add(iXUp);
        rotatePanel.add(iXDn);
        iXUp.addActionListener(this);
        iXDn.addActionListener(this);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        rotatePanel.add(new JLabel("angY"));
        iAngYInc = new JTextField("30", 5);
        iAngY = new JTextField("0", 10);
        iYUp = new JButton("up");
        iYDn = new JButton("dn");
        rotatePanel.add(iAngYInc);
        rotatePanel.add(iAngY);
        rotatePanel.add(iYUp);
        rotatePanel.add(iYDn);
        iYUp.addActionListener(this);
        iYDn.addActionListener(this);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        rotatePanel.add(new JLabel("angZ"));
        iAngZInc = new JTextField("30", 5);
        iAngZ = new JTextField("0", 10);
        iZUp = new JButton("up");
        iZDn = new JButton("dn");
        rotatePanel.add(iAngZInc);
        rotatePanel.add(iAngZ);
        rotatePanel.add(iZUp);
        rotatePanel.add(iZDn);
        iZUp.addActionListener(this);
        iZDn.addActionListener(this);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        rotatePanel.add(new JLabel("Pos"));
        Transform3D t3d = new Transform3D();
        iTranslateGroup.getTransform(t3d);
        t3d.get(iMatrix);
        iPosIncr = new JTextField("0.2", 5);
        iPos = new JTextField(fmt1(iMatrix.m23), 10);
        iPIn = new JButton("in");
        iPOut = new JButton("out");
        rotatePanel.add(iPosIncr);
        rotatePanel.add(iPos);
        rotatePanel.add(iPIn);
        rotatePanel.add(iPOut);
        iPIn.addActionListener(this);
        iPOut.addActionListener(this);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        iRestore = new JButton("restore");
        rotatePanel.add(iRestore);
        iRestore.addActionListener(this);
        iUndoButton = new JButton("undo");
        iUndoButton.addActionListener(this);
        rotatePanel.add(iUndoButton);
        rotatePanels.add(rotatePanel);
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        iLoadButton = new JButton("load from file");
        rotatePanel.add(iLoadButton);
        iLoadButton.addActionListener(this);
        iSaveButton = new JButton("save to file");
        iSaveButton.addActionListener(this);
        rotatePanel.add(iSaveButton);
        rotatePanels.add(rotatePanel);
        iCurrentRotDir = ".";
        rotatePanel = new JPanel();
        rotatePanel.setLayout(new GridLayout(1, 0));
        iSaveImageButton = new JButton("saveImageAs");
        rotatePanel.add(iSaveImageButton);
        iSaveImageButton.addActionListener(this);
        rotatePanel.add(iSaveImageButton);
        rotatePanels.add(rotatePanel);
        newPanel.add(rotatePanels);
        secondPanel.add(newPanel, "West");
        iIndicator = new Indicator3D();
        secondPanel.add(iIndicator, "East");
        iImagePanel.add(secondPanel, "South");
    }

    private void reportDispProps() {
        for (int i = 0; i < iDispProps2.length; i++) {
            System.out.println("dispProp: " + i + CS + iDispProps2[i].iName + CS + iDispProps2[i].iLineageNum);
        }
    }

    public void updateDisplayedTab() {
        iTabbedPane.setSelectedIndex(0);
        insertContent(iTitle);
    }

    private void applyTrans(double incr, char axis) {
        int angle = 0;
        Transform3D t3d = new Transform3D();
        switch(axis) {
            case 'x':
                t3d.rotX(incr);
                iUndo.add(new Trans(t3d, incr, 'x'));
                angle = Integer.parseInt(iAngX.getText());
                angle += Math.round(Math.toDegrees(incr));
                angle = angle % 360;
                iAngX.setText(String.valueOf(angle));
                break;
            case 'y':
                t3d.rotY(incr);
                iUndo.add(new Trans(t3d, incr, 'y'));
                angle = Integer.parseInt(iAngY.getText());
                angle += Math.round(Math.toDegrees(incr));
                angle = angle % 360;
                iAngY.setText(String.valueOf(angle));
                break;
            case 'z':
                t3d.rotZ(incr);
                iUndo.add(new Trans(t3d, incr, 'z'));
                angle = Integer.parseInt(iAngZ.getText());
                angle += Math.round(Math.toDegrees(incr));
                angle = angle % 360;
                iAngZ.setText(String.valueOf(angle));
                break;
        }
        iRotate.mul(t3d);
        iIndicator.apply(t3d);
        iRotGroup.setTransform(iRotate);
    }

    private void handleRotatePanel(Object o) {
        int angle = 0;
        Transform3D t3d = new Transform3D();
        double incrDeg = 30;
        double incr = Math.toRadians(incrDeg);
        if (o == iXUp || o == iXDn) {
            incrDeg = Integer.parseInt(iAngXInc.getText());
            incr = Math.toRadians(incrDeg);
            if (o == iXDn) {
                incr *= -1;
            }
            applyTrans(incr, 'x');
            return;
        }
        if (o == iYUp || o == iYDn) {
            incrDeg = Integer.parseInt(iAngYInc.getText());
            incr = Math.toRadians(incrDeg);
            if (o == iYDn) {
                incr *= -1;
            }
            applyTrans(incr, 'y');
            return;
        }
        if (o == iZUp || o == iZDn) {
            incrDeg = Integer.parseInt(iAngZInc.getText());
            incr = Math.toRadians(incrDeg);
            if (o == iZDn) {
                incr *= -1;
            }
            applyTrans(incr, 'z');
            return;
        }
        if (o == iPIn || o == iPOut) {
            double pos = Double.parseDouble(iPos.getText());
            double posInc = Double.parseDouble(iPosIncr.getText());
            if (o == iPIn) posInc *= -1;
            pos += posInc;
            iPos.setText(fmt1(pos));
            iTranslateGroup.getTransform(t3d);
            Matrix4d m4d = new Matrix4d();
            t3d.get(m4d);
            Matrix4d mincr = new Matrix4d();
            mincr.m23 = posInc;
            m4d.add(mincr);
            t3d.set(m4d);
            iTranslateGroup.setTransform(t3d);
            t3d.set(mincr);
            iUndo.add(new Trans(new Transform3D(), posInc, 'p'));
        }
        if (o == iRestore) {
            iRotate.mulInverse(iRotate);
            iRotGroup.setTransform(iRotate);
            iUndo.clear();
            iIndicator.restore();
            iAngX.setText("0");
            iAngY.setText("0");
            iAngZ.setText("0");
            t3d.set(iMatrix);
            iTranslateGroup.setTransform(t3d);
            iPos.setText(fmt1(iMatrix.m23));
            return;
        }
        if (o == iUndoButton && iUndo.size() > 0) {
            Trans t = (Trans) iUndo.remove(iUndo.size() - 1);
            Transform3D t3 = t.iT3d;
            if (t3 != null) {
                double angInc = Math.toDegrees(t.iAngInc);
                switch(t.iAxis) {
                    case 'x':
                        angle = Integer.parseInt(iAngX.getText());
                        angle -= angInc;
                        iAngX.setText(String.valueOf(angle));
                        handleRotateUndo(t3);
                        break;
                    case 'y':
                        angle = Integer.parseInt(iAngY.getText());
                        angle -= angInc;
                        iAngY.setText(String.valueOf(angle));
                        handleRotateUndo(t3);
                        break;
                    case 'z':
                        angle = Integer.parseInt(iAngZ.getText());
                        angle -= angInc;
                        iAngZ.setText(String.valueOf(angle));
                        handleRotateUndo(t3);
                        break;
                    case 'p':
                        iTranslateGroup.getTransform(t3d);
                        Matrix4d m4d = new Matrix4d();
                        t3d.get(m4d);
                        m4d.m23 -= t.iAngInc;
                        t3d.set(m4d);
                        iTranslateGroup.setTransform(t3d);
                        double pos = Double.parseDouble(iPos.getText());
                        pos -= t.iAngInc;
                        iPos.setText(fmt1(pos));
                        break;
                }
            }
            iRotGroup.setTransform(iRotate);
        }
    }

    private void handleRotateUndo(Transform3D t3) {
        t3.invert();
        iRotate.mul(t3);
        iIndicator.apply(t3);
    }

    private void saveRotations() {
        File file = null;
        JFileChooser fileChooser = new JFileChooser(iCurrentRotDir);
        int returnVal = fileChooser.showSaveDialog(iAceTree);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            System.out.println("Save command cancelled by user.");
            return;
        }
        iCurrentRotDir = file.getParent();
        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            pw = new PrintWriter(fos, true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        pw.println("<?xml version='1.0' encoding='utf-8'?>");
        pw.println();
        pw.println("<rotations>");
        for (int i = 0; i < iUndo.size(); i++) {
            Trans t = (Trans) iUndo.get(i);
            StringBuffer sb = new StringBuffer();
            sb.append("<rotation ");
            sb.append("radians=\"" + t.iAngInc + "\" ");
            sb.append("axis=\"" + t.iAxis + "\"/>");
            pw.println(sb.toString());
        }
        pw.println("</rotations>");
    }

    private void loadRotations() {
        File file = null;
        JFileChooser fileChooser = new JFileChooser(iCurrentRotDir);
        int returnVal = fileChooser.showOpenDialog(iAceTree);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else {
            System.out.println("Save command cancelled by user.");
            return;
        }
        iCurrentRotDir = file.getParent();
        try {
            FileReader fr = new FileReader(file);
            QDParser.parse(this, fr);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startElement(String tag, Hashtable h) throws Exception {
        if (tag.equals("rotation")) {
            String incrs = (String) h.get("radians");
            String axiss = (String) h.get("axis");
            double incr = Double.parseDouble(incrs);
            char axis = axiss.charAt(0);
            applyTrans(incr, axis);
        } else if (tag.equals("lineage")) {
            String name = (String) h.get("name");
            String color = (String) h.get("color");
            iDispProps2[iLineageCount].iName = name;
            iDispProps2[iLineageCount].iLineageNum = iPT2.getColorNumber(color);
            iLineageCount++;
        }
    }

    private class Trans {

        public Transform3D iT3d;

        public double iAngInc;

        public char iAxis;

        public Trans(Transform3D t, double a, char axis) {
            iT3d = t;
            iAngInc = a;
            iAxis = axis;
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == iXUp || o == iXDn || o == iYUp || o == iYDn || o == iZUp || o == iZDn || o == iPIn || o == iPOut || o == iRestore || o == iUndoButton) {
            handleRotatePanel(o);
            return;
        }
        if (o == iLoadButton) {
            loadRotations();
        } else if (o == iSaveButton) {
            saveRotations();
        } else if (o == iSaveImageButton) {
            saveImageAs();
        }
    }

    public void insertContent(String title) {
        while (iSaveInProcess) ;
        iTitle = title;
        iFrame.setTitle(iTitle);
        if (iBG != null) iBG.detach();
        iBG = createSceneGraph();
        iUniverse.addBranchGraph(iBG);
        iPickCanvas = new PickCanvas(iCanvas, iBG);
        iPickCanvas.setMode(PickCanvas.BOUNDS);
        if (iSaveImage) {
            iThread = new Thread(this);
            iThread.start();
        }
    }

    public BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();
        root.setCapability(BranchGroup.ALLOW_DETACH);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        Color3f bgColor = new Color3f(0.3f, 0.3f, 0.3f);
        Color3f lColor1 = new Color3f(1f, 1f, 1f);
        Vector3d lPos1 = new Vector3d(0.0, 0.0, 2.0);
        Vector3f lDirect1 = new Vector3f(lPos1);
        lDirect1.negate();
        Light lgt1 = new DirectionalLight(lColor1, lDirect1);
        lgt1.setInfluencingBounds(bounds);
        root.addChild(lgt1);
        int m = iDispProps2[iDispProps2.length - 1].iLineageNum;
        switch(m) {
            case 0:
                bgColor = new Color3f(.7f, .7f, .7f);
                break;
            case 1:
                bgColor = new Color3f(.3f, .3f, .3f);
                break;
            default:
                bgColor = new Color3f(.1f, .1f, .1f);
                break;
        }
        iBackground = new Background(bgColor);
        iBackground.setApplicationBounds(bounds);
        root.addChild(iBackground);
        TransformGroup objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        new Nuclei3D();
        iBG.compile();
        objRotate.addChild(iBG);
        TransformGroup initRotGroup = new TransformGroup();
        Transform3D initRotate = new Transform3D();
        NucleiMgr nucMgr = iAceTree.getNucleiMgr();
        int ap = nucMgr.getParameters().apInit;
        int dv = nucMgr.getParameters().dvInit;
        int lr = nucMgr.getParameters().lrInit;
        if (ap == -1) {
            Transform3D apt = new Transform3D();
            apt.rotZ(Math.PI);
            initRotate.mul(apt);
            ap = -ap;
            dv = -dv;
        }
        if (dv == -1) {
            Transform3D dvt = new Transform3D();
            dvt.rotX(Math.PI);
            initRotate.mul(dvt);
            dv = -dv;
            lr = -lr;
        }
        initRotGroup.setTransform(initRotate);
        initRotGroup.addChild(objRotate);
        if (iRotate == null) iRotate = new Transform3D();
        iRotGroup = new TransformGroup(iRotate);
        iRotGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        iRotGroup.addChild(initRotGroup);
        root.addChild(iRotGroup);
        MouseRotate myMouseRotate = new MouseRotate();
        myMouseRotate.setTransformGroup(objRotate);
        myMouseRotate.setSchedulingBounds(new BoundingSphere());
        root.addChild(myMouseRotate);
        root.compile();
        return root;
    }

    public void mouseClicked(MouseEvent e) {
        iPickCanvas.setShapeLocation(e);
        PickResult[] results = iPickCanvas.pickAll();
        String name = getPickedNucleusNames(results);
        iPick.setText("you picked: " + name);
    }

    private String getPickedNucleusNames(PickResult[] results) {
        String s = "none";
        Vector v = new Vector();
        if (results != null) {
            for (int i = (results.length - 1); i >= 0; i--) {
                Primitive p = (Primitive) results[i].getNode(PickResult.PRIMITIVE);
                if (p != null) {
                    String pname = p.getClass().getName();
                    if (pname.indexOf("NamedSphere") >= 0) {
                        s = ((NamedSphere) p).iName;
                        v.add(0, s);
                    }
                }
            }
        }
        if (v.size() == 0) return "none";
        Enumeration e = v.elements();
        s = "";
        while (e.hasMoreElements()) {
            if (s.length() > 0) s += CS;
            s += (String) e.nextElement();
        }
        return s;
    }

    public class NamedSphere extends Sphere {

        String iName;

        public NamedSphere(String name, float r, Appearance a) {
            super(r, a);
            iName = name;
        }
    }

    public class SublineageDisplayProperty {

        public String iName;

        public int iLineageNum;

        public SublineageDisplayProperty(String name, int lineageNum) {
            iName = name;
            iLineageNum = lineageNum;
        }
    }

    public class PropertiesTab2 implements ActionListener {

        JPanel iPanel;

        SublineageUI[] iSubUI;

        JTextField iMinRedField;

        JTextField iMaxRedField;

        JCheckBox iUseExprBox;

        Image3D2 iParent;

        JRadioButton iUseExprColors;

        JCheckBox iShowNonExpressingChkBox;

        public PropertiesTab2(Image3D2 parent) {
            iParent = parent;
            Border blackline = BorderFactory.createLineBorder(Color.black);
            iSubUI = new SublineageUI[iDispProps2.length];
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
            JPanel[] testPanel = new JPanel[iDispProps2.length];
            JTextField textField;
            JComboBox cb;
            JPanel labelPanel = new JPanel();
            JLabel sublineage = new JLabel("sublineage");
            JLabel color = new JLabel("color");
            labelPanel.setLayout(new GridLayout(1, 2));
            labelPanel.add(sublineage);
            labelPanel.add(color);
            lineagePanel.add(labelPanel);
            for (int i = 0; i < iDispProps2.length; i++) {
                iSubUI[i] = new SublineageUI(i);
                lineagePanel.add(iSubUI[i].iPanel);
            }
            lineagePanel.setMaximumSize(new Dimension(200, 200));
            iPanel.add(topPart, BorderLayout.NORTH);
            JPanel botPart = new JPanel();
            botPart.setLayout(new GridLayout(3, 1));
            iPanel.add(botPart, BorderLayout.CENTER);
            JPanel filePanel = new JPanel();
            filePanel.setLayout(new GridLayout(1, 2));
            JButton load = new JButton("Load from file");
            JButton save = new JButton("Save to file");
            filePanel.add(load);
            filePanel.add(save);
            load.addActionListener(this);
            save.addActionListener(this);
            botPart.add(filePanel);
            JPanel jp = new JPanel(new FlowLayout());
            jp.setBorder(blackline);
            iMinRedField = new JTextField(String.valueOf(iMinRed), 7);
            iMaxRedField = new JTextField(String.valueOf(iMaxRed), 7);
            iUseExprBox = new JCheckBox("Use Expression", iUseExpression);
            jp.add(iUseExprBox);
            jp.add(new JLabel("minRed"));
            jp.add(iMinRedField);
            jp.add(new JLabel("maxRed"));
            jp.add(iMaxRedField);
            JPanel jp2 = new JPanel();
            jp2.add(jp);
            jp2.setBorder(blackline);
            jp = new JPanel(new FlowLayout());
            iUseExprColors = new JRadioButton("expression");
            iUseExprColors.setSelected(true);
            JRadioButton lineage = new JRadioButton("lineage");
            ButtonGroup bg = new ButtonGroup();
            bg.add(iUseExprColors);
            bg.add(lineage);
            jp.add(new JLabel("color via: "));
            jp.add(iUseExprColors);
            jp.add(lineage);
            jp.setBorder(blackline);
            jp2.add(jp);
            jp = new JPanel(new FlowLayout());
            iShowNonExpressingChkBox = new JCheckBox("Show non-expressing", iShowNonExpressing);
            jp.add(iShowNonExpressingChkBox);
            jp.setBorder(blackline);
            jp2.add(jp);
            botPart.add(jp2);
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
            botPart.add(buttonPanel);
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Cancel")) {
                updateDisplayedTab();
            } else if (command.equals("Reset")) {
                iDispProps2 = getDisplayProps();
                for (int i = 0; i < iDispProps2.length; i++) {
                    iSubUI[i].iTF.setText(iDispProps2[i].iName);
                    iSubUI[i].iCB.setSelectedIndex(iDispProps2[i].iLineageNum);
                }
                iMinRed = 25000;
                iMaxRed = 100000;
                iUseExpression = false;
            } else if (command.equals("Apply")) {
                for (int i = 0; i < iDispProps2.length; i++) {
                    String name = iSubUI[i].iTF.getText();
                    if (name.length() == 0) name = "-";
                    int num = iSubUI[i].iCB.getSelectedIndex();
                    iDispProps2[i].iName = name;
                    iDispProps2[i].iLineageNum = num;
                }
                iMinRed = Integer.parseInt(iMinRedField.getText());
                iMaxRed = Integer.parseInt(iMaxRedField.getText());
                iUseExpression = iUseExprBox.isSelected();
                iUseExpressionColors = iUseExprColors.isSelected();
                iShowNonExpressing = iShowNonExpressingChkBox.isSelected();
                iAceTree.setDispProps3D2(iDispProps2);
                updateDisplayedTab();
            } else if (command.equals("Load from file")) {
                System.out.println("Load from file");
                loadFromFile();
            } else if (command.equals("Save to file")) {
                System.out.println("Save to file");
                saveToFile();
            }
        }

        private void saveToFile() {
            JFileChooser fileChooser = new JFileChooser(iCurrentRotDir);
            int returnVal = fileChooser.showSaveDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File file = fileChooser.getSelectedFile();
            iCurrentRotDir = file.getParent();
            System.out.println("saveToFile: " + file);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(fileChooser.getSelectedFile()), true);
                pw.println("<?xml version='1.0' encoding='utf-8'?>");
                pw.println();
                pw.println("<lineages>");
                for (int i = 0; i < iDispProps2.length - 2; i++) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("<lineage ");
                    sb.append("name=\"" + iDispProps2[i].iName + "\" ");
                    sb.append("color=\"" + COLORS[iDispProps2[i].iLineageNum] + "\"/>");
                    pw.println(sb.toString());
                }
                pw.println("</lineages>");
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }

        private void loadFromFile() {
            JFileChooser fileChooser = new JFileChooser(iCurrentRotDir);
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File file = fileChooser.getSelectedFile();
            iCurrentRotDir = file.getParent();
            iLineageCount = 0;
            try {
                FileReader fr = new FileReader(file);
                QDParser.parse(iParent, fr);
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = iLineageCount; i < iDispProps2.length - 2; i++) {
                iDispProps2[i].iName = "";
                iDispProps2[i].iLineageNum = 0;
            }
            update();
        }

        public int getColorNumber(String colorName) {
            int k = 0;
            for (int i = 0; i < COLORS.length; i++) {
                if (colorName.equals(COLORS[i])) return i;
            }
            return k;
        }

        public void update() {
            for (int i = 0; i < iDispProps2.length - 2; i++) {
                iSubUI[i].iTF.setText(iDispProps2[i].iName);
                iSubUI[i].iCB.setSelectedIndex(iDispProps2[i].iLineageNum);
            }
        }

        public class SublineageUI {

            public JPanel iPanel;

            public JTextField iTF;

            public JComboBox iCB;

            public SublineageUI(int i) {
                iPanel = new JPanel();
                iPanel.setLayout(new GridLayout(1, 2));
                iTF = new JTextField(iDispProps2[i].iName, WIDTH);
                String[] list;
                list = COLORS;
                if (i == iDispProps2.length - 2) list = TRANSPROPS; else if (i == iDispProps2.length - 1) list = GRAYDEPTH;
                iCB = new JComboBox(list);
                iCB.setSelectedIndex(iDispProps2[i].iLineageNum);
                iPanel.add(iTF);
                iPanel.add(iCB);
                iPanel.setMaximumSize(new Dimension(200, 10));
            }
        }

        public JPanel getPanel() {
            return iPanel;
        }

        private String[] COLORS = { "red", "blue", "green", "yellow", "cyan", "magenta", "pink", "gray", "white", "omit" };

        private String[] TRANSPROPS = { "omit", "transparent", "white" };

        private String[] GRAYDEPTH = { "white", "light gray    ", "dark gray" };

        private static final int WIDTH = 15;
    }

    private SublineageDisplayProperty[] getDisplayProps() {
        SublineageDisplayProperty[] dispProps = { new SublineageDisplayProperty("ABa", 0), new SublineageDisplayProperty("ABp", 1), new SublineageDisplayProperty("C", 5), new SublineageDisplayProperty("D", 6), new SublineageDisplayProperty("E", 2), new SublineageDisplayProperty("MS", 4), new SublineageDisplayProperty("P", 3), new SublineageDisplayProperty("polar", 7), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("", 2), new SublineageDisplayProperty("other", 2), new SublineageDisplayProperty("background", 1) };
        return dispProps;
    }

    private int getLineageNumber(String name) {
        if (name.indexOf("Z") >= 0) name = "P";
        int num = iDispProps2.length;
        for (int i = 0; i < iDispProps2.length; i++) {
            if (name.indexOf(iDispProps2[i].iName) >= 0) {
                num = iDispProps2[i].iLineageNum;
                break;
            }
        }
        return num;
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

    public static void setSaveImageState(boolean saveIt) {
        iSaveImage = saveIt;
    }

    public void saveImage() {
        String saveDir = iAceTree.iImgWin.getSaveImageDirectory();
        if (saveDir == null) {
            iAceTree.iImgWin.cancelSaveOperations();
            iSaveImage = false;
            return;
        }
        Rectangle screenRect = iFrame.getBounds();
        int topAdjust = 23;
        int y = screenRect.y;
        screenRect.y += topAdjust;
        int height = screenRect.height;
        screenRect.height -= topAdjust;
        String title = saveDir + "/";
        Robot robot = null;
        try {
            robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            title += iTitle + "." + IMAGETYPE;
            ImageIO.write(image, IMAGETYPE, new File(title));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("file: " + title + " written");
        iSaveInProcess = false;
    }

    public void saveImageAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setPreferredSize(new Dimension(400, 300));
        fileChooser.setCurrentDirectory(new File(iSaveImageAsDir));
        fileChooser.setSelectedFile(new File(iLastSaveAsName));
        String path = "";
        int returnVal = fileChooser.showSaveDialog(iAceTree);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            path = file.getPath();
            iSaveImageAsDir = file.getParent();
            iLastSaveAsName = file.getName() + "x";
        } else {
            System.out.println("Save command cancelled by user.");
        }
        Rectangle screenRect = iFrame.getBounds();
        int topAdjust = 23;
        int y = screenRect.y;
        screenRect.y += topAdjust;
        int height = screenRect.height;
        screenRect.height -= topAdjust;
        Robot robot = null;
        try {
            robot = new Robot();
            BufferedImage image = robot.createScreenCapture(screenRect);
            ImageIO.write(image, IMAGETYPE, new File(path + "." + IMAGETYPE));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("file: " + path + " written");
        iSaveInProcess = false;
    }

    public BufferedImage getBufferedImage() {
        int width = (int) (iCanvas.getSize().getWidth());
        int height = (int) (iCanvas.getSize().getHeight());
        int cursorType = iCanvas.getCursor().getType();
        iCanvas.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        GraphicsContext3D ctx = iCanvas.getGraphicsContext3D();
        Raster ras = new Raster(new Point3f(-1f, -1f, -1f), Raster.RASTER_COLOR, 0, 0, width, height, new ImageComponent2D(ImageComponent.FORMAT_RGB, new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)), null);
        ctx.readRaster(ras);
        BufferedImage img = ras.getImage().getImage();
        iCanvas.setCursor(new Cursor(cursorType));
        return img;
    }

    private class WinEventMgr extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            iFrame.dispose();
            iAceTree.image3DOff();
        }
    }

    private static final String CS = ", ", IMAGETYPE = "jpeg";

    private Color3f[] BACKGROUNDS = { new Color3f(1.f, 1.f, 1.f), new Color3f(0.3f, 0.3f, 0.3f), new Color3f(0.1f, 0.1f, 0.1f) };

    private static final int MINRED = 25000, MAXRED = 100000, SPECIAL = 1;

    public static void main(String[] args) {
    }

    private static void println(String s) {
        System.out.println(s);
    }

    private static final DecimalFormat DF1 = new DecimalFormat("####.##");

    private static final DecimalFormat DF4 = new DecimalFormat("####.####");

    private static String fmt1(double x) {
        return DF1.format(x);
    }

    public void startDocument() throws Exception {
    }

    public void endDocument() throws Exception {
    }

    public void text(String str) throws Exception {
    }

    public void endElement(String tag) throws Exception {
    }
}
