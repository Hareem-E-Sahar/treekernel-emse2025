package calc;

import gui.NewCalc;
import java.util.LinkedList;
import util.OpenCalcUtil;

/**
  * A <code>calc</code> object represents the entire system for reading in 
  * an equation and then evaluating it. Once an equation has been parsed
  * it can be solved, integrated or differentiated at a point.
  * @author jason
  * 
  */
public class Calc {

    public LinkedList<Element> CURRLIST;

    public LinkedList<Element> ORIGIONALLIST;

    public Evaluator CURREVAL;

    public VarStorage VARLIST;

    public ConstantStorage CONSTLIST;

    public Operators OPSLIST;

    public NewCalc calcObj;

    public ParseElements PARSER;

    private int angleUnits;

    private boolean hadParsingError;

    /**
	 * The standard constructor.
	 * 
	 * @param currCalcObj - The user interface associated with this calculator
	 */
    public Calc(NewCalc currCalcObj) {
        calcObj = currCalcObj;
        ORIGIONALLIST = new LinkedList<Element>();
        CONSTLIST = new ConstantStorage(this);
        CURRLIST = new LinkedList<Element>();
        CURREVAL = new Evaluator(this);
        VARLIST = new VarStorage(this);
        OPSLIST = new Operators(this);
        PARSER = new ParseElements(this);
        setAngleUnits(1);
        hadParsingError = false;
    }

    public Calc() {
        ORIGIONALLIST = new LinkedList<Element>();
        CONSTLIST = new ConstantStorage(this);
        CURRLIST = new LinkedList<Element>();
        CURREVAL = new Evaluator(this);
        VARLIST = new VarStorage(this);
        OPSLIST = new Operators(this);
        PARSER = new ParseElements(this);
        setAngleUnits(1);
        hadParsingError = false;
    }

    /**
	 * Iterates through the LinkedList, adding each element's <code>toString()
	 * </code> result to a String representation of the entire expression.
	 * 
	 * @param list a linked list containing expression's elements
	 * @return String representation of the current expression
	 */
    public String printList(LinkedList<Element> list) {
        String result = new String();
        int i = 0;
        Object currObj = new Object();
        do {
            currObj = list.get(i);
            i++;
            if (currObj instanceof Operator) currObj = (Operator) currObj; else if (currObj instanceof Num) currObj = (Num) currObj; else if (currObj instanceof Var) currObj = (Var) currObj;
            result += currObj.toString();
        } while (currObj != list.getLast());
        return result;
    }

    public VarStorage getVarList() {
        return VARLIST;
    }

    /**
	 * Takes a string and parses it into {@link Element}s, which are stored in a
	 * <code>LinkedList</code>.
	 * 
	 * @param s
	 *            The string to be parsed into an expression
	 * @throws Exception 
	 */
    public void parse(String s) {
        ORIGIONALLIST.clear();
        try {
            PARSER.ParseExpression(s);
            hadParsingError = false;
        } catch (Exception e) {
            hadParsingError = true;
        }
    }

    public void parse_mod(String s) throws Exception {
        ORIGIONALLIST.clear();
        try {
            OpenCalcUtil util = new OpenCalcUtil();
            util.validateParenthesis(s);
            util.beginCheck(s);
            util.endCheck(s);
            PARSER.ParseExpression(s);
            hadParsingError = false;
        } catch (Exception e) {
            hadParsingError = true;
            throw e;
        }
    }

    /**
	 * Takes the currently stored list and evaluates the expression. 
	 * The List is copied initially, because elements are removed from the list 
	 * as individual operations are performed. This destroys a list, 
	 * necessitating the storage of the list to prevent the need to parse one 
	 * equation repeatedly.
	 * 
	 * {@link Calc#parse(String s)} must be called first!!
	 * 
	 * @return result the answer of the expression
	 */
    @SuppressWarnings("unchecked")
    public double solve() {
        if (hadParsingError == true) return Double.MAX_VALUE;
        CURRLIST = (LinkedList<Element>) ORIGIONALLIST.clone();
        double result = 0;
        try {
            result = CURREVAL.eval(CURRLIST);
        } catch (Exception e) {
            result = Double.MAX_VALUE;
        }
        return result;
    }

    /**
	 * Takes the function currently parsed and calculates the derivative
	 * at a point.
	 * 
	 * @param x - that value at which the derivation is approximated
	 * @return the approx. derivative
	 */
    public double deriveAtPoint(double x) {
        double firstVal = 0;
        VARLIST.setVarVal("x", x);
        firstVal = solve();
        VARLIST.setVarVal("x", x + .00001);
        return ((solve() - firstVal) / .00001);
    }

    /**
	 * Takes the function currently parsed and finds the integral from
	 * a to b, using a trapezoid approximation.
	 * 
	 * @param a - start of integral
	 * @param b - end of integral
	 * @return - approximate integral
	 */
    public double integrate(double a, double b) {
        double lastY, currY, aveY, result = 0;
        int numTraps = 1000;
        VARLIST.setVarVal("x", a);
        lastY = solve();
        double xStep = (b - a) / numTraps;
        int trapCount = 0;
        for (int i = 0; i < numTraps; i++) {
            VARLIST.updateVarVal("x", xStep);
            currY = solve();
            aveY = (lastY + currY) / 2;
            result += aveY * xStep;
            trapCount++;
            lastY = currY;
        }
        return result;
    }

    /**
	 * Gets the GUI object associated with this <code>calc</code>
	 * object.
	 * 
	 * @return current NewCalc object
	 */
    public NewCalc getGUI() {
        return calcObj;
    }

    /**
	 * Sets the units for all entered and returned angle values.
	 * 1-Radians   2-Degrees     3-Gradians
	 * @param angleUnits
	 */
    public void setAngleUnits(int angleUnits) {
        this.angleUnits = angleUnits;
    }

    /**
	 * Returns the current angleUnit
	 * 1-Radians   2-Degrees     3-Gradians
	 * 
	 * @return angleUnits - int for angleUnit
	 */
    public int getAngleUnits() {
        return angleUnits;
    }

    /**
	 * Gets the current list of stored constants.
	 * 
	 * @return the ConstantStorage object associated with this calc
	 */
    public ConstantStorage getConstantList() {
        return CONSTLIST;
    }

    /**
	 * Gets the list of operators.
	 *
	 * @return the operator list associated with this calc
	 */
    public Operators getOpsList() {
        return OPSLIST;
    }
}
