package ucar.multiarray;

import java.io.IOException;

/**
 * This MultiArray implementation wraps another MultiArray
 * of Character componentType to produce a MultiArray of
 * one less rank with String componentType.
 *
 * @see MultiArray
 * @author $Author: rboller_cvs $
 * @version $Revision: 1.1 $ $Date: 2009/04/17 18:05:26 $
 */
public class StringCharAdapter implements MultiArray {

    /**
	 * Construct a new proxy.
	 * @param delegate MultiArray of Character componentType
	 * @param fillValue char which used as terminator and fill value
	 */
    public StringCharAdapter(MultiArray delegate, char fillValue) {
        if (delegate.getComponentType() != Character.TYPE) throw new IllegalArgumentException("Not a Character Array");
        delegate_ = delegate;
        fillValue_ = fillValue;
        lengths_ = new int[delegate_.getRank() - 1];
        final int[] dlengths = delegate_.getLengths();
        System.arraycopy(dlengths, 0, lengths_, 0, lengths_.length);
        maxStringLen_ = dlengths[lengths_.length];
    }

    public char getFillValue() {
        return fillValue_;
    }

    /**
	 * Returns the Class object representing the component
	 * type of the array.
	 * @return Class The componentType
	 * @see java.lang.Class#getComponentType
	 */
    public Class getComponentType() {
        try {
            return Class.forName("java.lang.String");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Implementation problem");
        }
    }

    /**
	 * Returns the number of dimensions of the backing MultiArray,
	 * as transformed by the <code>rankInverseMap()</code> method of
	 * the IndexMap.
	 * @return int number of dimensions
	 */
    public int getRank() {
        return lengths_.length;
    }

    /**
	 * Returns the shape of the backing MultiArray as transformed
	 * by the <code>dimensionsInverseMap()</code> method of
	 * the IndexMap.
	 * @return int array whose length is the rank of this
	 * MultiArray and whose elements represent the
	 * length of each of it's dimensions
	 */
    public int[] getLengths() {
        if (isUnlimited()) {
            System.arraycopy(delegate_.getLengths(), 0, lengths_, 0, lengths_.length);
        }
        return (int[]) lengths_.clone();
    }

    /**
	 * Returns <code>true</code> if and only if the effective dimension
	 * lengths can change.
	 */
    public boolean isUnlimited() {
        return delegate_.isUnlimited();
    }

    /**
	 * Convenience interface; return <code>true</code>
	 * if and only if the rank is zero.
	 * @return boolean <code>true</code> iff rank == 0
	 */
    public boolean isScalar() {
        return 0 == this.getRank();
    }

    /**
	 * @return Object value at <code>index</code>
	 * Length of index must equal rank() of this.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 */
    public Object get(int[] index) throws IOException {
        final int[] dIndex = new int[lengths_.length + 1];
        System.arraycopy(index, 0, dIndex, 0, lengths_.length);
        final char[] buf = new char[maxStringLen_];
        int ii = 0;
        for (; ii < maxStringLen_; ii++) {
            dIndex[lengths_.length] = ii;
            buf[ii] = delegate_.getChar(dIndex);
            if (buf[ii] == fillValue_) break;
        }
        return new String(buf, 0, ii);
    }

    public boolean getBoolean(int[] index) {
        throw new IllegalArgumentException();
    }

    public char getChar(int[] index) throws IOException {
        if (index.length > lengths_.length) return delegate_.getChar(index);
        throw new IllegalArgumentException();
    }

    public byte getByte(int[] index) {
        throw new IllegalArgumentException();
    }

    public short getShort(int[] index) {
        throw new IllegalArgumentException();
    }

    public int getInt(int[] index) {
        throw new IllegalArgumentException();
    }

    public long getLong(int[] index) {
        throw new IllegalArgumentException();
    }

    public float getFloat(int[] index) {
        throw new IllegalArgumentException();
    }

    public double getDouble(int[] index) {
        throw new IllegalArgumentException();
    }

