package dr.inference.trace;

import dr.inferencexml.trace.MarginalLikelihoodAnalysisParser;
import dr.math.EmpiricalBayesPoissonSmoother;
import dr.stats.DiscreteStatistics;
import dr.util.Attribute;
import dr.util.FileHelpers;
import dr.util.HeapSort;
import dr.xml.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author Philippe Lemey
 * @author Marc A. Suchard
 */
public class OldDnDsPerSiteAnalysis {

    public static final String DNDS_PERSITE_ANALYSIS = "olddNdSPerSiteAnalysis";

    public static final String COND_SPERSITE_COLUMNS = "conditionalSperSite";

    public static final String UNCOND_SPERSITE_COLUMNS = "unconditionalSperSite";

    public static final String COND_NPERSITE_COLUMNS = "conditionalNperSite";

    public static final String UNCOND_NPERSITE_COLUMNS = "unconditionalNperSite";

    OldDnDsPerSiteAnalysis(double[][] sampleSperSite, double[][] unconditionalS, double[][] sampleNperSite, double[][] unconditionalN) {
        numSites = sampleNperSite.length;
        numSamples = sampleNperSite[0].length;
        allSamples = new double[NUM_VARIABLES][numSites][numSamples];
        allSamples[COND_S] = sampleSperSite;
        allSamples[UNCOND_S] = unconditionalS;
        allSamples[COND_N] = sampleNperSite;
        allSamples[UNCOND_N] = unconditionalN;
        if (DEBUG) {
            System.err.println("sumSites = " + numSites);
            System.err.println("numSamples = " + numSamples);
        }
        smoothSamples = performSmoothing(allSamples);
        smoothDnDsSamples = getDnDsSamples(smoothSamples);
        rawMeanStats = computeMeanStats(allSamples);
        smoothMeanStats = computeMeanStats(smoothSamples);
        smoothMeanDnDsStats = computeMeanStats(smoothDnDsSamples);
        smoothHPDDnDsStats = computeHPDStats(smoothDnDsSamples);
    }

    private double[][][] getDnDsSamples(double[][][] smoothedSamples) {
        double[][][] dNdSArray = new double[3][][];
        dNdSArray[0] = get2DArrayRatio(smoothedSamples[0], smoothedSamples[1]);
        dNdSArray[1] = get2DArrayRatio(smoothedSamples[2], smoothedSamples[3]);
        dNdSArray[2] = get2DArrayRatio(dNdSArray[1], dNdSArray[0]);
        return dNdSArray;
    }

    private double[][] get2DArrayRatio(double[][] numerator, double[][] denominator) {
        double[][] returnArray = new double[numerator.length][numerator[0].length];
        for (int site = 0; site < numerator.length; site++) {
            returnArray[site] = get1DArrayRatio(numerator[site], denominator[site]);
        }
        return returnArray;
    }

    private double[] get1DArrayRatio(double[] numerator, double[] denominator) {
        double[] returnArray = new double[numerator.length];
        for (int sample = 0; sample < numerator.length; sample++) {
            returnArray[sample] = numerator[sample] / denominator[sample];
        }
        return returnArray;
    }

    private double[][] transpose(double[][] in) {
        double[][] out = new double[in[0].length][in.length];
        for (int r = 0; r < in.length; r++) {
            for (int c = 0; c < in[0].length; c++) {
                out[c][r] = in[r][c];
            }
        }
        return out;
    }

    private double computeDnDs(double[][][] allSamples, int site, int sample) {
        return (allSamples[COND_N][site][sample] / allSamples[COND_S][site][sample]) / (allSamples[UNCOND_N][site][sample] / allSamples[UNCOND_S][site][sample]);
    }

    private double[][][] performSmoothing(double[][][] allSamples) {
        double[][][] smoothedArray = new double[allSamples.length][][];
        double[][][] transpose = new double[allSamples.length][][];
        for (int i = 0; i < allSamples.length; i++) {
            transpose[i] = transpose(allSamples[i]);
            for (int sample = 0; sample < numSamples; ++sample) {
                transpose[i][sample] = EmpiricalBayesPoissonSmoother.smooth(transpose[i][sample]);
            }
            smoothedArray[i] = transpose(transpose[i]);
        }
        return smoothedArray;
    }

