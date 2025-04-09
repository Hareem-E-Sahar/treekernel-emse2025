package dynamics.branch;

import java.io.File;
import java.util.Random;

/**
 * @author umezawa
 *
 * ���̐������ꂽ�R�����g�̑}����e���v���[�g��ύX���邽��
 * �E�B���h�E > �ݒ� > Java > �R�[�h���� > �R�[�h�ƃR�����g
 */
public class Shoot {

    private double[][] ellipsoid;

    private double l;

    private double w;

    private double d;

    private double density;

    private double[] shootbase;

    private double[] shoottip;

    private double[] shootmid;

    private double sla;

    private double leafmass;

    private double leafarea;

    private double diameter;

    private double woodymass;

    private double totalmass;

    private double elevation;

    private double aspect;

    private int indid;

    private int shootid;

    private int shootbirthyear;

    private int shootage;

    private double[][] lightpoint;

    private double predlight;

    public static double[][] ldirection;

    public static int totallightLinenum;

    static double coeff;

    private boolean alive;

    public static double residue;

    private static Random rg = new Random();

    private Shoot first;

    private Shoot next;

    private Individual individual;

    public Shoot(int year, int indid, int shootid, double base2, double[] tip) {
        ellipsoid = new double[3][3];
        shootbase = new double[3];
        shootmid = new double[3];
        lightpoint = new double[9][3];
        ldirection = new double[900][3];
        shootbirthyear = year;
        shootage = 0;
        this.indid = indid;
        this.shootid = shootid;
        shoottip = tip;
        shootbase[2] = base2;
        predlight = -1.0;
        this.alive = true;
    }

    public void setShootParameters(double[] tmpindbase, double[] tmpindtip) {
        double[] x = new double[3];
        shoottip[0] += tmpindbase[0];
        shoottip[1] += tmpindbase[1];
        shootbase[0] = tmpindbase[0];
        shootbase[1] = tmpindbase[1];
        for (int i = 0; i < 3; ++i) {
            shootmid[i] = (shoottip[i] + shootbase[i]) / 2;
            x[i] = shoottip[i] - shootbase[i];
        }
        l = Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
        d = 0.300191 * l;
        if (shootbase[2] > tmpindtip[2]) {
            w = Math.exp(-0.061675019) * Math.pow(l, 0.815829552);
        } else {
            w = Math.exp(-0.061675019 - 1.531814058) * Math.pow(l, 0.815829552 + 0.370907952);
        }
        double lxy = Math.sqrt(x[0] * x[0] + x[1] * x[1]);
        if (lxy != 0.0) {
            this.elevation = Math.atan(x[2] / lxy);
            if (x[1] > 0) {
                this.aspect = Math.acos(x[0] / lxy);
            } else {
                this.aspect = 2.0 * Math.PI - Math.acos(x[0] / lxy);
            }
        } else {
            this.elevation = Math.PI / 2.0;
            this.aspect = 0.0;
        }
        calcMass();
        for (int i = 0; i < 3; ++i) {
            x[i] = x[i] / l;
        }
        calcEllpsoid(x);
        return;
    }

    /**
	 * ���z�ʒu�̐ݒ�
	 * @return
	 */
    public void calcLightpoint() {
        double ll, mm;
        double[] w1 = new double[3];
        double[] w2 = new double[3];
        for (int i = 0; i < 3; ++i) {
            lightpoint[0][i] = shootmid[i];
            lightpoint[1][i] = (shootmid[i] + 3 * shoottip[i]) / 4;
            lightpoint[2][i] = (shootmid[i] + 3.0 * shootbase[i]) / 4;
        }
        ll = shoottip[0] - shootbase[0];
        mm = shoottip[1] - shootbase[1];
        w1[0] = shootmid[0] + mm * w / (2.0 * Math.sqrt(mm * mm + ll * ll));
        w1[1] = shootmid[1] + ll * w / (2.0 * Math.sqrt(mm * mm + ll * ll));
        w1[2] = shootmid[2];
        w2[0] = shootmid[0] - mm * w / (2.0 * Math.sqrt(mm * mm + ll * ll));
        w2[1] = shootmid[1] - ll * 2 / (2.0 * Math.sqrt(mm * mm + ll * ll));
        w2[2] = shootmid[2];
        for (int i = 0; i < 3; ++i) {
            lightpoint[3][i] = (shootmid[i] + 3 * w1[i]) / 4;
            lightpoint[4][i] = (shootmid[i] + 3 * w2[i]) / 4;
            lightpoint[5][i] = (lightpoint[1][i] + lightpoint[3][i]) / 2;
            lightpoint[6][i] = (lightpoint[1][i] + lightpoint[4][i]) / 2;
            lightpoint[7][i] = (lightpoint[2][i] + lightpoint[3][i]) / 2;
            lightpoint[8][i] = (lightpoint[2][i] + lightpoint[4][i]) / 2;
        }
        return;
    }

