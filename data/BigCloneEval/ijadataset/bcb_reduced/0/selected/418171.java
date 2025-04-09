package edu.ucla.stat.SOCR.analyses.model;

import edu.ucla.stat.SOCR.analyses.result.*;
import edu.ucla.stat.SOCR.analyses.data.*;
import edu.ucla.stat.SOCR.analyses.exception.*;
import edu.ucla.stat.SOCR.util.AnalysisUtility;
import edu.ucla.stat.SOCR.util.QSortAlgorithm;
import edu.ucla.stat.SOCR.distributions.StudentDistribution;
import edu.ucla.stat.SOCR.distributions.NormalDistribution;
import java.util.*;
import java.math.BigInteger;
import edu.ucla.stat.SOCR.analyses.data.DataType;

public class TwoIndependentWilcoxon implements Analysis {

    private static final String X_DATA_TYPE = DataType.QUANTITATIVE;

    private static final String Y_DATA_TYPE = DataType.QUANTITATIVE;

    private String type = "TwoIndependentWilcoxon";

    private HashMap<String, Object> resultMap = null;

    private boolean isLargeSample = false;

    public static int SIZE_LARGE_SAMPLE = 10;

    private static int SIZE_BIG_INT = 1;

    private static String U_STAT_EXPECTATION_FORMULA = "(n_1 * n_2)/ 2";

    private static String U_STAT_VARIANCE_FORMULA = "n_1 * n_2 * (n_1 + n_2 + 1) / 12";

    private String dataSummary1 = "";

    private String dataSummary2 = "";

    private String exactComboSummary = "";

    private boolean pValueAtLeft = true;

    private int sameSizeOfSmallerRankSum = 0;

    private int sameSizeOfLargerRankSum = 0;

    HashMap<String, Object> texture = new HashMap<String, Object>();

    HashMap<String, Object> completeMap = new HashMap<String, Object>();

    String nameX = null, nameY = null;

    public String getAnalysisType() {
        return type;
    }

    public Result analyze(Data data, short analysisType) throws WrongAnalysisException, DataIsEmptyException {
        Result result = null;
        if (analysisType != AnalysisType.TWO_INDEPENDENT_WILCOXON) throw new WrongAnalysisException();
        HashMap<String, Object> xMap = data.getMapX();
        HashMap<String, Object> yMap = data.getMapY();
        if (xMap == null || yMap == null) throw new WrongAnalysisException();
        Set<String> keySet = xMap.keySet();
        Iterator<String> iterator = keySet.iterator();
        String keys = "";
        double x[] = null;
        double y[] = null;
        while (iterator.hasNext()) {
            keys = (String) iterator.next();
            try {
                Class cls = keys.getClass();
            } catch (Exception e) {
            }
            Column xColumn = (Column) xMap.get(keys);
            String xDataType = xColumn.getDataType();
            if (!xDataType.equalsIgnoreCase(X_DATA_TYPE)) {
                throw new WrongAnalysisException(WrongAnalysisException.ERROR_MESSAGE);
            }
            x = xColumn.getDoubleArray();
        }
        nameX = keys;
        double combo[] = null;
        keySet = yMap.keySet();
        iterator = keySet.iterator();
        while (iterator.hasNext()) {
            keys = (String) iterator.next();
            try {
                Class cls = keys.getClass();
            } catch (Exception e) {
            }
        }
        nameY = keys;
        Column yColumn = (Column) yMap.get(keys);
        String yDataType = yColumn.getDataType();
        if (!yDataType.equalsIgnoreCase(Y_DATA_TYPE)) {
            throw new WrongAnalysisException(WrongAnalysisException.ERROR_MESSAGE);
        }
        y = yColumn.getDoubleArray();
        for (int i = 0; i < y.length; i++) {
        }
        Arrays.sort(x);
        Arrays.sort(y);
        for (int i = 0; i < x.length; i++) {
        }
        for (int i = 0; i < y.length; i++) {
        }
        return doAnalysis(x, y);
    }