    private double computeRatio(double[][][] allSamples, int site, int sample, int numerator, int denominator) {
        return allSamples[numerator][site][sample] / allSamples[denominator][site][sample];
    }

    private double[][] computeMeanStats(double[][][] allSamples) {
        double[][] statistics = new double[allSamples.length][allSamples[0].length];
        for (int variable = 0; variable < allSamples.length; ++variable) {
            statistics[variable] = mean(allSamples[variable]);
        }
        return statistics;
    }

    private double[][][] computeHPDStats(double[][][] allSamples) {
        double[][][] statistics = new double[allSamples.length][allSamples[0].length][2];
        for (int variable = 0; variable < allSamples.length; variable++) {
            statistics[variable] = getArrayHPDintervals(allSamples[variable]);
        }
        return statistics;
    }

    public String output() {
        StringBuffer sb = new StringBuffer();
        sb.append("site\tcS\tuS\tcN\tuN\tsmooth(cS)\tsmooth(uS)\tsmooth(cN)\tsmooth(uN)" + "\tsmooth(cS/uS)\tsmooth(cN/uN)\tsmooth((cN/uN)/(cS/uS))\t[hpd]\n");
        for (int site = 0; site < numSites; site++) {
            sb.append(site + 1 + "\t" + rawMeanStats[0][site] + "\t" + rawMeanStats[1][site] + "\t" + rawMeanStats[2][site] + "\t" + rawMeanStats[3][site] + "\t" + smoothMeanStats[0][site] + "\t" + smoothMeanStats[1][site] + "\t" + smoothMeanStats[2][site] + "\t" + smoothMeanStats[3][site] + "\t" + smoothMeanDnDsStats[0][site] + "\t" + smoothMeanDnDsStats[1][site] + "\t" + smoothMeanDnDsStats[2][site] + "\t" + "[" + smoothHPDDnDsStats[2][site][0] + "," + smoothHPDDnDsStats[2][site][1] + "]");
            if (smoothHPDDnDsStats[2][site][0] > 1 || smoothHPDDnDsStats[2][site][1] < 1) {
                sb.append("\t*");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static double[][] getArrayHPDintervals(double[][] array) {
        double[][] returnArray = new double[array.length][2];
        for (int row = 0; row < array.length; row++) {
            int counter = 0;
            for (int col = 0; col < array[0].length; col++) {
                if (!(((Double) array[row][col]).isNaN())) {
                    counter += 1;
                }
            }
            if (counter > 0) {
                double[] rowNoNaNArray = new double[counter];
                int index = 0;
                for (int col = 0; col < array[0].length; col++) {
                    if (!(((Double) array[row][col]).isNaN())) {
                        rowNoNaNArray[index] = array[row][col];
                        index += 1;
                    }
                }
                int[] indices = new int[counter];
                HeapSort.sort(rowNoNaNArray, indices);
                double hpdBinInterval[] = getHPDInterval(0.95, rowNoNaNArray, indices);
                returnArray[row][0] = hpdBinInterval[0];
                returnArray[row][1] = hpdBinInterval[1];
            } else {
                returnArray[row][0] = Double.NaN;
                returnArray[row][1] = Double.NaN;
            }
        }
        return returnArray;
    }

    private static double[] getHPDInterval(double proportion, double[] array, int[] indices) {
        double returnArray[] = new double[2];
        double minRange = Double.MAX_VALUE;
        int hpdIndex = 0;
        int diff = (int) Math.round(proportion * (double) array.length);
        for (int i = 0; i <= (array.length - diff); i++) {
            double minValue = array[indices[i]];
            double maxValue = array[indices[i + diff - 1]];
            double range = Math.abs(maxValue - minValue);
            if (range < minRange) {
                minRange = range;
                hpdIndex = i;
            }
        }
        returnArray[0] = array[indices[hpdIndex]];
        returnArray[1] = array[indices[hpdIndex + diff - 1]];
        return returnArray;
    }

    private static double[] mean(double[][] x) {
        double[] returnArray = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            returnArray[i] = DiscreteStatistics.mean(x[i]);
        }
        return returnArray;
    }

    public static XMLObjectParser PARSER = new AbstractXMLObjectParser() {

        public String getParserName() {
            return DNDS_PERSITE_ANALYSIS;
        }

        public Object parseXMLObject(XMLObject xo) throws XMLParseException {
            String fileName = xo.getStringAttribute(FileHelpers.FILE_NAME);
            try {
                File file = new File(fileName);
                String name = file.getName();
                String parent = file.getParent();
                if (!file.isAbsolute()) {
                    parent = System.getProperty("user.dir");
                }
                file = new File(parent, name);
                fileName = file.getAbsolutePath();
                LogFileTraces traces = new LogFileTraces(fileName, file);
                traces.loadTraces();
                int maxState = traces.getMaxState();
                int burnin = xo.getAttribute(MarginalLikelihoodAnalysisParser.BURN_IN, maxState / 10);
                if (burnin < 0 || burnin >= maxState) {
                    burnin = maxState / 5;
                    System.out.println("WARNING: Burn-in larger than total number of states - using to 20%");
                }
                traces.setBurnIn(burnin);
                double samples[][][] = new double[NUM_VARIABLES][][];
                for (int variable = 0; variable < NUM_VARIABLES; ++variable) {
                    XMLObject cxo = xo.getChild(names[variable]);
                    String columnName = cxo.getStringAttribute(Attribute.NAME);
                    int traceStartIndex = -1;
                    int traceEndIndex = -1;
                    boolean traceIndexFound = false;
                    for (int i = 0; i < traces.getTraceCount(); i++) {
                        String traceName = traces.getTraceName(i);
                        if (traceName.trim().contains(columnName)) {
                            traceEndIndex = i;
                            if (!traceIndexFound) {
                                traceStartIndex = i;
                                traceIndexFound = true;
                            }
                        }
                    }
                    if (traceStartIndex == -1) {
                        throw new XMLParseException(columnName + " columns can not be found for " + getParserName() + " element.");
                    }
                    int numberOfSites = 1 + (traceEndIndex - traceStartIndex);
                    double[][] countPerSite = new double[numberOfSites][];
                    for (int a = 0; a < numberOfSites; a++) {
                        List<Double> values = traces.getValues((a + traceStartIndex));
                        countPerSite[a] = new double[values.size()];
                        for (int i = 0; i < values.size(); i++) {
                            countPerSite[a][i] = values.get(i);
                        }
                    }
                    samples[variable] = countPerSite;
                }
                OldDnDsPerSiteAnalysis analysis = new OldDnDsPerSiteAnalysis(samples[COND_S], samples[UNCOND_S], samples[COND_N], samples[UNCOND_N]);
                System.out.println(analysis.output());
                return analysis;
            } catch (FileNotFoundException fnfe) {
                throw new XMLParseException("File '" + fileName + "' can not be opened for " + getParserName() + " element.");
            } catch (IOException ioe) {
                throw new XMLParseException(ioe.getMessage());
            } catch (TraceException e) {
                throw new XMLParseException(e.getMessage());
            }
        }

        public String getParserDescription() {
            return "Performs a trace dN dS analysis.";
        }

        public Class getReturnType() {
            return OldDnDsPerSiteAnalysis.class;
        }

        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        private final XMLSyntaxRule[] rules = { new StringAttributeRule(FileHelpers.FILE_NAME, "The traceName of a BEAST log file (can not include trees, which should be logged separately") };
    };

    private final int numSites;

    private final int numSamples;

    private double[][][] allSamples;

    private static final int NUM_VARIABLES = 4;

    private static final int COND_S = 0;

    private static final int UNCOND_S = 1;

    private static final int COND_N = 2;

    private static final int UNCOND_N = 3;

    private static final String[] names = { COND_SPERSITE_COLUMNS, UNCOND_SPERSITE_COLUMNS, COND_NPERSITE_COLUMNS, UNCOND_NPERSITE_COLUMNS };

    private double[][][] smoothSamples;

    private double[][][] smoothDnDsSamples;

    private static final boolean DEBUG = true;

    private double[][] rawMeanStats;

    private double[][] smoothMeanStats;

    private double[][] smoothMeanDnDsStats;

    private double[][][] smoothHPDDnDsStats;
}
