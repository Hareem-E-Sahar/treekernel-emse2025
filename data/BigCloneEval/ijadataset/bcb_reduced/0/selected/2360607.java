package org.expasy.jpl.core.mol.polymer.pept;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.expasy.jpl.commons.base.builder.BuilderException;
import org.expasy.jpl.commons.base.builder.InstanceBuilder;
import org.expasy.jpl.commons.base.cond.Condition;
import org.expasy.jpl.commons.base.cond.ConditionImpl;
import org.expasy.jpl.commons.base.cond.ConditionInterpreter;
import org.expasy.jpl.commons.base.cond.operator.api.Operator;
import org.expasy.jpl.commons.base.cond.operator.impl.OperatorBelongs;
import org.expasy.jpl.commons.base.cond.operator.impl.OperatorEquals;

/**
 * This object is a wrapper over {@code Condition} on a {@code PeptideType}.
 * 
 * It interpretes string expression over peptide type conditions.
 * 
 * Each peptide type condition is defined as specific internal names (a, b, c,
 * x, y, z, i or p). Here is the grammar below:
 * 
 * <pre>
 * EXPR := COND | UOP EXPR | COND BOP EXPR
 * COND := MPT_COND{1,2} | SPT_COND
 * UOP := &quot;!&quot;
 * BOP := &quot;&amp;&quot; | &quot;|&quot;
 * MPT_COND := &quot;a&quot; | &quot;b&quot; | &quot;c&quot; | &quot;x&quot; | &quot;y&quot; | &quot;z&quot;
 * SPT_COND := &quot;p&quot; | &quot;i&quot;
 * </pre>
 * 
 * <h3>Term definitions:</h3>
 * <dl>
 * <df>BOP</df>
 * <dt>Binary Operator</dt>
 * <df>UOP</df>
 * <dt>Unary Operator</dt>
 * <df>MPT_COND</df>
 * <dt>Multiple Peptide Type Condition</dt>
 * <df>SPT_COND</df>
 * <dt>Single Peptide Type Condition</dt>
 * </dl>
 * 
 * <h3>Example 1</h3>
 * 
 * <pre>
 * Condition&lt;PeptideType&gt; condition =
 *     new PeptideTypeCondition.Builder&lt;PeptideType&gt;(&quot;b &amp; !y&quot;).build();
 * 
 * PeptideType pepType =
 *     PeptideType.getInstance(TerminusType.TypeN.FRAG_Y,
 *         TerminusType.TypeC.FRAG_B);
 * 
 * Assert.assertTrue(condition.isFalse(pepType));
 * </pre>
 * 
 * <h3>Warning:</h3>
 * <p>
 * Any object can be tested in the condition whenever an instance of {@code
 * PeptideType} is reachable from that object through a {@code JPLICaster} given
 * a stub method of the condition builder:
 * </p>
 * 
 * <h3>Example 2</h3>
 * 
 * <pre>
 * // the peptide to test type on  
 * Ppeptide peptide = new Peptide.Builder(&quot;ARHBRH&quot;)
 *   .nterm(NTerminus.FRAG_X).cterm(CTerminus.FRAG_B).build()
 *   
 * // lead to peptide's type
 * Transformer&lt;Peptide, PeptideType&gt; toPepType =
 * 	    new Transformer&lt;Peptide, PeptideType&gt;() {
 * 		    
 * 		    public PeptideType process(Peptide object) {
 * 			    return object.getPeptideType();
 * 		    }
 * 	    };
 * 
 * // create the condition
 * Condition&lt;Peptide&gt; cond = 
 * 	new PeptideTypeCondition.Builder&lt;Peptide&gt;(&quot;b|y&quot;)
 * 	        .path(toPepType).build();
 * 
 *  // testing conditions on peptides
 *  Assert.assertTrue(cond.isTrue(peptide));
 * 
 * </pre>
 * 
 * @see ConditionInterpreter
 * 
 * @author nikitin
 * 
 */
public class PeptideTypeCondition<T> {

    private static Log logger = LogFactory.getLog(PeptideTypeCondition.class);

    @SuppressWarnings("unchecked")
    private static final Operator BELONGS = OperatorBelongs.newInstance();

    @SuppressWarnings("unchecked")
    private static final Operator EQUALS = OperatorEquals.newInstance();

    /** the main engine that create conditional expressions */
    private final ConditionInterpreter<T> engine;

