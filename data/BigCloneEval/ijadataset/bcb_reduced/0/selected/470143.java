package org.expasy.jpl.commons.base.cond;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.expasy.jpl.commons.base.builder.Interpreter;
import org.expasy.jpl.commons.base.cond.ConditionImpl.ConditionRuntimeException;
import org.expasy.jpl.commons.base.cond.operator.OperatorManager;
import org.expasy.jpl.commons.base.cond.operator.OperatorManager.InvalidOperatorException;

/**
 * {@code ConditionInterpreter} translates condition type expressions into
 * {@code Condition}. It helps building complex {@code ConditionImpl}s.
 * 
 * <p>
 * Expressions are represented as an expression tree. [conditional] Expression
 * trees represent code in a tree-like data structure, where each node is a
 * expression over condition.
 * </p>
 * 
 * <p>
 * A complex conditional expression is an expression of simple condition {@code
 * operands} (defined internally as variable (CVAR)) and classic unary (OR) and
 * binary {@code operators} (AND, OR). Here is the grammar below:
 * </p>
 * 
 * <p>
 * EXPR := COND | NOT? EXPR<br/>
 * COND := CVAR BOP COND<br/>
 * CVAR := \w+<br/>
 * BOP := AND | OR<br/>
 * NOT := '!' <br/>
 * AND := '&'<br/>
 * OR := '|'<br/>
 * </p>
 * 
 * How to create a complex condition ?
 * <ol>
 * <li>register simple conditions in engine as variables (CVAR).</li>
 * <li>compile engine with a given string expression of defined variables (CVAR)
 * and logical operators (NOT, AND and OR).</li>
 * <li>get and test the condition as a simple condition {@code Condition}.</li>
 * </ol>
 * 
 * Example:
 * 
 * <pre>
 * ConditionInterpreter&lt;Double&gt; interpreter =
 * 	ConditionInterpreter.newInstance();
 * 
 * engine.register("c1", new ConditionImpl.Builder&lt;Double, Double&gt;(0.)
 *   .operator(OperatorGreaterThan.newInstance()).build());<br/>
 * 
 * engine.register("c2", new ConditionImpl.Builder&lt;Double, Double&gt;(10.)
 *   .operator(OperatorLowerThan.newInstance()).build());<br/>
 * 
 * Condition&lt;Double&gt; condition = interpreter.translate("c1 & c2");
 * 
 * Assert.assertTrue(condition.evaluate(4.));
 * Assert.assertFalse(condition.evaluate(14.));
 * </pre>
 * 
 * @see ConditionImpl
 * 
 * @author nikitin
 * 
 * @version 1.0
 * 
 */
public final class ConditionInterpreter<T> implements Interpreter<Condition<T>> {

    private static final OperatorManager OPERATOR_MANAGER = OperatorManager.getInstance();

    /** the table with all condition variables */
    private final Map<String, Condition<T>> conditions;

    /** counter for condition naming */
    private int counter;

    private String currentConditionName;

    /**
	 * <expression> ::= [ "not" ] <id> | "(" <expression> <operator>
	 * <expression> ")" <id> ::= <String> <operator> ::= "or" | "and"
	 */
    private class Parser {

        /** the expression to parse */
        String expression;

        /** current position on the expression */
        int cursor = 0;

        Parser(final String expression) {
            this.expression = expression;
        }

        /**
		 * Create a new expression tree
		 * 
		 * @return an expression
		 * 
		 * @throws ParseException if invalid expression
		 */
        Condition<T> parse() throws ParseException {
            final Condition<T> tree = newAbstractSyntaxTree();
            return tree;
        }

        /**
		 * Move cursor forward while blank character
		 */
        private void skipBlanks() {
            while ((cursor < expression.length()) && ((expression.charAt(cursor) == ' ') || (expression.charAt(cursor) == '\t'))) {
                cursor++;
            }
        }

        /**
		 * @return the next identifier from current cursor position
		 */
        private String getNextIdentifier() {
            final Pattern pat = Pattern.compile("(\\w+).*");
            final Matcher match = pat.matcher(expression.substring(cursor));
            if (match.find()) {
                cursor += match.end(1);
                return match.group(1);
            }
            return null;
        }

        private Condition<T> newAbstractSyntaxTree() throws ParseException {
            if (expression == null || expression.length() == 0) {
                throw new ParseException("Cannot parse empty expression.", -1);
            }
            skipBlanks();
            boolean not;
            not = false;
            if (expression.charAt(cursor) == '!') {
                cursor++;
                not = true;
            }
            Condition<T> exp = expressionValue();
            if (not) {
                exp = new ConditionalUnaryNotOperator<T>(exp);
            }
            skipBlanks();
            while ((cursor < expression.length()) && ((expression.charAt(cursor) == '&') || (expression.charAt(cursor) == '|'))) {
                final char op = expression.charAt(cursor);
                cursor++;
                final Condition<T> nextTerm = newAbstractSyntaxTree();
                exp = new ConditionalBinOperator<T>(op, exp, nextTerm);
                skipBlanks();
            }
            return exp;
        }

