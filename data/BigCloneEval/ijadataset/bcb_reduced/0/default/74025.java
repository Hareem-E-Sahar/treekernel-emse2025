import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;
import javax.media.opengl.*;
import com.sun.opengl.util.BufferUtil;
import jahuwaldt.gl.LODLandscape;
import jahuwaldt.gl.HeightMap;
import jahuwaldt.gl.GLTools;
import jahuwaldt.util.LRUTileCache;
import jahuwaldt.swing.ProgressBarHandler;

/**
*  This class is used to render a landscape (rectangular array of
*  altitude points) using a version of the GeoMipMap level-of-detail
*  algorithm.
*
*  <p>  This code is based on the paper "Fast Terrain Rendering Using Geometrical MipMapping",
*       Willem H. de Boer, whdeboer@iname.com, E-mersion Project, October 2000,
*       http://www.flipcode.com/tutorials/tut_geomipmaps.shtml.
*  </p>
*
*  <p>  Modified by:  Joseph A. Huwaldt   </p>
*
*  @author  Joseph A. Huwaldt   Date:  April 19, 2001
*  @version March 30, 2010
**/
public class GeoMMLandscape2 implements LODLandscape {

    private int gMapXSize;

    private int gMapYSize;

    private float gGridSpacing = 1;

    private HeightMap gHeightMap;

    private int minAlt;

    private float colorFactor;

    private int gNumTrisRendered = 0;

    private int gDrawMode = kUseTexture;

    private QuadTreeNode[] gPatches;

    private int gNumPatches;

    private TerrainBlock[][] gBlocks;

    private int gBlockSize;

    private int gUndefinedElev;

    private int gMipMapLevels;

    private FloatBuffer vertexBuffer;

    private FloatBuffer textureBuffer;

    private boolean useAltShading = true;

    private FloatBuffer colorBuffer;

    private int gThreshold;

    private float gTextureScaleX, gTextureScaleY;

    /**
	*  Create a new GeoMipMap landscape renderer using the specified height map.
	*
	*  @param  gl           The OpenGL context we are rendering into.
	*  @param  altMap       The map of altitude points (height map) that make up the terrain.
	*                       The map width & height must be 2^n + 1.  A reference is kept.
	*  @param  blockSize    The size of an individual block of terrain (square and must be 2^m).
	*  @param  gridSpacing  The model (world) coordinate distance between grid points.
	*  @param  undefinedElev A code indicating values less than this are undefined, but coded
	*                        with a fill value.  To not use this feature, pass (-Float.MAX_VALUE).
	*  @param  vfNear       The distance to the view frustum near clipping plane.
	*  @param  vfTop        The top coordinate of the view frustum near clipping plane.
	*  @param  vRes         The height of the rendering window in pixels.
	*  @param  tau          The threshold for switching mip-map levels.
	*  @param  progBar      A progress bar handler that shows progress of initialization.  Pass null
	*                       for no progress bar.
	*  @param  pscale       Progress scaling factor between 0 and 1.  Indicates how long this task
	*                       should take relative to others for updating progress bar.  Set to 1.0F
	*                       if this task should take up the entire progress bar.
	**/
    public GeoMMLandscape2(HeightMap altMap, int blockSize, float gridSpacing, int undefinedElev, float vfNear, float vfTop, int vRes, int tau, ProgressBarHandler progBar, float pscale) throws IllegalArgumentException, InterruptedException {
        int height = altMap.getHeight() - 1;
        int width = altMap.getWidth() - 1;
        gBlockSize = blockSize;
        gUndefinedElev = undefinedElev;
        if (width % blockSize != 0) throw new IllegalArgumentException("Height map width must be (blockSize*n + 1).");
        if (height % blockSize != 0) throw new IllegalArgumentException("Height map height must be (blockSize*n + 1).");
        gMapXSize = width;
        gMapYSize = height;
        gHeightMap = altMap;
        gGridSpacing = gridSpacing;
        this.minAlt = (int) altMap.minAlt();
        colorFactor = 0.8f / (altMap.maxAlt() - minAlt);
        gMipMapLevels = log2(blockSize) + 1;
        vertexBuffer = BufferUtil.newFloatBuffer((blockSize + 1) * 2 * 3);
        textureBuffer = BufferUtil.newFloatBuffer((blockSize + 1) * 2 * 2);
        colorBuffer = BufferUtil.newFloatBuffer((blockSize + 1) * 2 * 3);
        GLTools.initSinDCosD();
        gThreshold = tau;
        float A = vfNear / (float) Math.abs(vfTop);
        float T = 2 * tau / (float) vRes;
        float kC = A / T;
        initPatches(width, height, blockSize, kC, progBar, pscale * 0.9F);
        linkTerrainBlocks();
    }

