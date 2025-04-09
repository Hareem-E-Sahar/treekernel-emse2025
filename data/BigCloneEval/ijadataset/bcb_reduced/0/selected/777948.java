package net.sf.i2canalyzer;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class that holds a list of DataItem Instances read from a file.
 *
 * @author Michael Rumpf
 */
public final class I2CDataItemList {

    private String mName;

    private String mFilename;

    private List mList;

    private Color mColor;

    private int mStartIndex;

    private int mEndIndex;

    private int mStartTime;

    private int mTimeRange;

    /**
     * Constructor.
     *
     * @param name The name of the data item list.
     * @param filename The name of the file from which the data
     *        items have been read.
     * @param color The color to be used for painting the graph.
     * @param list The of data items.
     */
    private I2CDataItemList(final String name, final String filename, final Color color, final List list) {
        mName = name;
        mFilename = filename;
        mList = list;
        mColor = color;
        mStartIndex = -1;
        mEndIndex = -1;
        mStartTime = -1;
        mTimeRange = -1;
    }

    /**
     * Returns the name of the data set.
     *
     * @return The data set name.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the name of the file where the DataItems have been read from.
     *
     * @return The name of the source file.
     */
    public String getFilename() {
        return mFilename;
    }

    /**
     * Returns the color with which this data set should be drawn.
     *
     * @return The color of the dataset to be drawn.
     */
    public Color getColor() {
        return mColor;
    }

    /**
     * Set the time interval and range of the chart being displayed.
     * This method finds the index of a data item that is smaller
     * than the startime of the view area. If no such item can be
     * found then the start index is set to -1.
     * It also finds the index of a data item with a timestamp
     * larger or equal to the end time of the view area. If no
     * such item can be found then the end index is set to -1.
     *
     * @param starttime The starttime of the chart area.
     * @param timerange The time range that spans the chart area.
     */
    public void setTimeInterval1(final int starttime, final int timerange) {
        mStartTime = starttime;
        mTimeRange = timerange;
        mStartIndex = -1;
        boolean bFoundStart = false;
        for (int i = 0; i < mList.size() && !bFoundStart; i++) {
            final I2CDataItem di = getItem(i);
            if (di.getTime() < starttime) {
                mStartIndex = i;
            } else {
                bFoundStart = true;
            }
        }
        boolean bFoundEnd = false;
        mEndIndex = mList.size();
        int endtime = starttime + timerange;
        for (int i = mList.size() - 1; i > -1 && !bFoundEnd; i--) {
            final I2CDataItem di = getItem(i);
            if (di.getTime() > endtime) {
                mEndIndex = i;
            } else {
                bFoundEnd = true;
            }
        }
    }