        private Condition<T> expressionValue() throws ParseException {
            skipBlanks();
            final char ch = expression.charAt(cursor);
            if (Character.isJavaIdentifierPart(ch)) {
                final String id = getNextIdentifier();
                if (conditions.containsKey(id)) {
                    return conditions.get(id);
                } else {
                    throw new ConditionRuntimeException(id + " was not found in table of symbols");
                }
            } else if (ch == '(') {
                cursor++;
                final Condition<T> exp = newAbstractSyntaxTree();
                skipBlanks();
                if (expression.charAt(cursor) != ')') {
                    throw new ParseException("Missing right parenthesis.", cursor);
                }
                cursor++;
                return exp;
            } else if (ch == '\n') {
                throw new ParseException("End-of-line encountered in the middle of an expression.", cursor);
            } else if (ch == ')') {
                throw new ParseException("Extra right parenthesis.", cursor);
            } else if ((ch == '+') || (ch == '-') || (ch == '*') || (ch == '/')) {
                throw new ParseException("Misplaced operator.", cursor);
            } else {
                throw new ParseException("Unexpected character \"" + ch + "\" encountered.", cursor);
            }
        }
    }

    /**
	 * Default constructor
	 */
    private ConditionInterpreter() {
        conditions = new HashMap<String, Condition<T>>();
    }

    public static <T> ConditionInterpreter<T> newInstance() {
        return new ConditionInterpreter<T>();
    }

    /**
	 * Add a named condition in the condition table.
	 * 
	 * @param conditionName the condition name.
	 * @param condition the condition instance.
	 */
    public ConditionInterpreter<T> register(final String conditionName, final Condition<T> condition) {
        conditions.put(conditionName, condition);
        return this;
    }

    /**
	 * Add anonymous condition instance in the condition table.
	 * 
	 * <p>
	 * Do not forget to get
	 * </p>
	 * 
	 * @param condition the condition instance.
	 */
    public ConditionInterpreter<T> register(final Condition<T> condition) {
        setNextConditionName(nextConditionName());
        conditions.put(currentConditionName, condition);
        return this;
    }

    /**
	 * @throws InvalidOperatorException
	 * 
	 */
    @SuppressWarnings("unchecked")
    public ConditionInterpreter<T> register(final String conditionName, final String condition) throws InvalidOperatorException {
        String[] lvalOpRval = OPERATOR_MANAGER.getRvalueOpLvalue(condition);
        conditions.put(conditionName, (Condition<T>) ConditionImpl.valueOf(lvalOpRval[0], lvalOpRval[1], lvalOpRval[2]));
        return this;
    }

    /**
	 * Add anonymous condition.
	 * 
	 * @param condition the string format condition.
	 * @return the engine.
	 * @throws InvalidOperatorException
	 */
    public ConditionInterpreter<T> register(final String condition) throws InvalidOperatorException {
        setNextConditionName(nextConditionName());
        register(currentConditionName, condition);
        return this;
    }

    /**
	 * @return the next generated condition name.
	 */
    public String getNextConditionName() {
        return currentConditionName;
    }

    /**
	 * Set the next condition name.
	 */
    private void setNextConditionName(String name) {
        currentConditionName = "_cond" + (counter++);
    }

    /**
	 * Generate and ..
	 * 
	 * @return the next condition name.
	 */
    private String nextConditionName() {
        return "_cond" + (counter++);
    }

    /**
	 * Remove the condition.
	 * 
	 * @param name the condition name.
	 * @return the removed condition.
	 */
    public Condition<T> removeCondition(final String name) {
        if (conditions.containsKey(name)) {
            return conditions.remove(name);
        }
        return null;
    }

    /**
	 * Remove all conditions.
	 */
    public void removeAllConditions() {
        conditions.clear();
    }

    /**
	 * Get the condition given its name.
	 * 
	 * @param name the condition name.
	 * @return the fetched condition.
	 */
    public Condition<T> getCondition(final String name) {
        if (conditions.containsKey(name)) {
            return conditions.get(name);
        }
        return null;
    }

    /**
	 * @return the number of stored condition.
	 */
    public int getNumOfCondition() {
        return conditions.size();
    }

    /**
	 * Display the internal content of defined conditions variables.
	 */
    public void traceInternalConditions() {
        for (final String id : conditions.keySet()) {
            System.out.println(id + ": " + conditions.get(id));
        }
    }

    /**
	 * Parse expression and return a tree conditional expression.
	 * 
	 * @param expression the expression to evaluate.
	 * @return a tree expression of conditions.
	 * 
	 * @throws ParseException if invalid expression.
	 */
    public Condition<T> translate(String expression) throws ParseException {
        try {
            expression = transform(expression);
        } catch (InvalidOperatorException e) {
            throw new ParseException(e.getMessage(), -1);
        }
        Parser parser = new Parser(expression);
        return parser.parse();
    }

    private String transform(String expression) throws InvalidOperatorException {
        if (expression == null || expression.length() == 0) {
            throw new IllegalArgumentException("cannot transform undefined expression!");
        }
        StringBuilder sb = new StringBuilder();
        Pattern pat = Pattern.compile("([&|)(!])");
        Matcher matcher = pat.matcher(expression);
        int nextStart = 0;
        while (matcher.find()) {
            String oper = matcher.group();
            int beg = matcher.start();
            int end = matcher.end();
            String match = expression.substring(nextStart, beg).trim();
            if (match.length() > 0) {
                if (OPERATOR_MANAGER.isConditionContainsOperator(match)) {
                    register(match);
                    sb.append(currentConditionName);
                } else {
                    sb.append(match);
                }
            }
            sb.append(oper);
            nextStart = end;
        }
        if (nextStart > 0) {
            String match = expression.substring(nextStart).trim();
            if (match.length() > 0) {
                if (OPERATOR_MANAGER.isConditionContainsOperator(match)) {
                    register(match);
                    sb.append(currentConditionName);
                } else {
                    sb.append(match);
                }
            }
            return sb.toString();
        } else if (OPERATOR_MANAGER.isConditionContainsOperator(expression)) {
            register(expression);
            return currentConditionName;
        }
        return expression;
    }
}
