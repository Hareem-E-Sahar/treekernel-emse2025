package net.sourceforge.webcompmath.functions;

import net.sourceforge.webcompmath.data.Cases;
import net.sourceforge.webcompmath.data.CloneableExpression;
import net.sourceforge.webcompmath.data.CloneableExpressionCommand;
import net.sourceforge.webcompmath.data.ClonedMathObject;
import net.sourceforge.webcompmath.data.DataUtils;
import net.sourceforge.webcompmath.data.Expression;
import net.sourceforge.webcompmath.data.ExpressionCommand;
import net.sourceforge.webcompmath.data.ExpressionProgram;
import net.sourceforge.webcompmath.data.MathObject;
import net.sourceforge.webcompmath.data.ParseError;
import net.sourceforge.webcompmath.data.Parser;
import net.sourceforge.webcompmath.data.ParserContext;
import net.sourceforge.webcompmath.data.ParserExtension;
import net.sourceforge.webcompmath.data.StackOfDouble;
import net.sourceforge.webcompmath.data.Variable;

/**
 * The NumMinMaxParser class makes it possible to use numerical minimums and
 * maximums, such as numMin(xx,0,5,xx^2) in a Parser. The numerical min or max
 * psedu-function has four parameters: (1) The variable, which must be an
 * identifier and should be different than other variables registered with the
 * parser; (2) The lower limit for the min/max, given as an expression (which
 * can involve variables); (3) The upper limit for the min/max, given as an
 * expression (which can involve variables); and (4) the expression over which
 * to find the min or max. The expression in the fourth parameter can (and
 * presumably will) use the variable (as well as other identifiers known to the
 * parser).
 * <p>
 * The created psuedo function divides the interval into 1000 subintervals. It
 * then checks the value of the derivative of the expression at the endpoints of
 * each subinterval to see if a local minimum/maximum might be in the interval.
 * If so, it hunts recursively for that local min/max, else it uses the min/max
 * of the two subinterval endpoints. The function then returns the min/max of
 * all 1000 subinterval min/max's.
 * <p>
 * To use numerical min with a Parser p, just say p.add(new
 * NumMinMaxParser(NumMinParser.MIN)). It's unlikely that you will ever need to
 * do anything else with NumMinMaxParsers. If you want to use a name other than
 * "numMin" or "numMax", you can change the name after creating the
 * NumMinMaxParser object but before adding it to a parser.
 * 
 */
public class NumMinMaxParser implements ParserExtension {

    private static final long serialVersionUID = -4576124881054239453L;

    private MinMaxType t;

    /**
	 * The type of parser to create
	 */
    public enum MinMaxType {

        /**
		 * create a minimum parser
		 */
        MIN, /**
		 * create a maximum parser
		 */
        MAX
    }

    ;

    /**
	 * Constructor
	 * 
	 * @param t
	 *            the type, either MinMaxType.Min or MinMaxType.Max
	 */
    public NumMinMaxParser(MinMaxType t) {
        this.t = t;
        if (t == MinMaxType.MIN) {
            name = "numMin";
        } else {
            name = "numMax";
        }
    }

    private String name;

    /**
	 * Set the name, which will be used in place of "numMin" or "numMax" in
	 * expressions. This should not be done after the NumMinMaxParser has been
	 * added to a Parser.
	 * 
	 * @param name
	 *            name of numerical min/max
	 */
    public void setName(String name) {
        this.name = name;
    }

    /**
	 * Get the name, which will be used in place of "numMin" or "numMax" in
	 * expressions.
	 * 
	 * @return name of min/max
	 */
    public String getName() {
        return name;
    }

