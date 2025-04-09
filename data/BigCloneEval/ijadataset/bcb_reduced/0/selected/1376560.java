package shu.cms;

import shu.cms.colorspace.independ.*;
import shu.math.*;

/**
 * <p>Title: Colour Management System</p>
 *
 * <p>Description: </p>
 * from lcms
 * ��t����
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author cms.shu.edu.tw
 * @version 1.0
 */
public final class DeltaE {

    protected static final double LOGE = 0.434294481;

    protected CIELab _Lab1, _Lab2;

    protected CIEXYZ XYZ1, XYZ2;

    protected double[] deltaE;

    protected double[] CIE2000DeltaLCh;

    protected double[] CIE2000Parameters;

    protected boolean isCIE2000DeltaLCh = false;

    protected String description;

    public static enum Formula {

        CIE94(0), CIE(1), CMC11(2), CMC21(3), BFD(4), CIE2000(5);

        int index;

        Formula(int index) {
            this.index = index;
        }
    }

    public static enum Material {

        Textiles, GraphicArts
    }

    public CIEXYZ getXYZ1() {
        return XYZ1;
    }

    public CIEXYZ getXYZ2() {
        return XYZ2;
    }

    /**
   * ����sumDeltaE�ϥ�
   */
    protected DeltaE() {
        deltaE = new double[Formula.values().length];
    }

    /**
   * �]�t��A�����\��,�Nwhite�A����D65�U,�A�p��Lab
   * @param _XYZ1 CIEXYZ
   * @param _XYZ2 CIEXYZ
   * @param white CIEXYZ
   */
    public DeltaE(final CIEXYZ _XYZ1, final CIEXYZ _XYZ2, final CIEXYZ white) {
        this(_XYZ1, _XYZ2, white, true);
    }

    /**
   * �p��Lab
   * @param _XYZ1 CIEXYZ
   * @param _XYZ2 CIEXYZ
   * @param white CIEXYZ
   * @param adaptedToD65 boolean �O�_�n�@��A����D65�U
   */
    public DeltaE(final CIEXYZ _XYZ1, final CIEXYZ _XYZ2, final CIEXYZ white, boolean adaptedToD65) {
        this(CIELab.fromXYZ(_XYZ1, white), CIELab.fromXYZ(_XYZ2, white), adaptedToD65);
        this.XYZ1 = _XYZ1;
        this.XYZ2 = _XYZ2;
    }

    public double[] getDeltauvPrime() {
        if (XYZ1 == null || XYZ2 == null) {
            throw new IllegalStateException("XYZ1 == null || XYZ2 == null");
        }
        CIExyY xyY1 = new CIExyY(XYZ1);
        CIExyY xyY2 = new CIExyY(XYZ2);
        return xyY1.getDeltauvPrime(xyY2);
    }

    /**
   * �̷�Lab1�MLab2�p���t, �åB��NLab1�MLab2�A����D65�U
   * @param Lab1 CIELab
   * @param Lab2 CIELab
   */
    public DeltaE(final CIELab Lab1, final CIELab Lab2) {
        this(Lab1, Lab2, true);
    }

    public DeltaE(final CIELab Lab1, final CIELab Lab2, boolean adaptedToD65) {
        deltaE = new double[] { -1, -1, -1, -1, -1, -1 };
        this._Lab1 = adaptedToD65 ? Lab1.getLabAdaptedToD65() : Lab1;
        this._Lab2 = adaptedToD65 ? Lab2.getLabAdaptedToD65() : Lab2;
    }

    public static final double getDeltaE(CIELab Lab1, CIELab Lab2, Formula type) {
        switch(type) {
            case BFD:
                return BFDDeltaE(Lab1, Lab2);
            case CIE:
                return CIEDeltaE(Lab1, Lab2);
            case CIE2000:
                return CIE2000DeltaE(Lab1, Lab2);
            case CIE94:
                return CIE94DeltaE(Lab1, Lab2);
            case CMC11:
                return CMC11DeltaE(Lab1, Lab2);
            case CMC21:
                return CMC21DeltaE(Lab1, Lab2);
            default:
                return Double.NaN;
        }
    }

