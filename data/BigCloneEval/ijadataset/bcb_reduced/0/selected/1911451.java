package gumbo.tech.math.util;

import gumbo.core.util.Quantitative;

/**
 * Constants and utilities related to graphical math operations.
 *
 * @author  Jon Barrilleaux (jonb@jmbaai.com) of JMB and Associates Inc.
 * @version $Revision: 1.1 $
 */
public class MathUtils {

    private MathUtils() {
    }

    /** Utilities based on type float. */
    public static final class F {

        /**
		 * "Standard" tolerance for testing approximate float value equality.
		 * Tolerance is one part per thousand.
		 */
        public static final float TOLERANCE = (float) 0.001;

        public static final float SQRT_TWO = (float) MathUtils.D.SQRT_TWO;

        public static final float SQRT_THREE = (float) MathUtils.D.SQRT_THREE;

        public static final float CBRT_THREE = (float) MathUtils.D.CBRT_THREE;

        public static final float PI = (float) MathUtils.D.PI;

        public static final float TWO_PI = (float) MathUtils.D.TWO_PI;

        public static final float FOUR_PI = (float) MathUtils.D.FOUR_PI;

        public static final float HALF_PI = (float) MathUtils.D.HALF_PI;

        public static final float QUARTER_PI = (float) MathUtils.D.QUARTER_PI;

