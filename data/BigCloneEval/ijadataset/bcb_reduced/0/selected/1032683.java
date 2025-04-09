package jumbo.euclid;

import jumbo.xml.util.Util;

/**
<P>
 ObjectArray - array of Objects.  
<P>
 ObjectArray represents a 1-dimensional vector/array of Objects, 
 and is used in SpreadSheets, etc. The class is designed with the 
 idea that all Objects are of the same type, but this is not enforced,
 and you may wish to experiment. 
<P>
 There is provision for 2 homegenous arrays, INT and REAL , for 
 compatibility with RealArray and IntArray
 In some cases the type of the Object (e.g. integer) might lead to
 additional features. 
<P>
It can be sorted if all Objects are of type Sortable.
<P>
Hacked from RealArray - it shares a number of functions.
@author (C) P. Murray-Rust, 1996
*/
public class ObjectArray extends Status {

    /** maximum number of elements (for bound checking) - resettable */
    private int maxelem = 10000;

    /** actual number of elements */
    int nelem;

    /** the array of Objects */
    Object[] array;

    /** is the array homogeneous?  (null elements are allowed) */
    boolean homogeneous = false;

    public static final int OBJECT = 0;

    public static final int INT = 1;

    public static final int REAL = 2;

    int objectType = OBJECT;

    int bufsize = 5;

    /** default is an array of zero points 
*/
    public ObjectArray() {
        nelem = 0;
        bufsize = 5;
        array = new Object[bufsize];
    }

    private boolean checkSize(int n) {
        if (n < 0) {
            n = 0;
            return false;
        } else {
            nelem = n;
            if (nelem > maxelem) maxelem = nelem;
            if (bufsize < nelem) bufsize = nelem;
            return true;
        }
    }

    private void makeSpace(int newCount) {
        if (newCount > bufsize) {
            while (newCount > bufsize) {
                bufsize *= 2;
            }
            Object[] array1 = new Object[bufsize];
            System.arraycopy(array, 0, array1, 0, nelem);
            array = array1;
        }
    }

    /** creates n-element array initialised to null
*/
    public ObjectArray(int n) {
        this(n, null);
    }

    /** set all elements of the array to a given Object
*/
    public ObjectArray(int n, Object elem1) {
        if (!checkSize(n)) return;
        array = new Object[n];
        for (int i = 0; i < n; i++) {
            array[i] = elem1;
        }
    }

    /** convert an IntArray to a ObjectArray
*/
    public ObjectArray(IntArray ia) {
        if (!checkSize(ia.size())) return;
        objectType = INT;
        array = new Integer[nelem];
        bufsize = nelem;
        for (int i = 0; i < nelem; i++) {
            array[i] = new Integer(ia.elementAt(i));
        }
    }

    /** convert an RealArray to a ObjectArray
*/
    public ObjectArray(RealArray ra) {
        if (!checkSize(ra.size())) return;
        objectType = REAL;
        array = new Double[nelem];
        bufsize = nelem;
        for (int i = 0; i < nelem; i++) {
            array[i] = new Double(ra.elementAt(i));
        }
    }

    /** get the Class of the objects, if this is common to all, else null.
some of the elements can be null (i.e. 'missing') 

*/
    public Class getObjectClass() {
        homogeneous = true;
        Class c = null;
        if (size() == 0) return null;
        for (int i = 0; i < size(); i++) {
            if (array[i] == null) continue;
            Class cc = array[i].getClass();
            if (cc == null) continue;
            if (c == null) {
                c = cc;
            } else if (!cc.equals(c)) {
                homogeneous = false;
                return null;
            }
        }
        return c;
    }

    public boolean isHomogeneous() {
        getObjectClass();
        return homogeneous;
    }

    /** get the type of the Objects as INT, etc
*/
    public int getObjectType() {
        return objectType;
    }

    /** subarray of another array - inclusive; if low > high or other silly 
  indices, creates default array */
    public ObjectArray(ObjectArray m, int low, int high) {
        nelem = high - low + 1;
        if (low < 0 || low > high || high >= m.size()) return;
        if (!checkSize(nelem)) return;
        array = new Object[nelem];
        bufsize = nelem;
        System.arraycopy(m.array, low, array, 0, nelem);
    }

