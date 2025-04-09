package jumbo.euclid;

import java.util.Hashtable;
import jumbo.xml.util.Util;

/** 
    Stores a unique set of Strings.  By default this is case-sensitive, but
the user can set this to insensitive.  The actual value stored is 
case-sensitive.  Also keeps a score of the indexNumber of each element,
and can thus be used for keying Vectors.
@author (C) P. Murray-Rust, 1996
*/
public class StringSet {

    String elem[];

    int nbuff = 5;

    int nelem;

    Hashtable ht;

    Hashtable htindex;

    boolean ignoreCase = false;

    public static final int IGNORE_CASE = 1;

    /** default contsructor is case-sensitive 
*/
    public StringSet() {
        initialise();
    }

    private void initialise() {
        elem = new String[nbuff];
        for (int i = 0; i < nbuff; i++) {
            elem[i] = new String();
        }
        ht = new Hashtable();
        htindex = new Hashtable();
    }

    /** set case insensitivity if 'ignore' arg is 'StringSet.IGNORE_CASE' */
    public StringSet(int ignore) {
        this();
        if (ignore == IGNORE_CASE) {
            this.ignoreCase = true;
        }
    }

    /** create a StringSet from the Strings (duplicates are ignored); case-sensitive */
    public StringSet(String strings[]) {
        this();
        for (int i = 0; i < strings.length; i++) {
            addElement(strings[i]);
        }
    }

    /** contract buffer to exact number of elements
*/
    public void contract() {
        if (nelem < elem.length) {
            String[] newelem = new String[nelem];
            System.arraycopy(elem, 0, newelem, 0, nelem);
            elem = newelem;
            nbuff = nelem;
        }
    }

    /** return all members (case sensitive by default) 
*/
    public String[] getElements() {
        contract();
        return elem;
    }

    public int size() {
        return nelem;
    }

    /** state of case-sensitivity 
*/
    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    String getKey(String value) {
        if (value == null) return null;
        String key = value;
        if (ignoreCase) {
            key = key.toLowerCase();
        }
        return key;
    }

    /** adds an element.  If String is already present, does nothing 
*/
    public void addElement(String value) {
        String key = getKey(value);
        if (this.contains(key)) {
            return;
        }
        if (nelem >= nbuff) {
            nbuff *= 2;
            String temp[] = new String[nbuff];
            for (int i = 0; i < nelem; i++) {
                temp[i] = elem[i];
            }
            elem = temp;
        }
        elem[nelem] = value;
        ht.put(key, value);
        htindex.put(key, new Integer(nelem));
        nelem++;
    }

    /** does Set contain String? (according to case-sensitivity */
    public boolean contains(String value) {
        return ht.containsKey(getKey(value));
    }

    /** index of a given String (default is case-sensitive); -1 if not found */
    public int indexOf(String value) {
        String key = getKey(value);
        if (htindex.containsKey(key)) {
            return ((Integer) htindex.get(key)).intValue();
        } else {
            return -1;
        }
    }

    /** return (case-sensitive) element */
    public String elementAt(int i) {
        if (i < 0 || i > nelem) {
            return null;
        }
        return elem[i];
    }

    /** catenates one set onto another.  Fails if Sets differ in  
    case-sensitivity.
@exception StringSetException mixed case is not allowed 
*/
    public void addSet(StringSet is) throws StringSetException {
        if (is.ignoreCase != this.ignoreCase) {
            throw new StringSetException("addSet: mixed case not allowed");
        }
        if (is == null || is.size() == 0) return;
        for (int i = 0; i < is.nelem; i++) {
            String s = is.elementAt(i);
            if (!this.contains(s)) {
                this.addElement(s);
            }
        }
    }

    public void debug() {
        for (int i = 0; i < nelem; i++) {
            System.out.print(" " + elem[i]);
        }
        System.out.println("");
    }