    /**
	 * �v�Z������
	 * 
	 * @return
	 */
    public double calcInterceptLenForShoot(double l, double m, double n, double[] coord, Flag f) {
        double x, y, z, a, b, c, node1x, node1y, node1z, node2x, node2y, node2z;
        double k1, k2, discriminant, interceptlen;
        x = coord[0] - shootmid[0];
        y = coord[1] - shootmid[1];
        z = coord[2] - shootmid[2];
        a = ellipsoid[0][0] * l * l + ellipsoid[1][1] * m * m + ellipsoid[2][2] * n * n + 2.0 * (ellipsoid[0][1] * m * l + ellipsoid[0][2] * l * n + ellipsoid[1][2] * m * n);
        b = 2.0 * (ellipsoid[0][0] * l * x + ellipsoid[1][1] * m * y + ellipsoid[2][2] * n * z + ellipsoid[0][1] * m * x + ellipsoid[0][1] * l * y + ellipsoid[0][2] * l * z + ellipsoid[0][2] * n * x + ellipsoid[1][2] * n * y + ellipsoid[1][2] * m * z);
        c = ellipsoid[0][0] * x * x + ellipsoid[1][1] * y * y + ellipsoid[2][2] * z * z + 2.0 * (ellipsoid[0][1] * x * y + ellipsoid[0][2] * x * z + ellipsoid[1][2] * y * z) - 1;
        if ((discriminant = b * b - 4 * a * c) > 0) {
            k1 = (-b + Math.sqrt(discriminant)) / (2 * a);
            k2 = (-b - Math.sqrt(discriminant)) / (2 * a);
            node1x = l * k1 + x;
            node1y = m * k1 + y;
            node1z = n * k1 + z;
            node2x = l * k2 + x;
            node2y = m * k2 + y;
            node2z = n * k2 + z;
            if (node1z >= z) {
                if (node2z >= z) {
                    interceptlen = density * Math.sqrt((l * l + m * m + n * n) * discriminant / (a * a));
                } else {
                    interceptlen = density * Math.sqrt((l * l + m * m + n * n) * k1 * k1);
                    f.setOutside(true);
                }
            } else {
                if (node2z >= z) {
                    interceptlen = density * Math.sqrt((l * l + m * m + n * n) * k2 * k2);
                    f.setOutside(true);
                } else {
                    interceptlen = 0.0;
                }
            }
        } else {
            interceptlen = 0;
        }
        return interceptlen;
    }

    void readLightLineDirectioin(File lightfile) {
        String tmpstr;
        final String SOC = new String("soc");
        final String UOC = new String("uoc");
        final int ANGLENUMMAX = 30;
        int azimuthnum, elevationnum, elevationnummax;
        double lightanglemin, tmp, sinelevationu, sinelevationl, sinelevationm, anglefromvertical;
        double[] azimuth = new double[ANGLENUMMAX];
        double[] elevation = new double[ANGLENUMMAX];
        for (int i = 0; i < azimuthnum; ++i) {
            azimuth[i] = i * 2 * Math.PI / azimuthnum;
        }
        tmp = 1.0 / elevationnum;
        elevationnummax = elevationnum;
        if (tmpstr.compareToIgnoreCase(SOC) != 0 && tmpstr.compareToIgnoreCase(UOC) != 0) {
            System.err.println("error sky model is soc or uoc");
            return;
        }
        if (tmpstr.compareToIgnoreCase(SOC) == 0) {
            sinelevationu = 1.0;
            sinelevationl = (-1.0 + Math.sqrt(1.0 + 4.0 * sinelevationu * sinelevationu + 4.0 * sinelevationu - 8.0 / elevationnum)) / 2.0;
            sinelevationm = (sinelevationu + sinelevationl) / 2.0;
            anglefromvertical = Math.acos(sinelevationm);
            elevation[0] = Math.PI / 2.0 - anglefromvertical;
            for (int i = 1; i < elevationnum; ++i) {
                sinelevationu = sinelevationl;
                sinelevationl = (-1.0 + Math.sqrt(1.0 + 4.0 * sinelevationu * sinelevationu + 4.0 * sinelevationu - 8.0 / elevationnum)) / 2.0;
                sinelevationm = (sinelevationu + sinelevationl) / 2.0;
                anglefromvertical = Math.acos(sinelevationm);
                if (anglefromvertical > Math.PI * (90.0 - lightanglemin) / 180.0) {
                    elevationnummax = i;
                    break;
                } else {
                    elevation[i] = Math.PI / 2.0 - anglefromvertical;
                }
            }
        } else {
            sinelevationu = 1.0;
            sinelevationl = sinelevationu - 1.0 / elevationnum;
            sinelevationm = (sinelevationu + sinelevationl) / 2.0;
            anglefromvertical = Math.acos(sinelevationm);
            elevation[0] = Math.PI / 2.0 - anglefromvertical;
            for (int i = 0; i < elevationnum; ++i) {
                sinelevationu = sinelevationl;
                sinelevationl = sinelevationu - 1.0 / elevationnum;
                sinelevationm = (sinelevationu + sinelevationl) / 2.0;
                anglefromvertical = Math.acos(sinelevationm);
                if (anglefromvertical > Math.PI * (90.0 - lightanglemin) / 180.0) {
                    elevationnummax = i;
                } else {
                    elevation[i] = Math.PI / 2.0 - anglefromvertical;
                }
            }
        }
        setTotallightLinenum(0);
        for (int i = 0; i < azimuthnum; ++i) {
            for (int j = 0; j < elevationnummax; ++j) {
                setLdirection(totallightLinenum, 0, Math.cos(elevation[j]) * Math.cos(azimuth[i]));
                setLdirection(totallightLinenum, 1, Math.cos(elevation[j]) * Math.sin(azimuth[i]));
                setLdirection(totallightLinenum, 2, Math.sin(elevation[j]));
                incTotallightlinenum();
            }
        }
        return;
    }

