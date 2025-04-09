package org.javaseis.array;

/**
 * Java implementation of in-memory array transposes.
 * Designed for use with JavaSeis Parallel Distributed Objects, using
 * 1D Java arrays as the base storage.
 * <p>
 * The methods in this class are static methods that can be called for any
 * 1D Java array that has been "shaped" as a 2D Fortran style array. Arbitrary
 * object arrays and primitive types are supported.  The "basic" operations are
 * provided, which are transpose the last two dimensions of 2D and 3D arrays
 * respectively.  More complex transposes are built from the basic types.
 * <p>
 * For example, a "321" transpose is constructed from:
 * <pre>
 * 123 -> 213 using T21 transpose
 * 213 -> 231 using T132 transpose
 * 231 -> 321 using T21 transpose
 * </pre>
 * See the MultiArray class for more examples.
 * <p>
 * @author Chuck Mosher for JavaSeis.org
 *
 */
public class Transpose {

    /**
   * Transpose the frames of a 2D Fortran style array stored in a JavaSeis IMultiArray.
   * A byte array of length (n1+n2)/2 is allocated internally for work space.
   * Runtime exceptions are thrown on memory or algorithm failures.
   *
   * @param a MultiArray to be transposed
   */
    public static void transpose21(IMultiArray a) {
        if (a.getDimensions() < 2) throw new IllegalArgumentException("Input MultiArray must have at least 2 dimensions");
        int elementCount = a.getElementCount();
        int n1 = a.getLength(0);
        int n2 = a.getLength(1);
        long offset = 0;
        long nframe = a.getTotalFrameCount();
        long frameLength = a.getFrameLength();
        IBackingArray b = a.getBackingArray();
        for (int i = 0; i < nframe; i++) {
            Transpose.transpose21(b, offset, elementCount, n1, n2);
            offset += frameLength;
        }
    }

    public static void transpose132(IMultiArray a) {
        if (a.getDimensions() < 3) throw new IllegalArgumentException("Input MultiArray must have at least 3 dimensions");
        int elementCount = a.getElementCount();
        int n1 = a.getLength(0);
        int n2 = a.getLength(1);
        int n3 = a.getLength(2);
        long offset = 0;
        long nvol = a.getTotalVolumeCount();
        long volumeLength = a.getVolumeLength();
        IBackingArray b = a.getBackingArray();
        for (int i = 0; i < nvol; i++) {
            Transpose.transpose132(b, offset, elementCount, n1, n2, n3);
            offset += volumeLength;
        }
    }

    public static void transpose1243(IMultiArray a) {
        if (a.getDimensions() < 4) throw new IllegalArgumentException("Input MultiArray must have at least 3 dimensions");
        int n1 = a.getLength(0) * a.getElementCount();
        int n2 = a.getLength(1);
        int n3 = a.getLength(2);
        int n4 = a.getLength(3);
        long offset = 0;
        long nhc = 1;
        long hypercubeLength = a.getHypercubeLength();
        IBackingArray b = a.getBackingArray();
        for (int i = 0; i < nhc; i++) {
            Transpose.transpose132(b, offset, n1, n2, n3, n4);
            offset += hypercubeLength;
        }
    }

    /**
   * Transpose a 2D Fortran style array stored in a JavaSeis IBackingArray.
   * A byte array of length (n1+n2)/2 is allocated internally for work space.
   * Runtime exceptions are thrown on memory or algorithm failures.
   *
   * @param a IBackingArray "shaped" as a 2D Fortran array
   * @param offset offset into backing array of the 2D frame to be transposed
   * @param n1 first Fortran dimension
   * @param n2 second Fortran dimension
   * @param elementCount  element count (e.g. 2 for complex numbers).
   */
    public static void transpose21(IBackingArray a, long offset, int elementCount, int n1, int n2) {
        if (a.getBackingArrayType() == BackingArray.Type.JAVA_ARRAY) {
            ArrayStorage as = (ArrayStorage) a;
            int ia = (int) (as.getOffset() + offset);
            if (elementCount == 1) Transpose.transpose21((as.getArray()), ia, n1, n2); else Transpose.transpose132((as.getArray()), ia, elementCount, n1, n2);
        } else {
            Transpose.backingArray132(a, offset, elementCount, n1, n2);
        }
    }

