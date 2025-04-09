package solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import model.ClusterManager;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.integer.AbstractIntVarSelector;
import choco.kernel.solver.search.integer.ValIterator;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * CSolver implements a constraint solver to generate initial solutions
 * that satisfy all hard constraints of the problem.
 * @author Paul A. Rubin
 */
public class CSolver {

    public enum Result {

        TIMELIMIT, SOLUTIONLIMIT, UNSOLVED, SOLVED, BORKED
    }

    ;

    private int maxRosters;

    private int minRosters;

    private int maxSize;

    private int minSize;

    private int[] capacity;

    private int nClusters;

    private boolean useAll;

    private ClusterManager mgr;

    private Model model;

    private CPSolver solver;

    private IntegerVariable[][] assign;

    private IntegerVariable[][] transpose;

    private IntegerVariable[] size;

    private IntegerVariable[] assignTo;

    private IntegerVariable[] used;

    private IntegerVariable nUsed;

    private List<Integer> singletons;

    private SolutionFactory factory;

    private Result result;

    private int nFound;

    /**
   * Signals an infeasibile constraint problem.
   */
    public class InfeasibilityException extends Exception {
    }

    /**
   * Signals failure to find any solutions, possibility due to the time limit.
   */
    public class NoSolutionException extends Exception {
    }

    /**
   * Signals full enumeration of the feasible region (the problem is solved!)
   */
    public class EnumerationException extends Exception {

        public EnumerationException(String msg) {
            super(msg);
        }
    }

    /**
   * Constructor
   * @param maxR  maximum number of rosters
   * @param minR  minimum number of rosters
   * @param maxS  maximum roster size
   * @param minS  minimum roster size
   * @param u     mandatory to use all individuals?
   * @param cm    the applicable cluster manager
   * @param f     the solution factory that owns this instance
   */
    public CSolver(int maxR, int minR, int maxS, int minS, boolean u, ClusterManager cm, SolutionFactory f) {
        this.model = new CPModel();
        this.solver = null;
        this.nFound = 0;
        this.maxRosters = maxR;
        this.minRosters = minR;
        this.maxSize = maxS;
        this.minSize = minS;
        this.useAll = u;
        this.mgr = cm;
        this.nClusters = cm.getClusterCount();
        this.capacity = cm.getCapacities();
        this.singletons = cm.getSingletons();
        this.factory = f;
        this.result = Result.UNSOLVED;
        if (useAll) {
            int all = 0;
            for (int c : capacity) {
                all += c;
            }
            int m = (int) Math.round(Math.ceil(((double) all) / maxSize));
            minRosters = Math.max(minRosters, m);
        }
        assign = new IntegerVariable[nClusters][maxRosters + 1];
        transpose = new IntegerVariable[maxRosters + 1][nClusters];
        for (int c = 0; c < nClusters; c++) {
            int x = capacity[c];
            for (int r = 1; r <= maxRosters; r++) {
                assign[c][r] = Choco.makeIntVar("assign_" + c + "_" + r, 0, x);
                model.addVariable(assign[c][r]);
                transpose[r][c] = assign[c][r];
            }
            assign[c][0] = Choco.makeIntVar("unassigned_" + c, 0, (useAll) ? 0 : x);
            model.addVariable(assign[c][0]);
            transpose[0][c] = assign[c][0];
        }
        size = new IntegerVariable[maxRosters + 1];
        used = new IntegerVariable[maxRosters + 1];
        int[] temp = new int[maxSize - minSize + 2];
        temp[0] = 0;
        for (int i = 0; i <= maxSize - minSize; i++) {
            temp[i + 1] = minSize + i;
        }
        for (int r = 1; r <= minRosters; r++) {
            size[r] = Choco.makeIntVar("size_" + r, minSize, maxSize);
            used[r] = Choco.makeIntVar("used_" + r, 1, 1);
        }
        for (int r = minRosters + 1; r <= maxRosters; r++) {
            size[r] = Choco.makeIntVar("size_" + r, temp);
            used[r] = Choco.makeIntVar("used_" + r, 0, 1);
        }
        int h = (useAll) ? 1 : 0;
        used[0] = Choco.makeIntVar("used_0", 0, 0);
        assignTo = new IntegerVariable[nClusters];
        for (int i : singletons) {
            assignTo[i] = Choco.makeIntVar("assign_" + i + "_to", h, maxRosters);
        }
        nUsed = Choco.makeIntVar("nRosters", minRosters, maxRosters);
        model.addVariable(nUsed);
        for (int c = 0; c < nClusters; c++) {
            model.addConstraint(Choco.eq(Choco.sum(assign[c]), capacity[c]));
        }
        for (int r = 1; r <= maxRosters; r++) {
            model.addConstraint(Choco.eq(Choco.sum(transpose[r]), size[r]));
        }
        for (int r = 1; r <= maxRosters; r++) {
            model.addConstraint(Choco.reifiedIntConstraint(used[r], Choco.gt(size[r], 0)));
        }
        model.addConstraint(Choco.eq(Choco.sum(used), nUsed));
        for (int i : singletons) {
            model.addConstraint(Choco.domainConstraint(assignTo[i], assign[i]));
        }
        Set<HashSet<Integer>> cliques = mgr.getCliques();
        for (Set<Integer> c : cliques) {
            if (c.size() < 2) {
                continue;
            }
            Integer[] clist = c.toArray(new Integer[1]);
            for (int i = 1; i < clist.length; i++) {
                model.addConstraint(Choco.eq(assignTo[clist[0]], assignTo[clist[i]]));
            }
        }
        Map<Integer, ArrayList<Integer>> incompatible = mgr.getIncompatible();
        if (incompatible != null) {
            for (int i0 : incompatible.keySet()) {
                for (int i1 : incompatible.get(i0)) {
                    if (i0 < i1) {
                        model.addConstraint(Choco.or(Choco.eq(assignTo[i0], 0), Choco.neq(assignTo[i0], assignTo[i1])));
                    }
                }
            }
        }
        for (int r = 1; r < maxRosters; r++) {
            model.addConstraint(Choco.lexeq(transpose[r + 1], transpose[r]));
        }
        for (int r = minRosters; r < maxRosters; r++) {
            model.addConstraint(Choco.leq(used[r + 1], used[r]));
        }
    }

