package com.yerihyo.yeritools.math;

import java.awt.Rectangle;
import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.collections.CountMap;
import com.yerihyo.yeritools.collections.MapToolkit;
import com.yerihyo.yeritools.collections.CollectionsToolkit.NumericalOperator;
import com.yerihyo.yeritools.collections.CollectionsToolkit.ScoreWrapper;
import com.yerihyo.yeritools.collections.MapToolkit.MapValueMap;
import com.yerihyo.yeritools.debug.YeriDebug;
import com.yerihyo.yeritools.swing.SwingToolkit.ValueCustomizedListCellRenderer;
import com.yerihyo.yeritools.text.StringToolkit;

public class StatisticsToolkit {

    public static class PercentRenderer extends ValueCustomizedListCellRenderer {

        private PercentRenderer(ListCellRenderer renderer) {
            super(renderer);
        }

        public static PercentRenderer create(ListCellRenderer originalRenderer) {
            PercentRenderer renderer = new PercentRenderer(originalRenderer);
            return renderer;
        }

        @Override
        protected String getText(Object value) {
            if (value == null) {
                return "no item selected";
            }
            if (!(value instanceof Number)) {
                throw new UnsupportedOperationException();
            }
            Number number = (Number) value;
            double doubleValue = number.doubleValue();
            return (doubleValue * 100) + "%";
        }
    }

    public static ChoiceFormat createChoiceFormat(String[] labelArray) {
        return createChoiceFormat(labelArray, 0);
    }

    public static ChoiceFormat createChoiceFormat(String[] labelArray, int startNumber) {
        double[] keyArray = new double[labelArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            keyArray[i] = i + startNumber;
        }
        return new ChoiceFormat(keyArray, labelArray);
    }

    public static ChoiceFormat createChoiceFormat(Map<? extends Number, ? extends CharSequence> map) {
        Set<? extends Number> keySet = map.keySet();
        keySet.remove(null);
        double[] keyArray = StatisticsToolkit.toDoubleArray(keySet);
        String[] valueArray = new String[keyArray.length];
        for (int i = 0; i < keyArray.length; i++) {
            CharSequence cs = map.get(keyArray[i]);
            valueArray[i] = cs == null ? null : cs.toString();
        }
        return new ChoiceFormat(keyArray, valueArray);
    }

    public static void main(String[] args) {
        test04();
    }

    protected static void test03() {
        System.out.println(0.0 / 0.0);
    }

    protected static void test01() {
        Rectangle r = new Rectangle(3, 4, 5, 6);
        Rectangle[] unavaiableRectangleArray = new Rectangle[] { new Rectangle(0, 0, 4, 5), new Rectangle(5, 6, 1, 2), new Rectangle(6, 3, 3, 3), new Rectangle(2, 7, 3, 2), new Rectangle(6, 7, 3, 3), new Rectangle(5, 7, 1, 2) };
        boolean[][] availPosBox = getAvailPositionBooleanBox(r, unavaiableRectangleArray);
        System.out.println(CollectionsToolkit.toString(availPosBox, new char[] { '@', '.' }));
    }

    public static void test02() {
        double[] ds = { 0.968965517241379, 0.975862068965517, 0.972413793103448, 0.989655172413793, 0.993127147766323 };
        System.out.println(stddev(ds));
    }

    /**
	 * i goes bottom
	 * j goes right
	 * 
	 * @param r
	 * @param unavaiableRectangleArray
	 * @return
	 */
    protected static boolean[][] getAvailPositionBooleanBox(Rectangle r, Rectangle[] unavaiableRectangleArray) {
        boolean[][] box = new boolean[r.width][r.height];
        CollectionsToolkit.fill(box, true);
        for (Rectangle c : unavaiableRectangleArray) {
            int startX = c.x - r.x;
            int startY = c.y - r.y;
            for (int i = Math.max(startX, 0); i < Math.min(startX + c.width, box.length); i++) {
                for (int j = Math.max(startY, 0); j < Math.min(startY + c.height, box[i].length); j++) {
                    box[i][j] = false;
                }
            }
        }
        return box;
    }

