package petrieditor.modules.invariants;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @author Piotr Mlocek
 */
public class Invariants {

    public static void testPInvariants(int m[][], Matrix inv) {
        test(new Matrix(m).transpose(), inv);
    }

    public static void testTInvariants(int m[][], Matrix inv) {
        test(new Matrix(m), inv);
    }

    /**
     * Funkcja sprawdzajaca, czy znalezione niezmienniki sa poprawne
     *
     * @param incidence macierz incydencji sieci
     * @param invariants macierz, ktorej kolumny sa niezmiennikami sieci
     */
    public static void test(Matrix incidence, Matrix invariants) {
        for (int a = 0; a < invariants.getColumnDimension(); a++) {
            Matrix i = invariants.getMatrix(0, invariants.getRowDimension() - 1, a, a);
            Matrix r = incidence.mul(incidence, i);
            if (r.isZeroMatrix()) System.out.println(a + ":OK"); else System.out.println(a + ":Error");
        }
    }

    public static Matrix computePInvariants(int m[][]) {
        return compute(Matrix.constructWithCopy(m));
    }

    public static Matrix computeTInvariants(int m[][]) {
        return compute(Matrix.constructWithCopy(m).transpose());
    }

    /**
     * Zwraca najwiekszy wspolny dzielnik
     *
     * @param a pierwsza liczba
     * @param b druga liczba
     * @return najwiekszy wspolny dzielnik liczb
     */
    private static int gcd(int a, int b) {
        int tmp;
        while (b != 0) {
            tmp = b;
            b = a % b;
            a = tmp;
        }
        return a;
    }

    /**
     * Redukuje wektor przez najwiekszy wspolny dzielnik znajdujacych sie w nim liczb
     *
     * @param a wektor do zredukowania
     * @return najwiekszy wspolny dzielnik znajdujacych sie w wektorze liczb
     */
    private static int reduceByGcd(int a[]) {
        int j = 0;
        while (j < a.length && a[j] == 0) ++j;
        if (j == a.length) return 0;
        int initial = a[j];
        int divisor = initial;
        for (j = 0; j < a.length; ++j) if (a[j] != 0) divisor = gcd(divisor, a[j]);
        if (divisor != 0) {
            if ((divisor > 0) ^ (initial > 0)) divisor = -divisor;
            if (divisor != 1) for (j = 0; j < a.length; ++j) a[j] = a[j] / divisor;
        }
        return divisor;
    }

    /**
     * Sprawdza, czy wektor jest niezerowy
     * 
     * @return true jesli wektor posiada niezerowy element, false w przeciwnym wypadku
     */
    private static boolean isNonZero(int a[]) {
        for (int i = 0; i < a.length; ++i) if (a[i] != 0) return true;
        return false;
    }

    /**
     * Wlasciwa procedura obliczajaca niezmienniki
     * 
     * @param incidence macierz incydencji sieci
     * @return macierz, ktorej kolumny sa kolejnymi niezmiennikami
     */
    private static Matrix compute(Matrix incidence) {
        int rows = incidence.getRowDimension(), cols = incidence.getColumnDimension();
        Matrix basis = Matrix.identity(rows, rows);
        int row = 0, pivot = 0;
        while (row < rows && pivot < cols) {
            while (pivot < cols && incidence.get(row, pivot) == 0) {
                int k = row + 1;
                while (k < rows && incidence.get(row, pivot) == 0) {
                    if (incidence.get(k, pivot) != 0) {
                        int tmp[] = incidence.getRowCopy(row);
                        incidence.setRow(row, incidence.getRow(k));
                        incidence.setRow(k, tmp);
                        tmp = basis.getRowCopy(row);
                        basis.setRow(row, basis.getRow(k));
                        basis.setRow(k, tmp);
                    } else ++k;
                }
                if (incidence.get(row, pivot) == 0) ++pivot;
            }
            if (pivot < cols) {
                int k1 = incidence.get(row, pivot);
                for (int k = row + 1; k < rows; ++k) {
                    if (incidence.get(k, pivot) != 0) {
                        int k2 = incidence.get(k, pivot);
                        for (int j = pivot; j < cols; ++j) incidence.set(k, j, k1 * incidence.get(k, j) - k2 * incidence.get(row, j));
                        for (int j = 0; j < rows; ++j) basis.set(k, j, k1 * basis.get(k, j) - k2 * basis.get(row, j));
                        int l1[] = incidence.getRowCopy(k);
                        int l2[] = basis.getRowCopy(k);
                        int fat1 = reduceByGcd(l1);
                        int fat2 = reduceByGcd(l2);
                        if (fat1 != fat2) {
                            int fatgcd = gcd(fat1, fat2);
                            for (int j = 0; j < cols; ++j) incidence.set(k, j, incidence.get(k, j) / fatgcd);
                            for (int j = 0; j < rows; ++j) basis.set(k, j, basis.get(k, j) / fatgcd);
                        } else {
                            incidence.setRow(k, l1);
                            basis.setRow(k, l2);
                        }
                    }
                }
            }
            ++row;
            ++pivot;
        }
        int inv = 0;
        for (int i = 0; i < rows; ++i) if (!isNonZero(incidence.getRow(i))) ++inv;
        Matrix nbasis = new Matrix(inv, rows);
        inv = 0;
        for (int i = 0; i < rows; ++i) if (!isNonZero(incidence.getRow(i))) nbasis.setRow(inv++, basis.getRow(i));
        basis = nbasis;
        basis = deleteNotReducableRows(basis);
        return getPositive(basis).transpose();
    }

