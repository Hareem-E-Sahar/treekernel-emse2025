package jopt.csp.util;

/**
 * A sorted set of integer intervals
 */
public class IntValIntervalSet extends IntIntervalSet {

    private SortableIntList worthOfInterval;

    /**
     *  Creates a new set
     */
    public IntValIntervalSet() {
        super();
        worthOfInterval = new SortableIntList();
        clear();
    }

    /**
     * Constructor for cloning a set
     * 
     * @param set set to be cloned
     */
    private IntValIntervalSet(IntValIntervalSet set) {
        super(set);
        this.worthOfInterval = new SortableIntList(set.worthOfInterval);
    }

    /**
     * Creates a duplicate of this set
     */
    public Object clone() {
        return new IntValIntervalSet(this);
    }

    /**
     * Returns value of the interval at index idx
     */
    public int getWorth(int idx) {
        return worthOfInterval.get(idx);
    }

    /**
     * Adds a value to set
     */
    public void add(int val) {
        add(val, val);
    }

    /**
     * Utility function to remove interval from storage arrays and update
     * previous and next interval indices
     */
    protected void freeInterval(int idx) {
        super.freeInterval(idx);
        worthOfInterval.set(idx, 0);
    }

    /**
     * Adds a range of values to set
     */
    public void add(int start, int end) {
        add(start, end, 0);
    }

    public void add(IntValIntervalSet ivis) {
        int index = ivis.getFirstIntervalIndex();
        while (index >= 0) {
            add(ivis.getMin(index), ivis.getMax(index), ivis.getWorth(index));
            index = ivis.getNextIntervalIndex(index);
        }
    }

    public void remove(IntValIntervalSet ivis) {
        int index = ivis.getFirstIntervalIndex();
        while (index >= 0) {
            remove(ivis.getMin(index), ivis.getMax(index), ivis.getWorth(index));
            index = ivis.getNextIntervalIndex(index);
        }
    }

    public void add(IntIntervalSet ivis, int val) {
        int index = ivis.getFirstIntervalIndex();
        while (index >= 0) {
            add(ivis.getMin(index), ivis.getMax(index), val);
            index = ivis.getNextIntervalIndex(index);
        }
    }

    /**
     * Adds a range of values to set
     */
    public void add(int start, int end, int worth) {
        if (start > end) return;
        if (worth == 0) {
            return;
        }
        int idx = indexOfValue(start);
        if ((idx < 0 && indexOfValue(end) == idx) || idx == Integer.MIN_VALUE) this.addWithoutOverlap(start, end, idx, worth); else this.addWithOverlap(start, end, idx, worth);
        int startIndex = indexOfValue(start);
        this.checkForAdjacentIntervals(startIndex);
        int endIndex = indexOfValue(end);
        this.checkForAdjacentIntervals(endIndex);
        this.freeIntervalsOfZeroWorth();
    }

    public void freeIntervalsOfZeroWorth() {
        for (int i = 0; i < worthOfInterval.size(); i++) {
            if ((worthOfInterval.get(i) == 0) && (prevIdxList[i] != FREE_POSITION_MARKER)) {
                remove(startVals[i], endVals[i]);
            }
        }
    }

    /**
     * Handles adding intervals that do not overlap any existing intervals
     */
    private int addWithoutOverlap(int start, int end, int idx, int worth) {
        int newIdx = recordInterval(start, end);
        if (size == 0) {
            firstIntervalIdx = newIdx;
            lastIntervalIdx = newIdx;
        } else if (idx == -(firstIntervalIdx + 1)) {
            prevIdxList[firstIntervalIdx] = newIdx;
            nextIdxList[newIdx] = firstIntervalIdx;
            prevIdxList[newIdx] = -1;
            firstIntervalIdx = newIdx;
        } else if (idx == Integer.MIN_VALUE) {
            nextIdxList[lastIntervalIdx] = newIdx;
            prevIdxList[newIdx] = lastIntervalIdx;
            nextIdxList[newIdx] = -1;
            lastIntervalIdx = newIdx;
        } else {
            idx = -idx - 1;
            int prev = prevIdxList[idx];
            nextIdxList[newIdx] = idx;
            prevIdxList[idx] = newIdx;
            nextIdxList[prev] = newIdx;
            prevIdxList[newIdx] = prev;
        }
        adjustSize(((long) end) - ((long) start) + 1);
        notifyIntervalAddition(start, end);
        worthOfInterval.set(newIdx, worth);
        return newIdx;
    }

