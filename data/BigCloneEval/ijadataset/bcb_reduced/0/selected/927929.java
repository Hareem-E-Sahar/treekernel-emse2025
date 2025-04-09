package org.reprap.scanning.DataStructures;

import org.reprap.scanning.Geometry.LineSegment2DIndices;

public class OrderedListLineSegment2dIndices {

    private LineSegment2DIndices[] list;

    private int n;

    private long[] insertionorder;

    private int nextFIFO;

    public OrderedListLineSegment2dIndices(int high) {
        list = new LineSegment2DIndices[0];
        n = high;
        insertionorder = new long[0];
        nextFIFO = 0;
    }

    public OrderedListLineSegment2dIndices clone() {
        OrderedListLineSegment2dIndices returnvalue = new OrderedListLineSegment2dIndices(n);
        returnvalue.list = list.clone();
        returnvalue.insertionorder = insertionorder.clone();
        returnvalue.nextFIFO = nextFIFO;
        return returnvalue;
    }

    public boolean InsertIfNotExist(LineSegment2DIndices addition) {
        boolean insert = true;
        addition.SetHash(n);
        if (list.length == 0) {
            list = new LineSegment2DIndices[1];
            list[0] = addition.clone();
            insertionorder = new long[1];
            insertionorder[0] = addition.hashvalue;
            nextFIFO = 0;
        } else {
            int insertpoint;
            if ((list[0].hashvalue <= addition.hashvalue) && (list[list.length - 1].hashvalue >= addition.hashvalue)) {
                insertpoint = Find(addition.hashvalue);
                if (list[insertpoint].hashvalue == addition.hashvalue) insert = false;
            } else {
                if (list[list.length - 1].hashvalue < addition.hashvalue) insertpoint = list.length; else insertpoint = 0;
            }
            if (insert) {
                LineSegment2DIndices[] returnvalue = new LineSegment2DIndices[list.length + 1];
                for (int i = 0; i < returnvalue.length; i++) {
                    if (i < insertpoint) returnvalue[i] = list[i].clone(); else if (i > insertpoint) returnvalue[i] = list[i - 1].clone(); else returnvalue[i] = addition.clone();
                }
                list = returnvalue.clone();
                long[] insertorder = new long[insertionorder.length + 1];
                for (int i = 0; i < insertionorder.length; i++) insertorder[i] = insertionorder[i];
                insertorder[insertionorder.length] = addition.hashvalue;
                insertionorder = insertorder.clone();
            }
        }
        return insert;
    }

    public boolean DeleteIfExist(LineSegment2DIndices target) {
        boolean returnvalue = false;
        if (list.length != 0) {
            target.SetHash(n);
            int index = Exists(target.hashvalue);
            returnvalue = (index != -1);
            if (returnvalue) DeleteEntry(index, true);
        }
        return returnvalue;
    }

    public LineSegment2DIndices GetFirstFIFO() {
        nextFIFO = 0;
        return ExtractFIFO();
    }

    public void DeleteExtractedFIFOOrder() {
        if (nextFIFO != 0) {
            long[] newinsertionorder = new long[insertionorder.length - nextFIFO];
            for (int i = nextFIFO; i < insertionorder.length; i++) newinsertionorder[i - nextFIFO] = insertionorder[i];
            insertionorder = newinsertionorder.clone();
            nextFIFO = 0;
        }
    }

    public LineSegment2DIndices ExtractFIFO() {
        LineSegment2DIndices returnvalue;
        if (insertionorder.length == 0) returnvalue = new LineSegment2DIndices(); else {
            int index = -1;
            while ((index == -1) && (nextFIFO < insertionorder.length)) {
                index = Exists(insertionorder[nextFIFO]);
                nextFIFO++;
            }
            if ((nextFIFO > insertionorder.length) || (index == -1)) {
                list = new LineSegment2DIndices[0];
                insertionorder = new long[0];
                nextFIFO = 0;
                returnvalue = new LineSegment2DIndices();
            } else {
                returnvalue = list[index].clone();
                DeleteEntry(index, true);
            }
        }
        return returnvalue;
    }

    public int getLength() {
        return list.length;
    }

    public boolean FaceExists(LineSegment2DIndices f) {
        f.SetHash(n);
        return (Exists(f.hashvalue) != -1);
    }

    public LineSegment2DIndices[] GetFullUnorderedList() {
        return list;
    }

    private int Find(long targethash) {
        int returnvalue;
        returnvalue = Find(targethash, 0, list.length);
        return returnvalue;
    }

    private int Exists(long targethash) {
        int index = Find(targethash, 0, list.length);
        if (index == list.length) return -1; else {
            if (list[index].hashvalue != targethash) return -1; else return index;
        }
    }

    private int Find(long targethash, int left, int right) {
        int returnvalue;
        if ((right - left) <= 1) {
            if (list.length == 0) returnvalue = 0; else if (list[left].hashvalue == targethash) returnvalue = left; else returnvalue = right;
        } else {
            int mid = (left + right) / 2;
            if (list[mid].hashvalue > targethash) returnvalue = Find(targethash, left, mid); else returnvalue = Find(targethash, mid, right);
        }
        return returnvalue;
    }

    private void DeleteEntry(int index, boolean changeinsertionorderarray) {
        if (changeinsertionorderarray) {
            int numberofmatches = 0;
            long hashtomatch = list[index].hashvalue;
            for (int i = 0; i < insertionorder.length; i++) if (insertionorder[i] == hashtomatch) numberofmatches++;
            if (numberofmatches != 0) {
                long[] newinsertionarray = new long[insertionorder.length - numberofmatches];
                int j = 0;
                for (int i = 0; i < insertionorder.length; i++) {
                    if (insertionorder[i] != hashtomatch) {
                        newinsertionarray[j] = insertionorder[i];
                        j++;
                    } else if (i < nextFIFO) nextFIFO--;
                }
                insertionorder = newinsertionarray.clone();
            }
        }
        LineSegment2DIndices[] returnvalue = new LineSegment2DIndices[list.length - 1];
        for (int i = 0; i < list.length; i++) {
            if (index > i) returnvalue[i] = list[i].clone();
            if (index < i) returnvalue[i - 1] = list[i].clone();
        }
        list = returnvalue.clone();
    }

    public void PrintFIFO() {
        if (insertionorder.length != 0) {
            int i = 0;
            int index = -1;
            while (i < insertionorder.length) {
                index = Exists(insertionorder[i]);
                if (index != -1) {
                    LineSegment2DIndices f = list[index].clone();
                    System.out.print(i + " " + insertionorder[i] + ": ");
                    f.print();
                } else System.out.print(i + " " + insertionorder[i] + " ");
                if (nextFIFO == i) System.out.print("  <=====");
                System.out.println();
                i++;
            }
        }
    }
}
