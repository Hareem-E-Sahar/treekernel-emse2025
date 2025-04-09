package org.xteam.cs.lex.model;

import java.util.*;

/** 
 * CharSet implemented with intervals
 *
 * [fixme: optimizations possible]
 *
 * @author Gerwin Klein
 * @version JFlex 1.4.1, $Revision: 2.9 $, $Date: 2004/11/07 00:12:48 $
 */
public final class IntCharSet {

    public static final class Interval {

        char start;

        char end;

        public Interval(char start, char end) {
            if (start <= end) {
                this.start = start;
                this.end = end;
            } else {
                this.start = end;
                this.end = start;
            }
        }

        public Interval(Interval other) {
            this.start = other.start;
            this.end = other.end;
        }

        public boolean contains(char point) {
            return start <= point && end >= point;
        }

        public boolean contains(Interval other) {
            return this.start <= other.start && this.end >= other.end;
        }

        public Interval copy() {
            return new Interval(start, end);
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Interval)) return false;
            Interval other = (Interval) o;
            return other.start == this.start && other.end == this.end;
        }

        public String toString() {
            StringBuffer result = new StringBuffer();
            if (isPrintable(start)) result.append("'" + start + "'"); else result.append("\\" + (int) start);
            if (start != end) {
                result.append('-');
                if (isPrintable(end)) result.append("'" + end + "'"); else result.append("\\" + (int) end);
            }
            return result.toString();
        }

        private static boolean isPrintable(char c) {
            return c > 31 && c < 127;
        }

        public boolean isSingle() {
            return start == end;
        }

        public char start() {
            return start;
        }

        public int end() {
            return end;
        }
    }

    private List<Interval> intervalls;

    private int pos;

    public IntCharSet() {
        this.intervalls = new ArrayList<Interval>();
    }

    public IntCharSet(char c) {
        this(new Interval(c, c));
    }

    public IntCharSet(Interval intervall) {
        this();
        intervalls.add(new Interval(intervall));
    }

    public IntCharSet(List<Interval> chars) {
        this();
        for (Interval i : chars) {
            add(new Interval(i));
        }
    }

    /**
	 * returns the index of the intervall that contains
	 * the character c, -1 if there is no such intevall
	 *
	 * @prec: true
	 * @post: -1 <= return < intervalls.size() && 
	 *        (return > -1 --> intervalls[return].contains(c))
	 * 
	 * @param c  the character
	 * @return the index of the enclosing interval, -1 if no such interval  
	 */
    private int indexOf(char c) {
        int start = 0;
        int end = intervalls.size() - 1;
        while (start <= end) {
            int check = (start + end) / 2;
            Interval i = (Interval) intervalls.get(check);
            if (start == end) return i.contains(c) ? start : -1;
            if (c < i.start) {
                end = check - 1;
                continue;
            }
            if (c > i.end) {
                start = check + 1;
                continue;
            }
            return check;
        }
        return -1;
    }

    public IntCharSet add(IntCharSet set) {
        for (int i = 0; i < set.intervalls.size(); i++) add((Interval) set.intervalls.get(i));
        return this;
    }

    public void add(Interval intervall) {
        int size = intervalls.size();
        for (int i = 0; i < size; i++) {
            Interval elem = (Interval) intervalls.get(i);
            if (elem.end + 1 < intervall.start) continue;
            if (elem.contains(intervall)) return;
            if (elem.start > intervall.end + 1) {
                intervalls.add(i, new Interval(intervall));
                return;
            }
            if (intervall.start < elem.start) elem.start = intervall.start;
            if (intervall.end <= elem.end) return;
            elem.end = intervall.end;
            i++;
            while (i < size) {
                Interval x = (Interval) intervalls.get(i);
                if (x.start > elem.end + 1) return;
                elem.end = x.end;
                intervalls.remove(i);
                size--;
            }
            return;
        }
        intervalls.add(new Interval(intervall));
    }

    public void add(char c) {
        int size = intervalls.size();
        for (int i = 0; i < size; i++) {
            Interval elem = (Interval) intervalls.get(i);
            if (elem.end + 1 < c) continue;
            if (elem.contains(c)) return;
            if (elem.start > c + 1) {
                intervalls.add(i, new Interval(c, c));
                return;
            }
            if (c + 1 == elem.start) {
                elem.start = c;
                return;
            }
            elem.end = c;
            if (i + 1 >= size) return;
            Interval x = (Interval) intervalls.get(i + 1);
            if (x.start <= c + 1) {
                elem.end = x.end;
                intervalls.remove(i + 1);
            }
            return;
        }
        intervalls.add(new Interval(c, c));
    }

    public boolean contains(char singleChar) {
        return indexOf(singleChar) >= 0;
    }

    /**
	 * o instanceof Interval
	 */
    public boolean equals(Object o) {
        IntCharSet set = (IntCharSet) o;
        if (intervalls.size() != set.intervalls.size()) return false;
        for (int i = 0; i < intervalls.size(); i++) {
            if (!intervalls.get(i).equals(set.intervalls.get(i))) return false;
        }
        return true;
    }

    private char min(char a, char b) {
        return a <= b ? a : b;
    }

    private char max(char a, char b) {
        return a >= b ? a : b;
    }

    public IntCharSet and(IntCharSet set) {
        IntCharSet result = new IntCharSet();
        int i = 0;
        int j = 0;
        int size = intervalls.size();
        int setSize = set.intervalls.size();
        while (i < size && j < setSize) {
            Interval x = (Interval) this.intervalls.get(i);
            Interval y = (Interval) set.intervalls.get(j);
            if (x.end < y.start) {
                i++;
                continue;
            }
            if (y.end < x.start) {
                j++;
                continue;
            }
            result.intervalls.add(new Interval(max(x.start, y.start), min(x.end, y.end)));
            if (x.end >= y.end) j++;
            if (y.end >= x.end) i++;
        }
        return result;
    }

    public void sub(IntCharSet set) {
        int i = 0;
        int j = 0;
        int setSize = set.intervalls.size();
        while (i < intervalls.size() && j < setSize) {
            Interval x = (Interval) this.intervalls.get(i);
            Interval y = (Interval) set.intervalls.get(j);
            if (x.end < y.start) {
                i++;
                continue;
            }
            if (y.end < x.start) {
                j++;
                continue;
            }
            if (x.start == y.start && x.end == y.end) {
                intervalls.remove(i);
                j++;
                continue;
            }
            if (x.start == y.start) {
                x.start = (char) (y.end + 1);
                j++;
                continue;
            }
            if (x.end == y.end) {
                x.end = (char) (y.start - 1);
                i++;
                j++;
                continue;
            }
            intervalls.add(i, new Interval(x.start, (char) (y.start - 1)));
            x.start = (char) (y.end + 1);
            i++;
            j++;
        }
    }

    public boolean containsElements() {
        return intervalls.size() > 0;
    }

    public int numIntervalls() {
        return intervalls.size();
    }

    public Interval getNext() {
        if (pos == intervalls.size()) pos = 0;
        return intervalls.get(pos++);
    }

    /**
	 * Create a caseless version of this charset.
	 * <p>
	 * The caseless version contains all characters of this char set,
	 * and additionally all lower/upper/title case variants of the 
	 * characters in this set.
	 * 
	 * @return a caseless copy of this set
	 */
    public IntCharSet getCaseless() {
        IntCharSet n = copy();
        int size = intervalls.size();
        for (int i = 0; i < size; i++) {
            Interval elem = (Interval) intervalls.get(i);
            for (char c = elem.start; c <= elem.end; c++) {
                n.add(Character.toLowerCase(c));
                n.add(Character.toUpperCase(c));
                n.add(Character.toTitleCase(c));
            }
        }
        return n;
    }

    /**
	 * Make a string representation of this char set.
	 * 
	 * @return a string representing this char set.
	 */
    public String toString() {
        StringBuffer result = new StringBuffer("{ ");
        for (int i = 0; i < intervalls.size(); i++) result.append(intervalls.get(i));
        result.append(" }");
        return result.toString();
    }

    /** 
	 * Return a (deep) copy of this char set
	 * 
	 * @return the copy
	 */
    public IntCharSet copy() {
        IntCharSet result = new IntCharSet();
        int size = intervalls.size();
        for (int i = 0; i < size; i++) {
            Interval iv = ((Interval) intervalls.get(i)).copy();
            result.intervalls.add(iv);
        }
        return result;
    }
}
