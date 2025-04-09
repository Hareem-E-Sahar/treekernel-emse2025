package com.flaptor.hist4j;

/**
 * The HistogramDataNode stores the histogram data for a range of values.
 * It knows the minimum and maximum values for which it counts the number of instances.
 * When the count exceeds the allowed limit it splits itself in two, increasing the
 * histogram resolution for this range.
 * @author Jorge Handl
 */
public class HistogramDataNode extends HistogramNode {

    private long count;

    private float minValue, maxValue;

    /**
     * Creates an empty data node.
     */
    public HistogramDataNode() {
        reset();
    }

    /**
     * Creates a data node for the given range with the given instance count.
     * @param count the number of data instances in the given range.
     * @param minValue the start of the range of counted values.
     * @param maxValue the end of the range of counted values.
     */
    public HistogramDataNode(long count, float minValue, float maxValue) {
        reset();
        this.count = count;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Clears the data node.
     */
    public void reset() {
        count = 0;
        minValue = Float.MAX_VALUE;
        maxValue = -Float.MAX_VALUE;
    }

    /**
     * Adds a value to the data node.<p>
     * If the value falls inside of the nodes' range and the count does not exceed the imposed limit, it simply increments the count.<br>
     * If the value falls outside of the nodes' range, it expands the range.<br>
     * If the count exceeds the limit, it splits in two assuming uniform distribution inside the node.<br>
     * If the value falls outside of the nodes' range AND the count exceeds the limit, it creates a new node for that value.
     * @param root a reference to the adaptive histogram instance that uses this structure.
     * @param value the value for which the count is to be incremented.
     * @return A reference to itself if no structural change happened, or a reference to the new fork node if this node was split.
     */
    public HistogramNode addValue(AdaptiveHistogram root, float value) {
        HistogramNode self = this;
        if (value >= minValue && value <= maxValue) {
            if (count < root.getCountPerNodeLimit()) {
                count++;
            } else {
                float splitValue = (minValue + maxValue) / 2;
                long rightCount = count / 2;
                long leftCount = rightCount;
                boolean countWasOdd = (leftCount + rightCount < count);
                if (value > splitValue) {
                    rightCount++;
                    leftCount += (countWasOdd ? 1 : 0);
                } else {
                    leftCount++;
                    rightCount += (countWasOdd ? 1 : 0);
                }
                HistogramNode leftNode = new HistogramDataNode(leftCount, minValue, splitValue);
                HistogramNode rightNode = new HistogramDataNode(rightCount, splitValue, maxValue);
                self = new HistogramForkNode(splitValue, leftNode, rightNode);
            }
        } else {
            if (count < root.getCountPerNodeLimit()) {
                count++;
                if (value < minValue) minValue = value;
                if (value > maxValue) maxValue = value;
            } else {
                if (value < minValue) {
                    minValue = Math.min(minValue, (value + maxValue) / 2);
                    self = new HistogramForkNode(minValue, new HistogramDataNode(1, value, minValue), this);
                } else {
                    maxValue = Math.max(maxValue, (minValue + value) / 2);
                    self = new HistogramForkNode(maxValue, this, new HistogramDataNode(1, maxValue, value));
                }
            }
        }
        return self;
    }

    /**
     * Returns the number of data points stored in the same bucket as a given value.
     * @param value the reference data point.
     * @return the number of data points stored in the same bucket as the reference point.
     */
    public long getCount(float value) {
        long res = 0;
        if (value >= minValue && value <= maxValue) {
            res = count;
        }
        return res;
    }

    /**
     * Returns the cumulative density function for a given data point.
     * @param value the reference data point.
     * @return the cumulative density function for the reference point.
     */
    public long getAccumCount(float value) {
        long res = 0;
        if (value >= minValue) {
            res = count;
        }
        return res;
    }

    private float interpolate(float x0, float y0, float x1, float y1, float x) {
        return y0 + ((x - x0) * (y1 - y0)) / (x1 - x0);
    }

    /**
     * Returns the data point where the running cumulative count reaches the target cumulative count.
     * It uses linear interpolation over the range of the node to get a better estimate of the true value.
     * @param accumCount an array containing:<br>
     *      - accumCount[0] the running cumulative count. <br>
     *      - accumCount[1] the target cumulative count.
     * @return the data point where the running cumulative count reaches the target cumulative count.
     */
    public Float getValueForAccumCount(long[] accumCount) {
        Float res = null;
        long runningAccumCount = accumCount[0];
        long targetAccumCount = accumCount[1];
        if (runningAccumCount <= targetAccumCount && runningAccumCount + count >= targetAccumCount) {
            float val = interpolate((float) runningAccumCount, minValue, (float) (runningAccumCount + count), maxValue, (float) targetAccumCount);
            res = new Float(val);
        }
        accumCount[0] += count;
        return res;
    }

    /** 
     * Applies a convertion function to the values stored in the histogram.
     * @param valueConversion a class that defines a function to convert the value.
     */
    public void apply(AdaptiveHistogram.ValueConversion valueConversion) {
        minValue = valueConversion.convertValue(minValue);
        maxValue = valueConversion.convertValue(maxValue);
    }

    /**
     * Prints this nodes' data with a margin depending on the level of the node in the tree.
     * @param level the level of this node in the tree.
     */
    public void show(int level) {
        margin(level);
        System.out.println("Data: " + count + " (" + minValue + "-" + maxValue + ")");
    }
}
