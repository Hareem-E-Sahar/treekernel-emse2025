package com.eteks.sweethome3d.swing;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import javax.imageio.ImageIO;
import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Light;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PhysicalBody;
import javax.media.j3d.PhysicalEnvironment;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import com.eteks.sweethome3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Super class of 3D preview component for model. 
 */
public class ModelPreviewComponent extends JComponent {

    private SimpleUniverse universe;

    private Canvas3D canvas3D;

    private BranchGroup sceneTree;

    private float viewYaw = (float) Math.PI / 8;

    private float viewPitch = -(float) Math.PI / 16;

    private float viewScale = 1;

    private Object iconImageLock;

    /**
   * Returns an 3D model preview component.
   */
    public ModelPreviewComponent() {
        this(false);
    }

    /**
   * Returns an 3D model preview component that lets the user change its pitch and scale 
   * if <code>pitchAndScaleChangeSupported</code> is <code>true</code>.
   */
    public ModelPreviewComponent(boolean pitchAndScaleChangeSupported) {
        this.canvas3D = Component3DManager.getInstance().getOnscreenCanvas3D(new Component3DManager.RenderingObserver() {

            public void canvas3DPreRendered(Canvas3D canvas3d) {
            }

            public void canvas3DPostRendered(Canvas3D canvas3d) {
            }

            public void canvas3DSwapped(Canvas3D canvas3d) {
                ModelPreviewComponent.this.canvas3DSwapped();
            }
        });
        setLayout(new GridLayout(1, 1));
        add(this.canvas3D);
        this.canvas3D.setFocusable(false);
        addMouseListeners(this.canvas3D, pitchAndScaleChangeSupported);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        this.sceneTree = createSceneTree();
        addAncestorListener();
    }

