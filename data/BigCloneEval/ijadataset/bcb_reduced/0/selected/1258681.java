package syn3d.ui.xith3d;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import syn3d.base.ActiveNode;
import syn3d.nodes.SceneNode;
import syn3d.nodes.xith3d.SceneNodeXith3D;
import syn3d.ui.Frame3DBase;
import syn3d.base.PluginManager;
import syn3d.base.java3d.Java3DPluginManager;
import com.xith3d.picking.PickRenderResult;
import com.xith3d.render.CanvasPeer;
import com.xith3d.render.RenderPeer;
import com.xith3d.scenegraph.AmbientLight;
import com.xith3d.scenegraph.Appearance;
import com.xith3d.scenegraph.Bounds;
import com.xith3d.scenegraph.BranchGroup;
import com.xith3d.scenegraph.Canvas3D;
import com.xith3d.scenegraph.DirectionalLight;
import com.xith3d.scenegraph.Locale;
import com.xith3d.scenegraph.Node;
import com.xith3d.scenegraph.PolygonAttributes;
import com.xith3d.scenegraph.Shape3D;
import com.xith3d.scenegraph.Transform3D;
import com.xith3d.scenegraph.TransformGroup;
import com.xith3d.scenegraph.View;
import com.xith3d.scenegraph.VirtualUniverse;
import com.xith3d.spatial.bounds.Sphere;
import com.xith3d.test.TestUtils;

/**
 * Class description ...
 * 
 * @author Claude CAZENAVE
 *
 */
public class Frame3DXith3D extends Frame3DBase {

    private static boolean init = false;

    private static boolean useJogl = true;

    protected RenderPeer peer;

    protected CanvasPeer canvasPeer;

    protected Canvas3D canvas;

    protected View view;

    protected Transform3D sceneTransform = new Transform3D();

    protected Matrix4f sceneMatrix = new Matrix4f();

    protected Transform3D transform;

    protected Transform3D tempTransform;

    protected ArrayList lastPickSelection = new ArrayList();

    protected int lastMouseX = -1, lastMouseY = -1;

    protected SceneNodeXith3D scene;

    public SceneNode getScene() {
        return scene;
    }

    /**
     * @return Returns the view.
     */
    public View getView() {
        return view;
    }