    public final double getDeltaE(Formula type) {
        switch(type) {
            case BFD:
                return getBFDDeltaE();
            case CIE:
                return getCIEDeltaE();
            case CIE2000:
                return getCIE2000DeltaE();
            case CIE94:
                return getCIE94DeltaE();
            case CMC11:
                return getCMC11DeltaE();
            case CMC21:
                return getCMC21DeltaE();
            default:
                return Double.NaN;
        }
    }

    private static boolean MeasuredInCIE2000DeltaE = true;

    public static final void setMeasuredInCIE2000DeltaE(boolean measuredInCIE2000DeltaE) {
        MeasuredInCIE2000DeltaE = measuredInCIE2000DeltaE;
    }

    public static final String getMeasuredDeltaEDescription() {
        if (MeasuredInCIE2000DeltaE) {
            return "CIEDeltaE2000";
        } else {
            return "CIEDeltaE";
        }
    }

    public final double getMeasuredDeltaE() {
        if (MeasuredInCIE2000DeltaE) {
            return getCIE2000DeltaE();
        } else {
            return getCIEDeltaE();
        }
    }

    public final double getBFDDeltaE() {
        if (deltaE[Formula.BFD.index] == -1) {
            deltaE[Formula.BFD.index] = BFDDeltaE(_Lab1, _Lab2);
        }
        return deltaE[Formula.BFD.index];
    }

    public final double getCIE94DeltaE() {
        if (deltaE[Formula.CIE94.index] == -1) {
            deltaE[Formula.CIE94.index] = CIE94DeltaE(_Lab1, _Lab2);
        }
        return deltaE[Formula.CIE94.index];
    }

    public final double getCIE2000DeltaE(double Kl, double Kc, double Kh) {
        return CIE2000DeltaE(_Lab1, _Lab2, Kl, Kc, Kh);
    }

    public final double getCIE2000DeltaE() {
        if (deltaE[Formula.CIE2000.index] == -1) {
            deltaE[Formula.CIE2000.index] = CIE2000DeltaE(_Lab1, _Lab2);
            CIE2000DeltaLCh = new double[3];
            System.arraycopy(_CIE2000DeltaLCH, 0, CIE2000DeltaLCh, 0, 3);
            CIE2000Parameters = new double[10];
            System.arraycopy(_CIE2000Parameters, 0, CIE2000Parameters, 0, 10);
        }
        return deltaE[Formula.CIE2000.index];
    }

    /**
   * ��oCIEDE2000��׮t
   * @return double
   */
    public final double getCIE2000DeltaL() {
        getCIE2000DeltaE();
        double dL = CIE2000Parameters[0];
        double Sl = CIE2000Parameters[3];
        double Kl = CIE2000Parameters[7];
        double deltaL = Math.sqrt(Maths.sqr(dL / (Sl * Kl)));
        return deltaL;
    }

    /**
   * ��oCIEDE2000���Ŭ���t
   * @return double
   */
    public final double getCIE2000Deltaab() {
        getCIE2000DeltaE();
        double dC = CIE2000Parameters[1];
        double dh = CIE2000Parameters[2];
        double Sc = CIE2000Parameters[4];
        double Sh = CIE2000Parameters[5];
        double Rt = CIE2000Parameters[6];
        double Kc = CIE2000Parameters[8];
        double Kh = CIE2000Parameters[9];
        double deltaab = Math.sqrt(Maths.sqr(dC / (Sc * Kc)) + Maths.sqr(dh / (Sh * Kh)) + Rt * (dC / (Sc * Kc)) * (dh / (Sh * Kh)));
        return deltaab;
    }

    public final double[] getCIE2000DeltaLCh() {
        getCIE2000DeltaE();
        return CIE2000DeltaLCh;
    }

    public final double getCIEDeltaE() {
        if (deltaE[Formula.CIE.index] == -1) {
            deltaE[Formula.CIE.index] = CIEDeltaE(_Lab1, _Lab2);
        }
        return deltaE[Formula.CIE.index];
    }

    public final double getCMC11DeltaE() {
        if (deltaE[Formula.CMC11.index] == -1) {
            deltaE[Formula.CMC11.index] = CMC11DeltaE(_Lab1, _Lab2);
        }
        return deltaE[Formula.CMC11.index];
    }

    public final double getCMC21DeltaE() {
        if (deltaE[Formula.CMC21.index] == -1) {
            deltaE[Formula.CMC21.index] = CMC21DeltaE(_Lab1, _Lab2);
        }
        return deltaE[Formula.CMC21.index];
    }

