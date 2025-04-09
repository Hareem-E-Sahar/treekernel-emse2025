package edu.teacmore.gproection.twodemensions;

import java.util.Map;
import java.util.Vector;
import edu.math.Condition;
import edu.math.GPHTMLView;
import edu.math.HTMLView;
import edu.math.Polynomial;
import edu.math.PolynomialItem;
import edu.webteach.practice.data.ICalculator;
import edu.webteach.practice.data.IDialog;
import edu.webteach.practice.data.IShow;
import edu.webteach.practice.data.ITest;
import edu.webteach.practice.data.Mistake;
import edu.webteach.practice.data.Step;
import edu.webteach.practice.util.HtmlPage;
import java.util.*;
import edu.math.Fraction;
import edu.math.MathVector;

public class BuildConditions implements ICalculator, IShow, IDialog, ITest {

    public int calc(Map in, Map results) {
        MathVector zk = (MathVector) in.get("zk");
        MathVector a = (MathVector) in.get("a");
        MathVector lengths = (MathVector) in.get("length");
        Vector limits = new Vector();
        Vector altlimits = new Vector();
        Vector<Condition> limitations = new Vector<Condition>();
        int zkSize = zk.size();
        for (int i = 0; i < zkSize; i += 2) {
            double l1X = ((Fraction) lengths.get(i)).doubleValue();
            double e1X = ((Fraction) zk.get(i)).doubleValue();
            double l1Y = ((Fraction) lengths.get(i + 1)).doubleValue();
            double e1Y = ((Fraction) zk.get(i + 1)).doubleValue();
            for (int j = i + 2; j < zkSize; j += 2) {
                double l2X = ((Fraction) lengths.get(j)).doubleValue();
                double e2X = ((Fraction) zk.get(j)).doubleValue();
                double subLengthX = (l1X + l2X) / 2;
                double subCentrX = Math.abs(e1X - e2X);
                double l2Y = ((Fraction) lengths.get(j + 1)).doubleValue();
                double e2Y = ((Fraction) zk.get(j + 1)).doubleValue();
                double subLengthY = (l1Y + l2Y) / 2;
                double subCentrY = Math.abs(e1Y - e2Y);
                if (subCentrX >= subLengthX) {
                    Condition cond = new Condition();
                    cond.setSign(Condition.SIGN_NOTLESS);
                    if (e1X > e2X) cond.setLeftPart(Polynomial.parse("x" + (i + 1) + "-x" + (j + 1))); else cond.setLeftPart(Polynomial.parse("x" + (j + 1) + "-x" + (i + 1)));
                    cond.setRightPart(Polynomial.parse(Double.toString(subLengthX)));
                    limits.add(cond);
                } else {
                    limits.add("");
                }
                if (subCentrY >= subLengthY) {
                    Condition cond = new Condition();
                    cond.setSign(Condition.SIGN_NOTLESS);
                    if (e2Y > e1Y) cond.setLeftPart((Polynomial) Polynomial.parse("x" + (i + 2) + "-x" + (j + 2)).mul(-1)); else cond.setLeftPart((Polynomial) Polynomial.parse("x" + (j + 2) + "-x" + (i + 2)).mul(-1));
                    cond.setRightPart(Polynomial.parse(Double.toString(subLengthY)));
                    altlimits.add(cond);
                } else {
                    altlimits.add("");
                }
            }
        }
        for (int i = 0; i < zkSize / 2 + 2; i++) {
            try {
                if (limits.elementAt(i) instanceof Condition) {
                    limitations.add((Condition) limits.elementAt(i));
                } else if (altlimits.get(i) instanceof Condition) {
                    limitations.add((Condition) altlimits.elementAt(i));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < zkSize / 2 + 2; i++) {
            try {
                if (limits.get(i) instanceof Condition && !(altlimits.get(i) instanceof Condition)) {
                    altlimits.remove(i);
                    altlimits.add(i, limits.get(i));
                } else {
                    if (altlimits.get(i) instanceof Condition && !(limits.get(i) instanceof Condition)) {
                        limits.remove(i);
                        limits.add(i, altlimits.get(i));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < zkSize; i++) {
            Condition cond = new Condition();
            cond.setSign(Condition.SIGN_NOTLESS);
            cond.setLeftPart(Polynomial.parse("x" + (i + 1)));
            cond.setRightPart(Polynomial.parse(Double.toString(((Fraction) lengths.get(i)).doubleValue() / 2)));
            limitations.add(cond);
            Condition cond1 = new Condition();
            cond1.setSign(Condition.SIGN_NOTGREATER);
            cond1.setLeftPart(Polynomial.parse("x" + (i + 1)));
            cond1.setRightPart(Polynomial.parse(Double.toString(((Fraction) a.get(i % 2)).doubleValue() - ((Fraction) lengths.get(i)).doubleValue() / 2)));
            limitations.add(cond1);
        }
        results.put("limits", limits);
        results.put("altlimits", altlimits);
        results.put("limitations", limitations);
        results.put("zz0", zk.clone());
        return 0;
    }

    public Mistake check(int retcode, Map calculated, Map inputted) {
        Vector<Condition> limitations = (Vector<Condition>) calculated.get("limitations");
        Vector<Condition> limits = (Vector<Condition>) calculated.get("limits");
        Vector<Condition> altlimits = (Vector<Condition>) calculated.get("altlimits");
        Mistake mistake = new Mistake();
        boolean isMistake = false;
        int k = 0;
        for (int i = 0; i < limits.size(); i++, k++) {
            Condition limitation = limitations.get(i);
            Polynomial leftPart = limitation.getLeftPart();
            Vector variables = leftPart.getVariables();
            Vector values = leftPart.getItems();
            for (int j = 0; j < values.size(); j++) {
                PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                String var = "unp_" + i + "_" + j;
                if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                    isMistake = true;
                    mistake.add(var, inputted.get(var));
                } else {
                    mistake.add("right-" + var, inputted.get(var));
                }
            }
            Polynomial rightPart = limitation.getRightPart();
            values = rightPart.getItems();
            PolynomialItem pi = (PolynomialItem) values.elementAt(0);
            String var = "unpb_" + i;
            if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                isMistake = true;
                mistake.add(var, inputted.get(var));
            } else {
                mistake.add("right-" + var, inputted.get(var));
            }
        }
        for (int i = 0; i < limits.size(); i++) {
            Condition limit = limits.get(i);
            Condition altlimit = altlimits.get(i);
            if (!limit.equals(altlimit)) {
                Polynomial leftPart = altlimit.getLeftPart();
                Vector variables = leftPart.getVariables();
                Vector values = leftPart.getItems();
                for (int j = 0; j < values.size(); j++) {
                    PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                    String var = "unpa_" + i + "_" + j;
                    if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                        isMistake = true;
                        mistake.add(var, inputted.get(var));
                    } else {
                        mistake.add("right-" + var, inputted.get(var));
                    }
                }
                Polynomial rightPart = altlimit.getRightPart();
                values = rightPart.getItems();
                PolynomialItem pi = (PolynomialItem) values.elementAt(0);
                String var = "unpab_" + i;
                if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                    isMistake = true;
                    mistake.add(var, inputted.get(var));
                } else {
                    mistake.add("right-" + var, inputted.get(var));
                }
            }
        }
        for (int i = k; i < limitations.size(); i++) {
            Condition limitation = limitations.get(i);
            Polynomial leftPart = limitation.getLeftPart();
            Vector variables = leftPart.getVariables();
            Vector values = leftPart.getItems();
            for (int j = 0; j < values.size(); j++) {
                PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                String var = "uno_" + i + "_" + j;
                if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                    isMistake = true;
                    mistake.add(var, inputted.get(var));
                } else {
                    mistake.add("right-" + var, inputted.get(var));
                }
            }
            Polynomial rightPart = limitation.getRightPart();
            values = rightPart.getItems();
            PolynomialItem pi = (PolynomialItem) values.elementAt(0);
            String var = "unob_" + i;
            if (!pi.getCoefficient().equals(Fraction.parse((String) inputted.get(var)))) {
                isMistake = true;
                mistake.add(var, inputted.get(var));
            } else {
                mistake.add("right-" + var, inputted.get(var));
            }
        }
        if (isMistake) return mistake;
        return Mistake.NO_MISTAKES;
    }

