package yager.utils;

import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import yager.render.RenderAtom;
import yager.render.Shader;
import yager.render.TypeSortable;

/** Various methods that are reused often.
 * @author Ryan Hild
 */
public final class Utils {

    public static int increaseSize = 20;

    public static final Object[] checkSize(Object[] o, int newSize) {
        if (o.length >= newSize) return o;
        return changeSize(o, newSize + increaseSize);
    }

    public static final Object[] checkSize(Object[] o, int newSize, int increaseBy) {
        if (o.length >= newSize) return o;
        return changeSize(o, newSize + increaseBy);
    }

    public static final Object[] changeSize(Object[] o, int newSize) {
        if (o.length == newSize) return o;
        Object[] temp = (Object[]) java.lang.reflect.Array.newInstance(o.getClass().getComponentType(), newSize);
        if (o.length < newSize) System.arraycopy(o, 0, temp, 0, o.length); else System.arraycopy(o, 0, temp, 0, temp.length);
        return temp;
    }

    public static final float[] checkSize(float[] o, int newSize) {
        if (o.length >= newSize) return o;
        return changeSize(o, newSize + increaseSize);
    }

    public static final float[] checkSize(float[] o, int newSize, int increaseBy) {
        if (o.length >= newSize) return o;
        return changeSize(o, newSize + increaseBy);
    }

    public static final float[] changeSize(float[] o, int newSize) {
        if (o.length == newSize) return o;
        float[] temp = new float[newSize];
        if (o.length < newSize) System.arraycopy(o, 0, temp, 0, o.length); else System.arraycopy(o, 0, temp, 0, temp.length);
        return temp;
    }

    public static final int fromUnsignedByte(final byte b) {
        if (b < 0) return 0x80 + (b & 0x7F); else return b;
    }

    public static final int fromUnsignedShort(final short s) {
        if (s < 0) return 0x8000 + (s & 0x7FFF); else return s;
    }

    public static final long fromUnsignedInt(final int i) {
        if (i < 0) return 0x80000000 + (i & 0x7FFFFFFF); else return i;
    }

    public static final int toUnsignedInt(final long l) {
        int temp = 0;
        int and = 0x80000000;
        for (int i = 0; i < 32; ++i) {
            temp += l & and;
            and /= 2;
        }
        return temp;
    }

    public static final short toUnsignedShort(final int i) {
        short temp = 0;
        int and = 0x8000;
        for (int j = 0; j < 16; ++j) {
            temp += i & and;
            and /= 2;
        }
        return temp;
    }

    public static final byte toUnsignedByte(final short s) {
        byte temp = 0;
        int and = 0x80;
        for (int i = 0; i < 8; ++i) {
            temp += s & and;
            and /= 2;
        }
        return temp;
    }

    public static final int compare(Tuple3f a, Tuple3f b) {
        if (a == b) return 0;
        if (a.x > b.x) return 1;
        if (a.x < b.x) return -1;
        if (a.y > b.y) return 1;
        if (a.y < b.y) return -1;
        if (a.z > b.z) return 1;
        if (a.z < b.z) return -1;
        return 0;
    }

    public static final int compare(Tuple4f a, Tuple4f b) {
        if (a == b) return 0;
        if (a.w > b.w) return 1;
        if (a.w < b.w) return -1;
        if (a.x > b.x) return 1;
        if (a.x < b.x) return -1;
        if (a.y > b.y) return 1;
        if (a.y < b.y) return -1;
        if (a.z > b.z) return 1;
        if (a.z < b.z) return -1;
        return 0;
    }

    public static final int compare(Matrix4f m1, Matrix4f m2) {
        if (m1 == m2) return 0;
        if (m1.m00 > m2.m00) return 1;
        if (m1.m00 < m2.m00) return -1;
        if (m1.m01 > m2.m01) return 1;
        if (m1.m01 < m2.m01) return -1;
        if (m1.m02 > m2.m02) return 1;
        if (m1.m02 < m2.m02) return -1;
        if (m1.m03 > m2.m03) return 1;
        if (m1.m03 < m2.m03) return -1;
        if (m1.m10 > m2.m10) return 1;
        if (m1.m10 < m2.m10) return -1;
        if (m1.m11 > m2.m11) return 1;
        if (m1.m11 < m2.m11) return -1;
        if (m1.m12 > m2.m12) return 1;
        if (m1.m12 < m2.m12) return -1;
        if (m1.m13 > m2.m13) return 1;
        if (m1.m13 < m2.m13) return -1;
        if (m1.m20 > m2.m20) return 1;
        if (m1.m20 < m2.m20) return -1;
        if (m1.m21 > m2.m21) return 1;
        if (m1.m21 < m2.m21) return -1;
        if (m1.m22 > m2.m22) return 1;
        if (m1.m22 < m2.m22) return -1;
        if (m1.m23 > m2.m23) return 1;
        if (m1.m23 < m2.m23) return -1;
        if (m1.m30 > m2.m30) return 1;
        if (m1.m30 < m2.m30) return -1;
        if (m1.m31 > m2.m31) return 1;
        if (m1.m31 < m2.m31) return -1;
        if (m1.m32 > m2.m32) return 1;
        if (m1.m32 < m2.m32) return -1;
        if (m1.m33 > m2.m33) return 1;
        if (m1.m33 < m2.m33) return -1;
        return 0;
    }

    public static final int compare(TypeSortable a, TypeSortable b) {
        if (a.getType() < b.getType()) return -1;
        if (a.getType() > b.getType()) return 1;
        if (a.getIdentifier() < b.getIdentifier()) return -1;
        if (a.getIdentifier() > b.getIdentifier()) return 1;
        return 0;
    }

    public static final int compare(RenderAtom a, RenderAtom b) {
        if (a == b) return 0;
        Shader[] aShaders = a.getShaders();
        Shader[] bShaders = b.getShaders();
        if (aShaders.length < bShaders.length) return -1;
        if (aShaders.length > bShaders.length) return 1;
        for (int i = 0; i < aShaders.length; ++i) {
            Shader aShader = aShaders[i];
            Shader bShader = bShaders[i];
            int compare = Utils.compare(aShader, bShader);
            if (compare != 0) return compare;
        }
        return 0;
    }
}
