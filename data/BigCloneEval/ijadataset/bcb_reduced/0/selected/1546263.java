package com.ecm.graphics.render.flatFaceRender;

import java.util.LinkedHashMap;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import com.ecm.graphics.data.GraphData;
import com.ecm.graphics.tools.ColorTable;

public class Graph3dFlatFaceRender extends TransformGroup implements DataChanger {

    private int xWidth;

    private int zDepth;

    private LinkedHashMap tempFaceHash = new LinkedHashMap();

    private ValuePlane[][] faces;

    private float fudgedScaleYMax;

    private TransformGroup wallTG;

    public Graph3dFlatFaceRender(float fudgedScaleYMax) {
        this.xWidth = GraphData.getXWidth();
        this.zDepth = GraphData.getZDepth();
        this.fudgedScaleYMax = fudgedScaleYMax;
        this.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        this.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        this.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        this.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        this.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        faces = getFaces();
        wallTG = buildWalls();
        this.addChild(wallTG);
        this.addChild(buildFaces());
    }

    /**
	 * Build up all the 2d faces
	 * 
	 * @return
	 */
    private TransformGroup buildFaces() {
        TransformGroup facesTransformGroup = new TransformGroup();
        for (int i = 0; i < zDepth; i++) {
            for (int j = 0; j < xWidth; j++) {
                facesTransformGroup.addChild(faces[j][i]);
            }
        }
        return facesTransformGroup;
    }

    /**
	 * Return a plane at the x/z position
	 * Helper method
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
    private ValuePlane getValueFace(int x, int z) {
        float thickness = 0;
        Point3f A = new Point3f(x + thickness, 0, -z - thickness);
        Point3f B = new Point3f(x + 1 - thickness, 0, -z - thickness);
        Point3f C = new Point3f(x + 1 - thickness, 0, -(z + 1 - thickness));
        Point3f D = new Point3f(x + thickness, 0, -(z + 1 - thickness));
        ValuePlane plane = new ValuePlane(A, B, C, D, GraphData.getCellValue(x, z), x, z, this.fudgedScaleYMax, this);
        return plane;
    }

    /**
	 * Populate faces array
	 * 
	 * @return
	 */
    private ValuePlane[][] getFaces() {
        ValuePlane[][] tempFaces = new ValuePlane[xWidth][zDepth];
        for (int i = 0; i < zDepth; i++) {
            for (int j = 0; j < xWidth; j++) {
                tempFaces[j][i] = getValueFace(j, i);
                GraphData.addGraphDataCellListener(j, i, tempFaces[j][i]);
            }
        }
        return tempFaces;
    }

    /**
	 * Return a plane at the x/z position
	 * 
	 * @param x
	 * @param z
	 * @return
	 */
    private TransformGroup buildWalls() {
        TransformGroup wallTG = new TransformGroup();
        wallTG.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
        wallTG.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        wallTG.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        for (int x = 0; x < this.xWidth; x++) {
            for (int z = 0; z < this.zDepth; z++) {
                ValuePlane plane1 = this.faces[x][z];
                BranchGroup tempBG = buildWallBG(x, z, plane1);
                plane1.setWallBG(tempBG);
                wallTG.addChild(tempBG);
            }
        }
        return wallTG;
    }

    public void modifyHeight(int x, int z, float value, int totalCount) {
        this.tempFaceHash.put(x + "," + z, new MyPoint(x, z));
        if (this.tempFaceHash.size() == totalCount) {
            for (int i = 0; i < zDepth; i++) {
                for (int j = 0; j < xWidth; j++) {
                    MyPoint tempPoint = (MyPoint) this.tempFaceHash.get(j + "," + i);
                    if (tempPoint != null) {
                        ValuePlane vp = faces[j][i];
                        if (vp.getWallBG() != null) {
                            int bgIndex = this.wallTG.indexOfChild(vp.getWallBG());
                            BranchGroup newWallBG = this.buildWallBG(tempPoint.x, tempPoint.z, vp);
                            vp.setWallBG(newWallBG);
                            this.wallTG.setChild(newWallBG, bgIndex);
                        }
                    }
                }
            }
            for (int i = 0; i < zDepth; i++) {
                for (int j = 0; j < xWidth; j++) {
                    MyPoint tempPoint = (MyPoint) this.tempFaceHash.get(j + "," + i);
                    if (tempPoint == null) {
                        if (this.hasSelectedNeighbor(j, i)) {
                            ValuePlane vpb = faces[j][i];
                            if (vpb.getWallBG() != null) {
                                int bgIndex = this.wallTG.indexOfChild(vpb.getWallBG());
                                BranchGroup newWallBG = this.buildWallBG(j, i, vpb);
                                vpb.setWallBG(newWallBG);
                                this.wallTG.setChild(newWallBG, bgIndex);
                            }
                        }
                    }
                }
            }
            this.tempFaceHash.clear();
        }
    }

