package gov.sns.tools.optimizer;

import java.util.*;

/**
 *  This class implements Brent's algorithm of an one-dimensional search.
 *
 *@author    shishlo
 */
public class BrentOneDimSearchAlgorithm extends SearchAlgorithm {

    private double CGOLD = 0.381966;

    private int ITMAX = 100;

    private double ZEPS = 1.0e-10;

    private double TOL = 1.0e-9;

    private double bestScore = Double.MAX_VALUE;

    private boolean shouldStop = true;

    private double[] x_arr = new double[1];

    private BracketFinder bracketFinder = new BracketFinder();

    private SearchAlgorithm externalSA = null;

    private boolean debug = false;

    public void setItmax(int itmax) {
        ITMAX = itmax;
    }

    public void setZeps(double zeps) {
        ZEPS = zeps;
    }

    public void setTol(double tol) {
        TOL = tol;
    }

    /**
	 *  Creates a new instance of BrentOneDimSearchAlgorithm
	 */
    public BrentOneDimSearchAlgorithm() {
        super();
    }

    /**
	 *  Sets the maximal number of iteration for one dimensional search
	 *
	 *@param  ITMAX  The new maximal number of iteration
	 */
    public void setMaxIteration(int ITMAX) {
        this.ITMAX = ITMAX;
    }

    /**
	 *  Sets the external Search Algorithm to the
	 *  BrentOneDimSearchAlgorithm object.
	 *  This method is used if an outside algorithm uses one dimensional search. 
	 *
	 *@param  externalSA  The new external Search Algorithm
	 */
    void setExternalSearchAlgorithm(SearchAlgorithm externalSA) {
        this.externalSA = externalSA;
    }

    /**
	 *  Gets the external Search Algorithm of the
	 *  BrentOneDimSearchAlgorithm object
	 *
	 *@return    The external Search Algorithm
	 */
    protected SearchAlgorithm getExternalSearchAlgorithm() {
        return externalSA;
    }

    /**
	 *  reset for searching from scratch; forget history
	 */
    @Override
    protected void reset() {
        shouldStop = false;
        List<ParameterProxy> varsV = getVariables();
        int nD = varsV.size();
        if (nD != 1) {
            shouldStop = true;
            return;
        }
        ParameterProxy variable = varsV.get(0);
        double x = variable.getValue();
        x_arr[0] = x;
        if (!accept(x_arr)) {
            shouldStop = true;
            return;
        }
        bestScore = getScorer().score();
        Map<ParameterProxy, Double> bestMap = getBestSolutionMap();
        bestMap.clear();
        bestMap.put(variable, new Double(variable.getValue()));
        if (bestScore == 0.) {
            shouldStop = true;
        }
    }

