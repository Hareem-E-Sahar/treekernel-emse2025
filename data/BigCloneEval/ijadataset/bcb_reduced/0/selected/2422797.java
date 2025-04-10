package org.jmol.viewer;

import org.jmol.util.Bmp;
import org.jmol.g3d.Graphics3D;
import javax.vecmath.*;

class SasFlattenedPointList {

    Graphics3D g3d;

    int geodesicLevel;

    int count;

    short[] vertexes = new short[32];

    float[] angles = new float[32];

    float[] distances = new float[32];

    SasFlattenedPointList(Graphics3D g3d, int geodesicLevel) {
        this.g3d = g3d;
        this.geodesicLevel = geodesicLevel;
    }

    private static final float PI = (float) Math.PI;

    void reset() {
        count = 0;
    }

    void generateTorusSegment(short startingVertex, short vertexIncrement, float startingAngle, float angleIncrement, int segmentCount) {
        if (vertexes.length < segmentCount) {
            vertexes = Util.ensureLength(vertexes, segmentCount);
            angles = Util.ensureLength(angles, segmentCount);
            distances = Util.ensureLength(distances, segmentCount);
        }
        count = segmentCount;
        short vertex = startingVertex;
        float angle = startingAngle;
        for (int i = 0; i < segmentCount; ++i) {
            vertexes[i] = vertex;
            vertex += vertexIncrement;
            angles[i] = angle;
            angle += angleIncrement;
            distances[i] = 0;
        }
    }

    void add(short vertex, float angle, float distance) {
        if (count == vertexes.length) {
            vertexes = Util.doubleLength(vertexes);
            angles = Util.doubleLength(angles);
            distances = Util.doubleLength(distances);
        }
        vertexes[count] = vertex;
        angles[count] = angle;
        distances[count] = distance;
        ++count;
    }

    void duplicateFirstPointPlus2Pi() {
        add(vertexes[0], angles[0] + 2 * PI, distances[0]);
    }

    void sort() {
        for (int i = count; --i > 0; ) for (int j = i; --j >= 0; ) if (angles[j] > angles[i]) {
            Util.swap(angles, i, j);
            Util.swap(distances, i, j);
            Util.swap(vertexes, i, j);
        }
    }

    int find(float angle) {
        int i;
        for (i = 0; i < count; ++i) if (angles[i] >= angle) return i;
        return -1;
    }

    int findGE(float angle) {
        int min = 0;
        int max = count;
        while (min != max) {
            int mid = (min + max) / 2;
            float midAngle = angles[mid];
            if (midAngle < angle) min = mid + 1; else max = mid;
        }
        return min;
    }

    int findGT(float angle) {
        int min = 0;
        int max = count;
        while (min != max) {
            int mid = (min + max) / 2;
            float midAngle = angles[mid];
            if (midAngle <= angle) min = mid + 1; else max = mid;
        }
        return min;
    }

    final Point3f vertexPointT = new Point3f();

    final Vector3f vertexVectorT = new Vector3f();

    final Point3f projectedPointT = new Point3f();

    final Vector3f projectedVectorT = new Vector3f();

    void setGeodesicEdge(Point3f geodesicCenter, float geodesicRadius, Point3f planeCenter, Vector3f axisUnitVector, Point3f planeZeroPoint, boolean fullTorus, Vector3f vector0, Vector3f vector90, Vector3f[] geodesicVertexVectors, int[] edgeMap) {
        float radiansPerAngstrom = PI / geodesicRadius;
        count = 0;
        for (int v = -1; (v = Bmp.nextSetBit(edgeMap, v + 1)) >= 0; ) {
            vertexPointT.scaleAdd(geodesicRadius, geodesicVertexVectors[v], geodesicCenter);
            vertexVectorT.sub(vertexPointT, planeCenter);
            float distance = axisUnitVector.dot(vertexVectorT);
            projectedPointT.scaleAdd(-distance, axisUnitVector, vertexPointT);
            projectedVectorT.sub(projectedPointT, planeCenter);
            float angle = calcAngleInThePlane(vector0, vector90, projectedVectorT);
            add((short) v, angle, distance * radiansPerAngstrom);
        }
        sort();
        if (fullTorus) duplicateFirstPointPlus2Pi();
    }

    static float calcAngleInThePlane(Vector3f radialVector0, Vector3f radialVector90, Vector3f vectorInQuestion) {
        float angle = radialVector0.angle(vectorInQuestion);
        float angle90 = radialVector90.angle(vectorInQuestion);
        if (angle90 > PI / 2) angle = 2 * PI - angle;
        return angle;
    }

    void buildForStitching(float startingAngle, float endingAngle, SasFlattenedPointList fplIdeal, SasFlattenedPointList fplActual, SasFlattenedPointList fplVisibleIdeal, boolean dump) {
        int minVisibleIdeal = fplVisibleIdeal.findGE(startingAngle);
        int maxVisibleIdeal = fplVisibleIdeal.findGT(endingAngle);
        if (dump) {
            System.out.println("buildForStitching(" + startingAngle + "," + endingAngle + ",...)");
            System.out.println("fplVisibleIdeal=");
            fplVisibleIdeal.dump();
            System.out.println("minVisibleIdeal=" + minVisibleIdeal);
            System.out.println("maxVisibleIdeal=" + maxVisibleIdeal);
        }
        count = 0;
        if (minVisibleIdeal == maxVisibleIdeal) return;
        for (int i = minVisibleIdeal; i < maxVisibleIdeal; ++i) {
            add(fplVisibleIdeal.vertexes[i], fplVisibleIdeal.angles[i], fplVisibleIdeal.distances[i]);
        }
    }

    void dump() {
        System.out.println(" SasFlattenedPointList.dump() count=" + count);
        for (int i = 0; i < count; ++i) {
            System.out.println(" " + i + ":" + angles[i] + "," + vertexes[i] + "," + distances[i]);
        }
    }
}