    /**
	 * Length of index must equal rank() of this.
	 * Values of index components must be less than corresponding
	 * values from getLengths().
	 */
    public void set(int[] index, Object value) throws IOException {
        if (value instanceof String) {
            final int[] dIndex = new int[lengths_.length + 1];
            System.arraycopy(index, 0, dIndex, 0, lengths_.length);
            final String sValue = (String) value;
            final int stringLen = ((String) value).length();
            for (int ii = 0; ii < maxStringLen_; ii++) {
                dIndex[lengths_.length] = ii;
                if (ii >= stringLen) {
                    delegate_.setChar(dIndex, fillValue_);
                    continue;
                }
                delegate_.setChar(dIndex, ((String) value).charAt(ii));
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    public void setBoolean(int[] index, boolean value) {
        throw new IllegalArgumentException();
    }

    public void setChar(int[] index, char value) {
        throw new IllegalArgumentException();
    }

    public void setByte(int[] index, byte value) {
        throw new IllegalArgumentException();
    }

    public void setShort(int[] index, short value) {
        throw new IllegalArgumentException();
    }

    public void setInt(int[] index, int value) {
        throw new IllegalArgumentException();
    }

    public void setLong(int[] index, long value) {
        throw new IllegalArgumentException();
    }

    public void setFloat(int[] index, float value) {
        throw new IllegalArgumentException();
    }

    public void setDouble(int[] index, double value) {
        throw new IllegalArgumentException();
    }

    /**
	 * @see Accessor#copyout
	 */
    public MultiArray copyout(int[] origin, int[] shape) throws IOException {
        final int rank = getRank();
        if (origin.length != rank || shape.length != rank) throw new IllegalArgumentException("Rank Mismatch");
        final MultiArrayImpl data = new MultiArrayImpl(getComponentType(), shape);
        AbstractAccessor.copyO(this, origin, data, shape);
        return data;
    }

    /**
	 * @see Accessor#copyin
	 */
    public void copyin(int[] origin, MultiArray data) throws IOException {
        final int rank = getRank();
        if (origin.length != rank || data.getRank() != rank) throw new IllegalArgumentException("Rank Mismatch");
        if (data.getComponentType() != getComponentType()) throw new ArrayStoreException();
        AbstractAccessor.copy(data, data.getLengths(), this, origin);
    }

    /**
	 * @see Accessor#toArray
	 * TODO: optimize?
	 */
    public Object toArray() throws IOException {
        return this.toArray(null, null, null);
    }

    /**
	 * @see Accessor#toArray
	 * TODO: optimize?
	 */
    public Object getStorage() {
        return delegate_.getStorage();
    }

    /**
	 * @see Accessor#toArray
	 * TODO: optimize?
	 */
    public Object toArray(Object dst, int[] origin, int[] shape) throws IOException {
        final int rank = getRank();
        if (origin == null) origin = new int[rank]; else if (origin.length != rank) throw new IllegalArgumentException("Rank Mismatch");
        int[] shp = null;
        if (shape == null) shp = getLengths(); else if (shape.length == rank) shp = (int[]) shape.clone(); else throw new IllegalArgumentException("Rank Mismatch");
        final int[] products = new int[rank];
        final int length = MultiArrayImpl.numberOfElements(shp, products);
        dst = MultiArrayImpl.fixDest(dst, length, getComponentType());
        final MultiArrayImpl data = new MultiArrayImpl(shp, products, dst);
        AbstractAccessor.copyO(this, origin, data, shp);
        return dst;
    }

    private final MultiArray delegate_;

    private final char fillValue_;

    private final int[] lengths_;

    private final int maxStringLen_;

    private static String MultiArrayToString(MultiArray ma) {
        StringBuffer buf = new StringBuffer();
        final int rank = ma.getRank();
        if (rank > 0) {
            buf.append("{\n\t");
            final int[] dims = ma.getLengths();
            final int last = dims[0] - 1;
            for (int ii = 0; ii <= last; ii++) {
                final MultiArray inner = new MultiArrayProxy(ma, new SliceMap(0, ii));
                buf.append(MultiArrayToString(inner));
                if (ii != last) buf.append(", ");
            }
            buf.append("\n}");
        } else {
            try {
                buf.append(ma.get((int[]) null));
            } catch (IOException ee) {
            }
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        MultiArray cha = new MultiArrayImpl(Character.TYPE, new int[] { 4, 5 });
        MultiArray sta = new StringCharAdapter(cha, (char) 0);
        int[] index = { 0 };
        try {
            sta.set(index, "KDEN");
            index[0]++;
            sta.set(index, "KBOU");
            index[0]++;
            sta.set(index, "KABQ");
            index[0]++;
            sta.set(index, "KPHX");
            System.out.println(MultiArrayToString(sta));
            System.out.println(MultiArrayToString(cha));
        } catch (Exception ee) {
        }
    }
}
