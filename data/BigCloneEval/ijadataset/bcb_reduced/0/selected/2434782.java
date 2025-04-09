package tablatureGenerator.csp.objectiveFunctions;

import java.util.LinkedList;
import java.util.Queue;
import com.sun.org.apache.bcel.internal.generic.FRETURN;
import tablatureGenerator.csp.IObjectiveFunction;
import tablatureGenerator.csp.TablatureModel;
import tablatureGenerator.csp.TablatureSolver;
import tablatureGenerator.music.instruments.AbstractStringInstrument;
import tablatureGenerator.music.score.Score;
import choco.Choco;
import choco.Options;
import choco.cp.model.managers.operators.MinManager;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;

@SuppressWarnings("unused")
public class FretVariation implements IObjectiveFunction {

    public MovesDistancesVars movesDistances;

    public FretVariation() {
    }

    public void apply(TablatureSolver solver) {
        globalApproach(solver);
    }

    private void localApproach(TablatureSolver solver) {
        TablatureModel model = solver.getTablatureModel();
        AbstractStringInstrument instrument = model.instrument;
        Score score = model.score;
        movesDistances = new MovesDistancesVars(instrument, score);
        for (int i = 0; i < score.getBeatCount() - 1; i++) {
            model.addConstraint(Choco.eq(movesDistances.localDistances[i], Choco.abs(Choco.minus(model.fretVars[i + 1], model.fretVars[i]))));
        }
        model.addConstraint(Choco.eq(movesDistances.totalDistance, Choco.sum(movesDistances.localDistances)));
    }

    private void globalApproach(TablatureSolver solver) {
        TablatureModel model = solver.getTablatureModel();
        AbstractStringInstrument instrument = model.instrument;
        Score score = model.score;
        movesDistances = new MovesDistancesVars(instrument, score);
        Queue<Integer> begins = new LinkedList<Integer>();
        Queue<Integer> ends = new LinkedList<Integer>();
        begins.add(0);
        ends.add(score.getBeatCount() - 1);
        int index = 0;
        while (begins.size() > 0) {
            int begin = begins.remove();
            int end = ends.remove();
            int middle = (begin + end) / 2;
            model.addConstraint(Choco.eq(movesDistances.globalDistances[index], Choco.abs(Choco.minus(model.fretVars[middle], model.fretVars[begin]))));
            index++;
            if (middle - begin > 1) {
                begins.add(begin);
                ends.add(middle);
            }
            if (end - middle > 1) {
                begins.add(middle);
                ends.add(end);
            }
        }
        model.addConstraint(Choco.eq(movesDistances.globalDistances[index], Choco.abs(Choco.minus(model.fretVars[score.getBeatCount() - 1], model.fretVars[score.getBeatCount() / 2]))));
        model.addConstraint(Choco.eq(movesDistances.totalDistance, Choco.sum(movesDistances.globalDistances)));
    }

    public static class MovesDistancesVars {

        public IntegerVariable[] localDistances;

        public IntegerVariable[] globalDistances;

        public IntegerVariable[] minMaxDiffs;

        public final int MINMAX_BLOCKS_SIZE = 10;

        public int minMaxBlocksCount;

        public IntegerVariable[] dirUp;

        public IntegerVariable[] dirDown;

        public IntegerVariable totalDistance;

        public MovesDistancesVars(AbstractStringInstrument instrument, Score score) {
            int n = score.getBeatCount() - 1;
            localDistances = new IntegerVariable[n];
            globalDistances = new IntegerVariable[n];
            for (int i = 0; i < n; i++) {
                localDistances[i] = Choco.makeIntVar("Deplacement " + (i + 1), 0, instrument.getFretCount(), Options.V_NO_DECISION);
                globalDistances[i] = Choco.makeIntVar("Deplacement " + (i + 1), 0, instrument.getFretCount(), Options.V_NO_DECISION);
            }
            totalDistance = Choco.makeIntVar("Deplacement total", Options.V_OBJECTIVE);
        }
    }
}
