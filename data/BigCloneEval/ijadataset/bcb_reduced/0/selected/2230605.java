package com.daffodilwoods.daffodildb.server.sql99.expression.booleanvalueexpression.predicates;

import java.util.*;
import com.daffodilwoods.daffodildb.utils.*;
import com.daffodilwoods.daffodildb.utils.comparator.*;
import com.daffodilwoods.database.resource.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class ByteInComparator extends SuperComparator {

    private SuperComparator superComparator;

    private Object localCacheSingle;

    private SuperComparator[] comparators;

    private Object[] localCache;

    public ByteInComparator(SuperComparator[] superComparator0, Object localCache0) throws DException {
        comparators = superComparator0;
        localCache = (Object[]) localCache0;
        Arrays.sort(localCache, new Comparator(GetByteComparator.objectComparator));
    }

    public int compare(Object leftFieldBases, Object o2) throws DException {
        return binarySearch(leftFieldBases, localCache);
    }

    private int binarySearch(Object left, Object[] rightArray) throws DException {
        int low = 0, high = rightArray.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            SuperComparator comparator = comparators[mid];
            int returnValue = comparator.compare(left, rightArray[mid]);
            if (returnValue < 0) {
                high = mid - 1;
            } else if (returnValue > 0) {
                low = mid + 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    public String toString() {
        return "ByteInComparator[" + superComparator + "]";
    }
}

class Comparator implements java.util.Comparator {

    private SuperComparator comparator;

    Comparator(SuperComparator comparator0) {
        comparator = comparator0;
    }

    public int compare(Object leftFieldBases, Object o2) {
        try {
            return comparator.compare(leftFieldBases, o2);
        } catch (DException ex) {
        }
        return -1;
    }
}
