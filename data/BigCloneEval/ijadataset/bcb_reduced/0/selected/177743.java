package com.dekaru.math.method;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.nfunk.jep.*;
import com.dekaru.util.PrintReader;

public class SuccessiveBisections extends Method {

    private Vector<String> fx;

    private Vector<Double> x1, x2, err;

    private Vector<Integer> max;

    private JEP myp;

    public SuccessiveBisections() {
        super(new PrintReader());
        fx = new Vector<String>();
        x1 = new Vector<Double>();
        x2 = new Vector<Double>();
        err = new Vector<Double>();
        max = new Vector<Integer>();
        myp = new JEP();
    }

    public SuccessiveBisections(PrintReader pr) {
        super(pr);
        fx = new Vector<String>();
        x1 = new Vector<Double>();
        x2 = new Vector<Double>();
        err = new Vector<Double>();
        max = new Vector<Integer>();
        myp = new JEP();
    }

    public SuccessiveBisections(Vector<String> fx) {
        super(new PrintReader());
        this.fx = fx;
        x1 = new Vector<Double>();
        x2 = new Vector<Double>();
        err = new Vector<Double>();
        max = new Vector<Integer>();
        myp = new JEP();
    }

    public SuccessiveBisections(PrintReader pr, Vector<String> fx) {
        super(pr);
        this.fx = fx;
        x1 = new Vector<Double>();
        x2 = new Vector<Double>();
        err = new Vector<Double>();
        max = new Vector<Integer>();
        myp = new JEP();
    }

    /**
     * Inicia la aplicaci�n Montante en modo consola.
     * 
     * PENDIENTE.
     * 
     * @param args the command line arguments
     * @throws 	   IOException
     */
    public static void main(String[] args) throws IOException {
        SuccessiveBisections mn = new SuccessiveBisections();
        mn.init();
        mn.displayMenu();
    }

    public void init() {
        myp.addStandardFunctions();
        myp.addStandardConstants();
        myp.addVariable("x", 0);
        pr.println("\nM�todo de Bisecciones Sucesivas. \nDeterminaci�n de ra�ces de ecuaciones no lineales.");
    }

    public void getValuesFromConsole() throws IOException {
        try {
            pr.prints("Introduce la funci�n. \n    F(x) = ");
            pr.flush();
            this.fx.add(pr.readLines());
            pr.printlns("\nDefinici�n de intervalo:");
            pr.prints("\nx1 = ");
            pr.flush();
            x1.add(Double.parseDouble(pr.readLines()));
            pr.prints("\nx2 = ");
            pr.flush();
            x2.add(Double.parseDouble(pr.readLines()));
            pr.prints("\nMargen de Error: ");
            pr.flush();
            err.add(Double.parseDouble(pr.readLines()));
            pr.prints("\nM�x.No. de Iteraciones:");
            pr.flush();
            max.add(Integer.parseInt(pr.readLines()));
            pr.println("\n" + fx.get(0).toString() + " [" + x1.get(0) + "," + x2.get(0) + "]");
        } catch (NumberFormatException nfe) {
            pr.printlns(ERROR_NUM);
        }
    }

    public void getValuesFromFile() {
        try {
            String s = pr.readLinef();
            int i = 0;
            while (!s.equals(".")) {
                StringTokenizer stk = new StringTokenizer(s, " ");
                this.fx.add(stk.nextToken());
                this.x1.add(Double.parseDouble(stk.nextToken()));
                this.x2.add(Double.parseDouble(stk.nextToken()));
                this.err.add(Double.parseDouble(stk.nextToken()));
                this.max.add(Integer.parseInt(stk.nextToken()));
                s = pr.readLinef();
                i++;
            }
        } catch (NumberFormatException nfe) {
            pr.println(nfe.toString());
        } catch (IOException ioe) {
            pr.println(ioe.toString());
        } catch (NullPointerException npe) {
        }
        pr.println(this.toString());
    }

    public void solve() {
        String s = new String();
        for (int i = 0; i < fx.size(); i++) {
            try {
                pr.println("\nF(x) = " + fx.get(i) + "\n");
                bisectar(i, this.x1.get(i).doubleValue(), this.x2.get(i).doubleValue(), 1);
            } catch (NullPointerException npe) {
                pr.println("ERROR.");
            }
        }
    }

    public void bisectar(int i, double x1, double x2, int count) {
        myp.parseExpression(fx.get(i));
        myp.setVarValue("x", x1);
        double fx1 = myp.getValue();
        myp.setVarValue("x", x2);
        double fx2 = myp.getValue();
        pr.println("Bisecci�n " + count);
        pr.println("Intervalo: [" + x1 + "," + x2 + "]");
        pr.println("f(x1) = " + fx1);
        pr.println("f(x2) = " + fx2);
        if (fx1 * fx2 < 0) {
            double xm = (x1 + x2) / 2;
            myp.parseExpression(fx.get(i));
            myp.addVariable("x", xm);
            double fxm = myp.getValue();
            pr.println("  xm  = " + xm);
            pr.println("f(xm) = " + fxm);
            if (fxm <= err.get(i) || count == max.get(i)) {
                pr.println("\nRA�Z ENCONTRADA: " + xm + "\nMARGEN DE ERROR: " + fxm + "\n");
            } else {
                pr.println();
                if (fx1 * fxm < 0) {
                    bisectar(i, x1, xm, ++count);
                } else {
                    if (fx2 * fxm < 0) {
                        bisectar(i, xm, x2, ++count);
                    } else {
                        pr.println("\nERROR: Intervalo inadecuado.");
                    }
                }
            }
        } else {
            pr.println("\nERROR: Intervalo inadecuado.");
        }
    }

    public String toString() {
        String s = new String();
        for (int i = 0; i < fx.size(); i++) {
            s += "\n" + this.fx.get(i).toString() + " [" + this.x1.get(i) + "," + this.x2.get(i) + "]" + " err:" + this.err.get(i) + " " + "max:" + this.max.get(i);
        }
        return s;
    }
}