    void calcLight() {
        double[] tmppred = new double[9];
        double[] coord = new double[3];
        Flag[] tmpflag = new Flag[9];
        Shoot tmpshoot;
        double meanpred;
        for (int i = 0; i < 9; ++i) {
            coord[0] = lightpoint[i][0];
            coord[1] = lightpoint[i][1];
            coord[2] = lightpoint[i][2];
            tmppred[i] = 0.0;
            for (int linenum = 0; linenum < getTotallightLinenum(); ++linenum) {
                double dl = 0.0;
                tmpshoot = getFirst();
                while (tmpshoot.getNext() != null) {
                    if (tmpshoot != this) {
                        dl += tmpshoot.calcInterceptLenForShoot(getLdirection(linenum, 0), getLdirection(linenum, 1), getLdirection(linenum, 2), coord, tmpflag[i]);
                    }
                    tmpshoot = tmpshoot.getNext();
                }
                tmppred[i] += Math.exp(-1.0 * getCoeff() * dl);
            }
            tmppred[i] /= getTotallightLinenum();
            tmppred[i] *= 100;
        }
        meanpred = 0.0;
        for (int i = 0; i < 9; ++i) {
            if (tmpflag[i].isOutside()) {
                meanpred += 1.70 * tmppred[i];
            } else {
                meanpred += tmppred[i];
            }
        }
        predlight = meanpred / 9.0;
        return;
    }

    void determineSurvival(double tmpindheight) {
        double lc;
        double p;
        double h;
        h = shoottip[2] / tmpindheight;
        lc = -6.86808 + 0.0157 * l + predlight + 7.4229 * h;
        p = Math.exp(lc) / (1 + Math.exp(lc));
        if (rg.nextDouble() < p) {
            alive = true;
        } else {
            alive = false;
        }
        return;
    }

    void makeGrowth(double indheight, int grvar) {
        double[] x = new double[3];
        double nl, ne;
        double h;
        h = shoottip[2] / indheight;
        if (grvar == 0) {
            nl = l + 0.28774 * predlight + 26.73134 * h - 23.46305;
        } else {
            nl = l + 0.28774 * predlight + 26.73134 * h - 23.47385 + getRand(0.0, 20.826);
        }
        if (nl < 0) {
            alive = false;
        }
        if (alive) {
            ++shootage;
            if (grvar == 0) {
                ne = elevation - 0.53778 + 0.41740 * h - 0.58672 * elevation + 0.16457 * Math.log(l);
            } else {
                ne = elevation - 0.53778 + 0.41740 * h - 0.58672 * elevation + 0.16457 * Math.log(l) + getRand(0.0, 0.2270);
            }
            elevation = ne;
            shoottip[0] = nl * Math.cos(ne) * Math.cos(aspect) + shootbase[0];
            shoottip[1] = nl * Math.cos(ne) * Math.sin(aspect) + shootbase[1];
            shoottip[2] = nl * Math.sin(ne) + shootbase[2];
            l = nl;
            d = 0.300191 * l;
            if (shootbase[2] > indheight / 2) {
                w = Math.exp(-0.061675019) * Math.pow(l, 0.81589552);
            } else {
                w = Math.exp(-0.061675019 - 1.53184058) * Math.pow(l, 0.81589552 + 0.37090752);
            }
            calcEllpsoid(x);
            calcLightpoint();
        }
        return;
    }