    /**
	 *  Makes searching. It returns shouldStop boolean.
	 *
	 *@return    The boolean shouldStop variable
	 */
    @Override
    protected boolean makeStep() {
        if (shouldStop) {
            return shouldStop;
        }
        List<ParameterProxy> varsV = getVariables();
        int nD = varsV.size();
        if (nD != 1) {
            shouldStop = true;
            return shouldStop;
        }
        ParameterProxy variable = varsV.get(0);
        bracketFinder.setParameterProxy(variable);
        bracketFinder.setSearchAlgorithm(this);
        Pair[] resPairs = bracketFinder.getBrackets(variable.getValue());
        if (resPairs == null) {
            shouldStop = true;
            return shouldStop;
        }
        setBestSolution(resPairs[1], resPairs[0].x - resPairs[2].x);
        Pair a;
        Pair b;
        Pair x;
        Pair v;
        Pair w;
        a = new Pair();
        b = new Pair();
        x = new Pair();
        v = new Pair();
        w = new Pair();
        if (resPairs[0].x < resPairs[2].x) {
            a.copy(resPairs[0]);
            b.copy(resPairs[2]);
        } else {
            a.copy(resPairs[2]);
            b.copy(resPairs[0]);
        }
        x.copy(resPairs[1]);
        v.copy(resPairs[1]);
        w.copy(resPairs[1]);
        Pair u = new Pair();
        double d = 0.;
        double e = 0.;
        double xm;
        double tol1;
        double tol2;
        double temp;
        double p;
        double q;
        double r;
        for (int i = 0; i < ITMAX; i++) {
            xm = (a.x + b.x) / 2.0;
            tol1 = TOL * Math.abs(x.x) + ZEPS;
            tol2 = 2.0 * tol1;
            if (Math.abs(x.x - xm) <= (tol2 - (b.x - a.x) / 2.0)) {
                setBestSolution(x, b.x - a.x);
                shouldStop = true;
                return shouldStop;
            }
            if (Math.abs(e) > tol1) {
                boolean failParabolic = false;
                r = (x.x - w.x) * (x.f - v.f);
                q = (x.x - v.x) * (x.f - w.f);
                p = (x.x - v.x) * q - (x.x - w.x) * r;
                if (((x.x - w.x) == 0) || ((x.x - v.x) == 0) || ((v.x - w.x) == 0)) {
                    if (debug) {
                        System.out.println("failParabolic!");
                    }
                    failParabolic = true;
                } else {
                    if (debug) {
                        System.out.println("not failParabolic");
                    }
                }
                if (q > 0.) {
                    p = -p;
                }
                q = Math.abs(q);
                temp = e;
                e = d;
                if ((failParabolic) || Math.abs(p) >= Math.abs(0.5 * q * temp) || (p <= q * (a.x - x.x) || p >= q * (b.x - x.x))) {
                    if (x.x >= xm) {
                        d = CGOLD * (a.x - x.x);
                    } else {
                        d = CGOLD * (b.x - x.x);
                    }
                } else {
                    d = p / q;
                    u.x = x.x + d;
                    if (u.x - a.x < tol2 || b.x - u.x < tol2) {
                        d = tol1 * SIGN(xm - x.x);
                    }
                }
            } else {
                if (x.x >= xm) {
                    e = a.x - x.x;
                } else {
                    e = b.x - x.x;
                }
                d = CGOLD * e;
            }
            if (Math.abs(d) >= tol1) {
                u.x = x.x + d;
            } else {
                if (debug) {
                    System.out.println("now Math.abs(d)<tol1, x.x+tol1*SING(d) = " + (x.x + tol1 * SIGN(d)));
                }
                u.x = x.x + tol1 * SIGN(d);
            }
            u.x = bracketFinder.checkLimits(u.x);
            bracketFinder.calculate(u);
            if (u.f <= x.f) {
                if (u.x >= x.x) {
                    a.copy(x);
                } else {
                    b.copy(x);
                }
                v.copy(w);
                w.copy(x);
                x.copy(u);
            } else {
                if (u.x < x.x) {
                    a.copy(u);
                } else {
                    b.copy(u);
                }
                if (u.f <= w.f || w.x == x.x) {
                    v.copy(w);
                    w.copy(u);
                } else if (u.f <= v.f || v.x == x.x || v.x == w.x) {
                    v.copy(u);
                }
            }
        }
        if (debug) {
            System.out.println("debug best x=" + x.x + " func=" + x.f);
            System.out.println("x:a,b,x,v,w = " + a.x + " " + b.x + " " + x.x + " " + v.x + " " + w.x);
            System.out.println("f:a,b,x,v,w = " + a.f + " " + b.f + " " + x.f + " " + v.f + " " + w.f);
        }
        setBestSolution(x, b.x - a.x);
        shouldStop = true;
        return shouldStop;
    }

    /**
	 *  Sets the best solution
	 *
	 *@param  p     The new best solution as an instance of Pair class
	 *@param  step  The new bestSolution value
	 */
    private void setBestSolution(Pair p, double step) {
        ParameterProxy variable = getVariables().get(0);
        variable.setValue(p.x);
        variable.setError(Math.abs(step));
        Map<ParameterProxy, Double> bestMap = getBestSolutionMap();
        bestMap.clear();
        bestMap.put(variable, new Double(variable.getValue()));
        bestScore = p.f;
    }

    /**
	 *  The sign function
	 *
	 *@param  a  The input variable
	 *@return    -1, 0, or +1
	 */
    private static double SIGN(double a) {
        if (a > 0.) {
            return 1.0;
        }
        if (a < 0.) {
            return -1.0;
        }
        return 0.;
    }

    /**
	 *  returns the best score after step
	 *
	 *@return    The bestScore value
	 */
    @Override
    protected double getBestScore() {
        return bestScore;
    }