    private int binarySearchStart(final int time, final int startidx, final int endidx) {
        int high = endidx;
        int low = startidx;
        int probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            final I2CDataItem di = getItem(probe);
            if (di.getTime() < time) low = probe; else high = probe;
        }
        return low;
    }

    private int binarySearchEnd(final int time, final int startidx, final int endidx) {
        int high = endidx;
        int low = startidx;
        int probe;
        while (high - low > 1) {
            probe = (high + low) / 2;
            final I2CDataItem di = getItem(probe);
            if (di.getTime() > time) high = probe; else low = probe;
        }
        return high;
    }

    public void setTimeInterval(final int starttime, final int timerange) {
        final int oldStartTime = mStartTime;
        final int oldEndTime = mStartTime + mTimeRange;
        final int oldStartIndex = mStartIndex;
        final int oldEndIndex = mEndIndex;
        final int newStartTime = starttime;
        final int newEndTime = starttime + timerange;
        int newStartIndex = -1;
        int newEndIndex = mList.size();
        if (mStartTime == -1 && mTimeRange == -1) {
            newStartIndex = binarySearchStart(newStartTime, -1, mList.size());
            newEndIndex = binarySearchEnd(newEndTime, newStartIndex, mList.size());
        } else {
            if (newStartTime < oldStartTime) {
                newStartIndex = binarySearchStart(newStartTime, -1, oldStartIndex + 1);
            } else if (newStartTime > oldStartTime) {
                if (newStartTime <= oldEndTime) {
                    newStartIndex = binarySearchStart(newStartTime, oldStartIndex, oldEndIndex);
                } else {
                    newStartIndex = binarySearchStart(newStartTime, oldEndIndex - 1, mList.size());
                }
            } else {
                newStartIndex = oldStartIndex;
            }
            if (newEndTime > oldEndTime) {
                newEndIndex = binarySearchEnd(newEndTime, oldEndIndex - 1, mList.size());
            } else if (newEndTime < oldEndTime) {
                if (newEndTime >= oldStartTime) {
                    newEndIndex = binarySearchEnd(newEndTime, oldStartIndex, oldEndIndex);
                } else {
                    newEndIndex = binarySearchEnd(newEndTime, -1, oldStartIndex + 1);
                }
            } else {
                newEndIndex = oldEndIndex;
            }
        }
        mStartTime = starttime;
        mTimeRange = timerange;
        mStartIndex = newStartIndex;
        mEndIndex = newEndIndex;
    }

    /**
     * Returns the index of the list entry that is smaller
     * or equal than the start time.
     *
     * @return The index of a list item that represents the start time.
     */
    public int getStartIndex() {
        return mStartIndex;
    }

    /**
     * Returns the index of the list entry that is larger
     * than the start time.
     *
     * @return The index of a list item that represents the start time.
     */
    public int getEndIndex() {
        return mEndIndex;
    }

    /**
     * Returns an unmodifiable reference to the internal list
     * of DataItem instances.
     *
     * @return A list reference with DataItem instances.
     */
    public List getList() {
        return Collections.unmodifiableList(mList);
    }

    /**
     * Returns an data item from the list.
     *
     * @param i The index to return the data item for.
     * @return The data item for the given index.
     */
    public I2CDataItem getItem(final int i) {
        return (I2CDataItem) mList.get(i);
    }

    /**
     * Return the range of microseconds between the first data item
     * and the last data item in the list.
     * @return The difference between the max(x) and min(x). 
     */
    public int getTimeRange() {
        final I2CDataItem dimin = (I2CDataItem) mList.get(0);
        final I2CDataItem dimax = (I2CDataItem) mList.get(mList.size() - 1);
        return dimax.getTime() - dimin.getTime();
    }

    /**
     * Return the minimum x value.
     *
     * @return The minimum x value. 
     */
    public int getTimeMin() {
        final I2CDataItem di = (I2CDataItem) mList.get(0);
        return di.getTime();
    }

    /**
     * Return the maximum x value.
     *
     * @return The maximum x value. 
     */
    public int getTimeMax() {
        final I2CDataItem di = (I2CDataItem) mList.get(mList.size() - 1);
        return di.getTime();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Name=" + mName + ", Filename=" + mFilename + ", Color=" + mColor + ", Start=" + mStartIndex + ", End=" + mEndIndex;
    }

    /**
     * Creates a list of data items by reading from the specified file.
     *
     * @param name The name of the data item list.
     * @param filename The name of the data file.
     * @param color The color with wich the data item list should be drawn.
     * @return An instance of the DataItemList class.
     * @throws Exception when a problem reading the file occurs.
     */
    public static I2CDataItemList createDataItemListFromFile(final String name, final String filename, final Color color) throws Exception {
        I2CDataItemList dilist = null;
        final List list = new ArrayList();
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(filename));
            String line = input.readLine();
            while (line != null) {
                final int space = line.indexOf(' ');
                final String timestr = line.substring(0, space);
                final String valuestr = line.substring(space + 1);
                final int time = Integer.valueOf(timestr).intValue();
                final int value = Integer.valueOf(valuestr).intValue();
                list.add(new I2CDataItem(time, value));
                line = input.readLine();
            }
            dilist = new I2CDataItemList(name, filename, color, list);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return dilist;
    }
}
