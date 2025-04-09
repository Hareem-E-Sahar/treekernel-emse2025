package utilities;

import java.util.StringTokenizer;

public class MathRoutines {

    /** Creates a new instance of Correlation */
    public MathRoutines() {
    }

    public static int numberCombinations(int n, int c) {
        int combinations = 1;
        for (int i = n; i >= n - c + 1; i--) {
            combinations *= i;
        }
        for (int i = 2; i <= c; i++) {
            combinations /= i;
        }
        return combinations;
    }

    public static float getColumnAverage(float[][] M, int collumn, int classe) {
        int collumns = M[0].length;
        float sum = 0;
        float r = 0;
        for (int i = 0; i < M.length; i++) {
            int cl = ((int) M[i][collumns - 1]);
            if (cl == classe) {
                sum += M[i][collumn];
                r += 1;
            }
        }
        if (r > 0) {
            return (sum / r);
        } else {
            return (0);
        }
    }

    public static float getColumnAverage(float[][] M, int column) {
        float sum = 0;
        float r = 0;
        for (int i = 0; i < M.length; i++) {
            sum += M[i][column];
            r += 1;
        }
        if (r > 0) {
            return (sum / r);
        } else {
            return (0);
        }
    }

    public static double getStd(double[][] M, double average, int collumn, int classe) {
        int collumns = M[0].length;
        double sum = 0;
        for (int i = 0; i < M.length; i++) {
            int cl = ((int) M[i][collumns - 1]);
            if (cl == classe) {
                sum += Math.pow((M[i][collumn] - average), 2);
            }
        }
        return (Math.sqrt(sum));
    }

    public static Object[][] getCorrelationCoeficientClasses(float[][] Md, int[] features) {
        int lines = Md.length;
        int collumns = Md[0].length;
        int classes = fs.Main.maximumValue(Md, 0, lines - 1, collumns - 1, collumns - 1) + 1;
        double[][] avg_values = new double[classes][features.length];
        double[] std_values = new double[classes];
        double[] avg_classes = new double[classes];
        for (int f = 0; f < features.length; f++) {
            for (int c = 0; c < classes; c++) {
                avg_values[c][f] = MathRoutines.getColumnAverage(Md, features[f], c);
            }
        }
        double number_features = features.length;
        for (int c = 0; c < classes; c++) {
            for (int f = 0; f < features.length; f++) {
                avg_classes[c] += avg_values[c][f];
            }
            avg_classes[c] = avg_classes[c] / number_features;
        }
        for (int c = 0; c < classes; c++) {
            double sum = 0;
            for (int f = 0; f < features.length; f++) {
                sum += (Math.pow((avg_values[c][f] - avg_classes[c]), 2));
            }
            std_values[c] = Math.sqrt((sum / number_features));
        }
        Object[][] output = new Object[classes][classes];
        for (int c1 = 0; c1 < classes; c1++) {
            for (int c2 = c1 + 1; c2 < classes; c2++) {
                double sum_c1c2 = 0;
                for (int f = 0; f < features.length; f++) {
                    sum_c1c2 += (avg_values[c1][f] - avg_classes[c1]) * (avg_values[c2][f] - avg_classes[c2]);
                }
                sum_c1c2 = sum_c1c2 / number_features;
                double cc = sum_c1c2 / (std_values[c1] * std_values[c2]);
                output[c1][c2] = cc;
            }
        }
        return (output);
    }

    public static Object[][] getCorrelationCoeficientFeatures(float[][] Md, int[] features) {
        float[] std_values = new float[features.length];
        float[] avg_values = new float[features.length];
        for (int f = 0; f < features.length; f++) {
            avg_values[f] = MathRoutines.getColumnAverage(Md, features[f]);
        }
        for (int f = 0; f < features.length; f++) {
            double sum = 0;
            for (int lin = 0; lin < Md.length; lin++) {
                sum += (Math.pow((Md[lin][features[f]] - avg_values[f]), 2));
            }
            std_values[f] = ((Double) Math.sqrt((sum / (float) Md.length))).floatValue();
        }
        Object[][] output = new Object[features.length][features.length];
        for (int f1 = 0; f1 < features.length; f1++) {
            for (int f2 = f1 + 1; f2 < features.length; f2++) {
                double sum_f1f2 = 0;
                for (int lin = 0; lin < Md.length; lin++) {
                    sum_f1f2 += (Md[lin][features[f1]] - avg_values[f1]) * (Md[lin][features[f2]] - avg_values[f2]);
                }
                sum_f1f2 = sum_f1f2 / (double) Md.length;
                output[f1][f2] = sum_f1f2 / (std_values[f1] * std_values[f2]);
            }
        }
        return (output);
    }

    public static float[][] TransposeMatrix(float[][] M) {
        int lines = M.length;
        int columns = M[0].length;
        float[][] Mi = new float[columns][lines];
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < columns; j++) {
                Mi[j][i] = M[i][j];
            }
        }
        return Mi;
    }

    public static int Bin2Dec(String bin) {
        int dec = 0;
        int size = bin.length() - 1;
        int or = 1;
        for (int i = 0; i <= size; i++) {
            String vs = bin.substring(size - i, size - i + 1);
            int v = Integer.valueOf(vs);
            dec += v * or;
            if (or == 1) {
                or = 2;
            } else {
                or *= 2;
            }
        }
        return (dec);
    }

    public static int BaseN2Dec(String str, int base) {
        int dec = 0;
        int size = str.length() - 1;
        int or = 1;
        for (int i = 0; i <= size; i++) {
            String vs = str.substring(size - i, size - i + 1);
            int v = Integer.valueOf(vs);
            dec += v * or;
            if (or == 1) {
                or = base;
            } else {
                or *= base;
            }
        }
        return (dec);
    }

    public static String Dec2BaseN(int dec, int base, int size) {
        StringBuffer str = new StringBuffer();
        int q = dec % base, r = 0;
        while (dec >= base) {
            q = (dec / base);
            r = (dec % base);
            str.append(r + " ");
            dec = q;
        }
        str.append(q + " ");
        StringTokenizer s = new StringTokenizer(str.toString());
        while (s.countTokens() < size) {
            str.append(0 + " ");
            s = new StringTokenizer(str.toString());
        }
        str.setLength(str.length() - 1);
        return (str.reverse().toString());
    }

    public static boolean[] Str2Boolean(String bindigits) {
        StringTokenizer s = new StringTokenizer(bindigits);
        int size = s.countTokens();
        boolean[] vetb = new boolean[size];
        for (int i = 0; i < size; i++) {
            if (s.nextToken().equalsIgnoreCase("1")) {
                vetb[i] = true;
            }
        }
        return vetb;
    }

    public static boolean Int2Boolean(int bindigit) {
        if (bindigit == 1) {
            return true;
        } else return false;
    }

    public static int Boolean2Int(boolean bindigit) {
        if (bindigit) {
            return 1;
        } else return 0;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 27; i++) {
            System.out.println(Dec2BaseN(i, 3, 3));
        }
    }

    public static char[][] float2char(float[][] M) {
        int lines = M.length;
        int collumns = M[0].length;
        char[][] M2 = new char[lines][collumns];
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < collumns; j++) {
                int temp = (int) M[i][j];
                M2[i][j] = (char) temp;
            }
        }
        return M2;
    }
}
