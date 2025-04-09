package edu.ucla.stat.SOCR.analyses.data;

import edu.ucla.stat.SOCR.analyses.exception.*;
import edu.ucla.stat.SOCR.util.*;
import java.math.BigInteger;

public class ContingencyTable {

    private int grandTotal;

    private String[] rowName;

    private String[] colName;

    private int[] rowSumObserved;

    private int[] colSumObserved;

    private double[] rowSumExpected;

    private double[] colSumExpected;

    private int numberRow;

    private int numberCol;

    private double[][] observed;

    private double[][] expected;

    public ContingencyTable(double[][] observed) {
        try {
            this.observed = observed;
            this.numberRow = observed.length;
            this.numberCol = observed[0].length;
            this.rowSumObserved = new int[this.numberRow];
            this.colSumObserved = new int[this.numberCol];
            double[][] currentColumnArray = new double[this.numberCol][this.numberRow];
            for (int i = 0; i < this.numberRow; i++) {
                rowSumObserved[i] = (int) AnalysisUtility.sum(observed[i]);
                for (int j = 0; j < this.numberCol; j++) {
                    currentColumnArray[j][i] = observed[i][j];
                    this.grandTotal += observed[i][j];
                }
            }
            for (int j = 0; j < this.numberCol; j++) {
                colSumObserved[j] = (int) AnalysisUtility.sum(currentColumnArray[j]);
            }
        } catch (DataIsEmptyException e) {
        } catch (NullPointerException e) {
        }
    }

    public ContingencyTable(double[][] observed, String[] rowName, String[] colName) {
        try {
            this.observed = observed;
            this.numberRow = observed.length;
            this.numberCol = observed[0].length;
            this.rowName = rowName;
            this.colName = colName;
            this.rowSumObserved = new int[this.numberRow];
            this.colSumObserved = new int[this.numberCol];
            double[][] currentColumnArray = new double[this.numberCol][this.numberRow];
            for (int i = 0; i < this.numberRow; i++) {
                rowSumObserved[i] = (int) AnalysisUtility.sum(observed[i]);
                for (int j = 0; j < this.numberCol; j++) {
                    currentColumnArray[j][i] = observed[i][j];
                    this.grandTotal += observed[i][j];
                }
            }
            for (int j = 0; j < this.numberCol; j++) {
                colSumObserved[j] = (int) AnalysisUtility.sum(currentColumnArray[j]);
            }
        } catch (DataIsEmptyException e) {
        } catch (NullPointerException e) {
        }
    }

    public ContingencyTable(double[][] observed, double[][] expected, String[] rowName, String[] colName) {
        new ContingencyTable(observed, rowName, colName);
        this.setExpected(expected);
    }

    public void setExpected(double[][] expected) {
        double[][] currentColumnArray = new double[this.numberCol][this.numberRow];
        try {
            for (int i = 0; i < this.numberRow; i++) {
                rowSumExpected[i] = (int) AnalysisUtility.sum(expected[i]);
                for (int j = 0; j < this.numberCol; j++) {
                    currentColumnArray[j][i] = expected[i][j];
                }
            }
            for (int j = 0; j < this.numberCol; j++) {
                colSumExpected[j] = (int) AnalysisUtility.sum(currentColumnArray[j]);
            }
        } catch (DataIsEmptyException e) {
        } catch (NullPointerException e) {
        }
    }

    public void setExpectedProbabilities() throws DataException {
        double[] rowProbability = new double[this.numberRow];
        double[] colProbability = new double[this.numberCol];
        for (int i = 0; i < this.numberRow; i++) {
            rowProbability[i] = (double) rowSumObserved[i] / (double) grandTotal;
        }
        for (int i = 0; i < this.numberCol; i++) {
            colProbability[i] = (double) colSumObserved[i] / (double) grandTotal;
        }
        rowSumExpected = new double[this.numberRow];
        colSumExpected = new double[this.numberCol];
        expected = new double[this.numberRow][this.numberCol];
        for (int i = 0; i < this.numberRow; i++) {
            expected[i] = new double[this.numberCol];
            rowSumExpected[i] = ((double) grandTotal * rowProbability[i]);
            for (int j = 0; j < this.numberCol; j++) {
                if (i == 0) {
                    colSumExpected[j] = ((double) grandTotal * colProbability[j]);
                }
                expected[i][j] = grandTotal * rowProbability[i] * colProbability[j];
            }
        }
    }

    public void setExpectedProbabilities(double[] rowProbability, double[] colProbability) throws DataException {
        int rowLength = rowProbability.length;
        int colLength = colProbability.length;
        if (rowLength != this.numberRow || colLength != this.numberCol) {
            throw new DataException("Probablities not in correct dimension.");
        }
        rowSumExpected = new double[rowLength];
        colSumExpected = new double[colLength];
        expected = new double[rowLength][colLength];
        System.out.println("ContingencyTable: setExpceted row 2nd index =" + rowLength + "col 2st index= " + colLength);
        for (int i = 0; i < rowLength; i++) {
            expected[i] = new double[colLength];
            rowSumExpected[i] = ((double) grandTotal * rowProbability[i]);
            for (int j = 0; j < rowLength; j++) {
                if (i == 0) {
                    colSumExpected[j] = ((double) grandTotal * colProbability[j]);
                }
                expected[i][j] = grandTotal * rowProbability[i] * colProbability[j];
            }
        }
    }