    public static boolean[][] transpose(boolean[][] booleanBox) {
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(booleanBox));
        boolean[][] returnBox = new boolean[booleanBox[0].length][booleanBox.length];
        for (int i = 0; i < booleanBox.length; i++) {
            for (int j = 0; j < booleanBox[i].length; j++) {
                returnBox[j][i] = booleanBox[i][j];
            }
        }
        return returnBox;
    }

    public static double[][] transpose(double[][] doubleBox) {
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(doubleBox));
        double[][] returnBox = new double[doubleBox[0].length][doubleBox.length];
        for (int i = 0; i < doubleBox.length; i++) {
            for (int j = 0; j < doubleBox[i].length; j++) {
                returnBox[j][i] = doubleBox[i][j];
            }
        }
        return returnBox;
    }

    public static int[] indexAsValueIntArray(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = i;
        }
        return array;
    }

    public static double pnorm(double[] v, int p) {
        YeriDebug.ASSERT(p > 0);
        double sum = 0;
        for (double d : v) {
            sum += Math.pow(Math.abs(d), p);
        }
        return Math.pow(sum, 1 / p);
    }

    public static class NumberTextValueComparator implements Comparator<CharSequence> {

        @Override
        public int compare(CharSequence c1, CharSequence c2) {
            double arg1 = Double.parseDouble(c1.toString());
            double arg2 = Double.parseDouble(c2.toString());
            return NumberValueComparator.compareStatic(arg1, arg2);
        }
    }

    public static class NumberValueComparator implements Comparator<Number> {

        @Override
        public int compare(Number arg1, Number arg2) {
            return compareStatic(arg1, arg2);
        }

        public static int compareStatic(Number arg1, Number arg2) {
            double d1 = arg1.doubleValue();
            double d2 = arg2.doubleValue();
            double diff = d1 - d2;
            int result = diff < 0 ? -1 : (diff == 0 ? 0 : 1);
            return result;
        }
    }

    public static double vectorCosine(final Map<String, ? extends Number> map1, final Map<String, ? extends Number> map2) {
        Collection<? extends Number> values1 = map1.values();
        double[] v1 = toDoubleArray(values1);
        double v1NormSquare = innerProduct(v1, v1);
        Collection<? extends Number> values2 = map2.values();
        double[] v2 = toDoubleArray(values2);
        double v2NormSquare = innerProduct(v2, v2);
        Set<String> keys1 = map1.keySet();
        Set<String> keys2 = map2.keySet();
        Set<String> union = new HashSet<String>();
        union.addAll(keys1);
        union.addAll(keys2);
        double innerProduct = 0;
        for (String key : union) {
            Number number1 = map1.get(key);
            Number number2 = map2.get(key);
            if (number1 == null || number2 == null) {
                continue;
            }
            innerProduct += number1.doubleValue() * number2.doubleValue();
        }
        double returnValue = innerProduct / Math.sqrt(v1NormSquare * v2NormSquare);
        return returnValue;
    }

    public static boolean isRectangular(double[][] a) {
        if (a.length == 0) {
            return true;
        }
        int length = a[0].length;
        for (int i = 1; i < a.length; i++) {
            double[] x = a[i];
            if (x.length != length) {
                return false;
            }
        }
        return true;
    }

    public static double[] toDoubleArray(Collection<? extends Number> v) {
        double[] returnValue = new double[v.size()];
        Iterator<? extends Number> iterator = v.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Number number = iterator.next();
            returnValue[i] = number.doubleValue();
        }
        return returnValue;
    }

    public static double innerProduct(double[] v1, double[] v2) {
        YeriDebug.ASSERT(v1.length != 0, "Null Vector");
        YeriDebug.ASSERT(v2.length != 0, "Null Vector");
        YeriDebug.ASSERT_compareInteger(v1.length, v2.length);
        double result = 0;
        for (int i = 0; i < v1.length; i++) {
            result += v1[i] * v2[i];
        }
        return result;
    }

    public static double innerProduct(Collection<? extends Number> v1, Collection<? extends Number> v2) {
        YeriDebug.ASSERT(v1.size() != 0, "Null Vector");
        YeriDebug.ASSERT(v2.size() != 0, "Null Vector");
        YeriDebug.ASSERT_compareInteger(v1.size(), v2.size());
        double result = 0;
        Iterator<? extends Number> v1Iterator = v1.iterator();
        Iterator<? extends Number> v2Iterator = v2.iterator();
        for (int i = 0; i < v1.size(); i++) {
            result += v1Iterator.next().doubleValue() * v2Iterator.next().doubleValue();
        }
        return result;
    }

    public static double getCovariance(Collection<? extends Number> v1Collection, Collection<? extends Number> v2Collection) {
        YeriDebug.ASSERT(v1Collection.size() != 0, "Null Vector");
        YeriDebug.ASSERT(v2Collection.size() != 0, "Null Vector");
        YeriDebug.ASSERT_compareInteger(v1Collection.size(), v2Collection.size());
        double v1Mean = getAverage(v1Collection);
        double v2Mean = getAverage(v2Collection);
        double result = 0;
        Iterator<? extends Number> v1Iterator = v1Collection.iterator();
        Iterator<? extends Number> v2Iterator = v2Collection.iterator();
        for (int i = 0; i < v1Collection.size(); i++) {
            double v1 = v1Iterator.next().doubleValue();
            double v2 = v2Iterator.next().doubleValue();
            result += (v1 - v1Mean) * (v2 - v2Mean);
        }
        return result;
    }

    public static double getCorrelation(Collection<? extends Number> v1Collection, Collection<? extends Number> v2Collection) {
        double covariance = getCovariance(v1Collection, v2Collection);
        double returnValue = covariance / (stddev(v1Collection) * stddev(v2Collection));
        return returnValue;
    }

    public static double logInnerProduct(double[] logV1, double[] logV2) {
        YeriDebug.ASSERT(logV1.length != 0, "Null Vector");
        YeriDebug.ASSERT(logV2.length != 0, "Null Vector");
        YeriDebug.ASSERT_compareInteger(logV1.length, logV2.length);
        double logResult = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < logV1.length; i++) {
            double logResultItem = StatisticsToolkit.logMultiply(logV1[i], logV2[i]);
            logResult = StatisticsToolkit.logAdd(logResult, logResultItem);
        }
        return logResult;
    }

    public static double[] multiplyMatrixToVector(double[][] a, double[] v) {
        YeriDebug.ASSERT(v.length != 0, "Null Vector");
        YeriDebug.ASSERT(a.length != 0, "Null Matrix");
        YeriDebug.ASSERT(isRectangular(a), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(a[0].length, v.length);
        double[] result = new double[a.length];
        Arrays.fill(result, 0);
        for (int i = 0; i < a.length; i++) {
            result[i] = innerProduct(a[i], v);
        }
        return result;
    }

    public static double[] multiplyLogMatrixToLogVector(double[][] logA, double[] logV) {
        YeriDebug.ASSERT(logV.length != 0, "Null Vector");
        YeriDebug.ASSERT(logA.length != 0, "Null Matrix");
        YeriDebug.ASSERT(isRectangular(logA), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(logA[0].length, logV.length);
        double[] logResult = new double[logA.length];
        for (int i = 0; i < logA.length; i++) {
            logResult[i] = logInnerProduct(logA[i], logV);
        }
        return logResult;
    }

    public static double[] multiplyVectorToMatrix(double[] v, double[][] a) {
        YeriDebug.ASSERT(v.length != 0, "Null Vector");
        YeriDebug.ASSERT(a.length != 0, "Null Matrix");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(a), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(v.length, a.length);
        double[] result = new double[v.length];
        Arrays.fill(result, 0);
        for (int j = 0; j < a[0].length; j++) {
            for (int i = 0; i < v.length; i++) {
                result[j] += v[i] * a[i][j];
            }
        }
        return result;
    }

    public static class ViterbiElement implements Comparable<ViterbiElement> {

        private double score;

        private int backPointer;

        public ViterbiElement() {
            this(Double.NEGATIVE_INFINITY, -1);
        }

        public ViterbiElement(double score, int backPointer) {
            this.score = score;
            this.backPointer = backPointer;
        }

        public double getScore() {
            return score;
        }

        public int getBackPointer() {
            return backPointer;
        }

        public void update(double d, int i) {
            if (d <= score) {
                return;
            }
            score = d;
            backPointer = i;
        }

        public static String getDescription(ViterbiElement[] viterbiElementArray) {
            StringBuffer buffer = new StringBuffer();
            buffer.append('[');
            for (ViterbiElement viterbiElement : viterbiElementArray) {
                buffer.append(viterbiElement.getDescription()).append(',');
            }
            buffer.append(']');
            return buffer.toString();
        }

        public String getDescription() {
            return "(" + score + "," + backPointer + ")";
        }

        public String toString() {
            return this.getDescription();
        }

        @Override
        public int compareTo(ViterbiElement arg0) {
            double diff = this.getScore() - arg0.getScore();
            return (diff < 0) ? -1 : (diff == 0 ? 0 : 1);
        }
    }

    public static ViterbiElement[] getLogViterbi(ViterbiElement[] logV, double[][] logA) {
        YeriDebug.ASSERT(logV.length != 0, "Null Vector");
        YeriDebug.ASSERT(logA.length != 0, "Null Matrix");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(logA), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(logV.length, logA.length);
        ViterbiElement[] logResult = new ViterbiElement[logV.length];
        for (int j = 0; j < logA[0].length; j++) {
            double[] pathScoreArray = new double[logV.length];
            for (int i = 0; i < logV.length; i++) {
                pathScoreArray[i] = StatisticsToolkit.logMultiply(logV[i].getScore(), logA[i][j]);
            }
            ViterbiElement viterbiElement = StatisticsToolkit.getViterbiMax(pathScoreArray);
            logResult[j] = viterbiElement;
        }
        return logResult;
    }

    private static ViterbiElement getViterbiMax(double[] pathLogScoreArray) {
        ViterbiElement viterbiElement = new ViterbiElement();
        for (int i = 0; i < pathLogScoreArray.length; i++) {
            viterbiElement.update(pathLogScoreArray[i], i);
        }
        return viterbiElement;
    }

    public static double[] multiplyLogVectorToLogMatrix(double[] logV, double[][] logA) {
        YeriDebug.ASSERT(logV.length != 0, "Null Vector");
        YeriDebug.ASSERT(logA.length != 0, "Null Matrix");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(logA), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(logV.length, logA.length);
        double[] logResult = new double[logV.length];
        Arrays.fill(logResult, Double.NEGATIVE_INFINITY);
        for (int j = 0; j < logA[0].length; j++) {
            for (int i = 0; i < logV.length; i++) {
                double resultItem = StatisticsToolkit.logMultiply(logV[i], logA[i][j]);
                logResult[j] = StatisticsToolkit.logAdd(logResult[j], resultItem);
            }
        }
        return logResult;
    }

    public static double logCalculate(double lp1, double lp2, NumericalOperator numericalOperator) {
        if (numericalOperator == NumericalOperator.ADD) {
            return logAdd(lp1, lp2);
        } else if (numericalOperator == NumericalOperator.SUBTRACT) {
            return logSubtract(lp1, lp2);
        } else if (numericalOperator == NumericalOperator.MULTIPLY) {
            return logMultiply(lp1, lp2);
        } else if (numericalOperator == NumericalOperator.DIVIDE) {
            return logDivide(lp1, lp2);
        } else {
            throw new RuntimeException("Illegal Operator!");
        }
    }

    public static double logAdd(double lp1, double lp2) {
        if (lp1 >= lp2) {
            return lp1 + Math.log1p(Math.exp(lp2 - lp1));
        } else {
            return lp2 + Math.log1p(Math.exp(lp1 - lp2));
        }
    }

    public static double logSubtract(double lp1, double lp2) {
        if (lp1 > lp2) {
            return lp1 + Math.log(1 - Math.exp(lp2 - lp1));
        } else {
            return Double.NaN;
        }
    }

    public static double logMultiply(double lp1, double lp2) {
        return lp1 + lp2;
    }

    public static double logDivide(double lp1, double lp2) {
        return lp1 - lp2;
    }

    public static double[][] multiplyMatrix(double[][] a, double[][] b) {
        YeriDebug.ASSERT(a.length != 0, "Null Matrix");
        YeriDebug.ASSERT(b.length != 0, "Null Matrix");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(a), "Matrix is not rectangular!");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(b), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(a[0].length, b.length);
        double[][] result = new double[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            Arrays.fill(result[i], 0);
            for (int k = 0; k < b[0].length; k++) {
                for (int j = 0; j < b.length; j++) {
                    result[i][k] += a[i][j] * b[j][k];
                }
            }
        }
        return result;
    }

    public static double[][] multiplyLogMatrix(double[][] logA, double[][] logB) {
        YeriDebug.ASSERT(logA.length != 0, "Null Matrix");
        YeriDebug.ASSERT(logB.length != 0, "Null Matrix");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(logA), "Matrix is not rectangular!");
        YeriDebug.ASSERT(CollectionsToolkit.isRectangular(logB), "Matrix is not rectangular!");
        YeriDebug.ASSERT_compareInteger(logA[0].length, logB.length);
        double[][] logResult = new double[logA.length][logB[0].length];
        for (int i = 0; i < logA.length; i++) {
            Arrays.fill(logResult[i], Double.NEGATIVE_INFINITY);
            for (int k = 0; k < logB[0].length; k++) {
                for (int j = 0; j < logB.length; j++) {
                    double logResultItem = logMultiply(logA[i][j], logB[j][k]);
                    logResult[i][k] = StatisticsToolkit.logAdd(logResult[i][k], logResultItem);
                }
            }
        }
        return logResult;
    }

    public static enum PredicateQuantifier {

        ALL, EXIST;

        public boolean getContinueSearchingValue() {
            if (this == ALL) {
                return true;
            } else {
                return false;
            }
        }

        public boolean getStopSearchingValue() {
            return !this.getContinueSearchingValue();
        }
    }

    /** TODO - need to consider overflow when negative logDigit * */
    public static double round(double d, int logDigit) {
        double digit = Math.pow(10, logDigit);
        int dRoundToZeroDigit = (int) (d / digit);
        return dRoundToZeroDigit * digit;
    }

    public static String getRoundedString(double d, int logDigit) {
        StringBuffer pattern = new StringBuffer("0.");
        for (int i = 0; i > logDigit; i--) {
            pattern.append('0');
        }
        DecimalFormat numberFormat = new DecimalFormat(pattern.toString());
        return numberFormat.format(d);
    }

    public static int getCompareValue(double diff) {
        return (diff < 0) ? -1 : (diff == 0 ? 0 : 1);
    }

    public static int sqrtInt(int value) {
        return (int) Math.sqrt(value);
    }

    public static double getProportion(double min, double max, double value) {
        return (value - min) / (max - min);
    }

    public static double getValue(double min, double max, double proportion) {
        return (max - min) * proportion + min;
    }

    public static double[] getMinMax(List<? extends String> values) {
        double[] minmax = new double[2];
        for (int i = 0; ; i++) {
            String value = values.get(i);
            try {
                minmax[0] = Double.parseDouble(value);
                break;
            } catch (NumberFormatException ex) {
            } catch (NullPointerException ex) {
            }
        }
        for (int i = values.size() - 1; ; i--) {
            String value = values.get(i);
            try {
                minmax[1] = Double.parseDouble(value);
                break;
            } catch (NumberFormatException ex) {
            } catch (NullPointerException ex) {
            }
        }
        return minmax;
    }

    public static double[] indexAsValueDoubleArray(int length) {
        double[] array = new double[length];
        for (int i = 0; i < length; i++) {
            array[i] = i;
        }
        return array;
    }

    public static class YeriDigitOverflowException extends RuntimeException {

        public static final long serialVersionUID = 1;

        public YeriDigitOverflowException() {
            super();
        }

        public YeriDigitOverflowException(String message) {
            super(message);
        }

        public YeriDigitOverflowException(String message, Throwable cause) {
            super(cause);
        }

        public YeriDigitOverflowException(Throwable cause) {
            super(cause);
        }

        public YeriDigitOverflowException(int number, int digit) {
            this("Number: " + number + ", Digit: " + digit);
        }
    }

    public static int getDigitCount(double d) {
        double logValue = Math.log10(d);
        return ((int) Math.floor(logValue)) + 1;
    }

    public static double getSmallestNumberOfSameDigit(double d) {
        return getSmallestNumberOfDigit(getDigitCount(d));
    }

    public static int getSmallestNumberOfDigit(int digitCount) {
        return (int) Math.pow(10, digitCount - 1);
    }

    public static int getHeadNumber(double d) {
        double smallestNumberOfSameDigit = getSmallestNumberOfSameDigit(d);
        int headNumber = (int) Math.floor(d / smallestNumberOfSameDigit);
        return headNumber;
    }

    public static int getTickNumber(double diff) {
        int digitCount = getDigitCount(diff);
        int smallestNumberOfDigit = getSmallestNumberOfDigit(digitCount);
        int headNumber = getHeadNumber(diff);
        int tickNumber;
        if (smallestNumberOfDigit == 1) {
            return 1;
        }
        switch(headNumber) {
            case 1:
                tickNumber = smallestNumberOfDigit / 10;
                break;
            case 2:
            case 3:
                tickNumber = smallestNumberOfDigit / 5;
                break;
            case 4:
            case 5:
            case 6:
                tickNumber = smallestNumberOfDigit / 2;
                break;
            case 7:
            case 8:
            case 9:
                tickNumber = smallestNumberOfDigit;
                break;
            default:
                throw new RuntimeException("This should not happen! value=" + headNumber);
        }
        return tickNumber;
    }

    public static String makeDigitedNumberString(int number, int digit) throws YeriDigitOverflowException {
        StringBuffer returnString = new StringBuffer();
        int current;
        if (number < 0) throw new YeriDigitOverflowException(number, digit);
        for (int i = 0; i < digit; i++) {
            current = number % 10;
            returnString.insert(0, current);
            number /= 10;
        }
        return returnString.toString();
    }

    public static int findGCF(int a, int b) {
        int absA = Math.abs(a);
        int absB = Math.abs(b);
        int big = absA >= absB ? absA : absB;
        int small = absB <= absA ? absB : absA;
        int r = small;
        if (small <= 0) {
            throw new IllegalArgumentException("All arguments must be >0. Not " + small);
        }
        while (r > 0) {
            r = big % small;
            big = small;
            small = r;
        }
        return big;
    }

    public static int[] getMinMaxIndex(short[] numbers) {
        int[] minmaxIndex = new int[2];
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < numbers.length; i++) {
            int number = numbers[i];
            if (number < min) {
                minmaxIndex[0] = i;
                min = number;
            }
            if (number > max) {
                minmaxIndex[1] = i;
                max = number;
            }
        }
        return minmaxIndex;
    }

    public static int[] getMinMaxIndex(int[] numbers) {
        int[] minmaxIndex = new int[2];
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < numbers.length; i++) {
            int number = numbers[i];
            if (number < min) {
                minmaxIndex[0] = i;
                min = number;
            }
            if (number > max) {
                minmaxIndex[1] = i;
                max = number;
            }
        }
        return minmaxIndex;
    }

    public static int getSum(short[] row) {
        int sum = 0;
        for (int number : row) {
            sum += number;
        }
        return sum;
    }

    public static int getSum(int[] row) {
        int sum = 0;
        for (int number : row) {
            sum += number;
        }
        return sum;
    }

    public static int getSum(int[][] matrix) {
        int sum = 0;
        for (int[] row : matrix) {
            sum += getSum(row);
        }
        return sum;
    }

    public static double getAverage(int[] numbers) {
        int sum = 0;
        for (int number : numbers) {
            sum += number;
        }
        return ((double) sum) / numbers.length;
    }

    public static double getAverage(double[] ds) {
        double result = 0;
        for (double d : ds) {
            result += d;
        }
        return result / ds.length;
    }

    public static double getAverage(Collection<? extends Number> numberCollection) {
        double result = 0;
        for (Number number : numberCollection) {
            result += number.doubleValue();
        }
        return result / numberCollection.size();
    }

    public static double variance(double[] ds) {
        return getVariance(ds);
    }

    public static double getVariance(double[] ds) {
        double squareSum = 0;
        double sum = 0;
        for (double d : ds) {
            squareSum += (d * d);
            sum += d;
        }
        double mean = sum / ds.length;
        return squareSum / ds.length - mean * mean;
    }

    public static double variance(Collection<? extends Number> numberCollection) {
        return getVariance(numberCollection);
    }

    public static double getVariance(Collection<? extends Number> numberCollection) {
        double squareSum = 0;
        double sum = 0;
        for (Number number : numberCollection) {
            double d = number.doubleValue();
            squareSum += (d * d);
            sum += d;
        }
        int collectionSize = numberCollection.size();
        double mean = sum / collectionSize;
        return squareSum / collectionSize - mean * mean;
    }

    public static double stddev(double[] ds) {
        return getStdDev(ds);
    }

    public static double getStdDev(double[] ds) {
        return Math.sqrt(variance(ds));
    }

    public static double stddev(Collection<? extends Number> numberCollection) {
        return getStdDev(numberCollection);
    }

    public static double getStdDev(Collection<? extends Number> numberCollection) {
        return Math.sqrt(variance(numberCollection));
    }

    public static double log(double base, double a) {
        return getLog(base, a);
    }

    public static double getLog(double base, double a) {
        return Math.log(a) / Math.log(base);
    }

    public static double getEntropy(List<? extends Object> oArray) {
        CountMap<Object> countMap = new CountMap<Object>();
        countMap.increCountAll(oArray);
        return countMap.entropy();
    }

    public static double getEntropy(Object[] oArray) {
        CountMap<Object> countMap = new CountMap<Object>();
        countMap.increCountAll(oArray);
        return countMap.entropy();
    }

    public static double getEntropy(int[] counts) {
        int sum = 0;
        for (int count : counts) {
            sum += count;
        }
        double[] probabilities = new double[counts.length];
        for (int i = 0; i < counts.length; i++) {
            probabilities[i] = ((double) counts[i]) / sum;
        }
        return getEntropy(probabilities);
    }

    public static double getEntropy(double[] probabilities) {
        return getEntropy(probabilities, 0.00001);
    }

    public static double getEntropy(double[] probabilities, double threshold) throws RuntimeException {
        double total = 0;
        for (double p : probabilities) {
            total += p;
        }
        if (Math.abs(total - 1) > threshold) {
            throw new RuntimeException("Probabilities doesn't sum up to 1.");
        }
        double entropy = 0;
        for (double p : probabilities) {
            entropy += -p * log(2, p);
        }
        return entropy;
    }

    public static double avgLogLikelihood(double[] likelihoods) {
        double returnValue = 0;
        for (double p : likelihoods) {
            returnValue += log(2, p);
        }
        return returnValue / likelihoods.length;
    }

    public static int logFloor(int base, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Input(" + n + ") must be larger than or equal to 0");
        }
        int i = 0;
        for (int a = base; a <= n; i++) {
            a *= base;
        }
        return i;
    }

    public static boolean isInteger(double minValue) {
        return minValue - Math.floor(minValue) == 0;
    }

    public static double[] getColVector(double[][] a, int j) {
        double[] returnValue = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            returnValue[i] = a[i][j];
        }
        return returnValue;
    }

    public static double[] getRowVector(double[][] a, int i) {
        return a[i];
    }

    public static CharSequence rightAlignedValue(int value, int characterLength) {
        StringBuilder stringBuffer = new StringBuilder();
        Formatter formatter = new Formatter(stringBuffer, Locale.US);
        formatter.format("%1$" + characterLength + "s", Integer.toString(value));
        return stringBuffer;
    }

    public static String classificationAccuracyToString(int[][] classificationAccuracy, int characterLength) {
        StringBuilder stringBuilder = new StringBuilder();
        int correct = 0;
        int wrong = 0;
        for (int i = 0; i < classificationAccuracy.length; i++) {
            int[] row = classificationAccuracy[i];
            for (int j = 0; j < row.length; j++) {
                int value = row[j];
                if (i == j) {
                    correct += value;
                } else {
                    wrong += value;
                }
                stringBuilder.append(rightAlignedValue(value, characterLength));
            }
            stringBuilder.append(StringToolkit.newLine());
        }
        int total = correct + wrong;
        double accuracy = ((double) correct) / total;
        stringBuilder.append("Accuracy: ").append(accuracy);
        return stringBuilder.toString();
    }

    public static double vectorNorm(double[] lambda, int i) {
        if (i == 1) {
            return Math.sqrt(vectorNorm(lambda, 2));
        } else if (i == 2) {
            return innerProduct(lambda, lambda);
        } else {
            throw new UnsupportedOperationException("Norm of degree '" + i + "' not implemented yet!");
        }
    }

    public static void assertEquality(double d1, double d2, double threshold) {
        double diff = Math.abs(d1 - d2);
        YeriDebug.ASSERT(diff < threshold, "'" + d1 + "' and '" + d2 + "' is different by " + diff + ">" + threshold + " !");
    }

    public static double logCalculate(double[] logValueArray, NumericalOperator numericalOperator) {
        if (numericalOperator == NumericalOperator.ADD) {
            double result = Double.NEGATIVE_INFINITY;
            for (double logValue : logValueArray) {
                result = logCalculate(result, logValue, numericalOperator);
            }
            return result;
        } else {
            throw new UnsupportedOperationException("");
        }
    }

    public static double[] mergeRows(double[][] testArray) {
        CollectionsToolkit.isRectangular(testArray);
        double[] returnValue = new double[testArray[0].length];
        Arrays.fill(returnValue, 0);
        for (int j = 0; j < testArray[0].length; j++) {
            for (int i = 0; i < testArray.length; i++) {
                returnValue[j] += testArray[i][j];
            }
        }
        return returnValue;
    }

    public static double[] mergeCols(double[][] testArray) {
        CollectionsToolkit.isRectangular(testArray);
        double[] returnValue = new double[testArray.length];
        for (int i = 0; i < testArray.length; i++) {
            returnValue[i] = CollectionsToolkit.sum(testArray[i]);
        }
        return returnValue;
    }

    public static void checkValueIncreasing(int minValue, int maxValue) {
        if (minValue <= maxValue) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Min (").append(minValue).append(") bigger than Max(").append(maxValue).append(")!");
        throw new IllegalArgumentException(builder.toString());
    }

    public static void checkValueIncreasing(double minValue, double maxValue) {
        if (minValue <= maxValue) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Min (").append(minValue).append(") bigger than Max(").append(maxValue).append(")!");
        throw new IllegalArgumentException(builder.toString());
    }

    public static void checkValueIncreasing(double minValue, double midValue, double maxValue) {
        checkValueIncreasing(minValue, midValue);
        checkValueIncreasing(midValue, maxValue);
    }

    public static boolean areValuesIncreasing(double minValue, double midValue, double maxValue) {
        return minValue <= midValue && midValue <= maxValue;
    }

    public static int shrinkToFit(int value, int target) {
        int returnValue = value;
        while (returnValue > target) {
            returnValue /= 2;
        }
        return returnValue;
    }

    public static double fitBetweenMinMax(double min, double value, double max) {
        checkValueIncreasing(min, max);
        double returnValue = Math.max(min, value);
        returnValue = Math.min(max, returnValue);
        return returnValue;
    }

    public static int fitBetweenMinMax(int min, int value, int max) {
        checkValueIncreasing(min, max);
        int returnValue = Math.max(min, value);
        returnValue = Math.min(max, returnValue);
        return returnValue;
    }

    public static int getOnesideBufferLength(int subpartLength, int totalLength) {
        return (totalLength - subpartLength) / 2;
    }

    public static int[] getRegionStartEnd(int position, int length, int alignment) {
        if (alignment == SwingConstants.LEFT) {
            return new int[] { position, position + length };
        } else if (alignment == SwingConstants.CENTER) {
            return new int[] { position - length / 2, position + length / 2 };
        } else if (alignment == SwingConstants.RIGHT) {
            return new int[] { position - length, position };
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static double[] getRegionStartEnd(double position, double length, int alignment) {
        if (alignment == SwingConstants.LEFT) {
            return new double[] { position, position + length };
        } else if (alignment == SwingConstants.CENTER) {
            return new double[] { position - length / 2, position + length / 2 };
        } else if (alignment == SwingConstants.RIGHT) {
            return new double[] { position - length, position };
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static double[] shrinkRegionStartEnd(double start, double end, double proportion, int alignment) {
        StatisticsToolkit.checkValueIncreasing(0, proportion, 1);
        double position = 0;
        switch(alignment) {
            case SwingConstants.LEFT:
                position = start;
                break;
            case SwingConstants.CENTER:
                position = (start + end) / 2;
                break;
            case SwingConstants.RIGHT:
                position = end;
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return getRegionStartEnd(position, (end - start) * proportion, alignment);
    }

    public static double sumDoubleList(double[] doubleArray) {
        double total = 0;
        for (double d : doubleArray) {
            total += d;
        }
        return total;
    }

    public static double sumValueList(List<? extends Number> numberList) {
        double total = 0;
        for (Number number : numberList) {
            total += number.doubleValue();
        }
        return total;
    }

    public static double[][] makeNormalCumulativeProportionalArray(double[] doubleArray) {
        double total = sumDoubleList(doubleArray);
        double[] normalArray = new double[doubleArray.length];
        double[] cumulativeArray = new double[doubleArray.length];
        double cumulatedValue = 0;
        for (int i = 0; i < doubleArray.length; i++) {
            normalArray[i] = doubleArray[i] / total;
            cumulatedValue += normalArray[i];
            cumulativeArray[i] = cumulatedValue;
        }
        return new double[][] { normalArray, cumulativeArray };
    }

    public static double[] makeNormalProportionalArray(double[] doubleArray) {
        return makeNormalCumulativeProportionalArray(doubleArray)[0];
    }

    public static double[] toDoubleArray(List<? extends Number> numberList) {
        double[] array = new double[numberList.size()];
        for (int i = 0; i < numberList.size(); i++) {
            array[i] = numberList.get(i).doubleValue();
        }
        return array;
    }

    public static double[] makeNormalProportionalArray(List<? extends Number> numberList) {
        return makeNormalCumulativeProportionalArray(toDoubleArray(numberList))[0];
    }

    public static double[] makeCumulativeProportionalArray(List<? extends Number> numberList) {
        return makeNormalCumulativeProportionalArray(toDoubleArray(numberList))[1];
    }

    public static double getCosineSimilarity(double[] v1, double[] v2) {
        return innerProduct(v1, v2) / Math.sqrt(innerProduct(v1, v1)) * Math.sqrt(innerProduct(v2, v2));
    }

    public static int[][] calculateEach(int[][] v1, int[][] v2, NumericalOperator op) {
        return CollectionsToolkit.calculateEach(v1, v2, op);
    }

    public static int[] calculateEach(int[] v1, int[] v2, NumericalOperator op) {
        return CollectionsToolkit.calculateEach(v1, v2, op);
    }

    public static double[] calculateEach(double[] v1, double[] v2, NumericalOperator op) {
        return CollectionsToolkit.calculateEach(v1, v2, op);
    }

    public static double[] calculateEach(double[] v1, Number[] v2, NumericalOperator op) {
        return CollectionsToolkit.calculateEach(v1, v2, op);
    }

    public static int getArea(Rectangle r) {
        return r.width * r.height;
    }

    public static class AreaComparator implements Comparator<Rectangle> {

        @Override
        public int compare(Rectangle o1, Rectangle o2) {
            return getArea(o1) - getArea(o2);
        }
    }

    public static double[] normalize(double[] vector) {
        double vectorSize = Math.sqrt(innerProduct(vector, vector));
        double[] returnArray = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            returnArray[i] = vector[i] / vectorSize;
        }
        return returnArray;
    }

    public static double normalizeNumber(Collection<? extends Number> numberCollection, Number targetNumber) {
        NormalDistribution normalDistribution = NormalDistribution.createFromSample(numberCollection);
        return normalDistribution.getNormalizedValue(targetNumber.doubleValue());
    }

    public static class NormalDistribution {

        private double mean = 0;

        private double variance = 1;

        public static enum Side {

            LEFT, CENTER, RIGHT
        }

        public NormalDistribution(double mean, double variance) {
            this.mean = mean;
            this.variance = variance;
        }

        public double getNormalizedValue(Number value) {
            double diff = value.doubleValue() - mean;
            if (diff == 0) {
                return 0;
            } else {
                return diff / variance;
            }
        }

        public double normalizeValue(Number value) {
            return getNormalizedValue(value);
        }

        public double denormalizeValue(double normalizedValue) {
            return mean + normalizedValue * variance;
        }

        public static NormalDistribution createFromSample(Collection<? extends Number> numberCollection) {
            double total = 0;
            double sumOfSquare = 0;
            int count = numberCollection.size();
            for (Number number : numberCollection) {
                total += number.doubleValue();
                sumOfSquare += number.doubleValue() * number.doubleValue();
            }
            double mean = total / count;
            double sd = (count == 1) ? 0 : (Math.sqrt((sumOfSquare - mean * mean * count) / (count - 1)));
            return new NormalDistribution(mean, sd);
        }

        public static NormalDistribution createFromSample(Map<? extends Number, Integer> countMap) {
            double total = 0;
            double sumOfSquare = 0;
            int count = 0;
            for (Number key : countMap.keySet()) {
                double keyValue = key.doubleValue();
                int keyCount = countMap.get(key);
                count += keyCount;
                total = keyValue * keyCount;
                sumOfSquare += keyValue * keyValue * keyCount;
            }
            double mean = total / count;
            double sd = (count == 1) ? 0 : (Math.sqrt((sumOfSquare - mean * mean * count) / (count - 1)));
            return new NormalDistribution(mean, sd);
        }

        public double getConfidenceIntervalWidth(double confidence, int df) {
            if (confidence == 0.90) {
                return 1.64485 * this.variance;
            } else if (confidence == 0.95) {
                return 1.95996 * this.variance;
            } else if (confidence == 0.99) {
                return 2.57583 * this.variance;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        public double[] getConfidenceInterval(double confidence, Side side) {
            if (side != Side.CENTER) {
                throw new UnsupportedOperationException();
            }
            double intervalWidth = getConfidenceIntervalWidth(confidence, Integer.MAX_VALUE);
            return new double[] { mean - intervalWidth, mean + intervalWidth };
        }
    }

    public static double[] normalizeNumberCollection(Collection<? extends Number> numberCollection) {
        NormalDistribution normalDistribution = NormalDistribution.createFromSample(numberCollection);
        double[] array = new double[numberCollection.size()];
        Iterator<? extends Number> iterator = numberCollection.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            array[i] = normalDistribution.normalizeValue(iterator.next());
        }
        return array;
    }

    public static double denormalizeNumber(Collection<? extends Number> numberCollection, Number normalizedNumber) {
        NormalDistribution normalDistribution = NormalDistribution.createFromSample(numberCollection);
        return normalDistribution.denormalizeValue(normalizedNumber.doubleValue());
    }

    public abstract static class InvertedIndexSimilarityCalculator {

        private int minimumRequiredSharedItemForComparison = 2;

        public String getShortenedName() {
            String classname = this.getClass().getName();
            int taleLength = ("InvertedIndexSimilarityCalculator").length();
            int dollarIndex = classname.lastIndexOf('$');
            return classname.substring(dollarIndex + 1, classname.length() - taleLength);
        }

        public InvertedIndexSimilarityCalculator() {
        }

        public Number getSimilarityScore(Map<Integer, ? extends Number> invertedIndex1, Map<Integer, ? extends Number> invertedIndex2) {
            return calculateSimilarityScore(invertedIndex1, invertedIndex2);
        }

        protected abstract Number calculateSimilarityScore(Map<Integer, ? extends Number> invertedIndex1, Map<Integer, ? extends Number> invertedIndex2);

        public SortedSet<ScoreWrapper<Integer>> getKeyIDsRelatedToValueIDOrderedBySimilarityDesc(MapValueMap<Integer, Integer, ? extends Number> invertedIndexMap, int[] keyValueIDPair) {
            int keyID = keyValueIDPair[0];
            int valueID = keyValueIDPair[1];
            Comparator<? super ScoreWrapper<Integer>> comparator = ScoreWrapper.getReverseComparator();
            TreeSet<ScoreWrapper<Integer>> similaritySet = new TreeSet<ScoreWrapper<Integer>>(comparator);
            for (int thisKeyID : invertedIndexMap.keySet()) {
                if (thisKeyID == keyID) {
                    continue;
                }
                Map<Integer, ? extends Number> thisKeyValueMap = invertedIndexMap.get(thisKeyID);
                if (!thisKeyValueMap.containsKey(valueID)) {
                    continue;
                }
                Number similarityScore = getSimilarityScore(invertedIndexMap.get(keyID), invertedIndexMap.get(thisKeyID));
                if (similarityScore == null) {
                    continue;
                }
                ScoreWrapper<Integer> similarity = new ScoreWrapper<Integer>(thisKeyID, similarityScore.doubleValue());
                similaritySet.add(similarity);
            }
            return similaritySet;
        }

        public int getMinimumRequiredSharedItemForComparison() {
            return minimumRequiredSharedItemForComparison;
        }

        public void setMinimumRequiredSharedItemForComparison(int minimumRequiredSharedItemForComparison) {
            this.minimumRequiredSharedItemForComparison = minimumRequiredSharedItemForComparison;
        }
    }

    public static class InnerProductInvertedIndexSimilarityCalculator extends InvertedIndexSimilarityCalculator {

        public InnerProductInvertedIndexSimilarityCalculator() {
            super();
        }

        protected Number calculateSimilarityScore(Map<Integer, ? extends Number> invertedIndex1, Map<Integer, ? extends Number> invertedIndex2) {
            Set<Integer> commonUserInvertedIndexKeySet = CollectionsToolkit.intersection(invertedIndex1.keySet(), invertedIndex2.keySet(), null);
            if (commonUserInvertedIndexKeySet.size() < getMinimumRequiredSharedItemForComparison()) {
                return null;
            }
            List<Number> valueList01 = MapToolkit.getValueList(invertedIndex1, commonUserInvertedIndexKeySet);
            List<Number> valueList02 = MapToolkit.getValueList(invertedIndex2, commonUserInvertedIndexKeySet);
            return StatisticsToolkit.innerProduct(valueList01, valueList02);
        }
    }

    public static class PearsonsCorrelationInvertedIndexSimilarityCalculator extends InvertedIndexSimilarityCalculator {

        public PearsonsCorrelationInvertedIndexSimilarityCalculator() {
            super();
        }

        protected Number calculateSimilarityScore(Map<Integer, ? extends Number> invertedIndex1, Map<Integer, ? extends Number> invertedIndex2) {
            Set<Integer> commonUserInvertedIndexKeySet = CollectionsToolkit.intersection(invertedIndex1.keySet(), invertedIndex2.keySet(), null);
            if (commonUserInvertedIndexKeySet.size() < getMinimumRequiredSharedItemForComparison()) {
                return null;
            }
            List<Number> valueList01 = MapToolkit.getValueList(invertedIndex1, commonUserInvertedIndexKeySet);
            List<Number> valueList02 = MapToolkit.getValueList(invertedIndex2, commonUserInvertedIndexKeySet);
            double correlration = StatisticsToolkit.getCorrelation(valueList01, valueList02);
            return Double.isNaN(correlration) ? null : correlration;
        }
    }

    public static double getPhiSqaureOf2By2(int[][] empiricalTable) {
        int a = empiricalTable[0][0];
        int b = empiricalTable[0][1];
        int c = empiricalTable[1][0];
        int d = empiricalTable[1][1];
        double top = Math.pow(a * d - b * c, 2);
        double bottom = ((a + b) * (a + c) * (b + d) * (c + d));
        double phiSquare = top == 0 ? 0 : (top / bottom);
        fitBetweenMinMax(0, phiSquare, 1);
        YeriDebug.ASSERT(!Double.isInfinite(phiSquare));
        YeriDebug.ASSERT(!Double.isNaN(phiSquare));
        return phiSquare;
    }

    public static double getChiSqaureOf2By2(int[][] empiricalTable) {
        double phiSquare = getPhiSqaureOf2By2(empiricalTable);
        int n = getSum(empiricalTable);
        return n * phiSquare;
    }

    public static double getPointEntropy(double n) {
        return n == 0 ? 0 : (n * log(2, n));
    }

    public static double getMutualInformationOf2By2(int[][] empiricalTable) {
        int a = empiricalTable[0][0];
        int b = empiricalTable[0][1];
        int c = empiricalTable[1][0];
        int d = empiricalTable[1][1];
        int n = getSum(empiricalTable);
        double line1 = getPointEntropy(n);
        double line2 = getPointEntropy(a);
        double line3 = getPointEntropy(b);
        double line4 = getPointEntropy(c);
        double line5 = getPointEntropy(d);
        double line6 = getPointEntropy(a + b);
        double line7 = getPointEntropy(a + c);
        double line8 = getPointEntropy(b + d);
        double line9 = getPointEntropy(c + d);
        double sum = line1 + line2 + line3 + line4 + line5 - line6 - line7 - line8 - line9;
        double rv = sum / n;
        YeriDebug.ASSERT(!Double.isInfinite(rv));
        YeriDebug.ASSERT(!Double.isNaN(rv));
        return rv;
    }

    public static double getPointwiseMutualInformationOf2By2(int[][] empiricalTable) {
        int a = empiricalTable[0][0];
        int b = empiricalTable[0][1];
        int c = empiricalTable[1][0];
        int n = getSum(empiricalTable);
        double line1 = a * log(2, n);
        double line2 = getPointEntropy(a);
        double line3 = a * log(2, a + b);
        double line4 = a * log(2, a + c);
        double sum = line1 + line2 - line3 - line4;
        double rv = sum / n;
        YeriDebug.ASSERT(!Double.isInfinite(rv));
        YeriDebug.ASSERT(!Double.isNaN(rv));
        return rv;
    }

    public static double getRSJWeightOf2By2(int[][] empiricalTable) {
        int a = empiricalTable[0][0];
        int b = empiricalTable[0][1];
        int c = empiricalTable[1][0];
        int d = empiricalTable[1][1];
        double rv = 0;
        if (!(a == 0 && b == 0)) {
            rv += (log(2, a) - log(2, b));
        } else if (!(c == 0 && d == 0)) {
            rv += (log(2, d) - log(2, c));
        }
        YeriDebug.ASSERT(!Double.isNaN(rv));
        return rv;
    }

    public static int getSign(double d) {
        return d < 0 ? -1 : (d == 0 ? 0 : 1);
    }

    public static int[] getMinMaxIndex(double[] numbers) {
        int[] minmaxIndex = new int[2];
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int i = 0; i < numbers.length; i++) {
            double number = numbers[i];
            if (number < min) {
                minmaxIndex[0] = i;
                min = number;
            }
            if (number > max) {
                minmaxIndex[1] = i;
                max = number;
            }
        }
        return minmaxIndex;
    }

    public static double[] getMinMax(double[] numbers) {
        int[] minmaxIndex = getMinMaxIndex(numbers);
        return new double[] { numbers[minmaxIndex[0]], numbers[minmaxIndex[1]] };
    }

    public static double getDCG(int[] rankValueArray) {
        double dcg = 0;
        for (int i = 0; i < rankValueArray.length; i++) {
            int rankValue = rankValueArray[i];
            int j = i + 1;
            double dcgItem = (Math.pow(2, rankValue) - 1) / log(2, j + 1);
            dcg += dcgItem;
        }
        return dcg;
    }

    public static double getNDCG(int[] rankValueArray, int maxRankValue) {
        int[] maxRankValueArray = new int[rankValueArray.length];
        Arrays.fill(maxRankValueArray, maxRankValue);
        double dcg = getDCG(rankValueArray);
        double maxDCG = getDCG(maxRankValueArray);
        return dcg / maxDCG;
    }

    protected static void test04() {
        System.out.println("q1 A: " + getNDCG(new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, 1));
        System.out.println("q1 B: " + getNDCG(new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, 1));
        System.out.println("q2 A: " + getNDCG(new int[] { 1, 0, 0, 1, 0, 0, 0, 0, 1, 0 }, 1));
        System.out.println("q2 B: " + getNDCG(new int[] { 0, 1, 0, 0, 1, 0, 0, 0, 0, 0 }, 1));
        System.out.println("q3 A: " + getNDCG(new int[] { 0, 1, 0, 0, 1, 1, 0, 0, 0, 1 }, 1));
        System.out.println("q3 B: " + getNDCG(new int[] { 1, 0, 1, 0, 0, 1, 1, 0, 1, 1 }, 1));
    }

    /**
	 * 
	 * @param startIndexArray starting index of every segment (0:inclusive, n:exclusive)
	 * @param totalLength
	 * @return
	 */
    public static int[] indexArrayToLengthArray(int[] startIndexArray, int totalLength) {
        int[] lengthArray = new int[startIndexArray.length];
        for (int i = 0; i < startIndexArray.length - 1; i++) {
            lengthArray[i] = startIndexArray[i + 1] - startIndexArray[i];
        }
        lengthArray[startIndexArray.length - 1] = totalLength - startIndexArray[startIndexArray.length - 1];
        return lengthArray;
    }

    public static int[] lengthArrayToEndIndexArray(int[] lengthArray) {
        int[] endIndexArray = new int[lengthArray.length];
        endIndexArray[0] = lengthArray[0];
        for (int i = 1; i < lengthArray.length; i++) {
            endIndexArray[i] = endIndexArray[i - 1] + lengthArray[i];
        }
        return endIndexArray;
    }

    public static int[] endIndexArrayTolengthArray(int[] endIndexArray) {
        int[] lengthArray = new int[endIndexArray.length];
        lengthArray[0] = endIndexArray[0];
        for (int i = 1; i < lengthArray.length; i++) {
            lengthArray[i] = endIndexArray[i] + endIndexArray[i - 1];
        }
        return lengthArray;
    }

    public static int[] getValueArray(int[] valueArray, int[] indexArray) {
        List<Integer> valueList = new ArrayList<Integer>();
        for (int index : indexArray) {
            valueList.add(valueArray[index]);
        }
        return CollectionsToolkit.toIntArray(valueList);
    }

    public static int[] getMinMax(int[] numbers) {
        int[] indexArray = getMinMaxIndex(numbers);
        return getValueArray(numbers, indexArray);
    }

    public static int[] createIntegerSequenceArray(int start, int length) {
        int[] valueArray = new int[length];
        for (int i = 0; i < valueArray.length; i++) {
            valueArray[i] = start + i;
        }
        return valueArray;
    }

    public static double[] toDoubleArray(Number[] numberArray) {
        double[] returnValue = new double[numberArray.length];
        for (int i = 0; i < numberArray.length; i++) {
            returnValue[i] = numberArray[i].doubleValue();
        }
        return returnValue;
    }

    public static double[] toDoubleArray(int[] numberArray) {
        double[] returnValue = new double[numberArray.length];
        for (int i = 0; i < numberArray.length; i++) {
            returnValue[i] = numberArray[i];
        }
        return returnValue;
    }

    public static double[][] toDoubleArray(Map<? extends Number, ? extends Number> pairMap) {
        double[][] returnValue = new double[pairMap.size()][2];
        SortedMap<Number, Number> sortedMap = new TreeMap<Number, Number>(pairMap);
        int i = 0;
        for (Map.Entry<? extends Number, ? extends Number> entry : sortedMap.entrySet()) {
            returnValue[i] = new double[] { entry.getKey().doubleValue(), entry.getValue().doubleValue() };
            i++;
        }
        return returnValue;
    }

    public static Integer[][] getWrapperArray(int[][] array) {
        Integer[][] returnArray = new Integer[array.length][];
        for (int i = 0; i < array.length; i++) {
            returnArray[i] = new Integer[array[i].length];
            for (int j = 0; j < array[i].length; j++) {
                returnArray[i][j] = array[i][j];
            }
        }
        return returnArray;
    }

    public static double getWeightedAverage(Map<? extends Number, ? extends Number> countMap) {
        return getWeightedSum(countMap) / countMap.size();
    }

    public static double getWeightedSum(Map<? extends Number, ? extends Number> countMap) {
        double total = 0;
        for (Number key : countMap.keySet()) {
            Number value = countMap.get(key);
            total += key.doubleValue() * value.doubleValue();
        }
        return total;
    }
}