    /**
	 * @param d
	 * @param e
	 * @return
	 */
    private double getRand(double d, double e) {
        double rnor = 0.0;
        for (int i = 0; i < 12; ++i) {
            rnor += rg.nextDouble();
        }
        return (d * (rnor - 6.0) + e);
    }

    public double getLeafMassLight() {
        return leafmass * predlight;
    }

    void calcMass() {
        leafmass = Math.exp(-8.651287) * Math.pow(l, 2.554367);
        leafarea = leafmass * sla;
        density = leafarea / (Math.PI / 6.0 * l * w * d);
        diameter = 0.0073 * l + 0.1391;
        woodymass = Math.exp(-0.847) * Math.pow(l, 1.007);
        totalmass = leafmass + woodymass;
    }

    void calcEllpsoid(double[] x) {
        double[][] p = new double[3][3];
        double[][] a = new double[3][3];
        double[][] tpa = new double[3][3];
        double[][] tp = new double[3][3];
        double sinphi, cosphi, sintheta, costheta, l02;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                a[i][j] = 0;
                tpa[i][j] = 0;
                ellipsoid[i][j] = 0;
            }
        }
        a[0][0] = 1.0 / ((l / 2) * (l / 2));
        a[1][1] = 1.0 / ((w / 2) * (w / 2));
        a[2][2] = 1.0 / ((d / 2) * (d / 2));
        l02 = Math.sqrt(x[0] * x[0] + x[2] * x[2]);
        if (x[0] > 0) {
            sinphi = x[1];
            cosphi = l02;
            sintheta = x[2] / l02;
            costheta = x[0] / l02;
        } else {
            sinphi = x[1];
            cosphi = -1.0 * l02;
            sintheta = -1.0 * x[2] / l02;
            costheta = -1.0 * x[0] / l02;
        }
        p[0][0] = cosphi * costheta;
        p[0][1] = sinphi;
        p[0][2] = cosphi * sintheta;
        p[1][0] = -1.0 * sinphi * costheta;
        p[1][1] = cosphi;
        p[1][2] = -1.0 * sinphi * sintheta;
        p[2][0] = -1.0 * sintheta;
        p[2][1] = 0;
        p[2][2] = costheta;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                tp[i][j] = p[j][i];
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 3; ++k) {
                    tpa[i][j] += tp[i][k] * a[k][j];
                }
            }
        }
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                for (int k = 0; k < 3; ++k) {
                    ellipsoid[i][j] += tpa[i][k] * p[k][j];
                }
            }
        }
        return;
    }

    /**
	 * @param linenum
	 * @param i
	 * @return
	 */
    private double getLdirection(int linenum, int i) {
        return ldirection[linenum][i];
    }

    /**
	 * 
	 */
    private synchronized void incTotallightlinenum() {
        ++totallightLinenum;
        return;
    }

    /**
	 * @param totallightLinenum
	 * @param i
	 * @param d
	 */
    private void setLdirection(int t, int i, double d) {
        ldirection[t][i] = d;
        return;
    }

    /**
	 * @return
	 */
    public static double getCoeff() {
        return coeff;
    }

    /**
	 * @return
	 */
    public static double[][] getLdirection() {
        return ldirection;
    }

    /**
	 * @return
	 */
    public static double getResidue() {
        return residue;
    }

    /**
	 * @return
	 */
    public static int getTotallightLinenum() {
        return totallightLinenum;
    }

    /**
	 * @return
	 */
    public double getAspect() {
        return aspect;
    }

    /**
	 * @return
	 */
    public double getD() {
        return d;
    }

    /**
	 * @return
	 */
    public boolean isAlive() {
        return alive;
    }

    /**
	 * @return
	 */
    public double getDensity() {
        return density;
    }

    /**
	 * @return
	 */
    public double getDiameter() {
        return diameter;
    }

    /**
	 * @return
	 */
    public double getElevation() {
        return elevation;
    }

    /**
	 * @return
	 */
    public double[][] getEllipsoid() {
        return ellipsoid;
    }

    /**
	 * @return
	 */
    public double getL() {
        return l;
    }

    /**
	 * @return
	 */
    public double getLeafarea() {
        return leafarea;
    }

    /**
	 * @return
	 */
    public double getLeafmass() {
        return leafmass;
    }

    /**
	 * @return
	 */
    public double[][] getLightpoint() {
        return lightpoint;
    }

    /**
	 * @return
	 */
    public double getPredlight() {
        return predlight;
    }

    /**
	 * @return
	 */
    public double[] getShootbase() {
        return shootbase;
    }

    /**
	 * @return
	 */
    public double[] getShootmid() {
        return shootmid;
    }

    /**
	 * @return
	 */
    public double[] getShoottip() {
        return shoottip;
    }

    /**
	 * @return
	 */
    public double getSla() {
        return sla;
    }

    /**
	 * @return
	 */
    public double getTotalmass() {
        return totalmass;
    }

    /**
	 * @return
	 */
    public double getW() {
        return w;
    }

    /**
	 * @return
	 */
    public double getWoodymass() {
        return woodymass;
    }

    /**
	 * @param d
	 */
    public static void setCoeff(double d) {
        coeff = d;
    }

    /**
	 * @param ds
	 */
    public static void setLdirection(double[][] ds) {
        ldirection = ds;
    }

    /**
	 * @param d
	 */
    public static void setResidue(double d) {
        residue = d;
    }

    /**
	 * @param i
	 */
    public static void setTotallightLinenum(int i) {
        totallightLinenum = i;
    }

    /**
	 * @param d
	 */
    public void setAspect(double d) {
        aspect = d;
    }

    /**
	 * @param d
	 */
    public void setD(double d) {
        this.d = d;
    }

    /**
	 * @param b
	 */
    public void setAlive(boolean b) {
        alive = b;
    }

    /**
	 * @param d
	 */
    public void setDensity(double d) {
        density = d;
    }

    /**
	 * @param d
	 */
    public void setDiameter(double d) {
        diameter = d;
    }

    /**
	 * @param d
	 */
    public void setElevation(double d) {
        elevation = d;
    }

    /**
	 * @param ds
	 */
    public void setEllipsoid(double[][] ds) {
        ellipsoid = ds;
    }

    /**
	 * @param d
	 */
    public void setL(double d) {
        l = d;
    }

    /**
	 * @param d
	 */
    public void setLeafarea(double d) {
        leafarea = d;
    }

    /**
	 * @param d
	 */
    public void setLeafmass(double d) {
        leafmass = d;
    }

    /**
	 * @param ds
	 */
    public void setLightpoint(double[][] ds) {
        lightpoint = ds;
    }

    /**
	 * @param d
	 */
    public void setPredlight(double d) {
        predlight = d;
    }

    /**
	 * @param ds
	 */
    public void setShootbase(double[] ds) {
        shootbase = ds;
    }

    /**
	 * @param ds
	 */
    public void setShootmid(double[] ds) {
        shootmid = ds;
    }

    /**
	 * @param ds
	 */
    public void setShoottip(double[] ds) {
        shoottip = ds;
    }

    /**
	 * @param d
	 */
    public void setSla(double d) {
        sla = d;
    }

    /**
	 * @param d
	 */
    public void setTotalmass(double d) {
        totalmass = d;
    }

    /**
	 * @param d
	 */
    public void setW(double d) {
        w = d;
    }

    /**
	 * @param d
	 */
    public void setWoodymass(double d) {
        woodymass = d;
    }

    /**
	 * @return
	 */
    public Shoot getNext() {
        return next;
    }

    /**
	 * @return
	 */
    public Shoot getFirst() {
        return first;
    }

    /**
	 * @param shoot
	 */
    public void setNext(Shoot shoot) {
        next = shoot;
    }

    /**
	 * @param shoot
	 */
    public void setPre(Shoot shoot) {
        first = shoot;
    }

    /**
	 * @return
	 */
    public int getIndid() {
        return indid;
    }

    /**
	 * @return
	 */
    public int getShootage() {
        return shootage;
    }

    /**
	 * @return
	 */
    public int getShootbirthyear() {
        return shootbirthyear;
    }

    /**
	 * @return
	 */
    public int getShootid() {
        return shootid;
    }

    /**
	 * @param i
	 */
    public void setIndid(int i) {
        indid = i;
    }

    /**
	 * @param i
	 */
    public void setShootage(int i) {
        shootage = i;
    }

    /**
	 * @param i
	 */
    public void setShootbirthyear(int i) {
        shootbirthyear = i;
    }

    /**
	 * @param i
	 */
    public void setShootid(int i) {
        shootid = i;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }
}

class Flag {

    boolean outside;

    public Flag() {
        outside = true;
    }

    void setOutside(boolean b) {
        outside = b;
        return;
    }

    public boolean isOutside() {
        return outside;
    }
}