    public int getGrandTotal() {
        return this.grandTotal;
    }

    public int[] getRowSumObserved() {
        for (int i = 0; i < this.numberRow; i++) {
        }
        return this.rowSumObserved;
    }

    public int[] getColSumObserved() {
        return this.colSumObserved;
    }

    public String[] getRowNames() {
        return this.rowName;
    }

    public int getNumberRow() {
        return this.numberRow;
    }

    public int getNumberCol() {
        return this.numberCol;
    }

    public String[] getColNames() {
        return this.colName;
    }

    public double getPCutoff() throws NumberTooBigException {
        BigInteger[] rowFactorial = new BigInteger[this.numberRow];
        BigInteger[] colFactorial = new BigInteger[this.numberCol];
        BigInteger totalFactorial = AnalysisUtility.factorialBigInt(this.grandTotal);
        for (int i = 0; i < this.numberRow; i++) {
            rowFactorial[i] = AnalysisUtility.factorialBigInt((int) this.rowSumObserved[i]);
        }
        for (int j = 0; j < this.numberCol; j++) {
            try {
                colFactorial[j] = AnalysisUtility.factorialBigInt((int) this.colSumObserved[j]);
            } catch (Exception e) {
            }
        }
        BigInteger nume = new BigInteger(0 + "");
        BigInteger deno = new BigInteger(1 + "");
        try {
            nume = AnalysisUtility.product(rowFactorial).multiply(AnalysisUtility.product(colFactorial));
            for (int i = 0; i < this.numberRow; i++) {
                for (int j = 0; j < this.numberCol; j++) {
                    deno = deno.multiply(AnalysisUtility.factorialBigInt((int) observed[i][j]));
                }
            }
        } catch (Exception e) {
        }
        deno = deno.multiply(AnalysisUtility.factorialBigInt((int) this.grandTotal));
        double result = nume.doubleValue() / deno.doubleValue();
        if ((new Double(result)).equals(new Double(Double.NaN))) {
            throw new NumberTooBigException("Some numbers in the calculation exceed tolarance");
        }
        return result;
    }

    public double getPCutoff(boolean b) {
        double[] rowFactorial = new double[this.numberRow];
        double[] colFactorial = new double[this.numberCol];
        double totalFactorial = AnalysisUtility.factorial((int) this.grandTotal);
        for (int i = 0; i < this.numberRow; i++) {
            rowFactorial[i] = AnalysisUtility.factorial((int) this.rowSumObserved[i]);
        }
        for (int j = 0; j < this.numberCol; j++) {
            try {
                colFactorial[j] = AnalysisUtility.factorial((int) this.colSumObserved[j]);
            } catch (Exception e) {
            }
        }
        double nume = 0;
        double deno = 1;
        try {
            nume = AnalysisUtility.product(rowFactorial) * AnalysisUtility.product(colFactorial);
            for (int i = 0; i < this.numberRow; i++) {
                for (int j = 0; j < this.numberCol; j++) {
                    deno *= AnalysisUtility.factorial((int) observed[i][j]);
                }
            }
        } catch (Exception e) {
        }
        deno *= totalFactorial;
        return nume / deno;
    }

    public double findPearsonChiSquare() {
        double[][] diff = new double[this.numberRow][this.numberCol];
        double chi = 0;
        for (int i = 0; i < this.numberRow; i++) {
            for (int j = 0; j < this.numberCol; j++) {
                diff[i][j] = expected[i][j] - observed[i][j];
                chi += (diff[i][j] * diff[i][j]) / expected[i][j];
            }
        }
        return chi;
    }

    public double[][] getExpected() {
        return this.expected;
    }

    public double getObserved(int i, int j) {
        if (0 <= i && i < this.numberRow && 0 <= j && j < this.numberCol) return this.observed[i][j]; else return 0;
    }

    public double[][] getObserved() {
        return this.observed;
    }

    public int[] getRowSum() {
        return this.rowSumObserved;
    }

    public int[] getColSum() {
        return this.colSumObserved;
    }

    public int getDF() {
        int df = ((this.numberRow - 1) * (this.numberCol - 1));
        return df;
    }

    public static void main(String[] args) {
        double[][] test = { { 100, 200, 10 }, { 1, 4, 10 } };
        String[] rname = { "a", "an", "this", "that", "what", "without" };
        String[] cname = { "Sense and Sensibitilty", "Emma", "Sanction I" };
        ContingencyTable ct = new ContingencyTable(test);
        ct.getRowNames();
        ct.getColNames();
        ct.getRowSumObserved();
        ct.getColSumObserved();
        ct.getGrandTotal();
        try {
            System.out.println("pCutoff = " + ct.getPCutoff());
        } catch (NumberTooBigException e) {
        }
        double[] colP = { .5, .5 };
        try {
            ct.setExpectedProbabilities();
        } catch (Exception e) {
        }
        ct.findPearsonChiSquare();
        ct.getDF();
    }
}
