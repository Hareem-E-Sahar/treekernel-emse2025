package org.taddei.jemv.util;

import visad.*;
import visad.java3d.MouseBehaviorJ3D;
import visad.java3d.VisADCanvasJ3D;
import visad.java3d.DisplayRendererJ3D;
import java.lang.reflect.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;

public class DefinableLightD3DRenderer extends visad.java3d.DefaultDisplayRendererJ3D {

    private float lightIntensity = 0.9f;

    private float[] lightSourcePoint = new float[] { 0f, 0f, 0f };

    boolean fixedLight = false;

    private Object not_destroyed = new Object();

    /** color of box and cursor */
    private ColoringAttributes box_color = null;

    private ColoringAttributes cursor_color = null;

    /** line of box and cursor */
    private LineAttributes box_line = null;

    private LineAttributes cursor_line = null;

    private Class mouseBehaviorJ3DClass = null;

    private MouseBehaviorJ3D mouse = null;

    /**
   * This is the default <CODE>DisplayRenderer</CODE> used by the
   * <CODE>DisplayImplJ3D</CODE> constructor.
   * It draws a 3-D cube around the scene.<P>
   * The left mouse button controls the projection as follows:
   * <UL>
   *  <LI>mouse drag rotates in 3-D
   *  <LI>mouse drag with Shift down zooms the scene
   *  <LI>mouse drag with Ctrl translates the scene sideways
   * </UL>
   * The center mouse button activates and controls the
   * 3-D cursor as follows:
   * <UL>
   *  <LI>mouse drag translates the cursor sideways
   *  <LI>mouse drag with Shift translates the cursor in and out
   *  <LI>mouse drag with Ctrl rotates scene in 3-D with cursor on
   * </UL>
   * The right mouse button is used for direct manipulation by clicking on
   * the depiction of a <CODE>Data</CODE> object and dragging or re-drawing
   * it.<P>
   * Cursor and direct manipulation locations are displayed in RealType
   * values.<P>
   * <CODE>BadMappingExceptions</CODE> and
   * <CODE>UnimplementedExceptions</CODE> are displayed<P>
   */
    public DefinableLightD3DRenderer() {
        super();
        mouseBehaviorJ3DClass = MouseBehaviorJ3D.class;
    }

    /**
   * @param mbClass - sub Class of MouseBehaviorJ3D
  */
    public DefinableLightD3DRenderer(Class mbj3dClass) {
        super();
        mouseBehaviorJ3DClass = mbj3dClass;
    }

    public DefinableLightD3DRenderer(float lightIntensity, float[] lightPosition, boolean fixLight) {
        super();
        mouseBehaviorJ3DClass = MouseBehaviorJ3D.class;
        this.lightIntensity = lightIntensity;
        lightSourcePoint = lightPosition;
        fixedLight = fixLight;
    }

    public void destroy() {
        not_destroyed = null;
        box_color = null;
        cursor_color = null;
        mouse = null;
        super.destroy();
    }

    /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ3D</CODE> for
   * embedded user interface.
   * @param v
   * @param vpt
   * @param c
   * @return Scene graph root.
   */
    public BranchGroup createSceneGraph(View v, TransformGroup vpt, VisADCanvasJ3D c) {
        if (not_destroyed == null) return null;
        BranchGroup root = getRoot();
        if (root != null) return root;
        try {
            Class[] param = new Class[] { DisplayRendererJ3D.class };
            Constructor mbConstructor = mouseBehaviorJ3DClass.getConstructor(param);
            mouse = (MouseBehaviorJ3D) mbConstructor.newInstance(new Object[] { this });
        } catch (Exception e) {
            throw new VisADError("cannot construct " + mouseBehaviorJ3DClass);
        }
        getDisplay().setMouseBehavior(mouse);
        box_color = new ColoringAttributes();
        cursor_color = new ColoringAttributes();
        root = createBasicSceneGraph(v, vpt, c, mouse, box_color, cursor_color);
        TransformGroup trans = getTrans();
        LineArray box_geometry = new LineArray(24, LineArray.COORDINATES);
        box_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
        box_geometry.setCoordinates(0, box_verts);
        Appearance box_appearance = new Appearance();
        box_line = new LineAttributes();
        box_line.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
        box_appearance.setLineAttributes(box_line);
        box_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        box_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        float[] ctlBox = getRendererControl().getBoxColor();
        box_color.setColor(ctlBox[0], ctlBox[1], ctlBox[2]);
        box_appearance.setColoringAttributes(box_color);
        Shape3D box = new Shape3D(box_geometry, box_appearance);
        box.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        BranchGroup box_on = getBoxOnBranch();
        box_on.addChild(box);
        Appearance cursor_appearance = new Appearance();
        cursor_line = new LineAttributes();
        cursor_line.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
        cursor_appearance.setLineAttributes(cursor_line);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        float[] ctlCursor = getRendererControl().getCursorColor();
        cursor_color.setColor(ctlCursor[0], ctlCursor[1], ctlCursor[2]);
        cursor_appearance.setColoringAttributes(cursor_color);
        BranchGroup cursor_on = getCursorOnBranch();
        LineArray cursor_geometry = new LineArray(6, LineArray.COORDINATES);
        cursor_geometry.setCoordinates(0, cursor_verts);
        Shape3D cursor = new Shape3D(cursor_geometry, cursor_appearance);
        cursor_on.addChild(cursor);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2000000.0);
        mouse.setSchedulingBounds(bounds);
        trans.addChild(mouse);
        Color3f color = new Color3f(0.6f, 0.6f, 0.6f);
        AmbientLight light = new AmbientLight(color);
        light.setInfluencingBounds(bounds);
        root.addChild(light);
        Color3f dcolor = new Color3f(lightIntensity, lightIntensity, lightIntensity);
        Vector3f direction1 = new Vector3f(lightSourcePoint[0], lightSourcePoint[1], lightSourcePoint[2]);
        Vector3f direction2 = new Vector3f(-lightSourcePoint[0], -lightSourcePoint[1], -lightSourcePoint[2]);
        DirectionalLight light1 = new DirectionalLight(true, dcolor, direction1);
        light1.setInfluencingBounds(bounds);
        DirectionalLight light2 = new DirectionalLight(true, dcolor, direction2);
        light2.setInfluencingBounds(bounds);
        if (fixedLight) {
            root.addChild(light1);
            root.addChild(light2);
        } else {
            BranchGroup group2 = new BranchGroup();
            group2.addChild(light1);
            group2.addChild(light2);
            trans.addChild(group2);
        }
        return root;
    }

    private static final float[] box_verts = { -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f };

    private static final float[] cursor_verts = { 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f };
}
