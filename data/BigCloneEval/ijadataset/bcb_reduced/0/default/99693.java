import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.media.j3d.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.vecmath.*;
import com.sun.image.codec.jpeg.*;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.io.*;
import java.util.Enumeration;

/** Tegner opp  canvas, sylindre og håndterer alt av 
 * 	MouseBehaviors og skygge
 * 
 * 	 @author Alexander Vrtis, Andreas Leknes, Steffan Sørenes
 */
public class DNACanvas_3 extends Applet {

    private PolygonAttributes pa = new PolygonAttributes();

    private boolean setWireframe = false;

    private JTextArea status;

    private Appearance sap;

    private PolygonAttributes polyAttr;

    private TransformGroup tg;

    private TransformGroup helixtg;

    private PointLight ptlight;

    private boolean paused = false;

    private Helix helix;

    private Canvas3D cv;

    private Point3f light1Pos = new Point3f(0.0f, 0.0f, 3.5f);

    private Shape3D shadowS;

    private String currentWord = "atcgatagc";

    private int idx = 0;

    private Transform3D tr3d = null;

    private ShadowUpdater updater = null;

    public static void main(String[] args) {
        new MainFrame(new DNACanvas_3(), 640, 480);
    }

    /** Oppdaterer skyggen til molekylet
	 * 
	 */
    private void refreshShadow() {
        paused = true;
        status.append(" Fargekombinasjon: " + currentWord);
        shadowS.removeAllGeometries();
        ptlight.setPosition(light1Pos);
        tegnDNA();
        updateShadow();
        paused = false;
    }

