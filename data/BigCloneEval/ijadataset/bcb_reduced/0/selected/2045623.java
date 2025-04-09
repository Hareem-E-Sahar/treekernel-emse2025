package org.jcrpg.threed.jme.geometryinstancing;

import java.util.ArrayList;

public class QuickOrderedList {

    private ArrayList<Integer> listOrdering = new ArrayList<Integer>();

    private ArrayList<Object> list = new ArrayList<Object>();

    public static long timeCounter = 0;

    public int getNearestIndex(int orderingValue) {
        return getNearestIndexPriv(orderingValue, 0, listOrdering.size() - 1);
    }

    private int getNearestIndexPriv(int orderingValue, int from, int to) {
        if (from == to) return from;
        int center = (from + to) / 2;
        int val = listOrdering.get(center);
        if (val == orderingValue) return center;
        if (val < orderingValue) {
            if (from + 1 == to) {
                return to;
            }
            return getNearestIndexPriv(orderingValue, center, to);
        }
        if (from + 1 == to) {
            return from;
        }
        return getNearestIndexPriv(orderingValue, from, center);
    }

    public void addElement(int orderingValue, Object value) {
        long time = System.currentTimeMillis();
        int index = 0;
        if (list.size() == 0) {
        } else {
            index = getNearestIndex(orderingValue);
        }
        listOrdering.add(index, orderingValue);
        list.add(index, value);
        timeCounter += System.currentTimeMillis() - time;
    }

    public void removeElement(Object value) {
        int index = 0;
        if (list.size() == 0) {
        } else {
            index = list.indexOf(value);
            listOrdering.remove(index);
            list.remove(index);
        }
    }

    public Object removeElementWithEqualOrBiggerOrderingValue(int orderingValue) {
        long time = System.currentTimeMillis();
        int index = 0;
        if (list.size() == 0) {
            timeCounter += System.currentTimeMillis() - time;
            return null;
        } else {
            index = getNearestIndex(orderingValue);
        }
        int order = listOrdering.get(index);
        if (order >= orderingValue) {
            listOrdering.remove(index);
            timeCounter += System.currentTimeMillis() - time;
            return list.remove(index);
        }
        timeCounter += System.currentTimeMillis() - time;
        return null;
    }

    public Object removeElementWithEqualOrderingValue(int orderingValue) {
        long time = System.currentTimeMillis();
        int index = 0;
        if (list.size() == 0) {
            timeCounter += System.currentTimeMillis() - time;
            return null;
        } else {
            index = getNearestIndex(orderingValue);
        }
        int order = listOrdering.get(index);
        if (order == orderingValue) {
            listOrdering.remove(index);
            timeCounter += System.currentTimeMillis() - time;
            return list.remove(index);
        }
        timeCounter += System.currentTimeMillis() - time;
        return null;
    }

    public static void main(String[] arg) {
        QuickOrderedList l = new QuickOrderedList();
        for (int i = 0; i < 1000; i++) {
            l.addElement(1000, "" + i);
        }
        for (int i = 0; i < 1000; i++) {
            if (l.removeElementWithEqualOrBiggerOrderingValue(1000) == null) System.out.println("ERROR!");
        }
    }
}