    /**
  * Transpose a 2D Fortran style array stored in a JavaSeis IBackingArray.
  * A byte array of length (n1+n2)/2 is allocated internally for work space.
  * Runtime exceptions are thrown on memory or algorithm failures.
  *
  * @param a IBackingArray "shaped" as a 2D Fortran array
  * @param offset offset into backing array for the 2D frame to be transposed
  * @param elementCount  element count (e.g. 2 for complex numbers).
  * @param n1 first Fortran dimension
  * @param n2 second Fortran dimension
  */
    public static void transpose132(IBackingArray a, long offset, int elementCount, int n1, int n2, int n3) {
        if (a.getBackingArrayType() == BackingArray.Type.JAVA_ARRAY) {
            ArrayStorage as = (ArrayStorage) a;
            int ia = (int) as.getOffset() + (int) offset;
            Transpose.transpose132((as.getArray()), ia, elementCount * n1, n2, n3);
        } else {
            Transpose.backingArray132(a, offset, elementCount * n1, n2, n3);
        }
    }

    /**
     * Transpose a 2D Fortran style array stored in a 1D Java array.
     * Routines are provided for primitive types and Object arrays.
     * A byte array of length (n1+n2)/2 is allocated internally for work space.
     * Runtime exceptions are thrown on memory or algorithm failures.
     *
     * @param a 1D vector "shaped" as a 2D Fortran array
     * @param ia offset in the 1D array to the start of the 2D array
     * @param n1 first Fortran dimension
     * @param n2 second Fortran dimension
     * @param elementCount  element count (e.g. 2 for complex numbers).
     */
    @SuppressWarnings("unchecked")
    public static void transpose21(Object a, int ia, int n1, int n2) {
        int move_size = (n1 + n2) / 2;
        byte[] move = new byte[move_size];
        Class componentType = a.getClass().getComponentType();
        if (componentType == null) throw new IllegalArgumentException("Input argument must be an array of objects or primitive types");
        if (componentType.equals(byte.class)) {
            byte21a((byte[]) a, ia, n1, n2, move, move_size);
        } else if (componentType.equals(short.class)) {
            short21a((short[]) a, ia, n1, n2, move, move_size);
        } else if (componentType.equals(int.class)) {
            int21a((int[]) a, ia, n1, n2, move, move_size);
        } else if (componentType.equals(float.class)) {
            float21a((float[]) a, ia, n1, n2, move, move_size);
        } else if (componentType.equals(long.class)) {
            long21a((long[]) a, ia, n1, n2, move, move_size);
        } else if (componentType.equals(double.class)) {
            double21a((double[]) a, ia, n1, n2, move, move_size);
        } else {
            object21a((Object[]) a, ia, n1, n2, move, move_size);
        }
    }

    /**
     * Transpose the last two dimensions of a 3D Fortran style array stored in a
     * 1D Java array. Routines are provided for primitive types and Object
     * arrays. A byte array of length (n2+n3)/2 is allocated internally for work
     * space, along with two vectors of length n1. Runtime exceptions are thrown
     * on memory or algorithm failures.
     *
     * @param a
     *            1D vector "shaped" as a 2D Fortran array
     * @param n1
     *            first Fortran dimension
     * @param n2
     *            second Fortran dimension
     */
    @SuppressWarnings("unchecked")
    public static void transpose132(Object a, int ia, int n1, int n2, int n3) {
        int move_size = (n2 + n3) / 2;
        byte[] move = new byte[move_size];
        Class componentType = a.getClass().getComponentType();
        if (componentType == null) throw new IllegalArgumentException("Input argument must be an array of objects or primitive types");
        if (componentType.equals(byte.class)) {
            byte132a((byte[]) a, ia, n1, n2, n3, move, move_size);
        } else if (componentType.equals(short.class)) {
            short132a((short[]) a, ia, n1, n2, n3, move, move_size);
        } else if (componentType.equals(int.class)) {
            int132a((int[]) a, ia, n1, n2, n3, move, move_size);
        } else if (componentType.equals(float.class)) {
            float132a((float[]) a, ia, n1, n2, n3, move, move_size);
        } else if (componentType.equals(long.class)) {
            long132a((long[]) a, ia, n1, n2, n3, move, move_size);
        } else if (componentType.equals(double.class)) {
            double132a((double[]) a, ia, n1, n2, n3, move, move_size);
        } else {
            object132a((Object[]) a, ia, n1, n2, n3, move, move_size);
        }
    }

