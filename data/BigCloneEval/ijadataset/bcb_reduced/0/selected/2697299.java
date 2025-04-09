package AccordionDrawer;

import RedBlackTree.RBTree;
import java.awt.Color;
import java.util.Hashtable;
import java.util.SortedSet;

/**
 * @author Hilde, Qiang Kong
 */
public class DynSplitLine extends RBTree {

    public static final double defaultMinStuckValue = 0.001f;

    public static final double defaultMaxStuckValue = 0.999f;

    boolean horizontal;

    double minStuckValue, maxStuckValue;

    public AccordionDrawer ad;

    public DynSplitLine(AccordionDrawer ad, boolean horizontal, double minStuckValue, double maxStuckValue) {
        super();
        this.ad = ad;
        this.horizontal = horizontal;
        this.minStuckValue = minStuckValue;
        this.maxStuckValue = maxStuckValue;
    }

    public DynSplitCell getSplitLineIndex(double screenPosition) {
        if (screenPosition < minStuckValue || screenPosition > maxStuckValue) {
            return null;
        }
        if (getRootCell() == null) return null;
        DynSplitCell currentCell = getRootCell();
        DynSplitCell returnCell = currentCell;
        boolean rightFlag = false;
        while (currentCell != null) {
            returnCell = currentCell;
            rightFlag = false;
            if (screenPosition == getAbsoluteValue(currentCell.getIndex())) return currentCell; else if (screenPosition < getAbsoluteValue(currentCell.getIndex())) currentCell = currentCell.getLeftChild(); else {
                currentCell = currentCell.getRightChild();
                rightFlag = true;
            }
        }
        if (!rightFlag) return returnCell; else return returnCell.getNextCell();
    }

    public double getAbsoluteValue(long index) {
        if (index == min) return minStuckValue;
        if (index == max) return maxStuckValue;
        computePlaceThisFrame(index);
        if (index > min && index < max) return getCell(index).getAbsoluteValue();
        System.out.println("DynSplitLine::getAbsoluteValue::Illegal absolute value index: " + index);
        return -1f;
    }

    public void setMaxStuckValue(double f) {
        maxStuckValue = f;
    }

    public void setMinStuckValue(double f) {
        minStuckValue = f;
    }

    public double getMaxStuckValue() {
        return maxStuckValue;
    }

    public double getMinStuckValue() {
        return minStuckValue;
    }

    public DynSplitCell getCell(long cellIndex) {
        DynSplitCell current = getRootCell();
        while (current != null) {
            if (cellIndex == current.getIndex()) {
                return current;
            } else if (cellIndex < current.getIndex()) current = current.getLeftChild(); else current = current.getRightChild();
        }
        return null;
    }

    public long getMinLine(long cellIndex) {
        DynSplitCell returnCell = getCell(cellIndex);
        if (returnCell != null) {
            return returnCell.min;
        } else return -1;
    }

    public long getMaxLine(long cellIndex) {
        DynSplitCell returnCell = getCell(cellIndex);
        if (returnCell != null) {
            return returnCell.max;
        } else return -1;
    }

    public long getParentSplitIndex(long cellIndex) {
        DynSplitCell returnCell = getCell(cellIndex);
        if (returnCell != null && !isRoot(cellIndex)) {
            if (returnCell == getRootCell()) System.out.println("getParentSplitIndex:: reached root!");
            return returnCell.getParent().getIndex();
        } else return -1;
    }