    /** copy constructor 
*/
    public ObjectArray(ObjectArray m) {
        this.shallowCopy(m);
        System.arraycopy(m.array, 0, array, 0, nelem);
    }

    /** shallowCopy 
*/
    public void shallowCopy(ObjectArray m) {
        nelem = m.nelem;
        bufsize = m.bufsize;
        maxelem = m.maxelem;
        array = m.array;
    }

    /** extracts a given element from the array
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public Object elementAt(int elem) throws ArrayIndexOutOfBoundsException {
        return array[elem];
    }

    /** get actual number of elements
*/
    public int size() {
        return nelem;
    }

    /** return the array as a Object[]; this has to resize the array to the 
 precise length used , or confusion will result! Note that this gives the 
 user access to
 the actual array, so that they can alter its contents.  This should be used 
 with care, but Java should stop any access outside the buffer limits. */
    public Object[] getArray() {
        if (nelem != array.length) {
            Object[] temp = new Object[nelem];
            System.arraycopy(array, 0, temp, 0, nelem);
            array = temp;
        }
        return array;
    }

    /** return contents as a RealArray if REAL , else null
*/
    public RealArray getRealArray() {
        if (objectType == REAL) {
            RealArray ra = new RealArray();
            for (int i = 0; i < size(); i++) {
                ra.addElement(((Double) array[i]).doubleValue());
            }
            return ra;
        }
        return null;
    }

    /** return contents as a IntArray if INT, else null
*/
    public IntArray getIntArray() {
        if (objectType == INT) {
            IntArray ia = new IntArray();
            for (int i = 0; i < size(); i++) {
                ia.addElement(((Integer) array[i]).intValue());
            }
            return ia;
        }
        return null;
    }

    /** clear all elements of array to null (or 0 or 0.0)
*/
    public void clearArray() {
        if (objectType == OBJECT) {
            for (int i = 0; i < size(); i++) {
                array[i] = null;
            }
        } else if (objectType == INT) {
            for (int i = 0; i < size(); i++) {
                array[i] = new Integer(0);
            }
        } else if (objectType == REAL) {
            for (int i = 0; i < size(); i++) {
                array[i] = new Double(0.0);
            }
        }
    }

    /** return the elements in reverse order as Object[]
*/
    public Object[] getReverseArray() {
        int count = size();
        Object[] temp = new Object[count];
        for (int i = 0; i < size(); i++) {
            temp[i] = this.array[--count];
        }
        return temp;
    }

    /** reset the maximum index (for when poking elements) (no other effect) 
*/
    public void setMaxIndex(int max) {
        maxelem = max;
    }

    private void checkConformable(ObjectArray m) throws UnequalArraysException {
        if (nelem != m.nelem) {
            throw new UnequalArraysException();
        }
    }

    /** are two arrays equal in all elements? 
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public boolean equals(ObjectArray f) throws UnequalArraysException {
        checkConformable(f);
        for (int i = 0; i < nelem; i++) {
            if (array[i].equals(f.array[i])) {
                return false;
            }
        }
        return true;
    }

    /** set a given element into the array; must be less than current max index
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public void setElementAt(int elem, Object f) throws ArrayIndexOutOfBoundsException {
        array[elem] = f;
    }

    /** RHS: get a subObjectArray from element start to end
*/
    public ObjectArray getSubArray(int start, int end) {
        int nel = end - start + 1;
        ObjectArray f = new ObjectArray(nel, null);
        System.arraycopy(array, start, f.array, 0, nel);
        return f;
    }

    /** copy a smaller array into the array statrting at start 
*/
    public void setElements(int start, Object[] a) {
        System.arraycopy(a, 0, this.array, start, a.length);
    }

    /** delete element and close up; if outside range, take no action
*/
    public void deleteElement(int elem) {
        if (elem < 0 || elem >= nelem) return;
        nelem--;
        if (bufsize > nelem * 2) {
            bufsize /= 2;
        }
        Object[] temp = new Object[bufsize];
        System.arraycopy(array, 0, temp, 0, elem);
        System.arraycopy(array, elem + 1, temp, elem, nelem - elem);
        array = temp;
    }

