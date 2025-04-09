import java.io.*;
import java.util.*;

/**
 * Reads the values from the output of Fred's method, usually contained in the file
 * acinas.out
 *
 * @author Andrew Warner
 * @version 1.0
 */
public class FredValsReader {

    /**
     * There is no constructor since this will only be used from a static context
     */
    public FredValsReader() {
    }

    /**
     * Read the values of the Fred's method output file (usually output.dat) into
     * an ArrayList
     * @pre filename is the name of a valid input file
     * @post the returned arraylist is an array of FredOutVals from the input file
     * @param filename the file to read the FredOutVals from
     */
    public static ArrayList<FredOutVal> readFredOutVals(File filename) throws IOException {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
            return null;
        }
        StringTokenizer reader;
        ArrayList<FredOutVal> returnVals = new ArrayList<FredOutVal>();
        double[] vals = new double[4];
        double[] percentages = new double[6];
        Double convertedVal;
        String nextLine = input.readLine();
        String token;
        while (nextLine != null) {
            reader = new StringTokenizer(nextLine);
            for (int i = 0; i < vals.length; i++) {
                if (!reader.hasMoreTokens()) {
                    throw new IndexOutOfBoundsException("Invalid input file for Fred sorting");
                }
                token = reader.nextToken();
                token = token.substring(0, token.length() - 1);
                convertedVal = new Double(token);
                vals[i] = convertedVal.doubleValue();
            }
            for (int i = 0; i < percentages.length; i++) {
                if (!reader.hasMoreTokens()) {
                    throw new IndexOutOfBoundsException("Invalid input file for Fred sorting");
                }
                token = reader.nextToken();
                if (i != percentages.length - 1) {
                    token = token.substring(0, token.length() - 1);
                }
                convertedVal = new Double(token);
                percentages[i] = convertedVal.doubleValue();
            }
            FredOutVal nextVal = new FredOutVal(vals[0], vals[1], (int) vals[2], vals[3], percentages.clone());
            returnVals.add(nextVal);
            nextLine = input.readLine();
        }
        input.close();
        return returnVals;
    }

    /**
     * Reads the values of omega, sigma, and npop from the hillclimbing output file,
     * exponentiates them, and outputs them as a FredOutVal with likelihoods as 0
     * @pre hClimbOut is a valid hill climbing output file
     * @post FredOutVal contains the final data points from hill climbing
     * @param hClimbOut A valid hill climbing output file
     * @return the FredOutVal with the final omega, sigma, and npop for hillclimbing
     */
    public static FredOutVal readHillClimbOutput(File hClimbOut) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(hClimbOut));
        String nextLine = input.readLine();
        StringTokenizer tok;
        while (nextLine != null) {
            tok = new StringTokenizer(nextLine);
            if (tok.hasMoreTokens()) {
                if (tok.nextToken().equals("Minimum")) {
                    break;
                }
            }
            nextLine = input.readLine();
        }
        if (nextLine == null) {
            System.out.println("Bad hillclimb output file");
            return null;
        }
        nextLine = input.readLine();
        nextLine = input.readLine();
        tok = new StringTokenizer(nextLine);
        double omega = (new Double(tok.nextToken())).doubleValue();
        double sigma = (new Double(tok.nextToken())).doubleValue();
        int npop = (int) ((new Double(tok.nextToken())).doubleValue() + 0.5);
        omega = Math.exp(omega);
        sigma = Math.exp(sigma);
        double[] percentages = new double[6];
        FredOutVal returnVal = new FredOutVal(omega, sigma, npop, 1.0e25, percentages);
        input.close();
        return returnVal;
    }
}
