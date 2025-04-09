package net.sf.javagimmicks.collections.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

class DifferenceAlgorithm<T> {

    protected final List<T> _fromList;

    protected final List<T> _toList;

    protected final Comparator<T> _comparator;

    protected final DifferenceList<T> _differences = new DefaultDifferenceList<T>();

    protected DefaultDifference<T> _pending;

    protected DifferenceAlgorithm(List<T> fromList, List<T> toList, Comparator<T> comparator) {
        _fromList = fromList;
        _toList = toList;
        _comparator = comparator;
        traverseSequences();
        if (_pending != null) {
            _differences.add(_pending);
        }
    }

    public DifferenceList<T> getDifferences() {
        return _differences;
    }

    /**
	 * Traverses the sequences, seeking the longest common subsequences,
	 * invoking the methods <code>finishedA</code>, <code>finishedB</code>,
	 * <code>onANotB</code>, and <code>onBNotA</code>.
	 */
    protected void traverseSequences() {
        List<Integer> matches = getLongestCommonSubsequences();
        final int lastIndexA = _fromList.size() - 1;
        final int lastIndexB = _toList.size() - 1;
        int indexB = 0;
        int indexA;
        int lastMatchIndex = matches.size() - 1;
        for (indexA = 0; indexA <= lastMatchIndex; ++indexA) {
            Integer bLine = matches.get(indexA);
            if (bLine == null) {
                onANotB(indexA, indexB);
            } else {
                while (indexB < bLine) {
                    onBNotA(indexA, indexB++);
                }
                onMatch(indexA, indexB++);
            }
        }
        boolean calledFinishA = false;
        boolean calledFinishB = false;
        while (indexA <= lastIndexA || indexB <= lastIndexB) {
            if (indexA == lastIndexA + 1 && indexB <= lastIndexB) {
                if (!calledFinishA && callFinishedA()) {
                    finishedA(lastIndexA);
                    calledFinishA = true;
                } else {
                    while (indexB <= lastIndexB) {
                        onBNotA(indexA, indexB++);
                    }
                }
            }
            if (indexB == lastIndexB + 1 && indexA <= lastIndexA) {
                if (!calledFinishB && callFinishedB()) {
                    finishedB(lastIndexB);
                    calledFinishB = true;
                } else {
                    while (indexA <= lastIndexA) {
                        onANotB(indexA++, indexB);
                    }
                }
            }
            if (indexA <= lastIndexA) {
                onANotB(indexA++, indexB);
            }
            if (indexB <= lastIndexB) {
                onBNotA(indexA, indexB++);
            }
        }
    }

    /**
	 * Override and return true in order to have <code>finishedA</code>
	 * invoked at the last element in the <code>a</code> list.
	 */
    protected boolean callFinishedA() {
        return false;
    }

    /**
	 * Override and return true in order to have <code>finishedB</code>
	 * invoked at the last element in the <code>b</code> list.
	 */
    protected boolean callFinishedB() {
        return false;
    }

    /**
	 * Invoked at the last element in <code>a</code>, if
	 * <code>callFinishedA</code> returns true.
	 */
    protected void finishedA(int lastA) {
    }

    /**
	 * Invoked at the last element in <code>b</code>, if
	 * <code>callFinishedB</code> returns true.
	 */
    protected void finishedB(int lastB) {
    }

    /**
	 * Invoked for elements in <code>a</code> and not in <code>b</code>.
	 */
    protected void onANotB(int indexA, int indexB) {
        if (_pending == null) {
            _pending = new DefaultDifference<T>();
            _pending.setDeleteStartIndex(indexA);
            _pending.setDeleteEndIndex(indexA);
            _pending.setAddStartIndex(indexB);
            _pending.setFromList(_fromList);
            _pending.setToList(_toList);
        } else {
            setDeleted(_pending, indexA);
        }
    }

    /**
	 * Invoked for elements in <code>b</code> and not in <code>a</code>.
	 */
    protected void onBNotA(int indexA, int indexB) {
        if (_pending == null) {
            _pending = new DefaultDifference<T>();
            _pending.setDeleteStartIndex(indexA);
            _pending.setAddStartIndex(indexB);
            _pending.setAddEndIndex(indexB);
            _pending.setFromList(_fromList);
            _pending.setToList(_toList);
        } else {
            setAdded(_pending, indexB);
        }
    }

    /**
	 * Invoked for elements matching in <code>a</code> and <code>b</code>.
	 */
    protected void onMatch(int indexA, int indexB) {
        if (_pending != null) {
            _differences.add(_pending);
            _pending = null;
        }
    }

    /**
	 * Compares the two objects, using the comparator provided with the
	 * constructor, if any.
	 */
    @SuppressWarnings("unchecked")
    protected boolean equals(T x, T y) {
        if (_comparator == null) {
            if (x instanceof Comparable) {
                return ((Comparable) x).compareTo(y) == 0;
            } else {
                return x.equals(y);
            }
        } else {
            return _comparator.compare(x, y) == 0;
        }
    }

