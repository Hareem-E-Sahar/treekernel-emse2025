package tablatureGenerator.csp.strategies;

import java.util.LinkedList;
import java.util.Queue;
import tablatureGenerator.csp.ISearchStrategy;
import tablatureGenerator.csp.TablatureModel;
import tablatureGenerator.csp.TablatureSolver;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class DivisionStrategy implements ISearchStrategy {

    public void apply(TablatureSolver solver) {
        TablatureModel model = (TablatureModel) solver.getModel();
        IntDomainVar[] allVars = new IntDomainVar[model.score.getBeatCount()];
        for (int i = 0; i < model.score.getBeatCount(); i++) {
            allVars[i] = solver.getVar(model.fretVars[i]);
        }
        IntDomainVar[] varOrder = buildVarOrder(allVars);
        solver.addGoal(new AssignVar(new StaticVarOrder(solver, varOrder), new IncreasingDomain()));
        solver.generateSearchStrategy();
    }

    private IntDomainVar[] buildVarOrder(IntDomainVar[] vars) {
        IntDomainVar[] order = new IntDomainVar[vars.length];
        Queue<Integer> begins = new LinkedList<Integer>();
        Queue<Integer> ends = new LinkedList<Integer>();
        order[0] = vars[0];
        begins.add(1);
        int e = 8;
        ends.add(e);
        int index = 1;
        while (begins.size() > 0) {
            int begin = begins.remove();
            int end = ends.remove();
            int middle = (begin + end) / 2;
            System.out.print(middle + "-" + begin + ", ");
            order[index++] = vars[middle];
            if (middle - begin > 1) {
                begins.add(begin);
                ends.add(middle);
            }
            if (end - middle > 1) {
                begins.add(middle);
                ends.add(end);
            }
        }
        System.out.print(e + "-" + (e / 2) + ", ");
        order[index] = vars[vars.length - 1];
        return order;
    }
}