    /**
     * Handles adding intervals that overlap at least one existing interval 
     */
    private int addWithOverlap(int start, int end, int idx, int worth) {
        if (idx < 0) idx = -idx - 1;
        int s = startVals[idx];
        int e = endVals[idx];
        if (s > end) {
            return addWithoutOverlap(start, end, -idx - 1, worth);
        } else if (s <= start && end <= e) {
            int oldWorth = worthOfInterval.get(idx);
            int firstIdx = idx;
            int secondIdx = recordInterval(start, end);
            int thirdIdx = recordInterval(end, e);
            int fourthIdx = nextIdxList[idx];
            worthOfInterval.set(secondIdx, (worth + oldWorth));
            worthOfInterval.set(thirdIdx, oldWorth);
            prevIdxList[secondIdx] = firstIdx;
            prevIdxList[thirdIdx] = secondIdx;
            if (fourthIdx >= 0) {
                prevIdxList[fourthIdx] = thirdIdx;
            }
            nextIdxList[firstIdx] = secondIdx;
            nextIdxList[secondIdx] = thirdIdx;
            nextIdxList[thirdIdx] = fourthIdx;
            startVals[secondIdx] = start;
            startVals[thirdIdx] = end + 1;
            endVals[firstIdx] = start - 1;
            endVals[secondIdx] = end;
            endVals[thirdIdx] = e;
            if (firstIdx == lastIntervalIdx) {
                lastIntervalIdx = thirdIdx;
            }
            if (start == s) {
                freeInterval(firstIdx);
            }
            if (end == e) {
                freeInterval(thirdIdx);
            }
            return secondIdx;
        } else if (s > start && end < e) {
            int oldWorth = worthOfInterval.get(idx);
            int firstIdx = prevIdxList[idx];
            int secondIdx = recordInterval(start, s - 1);
            int thirdIdx = recordInterval(s, end);
            int fourthIdx = idx;
            worthOfInterval.set(secondIdx, (worth));
            worthOfInterval.set(thirdIdx, worth + oldWorth);
            prevIdxList[secondIdx] = firstIdx;
            prevIdxList[thirdIdx] = secondIdx;
            prevIdxList[fourthIdx] = thirdIdx;
            if (firstIdx >= 0) {
                nextIdxList[firstIdx] = secondIdx;
            }
            nextIdxList[secondIdx] = thirdIdx;
            nextIdxList[thirdIdx] = fourthIdx;
            startVals[secondIdx] = start;
            startVals[thirdIdx] = s;
            startVals[fourthIdx] = end + 1;
            endVals[secondIdx] = s - 1;
            endVals[thirdIdx] = end;
            if (firstIdx < 0) {
                firstIntervalIdx = secondIdx;
            }
            size += s - start;
            longSize += s - start;
            return secondIdx;
        } else if (s > start && end == e) {
            int oldWorth = worthOfInterval.get(idx);
            int firstIdx = prevIdxList[idx];
            int secondIdx = recordInterval(start, s - 1);
            int thirdIdx = idx;
            worthOfInterval.set(secondIdx, (worth));
            worthOfInterval.set(thirdIdx, worth + oldWorth);
            prevIdxList[secondIdx] = firstIdx;
            prevIdxList[thirdIdx] = secondIdx;
            if (firstIdx >= 0) {
                nextIdxList[firstIdx] = secondIdx;
            }
            nextIdxList[secondIdx] = thirdIdx;
            startVals[secondIdx] = start;
            startVals[thirdIdx] = s;
            endVals[secondIdx] = s - 1;
            endVals[thirdIdx] = end;
            if (firstIdx < 0) {
                firstIntervalIdx = secondIdx;
            }
            size += s - start;
            longSize += s - start;
            return secondIdx;
        } else if (s < start && end > e && e >= start) {
            int oldWorth = worthOfInterval.get(idx);
            int tEnd = end;
            boolean addThird = (endVals[idx] < tEnd);
            if (nextIdxList[idx] >= 0) {
                if (startVals[nextIdxList[idx]] <= tEnd) {
                    tEnd = startVals[nextIdxList[idx]] - 1;
                }
                addThird = (endVals[idx] < tEnd);
            }
            int firstIdx = idx;
            int secondIdx = recordInterval(start, e);
            int thirdIdx = nextIdxList[idx];
            if (addThird) {
                thirdIdx = recordInterval(e + 1, tEnd);
            }
            int fourthIdx = nextIdxList[idx];
            worthOfInterval.set(secondIdx, (worth + oldWorth));
            if (addThird) {
                worthOfInterval.set(thirdIdx, worth);
            }
            prevIdxList[secondIdx] = firstIdx;
            if (addThird) {
                prevIdxList[thirdIdx] = secondIdx;
            }
            if ((firstIdx >= 0) && (fourthIdx >= 0)) {
                if (addThird) {
                    prevIdxList[fourthIdx] = thirdIdx;
                } else {
                    prevIdxList[fourthIdx] = secondIdx;
                }
            }
            nextIdxList[firstIdx] = secondIdx;
            if (addThird) {
                nextIdxList[secondIdx] = thirdIdx;
                nextIdxList[thirdIdx] = fourthIdx;
            } else {
                nextIdxList[secondIdx] = fourthIdx;
            }
            startVals[secondIdx] = start;
            endVals[firstIdx] = start - 1;
            endVals[secondIdx] = e;
            if (addThird) {
                startVals[thirdIdx] = e + 1;
                endVals[thirdIdx] = tEnd;
            }
            size += (tEnd - e);
            longSize += (tEnd - e);
            if (fourthIdx < 0) {
                if (addThird) {
                    lastIntervalIdx = thirdIdx;
                } else {
                    lastIntervalIdx = secondIdx;
                }
                return lastIntervalIdx;
            } else {
                if ((tEnd + 1) <= end) {
                    addWithOverlap(tEnd + 1, end, fourthIdx, worth);
                }
            }
            return secondIdx;
        } else if (s >= start && end >= e) {
            int oldWorth = worthOfInterval.get(idx);
            int nextIdx = nextIdxList[idx];
            int tEnd = end;
            if ((nextIdx >= 0) && (tEnd >= startVals[nextIdxList[idx]])) {
                tEnd = startVals[nextIdxList[idx]] - 1;
            }
            int indexCalc = -(nextIdxList[idx] + 1);
            if (nextIdxList[idx] < 0) {
                indexCalc = Integer.MIN_VALUE;
            }
            if (s != start) {
                int nextIndex = indexCalc;
                if (indexCalc < 0) {
                    nextIndex = -(idx + 1);
                }
                addWithoutOverlap(start, s - 1, nextIndex, worth);
            }
            if (e != tEnd) {
                addWithoutOverlap(e + 1, tEnd, indexCalc, worth);
            }
            worthOfInterval.set(idx, oldWorth + worth);
            if (nextIdxList[idx] < 0) {
                return idx;
            } else if (tEnd + 1 <= end) {
                return addWithOverlap(tEnd + 1, end, nextIdx, worth);
            }
        }
        return -1;
    }