    /**
	 * At testing time, this hook will lead to the peptide type from T-object to
	 * test.
	 */
    private final Transformer<T, PeptideType> pepTypeCaster;

    /** the expression to interpret */
    private String expression;

    /** constructor based on builder */
    public PeptideTypeCondition(final Builder<T> builder) {
        engine = ConditionInterpreter.newInstance();
        expression = builder.expression;
        pepTypeCaster = builder.caster;
    }

    /**
	 * A builder to ease the construction of complex conditions
	 * 
	 * @author nikitin
	 */
    public static class Builder<T> implements InstanceBuilder<Condition<T>> {

        private final String expression;

        private Transformer<T, PeptideType> caster;

        public Builder(final String expression) {
            this.expression = expression;
        }

        /** peptide type accessor */
        public Builder<T> accessor(final Transformer<T, PeptideType> caster) {
            this.caster = caster;
            return this;
        }

        /**
		 * Build an instance of ConditionImpl
		 */
        public Condition<T> build() throws BuilderException {
            try {
                final PeptideTypeCondition<T> ptCond = new PeptideTypeCondition<T>(this);
                return ptCond.compile();
            } catch (final ParseException e) {
                throw new BuilderException(e.getMessage());
            }
        }
    }

    /**
	 * Compile a peak type specific language expression
	 * 
	 * @param content the expression content
	 * @return a conditional expression
	 * 
	 * @throws ParseException if expression content is not valid
	 */
    private Condition<T> compile() throws ParseException {
        final String content = formatExpression(expression);
        if (logger.isDebugEnabled()) {
            logger.debug("parsing expression = " + content);
        }
        Condition<T> conditionExpression = null;
        final Pattern condPattern = Pattern.compile("([nc]term)\\s*(!?=)\\s*([abcxyzip]+)");
        expression = "";
        final Matcher match = condPattern.matcher(content);
        int from = 0;
        while (match.find()) {
            if (logger.isDebugEnabled()) {
                logger.debug("found " + match.group(0));
            }
            expression += content.substring(from, match.start());
            from = match.end();
            final String condName = match.group(1).toLowerCase();
            final String operatorName = match.group(2);
            final String types = match.group(3);
            String opSymbol = "";
            conditionExpression = makeCondition(match, content, condName, operatorName, types, opSymbol);
            engine.register(condName + opSymbol + types, conditionExpression);
            expression += condName + opSymbol + types;
        }
        expression += content.substring(from);
        return engine.translate(expression);
    }

