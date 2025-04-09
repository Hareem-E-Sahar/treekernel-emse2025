package jumbo.euclid;

import jumbo.xml.util.Util;

/**
<P>
 IntArray - array of ints
<P>
 IntArray represents a 1-dimensional vector/array of ints and
 is basically a wrapper for int[] in Java
 There are a lot of useful member functions (sorting, ranges, parallel
 operations, etc. - have a look)
<P>
 The default is an array with zero points.  All arrays are valid objects.
<P>
 Attempting to create an array with < 0 points creates a default array 
 (zero points).
<P>
 Since int[] knows its length (unlike C), there are many cases where
 int[] can be safely used.  However it is not a first-class object
 and IntArray supplies this feature. int[] is referenceable
 through getArray().
@author (C) P. Murray-Rust, 1996
*/
public class IntArray extends Status {

    /** maximum number of elements (for bound checking) - resettable
*/
    private int maxelem = 10000;

    /** actual number of elements
*/
    int nelem;

    /** the array of ints
*/
    int[] array;

    int bufsize = 5;

    /** default is an array of zero points 
*/
    public IntArray() {
        nelem = 0;
        bufsize = 5;
        array = new int[bufsize];
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
            int[] array1 = new int[bufsize];
            System.arraycopy(array, 0, array1, 0, nelem);
            array = array1;
        }
    }

    /** creates n-element array initialised to 0 
*/
    public IntArray(int n) {
        this(n, 0);
    }

    /** This gives a nelem-element array initialised to elem1+(i-1)*delta
*/
    public IntArray(int n, int elem1, int delta) {
        if (!checkSize(n)) return;
        array = new int[n];
        bufsize = n;
        int ff = elem1;
        for (int i = 0; i < n; i++) {
            array[i] = ff;
            ff += delta;
        }
    }

    /** set all elements of the array to a given value 
*/
    public IntArray(int n, int elem1) {
        if (!checkSize(n)) return;
        array = new int[n];
        bufsize = n;
        for (int i = 0; i < n; i++) {
            array[i] = elem1;
        }
    }

    /** Formed by feeding in an existing array; requires the size.  
(You can use the *.length of the array if necessary) */
    public IntArray(int n, int[] arr) {
        if (!checkSize(n)) return;
        array = new int[n];
        bufsize = n;
        System.arraycopy(arr, 0, array, 0, n);
    }

    /** from an existing int[] (which knows its length) 
*/
    public IntArray(int[] arr) {
        this(arr.length, arr);
    }

    /** subarray of another array - inclusive; if low > high or other silly 
  indices, creates default array */
    public IntArray(IntArray m, int low, int high) {
        nelem = high - low + 1;
        if (low < 0 || low > high || high >= m.size()) return;
        if (!checkSize(nelem)) return;
        array = new int[nelem];
        bufsize = nelem;
        System.arraycopy(m.array, low, array, 0, nelem);
    }

    /** use another IntArray to subscript this one; i.e. 
I(this) = I(ref) subscripted by I(sub); Result has dimension
of I(sub). If any of I(sub) lies outside 0...refmax-1, throw
an error
*/
    public IntArray(IntArray ref, IntArray sub) throws IllegalArgumentException {
        this(sub.size());
        for (int i = 0; i < sub.size(); i++) {
            int j = sub.elementAt(i);
            if (j < 0 || j >= ref.size()) {
                throw new IllegalArgumentException();
            }
            this.setElementAt(i, ref.elementAt(j));
        }
    }

    /** clones another IntArray 
*/
    public Object clone() {
        IntArray temp = new IntArray(nelem);
        temp.nelem = nelem;
        temp.maxelem = maxelem;
        System.arraycopy(array, 0, temp.array, 0, nelem);
        temp.bufsize = nelem;
        return (Object) temp;
    }

    /** copy constructor 
*/
    public IntArray(IntArray m) {
        this.shallowCopy(m);
        System.arraycopy(m.array, 0, array, 0, nelem);
    }

    /** Create a given 'shape' of array for data filtering.  An intended use
 is with IntArray.arrayFilter().  
 The shapes (before scaling by maxval) are:
<UL>
<LI>"TRIANGLE"; 1/nn, 2/nn, ... 1 ... 2/nn, 1/nn; nelem is set to 2*nn - 1
<LI>"ZIGZAG"; 1/nn, 2/nn, ... 1 ... 1/nn, 0, -1/nn, -2/nn, -1, ... -1/nn,; 
  nelem is set to 4*nn - 1
</UL>
*/
    public IntArray(int nn, String shape, int maxval) {
        if (shape.toUpperCase().equals("TRIANGLE")) {
            nelem = nn * 2 - 1;
            if (!checkSize(nelem)) return;
            array = new int[nelem];
            int delta = maxval / ((int) nn);
            for (int i = 0; i < nn; i++) {
                array[i] = (i + 1) * delta;
                array[nelem - i - 1] = array[i];
            }
        } else if (shape.toUpperCase().equals("ZIGZAG")) {
            nelem = nn * 4 - 1;
            if (!checkSize(nelem)) return;
            array = new int[nelem];
            int delta = maxval / ((int) nn);
            for (int i = 0; i < nn; i++) {
                array[i] = (i + 1) * delta;
                array[2 * nn - i - 2] = array[i];
                array[2 * nn + i] = -array[i];
                array[nelem - i - 1] = -array[i];
            }
            array[2 * nn - 1] = 0;
        }
    }

    /** from an array of Strings (which must represent Ints)
@exception NumberFormatException a string could not be interpreted as Int
*/
    public IntArray(String[] strings) throws NumberFormatException {
        this(strings.length);
        for (int i = 0; i < strings.length; i++) {
            array[i] = (Integer.valueOf(strings[i])).intValue();
        }
    }

    /** from a String with space-separated strings representing Ints
@exception NumberFormatException a string could not be interpreted as Int
*/
    public IntArray(String string) throws NumberFormatException {
        this(Util.split(string));
    }

    /** shallowCopy 
*/
    public void shallowCopy(IntArray m) {
        nelem = m.nelem;
        bufsize = m.bufsize;
        maxelem = m.maxelem;
        array = m.array;
    }

    /** extracts a given element from the array
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public int elementAt(int elem) throws ArrayIndexOutOfBoundsException {
        return array[elem];
    }

    /** get actual number of elements
*/
    public int size() {
        return nelem;
    }

    /** return the array as a int[]; this has to resize the array to the precise
 length used , or confusion will result! Note that this gives the user access to
 the actual array, so that they can alter its contents.  This should be used 
 with care, but Java should stop any access outside the buffer limits. */
    public int[] getArray() {
        if (nelem != array.length) {
            int[] temp = new int[nelem];
            System.arraycopy(array, 0, temp, 0, nelem);
            array = temp;
        }
        return array;
    }

    /** clear all elements of array 
*/
    public void clearArray() {
        for (int i = 0; i < size(); i++) {
            array[i] = 0;
        }
    }

    /** return the elements in reverse order as int[]
*/
    public int[] getReverseArray() {
        int count = size();
        int[] temp = new int[count];
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

    private void checkConformable(IntArray m) throws UnequalArraysException {
        if (nelem != m.nelem) {
            throw new UnequalArraysException();
        }
    }

    /** are two arrays equal in all elements? (use epsilon as tolerance)
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public boolean equals(IntArray f) throws UnequalArraysException {
        checkConformable(f);
        for (int i = 0; i < nelem; i++) {
            if (array[i] != f.array[i]) {
                return false;
            }
        }
        return true;
    }

    /** array addition - adds conformable arrays
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public IntArray plus(IntArray f) throws UnequalArraysException {
        checkConformable(f);
        IntArray m = (IntArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] = f.array[i] + array[i];
        }
        return m;
    }

    /** array subtraction - subtracts conformable arrays
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public IntArray subtract(IntArray f) throws UnequalArraysException {
        checkConformable(f);
        IntArray m = (IntArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] = f.array[i] - array[i];
        }
        return m;
    }

    /** change the sign of all elements; MODIFIES this 
*/
    public void negative() {
        for (int i = 0; i < size(); i++) {
            array[i] = -array[i];
        }
    }

    /** array multiplication by a scalar; does NOT modify 'this'
*/
    public IntArray multiplyBy(int f) {
        IntArray m = (IntArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] *= f;
        }
        return m;
    }

    /** set a given element into the array; must be less than current max index
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public void setElementAt(int elem, int f) throws ArrayIndexOutOfBoundsException {
        array[elem] = f;
    }

    /** RHS: get a subIntArray from element start to end
*/
    public IntArray getSubArray(int start, int end) {
        int nel = end - start + 1;
        IntArray f = new IntArray(nel, 0);
        System.arraycopy(array, start, f.array, 0, nel);
        return f;
    }

    /** copy a smaller array into the array statrting at start 
*/
    public void setElements(int start, int[] a) {
        System.arraycopy(a, 0, this.array, start, a.length);
    }

    /** is the array filled with zeros?
*/
    public boolean isClear() {
        for (int i = 0; i < nelem; i++) {
            if (array[i] == 0) return false;
        }
        return true;
    }

    /** initialise array to given int[] 
*/
    public void setAllElements(int f) {
        Int.initArray(nelem, array, f);
    }

    /** sum all elements
*/
    public int sumAllElements() {
        int sum = 0;
        for (int i = 0; i < nelem; i++) {
            sum += array[i];
        }
        return sum;
    }

    /** absolute sum of all elements
*/
    public int absSumAllElements() {
        int sum = 0;
        for (int i = 0; i < nelem; i++) {
            sum += Math.abs(array[i]);
        }
        return sum;
    }

    /** inner product - same as dotProduct
*/
    public int innerProduct() {
        int result = Integer.MIN_VALUE;
        try {
            result = this.dotProduct(this);
        } catch (UnequalArraysException x) {
        }
        return result;
    }

    /** dot product of two IntArrays - if of same length - else zero
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public int dotProduct(IntArray f) throws UnequalArraysException {
        checkConformable(f);
        int sum = 0;
        for (int i = 0; i < nelem; i++) {
            sum += array[i] * f.array[i];
        }
        return sum;
    }

    /** cumulative sum of array (new RA contains
<BR>
elem[i] = sum(k = 0 to i) f[k]
<P>
does not modify 'this'
*/
    public IntArray cumulativeSum() {
        IntArray temp = new IntArray(nelem);
        int sum = 0;
        for (int i = 0; i < nelem; i++) {
            sum += array[i];
            temp.array[i] = sum;
        }
        return temp;
    }

    /** apply filter (i.e. convolute RA with another RA).  This is 1-D
 image processing.  If <TT>filter</TT> has <= 1 element, return <TT>this</TT>
 unchanged.  <TT>filter</TT> should have an odd number of elements.  
<P>
 The filter can be created with a IntArray constructor
*/
    public IntArray applyFilter(IntArray filter) {
        if (nelem == 0 || filter == null || filter.nelem <= 1) {
            return this;
        }
        int nfilter = filter.size();
        int midfilter = (nfilter - 1) / 2;
        IntArray temp = new IntArray(nelem);
        int wt = 0;
        int sum = 0;
        for (int j = 0; j < midfilter; j++) {
            wt = 0;
            sum = 0;
            int l = 0;
            for (int k = midfilter - j; k < nfilter; k++) {
                wt += Math.abs(filter.array[k]);
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        wt = filter.absSumAllElements();
        for (int j = midfilter; j < nelem - midfilter; j++) {
            sum = 0;
            int l = j - midfilter;
            for (int k = 0; k < nfilter; k++) {
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        for (int j = nelem - midfilter; j < nelem; j++) {
            wt = 0;
            sum = 0;
            int l = j - midfilter;
            for (int k = 0; k < midfilter + nelem - j; k++) {
                wt += Math.abs(filter.array[k]);
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        return temp;
    }

    /** index of largest element; returns -1 if zero element array
*/
    public int indexOfLargestElement() {
        int index = -1;
        int value = Integer.MIN_VALUE;
        for (int i = 0; i < nelem; i++) {
            if (array[i] > value) {
                value = array[i];
                index = i;
            }
        }
        return index;
    }

    /** index of smallest element
*/
    public int indexOfSmallestElement() {
        int index = -1;
        int value = Integer.MAX_VALUE;
        for (int i = 0; i < nelem; i++) {
            if (array[i] < value) {
                value = array[i];
                index = i;
            }
        }
        return index;
    }

    /** value of largest element
*/
    public int largestElement() {
        return array[indexOfLargestElement()];
    }

    /** value of smallest element
*/
    public int smallestElement() {
        return array[indexOfSmallestElement()];
    }

    /** range of array (default IntRange for zero array)
*/
    public IntRange range() {
        IntRange r = new IntRange();
        for (int i = 0; i < nelem; i++) {
            r.add(array[i]);
        }
        return r;
    }

    /** delete element and close up; if outside range, take no action
*/
    public void deleteElement(int elem) {
        if (elem < 0 || elem >= nelem) return;
        nelem--;
        if (bufsize > nelem * 2) {
            bufsize /= 2;
        }
        int[] temp = new int[bufsize];
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
        int[] temp = new int[bufsize];
        System.arraycopy(array, 0, temp, 0, low);
        System.arraycopy(array, high + 1, temp, low, nelem - low);
    }

    /** insert element and expand; if outside range, take no action
*/
    public void insertElementAt(int elem, int f) {
        if (elem < 0 || elem > nelem) return;
        nelem++;
        makeSpace(nelem);
        int[] array1 = new int[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        array1[elem] = f;
        System.arraycopy(array, elem, array1, elem + 1, nelem - elem);
        array = array1;
    }

    /** insert a IntArray at position elem and expand
*/
    public void insertArray(int elem, IntArray f) {
        int n = f.size();
        if (elem < 0 || elem >= nelem || n < 1) return;
        nelem += n;
        makeSpace(nelem);
        int[] array1 = new int[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        System.arraycopy(f.getArray(), 0, array1, elem, n);
        System.arraycopy(array, elem, array1, n + elem, nelem - elem - n);
    }

    /** append element
*/
    public void addElement(int f) {
        makeSpace(nelem + 1);
        array[nelem++] = f;
    }

    /** append elements
*/
    public void addArray(IntArray f) {
        makeSpace(nelem + f.nelem);
        System.arraycopy(f.array, 0, array, nelem, f.nelem);
        nelem += f.nelem;
    }

    /** reorder by index in IntSet; does NOT modify array
@exception BadSubscriptException an element of idx is outside range of <TT>this</TT>
*/
    public IntArray getReorderedArray(IntSet idx) throws BadSubscriptException {
        IntArray temp = new IntArray(nelem);
        for (int i = 0; i < nelem; i++) {
            int index = idx.elementAt(i);
            if (index > nelem) {
                throw new BadSubscriptException();
            }
            temp.array[i] = array[index];
        }
        return temp;
    }

    /** return index of elements within a given range
*/
    public IntSet inRange(IntRange r) {
        int n = size();
        IntSet temp = new IntSet();
        for (int i = 0; i < n; i++) {
            if (r.isValid() && r.includes(array[i])) {
                temp.addElement(i);
            }
        }
        return temp;
    }

    /** return index of elements outside a given range
*/
    public IntSet outOfRange(IntRange r) {
        int n = size();
        IntSet temp = new IntSet();
        for (int i = 0; i < n; i++) {
            if (r.isValid() && !r.includes(array[i])) {
                temp.addElement(i);
            }
        }
        return temp;
    }

    /** returns values as strings 
*/
    public String[] getStringValues() {
        String[] temp = new String[nelem];
        for (int i = 0; i < nelem; i++) {
            temp[i] = Integer.toString(array[i]);
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

    /** delete elements (lo - > hi inclusive) in a float[] and close up; 
  if hi >= float.length hi is reset to float.length-1.  */
    public static int[] deleteElements(int[] f, int low, int hi) {
        if (hi >= f.length) hi = f.length - 1;
        if (low < 0) low = 0;
        int ndel = hi - low + 1;
        if (ndel <= 0) return f;
        int[] temp = new int[f.length - ndel];
        System.arraycopy(f, 0, temp, 0, low);
        System.arraycopy(f, hi + 1, temp, low, f.length - hi - 1);
        return temp;
    }

    /** copy a int[] into a new one 
*/
    public static int[] copy(int[] f) {
        int temp[] = new int[f.length];
        System.arraycopy(f, 0, temp, 0, f.length);
        return temp;
    }

    /**
    quick sort - modified from p96 - 97 (Hansen - C++ answer book)
    
    Scalar sort refers to sorting IntArray and IntArray (and similar classes)
    where the objects themeselves are sorted.
    
    Index sort refers to sorting indexes (held as IntSet's) to the object
    and getting the sorted object(s) with reorderBy(IntSet idx);
    
*/
    void xfswap(int[] x, int a, int b) {
        int tmp = x[a];
        x[a] = x[b];
        x[b] = tmp;
    }

    static final int CUTOFF = 16;

    private void inssort(int left, int right) {
        int k;
        for (int i = left + 1; i <= right; i++) {
            int v = array[i];
            int j;
            for (j = i, k = j - 1; j > 0 && array[k] > v; j--, k--) {
                array[j] = array[k];
            }
            array[j] = v;
        }
    }

    private int partition(int left, int right) {
        int mid = (left + right) / 2;
        if (array[left] > array[mid]) xfswap(array, left, mid);
        if (array[left] > array[right]) xfswap(array, left, right);
        if (array[mid] > array[right]) xfswap(array, mid, right);
        int j = right - 1;
        xfswap(array, mid, j);
        int i = left;
        int v = array[j];
        do {
            do {
                i++;
            } while (array[i] < v);
            do {
                j--;
            } while (array[j] > v);
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

    /** MODIFIES array  to be in ascending order 
*/
    public void sortAscending() {
        if (nelem <= 0) return;
        iqsort(0, nelem - 1);
        inssort(0, nelem - 1);
    }

    /** MODIFIES array  to be in ascending order 
*/
    public void sortDescending() {
        sortAscending();
        reverse();
    }

    /** MODIFIES array to be in reverse order
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
        IntArray iarray = new IntArray(idx.getElements());
        xxiqsort(iarray, array, 0, nelem - 1);
        xxinssort(iarray, array, 0, nelem - 1);
        try {
            idx = new IntSet(iarray.getArray());
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

    private void xxinssort(IntArray iarr, int[] pfl, int left, int right) {
        int j, k;
        for (int i = left + 1; i <= right; i++) {
            int v = iarr.elementAt(i);
            for (j = i, k = j - 1; j > 0 && pfl[iarr.elementAt(k)] > pfl[v]; j--, k--) {
                iarr.setElementAt(j, iarr.elementAt(k));
            }
            iarr.setElementAt(j, v);
        }
    }

    private int xxpartition(IntArray iarr, int[] pfl, int left, int right) {
        int mid = (left + right) / 2;
        if (pfl[iarr.elementAt(left)] > pfl[iarr.elementAt(mid)]) xxfswap(iarr, left, mid);
        if (pfl[iarr.elementAt(left)] > pfl[iarr.elementAt(right)]) xxfswap(iarr, left, right);
        if (pfl[iarr.elementAt(mid)] > pfl[iarr.elementAt(right)]) xxfswap(iarr, mid, right);
        int j = right - 1;
        xxfswap(iarr, mid, j);
        int i = left;
        int v = pfl[iarr.elementAt(j)];
        do {
            do {
                i++;
            } while (pfl[iarr.elementAt(i)] < v);
            do {
                j--;
            } while (pfl[iarr.elementAt(j)] > v);
            xxfswap(iarr, i, j);
        } while (i < j);
        xxfswap(iarr, j, i);
        xxfswap(iarr, i, right - 1);
        return i;
    }

    private void xxiqsort(IntArray iarr, int[] pfl, int left, int right) {
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

    public static void test() {
        System.out.println("--------------Testing IntArray--------------\n");
        IntArray m9 = new IntArray();
        System.out.println("................................................\n");
        int i, j;
        System.out.println("................................................\n");
        IntRange r1 = new IntRange();
        System.out.println("r1: " + r1 + "\n");
        System.out.println("................................................\n");
        IntRange r2 = new IntRange(1, 6);
        System.out.println("r2: " + r2 + "\n");
        System.out.println("................................................\n");
        int f = 3;
        boolean bb = r2.includes(f);
        System.out.println(" bb: " + bb + "\n");
        System.out.println("................................................\n");
        f = -3;
        bb = r2.includes(f);
        System.out.println(" bb: " + bb + "\n");
        System.out.println("................................................\n");
        IntRange r3 = new IntRange(5, 2);
        System.out.println("r3: " + r3 + "\n");
        System.out.println("................................................\n");
        IntArray m0 = new IntArray();
        System.out.println("m0:\n" + m0 + "\n");
        System.out.println("................................................\n");
        IntArray m1 = new IntArray(3);
        System.out.println("m1:\n" + m1 + "\n");
        System.out.println("................................................\n");
        m9 = m1;
        int[] mat;
        int[] temp;
        temp = mat = new int[4 * 5];
        int count = 0;
        for (i = 1; i <= 4; i++) for (j = 1; j <= 5; j++) temp[count++] = -10 * i + j;
        IntArray m3 = new IntArray(20, mat);
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        IntArray m4 = new IntArray(m3);
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        f = m4.elementAt(10);
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        IntArray m19 = new IntArray(m4, 6, 9);
        System.out.println("m19:\n" + m19 + "\n");
        System.out.println("................................................\n");
        m9 = m19;
        System.out.println("................................................\n");
        m4.setElementAt(5, 123);
        int nelem = m4.size();
        int[] fr;
        fr = m4.getArray();
        System.out.println("................................................\n");
        boolean b = false;
        try {
            b = (m3.equals(m4));
        } catch (UnequalArraysException e) {
            Util.bug(e);
        }
        System.out.println(" b: " + b + "\n");
        System.out.println("................................................\n");
        m3.negative();
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        m3 = m3.multiplyBy(-13);
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        IntArray m8 = null;
        try {
            m8 = m3.plus(m4);
            System.out.println("m8:\n" + m8 + "\n");
            System.out.println("................................................\n");
            m8 = m3.subtract(m4);
            System.out.println("m8:\n" + m8 + "\n");
            System.out.println("................................................\n");
            m3 = m3.plus(m4);
            System.out.println("m3:\n" + m3 + "\n");
            System.out.println("................................................\n");
            m3 = m3.subtract(m4);
        } catch (UnequalArraysException e) {
            Util.bug(e);
        }
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        int i1 = m3.indexOfSmallestElement();
        int i2 = m3.indexOfLargestElement();
        System.out.println("i1/i2: " + i1 + "/" + i2 + "\n");
        System.out.println("................................................\n");
        int f1 = m3.smallestElement();
        int f2 = m3.largestElement();
        System.out.println("f1/f2: " + f1 + "/" + f2 + "\n");
        System.out.println("................................................\n");
        m4.clearArray();
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        m4.setAllElements(23);
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        System.out.println("................................................\n");
        temp = mat = new int[4 * 5];
        count = 0;
        for (i = 1; i <= 4; i++) {
            for (j = 1; j <= 5; j++) {
                temp[count++] = 10 * i + j;
            }
        }
        System.out.println("................................................\n");
        IntArray m14 = new IntArray(20, mat);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.deleteElement(12);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.deleteElements(3, 7);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.setElementAt(11, 99);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.insertArray(5, m9);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        IntArray m15 = new IntArray(m14);
        IntSet idx = m15.indexSortAscending();
        System.out.println("idx:\n" + idx + "\n");
        System.out.println("................................................\n");
        m15 = m15.getReorderedArray(idx);
        System.out.println("m15:\n" + m15 + "\n");
        System.out.println("................................................\n");
        IntSet inv = idx.inverseMap();
        System.out.println("inv:\n" + inv + "\n");
        System.out.println("................................................\n");
        m15 = m15.getReorderedArray(inv);
        System.out.println("m15:\n" + m15 + "\n");
        System.out.println("................................................\n");
        m14.sortAscending();
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        System.out.println("................................................\n");
        m14.sortDescending();
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        int[] ptrs = { 2, 0, 3 };
        IntArray ptr = new IntArray(ptrs);
        int[] refs = { 20, 10, 15, 25 };
        IntArray ref = new IntArray(refs);
        System.out.println("PTR: " + new IntArray(ref, ptr));
    }

    public static void main(String[] args) {
        test();
    }
}