    private boolean hasSelectedNeighbor(int x, int z) {
        if (x > 0) {
            Object temp = this.tempFaceHash.get((int) (x - 1) + "," + z);
            if (temp != null) {
                return true;
            }
        }
        if (x < this.xWidth) {
            Object temp = this.tempFaceHash.get((int) (x + 1) + "," + z);
            if (temp != null) {
                return true;
            }
        }
        if (z < this.zDepth) {
            Object temp = this.tempFaceHash.get(x + "," + (int) (z + 1));
            if (temp != null) {
                return true;
            }
        }
        if (z > 0) {
            Object temp = this.tempFaceHash.get(x + "," + (int) (z - 1));
            if (temp != null) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Internal class to help with keeping track of points when updating face walls
	 * 
	 * @author botman
	 *
	 */
    private class MyPoint {

        public int x;

        public int z;

        public MyPoint(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public boolean areNeighbors(MyPoint a, MyPoint b) {
            if (a.x == b.x) {
                if (Math.abs(a.z - b.z) == 1) {
                    return true;
                }
            }
            if (a.z == b.z) {
                if (Math.abs(a.x - b.x) == 1) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
	 * Build a wall branch group
	 * 
	 * @param x
	 * @param z
	 * @param plane1
	 * @return
	 */
    private BranchGroup buildWallBG(int x, int z, ValuePlane plane1) {
        BranchGroup tempBG = new BranchGroup();
        tempBG.setCapability(BranchGroup.ALLOW_DETACH);
        if (x != 0) {
            ValuePlane plane2 = this.faces[x - 1][z];
            Plane2d plane = buildSideWall(plane1, plane2);
            tempBG.addChild(plane);
        }
        if (z != 0) {
            ValuePlane plane2 = this.faces[x][z - 1];
            Plane2d plane = buildBottomWall(plane1, plane2);
            tempBG.addChild(plane);
        }
        return tempBG;
    }

    /**
	 * Build a bottom wall
	 * 
	 * @param plane1
	 * @param plane2
	 * @return
	 */
    private Plane2d buildBottomWall(ValuePlane plane1, ValuePlane plane2) {
        float value1Temp = plane1.getModifiedYValue();
        float value2Temp = plane2.getModifiedYValue();
        Point3f A = plane1.getA();
        Point3f B = plane1.getB();
        Point3f C = plane2.getC();
        Point3f D = plane2.getD();
        float[] tempA = new float[3];
        A.get(tempA);
        tempA[1] = value1Temp;
        A.set(tempA);
        float[] tempB = new float[3];
        B.get(tempB);
        tempB[1] = value1Temp;
        B.set(tempB);
        float[] tempC = new float[3];
        C.get(tempC);
        tempC[1] = value2Temp;
        C.set(tempC);
        float[] tempD = new float[3];
        D.get(tempD);
        tempD[1] = value2Temp;
        D.set(tempD);
        float value1 = plane1.getTheValue();
        float value2 = plane2.getTheValue();
        float aveValue = (value1 + value2) / 2;
        Color3f theColor = ColorTable.getColor(aveValue);
        Plane2d plane = new Plane2d(A, B, C, D, theColor, false, 0, 0, 0.0f, true);
        return plane;
    }

    /**
	 * Build a side wall
	 * 
	 * @param plane1
	 * @param plane2
	 * @return
	 */
    private Plane2d buildSideWall(ValuePlane plane1, ValuePlane plane2) {
        float value1Temp = plane1.getModifiedYValue();
        float value2Temp = plane2.getModifiedYValue();
        Point3f A = plane1.getD();
        Point3f B = plane1.getA();
        Point3f C = plane2.getB();
        Point3f D = plane2.getC();
        float[] tempA = new float[3];
        A.get(tempA);
        tempA[1] = value1Temp;
        A.set(tempA);
        float[] tempB = new float[3];
        B.get(tempB);
        tempB[1] = value1Temp;
        B.set(tempB);
        float[] tempC = new float[3];
        C.get(tempC);
        tempC[1] = value2Temp;
        C.set(tempC);
        float[] tempD = new float[3];
        D.get(tempD);
        tempD[1] = value2Temp;
        D.set(tempD);
        float value1 = plane1.getTheValue();
        float value2 = plane2.getTheValue();
        float aveValue = (value1 + value2) / 2;
        Color3f theColor = ColorTable.getColor(aveValue);
        Plane2d plane = new Plane2d(A, B, C, D, theColor, false, 0, 0, 0.0f, true);
        return plane;
    }
}
