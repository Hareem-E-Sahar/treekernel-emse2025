package edu.ucla.stat.SOCR.util;

import java.util.*;
import edu.ucla.stat.SOCR.analyses.exception.*;

public class Utility {

    public static double digitTruncator(double input) {
        double result = 0;
        if (input == 2.7199999999999998) {
            result = 2.72;
        } else if (input == 2.7800000000000002) {
            result = 2.78;
        } else if (input == 3.1399999999999997) {
            result = 3.14;
        } else if (input == 3.6799999999999997) {
            result = 3.68;
        } else {
            result = input;
        }
        return result;
    }

    public static String digitAugmentator(double input) {
        String result = null;
        if (input == 2.0) {
            result = "2.00";
        } else if (input == 2.3) {
            result = "2.30";
        } else if (input == 2.6) {
            result = "2.60";
        } else if (input == 2.9) {
            result = "2.90";
        } else if (input == 3.2) {
            result = "3.20";
        } else if (input == 3.5) {
            result = "3.50";
        } else {
            result = input + "";
        }
        return result;
    }

    public static int sign(double input) {
        int result = result = (input > 0) ? 1 : ((input == 0) ? 0 : -1);
        return result;
    }

    public static int[] sign(double[] input) {
        int[] result = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (input[i] > 0) ? 1 : ((input[i] == 0) ? 0 : -1);
        }
        return result;
    }

    public static double prod(double[] input) {
        double result = input[0];
        for (int i = 1; i < input.length; i++) {
            result *= input[i];
        }
        return result;
    }

    public static double min(double[] input) {
        double result = input[0];
        for (int i = 1; i < input.length; i++) {
            result = Math.min(result, input[i]);
        }
        return result;
    }

    public static double max(double[] input) {
        double result = input[0];
        for (int i = 1; i < input.length; i++) {
            result = Math.max(result, input[i]);
        }
        return result;
    }

    public static double[] exp(double[] input) {
        double[] result = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = Math.exp(input[i]);
        }
        return result;
    }

    public static double[] listToDoubleArray(ArrayList list) {
        int size = list.size();
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = ((Double) list.get(i)).doubleValue();
        }
        return result;
    }

    public static int[] listToIntArray(ArrayList list) {
        int size = list.size();
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = ((Integer) list.get(i)).intValue();
        }
        return result;
    }

    public static byte[] listToByteArray(ArrayList list) {
        int size = list.size();
        byte[] result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = ((Byte) list.get(i)).byteValue();
        }
        return result;
    }

    public static double[] innerProduct(double[] x, double[] y) throws WrongDataFormatException {
        if (x.length == 0 || y.length == 0 || x.length != y.length) {
            throw new WrongDataFormatException("different length of vector in inner product calculation.");
        }
        double[] result = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            result[i] = x[i] * y[i];
        }
        return result;
    }

    public static double[] truncateArray(double[] input, int start, int end) throws WrongDataFormatException {
        if (input.length == 0 || start > end || start > input.length || end > input.length) {
            throw new WrongDataFormatException("wrong size of input or inappropriate start and or end.");
        }
        double[] result = new double[end - start];
        for (int i = 0; i < end - start; i++) {
            result[i] = input[start + i];
        }
        return result;
    }

    public static String[][] truncateDigits(double[][] input, int numberDigits) {
        String[][] result = new String[input.length][];
        int indexDot = -1;
        String integerPart = null, decimalPart = null, dot = ".";
        String wholePart = null;
        for (int i = 0; i < input.length; i++) {
            result[i] = new String[input[i].length];
            for (int j = 0; j < input[i].length; j++) {
                System.out.println("input[" + i + "][" + j + "] = " + input[i][j]);
                wholePart = input[i][j] + "";
                indexDot = (wholePart).indexOf(dot);
                if (indexDot >= 0) {
                    integerPart = wholePart.substring(0, indexDot);
                    try {
                        decimalPart = wholePart.substring(indexDot + 1, indexDot + 1 + numberDigits);
                    } catch (Exception e) {
                        decimalPart = wholePart.substring(indexDot + 1, wholePart.length());
                    }
                    result[i][j] = integerPart + dot + decimalPart;
                } else {
                    result[i][j] = input[i][j] + "";
                }
            }
        }
        return result;
    }

    public static double[][] reverseDoubleIndex(double[][] input) {
        int iLength = input.length;
        double[][] result = new double[iLength][iLength];
        for (int i = 0; i < iLength; i++) {
            for (int j = 0; j < iLength; j++) {
                try {
                    result[i][j] = input[j][i];
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    public static void print(double[] array, String name) {
        System.out.println("Print 1D double array ");
        for (int i = 0; i < array.length; i++) {
            System.out.println(name + " array[" + i + "] = " + array[i]);
        }
        System.out.println();
    }

    public static void print(double[][] array, String name) {
        System.out.println("Print 2D double array");
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                System.out.println(name + " array[" + i + "][" + j + "] = " + array[i][j]);
            }
        }
        System.out.println();
    }

    public static double[] getLogArray(double[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = Math.log(input[i]);
        }
        return output;
    }

    public static double[] getLog10Array(double[] input) {
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = Math.log10(input[i]);
        }
        return output;
    }

    public static double log10(double input) {
        return Math.log(input) / Math.log(10);
    }

    public static ArrayList doubleArrayToList(double[] input) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (int i = 0; i < input.length; i++) {
            result.add(new Double(input[i]));
        }
        return result;
    }

    public static double findKolmogorovProb(double z) {
        int maxStep = 1000;
        double p = 0;
        for (int j = 1; j <= maxStep; j++) {
            double onePower = (j % 2 == 0) ? -1 : 1;
            p += onePower * Math.exp(-2 * j * j * z * z);
        }
        p *= 2;
        System.out.println("findKolmogorovPValue p = " + p);
        return p;
    }

    public static double getStudentTCriticalPoint95CI(int df) {
        double alpha = 0.05;
        double cp = 0;
        double area = 1 - .5 * alpha;
        if (area == .975) {
            if (df >= 120) cp = 1.979930; else if (df >= 60) cp = 2.000298; else if (df >= 40) cp = 2.021075; else cp = criticalPointLookUp[df - 1];
        }
        return cp;
    }

    public static double getFlignerKilleenNormalQuantile(int totalSize, double rank) {
        double ans = (1 + (rank / (((double) totalSize) + 1))) / 2;
        return ans;
    }

    private static double[] criticalPointLookUp = { 12.7062047361747, 4.30265272974946, 3.18244630528371, 2.77644510519779, 2.57058183563631, 2.44691185114497, 2.36462425159278, 2.30600413520417, 2.26215716279820, 2.22813885198627, 2.20098516009164, 2.17881282966723, 2.16036865646279, 2.14478668791780, 2.13144954555978, 2.11990529922125, 2.10981557783332, 2.10092204024104, 2.09302405440831, 2.08596344726586, 2.07961384472768, 2.07387306790403, 2.06865761041905, 2.06389856162803, 2.05953855275330, 2.05552943864287, 2.05183051648029, 2.04840714179525, 2.04522964213270, 2.04227245630124, 2.03951344639641, 2.0369333434601, 2.03451529744934, 2.03224450931772, 2.03010792825034, 2.02809400098045, 2.02619246302911, 2.02439416391197, 2.02269092003676, 2.02107539030627 };

    public static void main(String args[]) {
        getFlignerKilleenNormalQuantile(14, 1);
    }
}