    /**
   * Returns component preferred size.
   */
    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        } else {
            return new Dimension(200, 200);
        }
    }

    /**
   * Returns the canvas 3D displayed by this component.
   */
    protected Canvas3D getCanvas3D() {
        return this.canvas3D;
    }

    /**
   * Adds an AWT mouse listener to canvas that will update view platform transform.  
   */
    private void addMouseListeners(Canvas3D canvas3D, final boolean pitchAndScaleChangeSupported) {
        final float ANGLE_FACTOR = 0.02f;
        final float ZOOM_FACTOR = 0.02f;
        MouseInputAdapter mouseListener = new MouseInputAdapter() {

            private int xLastMouseMove;

            private int yLastMouseMove;

            @Override
            public void mousePressed(MouseEvent ev) {
                this.xLastMouseMove = ev.getX();
                this.yLastMouseMove = ev.getY();
            }

            @Override
            public void mouseDragged(MouseEvent ev) {
                if (getModel() != null) {
                    setViewYaw(getViewYaw() - ANGLE_FACTOR * (ev.getX() - this.xLastMouseMove));
                    this.xLastMouseMove = ev.getX();
                    if (pitchAndScaleChangeSupported) {
                        if (ev.isAltDown()) {
                            setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float) Math.exp((ev.getY() - this.yLastMouseMove) * ZOOM_FACTOR))));
                        } else {
                            setViewPitch(Math.max(-(float) Math.PI / 4, Math.min(0, getViewPitch() - ANGLE_FACTOR * (ev.getY() - this.yLastMouseMove))));
                        }
                        this.yLastMouseMove = ev.getY();
                    }
                }
            }
        };
        canvas3D.addMouseListener(mouseListener);
        canvas3D.addMouseMotionListener(mouseListener);
        if (pitchAndScaleChangeSupported) {
            canvas3D.addMouseWheelListener(new MouseWheelListener() {

                public void mouseWheelMoved(MouseWheelEvent ev) {
                    setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float) Math.exp(ev.getWheelRotation() * ZOOM_FACTOR))));
                }
            });
        }
    }

    /**
   * Adds an ancestor listener to this component to manage canvas universe 
   * creation and clean up.  
   */
    private void addAncestorListener() {
        addAncestorListener(new AncestorListener() {

            public void ancestorAdded(AncestorEvent event) {
                if (universe == null) {
                    createUniverse();
                }
            }

            public void ancestorRemoved(AncestorEvent event) {
                if (universe != null) {
                    disposeUniverse();
                }
            }

            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    /**
   * Creates universe bound to canvas.
   */
    private void createUniverse() {
        this.universe = new SimpleUniverse(this.canvas3D);
        this.canvas3D.setFocusable(false);
        updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), getViewYaw(), getViewPitch(), getViewScale());
        this.universe.addBranchGraph(this.sceneTree);
        if (OperatingSystem.isMacOSX()) {
            final Component root = SwingUtilities.getRoot(this);
            EventQueue.invokeLater(new Runnable() {

                public void run() {
                    Dimension rootSize = root.getSize();
                    root.setSize(new Dimension(rootSize.width + 1, rootSize.height));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                    root.setSize(new Dimension(rootSize.width, rootSize.height));
                    if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != root) {
                        root.requestFocus();
                    }
                }
            });
        }
    }

    /**
   * Disposes universe bound to canvas.
   */
    private void disposeUniverse() {
        this.universe.getLocale().removeBranchGraph(this.sceneTree);
        this.universe.cleanup();
        this.universe = null;
    }

    /**
   * Creates a view bound to the universe that views current model from a point of view oriented with 
   * <code>yaw</code> and <code>pitch</code> angles.
   */
    protected View createView(float yaw, float pitch, float scale, int projectionPolicy) {
        if (this.universe == null) {
            createUniverse();
        }
        PhysicalBody physicalBody = this.universe.getViewer().getPhysicalBody();
        PhysicalEnvironment physicalEnvironment = this.universe.getViewer().getPhysicalEnvironment();
        View view = new View();
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);
        view.setProjectionPolicy(projectionPolicy);
        ViewingPlatform viewingPlatform = new ViewingPlatform();
        viewingPlatform.setUniverse(this.universe);
        this.universe.getLocale().addBranchGraph((BranchGroup) viewingPlatform.getViewPlatformTransform().getParent());
        view.attachViewPlatform(viewingPlatform.getViewPlatform());
        updateViewPlatformTransform(viewingPlatform.getViewPlatformTransform(), yaw, pitch, scale);
        return view;
    }

    /**
   * Returns the <code>yaw</code> angle used by view platform transform.
   */
    protected float getViewYaw() {
        return this.viewYaw;
    }

    /**
   * Sets the <code>yaw</code> angle used by view platform transform.
   */
    protected void setViewYaw(float viewYaw) {
        this.viewYaw = viewYaw;
        if (this.universe != null) {
            updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), getViewYaw(), getViewPitch(), getViewScale());
        }
    }

    /**
   * Returns the zoom factor used by view platform transform.
   */
    protected float getViewScale() {
        return this.viewScale;
    }

    /**
   * Sets the zoom factor used by view platform transform.
   */
    protected void setViewScale(float viewScale) {
        this.viewScale = viewScale;
        if (this.universe != null) {
            updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), getViewYaw(), getViewPitch(), getViewScale());
        }
    }

    /**
   * Returns the <code>pitch</code> angle used by view platform transform.
   */
    protected float getViewPitch() {
        return this.viewPitch;
    }

    /**
   * Sets the <code>pitch</code> angle used by view platform transform.
   */
    protected void setViewPitch(float viewPitch) {
        this.viewPitch = viewPitch;
        if (this.universe != null) {
            updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), getViewYaw(), getViewPitch(), getViewScale());
        }
    }

    /**
   * Updates the given view platform transformation from yaw angle, pitch angle and scale. 
   */
    private void updateViewPlatformTransform(TransformGroup viewPlatformTransform, float viewYaw, float viewPitch, float viewScale) {
        double nominalDistanceToCenter = 1.4 / Math.tan(Math.PI / 8);
        Transform3D pitchRotation = new Transform3D();
        pitchRotation.rotX(viewPitch);
        Transform3D yawRotation = new Transform3D();
        yawRotation.rotY(viewYaw);
        Transform3D transform = new Transform3D();
        transform.setTranslation(new Vector3d(Math.sin(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch), nominalDistanceToCenter * Math.sin(-viewPitch), Math.cos(viewYaw) * nominalDistanceToCenter * Math.cos(viewPitch)));
        Transform3D scale = new Transform3D();
        scale.setScale(viewScale);
        yawRotation.mul(pitchRotation);
        transform.mul(yawRotation);
        scale.mul(transform);
        viewPlatformTransform.setTransform(scale);
    }

    /**
   * Returns scene tree root.
   */
    private BranchGroup createSceneTree() {
        BranchGroup root = new BranchGroup();
        root.setCapability(BranchGroup.ALLOW_DETACH);
        root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        root.addChild(getModelTree());
        root.addChild(getBackgroundNode());
        for (Light light : getLights()) {
            root.addChild(light);
        }
        return root;
    }

    /**
   * Returns the background node.  
   */
    private Node getBackgroundNode() {
        Background background = new Background(new Color3f(0.9f, 0.9f, 0.9f));
        background.setCapability(Background.ALLOW_COLOR_WRITE);
        background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        return background;
    }

    /**
   * Sets the background color.
   */
    public void setBackground(Color backgroundColor) {
        super.setBackground(backgroundColor);
        ((Background) this.sceneTree.getChild(1)).setColor(new Color3f(backgroundColor));
    }

    /**
   * Returns the lights of the scene.
   */
    private Light[] getLights() {
        Light[] lights = { new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(1.732f, -0.8f, -1)), new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(-1.732f, -0.8f, -1)), new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(0, -0.8f, 1)), new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f)) };
        for (Light light : lights) {
            light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
        }
        return lights;
    }

    /**
   * Returns the root of model tree.
   */
    private Node getModelTree() {
        TransformGroup modelTransformGroup = new TransformGroup();
        modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
        modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
        modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        return modelTransformGroup;
    }

    /**
   * Returns the <code>model</code> displayed by this component. 
   */
    public BranchGroup getModel() {
        TransformGroup modelTransformGroup = (TransformGroup) this.sceneTree.getChild(0);
        if (modelTransformGroup.numChildren() > 0) {
            return (BranchGroup) modelTransformGroup.getChild(0);
        } else {
            return null;
        }
    }

    /**
   * Sets the <code>model</code> displayed by this component. 
   * The model is shown at its default orientation and in a box of 1 unit wide.
   */
    public void setModel(BranchGroup model) {
        TransformGroup modelTransformGroup = (TransformGroup) this.sceneTree.getChild(0);
        modelTransformGroup.removeAllChildren();
        if (model != null) {
            model = (BranchGroup) model.cloneTree(true);
            model.setCapability(BranchGroup.ALLOW_DETACH);
            setNodeCapabilities(model);
            if (model.numChildren() > 0) {
                BoundingBox bounds = ModelManager.getInstance().getBounds(model);
                Point3d lower = new Point3d();
                bounds.getLower(lower);
                Point3d upper = new Point3d();
                bounds.getUpper(upper);
                Transform3D translation = new Transform3D();
                translation.setTranslation(new Vector3d(-lower.x - (upper.x - lower.x) / 2, -lower.y - (upper.y - lower.y) / 2, -lower.z - (upper.z - lower.z) / 2));
                Transform3D modelTransform = new Transform3D();
                modelTransform.setScale(1.8 / Math.max(Math.max(upper.x - lower.x, upper.y - lower.y), upper.z - lower.z));
                modelTransform.mul(translation);
                modelTransformGroup.setTransform(modelTransform);
                modelTransformGroup.addChild(model);
            }
        }
    }

    /**
   * Sets the capability to read bounds, to write polygon and material attributes  
   * for all children of <code>node</code>.
   */
    private void setNodeCapabilities(Node node) {
        if (node instanceof Group) {
            node.setCapability(Group.ALLOW_CHILDREN_READ);
            Enumeration<?> enumeration = ((Group) node).getAllChildren();
            while (enumeration.hasMoreElements()) {
                setNodeCapabilities((Node) enumeration.nextElement());
            }
        } else if (node instanceof Shape3D) {
            node.setCapability(Node.ALLOW_BOUNDS_READ);
            Appearance appearance = ((Shape3D) node).getAppearance();
            if (appearance == null) {
                appearance = new Appearance();
                ((Shape3D) node).setAppearance(appearance);
            }
            appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
            appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
            appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
            node.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
            PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
            if (polygonAttributes == null) {
                polygonAttributes = new PolygonAttributes();
                polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
                polygonAttributes.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
                appearance.setPolygonAttributes(polygonAttributes);
            }
        }
    }

    /**
   * Sets the back face visibility of all <code>Shape3D</code> children nodes of displayed model.
   */
    protected void setBackFaceShown(boolean backFaceShown) {
        setBackFaceShown(this.sceneTree.getChild(0), backFaceShown);
    }

    /**
   * Sets the back face visibility of all <code>Shape3D</code> children nodes of <code>node</code>.
   */
    private void setBackFaceShown(Node node, boolean backFaceShown) {
        if (node instanceof Group) {
            Enumeration<?> enumeration = ((Group) node).getAllChildren();
            while (enumeration.hasMoreElements()) {
                setBackFaceShown((Node) enumeration.nextElement(), backFaceShown);
            }
        } else if (node instanceof Shape3D) {
            Appearance appearance = ((Shape3D) node).getAppearance();
            PolygonAttributes polygonAttributes = appearance.getPolygonAttributes();
            polygonAttributes.setCullFace(backFaceShown ? PolygonAttributes.CULL_FRONT : PolygonAttributes.CULL_BACK);
            polygonAttributes.setBackFaceNormalFlip(backFaceShown);
        }
    }

    /**
   * Updates the rotation of the model displayed by this component. 
   * The model is shown at its default size.
   */
    protected void setModelRotation(float[][] modelRotation) {
        BranchGroup model = getModel();
        if (model != null && model.numChildren() > 0) {
            BoundingBox bounds = ModelManager.getInstance().getBounds(model);
            Point3d lower = new Point3d();
            bounds.getLower(lower);
            Point3d upper = new Point3d();
            bounds.getUpper(upper);
            Transform3D translation = new Transform3D();
            translation.setTranslation(new Vector3d(-lower.x - (upper.x - lower.x) / 2, -lower.y - (upper.y - lower.y) / 2, -lower.z - (upper.z - lower.z) / 2));
            Transform3D rotationTransform = new Transform3D();
            if (modelRotation != null) {
                Matrix3f modelRotationMatrix = new Matrix3f(modelRotation[0][0], modelRotation[0][1], modelRotation[0][2], modelRotation[1][0], modelRotation[1][1], modelRotation[1][2], modelRotation[2][0], modelRotation[2][1], modelRotation[2][2]);
                rotationTransform.setRotation(modelRotationMatrix);
            }
            rotationTransform.mul(translation);
            Transform3D modelTransform = new Transform3D();
            modelTransform.setScale(1.8 / Math.max(Math.max((upper.x - lower.x), (upper.z - lower.z)), (upper.y - lower.y)));
            modelTransform.mul(rotationTransform);
            TransformGroup modelTransformGroup = (TransformGroup) this.sceneTree.getChild(0);
            modelTransformGroup.setTransform(modelTransform);
        }
    }

    /**
   * Updates the rotation and the size of the model displayed by this component. 
   */
    protected void setModelRotationAndSize(float[][] modelRotation, float width, float depth, float height) {
        BranchGroup model = getModel();
        if (model != null && model.numChildren() > 0) {
            BoundingBox bounds = ModelManager.getInstance().getBounds(model);
            Point3d lower = new Point3d();
            bounds.getLower(lower);
            Point3d upper = new Point3d();
            bounds.getUpper(upper);
            Transform3D translation = new Transform3D();
            translation.setTranslation(new Vector3d(-lower.x - (upper.x - lower.x) / 2, -lower.y - (upper.y - lower.y) / 2, -lower.z - (upper.z - lower.z) / 2));
            Transform3D scaleOneTransform = new Transform3D();
            scaleOneTransform.setScale(new Vector3d(1 / (upper.x - lower.x), 1 / (upper.y - lower.y), 1 / (upper.z - lower.z)));
            scaleOneTransform.mul(translation);
            Transform3D rotationTransform = new Transform3D();
            if (modelRotation != null) {
                Matrix3f modelRotationMatrix = new Matrix3f(modelRotation[0][0], modelRotation[0][1], modelRotation[0][2], modelRotation[1][0], modelRotation[1][1], modelRotation[1][2], modelRotation[2][0], modelRotation[2][1], modelRotation[2][2]);
                rotationTransform.setRotation(modelRotationMatrix);
            }
            rotationTransform.mul(scaleOneTransform);
            Transform3D scaleTransform = new Transform3D();
            if (width != 0 && depth != 0 && height != 0) {
                scaleTransform.setScale(new Vector3d(width, height, depth));
            }
            scaleTransform.mul(rotationTransform);
            Transform3D modelTransform = new Transform3D();
            if (width != 0 && depth != 0 && height != 0) {
                modelTransform.setScale(1.8 / Math.max(Math.max(width, height), depth));
            } else {
                modelTransform.setScale(1.8 / Math.max(Math.max((upper.x - lower.x), (upper.z - lower.z)), (upper.y - lower.y)));
            }
            modelTransform.mul(scaleTransform);
            TransformGroup modelTransformGroup = (TransformGroup) this.sceneTree.getChild(0);
            modelTransformGroup.setTransform(modelTransform);
        }
    }

    /**
   * Sets the color applied to piece model.
   */
    protected void setModelColor(Integer color) {
        if (color != null) {
            Color3f materialColor = new Color3f(((color >>> 16) & 0xFF) / 256f, ((color >>> 8) & 0xFF) / 256f, (color & 0xFF) / 256f);
            setMaterial(this.sceneTree.getChild(0), new Material(materialColor, new Color3f(), materialColor, materialColor, 64));
        } else {
            setMaterial(this.sceneTree.getChild(0), null);
        }
    }

    /**
   * Sets the material attribute of all <code>Shape3D</code> children nodes of <code>node</code> 
   * with a given <code>material</code>. 
   */
    private void setMaterial(Node node, Material material) {
        if (node instanceof Group) {
            Enumeration<?> enumeration = ((Group) node).getAllChildren();
            while (enumeration.hasMoreElements()) {
                setMaterial((Node) enumeration.nextElement(), material);
            }
        } else if (node instanceof Shape3D) {
            Shape3D shape = (Shape3D) node;
            String shapeName = (String) shape.getUserData();
            if (shapeName == null || !shapeName.startsWith(ModelManager.WINDOW_PANE_SHAPE_PREFIX)) {
                Appearance appearance = shape.getAppearance();
                Material defaultMaterial = (Material) appearance.getUserData();
                if (defaultMaterial == null) {
                    defaultMaterial = appearance.getMaterial();
                    appearance.setUserData(defaultMaterial);
                }
                if (material != null) {
                    appearance.setMaterial(material);
                } else {
                    appearance.setMaterial(defaultMaterial);
                }
            }
        }
    }

    /**
   * Returns the icon image matching the displayed view.  
   */
    private BufferedImage getIconImage(int maxWaitingDelay) {
        Color backgroundColor = getBackground();
        BufferedImage imageWithWhiteBackgound = null;
        BufferedImage imageWithBlackBackgound = null;
        this.iconImageLock = new Object();
        try {
            Point canvas3DOrigin = new Point();
            SwingUtilities.convertPointToScreen(canvas3DOrigin, this.canvas3D);
            Robot robot = new Robot();
            if (this.iconImageLock != null) {
                synchronized (this.iconImageLock) {
                    setBackground(Color.WHITE);
                    try {
                        this.iconImageLock.wait(maxWaitingDelay / 2);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            imageWithWhiteBackgound = robot.createScreenCapture(new Rectangle(canvas3DOrigin, this.canvas3D.getSize()));
            if (this.iconImageLock != null) {
                synchronized (this.iconImageLock) {
                    setBackground(Color.BLACK);
                    try {
                        this.iconImageLock.wait(maxWaitingDelay / 2);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            imageWithBlackBackgound = robot.createScreenCapture(new Rectangle(canvas3DOrigin, this.canvas3D.getSize()));
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        } finally {
            this.iconImageLock = null;
            setBackground(backgroundColor);
        }
        int[] imageWithWhiteBackgoundPixels = imageWithWhiteBackgound.getRGB(0, 0, imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), null, 0, imageWithWhiteBackgound.getWidth());
        int[] imageWithBlackBackgoundPixels = imageWithBlackBackgound.getRGB(0, 0, imageWithBlackBackgound.getWidth(), imageWithBlackBackgound.getHeight(), null, 0, imageWithBlackBackgound.getWidth());
        for (int i = 0; i < imageWithBlackBackgoundPixels.length; i++) {
            if (imageWithBlackBackgoundPixels[i] != imageWithWhiteBackgoundPixels[i] && imageWithBlackBackgoundPixels[i] == 0xFF000000 && imageWithWhiteBackgoundPixels[i] == 0xFFFFFFFF) {
                imageWithWhiteBackgoundPixels[i] = 0;
            }
        }
        BufferedImage iconImage = new BufferedImage(imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) iconImage.getGraphics();
        g2D.drawImage(Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), imageWithWhiteBackgoundPixels, 0, imageWithWhiteBackgound.getWidth())), null, null);
        g2D.dispose();
        return iconImage;
    }

    /**
   * Returns the icon content matching the displayed view.
   */
    public Content getIcon(int maxWaitingDelay) throws IOException {
        File tempIconFile = File.createTempFile("icon", ".png");
        tempIconFile.deleteOnExit();
        ImageIO.write(getIconImage(maxWaitingDelay), "png", tempIconFile);
        return new TemporaryURLContent(tempIconFile.toURI().toURL());
    }

    /**
   * Notifies the canvas 3D displayed by this component was swapped.
   */
    private void canvas3DSwapped() {
        if (this.iconImageLock != null) {
            synchronized (this.iconImageLock) {
                this.iconImageLock.notify();
            }
        }
    }
}
