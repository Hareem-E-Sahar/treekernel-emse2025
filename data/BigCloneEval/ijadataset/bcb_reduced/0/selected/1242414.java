package moller.util.datatype;

public class Rational {

    private int numerator;

    private int denominator;

    public static final String DELIMITER = "/";

    public Rational(String numerator, String denominator) {
        this(Integer.parseInt(numerator), Integer.parseInt(denominator));
    }

    public Rational(int numerator, int denominator) {
        if (denominator == 0) {
            throw new RuntimeException("Denominator is zero");
        }
        int g = gcd(numerator, denominator);
        this.numerator = numerator / g;
        this.denominator = denominator / g;
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    public void setDenominator(int denominator) {
        this.denominator = denominator;
    }

    public String getDelimiter() {
        return DELIMITER;
    }

    public String toString() {
        if (denominator == 1) {
            return numerator + "";
        } else {
            return numerator + DELIMITER + denominator;
        }
    }

    private int gcd(int m, int n) {
        if (0 == n) {
            return m;
        } else {
            return gcd(n, m % n);
        }
    }
}
