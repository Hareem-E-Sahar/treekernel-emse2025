package visad.java2d;

import visad.*;
import java.lang.reflect.*;

/**
 * <CODE>DefaultDisplayRendererJ2D</CODE> is the VisAD class for
 * default background and metadata rendering under Java2D.<P>
 */
public class DefaultDisplayRendererJ2D extends DisplayRendererJ2D {

    /** box outline for data */
    private VisADAppearance box = null;

    /** cursor */
    private VisADAppearance cursor = null;

    private Class mouseBehaviorJ2DClass = null;

    /** Behavior for mouse interactions */
    private MouseBehaviorJ2D mouse = null;

    /**
   * This is the default <CODE>DisplayRenderer</CODE> used by the
   * <CODE>DisplayImplJ2D</CODE> constructor.
   * It draws a 2-D box around the scene and a 2-D cursor.<P>
   * The left mouse button controls the projection as follows:
   * <UL>
   *  <LI>mouse drag or mouse drag with Ctrl translates the scene sideways
   *  <LI>mouse drag with Shift down zooms the scene
   * </UL>
   * The center mouse button activates and controls the 2-D cursor as
   * follows:
   * <UL>
   *  <LI>mouse drag translates the cursor sideways
   * </UL>
   * The right mouse button is used for direct manipulation by clicking on
   * the depiction of a <CODE>Data</CODE> object and dragging or re-drawing
   * it.<P>
   * Cursor and direct manipulation locations are displayed in RealType
   * values<P>
   * <CODE>BadMappingExceptions</CODE> and
   * <CODE>UnimplementedExceptions</CODE> are displayed<P>
   * No RealType may be mapped to ZAxis, Latitude or Alpha.
   */
    public DefaultDisplayRendererJ2D() {
        super();
        mouseBehaviorJ2DClass = MouseBehaviorJ2D.class;
    }

    /**
   * @param mbClass - sub Class of MouseBehaviorJ2D
  */
    public DefaultDisplayRendererJ2D(Class mbj2dClass) {
        super();
        mouseBehaviorJ2DClass = mbj2dClass;
    }

    public boolean getMode2D() {
        return true;
    }

    public boolean legalDisplayScalar(DisplayRealType type) {
        if (Display.ZAxis.equals(type) || Display.Latitude.equals(type) || Display.Alpha.equals(type)) return false; else return super.legalDisplayScalar(type);
    }

    /**
   * Create scene graph root, if none exists, with Transform
   * and direct manipulation root;
   * create 3-D box, lights and <CODE>MouseBehaviorJ2D</CODE> for
   * embedded user interface.
   * @param c
   * @return Scene graph root.
   * @exception DisplayException
   */
    public VisADGroup createSceneGraph(VisADCanvasJ2D c) throws DisplayException {
        VisADGroup root = getRoot();
        if (root != null) return root;
        try {
            Class[] param = new Class[] { DisplayRendererJ2D.class };
            Constructor mbConstructor = mouseBehaviorJ2DClass.getConstructor(param);
            mouse = (MouseBehaviorJ2D) mbConstructor.newInstance(new Object[] { this });
        } catch (Exception e) {
            throw new VisADError("cannot construct " + mouseBehaviorJ2DClass);
        }
        getDisplay().setMouseBehavior(mouse);
        box = new VisADAppearance();
        cursor = new VisADAppearance();
        root = createBasicSceneGraph(c, mouse, box, cursor);
        VisADLineArray box_array = new VisADLineArray();
        box_array.coordinates = box_verts;
        box_array.vertexCount = 8;
        float[] ctlBox = getRendererControl().getBoxColor();
        box.red = ctlBox[0];
        box.green = ctlBox[1];
        box.blue = ctlBox[2];
        box.color_flag = true;
        box.array = box_array;
        VisADGroup box_on = getBoxOnBranch();
        box_on.addChild(box);
        VisADLineArray cursor_array = new VisADLineArray();
        cursor_array.coordinates = cursor_verts;
        cursor_array.vertexCount = 4;
        float[] ctlCursor = getRendererControl().getCursorColor();
        cursor.red = ctlCursor[0];
        cursor.green = ctlCursor[1];
        cursor.blue = ctlCursor[2];
        cursor.color_flag = true;
        cursor.array = cursor_array;
        VisADGroup cursor_on = getCursorOnBranch();
        cursor_on.addChild(cursor);
        return root;
    }

    /**
   * set the aspect for the containing box
   * aspect double[3] array used to scale x, y and z box sizes
   */
    public void setBoxAspect(double[] aspect) {
        float[] new_verts = new float[box_verts.length];
        for (int i = 0; i < box_verts.length; i += 3) {
            new_verts[i] = (float) (box_verts[i] * aspect[0]);
            new_verts[i + 1] = (float) (box_verts[i + 1] * aspect[1]);
            new_verts[i + 2] = (float) (box_verts[i + 2] * aspect[2]);
        }
        VisADLineArray box_array = (VisADLineArray) box.array;
        box_array.coordinates = new_verts;
    }

    public void setLineWidth(float width) {
        box.lineWidth = width;
        cursor.lineWidth = width;
    }

    private static final float[] box_verts = { -1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f };

    private static final float[] cursor_verts = { 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.1f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f };
}
