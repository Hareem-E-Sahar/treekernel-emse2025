package flanagan.analysis;

import java.util.*;
import java.text.*;
import java.awt.*;
import flanagan.math.*;
import flanagan.io.*;
import flanagan.analysis.*;
import flanagan.plot.*;

public class PCA extends Scores {

    private Matrix data = null;

    private Matrix dataMinusMeans = null;

    private Matrix dataMinusMeansTranspose = null;

    private Matrix covarianceMatrix = null;

    private Matrix correlationMatrix = null;

    private double[] eigenValues = null;

    private double[] orderedEigenValues = null;

    private int[] eigenValueIndices = null;

    private double eigenValueTotal = 0.0;

    private double[] rotatedEigenValues = null;

    private double[] usRotatedEigenValues = null;

    private int nMonteCarlo = 200;

    private double[][] randomEigenValues = null;

    private double[] randomEigenValuesMeans = null;

    private double[] randomEigenValuesSDs = null;

    private double[] randomEigenValuesPercentiles = null;

    private double percentile = 95.0;

    private boolean gaussianDeviates = false;

    private double[] proportionPercentage = null;

    private double[] cumulativePercentage = null;

    private double[] rotatedProportionPercentage = null;

    private double[] rotatedCumulativePercentage = null;

    private double[][] eigenVectorsAsColumns = null;

    private double[][] eigenVectorsAsRows = null;

    private double[][] orderedEigenVectorsAsColumns = null;

    private double[][] orderedEigenVectorsAsRows = null;

    private double[][] loadingFactorsAsColumns = null;

    private double[][] loadingFactorsAsRows = null;

    private double[][] rotatedLoadingFactorsAsColumns = null;

    private double[][] rotatedLoadingFactorsAsRows = null;

    private double[][] usRotatedLoadingFactorsAsColumns = null;

    private double[][] usRotatedLoadingFactorsAsRows = null;

    private double[] communalities = null;

    private boolean covRhoOption = false;

    private int greaterThanOneLimit = 0;

    private int percentileCrossover = 0;

    private int meanCrossover = 0;

    private int nVarimaxMax = 1000;

    private int nVarimax = 0;

    private double varimaxTolerance = 0.0001;

    private boolean varimaxOption = true;

    private boolean pcaDone = false;

    private boolean monteCarloDone = false;

    private boolean rotationDone = false;

    public PCA() {
        super.trunc = 4;
    }

    public void useCovarianceMatrix() {
        this.covRhoOption = true;
    }

    public void useCorrelationMatrix() {
        this.covRhoOption = false;
    }

    public void useNormalVarimax() {
        this.varimaxOption = true;
    }

    public void useRawVarimax() {
        this.varimaxOption = false;
    }

    public String getVarimaxOption() {
        if (this.varimaxOption) {
            return "normal varimax option";
        } else {
            return "raw varimax option";
        }
    }

    public void setNumberOfSimulations(int nSimul) {
        this.nMonteCarlo = nSimul;
    }

    public int getNumberOfSimulations() {
        return this.nMonteCarlo;
    }

    public void useGaussianDeviates() {
        this.gaussianDeviates = true;
    }

    public void useUniformDeviates() {
        this.gaussianDeviates = false;
    }

    public void setParallelAnalysisPercentileValue(double percent) {
        this.percentile = percent;
    }

    public double getParallelAnalysisPercentileValue() {
        return this.percentile;
    }

    public void pca() {
        if (!this.pcaDone) {
            if (this.nItems == 1) throw new IllegalArgumentException("You have entered only one item - PCA is not meaningful");
            if (this.nPersons == 1) throw new IllegalArgumentException("You have entered only one score or measurement source - PCA is not meaningful");
            if (!this.dataPreprocessed) this.preprocessData();
            this.data = new Matrix(super.scores0);
            this.dataMinusMeans = this.data.subtractRowMeans();
            this.dataMinusMeansTranspose = this.dataMinusMeans.transpose();
            this.covarianceMatrix = this.dataMinusMeans.times(this.dataMinusMeansTranspose);
            double denom = this.nPersons;
            if (!super.nFactorOption) denom -= 1.0;
            this.covarianceMatrix = this.covarianceMatrix.times(1.0 / denom);
            double[][] cov = this.covarianceMatrix.getArrayCopy();
            double[][] corr = new double[this.nItems][this.nItems];
            for (int i = 0; i < this.nItems; i++) {
                for (int j = 0; j < this.nItems; j++) {
                    if (i == j) {
                        corr[i][j] = 1.0;
                    } else {
                        corr[i][j] = cov[i][j] / Math.sqrt(cov[i][i] * cov[j][j]);
                    }
                }
            }
            this.correlationMatrix = new Matrix(corr);
            Matrix forEigen = null;
            if (covRhoOption) {
                forEigen = this.covarianceMatrix;
            } else {
                forEigen = this.correlationMatrix;
            }
            this.eigenValues = forEigen.getEigenValues();
            this.orderedEigenValues = forEigen.getSortedEigenValues();
            this.eigenValueIndices = forEigen.eigenValueIndices();
            this.eigenVectorsAsColumns = forEigen.getEigenVectorsAsColumns();
            this.eigenVectorsAsRows = forEigen.getEigenVectorsAsRows();
            this.orderedEigenVectorsAsColumns = forEigen.getSortedEigenVectorsAsColumns();
            this.orderedEigenVectorsAsRows = forEigen.getSortedEigenVectorsAsRows();
            ArrayMaths am = new ArrayMaths(this.orderedEigenValues);
            double total = am.sum();
            am = am.times(100.0 / total);
            this.proportionPercentage = am.array();
            this.cumulativePercentage = new double[this.nItems];
            this.cumulativePercentage[0] = this.proportionPercentage[0];
            this.eigenValueTotal = 0.0;
            for (int i = 1; i < this.nItems; i++) {
                this.cumulativePercentage[i] = this.cumulativePercentage[i - 1] + this.proportionPercentage[i];
                this.eigenValueTotal += this.eigenValues[i];
            }
            boolean test = true;
            int counter = 0;
            while (test) {
                if (this.orderedEigenValues[counter] < 1.0) {
                    this.greaterThanOneLimit = counter;
                    test = false;
                } else {
                    counter++;
                    if (counter == this.nItems) {
                        this.greaterThanOneLimit = counter;
                        test = false;
                    }
                }
            }
            this.loadingFactorsAsColumns = new double[this.nItems][this.nItems];
            this.loadingFactorsAsRows = new double[this.nItems][this.nItems];
            for (int i = 0; i < this.nItems; i++) {
                for (int j = 0; j < this.nItems; j++) {
                    this.loadingFactorsAsColumns[i][j] = this.orderedEigenVectorsAsColumns[i][j] * Math.sqrt(Math.abs(this.orderedEigenValues[j]));
                    this.loadingFactorsAsRows[i][j] = this.orderedEigenVectorsAsRows[i][j] * Math.sqrt(Math.abs(this.orderedEigenValues[i]));
                }
            }
            this.communalities = new double[this.nItems];
            for (int k = 0; k < this.nItems; k++) {
                double sum = 0.0;
                for (int j = 0; j < this.nItems; j++) sum += loadingFactorsAsRows[j][k] * loadingFactorsAsRows[j][k];
                this.communalities[k] = sum;
            }
        }
        this.pcaDone = true;
    }

