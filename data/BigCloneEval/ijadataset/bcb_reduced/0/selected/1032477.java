package org.fudaa.fudaa.commun;

import org.fudaa.ebli.geometrie.GrBoite;
import org.fudaa.ebli.geometrie.GrElement;
import org.fudaa.ebli.geometrie.GrMaillageElement;
import org.fudaa.ebli.geometrie.GrNoeud;
import org.fudaa.ebli.geometrie.GrPoint;

/**
 * Une classe d'interpolation de points sur un maillage 3D.
 *
 * @version $Id: FudaaInterpolateurMaillage.java,v 1.12 2006-09-19 15:01:55 deniger Exp $
 * @author Bertrand Marchand
 */
public class FudaaInterpolateurMaillage implements Cloneable {

    /** Interpolation avec une m�thode lin�aire. */
    public static final int METHODE_LINEAIRE = 0;

    /** Maillage associ�. */
    private GrMaillageElement maillage_;

    /** M�thode utilis�e. */
    private int methode_;

    /** Extrapolation (Z du noeud le + proche) autoris�e. */
    private boolean extrapole_ = true;

    /** x min du domaine base d'interpolation. */
    private double domMnx_;

    /** x max du domaine base d'interpolation. */
    private double domMxx_;

    /** y min du domaine base d'interpolation. */
    private double domMny_;

    /** y max du domaine base d'interpolation. */
    private double domMxy_;

    /** largeur d'une case d'interpolation. */
    private double lgCase_;

    /** hauteur d'une case d'interpolation. */
    private double htCase_;

    /** nombre de cases suivant x. */
    private int nbCaseX_;

    /** nombre de cases suivant y. */
    private int nbCaseY_;

    /** tableau des cases contenant les elements. */
    private EleChaine[][] cases_;

    /** distance maxi autoris�e d'inclusion d'un point dans l'�l�ment. */
    private double deltaMax_;

    static class EleChaine {

        public EleChaine suivant_;

        public GrElement element_;

        public EleChaine(final GrElement _element) {
            element_ = _element;
            suivant_ = null;
        }
    }

    /**
   * Cr�ation d'un interpolateur avec maillage, methode METHODE_LINEAIRE.
   *
   * @param _maillage Maillage d'interpolation.
   */
    public FudaaInterpolateurMaillage(final GrMaillageElement _maillage) {
        this(_maillage, METHODE_LINEAIRE);
    }

    /**
   * Cr�ation d'un interpolateur sans attributs.
   */
    public FudaaInterpolateurMaillage() {
        this(null, METHODE_LINEAIRE);
    }

    /**
   * Cr�ation d'un interpolateur.
   *
   * @param _maillage Maillage d'interpolation.
   * @param _methode M�thode d'interpolation
   */
    public FudaaInterpolateurMaillage(final GrMaillageElement _maillage, final int _methode) {
        super();
        if (_maillage != null) {
            maillage(_maillage);
        }
        methode(_methode);
        deltaMax_ = 1.0;
    }

    public String toString() {
        return "FudaaInterpolateurMaillage";
    }

    /**
   * Affectation du maillage d'interpolation.
   */
    public void maillage(final GrMaillageElement _maillage) {
        maillage_ = _maillage;
        init();
    }

    /**
   * Retourne le maillage d'interpolation.
   */
    public GrMaillageElement maillage() {
        return maillage_;
    }

    /**
   * Affectation de la m�thode d'interpolation.
   *
   * @param _methode M�thode utilis�e pour l'interpolation. Seule la m�thode METHODE_LINEAIRE est autoris�e
   */
    public final void methode(final int _methode) {
        if (_methode != METHODE_LINEAIRE) {
            throw new IllegalArgumentException("Seule la m�thode METHODE_LINEAIRE est autoris�e");
        }
        methode_ = _methode;
    }

    /**
   * @return la m�thode d'interpolation utilis�e.
   */
    public int methode() {
        return methode_;
    }

    /**
   * Extrapolation si le point est trop loin du maillage.
   *
   * @param _b true : Extrapolation (Z du noeud le + proche). false : Le Z du point vaudra Double.NaN.
   */
    public void setZExtrapole(final boolean _b) {
        extrapole_ = _b;
    }