    /**
   * BFD deltaE�p��ϥ�
   * @param Lab CIELab
   * @return double
   */
    protected static double computeLBFD(CIELab Lab) {
        double yt;
        if (Lab.L > 7.996969) {
            yt = (Maths.sqr((Lab.L + 16) / 116) * ((Lab.L + 16) / 116)) * 100;
        } else {
            yt = 100 * (Lab.L / 903.3);
        }
        return (54.6 * (LOGE * (Math.log(yt + 1.5))) - 9.6);
    }

    public static final double BFDDeltaE(final CIELab Lab1, final CIELab Lab2) {
        double lbfd1, lbfd2, AveC, Aveh, dE, deltaL, deltaC, deltah, dc, t, g, dh, rh, rc, rt, bfd;
        CIELCh LCh1, LCh2;
        if (Lab1.L == 0 && Lab2.L == 0) {
            return 0;
        }
        lbfd1 = computeLBFD(Lab1);
        lbfd2 = computeLBFD(Lab2);
        deltaL = lbfd2 - lbfd1;
        LCh1 = new CIELCh(Lab1);
        LCh2 = new CIELCh(Lab2);
        deltaC = LCh2.C - LCh1.C;
        AveC = (LCh1.C + LCh2.C) / 2;
        Aveh = (LCh1.h + LCh2.h) / 2;
        dE = CIEDeltaE(Lab1, Lab2);
        if (Maths.sqr(dE) > (Maths.sqr(Lab2.L - Lab1.L) + Maths.sqr(deltaC))) {
            deltah = Math.sqrt(Maths.sqr(dE) - Maths.sqr(Lab2.L - Lab1.L) - Maths.sqr(deltaC));
        } else {
            deltah = 0;
        }
        dc = 0.035 * AveC / (1 + 0.00365 * AveC) + 0.521;
        g = Math.sqrt(Maths.sqr(Maths.sqr(AveC)) / (Maths.sqr(Maths.sqr(AveC)) + 14000));
        t = 0.627 + (0.055 * Math.cos((Aveh - 254) / (180 / Math.PI)) - 0.040 * Math.cos((2 * Aveh - 136) / (180 / Math.PI)) + 0.070 * Math.cos((3 * Aveh - 31) / (180 / Math.PI)) + 0.049 * Math.cos((4 * Aveh + 114) / (180 / Math.PI)) - 0.015 * Math.cos((5 * Aveh - 103) / (180 / Math.PI)));
        dh = dc * (g * t + 1 - g);
        rh = -0.260 * Math.cos((Aveh - 308) / (180 / Math.PI)) - 0.379 * Math.cos((2 * Aveh - 160) / (180 / Math.PI)) - 0.636 * Math.cos((3 * Aveh + 254) / (180 / Math.PI)) + 0.226 * Math.cos((4 * Aveh + 140) / (180 / Math.PI)) - 0.194 * Math.cos((5 * Aveh + 280) / (180 / Math.PI));
        rc = Math.sqrt((AveC * AveC * AveC * AveC * AveC * AveC) / ((AveC * AveC * AveC * AveC * AveC * AveC) + 70000000));
        rt = rh * rc;
        bfd = Math.sqrt(Maths.sqr(deltaL) + Maths.sqr(deltaC / dc) + Maths.sqr(deltah / dh) + (rt * (deltaC / dc) * (deltah / dh)));
        return bfd;
    }

    public static final double CIEDeltaEuv(final CIEXYZ _XYZ1, final CIEXYZ _XYZ2, final CIEXYZ white) {
        return CIEDeltaEuv(_XYZ1, _XYZ2, white, true);
    }

    public static final double CIEDeltaEuv(final CIEXYZ _XYZ1, final CIEXYZ _XYZ2, final CIEXYZ white, boolean adaptedToD65) {
        CIELuv Luv1 = new CIELuv(_XYZ1, white);
        CIELuv Luv2 = new CIELuv(_XYZ2, white);
        return CIEDeltaE(Luv1, Luv2, adaptedToD65);
    }

    public static final double CIEDeltaE(final CIELuv Luv1, final CIELuv Luv2) {
        return CIEDeltaE(Luv1, Luv2, true);
    }