    /**
   * Tries to generate an assignment that meets all mandatory constraints.
   * @param tlim time limit (in seconds) for finding solutions
   * @param slim solution limit
   * @return the final solution status
   */
    public Result solve(long tlim, int slim) throws InfeasibilityException, NoSolutionException, EnumerationException {
        solver = new CPSolver();
        solver.read(model);
        solver.setVarIntSelector(new AssignmentVarSelector(solver));
        solver.setValIntIterator(new AssignmentValIterator(solver.getVar(nUsed)));
        int tl = (tlim < 1) ? 1000 : (int) (1000 * tlim);
        solver.setTimeLimit(tl);
        nFound = 0;
        result = nextSolution();
        while (nFound < slim && result == Result.SOLVED) {
            result = nextSolution();
        }
        if (nFound == slim) {
            result = Result.SOLUTIONLIMIT;
        }
        return result;
    }

    /**
   * Inner class used to select variables for branching.
   */
    class AssignmentVarSelector extends AbstractIntVarSelector {

        private IntDomainVar nT;

        private IntDomainVar[][] a;

        /**
     * Constructor.
     * @param solver the solver that will use this instance
     */
        public AssignmentVarSelector(Solver solver) {
            this.nT = solver.getVar(nUsed);
            a = new IntDomainVar[nClusters][maxRosters + 1];
            for (int i = 0; i < nClusters; i++) {
                for (int j = 0; j <= maxRosters; j++) {
                    a[i] = solver.getVar(assign[i]);
                }
            }
        }

        /**
     * selectIntVar selects the variable on which to branch.
     * Highest priority goes to the number of rosters if not instantiated.
     * Next highest priority goes to achieving mininum roster size for all
     * rosters being used.
     * After that, the first available assignment is used.
     * @return the variable on which to branch (or null if not possible)
     * @throws ContradictionException if a conundrum occurs
     */
        public IntDomainVar selectIntVar() throws ContradictionException {
            boolean[][] inst = new boolean[nClusters][maxRosters + 1];
            if (!nT.isInstantiated()) {
                return nT;
            }
            int n = nT.getVal();
            for (int t = 1; t <= n; t++) {
                int s = 0;
                for (int i = 0; i < nClusters; i++) {
                    if (a[i][t].isInstantiated()) {
                        inst[i][t] = true;
                        s += a[i][t].getVal();
                    } else {
                        inst[i][t] = false;
                    }
                }
                if (s < minSize) {
                    for (int i = 0; i < nClusters; i++) {
                        if (!inst[i][t]) {
                            return a[i][t];
                        }
                    }
                    System.err.println("About to throw an exception because roster " + t + " is short and no clusters are left to assign!");
                    throw new ContradictionException();
                }
            }
            for (int i = 0; i < nClusters; i++) {
                for (int t = 1; t <= n; t++) {
                    if (!inst[i][t]) {
                        return a[i][t];
                    }
                }
            }
            for (int i = 0; i < nClusters; i++) {
                if (!a[i][0].isInstantiated()) {
                    return a[i][0];
                }
            }
            return null;
        }
    }