    public void computePlaceThisFrame(long index) {
        min = getRootCell().min;
        max = getRootCell().max;
        DynSplitCell cell = getCell(index);
        long cellIndex = cell.getIndex();
        if (cellIndex < min || cellIndex > max) {
            System.out.println("DynSplitLine::computePlaceThisFrame::Bad index value: " + cellIndex);
            return;
        }
        int frameNum = ad.getFrameNum();
        if (cellIndex == min) {
            cell.setAbsoluteValue(this.minStuckValue);
            return;
        } else if (cellIndex == max) {
            cell.setAbsoluteValue(this.maxStuckValue);
            return;
        }
        if (cellIndex > min && cellIndex < max && frameNum > cell.computedFrame) {
            int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
            if (!isRoot(cellIndex)) {
                DynSplitCell parentCell = cell.getParent();
                long parent = parentCell.getIndex();
                computePlaceThisFrame(parent);
                if (cell.isLeftChild()) {
                    long minLine = getMinLine(parent);
                    computePlaceThisFrame(minLine);
                    double parMinPosition = minLine == min ? minStuckValue : this.getCell(minLine).getAbsoluteValue();
                    double parSplitPosition = parentCell.getAbsoluteValue();
                    double range = parSplitPosition - parMinPosition;
                    cell.setAbsoluteValue(parMinPosition + range * cell.getSplitValue());
                    if (cell.getAbsoluteValue() < minStuckValue || cell.getAbsoluteValue() > maxStuckValue) System.out.println("Bad absolute value: " + cellIndex + " " + cell.getAbsoluteValue() + " " + minStuckValue + " " + maxStuckValue);
                } else {
                    long maxLine = getMaxLine(parent);
                    computePlaceThisFrame(maxLine);
                    double parMaxPosition = maxLine == max ? maxStuckValue : this.getCell(maxLine).getAbsoluteValue();
                    double parSplitPosition = parentCell.getAbsoluteValue();
                    double range = parMaxPosition - parSplitPosition;
                    cell.setAbsoluteValue(parSplitPosition + range * cell.getSplitValue());
                    if (cell.getAbsoluteValue() < minStuckValue || cell.getAbsoluteValue() > maxStuckValue) System.out.println("Bad absolute value: " + cellIndex + " " + cell.getAbsoluteValue() + " " + minStuckValue + " " + maxStuckValue);
                }
            } else {
                double myMinPosition = minStuckValue;
                double myMaxPosition = maxStuckValue;
                double range = myMaxPosition - myMinPosition;
                cell.setAbsoluteValue(myMinPosition + range * cell.getSplitValue());
                if (cell.getAbsoluteValue() < minStuckValue || cell.getAbsoluteValue() > maxStuckValue) System.out.println("Bad absolute value: " + cellIndex + " " + cell.getAbsoluteValue() + " " + minStuckValue + " " + maxStuckValue);
            }
            cell.setComputedFrame(frameNum);
        }
    }

    public void drawBackground(Color color) {
    }