    private static int float21a(float[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        float b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int int21a(int[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        int b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int short21a(short[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        short b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int byte21a(byte[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        byte b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int long21a(long[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        long b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int double21a(double[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        double b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static <T> int object21a(T[] a, int ia, int n1, int n2, byte[] move, int move_size) {
        int i, j, im, mn;
        T b, c, d;
        int ncount;
        int k;
        if (n1 < 0 || n2 < 0) throw new IllegalArgumentException("n2,n1 < 0");
        if (n1 < 2 || n2 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        if (n1 == n2) {
            for (i = 0; i < n2; ++i) for (j = i + 1; j < n2; ++j) {
                b = a[ia + i + j * n2];
                a[ia + i + j * n2] = a[ia + j + i * n2];
                a[ia + j + i * n2] = b;
            }
            return 0;
        }
        ncount = 2;
        k = (mn = n1 * n2) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n1 >= 3 && n2 >= 3) ncount += TOMS_gcd(n1 - 1, n2 - 1) - 1;
        i = 1;
        im = n1;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            b = a[ia + i1];
            i1c = kmi;
            c = a[ia + i1c];
            while (true) {
                i2 = n1 * i1 - k * (i1 / n2);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    d = b;
                    b = c;
                    c = d;
                    break;
                }
                a[ia + i1] = a[ia + i2];
                a[ia + i1c] = a[ia + i2c];
                i1 = i2;
                i1c = i2c;
            }
            a[ia + i1] = b;
            a[ia + i1c] = c;
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n1;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n1 * i1 - k * (i1 / n2);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int TOMS_gcd(int a, int b) {
        int r;
        do {
            r = a % b;
            a = b;
            b = r;
        } while (r != 0);
        return a;
    }

    private static int byte132a(byte[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        byte[] b, c;
        byte d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new byte[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new byte[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int short132a(short[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        short[] b, c;
        short d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new short[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new short[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int int132a(int[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        int[] b, c;
        int d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new int[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new int[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int long132a(long[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        long[] b, c;
        long d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new long[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new long[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int float132a(float[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        float[] b, c;
        float d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new float[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) {
                for (j = i + 1; j < n3; ++j) {
                    ij = ia + n1 * (i + j * n3);
                    ji = ia + n1 * (j + i * n3);
                    System.arraycopy(a, ij, b, 0, n1);
                    System.arraycopy(a, ji, a, ij, n1);
                    System.arraycopy(b, 0, a, ji, n1);
                }
            }
            return 0;
        }
        c = new float[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int double132a(double[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        double[] b, c;
        double d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new double[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new double[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int object132a(Object[] a, int ia, int n1, int n2, int n3, byte[] move, int move_size) {
        Object[] b, c;
        Object d;
        int i, j, k, ij, ji, im, mn, ncount;
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        b = new Object[n1];
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                System.arraycopy(a, ij, b, 0, n1);
                System.arraycopy(a, ji, a, ij, n1);
                System.arraycopy(b, 0, a, ji, n1);
            }
            return 0;
        }
        c = new Object[n1];
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            System.arraycopy(a, ia + n1 * i1, b, 0, n1);
            i1c = kmi;
            System.arraycopy(a, ia + n1 * i1c, c, 0, n1);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    for (int l = 0; l < n1; l++) {
                        d = b[l];
                        b[l] = c[l];
                        c[l] = d;
                    }
                    break;
                }
                System.arraycopy(a, ia + n1 * i2, a, ia + n1 * i1, n1);
                System.arraycopy(a, ia + n1 * i2c, a, ia + n1 * i1c, n1);
                i1 = i2;
                i1c = i2c;
            }
            System.arraycopy(b, 0, a, ia + n1 * i1, n1);
            System.arraycopy(c, 0, a, ia + n1 * i1c, n1);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }

    private static int backingArray132(IBackingArray a, long ia, int n1, int n2, int n3) {
        IBackingArray b, c, d;
        int i, j, k, im, mn, ncount;
        long ij, ji, n1l;
        int move_size = (n3 + n2) / 2;
        byte[] move = new byte[move_size];
        if (move == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 < 0 || n3 < 0) throw new IllegalArgumentException("n3,n2 < 0");
        if (n2 < 2 || n3 < 2) return 0;
        if (move_size < 1) throw new IllegalArgumentException("move_size < 1");
        n1l = n1;
        b = BackingArray.factory(a.getClassType(), n1, BackingArray.Type.JAVA_ARRAY);
        if (b == null) throw new OutOfMemoryError("Could not allocate work array");
        if (n2 == n3) {
            for (i = 0; i < n3; ++i) for (j = i + 1; j < n3; ++j) {
                ij = ia + n1 * (i + j * n3);
                ji = ia + n1 * (j + i * n3);
                BackingArray.arraycopy(a, ij, b, 0l, n1l);
                BackingArray.arraycopy(a, ji, a, ij, n1l);
                BackingArray.arraycopy(b, 0l, a, ji, n1l);
            }
            return 0;
        }
        c = BackingArray.factory(a.getClassType(), n1, BackingArray.Type.JAVA_ARRAY);
        if (c == null) {
            b = null;
            throw new OutOfMemoryError("Could not allocate work array");
        }
        ncount = 2;
        k = (mn = n2 * n3) - 1;
        for (i = 0; i < move_size; ++i) move[i] = 0;
        if (n2 >= 3 && n3 >= 3) ncount += TOMS_gcd(n2 - 1, n3 - 1) - 1;
        i = 1;
        im = n2;
        while (true) {
            int i1, i2, i1c, i2c;
            int kmi;
            i1 = i;
            kmi = k - i;
            ij = ia + n1 * i1;
            BackingArray.arraycopy(a, ij, b, 0l, n1l);
            i1c = kmi;
            ij = ia + n1 * i1c;
            BackingArray.arraycopy(a, ij, c, 0l, n1l);
            d = BackingArray.factory(a.getClassType(), n1, BackingArray.Type.JAVA_ARRAY);
            while (true) {
                i2 = n2 * i1 - k * (i1 / n3);
                i2c = k - i2;
                if (i1 < move_size) move[i1] = 1;
                if (i1c < move_size) move[i1c] = 1;
                ncount += 2;
                if (i2 == i) break;
                if (i2 == kmi) {
                    BackingArray.arraycopy(b, 0, d, 0, n1);
                    BackingArray.arraycopy(c, 0, b, 0, n1);
                    BackingArray.arraycopy(d, 0, c, 0, n1);
                    break;
                }
                ij = ia + n1 * i2;
                ji = ia + n1 * i1;
                BackingArray.arraycopy(a, ij, a, ji, n1l);
                ij = ia + n1 * i2c;
                ji = ia + n1 * i1c;
                BackingArray.arraycopy(a, ij, a, ji, n1l);
                i1 = i2;
                i1c = i2c;
            }
            ij = ia + n1 * i1;
            BackingArray.arraycopy(b, 0l, a, ij, n1l);
            ij = ia + n1 * i1c;
            BackingArray.arraycopy(c, 0l, a, ij, n1l);
            if (ncount >= mn) break;
            while (true) {
                int max;
                max = k - i;
                ++i;
                if (i > max) return i;
                im += n2;
                if (im > k) im -= k;
                i2 = im;
                if (i == i2) continue;
                if (i >= move_size) {
                    while (i2 > i && i2 < max) {
                        i1 = i2;
                        i2 = n2 * i1 - k * (i1 / n3);
                    }
                    if (i2 == i) break;
                } else if (move[i] == 0) break;
            }
        }
        return 0;
    }
}