    /** outputs the components as a list separated by "\n" - bad luck if they
 already contain this!
*/
    public String toString() {
        if (size() == 0) return "";
        StringBuffer temp = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            temp.append("\n");
            temp.append(elem[i]);
        }
        return temp.toString();
    }

    /**
    quick sort - modified from p96 - 97 (Hansen - C++ answer book)
    
    Scalar sort refers to sorting StringSet (and similar classes)
    where the objects themeselves are sorted.
    
    Index sort refers to sorting indexes (held as IntSet's) to the object
    and getting the sorted object(s) with reorderBy(IntSet idx);
    
*/
    private void xfswap(String[] s, int a, int b) {
        String tmp = s[a];
        s[a] = s[b];
        s[b] = tmp;
    }

    static final int CUTOFF = 16;

    private void inssort(int left, int right) {
        int k;
        for (int i = left + 1; i <= right; i++) {
            String v = elem[i];
            int j;
            for (j = i, k = j - 1; j > 0 && elem[k].compareTo(v) > 0; j--, k--) {
                elem[j] = elem[k];
            }
            elem[j] = v;
        }
    }

    private int partition(int left, int right) {
        int mid = (left + right) / 2;
        if (elem[left].compareTo(elem[mid]) > 0) xfswap(elem, left, mid);
        if (elem[left].compareTo(elem[right]) > 0) xfswap(elem, left, right);
        if (elem[mid].compareTo(elem[right]) > 0) xfswap(elem, mid, right);
        int j = right - 1;
        xfswap(elem, mid, j);
        int i = left;
        String v = elem[j];
        do {
            do {
                i++;
            } while (elem[i].compareTo(v) < 0);
            do {
                j--;
            } while (elem[j].compareTo(v) > 0);
            xfswap(elem, i, j);
        } while (i < j);
        xfswap(elem, j, i);
        xfswap(elem, i, right - 1);
        return i;
    }

    private void iqsort(int left, int right) {
        while (right - left > CUTOFF) {
            int i = partition(left, right);
            if (i - left > right - i) {
                iqsort(i + 1, right);
                right = i - 1;
            } else {
                iqsort(left, i - 1);
                left = i + 1;
            }
        }
    }

    /** MODIFIES StringSet to be in ascending order 
*/
    public void sortAscending() {
        if (nelem <= 0) return;
        iqsort(0, nelem - 1);
        inssort(0, nelem - 1);
    }

    /** MODIFIES StringSet to be in descending order 
*/
    public void sortDescending() {
        sortAscending();
        reverse();
    }

    /** MODIFIES StringSet to be in reverse order
*/
    public void reverse() {
        int i = 0, j = nelem - 1;
        while (i < j) {
            xfswap(elem, i, j);
            i++;
            j--;
        }
    }

    /** sort elem into ascending order via indexes; elem NOT MODIFIED
*/
    static final int XXCUTOFF = 16;

    public IntSet indexSortAscending() {
        if (nelem <= 0) {
            return new IntSet();
        }
        IntSet idx = new IntSet(nelem);
        IntArray ielem = new IntArray(idx.getElements());
        xxiqsort(ielem, elem, 0, nelem - 1);
        xxinssort(ielem, elem, 0, nelem - 1);
        try {
            idx = new IntSet(ielem.getArray());
        } catch (Exception e) {
            Util.bug(e);
        }
        return idx;
    }

    /** sort elem into descending order via indexes; elem NOT MODIFIED
*/
    public IntSet indexSortDescending() {
        IntSet idx;
        idx = indexSortAscending();
        int[] temp = new IntArray(idx.getElements()).getReverseArray();
        try {
            idx = new IntSet(temp);
        } catch (Exception e) {
            Util.bug(e);
        }
        return idx;
    }

    private void xxinssort(IntArray iarr, String[] pfl, int left, int right) {
        int j, k;
        for (int i = left + 1; i <= right; i++) {
            int v = iarr.elementAt(i);
            for (j = i, k = j - 1; j > 0 && pfl[iarr.elementAt(k)].compareTo(pfl[v]) > 0; j--, k--) {
                iarr.setElementAt(j, iarr.elementAt(k));
            }
            iarr.setElementAt(j, v);
        }
    }

    private int xxpartition(IntArray iarr, String[] pfl, int left, int right) {
        int mid = (left + right) / 2;
        if (pfl[iarr.elementAt(left)].compareTo(pfl[iarr.elementAt(mid)]) > 0) xxfswap(iarr, left, mid);
        if (pfl[iarr.elementAt(left)].compareTo(pfl[iarr.elementAt(right)]) > 0) xxfswap(iarr, left, right);
        if (pfl[iarr.elementAt(mid)].compareTo(pfl[iarr.elementAt(right)]) > 0) xxfswap(iarr, mid, right);
        int j = right - 1;
        xxfswap(iarr, mid, j);
        int i = left;
        String v = pfl[iarr.elementAt(j)];
        do {
            do {
                i++;
            } while (pfl[iarr.elementAt(i)].compareTo(v) < 0);
            do {
                j--;
            } while (pfl[iarr.elementAt(j)].compareTo(v) > 0);
            xxfswap(iarr, i, j);
        } while (i < j);
        xxfswap(iarr, j, i);
        xxfswap(iarr, i, right - 1);
        return i;
    }

    private void xxiqsort(IntArray iarr, String[] pfl, int left, int right) {
        while (right - left > XXCUTOFF) {
            int i = xxpartition(iarr, pfl, left, right);
            if (i - left > right - i) {
                xxiqsort(iarr, pfl, i + 1, right);
                right = i - 1;
            } else {
                xxiqsort(iarr, pfl, left, i - 1);
                left = i + 1;
            }
        }
    }

    private void xxfswap(IntArray iarr, int a, int b) {
        int t = iarr.elementAt(a);
        iarr.setElementAt(a, iarr.elementAt(b));
        iarr.setElementAt(b, t);
    }

    public static void main(String[] args) {
        System.out.println("First StringSet...");
        StringSet is = new StringSet();
        is.debug();
        System.out.print("adding 'one'");
        is.addElement("one");
        is.debug();
        System.out.print("adding 'seven'");
        is.addElement("seven");
        is.debug();
        System.out.print("adding 'three'");
        is.addElement("three");
        is.debug();
        System.out.print("adding 'seven'");
        is.addElement("seven");
        is.debug();
        System.out.print("adding 'Seven'");
        is.addElement("Seven");
        is.debug();
        System.out.println("index of 'three' " + is.indexOf("three"));
        System.out.println("is contains 'three' " + is.contains("three"));
        System.out.println("index of 'Three' " + is.indexOf("Three"));
        System.out.println("is contains 'Three' " + is.contains("Three"));
        System.out.println("Second StringSet...");
        StringSet it = new StringSet();
        System.out.print("adding 'two'");
        it.addElement("two");
        it.debug();
        System.out.print("adding 'one'");
        it.addElement("one");
        it.debug();
        System.out.print("adding 'Two'");
        it.addElement("Two");
        it.debug();
        System.out.print("adding 'five'");
        it.addElement("five");
        it.debug();
        System.out.println("Combining two sets...");
        try {
            is.addSet(it);
        } catch (Exception e) {
            System.out.println(e);
        }
        is.debug();
        System.out.println("Testing 'elementAt'");
        for (int i = 0; i < is.size(); i++) {
            System.out.print(" " + is.elementAt(i));
        }
        System.out.println("");
        System.out.println("Adding existing StringSet to null one");
        StringSet iw = new StringSet();
        try {
            iw.addSet(it);
        } catch (Exception e) {
            System.out.println(e);
        }
        iw.debug();
        System.out.println("Adding null StringSet to existing one");
        StringSet ix = new StringSet();
        try {
            it.addSet(ix);
        } catch (Exception e) {
            System.out.println(e);
        }
        it.debug();
        System.out.println("Case-Insensitive StringSet...");
        StringSet iy = new StringSet(StringSet.IGNORE_CASE);
        System.out.print("adding 'two'");
        iy.addElement("two");
        iy.debug();
        System.out.print("adding 'one'");
        iy.addElement("one");
        iy.debug();
        System.out.print("adding 'Two'");
        iy.addElement("Two");
        iy.debug();
        System.out.print("adding 'five'");
        iy.addElement("five");
        iy.debug();
        System.out.println("iy contains 'two' " + iy.contains("two"));
        System.out.println("iy contains 'Two' " + iy.contains("Two"));
    }
}