    /**
	 * When the name of this ParserExtension is encountered by a parser with
	 * which the extension is registered, the parser calls this routine to parse
	 * the subexpression. The subexpression has the form ( <variable>,
	 * <lower-limit>, <upper-limit>, <expression>). This method is not meant to
	 * be called directly
	 * 
	 * @param parser
	 *            parser to use
	 * @param context
	 *            context to use
	 */
    public void doParse(Parser parser, ParserContext context) {
        int tok = context.next();
        String open = context.tokenString;
        if (tok == ParserContext.OPCHARS && (open.equals("(") || (open.equals("[") && (context.options & Parser.BRACKETS) != 0) || (open.equals("{") && (context.options & Parser.BRACES) != 0))) {
            String close = open.equals("(") ? ")" : (open.equals("[") ? "]" : "}");
            tok = context.next();
            if (tok != ParserContext.IDENTIFIER) throw new ParseError("Expected the variable as the first argument of " + name + ".", context);
            String varName = context.tokenString;
            tok = context.next();
            if (tok != ParserContext.OPCHARS || !context.tokenString.equals(",")) throw new ParseError("Exprected a comma after the variable, " + varName + ".", context);
            ExpressionProgram saveProg = context.prog;
            context.prog = new ExpressionProgram();
            parser.parseExpression(context);
            tok = context.next();
            if (tok != ParserContext.OPCHARS || !context.tokenString.equals(",")) throw new ParseError("Exprected a comma after the lower limit expression for " + name + ".", context);
            ExpressionProgram lowerLimit = context.prog;
            context.prog = new ExpressionProgram();
            parser.parseExpression(context);
            tok = context.next();
            if (tok != ParserContext.OPCHARS || !context.tokenString.equals(",")) throw new ParseError("Exprected a comma after the upper limit expression for " + name + ".", context);
            ExpressionProgram upperLimit = context.prog;
            Variable v = new Variable(varName);
            context.mark();
            context.add(v);
            context.prog = new ExpressionProgram();
            parser.parseExpression(context);
            tok = context.next();
            if (tok != ParserContext.OPCHARS || !context.tokenString.equals(close)) throw new ParseError("Expected a \"" + close + "\" at the end of the paramter list for " + name + ".", context);
            context.revert();
            saveProg.addCommandObject(new Cmd(v, lowerLimit, upperLimit, context.prog, this, t));
            context.prog = saveProg;
        } else throw new ParseError("Parentheses required around parameters of numerical min/max.", context);
    }

    private static class Cmd implements ExpressionCommand, CloneableExpressionCommand, Cloneable {

        private static final long serialVersionUID = -2056727306553228870L;

        private Variable mmVar;

        private ExpressionProgram mmExpr;

        private Expression mmDeriv;

        private ExpressionProgram lowerLimitExpr;

        private ExpressionProgram upperLimitExpr;

        private MathObject obj;

        private MinMaxType t;

        Cmd(Variable v, ExpressionProgram ll, ExpressionProgram ul, ExpressionProgram e, MathObject o, MinMaxType t) {
            mmVar = v;
            mmExpr = e;
            mmDeriv = mmExpr.derivative(mmVar);
            lowerLimitExpr = ll;
            upperLimitExpr = ul;
            obj = o;
            this.t = t;
        }

        /**
		 * This routine is called when an ExpressionCommand object is
		 * encountered during the evaluation of an ExpressionProgram. The stack
		 * may contain results of previous commands in the program. In this
		 * case, the command is a numerical min/max.
		 * 
		 * @see net.sourceforge.webcompmath.data.ExpressionCommand#apply(net.sourceforge.webcompmath.data.StackOfDouble,
		 *      net.sourceforge.webcompmath.data.Cases)
		 */
        public void apply(StackOfDouble stack, Cases cases) {
            double intervals = 1000;
            double upper = upperLimitExpr.getVal();
            double lower = lowerLimitExpr.getVal();
            double val = (t == MinMaxType.MIN ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY);
            if (Double.isNaN(upper) || Double.isNaN(lower)) {
                stack.push(Double.NaN);
                return;
            }
            if (upper == lower) {
                mmVar.setVal(lower);
                stack.push(mmExpr.getVal());
                return;
            }
            if (lower > upper) {
                double temp = lower;
                lower = upper;
                upper = temp;
            }
            double delta = (upper - lower) / intervals;
            for (double x = lower + delta; x < upper; x += delta) {
                mmVar.setVal(x);
                if (t == MinMaxType.MIN) {
                    val = Math.min(val, searchMin(x, delta, 1));
                } else {
                    val = Math.max(val, searchMax(x, delta, 1));
                }
            }
            stack.push(val);
        }

        private double searchMin(double left, double delta, int depth) {
            double right = left + delta;
            mmVar.setVal(left);
            double leftVal = mmExpr.getVal();
            boolean incrLeft = mmDeriv.getVal() > 0;
            mmVar.setVal(right);
            double rightVal = mmExpr.getVal();
            boolean incrRight = mmDeriv.getVal() > 0;
            if (!incrLeft && incrRight) {
                double mid = (left + right) / 2;
                if (depth >= 13) {
                    mmVar.setVal(mid);
                    return mmExpr.getVal();
                }
                double slope = mmDeriv.getVal();
                if (slope < 0) return searchMin(mid, right, depth + 1); else return searchMin(left, mid, depth + 1);
            }
            return Math.min(leftVal, rightVal);
        }

        private double searchMax(double left, double delta, int depth) {
            double right = left + delta;
            mmVar.setVal(left);
            double leftVal = mmExpr.getVal();
            boolean incrLeft = mmDeriv.getVal() > 0;
            mmVar.setVal(right);
            double rightVal = mmExpr.getVal();
            boolean incrRight = mmDeriv.getVal() > 0;
            if (incrLeft && !incrRight) {
                double mid = (left + right) / 2;
                if (depth >= 13) {
                    mmVar.setVal(mid);
                    return mmExpr.getVal();
                }
                double slope = mmDeriv.getVal();
                if (slope > 0) return searchMax(mid, right, depth + 1); else return searchMax(left, mid, depth + 1);
            }
            return Math.max(leftVal, rightVal);
        }

