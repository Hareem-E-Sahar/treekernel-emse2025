package jumbo.euclid;

import jumbo.xml.util.Util;

/**
<P>
 RealArray - array of doubles
<P>
 RealArray represents a 1-dimensional vector/array of doubles and
 is basically a wrapper for double[] in Java
 There are a lot of useful member functions (sorting, ranges, parallel
 operations, etc. - have a look)
<P>
 The default is an array with zero points.  All arrays are valid objects.
<P>
 Attempting to create an array with < 0 points creates a default array 
 (zero points).
<P>
 Since double[] knows its length (unlike C), there are many cases where
 double[] can be safely used.  However it is not a first-class object
 and RealArray supplies this feature. double[] is referenceable
 through getArray().
@author (C) P. Murray-Rust, 1996
*/
public class RealArray extends Status {

    public static final int ABOVE = 1;

    public static final int BELOW = 2;

    /** maximum number of elements (for bound checking) - resettable
*/
    private int maxelem = 10000;

    /** actual number of elements
*/
    int nelem;

    /** the array of doubles
*/
    double[] array;

    int bufsize = 5;

    /** default is an array of zero points 
*/
    public RealArray() {
        nelem = 0;
        bufsize = 5;
        array = new double[bufsize];
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
            double[] array1 = new double[bufsize];
            System.arraycopy(array, 0, array1, 0, nelem);
            array = array1;
        }
    }

    /** creates n-element array initialised to 0.0 
*/
    public RealArray(int n) {
        this(n, 0.0);
    }

    /** This gives a nelem-element array initialised to elem1+(i-1)*delta
*/
    public RealArray(int n, double elem1, double delta) {
        if (!checkSize(n)) return;
        array = new double[n];
        bufsize = n;
        double ff = elem1;
        for (int i = 0; i < n; i++) {
            array[i] = ff;
            ff += delta;
        }
    }

    /** set all elements of the array to a given value 
*/
    public RealArray(int n, double elem1) {
        if (!checkSize(n)) return;
        array = new double[n];
        bufsize = n;
        for (int i = 0; i < n; i++) {
            array[i] = elem1;
        }
    }

    /** Formed by feeding in an existing array; requires the size.  
(You can use the *.length of the array if necessary) */
    public RealArray(int n, double[] arr) {
        if (!checkSize(n)) return;
        array = new double[n];
        bufsize = n;
        System.arraycopy(arr, 0, array, 0, n);
    }

    /** from an existing double[] (which knows its length) 
*/
    public RealArray(double[] arr) {
        this(arr.length, arr);
    }

    /** convert an IntArray to a RealArray
*/
    public RealArray(IntArray ia) {
        if (!checkSize(ia.size())) return;
        array = new double[nelem];
        bufsize = nelem;
        for (int i = 0; i < nelem; i++) {
            array[i] = (new Double(ia.elementAt(i))).doubleValue();
        }
    }

    /** subarray of another array - inclusive; if low > high or other silly 
  indices, creates default array */
    public RealArray(RealArray m, int low, int high) {
        nelem = high - low + 1;
        if (low < 0 || low > high || high >= m.size()) return;
        if (!checkSize(nelem)) return;
        array = new double[nelem];
        bufsize = nelem;
        System.arraycopy(m.array, low, array, 0, nelem);
    }

    /** clones another RealArray 
*/
    public Object clone() {
        RealArray temp = new RealArray(nelem);
        temp.nelem = nelem;
        temp.maxelem = maxelem;
        System.arraycopy(array, 0, temp.array, 0, nelem);
        temp.bufsize = nelem;
        return (Object) temp;
    }

    /** copy constructor 
*/
    public RealArray(RealArray m) {
        this.shallowCopy(m);
        System.arraycopy(m.array, 0, array, 0, nelem);
    }

    /** Create a given 'shape' of array for data filtering.  An intended use
 is with RealArray.arrayFilter().  
 The shapes (before scaling by maxval) are:
<UL>
<LI>"TRIANGLE"; 1/nn, 2/nn, ... 1 ... 2/nn, 1/nn; nelem is set to 2*nn - 1
<LI>"ZIGZAG"; 1/nn, 2/nn, ... 1 ... 1/nn, 0, -1/nn, -2/nn, -1, ... -1/nn,; 
  nelem is set to 4*nn - 1
</UL>
*/
    public RealArray(int nn, String shape, double maxval) {
        if (shape.toUpperCase().equals("TRIANGLE")) {
            nelem = nn * 2 - 1;
            if (!checkSize(nelem)) return;
            array = new double[nelem];
            double delta = maxval / ((double) nn);
            for (int i = 0; i < nn; i++) {
                array[i] = (i + 1) * delta;
                array[nelem - i - 1] = array[i];
            }
        } else if (shape.toUpperCase().equals("ZIGZAG")) {
            nelem = nn * 4 - 1;
            if (!checkSize(nelem)) return;
            array = new double[nelem];
            double delta = maxval / ((double) nn);
            for (int i = 0; i < nn; i++) {
                array[i] = (i + 1) * delta;
                array[2 * nn - i - 2] = array[i];
                array[2 * nn + i] = -array[i];
                array[nelem - i - 1] = -array[i];
            }
            array[2 * nn - 1] = 0.0;
        }
    }

    public static final String GAUSSIAN = "Gaussian";

    public static final String GAUSSIAN_FIRST_DERIVATIVE = "Gaussian First Derivative";

    public static final String GAUSSIAN_SECOND_DERIVATIVE = "Gaussian Second Derivative";

    /** creates a filter based on Gaussian and derivatives. Scaled so that approximately
	2.5 sigma is included (i.e. value at edge is ca 0.01 of centre */
    public static RealArray getFilter(int halfWidth, String function) {
        if (!function.equals(GAUSSIAN) && !function.equals(GAUSSIAN_FIRST_DERIVATIVE) && !function.equals(GAUSSIAN_SECOND_DERIVATIVE)) return null;
        if (halfWidth < 1) halfWidth = 1;
        double xar[] = new double[2 * halfWidth + 1];
        double limit = 7.0;
        double sum = 0;
        double x = 0.0;
        double y = 1.0;
        double dHalf = limit * 0.693 * 0.693 / (double) halfWidth;
        for (int i = 0; i <= halfWidth; i++) {
            if (function.equals(GAUSSIAN)) y = Math.exp(-x * x);
            if (function.equals(GAUSSIAN_FIRST_DERIVATIVE)) y = -2 * x * Math.exp(-x * x);
            if (function.equals(GAUSSIAN_SECOND_DERIVATIVE)) y = (4 * (x * x) - 2.0) * Math.exp(-x * x);
            xar[halfWidth + i] = (function.equals(GAUSSIAN_FIRST_DERIVATIVE)) ? -y : y;
            xar[halfWidth - i] = y;
            sum += (i == 0) ? y : 2 * y;
            x += dHalf;
        }
        if (function.equals(GAUSSIAN)) {
            for (int i = 0; i < 2 * halfWidth + 1; i++) {
                xar[i] /= sum;
            }
        }
        RealArray r = new RealArray(xar);
        return r;
    }

    /** from an array of Strings (which must represent Reals)
@exception NumberFormatException a string could not be interpreted as Real
*/
    public RealArray(String[] strings) throws NumberFormatException {
        this(strings.length);
        for (int i = 0; i < strings.length; i++) {
            array[i] = (Double.valueOf(strings[i])).doubleValue();
        }
    }

    /** from a String with space-separated strings representing Reals
@exception NumberFormatException a string could not be interpreted as Real
*/
    public RealArray(String string) throws NumberFormatException {
        this(Util.split(string));
    }

    /** shallowCopy 
*/
    public void shallowCopy(RealArray m) {
        nelem = m.nelem;
        bufsize = m.bufsize;
        maxelem = m.maxelem;
        array = m.array;
    }

    /** extracts a given element from the array
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public double elementAt(int elem) throws ArrayIndexOutOfBoundsException {
        return array[elem];
    }

    /** get actual number of elements
*/
    public int size() {
        return nelem;
    }

    /** return the array as a double[]; this has to resize the array to the precise
 length used , or confusion will result! Note that this gives the user access to
 the actual array, so that they can alter its contents.  This should be used 
 with care, but Java should stop any access outside the buffer limits. */
    public double[] getArray() {
        if (nelem != array.length) {
            double[] temp = new double[nelem];
            System.arraycopy(array, 0, temp, 0, nelem);
            array = temp;
        }
        return array;
    }

    /** clear all elements of array 
*/
    public void clearArray() {
        for (int i = 0; i < size(); i++) {
            array[i] = 0.0;
        }
    }

    /** return the elements in reverse order as double[]
*/
    public double[] getReverseArray() {
        int count = size();
        double[] temp = new double[count];
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

    private void checkConformable(RealArray m) throws UnequalArraysException {
        if (nelem != m.nelem) {
            throw new UnequalArraysException();
        }
    }

    /** are two arrays equal in all elements?
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public boolean equals(RealArray f) throws UnequalArraysException {
        return equals(f, Real.getEpsilon());
    }

    /** are two arrays equal in all elements? (use epsilon as tolerance)
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public boolean equals(RealArray f, double epsilon) throws UnequalArraysException {
        checkConformable(f);
        for (int i = 0; i < nelem; i++) {
            if (!Real.isEqual(array[i], f.array[i], epsilon)) {
                return false;
            }
        }
        return true;
    }

    /** array addition - adds conformable arrays
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public RealArray plus(RealArray f) throws UnequalArraysException {
        checkConformable(f);
        RealArray m = (RealArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] = f.array[i] + array[i];
        }
        return m;
    }

    /** array subtraction - subtracts conformable arrays
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public RealArray subtract(RealArray f) throws UnequalArraysException {
        checkConformable(f);
        RealArray m = (RealArray) this.clone();
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

    /** add a scalar to all elements; does NOT modify 'this';
for subtraction use negative scalar
*/
    public RealArray addScalar(double f) {
        RealArray m = (RealArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] += f;
        }
        return m;
    }

    /** array multiplication by a scalar; does NOT modify 'this'
*/
    public RealArray multiplyBy(double f) {
        RealArray m = (RealArray) this.clone();
        for (int i = 0; i < nelem; i++) {
            m.array[i] *= f;
        }
        return m;
    }

    /** set a given element into the array; must be less than current max index
@exception ArrayIndexOutOfBoundsException elem >= size of <TT>this</TT>
*/
    public void setElementAt(int elem, double f) throws ArrayIndexOutOfBoundsException {
        array[elem] = f;
    }

    /** RHS: get a subRealArray from element start to end
*/
    public RealArray getSubArray(int start, int end) {
        int nel = end - start + 1;
        RealArray f = new RealArray(nel, 0);
        System.arraycopy(array, start, f.array, 0, nel);
        return f;
    }

    /** copy a smaller array into the array statrting at start 
*/
    public void setElements(int start, double[] a) {
        System.arraycopy(a, 0, this.array, start, a.length);
    }

    /** is the array filled with zeros?
*/
    public boolean isClear() {
        for (int i = 0; i < nelem; i++) {
            if (Real.isZero(array[i])) return false;
        }
        return true;
    }

    /** initialise array to given double[] 
*/
    public void setAllElements(double f) {
        Real.initArray(nelem, array, f);
    }

    /** sum all elements
*/
    public double sumAllElements() {
        double sum = 0.0;
        for (int i = 0; i < nelem; i++) {
            sum += array[i];
        }
        return sum;
    }

    /** absolute sum of all elements
*/
    public double absSumAllElements() {
        double sum = 0.0;
        for (int i = 0; i < nelem; i++) {
            sum += Math.abs(array[i]);
        }
        return sum;
    }

    /** inner product - same as dotProduct
*/
    public double innerProduct() {
        double result = Double.NEGATIVE_INFINITY;
        try {
            result = this.dotProduct(this);
        } catch (UnequalArraysException x) {
        }
        return result;
    }

    /** dot product of two RealArrays - if of same length - else zero
@exception UnequalArraysException f is different size from <TT>this</TT>
*/
    public double dotProduct(RealArray f) throws UnequalArraysException {
        checkConformable(f);
        double sum = 0.0;
        for (int i = 0; i < nelem; i++) {
            sum += array[i] * f.array[i];
        }
        return sum;
    }

    /** Euclidean length of vector
*/
    public double euclideanLength() {
        return Math.sqrt(innerProduct());
    }

    /** root mean square sqrt(sigma(x(i)**2)/n)
@exception ArrayTooSmallException must have at least 1 point
*/
    public double rms() throws ArrayTooSmallException {
        if (nelem == 0) {
            throw new ArrayTooSmallException();
        }
        return euclideanLength() / Math.sqrt((double) nelem);
    }

    /** get unit vector
@exception ZeroVectorException elements of <TT>this</TT> are all zero
*/
    public RealArray unitVector() throws ZeroVectorException {
        double l = euclideanLength();
        if (Real.isZero(l)) {
            throw new ZeroVectorException();
        }
        double scale = 1.0 / l;
        RealArray f = new RealArray(nelem);
        f = this.multiplyBy(scale);
        return f;
    }

    /** cumulative sum of array (new RA contains
<BR>
elem[i] = sum(k = 0 to i) f[k]
<P>
does not modify 'this'
*/
    public RealArray cumulativeSum() {
        RealArray temp = new RealArray(nelem);
        double sum = 0.0;
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
 The filter can be created with a RealArray constructor
*/
    public RealArray applyFilter(RealArray filter) {
        if (nelem == 0 || filter == null || filter.nelem <= 1) {
            return this;
        }
        int nfilter = filter.size();
        int midfilter = (nfilter - 1) / 2;
        RealArray temp = new RealArray(nelem);
        double wt = 0;
        double sum = 0;
        for (int j = 0; j < midfilter; j++) {
            wt = 0.0;
            sum = 0.0;
            int l = 0;
            for (int k = midfilter - j; k < nfilter; k++) {
                wt += Math.abs(filter.array[k]);
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        wt = filter.absSumAllElements();
        for (int j = midfilter; j < nelem - midfilter; j++) {
            sum = 0.0;
            int l = j - midfilter;
            for (int k = 0; k < nfilter; k++) {
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        for (int j = nelem - midfilter; j < nelem; j++) {
            wt = 0.0;
            sum = 0.0;
            int l = j - midfilter;
            for (int k = 0; k < midfilter + nelem - j; k++) {
                wt += Math.abs(filter.array[k]);
                sum += filter.array[k] * this.array[l++];
            }
            temp.array[j] = sum / wt;
        }
        return temp;
    }

    /** trims array to limit. if flag = BELOW values below limit
	are set to limit, else if flag == ABOVE values above limit
	are set to limit;
	*/
    public RealArray trim(int flag, double limit) {
        RealArray temp = new RealArray(nelem);
        for (int i = 0; i < nelem; i++) {
            double v = array[i];
            if ((flag == BELOW && v < limit) || (flag == ABOVE && v > limit)) v = limit;
            temp.array[i] = v;
        }
        return temp;
    }

    /** index of largest element; returns -1 if zero element array
*/
    public int indexOfLargestElement() {
        int index = -1;
        double value = Double.NEGATIVE_INFINITY;
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
        double value = Double.POSITIVE_INFINITY;
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
    public double largestElement() {
        return array[indexOfLargestElement()];
    }

    /** value of largest element (synonym)
*/
    public double getMax() {
        return array[indexOfLargestElement()];
    }

    /** value of smallest element
*/
    public double smallestElement() {
        return array[indexOfSmallestElement()];
    }

    /** value of smallest element
*/
    public double getMin() {
        return array[indexOfSmallestElement()];
    }

    /** range of array (default RealRange for zero array)
*/
    public RealRange getRange() {
        RealRange r = new RealRange();
        for (int i = 0; i < nelem; i++) {
            r.add(array[i]);
        }
        return r;
    }

    /** as above (deprecated)
*/
    public RealRange range() {
        return this.getRange();
    }

    /** delete element and close up; if outside range, take no action
*/
    public void deleteElement(int elem) {
        if (elem < 0 || elem >= nelem) return;
        nelem--;
        if (bufsize > nelem * 2) {
            bufsize /= 2;
        }
        double[] temp = new double[bufsize];
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
        double[] temp = new double[bufsize];
        System.arraycopy(array, 0, temp, 0, low);
        System.arraycopy(array, high + 1, temp, low, nelem - low);
    }

    /** insert element and expand; if outside range, take no action
*/
    public void insertElementAt(int elem, double f) {
        if (elem < 0 || elem > nelem) return;
        nelem++;
        makeSpace(nelem);
        double[] array1 = new double[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        array1[elem] = f;
        System.arraycopy(array, elem, array1, elem + 1, nelem - elem);
        array = array1;
    }

    /** insert a RealArray at position elem and expand
*/
    public void insertArray(int elem, RealArray f) {
        int n = f.size();
        if (elem < 0 || elem >= nelem || n < 1) return;
        nelem += n;
        makeSpace(nelem);
        double[] array1 = new double[nelem];
        System.arraycopy(array, 0, array1, 0, elem);
        System.arraycopy(f.getArray(), 0, array1, elem, n);
        System.arraycopy(array, elem, array1, n + elem, nelem - elem - n);
    }

    /** append element
*/
    public void addElement(double f) {
        makeSpace(nelem + 1);
        array[nelem++] = f;
    }

    /** append elements
*/
    public void addArray(RealArray f) {
        makeSpace(nelem + f.nelem);
        System.arraycopy(f.array, 0, array, nelem, f.nelem);
        nelem += f.nelem;
    }

    /** reorder by index in IntSet; does NOT modify array
@exception BadSubscriptException an element of idx is outside range of <TT>this</TT>
*/
    public RealArray getReorderedArray(IntSet idx) throws BadSubscriptException {
        RealArray temp = new RealArray(nelem);
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
    public IntSet inRange(RealRange r) {
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
    public IntSet outOfRange(RealRange r) {
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
            temp[i] = Double.toString(array[i]);
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
    public static double[] deleteElements(double[] f, int low, int hi) {
        if (hi >= f.length) hi = f.length - 1;
        if (low < 0) low = 0;
        int ndel = hi - low + 1;
        if (ndel <= 0) return f;
        double[] temp = new double[f.length - ndel];
        System.arraycopy(f, 0, temp, 0, low);
        System.arraycopy(f, hi + 1, temp, low, f.length - hi - 1);
        return temp;
    }

    /** copy a double[] into a new one 
*/
    public static double[] copy(double[] f) {
        double temp[] = new double[f.length];
        System.arraycopy(f, 0, temp, 0, f.length);
        return temp;
    }

    /**
    quick sort - modified from p96 - 97 (Hansen - C++ answer book)
    
    Scalar sort refers to sorting IntArray and RealArray (and similar classes)
    where the objects themeselves are sorted.
    
    Index sort refers to sorting indexes (held as IntSet's) to the object
    and getting the sorted object(s) with reorderBy(IntSet idx);
    
*/
    void xfswap(double[] x, int a, int b) {
        double tmp = x[a];
        x[a] = x[b];
        x[b] = tmp;
    }

    static final int CUTOFF = 16;

    private void inssort(int left, int right) {
        int k;
        for (int i = left + 1; i <= right; i++) {
            double v = array[i];
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
        double v = array[j];
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

    private void xxinssort(IntArray iarr, double[] pfl, int left, int right) {
        int j, k;
        for (int i = left + 1; i <= right; i++) {
            int v = iarr.elementAt(i);
            for (j = i, k = j - 1; j > 0 && pfl[iarr.elementAt(k)] > pfl[v]; j--, k--) {
                iarr.setElementAt(j, iarr.elementAt(k));
            }
            iarr.setElementAt(j, v);
        }
    }

    private int xxpartition(IntArray iarr, double[] pfl, int left, int right) {
        int mid = (left + right) / 2;
        if (pfl[iarr.elementAt(left)] > pfl[iarr.elementAt(mid)]) xxfswap(iarr, left, mid);
        if (pfl[iarr.elementAt(left)] > pfl[iarr.elementAt(right)]) xxfswap(iarr, left, right);
        if (pfl[iarr.elementAt(mid)] > pfl[iarr.elementAt(right)]) xxfswap(iarr, mid, right);
        int j = right - 1;
        xxfswap(iarr, mid, j);
        int i = left;
        double v = pfl[iarr.elementAt(j)];
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

    private void xxiqsort(IntArray iarr, double[] pfl, int left, int right) {
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
        System.out.println("--------------Testing RealArray--------------\n");
        RealArray m9 = new RealArray();
        System.out.println("................................................\n");
        int i, j;
        System.out.println("................................................\n");
        RealRange r1 = new RealRange();
        System.out.println("r1: " + r1 + "\n");
        System.out.println("................................................\n");
        RealRange r2 = new RealRange(0.2, 5.7);
        System.out.println("r2: " + r2 + "\n");
        System.out.println("................................................\n");
        double f = 2.7;
        boolean bb = r2.includes(f);
        System.out.println(" bb: " + bb + "\n");
        System.out.println("................................................\n");
        f = -2.7;
        bb = r2.includes(f);
        System.out.println(" bb: " + bb + "\n");
        System.out.println("................................................\n");
        RealRange r3 = new RealRange(5.0, 2.0);
        System.out.println("r3: " + r3 + "\n");
        System.out.println("................................................\n");
        RealArray m0 = new RealArray();
        System.out.println("m0:\n" + m0 + "\n");
        System.out.println("................................................\n");
        RealArray m1 = new RealArray(3);
        System.out.println("m1:\n" + m1 + "\n");
        System.out.println("................................................\n");
        m9 = m1;
        double[] mat;
        double[] temp;
        temp = mat = new double[4 * 5];
        int count = 0;
        for (i = 1; i <= 4; i++) for (j = 1; j <= 5; j++) temp[count++] = -10 * i + j;
        RealArray m3 = new RealArray(20, mat);
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        RealArray m4 = new RealArray(m3);
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        f = m4.elementAt(10);
        System.out.println(" f: " + f + "\n");
        System.out.println("................................................\n");
        RealArray m19 = new RealArray(m4, 6, 9);
        System.out.println("m19:\n" + m19 + "\n");
        System.out.println("................................................\n");
        m9 = m19;
        System.out.println("................................................\n");
        m4.setElementAt(5, 123.0);
        int nelem = m4.size();
        double[] fr;
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
        m3 = m3.multiplyBy(-1.03);
        System.out.println("m3:\n" + m3 + "\n");
        System.out.println("................................................\n");
        RealArray m8 = null;
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
        double f1 = m3.smallestElement();
        double f2 = m3.largestElement();
        System.out.println("f1/f2: " + f1 + "/" + f2 + "\n");
        System.out.println("................................................\n");
        m4.clearArray();
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        m4.setAllElements(23.);
        System.out.println("m4:\n" + m4 + "\n");
        System.out.println("................................................\n");
        double farr[] = { 1., 2., 3. };
        RealArray ff = new RealArray(3, farr);
        double len = ff.euclideanLength();
        System.out.println("len: " + len + "\n");
        System.out.println("................................................\n");
        RealArray m20 = null;
        try {
            m20 = new RealArray(ff.unitVector());
        } catch (ZeroVectorException e) {
            Util.bug(e);
        }
        System.out.println("m20:\n" + m20 + "\n");
        System.out.println("................................................\n");
        double dot = ff.innerProduct();
        System.out.println("dot: " + dot + "\n");
        System.out.println("................................................\n");
        RealSquareMatrix m21 = RealSquareMatrix.outerProduct(ff);
        System.out.println("m21:\n" + m21 + "\n");
        System.out.println("................................................\n");
        temp = mat = new double[4 * 5];
        count = 0;
        for (i = 1; i <= 4; i++) {
            for (j = 1; j <= 5; j++) {
                temp[count++] = 10 * i + j;
            }
        }
        System.out.println("................................................\n");
        RealArray m14 = new RealArray(20, mat);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.deleteElement(12);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.deleteElements(3, 7);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.setElementAt(11, 99.0);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        m14.insertArray(5, m9);
        System.out.println("m14:\n" + m14 + "\n");
        System.out.println("................................................\n");
        RealArray m15 = new RealArray(m14);
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
        System.out.println("................................................\n");
        RealArray filter = new RealArray(3, "TRIANGLE", 5.0);
        System.out.println("TRIANGLE " + filter);
        System.out.println("Filtered array " + m14.applyFilter(filter));
        filter = new RealArray(3, "ZIGZAG", 2.0);
        System.out.println("ZIGZAG" + filter);
        System.out.println("Filtered array " + m14.applyFilter(filter));
    }

    public static void main(String[] args) {
        test();
    }
}