    /**
	*  This method is called to indicate if automatic altitude shading should be
	*  used when rendering.  Altitude shading darkens the terrain
	*  at low altitudes and brightens it at high altitudes.
	**/
    public void setAltShading(boolean flag) {
        useAltShading = flag;
    }

    /**
	*  Set the drawing mode to one of the constants defined
	*  in this class.
	**/
    public void setDrawMode(int mode) {
        if (mode < 0) mode = 0; else if (mode > 3) mode = 3;
        gDrawMode = mode;
    }

    /**
	*  Return the drawing mode used by this landscape.
	**/
    public int getDrawMode() {
        return gDrawMode;
    }

    /**
	*  Return the level-of-detail threshold used to set the
	*  mip-map levels used in this landscape rendering algorithm.
	*  The threshold is an integer number representing the maximum
	*  error allowed in screen pixels.  A value of 1 gives the highest
	*  level of detail.  Larger numbers result in lower detail.
	**/
    public int getLevelOfDetail() {
        return gThreshold;
    }

    /**
	*  Set the level-of-detail threshold used to set the
	*  mip-map levels used in this landscape rendering algorithm.
	*  The threshold is an integer number representing the maximum
	*  error allowed in screen pixels.  A value of 1 gives the highest
	*  level of detail.  Larger numbers result in lower detail.
	**/
    public void setLevelOfDetail(int number) {
        if (number < 1) number = 1;
        int sizeY = gBlocks.length;
        int sizeX = gBlocks[0].length;
        for (int i = 0; i < sizeY; ++i) {
            for (int j = 0; j < sizeX; ++j) {
                gBlocks[i][j].changeThreshold(number);
            }
        }
        gThreshold = number;
    }

    /**
	*  Returns the actual number of triangles rendered during
	*  the last pass through the render() method.
	**/
    public int numTrianglesRendered() {
        return gNumTrisRendered;
    }

    /**
	*  Initialize all the quad-tree patches in the landscape down to
	*  terrain block size.
	**/
    private void initPatches(int mapWidth, int mapHeight, int blockSize, float kC, ProgressBarHandler progBar, float pscale) throws InterruptedException {
        int sizeX = mapWidth / blockSize;
        int sizeY = mapHeight / blockSize;
        gBlocks = new TerrainBlock[sizeY][sizeX];
        float progInc = pscale / sizeY;
        for (int i = 0; i < sizeY; ++i) {
            for (int j = 0; j < sizeX; ++j) gBlocks[i][j] = new TerrainBlock(j * blockSize, i * blockSize, blockSize, kC);
            if (progBar != null) {
                if (progBar.isCanceled()) throw new InterruptedException("User canceled.");
                progBar.setProgress(progBar.getProgress() + progInc);
            }
        }
        ArrayList patchList = new ArrayList();
        findPatches(patchList, 0, 0, mapWidth, mapHeight, blockSize, kC);
        gNumPatches = patchList.size();
        gPatches = new QuadTreeNode[gNumPatches];
        for (int i = 0; i < gNumPatches; ++i) {
            QuadTreeNode patch = (QuadTreeNode) patchList.get(i);
            gPatches[i] = patch;
        }
    }

    /**
	*  Link up terrain blocks.
	**/
    private void linkTerrainBlocks() {
        int nx = gBlocks[0].length;
        int ny = gBlocks.length;
        for (int yPos = 0; yPos < ny; ++yPos) {
            for (int xPos = 0; xPos < nx; ++xPos) {
                TerrainBlock block = gBlocks[yPos][xPos];
                if (yPos > 0) block.north = gBlocks[yPos - 1][xPos];
                if (yPos < gBlocks.length - 1) block.south = gBlocks[yPos + 1][xPos];
                if (xPos > 0) block.west = gBlocks[yPos][xPos - 1];
                if (xPos < gBlocks[0].length - 1) block.east = gBlocks[yPos][xPos + 1];
            }
        }
    }