        /**
		 * The ExpressionCommand occurs in the program prog at the index
		 * indicated by myIndex. Add commands to deriv that will evaluate the
		 * derivative of this command with respect to the variable wrt. This
		 * just assumes that the limits don't depend on the wrt variable, so it
		 * always returns 0 as the derivative.
		 * 
		 * @see net.sourceforge.webcompmath.data.ExpressionCommand#compileDerivative(net.sourceforge.webcompmath.data.ExpressionProgram,
		 *      int, net.sourceforge.webcompmath.data.ExpressionProgram,
		 *      net.sourceforge.webcompmath.data.Variable)
		 */
        public void compileDerivative(ExpressionProgram prog, int myIndex, ExpressionProgram deriv, Variable wrt) {
            deriv.addConstant(0);
        }

        /**
		 * The ExpressionCommand occurs in the program prog at the index
		 * indicated by myIndex. Return the total number of indices in prog
		 * occupied by this command and the commands that generate data used by
		 * this command. In this case, that means just this Cmd object.
		 * 
		 * @see net.sourceforge.webcompmath.data.ExpressionCommand#extent(net.sourceforge.webcompmath.data.ExpressionProgram,
		 *      int)
		 */
        public int extent(ExpressionProgram prog, int myIndex) {
            return 1;
        }

        /**
		 * Return true if this command depends on the value of x, false
		 * otherwise. That is, when apply() is called, can the result depend on
		 * the value of x?
		 * 
		 * @see net.sourceforge.webcompmath.data.ExpressionCommand#dependsOn(net.sourceforge.webcompmath.data.Variable)
		 */
        public boolean dependsOn(Variable x) {
            return mmExpr.dependsOn(x) || lowerLimitExpr.dependsOn(x) || upperLimitExpr.dependsOn(x);
        }

        /**
		 * The ExpressionCommand occurs in the program prog at the index
		 * indicated by myIndex. Add a print string representation of the
		 * sub-expression represented by this command.
		 * 
		 * @see net.sourceforge.webcompmath.data.ExpressionCommand#appendOutputString(net.sourceforge.webcompmath.data.ExpressionProgram,
		 *      int, java.lang.StringBuffer)
		 */
        public void appendOutputString(ExpressionProgram prog, int myIndex, StringBuffer buffer) {
            buffer.append(obj.getName() + "(");
            buffer.append(mmVar.getName());
            buffer.append(", ");
            buffer.append(lowerLimitExpr.toString());
            buffer.append(", ");
            buffer.append(upperLimitExpr.toString());
            buffer.append(", ");
            buffer.append(mmExpr.toString());
            buffer.append(")");
        }

        /**
		 * @see net.sourceforge.webcompmath.data.CloneableExpressionCommand#cloneExpressionCommand(net.sourceforge.webcompmath.data.Variable[])
		 */
        public ExpressionCommand cloneExpressionCommand(Variable[] vars) {
            Cmd cc = null;
            try {
                cc = (Cmd) super.clone();
                cc.mmVar = (Variable) mmVar.cloneExpression(vars);
                Variable[] mmArray = new Variable[1];
                mmArray[0] = cc.mmVar;
                Variable[] varArray = DataUtils.mergeVariables(mmArray, vars);
                if (mmExpr instanceof CloneableExpression) {
                    cc.mmExpr = (ExpressionProgram) ((CloneableExpression) mmExpr).cloneExpression(varArray);
                }
                if (cc.mmExpr == null) {
                    cc.mmExpr = mmExpr;
                }
                cc.mmDeriv = cc.mmExpr.derivative(cc.mmVar);
                if (lowerLimitExpr instanceof CloneableExpression) {
                    cc.lowerLimitExpr = (ExpressionProgram) ((CloneableExpression) lowerLimitExpr).cloneExpression(vars);
                }
                if (cc.lowerLimitExpr == null) {
                    cc.lowerLimitExpr = lowerLimitExpr;
                }
                if (upperLimitExpr instanceof CloneableExpression) {
                    cc.upperLimitExpr = (ExpressionProgram) ((CloneableExpression) upperLimitExpr).cloneExpression(vars);
                }
                if (cc.upperLimitExpr == null) {
                    cc.upperLimitExpr = upperLimitExpr;
                }
                cc.obj = new ClonedMathObject(obj.getName());
            } catch (CloneNotSupportedException e) {
                return null;
            }
            return cc;
        }
    }
}