    private TwoIndependentWilcoxonResult doAnalysis(double[] x, double[] y) throws DataIsEmptyException {
        HashMap<String, Object> texture = new HashMap<String, Object>();
        TwoIndependentWilcoxonResult result = new TwoIndependentWilcoxonResult(texture);
        int nX = x.length;
        int nY = y.length;
        int nTotal = nX + nY;
        if (nX > SIZE_LARGE_SAMPLE || nY > SIZE_LARGE_SAMPLE || nTotal > SIZE_LARGE_SAMPLE) {
            isLargeSample = true;
        }
        double meanX = AnalysisUtility.mean(x);
        double meanY = AnalysisUtility.mean(y);
        DataCase[] groupA = new DataCase[nX];
        DataCase[] groupB = new DataCase[nY];
        for (int i = 0; i < nX; i++) {
            groupA[i] = new DataCase(x[i], nameX);
        }
        for (int i = 0; i < nY; i++) {
            groupB[i] = new DataCase(y[i], nameY);
        }
        texture.put((String) TwoIndependentWilcoxonResult.MEAN_X, new Double(meanX));
        texture.put((String) TwoIndependentWilcoxonResult.MEAN_Y, new Double(meanY));
        WilcoxonRankTest wilcoxonRankTest = new WilcoxonRankTest(groupA, groupB);
        DataCase[] combo = wilcoxonRankTest.getRankedArray();
        double sampleSize1 = 0, sampleSize2 = 0;
        double sum1 = 0, sum2 = 0;
        String groupName1 = "";
        String groupName2 = "";
        StringBuffer sb1 = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        int iA = 0, iB = 0;
        int listLength = 5;
        if (nX <= nY) {
            groupName1 = nameX;
            groupName2 = nameY;
            sampleSize1 = nX;
            sampleSize2 = nY;
            sb1.append(nameX + ": ");
            sb2.append(nameY + ": ");
            for (int i = 0; i < combo.length; i++) {
                if ((combo[i].getGroup()).equals(nameX)) {
                    sum1 = sum1 + combo[i].getRank();
                    sb1.append(combo[i].getValue() + "(" + combo[i].getRank() + "), ");
                    if (iA % listLength == listLength - 1) {
                        sb1.append("\n\t");
                    }
                    iA++;
                } else {
                    sb2.append(combo[i].getValue() + "(" + combo[i].getRank() + "), ");
                    if (iB % listLength == listLength - 1) {
                        sb2.append("\n\t");
                    }
                    iB++;
                }
            }
            sum2 = .5 * nTotal * (nTotal + 1) - sum1;
        } else {
            groupName1 = nameY;
            groupName2 = nameX;
            sampleSize1 = nY;
            sampleSize2 = nX;
            sb1.append(nameY + ": ");
            sb2.append(nameX + ": ");
            iA = 0;
            iB = 0;
            for (int i = 0; i < combo.length; i++) {
                if ((combo[i].getGroup()).equals(nameY)) {
                    sum1 = sum1 + combo[i].getRank();
                    sb1.append(combo[i].getValue() + "(" + combo[i].getRank() + "), ");
                    if (iA % listLength == listLength - 1) {
                        sb1.append("\n\t");
                    }
                    iA++;
                } else {
                    sb2.append(combo[i].getValue() + "(" + combo[i].getRank() + "), ");
                    if (iB % listLength == listLength - 1) {
                        sb2.append("\n\t");
                    }
                    iB++;
                }
            }
            sum2 = .5 * nTotal * (nTotal + 1) - sum1;
        }
        dataSummary1 = sb1.toString();
        dataSummary2 = sb2.toString();
        texture.put((String) TwoIndependentWilcoxonResult.DATA_SUMMARY_1, dataSummary1);
        texture.put((String) TwoIndependentWilcoxonResult.DATA_SUMMARY_2, dataSummary2);
        sampleSize2 = nTotal - sampleSize1;
        double uStat1 = 0, uStat2 = 0;
        uStat1 = (sampleSize1 * sampleSize2 + .5 * (sampleSize1 * (sampleSize1 + 1))) - sum1;
        uStat2 = (sampleSize1 * sampleSize2 + .5 * (sampleSize2 * (sampleSize2 + 1))) - sum2;
        if ((groupName1.equalsIgnoreCase(nameX) && sum1 <= sum2) || (groupName1.equalsIgnoreCase(nameY) && sum1 >= sum2)) {
            pValueAtLeft = true;
        } else {
            pValueAtLeft = false;
        }
        double commonTerm = factorialRatio((int) sampleSize1, (int) (sampleSize1 + sampleSize2));
        double cummulativePValue = 0;
        boolean hasTie = wilcoxonRankTest.getHasTie();
        int maxNumberTies = wilcoxonRankTest.getMaxNumberTies();
        texture.put((String) TwoIndependentWilcoxonResult.RANK_SUM_SMALL, new Double(sum1));
        texture.put((String) TwoIndependentWilcoxonResult.RANK_SUM_LARGE, new Double(sum2));
        texture.put((String) TwoIndependentWilcoxonResult.U_STAT_SMALL, new Double(uStat1));
        texture.put((String) TwoIndependentWilcoxonResult.U_STAT_LARGE, new Double(uStat2));
        texture.put((String) TwoIndependentWilcoxonResult.GROUP_NAME_SMALL, groupName1);
        texture.put((String) TwoIndependentWilcoxonResult.GROUP_NAME_LARGE, groupName2);
        double meanU = .5 * nX * nY;
        texture.put((String) TwoIndependentWilcoxonResult.IS_LARGE_SAMPLE, new Boolean(isLargeSample));
        if (isLargeSample) {
            double varU = 0, zScore = 0, pValueTwoSided = 0, pValueOneSided = 0;
            double nume = nX * nY * (nX + nY + 1);
            varU = nume / 12;
            zScore = (uStat1 - meanU) / Math.sqrt(varU);
            NormalDistribution nDistribution = new NormalDistribution();
            if (zScore >= 0) {
                pValueTwoSided = 2 * (1 - nDistribution.getCDF(zScore));
            } else {
                pValueTwoSided = 2 * nDistribution.getCDF(zScore);
            }
            pValueOneSided = 1 - nDistribution.getCDF(Math.abs(zScore));
            texture.put((String) TwoIndependentWilcoxonResult.U_STAT_EXPECTATION_FORMULA, this.U_STAT_EXPECTATION_FORMULA);
            texture.put((String) TwoIndependentWilcoxonResult.U_STAT_VARIANCE_FORMULA, this.U_STAT_VARIANCE_FORMULA);
            texture.put((String) TwoIndependentWilcoxonResult.MEAN_U, new Double(meanU));
            texture.put((String) TwoIndependentWilcoxonResult.VAR_U, new Double(varU));
            texture.put((String) TwoIndependentWilcoxonResult.Z_SCORE, new Double(zScore));
            texture.put((String) Result.P_VALUE_TWO_SIDED, new Double(pValueTwoSided));
            texture.put((String) Result.P_VALUE_ONE_SIDED, new Double(pValueOneSided));
        } else {
            double minPossible = 0;
            double maxPossible = 0;
            TreeMap<String, Object> xKey = new TreeMap<String, Object>();
            double dec = 0, inc = 0;
            double currentValue = 0;
            TreeMap<String, Object> countMap = new TreeMap<String, Object>();
            if (!hasTie && pValueAtLeft) {
                dec = 1;
                currentValue = Math.min(sum1, sum2);
                minPossible = AnalysisUtility.sumPossitiveIntegerSequenceFromOne((int) nX);
                int numberCombo = 0;
                while (currentValue >= minPossible) {
                    xKey.put(currentValue + "", "");
                    currentValue -= dec;
                    numberCombo++;
                }
                cummulativePValue = findExactPValue(xKey, combo, (int) sampleSize1, (int) sampleSize2, commonTerm);
            } else if (!hasTie && !pValueAtLeft) {
                inc = 1;
                currentValue = Math.max(sum1, sum2);
                maxPossible = AnalysisUtility.sumPossitiveIntegerSequencePartial((int) sampleSize1, (int) (sampleSize1 + sampleSize2));
                while (currentValue <= maxPossible) {
                    xKey.put(currentValue + "", "");
                    currentValue += inc;
                }
                cummulativePValue = findExactPValue(xKey, combo, (int) sampleSize2, (int) sampleSize1, commonTerm);
            } else if (hasTie && pValueAtLeft) {
                dec = .5;
                currentValue = Math.min(sum1, sum2);
                minPossible = AnalysisUtility.sumPossitiveIntegerSequenceFromOne((int) sampleSize1);
                while (currentValue >= minPossible) {
                    xKey.put(currentValue + "", "");
                    currentValue -= dec;
                }
                cummulativePValue = findExactPValue(xKey, combo, (int) sampleSize1, (int) sampleSize2, commonTerm);
            } else if (hasTie && !pValueAtLeft) {
                inc = .5;
                currentValue = Math.max(sum1, sum2);
                maxPossible = AnalysisUtility.sumPossitiveIntegerSequencePartial((int) sampleSize2, (int) (sampleSize1 + sampleSize2));
                while (currentValue <= maxPossible) {
                    xKey.put(currentValue + "", "");
                    currentValue += inc;
                }
                cummulativePValue = findExactPValue(xKey, combo, (int) sampleSize2, (int) sampleSize1, commonTerm);
            } else {
            }
            if (pValueAtLeft && groupName1.equalsIgnoreCase(nameX) && sum1 < sum2) {
            }
            texture.put(TwoIndependentWilcoxonResult.P_VALUE_ONE_SIDED, new Double(cummulativePValue));
            texture.put(TwoIndependentWilcoxonResult.P_VALUE_TWO_SIDED, new Double(2 * cummulativePValue));
        }
        return result;
    }