    private void checkTerms(Matcher matcher, String content, String condName, String types) throws ParseException {
        if (condName.charAt(0) == 'n') {
            final Pattern typePattern = Pattern.compile(".*([abc]+).*");
            final Matcher typeMatch = typePattern.matcher(types);
            if (typeMatch.find()) {
                throw new ParseException("in condition '" + content + "', bad N-terminus condition: " + " invalid " + typeMatch.group(1) + " type", matcher.start(3) + typeMatch.start(1));
            }
        } else {
            final Pattern typePattern = Pattern.compile(".*([xyz]+).*");
            final Matcher typeMatch = typePattern.matcher(types);
            if (typeMatch.find()) {
                throw new ParseException("bad C-terminus condition: " + " invalid " + typeMatch.group(1) + " type", matcher.start(3) + typeMatch.start(1));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Condition<T> makeCondition(Matcher match, String content, String condName, String operatorName, String types, String opSymbol) throws ParseException {
        ConditionImpl.Builder<T, PeptideType> builder = null;
        boolean not = false;
        checkTerms(match, content, condName, types);
        if (operatorName.equals("!=")) {
            opSymbol = "_not";
            not = true;
        }
        Operator<T, ?> operator;
        if (types.length() == 1) {
            char c = types.charAt(0);
            PeptideType value = null;
            if (c == 'i' || c == 'p') {
                if (c == 'i') {
                    value = PeptideTypeImpl.getImmoniumInstance();
                } else {
                    value = PeptideTypeImpl.getPrecursorInstance();
                }
                operator = EQUALS;
                opSymbol += "_eq_";
            } else {
                value = PeptideTypeImpl.valueOf(c);
                operator = BELONGS;
                opSymbol += "_belongs_";
            }
            if (not) {
                builder = new ConditionImpl.Builder<T, PeptideType>(value).operator(operator).not();
            } else {
                builder = new ConditionImpl.Builder<T, PeptideType>(value).operator(operator);
            }
            if (pepTypeCaster != null) {
                builder.accessor(pepTypeCaster);
            }
        } else {
            throw new IllegalArgumentException(types + ": bad types expression");
        }
        return builder.build();
    }

    /**
	 * Format expression in a valid string. (TODO: to simplify this parser as
	 * format changed !)
	 * 
	 * @param expression the expression to make valid
	 * @return a valid expression
	 * 
	 * @throws ParseException if cannot make it valid
	 */
    static String formatExpression(final String expression) throws ParseException {
        final Pattern pattern = Pattern.compile("([(!]*)\\s*(!?=)?" + "\\s*([abcxyzip]+(?:\\s*,\\s*[abcxyzip]{1,2})*)\\s*(\\)*)" + "\\s*([|&])?\\s*");
        final Matcher match = pattern.matcher(expression);
        boolean found = false;
        final StringBuffer sb = new StringBuffer();
        String binOperator = null;
        int shift = 0;
        int openParenthesis = 0;
        int closedParenPos = 0;
        while (match.find()) {
            String term = "term";
            String op = "=";
            found = true;
            if (logger.isDebugEnabled()) {
                logger.debug(shift + ": '" + match.group(0) + "' found ! ");
            }
            if (match.group(1) != null) {
                openParenthesis += match.group(1).replace("!", "").length();
                sb.append(match.group(1));
            }
            if (match.group(2) != null) {
                op = match.group(2);
            }
            if (term.equals("term")) {
                final String[] types = match.group(3).split(",");
                for (final String type : types) {
                    try {
                        sb.append(toTermString(type, op));
                    } catch (final ParseException e) {
                        throw new ParseException(e.getMessage() + " '" + expression + "': parse exception", shift + match.start(3));
                    }
                    sb.append(" | ");
                }
                sb.delete(sb.length() - 2, sb.length());
            } else {
                sb.append(term);
                sb.append(op);
                sb.append(match.group(3));
            }
            if (match.group(4) != null) {
                closedParenPos = match.end(4);
                openParenthesis -= match.group(4).length();
                sb.append(match.group(4));
            }
            binOperator = match.group(5);
            if (binOperator != null) {
                sb.append(" " + binOperator + " ");
            }
            if (logger.isDebugEnabled()) {
                for (int i = 1; i <= match.groupCount(); i++) {
                    logger.debug("group " + i + ": " + match.group(i));
                }
            }
            shift = match.end();
        }
        if (!found) {
            throw new ParseException("bad peak type conditional expression: " + expression, 0);
        } else if (openParenthesis != 0) {
            final boolean beg = (openParenthesis > 0) ? true : false;
            throw new ParseException(((beg) ? "missing" : "remove") + " token ')'" + " in conditional expression: '" + expression + "'", closedParenPos);
        }
        return sb.toString();
    }

    /**
	 * Convert part of expression (term in nterm + cterm)
	 * 
	 * @param type the peak type
	 * @param op the operator
	 * @return an expression f(term) = nterm/cterm
	 * 
	 * @throws ParseException if invalid type
	 */
    public static String toTermString(final String type, final String op) throws ParseException {
        final StringBuffer sb = new StringBuffer();
        final StringBuffer sbNt = new StringBuffer();
        final StringBuffer sbCt = new StringBuffer();
        if (type.length() > 2) {
            throw new ParseException("too many types '" + type + "': " + type.length() + " > 2 (max termini types)", 0);
        }
        for (int i = 0; i < type.length(); i++) {
            final char c = type.charAt(i);
            switch(c) {
                case 'a':
                case 'b':
                case 'c':
                    sbCt.append(c);
                    break;
                case 'x':
                case 'y':
                case 'z':
                    sbNt.append(c);
                    break;
                case 'i':
                    sbNt.append(c);
                    sbCt.append(c);
                    break;
                case 'p':
                    sbNt.append(c);
                    sbCt.append(c);
                    break;
            }
        }
        if (sbNt.length() > 0) {
            sb.append("nterm" + op + sbNt);
        }
        if (sbCt.length() > 0) {
            if (sb.length() > 0) {
                sb.append(" & ");
            }
            sb.append("cterm" + op + sbCt);
        }
        if ((sbNt.length() > 0) && (sbCt.length() > 0)) {
            return "(" + sb.toString() + ")";
        } else {
            return sb.toString();
        }
    }
}
