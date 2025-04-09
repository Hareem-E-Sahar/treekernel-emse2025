package trashcan.sds;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.*;
import jpatch.boundary.settings.RealtimeRendererSettings;
import jpatch.boundary.settings.Settings;
import static java.lang.Math.*;
import static trashcan.sds.SdsConstants.*;
import static trashcan.sds.SdsWeights.*;

public class Dicer {

    private static RealtimeRendererSettings RENDERER_SETTINGS = Settings.getInstance().realtimeRenderer;

    static final int UNUSED = 0;

    static final int EDGE = 1;

    static final int EDGE_H = 2;

    static final int EDGE_V = 3;

    static final int FACE = 4;

    static final int POINT = 5;

    static final int CREASE_4_5 = 6;

    static final int CREASE_4_6 = 7;

    static final int CREASE_4_7 = 8;

    static final int CREASE_5_6 = 9;

    static final int CREASE_5_7 = 10;

    static final int CREASE_6_7 = 11;

    private static final int MAX_FAN_LENGTH = MAX_VALENCE * 2 - 5;

    public static final int GRID_START = MAX_FAN_LENGTH * 2;

    private final float[][][] subdivPoints = new float[MAX_SUBDIV][][];

    private final float[][][] limitPoints = new float[MAX_SUBDIV][][];

    private final float[][][] limitNormals = new float[MAX_SUBDIV][][];

    private final int[][][] patchStencil = new int[MAX_SUBDIV][][];