        public static final float SQUARE_PI = (float) MathUtils.D.SQUARE_PI;

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(float[] in, float[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(Float[] in, float[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(float[] in, Float[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(Float[] in, Float[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(float[][] in, float[][] out) {
            if (in == out) return;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(Float[][] in, float[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(float[][] in, Float[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(Float[][] in, Float[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /**
		 * Returns the modulo of a value. The sign of the result is that of the
		 * modulus (divisor). This is unlike the Java modulus operator (%).
		 * @param value The value (dividend) to be converted.
		 * @param modulus The modulus (divisor) of the converted value.
		 * @return The result.
		 */
        public float mod(float value, float modulus) {
            float div = modulus / value;
            return value * (div - (int) div);
        }

        /**
		 * Returns the remainder of a value. The sign of the result is that of
		 * the value (dividend). This is like the Java modulus operator (%).
		 * @param value The value (dividend) to be converted.
		 * @param modulus The modulus (divisor) of the converted value.
		 * @return The result.
		 */
        public static float rem(float value, float modulus) {
            return value % modulus;
        }

        /**
		 * Computes the modulo of a value over a specified range, which includes
		 * the minimum value, but excludes the maximum value. If infinite or
		 * NaN, returns the range minimum.
		 * @param val The value.
		 * @param min Range minimum value.
		 * @param max Range maximum value.
		 * @return The result.
		 */
        public static float modFloor(float val, float min, float max) {
            if (Float.isInfinite(val) || Float.isNaN(val)) return min;
            float range = max - min;
            while (val >= max) val -= range;
            while (val < min) val += range;
            return val;
        }

        /**
		 * Computes the modulo of a value over a specified range, which includes
		 * the maximum value, but excludes the minimum value. If infinite or
		 * NaN, returns the range maximum.
		 * @param val The value.
		 * @param min Range minimum value.
		 * @param max Range maximum value.
		 * @return The result.
		 */
        public static float modCeil(float val, float min, float max) {
            if (Float.isInfinite(val) || Float.isNaN(val)) return max;
            float range = max - min;
            while (val > max) val -= range;
            while (val <= min) val += range;
            return val;
        }
    }

    /** Utilities based on type double. */
    public static final class D {

        public static final double SQRT_TWO = Math.pow(2.0, 1.0 / 2.0);

        public static final double SQRT_THREE = Math.pow(3.0, 1.0 / 2.0);

        public static final double CBRT_THREE = Math.pow(3.0, 1.0 / 3.0);

        public static final double PI = Math.PI;

        public static final double TWO_PI = Math.PI * 2.0;

        public static final double FOUR_PI = Math.PI * 4.0;

        public static final double HALF_PI = Math.PI / 2.0;

        public static final double QUARTER_PI = Math.PI / 4.0;

        public static final double SQUARE_PI = Math.PI * Math.PI;

        /**
		 * "Standard" tolerance for testing approximate double value equality.
		 * Tolerance is one part per million.
		 */
        public static final double TOLERANCE = Quantitative.Utils.TOLERANCE;

        /**
		 * Returns true if the two value are approximately equal, within plus or
		 * minus a standard tolerance (TOLERANCE), inclusive.
		 * @param valA Value A.
		 * @param valB Value B.
		 * @return The result.
		 */
        public static boolean equalsValue(double valA, double valB) {
            return Quantitative.Utils.equalsValue(valA, valB, TOLERANCE);
        }

        /**
		 * Returns true if the two value are approximately equal, within plus or
		 * minus a specified tolerance, inclusive.
		 * @param valA Value A.
		 * @param valB Value B.
		 * @param tolerance Magnitude of the tolerance (sign is ignored).
		 * @return The result. False if only one of the values is NaN;
		 * otherwise, true if tolerance is infinite. If tolerance is not
		 * infinite, false if only one of the values is infinite.
		 */
        public static boolean equalsValue(double valA, double valB, double tolerance) {
            return Quantitative.Utils.equalsValue(valA, valB, tolerance);
        }

        /**
		 * Returns the radian angle modulo 2*PI, with the sign being handled
		 * correctly (modRadian(-0.5*PI) = 1.5*PI).
		 * @param angle Input angle, in radians.
		 * @return The result.
		 */
        public static double modRadian(double angle) {
            if (angle >= 0.0 && angle < MathUtils.D.TWO_PI) {
                return angle;
            }
            return angle - (TWO_PI * Math.floor(angle / TWO_PI));
        }

        /**
		 * Returns the degree angle modulo 360, with the sign being handled
		 * correctly (modDegree(-90) = 270).
		 * @param angle Input angle, in degrees.
		 * @return The result.
		 */
        public static double modDegree(double angle) {
            if (angle >= 0.0 && angle < 360.0) {
                return angle;
            }
            return angle - (360.0 * Math.floor(angle / 360.0));
        }

        /** Sets an array to all zeros. */
        public static void clearArray(double[] val) {
            for (int valI = 0; valI < val.length; valI++) {
                val[valI] = 0.0;
            }
        }

        /** Sets an array to all zeros. */
        public static void clearArray(Double[] val) {
            for (int valI = 0; valI < val.length; valI++) {
                val[valI] = 0.0;
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(double[] in, double[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(Double[] in, double[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(double[] in, Double[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies an arbitrary length array from input to out. */
        public static void copyArray(Double[] in, Double[] out) {
            for (int valI = 0; valI < in.length; valI++) {
                out[valI] = in[valI];
            }
        }

        /** Copies a matrix (arbitrary 2D array) to an array, in row major form. */
        public static void matrixToArray(double[][] in, double[] out) {
            int eleI = 0;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[eleI++] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) to an array, in row major form. */
        public static void matrixToArray(Double[][] in, double[] out) {
            int eleI = 0;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[eleI++] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) to an array, in row major form. */
        public static void matrixToArray(double[][] in, Double[] out) {
            int eleI = 0;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[eleI++] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) to an array, in row major form. */
        public static void matrixToArray(Double[][] in, Double[] out) {
            int eleI = 0;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[eleI++] = in[rowI][colI];
                }
            }
        }

        /** Sets a matrix (arbitrary 2D array) to all zeros. */
        public static void clearMatrix(double[][] val) {
            int rowC = val.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = val[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    val[rowI][colI] = 0.0;
                }
            }
        }

        /** Sets a matrix (arbitrary 2D array) to all zeros. */
        public static void clearMatrix(Double[][] val) {
            int rowC = val.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = val[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    val[rowI][colI] = 0.0;
                }
            }
        }

        /**
		 * Sets a matrix (arbitrary 2D array) to an identity matrix (ones along
		 * the diagonal, zeros elsewhere).
		 */
        public static void identityMatrix(double[][] val) {
            int rowC = val.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = val[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    if (rowI == colI) val[rowI][colI] = 1.0; else val[rowI][colI] = 0.0;
                }
            }
        }

        /**
		 * Sets a matrix (arbitrary 2D array) to an identity matrix (ones along
		 * the diagonal, zeros elsewhere).
		 */
        public static void identityMatrix(Double[][] val) {
            int rowC = val.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = val[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    if (rowI == colI) val[rowI][colI] = 1.0; else val[rowI][colI] = 0.0;
                }
            }
        }

        /**
		 * Returns true if a matrix (square 2D array) represents an identity
		 * matrix (ones along the diagonal, zeros elsewhere).
		 */
        public static boolean isIdentityMatrix(double[][] val) {
            int rowC = val.length;
            for (int diagI = 0; diagI < rowC; diagI++) {
                if (val[diagI][diagI] != 1.0) return false;
            }
            for (int rowI = 0; rowI < rowC; rowI++) {
                for (int colI = 0; colI < rowC; colI++) {
                    if (colI == rowI) continue;
                    if (val[rowI][colI] != 0.0) return false;
                }
            }
            return true;
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(double[][] in, double[][] out) {
            if (in == out) return;
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(Double[][] in, double[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(double[][] in, Double[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies a matrix (arbitrary 2D array) from input to out. */
        public static void copyMatrix(Double[][] in, Double[][] out) {
            int rowC = in.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = in[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[rowI][colI];
                }
            }
        }

        /** Copies an array, in row major form, to a matrix (arbitrary 2D array). */
        public static void arrayToMatrix(double[] in, double[][] out) {
            int eleI = 0;
            int rowC = out.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = out[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[eleI++];
                }
            }
        }

        /** Copies an array, in row major form, to a matrix (arbitrary 2D array). */
        public static void arrayToMatrix(Double[] in, double[][] out) {
            int eleI = 0;
            int rowC = out.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = out[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[eleI++];
                }
            }
        }

        /** Copies an array, in row major form, to a matrix (arbitrary 2D array). */
        public static void arrayToMatrix(double[] in, Double[][] out) {
            int eleI = 0;
            int rowC = out.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = out[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[eleI++];
                }
            }
        }

        /** Copies an array, in row major form, to a matrix (arbitrary 2D array). */
        public static void arrayToMatrix(Double[] in, Double[][] out) {
            int eleI = 0;
            int rowC = out.length;
            for (int rowI = 0; rowI < rowC; rowI++) {
                int colC = out[rowI].length;
                for (int colI = 0; colI < colC; colI++) {
                    out[rowI][colI] = in[eleI++];
                }
            }
        }

        /** Sets the dimension values of a length 2 array. */
        public static void setTuple2(double x, double y, double[] out) {
            out[0] = x;
            out[1] = y;
        }

        /** Sets a length 2 array to all zeros. */
        public static void clearTuple2(double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
        }

        /** Sets a length 2 array to all zeros. */
        public static void clearTuple2(Double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
        }

        /** Copies the dimension values of a length 2 array. */
        public static void copyTuple2(double[] in, double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
        }

        /** Copies the dimension values of a length 2 array. */
        public static void copyTuple2(Double[] in, double[] out) {
            out[0] = in[0];
            out[1] = in[1];
        }

        /** Copies the dimension values of a length 2 array. */
        public static void copyTuple2(double[] in, Double[] out) {
            out[0] = in[0];
            out[1] = in[1];
        }

        /** Copies the dimension values of a length 2 array. */
        public static void copyTuple2(Double[] in, Double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
        }

        /** Sets the dimension values of a length 3 array. */
        public static void setTuple3(double x, double y, double z, double[] out) {
            out[0] = x;
            out[1] = y;
            out[2] = z;
        }

        /** Sets a length 3 array to all zeros. */
        public static void clearTuple3(double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
            out[2] = 0.0;
        }

        /** Sets a length 3 array to all zeros. */
        public static void clearTuple3(Double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
            out[2] = 0.0;
        }

        /** Copies the dimension values of a length 3 array. */
        public static void copyTuple3(double[] in, double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
        }

        /** Copies the dimension values of a length 3 array. */
        public static void copyTuple3(Double[] in, double[] out) {
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
        }

        /** Copies the dimension values of a length 3 array. */
        public static void copyTuple3(double[] in, Double[] out) {
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
        }

        /** Copies the dimension values of a length 3 array. */
        public static void copyTuple3(Double[] in, Double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
        }

        /** Sets the dimension values of a length 4 array. */
        public static void setTuple4(double x, double y, double z, double w, double[] out) {
            out[0] = x;
            out[1] = y;
            out[2] = z;
            out[3] = w;
        }

        /** Sets a length 4 array to all zeros. */
        public static void clearTuple4(double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
            out[2] = 0.0;
            out[3] = 0.0;
        }

        /** Sets a length 4 array to all zeros. */
        public static void clearTuple4(Double[] out) {
            out[0] = 0.0;
            out[1] = 0.0;
            out[2] = 0.0;
            out[3] = 0.0;
        }

        /** Copies the dimension values of a length 4 array. */
        public static void copyTuple4(double[] in, double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
            out[3] = in[3];
        }

        /** Copies the dimension values of a length 4 array. */
        public static void copyTuple4(Double[] in, double[] out) {
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
            out[3] = in[3];
        }

        /** Copies the dimension values of a length 4 array. */
        public static void copyTuple4(double[] in, Double[] out) {
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
            out[3] = in[3];
        }

        /** Copies the dimension values of a length 4 array. */
        public static void copyTuple4(Double[] in, Double[] out) {
            if (in == out) return;
            out[0] = in[0];
            out[1] = in[1];
            out[2] = in[2];
            out[3] = in[3];
        }

        /**
		 * Returns the modulo of a value. The sign of the result is that of the
		 * modulus (divisor). This is unlike the Java modulus operator (%).
		 * @param value The value (dividend) to be converted.
		 * @param modulus The modulus (divisor) of the converted value.
		 * @return The result.
		 */
        public static double mod(double value, double modulus) {
            double div = modulus / value;
            return value * (div - (int) div);
        }

        /**
		 * Returns the remainder of a value. The sign of the result is that of
		 * the value (dividend). This is like the Java modulus operator (%).
		 * @param value The value (dividend) to be converted.
		 * @param modulus The modulus (divisor) of the converted value.
		 * @return The result.
		 */
        public static double rem(double value, double modulus) {
            return value % modulus;
        }

        /**
		 * Computes the modulo of a value over a specified range, which includes
		 * the minimum value, but excludes the maximum value. If infinite or
		 * NaN, returns the range minimum.
		 * @param val The value.
		 * @param min Range minimum value.
		 * @param max Range maximum value.
		 * @return The result.
		 */
        public static double modFloor(double val, double min, double max) {
            if (Double.isInfinite(val) || Double.isNaN(val)) return min;
            double range = max - min;
            while (val >= max) val -= range;
            while (val < min) val += range;
            return val;
        }

        /**
		 * Computes the modulo of a value over a specified range, which includes
		 * the maximum value, but excludes the minimum value. If infinite or
		 * NaN, returns the range maximum.
		 * @param val The value.
		 * @param min Range minimum value.
		 * @param max Range maximum value.
		 * @return The result.
		 */
        public static double modCeil(double val, double min, double max) {
            if (Double.isInfinite(val) || Double.isNaN(val)) return max;
            double range = max - min;
            while (val > max) val -= range;
            while (val <= min) val += range;
            return val;
        }

        /**
		 * Result of normalizing a zero length tuple, which is a normalized
		 * vector along the X-axis.
		 * <P>
		 * Note that an array can't be used since an array is not immutable.
		 */
        public static final Double NORMALIZED_ZERO_TUPLE2_X = 1.0;

        public static final Double NORMALIZED_ZERO_TUPLE2_Y = 0.0;

        /**
		 * Normalizes the value in length 2 array.  If the computed vector
		 * length is zero, returns the value NORMALIZED_ZERO_TUPLE2.
		 * @param in Input value.  Never null.
		 * @param out Return value.  Never null.  Can be the same as in.
		 */
        public static double[] normalizeTuple2(double[] in, double[] out) {
            double x = in[0];
            double y = in[1];
            if (x == 0.0 && y == 0.0) {
                out[0] = NORMALIZED_ZERO_TUPLE2_X;
                out[1] = NORMALIZED_ZERO_TUPLE2_Y;
            } else {
                double length = Math.sqrt(x * x + y * y);
                if (length == 0.0) {
                    out[0] = NORMALIZED_ZERO_TUPLE2_X;
                    out[1] = NORMALIZED_ZERO_TUPLE2_Y;
                } else {
                    double lengthInv = 1.0 / length;
                    out[0] = lengthInv * x;
                    out[1] = lengthInv * y;
                }
            }
            return out;
        }

        /**
		 * Result of normalizing a zero length tuple, which is a normalized
		 * vector along the X-axis.
		 * <P>
		 * Note that an array can't be used since an array is not immutable.
		 */
        public static final Double NORMALIZED_ZERO_TUPLE3_X = 1.0;

        public static final Double NORMALIZED_ZERO_TUPLE3_Y = 0.0;

        public static final Double NORMALIZED_ZERO_TUPLE3_Z = 0.0;

        /**
		 * Normalizes the value in length 3 array.  If the computed vector
		 * length is zero, returns the value NORMALIZED_ZERO_TUPLE3.
		 * @param in Input value.  Never null.
		 * @param out Return value.  Never null.  Can be the same as in.
		 */
        public static double[] normalizeTuple3(double[] in, double[] out) {
            double x = in[0];
            double y = in[1];
            double z = in[2];
            if (x == 0.0 && y == 0.0 && z == 0.0) {
                out[0] = NORMALIZED_ZERO_TUPLE3_X;
                out[1] = NORMALIZED_ZERO_TUPLE3_Y;
                out[2] = NORMALIZED_ZERO_TUPLE3_Z;
            } else {
                double length = Math.sqrt(x * x + y * y + z * z);
                if (length == 0.0) {
                    out[0] = NORMALIZED_ZERO_TUPLE3_X;
                    out[1] = NORMALIZED_ZERO_TUPLE3_Y;
                    out[2] = NORMALIZED_ZERO_TUPLE3_Z;
                } else {
                    double lengthInv = 1.0 / length;
                    out[0] = lengthInv * x;
                    out[1] = lengthInv * y;
                    out[2] = lengthInv * z;
                }
            }
            return out;
        }

        /**
		 * Transposes an input 3x3 matrix.
		 * @param in Input matrix.  Never null.
		 * @param out Output matrix.  Never null.  Can be the same as {@code in}.
		 */
        public static void transpose3(double[][] in, double[][] out) {
            if (D.isIdentityMatrix(in)) {
                if (in != out) D.identityMatrix(out);
                return;
            }
            double[][] copyIn;
            if (in == out) {
                D.copyMatrix(in, _dummyMatrix);
                copyIn = _dummyMatrix;
            } else {
                copyIn = in;
            }
            for (int rowI = 0; rowI < 3; rowI++) {
                for (int colI = 0; colI < 3; colI++) {
                    out[colI][rowI] = copyIn[rowI][colI];
                }
            }
        }

        /**
		 * Transposes an input 4x4 matrix.
		 * @param in Input matrix.  Never null.
		 * @param out Output matrix.  Never null.  Can be the same as {@code in}.
		 */
        public static void transpose4(double[][] in, double[][] out) {
            if (D.isIdentityMatrix(in)) {
                if (in != out) D.identityMatrix(out);
                return;
            }
            double[][] copyIn;
            if (in == out) {
                D.copyMatrix(in, _dummyMatrix);
                copyIn = _dummyMatrix;
            } else {
                copyIn = in;
            }
            for (int rowI = 0; rowI < 4; rowI++) {
                for (int colI = 0; colI < 4; colI++) {
                    out[colI][rowI] = copyIn[rowI][colI];
                }
            }
        }

        /**
		 * Inverts an input 3x3 matrix.
		 * @param in Input matrix.  Never null.
		 * @param out Output matrix.  Never null.  Can be the same as {@code in}.
		 * @throws IllegalStateException if the input matrix is singular.
		 */
        public static void invert3(double[][] in, double[][] out) {
            if (D.isIdentityMatrix(in)) {
                if (in != out) D.identityMatrix(out);
                return;
            }
            D.copyMatrix(in, _dummyMatrix);
            double[][] copyIn = _dummyMatrix;
            D.identityMatrix(out);
            for (int i = 0; i < 3; i++) {
                double alpha = copyIn[i][i];
                if (alpha == 0.0) {
                    throw new IllegalStateException("Matrix is singular.  in=" + toStringMatrix4(in));
                }
                for (int j = 0; j < 3; j++) {
                    copyIn[i][j] = copyIn[i][j] / alpha;
                    out[i][j] = out[i][j] / alpha;
                }
                for (int k = 0; k < 3; k++) {
                    if ((k - i) != 0) {
                        double beta = copyIn[k][i];
                        for (int j = 0; j < 3; j++) {
                            copyIn[k][j] -= beta * copyIn[i][j];
                            out[k][j] -= beta * out[i][j];
                        }
                    }
                }
            }
        }

        /**
		 * Inverts an input 4x4 matrix.
		 * @param in Input matrix.  Never null.
		 * @param out Output matrix.  Never null.  Can be the same as {@code in}.
		 * @throws IllegalStateException if the input matrix is singular.
		 */
        public static void invert4(double[][] in, double[][] out) {
            if (D.isIdentityMatrix(in)) {
                if (in != out) D.identityMatrix(out);
                return;
            }
            D.copyMatrix(in, _dummyMatrix);
            double[][] copyIn = _dummyMatrix;
            D.identityMatrix(out);
            for (int i = 0; i < 4; i++) {
                double alpha = copyIn[i][i];
                if (alpha == 0.0) {
                    throw new IllegalStateException("Matrix is singular.  in=" + toStringMatrix4(in));
                }
                for (int j = 0; j < 4; j++) {
                    copyIn[i][j] = copyIn[i][j] / alpha;
                    out[i][j] = out[i][j] / alpha;
                }
                for (int k = 0; k < 4; k++) {
                    if ((k - i) != 0) {
                        double beta = copyIn[k][i];
                        for (int j = 0; j < 4; j++) {
                            copyIn[k][j] -= beta * copyIn[i][j];
                            out[k][j] -= beta * out[i][j];
                        }
                    }
                }
            }
        }

        /**
		 * Returns a string with the element values of a 3x3 matrix.
		 * @param matrix Input matrix.  Never null.
		 */
        public static String toStringMatrix3(double[][] matrix) {
            return "[" + "[" + matrix[0][0] + " " + matrix[0][1] + " " + matrix[0][2] + "]" + "[" + matrix[1][0] + " " + matrix[1][1] + " " + matrix[1][2] + "]" + "[" + matrix[2][0] + " " + matrix[2][1] + " " + matrix[2][2] + "]" + "]";
        }

        /**
		 * Returns a string with the element values of a 4x4 matrix.
		 * @param matrix Input matrix.  Never null.
		 */
        public static String toStringMatrix4(double[][] matrix) {
            return "[" + "[" + matrix[0][0] + " " + matrix[0][1] + " " + matrix[0][2] + " " + matrix[0][3] + "]" + "[" + matrix[1][0] + " " + matrix[1][1] + " " + matrix[1][2] + " " + matrix[1][3] + "]" + "[" + matrix[2][0] + " " + matrix[2][1] + " " + matrix[2][2] + " " + matrix[2][3] + "]" + "[" + matrix[3][0] + " " + matrix[3][1] + " " + matrix[3][2] + " " + matrix[3][3] + "]" + "]";
        }

        private static double[][] _dummyMatrix = new double[4][4];
    }
}