    /**
	*  Recursively identify all the patches required to cover the terrain.
	*  We'll have one big patch in the upper left with smaller
	*  ones surrounding it as needed depending on map shape.
	**/
    private void findPatches(List patchList, int xOffset, int yOffset, int width, int height, int blockSize, float kC) {
        int maxPWidth = (int) Math.pow(2, log2(Math.min(width, height)));
        QuadTreeNode patch;
        if (maxPWidth > blockSize) patch = new Patch(xOffset, yOffset, maxPWidth, blockSize, kC); else {
            int xPos = xOffset / blockSize;
            int yPos = yOffset / blockSize;
            patch = gBlocks[yPos][xPos];
        }
        patchList.add(patch);
        int usedWidth = maxPWidth;
        int pWidth = maxPWidth;
        int diff = width - usedWidth;
        while (diff > 0) {
            while (pWidth > diff) pWidth /= 2;
            int vCount = maxPWidth / pWidth;
            for (int i = 0; i < vCount; ++i) {
                if (pWidth > blockSize) patch = new Patch(xOffset + usedWidth, yOffset + i * pWidth, pWidth, blockSize, kC); else {
                    int xPos = (xOffset + usedWidth) / blockSize;
                    int yPos = (yOffset + i * pWidth) / blockSize;
                    patch = gBlocks[yPos][xPos];
                }
                patchList.add(patch);
            }
            usedWidth += pWidth;
            diff = width - usedWidth;
        }
        usedWidth = maxPWidth;
        pWidth = maxPWidth;
        diff = height - usedWidth;
        while (diff > 0) {
            while (pWidth > diff) pWidth /= 2;
            int vCount = maxPWidth / pWidth;
            for (int i = 0; i < vCount; ++i) {
                if (pWidth > blockSize) patch = new Patch(xOffset + i * pWidth, yOffset + usedWidth, pWidth, blockSize, kC); else {
                    int xPos = (xOffset + i * pWidth) / blockSize;
                    int yPos = (yOffset + usedWidth) / blockSize;
                    patch = gBlocks[yPos][xPos];
                }
                patchList.add(patch);
            }
            usedWidth += pWidth;
            diff = height - usedWidth;
        }
        width -= maxPWidth;
        height -= maxPWidth;
        if (width > 0 && height > 0) findPatches(patchList, xOffset + maxPWidth, yOffset + maxPWidth, width, height, blockSize, kC);
    }

    /**
	*  Determine visibility of all terrain blocks and determine the geo mip-map level
	*  that will be used by each.
	*
	*  @param  fovX         The field of view in degrees.
	*  @param  viewPosition The location of the camera in model coordinates.
	*  @param  clipAngle    The direction the camera is pointing.
	**/
    public void update(float fovX, float[] viewPosition, float clipAngle) {
        int FOV_DIV_2 = Math.round(fovX / 2);
        int iClip = GLTools.range360(Math.round(clipAngle));
        int eyeX = (int) (viewPosition[0] / gGridSpacing) - (int) (gBlockSize / 2 * GLTools.sinD(iClip));
        int eyeY = (int) (viewPosition[2] / gGridSpacing) + (int) (gBlockSize / 2 * GLTools.cosD(iClip));
        int patch2 = gBlockSize * 2;
        int angle = GLTools.range360(iClip - FOV_DIV_2);
        int leftX = eyeX + (int) (patch2 * GLTools.sinD(angle));
        int leftY = eyeY - (int) (patch2 * GLTools.cosD(angle));
        angle = GLTools.range360(iClip + FOV_DIV_2);
        int rightX = eyeX + (int) (patch2 * GLTools.sinD(angle));
        int rightY = eyeY - (int) (patch2 * GLTools.cosD(angle));
        for (int i = 0; i < gNumPatches; ++i) {
            QuadTreeNode patch = gPatches[i];
            patch.setVisibility(eyeX, eyeY, leftX, leftY, rightX, rightY);
            patch.chooseMMLevel(viewPosition);
        }
    }