    public String dialog(Step step, Mistake e) {
        StringBuilder out = new StringBuilder();
        Map results = step.getOutputs();
        Vector<Condition> limitations = (Vector<Condition>) results.get("limitations");
        Vector<Condition> limits = (Vector<Condition>) results.get("limits");
        Vector<Condition> altlimits = (Vector<Condition>) results.get("altlimits");
        out.append("<p>����� ������������� ������������ �� ��?� � ���������:</p>");
        out.append("<table border=0>");
        int k = 0;
        for (int i = 0; i < limits.size(); i++, k++) {
            out.append("<tr><td><b>" + ReduceGreateThan.get2num(i + 1) + ". </b></td>");
            Condition limitation = limitations.get(i);
            Polynomial leftPart = limitation.getLeftPart();
            Vector variables = leftPart.getVariables();
            Vector values = leftPart.getItems();
            for (int j = 0; j < values.size(); j++) {
                if (j != 0) out.append("<td> + </td>");
                PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                String view = GPHTMLView.getVarView((String) variables.get(j));
                String key = "unp_" + i + "_" + j;
                out.append("<td>");
                out.append(Util.inputBoxView(e, key, 1, false));
                out.append("</td><td> � </td><td>" + view + "</td>");
            }
            out.append("<td> &ge; </td>");
            Polynomial rightPart = limitation.getRightPart();
            values = rightPart.getItems();
            PolynomialItem pi = (PolynomialItem) values.elementAt(0);
            String key = "unpb_" + i;
            out.append("<td>" + Util.inputBoxView(e, key, 1, false) + "</td></tr>");
        }
        out.append("</table>");
        out.append("<p>����� ������������� ������������ �� �������������� �����������:</p>");
        out.append("<table border=0>");
        for (int i = 0; i < limits.size(); i++) {
            out.append("<tr><td><b>" + ReduceGreateThan.get2num(i + 1) + ". </b></td>");
            Condition limit = limits.get(i);
            Condition altlimit = altlimits.get(i);
            if (!limit.equals(altlimit)) {
                Polynomial leftPart = altlimit.getLeftPart();
                Vector variables = leftPart.getVariables();
                Vector values = leftPart.getItems();
                for (int j = 0; j < values.size(); j++) {
                    if (j != 0) out.append("<td> + </td>");
                    PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                    String view = GPHTMLView.getVarView((String) variables.get(j));
                    String key = "unpa_" + i + "_" + j;
                    out.append("<td><input name=\"" + key + "\" type=\"text\" size=1 " + "value=\"" + Util.inputBoxView(e, key, 1, false) + "\" /></td><td> � </td><td>" + view + "</td>");
                }
                out.append("<td> &ge; </td>");
                Polynomial rightPart = altlimit.getRightPart();
                values = rightPart.getItems();
                PolynomialItem pi = (PolynomialItem) values.elementAt(0);
                String key = "unpab_" + i;
                out.append("<td><input name=\"" + key + "\" type=\"text\" size=1 " + "value=\"" + Util.inputBoxView(e, key, 1, false) + "\" /></td>");
            } else {
            }
            out.append("</tr>");
        }
        out.append("</table>");
        out.append("<p>����� ��������� ������:</p>");
        out.append("<table border=0>");
        for (int i = k; i < limitations.size(); i++) {
            out.append("<tr><td><b>" + ReduceGreateThan.get2num(i + 1) + ". </b></td>");
            Condition limitation = limitations.get(i);
            Polynomial leftPart = limitation.getLeftPart();
            Vector variables = leftPart.getVariables();
            Vector values = leftPart.getItems();
            for (int j = 0; j < values.size(); j++) {
                if (j != 0) out.append("<td> + </td>");
                PolynomialItem pi = (PolynomialItem) values.elementAt(j);
                String view = GPHTMLView.getVarView((String) variables.get(j));
                String key = "uno_" + i + "_" + j;
                out.append("<td>" + Util.inputBoxView(e, key, 1, false) + "</td><td> � </td><td>" + view + "</td>");
            }
            if (limitation.getSign() == Condition.SIGN_NOTGREATER) out.append("<td> &le; </td>"); else out.append("<td> &ge; </td>");
            Polynomial rightPart = limitation.getRightPart();
            values = rightPart.getItems();
            PolynomialItem pi = (PolynomialItem) values.elementAt(0);
            String key = "unob_" + i;
            out.append("<td>" + Util.inputBoxView(e, key, 1, false) + "</td></tr>");
        }
        out.append("</table>");
        return out.toString();
    }