    /**
   * Retourne l'autorisation d'extrapoler le Z d'un point trop loin du maillage.
   *
   * @return true : Extrapolation (Z du noeud le + proche). false : Le Z du point vaudra Double.NaN.
   */
    public boolean isZExtrapole() {
        return extrapole_;
    }

    /**
   * Interpolation d'un point sur le maillage.
   *
   * @param _point Point pour lequel on recherche le Z
   * @return Le point avec le Z interpol�. Un Z=Double.NaN signifie que le point est hors maillage.
   */
    public GrPoint interpolePoint(final GrPoint _point) {
        GrPoint r;
        r = valeur(_point);
        if (r == null) {
            throw new IllegalArgumentException("Le point de coordonn�es x:" + _point.x_ + " y:" + _point.y_ + " est hors maillage");
        }
        return r;
    }

    /**
   * Interpolation de points sur le maillage.
   *
   * @param _points Points pour lesquels on recherche le Z
   * @return Les points avec le Z interpol�. Un Z=Double.NaN signifie que le point est hors maillage.
   */
    public GrPoint[] interpolePoints(final GrPoint[] _points) {
        final GrPoint[] r = new GrPoint[_points.length];
        for (int i = 0; i < _points.length; i++) {
            r[i] = interpolePoint(_points[i]);
        }
        return r;
    }

    private void init() {
        GrNoeud[] nds;
        GrElement[] eles;
        GrNoeud[] ndsEle;
        EleChaine eleChaine;
        double[] xn;
        double[] yn;
        int imnx;
        int imxx;
        int imny;
        int imxy;
        GrBoite bt;
        nds = maillage_.noeuds();
        eles = maillage_.elements();
        if (eles.length <= 0 || nds.length <= 0) {
            throw new IllegalArgumentException("Le maillage ne comporte pas de noeuds ou d'�l�ments");
        }
        for (int i = 0; i < eles.length; i++) {
            if (eles[i].type_ != GrElement.T3) {
                throw new IllegalArgumentException("Le maillage doit �tre constitu� d'�l�ments T3");
            }
        }
        domMnx_ = Double.MAX_VALUE;
        domMxx_ = -Double.MAX_VALUE;
        domMny_ = Double.MAX_VALUE;
        domMxy_ = -Double.MAX_VALUE;
        for (int i = 0; i < nds.length; i++) {
            final GrPoint pt = nds[i].point_;
            domMnx_ = Math.min(domMnx_, pt.x_);
            domMxx_ = Math.max(domMxx_, pt.x_);
            domMny_ = Math.min(domMny_, pt.y_);
            domMxy_ = Math.max(domMxy_, pt.y_);
        }
        final double deltaw = (domMxx_ - domMnx_) / 100.;
        final double deltah = (domMxy_ - domMny_) / 100.;
        domMnx_ -= deltaw;
        domMxx_ += deltaw;
        domMny_ -= deltah;
        domMxy_ += deltah;
        xn = new double[2];
        yn = new double[2];
        for (int i = 0; i < eles.length; i++) {
            ndsEle = eles[i].noeuds_;
            for (int j = 0; j < ndsEle.length; j++) {
                for (int k = 0; k < 2; k++) {
                    final GrPoint pt = ndsEle[(j + k) % 3].point_;
                    xn[k] = pt.x_;
                    yn[k] = pt.y_;
                }
                lgCase_ += Math.sqrt((xn[1] - xn[0]) * (xn[1] - xn[0]) + (yn[1] - yn[0]) * (yn[1] - yn[0]));
            }
        }
        lgCase_ = lgCase_ / (eles.length * 3 * 2);
        if (lgCase_ < 0) {
            throw new IllegalArgumentException("Le maillage est incoh�rent");
        }
        nbCaseX_ = (int) ((domMxx_ - domMnx_) / lgCase_) + 1;
        nbCaseY_ = (int) ((domMxy_ - domMny_) / lgCase_) + 1;
        lgCase_ = (domMxx_ - domMnx_) / nbCaseX_;
        htCase_ = (domMxy_ - domMny_) / nbCaseY_;
        cases_ = new EleChaine[nbCaseX_][];
        for (int i = 0; i < cases_.length; i++) {
            cases_[i] = new EleChaine[nbCaseY_];
        }
        for (int i = 0; i < nbCaseX_; i++) {
            for (int j = 0; j < nbCaseY_; j++) {
                cases_[i][j] = null;
            }
        }
        for (int i = 0; i < eles.length; i++) {
            ndsEle = eles[i].noeuds_;
            bt = new GrBoite();
            for (int j = 0; j < ndsEle.length; j++) {
                bt.ajuste(ndsEle[j].point_);
            }
            imnx = (int) ((bt.o_.x_ - domMnx_) / lgCase_);
            imxx = (int) ((bt.e_.x_ - domMnx_) / lgCase_);
            imny = (int) ((bt.o_.y_ - domMny_) / htCase_);
            imxy = (int) ((bt.e_.y_ - domMny_) / htCase_);
            for (int ix = imnx; ix <= imxx; ix++) {
                for (int iy = imny; iy <= imxy; iy++) {
                    if (cases_[ix][iy] == null) {
                        cases_[ix][iy] = new EleChaine(eles[i]);
                    } else {
                        eleChaine = cases_[ix][iy];
                        while (eleChaine.suivant_ != null) {
                            eleChaine = eleChaine.suivant_;
                        }
                        eleChaine.suivant_ = new EleChaine(eles[i]);
                    }
                }
            }
        }
    }

