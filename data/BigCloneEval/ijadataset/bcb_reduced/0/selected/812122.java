package org.deri.iris.evaluation_old;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.BUILTIN;
import static org.deri.iris.factory.Factory.TERM;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.builtins.ExactEqualBuiltin;

/**
 * <p>
 * This class offers some miscellaneous operations.
 * </p>
 * <p>
 * $Id: MiscOps.java,v 1.23 2007-11-04 13:36:34 bazbishop237 Exp $
 * </p>
 * 
 * @author Richard Pöttler (richard dot poettler at deri dot at)
 * @author graham
 * @author Darko Anicic, DERI Innsbruck
 * 
 * @version $Revision: 1.23 $
 */
public class MiscOps {

    /** prefix for the variables in rectified rules. */
    private static final String VAR_PREFIX = "X_";

    private MiscOps() {
    }

    /**
	 * Rectifies a collectin of rules.
	 * 
	 * @param r
	 *            the rules to rectify
	 * @return a set of rectified rules corresponding to the given rules
	 * @throws NullPointerException
	 *             if the collection is {@code null}
	 * @see MiscOps#rectify(IRule)
	 */
    public static Set<IRule> rectify(final Collection<IRule> r) {
        if (r == null) {
            throw new NullPointerException("The rules must not be null");
        }
        final Set<IRule> rules = new HashSet<IRule>(r.size());
        for (final IRule rule : r) {
            rules.add(rectify(rule));
        }
        return rules;
    }

    /**
	 * <p>
	 * Rectifies a rule.
	 * </p>
	 * <p>
	 * Algorithm has been implemented from the book: "Principles of Database and 
	 * Knowledge-Base Systems Vol. 1", Jeffrey D. Ullman (page: 111.)
	 * </p>
	 * <p>
	 * Remark: This method cannot be used when function symbols (constructed terms) 
	 * in use. In such a case a modification of the  current implementation is 
	 * required. Only safe rules can be rectified.
	 * </p>
	 * @param r	The rule to rectify.
	 * @return 	The rectified rule.
	 * @throws 	NullPointerException
	 *             if the rule is {@code null}.
	 * @throws IllegalArgumentException
	 *             if the length of the head is unequal to 1.
	 */
    public static IRule rectify(final IRule r) {
        if (r == null) {
            throw new NullPointerException("The rule must not be null.");
        }
        if (r.getHead().size() != 1) {
            throw new IllegalArgumentException("There must be only one literal in the head.");
        }
        final ILiteral hl = r.getHead().get(0);
        final int arity = hl.getAtom().getPredicate().getArity();
        final List<ITerm> headTerms = new ArrayList<ITerm>(arity);
        final List<ILiteral> eqSubGoals = new ArrayList<ILiteral>(r.getBody().size());
        final Map<IVariable, List<ILiteral>> headVarsMap = new HashMap<IVariable, List<ILiteral>>();
        final Iterator<ITerm> terms = hl.getAtom().getTuple().iterator();
        for (int i = 0; i < arity; i++) {
            final ITerm t = terms.next();
            final IVariable newVar = TERM.createVariable(VAR_PREFIX + i);
            headTerms.add(newVar);
            if (t.isGround()) {
                eqSubGoals.add(BASIC.createLiteral(true, new ExactEqualBuiltin(newVar, t)));
            } else {
                final IVariable v = (IVariable) t;
                if (headVarsMap.containsKey(v)) {
                    headVarsMap.get(v).add(BASIC.createLiteral(true, BUILTIN.createEqual(t, newVar)));
                } else {
                    final List<ILiteral> eqLiterals = new ArrayList<ILiteral>();
                    eqLiterals.add(BASIC.createLiteral(true, BUILTIN.createEqual(t, newVar)));
                    headVarsMap.put(v, eqLiterals);
                }
            }
        }
        final List<ILiteral> bodyLiterals = new ArrayList<ILiteral>(r.getBody().size());
        for (final ILiteral l : r.getBody()) {
            final List<ITerm> litTerms = new ArrayList<ITerm>(l.getAtom().getPredicate().getArity());
            for (final ITerm t : l.getAtom().getTuple()) {
                if (!t.isGround()) {
                    final List<ILiteral> eqLiterals = headVarsMap.get(t);
                    if (eqLiterals != null) {
                        litTerms.add(eqLiterals.get(0).getAtom().getTuple().get(1));
                    } else {
                        litTerms.add(t);
                    }
                } else {
                    litTerms.add(t);
                }
            }
            final IAtom a;
            if (l.getAtom().isBuiltin()) {
                a = dublicateAtom(l.getAtom(), litTerms);
            } else {
                a = BASIC.createAtom(l.getAtom().getPredicate(), BASIC.createTuple(litTerms));
            }
            bodyLiterals.add(BASIC.createLiteral(l.isPositive(), a));
        }
        bodyLiterals.addAll(eqSubGoals);
        for (final List<ILiteral> equals : headVarsMap.values()) {
            if (equals.size() > 1) {
                ITerm last = null;
                for (final ILiteral l : equals) {
                    final ITerm actual = l.getAtom().getTuple().get(1);
                    if (last != null) {
                        bodyLiterals.add(BASIC.createLiteral(true, BUILTIN.createEqual(last, actual)));
                    }
                    last = actual;
                }
            }
        }
        return BASIC.createRule(java.util.Arrays.asList(new ILiteral[] { BASIC.createLiteral(hl.isPositive(), hl.getAtom().getPredicate(), BASIC.createTuple(headTerms)) }), bodyLiterals);
    }

    /**
	 * Creates a new instance of the given atom. This mehtod was intented to
	 * dublicate builtin atoms. Therefore the constructor of the atom must 
	 * be public and take an array of terms as parameters. If the builtin 
	 * should be dublicated with it's terms, then the term collection might 
	 * be <code>null</code>.
	 * @param a the atom to dublicate
	 * @param t the terms to put into the builtin (if <code>null</code> the
	 * terms from the atom are taken)
	 * @return the copy of the builtin
	 * @throws NullPointerException if the atom is <code>null</code>
	 * @throws IllegalArgumentException if the constructor couldn't be found
	 * @throws IllegalArgumentException if the builtin class is abstract
	 * @throws IllegalArgumentException if the construcot could not be
	 * accessed
	 * @throws IllegalArgumentException if the construcor threw an exception
	 * @deprecated using reflection is a bad idea -&gt; find a better approach
	 */
    private static IAtom dublicateAtom(final IAtom a, final Collection<ITerm> t) {
        if (a == null) {
            throw new NullPointerException("The atom must not be null");
        }
        final ITerm[] terms = (t == null) ? a.getTuple().toArray(new ITerm[a.getTuple().size()]) : t.toArray(new ITerm[t.size()]);
        try {
            return a.getClass().getConstructor(ITerm[].class).newInstance(new Object[] { terms });
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Couldn't find the consturctor for " + a.getClass().getName(), e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Couldn't create the instance " + "(the class is abstract) for " + a.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Couldn't access the constructor for " + a.getClass().getName(), e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalArgumentException("The constructor of " + a.getClass().getName() + " threw an exception", e);
        }
    }
}
