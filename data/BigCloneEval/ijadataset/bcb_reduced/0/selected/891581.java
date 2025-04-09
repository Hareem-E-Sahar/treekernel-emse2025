package visual3d.shape.wireframe;

import visual3d.datastruct.*;
import visual3d.mesh.ImplicitMesh;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 20.07.2004
 * Time: 12:13:30
 * To change this template use File | Settings | File Templates.
 */
public class MarchingCubes extends WireframeShape {

    private Mesh mesh;

    private int meshSize;

    private int curvesNum;

    private double threshold[];

    private visual3d.datastruct.Point draw_base, draw_x, draw_y;

    private visual3d.datastruct.Point unit_x, unit_y, unit_z;

    private double unit_width;

    public void init() {
        ImplicitMesh metaBallMesh = new ImplicitMesh(20);
        mesh = metaBallMesh;
        meshSize = mesh.getMeshSize();
        double aTh = 0.01;
        unit_width = 2.0 / (double) meshSize;
        unit_x = new visual3d.datastruct.Point(unit_width, 0.0, 0.0);
        unit_y = new visual3d.datastruct.Point(0.0, unit_width, 0.0);
        unit_z = new visual3d.datastruct.Point(0.0, 0.0, unit_width);
        if (aTh == 0.001) {
            curvesNum = 10;
            threshold = new double[curvesNum];
            for (int i = 0; i < curvesNum; i++) {
                threshold[i] = (double) (i + 1) / (double) (curvesNum + 1);
            }
        } else {
            curvesNum = 1;
            threshold = new double[curvesNum];
            threshold[0] = aTh;
        }
        for (int i = 0; i < curvesNum; i++) {
            addContourCurve(threshold[i]);
        }
        super.init();
    }

    private void addContourCurve(double aThreshold) {
        int x, y, z;
        for (z = 0; z < meshSize; z++) for (y = 0; y < meshSize; y++) for (x = 0; x < meshSize; x++) addOneBox(x, y, z, aThreshold);
    }

    private void addOneBox(int x, int y, int z, double aThreshold) {
        double mV[] = new double[8];
        mV[0] = mesh.getMeshValueAt(x, y, z);
        mV[1] = mesh.getMeshValueAt(x + 1, y, z);
        mV[2] = mesh.getMeshValueAt(x, y + 1, z);
        mV[3] = mesh.getMeshValueAt(x + 1, y + 1, z);
        mV[4] = mesh.getMeshValueAt(x, y, z + 1);
        mV[5] = mesh.getMeshValueAt(x + 1, y, z + 1);
        mV[6] = mesh.getMeshValueAt(x, y + 1, z + 1);
        draw_base = new visual3d.datastruct.Point((double) x * unit_width - 1.0, (double) y * unit_width - 1.0, (double) z * unit_width - 1.0);
        draw_x = unit_x;
        draw_y = unit_y;
        addOneMesh(mV[0], mV[1], mV[2], mV[3], 0.0, 0.0, 1.0, 1.0, aThreshold);
        draw_x = unit_x;
        draw_y = unit_z;
        addOneMesh(mV[0], mV[1], mV[4], mV[5], 0.0, 0.0, 1.0, 1.0, aThreshold);
        draw_x = unit_y;
        draw_y = unit_z;
        addOneMesh(mV[0], mV[2], mV[4], mV[6], 0.0, 0.0, 1.0, 1.0, aThreshold);
    }

    private void addLine(double x0, double y0, double x1, double y1) {
        visual3d.datastruct.Point p0, p1;
        p0 = new visual3d.datastruct.Point(draw_base.x + (draw_x.x * x0 + draw_y.x * y0), draw_base.y + (draw_x.y * x0 + draw_y.y * y0), draw_base.z + (draw_x.z * x0 + draw_y.z * y0));
        p1 = new visual3d.datastruct.Point(draw_base.x + (draw_x.x * x1 + draw_y.x * y1), draw_base.y + (draw_x.y * x1 + draw_y.y * y1), draw_base.z + (draw_x.z * x1 + draw_y.z * y1));
        boolean stop0 = false;
        boolean stop1 = false;
        for (int i = getListOfEdges().size() - 1; i >= 0; i--) {
            i = i == -1 ? 0 : i;
            if (!(stop0 || stop1)) {
                Point leftP = ((Edge) getListOfEdges().get(i)).getLeftPoint();
                Point RightP = ((Edge) getListOfEdges().get(i)).getRightPoint();
                if (leftP.x == p0.x && leftP.y == p0.y && leftP.z == p0.z) {
                    p0 = leftP;
                    stop0 = true;
                }
                if (RightP.x == p0.x && RightP.y == p0.y && RightP.z == p0.z) {
                    p0 = RightP;
                    stop0 = true;
                }
                if (leftP.x == p1.x && leftP.y == p1.y && leftP.z == p1.z) {
                    p1 = leftP;
                    stop1 = true;
                }
                if (RightP.x == p1.x && RightP.y == p1.y && RightP.z == p1.z) {
                    p1 = RightP;
                    stop1 = true;
                }
            } else {
                break;
            }
        }
        addEdge(new Edge(p0, p1));
    }