    public static void test(DataCase[] combo, int nX, int nY, String nameX, String nameY) {
        double sampleSize1 = 0;
        double sum1 = 0;
        if (nX < nY) {
            sampleSize1 = nX;
            for (int i = 0; i < combo.length; i++) {
                if ((combo[i].getGroup()).equals(nameX)) {
                    sum1 = sum1 + combo[i].getRank();
                }
            }
        } else {
            sampleSize1 = nY;
            for (int i = 0; i < combo.length; i++) {
                if ((combo[i].getGroup()).equals(nameY)) {
                    sum1 = sum1 + combo[i].getRank();
                }
            }
        }
    }

    private static double factorialRatio(int m, int n) {
        if (n < m) {
            return 0;
        }
        int mFactorial = AnalysisUtility.factorial(m);
        int diffFactorial = AnalysisUtility.factorial(n - m);
        int nFactorial = AnalysisUtility.factorial(n);
        return ((double) (mFactorial) / (double) nFactorial) * (double) diffFactorial;
    }

    private static double numberPermutation(int m, int n, double k) {
        if (k < 0) {
            return 0;
        } else if (m <= 0 && n <= 0) {
            return 0;
        } else if ((m == 0 || n == 0) && k == 0) {
            return 1;
        } else if ((m == 0 || n == 0) && k != 0) {
            return 0;
        } else {
            double prob1 = numberPermutation(m, n - 1, k - m);
            double prob2 = numberPermutation(m - 1, n, k);
            return prob1 + prob2;
        }
    }