    /**
	 * @param owner
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
    public Frame3DXith3D(SceneNodeXith3D scene, Object owner, String t, int x, int y, int width, int height, PluginManager pm) {
        super(owner, t, x, y, width, height, pm);
        this.scene = scene;
    }

    protected void internalMakePeer(Object owner) {
        if (!init) {
            try {
                peer = new com.xith3d.render.jogl.RenderPeerImpl();
                canvasPeer = peer.makeCanvas(owner, width, height, 16, false);
                useJogl = true;
            } catch (Throwable e) {
                peer = new com.xith3d.render.lwjgl.RenderPeerImpl();
                canvasPeer = peer.makeCanvas(owner, width, height, 16, false);
                useJogl = false;
            }
            init = true;
        } else {
            if (useJogl) {
                peer = new com.xith3d.render.jogl.RenderPeerImpl();
                canvasPeer = peer.makeCanvas(owner, width, height, 16, false);
            } else {
                peer = new com.xith3d.render.lwjgl.RenderPeerImpl();
                canvasPeer = peer.makeCanvas(owner, width, height, 16, false);
            }
        }
    }

    protected void createPeer(Object owner) {
        transform = new Transform3D();
        tempTransform = new Transform3D();
        internalMakePeer(owner);
        peerWindow = canvasPeer.getWindow();
        if (peerWindow != null) {
            WindowListener[] listeners = peerWindow.getWindowListeners();
            for (int i = 0; i < listeners.length; ++i) {
                peerWindow.removeWindowListener(listeners[i]);
            }
            peerWindow.dispose();
        }
        AutoRepaintFrame arf = new AutoRepaintFrame();
        arf.setBounds(x, y, width, height);
        arf.setLayout(new BorderLayout(0, 0));
        peerComponent = canvasPeer.getComponent();
        if (peerComponent == null) {
            internalMakePeer(arf);
            peerComponent = canvasPeer.getComponent();
        } else arf.add(peerComponent, BorderLayout.CENTER);
        peerWindow = arf;
        peerWindow.show();
        if (canvasPeer instanceof com.xith3d.render.jogl.CanvasPeerImpl) ((com.xith3d.render.jogl.CanvasPeerImpl) canvasPeer).setWindow(peerWindow);
        peerWindow.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                peerWindow.setVisible(false);
            }
        });
        if (peerWindow != null) {
            peerWindow.setLocation(x, y);
        }
        canvas = new Canvas3D();
        canvas.set3DPeer(canvasPeer);
        view = new View();
        view.addCanvas3D(canvas);
        if (peerComponent != null) {
            peerComponent.addMouseListener(this);
            peerComponent.addMouseMotionListener(this);
            peerComponent.addMouseWheelListener(this);
            peerComponent.addKeyListener(this);
        }
    }

    public void setPerspective(boolean p) {
        super.setPerspective(p);
        if (perspective) view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION); else view.setProjectionPolicy(View.PARALLEL_PROJECTION);
    }

    public void rotate2D(int newX, int newY) {
        int mode;
        if (scene != null) mode = scene.getLightRotationMode(); else mode = -1;
        if (mode != -1) {
            float dx = (float) (newX - pos2DX) / wsize;
            float dy = (float) (newY - pos2DY) / wsize;
            drotX.rotX(dy * 2 * (float) Math.PI);
            drotY.rotY(dx * 2 * (float) Math.PI);
            Matrix4f transform = new Matrix4f(rot);
            transform.mul(drotX);
            transform.mul(drotY);
            Matrix4f tmp = new Matrix4f(rot);
            tmp.invert();
            transform.mul(tmp);
            DirectionalLight light = scene.getLights()[mode];
            Vector3f direction = light.getDirection();
            transform.transform(direction);
            light.setDirection(direction);
            scene.showLightVector(direction.x, direction.y, direction.z);
            pos2DX = newX;
            pos2DY = newY;
            peerWindow.repaint();
            return;
        }
        if (scene != null) {
            float dx = (float) (newX - pos2DX) / wsize;
            float dy = (float) (newY - pos2DY) / wsize;
            drotX.rotX(-dy * 2 * (float) Math.PI);
            drotY.rotY(-dx * 2 * (float) Math.PI);
            Matrix4f transform = new Matrix4f(rot);
            transform.mul(drotX);
            transform.mul(drotY);
            Matrix4f tmp = new Matrix4f(rot);
            tmp.invert();
            transform.mul(tmp);
            DirectionalLight[] lights = scene.getLights();
            for (int i = 0; i < lights.length; ++i) {
                Vector3f direction = lights[i].getDirection();
                transform.transform(direction);
                lights[i].setDirection(direction);
            }
        }
        super.rotate2D(newX, newY);
    }

    public void applyTransform() {
        super.applyTransform();
        transform.set(rot);
        tempTransform.set(trans);
        transform.mul(tempTransform);
        tempTransform.set(zoom);
        transform.mul(tempTransform);
        view.setTransform(transform);
        peerWindow.repaint();
    }

    protected float getSceneSize() {
        boolean autocompute = scene.getBranchgroup().getBoundsAutoCompute();
        scene.getBranchgroup().setBoundsAutoCompute(true);
        scene.getBranchgroup().updateBounds(true);
        scene.getBranchgroup().setBoundsAutoCompute(autocompute);
        Bounds bounds = scene.getBranchgroup().getBounds();
        if (bounds instanceof Sphere) {
            return ((Sphere) bounds).getRadius();
        }
        return 0;
    }

    /** 
     * Adds or removes a single pick at the given position to the selected objects. 
     * @param posX the 2D X position where to do the picking   
     * @param posY the 2D Y position where to do the picking
     * @return Returns the current selection, possibly an empy array
     * @see ActiveNode.higlight(boolean,Object)  
     */
    public ArrayList toggleSinglePick(int posX, int posY) {
        lastMouseX = posX;
        lastMouseY = posY;
        PickRenderResult[] results = view.pick(canvas, posX, posY, 3, 3);
        if ((results != null) && (results.length > 0)) {
            Node nodes[] = results[0].getNodes();
            if (nodes != null) {
                Object o = nodes[0].getUserData();
                if (o instanceof ActiveNode) {
                    ((ActiveNode) o).highlight(true, nodes[0]);
                    lastPickSelection.add(o);
                    lastPickSelection.add(nodes[0]);
                    System.out.println(((ActiveNode) o).getName());
                }
            }
        }
        return lastPickSelection;
    }

