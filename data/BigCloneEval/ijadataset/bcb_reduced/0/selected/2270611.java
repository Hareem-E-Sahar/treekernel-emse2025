package net.sourceforge.gunner.commons.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;

/**
 * A cross between ColorCube and Box, but with a center not necessarily
 * at 0, 0, 0. Automatically set to not pickable.
 */
public class WireFrameBox extends Shape3D {

    /**
     * The vertices for the box.
     */
    protected static final float[] VERTS = { 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f };

    /**
     * The number of vertices.
     */
    protected static final int VERTEX_COUNT = 24;

    /**
     * Whether the box is visible or not.
     */
    private boolean visible = true;

    /**
     * Create a box with the given dimension.
     * @param xdim the x dimension size.
     * @param ydim the y dimension size.
     * @param zdim the z dimension size.
     * @param xcenter the x center.
     * @param ycenter the y center.
     * @param zcenter the z center.
     * @param color the color.
     */
    public WireFrameBox(float xdim, float ydim, float zdim, float xcenter, float ycenter, float zcenter, Color3f color) {
        setGeometry(getBoxGeometry(xdim, ydim, zdim, xcenter, ycenter, zcenter, color));
        setAppearance(getBoxAppearance());
        setPickable(false);
    }

    /**
     * Gets the geometry for a box with the elements.
     * @param xdim the x dimension size.
     * @param ydim the y dimension size.
     * @param zdim the z dimension size.
     * @param xcenter the x center.
     * @param ycenter the y center.
     * @param zcenter the z center.
     * @param color the color.
     * @return Geometry for a box with the elements.
     */
    protected Geometry getBoxGeometry(float xdim, float ydim, float zdim, float xcenter, float ycenter, float zcenter, Color3f color) {
        QuadArray cube = new QuadArray(VERTEX_COUNT, QuadArray.COORDINATES | QuadArray.COLOR_3);
        float[] scaledVerts = new float[VERTS.length];
        for (int i = 0; i < scaledVerts.length; i += 3) {
            scaledVerts[i] = (VERTS[i] * xdim) + xcenter;
        }
        for (int i = 1; i < scaledVerts.length; i += 3) {
            scaledVerts[i] = (VERTS[i] * ydim) + ycenter;
        }
        for (int i = 2; i < scaledVerts.length; i += 3) {
            scaledVerts[i] = (VERTS[i] * zdim) + zcenter;
        }
        cube.setCoordinates(0, scaledVerts);
        Color3f[] colors = new Color3f[VERTS.length / 3];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = color;
        }
        cube.setColors(0, colors);
        return cube;
    }

    /**
     * Sets whether the box is visible. This assumes no-one changes the
     * visiblilty through the rendering attributes themselves.
     * @param visible whether the box is visible.
     */
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            Appearance boxAppearance = getAppearance();
            RenderingAttributes boxRenderingAttributes = boxAppearance.getRenderingAttributes();
            boxRenderingAttributes.setVisible(visible);
            boxAppearance.setRenderingAttributes(boxRenderingAttributes);
            this.visible = visible;
        }
    }

    /**
     * Gets an Appearance for the box.
     * @return a new Appearance for the box.
     */
    protected Appearance getBoxAppearance() {
        Appearance boxAppearance = new Appearance();
        RenderingAttributes boxRenderingAttributes = new RenderingAttributes();
        boxAppearance.setRenderingAttributes(boxRenderingAttributes);
        PolygonAttributes polygonAttributes = new PolygonAttributes();
        polygonAttributes.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
        boxAppearance.setPolygonAttributes(polygonAttributes);
        return boxAppearance;
    }

    /**
     * Creates a new wireframe box slightly bigger than the size of geometry
     * and adds it to the TransformGroup which is then added to the
     * BranchGroup. (Useful when the scene graph is live)
     * this method adds the capability ALLOW_DETACH to boxBG
     * this method adds the capability ALLOW_TRANSFORM_WRITE to boxTg
     * @param geometry the geometry to create the box off of.
     * @param boxBG the BranchGroup to hold the TransformGroup.
     * @param boxTg the TransformGroup to hold the geometry.
     * @param t3D the Transform3D for the box to orient itself by.
     * @param color the color to make the box
     * @return a new WireFrameBox.
     */
    public static WireFrameBox createWireFrameBoxGeometry(GeometryArray[] geometry, BranchGroup boxBG, TransformGroup boxTg, Transform3D t3D, Color3f color) {
        boolean firstLoop = true;
        float minX = 0.0f;
        float maxX = 0.0f;
        float minY = 0.0f;
        float maxY = 0.0f;
        float minZ = 0.0f;
        float maxZ = 0.0f;
        for (int nGeometry = 0; nGeometry < geometry.length; nGeometry++) {
            float[] coords = null;
            int format = geometry[nGeometry].getVertexFormat();
            if ((format & GeometryArray.COORDINATES) > 0) {
                if ((format & GeometryArray.INTERLEAVED) == 0) {
                    coords = new float[geometry[nGeometry].getVertexCount() * 3];
                    geometry[nGeometry].getCoordinates(0, coords);
                } else if ((format & GeometryArray.INTERLEAVED) > 0) {
                    coords = geometry[nGeometry].getInterleavedVertices();
                }
            }
            if (firstLoop) {
                minX = maxX = coords[0];
            }
            for (int nCoords = 0; nCoords < coords.length; nCoords += 3) {
                if (minX > coords[nCoords]) {
                    minX = coords[nCoords];
                }
                if (maxX < coords[nCoords]) {
                    maxX = coords[nCoords];
                }
            }
            if (firstLoop) {
                minY = maxY = coords[1];
            }
            for (int nCoords = 1; nCoords < coords.length; nCoords += 3) {
                if (minY > coords[nCoords]) {
                    minY = coords[nCoords];
                }
                if (maxY < coords[nCoords]) {
                    maxY = coords[nCoords];
                }
            }
            if (firstLoop) {
                minZ = maxZ = coords[2];
            }
            for (int nCoords = 2; nCoords < coords.length; nCoords += 3) {
                if (minZ > coords[nCoords]) {
                    minZ = coords[nCoords];
                }
                if (maxZ < coords[nCoords]) {
                    maxZ = coords[nCoords];
                }
            }
            firstLoop = false;
        }
        float xCenter = (maxX + minX) / 2;
        float yCenter = (maxY + minY) / 2;
        float zCenter = (maxZ + minZ) / 2;
        float xSize = (Math.abs(maxX) + Math.abs(minX)) / 2;
        float ySize = (Math.abs(maxY) + Math.abs(minY)) / 2;
        float zSize = (Math.abs(maxZ) + Math.abs(minZ)) / 2;
        WireFrameBox box = new WireFrameBox(xSize * 1.1f, ySize * 1.1f, zSize * 1.1f, xCenter, yCenter, zCenter, color);
        boxTg.addChild(box);
        boxBG.addChild(boxTg);
        return box;
    }
}