    public static double cummulativeGE(int nX, int nY, double u) {
        int n = nX + nY;
        int lastW = 0;
        for (int i = n; i > n - nX; i--) {
            lastW = i + lastW;
        }
        double output = 0;
        double increment = u;
        double prob = 0;
        do {
            if (nX <= nY) {
                prob = exactProb(nX, nY, increment);
            } else {
                prob = exactProb(nY, nX, increment);
            }
            output = output + prob;
            increment++;
        } while (increment <= lastW);
        return output;
    }

    public static double cummulativeLE(int nX, int nY, double u) {
        int n = nX + nY;
        int firstW = (int) (.5 * nX * (nX + 1));
        double output = 0;
        double decrement = u;
        while (decrement >= firstW) {
            output = output + exactProb(nX, nY, decrement);
            decrement--;
        }
        return output;
    }

    public static double exactProb(int nX, int nY, double u) {
        int countw = 0;
        int n = nX + nY;
        int firstW = (int) (.5 * nX * (nX + 1));
        int lastW = 0;
        for (int i = n; i > n - nX; i--) {
            lastW = i + lastW;
        }
        double a = AnalysisUtility.factorial(nX);
        double b = AnalysisUtility.factorial(nY);
        double c = AnalysisUtility.factorial(n);
        double factorialPart = (a * b) / c;
        double possibleNumPermutation = numberPermutation(nX, nY, u - firstW);
        return possibleNumPermutation * factorialPart;
    }

    public static double exactPValue(int nX, int nY, double u) {
        int countw = 0;
        int n = nX + nY;
        int firstW = (int) (.5 * nX * (nX + 1));
        int lastW = 0;
        for (int i = n; i > n - nX; i--) {
            lastW = i + lastW;
        }
        double middle = (firstW + lastW) / 2;
        if (u <= middle) {
            return (cummulativeLE(nX, nY, u));
        } else {
            return (cummulativeGE(nX, nY, (firstW + lastW) - u));
        }
    }

