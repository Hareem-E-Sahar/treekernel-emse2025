package com.zigabyte.metastock.data;

import java.util.*;
import java.io.Serializable;

/** Implementation of {@link StockHistory} that do not rely
    on the data store.  Methods with date parameter are
    defined in terms of the {@link #get(int)} and {@link #size()} methods.
**/
public abstract class AbstractStockHistory implements StockHistory, Serializable {

    /** Stock symbol **/
    private String symbol;

    /** Full name **/
    private String name;

    /** Create a new history for the stock with the given symbol and name. **/
    public AbstractStockHistory(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    /** Insert data point into history. 
      @return old datapoint if there is a previous datapoint with the
      same date, else null. **/
    public abstract StockDataPoint add(StockDataPoint dataPoint);

    /** Remove data point at index.
      @return removed datapoint.
      @see #remove(java.util.Date) **/
    public abstract StockDataPoint remove(int index);

    /** Remove data point at dateTime.
      @return removed datapoint, or null if no datapoint with same dateTime.
      @see #remove(int) **/
    public StockDataPoint remove(java.util.Date dateTime) {
        int index = binarySearch(dateTime);
        if (index >= 0) return this.remove(index); else return null;
    }

    /** Remove all data points. **/
    public abstract void clear();

    /** Iterator for datapoints. **/
    public abstract Iterator<StockDataPoint> iterator();

    /** Stock symbol **/
    public String getSymbol() {
        return this.symbol;
    }

    /** Full name of stock **/
    public String getName() {
        return this.name;
    }

    /** Number of datapoints **/
    public abstract int size();

    /** Get datapoint at index **/
    public abstract StockDataPoint get(int index);

    /** Get datapoint at dateTime, or null if no datapoint with same dateTime. **/
    public StockDataPoint get(java.util.Date dateTime) {
        int index = binarySearch(dateTime);
        if (index >= 0) return this.get(index); else return null;
    }

    /** Get datapoint at dateTime,
      else if none at dateTime, get latest datapoint before dateTime,
      else if before all datapoints, return null. **/
    public StockDataPoint getAtOrBefore(java.util.Date dateTime) {
        int index = getIndexAtOrBefore(dateTime);
        if (index >= 0) return this.get(index); else return null;
    }

    /** Get index of dataPoint at dateTime,
      else if none at dateTime, get inndex of latest datapoint before dateTime,
      else if before all datapoints, return -1. **/
    public int getIndexAtOrBefore(java.util.Date dateTime) {
        int index = binarySearch(dateTime);
        if (index >= 0) return index; else {
            index = ~index;
            if (index > 0) return index - 1; else return -1;
        }
    }

    /** Get datapoint at dateTime,
      else if none at dateTime, get earliest datapoint after dateTime,
      else if after all datapoints, return null. **/
    public StockDataPoint getAtOrAfter(java.util.Date dateTime) {
        int index = getIndexAtOrAfter(dateTime);
        if (index >= 0) return this.get(index); else return null;
    }

    /** Get index of datapoint at dateTime,
      else if none at dateTime, get index of earliest datapoint after dateTime,
      else if after all datapoints, return -1. **/
    public int getIndexAtOrAfter(java.util.Date dateTime) {
        int index = binarySearch(dateTime);
        if (index >= 0) return index; else {
            index = ~index;
            if (index < this.size()) return index; else return -1;
        }
    }

    /** Binary search for dateTime in datapoints, which are sorted
      by dateTime earliest first.  If found, returns index, otherwise
      returns ~index to insert dateTime.  [~index is complement of
      index bits, it is equivalent to -(index+1), and is always negative
      even if index is zero.]
      @see java.util.Collections#binarySearch
   **/
    public int binarySearch(Date dateTime) {
        long msDate = dateTime.getTime();
        int lo = 0, hi = this.size();
        while (lo < hi) {
            int mid = (lo + hi) / 2;
            long midMsDate = this.get(mid).getDate().getTime();
            if (msDate < midMsDate) hi = mid; else if (msDate > midMsDate) lo = mid + 1; else return mid;
        }
        return ~lo;
    }

    /** Returns "symbol: name" **/
    public String toString() {
        return this.symbol + ": " + this.name;
    }
}