    /**
	 * Returns an array of the longest common subsequences.
	 */
    protected List<Integer> getLongestCommonSubsequences() {
        int startIndexA = 0;
        int endIndexA = _fromList.size() - 1;
        int startIndexB = 0;
        int endIndexB = _toList.size() - 1;
        final TreeMap<Integer, Integer> matches = new TreeMap<Integer, Integer>();
        while (startIndexA <= endIndexA && startIndexB <= endIndexB && equals(_fromList.get(startIndexA), _toList.get(startIndexB))) {
            matches.put(startIndexA++, startIndexB++);
        }
        while (startIndexA <= endIndexA && startIndexB <= endIndexB && equals(_fromList.get(endIndexA), _toList.get(endIndexB))) {
            matches.put(endIndexA--, endIndexB--);
        }
        final Map<T, List<Integer>> bMatches;
        if (_comparator == null) {
            if (_fromList.get(0) instanceof Comparable<?>) {
                bMatches = new TreeMap<T, List<Integer>>();
            } else {
                bMatches = new HashMap<T, List<Integer>>();
            }
        } else {
            bMatches = new TreeMap<T, List<Integer>>(_comparator);
        }
        for (int indexB = startIndexB; indexB <= endIndexB; ++indexB) {
            T key = _toList.get(indexB);
            List<Integer> positions = bMatches.get(key);
            if (positions == null) {
                positions = new ArrayList<Integer>();
                bMatches.put(key, positions);
            }
            positions.add(indexB);
        }
        final TreeMap<Integer, Integer> thresh = new TreeMap<Integer, Integer>();
        final TreeMap<Integer, Link> links = new TreeMap<Integer, Link>();
        for (int indexA = startIndexA; indexA <= endIndexA; ++indexA) {
            T key = _fromList.get(indexA);
            List<Integer> positions = bMatches.get(key);
            if (positions == null) {
                continue;
            }
            Integer k = 0;
            ListIterator<Integer> positionIter = positions.listIterator(positions.size());
            while (positionIter.hasPrevious()) {
                Integer j = positionIter.previous();
                k = insert(thresh, j, k);
                if (k != null) {
                    Link value = k > 0 ? links.get(k - 1) : null;
                    links.put(k, new Link(value, indexA, j));
                }
            }
        }
        if (!thresh.isEmpty()) {
            for (Link link = links.get(thresh.lastKey()); link != null; link = link._link) {
                matches.put(link._x, link._y);
            }
        }
        return toList(matches);
    }

    protected static List<Integer> toList(TreeMap<Integer, Integer> map) {
        Integer[] result = new Integer[map.size() == 0 ? 0 : 1 + map.lastKey()];
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            result[entry.getKey()] = entry.getValue();
        }
        return Arrays.asList(result);
    }

    protected static boolean isNonzero(Integer i) {
        return i != null && i != 0;
    }

    protected boolean isGreaterThan(TreeMap<Integer, Integer> thresh, Integer index, Integer val) {
        Integer lhs = thresh.get(index);
        return lhs != null && (val != null && lhs > val);
    }

    protected boolean isLessThan(TreeMap<Integer, Integer> thresh, Integer index, Integer val) {
        Integer lhs = thresh.get(index);
        return lhs != null && (val == null || lhs < val);
    }

    /**
	 * Adds the given value to the "end" of the threshold map, that is, with the
	 * greatest index/key.
	 */
    protected void append(TreeMap<Integer, Integer> thresh, Integer value) {
        int addIdx = thresh.isEmpty() ? 0 : thresh.lastKey() + 1;
        thresh.put(addIdx, value);
    }

    /**
	 * Inserts the given values into the threshold map.
	 */
    protected Integer insert(TreeMap<Integer, Integer> thresh, Integer j, Integer k) {
        if (isNonzero(k) && isGreaterThan(thresh, k, j) && isLessThan(thresh, k - 1, j)) {
            thresh.put(k, j);
        } else {
            int highIndex = -1;
            if (isNonzero(k)) {
                highIndex = k;
            } else if (!thresh.isEmpty()) {
                highIndex = thresh.lastKey();
            }
            if (highIndex == -1 || j.compareTo(thresh.get(thresh.lastKey())) > 0) {
                append(thresh, j);
                k = highIndex + 1;
            } else {
                int lowIndex = 0;
                while (lowIndex <= highIndex) {
                    int index = (highIndex + lowIndex) / 2;
                    Integer val = thresh.get(index);
                    int compareResult = j.compareTo(val);
                    if (compareResult == 0) {
                        return null;
                    } else if (compareResult > 0) {
                        lowIndex = index + 1;
                    } else {
                        highIndex = index - 1;
                    }
                }
                thresh.put(lowIndex, j);
                k = lowIndex;
            }
        }
        return k;
    }

    protected static void setDeleted(DefaultDifference<?> d, int index) {
        d.setDeleteStartIndex(Math.min(index, d.getDeleteStartIndex()));
        d.setDeleteEndIndex(Math.max(index, d.getDeleteEndIndex()));
    }

    protected static void setAdded(DefaultDifference<?> d, int index) {
        d.setAddStartIndex(Math.min(index, d.getAddStartIndex()));
        d.setAddEndIndex(Math.max(index, d.getAddEndIndex()));
    }

    protected static class Link {

        protected Link(Link link, Integer x, Integer y) {
            _link = link;
            _x = x;
            _y = y;
        }

        public final Link _link;

        public final Integer _x;

        public final Integer _y;
    }
}
