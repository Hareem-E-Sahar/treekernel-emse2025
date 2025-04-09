package alto.io.u;

/**
 * <p> Array arithmetic tools, and a single threaded object
 * vector. </p>
 * 
 *
 * @author John Pritchard 
 * @since 1.2
 */
public class Array extends java.lang.Object {

    private java.lang.Object[] list;

    private int p = 0, inc = 1;

    public Array() {
        this(11);
    }

    public Array(int cap) {
        this(cap, cap);
    }

    public Array(int cap, int inc) {
        super();
        if (0 < inc) this.inc = inc; else if (0 > inc) throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(inc));
        if (0 < cap) this.list = new java.lang.Object[cap]; else if (0 != cap) throw new java.lang.IllegalArgumentException(java.lang.String.valueOf(cap)); else return;
    }

    public int size() {
        return this.p;
    }

    public int capacity() {
        if (null == this.list) return 0; else return this.list.length;
    }

    public void clear() {
        java.lang.Object[] list = this.list;
        for (int cc = 0, count = this.p; cc < count; cc++) {
            list[cc] = null;
        }
        this.p = 0;
    }

    public boolean isEmpty() {
        return (1 > this.p);
    }

    public boolean isNotEmpty() {
        return (0 < this.p);
    }

    /**
     * @param dst Copy destination array
     * @return Number of object slots (any may be null) copied into
     * the destination array
     */
    public int copyInto(java.lang.Object[] dst) {
        if (null == dst) throw new java.lang.IllegalArgumentException(); else {
            int dst_len = dst.length;
            int src_len = this.p;
            int copy;
            if (src_len <= dst_len) copy = src_len; else copy = dst_len;
            java.lang.System.arraycopy(this.list, 0, dst, 0, copy);
            return copy;
        }
    }

    public java.lang.Object[] toArray() {
        int count = this.p;
        return copy(this.list, count, null);
    }

    public java.lang.Object[] toArray(java.lang.Object[] component) {
        if (null == component) return this.toArray(); else {
            int count = this.p;
            if (count == component.length) {
                System.arraycopy(this.list, 0, component, 0, count);
                return component;
            } else {
                return copy(this.list, count, component.getClass().getComponentType());
            }
        }
    }

    /**
     * @param el Add element to this list, may be null
     * @return Array index of added element
     */
    public int add(java.lang.Object el) {
        if (this.p >= this.capacity()) this.list = grow(this.list, (this.capacity() + this.inc));
        int re = this.p;
        this.list[this.p++] = el;
        return re;
    }

    public java.util.Enumeration elements() {
        return new Enumerator.Object(this.list, this.p);
    }

    public java.lang.Object get(int idx) {
        if (-1 < idx && idx < this.p) return this.list[idx]; else throw new ArrayIndexOutOfBoundsException(String.valueOf(idx));
    }

    public void set(int idx, java.lang.Object el) {
        if (-1 < idx && idx < this.p) this.list[idx] = el; else throw new ArrayIndexOutOfBoundsException(String.valueOf(idx));
    }

    /**
     * Add into head
     */
    public void push(java.lang.Object el) {
        this.p++;
        this.list = grow2(this.list, this.p, null);
        this.list[0] = el;
    }

    /**
     * Drop from head and return
     */
    public java.lang.Object pop() {
        if (0 < this.p) {
            java.lang.Object re = this.list[0];
            shift(this.list, 0);
            return re;
        } else return null;
    }

    public void poppush(java.lang.Object el) {
        this.pop();
        this.push(el);
    }

    public static final int[] add(int[] list, int a) {
        if (null == list) return new int[] { a }; else {
            int len = list.length;
            int[] copy = new int[len + 1];
            System.arraycopy(list, 0, copy, 0, len);
            copy[len] = a;
            return copy;
        }
    }

    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public static final void shift(java.lang.Object[] oary, int idx) {
        if (0 > idx) return; else {
            int len = oary.length, len1 = oary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(oary, 1, oary, 0, len1); else if (idx == len1) oary[idx] = null; else if (idx < len1) java.lang.System.arraycopy(oary, (idx + 1), oary, idx, len1 - idx);
        }
    }

    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public static final void shift(long[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = alto.io.u.hasharray.IndexConstants.KL_NIL; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public static final void shift(int[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = alto.io.u.hasharray.IndexConstants.ZED; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(double[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = alto.io.u.hasharray.IndexConstants.KL_NIL; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(float[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = alto.io.u.hasharray.IndexConstants.KL_NIL; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(char[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = (char) 0; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(short[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = (short) 0; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(byte[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = (byte) 0; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    public static final void shift(boolean[] iary, int idx) {
        if (0 > idx) return; else {
            int len = iary.length, len1 = iary.length - 1;
            if (0 == idx) java.lang.System.arraycopy(iary, 1, iary, 0, len1); else if (idx == len1) iary[idx] = false; else if (idx < len1) java.lang.System.arraycopy(iary, (idx + 1), iary, idx, len1 - idx);
        }
    }

    /**
     * <p> Object CAT with optional array component type.</p>
     * 
     * @param a Optional object array 
     * @param b Optional object array 
     * @param c Optional component type
     */
    public static final java.lang.Object[] cat(java.lang.Object[] a, java.lang.Object[] b, java.lang.Class c) {
        if (null == a) return b; else if (null == b) return a; else {
            int a_len = a.length;
            int b_len = b.length;
            int r_len = a_len + b_len;
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, r_len); else r = new java.lang.Object[r_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            java.lang.System.arraycopy(b, 0, r, a_len, b_len);
            return r;
        }
    }

    public static final java.lang.Object[] cat(java.lang.Object[] a, java.lang.Object b, Class c) {
        if (null == a) {
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, 1); else r = new java.lang.Object[1];
            r[0] = b;
            return r;
        } else {
            int a_len = a.length;
            int r_len = a_len + 1;
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, r_len); else r = new java.lang.Object[r_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            r[a_len] = b;
            return r;
        }
    }

    public static final int[] cat(int[] a, int[] b) {
        if (null == a) return b; else if (null == b) return a; else {
            int a_len = a.length;
            int b_len = b.length;
            int r_len = a_len + b_len;
            int[] r = new int[r_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            java.lang.System.arraycopy(b, 0, r, a_len, b_len);
            return r;
        }
    }

    public static final long[] cat(long[] a, long[] b) {
        if (null == a) return b; else if (null == b) return a; else {
            int a_len = a.length;
            int b_len = b.length;
            int r_len = a_len + b_len;
            long[] r = new long[r_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            java.lang.System.arraycopy(b, 0, r, a_len, b_len);
            return r;
        }
    }

    public static final java.lang.Object[] add(java.lang.Object[] list, java.lang.Object value, java.lang.Class component) {
        int idx = ((null != list) ? (list.length) : (0));
        java.lang.Object[] nlist = grow(list, (idx + 1), component);
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.Object[] add(java.lang.Object[] list, java.lang.Object value) {
        int idx = ((null != list) ? (list.length) : (0));
        java.lang.Object[] nlist = grow(list, (idx + 1));
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.String[] add(java.lang.String[] list, java.lang.String value) {
        int idx = ((null != list) ? (list.length) : (0));
        java.lang.String[] nlist = grow(list, (idx + 1));
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.Object[] set(java.lang.Object[] list, int idx, java.lang.Object value) {
        java.lang.Object[] nlist = grow(list, (idx + 1));
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.Object[] set(java.lang.Object[] list, int idx, java.lang.Object value, java.lang.Class component) {
        java.lang.Object[] nlist = grow(list, (idx + 1), component);
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.String[] set(java.lang.String[] list, int idx, java.lang.String value) {
        java.lang.String[] nlist = grow(list, (idx + 1));
        nlist[idx] = value;
        return nlist;
    }

    public static final java.lang.String[] grow(java.lang.String[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            java.lang.String[] r = new java.lang.String[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                java.lang.String[] r = new java.lang.String[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a_len);
                return r;
            } else return a;
        }
    }

    public static final java.lang.Object copy(java.lang.Object a) {
        if (a instanceof java.lang.Object[]) {
            java.lang.Object[] ary = (java.lang.Object[]) a;
            return copy(ary, classFor(ary));
        } else if (a instanceof long[]) return copy((long[]) a); else if (a instanceof int[]) return copy((int[]) a); else if (null == a) return null; else throw new IllegalArgumentException("Unrecognized");
    }

    public static final java.lang.Object[] copy(java.lang.Object[] a, Class c) {
        if (null == a) return a; else {
            int a_len = a.length;
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, a_len); else r = new java.lang.Object[a_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            return r;
        }
    }

    public static final java.lang.Object[] copy(java.lang.Object[] a, int a_len, Class c) {
        if (null == a) return a; else {
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, a_len); else r = new java.lang.Object[a_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            return r;
        }
    }

    public static final int[] copy(int[] a) {
        if (null == a) return a; else {
            int a_len = a.length;
            int[] r = new int[a_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            return r;
        }
    }

    public static final long[] copy(long[] a) {
        if (null == a) return a; else {
            int a_len = a.length;
            long[] r = new long[a_len];
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            return r;
        }
    }

    public static final java.lang.Object clear(java.lang.Object a) {
        if (null == a) return null; else {
            java.lang.Class component = classFor(a);
            if (null == component) return null; else return java.lang.reflect.Array.newInstance(component, 0);
        }
    }

    public static final java.lang.Class classFor(java.lang.Object ary) {
        if (null == ary) return null; else return ary.getClass().getComponentType();
    }

    public static final java.lang.Class classFor(java.lang.Object ary, java.lang.Class c) {
        if (null == ary) return c; else return ary.getClass().getComponentType();
    }

    public static final java.lang.Class classFor(java.lang.Object[] ary) {
        if (null == ary) return null; else return ary.getClass().getComponentType();
    }

    public static final int lengthOf(java.lang.Object ary) {
        if (null == ary) return 0; else return java.lang.reflect.Array.getLength(ary);
    }

    public static final java.lang.Object grow(java.lang.Object a) {
        return grow(a, (lengthOf(a) + 1), classFor(a));
    }

    public static final java.lang.Object grow(java.lang.Object a, java.lang.Class c) {
        return grow(a, (lengthOf(a) + 1), classFor(a, c));
    }

    public static final java.lang.Object grow(java.lang.Object a, int n_len, java.lang.Class c) {
        if (1 > n_len) throw new IllegalArgumentException(); else if (null == a) {
            java.lang.Object[] r;
            if (null != c) return java.lang.reflect.Array.newInstance(c, n_len); else throw new IllegalArgumentException();
        } else {
            if (null == c) c = classFor(a);
            int a_len = lengthOf(a);
            java.lang.Object r = java.lang.reflect.Array.newInstance(c, n_len);
            java.lang.System.arraycopy(a, 0, r, 0, a_len);
            return r;
        }
    }

    public static final java.lang.Object[] grow(java.lang.Object[] a, int n_len) {
        return grow(a, n_len, classFor(a));
    }

    public static final java.lang.Object[] grow(java.lang.Object[] a, int n_len, java.lang.Class c) {
        if (1 > n_len) return a; else if (null == a) {
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, n_len); else r = new java.lang.Object[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                java.lang.Object[] r;
                if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, n_len); else r = new java.lang.Object[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a_len);
                return r;
            } else return a;
        }
    }

    public static final java.lang.Object[] grow2(java.lang.Object[] a, int n_len, java.lang.Class c) {
        if (1 > n_len) return a; else if (null == a) {
            java.lang.Object[] r;
            if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, n_len); else r = new java.lang.Object[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                java.lang.Object[] r;
                if (null != c) r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(c, n_len); else r = new java.lang.Object[n_len];
                java.lang.System.arraycopy(a, 0, r, 1, a_len);
                return r;
            } else if (n_len == a_len) {
                java.lang.System.arraycopy(a, 0, a, 1, (a_len - 1));
                return a;
            } else {
                java.lang.System.arraycopy(a, 0, a, 1, n_len);
                return a;
            }
        }
    }

    public static final long[] grow(long[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            long[] r = new long[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                long[] r = new long[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final int[] grow(int[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            int[] r = new int[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                int[] r = new int[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final double[] grow(double[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            double[] r = new double[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                double[] r = new double[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final float[] grow(float[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            float[] r = new float[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                float[] r = new float[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final short[] grow(short[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            short[] r = new short[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                short[] r = new short[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final char[] grow(char[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            char[] r = new char[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                char[] r = new char[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final byte[] grow(byte[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            byte[] r = new byte[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                byte[] r = new byte[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final boolean[] grow(boolean[] a, int n_len) {
        if (1 > n_len) return a; else if (null == a) {
            boolean[] r = new boolean[n_len];
            return r;
        } else {
            int a_len = a.length;
            if (n_len > a_len) {
                boolean[] r = new boolean[n_len];
                java.lang.System.arraycopy(a, 0, r, 0, a.length);
                return r;
            } else return a;
        }
    }

    public static final java.lang.Object[] replace(java.lang.Object[] list, int idx, java.lang.Object item, java.lang.Class component) {
        if (null == list) {
            if (0 == idx) {
                if (null != component) list = (java.lang.Object[]) java.lang.reflect.Array.newInstance(component, 1); else list = new Object[1];
                list[idx] = item;
                return list;
            } else throw new java.lang.ArrayIndexOutOfBoundsException(java.lang.String.valueOf(idx));
        } else {
            int len = list.length;
            if (idx < len) {
                list[idx] = item;
                return list;
            } else if (idx == len) {
                java.lang.Object[] copy;
                if (null != component) copy = (java.lang.Object[]) java.lang.reflect.Array.newInstance(component, (len + 1)); else copy = new java.lang.Object[(len + 1)];
                java.lang.System.arraycopy(list, 0, copy, 0, len);
                copy[idx] = item;
                return copy;
            } else return set(list, idx, item, component);
        }
    }

    public static final java.lang.Object[] insert(java.lang.Object[] a, int idx) {
        int len = a.length;
        java.lang.Object[] r = new java.lang.Object[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final java.lang.Object[] insert(java.lang.Object[] a, int idx, java.lang.Class component) {
        int len = a.length;
        java.lang.Object[] r;
        if (null == component) r = new java.lang.Object[len + 1]; else r = (java.lang.Object[]) java.lang.reflect.Array.newInstance(component, (len + 1));
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final long[] insert(long[] a, int idx) {
        int len = a.length;
        long[] r = new long[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final int[] insert(int[] a, int idx) {
        int len = a.length;
        int[] r = new int[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final double[] insert(double[] a, int idx) {
        int len = a.length;
        double[] r = new double[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final float[] insert(float[] a, int idx) {
        int len = a.length;
        float[] r = new float[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final char[] insert(char[] a, int idx) {
        int len = a.length;
        char[] r = new char[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final short[] insert(short[] a, int idx) {
        int len = a.length;
        short[] r = new short[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final byte[] insert(byte[] a, int idx) {
        int len = a.length;
        byte[] r = new byte[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    public static final boolean[] insert(boolean[] a, int idx) {
        int len = a.length;
        boolean[] r = new boolean[len + 1];
        if (0 == idx) java.lang.System.arraycopy(a, 0, r, 1, len); else if (idx == (len - 1)) java.lang.System.arraycopy(a, 0, r, 0, len); else {
            java.lang.System.arraycopy(a, 0, r, 0, idx);
            java.lang.System.arraycopy(a, idx, r, idx + 1, (len - idx));
        }
        return r;
    }

    /**
     * @param src Source buffer
     * @param src_ofs Source offset within source buffer
     * @param src_len Source length from offset within buffer
     * @param trunc_ofs Truncate offset within source in absolute
     * buffer index coordinates --- same as 'src_ofs', not relative to
     * 'src_ofs', so that src_ofs value zero points to the identical
     * string character as 'trunc_ofs' value zero, etc.
     * @param trunc_len Truncate length from offset
     */
    public static final java.lang.Object[] trunc(java.lang.Object[] src, int src_ofs, int src_len, int trunc_ofs, int trunc_len) {
        if (null == src || 0 > src_ofs || 0 > src_len) throw new java.lang.IllegalArgumentException("src"); else if (0 > trunc_ofs || 1 > trunc_len || trunc_ofs < src_ofs || trunc_len > src_len) throw new java.lang.IllegalArgumentException("trunc"); else if (trunc_ofs == src_ofs) {
            if (trunc_len == src_len) {
                if (src.length == src_len) return src; else if (0 == src_len) return null; else {
                    java.lang.Class comp = src.getClass().getComponentType();
                    java.lang.Object[] re = (java.lang.Object[]) java.lang.reflect.Array.newInstance(comp, src_len);
                    System.arraycopy(src, src_ofs, re, 0, src_len);
                    return re;
                }
            } else {
                int re_len = (src_len - trunc_len);
                if (0 == re_len) return null; else {
                    java.lang.Class comp = src.getClass().getComponentType();
                    java.lang.Object[] re = (java.lang.Object[]) java.lang.reflect.Array.newInstance(comp, re_len);
                    System.arraycopy(src, (src_ofs + trunc_len), re, 0, re_len);
                    return re;
                }
            }
        } else if (trunc_len == src_len) throw new java.lang.IllegalArgumentException("trunc"); else {
            int a_ofs = src_ofs;
            int a_len = (trunc_ofs - src_ofs);
            java.lang.Object[] a = sublist(src, a_ofs, a_len);
            int b_ofs = trunc_ofs + trunc_len;
            int b_len = (src_len - (b_ofs + 1));
            java.lang.Object[] b = sublist(src, b_ofs, b_len);
            return cat(a, b);
        }
    }

    public static final Object[] trunc(Object[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                Object[] copy = new Object[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                Object[] copy = new Object[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final Object[] trunc(Object[] ary, int idx, Class component) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                Object[] copy;
                if (null != component) copy = (Object[]) java.lang.reflect.Array.newInstance(component, nlen); else copy = new Object[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                Object[] copy;
                if (null != component) copy = (Object[]) java.lang.reflect.Array.newInstance(component, nlen); else copy = new Object[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final long[] trunc(long[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                long[] copy = new long[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                long[] copy = new long[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final int[] trunc(int[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                int[] copy = new int[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                int[] copy = new int[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final double[] trunc(double[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                double[] copy = new double[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                double[] copy = new double[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final float[] trunc(float[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                float[] copy = new float[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                float[] copy = new float[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final short[] trunc(short[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                short[] copy = new short[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                short[] copy = new short[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final char[] trunc(char[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                char[] copy = new char[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                char[] copy = new char[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final byte[] trunc(byte[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                byte[] copy = new byte[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                byte[] copy = new byte[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final boolean[] trunc(boolean[] ary, int idx) {
        if (null == ary || 0 > idx) return ary; else {
            int len = ary.length;
            if (idx >= len) return ary; else if (0 == idx) {
                int nlen = (len - 1);
                boolean[] copy = new boolean[nlen];
                System.arraycopy(ary, 1, copy, 0, nlen);
                return copy;
            } else {
                int nlen = (len - 1);
                boolean[] copy = new boolean[nlen];
                System.arraycopy(ary, 0, copy, 0, (idx));
                System.arraycopy(ary, (idx + 1), copy, (idx), (nlen - idx));
                return copy;
            }
        }
    }

    public static final java.lang.Object[] cat(java.lang.Object[] aoary, java.lang.Object[] boary) {
        if (null == aoary) return boary; else if (null == boary) return aoary; else {
            int aclen = aoary.length, bclen = boary.length;
            java.lang.Class comp = aoary.getClass().getComponentType();
            java.lang.Object[] roary = (java.lang.Object[]) java.lang.reflect.Array.newInstance(comp, (aclen + bclen));
            System.arraycopy(aoary, 0, roary, 0, aclen);
            System.arraycopy(boary, 0, roary, aclen, bclen);
            return roary;
        }
    }

    public static final java.lang.Object[] sublist(java.lang.Object[] oary, int idx) {
        if (null == oary) return null; else return sublist(oary, idx, oary.length);
    }

    public static final java.lang.Object[] sublist(java.lang.Object[] oary, int from_idx, int to_idx) {
        if (null == oary || 0 >= to_idx || 0 > from_idx) return null; else {
            java.lang.Class comp = oary.getClass().getComponentType();
            java.lang.Object[] copier = (java.lang.Object[]) java.lang.reflect.Array.newInstance(comp, (to_idx - from_idx));
            System.arraycopy(oary, from_idx, copier, 0, copier.length);
            return copier;
        }
    }

    /**
     * <p> Iterators as for {@link Hasharray} typesy. </p>
     */
    public abstract static class Enumerator implements java.util.Enumeration {

        protected int tc = 0, len = 0;

        protected Enumerator() {
            super();
        }

        /**
         * <p> Iterate over objects.</p>
         * 
         * @author jdp 
         */
        public static final class Object extends Enumerator {

            java.lang.Object[] target = null;

            public Object(java.lang.Object[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Object(java.lang.Object[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else {
                    int tt = tc;
                    java.lang.Object ret = target[tt];
                    while (null == ret && tt < len && null == (ret = target[++tt])) ;
                    if (null == ret) return false; else return true;
                }
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    java.lang.Object ret = null;
                    while (tc < len && null == (ret = target[tc++])) ;
                    if (null == ret) throw new java.util.NoSuchElementException(); else return ret;
                }
            }
        }

        /**
         * <p> Iterate over integers.  Returns {@link java.lang.Integer}
         * objects.</p>
         * 
         * @author jdp
         */
        public static final class Int extends Enumerator {

            int[] target = null;

            public Int(int[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Int(int[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    int ret = target[tc++];
                    return new java.lang.Integer(ret);
                }
            }
        }

        /**
         * <p> Iterate over long 64 bit integer keys as 32 bit integers
         * using a 32 bit mask.  Returns {@link java.lang.Integer}
         * objects.</p>
         * 
         * @author jdp
         */
        public static final class Long extends Enumerator {

            long[] target = null;

            public Long(long[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Long(long[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    long ret = this.target[tc++];
                    return new java.lang.Long(ret);
                }
            }
        }

        public static final class Double extends Enumerator {

            double[] target = null;

            public Double(double[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Double(double[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    double ret = this.target[tc++];
                    return new java.lang.Double(ret);
                }
            }
        }

        public static final class Float extends Enumerator {

            float[] target = null;

            public Float(float[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Float(float[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    float ret = this.target[tc++];
                    return new java.lang.Float(ret);
                }
            }
        }

        public static final class Short extends Enumerator {

            short[] target = null;

            public Short(short[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Short(short[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    short ret = this.target[tc++];
                    return new java.lang.Short(ret);
                }
            }
        }

        public static final class Byte extends Enumerator {

            byte[] target = null;

            public Byte(byte[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Byte(byte[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    byte ret = this.target[tc++];
                    return new java.lang.Byte(ret);
                }
            }
        }

        public static final class Char extends Enumerator {

            char[] target = null;

            public Char(char[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Char(char[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    char ret = this.target[tc++];
                    return new java.lang.Character(ret);
                }
            }
        }

        public static final class Boolean extends Enumerator {

            boolean[] target = null;

            public Boolean(boolean[] target) {
                this(target, ((null == target) ? (0) : (target.length)));
            }

            public Boolean(boolean[] target, int size) {
                super();
                this.target = target;
                this.len = size;
            }

            public boolean hasMoreElements() {
                if (tc >= len) return false; else return true;
            }

            public java.lang.Object nextElement() {
                if (tc >= len) throw new java.util.NoSuchElementException(); else {
                    boolean ret = this.target[tc++];
                    if (ret) return java.lang.Boolean.TRUE; else return java.lang.Boolean.FALSE;
                }
            }
        }
    }
}
