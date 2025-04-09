class Fraction {

    int den, num;

    Fraction(int n) {
        num = n;
        den = 1;
    }

    Fraction(int n, int d) {
        num = n;
        den = d;
        reduce();
    }

    Fraction add(Fraction f) {
        Fraction ff = new Fraction(num * f.den + den * f.num, den * f.den);
        return ff;
    }

    Fraction mult(Fraction f) {
        Fraction ff = new Fraction(num * f.num, den * f.den);
        return ff;
    }

    Fraction div(Fraction f) {
        Fraction ff = new Fraction(num * f.den, den * f.num);
        return ff;
    }

    Fraction sub(Fraction f) {
        Fraction ff = new Fraction(num * f.den - f.num * den, den * f.den);
        return ff;
    }

    void print() {
        if (num == 0) eprog.EprogIO.print(0); else {
            eprog.EprogIO.print(num);
            eprog.EprogIO.print("/");
            eprog.EprogIO.print(den);
        }
    }

    int gdc(int n, int d) {
        int rem = n % d;
        while (rem != 0) {
            n = d;
            d = rem;
            rem = n % d;
        }
        return d;
    }

    void reduce() {
        int x = gdc(num, den);
        num /= x;
        den /= x;
        if (den < 0) {
            den *= -1;
            num *= -1;
        }
    }

    boolean less(Fraction f) {
        return (num * f.den < f.num * den);
    }

    boolean greater(Fraction f) {
        return (num * f.den > f.num * den);
    }

    boolean lesseq(Fraction f) {
        return (num * f.den <= f.num * den);
    }

    boolean greatereq(Fraction f) {
        return (num * f.den >= f.num * den);
    }

    boolean equalzero() {
        return (num == 0);
    }
}
