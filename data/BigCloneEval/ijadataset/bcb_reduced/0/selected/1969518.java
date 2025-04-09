package org.expasy.jpl.commons.ms.filtering.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.expasy.jpl.bio.sequence.JPLCTerminus;
import org.expasy.jpl.bio.sequence.JPLITerminus;
import org.expasy.jpl.bio.sequence.JPLNTerminus;
import org.expasy.jpl.commons.ms.peak.JPLIMSnPeakType;
import org.expasy.jpl.utils.builder.JPLBuilder;
import org.expasy.jpl.utils.builder.JPLBuilderBased;
import org.expasy.jpl.utils.condition.*;
import org.expasy.jpl.utils.condition.JPLIConditionalExpression.OPERATOR;
import org.expasy.jpl.utils.builder.JPLBuilderException;
import org.expasy.jpl.utils.parser.JPLParseException;

/**
 * This object handles creation of conditional expression from
 * string for peaks based on {@code JPLConditionalExpressionFactory}.
 * The expression by now permits only to test conditions on JPLIPeakType objects
 * 
 * @author nikitin
 *
 */
public class JPLPeakTypeConditions<T> implements JPLBuilderBased<JPLBuilder<JPLIConditionalExpression<T>>> {

    private static Log logger = LogFactory.getLog(JPLPeakTypeConditions.class);

    /** the main engine that create conditional expressions */
    private JPLConditionalExpressionEngine<T> engine;

    /** this hook gives a peak type from T */
    private JPLICastHook<T, JPLIMSnPeakType> toPeakTypeHook;

    private String expression;

    public JPLPeakTypeConditions(Builder<T> builder) {
        engine = new JPLConditionalExpressionEngine<T>();
        expression = builder.expression;
        toPeakTypeHook = builder.hook;
    }

    /**
	 * A builder to ease the construction of complex conditions
	 * 
	 * @author nikitin
	 */
    public static class Builder<T> implements JPLBuilder<JPLIConditionalExpression<T>> {

        private String expression;

        private JPLICastHook<T, JPLIMSnPeakType> hook;

        public Builder(String expression) {
            this.expression = expression;
        }

        public Builder<T> hook2PeakType(JPLICastHook<T, JPLIMSnPeakType> hook) {
            this.hook = hook;
            return this;
        }

        /**
		 * Build an instance of JPLCondition
		 */
        public JPLIConditionalExpression<T> build() throws JPLBuilderException {
            try {
                JPLPeakTypeConditions<T> ptCond = new JPLPeakTypeConditions<T>(this);
                return ptCond.compile();
            } catch (JPLParseException e) {
                throw new JPLBuilderException(e.getMessage());
            }
        }
    }

    public JPLBuilder<JPLIConditionalExpression<T>> getBuilder() {
        return null;
    }

    /**
	 * Compile a peak type specific language expression
	 * 
	 * @param content the expression content
	 * @return a conditional expression
	 * 
	 * @throws JPLParseException if expression content is not valid
	 */
    private JPLIConditionalExpression<T> compile() throws JPLParseException {
        JPLCastHookSequence<T, Set<JPLITerminus>> hookSequence = new JPLCastHookSequence<T, Set<JPLITerminus>>();
        if (toPeakTypeHook != null) {
            hookSequence.addHook(toPeakTypeHook);
        }
        JPLICastHook<JPLIMSnPeakType, Set<JPLITerminus>> toTerminiHook = new JPLICastHook<JPLIMSnPeakType, Set<JPLITerminus>>() {

            public Set<JPLITerminus> cast(JPLIMSnPeakType object) {
                Set<JPLITerminus> s = new HashSet<JPLITerminus>();
                s.add(object.getNTerm());
                s.add(object.getCTerm());
                return s;
            }
        };
        hookSequence.addHook(toTerminiHook);
        String content = formatExpression(expression);
        if (logger.isDebugEnabled()) {
            logger.debug("parsing expression = " + content);
        }
        JPLIConditionalExpression<T> conditionExpression = null;
        Pattern condPattern = Pattern.compile("([nNcC]t(?:erm)?)\\s*(!?=)\\s*([abcxyzipP]+)");
        expression = "";
        Matcher match = condPattern.matcher(content);
        int from = 0;
        while (match.find()) {
            boolean not = false;
            if (logger.isDebugEnabled()) {
                logger.debug("found " + match.group(0));
            }
            expression += content.substring(from, match.start());
            from = match.end();
            String condName = match.group(1).toLowerCase();
            String operatorName = match.group(2);
            String types = match.group(3);
            if (condName.charAt(0) == 'n') {
                Pattern typePattern = Pattern.compile(".*([abc]+).*");
                Matcher typeMatch = typePattern.matcher(types);
                if (typeMatch.find()) {
                    throw new JPLParseException("in condition '" + content + "', bad N-terminus condition: " + " invalid " + typeMatch.group(1) + " type", match.start(3) + typeMatch.start(1));
                }
            } else {
                Pattern typePattern = Pattern.compile(".*([xyz]+).*");
                Matcher typeMatch = typePattern.matcher(types);
                if (typeMatch.find()) {
                    throw new JPLParseException("bad C-terminus condition: " + " invalid " + typeMatch.group(1) + " type", match.start(3) + typeMatch.start(1));
                }
            }
            String opSymbol = "";
            if (operatorName.equals("!=")) {
                opSymbol = "_not";
                not = true;
            }
            OPERATOR operator;
            if (types.length() > 1) {
                Set<JPLITerminus> value = new HashSet<JPLITerminus>();
                operator = OPERATOR.INTER;
                opSymbol += "_inter_";
                for (int i = 0; i < types.length(); i++) {
                    JPLITerminus term = getTerminus(types.charAt(i), (condName.charAt(0) == 'n'));
                    ((Set<JPLITerminus>) value).add(term);
                }
                if (not) {
                    conditionExpression = new JPLCondition.Builder<T, Set<JPLITerminus>>(value).castHook(hookSequence).operator(operator).not().build();
                } else {
                    conditionExpression = new JPLCondition.Builder<T, Set<JPLITerminus>>(value).castHook(hookSequence).operator(operator).build();
                }
            } else {
                JPLITerminus value = getTerminus(types.charAt(0), (condName.charAt(0) == 'n'));
                operator = OPERATOR.CONTAINS;
                opSymbol += "_contains_";
                if (not) {
                    conditionExpression = new JPLCondition.Builder<T, JPLITerminus>(value).castHook(hookSequence).operator(operator).not().build();
                } else {
                    conditionExpression = new JPLCondition.Builder<T, JPLITerminus>(value).castHook(hookSequence).operator(operator).build();
                }
            }
            engine.addSymbol(condName + opSymbol + types, conditionExpression);
            expression += condName + opSymbol + types;
        }
        expression += content.substring(from);
        return engine.compile(expression);
    }