    private void addOneMesh(double z00, double z01, double z10, double z11, double x0, double y0, double x1, double y1, double aThreshold) {
        int cellCase;
        cellCase = calcCellCase(z00, z01, z10, z11, aThreshold);
        switch(cellCase) {
            case 1:
            case 2:
            case 4:
            case 8:
            case 7:
            case 11:
            case 13:
            case 14:
                addMeshCornerLine(cellCase, z00, z01, z10, z11, x0, y0, x1, y1, aThreshold);
                break;
            case 3:
            case 5:
            case 10:
            case 12:
                addMeshCrossLine(cellCase, z00, z01, z10, z11, x0, y0, x1, y1, aThreshold);
                break;
            case 9:
            case 6:
                double z0m, zm0, zmm, zm1, z1m, xm, ym;
                z0m = (z00 + z01) / 2;
                zm0 = (z00 + z10) / 2;
                zmm = (z00 + z01 + z10 + z11) / 4;
                zm1 = (z01 + z11) / 2;
                z1m = (z10 + z11) / 2;
                xm = (x0 + x1) / 2;
                ym = (y0 + y1) / 2;
                addOneMesh(z00, z0m, zm0, zmm, x0, y0, xm, ym, aThreshold);
                addOneMesh(z0m, z01, zmm, zm1, xm, y0, x1, ym, aThreshold);
                addOneMesh(zm0, zmm, z10, z1m, x0, ym, xm, y1, aThreshold);
                addOneMesh(zmm, zm1, z1m, z11, xm, ym, x1, y1, aThreshold);
                break;
            case 0:
            case 15:
                break;
        }
    }

    private void addMeshCornerLine(int aCase, double z00, double z01, double z10, double z11, double x0, double y0, double x1, double y1, double aThreshold) {
        double temp;
        if ((aCase == 7) || (aCase > 8)) aCase = 0xf ^ aCase;
        if ((aCase == 2) || (aCase == 8)) {
            temp = z00;
            z00 = z01;
            z01 = temp;
            temp = z10;
            z10 = z11;
            z11 = temp;
            temp = x0;
            x0 = x1;
            x1 = temp;
        }
        if ((aCase == 4) || (aCase == 8)) {
            temp = z00;
            z00 = z10;
            z10 = temp;
            temp = z01;
            z01 = z11;
            z11 = temp;
            temp = y0;
            y0 = y1;
            y1 = temp;
        }
        z00 = Math.abs(z00 - aThreshold);
        z01 = Math.abs(z01 - aThreshold);
        z10 = Math.abs(z10 - aThreshold);
        addLine((x0 * z01 + x1 * z00) / (z00 + z01), y0, x0, (y0 * z10 + y1 * z00) / (z00 + z10));
    }

    private void addMeshCrossLine(int aCase, double z00, double z01, double z10, double z11, double x0, double y0, double x1, double y1, double aThreshold) {
        if (aCase > 5) aCase = 0xf ^ aCase;
        z00 = Math.abs(z00 - aThreshold);
        z01 = Math.abs(z01 - aThreshold);
        z10 = Math.abs(z10 - aThreshold);
        z11 = Math.abs(z11 - aThreshold);
        if (aCase == 5) {
            addLine((x0 * z01 + x1 * z00) / (z00 + z01), y0, (x0 * z11 + x1 * z10) / (z10 + z11), y1);
        } else {
            addLine(x0, (y0 * z10 + y1 * z00) / (z00 + z10), x1, (y0 * z11 + y1 * z01) / (z01 + z11));
        }
    }

    private int calcCellCase(double z00, double z01, double z10, double z11, double aThreshold) {
        int n = 0;
        if (z00 > aThreshold) n += 1;
        if (z01 > aThreshold) n += 2;
        if (z10 > aThreshold) n += 4;
        if (z11 > aThreshold) n += 8;
        return n;
    }
}