    /**
	 *  returns the type of the algorithm - SearchAlgorithmConstants..
	 *
	 *@return    The type value
	 */
    @Override
    public int getType() {
        return SearchAlgorithmConstants.BRENT_SEARCH_ALGORITHM;
    }

    /**
	 *  The SHIFT method is analog of the C-function in NumRec.
	 *
	 *@param  a  The 1-st param.
	 *@param  b  The 2-nd param.
	 *@param  c  The 3-rd param.
	 *@param  d  The 4-th param.
	 */
    private static void SHIFT(Pair a, Pair b, Pair c, Pair d) {
        a.x = b.x;
        a.f = b.f;
        b.x = c.x;
        b.f = c.f;
        c.x = d.x;
        c.f = d.f;
    }

    /**
	 *  The class for bracketing the minimum of a function of one variable. An
	 *  implementation of NUMERICAL RECIPES IN C, Chapter 10.
	 *
	 *@author    shishlo
	 */
    class BracketFinder {

        private double GOLD = 1.618034;

        private double GLIMIT = 100.0;

        private double TINY = 1.0e-20;

        private ParameterProxy variable = null;

        private BrentOneDimSearchAlgorithm sA = null;

        private double[] x_arr = new double[1];

        /**
		 *  Constructor for the BracketFinder object
		 */
        public BracketFinder() {
        }

        /**
		 *  Sets the parameterProxy attribute of the BracketFinder object
		 *
		 *@param  variable  The new parameterProxy value
		 */
        public void setParameterProxy(ParameterProxy variable) {
            this.variable = variable;
        }

        /**
		 *  Sets the searchAlgorithm attribute of the BracketFinder object
		 *
		 *@param  sA  The new search algorithm
		 */
        public void setSearchAlgorithm(BrentOneDimSearchAlgorithm sA) {
            this.sA = sA;
        }

        public Pair[] getBrackets(double x0) {
            if (sA == null) {
                if (debug) {
                    System.out.println("bracketFinder(null) sA = null");
                }
                return null;
            }
            if (variable == null) {
                if (debug) {
                    System.out.println("bracketFinder(null) variable = null");
                }
                return null;
            }
            double xa, xb;
            if (variable.getStep() != 0.) {
                xa = x0 + variable.getStep();
                xa = checkLimits(xa);
                xb = x0;
                xb = checkLimits(xb);
                if (debug) {
                    System.out.println("bracketFinder> x0, xa, xb = " + x0 + " " + xa + " " + xb);
                }
            } else {
                System.out.println("bracketFinder(null), variable.getStep() == 0");
                return null;
            }
            return getBrackets(xa, xb);
        }

        public Pair[] getBrackets() {
            if (sA == null) {
                return null;
            }
            if (variable == null) {
                return null;
            }
            double xa;
            double xb;
            double x = variable.getValue();
            final double xl = variable.getLowerLimit();
            final double xu = variable.getUpperLimit();
            final double dx = variable.getStep();
            if (debug) {
                System.out.println("bracketFinder::getBrackets(), x(init) = " + x);
            }
            final double windowRatio = 0.1;
            if ((xl <= x) && (x <= xu)) {
                if (dx != 0) {
                    xa = x + dx;
                    xa = checkLimits(xa);
                    xb = x;
                } else {
                    if ((xl != (-Double.MAX_VALUE)) && (xu != Double.MAX_VALUE)) {
                        xa = x + (xu - xl) * windowRatio;
                        xa = checkLimits(xa);
                        xb = x;
                    } else {
                        xa = x + 0.1;
                        xa = checkLimits(xa);
                        xb = x;
                    }
                }
            } else {
                if ((xl != (-Double.MAX_VALUE)) && (xu != Double.MAX_VALUE)) {
                    xa = (xl + xu) / 2 + (xu - xl) * windowRatio;
                    xb = (xl + xu) / 2;
                } else {
                    xa = 0.1;
                    xb = 0;
                }
            }
            return getBrackets(xa, xb);
        }