    /**
	 * Format expression in a valid string
	 * 
	 * @param expression the expression to make valid
	 * @return a valid expression
	 * 
	 * @throws JPLParseException if cannot make it valid
	 */
    static String formatExpression(String expression) throws JPLParseException {
        Pattern pattern = Pattern.compile("([(!]*)\\s*([nNcC]?t(?:erm)?)?\\s*(!?=)" + "\\s*([abcxyzipP]+(?:\\s*,\\s*[abcxyzipP]{1,2})*)\\s*(\\)*)" + "\\s*([|&])?\\s*");
        Matcher match = pattern.matcher(expression);
        boolean found = false;
        StringBuffer sb = new StringBuffer();
        String binOperator = null;
        int shift = 0;
        int openParenthesis = 0;
        int closedParenPos = 0;
        while (match.find()) {
            found = true;
            if (logger.isDebugEnabled()) {
                logger.debug(shift + ": '" + match.group(0) + "' found ! ");
            }
            if (match.group(1) != null) {
                openParenthesis += match.group(1).replace("!", "").length();
                sb.append(match.group(1));
            }
            if (match.group(2).equals("term")) {
                String[] types = match.group(4).split(",");
                for (String type : types) {
                    try {
                        sb.append(toTermString(type, match.group(3)));
                    } catch (JPLParseException e) {
                        throw new JPLParseException("'" + expression + "': parse exception", shift + match.start(4), e);
                    }
                    sb.append(" | ");
                }
                sb.delete(sb.length() - 2, sb.length());
            } else {
                sb.append(match.group(2));
                sb.append(match.group(3));
                sb.append(match.group(4));
            }
            if (match.group(5) != null) {
                closedParenPos = match.end(5);
                openParenthesis -= match.group(5).length();
                sb.append(match.group(5));
            }
            binOperator = match.group(6);
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
            throw new JPLParseException("bad peak type conditional expression: " + expression, 0);
        } else if (openParenthesis != 0) {
            boolean beg = (openParenthesis > 0) ? true : false;
            throw new JPLParseException(((beg) ? "missing" : "remove") + " token ')'" + " in conditional expression: '" + expression + "'", closedParenPos);
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
	 * @throws JPLParseException if invalid type
	 */
    public static String toTermString(String type, String op) throws JPLParseException {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbNt = new StringBuffer();
        StringBuffer sbCt = new StringBuffer();
        if (type.length() > 2) {
            throw new JPLParseException("too many types '" + type + "': " + type.length() + " > 2 (max termini types)", 0);
        }
        for (int i = 0; i < type.length(); i++) {
            char c = type.charAt(i);
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
        if (sbNt.length() > 0 && sbCt.length() > 0) {
            return "(" + sb.toString() + ")";
        } else {
            return sb.toString();
        }
    }

    /**
	 * Get a JPLITerminus type from a character
	 * 
	 * @param c the char to convert
	 * @param isNterm an info on n terminus type when ambiguities
	 * @return an instance of JPLITerminus
	 */
    public static JPLITerminus getTerminus(char c, boolean isNterm) {
        JPLITerminus term = null;
        switch(c) {
            case 'a':
                term = JPLCTerminus.FragmentA;
                break;
            case 'b':
                term = JPLCTerminus.FragmentB;
                break;
            case 'c':
                term = JPLCTerminus.FragmentC;
                break;
            case 'x':
                term = JPLNTerminus.FragmentX;
                break;
            case 'y':
                term = JPLNTerminus.FragmentY;
                break;
            case 'z':
                term = JPLNTerminus.FragmentZ;
                break;
            case 'i':
                if (isNterm) {
                    term = JPLNTerminus.ImmoY;
                } else {
                    term = JPLCTerminus.ImmoA;
                }
                break;
            case 'p':
                if (isNterm) {
                    term = JPLNTerminus.Peptide;
                } else {
                    term = JPLCTerminus.Peptide;
                }
                break;
            case 'P':
                if (isNterm) {
                    term = JPLNTerminus.Protein;
                } else {
                    term = JPLCTerminus.Protein;
                }
                break;
        }
        return term;
    }
}