    private GrPoint valeur(final GrPoint _point) {
        double aa;
        double bb;
        double cc;
        double dd;
        double ee;
        double delta;
        double xpt;
        double ypt;
        double zpt;
        int ix;
        int iy;
        GrNoeud[] ndsEle;
        GrElement eleMin = null;
        int nbIter;
        EleChaine eleChaine;
        double[] xn;
        double[] yn;
        double[] zn;
        double[] dst;
        double dstMin;
        xn = new double[3];
        yn = new double[3];
        zn = new double[3];
        dst = new double[3];
        xpt = _point.x_;
        ypt = _point.y_;
        ix = (int) ((xpt - domMnx_) / lgCase_);
        iy = (int) ((ypt - domMny_) / htCase_);
        if (!(ix >= nbCaseX_ || ix < 0 || iy >= nbCaseY_ || iy < 0) && cases_[ix][iy] != null) {
            eleChaine = cases_[ix][iy];
            while (eleChaine != null) {
                ndsEle = eleChaine.element_.noeuds_;
                for (int i = 0; i < ndsEle.length; i++) {
                    final GrPoint pt = ndsEle[i].point_;
                    xn[i] = pt.x_;
                    yn[i] = pt.y_;
                    zn[i] = pt.z_;
                }
                final double dx21 = xn[1] - xn[0];
                final double dx31 = xn[2] - xn[0];
                final double dy21 = yn[1] - yn[0];
                final double dy31 = yn[2] - yn[0];
                final double a = dx21 * dy31 - dx31 * dy21;
                final double dx01 = xpt - xn[0];
                final double dy01 = ypt - yn[0];
                final double vksi = (dx01 * dy31 - dx31 * dy01) / a;
                final double veta = (dx21 * dy01 - dx01 * dy21) / a;
                if (vksi >= -1.e-5 && vksi <= 1. + 1.e-5) {
                    if (veta >= -1.e-5 && veta <= 1. + 1.e-5) {
                        final double coe1 = 1. - vksi - veta;
                        if (coe1 >= -1.e-5 && coe1 <= 1. + 1.e-5) {
                            zpt = coe1 * zn[0] + vksi * zn[1] + veta * zn[2];
                            return new GrPoint(xpt, ypt, zpt);
                        }
                    }
                }
                eleChaine = eleChaine.suivant_;
            }
            if (deltaMax_ > 1.e-2) {
                nbIter = 11;
            } else {
                nbIter = 1;
            }
            for (int j = 0; j < nbIter; j++) {
                if (j + 1 == nbIter) {
                    delta = deltaMax_;
                } else {
                    delta = j * (deltaMax_ - 1.e-2) / 10. + 1.e-2;
                }
                eleChaine = cases_[ix][iy];
                while (eleChaine != null) {
                    ndsEle = eleChaine.element_.noeuds_;
                    for (int i = 0; i < ndsEle.length; i++) {
                        final GrPoint pt = ndsEle[i].point_;
                        xn[i] = pt.x_;
                        yn[i] = pt.y_;
                        zn[i] = pt.z_;
                    }
                    for (int i = 0; i < 3; i++) {
                        aa = yn[i] - yn[(i + 1) % 3];
                        bb = xn[(i + 1) % 3] - xn[i];
                        cc = xn[i] * yn[(i + 1) % 3] - xn[(i + 1) % 3] * yn[i];
                        dst[i] = (aa * xpt + bb * ypt + cc) / Math.sqrt(aa * aa + bb * bb);
                    }
                    if ((dst[0] > -delta && dst[1] > -delta && dst[2] > -delta) || (dst[0] <= delta && dst[1] <= delta && dst[2] <= delta)) {
                        aa = yn[0] * (zn[1] - zn[2]) + yn[1] * (zn[2] - zn[0]) + yn[2] * (zn[0] - zn[1]);
                        bb = zn[0] * (xn[1] - xn[2]) + zn[1] * (xn[2] - xn[0]) + zn[2] * (xn[0] - xn[1]);
                        dd = xn[0] * (yn[1] - yn[2]) + xn[1] * (yn[2] - yn[0]) + xn[2] * (yn[0] - yn[1]);
                        ee = -(aa * xn[1] + bb * yn[1] + dd * zn[1]);
                        zpt = (-ee - aa * xpt - bb * ypt) / dd;
                        return new GrPoint(xpt, ypt, zpt);
                    }
                    eleChaine = eleChaine.suivant_;
                }
            }
        }
        if (!isZExtrapole()) {
            return new GrPoint(xpt, ypt, Double.NaN);
        }
        dstMin = Double.POSITIVE_INFINITY;
        for (ix = 0; ix < nbCaseX_; ix++) {
            for (iy = 0; iy < nbCaseY_; iy++) {
                eleChaine = cases_[ix][iy];
                while (eleChaine != null) {
                    ndsEle = eleChaine.element_.noeuds_;
                    for (int i = 0; i < ndsEle.length; i++) {
                        final GrPoint pt = ndsEle[i].point_;
                        xn[i] = pt.x_;
                        yn[i] = pt.y_;
                        zn[i] = pt.z_;
                    }
                    for (int i = 0; i < 3; i++) {
                        aa = yn[i] - yn[(i + 1) % 3];
                        bb = xn[(i + 1) % 3] - xn[i];
                        cc = xn[i] * yn[(i + 1) % 3] - xn[(i + 1) % 3] * yn[i];
                        final double d = Math.abs((aa * xpt + bb * ypt + cc) / Math.sqrt(aa * aa + bb * bb));
                        if (d < dstMin) {
                            eleMin = eleChaine.element_;
                            dstMin = d;
                        }
                    }
                    eleChaine = eleChaine.suivant_;
                }
            }
        }
        if (eleMin == null) {
            return new GrPoint(xpt, ypt, Double.NaN);
        }
        ndsEle = eleMin.noeuds_;
        for (int i = 0; i < ndsEle.length; i++) {
            final GrPoint pt = ndsEle[i].point_;
            xn[i] = pt.x_;
            yn[i] = pt.y_;
            zn[i] = pt.z_;
        }
        System.out.println("Distance min : " + dstMin);
        System.out.println("Coordonn�es des noeuds de l'�l�ment + proche :");
        System.out.println("x: " + xn[0] + ", y: " + yn[0] + ", z: " + zn[0]);
        System.out.println("x: " + xn[1] + ", y: " + yn[1] + ", z: " + zn[1]);
        System.out.println("x: " + xn[2] + ", y: " + yn[2] + ", z: " + zn[2]);
        aa = yn[0] * (zn[1] - zn[2]) + yn[1] * (zn[2] - zn[0]) + yn[2] * (zn[0] - zn[1]);
        bb = zn[0] * (xn[1] - xn[2]) + zn[1] * (xn[2] - xn[0]) + zn[2] * (xn[0] - xn[1]);
        dd = xn[0] * (yn[1] - yn[2]) + xn[1] * (yn[2] - yn[0]) + xn[2] * (yn[0] - yn[1]);
        ee = -(aa * xn[1] + bb * yn[1] + dd * zn[1]);
        zpt = (-ee - aa * xpt - bb * ypt) / dd;
        return new GrPoint(xpt, ypt, zpt);
    }
}
