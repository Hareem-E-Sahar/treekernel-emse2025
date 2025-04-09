package com.solidmatrix.regxmaker.util.shared.splicing;

import java.util.LinkedList;
import com.solidmatrix.regxmaker.util.shared.matching.CharMatcher;

/**
 * <PRE>
 * Name   : com.solidmatrix.regxmaker.util.shared.splicing.CharSplicer
 * Project: RegXmaker Library
 * Version: 1.1
 * Tier   : 3 (Function Class)
 * Author : Gennadiy Shafranovich
 * Purpose: Finds all static (same) sections in multiple char arrays
 *          Based on the multi-match technique.
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
 *
 * Modification History
 *
 * 06-29-2001 GS Created
 *               NOTE: implemintaion has to be tested
 *
 * 07-05-2001 GS Added FailSafe features
 *
 * 07-05-2004 YS Added licensing information
 * </PRE>
 */
public final class CharSplicer extends Splicer {

    /**
	 * the data to be spliced
     */
    private char[][] data;

    /**
	 * Delimiters flags for the multi-match splice
     */
    private boolean[][] delims;

    /**
	 * Constructs a new Splicer to splice arrays of the char 
     * primitive type. 
	 *
	 * @param   o  SecureObject to be verified
	 * @param   in  the data to be spliced
	 * @param   iw  minimum match size to record
     * @throws SecurityException if the given SecureObject has not been verified
	 */
    public CharSplicer(char[][] in, int iw) throws SecurityException {
        super(in.length, iw);
        processing = true;
        data = new char[elements][];
        delims = new boolean[elements][];
        for (int i = 0; i < elements && processing; i++) {
            delims[i] = makeEmptyDelimArray(in[i].length);
            data[i] = new char[in[i].length];
            for (int j = 0; j < in[i].length && processing; j++) data[i][j] = in[i][j];
        }
        processing = false;
    }

    /**
	 * Implements the multi-match splice technique for
     * arrays of the primitive char data type. The resulting
     * LinkedList will contain char arrays as the entry data type.
	 *
	 * @see smti.util.shared.Splicer#splice()
	 */
    public LinkedList splice() {
        if (spliced) return result;
        processing = true;
        int limit = elements;
        while (limit > 1 && processing) {
            int place = 0;
            for (int o = 0; o < limit - 1 && processing; o += 2) {
                LinkedList tempseq = new LinkedList();
                int tempsum = 0;
                CharMatcher matcher = new CharMatcher(data[o], data[o + 1], delims[o], delims[o + 1], ignoreWeight);
                addFailSafe(matcher);
                int[][] matches = matcher.match();
                removeFailSafe(matcher);
                if (matches.length == 3 && matches[0].length > 0 && processing) {
                    int mpos = 1;
                    int fcur = matches[0][0];
                    int weight = matches[2][0];
                    tempsum += weight;
                    char[] temp = new char[weight];
                    boolean done = false;
                    while (!done && processing) {
                        for (int t = 0; t < weight && processing; t++, fcur++) temp[t] = data[o][fcur];
                        tempseq.add(temp);
                        if (mpos < matches[0].length && processing) {
                            fcur = matches[0][mpos];
                            weight = matches[2][mpos];
                            tempsum += weight;
                            temp = new char[weight];
                            mpos++;
                        } else done = true;
                    }
                } else {
                    spliced = true;
                    return result;
                }
                int asize = tempsum;
                int cur = 0;
                char[] newB = new char[asize];
                boolean[] newD = makeEmptyDelimArray(asize);
                while (tempseq.size() > 0 && processing) {
                    char[] t = (char[]) tempseq.removeFirst();
                    for (int p = 0; p < t.length && processing; p++, cur++) newB[cur] = t[p];
                    if (tempseq.size() > 0) newD[cur] = true;
                }
                data[place] = newB;
                delims[place] = newD;
                place++;
                newB = null;
                newD = null;
                matches = null;
                matcher = null;
                tempseq = null;
            }
            limit = (limit + 1) / 2;
            if (place < limit && processing) {
                data[place] = data[elements - 1];
                delims[place] = delims[elements - 1];
            }
        }
        if (limit == 1 && processing) {
            int last = 0;
            int nextDelim = 0;
            while (nextDelim < data[0].length && processing) {
                while (nextDelim < data[0].length && !delims[0][nextDelim] && processing) nextDelim++;
                char[] t = new char[nextDelim - last];
                for (int i = 0; i < t.length && processing; i++) t[i] = data[0][last + i];
                result.add(t);
                last = nextDelim;
                nextDelim++;
            }
            if (last < data[0].length) {
                char[] t = new char[data[0].length - last];
                for (int i = 0; i < t.length && processing; i++) t[i] = data[0][last + i];
                result.add(t);
            }
        }
        spliced = true;
        processing = false;
        return result;
    }
}