    private final int[][][][][] fanStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][2][][];

    private final int[][][][] cornerStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][2][];

    private final int[][][] patchLimitStencil = new int[MAX_SUBDIV][][];

    private final int[][][][] cornerLimitStencil = new int[MAX_SUBDIV][MAX_VALENCE - 2][2][];

    private static final int[] CORNER_INDEXES = new int[] { 5, 6, 10, 9 };

    int[][][] rim0 = new int[MAX_SUBDIV][][];

    int[][][] rim1 = new int[MAX_SUBDIV][][];

    int[][][][] rimTriangles = new int[MAX_SUBDIV][MAX_SUBDIV][][];

    int[][][][] rimTriangleNormals = new int[MAX_SUBDIV][MAX_SUBDIV][][];

    public Dicer() {
        for (int level = 0; level < MAX_SUBDIV; level++) {
            final int dim = ((1 << level)) + 3;
            if (level == 0) {
                rim0[0] = new int[4][2];
                rim0[0][0] = new int[] { 5, 6 };
                rim0[0][1] = new int[] { 6, 10 };
                rim0[0][2] = new int[] { 10, 9 };
                rim0[0][3] = new int[] { 9, 5 };
                rim1[0] = new int[0][];
                rimTriangles[0][0] = new int[4][0];
                rimTriangleNormals[0][0] = new int[4][0];
            } else {
                rim0[level] = new int[4][dim - 2];
                for (int i = 0; i < (dim - 2); i++) {
                    rim0[level][0][i] = dim + i + 1;
                    rim0[level][1][i] = dim + dim - 2 + (dim * i);
                    rim0[level][2][i] = dim * dim - dim - 2 - i;
                    rim0[level][3][i] = dim * dim - 2 * dim - (dim * i) + 1;
                }
                rim1[level] = new int[4][dim - 4];
                for (int i = 0; i < (dim - 4); i++) {
                    rim1[level][0][i] = 2 * dim + i + 2;
                    rim1[level][1][i] = 2 * dim + dim - 3 + (dim * i);
                    rim1[level][2][i] = dim * dim - 2 * dim - 3 - i;
                    rim1[level][3][i] = dim * dim - 3 * dim - (dim * i) + 2;
                }
                for (int pairLevel = level; pairLevel >= 0; pairLevel--) {
                    rimTriangles[level][pairLevel] = new int[4][];
                    rimTriangleNormals[level][pairLevel] = new int[4][];
                    int[] tmpTriangles = new int[dim * 6];
                    int[] tmpNormals = new int[dim * 6];
                    for (int side = 0; side < 4; side++) {
                        int j = 0;
                        int levelDelta = level - pairLevel;
                        if (levelDelta < 0) {
                            continue;
                        }
                        int step = 1 << levelDelta;
                        int correction = step == 1 ? 0 : -1;
                        for (int i = 0; i < (dim - 2 - 1 * step); i += step) {
                            int ii = i + step / 2 + correction;
                            int innerNormal = ((side + 2) % 4) * 3;
                            if (ii >= rim1[level][side].length) {
                                ii = rim1[level][side].length - 1;
                                innerNormal = ((side + 3) % 4) * 3;
                            }
                            tmpNormals[j] = side * 3;
                            tmpTriangles[j++] = rim0[level][side][i] + GRID_START;
                            tmpNormals[j] = ((side + 1) % 4) * 3;
                            tmpTriangles[j++] = rim0[level][side][i + step] + GRID_START;
                            tmpNormals[j] = innerNormal;
                            tmpTriangles[j++] = rim1[level][side][ii] + GRID_START;
                        }
                        for (int i = 0; i < (dim - 5); i++) {
                            int ii = ((i + (step / 2) + 1) / step) * step;
                            int outerNormal = ((i + 1) % step < step / 2) ? side * 3 : ((side + 1) % 4) * 3;
                            tmpNormals[j] = outerNormal;
                            tmpTriangles[j++] = rim0[level][side][ii] + GRID_START;
                            tmpNormals[j] = ((side + 2) % 4) * 3;
                            tmpTriangles[j++] = rim1[level][side][i + 1] + GRID_START;
                            tmpNormals[j] = ((side + 3) % 4) * 3;
                            tmpTriangles[j++] = rim1[level][side][i] + GRID_START;
                        }
                        rimTriangles[level][pairLevel][side] = new int[j];
                        rimTriangleNormals[level][pairLevel][side] = new int[j];
                        System.arraycopy(tmpTriangles, 0, rimTriangles[level][pairLevel][side], 0, j);
                        System.arraycopy(tmpNormals, 0, rimTriangleNormals[level][pairLevel][side], 0, j);
                    }
                }
            }
            subdivPoints[level] = new float[dim * dim + GRID_START][3];
            limitPoints[level] = new float[dim * dim + GRID_START][3];
            limitNormals[level] = new float[dim * dim + GRID_START][12];
            for (int valence = 3; valence <= MAX_VALENCE; valence++) {
                for (int corner = 0; corner < 2; corner++) {
                    cornerStencil[level][valence - 3][corner] = new int[valence * 2 + 6];
                    cornerStencil[level][valence - 3][corner][0] = 0;
                    cornerStencil[level][valence - 3][corner][1] = 0;
                    cornerStencil[level][valence - 3][corner][2] = 0;
                    cornerStencil[level][valence - 3][corner][3] = 0;
                    cornerStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 1, 1);
                    cornerStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level, 1, 1);
                    cornerStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level, 0, 2);
                    cornerStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level, 1, 2);
                    cornerStencil[level][valence - 3][corner][8] = patchCornerIndex(corner, level, 2, 2);
                    cornerStencil[level][valence - 3][corner][9] = patchCornerIndex(corner, level, 2, 1);
                    cornerStencil[level][valence - 3][corner][10] = patchCornerIndex(corner, level, 2, 0);
                    for (int i = 0, n = valence * 2 - 5; i < n; i++) {
                        int index = i + 1;
                        if (index == n) {
                            index = 0;
                        }
                        cornerStencil[level][valence - 3][corner][i + 11] = cornerIndex(corner, valence, index);
                    }
                    cornerLimitStencil[level][valence - 3][corner] = new int[valence * 2 + 6];
                    cornerLimitStencil[level][valence - 3][corner][0] = 0;
                    cornerLimitStencil[level][valence - 3][corner][1] = 0;
                    cornerLimitStencil[level][valence - 3][corner][2] = 0;
                    cornerLimitStencil[level][valence - 3][corner][3] = 0;
                    cornerLimitStencil[level][valence - 3][corner][4] = patchCornerIndex(corner, level + 1, 1, 1);
                    cornerLimitStencil[level][valence - 3][corner][5] = patchCornerIndex(corner, level + 1, 1, 1);
                    cornerLimitStencil[level][valence - 3][corner][6] = patchCornerIndex(corner, level + 1, 0, 2);
                    cornerLimitStencil[level][valence - 3][corner][7] = patchCornerIndex(corner, level + 1, 1, 2);
                    cornerLimitStencil[level][valence - 3][corner][8] = patchCornerIndex(corner, level + 1, 2, 2);
                    cornerLimitStencil[level][valence - 3][corner][9] = patchCornerIndex(corner, level + 1, 2, 1);
                    cornerLimitStencil[level][valence - 3][corner][10] = patchCornerIndex(corner, level + 1, 2, 0);
                    for (int i = 0, n = valence * 2 - 5; i < n; i++) {
                        int index = i + 1;
                        if (index == n) {
                            index = 0;
                        }
                        cornerLimitStencil[level][valence - 3][corner][i + 11] = cornerIndex(corner, valence, index);
                    }
                    int[][] array = new int[cornerStencilLength(valence)][];
                    fanStencil[level][valence - 3][corner] = array;
                    if (valence == 3) {
                        array[0] = new int[] { EDGE, 0, 0, 0, cornerIndex2(corner, level, valence, 0), patchCornerIndex(corner, level, 1, 1), cornerIndex2(corner, level, valence, 2), cornerIndex2(corner, level, valence, 3), cornerIndex2(corner, level, valence, -1), cornerIndex2(corner, level, valence, -2) };
                        array[1] = array[0].clone();
                    } else {
                        for (int i = 0, n = valence * 2 - 5; i < n; i++) {
                            int index = i + 1;
                            if (index == n) {
                                index = 0;
                            }
                            if ((i & 1) == 0) {
                                array[index] = new int[] { EDGE, 0, 0, 0, cornerIndex2(corner, level, valence, i), patchCornerIndex(corner, level, 1, 1), cornerIndex2(corner, level, valence, i + 1), cornerIndex2(corner, level, valence, i + 2), cornerIndex2(corner, level, valence, i - 1), cornerIndex2(corner, level, valence, i - 2) };
                            } else {
                                array[index] = new int[] { FACE, cornerIndex2(corner, level, valence, i), cornerIndex2(corner, level, valence, i + 1), patchCornerIndex(corner, level, 1, 1), cornerIndex2(corner, level, valence, i - 1) };
                            }
                        }
                    }
                }
            }
            patchStencil[level] = new int[dim * dim][];
            for (int row = 0; row < dim; row++) {
                int rowStart = row * dim;
                for (int column = 0; column < dim; column++) {
                    int index = rowStart + column;
                    if ((row < 2 && column < 2) || (row > dim - 3 && column > dim - 3)) {
                        patchStencil[level][index] = new int[] { UNUSED, 0 };
                        continue;
                    }
                    int nextRow = row * 2 - 1;
                    int nextColumn = column * 2 - 1;
                    int nextLevelIndex_0 = getStencilIndex(level + 1, nextRow, nextColumn);
                    int nextLevelIndex_1 = getStencilIndex(level + 1, nextRow - 1, nextColumn);
                    int nextLevelIndex_2 = getStencilIndex(level + 1, nextRow, nextColumn + 1);
                    int nextLevelIndex_3 = getStencilIndex(level + 1, nextRow + 1, nextColumn);
                    int nextLevelIndex_4 = getStencilIndex(level + 1, nextRow, nextColumn - 1);
                    if ((column & 1) == 0) {
                        if ((row & 1) == 0) {
                            int c = column / 2;
                            int r = row / 2;
                            patchStencil[level][index] = new int[] { FACE, patchIndex(level, r, c), patchIndex(level, r, c + 1), patchIndex(level, r + 1, c + 1), patchIndex(level, r + 1, c) };
                        } else {
                            int c = column / 2;
                            int r = (row + 1) / 2;
                            patchStencil[level][index] = new int[] { EDGE_H, 0, 0, 0, nextLevelIndex_0, nextLevelIndex_1, nextLevelIndex_2, nextLevelIndex_3, nextLevelIndex_4, patchIndex(level, r, c), patchIndex(level, r, c + 1), patchIndex(level, r - 1, c), patchIndex(level, r - 1, c + 1), patchIndex(level, r + 1, c), patchIndex(level, r + 1, c + 1) };
                        }
                    } else {
                        if ((row & 1) == 0) {
                            int c = (column + 1) / 2;
                            int r = row / 2;
                            patchStencil[level][index] = new int[] { EDGE_V, 0, 0, 0, nextLevelIndex_0, nextLevelIndex_1, nextLevelIndex_2, nextLevelIndex_3, nextLevelIndex_4, patchIndex(level, r, c), patchIndex(level, r + 1, c), patchIndex(level, r, c + 1), patchIndex(level, r + 1, c + 1), patchIndex(level, r, c - 1), patchIndex(level, r + 1, c - 1) };
                        } else {
                            int c = (column + 1) / 2;
                            int r = (row + 1) / 2;
                            patchStencil[level][index] = new int[] { POINT, 0, nextLevelIndex_0, patchIndex(level, r, c), patchIndex(level, r - 1, c), patchIndex(level, r, c + 1), patchIndex(level, r + 1, c), patchIndex(level, r, c - 1), patchIndex(level, r - 1, c - 1), patchIndex(level, r - 1, c + 1), patchIndex(level, r + 1, c + 1), patchIndex(level, r + 1, c - 1) };
                        }
                    }
                }
            }
            patchLimitStencil[level] = new int[dim * dim][];
            for (int row = 1; row < dim - 1; row++) {
                int rowStart = row * dim;
                for (int column = 1; column < dim - 1; column++) {
                    if ((row == 1 && column == 1) || (row == dim - 2 && column == dim - 2)) {
                        continue;
                    }
                    int index = rowStart + column;
                    patchLimitStencil[level][index] = new int[] { patchIndex(level + 1, row, column), patchIndex(level + 1, row - 1, column), patchIndex(level + 1, row, column + 1), patchIndex(level + 1, row + 1, column), patchIndex(level + 1, row, column - 1), patchIndex(level + 1, row - 1, column - 1), patchIndex(level + 1, row - 1, column + 1), patchIndex(level + 1, row + 1, column + 1), patchIndex(level + 1, row + 1, column - 1) };
                }
            }
        }
    }

    private int getStencilIndex(int level, int row, int column) {
        final int dim = ((1 << level)) + 3;
        if (row < 0 | row > dim - 1 | column < 0 | column > dim - 1) {
            return 0;
        }
        return row * dim + column;
    }

    public float[][] getSubdivVertices(int level) {
        return subdivPoints[level];
    }

    public int[][] getStencils(int level) {
        return patchStencil[level];
    }

    public float[][] getLimitVertices(int level) {
        return limitPoints[level];
    }

    public float[][] getLimitNormals(int level) {
        return limitNormals[level];
    }

    public int[] getRimTriangles(int level, int side, int pairLevel) {
        if (pairLevel > level) {
            pairLevel = level;
        }
        return rimTriangles[level][pairLevel][side];
    }

    public int[] getRimTriangleNormals(int level, int side, int pairLevel) {
        if (pairLevel > level) {
            pairLevel = level;
        }
        return rimTriangleNormals[level][pairLevel][side];
    }

    public int[] getRim(int level, int side) {
        return rim0[level][side];
    }

    public int dice(final Slate2 slate, final int depth) {
        if (depth == 1) {
            final float[][] lp = limitPoints[0];
            final float[][] ln = limitNormals[0];
            for (int i = 0; i < 4; i++) {
                final SlateEdge edge = slate.corners[i][0];
                final Level2Vertex v = edge.vertex;
                final int gridPos = GRID_START + CORNER_INDEXES[i];
                final int normalIndex = 3 * i;
                lp[gridPos][0] = v.projectedLimit.x;
                lp[gridPos][1] = v.projectedLimit.y;
                lp[gridPos][2] = v.projectedLimit.z;
                if (v.corner > 0) {
                    final Level2Vertex vv = edge.pair.vertex;
                    final Level2Vertex vu = slate.corners[(i + 1) % 4][0].pair.vertex;
                    final float vx = vv.projectedPos.x - v.projectedPos.x;
                    final float vy = vv.projectedPos.y - v.projectedPos.y;
                    final float vz = vv.projectedPos.z - v.projectedPos.z;
                    final float ux = vu.projectedPos.x - v.projectedPos.x;
                    final float uy = vu.projectedPos.y - v.projectedPos.y;
                    final float uz = vu.projectedPos.z - v.projectedPos.z;
                    computeNormal(ux, uy, uz, vx, vy, vz, ln[gridPos], normalIndex);
                } else if (v.crease > 0) {
                    int creaseIndex0 = slate.creaseIndex0[i];
                    int creaseIndex1 = slate.creaseIndex1[i];
                    if (creaseIndex0 == -1 || creaseIndex1 == -1) {
                        System.exit(0);
                    }
                    final Point3f pc0 = slate.corners[i][creaseIndex0].pair.vertex.projectedPos;
                    final Point3f pc1 = slate.corners[i][creaseIndex1].pair.vertex.projectedPos;
                    final Point3f p0 = v.projectedPos;
                    final Point3f p3;
                    if (creaseIndex1 == 0) {
                        if (creaseIndex0 == 1) {
                            p3 = slate.fans[(i + 2) % 4][0];
                        } else {
                            p3 = slate.fans[(i + 3) % 4][0];
                        }
                    } else {
                        p3 = slate.fans[(i + 1) % 4][0];
                    }
                    final float vx = (pc1.x - pc0.x);
                    final float vy = (pc1.y - pc0.y);
                    final float vz = (pc1.z - pc0.z);
                    final float ux = p3.x - p0.x;
                    final float uy = p3.y - p0.y;
                    final float uz = p3.z - p0.z;
                    computeNormal(ux, uy, uz, vx, vy, vz, ln[gridPos], normalIndex);
                } else {
                    ln[gridPos][normalIndex] = v.projectedNormal.x;
                    ln[gridPos][normalIndex + 1] = v.projectedNormal.y;
                    ln[gridPos][normalIndex + 2] = v.projectedNormal.z;
                }
            }
            return 12;
        }
        final int dim = (1 << (depth - 1)) + 3;
        final Point3f[][] boundary = slate.fans;
        final SlateEdge[][] edges = slate.corners;
        float[][] geo = subdivPoints[0];
        geo[GRID_START + 5][0] = boundary[0][0].x;
        geo[GRID_START + 5][1] = boundary[0][0].y;
        geo[GRID_START + 5][2] = boundary[0][0].z;
        geo[GRID_START + 6][0] = boundary[1][0].x;
        geo[GRID_START + 6][1] = boundary[1][0].y;
        geo[GRID_START + 6][2] = boundary[1][0].z;
        geo[GRID_START + 2][0] = boundary[1][1].x;
        geo[GRID_START + 2][1] = boundary[1][1].y;
        geo[GRID_START + 2][2] = boundary[1][1].z;
        geo[GRID_START + 3][0] = boundary[1][2].x;
        geo[GRID_START + 3][1] = boundary[1][2].y;
        geo[GRID_START + 3][2] = boundary[1][2].z;
        geo[GRID_START + 7][0] = boundary[1][3].x;
        geo[GRID_START + 7][1] = boundary[1][3].y;
        geo[GRID_START + 7][2] = boundary[1][3].z;
        geo[GRID_START + 10][0] = boundary[2][0].x;
        geo[GRID_START + 10][1] = boundary[2][0].y;
        geo[GRID_START + 10][2] = boundary[2][0].z;
        geo[GRID_START + 9][0] = boundary[3][0].x;
        geo[GRID_START + 9][1] = boundary[3][0].y;
        geo[GRID_START + 9][2] = boundary[3][0].z;
        geo[GRID_START + 13][0] = boundary[3][1].x;
        geo[GRID_START + 13][1] = boundary[3][1].y;
        geo[GRID_START + 13][2] = boundary[3][1].z;
        geo[GRID_START + 12][0] = boundary[3][2].x;
        geo[GRID_START + 12][1] = boundary[3][2].y;
        geo[GRID_START + 12][2] = boundary[3][2].z;
        geo[GRID_START + 8][0] = boundary[3][3].x;
        geo[GRID_START + 8][1] = boundary[3][3].y;
        geo[GRID_START + 8][2] = boundary[3][3].z;
        patchStencil[1][7][1] = (int) (edges[0][0].creaseSharpness() * 0x10000);
        patchStencil[1][13][1] = (int) (edges[1][0].creaseSharpness() * 0x10000);
        patchStencil[1][17][1] = (int) (edges[2][0].creaseSharpness() * 0x10000);
        patchStencil[1][11][1] = (int) (edges[3][0].creaseSharpness() * 0x10000);
        patchStencil[1][3][1] = (int) (edges[1][2] == null ? 0 : edges[1][2].creaseSharpness() * 0x10000);
        patchStencil[1][9][1] = (int) (edges[1][3] == null ? 0 : edges[1][3].creaseSharpness() * 0x10000);
        patchStencil[1][21][1] = (int) (edges[3][2] == null ? 0 : edges[3][2].creaseSharpness() * 0x10000);
        patchStencil[1][15][1] = (int) (edges[3][3] == null ? 0 : edges[3][3].creaseSharpness() * 0x10000);
        patchStencil[1][8][1] = (int) (edges[1][0].vertex.crease * 0x10000);
        patchStencil[0][6][1] = (int) (edges[1][0].vertex.crease * 0x10000);
        if (edges[1][0].vertex.crease > 0) {
            patchStencil[1][8][0] = CREASE_5_7;
            patchStencil[0][6][0] = CREASE_5_7;
        } else {
            patchStencil[1][8][0] = POINT;
            patchStencil[0][6][0] = POINT;
        }
        patchStencil[1][16][1] = (int) (edges[3][0].vertex.crease * 0x10000);
        patchStencil[0][9][1] = (int) (edges[3][0].vertex.crease * 0x10000);
        if (edges[3][0].vertex.crease > 0) {
            patchStencil[1][16][0] = CREASE_4_6;
            patchStencil[0][9][0] = CREASE_4_6;
        } else {
            patchStencil[1][16][0] = POINT;
            patchStencil[0][9][0] = POINT;
        }
        for (int corner = 0; corner < 2; corner++) {
            final Point3f[] c = boundary[corner * 2];
            final int valence = Math.max(3, edges[corner * 2].length);
            final int n = c.length;
            final int start = corner * MAX_FAN_LENGTH;
            cornerStencil[0][valence - 3][corner][0] = (int) (edges[corner * 2][0].vertex.corner * 0x10000);
            cornerStencil[0][valence - 3][corner][1] = (int) (edges[corner * 2][0].vertex.crease * 0x10000);
            cornerStencil[1][valence - 3][corner][0] = (int) (edges[corner * 2][0].vertex.corner * 0x10000);
            cornerStencil[1][valence - 3][corner][1] = (int) (edges[corner * 2][0].vertex.crease * 0x10000);
            if (edges[corner * 2][0].vertex.crease > 0) {
                int ci0 = slate.getEdgeIndex(corner * 2, edges[corner * 2][0].vertex.creaseEdge0);
                int ci1 = slate.getEdgeIndex(corner * 2, edges[corner * 2][0].vertex.creaseEdge1);
                cornerStencil[0][valence - 3][corner][2] = 2 * ci0 + 7;
                cornerStencil[0][valence - 3][corner][3] = 2 * ci1 + 7;
                cornerStencil[1][valence - 3][corner][2] = 2 * ci0 + 7;
                cornerStencil[1][valence - 3][corner][3] = 2 * ci1 + 7;
            }
            for (int i = 1; i < n; i++) {
                final int index = start + (i % (n - 1));
                geo[index][0] = c[i].x;
                geo[index][1] = c[i].y;
                geo[index][2] = c[i].z;
            }
            if (n == 2) {
                geo[start + 1][0] = c[1].x;
                geo[start + 1][1] = c[1].y;
                geo[start + 1][2] = c[1].z;
            }
            for (int i = 2; i < edges[corner * 2].length; i++) {
                int index = (i == edges[corner * 2].length - 1) ? 0 : i * 2 - 3;
                fanStencil[1][valence - 3][corner][index][1] = edges[corner * 2][i] == null ? 0 : (int) (edges[corner * 2][i].creaseSharpness() * 0x10000);
            }
            if (n == 2) {
                fanStencil[1][valence - 3][corner][1][1] = fanStencil[1][valence - 3][corner][0][1];
            }
        }
        for (int level = 1; level < depth; level++) {
            final boolean rewriteStencils = (level < depth - 1 && level < MAX_SUBDIV - 1);
            final int[][] stencil = patchStencil[level];
            final int[][] nextLevel = rewriteStencils ? patchStencil[level + 1] : null;
            final float[][] out = subdivPoints[level];
            final float[][] in = subdivPoints[level - 1];
            final int n = stencil.length - 2;
            if (rewriteStencils) {
                for (int i = 2; i < n; i++) {
                    final int[] s = stencil[i];
                    final int outIndex = GRID_START + i;
                    switch(s[0]) {
                        case EDGE_H:
                            if (s[1] > 0) {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * 0.5f;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * 0.5f;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * 0.5f;
                                nextLevel[s[4]][1] = s[1] - 0x10000;
                                nextLevel[s[5]][1] = 0;
                                nextLevel[s[6]][1] = s[1] - 0x10000;
                                nextLevel[s[7]][1] = 0;
                                nextLevel[s[8]][1] = s[1] - 0x10000;
                                nextLevel[s[4]][0] = s[1] > 0x10000 ? CREASE_5_7 : POINT;
                            } else {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * EDGE0 + ((in[s[11]][0] + in[s[12]][0]) + (in[s[13]][0] + in[s[14]][0])) * EDGE1;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * EDGE0 + ((in[s[11]][1] + in[s[12]][1]) + (in[s[13]][1] + in[s[14]][1])) * EDGE1;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * EDGE0 + ((in[s[11]][2] + in[s[12]][2]) + (in[s[13]][2] + in[s[14]][2])) * EDGE1;
                                nextLevel[s[4]][1] = 0;
                                nextLevel[s[5]][1] = 0;
                                nextLevel[s[6]][1] = 0;
                                nextLevel[s[7]][1] = 0;
                                nextLevel[s[8]][1] = 0;
                                nextLevel[s[4]][0] = POINT;
                            }
                            break;
                        case EDGE_V:
                            if (s[1] > 0) {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * 0.5f;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * 0.5f;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * 0.5f;
                                nextLevel[s[4]][1] = s[1] - 0x10000;
                                nextLevel[s[5]][1] = s[1] - 0x10000;
                                nextLevel[s[6]][1] = 0;
                                nextLevel[s[7]][1] = s[1] - 0x10000;
                                nextLevel[s[8]][1] = 0;
                                nextLevel[s[4]][0] = s[1] > 0x10000 ? CREASE_4_6 : POINT;
                            } else {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * EDGE0 + ((in[s[11]][0] + in[s[12]][0]) + (in[s[13]][0] + in[s[14]][0])) * EDGE1;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * EDGE0 + ((in[s[11]][1] + in[s[12]][1]) + (in[s[13]][1] + in[s[14]][1])) * EDGE1;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * EDGE0 + ((in[s[11]][2] + in[s[12]][2]) + (in[s[13]][2] + in[s[14]][2])) * EDGE1;
                                nextLevel[s[4]][1] = 0;
                                nextLevel[s[5]][1] = 0;
                                nextLevel[s[6]][1] = 0;
                                nextLevel[s[7]][1] = 0;
                                nextLevel[s[8]][1] = 0;
                                nextLevel[s[4]][0] = POINT;
                            }
                            break;
                        case POINT:
                            if (s[1] > 0) {
                                out[outIndex][0] = in[s[3]][0];
                                out[outIndex][1] = in[s[3]][1];
                                out[outIndex][2] = in[s[3]][2];
                                nextLevel[s[2]][1] = s[1] - 0x10000;
                            } else {
                                out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
                                out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
                                out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
                                nextLevel[s[2]][1] = 0;
                            }
                            nextLevel[s[2]][0] = POINT;
                            break;
                        case FACE:
                            out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
                            out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
                            out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
                            break;
                        case CREASE_4_5:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[5]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[5]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[5]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_4_5 : POINT;
                            break;
                        case CREASE_4_6:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[6]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[6]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[6]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_4_6 : POINT;
                            break;
                        case CREASE_4_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[7]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_4_7 : POINT;
                            break;
                        case CREASE_5_6:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[6]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[6]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[6]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_5_6 : POINT;
                            break;
                        case CREASE_5_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[7]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_5_7 : POINT;
                            break;
                        case CREASE_6_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[6]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[6]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[6]][2] + in[s[7]][2]) * CREASE1;
                            nextLevel[s[2]][1] = s[1] - 0x10000;
                            nextLevel[s[2]][0] = s[1] > 0x10000 ? CREASE_6_7 : POINT;
                            break;
                    }
                }
            } else {
                for (int i = 2; i < n; i++) {
                    final int[] s = stencil[i];
                    final int outIndex = GRID_START + i;
                    switch(s[0]) {
                        case EDGE_H:
                            if (s[1] > 0) {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * 0.5f;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * 0.5f;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * 0.5f;
                            } else {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * EDGE0 + ((in[s[11]][0] + in[s[12]][0]) + (in[s[13]][0] + in[s[14]][0])) * EDGE1;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * EDGE0 + ((in[s[11]][1] + in[s[12]][1]) + (in[s[13]][1] + in[s[14]][1])) * EDGE1;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * EDGE0 + ((in[s[11]][2] + in[s[12]][2]) + (in[s[13]][2] + in[s[14]][2])) * EDGE1;
                            }
                            break;
                        case EDGE_V:
                            if (s[1] > 0) {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * 0.5f;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * 0.5f;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * 0.5f;
                            } else {
                                out[outIndex][0] = (in[s[9]][0] + in[s[10]][0]) * EDGE0 + ((in[s[11]][0] + in[s[12]][0]) + (in[s[13]][0] + in[s[14]][0])) * EDGE1;
                                out[outIndex][1] = (in[s[9]][1] + in[s[10]][1]) * EDGE0 + ((in[s[11]][1] + in[s[12]][1]) + (in[s[13]][1] + in[s[14]][1])) * EDGE1;
                                out[outIndex][2] = (in[s[9]][2] + in[s[10]][2]) * EDGE0 + ((in[s[11]][2] + in[s[12]][2]) + (in[s[13]][2] + in[s[14]][2])) * EDGE1;
                            }
                            break;
                        case POINT:
                            if (s[1] > 0) {
                                out[outIndex][0] = in[s[3]][0];
                                out[outIndex][1] = in[s[3]][1];
                                out[outIndex][2] = in[s[3]][2];
                            } else {
                                out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
                                out[outIndex][1] = in[s[3]][1] * VERTEX0 + ((in[s[4]][1] + in[s[6]][1]) + (in[s[5]][1] + in[s[7]][1])) * VERTEX1 + ((in[s[8]][1] + in[s[10]][1]) + (in[s[9]][1] + in[s[11]][1])) * VERTEX2;
                                out[outIndex][2] = in[s[3]][2] * VERTEX0 + ((in[s[4]][2] + in[s[6]][2]) + (in[s[5]][2] + in[s[7]][2])) * VERTEX1 + ((in[s[8]][2] + in[s[10]][2]) + (in[s[9]][2] + in[s[11]][2])) * VERTEX2;
                            }
                            break;
                        case FACE:
                            out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
                            out[outIndex][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
                            out[outIndex][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
                            break;
                        case CREASE_4_5:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[5]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[5]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[5]][2]) * CREASE1;
                            break;
                        case CREASE_4_6:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[6]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[6]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[6]][2]) * CREASE1;
                            break;
                        case CREASE_4_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[4]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[4]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[4]][2] + in[s[7]][2]) * CREASE1;
                            break;
                        case CREASE_5_6:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[6]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[6]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[6]][2]) * CREASE1;
                            break;
                        case CREASE_5_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[5]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[5]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[5]][2] + in[s[7]][2]) * CREASE1;
                            break;
                        case CREASE_6_7:
                            out[outIndex][0] = in[s[3]][0] * CREASE0 + (in[s[6]][0] + in[s[7]][0]) * CREASE1;
                            out[outIndex][1] = in[s[3]][1] * CREASE0 + (in[s[6]][1] + in[s[7]][1]) * CREASE1;
                            out[outIndex][2] = in[s[3]][2] * CREASE0 + (in[s[6]][2] + in[s[7]][2]) * CREASE1;
                            break;
                    }
                }
            }
            for (int corner = 0; corner < 2; corner++) {
                final int valence = Math.max(3, slate.corners[corner * 2].length);
                final int[] cs = cornerStencil[level][valence - 3][corner];
                final int outIndex = cs[4];
                float x = 0, y = 0, z = 0;
                if (cs[0] > 0) {
                    x = in[cs[5]][0];
                    y = in[cs[5]][1];
                    z = in[cs[5]][2];
                    if (rewriteStencils) {
                        cornerStencil[level + 1][valence - 3][corner][0] = cornerStencil[level][valence - 3][corner][0] - 0x10000;
                        cornerStencil[level + 1][valence - 3][corner][1] = cornerStencil[level][valence - 3][corner][1] - 0x10000;
                        cornerStencil[level + 1][valence - 3][corner][2] = cornerStencil[level][valence - 3][corner][2];
                        cornerStencil[level + 1][valence - 3][corner][3] = cornerStencil[level][valence - 3][corner][3];
                    }
                } else if (cs[1] > 0) {
                    x = in[cs[5]][0] * CREASE0 + (in[cs[cs[2]]][0] + in[cs[cs[3]]][0]) * CREASE1;
                    y = in[cs[5]][1] * CREASE0 + (in[cs[cs[2]]][1] + in[cs[cs[3]]][1]) * CREASE1;
                    z = in[cs[5]][2] * CREASE0 + (in[cs[cs[2]]][2] + in[cs[cs[3]]][2]) * CREASE1;
                    if (rewriteStencils) {
                        cornerStencil[level + 1][valence - 3][corner][0] = cornerStencil[level][valence - 3][corner][0] - 0x10000;
                        cornerStencil[level + 1][valence - 3][corner][1] = cornerStencil[level][valence - 3][corner][1] - 0x10000;
                        cornerStencil[level + 1][valence - 3][corner][2] = cornerStencil[level][valence - 3][corner][2];
                        cornerStencil[level + 1][valence - 3][corner][3] = cornerStencil[level][valence - 3][corner][3];
                    }
                }
                if (cs[0] > 0x10000 || cs[1] > 0x10000) {
                    out[outIndex][0] = x;
                    out[outIndex][1] = y;
                    out[outIndex][2] = z;
                } else {
                    float f0 = 0, f1 = 0, f2 = 0;
                    float e0 = 0, e1 = 0, e2 = 0;
                    for (int p = 6; p < cs.length; p++) {
                        f0 += in[cs[p]][0];
                        f1 += in[cs[p]][1];
                        f2 += in[cs[p++]][2];
                        e0 += in[cs[p]][0];
                        e1 += in[cs[p]][1];
                        e2 += in[cs[p]][2];
                    }
                    float smoothX = f0 * VERTEX_FACE[valence] + e0 * VERTEX_EDGE[valence] + in[cs[5]][0] * VERTEX_POINT[valence];
                    float smoothY = f1 * VERTEX_FACE[valence] + e1 * VERTEX_EDGE[valence] + in[cs[5]][1] * VERTEX_POINT[valence];
                    float smoothZ = f2 * VERTEX_FACE[valence] + e2 * VERTEX_EDGE[valence] + in[cs[5]][2] * VERTEX_POINT[valence];
                    if (rewriteStencils) {
                        cornerStencil[level + 1][valence - 3][corner][0] = 0;
                        cornerStencil[level + 1][valence - 3][corner][1] = 0;
                    }
                    if (cs[0] <= 0 && cs[1] <= 0) {
                        out[outIndex][0] = smoothX;
                        out[outIndex][1] = smoothY;
                        out[outIndex][2] = smoothZ;
                    } else {
                        float t = (cs[0] > 0) ? ((float) cs[0]) / 0x10000 : ((float) cs[1]) / 0x10000;
                        float t1 = 1 - t;
                        out[outIndex][0] = smoothX * t1 + x * t;
                        out[outIndex][1] = smoothY * t1 + y * t;
                        out[outIndex][2] = smoothZ * t1 + z * t;
                    }
                }
                final int[][] array = fanStencil[level][valence - 3][corner];
                final int[][] nextArray = rewriteStencils ? fanStencil[level + 1][valence - 3][corner] : null;
                final int m = array.length;
                for (int i = 0; i < m; i++) {
                    final int oi = MAX_FAN_LENGTH * corner + i;
                    final int[] s = array[i];
                    switch(s[0]) {
                        case EDGE:
                            if (s[1] > 0) {
                                out[oi][0] = (in[s[4]][0] + in[s[5]][0]) * 0.5f;
                                out[oi][1] = (in[s[4]][1] + in[s[5]][1]) * 0.5f;
                                out[oi][2] = (in[s[4]][2] + in[s[5]][2]) * 0.5f;
                                if (rewriteStencils) {
                                    nextArray[i][1] = s[1] - 0x10000;
                                }
                            } else {
                                out[oi][0] = (in[s[4]][0] + in[s[5]][0]) * EDGE0 + ((in[s[6]][0] + in[s[7]][0]) + (in[s[8]][0] + in[s[9]][0])) * EDGE1;
                                out[oi][1] = (in[s[4]][1] + in[s[5]][1]) * EDGE0 + ((in[s[6]][1] + in[s[7]][1]) + (in[s[8]][1] + in[s[9]][1])) * EDGE1;
                                out[oi][2] = (in[s[4]][2] + in[s[5]][2]) * EDGE0 + ((in[s[6]][2] + in[s[7]][2]) + (in[s[8]][2] + in[s[9]][2])) * EDGE1;
                                if (rewriteStencils) {
                                    nextArray[i][1] = 0;
                                }
                            }
                            break;
                        case FACE:
                            out[oi][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
                            out[oi][1] = ((in[s[1]][1] + in[s[3]][1]) + (in[s[2]][1] + in[s[4]][1])) * FACE0;
                            out[oi][2] = ((in[s[1]][2] + in[s[3]][2]) + (in[s[2]][2] + in[s[4]][2])) * FACE0;
                            break;
                    }
                }
            }
        }
        final int level = depth - 1;
        final int[][] limitStencil = patchLimitStencil[level];
        final int[][] stencil = patchStencil[level];
        final float[][] out = limitPoints[level];
        final float[][] norm = limitNormals[level];
        float limitX = 0, limitY = 0, limitZ = 0;
        float normalX = 0, normalY = 0, normalZ = 0;
        final float[][] in = subdivPoints[level];
        for (int i = dim + 2, n = limitStencil.length - dim - 2; i < n; i++) {
            final int[] ls = limitStencil[i];
            final int[] s = stencil[i];
            if (ls != null) {
                final int outIndex = GRID_START + i;
                if (s[0] == FACE || s[1] == 0) {
                    limitX = in[ls[0]][0] * LIMIT0 + ((in[ls[1]][0] + in[ls[3]][0]) + (in[ls[2]][0] + in[ls[4]][0])) * LIMIT1 + ((in[ls[5]][0] + in[ls[7]][0]) + (in[ls[6]][0] + in[ls[8]][0])) * LIMIT2;
                    limitY = in[ls[0]][1] * LIMIT0 + ((in[ls[1]][1] + in[ls[3]][1]) + (in[ls[2]][1] + in[ls[4]][1])) * LIMIT1 + ((in[ls[5]][1] + in[ls[7]][1]) + (in[ls[6]][1] + in[ls[8]][1])) * LIMIT2;
                    limitZ = in[ls[0]][2] * LIMIT0 + ((in[ls[1]][2] + in[ls[3]][2]) + (in[ls[2]][2] + in[ls[4]][2])) * LIMIT1 + ((in[ls[5]][2] + in[ls[7]][2]) + (in[ls[6]][2] + in[ls[8]][2])) * LIMIT2;
                } else {
                    switch(s[0]) {
                        case POINT:
                            limitX = in[ls[0]][0];
                            limitY = in[ls[0]][1];
                            limitZ = in[ls[0]][2];
                            break;
                        case EDGE_H:
                            limitX = (in[ls[2]][0] + in[ls[4]][0]) * 0.5f;
                            limitY = (in[ls[2]][1] + in[ls[4]][1]) * 0.5f;
                            limitZ = (in[ls[2]][2] + in[ls[4]][2]) * 0.5f;
                            break;
                        case EDGE_V:
                            limitX = (in[ls[1]][0] + in[ls[3]][0]) * 0.5f;
                            limitY = (in[ls[1]][1] + in[ls[3]][1]) * 0.5f;
                            limitZ = (in[ls[1]][2] + in[ls[3]][2]) * 0.5f;
                            break;
                        case CREASE_4_5:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[2]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[2]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[2]][2]) * CREASE_LIMIT1;
                            break;
                        case CREASE_4_6:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[3]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[3]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[3]][2]) * CREASE_LIMIT1;
                            break;
                        case CREASE_4_7:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[1]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[1]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[1]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
                            break;
                        case CREASE_5_6:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[2]][0] + in[ls[3]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[2]][1] + in[ls[3]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[2]][2] + in[ls[3]][2]) * CREASE_LIMIT1;
                            break;
                        case CREASE_5_7:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[2]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[2]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[2]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
                            break;
                        case CREASE_6_7:
                            limitX = in[ls[0]][0] * CREASE_LIMIT0 + (in[ls[3]][0] + in[ls[4]][0]) * CREASE_LIMIT1;
                            limitY = in[ls[0]][1] * CREASE_LIMIT0 + (in[ls[3]][1] + in[ls[4]][1]) * CREASE_LIMIT1;
                            limitZ = in[ls[0]][2] * CREASE_LIMIT0 + (in[ls[3]][2] + in[ls[4]][2]) * CREASE_LIMIT1;
                            break;
                    }
                }
                out[outIndex][0] = limitX;
                out[outIndex][1] = limitY;
                out[outIndex][2] = limitZ;
                if (s[0] == CREASE_4_6 || (s[0] == EDGE_V && s[1] > 0)) {
                    final float ux = in[ls[1]][0] - in[ls[3]][0];
                    final float uy = in[ls[1]][1] - in[ls[3]][1];
                    final float uz = in[ls[1]][2] - in[ls[3]][2];
                    final float vx0 = in[ls[2]][0] - in[ls[0]][0];
                    final float vy0 = in[ls[2]][1] - in[ls[0]][1];
                    final float vz0 = in[ls[2]][2] - in[ls[0]][2];
                    final float vx1 = in[ls[0]][0] - in[ls[4]][0];
                    final float vy1 = in[ls[0]][1] - in[ls[4]][1];
                    final float vz1 = in[ls[0]][2] - in[ls[4]][2];
                    final float[] normal = norm[outIndex];
                    computeNormal(vx0, vy0, vz0, ux, uy, uz, normal, 0);
                    computeNormal(vx1, vy1, vz1, ux, uy, uz, normal, 3);
                    normal[9] = normal[0];
                    normal[10] = normal[1];
                    normal[11] = normal[2];
                    normal[6] = normal[3];
                    normal[7] = normal[4];
                    normal[8] = normal[5];
                } else if (s[0] == CREASE_5_7 || (s[0] == EDGE_H && s[1] > 0)) {
                    final float ux = in[ls[2]][0] - in[ls[4]][0];
                    final float uy = in[ls[2]][1] - in[ls[4]][1];
                    final float uz = in[ls[2]][2] - in[ls[4]][2];
                    final float vx0 = in[ls[3]][0] - in[ls[0]][0];
                    final float vy0 = in[ls[3]][1] - in[ls[0]][1];
                    final float vz0 = in[ls[3]][2] - in[ls[0]][2];
                    final float vx1 = in[ls[0]][0] - in[ls[1]][0];
                    final float vy1 = in[ls[0]][1] - in[ls[1]][1];
                    final float vz1 = in[ls[0]][2] - in[ls[1]][2];
                    final float[] normal = norm[outIndex];
                    computeNormal(vx0, vy0, vz0, ux, uy, uz, normal, 0);
                    computeNormal(vx1, vy1, vz1, ux, uy, uz, normal, 9);
                    normal[3] = normal[0];
                    normal[4] = normal[1];
                    normal[5] = normal[2];
                    normal[6] = normal[9];
                    normal[7] = normal[10];
                    normal[8] = normal[11];
                } else {
                    final float ux = (in[ls[2]][0] - in[ls[4]][0]) * 4 + (in[ls[6]][0] - in[ls[5]][0]) + (in[ls[7]][0] - in[ls[8]][0]);
                    final float uy = (in[ls[2]][1] - in[ls[4]][1]) * 4 + (in[ls[6]][1] - in[ls[5]][1]) + (in[ls[7]][1] - in[ls[8]][1]);
                    final float uz = (in[ls[2]][2] - in[ls[4]][2]) * 4 + (in[ls[6]][2] - in[ls[5]][2]) + (in[ls[7]][2] - in[ls[8]][2]);
                    final float vx = (in[ls[1]][0] - in[ls[3]][0]) * 4 + (in[ls[5]][0] - in[ls[8]][0]) + (in[ls[6]][0] - in[ls[7]][0]);
                    final float vy = (in[ls[1]][1] - in[ls[3]][1]) * 4 + (in[ls[5]][1] - in[ls[8]][1]) + (in[ls[6]][1] - in[ls[7]][1]);
                    final float vz = (in[ls[1]][2] - in[ls[3]][2]) * 4 + (in[ls[5]][2] - in[ls[8]][2]) + (in[ls[6]][2] - in[ls[7]][2]);
                    final float[] normal = norm[outIndex];
                    computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
                    normal[9] = normal[6] = normal[3] = normal[0];
                    normal[10] = normal[7] = normal[4] = normal[1];
                    normal[11] = normal[8] = normal[5] = normal[2];
                }
            }
        }
        for (int corner = 0; corner < 2; corner++) {
            final int valence = Math.max(3, slate.corners[corner * 2].length);
            final int[] cps = cornerStencil[level][valence - 3][corner];
            final int[] cs = cornerLimitStencil[level][valence - 3][corner];
            final int outIndex = cs[4];
            if (cps[0] > 0) {
                out[outIndex][0] = in[cs[5]][0];
                out[outIndex][1] = in[cs[5]][1];
                out[outIndex][2] = in[cs[5]][2];
            } else if (cps[1] > 0) {
                out[outIndex][0] = in[cs[5]][0] * CREASE_LIMIT0 + (in[cs[cps[2]]][0] + in[cs[cps[3]]][0]) * CREASE_LIMIT1;
                out[outIndex][1] = in[cs[5]][1] * CREASE_LIMIT0 + (in[cs[cps[2]]][1] + in[cs[cps[3]]][1]) * CREASE_LIMIT1;
                out[outIndex][2] = in[cs[5]][2] * CREASE_LIMIT0 + (in[cs[cps[2]]][2] + in[cs[cps[3]]][2]) * CREASE_LIMIT1;
            } else {
                float f0 = 0, f1 = 0, f2 = 0;
                float e0 = 0, e1 = 0, e2 = 0;
                for (int p = 6; p < cs.length; p++) {
                    f0 += in[cs[p]][0];
                    f1 += in[cs[p]][1];
                    f2 += in[cs[p++]][2];
                    e0 += in[cs[p]][0];
                    e1 += in[cs[p]][1];
                    e2 += in[cs[p]][2];
                }
                out[outIndex][0] = e0 * VERTEX_EDGE_LIMIT[valence] + f0 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][0] * VERTEX_POINT_LIMIT[valence];
                out[outIndex][1] = e1 * VERTEX_EDGE_LIMIT[valence] + f1 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][1] * VERTEX_POINT_LIMIT[valence];
                out[outIndex][2] = e2 * VERTEX_EDGE_LIMIT[valence] + f2 * VERTEX_FACE_LIMIT[valence] + in[cs[5]][2] * VERTEX_POINT_LIMIT[valence];
            }
            if (cps[0] > 0) {
                final float vx = in[cs[7]][0] - in[cs[5]][0];
                final float vy = in[cs[7]][1] - in[cs[5]][1];
                final float vz = in[cs[7]][2] - in[cs[5]][2];
                final float ux = in[cs[9]][0] - in[cs[5]][0];
                final float uy = in[cs[9]][1] - in[cs[5]][1];
                final float uz = in[cs[9]][2] - in[cs[5]][2];
                float[] normal = norm[outIndex];
                computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
            } else if (cps[1] > 0) {
                int v = cps.length - 6;
                int a = cps[2] - 6;
                int b = cps[3] - 6;
                int aMinusB = (a + v - b) % v;
                int bMinusA = (b + v - a) % v;
                if (aMinusB < bMinusA) {
                    int tmp = b;
                    b = a;
                    a = tmp;
                }
                int next = (a == 1) ? 2 : (b + 2) % v;
                float dir = a == 1 ? 1 : -1;
                final float ux = dir * (in[cs[b + 6]][0] - in[cs[a + 6]][0]);
                final float uy = dir * (in[cs[b + 6]][1] - in[cs[a + 6]][1]);
                final float uz = dir * (in[cs[b + 6]][2] - in[cs[a + 6]][2]);
                final float vx = in[cs[next + 6]][0] - in[cs[5]][0];
                final float vy = in[cs[next + 6]][1] - in[cs[5]][1];
                final float vz = in[cs[next + 6]][2] - in[cs[5]][2];
                float[] normal = norm[outIndex];
                computeNormal(ux, uy, uz, vx, vy, vz, normal, corner * 6);
            } else {
                float ux = 0, uy = 0, uz = 0, vx = 0, vy = 0, vz = 0;
                for (int j = 0; j < valence; j++) {
                    int c2fi = j * 2 + 6;
                    int c2ei = j * 2 + 5;
                    if (c2ei == 5) {
                        c2ei = cs.length - 1;
                    }
                    int c3fi = c2fi + 2;
                    if (c3fi >= cs.length) {
                        c3fi -= cs.length - 6;
                    }
                    int c3ei = c2ei + 2;
                    if (c3ei >= cs.length) {
                        c3ei -= cs.length - 6;
                    }
                    float ew = TANGENT_EDGE_WEIGHT[valence][j];
                    float fw = TANGENT_FACE_WEIGHT[valence][j];
                    ux += in[cs[c3fi]][0] * fw;
                    uy += in[cs[c3fi]][1] * fw;
                    uz += in[cs[c3fi]][2] * fw;
                    ux += in[cs[c3ei]][0] * ew;
                    uy += in[cs[c3ei]][1] * ew;
                    uz += in[cs[c3ei]][2] * ew;
                    vx += in[cs[c2fi]][0] * fw;
                    vy += in[cs[c2fi]][1] * fw;
                    vz += in[cs[c2fi]][2] * fw;
                    vx += in[cs[c2ei]][0] * ew;
                    vy += in[cs[c2ei]][1] * ew;
                    vz += in[cs[c2ei]][2] * ew;
                    float[] normal = norm[outIndex];
                    computeNormal(ux, uy, uz, vx, vy, vz, normal, 0);
                    normal[9] = normal[6] = normal[3] = normal[0];
                    normal[10] = normal[7] = normal[4] = normal[1];
                    normal[11] = normal[8] = normal[5] = normal[2];
                }
            }
        }
        return 0;
    }

    private void computeNormal(final float ux, final float uy, final float uz, final float vx, final float vy, final float vz, final float[] normal, final int offset) {
        if (RENDERER_SETTINGS.softwareNormalize) {
            final float nx = vy * uz - vz * uy;
            final float ny = vz * ux - vx * uz;
            final float nz = vx * uy - vy * ux;
            final float nl = 1.0f / (float) sqrt(nx * nx + ny * ny + nz * nz);
            normal[offset] = nx * nl;
            normal[offset + 1] = ny * nl;
            normal[offset + 2] = nz * nl;
        } else {
            normal[offset] = vy * uz - vz * uy;
            normal[offset + 1] = vz * ux - vx * uz;
            normal[offset + 2] = vx * uy - vy * ux;
        }
    }

    private void quickDice(int[] valence, int depth) {
        for (int level = 1; level < depth; level++) {
            final int[][] stencil = patchStencil[level];
            final float[][] out = subdivPoints[level];
            final float[][] in = subdivPoints[level - 1];
            final int n = stencil.length - 2;
            for (int i = 2; i < n; i++) {
                final int[] s = stencil[i];
                final int outIndex = GRID_START + i;
                switch(s[0]) {
                    case EDGE_H:
                    case EDGE_V:
                        out[outIndex][0] = (in[s[7]][0] + in[s[8]][0]) * EDGE0 + ((in[s[9]][0] + in[s[10]][0]) + (in[s[11]][0] + in[s[12]][0])) * EDGE1;
                        break;
                    case POINT:
                        out[outIndex][0] = in[s[3]][0] * VERTEX0 + ((in[s[4]][0] + in[s[6]][0]) + (in[s[5]][0] + in[s[7]][0])) * VERTEX1 + ((in[s[8]][0] + in[s[10]][0]) + (in[s[9]][0] + in[s[11]][0])) * VERTEX2;
                        break;
                    case FACE:
                        out[outIndex][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
                        break;
                }
            }
            for (int corner = 0; corner < 2; corner++) {
                final int val = valence[corner];
                final int[] cs = cornerStencil[level][val - 3][corner];
                final int outIndex = cs[4];
                float f0 = 0, e0 = 0;
                for (int p = 6; p < cs.length; p++) {
                    f0 += in[cs[p++]][0];
                    e0 += in[cs[p]][0];
                }
                out[outIndex][0] = f0 * VERTEX_FACE[val] + e0 * VERTEX_EDGE[val] + in[cs[5]][0] * VERTEX_POINT[val];
                final int[][] array = fanStencil[level][val - 3][corner];
                final int m = array.length;
                for (int i = 0; i < m; i++) {
                    final int oi = MAX_FAN_LENGTH * corner + i;
                    final int[] s = array[i];
                    switch(s[0]) {
                        case EDGE:
                            out[oi][0] = (in[s[2]][0] + in[s[3]][0]) * EDGE0 + ((in[s[4]][0] + in[s[5]][0]) + (in[s[6]][0] + in[s[7]][0])) * EDGE1;
                            break;
                        case FACE:
                            out[oi][0] = ((in[s[1]][0] + in[s[3]][0]) + (in[s[2]][0] + in[s[4]][0])) * FACE0;
                            break;
                    }
                }
            }
        }
        final int level = depth - 1;
        final int[][] limitStencil = patchLimitStencil[level];
        final float[][] out = limitPoints[level];
        final float[][] in = subdivPoints[level];
        final int dim = (1 << (depth - 1)) + 3;
        for (int i = dim + 2, n = limitStencil.length - dim - 2; i < n; i++) {
            final int[] ls = limitStencil[i];
            if (ls == null) {
                continue;
            }
            final int outIndex = GRID_START + i;
            out[outIndex][0] = in[ls[0]][0] * LIMIT0 + ((in[ls[1]][0] + in[ls[3]][0]) + (in[ls[2]][0] + in[ls[4]][0])) * LIMIT1 + ((in[ls[5]][0] + in[ls[7]][0]) + (in[ls[6]][0] + in[ls[8]][0])) * LIMIT2;
            out[outIndex][1] = (in[ls[2]][0] - in[ls[4]][0]) * 4 + (in[ls[6]][0] - in[ls[5]][0]) + (in[ls[7]][0] - in[ls[8]][0]);
            out[outIndex][2] = (in[ls[1]][0] - in[ls[3]][0]) * 4 + (in[ls[5]][0] - in[ls[8]][0]) + (in[ls[6]][0] - in[ls[7]][0]);
        }
        for (int corner = 0; corner < 2; corner++) {
            final int val = valence[corner];
            final int[] cs = cornerLimitStencil[level][val - 3][corner];
            final int outIndex = cs[4];
            float f0 = 0, e0 = 0;
            for (int p = 6; p < cs.length; p++) {
                f0 += in[cs[p++]][0];
                e0 += in[cs[p]][0];
            }
            out[outIndex][0] = e0 * VERTEX_EDGE_LIMIT[val] + f0 * VERTEX_FACE_LIMIT[val] + in[cs[5]][0] * VERTEX_POINT_LIMIT[val];
            float ax = 0;
            float bx = 0;
            for (int j = 0; j < val; j++) {
                int c2fi = j * 2 + 6;
                int c2ei = j * 2 + 5;
                if (c2ei == 5) {
                    c2ei = cs.length - 1;
                }
                int c3fi = c2fi + 2;
                if (c3fi >= cs.length) {
                    c3fi -= cs.length - 6;
                }
                int c3ei = c2ei + 2;
                if (c3ei >= cs.length) {
                    c3ei -= cs.length - 6;
                }
                float ew = TANGENT_EDGE_WEIGHT[val][j];
                float fw = TANGENT_FACE_WEIGHT[val][j];
                ax += in[cs[c3fi]][0] * fw;
                ax += in[cs[c3ei]][0] * ew;
                bx += in[cs[c2fi]][0] * fw;
                bx += in[cs[c2ei]][0] * ew;
            }
            out[outIndex][1] = ax;
            out[outIndex][2] = bx;
        }
    }

    private static int cornerStencilLength(int valence) {
        return max(valence * 2 - 5, 2);
    }

    private static int patchIndex(int level, int row, int column) {
        int dim = (1 << (level - 1)) + 3;
        if (row < 0) {
            row += dim;
        }
        if (column < 0) {
            column += dim;
        }
        if ((row == 0 && column == 0) || (row == dim - 1 && column == dim - 1)) {
            throw new IllegalArgumentException("level=" + level + " row=" + row + " column=" + column);
        } else if (row == 0 && column == 1) {
            return 0;
        } else if (row == 1 && column == 0) {
            return 1;
        } else if (row == dim - 1 && column == dim - 2) {
            return MAX_FAN_LENGTH;
        } else if (row == dim - 2 && column == dim - 1) {
            return MAX_FAN_LENGTH + 1;
        } else {
            return GRID_START + row * dim + column;
        }
    }

    /**
	 * Computes the geometry array index for the specified row/column and rotating the specified corner
	 * to the opper left side.
	 * @param corner 0 for the upper left (outer) corner, 1 for the lower right (inner) corner
	 * @param level
	 * @param row
	 * @param column
	 * @return
	 */
    private static int patchCornerIndex(int corner, int level, int row, int column) {
        int dim = (1 << (level - 1)) + 3;
        switch(corner) {
            case 0:
                return patchIndex(level, row, column);
            case 1:
                return patchIndex(level, dim - 1 - row, dim - 1 - column);
            default:
                throw new IllegalArgumentException("" + corner);
        }
    }

    /**
	 * Computes the geometry array index for a given corner (fan) element.
	 */
    private static int cornerIndex(int corner, int valence, int i) {
        if (i < 0 || i >= cornerStencilLength(valence)) {
            throw new IllegalArgumentException(Integer.toString(i));
        }
        return MAX_FAN_LENGTH * corner + i;
    }

    private static int cornerIndex2(int corner, int level, int valence, int i) {
        int max = cornerStencilLength(valence);
        if (i == -2) {
            return patchCornerIndex(corner, level, 2, 1);
        } else if (i == -1) {
            return patchCornerIndex(corner, level, 2, 0);
        } else if (i == max) {
            return patchCornerIndex(corner, level, 0, 2);
        } else if (i == max + 1) {
            return patchCornerIndex(corner, level, 1, 2);
        } else {
            if (i == max - 1) {
                return cornerIndex(corner, valence, 0);
            } else {
                return cornerIndex(corner, valence, i + 1);
            }
        }
    }

    private boolean dumpStencil(int level) {
        boolean result = false;
        int dim = (1 << (level)) + 3;
        System.out.println();
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                int index = i * dim + j;
                switch(patchStencil[level][index][0]) {
                    case UNUSED:
                        System.out.print(".");
                        break;
                    case POINT:
                        System.out.print("P");
                        break;
                    case FACE:
                        System.out.print("F");
                        break;
                    case EDGE_H:
                        System.out.print("Eh");
                        break;
                    case EDGE_V:
                        System.out.print("Hv");
                        break;
                    case CREASE_5_7:
                        System.out.print("Ch");
                        break;
                    case CREASE_4_6:
                        System.out.print("Cv");
                        break;
                }
                if (patchStencil[level][index][0] != FACE && patchStencil[level][index][0] != UNUSED) {
                    if (patchStencil[level][index][1] > 0) {
                        result = true;
                    }
                    System.out.print(patchStencil[level][index][1]);
                }
                System.out.print("\t");
            }
            System.out.println();
        }
        return result;
    }

    private class Tester {

        JFrame frame = new JFrame();

        int level = 4;

        int valence = 3;

        final String[] TYPE = { ".", "E", "-", "|", "F", "P", "C" };

        final int SIZE = 300;

        final int OFF = 250;

        int sx, sy;

        int[][][] looktable = new int[99][99][];

        JPanel panel = new JPanel() {

            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.translate(OFF, OFF);
                g.setFont(new Font("sans serif", Font.PLAIN, 8));
                int dim = getDimension(level);
                int dim1 = getDimension(level - 1);
                Point p = new Point();
                Point p1 = new Point();
                int[] selectedStencil = null;
                if (sx > -10 && sy > -10) {
                    selectedStencil = looktable[sx + 10][sy + 10];
                }
                if (selectedStencil != null) {
                    int stencil = selectedStencil[0];
                    System.out.println(stencil);
                    g.setColor(Color.RED);
                    g.fillRect(sx * SIZE / (dim - 3), sy * SIZE / (dim - 3) - 8, 9, 9);
                    int start = 0;
                    int end = 0;
                    if (stencil == FACE) {
                        start = 1;
                        end = 5;
                    } else if (stencil == POINT) {
                        start = 3;
                        end = 12;
                    } else if (stencil == EDGE_H || stencil == EDGE_V) {
                        start = 7;
                        end = 13;
                    } else if (stencil == EDGE) {
                        start = 2;
                        end = 8;
                    } else if (stencil == 6) {
                        start = 2;
                        end = valence * 2 + 3;
                    }
                    for (int j = start; j < end; j++) {
                        int idx = selectedStencil[j];
                        g.drawString(Integer.toString(idx), -200, (j - start + 1) * 16);
                        getPos(level - 1, idx, p1);
                        if (p1.x > 0) {
                            if (p1.x < dim1 - 2) {
                                p1.x *= 2;
                            } else {
                                p1.x = dim + p1.x - dim1;
                            }
                        }
                        if (p1.y > 0) {
                            if (p1.y < dim1 - 2) {
                                p1.y *= 2;
                            } else {
                                p1.y = dim + p1.y - dim1;
                            }
                        }
                        g.drawRect(p1.x * SIZE / (dim - 3) - 2, p1.y * SIZE / (dim - 3) - 10, 12, 12);
                    }
                }
                for (int i = 0, n = dim * dim; i < n; i++) {
                    int index = GRID_START + i;
                    getPos(level, index, p);
                    int stencil = patchStencil[level][i][0];
                    boolean hl = (p.x & 1) == 0 && (p.y & 1) == 0;
                    g.setColor(hl ? Color.BLACK : Color.LIGHT_GRAY);
                    g.drawString(TYPE[stencil], p.x * SIZE / (dim - 3), p.y * SIZE / (dim - 3));
                }
                for (int corner = 0; corner < 2; corner++) {
                    for (int i = 0; i < valence * 2 - 5; i++) {
                        getFanPos(level, valence, corner, i, p);
                        int stencil = fanStencil[level][valence - 3][corner][i][0];
                        g.drawString(TYPE[stencil] + i, p.x * SIZE / (dim - 3), p.y * SIZE / (dim - 3));
                    }
                }
            }
        };

        Tester() {
            fillLooktable();
            panel.setBackground(Color.WHITE);
            panel.addMouseWheelListener(new MouseWheelListener() {

                public void mouseWheelMoved(MouseWheelEvent e) {
                    level += e.getWheelRotation();
                    if (level < 1) level = 1;
                    if (level > 4) level = 4;
                    fillLooktable();
                    panel.repaint();
                }
            });
            panel.addMouseMotionListener(new MouseMotionAdapter() {

                public void mouseMoved(MouseEvent e) {
                    int dim = getDimension(level);
                    int off = SIZE / (dim - 3) / 2;
                    sx = (int) Math.floor(((e.getX() - OFF + off) * (dim - 3.0) + SIZE) / SIZE - 1);
                    sy = (int) Math.floor(((e.getY() - OFF + 10 + off) * (dim - 3.0) + SIZE) / SIZE - 1);
                    panel.repaint();
                }
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800);
            frame.add(panel);
            frame.setVisible(true);
        }

        private void fillLooktable() {
            for (int i = 0; i < looktable.length; i++) {
                for (int j = 0; j < looktable[i].length; j++) {
                    looktable[i][j] = null;
                }
            }
            int dim = getDimension(level);
            Point p = new Point();
            for (int i = 0, n = dim * dim; i < n; i++) {
                int index = GRID_START + i;
                getPos(level, index, p);
                if (p.x > -10 && p.y > -10) {
                    looktable[p.x + 10][p.y + 10] = patchStencil[level][i];
                }
            }
            for (int corner = 0; corner < 2; corner++) {
                for (int i = 0; i < valence * 2 - 5; i++) {
                    getFanPos(level, valence, corner, i, p);
                    looktable[p.x + 10][p.y + 10] = fanStencil[level][valence - 3][corner][i];
                }
            }
            looktable[10][10] = cornerStencil[level][valence - 3][0];
            looktable[10][10][0] = 6;
            looktable[dim + 7][dim + 7] = cornerStencil[level][valence - 3][1];
            looktable[dim + 7][dim + 7][0] = 6;
        }

        private void getPos(int level, int index, Point p) {
            int dim = getDimension(level);
            if (index >= GRID_START) {
                int row = (index - GRID_START) / dim;
                int column = (index - GRID_START) % dim;
                p.setLocation(column - 1, row - 1);
            } else {
                if (index < MAX_FAN_LENGTH) {
                    getFanPos(level, valence, 0, index, p);
                } else {
                    getFanPos(level, valence, 1, index - MAX_FAN_LENGTH, p);
                }
            }
        }

        private void getFanPos(int level, int valence, int corner, int index, Point p) {
            int[][] stencil = fanStencil[level][valence - 3][corner];
            int m = valence - 2;
            int dim = getDimension(level);
            if (index == 0) {
                index = valence * 2 - 5;
            }
            if (corner == 0) {
                if (index < m) {
                    p.setLocation(-3, -3 + m - index);
                } else {
                    p.setLocation(-3 - m + index, -3);
                }
            } else if (corner == 1) {
                if (index < m) {
                    p.setLocation(dim, dim - m + index);
                } else {
                    p.setLocation(dim + m - index, dim);
                }
            } else {
                throw new IllegalArgumentException();
            }
        }

        private int getIndex(int level, int row, int column) {
            int dim = getDimension(level);
            return GRID_START + row * dim + column;
        }

        private int getDimension(int level) {
            return ((1 << level)) + 3;
        }
    }
}