    /**
	*  Render the landscape to the specified OpenGL rendering context.
	*
	*  @param  gl   The OpenGL rendering context that we are rendering into.
	**/
    public void render(GL gl) {
        gNumTrisRendered = 0;
        gl.glPushMatrix();
        gl.glScalef(gGridSpacing, 1, gGridSpacing);
        gl.glColor3f(1, 1, 1);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        vertexBuffer.rewind();
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertexBuffer);
        if (gDrawMode == kUseTexture) {
            gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
            textureBuffer.rewind();
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, textureBuffer);
        }
        if (useAltShading) {
            gl.glEnableClientState(GL.GL_COLOR_ARRAY);
            colorBuffer.rewind();
            gl.glColorPointer(3, GL.GL_FLOAT, 0, colorBuffer);
        }
        for (int i = 0; i < gNumPatches; ++i) {
            QuadTreeNode patch = gPatches[i];
            patch.render(gl);
        }
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        if (gDrawMode == kUseTexture) gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        if (useAltShading) gl.glDisableClientState(GL.GL_COLOR_ARRAY);
        gl.glPopMatrix();
    }

    /**
	*  Returns the base 2 logarithm of a number as an integer.
	**/
    private static int log2(int number) {
        return (int) (Math.log(number) / Math.log(2));
    }

    /**
	*  This is the interface for a quad-tree node that can either
	*  be a branch or leaf of the quad-tree data structure.
	**/
    private abstract class QuadTreeNode {

        /**
		*  Render the mesh in this quad-tree node.
		**/
        abstract void render(GL gl);

        /**
		*  Set this node's visibility flag based on triangle orientation.
		*  Examines triangles formed by the eye point, the left &
		*  right frustum points and the corners of the node.
		*  This method of frustum culling does not work if the camera can
		*  look up or down significantly.
		*
		*  @param  eyeX, eyeY     The grid coordinates of the eye view point.
		*  @param  leftX, leftY   The grid coordinates of the left frustum point.
		*  @param  rightX, rightY The grid coordinates of the right frustum point.
		**/
        abstract void setVisibility(int eyeX, int eyeY, int leftX, int leftY, int rightX, int rightY);

        /**
		*  Force this quad-tree patch and all of it's children to be visible.
		**/
        abstract void makeVisible();

        /**
		*  Chooses an appropriate mip-map level for all visible nodes.
		*
		*  @param  eyePosition  The world coordinate location of the eye point or
		*                       camera view point [x, y, z].
		**/
        abstract void chooseMMLevel(float[] eyePosition);

        /**
		*  Discover the orientation of a triangle's points:
		*  Taken from "Programming Principles in Computer Graphics",
		*      L. Ammeraal (Wiley)
		**/
        protected final boolean orientation(int pX, int pY, int qX, int qY, int rX, int rY) {
            int aX = qX - pX;
            int aY = qY - pY;
            int bX = rX - pX;
            int bY = rY - pY;
            int d = aX * bY - aY * bX;
            return (d <= 0);
        }
    }

    /**
	*  This class is a quad-tree node (branch) that represents an area or patch
	*  of the terrain which is larger than a single terrain block.
	**/
    private class Patch extends QuadTreeNode {

        private boolean isVisible = false;

        private int offsetX, offsetY, width;

        private QuadTreeNode[] child = new QuadTreeNode[4];

        /**
		*  Construct a Patch quad-tree node for a piece of our landscape.
		*
		*  @param  offsetX   Offset into the height map of the upper left corner
		*                    of this patch (X grid coordinate).
		*  @param  offsetY   Offset into the height map of the upper left corner
		*                    of this patch (Y grid coordinate).
		*  @param  width     The width of this patch in grid coordinates.
		*  @param  blockSize The width of a terrain block in grid coordinates.
		*  @param  kC        A constant used in calculating mip-map distances.
		**/
        Patch(int offsetX, int offsetY, int width, int blockSize, float kC) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            width /= 2;
            if (width > blockSize) {
                child[0] = new Patch(offsetX, offsetY, width, blockSize, kC);
                child[1] = new Patch(offsetX, offsetY + width, width, blockSize, kC);
                child[2] = new Patch(offsetX + width, offsetY + width, width, blockSize, kC);
                child[3] = new Patch(offsetX + width, offsetY, width, blockSize, kC);
            } else {
                int xPos = offsetX / blockSize;
                int yPos = offsetY / blockSize;
                child[0] = gBlocks[yPos][xPos];
                child[1] = gBlocks[yPos + 1][xPos];
                child[2] = gBlocks[yPos + 1][xPos + 1];
                child[3] = gBlocks[yPos][xPos + 1];
            }
        }

        /**
		*  Render the mesh in this quad-tree patch.
		**/
        final void render(GL gl) {
            if (isVisible) {
                for (int i = 0; i < 4; ++i) child[i].render(gl);
            }
        }

        /**
		*  Set patch's visibility flag based on triangle orientation.
		*  Examines triangles formed by the eye point, the left &
		*  right frustum points and the corners of the patch.
		*  This method of frustum culling does not work if the camera can
		*  look up or down significantly.
		*
		*  @param  eyeX, eyeY     The grid coordinates of the eye view point.
		*  @param  leftX, leftY   The grid coordinates of the left frustum point.
		*  @param  rightX, rightY The grid coordinates of the right frustum point.
		**/
        final void setVisibility(int eyeX, int eyeY, int leftX, int leftY, int rightX, int rightY) {
            int rightVis = cornerVisible(eyeX, eyeY, rightX, rightY);
            int leftVis = cornerVisible(leftX, leftY, eyeX, eyeY);
            isVisible = rightVis != 0 && leftVis != 0;
            if (isVisible) {
                if (rightVis == 4 && leftVis == 4) {
                    for (int i = 0; i < 4; ++i) child[i].makeVisible();
                } else {
                    for (int i = 0; i < 4; ++i) child[i].setVisibility(eyeX, eyeY, leftX, leftY, rightX, rightY);
                }
            }
        }

        /**
		*  Force this quad-tree patch and all of it's children to be visible.
		**/
        final void makeVisible() {
            isVisible = true;
            for (int i = 0; i < 4; ++i) child[i].makeVisible();
        }

        /**
		*  Chooses an appropriate mip-map level for all visible terrain blocks.
		*
		*  @param  eyePosition  The world coordinate location of the eye point or
		*                       camera view point [x, y, z].
		**/
        final void chooseMMLevel(float[] eyePosition) {
            if (isVisible) {
                for (int i = 0; i < 4; ++i) child[i].chooseMMLevel(eyePosition);
            }
        }

        /**
		*  Returns the number of corners of this patch that are visible in
		*  the current view frustum.  If none are visible, 0 is returned.
		*  If all the corners are visible, 4 is returned.
		*  Pass in left frustum limit as "eye" and eye point for "right"
		*  to evaluate the left view frustum limit.
		**/
        private final int cornerVisible(int eyeX, int eyeY, int rightX, int rightY) {
            int vis = 0;
            if (orientation(eyeX, eyeY, rightX, rightY, offsetX, offsetY)) ++vis;
            int oYpPS = offsetY + width;
            if (orientation(eyeX, eyeY, rightX, rightY, offsetX, oYpPS)) ++vis;
            int oXpPS = offsetX + width;
            if (orientation(eyeX, eyeY, rightX, rightY, oXpPS, oYpPS)) ++vis;
            if (orientation(eyeX, eyeY, rightX, rightY, oXpPS, offsetY)) ++vis;
            return vis;
        }
    }

    /**
	*  This class is a quad-tree node (leaf) that represents the smallest
	*  piece of the terrain height map that is rendered at a time.
	**/
    private class TerrainBlock extends QuadTreeNode {

        private boolean isVisible = false;

        private int offsetX, offsetY, width;

        private float[] dMin2;

        private float cx, cy, cz;

        private int cLevel;

        private int step;

        private boolean useDetailedTexture = false;

        TerrainBlock north, east, south, west;

        /**
		*  Construct a TerrainBlock quad-tree node for a piece of our landscape.
		*
		*  @param  offsetX   Offset into the height map of the upper left corner
		*                    of this block (X grid coordinate).
		*  @param  offsetY   Offset into the height map of the upper left corner
		*                    of this block (Y grid coordinate).
		*  @param  width     The width of this block in grid coordinates.
		*  @param  kC        A constant used in calculating mip-map distances.
		**/
        TerrainBlock(int offsetX, int offsetY, int width, float kC) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.width = width;
            dMin2 = new float[gMipMapLevels];
            calcDn2(kC);
            findBlockCenter();
        }

        /**
		*  Render the mesh in this terrain block.
		**/
        final void render(GL gl) {
            if (isVisible) {
                if (useDetailedTexture) {
                    gTextureScaleX = gTextureScaleY = 1F / width;
                } else {
                    gTextureScaleX = 1F / gMapXSize;
                    gTextureScaleY = 1F / gMapYSize;
                }
                if (cLevel == gMipMapLevels) renderMaxLevel(gl); else renderBlock(gl);
            }
        }

        /**
		*  Render the maximum mip-map level (lowest resolution level).
		*  This special case is performance optimized.
		**/
        private void renderMaxLevel(GL gl) {
            int idx = 0;
            int tidx = 0;
            int xPos = offsetX;
            int yPos = offsetY;
            int ypStep = yPos + width;
            idx = setVertexAndShading(idx, xPos, yPos);
            idx = setVertexAndShading(idx, xPos, ypStep);
            if (gDrawMode == kUseTexture) {
                tidx = setTextureCoord(tidx, 0, 0, xPos, yPos);
                tidx = setTextureCoord(tidx, 0, width, xPos, ypStep);
            }
            xPos += width;
            idx = setVertexAndShading(idx, xPos, yPos);
            idx = setVertexAndShading(idx, xPos, ypStep);
            if (gDrawMode == kUseTexture) {
                tidx = setTextureCoord(tidx, width, 0, xPos, yPos);
                tidx = setTextureCoord(tidx, width, width, xPos, ypStep);
            }
            gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
            gNumTrisRendered += 2;
        }

        /**
		*  Render the triangles in this terrain block's mesh at the current
		*  geo mip-map level.
		**/
        private void renderBlock(GL gl) {
            boolean fixNorth = (north != null && north.cLevel > cLevel);
            boolean fixSouth = (south != null && south.cLevel > cLevel);
            boolean fixEast = (east != null && east.cLevel > cLevel);
            boolean fixWest = (west != null && west.cLevel > cLevel);
            int xStart = 0;
            int xEnd = width;
            if (fixWest) xStart += step;
            if (fixEast) xEnd -= step;
            int yStart = 0;
            int yEnd = width;
            if (fixNorth) yStart += step;
            if (fixSouth) yEnd -= step;
            int vCount = ((xEnd - xStart) / step + 1) * 2;
            for (int i = yStart; i < yEnd; i += step) {
                int yPos = offsetY + i;
                int idx = 0;
                int tidx = 0;
                int xPos = offsetX + xStart;
                int ipStep = i + step;
                int ypStep = yPos + step;
                for (int j = xStart; j <= xEnd; j += step, xPos += step) {
                    idx = setVertexAndShading(idx, xPos, yPos);
                    idx = setVertexAndShading(idx, xPos, ypStep);
                    if (gDrawMode == kUseTexture) {
                        tidx = setTextureCoord(tidx, j, i, xPos, yPos);
                        tidx = setTextureCoord(tidx, j, ipStep, xPos, ypStep);
                    }
                }
                gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, vCount);
                gNumTrisRendered += vCount - 2;
            }
            if (fixWest) blendWestEdge(gl, fixNorth, fixSouth);
            if (fixEast) blendEastEdge(gl, fixNorth, fixSouth);
            if (fixNorth) blendNorthEdge(gl, fixEast, fixWest);
            if (fixSouth) blendSouthEdge(gl, fixEast, fixWest);
        }

        /**
		*  Set both the vertex in the vertex array and the shading in the color
		*  array.
		*
		*  @param  idx  Index to the next element in the vertex and color arrays.
		*  @param  xPos Horizontal position in the height map.
		*  @param  yPos Vertical position in the height map.
		*  @return The index to the next position in the vertex and color arrays.
		**/
        private int setVertexAndShading(int idx, int xPos, int yPos) {
            int idx2 = idx;
            float alt = gHeightMap.get(xPos, yPos);
            if (alt <= gUndefinedElev) alt = 2 * gUndefinedElev - alt;
            vertexBuffer.put(idx++, xPos);
            vertexBuffer.put(idx++, alt);
            vertexBuffer.put(idx++, yPos);
            if (useAltShading) {
                float shade = 0.2f + (alt - minAlt) * colorFactor;
                colorBuffer.put(idx2++, shade);
                colorBuffer.put(idx2++, shade);
                colorBuffer.put(idx2++, shade);
            }
            return idx;
        }

        /**
		*  Method to set the texture coordinate in the texture coordinate array
		*  to a proper value for the given grid coordinate locations, taking
		*  into account the use of local detailed textures, etc.
		*
		*  @param  idx   Offset into the texture coordinate array for the coordinate
		*                to set.
		*  @param  h     Horizontal position in this terrain block.
		*  @param  v     Vertical position in this terrain block.
		*  @param  xPos  Horizontal position in the height map array.
		*  @param  yPos  Vertical position in the height map array.
		*  @return The index to the next coordinate in the texture coordinate array.
		**/
        private int setTextureCoord(int idx, int h, int v, int xPos, int yPos) {
            if (useDetailedTexture) {
                textureBuffer.put(idx++, h * gTextureScaleX);
                textureBuffer.put(idx++, v * gTextureScaleY);
            } else {
                textureBuffer.put(idx++, xPos * gTextureScaleX);
                textureBuffer.put(idx++, yPos * gTextureScaleY);
            }
            return idx;
        }

        /**
		*  Method that blends in triangles skipped between this block and it's
		*  neighbor to the west.  These triangles were skipped because the neighbor
		*  block has a higher mip map level (is more coarse) than this block.
		**/
        private void blendWestEdge(GL gl, boolean fixNorth, boolean fixSouth) {
            int nStep = west.step;
            int last = width - nStep;
            int jStart = nStep, jEnd = 0;
            for (int i = 0; i < width; i += nStep) {
                int vCount = 0;
                int idx = 0;
                int tidx = 0;
                int xPos = offsetX;
                int yPos = offsetY + i;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, 0, i, xPos, yPos);
                ++vCount;
                yPos += nStep;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, 0, i + nStep, xPos, yPos);
                ++vCount;
                if (i == last && fixSouth) {
                    jStart -= step;
                    yPos -= step;
                }
                xPos = offsetX + step;
                for (int j = jStart; j >= jEnd; j -= step, yPos -= step) {
                    idx = setVertexAndShading(idx, xPos, yPos);
                    tidx = setTextureCoord(tidx, step, j, xPos, yPos);
                    ++vCount;
                }
                if (i == 0 && fixNorth) --vCount;
                gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vCount);
                gNumTrisRendered += vCount - 2;
                jStart += nStep;
                jEnd += nStep;
            }
        }

        /**
		*  Method that blends in triangles skipped between this block and it's
		*  neighbor to the east.  These triangles were skipped because the neighbor
		*  block has a higher mip map level (is more coarse) than this block.
		**/
        private void blendEastEdge(GL gl, boolean fixNorth, boolean fixSouth) {
            int nStep = east.step;
            int jStart = 0, jEnd = nStep;
            int last = width - nStep;
            for (int i = 0; i < width; i += nStep) {
                int vCount = 0;
                int idx = 0;
                int tidx = 0;
                int xPos = offsetX + width;
                int yPos = offsetY + i;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, width, i, xPos, yPos);
                ++vCount;
                if (i == 0 && fixNorth) {
                    jStart += step;
                    yPos += step;
                }
                if (i == last && fixSouth) jEnd -= step;
                int tmp = width - step;
                xPos = offsetX + tmp;
                for (int j = jStart; j <= jEnd; j += step, yPos += step) {
                    idx = setVertexAndShading(idx, xPos, yPos);
                    tidx = setTextureCoord(tidx, tmp, j, xPos, yPos);
                    ++vCount;
                }
                tmp = i + nStep;
                xPos = offsetX + width;
                yPos = offsetY + tmp;
                setVertexAndShading(idx, xPos, yPos);
                setTextureCoord(tidx, width, tmp, xPos, yPos);
                ++vCount;
                gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vCount);
                gNumTrisRendered += vCount - 2;
                if (i == 0 && fixNorth) jStart -= step;
                jStart += nStep;
                jEnd += nStep;
            }
        }

        /**
		*  Method that blends in triangles skipped between this block and it's
		*  neighbor to the north.  These triangles were skipped because the neighbor
		*  block has a higher mip map level (is more coarse) than this block.
		**/
        private void blendNorthEdge(GL gl, boolean fixEast, boolean fixWest) {
            int nStep = north.step;
            int jStart = 0, jEnd = nStep;
            int last = width - nStep;
            for (int i = 0; i < width; i += nStep) {
                int vCount = 0;
                int idx = 0;
                int tidx = 0;
                int xPos = offsetX + i;
                int yPos = offsetY;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, i, 0, xPos, yPos);
                ++vCount;
                if (i == 0 && fixWest) {
                    jStart += step;
                    xPos += step;
                }
                if (i == last && fixEast) {
                    jEnd -= step;
                }
                yPos += step;
                for (int j = jStart; j <= jEnd; j += step, xPos += step) {
                    idx = setVertexAndShading(idx, xPos, yPos);
                    tidx = setTextureCoord(tidx, j, step, xPos, yPos);
                    ++vCount;
                }
                int tmp = i + nStep;
                xPos = offsetX + tmp;
                yPos = offsetY;
                setVertexAndShading(idx, xPos, yPos);
                setTextureCoord(tidx, tmp, 0, xPos, yPos);
                ++vCount;
                gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vCount);
                gNumTrisRendered += vCount - 2;
                if (i == 0 && fixWest) jStart -= step;
                jStart += nStep;
                jEnd += nStep;
            }
        }

        /**
		*  Method that blends in triangles skipped between this block and it's
		*  neighbor to the south.  These triangles were skipped because the neighbor
		*  block has a higher mip map level (is more coarse) than this block.
		**/
        private void blendSouthEdge(GL gl, boolean fixEast, boolean fixWest) {
            int nStep = south.step;
            int last = width - nStep;
            int jStart = nStep, jEnd = 0;
            for (int i = 0; i < width; i += nStep) {
                int vCount = 0;
                int idx = 0;
                int tidx = 0;
                int xPos = offsetX + i;
                int yPos = offsetY + width;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, i, width, xPos, yPos);
                ++vCount;
                xPos += nStep;
                int y = i + nStep;
                idx = setVertexAndShading(idx, xPos, yPos);
                tidx = setTextureCoord(tidx, y, width, xPos, yPos);
                ++vCount;
                if (i == last && fixEast) {
                    jStart -= step;
                    xPos -= step;
                }
                y = width - step;
                yPos -= step;
                for (int j = jStart; j >= jEnd; j -= step, xPos -= step) {
                    idx = setVertexAndShading(idx, xPos, yPos);
                    tidx = setTextureCoord(tidx, j, y, xPos, yPos);
                    ++vCount;
                }
                if (i == 0 && fixWest) --vCount;
                gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, vCount);
                gNumTrisRendered += vCount - 2;
                if (i == last && fixEast) jStart += step;
                jStart += nStep;
                jEnd += nStep;
            }
        }

        /**
		*  Set block's visibility flag based on triangle orientation.
		*  Examines triangles formed by the eye point, the left &
		*  right frustum points and the corners of the block.
		*  This method of frustum culling does not work if the camera can
		*  look up or down significantly.
		*
		*  @param  eyeX, eyeY     The grid coordinates of the eye view point.
		*  @param  leftX, leftY   The grid coordinates of the left frustum point.
		*  @param  rightX, rightY The grid coordinates of the right frustum point.
		**/
        final void setVisibility(int eyeX, int eyeY, int leftX, int leftY, int rightX, int rightY) {
            isVisible = cornerVisible(eyeX, eyeY, rightX, rightY) && cornerVisible(leftX, leftY, eyeX, eyeY);
        }

        /**
		*  Returns true if any one corner of this patch is visible
		*  based on the eye point and the right view frustum limit.
		*  Pass in left frustum limit as "eye" and eye point for "right"
		*  to evaluate the left view frustum limit.
		**/
        private boolean cornerVisible(int eyeX, int eyeY, int rightX, int rightY) {
            if (orientation(eyeX, eyeY, rightX, rightY, offsetX, offsetY)) return true;
            int oYpPS = offsetY + width;
            if (orientation(eyeX, eyeY, rightX, rightY, offsetX, oYpPS)) return true;
            int oXpPS = offsetX + width;
            if (orientation(eyeX, eyeY, rightX, rightY, oXpPS, oYpPS)) return true;
            if (orientation(eyeX, eyeY, rightX, rightY, oXpPS, offsetY)) return true;
            return false;
        }

        /**
		*  Force this terrain block to be visible.
		**/
        final void makeVisible() {
            isVisible = true;
        }

        /**
		*  Chooses an appropriate mip-map level for all visible terrain blocks.
		*
		*  @param  eyePosition  The world coordinate location of the eye point or
		*                       camera view point [x, y, z].
		**/
        final void chooseMMLevel(float[] eyePosition) {
            if (isVisible) {
                float dx = eyePosition[0] - cx;
                float dy = eyePosition[1] - cy;
                float dz = eyePosition[2] - cz;
                float d2 = dx * dx + dy * dy + dz * dz;
                cLevel = 0;
                step = 1;
                int maxLevel = gMipMapLevels - 1;
                while (cLevel < maxLevel && d2 > dMin2[cLevel + 1]) {
                    ++cLevel;
                    step *= 2;
                }
            }
        }

        /**
		*  Change the threshold value used for choosing the appropriate
		*  mip-map level.
		**/
        final void changeThreshold(int tau) {
            float factor = gThreshold * gThreshold / (float) (tau * tau);
            int numLevels = dMin2.length;
            for (int level = 1; level < numLevels; ++level) {
                dMin2[level] *= factor;
            }
        }

        /**
		*  Calculates dMin^2 for each mip map level in this terrain block.
		**/
        private void calcDn2(float kC) {
            kC *= kC;
            dMin2[0] = 0;
            float deltaMax = 0;
            int numLevels = dMin2.length;
            for (int level = 1; level < numLevels; ++level) {
                int step = 2;
                int i = 1;
                for (; i < level; ++i) step *= 2;
                int endY = width + offsetY;
                int endX = width + offsetX;
                for (i = offsetY; i < endY; ++i) {
                    int posY = i / step;
                    posY *= step;
                    for (int j = offsetX; j < endX; ++j) {
                        if (i % step != 0 || j % step != 0) {
                            int posX = j / step;
                            posX *= step;
                            float delta = biLinearInterp(posX, posX + step, posY, posY + step, j, i);
                            if (delta > gUndefinedElev) {
                                float elev = gHeightMap.get(j, i);
                                if (elev > gUndefinedElev) delta -= elev; else delta = 0;
                            } else delta = 0;
                            delta = Math.abs(delta);
                            deltaMax = Math.max(deltaMax, delta);
                        }
                    }
                }
                dMin2[level] = deltaMax * deltaMax * kC;
            }
        }

        /**
		*  Interpolate a height value out of the height map for the given indexes.
		**/
        private float biLinearInterp(int lx, int hx, int ly, int hy, int tx, int ty) {
            float s00 = gHeightMap.get(lx, ly);
            float s01 = gHeightMap.get(hx, ly);
            float s10 = gHeightMap.get(lx, hy);
            float s11 = gHeightMap.get(hx, hy);
            float value = gUndefinedElev;
            if (s00 > gUndefinedElev && s01 > gUndefinedElev && s10 > gUndefinedElev && s11 > gUndefinedElev) {
                int dx = hx - lx;
                int dtx = tx - lx;
                float v0 = (s01 - s00) / dx * dtx + s00;
                float v1 = (s11 - s10) / dx * dtx + s10;
                value = (v1 - v0) / (hy - ly) * (ty - ly) + v0;
            }
            return value;
        }

        /**
		*  Finds the 3D center point of this terrain block in world coordinates.
		**/
        private void findBlockCenter() {
            float minAlt = Float.MAX_VALUE;
            float maxAlt = -minAlt;
            int endY = width + offsetY;
            int endX = width + offsetX;
            for (int i = offsetY; i < endY; ++i) {
                for (int j = offsetX; j < endX; ++j) {
                    float alt = gHeightMap.get(j, i);
                    if (alt <= gUndefinedElev) alt = 2 * gUndefinedElev - alt;
                    minAlt = Math.min(minAlt, alt);
                    maxAlt = Math.max(maxAlt, alt);
                }
            }
            cy = (minAlt + maxAlt) / 2;
            float wo2 = width * gGridSpacing / 2;
            cx = offsetX * gGridSpacing + wo2;
            cz = offsetY * gGridSpacing + wo2;
        }
    }
}