    public static final double CIEDeltaE(final CIELuv Luv1, final CIELuv Luv2, boolean adaptedToD65) {
        CIELuv _Luv1 = adaptedToD65 ? Luv1.getLuvAdaptedToD65() : Luv1;
        CIELuv _Luv2 = adaptedToD65 ? Luv2.getLuvAdaptedToD65() : Luv2;
        CIELab Lab1 = new CIELab(_Luv1.getValues(), _Luv1.getWhite());
        CIELab Lab2 = new CIELab(_Luv2.getValues(), _Luv2.getWhite());
        return CIEDeltaE(Lab1, Lab2);
    }

    public static final double CIEDeltaE(final CIELab Lab1, final CIELab Lab2) {
        double dL, da, db;
        if (Lab1.L == 0 && Lab2.L == 0) {
            return 0;
        }
        dL = Math.abs(Lab1.L - Lab2.L);
        da = Math.abs(Lab1.a - Lab2.a);
        db = Math.abs(Lab1.b - Lab2.b);
        return Math.pow(dL * dL + da * da + db * db, 0.5);
    }

    /**
   * CIE2000��t����(�ĥιw�]�ȹB��)
   * @param Lab1 CIELab
   * @param Lab2 CIELab
   * @return double
   */
    public static final double CIE2000DeltaE(final CIELab Lab1, final CIELab Lab2) {
        return CIE2000DeltaE(Lab1, Lab2, 1, 1, 1);
    }

    private static final double[] _CIE2000DeltaLCH = new double[3];

    /**
   * �p���t�Ϊ��Ѽ� {dL, dC, dh, Sl, Sc, Sh, Rt, Kl, Kc, Kh}
   */
    private static final double[] _CIE2000Parameters = new double[10];

    /**
   * CIE2000��t����
   * @param Lab1 CIELab
   * @param Lab2 CIELab
   * @param Kl double
   * @param Kc double
   * @param Kh double
   * @return double
   */
    public static final double CIE2000DeltaE(final CIELab Lab1, final CIELab Lab2, double Kl, double Kc, double Kh) {
        calculateCIE2000Parameters(Lab1, Lab2, Kc, Kc, Kh);
        double dL = _CIE2000Parameters[0];
        double dC = _CIE2000Parameters[1];
        double dh = _CIE2000Parameters[2];
        double Sl = _CIE2000Parameters[3];
        double Sc = _CIE2000Parameters[4];
        double Sh = _CIE2000Parameters[5];
        double Rt = _CIE2000Parameters[6];
        _CIE2000DeltaLCH[0] = dL;
        _CIE2000DeltaLCH[1] = dC;
        _CIE2000DeltaLCH[2] = dh;
        double deltaE00 = Math.sqrt(Maths.sqr(dL / (Sl * Kl)) + Maths.sqr(dC / (Sc * Kc)) + Maths.sqr(dh / (Sh * Kh)) + Rt * (dC / (Sc * Kc)) * (dh / (Sh * Kh)));
        return deltaE00;
    }

