package org.systemsbiology.chem;

import org.syntax.jedit.tokenmarker.PLSQLTokenMarker;
import org.systemsbiology.math.*;
import org.systemsbiology.util.*;

/**
 * Simulates the dynamics of a set of coupled chemical reactions
 * described by {@link Reaction} objects using the Gillespie stochastic
 * algorithm, "direct method".
 *
 * @author Stephen Ramsey
 */
public final class SimulatorStochasticLDM extends SimulatorStochasticBase implements IAliasableClass, ISimulator {

    public static final String CLASS_ALIAS = "logarithmic-direct";

    private static final long NUMBER_FIRINGS = 1;

    private double[] ArrayAggregateProb = null;

    private double[] InitialArrayAggregateProb = null;

    private Object[] mReactionDependencies;

    private int[] FirstReactionModified;

    private boolean deadlock;

    public DependencyGraphCreator dependcreat;

    private double deltaTimeToNextReaction;

    protected void prepareForStochasticSimulation(double pStartTime, SimulatorParameters pSimulatorParameters) throws DataNotFoundException {
        deadlock = false;
        ArrayAggregateProb = (double[]) InitialArrayAggregateProb.clone();
    }

    protected final int chooseIndexOfNextReaction(double[] ArrayAggregateReactionProb) throws IllegalArgumentException {
        double randomNumberUniformInterval = getRandomNumberUniformInterval(mRandomNumberGenerator);
        double fractionOfAggregateReactionProbabilityDensity = randomNumberUniformInterval * ArrayAggregateReactionProb[ArrayAggregateReactionProb.length - 1];
        int low = 0;
        int high = ArrayAggregateReactionProb.length;
        int mid;
        int counter = 0;
        while (low <= high) {
            mid = (low + high) / 2;
            counter++;
            if (ArrayAggregateReactionProb[mid] < fractionOfAggregateReactionProbabilityDensity) low = mid + 1; else high = mid - 1;
        }
        if (ArrayAggregateReactionProb[low] > fractionOfAggregateReactionProbabilityDensity) {
            actualaverageSearchDepthArray = counter;
            return (low);
        } else {
            return (-1);
        }
    }

    protected double iterate(MutableInteger pLastReactionIndex) throws DataNotFoundException, IllegalStateException {
        deadlock = false;
        double time = mSymbolEvaluator.getTime();
        int lastReactionIndex = pLastReactionIndex.getValue();
        if (NULL_REACTION != lastReactionIndex) {
            updateSymbolValuesForReaction(lastReactionIndex, mDynamicSymbolValues, mDynamicSymbolDelayedReactionAssociations, NUMBER_FIRINGS);
            if (mUseExpressionValueCaching) {
                clearExpressionValueCaches();
            }
        }
        computeReactionProbabilities(lastReactionIndex);
        deltaTimeToNextReaction = Double.POSITIVE_INFINITY;
        if (ArrayAggregateProb[ArrayAggregateProb.length - 1] == 0) deadlock = true;
        if (ArrayAggregateProb[ArrayAggregateProb.length - 1] > 0.0) {
            deltaTimeToNextReaction = chooseDeltaTimeToNextReaction(ArrayAggregateProb[ArrayAggregateProb.length - 1]);
        }
        int reactionIndex = -1;
        if (null != mDelayedReactionSolvers) {
            int nextDelayedReactionIndex = getNextDelayedReactionIndex(mDelayedReactionSolvers);
            if (nextDelayedReactionIndex >= 0) {
                DelayedReactionSolver solver = mDelayedReactionSolvers[nextDelayedReactionIndex];
                double nextDelayedReactionTime = solver.peekNextReactionTime();
                if (nextDelayedReactionTime < time + deltaTimeToNextReaction) {
                    deltaTimeToNextReaction = nextDelayedReactionTime - time;
                    reactionIndex = solver.getReactionIndex();
                    solver.pollNextReactionTime();
                }
            }
        }
        if (-1 == reactionIndex && ArrayAggregateProb[ArrayAggregateProb.length - 1] > 0.0) {
            reactionIndex = chooseIndexOfNextReaction(ArrayAggregateProb);
        }
        if (-1 != reactionIndex) {
            pLastReactionIndex.setValue(reactionIndex);
            time += deltaTimeToNextReaction;
        } else {
            time = Double.POSITIVE_INFINITY;
        }
        mSymbolEvaluator.setTime(time);
        return (time);
    }

    public Reaction getReactionfromName(String name) {
        return mReactions[((Integer) mReactionIndexes.get(name)).intValue()];
    }

    public Reaction[] getReactions() {
        return mReactions;
    }

