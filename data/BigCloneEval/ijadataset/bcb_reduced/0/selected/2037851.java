package org.fudaa.ebli.mathematiques;

import org.fudaa.ctulu.CtuluLibString;

/**
 * Une matrice.
 *
 * @version $Id: Matrice.java,v 1.8 2006-09-19 14:55:56 deniger Exp $
 * @author Guillaume Desnoix
 */
public class Matrice {

    private final double[][] a_;

    public Matrice(final int _ni, final int _nj) {
        a_ = new double[_ni][_nj];
    }

    public int ni() {
        return a_.length;
    }

    public int nj() {
        return a_[0].length;
    }

    public double a(final int _i, final int _j) {
        return a_[_i][_j];
    }

    public void a(final int _i, final int _j, final double _ia) {
        a_[_i][_j] = _ia;
    }

    public Matrice colonne(final int _c) {
        final int nj = nj();
        final Matrice r = new Matrice(1, nj);
        for (int j = 0; j < nj; j++) {
            r.a_[0][j] = a_[_c][j];
        }
        return r;
    }

    public Matrice ligne(final int _c) {
        final int ni = ni();
        final Matrice r = new Matrice(ni, 1);
        for (int i = 0; i < ni; i++) {
            r.a_[i][0] = a_[i][_c];
        }
        return r;
    }

    public String toString() {
        final int ni = ni();
        final int nj = nj();
        return "Matrice(" + ni + CtuluLibString.VIR + nj + ")";
    }

    public Matrice transposee() {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(nj, ni);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[j][i] = a_[i][j];
            }
        }
        return r;
    }

    public Matrice extraction(final int _i1, final int _j1, final int _i2, final int _j2) {
        final Matrice r = new Matrice(_i2 - _i1 + 1, _j2 - _j1 + 1);
        for (int i = _i1; i <= _i2; i++) {
            for (int j = _j1; j <= _j2; j++) {
                r.a_[i - _i1][j - _j1] = a_[i][j];
            }
        }
        return r;
    }

    public Matrice remplacement(final Matrice _m, final int _i0, final int _j0) {
        final int ni = _m.ni();
        final int nj = _m.nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = a_[i][j];
            }
        }
        for (int i = _i0; i < _i0 + ni; i++) {
            for (int j = _j0; j < _j0 + nj; j++) {
                r.a_[i][j] = _m.a_[i][j];
            }
        }
        return r;
    }

    public Matrice ajoutColonnes(final Matrice _m) {
        final int ni = ni() + _m.ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = (i < a_.length ? a_[i][j] : _m.a_[i - a_.length][j]);
            }
        }
        return r;
    }

    public Matrice ajoutLignes(final Matrice _m) {
        final int ni = ni();
        final int nj = nj() + _m.nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = (j < nj() ? a_[i][j] : _m.a_[i][j - nj()]);
            }
        }
        return r;
    }

    public Matrice addition(final Matrice _m) {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = a_[i][j] + _m.a_[i][j];
            }
        }
        return r;
    }

    public Matrice soustraction(final Matrice _m) {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = a_[i][j] - _m.a_[i][j];
            }
        }
        return r;
    }

    public Matrice multiplication(final double _d) {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = a_[i][j] * _d;
            }
        }
        return r;
    }

    public Matrice multiplication(final Matrice _m) {
        final int ni = ni();
        final int nj = _m.nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                for (int k = 0; k < nj(); k++) {
                    r.a_[i][j] += a_[i][k] * _m.a_[k][j];
                }
            }
        }
        return r;
    }

    public Matrice division(final double _d) {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = a_[i][j] / _d;
            }
        }
        return r;
    }

    double norme() {
        final Matrice m = multiplication(this.transposee());
        final int ni = m.ni();
        final int nj = m.nj();
        double r = 0.;
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r += m.a_[i][j];
            }
        }
        return r;
    }

    public Matrice constante(final double _d) {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = _d;
            }
        }
        return r;
    }

    public Matrice nulle() {
        return constante(0.);
    }

    public Matrice identite() {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = (i == j ? 1. : 0.);
            }
        }
        return r;
    }

    public Matrice triangulaire() {
        final int ni = ni();
        final int nj = nj();
        final Matrice r = new Matrice(ni, nj);
        for (int i = 0; i < ni; i++) {
            for (int j = 0; j < nj; j++) {
                r.a_[i][j] = (i <= j ? 1. : 0.);
            }
        }
        return r;
    }
}