    public String show(Step step) {
        StringBuilder out = new StringBuilder();
        Map inputs = step.getInputs();
        MathVector zk = (MathVector) inputs.get("zk");
        MathVector a = (MathVector) inputs.get("a");
        MathVector length = (MathVector) inputs.get("length");
        out.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" bordercolorlight=\"gray\" bordercolordark=\"white\">");
        out.append("<tr><td>������� ����������</td><td>" + HTMLView.getView(zk) + "</td></tr>");
        out.append("<tr><td>������� ����� ������������</td><td>" + HTMLView.getView(length) + "</td></tr>");
        out.append("<tr><td>�������</td><td>" + HTMLView.getView(length) + "</td></tr>");
        out.append("</table>");
        return out.toString();
    }

    public String showResults(Map results, int retcode, Step step) {
        StringBuilder out = new StringBuilder();
        Vector<Condition> limitations = (Vector<Condition>) results.get("limitations");
        Vector<Condition> altlimits = (Vector<Condition>) results.get("altlimits");
        out.append("<p>��������� (����� ������������� ������������ �� ����� �� ������ �� ��� ������):</p>");
        out.append(GPHTMLView.getConditionsView(limitations));
        out.append("<p>������������ ����� ������������� ������������:</p>");
        out.append("<table border=\"0\" >");
        for (int i = 0; i < altlimits.size(); i++) {
            out.append("<tr><td><b>" + GPHTMLView.get2num((i + 1)) + "</b></td>");
            Condition lim = altlimits.get(i);
            if (lim.equals(limitations.get(i))) out.append("<td colspan=6>�������������</td>"); else out.append("<td>" + GPHTMLView.getView(lim) + "</td>");
            out.append("</tr>");
        }
        out.append("</table>");
        return out.toString();
    }
}
