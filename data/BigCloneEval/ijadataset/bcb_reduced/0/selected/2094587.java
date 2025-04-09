package edu.Semaster.FractionCalculator.Model;

public class Fraction implements IFraction {

    private int m_numerator;

    private int m_denominator;

    final double DELTA = 0.001;

    public Fraction() {
        m_numerator = 1;
        m_denominator = 1;
    }

    public Fraction(int numerator, int denominator) {
        if (denominator != 0) {
            m_numerator = numerator;
            m_denominator = denominator;
        } else throw new IllegalArgumentException("invalid denominator");
    }

    public int getNumerator() {
        return m_numerator;
    }

    public int getDenominator() {
        return m_denominator;
    }

    public void setNumerator(int numerator) {
        m_numerator = numerator;
    }

    public void setDenominator(int denominator) {
        if (denominator != 0) {
            m_denominator = denominator;
        } else throw new IllegalArgumentException("invalid denominator");
    }

    public String toString() {
        return m_numerator + "/" + m_denominator;
    }

    public static int greatestCommonDivisor(int a, int b) {
        if (b == 0) return a; else return greatestCommonDivisor(b, a % b);
    }

    public void reduce() {
        int num = greatestCommonDivisor(m_numerator, m_denominator);
        m_numerator = m_numerator / num;
        m_denominator = m_denominator / num;
    }

    public double toDouble() {
        double result = (double) m_numerator / (double) m_denominator;
        return result;
    }

    public boolean equals(Object otherObject) {
        Fraction otherNumber = (Fraction) otherObject;
        if (Math.abs(m_numerator - otherNumber.m_numerator) < DELTA && Math.abs(m_denominator - otherNumber.m_denominator) < DELTA) return true; else return false;
    }

    public IFraction add(IFraction myFraction) {
        Fraction result = new Fraction();
        result.setNumerator(myFraction.getNumerator() * this.getDenominator() + this.getNumerator() * myFraction.getDenominator());
        result.setDenominator(myFraction.getDenominator() * this.getDenominator());
        result.reduce();
        return result;
    }

    public IFraction subtract(IFraction fraction1) {
        IFraction result = new Fraction();
        result.setNumerator(fraction1.getNumerator() * this.getDenominator() - this.getNumerator() * fraction1.getDenominator());
        result.setDenominator(fraction1.getDenominator() * this.getDenominator());
        result.reduce();
        return result;
    }

    public IFraction multiply(IFraction fraction1) {
        IFraction result = new Fraction();
        result.setNumerator((fraction1).getNumerator() * this.getNumerator());
        result.setDenominator(fraction1.getDenominator() * this.getDenominator());
        result.reduce();
        return result;
    }

    public IFraction divide(IFraction fraction1) {
        IFraction result = new Fraction();
        if (this.getNumerator() != 0) {
            result.setNumerator(fraction1.getNumerator() * this.getDenominator());
            result.setDenominator(fraction1.getDenominator() * this.getNumerator());
            result.reduce();
            return result;
        } else {
            throw new IllegalArgumentException("invalid denominator");
        }
    }
}
