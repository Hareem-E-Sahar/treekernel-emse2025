package santa.nice.ocr.kernel.vision;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;
import santa.nice.ocr.kernel.OCRUtility;
import santa.nice.util.ComparablePair;
import santa.nice.util.Pair;

public class SimpleSynaptics {

    private Rectangle paintFill(final int[][] distanceMap, int startRow, int startCol) {
        HashSet<Pair<Integer, Integer>> closed = new HashSet<Pair<Integer, Integer>>();
        Stack<Pair<Integer, Integer>> open = new Stack<Pair<Integer, Integer>>();
        open.push(new Pair<Integer, Integer>(startRow, startCol));
        int left = distanceMap[0].length;
        int right = 0;
        int top = distanceMap.length;
        int bottom = 0;
        while (open.empty() == false) {
            Pair<Integer, Integer> p = open.pop();
            closed.add(p);
            distanceMap[p.first][p.second] = -1;
            if (p.first > bottom) bottom = p.first;
            if (p.first < top) top = p.first;
            if (p.second < left) left = p.second;
            if (p.second > right) right = p.second;
            if (p.first - 1 >= 0 && distanceMap[p.first - 1][p.second] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first - 1, p.second);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && distanceMap[p.first + 1][p.second] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first + 1, p.second);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.second - 1 >= 0 && distanceMap[p.first][p.second - 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.second + 1 < distanceMap[0].length && distanceMap[p.first][p.second + 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first - 1 >= 0 && p.second - 1 >= 0 && distanceMap[p.first - 1][p.second - 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first - 1, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first - 1 >= 0 && p.second + 1 < distanceMap[0].length && distanceMap[p.first - 1][p.second + 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first - 1, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && p.second - 1 >= 0 && distanceMap[p.first + 1][p.second - 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first + 1, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && p.second + 1 < distanceMap[0].length && distanceMap[p.first + 1][p.second + 1] > 0) {
                Pair<Integer, Integer> p2 = new Pair<Integer, Integer>(p.first + 1, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
        }
        upext: for (int row = Math.max(0, top - (bottom - top) / 3); row < top; row++) {
            for (int col = left; col <= right; col++) {
                if (distanceMap[row][col] > 0) {
                    top = row;
                    break upext;
                }
            }
        }
        downext: for (int row = Math.min(distanceMap.length - 1, bottom + (bottom - top) / 3); row > bottom; row--) {
            for (int col = left; col <= right; col++) {
                if (distanceMap[row][col] > 0) {
                    bottom = row;
                    break downext;
                }
            }
        }
        return new Rectangle(left - 1, top - 1, right - left + 3, bottom - top + 3);
    }

    private int findMiddleLine(final int[][] distanceMap, int startRow, int startCol) {
        HashSet<ComparablePair<Integer, Integer>> closed = new HashSet<ComparablePair<Integer, Integer>>();
        Stack<ComparablePair<Integer, Integer>> open = new Stack<ComparablePair<Integer, Integer>>();
        open.push(new ComparablePair<Integer, Integer>(startRow, startCol));
        int left = distanceMap[0].length;
        int right = 0;
        int top = distanceMap.length;
        int bottom = 0;
        while (open.empty() == false) {
            ComparablePair<Integer, Integer> p = open.pop();
            closed.add(p);
            distanceMap[p.first][p.second] = -2;
            if (p.first > bottom) bottom = p.first;
            if (p.first < top) top = p.first;
            if (p.second < left) left = p.second;
            if (p.second > right) right = p.second;
            if (p.first - 1 >= 0 && distanceMap[p.first - 1][p.second] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first - 1, p.second);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && distanceMap[p.first + 1][p.second] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first + 1, p.second);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.second - 1 >= 0 && distanceMap[p.first][p.second - 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.second + 1 < distanceMap[0].length && distanceMap[p.first][p.second + 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first - 1 >= 0 && p.second - 1 >= 0 && distanceMap[p.first - 1][p.second - 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first - 1, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first - 1 >= 0 && p.second + 1 < distanceMap[0].length && distanceMap[p.first - 1][p.second + 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first - 1, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && p.second - 1 >= 0 && distanceMap[p.first + 1][p.second - 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first + 1, p.second - 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
            if (p.first + 1 < distanceMap.length && p.second + 1 < distanceMap[0].length && distanceMap[p.first + 1][p.second + 1] > 0) {
                ComparablePair<Integer, Integer> p2 = new ComparablePair<Integer, Integer>(p.first + 1, p.second + 1);
                if (closed.contains(p2) == false) open.push(p2);
            }
        }
        for (int row = top; row <= bottom; row++) {
            for (int col = left; col <= right; col++) {
                if (distanceMap[row][col] == -2) distanceMap[row][col] = 1;
            }
        }
        return (top + bottom) / 2;
    }

    public Vector<VisionResult> pick(BufferedImage image) {
        Color bgColor = OCRUtility.nearestCommonColor(image);
        int[][] distanceMap = new int[image.getHeight()][image.getWidth()];
        int[] distanceHistogram = new int[768];
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                distanceMap[row][col] = OCRUtility.colorDistance(bgColor, image.getRGB(col, row));
                distanceHistogram[distanceMap[row][col]]++;
            }
        }
        int highestHistogramPosition = 0;
        int histogramValue = 0;
        for (int i = 1; i < 768; i++) {
            if (distanceHistogram[i] > histogramValue) {
                highestHistogramPosition = i;
                histogramValue = distanceHistogram[highestHistogramPosition];
            }
        }
        int higherHistogramPosition = 0;
        histogramValue = 0;
        for (int i = highestHistogramPosition * 8; i < 768; i++) {
            if (distanceHistogram[i] > histogramValue && distanceHistogram[i] < distanceHistogram[highestHistogramPosition]) {
                higherHistogramPosition = i;
                histogramValue = distanceHistogram[higherHistogramPosition];
            }
        }
        int threshold = (highestHistogramPosition + higherHistogramPosition) / 2;
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                if (distanceMap[row][col] < threshold) {
                    distanceMap[row][col] = 0;
                } else {
                    distanceMap[row][col] = 1;
                }
            }
        }
        Vector<VisionResult> v = new Vector<VisionResult>();
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                if (distanceMap[row][col] > 0) {
                    row = findMiddleLine(distanceMap, row, col);
                    break;
                }
            }
            int lastX = -1;
            int nextPos = -1;
            for (int col = 0; col < image.getWidth(); col++) {
                if (distanceMap[row][col] > 0) {
                    Rectangle rect = paintFill(distanceMap, row, col);
                    if (rect.x >= 0 && rect.x + rect.width < image.getWidth() && rect.y >= 0 && rect.y + rect.getHeight() < image.getHeight() && rect.getHeight() + rect.getWidth() > 12) {
                        if (lastX == -1 || rect.x - lastX > rect.height / 3) v.add(null);
                        int[][] binImage = new int[rect.height][rect.width];
                        for (int i = 0; i < rect.height; i++) {
                            for (int j = 0; j < rect.width; j++) {
                                if (distanceMap[i + rect.y][j + rect.x] == -1) binImage[i][j] = 1; else binImage[i][j] = 0;
                            }
                        }
                        v.add(new VisionResult(image.getSubimage(rect.x, rect.y, rect.width, rect.height), binImage));
                        lastX = rect.x + rect.width;
                        nextPos = rect.y + rect.height;
                    }
                }
            }
            if (nextPos != -1) row = nextPos;
        }
        return v;
    }
}