        /**
		 *  Returns the brackets for a minimum value
		 * first search in b direction, the best is xb0 is possible loc minimum, xa0 has a larger value 
		 *@return    The brackets value
		 */
        public Pair[] getBrackets(double xa0, double xb0) {
            if (sA == null) {
                System.out.println("bracketFinder(null) sA = null");
                return null;
            }
            if (variable == null) {
                System.out.println("bracketFinder(null) variable = null");
                return null;
            }
            double xa = xa0;
            double xb = xb0;
            Pair a = new Pair();
            a.x = xa;
            Pair b = new Pair();
            b.x = xb;
            Pair dum = new Pair();
            Pair u = new Pair();
            a.x = checkLimits(a.x);
            if (!calculate(a)) {
                return null;
            }
            b.x = checkLimits(b.x);
            if (!calculate(b)) {
                return null;
            }
            if (b.f > a.f) {
                BrentOneDimSearchAlgorithm.SHIFT(dum, a, b, dum);
            }
            Pair c = new Pair();
            c.x = b.x + GOLD * (b.x - a.x);
            if (debug) {
                System.out.println("bracketFinder(Gold)(before checkLimits), c.x = " + c.x);
            }
            c.x = checkLimits(c.x);
            if (debug) {
                System.out.println("bracketFinder(Gold)(after checkLimits), c.x = " + c.x);
            }
            if (!calculate(c)) {
                return null;
            }
            if ((c.x == b.x) && ((c.x == variable.getLowerLimit()) || (c.x == variable.getUpperLimit()))) {
                Pair bold = new Pair();
                bold.x = b.x;
                bold.f = b.f;
                c.x = bold.x;
                c.f = bold.f;
                b.x = (a.x + bold.x) / 2;
                if (!calculate(b)) {
                    System.err.println("bracketFinder(null), calculte(b)=false(c.x hits boundary), b.x = " + b.x);
                    return null;
                }
                if (a.f <= b.f) {
                    if (debug) {
                        System.out.println("bracketFinder detected limits on c.x");
                    }
                    c.x = a.x + GOLD * (a.x - b.x);
                    if (debug) {
                        System.out.println("bracketFinder(Gold)(before checkLimits), c.x = " + c.x);
                    }
                    c.x = checkLimits(c.x);
                    if (!calculate(c)) {
                        System.err.println("bracketFinder(null), calculte(c)=false(c.x hits boundary, new c is in a direction), c.x = " + c.x);
                        return null;
                    }
                    BrentOneDimSearchAlgorithm.SHIFT(dum, a, b, dum);
                    if (b.f > a.f) {
                        BrentOneDimSearchAlgorithm.SHIFT(dum, a, c, dum);
                    }
                }
            }
            double r;
            double q;
            double ulim;
            Pair[] res = new Pair[3];
            res[0] = a;
            res[1] = b;
            res[2] = c;
            if (debug) {
                System.out.println("bracketFinder::getBrackets x: a, b, c = " + a.x + " " + b.x + " " + c.x);
                System.out.println("bracketFinder::getBrackets (a>b) f: a, b, c = " + a.f + " " + b.f + " " + c.f);
            }
            while (b.f > c.f) {
                r = (b.x - a.x) * (b.f - c.f);
                q = (b.x - c.x) * (b.f - a.f);
                u.x = b.x - ((b.x - c.x) * q - (b.x - a.x) * r) / (2.0 * SIGN(Math.max(Math.abs(q - r), TINY), q - r));
                ulim = b.x + GLIMIT * (c.x - b.x);
                if ((b.x - u.x) * (u.x - c.x) > 0.) {
                    if (debug) {
                        System.out.println("bracketFinder> now (b-u)*(u-c)>0");
                    }
                    u.x = checkLimits(u.x);
                    if (!calculate(u)) {
                        return null;
                    }
                    if (u.f < c.f) {
                        a.x = b.x;
                        a.f = b.f;
                        b.x = u.x;
                        b.f = u.f;
                        return res;
                    } else if (u.f > b.f) {
                        c.x = u.x;
                        c.f = u.f;
                        return res;
                    }
                    u.x = c.x + GOLD * (c.x - b.x);
                    u.x = checkLimits(u.x);
                    if (!calculate(u)) {
                        return null;
                    }
                } else if ((c.x - u.x) * (u.x - ulim) > 0.) {
                    if (debug) {
                        System.out.println("bracketFinder, (c-u)*(u-ulim)>0");
                    }
                    u.x = checkLimits(u.x);
                    if (!calculate(u)) {
                        return null;
                    }
                    if (u.f < c.f) {
                        if (debug) {
                            System.out.println("bracketFinder> now u.f<c.f");
                        }
                        dum.x = c.x + GOLD * (c.x - b.x);
                        dum.x = checkLimits(dum.x);
                        if (!calculate(dum)) {
                            System.out.println("bracketFinder(null), calculate(dum)=null, dum.x = " + dum.x);
                            return null;
                        }
                        if (debug) {
                            System.out.println("bracketFinder (before shift) a,b,c,u,dum (x) = " + a.x + " " + b.x + " " + c.x + " " + u.x + " " + dum.x);
                            System.out.println("bracketFinder (before shift) a,b,c,u,dum (f) = " + a.f + " " + b.f + " " + c.f + " " + u.f + " " + dum.x);
                        }
                        if (((u.x < dum.x) && (b.x < c.x)) || ((u.x > dum.x) && (b.x > c.x))) {
                            if (debug) {
                                System.out.println("bracketFinder, order is b, c, u, dum");
                            }
                            BrentOneDimSearchAlgorithm.SHIFT(b, c, u, dum);
                        } else {
                            if (debug) {
                                System.out.println("bracketFinder, order is b, c, dum, u");
                            }
                            BrentOneDimSearchAlgorithm.SHIFT(b, c, dum, u);
                        }
                        if (debug) {
                            System.out.println("bracketFinder (new) a,b,c,u = " + a.x + " " + b.x + " " + c.x + " " + u.x);
                        }
                    }
                } else if ((u.x - ulim) * (ulim - c.x) >= 0.) {
                    u.x = ulim;
                    if (!calculate(u)) {
                        System.out.println("bracketFinder(null), calculate(u)(4)=null, u.x = " + u.x);
                        return null;
                    }
                    if (debug) {
                        System.out.println("bracketFinder (new) a,b,c,u = " + a.x + " " + b.x + " " + c.x + " " + u.x);
                    }
                } else {
                    if (debug) {
                        System.out.println("bracketFinder, else, use GOLD");
                    }
                    u.x = c.x + GOLD * (c.x - b.x);
                    u.x = checkLimits(u.x);
                    if (!calculate(u)) {
                        return null;
                    }
                }
                BrentOneDimSearchAlgorithm.SHIFT(a, b, c, u);
            }
            return res;
        }