    /** 
     * Adds or removes all picks between the given position and the last position, 
     * to the selected objects. 
     * @param posX the 2D X position defining a region with the last position. All objects in this region should be picked.     
     * @param posX the 2D Y position defining a region with the last position. All objects in this region should be picked.     
     * @return Returns the current selection, possibly an empy array  
     * @see ActiveNode.higlight(boolean,Object)  
     */
    public ArrayList toggleAllPicks(int posX, int posY) {
        int x = (posX + lastMouseX) / 2;
        int y = (posY + lastMouseY) / 2;
        int dx = Math.abs(posX - lastMouseX) / 2;
        int dy = Math.abs(posY - lastMouseY) / 2;
        PickRenderResult[] results = view.pick(canvas, x, y, dx, dy);
        if ((results != null) && (results.length > 0)) {
            Node nodes[] = results[0].getNodes();
            if (nodes != null) {
                Object o = nodes[0].getUserData();
                if (o instanceof ActiveNode) {
                    ((ActiveNode) o).highlight(true, nodes[0]);
                    lastPickSelection.add(o);
                    lastPickSelection.add(nodes[0]);
                    System.out.println(((ActiveNode) o).getName());
                }
            }
        }
        return lastPickSelection;
    }

    /** 
     * Selects a single pick at the given position. 
     * @param posX the 2D X position where to do the picking   
     * @param posY the 2D Y position where to do the picking   
     * @return Returns the picked node or possibly a null object if there was nothing to pick at this position  
     * @see ActiveNode.higlight(boolean,Object)  
     */
    public ActiveNode pick(int posX, int posY) {
        int s = lastPickSelection.size();
        for (int i = 0; i < s; i += 2) {
            ActiveNode node = (ActiveNode) lastPickSelection.get(i);
            node.highlight(false, lastPickSelection.get(i + 1));
        }
        lastPickSelection.clear();
        toggleSinglePick(posX, posY);
        if (lastPickSelection.size() > 0) return (ActiveNode) lastPickSelection.get(0);
        return null;
    }

    public class AutoRepaintFrame extends Frame implements WindowFocusListener {

        /**
         * @throws java.awt.HeadlessException
         */
        public AutoRepaintFrame() throws HeadlessException {
            addWindowFocusListener(this);
        }

        public void paint(Graphics g) {
            super.paint(g);
            if (init) view.renderOnce();
        }

        public void windowGainedFocus(WindowEvent e) {
            if (init) view.renderOnce();
        }

        public void windowLostFocus(WindowEvent e) {
        }
    }

    /**
	 * Main function to test navigation
	 * @param args
	 */
    public static void main(String[] args) {
        VirtualUniverse universe = new VirtualUniverse();
        Locale locale = new Locale();
        universe.addLocale(locale);
        BranchGroup objRoot = new BranchGroup();
        Appearance a = new Appearance();
        a.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0));
        Shape3D sph = new Shape3D(TestUtils.createSphere(1.0f, 20), a);
        TransformGroup sphereTrans = new TransformGroup();
        sphereTrans.addChild(sph);
        objRoot.addChild(sphereTrans);
        AmbientLight aLgt = new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f));
        objRoot.addChild(aLgt);
        locale.addBranchGraph(objRoot);
        PluginManager pluginManager = (PluginManager) new Java3DPluginManager();
        Frame3DXith3D f1 = new Frame3DXith3D(null, null, "Test1", -1, -1, 600, 400, pluginManager);
        universe.addView(f1.view);
        while (true) {
            f1.view.renderOnce();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
        }
    }
}