    /**
   * �p��CIEDE2000�һݭn���Ѽ�
   * @param Lab1 CIELab
   * @param Lab2 CIELab
   * @param Kl double
   * @param Kc double
   * @param Kh double
   */
    protected static final void calculateCIE2000Parameters(final CIELab Lab1, final CIELab Lab2, double Kl, double Kc, double Kh) {
        double L1 = Lab1.L;
        double a1 = Lab1.a;
        double b1 = Lab1.b;
        double C = Math.sqrt(Maths.sqr(a1) + Maths.sqr(b1));
        double Ls = Lab2.L;
        double as = Lab2.a;
        double bs = Lab2.b;
        double Cs = Math.sqrt(Maths.sqr(as) + Maths.sqr(bs));
        double G = 0.5 * (1 - Math.sqrt(Math.pow((C + Cs) / 2, 7) / (Math.pow((C + Cs) / 2, 7) + Math.pow(25, 7))));
        double a_p = (1 + G) * a1;
        double b_p = b1;
        double C_p = Math.sqrt(Maths.sqr(a_p) + Maths.sqr(b_p));
        double h_p = atan2deg(a_p, b_p);
        double a_ps = (1 + G) * as;
        double b_ps = bs;
        double C_ps = Math.sqrt(Maths.sqr(a_ps) + Maths.sqr(b_ps));
        double h_ps = atan2deg(a_ps, b_ps);
        double meanC_p = (C_p + C_ps) / 2;
        double meanh_p = Math.abs(h_ps - h_p) <= 180 ? (h_ps + h_p) / 2 : (h_ps + h_p - 360) / 2;
        double delta_h = Math.abs(h_p - h_ps) <= 180 ? Math.abs(h_p - h_ps) : 360 - Math.abs(h_p - h_ps);
        double delta_L = Math.abs(L1 - Ls);
        double delta_C = Math.abs(C_p - C_ps);
        double delta_H = 2 * Math.sqrt(C_ps * C_p) * Math.sin(RADIANES(delta_h) / 2);
        double T = 1 - 0.17 * Math.cos(RADIANES(meanh_p - 30)) + 0.24 * Math.cos(RADIANES(2 * meanh_p)) + 0.32 * Math.cos(RADIANES(3 * meanh_p + 6)) - 0.2 * Math.cos(RADIANES(4 * meanh_p - 63));
        double Sh = 1 + 0.015 * ((C_ps + C_p) / 2) * T;
        double Sl = 1 + (0.015 * Maths.sqr((Ls + L1) / 2 - 50)) / Math.sqrt(20 + Maths.sqr((Ls + L1) / 2 - 50));
        double Sc = 1 + 0.045 * (C_p + C_ps) / 2;
        double delta_ro = 30 * Math.exp(-Maths.sqr(((meanh_p - 275) / 25)));
        double Rc = 2 * Math.sqrt((Math.pow(meanC_p, 7)) / (Math.pow(meanC_p, 7) + Math.pow(25, 7)));
        double Rt = -Math.sin(2 * RADIANES(delta_ro)) * Rc;
        _CIE2000Parameters[0] = delta_L;
        _CIE2000Parameters[1] = delta_C;
        _CIE2000Parameters[2] = delta_H;
        _CIE2000Parameters[3] = Sl;
        _CIE2000Parameters[4] = Sc;
        _CIE2000Parameters[5] = Sh;
        _CIE2000Parameters[6] = Rt;
        _CIE2000Parameters[7] = Kl;
        _CIE2000Parameters[8] = Kc;
        _CIE2000Parameters[9] = Kh;
    }

    /**
   * CIE2000DeltaE�p��ϥ�
   * @param deg double
   * @return double
   */
    protected static final double RADIANES(double deg) {
        return (deg * Math.PI) / 180.;
    }

    /**
   * CIE2000DeltaE�p��ϥ�
   * @param b double
   * @param a double
   * @return double
   */
    protected static final double atan2deg(double b, double a) {
        double h;
        if (a == 0 && b == 0) {
            h = 0;
        } else {
            h = Math.atan2(a, b);
        }
        h *= (180. / Math.PI);
        while (h > 360.) {
            h -= 360.;
        }
        while (h < 0) {
            h += 360.;
        }
        return h;
    }

    public static final double CIE94DeltaE(final CIELab Lab1, final CIELab Lab2, Material material) {
        double KL = 0, KC = 1, KH = 1, K1 = 0, K2 = 0;
        switch(material) {
            case GraphicArts:
                KL = 1;
                K1 = 0.045;
                K2 = 0.015;
                break;
            case Textiles:
                KL = 2;
                K1 = 0.048;
                K2 = 0.014;
                break;
        }
        double da = Lab1.a - Lab2.a;
        double db = Lab1.b - Lab2.b;
        CIELCh LCh1 = new CIELCh(Lab1);
        CIELCh LCh2 = new CIELCh(Lab2);
        double dL = LCh1.L - LCh2.L;
        double dC = LCh1.C - LCh2.C;
        double abc = Maths.sqr(da) + Maths.sqr(db) - Maths.sqr(dC);
        abc = abc < 0 ? 0 : abc;
        double dH = Maths.sqrt(abc);
        double SL = 1;
        double SC = 1 + K1 * LCh1.C;
        double SH = 1 + K2 * LCh1.C;
        double deltaE = Maths.sqrt(Maths.sqr(dL / (KL * SL)) + Maths.sqr(dC / (KC * SC)) + Maths.sqr(dH / (KH * SH)));
        return deltaE;
    }

