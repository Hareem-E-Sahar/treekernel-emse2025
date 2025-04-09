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
class RSAFac {

    int x, r, v, x1, count = 1;

    boolean success1 = false, success2 = false;

    Vector vList = new Vector();

    Vector aList = new Vector();

    Vector lList = new Vector();

    public RSAFac(int num, int bnum, int anum, int wnum) {
        x1 = GCD(wnum, num);
        if ((1 < x1) && (x1 < num)) {
            success1 = true;
            success2 = true;
        } else {
            r = Step5(anum, bnum);
            v = zpevd(wnum, r, 1, num);
            if (v == 1) success2 = false; else endStep(v, num);
        }
        return;
    }

    private void endStep(int v, int n) {
        int v0 = v;
        while (v != 1) {
            v0 = v;
            vList.addElement(new Integer(v));
            v = zpevd(v, 2, 1, n);
        }
        vList.addElement(new Integer(v));
        if (v0 == n - 1) success2 = false; else {
            success2 = true;
            x = GCD(v0 + 1, n);
        }
        return;
    }

    private int Step5(int a, int b) {
        int z = a * b - 1;
        while (z % 2 != 1) {
            count *= 2;
            z /= 2;
        }
        return z;
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
}
