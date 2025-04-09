package mx.uacam.fdi.io.simplex.resolvedor.mate;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>La clase <code>Monomio</code> describe a un monomio matemático con cada una 
 * de sus partes como lo son el coeficiente, la variable y el exponente.</p>
 * 
 * <p>Se considero que los exponestes de esta clase podian ser fracionario y 
 * negativos.</p>
 *
 * @author Neo Cs || [El Ángel Blanco]
 * @version 1.2.5, 10/10/09
 */
public class Monomio implements Comparable<Monomio>, Serializable {

    private static final long serialVersionUID = -3406449411910610504L;

    private double coeficiente;

    private char variable;

    private int subindice;

    private double exponente;

    public Monomio(String aMonomio) {
        coeficiente = 1;
        subindice = 1;
        exponente = 1;
        String regex = "([-\\+]*\\d*[a-z])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(aMonomio);
        while (m.find()) {
            int start = m.start();
            int end = m.end() - 1;
            String c = aMonomio.substring(start, end);
            c = c.contains("+") ? c.replace("+", "") : c;
            c = c.isEmpty() ? "1" : c;
            int value = Integer.parseInt(c);
            coeficiente = value;
        }
        regex = "([a-z])";
        p = Pattern.compile(regex);
        m = p.matcher(aMonomio);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            char value = aMonomio.substring(start, end).charAt(0);
            variable = value;
        }
        regex = "[a-z]\\d+";
        p = Pattern.compile(regex);
        m = p.matcher(aMonomio);
        while (m.find()) {
            int start = m.start() + 1;
            int value = Integer.parseInt(aMonomio.substring(start));
            subindice = value;
        }
    }

    /**
     * 
     * @param coeficiente
     * @param variable
     * @param subindice
     * @param exponente 
     */
    public Monomio(double coeficiente, char variable, int subindice, double exponente) {
        this.coeficiente = coeficiente;
        this.exponente = exponente;
        this.variable = variable;
        this.subindice = subindice;
    }

    /**
     * 
     * @param coeficiente
     * @param variable
     * @param subindice 
     */
    public Monomio(double coeficiente, char variable, int subindice) {
        this(coeficiente, variable, subindice, 1);
    }

    /**
     *
     * @param coeficiente
     * @param variable
     * @param exponente
     */
    public Monomio(double coeficiente, char variable, double exponente) {
        this(coeficiente, variable, 1, exponente);
    }

    /**
     *
     * @param coeficiente
     * @param variable
     */
    public Monomio(double coeficiente, char variable) {
        this(coeficiente, variable, 1.0);
    }

    /**
     *
     * @param variable
     * @param exponente
     */
    public Monomio(char variable, double exponente) {
        this(1, variable, exponente);
    }

    /**
     *
     * @param variable
     */
    public Monomio(char variable, int subindice) {
        this(1, variable, subindice, 1);
    }

    /**
     *
     * @param variable
     */
    public Monomio(char variable) {
        this(1, variable);
    }

    /**
     * @return the constante
     */
    public double getCoeficiente() {
        return coeficiente;
    }

    /**
     * @param constanate the constante to set
     */
    public void setCoeficiente(double constanate) {
        this.coeficiente = constanate;
    }

    /**
     * @return the variable
     */
    public char getVariable() {
        return variable;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(char variable) {
        if (Character.isLetter(variable)) {
            this.variable = variable;
        } else {
            throw new IllegalArgumentException("La variable debe ser una letra");
        }
    }

    /**
     * Get the value of subindice
     *
     * @return the value of subindice
     */
    public int getSubindice() {
        return subindice;
    }

    /**
     * Set the value of subindice
     *
     * @param subindice new value of subindice
     */
    public void setSubindice(int subindice) {
        this.subindice = subindice;
    }

    /**
     * @return the exponente
     */
    public double getExponente() {
        return exponente;
    }

    /**
     * @param exponente the exponente to set
     */
    public void setExponente(double exponente) {
        this.exponente = exponente;
    }

    @Override
    public int compareTo(Monomio monomio) {
        if (monomio.equals(this)) {
            return 0;
        } else {
            if (monomio.variable == variable && monomio.exponente == exponente && monomio.subindice == subindice) {
                return (int) (coeficiente - monomio.coeficiente);
            } else {
                throw new RuntimeException("No es posible comparar monomio");
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Monomio other = (Monomio) obj;
        if (Double.doubleToLongBits(this.coeficiente) != Double.doubleToLongBits(other.coeficiente)) {
            return false;
        }
        if (Double.doubleToLongBits(this.exponente) != Double.doubleToLongBits(other.exponente)) {
            return false;
        }
        if (this.variable != other.variable) {
            return false;
        }
        if (this.subindice != other.subindice) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.coeficiente) ^ (Double.doubleToLongBits(this.coeficiente) >>> 32));
        hash = 43 * hash + (int) (Double.doubleToLongBits(this.exponente) ^ (Double.doubleToLongBits(this.exponente) >>> 32));
        hash = 43 * hash + this.variable;
        hash = 43 * hash + this.subindice;
        return hash;
    }

    @Override
    public String toString() {
        return String.valueOf(coeficiente) + '(' + String.valueOf(variable) + String.valueOf(subindice) + ")^" + String.valueOf(exponente);
    }
}
