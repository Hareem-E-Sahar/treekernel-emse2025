import java.util.Vector;
import java.lang.Math;

/**
 *
 * This class implements the (p-1)-Algorithm by Pollard
 *
 *
 * @author Kristijan Dragicevic
 * @version xx.xx.2004
 *
 */
class PmeAlg {

    int B, a, n, firstd, d, count = 0;

    boolean success = false;

    Vector qList = new Vector();

    Vector aList = new Vector();

    Vector lList = new Vector();

    public PmeAlg(int num, int Boarder, int anum) {
        B = Boarder;
        n = num;
        a = anum;
        SecStep(n, a);
        if (firstd == 1) Pme(n, B, a); else {
            d = firstd;
            success = true;
        }
        return;
    }

    private int SecStep(int n, int a) {
        firstd = GCD(a, n);
        return firstd;
    }

    public int sizeof(Vector v) {
        return v.size();
    }

    private void Pme(int num, int Boarder, int anum) {
        int l;
        count = (int) Math.log((double) num);
        for (int j = 2; j != B + 1; j++) {
            if (j == 2 || ifPrime(j)) {
                l = (int) (Math.floor((double) Math.log(num) / (double) Math.log(j)));
                anum = zpevd(anum, j, l, num);
                aList.addElement(new Integer(anum));
                qList.addElement(new Integer(j));
                lList.addElement(new Integer(l));
            }
            d = GCD(anum - 1, num);
            if (d != 1) break;
        }
        if (d == 1 || d == num) success = false; else success = true;
        return;
    }

    private int zpevd(int a, int q, int l, int n) {
        int x = 1;
        for (int y = 0; y != l; y++) x = x * q;
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

    private int GCD(int amb, int num) {
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