    public void remove(int start, int end, int worth) {
        add(start, end, (-1) * worth);
    }

    public void remove(int start, int end) {
        if (start > end) return;
        int startIndex = indexOfValue(start);
        int endIndex = indexOfValue(end);
        int startWorth = 0;
        int endWorth = 0;
        if (startIndex >= 0) {
            startWorth = worthOfInterval.get(startIndex);
        }
        if (endIndex >= 0) {
            endWorth = worthOfInterval.get(endIndex);
        }
        super.remove(start, end);
        if (startIndex >= 0) {
            worthOfInterval.set(startIndex, startWorth);
            if (nextIdxList[startIndex] != endIndex) {
                endIndex = nextIdxList[startIndex];
            }
        }
        if (endIndex >= 0) {
            worthOfInterval.set(endIndex, endWorth);
        }
    }

    public int getMinWorthOverRange(int start, int end) {
        if (isIntervalEmpty(start, end)) {
            return 0;
        }
        int index = indexOfValue(start);
        int minVal = Integer.MAX_VALUE;
        if (index < 0) {
            minVal = 0;
            index = firstIntervalIdx;
            while (endVals[index] <= start) {
                index = getNextIntervalIndex(index);
            }
        }
        while (startVals[index] <= end) {
            if (worthOfInterval.get(index) < minVal) {
                minVal = worthOfInterval.get(index);
            }
            int nextIndex = getNextIntervalIndex(index);
            if (nextIndex < 0) break;
            if ((endVals[index] + 1 < startVals[nextIndex]) && (endVals[index] < end)) {
                if (0 < minVal) {
                    minVal = 0;
                }
            }
            index = nextIndex;
        }
        return minVal;
    }

