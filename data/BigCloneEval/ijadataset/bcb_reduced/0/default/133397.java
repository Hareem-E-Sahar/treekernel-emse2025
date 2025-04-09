import java.util.Vector;
import java.lang.Math;

/**
 *
 * This class implements the Quadratic Sieve Algorithm
 *
 *
 * @author Kristijan Dragicevic
 * @version xx.xx.2004
 *
 */
class QuadAlg {

    int m, x = 0, sumX, currentSol = 0, y = 1, result;

    boolean success = false, bSmooth = false, firstStep4 = true;

    Vector primeBase = new Vector();

    Vector allPrimes = new Vector();

    Vector xVector = new Vector();

    Vector sumXVec = new Vector();

    Vector YVector = new Vector();

    Vector ai = new Vector();

    Vector bi = new Vector();

    Vector lj = new Vector();

    Matrix vi = new Matrix();

    Matrix ei = new Matrix();

    Matrix solutionMat = new Matrix();

    public QuadAlg(int num, int t) {
        ei.setSize(1, 0);
        vi.setSize(1, 0);
        primeBase = buildPrimeBase(t, num);
        m = rootof(num);
        step3(m, num);
        while (!success) {
            y = 1;
            sumX = 1;
            lj.clear();
            if (firstStep4) {
                findSubset();
                firstStep4 = false;
            }
            computeXFromAi(num);
            computeLj();
            computeYFromPj(num);
            if ((sumX != y) || (sumX != (num - y))) success = !success;
            if (success) {
                result = GrCoDi(sumX - y, num);
                if (result == 1 || result == num) success = !success;
            }
            if (!success) currentSol++;
            if (currentSol == solutionMat.Rsize()) break;
        }
        return;
    }

    private void computeYFromPj(int n) {
        int helpPj = 0, helpLj = 0;
        y = 1;
        for (int i = 0; i != primeBase.size(); i++) {
            helpPj = ((Integer) primeBase.elementAt(i)).intValue();
            helpLj = ((Integer) lj.elementAt(i)).intValue();
            y = y * zpevd(helpPj, helpLj, 1, n);
            while (y < n) y += n;
            y = y % n;
        }
        YVector.addElement(new Integer(y));
    }

    private void computeLj() {
        int helpL = 1;
        lj.clear();
        for (int j = 0; j != primeBase.size(); j++) {
            for (int i = 0; i != solutionMat.Csize(); i++) {
                if (((Integer) solutionMat.get(currentSol, i)).intValue() == 1) helpL += ((Integer) ei.get(i, j)).intValue();
            }
            lj.addElement(new Integer(helpL / 2));
            helpL = 1;
        }
    }

    private void computeXFromAi(int n) {
        sumX = 1;
        for (int i = 0; i != solutionMat.Csize(); i++) {
            if (((Integer) solutionMat.get(currentSol, i)).intValue() == 1) {
                sumX *= ((Integer) ai.elementAt(i)).intValue();
                while (sumX < n) sumX += n;
                sumX = sumX % n;
                sumXVec.addElement(new Integer(i));
            }
        }
    }

    private void findSubset() {
        Matrix helpMat = new Matrix();
        helpMat.setSize(vi.Rsize(), vi.Csize());
        for (int i = 0; i != vi.Rsize(); i++) {
            for (int j = 0; j != vi.Csize(); j++) helpMat.set(i, j, vi.get(i, j));
        }
        helpMat.transpon();
        solutionMat = helpMat.dualSol();
        currentSol++;
    }

    private void step3(int m, int n) {
        bSmooth = false;
        int b, t = primeBase.size();
        for (int i = 0; i != t + 1; i++) {
            while (!bSmooth) {
                b = (x + m) * (x + m) - n;
                buildVectors(b);
                if (bSmooth) {
                    ai.addElement(new Integer(x + m));
                    bi.addElement(new Integer(b));
                    xVector.addElement(new Integer(x));
                }
                if (x <= 0) x = -x + 1; else x = -x;
            }
            bSmooth = false;
        }
        ei.setSize(ei.Rsize() - 1, ei.Csize());
        vi.setSize(ei.Rsize(), ei.Csize());
    }

    private void buildVectors(int b) {
        Vector helpEi = new Vector();
        Vector helpEi1 = new Vector();
        int helpE;
        int pj;
        int b2 = b;
        int countEi = 0;
        if (b2 < 0) {
            helpEi.addElement(new Integer(1));
            helpEi1.addElement(new Integer(1));
            b2 = -b2;
        } else {
            helpEi.addElement(new Integer(0));
            helpEi1.addElement(new Integer(0));
        }
        for (int i = 1; i != primeBase.size(); i++) {
            pj = ((Integer) primeBase.elementAt(i)).intValue();
            while (b2 % pj == 0) {
                countEi++;
                b2 /= pj;
            }
            helpEi.addElement(new Integer(countEi % 2));
            helpEi1.addElement(new Integer(countEi));
            countEi = 0;
        }
        if (b2 == 1) {
            bSmooth = true;
            ei.setRow(ei.Rsize() - 1, helpEi1);
            vi.setRow(vi.Rsize() - 1, helpEi);
            ei.setSize(ei.Rsize() + 1, ei.Csize());
            vi.setSize(ei.Rsize(), ei.Csize());
        } else bSmooth = false;
        return;
    }

    private Vector buildPrimeBase(int tx, int n) {
        Vector vec = new Vector();
        int count = 3;
        vec.addElement(new Integer(-1));
        if (n % 2 == 1) {
            vec.addElement(new Integer(2));
            tx--;
        }
        allPrimes.addElement(new Integer(2));
        while (tx - 1 != 0) {
            if (ifPrime(count)) {
                allPrimes.addElement(new Integer(count));
                if (quadRes(count, n)) {
                    vec.addElement(new Integer(count));
                    tx--;
                }
            }
            count++;
            count++;
        }
        return vec;
    }

    private int zpevd(int a, int q, int l, int n) {
        int x = 1;
        x = power(q, l);
        int z = 1;
        a = a % n;
        if (x == 0) return z;
        while (x != 0) {
            if (x % 2 == 0) {
                a = (a * a) % n;
                x = x / 2;
            } else {
                x = x - 1;
                z = (z * a) % n;
            }
        }
        return z;
    }

    private int power(int a, int b) {
        double help;
        help = Math.pow((double) a, (double) b);
        return (int) help;
    }

    private boolean quadRes(int p, int n) {
        int Legendre = 0;
        Legendre = zpevd(n, (p - 1) / 2, 1, p);
        if (Legendre == 1) return true; else return false;
    }

    private int rootof(int n) {
        return (int) Math.floor(Math.sqrt((double) n));
    }

    private int GrCoDi(int amb, int num) {
        int x1 = 0, x2 = 1, y1 = 1, y2 = 0, q, r, x, y;
        if (amb == 0) return num;
        while (amb > 0) {
            q = num / amb;
            r = num - q * amb;
            x = x2 - q * x1;
            y = y2 - q * y1;
            num = amb;
            amb = r;
            x2 = x1;
            x1 = x;
            y2 = y1;
            y1 = y;
        }
        return num;
    }

    private boolean ifPrime(int p) {
        Prime q = new Prime(p);
        return q.isPrime();
    }
}
