package com.solidmatrix.regxmaker.util.shared.matching;

import java.util.Iterator;
import java.util.TreeMap;

/**
 * <PRE>
 * Name   : com.solidmatrix.regxmaker.util.shared.matching.ByteMatcher
 * Project: RegXmaker Library
 * Version: 1.1
 * Tier   : 3 (Function Class)
 * Author : Gennadiy Shafranovich
 * Purpose: Finds matching sequences in two byte arrays
 *
 * Copyright (C) 2001, 2004 SolidMatrix Technologies, Inc.
 * This file is part of RegXmaker Library.
 *
 * RegXmaker Library is is free software; you can redistribute it and/or modify
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * RegXmaker library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Comments: Full, with javadoc.
 *
 * Modification History
 *
 * 06-28-2001 GS Created
 *               NOTE: implemintaion has to be tested
 *
 * 07-05-2001 GS Added FailSafe features
 *
 * 07-05-2004 YS Added licensing information
 * </PRE>
 */
public final class ByteMatcher extends Matcher {

    /**
     * First input byte array
     */
    private byte[] in1;

    /**
     * Second input byte array
     */
    private byte[] in2;

    /**
     * Boolean array containing flags wether a certain location 
     * in the first input byte array is a delimiter for 
     * multi-string splice.
     */
    private boolean[] upperDelim;

    /**
     * Boolean array containing flags wether a certain location 
     * in the second input byte array is a delimiter for 
     * multi-string splice.
     */
    private boolean[] lowerDelim;

    /**
	 * Constructs a new ByteMatcher with a given SecureObject,
     * input byte arrays and delimiters arrays and a default ignore
     * weight.<BR>
	 *
	 * @param   o  SecureObject used for verification
	 * @param   t1  First input byte array
	 * @param   t2  Second input byte array
	 * @param   upper  boolean array containing delimiter flags for first byte array
	 * @param   lower  boolean array containing delimiter flags for second byte array
     * @throws SecurityException if the given SecureObject has not been verified
	 */
    public ByteMatcher(byte[] t1, byte[] t2, boolean[] upper, boolean[] lower) throws SecurityException {
        this(t1, t2, upper, lower, DEFAULT_IGNORE_WEIGHT, false);
    }

    /**
	 * Constructs a new ByteMatcher with a given SecureObject,
     * input byte arrays, no delimiters within the byte arrays,
     * and the default ignore weight.<BR>
	 *
	 * @param   o  SecureObject used for verification
	 * @param   t1  First input byte array
	 * @param   t2  Second input byte array
     * @throws SecurityException if the given SecureObject has not been verified
	 */
    public ByteMatcher(byte[] t1, byte[] t2) throws SecurityException {
        this(t1, t2, DEFAULT_IGNORE_WEIGHT);
    }

    /**
	 * Constructs a new ByteMatcher with a given SecureObject,
     * input byte arrays, no delimiters within the byte arrays,
     * and a given ignore weight.<BR>
	 *
	 * @param   o  SecureObject used for verification
	 * @param   t1  First input byte array
	 * @param   t2  Second input byte array
	 * @param   iw  minimum size of match to be recorded
     * @throws SecurityException if the given SecureObject has not been verified
	 */
    public ByteMatcher(byte[] t1, byte[] t2, int iw) throws SecurityException {
        this(t1, t2, makeEmptyDelimArray(t1.length), makeEmptyDelimArray(t2.length), iw, false);
    }

    /**
	 * Constructs a new ByteMatcher with a given SecureObject,
     * input byte arrays, delimiter arrays, and a given ignore weight.<BR>
	 *
	 * @param   o  SecureObject used for verification
	 * @param   t1  First input byte array
	 * @param   t2  Second input byte array
	 * @param   upper  boolean array containing delimiter flags for first byte array
	 * @param   lower  boolean array containing delimiter flags for second byte array
	 * @param   iw  minimum size of match to be recorded
     * @throws SecurityException if the given SecureObject has not been verified
	 */
    public ByteMatcher(byte[] t1, byte[] t2, boolean[] upper, boolean[] lower, int iw) throws SecurityException {
        this(t1, t2, upper, lower, iw, false);
    }

    /**
     * Constructs a new ByteMatcher with a given SecureObject,
     * input byte arrays, delimiter arrays, a given ignore weight,
     * and a given debug status.<BR>
     *
     * @param   o  SecureObject used for verification
     * @param   t1  First input byte array
     * @param   t2  Second input byte array
     * @param   upper  boolean array containing delimiter flags for first byte array
     * @param   lower  boolean array containing delimiter flags for second byte array
     * @param   iw  minimum size of match to be recorded
     * @param   debug  flag wether the matcher will be in debug mode
     * @throws SecurityException if the given SecureObject has not been verified
     */
    public ByteMatcher(byte[] t1, byte[] t2, boolean[] upper, boolean[] lower, int iw, boolean debug) throws SecurityException {
        super(iw, debug);
        in1 = t1;
        in2 = t2;
        upperDelim = upper;
        lowerDelim = lower;
    }

    /**
     * A method which returns a two dimentional array as specified by
     * the getResult() method which contains the best set of matches
     * between given structures. <BR><BR>
     * 
     * This class uses the MatchTwo algorithm defined by 
     * SolidMatrix Technologies, Inc.
     *
     * @see smti.util.shared.Matcher#getResult()
     */
    public int[][] match() {
        if (matched) return result;
        processing = true;
        if (needsFastAlgo(in1.length, in2.length)) matchBlocks(0, 0, in1.length, in2.length, 0, 0, in1.length, in2.length); else matchSection(0, 0, in1.length, in2.length, 0, 0, in1.length, in2.length);
        result = outputLists();
        resetLists();
        processing = false;
        return result;
    }