    public int getMaxWorthOverRange(int start, int end) {
        int index = indexOfValue(start);
        int maxVal = Integer.MIN_VALUE;
        if (firstIntervalIdx < 0) {
            return 0;
        }
        if (index < 0) {
            maxVal = 0;
            index = firstIntervalIdx;
            while ((index >= 0) && (endVals[index] <= start)) {
                index = getNextIntervalIndex(index);
            }
        }
        while ((index >= 0) && (startVals[index] <= end)) {
            if (worthOfInterval.get(index) > maxVal) {
                maxVal = worthOfInterval.get(index);
            }
            int nextIndex = getNextIntervalIndex(index);
            if (nextIndex < 0) break;
            if ((endVals[index] + 1 < startVals[nextIndex]) && (startVals[nextIndex] < end)) {
                if (0 > maxVal) {
                    maxVal = 0;
                }
            }
            index = nextIndex;
        }
        return maxVal;
    }

    public IntIntervalSet getAllRangesWithMinWorth(int minWorth) {
        int currentIndex = firstIntervalIdx;
        IntIntervalSet set = new IntIntervalSet();
        while (currentIndex >= 0) {
            if (worthOfInterval.get(currentIndex) >= minWorth) {
                set.add(startVals[currentIndex], endVals[currentIndex]);
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return set;
    }

    public IntIntervalSet getAllRangesWithMaxWorth(int maxWorth) {
        int currentIndex = firstIntervalIdx;
        IntIntervalSet set = new IntIntervalSet();
        while (currentIndex >= 0) {
            if (worthOfInterval.get(currentIndex) <= maxWorth) {
                set.add(startVals[currentIndex], endVals[currentIndex]);
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return set;
    }

    public IntIntervalSet getAllRangesWithMinWorth(int minWorth, int start, int end) {
        int currentIndex = indexOfValue(start);
        if (currentIndex == Integer.MIN_VALUE) {
            return new IntIntervalSet();
        }
        if (currentIndex < 0) {
            currentIndex = -(currentIndex + 1);
        }
        IntIntervalSet set = new IntIntervalSet();
        while (currentIndex >= 0) {
            if (worthOfInterval.get(currentIndex) >= minWorth) {
                int startVal = Math.max(startVals[currentIndex], start);
                int endVal = Math.min(endVals[currentIndex], end);
                set.add(startVal, endVal);
            }
            currentIndex = getNextIntervalIndex(currentIndex);
            if ((currentIndex > 0) && (end < startVals[currentIndex])) {
                return set;
            }
        }
        return set;
    }

    public IntIntervalSet getAllRangesWithMaxWorth(int maxWorth, int start, int end) {
        int currentIndex = indexOfValue(start);
        if (currentIndex < 0) {
            currentIndex = -(currentIndex + 1);
        }
        IntIntervalSet set = new IntIntervalSet();
        while (currentIndex >= 0) {
            if (worthOfInterval.get(currentIndex) <= maxWorth) {
                int startVal = Math.max(startVals[currentIndex], start);
                int endVal = Math.min(endVals[currentIndex], end);
                set.add(startVal, endVal);
            }
            currentIndex = getNextIntervalIndex(currentIndex);
            if ((currentIndex > 0) && (end < startVals[currentIndex])) {
                return set;
            }
        }
        return set;
    }

    /**
     * Checks for and combines with intervals adjacent to the interval
     * at the specified index.
     */
    private void checkForAdjacentIntervals(int newIdx) {
        if (newIdx < 0) {
            return;
        }
        if (newIdx != firstIntervalIdx) {
            int previousIdx = prevIdxList[newIdx];
            if ((startVals[newIdx] - 1 == endVals[previousIdx]) && (worthOfInterval.get(newIdx) == worthOfInterval.get(previousIdx))) {
                startVals[newIdx] = startVals[previousIdx];
                int idxBeforePrevious = prevIdxList[previousIdx];
                prevIdxList[newIdx] = idxBeforePrevious;
                if (idxBeforePrevious != -1) nextIdxList[idxBeforePrevious] = newIdx; else firstIntervalIdx = newIdx;
                freeInterval(previousIdx);
            }
        }
        if (newIdx != lastIntervalIdx) {
            int nextIdx = nextIdxList[newIdx];
            if ((endVals[newIdx] + 1 == startVals[nextIdx]) && (worthOfInterval.get(newIdx) == worthOfInterval.get(nextIdx))) {
                endVals[newIdx] = endVals[nextIdx];
                int idxAfterNext = nextIdxList[nextIdx];
                nextIdxList[newIdx] = idxAfterNext;
                if (idxAfterNext != -1) prevIdxList[nextIdxList[nextIdx]] = newIdx; else lastIntervalIdx = newIdx;
                freeInterval(nextIdx);
            }
        }
    }

    public boolean equals(IntValIntervalSet ivis) {
        int thisIndex = this.firstIntervalIdx;
        int ivisIndex = ivis.firstIntervalIdx;
        if (this.size != ivis.size) {
            return false;
        }
        while ((thisIndex >= 0) && (ivisIndex >= 0)) {
            if ((ivis.startVals[ivisIndex] != this.startVals[thisIndex]) || (ivis.endVals[ivisIndex] != this.endVals[thisIndex]) || (ivis.worthOfInterval.get(ivisIndex) != this.worthOfInterval.get(thisIndex))) {
                return false;
            }
            thisIndex = this.getNextIntervalIndex(thisIndex);
            ivisIndex = ivis.getNextIntervalIndex(ivisIndex);
        }
        if ((thisIndex < 0) && (ivisIndex < 0)) {
            return true;
        } else {
            return false;
        }
    }

    public int getMinDiff(IntValIntervalSet ivis) {
        int ivisStart = ivis.getMin();
        int ivisEnd = ivis.getMax();
        int minDiff = Integer.MAX_VALUE;
        int ivisMin = -(ivis.getMaxWorthOverRange(ivisStart, this.getMin()));
        if ((this.getMin() > ivisStart) && (ivisMin < minDiff)) {
            minDiff = ivisMin;
        }
        ivisMin = -(ivis.getMaxWorthOverRange(this.getMax(), ivisEnd));
        if ((this.getMax() < ivisEnd) && (ivisMin < minDiff)) {
            minDiff = ivisMin;
        }
        int currentIndex = firstIntervalIdx;
        while (currentIndex >= 0) {
            int currentDiff = worthOfInterval.get(currentIndex) - ivis.getMaxWorthOverRange(startVals[currentIndex], endVals[currentIndex]);
            if (currentDiff < minDiff) {
                minDiff = currentDiff;
            }
            if (nextIdxList[currentIndex] >= 0) {
                if (startVals[nextIdxList[currentIndex]] > (endVals[currentIndex] + 1)) {
                    int gapDiff = -(ivis.getMaxWorthOverRange(endVals[currentIndex] + 1, startVals[nextIdxList[currentIndex]] - 1));
                    if (gapDiff < minDiff) {
                        minDiff = gapDiff;
                    }
                }
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return minDiff;
    }

    public int getMaxDiff(IntValIntervalSet ivis) {
        int ivisStart = ivis.getMin();
        int ivisEnd = ivis.getMax();
        int maxDiff = Integer.MIN_VALUE;
        int ivisMin = -(ivis.getMaxWorthOverRange(ivisStart, this.getMin()));
        if ((this.getMin() > ivisStart) && (ivisMin > maxDiff)) {
            maxDiff = ivisMin;
        }
        ivisMin = -(ivis.getMaxWorthOverRange(this.getMax(), ivisEnd));
        if ((this.getMax() < ivisEnd) && (ivisMin > maxDiff)) {
            maxDiff = ivisMin;
        }
        int currentIndex = firstIntervalIdx;
        while (currentIndex >= 0) {
            int worth = worthOfInterval.get(currentIndex);
            int minOfOther = ivis.getMinWorthOverRange(startVals[currentIndex], endVals[currentIndex]);
            int currentDiff = worth - minOfOther;
            if (currentDiff > maxDiff) {
                maxDiff = currentDiff;
            }
            if (nextIdxList[currentIndex] >= 0) {
                if (startVals[nextIdxList[currentIndex]] > (endVals[currentIndex] + 1)) {
                    int gapDiff = -(ivis.getMinWorthOverRange(endVals[currentIndex] + 1, startVals[nextIdxList[currentIndex]] - 1));
                    if (gapDiff > maxDiff) {
                        maxDiff = gapDiff;
                    }
                }
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return maxDiff;
    }

    public int getMaxDiff(IntValIntervalSet ivis, int start, int end) {
        int ivisStart = ivis.getMin();
        int ivisEnd = ivis.getMax();
        int maxDiff = Integer.MIN_VALUE;
        int startIndex = indexOfValue(start);
        int endIndex = indexOfValue(end);
        if (startIndex < 0) {
            startIndex = -(startIndex + 1);
        }
        if (endIndex < 0) {
            endIndex = -(endIndex + 1);
        }
        if (ivisStart < start) {
            ivisStart = start;
        }
        if (ivisEnd > end) {
            ivisEnd = end;
        }
        int ivisMax = -(ivis.getMaxWorthOverRange(ivisStart, startVals[startIndex]));
        if ((startVals[startIndex] > ivisStart) && (ivisMax > maxDiff)) {
            maxDiff = ivisMax;
        }
        ivisMax = -(ivis.getMaxWorthOverRange(endVals[endIndex], ivisEnd));
        if ((endVals[endIndex] < ivisEnd) && (ivisMax > maxDiff)) {
            maxDiff = ivisMax;
        }
        int currentIndex = startIndex;
        int postEndIndex = nextIdxList[endIndex];
        while (currentIndex != postEndIndex) {
            int worth = worthOfInterval.get(currentIndex);
            int rangeStart = startVals[currentIndex];
            int rangeEnd = endVals[currentIndex];
            if (rangeStart < start) {
                rangeStart = start;
            }
            if (rangeEnd > end) {
                rangeEnd = end;
            }
            int minOfOther = ivis.getMinWorthOverRange(rangeStart, rangeEnd);
            int currentDiff = worth - minOfOther;
            if (currentDiff > maxDiff) {
                maxDiff = currentDiff;
            }
            if (nextIdxList[currentIndex] >= 0) {
                if (startVals[nextIdxList[currentIndex]] > (endVals[currentIndex] + 1)) {
                    int gapDiff = -(ivis.getMinWorthOverRange(endVals[currentIndex] + 1, startVals[nextIdxList[currentIndex]] - 1));
                    if (gapDiff > maxDiff) {
                        maxDiff = gapDiff;
                    }
                }
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return maxDiff;
    }

    public int getMinDiff(IntValIntervalSet ivis, int start, int end) {
        int ivisStart = ivis.getMin();
        int ivisEnd = ivis.getMax();
        int minDiff = Integer.MAX_VALUE;
        int startIndex = indexOfValue(start);
        int endIndex = indexOfValue(end);
        if (startIndex < 0) {
            startIndex = -(startIndex + 1);
        }
        if (endIndex < 0) {
            endIndex = -(endIndex + 1);
        }
        if (ivisStart < start) {
            ivisStart = start;
        }
        if (ivisEnd > end) {
            ivisEnd = end;
        }
        int ivisMin = -(ivis.getMaxWorthOverRange(ivisStart, startVals[startIndex]));
        if ((startVals[startIndex] > ivisStart) && (ivisMin < minDiff)) {
            minDiff = ivisMin;
        }
        ivisMin = -(ivis.getMaxWorthOverRange(endVals[endIndex], ivisEnd));
        if ((endVals[endIndex] < ivisEnd) && (ivisMin < minDiff)) {
            minDiff = ivisMin;
        }
        int currentIndex = startIndex;
        int postEndIndex = nextIdxList[endIndex];
        while (currentIndex != postEndIndex) {
            int worth = worthOfInterval.get(currentIndex);
            int rangeStart = startVals[currentIndex];
            int rangeEnd = endVals[currentIndex];
            if (rangeStart < start) {
                rangeStart = start;
            }
            if (rangeEnd > end) {
                rangeEnd = end;
            }
            int maxOfOther = ivis.getMaxWorthOverRange(rangeStart, rangeEnd);
            int currentDiff = worth - maxOfOther;
            if (currentDiff < minDiff) {
                minDiff = currentDiff;
            }
            if (nextIdxList[currentIndex] >= 0) {
                if (startVals[nextIdxList[currentIndex]] > (endVals[currentIndex] + 1)) {
                    int gapDiff = -(ivis.getMinWorthOverRange(endVals[currentIndex] + 1, startVals[nextIdxList[currentIndex]] - 1));
                    if (gapDiff < minDiff) {
                        minDiff = gapDiff;
                    }
                }
            }
            currentIndex = getNextIntervalIndex(currentIndex);
        }
        return minDiff;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("{");
        int idx = firstIntervalIdx;
        while (idx >= 0) {
            buf.append("[");
            buf.append("i:" + idx + ",");
            buf.append(startVals[idx]);
            buf.append("..");
            buf.append(endVals[idx]);
            buf.append(",");
            buf.append(worthOfInterval.get(idx));
            buf.append("]");
            idx = nextIdxList[idx];
            if (idx >= 0) buf.append(", ");
        }
        buf.append("}");
        return buf.toString();
    }
}