        protected double checkLimits(double x) {
            double lowL = variable.getLowerLimit();
            double uppL = variable.getUpperLimit();
            if (x <= lowL) {
                x = lowL;
                return x;
            }
            if (x >= uppL) {
                x = uppL;
                return x;
            }
            return x;
        }

        /**
		 *  Calculates function value for independent variable in the Pair object.
		 *
		 *@param  pr  The Pair class instance
		 *@return     True or false in the case of success or fail
		 */
        protected boolean calculate(Pair pr) {
            if (sA.getWantToStop()) {
                return false;
            }
            x_arr[0] = pr.x;
            if (!sA.accept(x_arr)) {
                return false;
            }
            variable.setValue(pr.x);
            pr.f = sA.getScorer().score();
            sA.scoreCalculated();
            if (sA.getWantToStop()) {
                return false;
            }
            SearchAlgorithm extSA = sA.getExternalSearchAlgorithm();
            if (extSA != null) {
                extSA.scoreCalculated();
                if (extSA.getWantToStop()) {
                    sA.setWantToStop(true);
                    return false;
                }
            }
            return true;
        }

        /**
		 *  Description of the Method
		 *
		 *@param  a  Description of the Parameter
		 *@param  b  Description of the Parameter
		 *@return    Description of the Return Value
		 */
        private double SIGN(double a, double b) {
            if (b >= 0.) {
                return Math.abs(a);
            }
            return -Math.abs(a);
        }
    }

    /**
	 *  This class keeps two parameters.
	 *
	 *@author    shishlo
	 */
    class Pair {

        /**
		 *  Independent variable
		 */
        public double x = 0.;

        /**
		 *  The function value
		 */
        public double f = 0.;

        /**
		 *  Constructor for the Pair object
		 */
        public Pair() {
        }

        /**
		 *  Description of the Method
		 *
		 *@param  p  Description of the Parameter
		 */
        public void copy(Pair p) {
            x = p.x;
            f = p.f;
        }
    }
}