    public static final double CIE94DeltaE(final CIELab Lab1, final CIELab Lab2) {
        return CIE94DeltaE(Lab1, Lab2, Material.GraphicArts);
    }

    public static final double CMC11DeltaE(final CIELab Lab1, final CIELab Lab2) {
        return CMCDeltaE(Lab1, Lab2, 1, 1);
    }

    public static final double CMC21DeltaE(final CIELab Lab1, final CIELab Lab2) {
        return CMCDeltaE(Lab1, Lab2, 2, 1);
    }

    protected static final double CMCDeltaE(CIELab Lab1, CIELab Lab2, double l, double c) {
        double dE, dL, dC, dh, sl, sc, sh, t, f, cmc;
        CIELCh LCh1, LCh2;
        if (Lab1.L == 0 && Lab2.L == 0) {
            return 0;
        }
        LCh1 = new CIELCh(Lab1);
        LCh2 = new CIELCh(Lab2);
        dL = Lab2.L - Lab1.L;
        dC = LCh2.C - LCh1.C;
        dE = CIEDeltaE(Lab1, Lab2);
        if (Maths.sqr(dE) > (Maths.sqr(dL) + Maths.sqr(dC))) {
            dh = Math.sqrt(Maths.sqr(dE) - Maths.sqr(dL) - Maths.sqr(dC));
        } else {
            dh = 0;
        }
        if ((LCh1.h > 164) && (LCh1.h < 345)) {
            t = 0.56 + Math.abs(0.2 * Math.cos(((LCh1.h + 168) / (180 / Math.PI))));
        } else {
            t = 0.36 + Math.abs(0.4 * Math.cos(((LCh1.h + 35) / (180 / Math.PI))));
        }
        sc = 0.0638 * LCh1.C / (1 + 0.0131 * LCh1.C) + 0.638;
        sl = 0.040975 * Lab1.L / (1 + 0.01765 * Lab1.L);
        if (Lab1.L < 16) {
            sl = 0.511;
        }
        f = Math.sqrt((LCh1.C * LCh1.C * LCh1.C * LCh1.C) / ((LCh1.C * LCh1.C * LCh1.C * LCh1.C) + 1900));
        sh = sc * (t * f + 1 - f);
        cmc = Math.sqrt(Maths.sqr(dL / (l * sl)) + Maths.sqr(dC / (c * sc)) + Maths.sqr(dh / sh));
        return cmc;
    }

    /**
   * getMeanDeltaE�p��ϥ�
   * @param divisor int
   */
    protected final void divideDeltaE(int divisor) {
        for (int x = 0; x < deltaE.length; x++) {
            deltaE[x] /= divisor;
        }
    }

    /**
   * �p��Ҧ���deltaE
   */
    protected final void calculateAllDeltaE() {
        double de = getBFDDeltaE();
        assert !Double.isNaN(de);
        de = getCIE94DeltaE();
        assert !Double.isNaN(de);
        de = getCIE2000DeltaE();
        assert !Double.isNaN(de);
        de = getCIEDeltaE();
        assert !Double.isNaN(de);
        de = getCMC11DeltaE();
        assert !Double.isNaN(de);
        de = getCMC21DeltaE();
        assert !Double.isNaN(de);
    }

    /**
   * Returns a string representation of the object.
   *
   * @return a string representation of the object.
   */
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (this.isCIE2000DeltaLCh) {
            buf.append("L:" + CIE2000DeltaLCh[0] + " C:" + CIE2000DeltaLCh[1] + " h:" + CIE2000DeltaLCh[2]);
        } else {
            buf.append("CIE:" + deltaE[Formula.CIE.index] + " 94:" + deltaE[Formula.CIE94.index] + " 2k:" + deltaE[Formula.CIE2000.index] + " CMC11:" + deltaE[Formula.CMC11.index] + " CMC21:" + deltaE[Formula.CMC21.index] + " BFD:" + deltaE[Formula.BFD.index]);
        }
        return buf.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static void main(String[] args) {
        CIELab a = new CIELab(50, 3, 10);
        CIELab b = new CIELab(51, 4, 12);
        DeltaE de = new DeltaE(a, b);
        System.out.println(de.getCIE2000DeltaE());
        System.out.println(de.getCIE2000Deltaab());
    }
}
