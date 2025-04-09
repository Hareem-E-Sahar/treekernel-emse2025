import java.util.*;
import java.io.*;
import javax.swing.JTextArea;

/**
 * The confidence Interval class, extended by the 4 different types of 
 * cofidence intervals
 * 
 * @author Andrew Warner 
 * @version (a version number or a date)
 */
public class ConfidenceInterval extends Thread {

    /**
     * The last reader used for the confidence interval
     */
    protected CIReader lastReader;

    /**
     * The value from hillclimbing that we will use to find the npop confidence
     * interval
     */
    protected FredOutVal value;

    /**
     * The binning data
     */
    protected ArrayList bins;

    /**
     * The narrator class
     */
    protected NarrWriter narr;

    /**
     * The sequence values
     */
    protected int[] sequenceVals;

    /**
     * The confidence interval program to run
     */
    protected String cmd;

    /**
     * the input file for the confidence interval program
     */
    protected File input;

    /**
     * Whether or not we are running the new confidence interval or the old
     * one - the difference is only in reading the output
     */
    protected boolean isNew;

    /**
     * the range of the confidence interval; ie, the number to divide
     * the likelihood by to find out which likelihoods are out of confidence
     */
    protected double percentRange;

    /**
     * the type of confidence interval this is
     */
    protected String type;

    /**
     * The upper bound of the confidence interval program
     */
    protected double upperBound;

    /**
     * The lower bound for all confidence intervals is 1.0e-7 (besides npop)
     */
    protected double lowerBound = 1.0e-7;

    /**
     * the number of values to do within the interval (ie the value to put in
     * xnumics under the corresponding variable)
     */
    protected int incUpper;

    /**
     * the number of increments to run for the lower bound
     */
    protected int incLower;

    /**
     * The text area where the main progress of the program is stored
     */
    protected JTextArea log;

    /**
     * The output file that the confidence interval program will write to
     */
    protected File output;

    /**
     * The number of increments per order of magnitude
     */
    protected int incPerOM;

    /**
     * The probability threshold; ie, the likelihood value from hillclimbing
     * divided by either 6.83 (two tailed) or 3.87 (one tailed); any likelihood
     * less than this value is outside the confidence interval
     */
    protected double probThresh;

    /**
     * Constructor for objects of class ConfidenceInterval
     */
    protected ConfidenceInterval(FredOutVal value, BinningAndFred results, NarrWriter narr, JTextArea log, File input, File output, String cmd, boolean isNew) {
        this.value = value;
        this.bins = results.getBins();
        this.narr = narr;
        this.log = log;
        this.sequenceVals = results.getSeqVals();
        this.input = input;
        this.cmd = cmd;
        this.isNew = isNew;
        this.output = output;
    }

    /**
     * Writes the input for the confidence interval
     * @param type Is either "npop", "sigma", "omega", or "drift"; allowing
     * the method to determine which variable range to modify
     * @param range Is the range of variable for the current confidence
     * interval
     * @param xnumics Are the values for xnumics for the given confidence
     * interval
     * @param input the input file to write to
     * 
     */
    protected void writeInput(File input, String type, double[] range, int[] xnumics, double probThresh) {
        this.probThresh = probThresh;
        double[] omegaRange, sigmaRange, driftRange;
        int[] npopRange;
        if (type.equals("omega")) omegaRange = range; else {
            omegaRange = new double[2];
            omegaRange[0] = value.getOmega();
            omegaRange[1] = 10000;
        }
        if (type.equals("sigma")) sigmaRange = range; else {
            sigmaRange = new double[2];
            sigmaRange[0] = value.getSigma();
            sigmaRange[1] = 10000;
        }
        if (type.equals("drift")) driftRange = range; else driftRange = MasterVariables.getDriftRange();
        if (type.equals("npop")) {
            npopRange = new int[2];
            npopRange[0] = (int) range[0];
            npopRange[1] = (int) range[1];
        } else {
            npopRange = new int[2];
            npopRange[0] = value.getNpop();
            npopRange[1] = 10000;
        }
        int jwhichxavg = MasterVariables.getSortPercentage() + 1;
        double[] percentages = value.getPercentages();
        int numSuccesses = MasterVariables.NUM_CI_SUCCESSES;
        int nrep = (int) (numSuccesses / probThresh);
        try {
            InputWriter.writeFile(input, bins, omegaRange, sigmaRange, npopRange, driftRange, xnumics, sequenceVals[0], nrep, sequenceVals[1], jwhichxavg, probThresh);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * runs the confidence interval
     * @param type the type of confidence interval to run
     */
    protected FredOutVal runCI(String type) {
        StreamGobbler errorGobbler;
        StreamGobbler outputGobbler;
        try {
            Process p;
            p = Runtime.getRuntime().exec(cmd);
            errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR");
            outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitVal = p.waitFor();
            if (isNew) return getOutputValue();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Reads in the output data from the output file for this particular
     * confidence interval and returns the last data point recorded (either
     * the first value out of confidence or the last value run; in the case
     * of sigma if this value is still within confidence and sigma is 100
     * then sigma is > 100)
     * @return the result from this run of the confidence interval
     */
    protected FredOutVal getOutputValue() {
        try {
            BufferedReader input = new BufferedReader(new FileReader(output));
            ArrayList values = new ArrayList();
            String line = input.readLine();
            while (line != null) {
                values.add(line);
                line = input.readLine();
            }
            String result;
            StringTokenizer tk;
            double omega = -1, sigma = -1, drift = -1, yvalue = -1;
            int npop = -1;
            for (int i = 0; i < 2; i++) {
                result = values.get(values.size() - (2 * i + 2)) + "  " + values.get(values.size() - (2 * i + 1));
                tk = new StringTokenizer(result);
                omega = (new Double(tk.nextToken())).doubleValue();
                sigma = (new Double(tk.nextToken())).doubleValue();
                npop = (new Integer(tk.nextToken())).intValue();
                drift = (new Double(tk.nextToken())).doubleValue();
                yvalue = (-1) * (new Double(tk.nextToken())).doubleValue();
                if (yvalue > probThresh) break;
                if (values.size() < 3) break;
            }
            int sortPer = MasterVariables.getSortPercentage();
            double[] percentagesRes = new double[6];
            percentagesRes[sortPer] = yvalue;
            input.close();
            narr.println();
            narr.println("The output file for the " + type + "confidence interval:");
            narr.writeInput(output);
            return new FredOutVal(omega, sigma, npop, drift, percentagesRes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(FredOutVal newVal) {
        this.value = newVal;
    }

    public CIReader getLastReader() {
        return lastReader;
    }

    public void setUpperBound(double newVal) {
        this.upperBound = newVal;
    }
}