    private void matchBlocks(int start1, int start2, int end1, int end2, int lstart1, int lstart2, int lend1, int lend2) {
        long start = System.currentTimeMillis();
        int centerU = (end1 + start1) / 2;
        TreeMap leftU = map(in1, start1, centerU);
        TreeMap rightU = map(in1, centerU, end1);
        int centerL = (end2 + start2) / 2;
        TreeMap leftL = map(in2, start2, centerL);
        TreeMap rightL = map(in2, centerL, end2);
        int s1 = start1;
        int s2 = start2;
        int e1 = centerU;
        int e2 = centerL;
        int error = getError(leftU, leftL);
        int terror = getError(leftU, rightL);
        if (terror < error) {
            error = terror;
            s2 = centerL;
            e2 = end2;
        }
        terror = getError(rightU, leftL);
        if (terror < error) {
            error = terror;
            s1 = centerU;
            e1 = end1;
        }
        terror = getError(rightU, rightL);
        if (terror < error) {
            error = terror;
            s1 = centerU;
            e1 = end1;
            s2 = centerL;
            e2 = end2;
        }
        long cur = System.currentTimeMillis() - start;
        if (needsFastAlgo(e2 - s2, e1 - s1)) matchBlocks(s1, s2, e1, e2, lstart1, lstart2, lend1, lend2); else matchSection(s1, s2, e1, e2, lstart1, lstart2, lend1, lend2);
        if (debug) System.out.println("\tMatchBlocks Delay(" + (end1 - start1) + "~" + (end2 - start2) + "): " + (System.currentTimeMillis() - start) + " - " + cur);
    }

    private TreeMap map(byte[] in, int start, int end) {
        TreeMap map = new TreeMap();
        for (int i = start; i < end; i++) {
            Byte t = new Byte(in[i]);
            Counter c = (Counter) map.get(t);
            if (c == null) {
                c = new Counter();
                map.put(t, c);
            }
            c.increment();
        }
        return map;
    }

    private int getError(TreeMap upper, TreeMap lower) {
        int error = 0;
        Iterator ukeys = upper.keySet().iterator();
        while (ukeys.hasNext()) {
            Object ukey = ukeys.next();
            Counter c1 = (Counter) upper.get(ukey);
            if (lower.containsKey(ukey)) error += c1.error((Counter) lower.get(ukey)); else error += c1.getValue();
        }
        Iterator lkeys = lower.keySet().iterator();
        while (lkeys.hasNext()) {
            Object lkey = lkeys.next();
            Counter c2 = (Counter) lower.get(lkey);
            if (!upper.containsKey(lkey)) error += c2.getValue();
        }
        return error;
    }

    /**
	 * Recursive implemintation for the MatchTwo algorithm as defined
     * by SolidMatrix Technologies, Inc. <BR><BR>
     *
     * The structures matched are those that were given during
     * the instantiation of this class. The results are stored to 
     * the three linked lists defined by the super class (Matcher).
     * <BR><BR>
     *
     * The first call to this method should be in the following form:<BR>
     * matchTwo(default point array for upper bin list,<BR>
     *          default point array for lower bin list,<BR>
     *          0, 0, <BR>
     *          length of first input array,<BR>
     *          length of second input array)<BR>
	 */
    private void matchSection(int start1, int start2, int end1, int end2, int lstart1, int lstart2, int lend1, int lend2) {
        long start = System.currentTimeMillis();
        int bfor = -1;
        int bsec = -1;
        int weight = 0;
        for (int i = start1; i < end1; i++) {
            boolean inside1 = (i >= bfor && i < bfor + weight);
            for (int j = start2; j < end2; j++) {
                boolean inside2 = (j >= bsec && j < bsec + weight);
                if (inside1 && inside2) continue;
                if (in1[i] == in2[j]) {
                    int mw = 1;
                    int l = i + 1;
                    int m = j + 1;
                    while (l < lend1 && m < lend2 && !upperDelim[l] && !lowerDelim[m] && in1[l] == in2[m] && processing) {
                        mw++;
                        l++;
                        m++;
                    }
                    l = i - 1;
                    m = j - 1;
                    while (l > lstart1 && m > lstart2 && !upperDelim[l] && !lowerDelim[m] && in1[l] == in2[m] && processing) {
                        mw++;
                        l--;
                        m--;
                    }
                    l++;
                    m++;
                    if (mw > weight && mw >= ignoreWeight) {
                        bfor = l;
                        bsec = m;
                        weight = mw;
                    }
                }
            }
        }
        long cur = System.currentTimeMillis() - start;
        if (weight != 0 && processing) {
            int comp1 = bfor + weight;
            int comp2 = bsec + weight;
            if (bfor > lstart1 && bsec > lstart2 && processing) {
                if (needsFastAlgo(bfor - lstart1, bsec - lstart2)) matchBlocks(lstart1, lstart2, bfor, bsec, lstart1, lstart2, bfor, bsec); else matchSection(lstart1, lstart2, bfor, bsec, lstart1, lstart2, bfor, bsec);
            }
            firstMatch.add(new Integer(bfor));
            secondMatch.add(new Integer(bsec));
            weights.add(new Integer(weight));
            if (comp1 < lend1 && comp2 < lend2 && processing) {
                if (needsFastAlgo(lend1 - comp1, lend2 - comp2)) matchBlocks(comp1, comp2, lend1, lend2, comp1, comp2, lend1, lend2); else matchSection(comp1, comp2, lend1, lend2, comp1, comp2, lend1, lend2);
            }
        }
        if (debug) System.out.println("\tMatchSection Delay(" + (end1 - start1) + "~" + (end2 - start2) + "): " + (System.currentTimeMillis() - start) + " - " + cur);
    }
}