    /**
   * Inner class used to select the next value to try during branching.
   * Assignment order is smallest nonzero value first, increasing to
   * the domain maximum, and then zero last.
   * The exception is the number of rosters, which starts with its maximum
   * value and works downward.
   */
    class AssignmentValIterator implements ValIterator {

        private IntDomainVar nRosters;

        private int nRlo;

        private int nRhi;

        /**
     * Constructor.
     * @param v the roster count variable (solver version)
     */
        public AssignmentValIterator(IntDomainVar v) {
            nRosters = v;
            nRlo = v.getInf();
            nRhi = v.getSup();
        }

        /**
     * Indicates whether the variable has another value to try.
     * @param x the variable
     * @param i the current value
     * @return true unless the variable is currently set to 0
     */
        public boolean hasNextVal(Var x, int i) {
            if (x.equals(nRosters)) {
                return i > nRlo;
            } else {
                return i != 0;
            }
        }

        /**
     * Gets the first value in the variable's domain.
     * @param x the variable
     * @return the first (smallest non-zero) value in the variable's domain
     */
        public int getFirstVal(Var x) {
            if (x.equals(nRosters)) {
                return nRhi;
            } else {
                return ((IntDomainVar) x).getNextDomainValue(0);
            }
        }

        /**
     * Gets the next value in the variable's domain.
     * @param x the variable
     * @param i the current value
     * @return the next value in the domain
     */
        public int getNextVal(Var x, int i) {
            if (x.equals(nRosters)) {
                return nRosters.getPrevDomainValue(i);
            } else if (i < ((IntDomainVar) x).getSup()) {
                return ((IntDomainVar) x).getNextDomainValue(i);
            } else if (((IntDomainVar) x).getInf() == 0) {
                return 0;
            } else {
                return i;
            }
        }
    }

    /**
   * Gets the number of solutions found.
   * @return the number of solutions found
   */
    public int getSolutionCount() {
        return nFound;
    }

    /**
   * Attempts to locate a new solution to the CP problem.
   * @return the result of the attempt
   * @throws solver.CSolver.InfeasibilityException if the model is infeasible
   * @throws solver.CSolver.NoSolutionException if the solver times out unresolved
   * @throws solver.CSolver.EnumerationException if all solutions enumerated
   */
    public Result nextSolution() throws InfeasibilityException, NoSolutionException, EnumerationException {
        Boolean ok;
        if (result == Result.UNSOLVED) {
            ok = solver.solve();
        } else {
            ok = solver.nextSolution();
        }
        if (ok == null) {
            if (nFound > 0) {
                result = Result.TIMELIMIT;
                return result;
            } else {
                result = Result.BORKED;
                throw new NoSolutionException();
            }
        } else if (ok) {
            int[][] a = new int[nClusters][maxRosters + 1];
            for (int c = 0; c < nClusters; c++) {
                for (int r = 0; r <= maxRosters; r++) {
                    a[c][r] = solver.getVar(assign[c][r]).getVal();
                }
            }
            factory.addCandidate(a);
            nFound++;
            result = Result.SOLVED;
            return result;
        } else {
            if (nFound > 0) {
                result = Result.BORKED;
                throw new EnumerationException(Integer.toString(nFound));
            } else {
                result = Result.BORKED;
                throw new InfeasibilityException();
            }
        }
    }

    /**
   * Returns the result of the last solution attempt.
   * @return the last solution result
   */
    public Result getResult() {
        return result;
    }

    /**
   * Resets the solver time limit.
   * @param t the new solver time limit (secs.)
   */
    public void setTimeLimit(int t) {
        solver.setTimeLimit(t);
    }
}
