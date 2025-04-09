package edu.byu.ece.bitwidth.ptolemy.strategies;

import java.io.PrintStream;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.byu.ece.bitwidth.ptolemy.BitwidthDirector;
import edu.byu.ece.bitwidth.ptolemy.actor.BitwidthActor;
import edu.byu.ece.bitwidth.ptolemy.actor.TokenCollector;
import edu.byu.ece.bitwidth.ptolemy.data.BitwidthToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;

public class FindUniformBitwidth extends PrecisionStrategy {

    private final int MAXLENGTH = 32;

    private int currentLength;

    private final BitwidthDirector director;

    private int high, low;

    private List<BitwidthActor> quantizerList;

    private Log logger = LogFactory.getLog(this.getClass());

    private PrintStream report;

    public FindUniformBitwidth(BitwidthDirector director) {
        this.director = director;
    }

    @Override
    public boolean postfire(List<TokenCollector> outports) throws IllegalActionException {
        boolean done = false;
        boolean metErrorBound = true;
        for (TokenCollector tc : outports) {
            metErrorBound &= tc.metErrorConstraint();
        }
        if (findUniformBW(metErrorBound)) {
            report.println("Done finding Uniform Bitwidth\n");
            logger.info("Done finding Uniform Bitwidth\n");
            done = true;
        }
        return !done;
    }

    public long getDuration() {
        return 0;
    }

    /**
	 * 
	 * @param quantizers
	 * @param metErrorBound
	 * @param quantizerList 
	 * @return False if it needs to run again, True if a Uniform Bitwidth has been found.
	 * @throws IllegalActionException
	 */
    public boolean findUniformBW(boolean metErrorBound) throws IllegalActionException {
        director.resetStateElements();
        if (metErrorBound) {
            high = currentLength;
        } else {
            low = currentLength;
        }
        currentLength = (high + low) / 2;
        if (currentLength == high) {
            return true;
        }
        if (currentLength == low) {
            currentLength = high;
            return true;
        }
        setUniformValues();
        return false;
    }

    @Override
    public BitwidthToken newInstance(Token t) throws IllegalActionException {
        return null;
    }

    @Override
    public boolean prefire() {
        return false;
    }

    @Override
    public void initialize(List<BitwidthActor> quantizerList, PrintStream report) throws IllegalActionException {
        this.quantizerList = quantizerList;
        this.report = report;
        currentLength = MAXLENGTH / 2;
        high = MAXLENGTH;
        low = 0;
    }

    private boolean setUniformValues() throws IllegalActionException {
        for (BitwidthActor q : quantizerList) {
            q.setPrecision(currentLength);
            q.savePrecision();
        }
        report.println("Current Length is: " + currentLength);
        logger.debug("Current Length is: " + currentLength);
        return true;
    }

    public void setUniformValue(int precision) throws IllegalActionException {
        currentLength = precision;
        setUniformValues();
    }

    public int getUniformValue() {
        return currentLength;
    }

    public boolean setInitialUniformValues() throws IllegalActionException {
        currentLength = MAXLENGTH;
        return setUniformValues();
    }

    public String getName() {
        return "Binary Search Uniform Bitwidth Finder";
    }
}