    public void fireNextReactionEvent(MutableInteger pLastReactionIndex) throws DataNotFoundException {
        if (-1 == pLastReactionIndex.getValue()) {
            pLastReactionIndex.setValue(chooseIndexOfNextReaction(ArrayAggregateProb));
            if (-1 != pLastReactionIndex.getValue()) {
                updateSymbolValuesForReaction(pLastReactionIndex.getValue(), mDynamicSymbolValues, mDynamicSymbolDelayedReactionAssociations, NUMBER_FIRINGS);
                mSymbolEvaluator.setTime(mSymbolEvaluator.getTime() + deltaTimeToNextReaction);
            }
        } else {
            updateSymbolValuesForReaction(pLastReactionIndex.getValue(), mDynamicSymbolValues, mDynamicSymbolDelayedReactionAssociations, NUMBER_FIRINGS);
            mSymbolEvaluator.setTime(mSymbolEvaluator.getTime() + deltaTimeToNextReaction);
        }
    }

    public void computeReactionProbabilities(int lastReactionIndex) throws DataNotFoundException {
        if (lastReactionIndex != -1) {
            mReactionProbabilities[lastReactionIndex] = computeReactionRate(lastReactionIndex);
            Integer[] dependentReactions = (Integer[]) mReactionDependencies[lastReactionIndex];
            int numDependentReactions = dependentReactions.length;
            for (int ctr = numDependentReactions; --ctr >= 0; ) {
                Integer dependentReactionCtrObj = dependentReactions[ctr];
                int dependentReactionCtr = dependentReactionCtrObj.intValue();
                mReactionProbabilities[dependentReactionCtr] = computeReactionRate(dependentReactionCtr);
            }
            if (FirstReactionModified[lastReactionIndex] == 0) {
                ArrayAggregateProb[0] = mReactionProbabilities[0];
                for (int i = 1; i < mReactionProbabilities.length; i++) ArrayAggregateProb[i] = ArrayAggregateProb[i - 1] + mReactionProbabilities[i];
            } else for (int i = FirstReactionModified[lastReactionIndex]; i < mReactionProbabilities.length; i++) ArrayAggregateProb[i] = ArrayAggregateProb[i - 1] + mReactionProbabilities[i];
        } else {
            computeReactionProbabilities();
            ArrayAggregateProb[0] = mReactionProbabilities[0];
            for (int i = 1; i < mReactionProbabilities.length; i++) ArrayAggregateProb[i] = ArrayAggregateProb[i - 1] + mReactionProbabilities[i];
        }
    }

    public void initialize(Model pModel) throws DataNotFoundException, InvalidInputException {
        initializeSimulator(pModel);
        initializeSimulatorStochastic(pModel);
        setInitialized(true);
        computeReactionProbabilities();
        ArrayAggregateProb = new double[mReactionProbabilities.length];
        ArrayAggregateProb[0] = mReactionProbabilities[0];
        for (int i = 1; i < mReactionProbabilities.length; i++) ArrayAggregateProb[i] = ArrayAggregateProb[i - 1] + mReactionProbabilities[i];
        InitialArrayAggregateProb = (double[]) ArrayAggregateProb.clone();
        dependcreat = new DependencyGraphCreator(this);
        FirstReactionModified = dependcreat.getFirstReactionModified();
        mReactionDependencies = dependcreat.getReactionDependencies();
    }

    public double getTimeNextReactioEvent(MutableInteger pLastReactionIndex) {
        pLastReactionIndex.setValue(NULL_REACTION);
        double time = mSymbolEvaluator.getTime();
        deltaTimeToNextReaction = chooseDeltaTimeToNextReaction(ArrayAggregateProb[ArrayAggregateProb.length - 1]);
        if (null != mDelayedReactionSolvers) {
            int nextDelayedReactionIndex = getNextDelayedReactionIndex(mDelayedReactionSolvers);
            if (nextDelayedReactionIndex >= 0) {
                DelayedReactionSolver solver = mDelayedReactionSolvers[nextDelayedReactionIndex];
                double nextDelayedReactionTime = solver.peekNextReactionTime();
                if (nextDelayedReactionTime < time + deltaTimeToNextReaction) {
                    deltaTimeToNextReaction = nextDelayedReactionTime - time;
                    pLastReactionIndex.setValue(solver.getReactionIndex());
                    solver.pollNextReactionTime();
                }
            }
        }
        return deltaTimeToNextReaction;
    }

    public double getDeltaTimeToNextReaction() {
        return deltaTimeToNextReaction;
    }

    public boolean isInDeadlock() {
        return deadlock;
    }

    protected void modifyDefaultSimulatorParameters(SimulatorParameters pSimulatorParameters) {
    }

    public String getAlias() {
        return (CLASS_ALIAS);
    }

    public boolean isExactSimulator() {
        return true;
    }

    public boolean getDeadlock() {
        return deadlock;
    }
}
