package org.rhwlab.image;

import ij.process.ColorProcessor;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.Point;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.StringTokenizer;
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
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import net.sf.ij.jaiio.BufferedImageCreator;
import org.rhwlab.acetree.AceTree;
import org.rhwlab.acetree.NucUtils;
import org.rhwlab.snight.Movie;
import org.rhwlab.snight.NucleiMgr;
import org.rhwlab.snight.Nucleus;
import org.rhwlab.snight.Parameters;
import org.rhwlab.tree.Cell;
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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Image3D extends MouseAdapter implements ActionListener, Runnable {

    private PickCanvas iPickCanvas;

    private AceTree iAceTree;

    private NucleiMgr iNucleiMgr;

    public BranchGroup iBG;

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

    private Transform3D iTranslate = new Transform3D();

    protected Transform3D iCurrentTransform = new Transform3D();

    private TransformGroup iTranslateGroup;

    private Vector3d iTranslateVec = new Vector3d(0.0, 0.0, 0.0);

    private Matrix4d iMatrix = new Matrix4d();

    private double iZViewPos;

    private JPanel iImagePanel;

    private JPanel iControlPanel;

    private JTabbedPane iTabbedPane;

    public SublineageDisplayProperty[] iDispProps;

    private int iMinRed;

    private int iMaxRed;

    private boolean iUseExpression;

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
            Color3f c3f = new Color3f(color);
            Appearance app = setColor(c3f);
            return app;
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
                if (k < iDispProps.length - 2) app = getLineageColor(k); else {
                    int m = iDispProps[iDispProps.length - 2].iLineageNum;
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
                    app = getExpressionColor(n);
                    if (n.rweight < iMinRed) {
                        app = setColor(ColorConstants.white);
                        app.setTransparencyAttributes(tran);
                        iShowIt = true;
                    }
                } else if (iDispProps[iDispProps.length - 3].iName.indexOf("Special") == 0) {
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

    public class Axis {

        private BranchGroup axisBG;

        public Axis() {
            BranchGroup axisBG = iBG;
            LineArray axisXLines = new LineArray(2, LineArray.COORDINATES);
            axisBG.addChild(new Shape3D(axisXLines));
            axisXLines.setCoordinate(0, new Point3f(-1.0f, 0.0f, 0.0f));
            axisXLines.setCoordinate(1, new Point3f(1.0f, 0.0f, 0.0f));
            Color3f red = new Color3f(1.0f, 0.0f, 0.0f);
            Color3f green = new Color3f(0.0f, 1.0f, 0.0f);
            Color3f blue = new Color3f(0.0f, 0.0f, 1.0f);
            LineArray axisYLines = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
            axisBG.addChild(new Shape3D(axisYLines));
            axisYLines.setCoordinate(0, new Point3f(0.0f, -1.0f, 0.0f));
            axisYLines.setCoordinate(1, new Point3f(0.0f, 1.0f, 0.0f));
            axisYLines.setColor(0, green);
            axisYLines.setColor(1, blue);
            Point3f z1 = new Point3f(0.0f, 0.0f, -1.0f);
            Point3f z2 = new Point3f(0.0f, 0.0f, 1.0f);
            LineArray axisZLines = new LineArray(10, LineArray.COORDINATES | LineArray.COLOR_3);
            axisBG.addChild(new Shape3D(axisZLines));
            axisZLines.setCoordinate(0, z1);
            axisZLines.setCoordinate(1, z2);
            axisZLines.setCoordinate(2, z2);
            axisZLines.setCoordinate(3, new Point3f(0.1f, 0.1f, 0.9f));
            axisZLines.setCoordinate(4, z2);
            axisZLines.setCoordinate(5, new Point3f(-0.1f, 0.1f, 0.9f));
            axisZLines.setCoordinate(6, z2);
            axisZLines.setCoordinate(7, new Point3f(0.1f, -0.1f, 0.9f));
            axisZLines.setCoordinate(8, z2);
            axisZLines.setCoordinate(9, new Point3f(-0.1f, -0.1f, 0.9f));
            Color3f colors[] = new Color3f[9];
            colors[0] = new Color3f(0.0f, 1.0f, 1.0f);
            for (int v = 0; v < 9; v++) {
                colors[v] = red;
            }
            axisZLines.setColors(1, colors);
        }
    }

    public Image3D(AceTree aceTree, String title) {
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
        iTranslateGroup.getTransform(iTranslate);
        iTranslate.get(iMatrix);
        iZViewPos = iMatrix.m23;
        iTranslate.set(iMatrix);
        iTranslateGroup.setTransform(iTranslate);
        iPick = new JLabel("pick");
        iImagePanel = new JPanel();
        iImagePanel.setLayout(new BorderLayout());
        iImagePanel.add(makeToolBar(), "North");
        iImagePanel.add(iCanvas, "Center");
        iImagePanel.add(iPick, "South");
        iTabbedPane = new JTabbedPane();
        iTabbedPane.addTab("Image", null, iImagePanel, "View 3D image");
        Object dispProps = iAceTree.getDispProps3D();
        if (dispProps == null) iAceTree.setDispProps3D((Object) getDisplayProps());
        iDispProps = (SublineageDisplayProperty[]) iAceTree.getDispProps3D();
        PropertiesTab pt = new PropertiesTab(this);
        iControlPanel = pt.getPanel();
        iTabbedPane.addTab("Properties", null, iControlPanel, "Set color scheme");
        iFrame.getContentPane().add(iTabbedPane);
        iCanvas.addMouseListener(this);
        iFrame.pack();
        iFrame.show();
    }

    private void reportDispProps() {
        for (int i = 0; i < iDispProps.length; i++) {
            System.out.println("dispProp: " + i + CS + iDispProps[i].iName + CS + iDispProps[i].iLineageNum);
        }
    }

    public void updateDisplayedTab() {
        iTabbedPane.setSelectedIndex(0);
        insertContent(iTitle);
    }

    private JToolBar makeToolBar() {
        JToolBar jtb = new JToolBar("");
        jtb.setLayout(new GridLayout(1, 10));
        JLabel jl = new JLabel("angle");
        jtb.add(jl);
        iAngle = new JTextField("0", 4);
        jtb.add(iAngle);
        JButton set = new JButton("set");
        set.addActionListener(this);
        jtb.add(set);
        JButton jbr = new JButton("rotateX");
        jbr.addActionListener(this);
        jtb.add(jbr);
        JButton jb = new JButton("scale");
        jb.addActionListener(this);
        jtb.add(jb);
        iScale = new JTextField("1.0", 7);
        jtb.add(iScale);
        jb = new JButton("SetScale");
        jb.addActionListener(this);
        jtb.add(jb);
        jbr = new JButton("rotateY");
        jbr.addActionListener(this);
        jtb.add(jbr);
        return jtb;
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if (s.equals("set")) {
            Transform3D t3d = new Transform3D();
            t3d.rotX(Double.parseDouble(iAngle.getText()) * Math.PI / 180.);
            double news = Double.parseDouble(iScale.getText());
            t3d.setScale(news);
            iRotate.set(t3d);
            iRotGroup.setTransform(iRotate);
        } else if (s.equals("rotateX")) {
            Transform3D t3d = new Transform3D();
            t3d.rotX(Math.PI / 4.);
            iRotate.mul(t3d);
            iRotGroup.setTransform(iRotate);
            int angle = Integer.parseInt(iAngle.getText());
            angle += 45;
            angle = angle % 360;
            iAngle.setText(String.valueOf(angle));
        } else if (s.equals("rotateY")) {
            Transform3D t3d = new Transform3D();
            t3d.rotY(Math.PI / 2.);
            iRotate.mul(t3d);
            iRotGroup.setTransform(iRotate);
        } else if (s.equals("scale")) {
            Transform3D t3d = new Transform3D();
            t3d.set(1.1);
            iRotate.mul(t3d);
            iRotGroup.setTransform(iRotate);
            double sc = Double.parseDouble(iScale.getText());
            iScale.setText(String.valueOf(sc * 1.1));
        } else if (s.equals("SetScale")) {
            double news = Double.parseDouble(iScale.getText());
            iTranslateGroup.getTransform(iTranslate);
            iTranslate.get(iMatrix);
            iZViewPos = iMatrix.m23;
            iMatrix.m23 = news;
            iTranslate.set(iMatrix);
            iTranslateGroup.setTransform(iTranslate);
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
        Background bg = new Background(bgColor);
        bg.setApplicationBounds(bounds);
        root.addChild(bg);
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

    public class PropertiesTab implements ActionListener {

        JPanel iPanel;

        SublineageUI[] iSubUI;

        JTextField iMinRedField;

        JTextField iMaxRedField;

        JCheckBox iUseExprBox;

        Image3D iParent;

        public PropertiesTab(Image3D parent) {
            iParent = parent;
            Border blackline = BorderFactory.createLineBorder(Color.black);
            iSubUI = new SublineageUI[iDispProps.length];
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
            JLabel sublineage = new JLabel("sublineage");
            JLabel color = new JLabel("color");
            labelPanel.setLayout(new GridLayout(1, 2));
            labelPanel.add(sublineage);
            labelPanel.add(color);
            lineagePanel.add(labelPanel);
            for (int i = 0; i < iDispProps.length; i++) {
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
            botPart.add(jp);
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
                iDispProps = getDisplayProps();
                for (int i = 0; i < iDispProps.length; i++) {
                    iSubUI[i].iTF.setText(iDispProps[i].iName);
                    iSubUI[i].iCB.setSelectedIndex(iDispProps[i].iLineageNum);
                }
                iMinRed = 25000;
                iMaxRed = 100000;
                iUseExpression = false;
            } else if (command.equals("Apply")) {
                for (int i = 0; i < iDispProps.length; i++) {
                    String name = iSubUI[i].iTF.getText();
                    if (name.length() == 0) name = "-";
                    int num = iSubUI[i].iCB.getSelectedIndex();
                    iDispProps[i].iName = name;
                    iDispProps[i].iLineageNum = num;
                }
                iMinRed = Integer.parseInt(iMinRedField.getText());
                iMaxRed = Integer.parseInt(iMaxRedField.getText());
                iUseExpression = iUseExprBox.isSelected();
                iAceTree.setDispProps3D(iDispProps);
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
            JFileChooser fileChooser = new JFileChooser(".");
            int returnVal = fileChooser.showSaveDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File file = fileChooser.getSelectedFile();
            System.out.println("saveToFile: " + file);
            try {
                PrintStream ps = new PrintStream(new FileOutputStream(fileChooser.getSelectedFile()));
                for (int i = 0; i < iDispProps.length - 2; i++) {
                    ps.println(iDispProps[i].iName + CS + COLORS[iDispProps[i].iLineageNum]);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }

        private void loadFromFile() {
            JFileChooser fileChooser = new JFileChooser(".");
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File file = fileChooser.getSelectedFile();
            System.out.println("loadFromFile: " + file);
            int count = 0;
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String sr = br.readLine();
                while (sr != null && sr.length() > 2 && count < iDispProps.length - 2) {
                    String[] sa = sr.split(",");
                    sa[0] = sa[0].trim();
                    sa[1] = sa[1].trim();
                    System.out.println("loadFromFile: " + sa[0] + CS + sa[1]);
                    iDispProps[count].iName = sa[0];
                    iDispProps[count].iLineageNum = getColorNumber(sa[1]);
                    count++;
                    sr = br.readLine();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
            for (int i = count; i < iDispProps.length - 2; i++) {
                iDispProps[i].iName = "";
                iDispProps[i].iLineageNum = 0;
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
            for (int i = 0; i < iDispProps.length - 2; i++) {
                iSubUI[i].iTF.setText(iDispProps[i].iName);
                iSubUI[i].iCB.setSelectedIndex(iDispProps[i].iLineageNum);
            }
        }

        public class SublineageUI {

            public JPanel iPanel;

            public JTextField iTF;

            public JComboBox iCB;

            public SublineageUI(int i) {
                iPanel = new JPanel();
                iPanel.setLayout(new GridLayout(1, 2));
                iTF = new JTextField(iDispProps[i].iName, WIDTH);
                String[] list;
                list = COLORS;
                if (i == iDispProps.length - 2) list = TRANSPROPS; else if (i == iDispProps.length - 1) list = GRAYDEPTH;
                iCB = new JComboBox(list);
                iCB.setSelectedIndex(iDispProps[i].iLineageNum);
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
        int num = iDispProps.length;
        for (int i = 0; i < iDispProps.length; i++) {
            if (name.indexOf(iDispProps[i].iName) >= 0) {
                num = iDispProps[i].iLineageNum;
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
}
