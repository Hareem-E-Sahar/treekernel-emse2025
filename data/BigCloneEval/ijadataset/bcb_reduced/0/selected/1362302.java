package com.bwepow.wwj.mote;

import static java.lang.StrictMath.*;

/**
 *
 * @author Giampaolo Melis
 */
public class AffineTransformSolver {

    public static double computeAngle(double dx, double dy) {
        double angle = 0.0;
        if (dx == 0.0) {
            angle = PI / 2.0;
        } else {
            if (dx > 0.0) {
                return atan(dy / dx);
            } else {
                angle = (atan(dy / dx) + PI);
            }
        }
        return angle;
    }

    public static double[][] solve2Dto3x3(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double scale = distance(x3, x4, y3, y4) / distance(x1, x2, y1, y2);
        double theta = computeAngle((x4 - x3), (y4 - y3)) - computeAngle((x2 - x1), (y2 - y1));
        double tx1 = (x2 + x1) / 2;
        double ty1 = (y2 + y1) / 2;
        double tx2 = (x4 + x3) / 2;
        double ty2 = (y4 + y3) / 2;
        double[][] result = new double[3][3];
        result[2][0] = 0.0f;
        result[2][1] = 0.0f;
        result[2][2] = 1.0f;
        result[0][0] = scale * cos(theta);
        result[0][1] = -(scale * sin(theta));
        result[0][2] = -tx1 * result[0][0] - ty1 * result[1][0] + tx2;
        result[1][0] = scale * sin(theta);
        result[1][1] = scale * cos(theta);
        result[1][2] = -tx1 * result[0][1] - ty1 * result[1][1] + ty2;
        return result;
    }

    public static double getScale(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return distance(x3, x4, y3, y4) / distance(x1, x2, y1, y2);
    }

    public static double getTheta(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return computeAngle((x4 - x3), (y4 - y3)) - computeAngle((x2 - x1), (y2 - y1));
    }

    public static double distance(double x1, double x2, double y1, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return sqrt(dx * dx + dy * dy);
    }

    public static double[][] solve2Dto4x4(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double scale = distance(x3, x4, y3, y4) / distance(x1, x2, y1, y2);
        double theta = computeAngle((x4 - x3), (y4 - y3)) - computeAngle((x2 - x1), (y2 - y1));
        double tx1 = (x2 + x1) / 2;
        double ty1 = (y2 + y1) / 2;
        double tx2 = (x4 + x3) / 2;
        double ty2 = (y4 + y3) / 2;
        double[][] result = new double[4][4];
        result[0][0] = 1.0f;
        result[1][1] = 1.0f;
        result[2][2] = 1.0f;
        result[3][3] = 1.0f;
        result[0][0] = scale * cos(theta);
        result[0][1] = -(scale * sin(theta));
        result[0][3] = -tx1 * result[0][0] - ty1 * result[1][0] + tx2;
        result[1][0] = scale * sin(theta);
        result[1][1] = scale * cos(theta);
        result[1][3] = -tx1 * result[0][1] - ty1 * result[1][1] + ty2;
        return result;
    }
}