    private double findExactPValue(TreeMap<String, Object> xKey, DataCase[] combo, int sampleSize1, int sampleSize2, double commonTerm) {
        TreeMap<String, Object> countMap = new TreeMap<String, Object>();
        double cummulativePValue = 0;
        Set<String> keySet = xKey.keySet();
        Iterator<String> iterator = keySet.iterator();
        String key = "";
        int numberPossibilities = 0;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            double tempSum = 0;
            int sampleSizeTotal = sampleSize1 + sampleSize2;
            switch(sampleSize1) {
                case 1:
                    {
                        numberPossibilities = 1;
                        break;
                    }
                case 2:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                tempSum = combo[i].getRank() + combo[j].getRank();
                                if (tempSum == Double.parseDouble(key)) numberPossibilities++;
                            }
                        }
                        break;
                    }
                case 3:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank();
                                    if (tempSum == Double.parseDouble(key)) {
                                        numberPossibilities++;
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 4:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank();
                                        if (tempSum == Double.parseDouble(key)) {
                                            numberPossibilities++;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 5:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        for (int n = m + 1; n < sampleSizeTotal; n++) {
                                            tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank() + combo[n].getRank();
                                            if (tempSum == Double.parseDouble(key)) {
                                                numberPossibilities++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 6:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        for (int n = m + 1; n < sampleSizeTotal; n++) {
                                            for (int p = n + 1; p < sampleSizeTotal; p++) {
                                                tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank() + combo[n].getRank() + combo[p].getRank();
                                                if (tempSum == Double.parseDouble(key)) {
                                                    numberPossibilities++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 7:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        for (int n = m + 1; n < sampleSizeTotal; n++) {
                                            for (int p = n + 1; p < sampleSizeTotal; p++) {
                                                for (int q = p + 1; q < sampleSizeTotal; q++) {
                                                    tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank() + combo[n].getRank() + combo[p].getRank() + combo[q].getRank();
                                                    if (tempSum == Double.parseDouble(key)) numberPossibilities++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 8:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        for (int n = m + 1; n < sampleSizeTotal; n++) {
                                            for (int p = n + 1; p < sampleSizeTotal; p++) {
                                                for (int q = p + 1; q < sampleSizeTotal; q++) {
                                                    for (int r = q + 1; r < sampleSizeTotal; r++) {
                                                        tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank() + combo[n].getRank() + combo[p].getRank() + combo[q].getRank() + combo[r].getRank();
                                                        if (tempSum == Double.parseDouble(key)) numberPossibilities++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                case 9:
                    {
                        for (int i = 0; i < sampleSizeTotal; i++) {
                            for (int j = i + 1; j < sampleSizeTotal; j++) {
                                for (int k = j + 1; k < sampleSizeTotal; k++) {
                                    for (int m = k + 1; m < sampleSizeTotal; m++) {
                                        for (int n = m + 1; n < sampleSizeTotal; n++) {
                                            for (int p = n + 1; p < sampleSizeTotal; p++) {
                                                for (int q = p + 1; q < sampleSizeTotal; q++) {
                                                    for (int r = q + 1; r < sampleSizeTotal; r++) {
                                                        for (int s = r + 1; s < sampleSizeTotal; s++) {
                                                            tempSum = combo[i].getRank() + combo[j].getRank() + combo[k].getRank() + combo[m].getRank() + combo[n].getRank() + combo[p].getRank() + combo[q].getRank() + combo[r].getRank() + combo[s].getRank();
                                                            if (tempSum == Double.parseDouble(key)) numberPossibilities++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                default:
                    {
                    }
            }
            countMap.put(key, new Integer(numberPossibilities));
            numberPossibilities = 0;
        }
        keySet = countMap.keySet();
        iterator = keySet.iterator();
        key = "";
        int currentCount = 0;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            currentCount = ((Integer) countMap.get(key)).intValue();
            cummulativePValue += currentCount * commonTerm;
        }
        return cummulativePValue;
    }

    public static void main(String args[]) {
        double singleProb = 0;
        int nX = 3;
        int nY = 4;
    }
}
