package cn.edu.tsinghua.thss.alg.closestpair.algorithm;

import org.apache.log4j.Logger;
import cn.edu.tsinghua.thss.alg.closestpair.model.PairResult;
import cn.edu.tsinghua.thss.alg.closestpair.model.Point;

public class ClosestPair {

    private Logger log = Logger.getLogger(this.getClass());

    private static double[][] temp;

    /**
	 * Find the closest pair in O(nlogn) time.
	 * 
	 * @param points
	 *            points
	 * @param size
	 *            the number of points
	 * @return
	 */
    private static PairResult combine(double[][] points, int l, int mid, int r, PairResult pr) {
        double shortest = pr.getDistance();
        double d = pr.getDistance();
        double distance = 0;
        int left = mid, right = mid, k = 0;
        while (left >= 0 && (points[left][0] > (points[mid][0] - d))) left--;
        while (right <= r && (points[right][0] < (points[mid][0] + d))) right++;
        left++;
        right--;
        for (int i = left; i <= right; i++) {
            temp[k][0] = points[i][0];
            temp[k][1] = points[i][1];
            k++;
        }
        k = k - 1;
        ClosestPair.quickSortY(temp, 0, k);
        double x1 = pr.getX1(), y1 = pr.getY1();
        double x2 = pr.getX2(), y2 = pr.getY2();
        for (int i = 0; i <= k; i++) {
            int top = Math.min(i + 8, k);
            for (int j = i + 1; j <= top; j++) {
                if ((temp[i][0] - points[mid][0]) * (temp[j][0] - points[mid][0]) > 0) continue;
                distance = getDist(temp[i][0], temp[i][1], temp[j][0], temp[j][1]);
                if (distance < shortest) {
                    shortest = distance;
                    x1 = temp[i][0];
                    y1 = temp[i][1];
                    x2 = temp[j][0];
                    y2 = temp[j][1];
                }
            }
        }
        return new PairResult(x1, y1, x2, y2, shortest);
    }

    public static PairResult findClosestPair(double[][] points, int l, int r) {
        if (l + 2 < r) {
            int mid = (r + l) / 2;
            PairResult pr1 = ClosestPair.findClosestPair(points, l, mid);
            PairResult pr2 = ClosestPair.findClosestPair(points, mid + 1, r);
            PairResult pr;
            if (pr1.getDistance() < pr2.getDistance()) pr = pr1; else pr = pr2;
            pr = ClosestPair.combine(points, l, mid, r, pr);
            return pr;
        } else return ClosestPair.findClosestPairSimple(points, l, r);
    }

    public static PairResult findClosestPair(double[][] points, int size) {
        temp = new double[size][2];
        quickSortX(points, 0, size - 1);
        return findClosestPair(points, 0, size - 1);
    }

    /**
	 * Find the closest pair in O(n^2) time
	 * 
	 * @param points
	 *            points
	 * @param size
	 *            the number of points
	 * @return
	 */
    public static PairResult findClosestPairSimple(double[][] points, int l, int r) {
        int size = r - l + 1;
        if (size <= 1) return null;
        double shortest = getDist(points[l][0], points[l][1], points[l + 1][0], points[l + 1][1]);
        double temp = 0;
        double x1 = points[l][0], y1 = points[l][1];
        double x2 = points[l + 1][0], y2 = points[l + 1][1];
        for (int i = l; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                temp = getDist(points[i][0], points[i][1], points[j][0], points[j][1]);
                if (temp < shortest) {
                    shortest = temp;
                    x1 = points[i][0];
                    y1 = points[i][1];
                    x2 = points[j][0];
                    y2 = points[j][1];
                }
            }
        }
        return new PairResult(x1, y1, x2, y2, shortest);
    }

    public static PairResult findClosestPairSimple(double[][] points, int size) {
        PairResult pr = ClosestPair.findClosestPairSimple(points, 0, size - 1);
        return pr;
    }

    /**
	 * Get distance between two distinct points
	 * 
	 * @param x1
	 *            the x coordinate of the first point
	 * @param y1
	 *            the y coordinate of the first point
	 * @param x2
	 *            the x coordinate of the second point
	 * @param y2
	 *            the y coordinate of the second point
	 * @return the distance
	 */
    private static double getDist(double x1, double y1, double x2, double y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
    }

    /**
	 * Sort the points by quick sorting technique
	 * 
	 * @param points
	 *            points to sort
	 * @param size
	 *            number of points
	 * @return does sorting succeed
	 */
    private static int partitionX(double[][] points, int l, int r) {
        double compare = points[r][0];
        double x = 0, y = 0;
        int i = l - 1;
        for (int j = l; j < r; j++) {
            if (points[j][0] <= compare) {
                i = i + 1;
                x = points[i][0];
                y = points[i][1];
                points[i][0] = points[j][1];
                points[i][1] = points[j][1];
                points[j][0] = x;
                points[j][1] = y;
            }
        }
        x = points[r][0];
        y = points[r][1];
        points[r][0] = points[i + 1][0];
        points[r][1] = points[i + 1][1];
        points[i + 1][0] = x;
        points[i + 1][1] = y;
        return i + 1;
    }

    public static boolean quickSortX(double[][] points, int l, int r) {
        if (l < r) {
            int mid = ClosestPair.partitionX(points, l, r);
            ClosestPair.quickSortX(points, l, mid - 1);
            ClosestPair.quickSortX(points, mid + 1, r);
        }
        return true;
    }

    private static int partitionY(double[][] points, int l, int r) {
        double compare = points[r][1];
        double x = 0, y = 0;
        int i = l - 1;
        for (int j = l; j < r; j++) {
            if (points[j][1] <= compare) {
                i = i + 1;
                x = points[i][0];
                y = points[i][1];
                points[i][0] = points[j][0];
                points[i][1] = points[j][1];
                points[j][0] = x;
                points[j][1] = y;
            }
        }
        x = points[r][0];
        y = points[r][1];
        points[r][0] = points[i + 1][0];
        points[r][1] = points[i + 1][1];
        points[i + 1][0] = x;
        points[i + 1][1] = y;
        return i + 1;
    }

    private static boolean quickSortY(double[][] points, int l, int r) {
        if (l < r) {
            int mid = ClosestPair.partitionY(points, l, r);
            ClosestPair.quickSortY(points, l, mid - 1);
            ClosestPair.quickSortY(points, mid + 1, r);
        }
        return true;
    }
}
