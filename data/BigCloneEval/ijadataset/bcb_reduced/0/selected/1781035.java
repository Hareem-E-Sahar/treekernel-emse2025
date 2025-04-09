package sky4s.test.math.min.iter;

import java.util.*;

/**
 * <p>Title: Colour Management System</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author cms.shu.edu.tw
 * @version 1.0
 */
public class Minimisation {

    public static void main(String[] args) {
        double[] start = new double[] { 1, 2, 3 };
        double[] end = new double[] { 2, 3, 4 };
        Minimisation min = new Minimisation();
        min.minimisation(new Function(), start, end, 10, 0);
    }

    protected static boolean touch(int[] array, int touched) {
        int size = array.length;
        for (int x = 0; x < size; x++) {
            if (array[x] < touched) {
                return false;
            }
        }
        return true;
    }

    protected double[] vars;

    protected int[] varSearch;

    protected double[] vstart;

    protected double[] vend;

    protected double[] start;

    protected double[] end;

    protected void reset(int index) {
        varSearch[index] = 0;
        vstart[index] = start[index];
        vend[index] = end[index];
        vars[index] = start[index];
    }

    public void minimisation(MinimisationFunction minFunc, double[] start, double[] end, int searchTimes, double tolerance) {
        int size = start.length;
        if (size != end.length) {
            throw new IllegalArgumentException("start.length != end.length");
        }
        this.start = start;
        this.end = end;
        vars = new double[size];
        copy(start, vars);
        varSearch = new int[size];
        vstart = new double[size];
        copy(start, vstart);
        vend = new double[size];
        copy(end, vend);
        int index = 0;
        int endIndex = size - 1;
        BinarySearchFunction bFunc = new BinarySearchFunction(minFunc, vars);
        int x = 0;
        for (x = 0; ; x++) {
            if (index < endIndex) {
                index++;
                reset(index);
                continue;
            } else {
                for (; varSearch[index] < searchTimes; varSearch[index]++) {
                    double[] result = binarySearch(vstart, vend, index, bFunc, vars);
                    if (tolerance != 0 && (result[3] - result[2]) < tolerance) {
                        break;
                    }
                }
            }
            if (touch(varSearch, searchTimes)) {
                break;
            }
            reset(index);
            for (index--; index >= 0; index--) {
                double[] result = binarySearch(vstart, vend, index, bFunc, vars);
                varSearch[index]++;
                if (tolerance != 0 && (result[3] - result[2]) < tolerance) {
                    varSearch[index] = searchTimes;
                }
                if (varSearch[index] <= searchTimes) {
                    break;
                }
            }
        }
        System.out.println(x);
    }

    protected static void copy(double[] src, double[] dest) {
        int size = src.length;
        if (size != dest.length) {
            throw new IllegalArgumentException("src.length != dest.length");
        }
        for (int x = 0; x < size; x++) {
            dest[x] = src[x];
        }
    }

    protected static double[] binarySearch(double[] start, double[] end, int index, BinarySearchFunction func, double[] vars) {
        double[] result = binarySearch(start[index], end[index], index, func);
        start[index] = result[0];
        end[index] = result[1];
        vars[index] = result[4];
        System.out.println(Arrays.toString(vars));
        return result;
    }

    protected static double[] binarySearch(double start, double end, int varIndex, BinarySearchFunction func) {
        double startVal = func.function(start, varIndex);
        double endVal = func.function(end, varIndex);
        double middle = (start + end) / 2;
        double middleVal = func.function(middle, varIndex);
        double startDiff = Math.abs(startVal - middleVal);
        double endDiff = Math.abs(endVal - middleVal);
        if (startDiff < endDiff) {
            return new double[] { start, middle, startVal, middleVal, middle, middleVal };
        } else if (startDiff > endDiff) {
            return new double[] { middle, end, middleVal, endVal, middle, middleVal };
        } else {
            if (startVal < endVal) {
                return new double[] { start, middle, startVal, middleVal, middle, middleVal };
            } else {
                return new double[] { middle, end, middleVal, endVal, middle, middleVal };
            }
        }
    }

    static class BinarySearchFunction {

        BinarySearchFunction(MinimisationFunction func, double[] vars) {
            this.minFunction = func;
            this.staticVars = vars;
            functionVars = new double[staticVars.length];
        }

        MinimisationFunction minFunction;

        double[] staticVars;

        double[] functionVars;

        double function(double var, int index) {
            copy(staticVars, functionVars);
            functionVars[index] = var;
            return minFunction.function(functionVars);
        }
    }
}

class Function implements MinimisationFunction {

    public double function(double[] variables) {
        return 2. / variables[0] + variables[1] - variables[2];
    }
}