    /** delete elements and close up; if outside range take no action 
*/
    public void deleteElements(int low, int high) {
        if (low < 1 || low > high || high > nelem) return;
        nelem -= (high - low + 1);
        if (bufsize > nelem * 2) {
            bufsize /= 2;
        }
        Object[] temp = new Object[bufsize];
        System.arraycopy(array, 0, temp, 0, low);
        System.arraycopy(array, high + 1, temp, low, nelem - low);
    }

    /** insert element and expand; if outside range, take no action
*/
    public void insertElementAt(int elem, Object f) {
        if (elem < 0 || elem > nelem) return;
        nelem++;
        makeSpace(nelem);
        Object[] array1 = new Object[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        array1[elem] = f;
        System.arraycopy(array, elem, array1, elem + 1, nelem - elem);
        array = array1;
    }

    /** insert a ObjectArray at position elem and expand
*/
    public void insertArray(int elem, ObjectArray f) {
        int n = f.size();
        if (elem < 0 || elem >= nelem || n < 1) return;
        nelem += n;
        makeSpace(nelem);
        Object[] array1 = new Object[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        System.arraycopy(f.getArray(), 0, array1, elem, n);
        System.arraycopy(array, elem, array1, n + elem, nelem - elem - n);
    }

    /** append element
*/
    public void addElement(Object f) {
        makeSpace(nelem + 1);
        array[nelem++] = f;
    }

    /** append elements
*/
    public void addArray(ObjectArray f) {
        makeSpace(nelem + f.nelem);
        System.arraycopy(f.array, 0, array, nelem, f.nelem);
        nelem += f.nelem;
    }

    /** reorder by index in IntSet; does NOT modify array
@exception BadSubscriptException an element of idx is outside range of <TT>this</TT>
*/
    public ObjectArray getReorderedArray(IntSet idx) throws BadSubscriptException {
        ObjectArray temp = new ObjectArray(nelem);
        for (int i = 0; i < nelem; i++) {
            int index = idx.elementAt(i);
            if (index > nelem) {
                throw new BadSubscriptException();
            }
            temp.array[i] = array[index];
        }
        return temp;
    }

    /** returns values as strings 
*/
    public String[] getStringValues() {
        String[] temp = new String[nelem];
        for (int i = 0; i < nelem; i++) {
            temp[i] = array[i].toString();
        }
        return temp;
    }

    /** concatenates values with spaces
*/
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < nelem; i++) {
            s.append(' ');
            s.append(array[i]);
        }
        return new String(s);
    }

    /** copy a Object[] into a new one 
*/
    public static Object[] copy(Object[] f) {
        Object temp[] = new Object[f.length];
        System.arraycopy(f, 0, temp, 0, f.length);
        return temp;
    }

    /**
    quick sort - modified from p96 - 97 (Hansen - C++ answer book)
    
    Scalar sort refers to sorting ObjectArray (and similar classes)
    where the objects themeselves are sorted.
    
    Index sort refers to sorting indexes (held as IntSet's) to the object
    and getting the sorted object(s) with reorderBy(IntSet idx);
    
*/
    private void xfswap(Object[] s, int a, int b) {
        Sortable tmp = (Sortable) s[a];
        s[a] = s[b];
        s[b] = tmp;
    }

    static final int CUTOFF = 16;

    private void inssort(int left, int right) {
        int k;
        for (int i = left + 1; i <= right; i++) {
            Sortable v = (Sortable) array[i];
            int j;
            for (j = i, k = j - 1; j > 0 && ((Sortable) array[k]).compareTo(v) > 0; j--, k--) {
                array[j] = array[k];
            }
            array[j] = v;
        }
    }

