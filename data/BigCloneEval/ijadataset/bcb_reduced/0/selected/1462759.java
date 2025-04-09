package jgrx.iface.impl.terms;

import jgrx.iface.Term;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ellery
 */
public class TermUtil {

    public static Term pow(Term t1, Term t2) {
        return new PowerTerm(t1, t2);
    }

    public static Term mul(Term t1, Term t2) {
        Term[] ts = { t1, t2 };
        return new FactorsTerm(ts);
    }

    public static Term div(Term t1, Term t2) {
        return new FractionTerm(t1, t2);
    }

    public static Term add(Term t1, double d) {
        if (t1.isConstant()) {
            return new SimpleTerm(t1.getCoefficient() + d);
        }
        Term[] ts = { t1, new SimpleTerm(d) };
        return new ParthTerm(ts);
    }

    public static Term add(Term t1, Term t2) {
        Term[] ts = { t1, t2 };
        return new ParthTerm(ts);
    }

    public static Term sbt(Term t1, double d) {
        return add(t1, -d);
    }

    public static Term sbt(Term t1, Term t2) {
        Term[] ts = { t1, t2.mul(-1) };
        return new ParthTerm(ts);
    }

    public static Term trigSubstitution(Term ft) {
        if (ft instanceof TermList) {
            TermList pt = (TermList) ft;
            Term[] ts = new Term[pt.size()];
            for (int i = 0; i < pt.size(); i++) {
                ts[i] = trigSubstitution(pt.get(i));
            }
            return pt.spawn(ts);
        } else if (ft instanceof FunctionTerm) {
            FunctionTerm fct = (FunctionTerm) ft;
            switch(fct.getFunction()) {
                case Tan:
                    {
                        FunctionTerm n = new FunctionTerm("sin", fct.getTerm());
                        n.setCoefficient(ft.getCoefficient());
                        n.setPower(ft.getPower());
                        FunctionTerm d = new FunctionTerm("cos", fct.getTerm());
                        d.setCoefficient(ft.getCoefficient());
                        d.setPower(ft.getPower());
                        return n.div(d);
                    }
                case Cot:
                    {
                        FunctionTerm n = new FunctionTerm("cos", fct.getTerm());
                        n.setCoefficient(ft.getCoefficient());
                        n.setPower(ft.getPower());
                        FunctionTerm d = new FunctionTerm("sin", fct.getTerm());
                        d.setCoefficient(ft.getCoefficient());
                        d.setPower(ft.getPower());
                        return n.div(d);
                    }
                case Sec:
                    {
                        FunctionTerm n = new FunctionTerm("cos", fct.getTerm());
                        n.setCoefficient(ft.getCoefficient());
                        n.setPower(-ft.getPower());
                        return n;
                    }
                case Csc:
                    {
                        FunctionTerm n = new FunctionTerm("sin", fct.getTerm());
                        n.setCoefficient(ft.getCoefficient());
                        n.setPower(-ft.getPower());
                        return n;
                    }
                default:
                    {
                        return ft;
                    }
            }
        } else if (ft instanceof FractionTerm) {
            FractionTerm frt = (FractionTerm) ft;
            FractionTerm frt2 = new FractionTerm(trigSubstitution(frt.getNumerator()), trigSubstitution(frt.getDenominator()));
            frt2.setCoefficient(frt.getCoefficient());
            frt2.setPower(frt.getPower());
            return frt2;
        } else if (ft instanceof PowerTerm) {
            PowerTerm pt = (PowerTerm) ft;
            return new PowerTerm(trigSubstitution(pt.getBase()), trigSubstitution(pt.getExponent()), pt.getCoefficient());
        } else return ft;
    }

    public static void sort(List<Term> terms) {
        mergeSort(terms, 0, terms.size() - 1);
    }

    private static void mergeSort(List<Term> terms, int s, int l) {
        if (l - s >= 1) {
            mergeSort(terms, s, (l + s) / 2);
            mergeSort(terms, (l + s) / 2 + 1, l);
            merge(terms, s, l);
        }
    }

    private static void merge(List<Term> terms, int s, int l) {
        ArrayList<Term> temp = new ArrayList();
        int m = (l + s) / 2;
        int s1 = s;
        int u = m + 1;
        while (s1 <= m && u <= l) {
            if (terms.get(s1).compareTo(terms.get(u)) > 0) {
                temp.add(terms.get(s1));
                s1++;
            } else {
                temp.add(terms.get(u));
                u++;
            }
        }
        while (s1 <= m) {
            temp.add(terms.get(s1));
            s1++;
        }
        while (u <= l) {
            temp.add(terms.get(u));
            u++;
        }
        for (int i = s; i <= l; i++) {
            terms.set(i, temp.get(i - s));
        }
    }
}
