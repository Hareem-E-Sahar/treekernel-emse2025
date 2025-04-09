package org.fressia.core.sbes;

import org.apache.log4j.Logger;
import org.fressia.util.RegExpUtils;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;

/**
 * This factory builds sbes given an sbe text statement (see {@link Sbe}). It
 * uses a regular expression which defines the valid sbe type/condition for this
 * factory.
 *
 * @author Alvaro Egana
 *
 * @see Sbe
 */
public class SbeTypeFactory {

    private Logger logger = Logger.getLogger(getClass());

    /** Regular expression that defines the sbe.*/
    private String generalExpression;

    /** Contains the mapping between 'conditions' and
     * classes that implement those conditions.
     */
    private Map<String, String> conditionTypes;

    /** Regular expression that defines all the
     * allowed forms of a condition name.
     */
    private String condRegExpr;

    public SbeTypeFactory() {
    }

    /**
     * @param typeRegExpr  Regular expression that defines all the
     * allowed forms of a sbe type name. It should be just a
     * group of valid type names.
     * <p>
     * Example: "(t|tx|text)"
     * <p>
     * This defines a sbe type that can be named as either
     * 't' or 'tx' or 'text'.
     *
     * @param conditionTypes  Contains the mapping between 'conditions' and
     * classes that implement those conditions.
     * <p>
     * The map has the form:  <i>Regular expression</i> --&gt; <i>full qualified class name</i>
     * <p>
     * Example:
     * (v|val|valid) --&gt; org.fressia.asserts.xml.XmlIsValidAssert
     * <p>
     * Defines the condition whose names can be
     * "<i>v</i>", "<i>val</i>" and "<i>valid</i>" and whose implementing class
     * is "org.fressia.asserts.xml.XmlIsValidAssert".
     *
     */
    public SbeTypeFactory(String typeRegExpr, Map<String, String> conditionTypes) {
        condRegExpr = setTypes(conditionTypes);
        String cs = Sbe.SyntaxisConstants.COND_SIGN;
        String space = Sbe.SyntaxisConstants.SPACE;
        String argument = Sbe.SyntaxisConstants.ARGUMENT;
        this.generalExpression = space + typeRegExpr + space + cs + space + condRegExpr + space + argument + space;
    }

    /**
     * Checks whether an assert can be built by this
     * factory.
     *
     * @param textAssert Source text containing the assert statement.
     * @return <b><code>True</code></b> or <b><code>false</code></b> depending
     * on whether the assert can be built by this factory or not.
     */
    public boolean matches(String textAssert) {
        return textAssert.matches(generalExpression);
    }

    /**
     * Creates a sbe.
     *
     * @param textSbe Source text containing the sbe statement.
     * @return The new assert object.
     * @throws SbeTypeFactoryException
     */
    public Sbe createSbe(String textSbe) throws SbeTypeFactoryException {
        String cs = Sbe.SyntaxisConstants.COND_SIGN;
        String space = Sbe.SyntaxisConstants.SPACE;
        String cond = RegExpUtils.extract(textSbe, cs + space + condRegExpr);
        int ci = cond.indexOf(cs);
        cond = cond.substring(ci + 1).trim();
        String arg = RegExpUtils.extract(textSbe, Sbe.SyntaxisConstants.ARGUMENT);
        int e = arg.lastIndexOf(Sbe.SyntaxisConstants.ARG_END);
        int s = arg.indexOf(Sbe.SyntaxisConstants.ARG_START);
        return getSbe(cond, arg.substring(s + 1, e).trim());
    }

    public Map<String, String> getConditionTypes() {
        return conditionTypes;
    }

    private String setTypes(Map<String, String> conditionTypes) {
        this.conditionTypes = conditionTypes;
        Iterator<String> conds = conditionTypes.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        while (conds.hasNext()) {
            sb.append(conds.next());
            if (conds.hasNext()) {
                sb.append("|");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private Sbe getSbe(String condition, String argument) throws SbeTypeFactoryException {
        Map<String, String> condTypes = getConditionTypes();
        Iterator<String> keys = condTypes.keySet().iterator();
        String key;
        while (keys.hasNext()) {
            key = keys.next();
            if (condition.matches(key)) {
                logger.debug("matching condition: " + key);
                return getSbeInstance(argument, condTypes.get(key));
            }
        }
        throw new SbeTypeFactoryException("Structured boolean expression condition '" + condition + "' not recognized.");
    }

    @SuppressWarnings("unchecked")
    private Sbe getSbeInstance(String argument, String condType) {
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(condType);
            Constructor cons;
            cons = clazz.getConstructor(new Class[] { String.class });
            logger.debug("creating sbe of type: " + condType);
            logger.debug("sbe argument: " + argument);
            return (Sbe) cons.newInstance(new Object[] { argument });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
