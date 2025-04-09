package net.maizegenetics.analysis;

import pal.alignment.*;
import pal.distance.*;
import pal.misc.TableReport;
import net.maizegenetics.stats.Numeric;

/**
 * Title:        TASSEL
 * Description:  A java program to deal with diversity
 * Copyright:    Copyright (c) 2000
 * Company:      USDA-ARS/NCSU
 *
 * @author Ed Buckler
 * @version 1.0
 */
public class GeneralizedAlignmentDistanceMatrix extends DistanceMatrix implements TableReport {

    private int numSeqs;

    private IgnoreMissingPairwiseDistance pwd;

    private SitePattern sitePattern;

    private double kMin = 99999;

    private double kMax = -99999;

    private double kAvg = 0;

    private double kSD = 0;

    private double cutOff = 2;

    /**
     * PairwiseDistance mode:  missing is counted and goes in total (pal DEFAULT)
     */
    public static final int INCLUDE_MISSING = 1;

    /**
     * PairwiseDistance mode:  missing is ignored
     */
    public static final int IGNORE_MISSING = 2;

    /**
     * PairwiseDistance mode:  synonmyous site of codon alignment are calculated, missing is ignored
     */
    public static final int SYNONYMOUS_IGNORE_MISSING = 3;

    /**
     * PairwiseDistance mode:  nonsynonmyous site of codon alignment are calculated, missing is ignored
     */
    public static final int NONSYNONYMOUS_IGNORE_MISSING = 4;

    int pairwiseDistMode;

    /**
     * Holds the average numbers of sites in the comparisons
     */
    double avgTotalSites;

    /**
     * compute observed distances
     *
     * @param sp               site pattern
     * @param pairwiseDistMode PairwiseDistance calculation mode (for example IGNORE_MISSING);
     */
    public GeneralizedAlignmentDistanceMatrix(SitePattern sp, int pairwiseDistMode) {
        super();
        numSeqs = sp.getSequenceCount();
        sitePattern = sp;
        setIdGroup(sp);
        this.pairwiseDistMode = pairwiseDistMode;
        switch(pairwiseDistMode) {
            case INCLUDE_MISSING:
                {
                    pwd = new IncludeMissingPairwiseDistance(sp);
                    break;
                }
            case IGNORE_MISSING:
                {
                    pwd = new IgnoreMissingPairwiseDistance(sp);
                    break;
                }
            case SYNONYMOUS_IGNORE_MISSING:
                {
                    pwd = new SynonymousPairwiseDistance(sp);
                    break;
                }
            case NONSYNONYMOUS_IGNORE_MISSING:
                {
                    pwd = new NonSynonymousPairwiseDistance(sp);
                    break;
                }
        }
        computeDistances();
    }

    /**
     * recompute maximum-likelihood distances under new site pattern
     *
     * @param sp site pattern
     */
    public void recompute(SitePattern sp) {
        pwd.updateSitePattern(sp);
        computeDistances();
    }

    public void toSimilarity() {
        double[][] sim = new double[numSeqs][numSeqs];
        for (int i = 0; i < numSeqs; i++) {
            for (int j = i; j < numSeqs; j++) {
                sim[i][j] = 1 - this.getDistance(i, j);
                sim[j][i] = sim[i][j];
            }
        }
        setDistances(sim);
    }

    public void getKStatistics() {
        double total = 0;
        double totalsq = 0;
        double nk = numSeqs * (numSeqs - 1) / 2;
        for (int i = 0; i < numSeqs - 1; i++) {
            for (int j = i + 1; j < numSeqs; j++) {
                total += this.getDistance(i, j);
                totalsq += (this.getDistance(i, j) * this.getDistance(i, j));
                if (this.getDistance(i, j) < kMin) {
                    kMin = this.getDistance(i, j);
                }
                if (this.getDistance(i, j) > kMax) {
                    kMax = this.getDistance(i, j);
                }
            }
        }
        kAvg = total / nk;
        kSD = Math.sqrt((totalsq - nk * kAvg * kAvg) / (nk - 1));
        System.out.println(kAvg);
    }

    public void pullBackExtrem() {
        for (int i = 0; i < numSeqs - 1; i++) {
            for (int j = i + 1; j < numSeqs; j++) {
                if (this.getDistance(i, j) < kAvg - cutOff * kSD) {
                    this.setDistance(i, j, kAvg - cutOff * kSD);
                    kMin = this.getDistance(i, j);
                }
            }
        }
        System.out.println("values beyond 3 sd from mean were pulled back");
    }

    public void cutOff() {
        double[][] sim = new double[numSeqs][numSeqs];
        for (int i = 0; i < numSeqs; i++) {
            for (int j = i + 0; j < numSeqs; j++) {
                if (this.getDistance(i, j) > kAvg) {
                    sim[i][j] = this.getDistance(i, j);
                } else {
                    sim[i][j] = kAvg;
                }
                sim[j][i] = sim[i][j];
            }
        }
        kMin = kAvg;
        setDistances(sim);
    }

    public void rescale() {
        double[][] sim = new double[numSeqs][numSeqs];
        for (int i = 0; i < numSeqs; i++) {
            for (int j = i; j < numSeqs; j++) {
                if (this.getDistance(i, j) > 0) {
                    sim[i][j] = (this.getDistance(i, j) - kMin) * 2 / (kMax - kMin);
                }
                sim[j][i] = sim[i][j];
            }
        }
        setDistances(sim);
        System.out.println("K rescaled");
    }

    private void computeDistances() {
        avgTotalSites = 0;
        int count = 0;
        double[] params;
        double[][] distance = new double[numSeqs][numSeqs];
        for (int i = 0; i < numSeqs; i++) {
            distance[i][i] = 0;
            for (int j = i + 1; j < numSeqs; j++) {
                params = pwd.getDistance(i, j);
                distance[i][j] = params[0];
                distance[j][i] = params[0];
                avgTotalSites += params[1];
                count++;
            }
        }
        setDistances(distance);
        avgTotalSites /= (double) count;
    }

    public double[][] getDistance() {
        return super.getDistances();
    }

    public Object[] getTableColumnNames() {
        double[][] distance = this.getDistance();
        if (distance == null) {
            return null;
        }
        Object[] colNames = new Object[distance[0].length + 1];
        colNames[0] = "Taxa";
        for (int i = 1; i <= distance[0].length; i++) {
            colNames[i] = sitePattern.getIdentifier(i - 1);
        }
        return colNames;
    }

    public Object[][] getTableData() {
        double[][] distance = this.getDistance();
        Object[][] distObj = new Object[distance.length][];
        for (int i = 0; i < distance.length; i++) {
            Object[] tempRowObj = new Object[distance[i].length + 1];
            tempRowObj[0] = sitePattern.getIdentifier(i);
            for (int j = 1; j <= distance[i].length; j++) {
                tempRowObj[j] = "" + distance[i][j - 1];
            }
            distObj[i] = tempRowObj;
        }
        return distObj;
    }

    public String getTableTitle() {
        return "Alignment Distance Matrix";
    }

    public String toString(int d) {
        double[][] distance = this.getDistance();
        String newln = System.getProperty("line.separator");
        String outPut = new String();
        String num = new String();
        int i, j;
        for (i = 0; i < distance.length; i++) {
            for (j = 0; j < distance[i].length; j++) {
                Numeric x = new Numeric(distance[i][j]);
                num = x.toString(d);
                outPut = outPut + num + (char) 9;
            }
            outPut = outPut + newln;
        }
        return outPut;
    }

    public String toString() {
        return this.toString(6);
    }
}