    public void monteCarlo() {
        if (!pcaDone) this.pca();
        double[] rowMeans = super.rawItemMeans();
        double[] rowSDs = super.rawItemStandardDeviations();
        double[][] randomData = new double[super.nItems][super.nPersons];
        this.randomEigenValues = new double[this.nMonteCarlo][super.nItems];
        PsRandom rr = new PsRandom();
        for (int i = 0; i < this.nMonteCarlo; i++) {
            for (int j = 0; j < this.nItems; j++) {
                if (this.gaussianDeviates) {
                    randomData[j] = rr.gaussianArray(rowMeans[j], rowSDs[j], super.nPersons);
                } else {
                    randomData[j] = rr.doubleArray(super.nPersons);
                    randomData[j] = Stat.scale(randomData[j], rowMeans[j], rowSDs[j]);
                }
            }
            PCA pca = new PCA();
            if (this.covRhoOption) {
                pca.useCovarianceMatrix();
            } else {
                pca.useCorrelationMatrix();
            }
            pca.enterScoresAsRowPerItem(randomData);
            this.randomEigenValues[i] = pca.orderedEigenValues();
        }
        Matrix mat = new Matrix(randomEigenValues);
        this.randomEigenValuesMeans = mat.columnMeans();
        this.randomEigenValuesSDs = mat.columnStandardDeviations();
        this.randomEigenValuesPercentiles = new double[this.nItems];
        int pIndex1 = (int) Math.ceil(this.nMonteCarlo * this.percentile / 100.0);
        int pIndex2 = pIndex1 - 1;
        double factor = (this.percentile * this.nMonteCarlo / 100.0 - pIndex2);
        pIndex1--;
        pIndex2--;
        for (int j = 0; j < this.nItems; j++) {
            double[] ordered = new double[this.nMonteCarlo];
            for (int k = 0; k < this.nMonteCarlo; k++) ordered[k] = this.randomEigenValues[k][j];
            ArrayMaths am = new ArrayMaths(ordered);
            am = am.sort();
            ordered = am.array();
            this.randomEigenValuesPercentiles[j] = ordered[pIndex2] + factor * (ordered[pIndex1] - ordered[pIndex2]);
        }
        boolean test = true;
        int counter = 0;
        while (test) {
            if (this.orderedEigenValues[counter] <= this.randomEigenValuesPercentiles[counter]) {
                this.percentileCrossover = counter;
                test = false;
            } else {
                counter++;
                if (counter == this.nItems) {
                    this.percentileCrossover = counter;
                    test = false;
                }
            }
        }
        test = true;
        counter = 0;
        while (test) {
            if (this.orderedEigenValues[counter] <= this.randomEigenValuesMeans[counter]) {
                this.meanCrossover = counter;
                test = false;
            } else {
                counter++;
                if (counter == this.nItems) {
                    this.meanCrossover = counter;
                    test = false;
                }
            }
        }
        this.monteCarloDone = true;
    }

    public void screePlotDataAlone() {
        if (!this.pcaDone) this.pca();
        double[] components = new double[super.nItems];
        for (int i = 0; i < this.nItems; i++) components[i] = i + 1;
        PlotGraph pg = new PlotGraph(components, this.orderedEigenValues);
        pg.setGraphTitle("Principal Component Analysis Scree Plot");
        pg.setXaxisLegend("Component");
        pg.setYaxisLegend("Eigenvalues");
        pg.setLine(3);
        pg.setPoint(1);
        pg.plot();
    }

    public void screePlot() {
        if (!this.pcaDone) this.pca();
        if (!this.monteCarloDone) this.monteCarlo();
        double[][] plotData = new double[6][super.nItems];
        double[] components = new double[super.nItems];
        for (int i = 0; i < this.nItems; i++) components[i] = i + 1;
        plotData[0] = components;
        plotData[1] = this.orderedEigenValues;
        plotData[2] = components;
        plotData[3] = this.randomEigenValuesPercentiles;
        plotData[4] = components;
        plotData[5] = this.randomEigenValuesMeans;
        PlotGraph pg = new PlotGraph(plotData);
        pg.setErrorBars(2, this.randomEigenValuesSDs);
        if (this.gaussianDeviates) {
            pg.setGraphTitle("Principal Component Analysis Scree Plot with Parallel Analysis using Gaussian deviates (" + nMonteCarlo + " simulations)");
        } else {
            pg.setGraphTitle("Principal Component Analysis Scree Plot with Parallel Analysis using uniform deviates (" + nMonteCarlo + " simulations)");
        }
        pg.setGraphTitle2("Closed squares - data eigenvalues; open circles = Monte Carlo eigenvalue " + this.percentile + "% percentiles; error bars = standard deviations about the Monte carlo means (crosses)");
        pg.setXaxisLegend("Component");
        pg.setYaxisLegend("Eigenvalue");
        int[] line = { 3, 0, 3 };
        pg.setLine(line);
        int point[] = { 5, 1, 7 };
        pg.setPoint(point);
        pg.plot();
    }

    public void setVarimaxTolerance(double tolerance) {
        this.varimaxTolerance = tolerance;
    }

    public void setVarimaxMaximumIterations(int max) {
        this.nVarimaxMax = max;
    }