    public void init() {
        GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
        cv = new Canvas3D(gc);
        setLayout(new BorderLayout());
        add(cv, BorderLayout.CENTER);
        updater = new DNACanvas_3.ShadowUpdater();
        TextArea text = new TextArea();
        text.setMaximumSize(new Dimension(100, 20));
        Button setBand = new Button("Sett baseparfarge");
        setBand.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                currentWord = JOptionPane.showInputDialog(null, "mm");
                refreshShadow();
            }
        });
        Button scrShot = new Button("Ta skjermdump");
        scrShot.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                takeScreenShot();
                status.setText("");
                status.append("Screenshot taken");
            }
        });
        Button wf = new Button("Toggle Wireframe");
        wf.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (setWireframe == false) {
                    pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
                    polyAttr.setPolygonMode(PolygonAttributes.POLYGON_LINE);
                    pa.setCullFace(PolygonAttributes.CULL_BACK);
                    pa.setBackFaceNormalFlip(true);
                    setWireframe = true;
                    status.setText("");
                    status.append("Wireframe Mode On");
                } else {
                    pa.setCullFace(PolygonAttributes.CULL_BACK);
                    pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
                    polyAttr.setPolygonMode(PolygonAttributes.POLYGON_FILL);
                    setWireframe = false;
                    status.setText("");
                    status.append("Texture Mode On");
                }
            }
        });
        Panel buttonframe = new Panel();
        buttonframe.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        buttonframe.add(setBand, gbc);
        gbc.gridx = 1;
        buttonframe.add(scrShot, gbc);
        gbc.gridx = 2;
        buttonframe.add(wf, gbc);
        add(buttonframe, BorderLayout.NORTH);
        status = new JTextArea("Texture Mode On");
        status.setEditable(false);
        status.setBackground(Color.BLACK);
        status.setForeground(Color.WHITE);
        add(status, BorderLayout.SOUTH);
        VirtualUniverse vu = new VirtualUniverse();
        Locale loc = new Locale(vu);
        BranchGroup bgView = createViewBranch(cv);
        bgView.compile();
        loc.addBranchGraph(bgView);
        BranchGroup bg = createContentBranch();
        loc.addBranchGraph(bg);
        SettingsFrame sf = new SettingsFrame();
    }

    protected void takeScreenShot() {
        try {
            Robot robot = new Robot();
            Rectangle theRectOnScreen = new Rectangle(cv.getLocationOnScreen().x, cv.getLocationOnScreen().y, cv.getWidth(), cv.getHeight());
            BufferedImage bi = robot.createScreenCapture(theRectOnScreen);
            File f = new File(".\\screenshot.jpg");
            if (f.exists()) {
                f.delete();
            }
            try {
                if (f.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(f);
                    JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(fos);
                    encoder.encode(bi);
                    fos.flush();
                    fos.close();
                }
            } catch (IOException ioex) {
                status.setText("");
                status.append("Error writing JPEG");
            }
        } catch (AWTException aex) {
            status.setText("");
            status.append("Error getting Screen content");
        }
    }

    private BranchGroup createViewBranch(Canvas3D canvas) {
        View view = new View();
        view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
        ViewPlatform vp = new ViewPlatform();
        view.addCanvas3D(canvas);
        view.attachViewPlatform(vp);
        view.setPhysicalBody(new PhysicalBody());
        view.setPhysicalEnvironment(new PhysicalEnvironment());
        Transform3D trans = new Transform3D();
        Point3d eye = new Point3d(0, 0, 12);
        Point3d center = new Point3d(0, 0, 0);
        Vector3d vup = new Vector3d(0, 1, 0);
        trans.lookAt(eye, center, vup);
        trans.invert();
        TransformGroup tg = new TransformGroup(trans);
        tg.addChild(vp);
        BranchGroup bgView = new BranchGroup();
        bgView.addChild(tg);
        return bgView;
    }

    protected void tegnDNA() {
        String basepar = currentWord;
        basepar = "e" + basepar;
        StringBuilder basepar2 = new StringBuilder();
        for (int i = 0; i < basepar.length(); i++) {
            if (basepar.charAt(i) == 'a' || basepar.charAt(i) == 'A') basepar2.append("t"); else if (basepar.charAt(i) == 't' || basepar.charAt(i) == 'T') basepar2.append("a"); else if (basepar.charAt(i) == 'c' || basepar.charAt(i) == 'C') basepar2.append("g"); else if (basepar.charAt(i) == 'g' || basepar.charAt(i) == 'G') basepar2.append("c"); else basepar2.append(" ");
        }
        Color3f[] cc1 = new Color3f[basepar.length()];
        Color3f[] cc2 = new Color3f[basepar2.length()];
        for (int i = 0; i < cc1.length; i++) {
            cc1[i] = getColor(basepar.charAt(i));
            cc2[i] = getColor(basepar2.charAt(i));
        }
        try {
            helix.createGeometry(cc1, cc2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BranchGroup createContentBranch() {
        BranchGroup root = new BranchGroup();
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), 10000.0d);
        Appearance ap = new Appearance();
        pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        pa.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
        pa.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_READ);
        pa.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
        pa.setCullFace(PolygonAttributes.CULL_BACK);
        pa.setBackFaceNormalFlip(true);
        ap.setPolygonAttributes(pa);
        ap.setMaterial(new Material());
        tg = new TransformGroup();
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        helixtg = new TransformGroup();
        helixtg.setBounds(bounds);
        helixtg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        helixtg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        helix = new Helix();
        helix.setBounds(bounds);
        helix.setAppearance(ap);
        helix.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        Alpha alpha = new Alpha(-1, 10000);
        RotationInterpolator rotator = new RotationInterpolator(alpha, helixtg);
        rotator.setSchedulingBounds(bounds);
        MouseRotate mrotate = new MouseRotate(tg);
        mrotate.setSchedulingBounds(bounds);
        MouseTranslate mtranslator = new MouseTranslate(tg);
        mtranslator.setSchedulingBounds(bounds);
        MouseZoom mzoom = new MouseZoom(tg);
        mzoom.setSchedulingBounds(bounds);
        Background background = new Background(new Color3f(Color.white));
        background.setApplicationBounds(bounds);
        AmbientLight alight = new AmbientLight(true, new Color3f(Color.DARK_GRAY));
        alight.setInfluencingBounds(bounds);
        ptlight = new PointLight(new Color3f(Color.LIGHT_GRAY), light1Pos, new Point3f(1f, 0.2f, 0f));
        ptlight.setInfluencingBounds(bounds);
        ptlight.setCapability(PointLight.ALLOW_POSITION_WRITE);
        HelixContainer hc = new HelixContainer(5.0f);
        hc.setAppearance(ap);
        tegnDNA();
        updateShadow();
        ShadowBehavior sb = new DNACanvas_3.ShadowBehavior();
        sb.setSchedulingBounds(bounds);
        root.addChild(tg);
        root.addChild(background);
        root.addChild(alight);
        helixtg.addChild(rotator);
        helixtg.addChild(helix);
        tg.addChild(mrotate);
        tg.addChild(hc);
        tg.addChild(helixtg);
        tg.addChild(ptlight);
        tg.addChild(mtranslator);
        tg.addChild(mzoom);
        tg.addChild(sb);
        root.compile();
        return root;
    }

    private void updateShadow() {
        if (shadowS == null) {
            shadowS = new Shape3D();
            shadowS.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
            tg.addChild(shadowS);
            sap = new Appearance();
            sap.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
            ColoringAttributes colorAttr = new ColoringAttributes(0.1f, 0.1f, 0.1f, ColoringAttributes.FASTEST);
            sap.setColoringAttributes(colorAttr);
            TransparencyAttributes transAttr = new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.0f);
            sap.setTransparencyAttributes(transAttr);
            polyAttr = new PolygonAttributes();
            polyAttr.setCullFace(PolygonAttributes.CULL_NONE);
            polyAttr.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
            sap.setPolygonAttributes(polyAttr);
            shadowS.setAppearance(sap);
        }
        for (Enumeration en = helix.getAllGeometries(); en.hasMoreElements(); ) {
            GeometryArray shadow = createShadow(((GeometryArray) en.nextElement()), light1Pos, new Point3f(0.0f, 0.0f, -4.99f));
            shadowS.addGeometry(shadow);
        }
    }

    /** Setter opp fargene til sylindrene
	 * 
	 * @param chr - Navnet på baseparet
	 * @return
	 */
    private Color3f getColor(char chr) {
        Color3f ret;
        if (chr == 'a' || chr == 'A') ret = new Color3f(0.0f, 1.0f, 0.0f); else if (chr == 'c' || chr == 'C') ret = new Color3f(0.0f, 0.0f, 1.0f); else if (chr == 'g' || chr == 'G') ret = new Color3f(1.0f, 1.0f, 0.0f); else if (chr == 't' || chr == 'T') ret = new Color3f(1.0f, 0.0f, 0.0f); else ret = new Color3f(0.0f, 0.0f, 0.0f);
        return ret;
    }

    private GeometryArray createShadow(GeometryArray ga, Point3f light, Point3f plane) {
        GeometryInfo gi = new GeometryInfo(ga);
        gi.convertToIndexedTriangles();
        IndexedTriangleArray ita = (IndexedTriangleArray) gi.getIndexedGeometryArray();
        Vector3f v = new Vector3f();
        v.sub(plane, light);
        double[] mat = new double[16];
        for (int i = 0; i < 16; i++) {
            mat[i] = 0;
        }
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 1 - 0.001;
        mat[14] = -1 / v.length();
        Transform3D proj = new Transform3D();
        proj.set(mat);
        Transform3D u = new Transform3D();
        u.lookAt(new Point3d(light), new Point3d(plane), new Vector3d(0, 1, 0));
        proj.mul(u);
        tr3d = new Transform3D();
        u.invert();
        tr3d.mul(u, proj);
        int n = ita.getVertexCount();
        int count = ita.getIndexCount();
        IndexedTriangleArray shadow = new IndexedTriangleArray(n, GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE, count);
        shadow.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
        shadow.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
        double[] vert = new double[3 * n];
        Point3d p = new Point3d();
        for (int i = 0; i < n; i++) {
            ga.getCoordinate(i, p);
            Vector4d v4 = new Vector4d(p);
            v4.w = 1;
            tr3d.transform(v4);
            Point4d p4 = new Point4d(v4);
            p.project(p4);
            vert[3 * i] = p.x;
            vert[3 * i + 1] = p.y;
            vert[3 * i + 2] = p.z;
        }
        shadow.setCoordRefDouble(vert);
        int[] indices = new int[count];
        ita.getCoordinateIndices(0, indices);
        shadow.setCoordinateIndices(0, indices);
        return shadow;
    }

    class ShadowUpdater implements GeometryUpdater {

        public void updateData(Geometry geometry) {
            try {
                double[] vert = ((GeometryArray) geometry).getCoordRefDouble();
                int n = vert.length / 3;
                Transform3D rot = new Transform3D();
                helixtg.getTransform(rot);
                Transform3D tr = new Transform3D(tr3d);
                tr.mul(rot);
                Point3d p = new Point3d();
                for (int i = 0; i < n; i++) {
                    ((GeometryArray) helix.getGeometry(idx)).getCoordinate(i, p);
                    Vector4d v4 = new Vector4d(p);
                    v4.w = 1;
                    tr.transform(v4);
                    Point4d p4 = new Point4d(v4);
                    p.project(p4);
                    vert[3 * i] = p.x;
                    vert[3 * i + 1] = p.y;
                    vert[3 * i + 2] = p.z;
                }
                idx++;
            } catch (Exception e) {
            }
        }
    }

    class ShadowBehavior extends Behavior {

        WakeupOnElapsedFrames wakeup = null;

        public ShadowBehavior() {
            wakeup = new WakeupOnElapsedFrames(1);
        }

        public void initialize() {
            wakeupOn(wakeup);
        }

        public void processStimulus(java.util.Enumeration enumeration) {
            idx = 0;
            while (paused) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (Enumeration asd = shadowS.getAllGeometries(); asd.hasMoreElements(); ) {
                Object o = asd.nextElement();
                if (o == null) {
                } else {
                    GeometryArray ga = (GeometryArray) o;
                    ga.updateData(updater);
                }
            }
            wakeupOn(wakeup);
        }
    }

    /** Oppdaterer skyggen iforhold til lysoppsett
	 * 
	 * @param value X-koordinat for lyset
	 * @param value2 y-koordinat for lyset
	 * @param value3 z-koordinat for lyset
	 */
    private void setLightPosition(float value, float value2, float value3) {
        light1Pos.x = value;
        light1Pos.y = value2;
        light1Pos.z = value3;
        status.setText("");
        status.append("Lyskoordinater: " + light1Pos.toString());
        refreshShadow();
    }

    /**	Egen frame for å stille på skyggen
	 * 
	 */
    private class SettingsFrame extends JFrame {

        JSlider slider1;

        JSlider slider2;

        JSlider slider3;

        public SettingsFrame() {
            super("Skyggeinnstillinger:");
            this.setSize(200, 200);
            this.setVisible(true);
            LayoutManager layout = new GridBagLayout();
            this.setLayout(layout);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            slider1 = new JSlider(-50, 50, 0);
            slider2 = new JSlider(-50, 50, 0);
            slider3 = new JSlider(-50, 50, 0);
            ChangeListener listener = new ChangeListener() {

                public void stateChanged(ChangeEvent arg0) {
                    setLightPosition((float) slider1.getValue() / 10.0f, (float) slider2.getValue() / 10.0f, (float) slider3.getValue() / 10.0f);
                }
            };
            slider1.addChangeListener(listener);
            slider2.addChangeListener(listener);
            slider3.addChangeListener(listener);
            JLabel lbl = new JLabel("X Koordinat:");
            this.add(lbl, gbc);
            gbc.gridy = 2;
            lbl = new JLabel("Y Koordinat:");
            this.add(lbl, gbc);
            gbc.gridy = 4;
            lbl = new JLabel("Z Koordinat:");
            this.add(lbl, gbc);
            gbc.gridwidth = 3;
            gbc.gridx = 1;
            gbc.gridy = 0;
            this.add(slider1, gbc);
            gbc.gridy = 2;
            this.add(slider2, gbc);
            gbc.gridy = 4;
            this.add(slider3, gbc);
            this.pack();
        }
    }
}