    /**
     * Zwraca support niezmiennika w postaci tablicy.
     * Jesli na pozycji i jest 1 to oznacza to, ze element i nalezy do zbioru.
     *
     * @param a wektor, dla ktorego jest liczony support
     * @return support wektora
     */
    private static int[] getSupport(int[] a) {
        int res[] = new int[a.length];
        for (int i = 0; i < a.length; ++i) if (a[i] != 0) res[i] = 1;
        return res;
    }

    private static Matrix deleteNotReducableRows(Matrix basis) {
        int row = 0, pivot = 0;
        int rows = basis.getRowDimension(), cols = basis.getColumnDimension();
        while (row < rows && pivot < cols) {
            while (pivot < cols && basis.get(row, pivot) == 0) {
                int k = row + 1;
                while (k < rows && basis.get(row, pivot) == 0) if (basis.get(k, pivot) != 0) {
                    int[] tmp = basis.getRowCopy(row);
                    basis.setRow(row, basis.getRow(k));
                    basis.setRow(k, tmp);
                } else ++k;
                if (basis.get(row, pivot) == 0) ++pivot;
            }
            if (pivot < cols) {
                int f = reduceByGcd(basis.getRow(row));
                int k1 = basis.get(row, pivot);
                for (int k = 0; k < rows; ++k) {
                    if (k != row) {
                        if (basis.get(k, pivot) != 0) {
                            int k2 = basis.get(k, pivot);
                            for (int j = 0; j < cols; ++j) basis.set(k, j, k1 * basis.get(k, j) - k2 * basis.get(row, j));
                            int g = reduceByGcd(basis.getRow(k));
                        }
                    }
                }
            }
            ++row;
            ++pivot;
        }
        while (true) {
            boolean flag = false;
            for (int j = 0; j < cols; ++j) {
                boolean pos = false, neg = false;
                row = 0;
                while (!pos && row < basis.getRowDimension()) {
                    if (basis.get(row, j) > 0) pos = true; else if (basis.get(row, j) < 0) neg = true;
                    ++row;
                }
                if (neg && !pos) {
                    flag = true;
                    for (int l = basis.getRowDimension() - 1; l >= 0; --l) if (basis.get(l, j) < 0) {
                        basis = basis.eliminateRow(l);
                    }
                }
            }
            if (!flag) break;
        }
        return basis;
    }

    /**
     * Oblicza kombinacje liniowa wektora (k1 * a) i (k2 * b).
     *
     * @param a pierwszy wektor
     * @param b drugi wektor
     * @param c wektor wyjsciowy - wynik
     * @param k1 mnoznik pierwszego wektora
     * @param k2 mnoznik drugiego wektora
     */
    private static void linearCombination(int a[], int b[], int c[], int k1, int k2) {
        for (int i = 0; i < a.length; ++i) c[i] = a[i] * k1 + b[i] * k2;
    }

    /**
     * Sprawdza, czy jeden zbior jest podzbiorem drugiego.
     *
     * @param a zbior, ktory ma byc podzbiorem
     * @param b zbior, ktory ma byc nadzbiorem
     * @return <b>true</b> je�li a jest podzbiorem b, <b>false</b> w przeciwnym przypadku
     */
    private static boolean isSubset(int[] a, int[] b) {
        for (int i = 0; i < a.length; ++i) if (a[i] == 1 && b[i] == 0) return false;
        return true;
    }

    /**
     * Przeksztalca baze niezmiennikow, tak, aby byly one dodatnie.
     *
     * @param BB baza niezmiennikow
     */
    private static Matrix getPositive(Matrix BB) {
        int rows = BB.getRowDimension(), cols = BB.getColumnDimension();
        Vector<int[]> S = new Vector<int[]>();
        Vector<int[]> B = new Vector<int[]>();
        for (int i = 0; i < rows; ++i) {
            B.add(BB.getRow(i));
            int t[] = new int[cols];
            t = getSupport(BB.getRow(i));
            S.add(t);
        }
        int j = 0;
        while (j < cols) {
            int i = 0;
            while (i < B.size()) {
                if (B.get(i)[j] < 0) {
                    int k = 0;
                    while (k < B.size()) {
                        if (B.get(k)[j] > 0) {
                            int tmp[] = new int[cols];
                            int ns[] = new int[cols];
                            linearCombination(B.get(k), B.get(i), tmp, B.get(i)[j], -B.get(k)[j]);
                            ns = getSupport(tmp);
                            boolean exist = false;
                            for (int m = 0; m < B.size() && !exist; ++m) if (isSubset(S.get(m), ns)) exist = true;
                            if (!exist) {
                                reduceByGcd(tmp);
                                S.add(ns);
                                B.add(tmp);
                            }
                        }
                        ++k;
                    }
                }
                ++i;
            }
            Vector<int[]> NS = new Vector<int[]>();
            Vector<int[]> NB = new Vector<int[]>();
            for (i = 0; i < B.size(); ++i) {
                if (B.get(i)[j] >= 0) {
                    NS.add(S.get(i));
                    NB.add(B.get(i));
                }
            }
            S = NS;
            B = NB;
            ++j;
        }
        Matrix basis = new Matrix(B.size(), cols);
        for (int i = 0; i < B.size(); ++i) basis.setRow(i, B.get(i));
        return basis;
    }
}