    public int getVarimaxIterations() {
        return this.nVarimax;
    }

    public void varimaxRotation(int nFactors) {
        if (!this.pcaDone) this.pca();
        if (this.varimaxOption) {
            this.normalVarimaxRotation(nFactors);
        } else {
            this.rawVarimaxRotation(nFactors);
        }
    }

    public void varimaxRotation(double[][] loadingFactorMatrix) {
        if (this.varimaxOption) System.out.println("Method varimaxRotation: communality weights not supplied - raw varimax option used");
        this.rawVarimaxRotationInHouse(loadingFactorMatrix);
    }

    public void varimaxRotation(double[][] loadingFactorMatrix, double[] communalityWeights) {
        if (this.varimaxOption) {
            this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
        } else {
            System.out.println("Method varimaxRotation: raw varimax option chosen, supplied communality weights ignored");
            this.rawVarimaxRotationInHouse(loadingFactorMatrix);
        }
    }

    public void rawVarimaxRotation(int nFactors) {
        if (!this.pcaDone) this.pca();
        double[][] loadingFactorMatrix = new double[nFactors][this.nItems];
        for (int i = 0; i < nFactors; i++) loadingFactorMatrix[i] = this.loadingFactorsAsRows[i];
        double[] communalityWeights = new double[this.nItems];
        for (int i = 0; i < this.nItems; i++) communalityWeights[i] = 1.0;
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    private void rawVarimaxRotationInHouse(double[][] loadingFactorMatrix) {
        double[] communalityWeights = new double[this.nItems];
        for (int i = 0; i < this.nItems; i++) communalityWeights[i] = 1.0;
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    public void normalVarimaxRotation(int nFactors) {
        if (!this.pcaDone) this.pca();
        double[][] loadingFactorMatrix = new double[nFactors][this.nItems];
        for (int i = 0; i < nFactors; i++) loadingFactorMatrix[i] = this.loadingFactorsAsRows[i];
        double[] communalityWeights = new double[this.nItems];
        for (int i = 0; i < nItems; i++) {
            communalityWeights[i] = 0.0;
            for (int j = 0; j < nFactors; j++) communalityWeights[i] += loadingFactorMatrix[j][i] * loadingFactorMatrix[j][i];
        }
        this.normalVarimaxRotationInHouse(loadingFactorMatrix, communalityWeights);
    }

    private void normalVarimaxRotationInHouse(double[][] loadingFactorMatrix, double[] communalityWeights) {
        if (!this.pcaDone) this.pca();
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        this.usRotatedLoadingFactorsAsRows = new double[nRows][nColumns];
        this.rotatedLoadingFactorsAsRows = new double[nRows][nColumns];
        this.usRotatedEigenValues = new double[nRows];
        this.rotatedEigenValues = new double[nRows];
        this.rotatedProportionPercentage = new double[nRows];
        this.rotatedCumulativePercentage = new double[nRows];
        for (int j = 0; j < nColumns; j++) communalityWeights[j] = Math.sqrt(communalityWeights[j]);
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                loadingFactorMatrix[i][j] /= communalityWeights[j];
                this.usRotatedLoadingFactorsAsRows[i][j] = loadingFactorMatrix[i][j];
            }
        }
        double va = PCA.varimaxCriterion(this.usRotatedLoadingFactorsAsRows);
        double vaLast = 0;
        double angle = 0;
        boolean test = true;
        this.nVarimax = 0;
        while (test) {
            for (int i = 0; i < nRows - 1; i++) {
                for (int j = i + 1; j < nRows; j++) {
                    angle = PCA.varimaxAngle(this.usRotatedLoadingFactorsAsRows, i, j);
                    this.usRotatedLoadingFactorsAsRows = PCA.singleRotation(this.usRotatedLoadingFactorsAsRows, i, j, angle);
                    va = PCA.varimaxCriterion(this.usRotatedLoadingFactorsAsRows);
                }
            }
            if (Math.abs(va - vaLast) < this.varimaxTolerance) {
                test = false;
            } else {
                vaLast = va;
                this.nVarimax++;
                if (this.nVarimax > nVarimaxMax) {
                    test = false;
                    System.out.println("Method varimaxRotation: maximum iterations " + nVarimaxMax + "exceeded");
                    System.out.println("Current values returned");
                }
            }
        }
        this.usRotatedLoadingFactorsAsColumns = new double[nColumns][nRows];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                this.usRotatedLoadingFactorsAsRows[i][j] *= communalityWeights[j];
                this.usRotatedLoadingFactorsAsColumns[j][i] = this.usRotatedLoadingFactorsAsRows[i][j];
                loadingFactorMatrix[i][j] *= communalityWeights[j];
            }
        }
        double usRotatedEigenValueTotal = 0.0;
        double unRotatedEigenValueTotal = 0.0;
        for (int i = 0; i < nRows; i++) {
            this.usRotatedEigenValues[i] = 0.0;
            for (int j = 0; j < nColumns; j++) {
                this.usRotatedEigenValues[i] += this.usRotatedLoadingFactorsAsRows[i][j] * this.usRotatedLoadingFactorsAsRows[i][j];
            }
            usRotatedEigenValueTotal += this.usRotatedEigenValues[i];
            unRotatedEigenValueTotal += this.orderedEigenValues[i];
        }
        double scale0 = Math.abs(unRotatedEigenValueTotal / usRotatedEigenValueTotal);
        double scale1 = Math.sqrt(scale0);
        for (int i = 0; i < nRows; i++) {
            this.rotatedEigenValues[i] = scale0 * this.usRotatedEigenValues[i];
            this.rotatedProportionPercentage[i] = this.rotatedEigenValues[i] * 100.0 / this.eigenValueTotal;
            for (int j = 0; j < nColumns; j++) {
                this.rotatedLoadingFactorsAsRows[i][j] = scale1 * this.usRotatedLoadingFactorsAsRows[i][j];
            }
        }
        this.rotatedCumulativePercentage[0] = this.rotatedProportionPercentage[0];
        for (int i = 1; i < nRows; i++) this.rotatedCumulativePercentage[i] = this.rotatedCumulativePercentage[i - 1] + this.rotatedProportionPercentage[i];
        this.rotationDone = true;
    }

    public static double[][] rawVarimaxRotation(double[][] loadingFactorMatrix) {
        double tolerance = 0.0001;
        int nIterMax = 1000;
        return PCA.rawVarimaxRotation(loadingFactorMatrix, tolerance, nIterMax);
    }

    public static double[][] rawVarimaxRotation(double[][] loadingFactorMatrix, double tolerance, int nIterMax) {
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double[] communalityWeights = new double[nColumns];
        for (int i = 0; i < nColumns; i++) {
            communalityWeights[i] = 0.0;
            for (int j = 0; j < nRows; j++) communalityWeights[i] += loadingFactorMatrix[j][i] * loadingFactorMatrix[j][i];
        }
        return PCA.normalVarimaxRotation(loadingFactorMatrix, communalityWeights, tolerance, nIterMax);
    }

    public static double[][] normalVarimaxRotation(double[][] loadingFactorMatrix, double[] communalityWeights) {
        double tolerance = 0.0001;
        int nIterMax = 1000;
        return normalVarimaxRotation(loadingFactorMatrix, communalityWeights, tolerance, nIterMax);
    }

    public static double[][] normalVarimaxRotation(double[][] loadingFactorMatrix, double[] communalityWeights, double tolerance, int nIterMax) {
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        for (int i = 1; i < nRows; i++) if (loadingFactorMatrix[i].length != nColumns) throw new IllegalArgumentException("All rows must be the same length");
        double[][] rotatedLoadingFactorsAsRows = new double[nRows][nColumns];
        for (int j = 0; j < nColumns; j++) communalityWeights[j] = Math.sqrt(communalityWeights[j]);
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                loadingFactorMatrix[i][j] /= communalityWeights[j];
                rotatedLoadingFactorsAsRows[i][j] = loadingFactorMatrix[i][j];
            }
        }
        double va = PCA.varimaxCriterion(rotatedLoadingFactorsAsRows);
        double vaLast = 0;
        double angle = 0;
        boolean test = true;
        int nIter = 0;
        while (test) {
            for (int i = 0; i < nRows - 1; i++) {
                for (int j = i + 1; j < nRows; j++) {
                    angle = PCA.varimaxAngle(rotatedLoadingFactorsAsRows, i, j);
                    rotatedLoadingFactorsAsRows = PCA.singleRotation(rotatedLoadingFactorsAsRows, i, j, angle);
                    va = PCA.varimaxCriterion(rotatedLoadingFactorsAsRows);
                }
            }
            if (Math.abs(va - vaLast) < tolerance) {
                test = false;
            } else {
                vaLast = va;
                nIter++;
                if (nIter > nIterMax) {
                    test = false;
                    System.out.println("Method varimaxRotation: maximum iterations " + nIterMax + "exceeded");
                    System.out.println("Current values returned");
                }
            }
        }
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                rotatedLoadingFactorsAsRows[i][j] *= communalityWeights[j];
                loadingFactorMatrix[i][j] *= communalityWeights[j];
            }
        }
        return rotatedLoadingFactorsAsRows;
    }

    public static double[][] transposeMatrix(double[][] matrix) {
        int nRows = matrix.length;
        int nColumns = matrix[0].length;
        for (int i = 1; i < nRows; i++) if (matrix[i].length != nColumns) throw new IllegalArgumentException("All rows must be the same length");
        double[][] transpose = new double[nColumns][nRows];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                transpose[j][i] = matrix[i][j];
            }
        }
        return transpose;
    }

    public static double varimaxCriterion(double[][] loadingFactorMatrix) {
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double va1 = 0.0;
        double va2 = 0.0;
        double va3 = 0.0;
        for (int j = 0; j < nRows; j++) {
            double sum1 = 0.0;
            for (int k = 0; k < nColumns; k++) sum1 += Math.pow(loadingFactorMatrix[j][k], 4);
            va1 += sum1;
        }
        va1 *= nColumns;
        for (int j = 0; j < nRows; j++) {
            double sum2 = 0.0;
            for (int k = 0; k < nColumns; k++) sum2 += Math.pow(loadingFactorMatrix[j][k], 2);
            va2 += sum2 * sum2;
        }
        va3 = va1 - va2;
        return va3;
    }

    public static double varimaxAngle(double[][] loadingFactorMatrix, int k, int l) {
        int nColumns = loadingFactorMatrix[0].length;
        double uTerm = 0.0;
        double vTerm = 0.0;
        double bigA = 0.0;
        double bigB = 0.0;
        double bigC = 0.0;
        double bigD = 0.0;
        for (int j = 0; j < nColumns; j++) {
            double lmjk = loadingFactorMatrix[k][j];
            double lmjl = loadingFactorMatrix[l][j];
            uTerm = lmjk * lmjk - lmjl * lmjl;
            vTerm = 2.0 * lmjk * lmjl;
            bigA += uTerm;
            bigB += vTerm;
            bigC += uTerm * uTerm - vTerm * vTerm;
            bigD += 2.0 * uTerm * vTerm;
        }
        double bigE = bigD - 2.0 * bigA * bigB / nColumns;
        double bigF = bigC - (bigA * bigA - bigB * bigB) / nColumns;
        double angle = 0.25 * Math.atan2(bigE, bigF);
        return angle;
    }

    public static double[][] singleRotation(double[][] loadingFactorMatrix, int k, int l, double angle) {
        int nRows = loadingFactorMatrix.length;
        int nColumns = loadingFactorMatrix[0].length;
        double[][] rotatedMatrix = new double[nRows][nColumns];
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nColumns; j++) {
                rotatedMatrix[i][j] = loadingFactorMatrix[i][j];
            }
        }
        double sinphi = Math.sin(angle);
        double cosphi = Math.cos(angle);
        for (int j = 0; j < nColumns; j++) {
            rotatedMatrix[k][j] = loadingFactorMatrix[k][j] * cosphi + loadingFactorMatrix[l][j] * sinphi;
            rotatedMatrix[l][j] = -loadingFactorMatrix[k][j] * sinphi + loadingFactorMatrix[l][j] * cosphi;
        }
        return rotatedMatrix;
    }

    public double[] eigenValues() {
        if (!this.pcaDone) this.pca();
        return this.eigenValues;
    }

    public double[] orderedEigenValues() {
        if (!this.pcaDone) this.pca();
        return this.orderedEigenValues;
    }

    public int[] eigenValueIndices() {
        if (!this.pcaDone) this.pca();
        return this.eigenValueIndices;
    }

    public double eigenValueTotal() {
        if (!this.pcaDone) this.pca();
        return this.eigenValueTotal;
    }

    public double[] proportionPercentage() {
        if (!this.pcaDone) this.pca();
        return this.proportionPercentage;
    }

    public double[] cumulativePercentage() {
        if (!this.pcaDone) this.pca();
        return this.cumulativePercentage;
    }

    public double[] rotatedEigenValues() {
        if (!this.rotationDone) throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedEigenValues;
    }

    public double[] rotatedProportionPercentage() {
        if (!this.rotationDone) throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedProportionPercentage;
    }

    public double[] rotatedCumulativePercentage() {
        if (!this.rotationDone) throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedCumulativePercentage;
    }

    public double[][] eigenVectors() {
        if (!this.pcaDone) this.pca();
        return this.eigenVectorsAsColumns;
    }

    public double[][] eigenVectorsAsRows() {
        if (!this.pcaDone) this.pca();
        return this.eigenVectorsAsRows;
    }

    public double[][] orderedEigenVectorsAsColumns() {
        if (!this.pcaDone) this.pca();
        return this.orderedEigenVectorsAsColumns;
    }

    public double[][] orderedEigenVectors() {
        if (!this.pcaDone) this.pca();
        return this.orderedEigenVectorsAsColumns;
    }

    public double[][] orderedEigenVectorsAsRows() {
        if (!this.pcaDone) this.pca();
        return this.orderedEigenVectorsAsRows;
    }

    public double[][] loadingFactorsAsColumns() {
        if (!this.pcaDone) this.pca();
        return this.loadingFactorsAsColumns;
    }

    public double[][] loadingFactorsAsRows() {
        if (!this.pcaDone) this.pca();
        return this.loadingFactorsAsRows;
    }

    public double[][] rotatedLoadingFactorsAsColumns() {
        if (!this.rotationDone) throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedLoadingFactorsAsColumns;
    }

    public double[][] rotatedLoadingFactorsAsRows() {
        if (!this.rotationDone) throw new IllegalArgumentException("No rotation has been performed");
        return this.rotatedLoadingFactorsAsRows;
    }

    public double[] communalities() {
        if (!this.pcaDone) this.pca();
        return this.communalities;
    }

    public Matrix covarianceMatrix() {
        if (!this.pcaDone) this.pca();
        return this.covarianceMatrix;
    }

    public Matrix correlationMatrix() {
        if (!this.pcaDone) this.pca();
        return this.correlationMatrix;
    }

    public double[] monteCarloMeans() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.randomEigenValuesMeans;
    }

    public double[] monteCarloStandardDeviations() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.randomEigenValuesSDs;
    }

    public double[] monteCarloPercentiles() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.randomEigenValuesPercentiles;
    }

    public double[][] monteCarloEigenValues() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.randomEigenValues;
    }

    public Matrix originalData() {
        if (!this.pcaDone) this.pca();
        return this.data;
    }

    public Matrix xMatrix() {
        if (!this.pcaDone) this.pca();
        double denom = this.nItems;
        if (!super.nFactorOption) denom -= 1.0;
        Matrix mat = dataMinusMeans.times(1.0 / Math.sqrt(denom));
        return mat;
    }

    public Matrix xMatrixTranspose() {
        if (!this.pcaDone) this.pca();
        double denom = this.nItems;
        if (!super.nFactorOption) denom -= 1.0;
        Matrix mat = dataMinusMeansTranspose.times(1.0 / Math.sqrt(denom));
        return mat;
    }

    public int nEigenOneOrGreater() {
        if (!this.pcaDone) this.pca();
        return this.greaterThanOneLimit;
    }

    public int nMeanCrossover() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.meanCrossover;
    }

    public int nPercentileCrossover() {
        if (!this.monteCarloDone) this.monteCarlo();
        return this.percentileCrossover;
    }

    public void analysis() {
        this.outputFilename = "PCAOutput";
        if (this.fileOption == 1) {
            this.outputFilename += ".txt";
        } else {
            this.outputFilename += ".xls";
        }
        String message1 = "Output file name for the analysis details:";
        String message2 = "\nEnter the required name (as a single word) and click OK ";
        String message3 = "\nor simply click OK for default value";
        String message = message1 + message2 + message3;
        String defaultName = this.outputFilename;
        this.outputFilename = Db.readLine(message, defaultName);
        this.analysis(this.outputFilename);
    }

    public void analysis(String filename) {
        this.screePlot();
        this.outputFilename = filename;
        String outputFilenameWithoutExtension = null;
        String extension = null;
        int pos = filename.indexOf('.');
        if (pos == -1) {
            outputFilenameWithoutExtension = filename;
            if (this.fileOption == 1) {
                this.outputFilename += ".txt";
            } else {
                this.outputFilename += ".xls";
            }
        } else {
            extension = (filename.substring(pos)).trim();
            outputFilenameWithoutExtension = (filename.substring(0, pos)).trim();
            if (extension.equalsIgnoreCase(".xls")) {
                if (this.fileOption == 1) {
                    if (this.fileOptionSet) {
                        String message1 = "Your entered output file type is .xls";
                        String message2 = "\nbut you have chosen a .txt output";
                        String message = message1 + message2;
                        String headerComment = "Your output file name extension";
                        String[] comments = { message, "replace it with .txt [text file]" };
                        String[] boxTitles = { "Retain", ".txt" };
                        int defaultBox = 1;
                        int opt = Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                        if (opt == 2) this.outputFilename = outputFilenameWithoutExtension + ".txt";
                    } else {
                        this.fileOption = 2;
                    }
                }
            }
            if (extension.equalsIgnoreCase(".txt")) {
                if (this.fileOption == 2) {
                    if (this.fileOptionSet) {
                        String message1 = "Your entered output file type is .txt";
                        String message2 = "\nbut you have chosen a .xls output";
                        String message = message1 + message2;
                        String headerComment = "Your output file name extension";
                        String[] comments = { message, "replace it with .xls [Excel file]" };
                        String[] boxTitles = { "Retain", ".xls" };
                        int defaultBox = 1;
                        int opt = Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                        if (opt == 2) this.outputFilename = outputFilenameWithoutExtension + ".xls";
                    } else {
                        this.fileOption = 1;
                    }
                }
            }
            if (!extension.equalsIgnoreCase(".txt") && !extension.equalsIgnoreCase(".xls")) {
                String message1 = "Your extension is " + extension;
                String message2 = "\n    Do you wish to retain it:";
                String message = message1 + message2;
                String headerComment = "Your output file name extension";
                String[] comments = { message, "replace it with .txt [text file]", "replace it with .xls [MS Excel file]" };
                String[] boxTitles = { "Retain", ".txt", ".xls" };
                int defaultBox = 1;
                int opt = Db.optionBox(headerComment, comments, boxTitles, defaultBox);
                switch(opt) {
                    case 1:
                        this.fileOption = 1;
                        break;
                    case 2:
                        this.outputFilename = outputFilenameWithoutExtension + ".txt";
                        this.fileOption = 1;
                        break;
                    case 3:
                        this.outputFilename = outputFilenameWithoutExtension + ".xls";
                        this.fileOption = 2;
                        break;
                }
            }
        }
        if (this.fileOption == 1) {
            this.analysisText();
        } else {
            this.analysisExcel();
        }
        System.out.println("The analysis has been written to the file " + this.outputFilename);
    }

    private void analysisText() {
        FileOutput fout = null;
        if (this.fileNumberingSet) {
            fout = new FileOutput(this.outputFilename, 'n');
        } else {
            fout = new FileOutput(this.outputFilename);
        }
        if (!pcaDone) this.pca();
        if (!this.monteCarloDone) this.monteCarlo();
        fout.println("PRINCIPAL COMPONENT ANALYSIS");
        fout.println("Program: PCA - Analysis Output");
        for (int i = 0; i < this.titleLines; i++) fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();
        if (this.covRhoOption) {
            fout.println("Covariance matrix used");
        } else {
            fout.println("Correlation matrix used");
        }
        fout.println();
        int field1 = 10;
        int field2 = 12;
        int field3 = 2;
        fout.println("ALL EIGENVALUES");
        fout.print("Component ", field1);
        fout.print("Unordered ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.println("Difference ");
        fout.print(" ", field1);
        fout.print("index", field1);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.println(" ");
        for (int i = 0; i < this.nItems; i++) {
            fout.print(i + 1, field1);
            fout.print((this.eigenValueIndices[i] + 1), field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.proportionPercentage[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.cumulativePercentage[i], this.trunc), field2);
            if (i < this.nItems - 1) {
                fout.print(Fmath.truncate((this.orderedEigenValues[i] - this.orderedEigenValues[i + 1]), this.trunc), field2);
            } else {
                fout.print(" ", field2);
            }
            fout.print(" ", field3);
            fout.println();
        }
        fout.println();
        int nMax = this.greaterThanOneLimit;
        if (nMax < this.meanCrossover) nMax = this.meanCrossover;
        if (nMax < this.percentileCrossover) nMax = this.percentileCrossover;
        fout.println("EXTRACTED EIGENVALUES");
        fout.print(" ", field1);
        fout.print("Greater than unity", 3 * field2 + field3);
        fout.print("Greater than Monte Carlo Mean ", 3 * field2 + field3);
        fout.println("Greater than Monte Carlo Percentile");
        fout.print("Component ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);
        fout.print("Eigenvalue ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.println(" ");
        fout.print(" ", field1);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);
        fout.print(" ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.println(" ");
        int ii = 0;
        while (ii < nMax) {
            fout.print(ii + 1, field1);
            if (ii < this.greaterThanOneLimit) {
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc), (field2 + field3));
            }
            if (ii < this.meanCrossover) {
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc), (field2 + field3));
            }
            if (ii < this.percentileCrossover) {
                fout.print(Fmath.truncate(this.orderedEigenValues[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.proportionPercentage[ii], this.trunc), field2);
                fout.print(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
            }
            fout.println();
            ii++;
        }
        fout.println();
        fout.println("PARALLEL ANALYSIS");
        fout.println("Number of simulations = " + this.nMonteCarlo);
        if (this.gaussianDeviates) {
            fout.println("Gaussian random deviates used");
        } else {
            fout.println("Uniform random deviates used");
        }
        fout.println("Percentile value used = " + this.percentile + " %");
        fout.println();
        fout.print("Component ", field1);
        fout.print("Data ", field2);
        fout.print("Proportion ", field2);
        fout.print("Cumulative ", field2);
        fout.print(" ", field3);
        fout.print("Data ", field2);
        fout.print("Monte Carlo ", field2);
        fout.print("Monte Carlo ", field2);
        fout.println("Monte Carlo ");
        fout.print(" ", field1);
        fout.print("Eigenvalue ", field2);
        fout.print("as % ", field2);
        fout.print("percentage ", field2);
        fout.print(" ", field3);
        fout.print("Eigenvalue ", field2);
        fout.print("Eigenvalue ", field2);
        fout.print("Eigenvalue ", field2);
        fout.println("Eigenvalue ");
        fout.print(" ", field1);
        fout.print(" ", field2);
        fout.print(" ", field2);
        fout.print(" ", field2);
        fout.print(" ", field3);
        fout.print(" ", field2);
        fout.print("Percentile ", field2);
        fout.print("Mean ", field2);
        fout.println("Standard Deviation ");
        for (int i = 0; i < this.nItems; i++) {
            fout.print(i + 1, field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.proportionPercentage[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.cumulativePercentage[i], this.trunc), field2);
            fout.print(" ", field3);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.randomEigenValuesPercentiles[i], this.trunc), field2);
            fout.print(Fmath.truncate(this.randomEigenValuesMeans[i], this.trunc), field2);
            fout.println(Fmath.truncate(this.randomEigenValuesSDs[i], this.trunc));
        }
        fout.println();
        fout.println("CORRELATION MATRIX");
        fout.println("Original component indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for (int i = 0; i < this.nItems; i++) fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", field2);
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", 2 * field1);
            for (int j = 0; j < this.nItems; j++) fout.print(Fmath.truncate(this.correlationMatrix.getElement(j, i), this.trunc), field2);
            fout.println();
        }
        fout.println();
        fout.println("COVARIANCE MATRIX");
        fout.println("Original component indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for (int i = 0; i < this.nItems; i++) fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", field2);
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", 2 * field1);
            for (int j = 0; j < this.nItems; j++) fout.print(Fmath.truncate(this.covarianceMatrix.getElement(j, i), this.trunc), field2);
            fout.println();
        }
        fout.println();
        fout.println("EIGENVECTORS");
        fout.println("Original component indices in parenthesis");
        fout.println("Vector corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for (int i = 0; i < this.nItems; i++) fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", field2);
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.print((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")", 2 * field1);
            for (int j = 0; j < this.nItems; j++) fout.print(Fmath.truncate(this.orderedEigenVectorsAsRows[i][j], this.trunc), field2);
            fout.println();
        }
        fout.println();
        fout.println("LOADING FACTORS");
        fout.println("Original  indices in parenthesis");
        fout.println("Loading factors corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for (int i = 0; i < this.nItems; i++) fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", field2);
        fout.print(" ", field1);
        fout.print("Eigenvalue", field2);
        fout.print("Proportion", field2);
        fout.println("Cumulative %");
        fout.println("factor");
        for (int i = 0; i < this.nItems; i++) {
            fout.print((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")", 2 * field1);
            for (int j = 0; j < this.nItems; j++) fout.print(Fmath.truncate(this.loadingFactorsAsRows[i][j], this.trunc), field2);
            fout.print(" ", field1);
            fout.print(Fmath.truncate(this.orderedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(proportionPercentage[i], this.trunc), field2);
            fout.println(Fmath.truncate(cumulativePercentage[i], this.trunc));
        }
        fout.println();
        fout.println("ROTATED LOADING FACTORS");
        if (this.varimaxOption) {
            fout.println("NORMAL VARIMAX");
        } else {
            fout.println("RAW VARIMAX");
        }
        String message = "The ordered eigenvalues with Monte Carlo means and percentiles in parenthesis";
        message += "\n (Total number of eigenvalues = " + this.nItems + ")";
        int nDisplay = this.nItems;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int nDisplayLimit = 20 * screenHeight / 800;
        if (nDisplay > nDisplay) nDisplay = nDisplayLimit;
        for (int i = 0; i < nDisplay; i++) {
            message += "\n " + Fmath.truncate(this.orderedEigenValues[i], 4) + " (" + Fmath.truncate(this.randomEigenValuesMeans[i], 4) + "  " + Fmath.truncate(this.randomEigenValuesPercentiles[i], 4) + ")";
        }
        if (nDisplay < this.nItems) message += "\n . . . ";
        message += "\nEnter number of eigenvalues to be extracted";
        int nExtracted = this.greaterThanOneLimit;
        nExtracted = Db.readInt(message, nExtracted);
        this.varimaxRotation(nExtracted);
        fout.println("Varimax rotation for " + nExtracted + " extracted factors");
        fout.println("Rotated loading factors and eigenvalues scaled to ensure total 'rotated variance' matches unrotated variance for the extracted factors");
        fout.println("Original  indices in parenthesis");
        fout.println();
        fout.print(" ", field1);
        fout.print("component", field1);
        for (int i = 0; i < this.nItems; i++) fout.print((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")", field2);
        fout.print(" ", field1);
        fout.print("Eigenvalue", field2);
        fout.print("Proportion", field2);
        fout.println("Cumulative %");
        fout.println("factor");
        for (int i = 0; i < nExtracted; i++) {
            fout.print((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")", 2 * field1);
            for (int j = 0; j < this.nItems; j++) fout.print(Fmath.truncate(this.rotatedLoadingFactorsAsRows[i][j], this.trunc), field2);
            fout.print(" ", field1);
            fout.print(Fmath.truncate(rotatedEigenValues[i], this.trunc), field2);
            fout.print(Fmath.truncate(rotatedProportionPercentage[i], this.trunc), field2);
            fout.println(Fmath.truncate(rotatedCumulativePercentage[i], this.trunc));
        }
        fout.println();
        fout.close();
    }

    private void analysisExcel() {
        FileOutput fout = null;
        if (this.fileNumberingSet) {
            fout = new FileOutput(this.outputFilename, 'n');
        } else {
            fout = new FileOutput(this.outputFilename);
        }
        if (!pcaDone) this.pca();
        if (!this.monteCarloDone) this.monteCarlo();
        fout.println("PRINCIPAL COMPONENT ANALYSIS");
        fout.println("Program: PCA - Analysis Output");
        for (int i = 0; i < this.titleLines; i++) fout.println(title[i]);
        Date d = new Date();
        String day = DateFormat.getDateInstance().format(d);
        String tim = DateFormat.getTimeInstance().format(d);
        fout.println("Program executed at " + tim + " on " + day);
        fout.println();
        if (this.covRhoOption) {
            fout.println("Covariance matrix used");
        } else {
            fout.println("Correlation matrix used");
        }
        fout.println();
        fout.println("ALL EIGENVALUES");
        fout.printtab("Component ");
        fout.printtab("Unordered ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.println("Difference ");
        fout.printtab(" ");
        fout.printtab("index");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.println(" ");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab(i + 1);
            fout.printtab((this.eigenValueIndices[i] + 1));
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.proportionPercentage[i], this.trunc));
            fout.printtab(Fmath.truncate(this.cumulativePercentage[i], this.trunc));
            if (i < this.nItems - 1) {
                fout.printtab(Fmath.truncate((this.orderedEigenValues[i] - this.orderedEigenValues[i + 1]), this.trunc));
            } else {
                fout.printtab(" ");
            }
            fout.printtab(" ");
            fout.println();
        }
        fout.println();
        int nMax = this.greaterThanOneLimit;
        if (nMax < this.meanCrossover) nMax = this.meanCrossover;
        if (nMax < this.percentileCrossover) nMax = this.percentileCrossover;
        fout.println("EXTRACTED EIGENVALUES");
        fout.printtab(" ");
        fout.printtab("Greater than unity");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("Greater than Monte Carlo Mean ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.println("Greater than Monte Carlo Percentile");
        fout.printtab("Component ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.println(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.println(" ");
        int ii = 0;
        while (ii < nMax) {
            fout.printtab(ii + 1);
            if (ii < this.greaterThanOneLimit) {
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
                fout.printtab(" ");
            }
            if (ii < this.meanCrossover) {
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
                fout.printtab(" ");
            }
            if (ii < this.percentileCrossover) {
                fout.printtab(Fmath.truncate(this.orderedEigenValues[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.proportionPercentage[ii], this.trunc));
                fout.printtab(Fmath.truncate(this.cumulativePercentage[ii], this.trunc));
            }
            fout.println();
            ii++;
        }
        fout.println();
        fout.println("PARALLEL ANALYSIS");
        fout.println("Number of simulations = " + this.nMonteCarlo);
        if (this.gaussianDeviates) {
            fout.println("Gaussian random deviates used");
        } else {
            fout.println("Uniform random deviates used");
        }
        fout.println("Percentile value used = " + this.percentile + " %");
        fout.println();
        fout.printtab("Component ");
        fout.printtab("Data ");
        fout.printtab("Proportion ");
        fout.printtab("Cumulative ");
        fout.printtab(" ");
        fout.printtab("Data ");
        fout.printtab("Monte Carlo ");
        fout.printtab("Monte Carlo ");
        fout.println("Monte Carlo ");
        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("as % ");
        fout.printtab("percentage ");
        fout.printtab(" ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Eigenvalue ");
        fout.printtab("Eigenvalue ");
        fout.println("Eigenvalue ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab(" ");
        fout.printtab("Percentile ");
        fout.printtab("Mean ");
        fout.println("Standard Deviation ");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab(i + 1);
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.proportionPercentage[i], this.trunc));
            fout.printtab(Fmath.truncate(this.cumulativePercentage[i], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(this.randomEigenValuesPercentiles[i], this.trunc));
            fout.printtab(Fmath.truncate(this.randomEigenValuesMeans[i], this.trunc));
            fout.println(Fmath.truncate(this.randomEigenValuesSDs[i], this.trunc));
        }
        fout.println();
        fout.println("CORRELATION MATRIX");
        fout.println("Original component indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for (int i = 0; i < this.nItems; i++) fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
            fout.printtab(" ");
            for (int j = 0; j < this.nItems; j++) fout.printtab(Fmath.truncate(this.correlationMatrix.getElement(j, i), this.trunc));
            fout.println();
        }
        fout.println();
        fout.println("COVARIANCE MATRIX");
        fout.println("Original component indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for (int i = 0; i < this.nItems; i++) fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
            fout.printtab(" ");
            for (int j = 0; j < this.nItems; j++) fout.printtab(Fmath.truncate(this.covarianceMatrix.getElement(j, i), this.trunc));
            fout.println();
        }
        fout.println();
        fout.println("EIGENVECTORS");
        fout.println("Original component indices in parenthesis");
        fout.println("Vector corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for (int i = 0; i < this.nItems; i++) fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
        fout.println();
        fout.println("component");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")");
            fout.printtab(" ");
            for (int j = 0; j < this.nItems; j++) fout.printtab(Fmath.truncate(this.orderedEigenVectorsAsRows[i][j], this.trunc));
            fout.println();
        }
        fout.println();
        fout.println("LOADING FACTORS");
        fout.println("Original  indices in parenthesis");
        fout.println("Loading factors corresponding to an ordered eigenvalues in each row");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for (int i = 0; i < this.nItems; i++) fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
        fout.printtab(" ");
        fout.printtab("Eigenvalue");
        fout.printtab("% Proportion");
        fout.println("Cumulative %");
        fout.println("factor");
        for (int i = 0; i < this.nItems; i++) {
            fout.printtab((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")");
            fout.printtab(" ");
            for (int j = 0; j < this.nItems; j++) fout.printtab(Fmath.truncate(this.loadingFactorsAsRows[i][j], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(this.orderedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(proportionPercentage[i], this.trunc));
            fout.println(Fmath.truncate(cumulativePercentage[i], this.trunc));
        }
        fout.println();
        fout.println("ROTATED LOADING FACTORS");
        if (this.varimaxOption) {
            fout.println("NORMAL VARIMAX");
        } else {
            fout.println("RAW VARIMAX");
        }
        String message = "The ordered eigenvalues with Monte Carlo means and percentiles in parenthesis";
        message += "\n (Total number of eigenvalues = " + this.nItems + ")";
        int nDisplay = this.nItems;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int nDisplayLimit = 20 * screenHeight / 800;
        if (nDisplay > nDisplay) nDisplay = nDisplayLimit;
        for (int i = 0; i < nDisplay; i++) {
            message += "\n " + Fmath.truncate(this.orderedEigenValues[i], 4) + " (" + Fmath.truncate(this.randomEigenValuesMeans[i], 4) + "  " + Fmath.truncate(this.randomEigenValuesPercentiles[i], 4) + ")";
        }
        if (nDisplay < this.nItems) message += "\n . . . ";
        message += "\nEnter number of eigenvalues to be extracted";
        int nExtracted = this.greaterThanOneLimit;
        nExtracted = Db.readInt(message, nExtracted);
        this.varimaxRotation(nExtracted);
        fout.println("Varimax rotation for " + nExtracted + " extracted factors");
        fout.println("Rotated loading factors and eigenvalues scaled to ensure total 'rotated variance' matches unrotated variance for the extracted factors");
        fout.println("Original  indices in parenthesis");
        fout.println();
        fout.printtab(" ");
        fout.printtab("component");
        for (int i = 0; i < this.nItems; i++) fout.printtab((this.eigenValueIndices[i] + 1) + " (" + (i + 1) + ")");
        fout.printtab(" ");
        fout.printtab("Eigenvalue");
        fout.printtab("% Proportion");
        fout.println("Cumulative %");
        fout.println("factor");
        for (int i = 0; i < nExtracted; i++) {
            fout.printtab((i + 1) + " (" + (this.eigenValueIndices[i] + 1) + ")");
            fout.printtab(" ");
            for (int j = 0; j < this.nItems; j++) fout.printtab(Fmath.truncate(this.rotatedLoadingFactorsAsRows[i][j], this.trunc));
            fout.printtab(" ");
            fout.printtab(Fmath.truncate(rotatedEigenValues[i], this.trunc));
            fout.printtab(Fmath.truncate(rotatedProportionPercentage[i], this.trunc));
            fout.println(Fmath.truncate(rotatedCumulativePercentage[i], this.trunc));
        }
        fout.println();
        fout.close();
    }
}