    public int getMinIndexForPixelValue(int pixel) {
        return 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public DynSplitCell getRootCell() {
        if (this.getRoot() != sentinel) return (DynSplitCell) this.getRoot(); else return null;
    }

    public void drawRange(long min, long max, double pixelSize) {
    }

    private int getIndexForRangeSplit(long[] splitIndices, long rootIndex, int min, int max) {
        if (rootIndex == splitIndices[min] || rootIndex == splitIndices[max]) {
            System.out.println("Error: root index (" + rootIndex + ") shouldn't equal splitIndices[min] (" + splitIndices[min] + ") or splitIndices[max] (" + splitIndices[max] + ") when finding a split range");
        }
        int mid = (min + max) / 2;
        while (min + 1 < max && splitIndices[mid] != rootIndex) {
            if (splitIndices[mid] < rootIndex) min = mid; else max = mid;
            mid = (min + max) / 2;
        }
        mid = (min + max) / 2;
        return mid;
    }

    private boolean rangeSizeBigEnough(int min, int max, double pixelSize) {
        double size = getAbsoluteValue(max) - getAbsoluteValue(min);
        return size >= pixelSize;
    }

    public void moveLine(long dragIndex, int dragPixelEnd, long staticIndex, int numSteps, Hashtable newToMove) {
        ad.incrementFrameNumber();
        if (dragIndex == staticIndex) return;
        int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
        if (dragIndex == min || dragIndex == max) return;
        long[] range = { min, dragIndex, staticIndex, max };
        double dragEnd = (double) ad.s2w(dragPixelEnd, xy);
        if (dragEnd < minStuckValue + ad.minContextPeriphery || dragEnd > maxStuckValue - ad.minContextPeriphery) return;
        double staticEnd = getAbsoluteValue(staticIndex);
        double dragStart = getAbsoluteValue(dragIndex);
        double[] startValue = { minStuckValue, dragStart, staticEnd, maxStuckValue };
        double[] endValue = { minStuckValue, dragEnd, staticEnd, maxStuckValue };
        if (dragIndex > staticIndex) {
            range[1] = staticIndex;
            endValue[1] = staticEnd;
            startValue[1] = staticEnd;
            range[2] = dragIndex;
            endValue[2] = dragEnd;
            startValue[2] = dragStart;
        }
        int min = 0, max = range.length - 1;
        while (range[min] == range[min + 1]) min++;
        while (range[max] == range[max - 1]) max--;
        resizeRanges(range, startValue, endValue, numSteps, getRoot().getIndex(), min, max, newToMove);
    }

    private void resizeRanges(long[] splitIndices, double[] startValues, double[] endValues, int numSteps, long currRootIndex, int min, int max, Hashtable newToMove) {
        if (max <= min) {
            System.out.println("min == max in resize ranges: min:" + min + " max:" + max + " root:" + currRootIndex);
            return;
        }
        if (max - min <= 1) {
            return;
        }
        int index = getIndexForRangeSplit(splitIndices, currRootIndex, min, max);
        int xy = horizontal ? AccordionDrawer.X : AccordionDrawer.Y;
        double rootStartSize = startValues[max] - startValues[min];
        double rootEndSize = endValues[max] - endValues[min];
        if (rootStartSize < 0 || rootEndSize < 0) {
            System.out.println("this is probably a synchronization problem");
            return;
        }
        if (splitIndices[index] == currRootIndex) {
            SplitTransition st = new SplitTransition(this, currRootIndex, (endValues[index] - endValues[min]) / (rootEndSize), numSteps);
            newToMove.put(st.getHashKey(), st);
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], true), min, index, newToMove);
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], false), index, max, newToMove);
        } else {
            double rootOldLocation = getAbsoluteValue(currRootIndex);
            double rootOldSplit = getCell(currRootIndex).splitValue;
            double rootOldRangeSize = startValues[index + 1] - startValues[index];
            double rootNewRangeSize = endValues[index + 1] - endValues[index];
            if (rootNewRangeSize > 1 || rootNewRangeSize < 0) System.out.println("bad new root range size");
            double rootRatio = rootNewRangeSize / rootOldRangeSize;
            double rootNewSplitTemp = rootRatio * (rootOldLocation - startValues[index]) + endValues[index];
            double rootNewSplit = (rootNewSplitTemp - endValues[min]) / (rootEndSize);
            if (rootNewSplit > 1 || rootNewSplit < 0) System.out.println("bad new root split");
            double rootNewLocation = rootNewSplit * rootEndSize + endValues[min];
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            SplitTransition st = new SplitTransition(this, currRootIndex, rootNewSplit, numSteps);
            newToMove.put(st.getHashKey(), st);
            double tempEnd = endValues[index + 1];
            double tempStart = startValues[index + 1];
            long tempIndex = splitIndices[index + 1];
            endValues[index + 1] = rootNewLocation;
            startValues[index + 1] = rootOldLocation;
            splitIndices[index + 1] = currRootIndex;
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index + 1], splitIndices[max], true), min, index + 1, newToMove);
            endValues[index + 1] = tempEnd;
            startValues[index + 1] = tempStart;
            splitIndices[index + 1] = tempIndex;
            tempEnd = endValues[index];
            tempStart = startValues[index];
            tempIndex = splitIndices[index];
            endValues[index] = rootNewLocation;
            startValues[index] = rootOldLocation;
            splitIndices[index] = currRootIndex;
            if (rootNewLocation > 1 || rootNewLocation < 0) System.out.println("bad new root location");
            resizeRanges(splitIndices, startValues, endValues, numSteps, getSplitRoot(splitIndices[min], splitIndices[index], splitIndices[max], false), index, max, newToMove);
            endValues[index] = tempEnd;
            startValues[index] = tempStart;
            splitIndices[index] = tempIndex;
        }
    }

    private long getSplitRoot(long min, long split, long max, boolean left) {
        if (left) {
            if (getCell(split).getLeftChild() != null) return getCell(split).getLeftChild().getIndex(); else return min;
        } else {
            if (getCell(split).getRightChild() != null) return getCell(split).getRightChild().getIndex(); else return max;
        }
    }

    private static boolean createMin = false;

    private static boolean createMax = false;

    private long[] createIndexRanges(AbstractRangeList group) {
        long[] rangesTemp = group.getSplitIndices(horizontal);
        if (group.size() == 1 && rangesTemp[0] == -1 && rangesTemp[rangesTemp.length - 1] == size) return null;
        int minOffset = 0;
        createMin = false;
        createMax = false;
        int rangeSize = rangesTemp.length;
        if (rangesTemp[0] != -1) {
            rangeSize++;
            createMin = true;
            minOffset = 1;
        }
        if (rangesTemp[rangesTemp.length - 1] != size) {
            rangeSize++;
            createMax = true;
        }
        long[] ranges = new long[rangeSize];
        for (int i = 0; i < rangesTemp.length; i++) {
            ranges[i + minOffset] = rangesTemp[i];
        }
        if (createMin) ranges[0] = -1;
        if (createMax) ranges[ranges.length - 1] = size;
        return ranges;
    }

    public void resizeForest(AbstractRangeList group, int numSteps, Hashtable newToMove, double inflateIncr) {
        if (group.size() == 0) return;
        long[] ranges = createIndexRanges(group);
        if (ranges == null) return;
        if (group.size() == 1 && getAbsoluteValue(ranges[(1 + (createMin ? 1 : 0))]) - getAbsoluteValue(ranges[0 + (createMin ? 1 : 0)]) + inflateIncr < ad.minContextInside) return;
        double stuckRangeSize = maxStuckValue - minStuckValue;
        double[] startValues = new double[ranges.length];
        double[] endValues = new double[ranges.length];
        double[] extent = group.getSizesOfAllRanges(this);
        double oldTotalExtent = getTotalExtent(extent);
        int rangesInPeriphery = (createMin ? 0 : 1) + (createMax ? 0 : 1);
        double minGrowSize = stuckRangeSize - ad.minContextPeriphery * (2 - rangesInPeriphery) - (ranges.length - 1) * ad.minContextInside;
        if (oldTotalExtent >= minGrowSize) {
            System.out.println("Too much squishing, not going to grow (maybe too many ranges? " + ranges.length + ")");
            return;
        }
        double minShrinkSize = stuckRangeSize - ad.minContextPeriphery * rangesInPeriphery - ranges.length * ad.minContextInside;
        double oldTotalNonExtent = stuckRangeSize - oldTotalExtent;
        for (int i = 0; i < ranges.length; i++) {
            startValues[i] = getAbsoluteValue(ranges[i]);
        }
        endValues[0] = startValues[0];
        endValues[ranges.length - 1] = startValues[ranges.length - 1];
        int numRealRanges = extent.length;
        int numRealNonRanges = numRealRanges - 1;
        if (createMin) numRealNonRanges++;
        if (createMax) numRealNonRanges++;
        int startAt = 0;
        int endAt = ranges.length - 1;
        {
            if (oldTotalExtent + inflateIncr > minGrowSize) inflateIncr = minGrowSize - oldTotalExtent;
            double newTotalExtent = oldTotalExtent + inflateIncr;
            double newTotalNonExtent = oldTotalNonExtent - inflateIncr;
            double totalExtentRatio = newTotalExtent / oldTotalExtent;
            double totalNonExtentRatio = newTotalNonExtent / oldTotalNonExtent;
            double firstRange = startValues[startAt + 1] - startValues[startAt];
            double lastRange = startValues[endAt] - startValues[endAt - 1];
            if (createMin) {
                if (firstRange < ad.minContextPeriphery) {
                    System.out.println("Area before first range might be squished too small");
                    endValues[startAt + 1] = startValues[startAt + 1];
                } else {
                    endValues[startAt + 1] = startValues[startAt] + firstRange * totalNonExtentRatio;
                    if (endValues[startAt + 1] - endValues[startAt] < ad.minContextPeriphery) endValues[startAt + 1] = endValues[startAt] + (double) ad.minContextPeriphery;
                }
                startAt++;
            }
            if (createMax) {
                if (lastRange < ad.minContextPeriphery) {
                    System.out.println("Area after last range might be squished too small");
                    endValues[endAt - 1] = startValues[endAt - 1];
                } else {
                    endValues[endAt - 1] = startValues[endAt] - lastRange * totalNonExtentRatio;
                    if (endValues[endAt] - endValues[endAt - 1] < ad.minContextPeriphery) endValues[endAt - 1] = endValues[endAt] - (double) ad.minContextPeriphery;
                }
                endAt--;
            }
            for (int i = startAt; i <= endAt - 2; i += 2) {
                endValues[i + 1] = startValues[i + 1] - startValues[i];
                endValues[i + 1] *= totalExtentRatio;
                endValues[i + 1] += endValues[i];
                endValues[i + 2] = startValues[i + 2] - startValues[i + 1];
                endValues[i + 2] *= totalNonExtentRatio;
                endValues[i + 2] += endValues[i + 1];
                if (endValues[i + 2] - endValues[i + 1] < ad.minContextInside) {
                    double halfAddBack = ((startValues[i + 2] - startValues[i + 1]) - ad.minContextInside) / 2;
                    if (halfAddBack > 0) {
                        endValues[i + 1] = startValues[i + 1] + halfAddBack;
                        endValues[i + 2] = startValues[i + 2] - halfAddBack;
                    } else {
                        System.out.println("Not moving, min context inside is too small: " + (endValues[i + 1] - endValues[i]) + " " + (endValues[i + 3] - endValues[i + 2]));
                        endValues[i + 1] = startValues[i + 1];
                        endValues[i + 2] = startValues[i + 2];
                        System.out.println("Real: " + (endValues[i + 1] - endValues[i]) + " " + (endValues[i + 3] - endValues[i + 2]));
                    }
                }
            }
        }
        int min = 0, max = ranges.length - 1;
        resizeRanges(ranges, startValues, endValues, numSteps, getRoot().getIndex(), min, max, newToMove);
    }

    private double getTotalExtent(double[] extent) {
        double totalExtent = 0f;
        for (int i = 0; i < extent.length; i++) {
            totalExtent += extent[i];
        }
        return totalExtent;
    }

    public void resetSplitValue(DynSplitCell cell) {
        DynSplitCell current = cell;
        if (current != null) {
            current.splitValue = current.defaultSplitValue;
            resetSplitValue(current.getLeftChild());
            resetSplitValue(current.getRightChild());
        }
    }

    public long getPrev(long index) {
        DynSplitCell cell = this.getCell(index).getPrevCell();
        if (cell != null) return cell.getIndex(); else return -1;
    }

    public long getNext(long index) {
        DynSplitCell cell = this.getCell(index).getNextCell();
        if (cell != null) return cell.getIndex(); else return -1;
    }
}