    private int partition(int left, int right) {
        int mid = (left + right) / 2;
        if (((Sortable) array[left]).compareTo((Sortable) array[mid]) > 0) xfswap(array, left, mid);
        if (((Sortable) array[left]).compareTo((Sortable) array[right]) > 0) xfswap(array, left, right);
        if (((Sortable) array[mid]).compareTo((Sortable) array[right]) > 0) xfswap(array, mid, right);
        int j = right - 1;
        xfswap(array, mid, j);
        int i = left;
        Sortable v = (Sortable) array[j];
        do {
            do {
                i++;
            } while (((Sortable) array[i]).compareTo(v) < 0);
            do {
                j--;
            } while (((Sortable) array[j]).compareTo(v) > 0);
            xfswap(array, i, j);
        } while (i < j);
        xfswap(array, j, i);
        xfswap(array, i, right - 1);
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

    /** MODIFIES ObjectArray to be in ascending order 
*/
    public void sortAscending() {
        if (nelem <= 0) return;
        iqsort(0, nelem - 1);
        inssort(0, nelem - 1);
    }

    /** MODIFIES ObjectArray to be in descending order 
*/
    public void sortDescending() {
        sortAscending();
        reverse();
    }

    /** MODIFIES ObjectArray to be in reverse order
*/
    public void reverse() {
        int i = 0, j = nelem - 1;
        while (i < j) {
            xfswap(array, i, j);
            i++;
            j--;
        }
    }

    /** sort array into ascending order via indexes; array NOT MODIFIED
*/
    static final int XXCUTOFF = 16;

    public IntSet indexSortAscending() {
        if (nelem <= 0) {
            return new IntSet();
        }
        IntSet idx = new IntSet(nelem);
        IntArray ielem = new IntArray(idx.getElements());
        xxiqsort(ielem, array, 0, nelem - 1);
        xxinssort(ielem, array, 0, nelem - 1);
        try {
            idx = new IntSet(ielem.getArray());
        } catch (Exception e) {
            Util.bug(e);
        }
        return idx;
    }

    /** sort array into descending order via indexes; array NOT MODIFIED
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

    private void xxinssort(IntArray iarr, Object[] pfl, int left, int right) {
        int j, k;
        for (int i = left + 1; i <= right; i++) {
            int v = iarr.elementAt(i);
            for (j = i, k = j - 1; j > 0 && ((Sortable) pfl[iarr.elementAt(k)]).compareTo((Sortable) pfl[v]) > 0; j--, k--) {
                iarr.setElementAt(j, iarr.elementAt(k));
            }
            iarr.setElementAt(j, v);
        }
    }

    private int xxpartition(IntArray iarr, Object[] pfl, int left, int right) {
        int mid = (left + right) / 2;
        if (((Sortable) pfl[iarr.elementAt(left)]).compareTo((Sortable) pfl[iarr.elementAt(mid)]) > 0) xxfswap(iarr, left, mid);
        if (((Sortable) pfl[iarr.elementAt(left)]).compareTo((Sortable) pfl[iarr.elementAt(right)]) > 0) xxfswap(iarr, left, right);
        if (((Sortable) pfl[iarr.elementAt(mid)]).compareTo((Sortable) pfl[iarr.elementAt(right)]) > 0) xxfswap(iarr, mid, right);
        int j = right - 1;
        xxfswap(iarr, mid, j);
        int i = left;
        Sortable v = (Sortable) pfl[iarr.elementAt(j)];
        do {
            do {
                i++;
            } while (((Sortable) pfl[iarr.elementAt(i)]).compareTo(v) < 0);
            do {
                j--;
            } while (((Sortable) pfl[iarr.elementAt(j)]).compareTo(v) > 0);
            xxfswap(iarr, i, j);
        } while (i < j);
        xxfswap(iarr, j, i);
        xxfswap(iarr, i, right - 1);
        return i;
    }

    private void xxiqsort(IntArray iarr, Object[] pfl, int left, int right) {
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
        ObjectArray oa = new ObjectArray();
        oa.addElement(new Foo("twelve"));
        oa.addElement(new Foo("four"));
        oa.addElement(new Foo("seven"));
        oa.addElement(new Foo("one"));
        oa.addElement(new Foo("thirteen"));
        oa.addElement(new Foo("twenty"));
        for (int i = 0; i < oa.size(); i++) {
            System.out.println("" + ((Foo) oa.elementAt(i)));
        }
        oa.sortAscending();
        for (int i = 0; i < oa.size(); i++) {
            System.out.println("" + ((Foo) oa.elementAt(i)));
        }
    }
}

class Foo implements Sortable {

    String s;

    public Foo(String s) {
        this.s = s;
    }

    public int compareTo(Sortable ss) {
        if (!(ss instanceof Foo)) {
            System.out.println("ClassCast error");
        }
        Foo f = (Foo) ss;
        if (s.length() > f.s.length()) return 1;
        if (s.length() < f.s.length()) return -1;
        return 0;
    }

    public String toString() {
        return s + s.length();
    }
}
