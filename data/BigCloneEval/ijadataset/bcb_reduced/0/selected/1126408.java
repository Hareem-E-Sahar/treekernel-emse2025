package visad.java3d;

import visad.*;
import java.lang.reflect.*;
import javax.media.j3d.*;
import javax.vecmath.*;

/**
 * <CODE>DefaultDisplayRendererJ3D</CODE> is the VisAD class for the
 * default background and metadata rendering algorithm under Java3D.<P>
 */
public class DefaultDisplayRendererJ3D extends DisplayRendererJ3D {

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
    public DefaultDisplayRendererJ3D() {
        super();
        mouseBehaviorJ3DClass = MouseBehaviorJ3D.class;
    }

    /**
   * @param mbClass - sub Class of MouseBehaviorJ3D
  */
    public DefaultDisplayRendererJ3D(Class mbj3dClass) {
        super();
        mouseBehaviorJ3DClass = mbj3dClass;
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
        box_geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
        box_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        box_geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
        box_geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        box_geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
        box_geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
        box_geometry.setCoordinates(0, box_verts);
        Appearance box_appearance = new Appearance();
        box_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
        box_appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
        box_appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
        box_appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
        box_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
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
        box.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        BranchGroup box_on = getBoxOnBranch();
        box_on.addChild(box);
        Appearance cursor_appearance = new Appearance();
        cursor_line = new LineAttributes();
        cursor_line.setCapability(LineAttributes.ALLOW_WIDTH_WRITE);
        cursor_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_TEXGEN_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
        cursor_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        cursor_appearance.setLineAttributes(cursor_line);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        cursor_color.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
        float[] ctlCursor = getRendererControl().getCursorColor();
        cursor_color.setColor(ctlCursor[0], ctlCursor[1], ctlCursor[2]);
        cursor_appearance.setColoringAttributes(cursor_color);
        BranchGroup cursor_on = getCursorOnBranch();
        LineArray cursor_geometry = new LineArray(6, LineArray.COORDINATES);
        cursor_geometry.setCoordinates(0, cursor_verts);
        cursor_geometry.setCapability(GeometryArray.ALLOW_COLOR_READ);
        cursor_geometry.setCapability(GeometryArray.ALLOW_COORDINATE_READ);
        cursor_geometry.setCapability(GeometryArray.ALLOW_COUNT_READ);
        cursor_geometry.setCapability(GeometryArray.ALLOW_FORMAT_READ);
        cursor_geometry.setCapability(GeometryArray.ALLOW_NORMAL_READ);
        cursor_geometry.setCapability(GeometryArray.ALLOW_TEXCOORD_READ);
        Shape3D cursor = new Shape3D(cursor_geometry, cursor_appearance);
        cursor.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        cursor.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        cursor_on.addChild(cursor);
        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 2000000.0);
        mouse.setSchedulingBounds(bounds);
        trans.addChild(mouse);
        Color3f color = new Color3f(0.6f, 0.6f, 0.6f);
        AmbientLight light = new AmbientLight(color);
        light.setCapability(Light.ALLOW_COLOR_READ);
        light.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_READ);
        light.setCapability(Light.ALLOW_SCOPE_READ);
        light.setCapability(Light.ALLOW_STATE_READ);
        light.setInfluencingBounds(bounds);
        root.addChild(light);
        Color3f dcolor = new Color3f(0.9f, 0.9f, 0.9f);
        Vector3f direction1 = new Vector3f(0.0f, 0.0f, 1.0f);
        Vector3f direction2 = new Vector3f(0.0f, 0.0f, -1.0f);
        DirectionalLight light1 = new DirectionalLight(true, dcolor, direction1);
        light1.setCapability(DirectionalLight.ALLOW_DIRECTION_READ);
        light1.setCapability(Light.ALLOW_COLOR_READ);
        light1.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_READ);
        light1.setCapability(Light.ALLOW_SCOPE_READ);
        light1.setCapability(Light.ALLOW_STATE_READ);
        light1.setInfluencingBounds(bounds);
        DirectionalLight light2 = new DirectionalLight(true, dcolor, direction2);
        light2.setCapability(DirectionalLight.ALLOW_DIRECTION_READ);
        light2.setCapability(Light.ALLOW_COLOR_READ);
        light2.setCapability(Light.ALLOW_INFLUENCING_BOUNDS_READ);
        light2.setCapability(Light.ALLOW_SCOPE_READ);
        light2.setCapability(Light.ALLOW_STATE_READ);
        light2.setInfluencingBounds(bounds);
        root.addChild(light1);
        root.addChild(light2);
        return root;
    }

    /**
   * set the aspect for the containing box
   * aspect double[3] array used to scale x, y and z box sizes
   */
    public void setBoxAspect(double[] aspect) {
        if (not_destroyed == null) return;
        float[] new_verts = new float[box_verts.length];
        for (int i = 0; i < box_verts.length; i += 3) {
            new_verts[i] = (float) (box_verts[i] * aspect[0]);
            new_verts[i + 1] = (float) (box_verts[i + 1] * aspect[1]);
            new_verts[i + 2] = (float) (box_verts[i + 2] * aspect[2]);
        }
        BranchGroup box_on = getBoxOnBranch();
        Shape3D box = (Shape3D) box_on.getChild(0);
        LineArray box_geometry = (LineArray) box.getGeometry();
        box_geometry.setCoordinates(0, new_verts);
    }

    public void setLineWidth(float width) {
        box_line.setLineWidth(width);
        cursor_line.setLineWidth(width);
    }

    private static final float[] box_verts = { -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f };

    private static final float[] cursor_verts = { 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f };
}
