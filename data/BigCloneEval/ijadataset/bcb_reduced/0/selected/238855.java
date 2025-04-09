package saadadb.products;

import java.io.File;
import java.util.LinkedHashMap;
import saadadb.collection.ImageSaada;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/** * @version $Id: Image2DCoordinate.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public class Image2DCoordinate {

    int aladin;

    double[] xyapoly = new double[10];

    double[] xydpoly = new double[10];

    double[] adypoly = new double[10];

    double[] adxpoly = new double[10];

    double epoch;

    int flagepoc = 0;

    double equinox = SaadaConstant.DOUBLE;

    double alpha = SaadaConstant.DOUBLE;

    double delta = SaadaConstant.DOUBLE;

    double yz = SaadaConstant.DOUBLE;

    double xz = SaadaConstant.DOUBLE;

    double focale = SaadaConstant.DOUBLE;

    double Xorg = SaadaConstant.DOUBLE;

    double Yorg = SaadaConstant.DOUBLE;

    double incX = SaadaConstant.DOUBLE;

    double incY = SaadaConstant.DOUBLE;

    double alphai = SaadaConstant.DOUBLE;

    double deltai = SaadaConstant.DOUBLE;

    double incA = SaadaConstant.DOUBLE;

    double incD = SaadaConstant.DOUBLE;

    double Xcen = SaadaConstant.DOUBLE;

    double Ycen = SaadaConstant.DOUBLE;

    double widtha = SaadaConstant.DOUBLE;

    double widthd = SaadaConstant.DOUBLE;

    int xnpix = SaadaConstant.INT;

    int ynpix = SaadaConstant.INT;

    double rota = SaadaConstant.DOUBLE;

    double cdelz = SaadaConstant.DOUBLE;

    double sdelz = SaadaConstant.DOUBLE;

    String type1;

    String type2;

    double[][] CD = new double[2][2];

    double[][] ID = new double[2][2];

    public static int FK5 = 5;

    public static int FK4 = 1;

    public static int ICRS = 6;

    public static int GALACTIC = 2;

    public static int SUPERGALACTIC = 3;

    public static int ECLIPTIC = 4;

    public static int XYLINEAR = 7;

    protected int system = FK5;

    protected int proj;

    private double deg_to_rad = Math.PI / 180.;

    private double rad_to_deg = 180. / Math.PI;

    String[] projection = { "SINUS", "TANGENTIAL", "ARC", "AITOFF", "ZENITAL_EQUAL_AREA", "STEREOGRAPHIC", "CARTESIAN", "NCP" };

    private static Astroframe AF_FK4 = new FK4();

    private static Astroframe AF_FK5 = new FK5();

    private static Astroframe AF_ICRS = new ICRS();

    private static Astroframe AF_GAL = new Galactic();

    public Image2DCoordinate() {
    }

    public void setImage2DCoordinate(LinkedHashMap<String, AttributeHandler> tableAttributeHandler) throws Exception {
        double det;
        aladin = 0;
        AttributeHandler ah;
        if ((ah = tableAttributeHandler.get("_zimage")) != null && ah.getValue().toString().equalsIgnoreCase("true")) {
            xnpix = Integer.parseInt((tableAttributeHandler.get("_znaxis1")).getValue());
            ynpix = Integer.parseInt((tableAttributeHandler.get("_znaxis2")).getValue());
        } else {
            xnpix = Integer.parseInt((tableAttributeHandler.get("_naxis1")).getValue());
            ynpix = Integer.parseInt((tableAttributeHandler.get("_naxis2")).getValue());
        }
        if (tableAttributeHandler.containsKey("_PLTRAS")) {
            alpha = Double.parseDouble((tableAttributeHandler.get("_pltras")).getValue());
            Dss(tableAttributeHandler);
            return;
        } else {
            Xcen = Double.parseDouble((tableAttributeHandler.get("_crpix1")).getValue());
            Ycen = Double.parseDouble((tableAttributeHandler.get("_crpix2")).getValue());
            alphai = Double.parseDouble((tableAttributeHandler.get("_crval1")).getValue());
            deltai = Double.parseDouble((tableAttributeHandler.get("_crval2")).getValue());
            if (tableAttributeHandler.containsKey("_cd1_1") && tableAttributeHandler.containsKey("_cd1_2") && tableAttributeHandler.containsKey("_cd2_1") && tableAttributeHandler.containsKey("_cd2_2")) {
                CD[0][0] = Double.parseDouble((tableAttributeHandler.get("_cd1_1")).getValue());
                CD[0][1] = Double.parseDouble((tableAttributeHandler.get("_cd1_2")).getValue());
                CD[1][0] = Double.parseDouble((tableAttributeHandler.get("_cd2_1")).getValue());
                CD[1][1] = Double.parseDouble((tableAttributeHandler.get("_cd2_2")).getValue());
                incA = Math.sqrt(CD[0][0] * CD[0][0] + CD[0][1] * CD[0][1]);
                incD = Math.sqrt(CD[1][0] * CD[1][0] + CD[1][1] * CD[1][1]);
                rota = Math.acos(CD[0][0] / incA) * (180. / Math.PI);
            } else {
                incA = Double.parseDouble((tableAttributeHandler.get("_cdelt1")).getValue());
                incD = Double.parseDouble((tableAttributeHandler.get("_cdelt2")).getValue());
                if (tableAttributeHandler.containsKey("_crota1") || tableAttributeHandler.containsKey("_crota2")) {
                    double rota1 = 0, rota2 = 0;
                    if (tableAttributeHandler.containsKey("_crota1") && tableAttributeHandler.containsKey("_crota2")) {
                        rota1 = Double.parseDouble((tableAttributeHandler.get("_crota1")).getValue());
                        rota2 = Double.parseDouble((tableAttributeHandler.get("_crota2")).getValue());
                    } else {
                        if (tableAttributeHandler.containsKey("_crota1")) rota1 = Double.parseDouble((tableAttributeHandler.get("_crota1")).getValue());
                        if (tableAttributeHandler.containsKey("_crota2")) rota2 = Double.parseDouble((tableAttributeHandler.get("_crota2")).getValue());
                    }
                    rota = rota1;
                    if (rota1 == 0) {
                        rota = rota2;
                    } else {
                        if (rota2 != 0) {
                            rota = (rota1 + rota2) / 2;
                        }
                    }
                    CD[0][0] = incA * Math.cos((rota / 180.) * Math.PI);
                    CD[0][1] = -incD * Math.sin((rota / 180.) * Math.PI);
                    CD[1][0] = incA * Math.sin((rota / 180.) * Math.PI);
                    CD[1][1] = incD * Math.cos((rota / 180.) * Math.PI);
                } else {
                    if (tableAttributeHandler.containsKey("_pc001001") && tableAttributeHandler.containsKey("_pc001002") && tableAttributeHandler.containsKey("_pc002001") && tableAttributeHandler.containsKey("_pc002002")) {
                        CD[0][0] = Double.parseDouble((tableAttributeHandler.get("_pc001001")).getValue()) * incA;
                        CD[0][1] = Double.parseDouble((tableAttributeHandler.get("_pc001002")).getValue()) * incA;
                        CD[1][0] = Double.parseDouble((tableAttributeHandler.get("_pc002001")).getValue()) * incD;
                        CD[1][1] = Double.parseDouble((tableAttributeHandler.get("_pc002002")).getValue()) * incD;
                        rota = Math.acos(Double.parseDouble((tableAttributeHandler.get("_pc001001")).getValue())) * (180. / Math.PI);
                    } else {
                        rota = 0.0;
                        CD[0][0] = incA;
                        CD[0][1] = 0;
                        CD[1][0] = 0;
                        CD[1][1] = incD;
                        AttributeHandler origin = (tableAttributeHandler.get("_origin"));
                        if (origin != null && origin.getValue().startsWith("'DeNIS")) {
                            if (CD[0][0] > 0) CD[0][0] = -CD[0][0];
                            if (CD[1][1] > 0) CD[1][1] = -CD[1][1];
                        }
                    }
                }
            }
        }
        if (tableAttributeHandler.containsKey("_epoch")) {
            epoch = Double.parseDouble((tableAttributeHandler.get("_epoch")).getValue());
            flagepoc = 1;
        } else {
            epoch = 0.0;
        }
        if (tableAttributeHandler.containsKey("_equinox")) {
            equinox = Double.parseDouble((tableAttributeHandler.get("_equinox")).getValue());
        } else {
            if (tableAttributeHandler.containsKey("_epoch")) {
                equinox = Double.parseDouble((tableAttributeHandler.get("_epoch")).getValue());
                epoch = equinox;
                flagepoc = 1;
            } else {
                equinox = 2000.0;
                epoch = 2000.0;
                flagepoc = 0;
            }
        }
        type1 = (tableAttributeHandler.get("_ctype1")).getValue();
        type2 = (tableAttributeHandler.get("_ctype1")).getValue();
        if (type1.startsWith("'DEC")) {
            double tmp_invert = deltai;
            deltai = alphai;
            alphai = tmp_invert;
            tmp_invert = CD[0][0];
            CD[0][0] = CD[1][0];
            CD[1][0] = tmp_invert;
            tmp_invert = CD[1][1];
            CD[1][1] = CD[0][1];
            CD[0][1] = tmp_invert;
            tmp_invert = incA;
            incA = incD;
            incD = tmp_invert;
        }
        if (type1.startsWith("'ELON")) {
            system = 4;
        }
        if (type1.startsWith("'GLON")) {
            system = 2;
        }
        if (type1.startsWith("'SLON")) {
            system = 3;
        }
        if (equinox == 1950.0) {
            system = 1;
        }
        if (tableAttributeHandler.containsKey("_radecsys")) {
            String Syst = (tableAttributeHandler.get("_radecsys")).getValue();
            if (Syst.indexOf("ICRS") >= 0) system = 6; else if (Syst.indexOf("FK5") >= 0) system = 5; else if (Syst.indexOf("FK4") >= 0) system = 1;
        }
        proj = 0;
        if (type1.indexOf("SIN") >= 0) proj = 1; else if (type1.indexOf("TAN") >= 0) proj = 2; else if (type1.indexOf("COE") >= 0) proj = 2; else if (type1.indexOf("ARC") >= 0) proj = 3; else if (type1.indexOf("AIT") >= 0) proj = 4; else if (type1.indexOf("ZEA") >= 0) proj = 5; else if (type1.indexOf("STG") >= 0) proj = 6; else if (type1.indexOf("CAR") >= 0) proj = 7; else if (type1.indexOf("NCP") >= 0) proj = 8; else if (type1.indexOf("ZPN") >= 0) proj = 9;
        if (proj == 0) {
            Messenger.printMsg(Messenger.WARNING, "WCS CTYPE <" + type1 + "> has no specified projection: Take TAN projection by default");
        }
        widtha = xnpix * Math.abs(incA);
        widthd = ynpix * Math.abs(incD);
        cdelz = Math.cos(deltai * deg_to_rad);
        sdelz = Math.sin(deltai * deg_to_rad);
        det = CD[0][0] * CD[1][1] - CD[0][1] * CD[1][0];
        ID[0][0] = CD[1][1] / det;
        ID[0][1] = -CD[0][1] / det;
        ID[1][0] = -CD[1][0] / det;
        ID[1][1] = CD[0][0] / det;
    }

    protected void Dss(LinkedHashMap<String, AttributeHandler> tableAttributeHandler) throws Exception {
        int sign = 1;
        double det;
        proj = 2;
        alpha += Integer.parseInt((tableAttributeHandler.get("_pltram")).getValue()) * 60.;
        alpha += Integer.parseInt((tableAttributeHandler.get("_pltrah")).getValue()) * 3600.;
        alpha /= 240.;
        if ((tableAttributeHandler.get("_PLTDECSN")).getValue().startsWith("'-")) sign = -1;
        ;
        delta = Double.parseDouble((tableAttributeHandler.get("_pltdecs")).getValue());
        delta += Integer.parseInt((tableAttributeHandler.get("_pltdecm")).getValue()) * 60.;
        delta += Integer.parseInt((tableAttributeHandler.get("_pltdecd")).getValue()) * 3600.;
        delta /= 3600.;
        delta *= sign;
        focale = Double.parseDouble((tableAttributeHandler.get("_pltscale")).getValue());
        focale = 180. * 3600. / Math.PI / focale;
        equinox = Double.parseDouble((tableAttributeHandler.get("_equinox")).getValue());
        if (tableAttributeHandler.containsKey("_EPOCH")) {
            epoch = Double.parseDouble((tableAttributeHandler.get("_epoch")).getValue());
        } else {
            epoch = equinox;
            flagepoc = 0;
        }
        xz = Double.parseDouble((tableAttributeHandler.get("_pp03")).getValue());
        xz /= 1000.;
        yz = Double.parseDouble((tableAttributeHandler.get("_ppO6")).getValue());
        yz /= 1000.;
        xyapoly[2] = Double.parseDouble((tableAttributeHandler.get("_amdx1")).getValue());
        xydpoly[1] = Double.parseDouble((tableAttributeHandler.get("_amdy1")).getValue());
        xyapoly[1] = Double.parseDouble((tableAttributeHandler.get("_amdx2")).getValue());
        xydpoly[2] = Double.parseDouble((tableAttributeHandler.get("_amdy2")).getValue());
        xyapoly[0] = Double.parseDouble((tableAttributeHandler.get("_amdx3")).getValue());
        xydpoly[0] = Double.parseDouble((tableAttributeHandler.get("_amdy3")).getValue());
        xyapoly[4] = Double.parseDouble((tableAttributeHandler.get("_amdx4")).getValue());
        xydpoly[3] = Double.parseDouble((tableAttributeHandler.get("_amdy4")).getValue());
        xyapoly[5] = Double.parseDouble((tableAttributeHandler.get("_amdx5")).getValue());
        xydpoly[5] = Double.parseDouble((tableAttributeHandler.get("_amdy5")).getValue());
        xyapoly[3] = Double.parseDouble((tableAttributeHandler.get("_amdx6")).getValue());
        xydpoly[4] = Double.parseDouble((tableAttributeHandler.get("_amdy6")).getValue());
        xyapoly[4] += Double.parseDouble((tableAttributeHandler.get("_amdx7")).getValue());
        xydpoly[4] += Double.parseDouble((tableAttributeHandler.get("_amdy7")).getValue());
        xyapoly[3] += Double.parseDouble((tableAttributeHandler.get("_amdx7")).getValue());
        xydpoly[3] += Double.parseDouble((tableAttributeHandler.get("_amdy7")).getValue());
        xyapoly[7] = Double.parseDouble((tableAttributeHandler.get("_amdx8")).getValue());
        xydpoly[6] = Double.parseDouble((tableAttributeHandler.get("_amdy8")).getValue());
        xyapoly[9] = Double.parseDouble((tableAttributeHandler.get("_amdx9")).getValue());
        xydpoly[8] = Double.parseDouble((tableAttributeHandler.get("_amdy9")).getValue());
        xyapoly[8] = Double.parseDouble((tableAttributeHandler.get("_amdx10")).getValue());
        xydpoly[9] = Double.parseDouble((tableAttributeHandler.get("_amdy10")).getValue());
        xyapoly[6] = Double.parseDouble((tableAttributeHandler.get("_amdx11")).getValue());
        xydpoly[7] = Double.parseDouble((tableAttributeHandler.get("_amdy11")).getValue());
        xyapoly[7] += Double.parseDouble((tableAttributeHandler.get("_amdx12")).getValue());
        xydpoly[6] += Double.parseDouble((tableAttributeHandler.get("_amdy12")).getValue());
        xyapoly[8] += Double.parseDouble((tableAttributeHandler.get("_amdx12")).getValue());
        xydpoly[9] += Double.parseDouble((tableAttributeHandler.get("_amdy12")).getValue());
        xyapoly[0] /= focale;
        xydpoly[0] /= focale;
        xyapoly[1] *= -1.;
        xydpoly[1] *= -1.;
        xyapoly[2] *= -1.;
        xydpoly[2] *= -1.;
        xyapoly[3] *= focale;
        xydpoly[3] *= focale;
        xyapoly[4] *= focale;
        xydpoly[4] *= focale;
        xyapoly[5] *= focale;
        xydpoly[5] *= focale;
        xyapoly[6] *= -focale * focale;
        xydpoly[6] *= -focale * focale;
        xyapoly[7] *= -focale * focale;
        xydpoly[7] *= -focale * focale;
        xyapoly[8] *= -focale * focale;
        xydpoly[8] *= -focale * focale;
        xyapoly[9] *= -focale * focale;
        xydpoly[9] *= -focale * focale;
        int i;
        for (i = 0; i <= 9; i++) {
            xyapoly[i] /= (180 * 3600 / Math.PI / focale);
            xydpoly[i] /= (180 * 3600 / Math.PI / focale);
        }
        incX = Double.parseDouble((tableAttributeHandler.get("_xpixelsz")).getValue());
        incY = Double.parseDouble((tableAttributeHandler.get("_ypixelsz")).getValue());
        xnpix = Integer.parseInt((tableAttributeHandler.get("_naxis1")).getValue());
        ynpix = Integer.parseInt((tableAttributeHandler.get("_naxis2")).getValue());
        Xorg = incX * Integer.parseInt((tableAttributeHandler.get("_cnpix1")).getValue());
        Yorg = incY * (13999 - Integer.parseInt((tableAttributeHandler.get("_cnpix2")).getValue()) - ynpix);
        yz = incY * 13999 / 1000. - yz;
        cdelz = Math.cos(delta * deg_to_rad);
        sdelz = Math.sin(delta * deg_to_rad);
        aladin = 1;
        GetWCS_i();
        det = CD[0][0] * CD[1][1] - CD[0][1] * CD[1][0];
        ID[0][0] = CD[1][1] / det;
        ID[0][1] = -CD[0][1] / det;
        ID[1][0] = -CD[1][0] / det;
        ID[1][1] = CD[0][0] / det;
        incA = Math.sqrt(CD[0][0] * CD[0][0] + CD[0][1] * CD[0][1]);
        incD = Math.sqrt(CD[1][0] * CD[1][0] + CD[1][1] * CD[1][1]);
        widtha = xnpix * Math.abs(incA);
        widthd = ynpix * Math.abs(incD);
        cdelz = Math.cos(deltai * deg_to_rad);
        sdelz = Math.sin(deltai * deg_to_rad);
        type1 = "'RA---TAN'";
        type2 = "'DEC--TAN'";
        aladin = 0;
    }

    protected void GetWCS_i() throws Exception {
        Coord a_d = new Coord();
        Coord x_y_1 = new Coord();
        Coord x_y_2 = new Coord();
        Coord x_y_3 = new Coord();
        Coord x_y_4 = new Coord();
        double alpha1, delta1;
        double alpha2, delta2;
        double alpha3, delta3;
        double alpha4, delta4;
        if (aladin == 1) {
            Xcen = xnpix / 2.;
            Ycen = ynpix / 2.;
            a_d.x = Xcen;
            a_d.y = Ycen;
            GetCoord(a_d);
            alphai = a_d.al;
            deltai = a_d.del;
            x_y_1.x = Xcen - xnpix / 4.;
            x_y_1.y = Ycen - ynpix / 4.;
            GetCoord(x_y_1);
            double cdelz1, sdelz1;
            cdelz1 = Math.cos((deltai / 180.) * Math.PI);
            sdelz1 = Math.sin((deltai / 180.) * Math.PI);
            double xst, yst, deno;
            deno = Math.sin(x_y_1.del * Math.PI / 180.) * sdelz1 + Math.cos(x_y_1.del * Math.PI / 180.) * cdelz1 * Math.cos((x_y_1.al - alphai) * Math.PI / 180.);
            xst = Math.cos(x_y_1.del * Math.PI / 180.) * Math.sin((x_y_1.al - alphai) * Math.PI / 180.) / deno;
            yst = Math.sin(x_y_1.del * Math.PI / 180.) * cdelz1 - Math.cos(x_y_1.del * Math.PI / 180.) * sdelz1 * Math.cos((x_y_1.al - alphai) * Math.PI / 180.) / deno;
            CD[0][0] = -(ynpix * xst + xnpix * yst) * 2 / ynpix / xnpix;
            CD[0][1] = +(ynpix * xst - xnpix * yst) * 2 / xnpix / ynpix;
            x_y_2.x = Xcen + xnpix / 4.;
            x_y_2.y = Ycen - ynpix / 4.;
            GetCoord(x_y_2);
            deno = Math.sin(x_y_2.del * Math.PI / 180.) * sdelz1 + Math.cos(x_y_2.del * Math.PI / 180.) * cdelz1 * Math.cos((x_y_2.al - alphai) * Math.PI / 180.);
            xst = Math.cos(x_y_2.del * Math.PI / 180.) * Math.sin((x_y_2.al - alphai) * Math.PI / 180.) / deno;
            yst = Math.sin(x_y_2.del * Math.PI / 180.) * cdelz1 - Math.cos(x_y_2.del * Math.PI / 180.) * sdelz1 * Math.cos((x_y_2.al - alphai) * Math.PI / 180.) / deno;
            CD[0][0] += (ynpix * xst - xnpix * yst) * 2 / ynpix / xnpix;
            CD[0][1] += (ynpix * xst + xnpix * yst) * 2 / xnpix / ynpix;
            x_y_3.x = Xcen - xnpix / 4.;
            x_y_3.y = Ycen + ynpix / 4.;
            GetCoord(x_y_3);
            deno = Math.sin(x_y_3.del * Math.PI / 180.) * sdelz1 + Math.cos(x_y_3.del * Math.PI / 180.) * cdelz1 * Math.cos((x_y_3.al - alphai) * Math.PI / 180.);
            xst = Math.cos(x_y_3.del * Math.PI / 180.) * Math.sin((x_y_3.al - alphai) * Math.PI / 180.) / deno;
            yst = Math.sin(x_y_3.del * Math.PI / 180.) * cdelz1 - Math.cos(x_y_3.del * Math.PI / 180.) * sdelz1 * Math.cos((x_y_3.al - alphai) * Math.PI / 180.) / deno;
            CD[0][0] -= (ynpix * xst - xnpix * yst) * 2 / ynpix / xnpix;
            CD[0][1] -= (xst * ynpix + yst * xnpix) * 2 / xnpix / ynpix;
            x_y_4.x = Xcen + xnpix / 4.;
            x_y_4.y = Ycen + ynpix / 4.;
            GetCoord(x_y_4);
            deno = Math.sin(x_y_4.del * Math.PI / 180.) * sdelz1 + Math.cos(x_y_4.del * Math.PI / 180.) * cdelz1 * Math.cos((x_y_4.al - alphai) * Math.PI / 180.);
            xst = Math.cos(x_y_4.del * Math.PI / 180.) * Math.sin((x_y_4.al - alphai) * Math.PI / 180.) / deno;
            yst = Math.sin(x_y_4.del * Math.PI / 180.) * cdelz1 - Math.cos(x_y_4.del * Math.PI / 180.) * sdelz1 * Math.cos((x_y_4.al - alphai) * Math.PI / 180.) / deno;
            CD[0][0] += (ynpix * xst + xnpix * yst) * 2 / ynpix / xnpix;
            CD[0][1] -= (xst * ynpix - yst * xnpix) * 2 / xnpix / ynpix;
            CD[0][0] *= 180. / Math.PI / 4.;
            CD[0][1] *= 180. / Math.PI / 4.;
            CD[1][0] = CD[0][1];
            CD[1][1] = -CD[0][0];
            equinox = 2000.0;
            proj = 2;
        }
    }

    /**
	 * @param c
	 * @throws Exception
	 */
    public void GetCoord(Coord c) throws Exception {
        double x_obj = 1.;
        double y_obj = 1.;
        double x_objr;
        double y_objr;
        double posx;
        double posy;
        if (aladin == 1) {
            x_obj = (c.x * incX + Xorg) / 1000.;
            y_obj = (c.y * incY + Yorg) / 1000.;
            x_objr = (x_obj - xz) / focale;
            y_objr = (y_obj - yz) / focale;
            posx = xyapoly[0] + xyapoly[1] * y_objr + xyapoly[2] * x_objr + xyapoly[3] * y_objr * y_objr + xyapoly[4] * x_objr * x_objr + xyapoly[5] * y_objr * x_objr + xyapoly[6] * y_objr * y_objr * y_objr + xyapoly[7] * x_objr * x_objr * x_objr + xyapoly[8] * y_objr * y_objr * x_objr + xyapoly[9] * y_objr * x_objr * x_objr;
            posy = xydpoly[0] + xydpoly[1] * y_objr + xydpoly[2] * x_objr + xydpoly[3] * y_objr * y_objr + xydpoly[4] * x_objr * x_objr + xydpoly[5] * y_objr * x_objr + xydpoly[6] * y_objr * y_objr * y_objr + xydpoly[7] * x_objr * x_objr * x_objr + xydpoly[8] * y_objr * y_objr * x_objr + xydpoly[9] * y_objr * x_objr * x_objr;
            c.al = alpha + (Math.atan(posx / (cdelz - posy * sdelz))) * rad_to_deg;
            c.del = Math.atan(Math.cos((c.al - alpha) * deg_to_rad) * (sdelz + posy * cdelz) / (cdelz - posy * sdelz)) * rad_to_deg;
            if ((c.del * delta < 0) && (Math.abs(delta) > 87.)) {
                c.al += 180.;
                c.del = -c.del;
            }
            if (c.al > 360.) c.al -= 360.;
            if (c.al < 0.) c.al += 360.;
        } else {
            x_obj = c.x - Xcen + 1;
            y_obj = ynpix - Ycen - c.y;
            x_objr = (CD[0][0] * x_obj + CD[0][1] * y_obj);
            y_objr = (CD[1][0] * x_obj + CD[1][1] * y_obj);
            x_objr *= deg_to_rad;
            y_objr *= deg_to_rad;
            double X;
            double tet;
            switch(proj) {
                case 1:
                    c.del = rad_to_deg * (Math.asin(y_objr * cdelz + sdelz * Math.sqrt(1 - y_objr * y_objr - x_objr * x_objr)));
                    X = x_objr / (cdelz * Math.sqrt(1 - y_objr * y_objr - x_objr * x_objr) - y_objr * sdelz);
                    c.al = alphai + rad_to_deg * Math.atan(X);
                    double sign;
                    if (deltai == 0) sign = 1; else sign = deltai / Math.abs(deltai);
                    if (sign * y_objr - cdelz > 0) c.al += 180.;
                    break;
                case 8:
                    c.del = rad_to_deg * (Math.acos((cdelz - y_objr * sdelz) / Math.cos((c.al - alpha) * deg_to_rad)));
                    X = x_objr / (cdelz - y_objr * sdelz);
                    c.al = alphai + rad_to_deg * Math.atan(X);
                    break;
                case 2:
                    double deno = cdelz - y_objr * sdelz;
                    double d_al = Math.atan(x_objr / deno);
                    c.del = (180. / Math.PI) * Math.atan(Math.cos(d_al) * (sdelz + y_objr * cdelz) / deno);
                    c.al = alphai + d_al * rad_to_deg;
                    if ((c.del * deltai < 0) && (Math.abs(deltai) > 87.)) {
                        c.al += 180.;
                        c.del = -c.del;
                    }
                    break;
                case 9:
                case 3:
                    tet = Math.sqrt(x_objr * x_objr + y_objr * y_objr);
                    c.del = rad_to_deg * Math.asin(+y_objr * cdelz * Math.sin(tet) / tet + sdelz * Math.cos(tet));
                    if (tet < Math.PI / 2) c.al = alphai + rad_to_deg * Math.asin(Math.sin(tet) * x_objr / (tet * Math.cos(c.del * deg_to_rad))); else c.al = alphai + 180. - rad_to_deg * Math.asin(Math.sin(tet) * x_objr / (tet * Math.cos(c.del * deg_to_rad)));
                    ;
                    break;
                case 4:
                    double z = 1 - x_objr * x_objr / 16 - y_objr * y_objr / 4;
                    if (z < 0.5) {
                        c.del = -32000;
                        c.al = -32000.0;
                    } else {
                        double Z = Math.sqrt(z);
                        c.del = rad_to_deg * Math.asin(y_objr * Z);
                        c.al = 2 * rad_to_deg * Math.atan((x_objr * Z / 2) / (2 * Z * Z - 1));
                    }
                    break;
                case 5:
                    double phi;
                    double rtet = Math.sqrt(x_objr * x_objr + y_objr * y_objr) / 2.;
                    tet = Math.PI / 2. - 2 * Math.asin(rtet);
                    if (y_objr != 0.0) phi = Math.atan(-x_objr / y_objr); else phi = Math.atan(-x_objr);
                    if (y_objr < 0.0) phi = phi + Math.PI;
                    c.del = rad_to_deg * Math.asin(sdelz * Math.sin(tet) + cdelz * Math.cos(tet) * Math.cos(phi));
                    double arg1 = (Math.sin(tet) * cdelz - Math.cos(tet) * sdelz * Math.cos(phi));
                    double arg;
                    arg = -(Math.cos(tet) * Math.sin(phi));
                    if (Math.abs(deltai) != 90.) c.al = alphai + rad_to_deg * Math.atan2(arg, arg1); else if (deltai == 90.) c.al = rad_to_deg * (phi + Math.PI); else c.al = rad_to_deg * (-phi);
                    if ((c.del * c.del > 90. * 90.) && (Math.abs(deltai) > 65.)) {
                        c.al = 180. - c.al;
                        c.del = 2 * deltai - c.del;
                    }
                    break;
                case 6:
                    double sintet = Math.sin(Math.PI / 2 - 2 * Math.atan(Math.sqrt(y_objr * y_objr + x_objr * x_objr) / 2));
                    c.del = rad_to_deg * Math.asin(cdelz * y_objr / 2 + sintet * (sdelz + cdelz * y_objr / 2));
                    deno = sintet * (2 * cdelz - y_objr * sdelz) - y_objr * sdelz;
                    c.al = alphai + rad_to_deg * Math.atan2(x_objr * (1 + sintet), deno);
                    break;
                case 7:
                    c.al = alphai + x_objr * rad_to_deg;
                    c.del = deltai + y_objr * rad_to_deg;
                    break;
                default:
                    break;
            }
            if (equinox != 2000.0) {
                Astroframe j2000 = Coord.getAstroframe("FK5", Double.toString(2000));
                ;
                Astroframe natif = Coord.getAstroframe("FK4", Double.toString(equinox));
                double converted[] = Coord.convert(natif, new double[] { c.al, c.del }, j2000);
                c.al = converted[0];
                c.del = converted[1];
            }
            if (system == 2) {
                Astroframe fk5 = Coord.getAstroframe("FK5", Double.toString(2000));
                ;
                Astroframe natif = Coord.getAstroframe("FK5", Double.toString(equinox));
                double converted[] = Coord.convert(natif, new double[] { c.al, c.del }, fk5);
                c.al = converted[0];
                c.del = converted[1];
            }
            if (c.al > 360.) c.al -= 360.;
            if (c.al < 0.) c.al += 360.;
        }
    }

    public void GetXY(Coord c) throws Exception {
        double x_obj = 1.;
        double y_obj = 1.;
        double x_objr;
        double y_objr;
        double x_tet_phi;
        double y_tet_phi;
        double y_stand = 0.03;
        double x_stand = 0.03;
        double delrad;
        double alrad;
        double dr;
        double al, del;
        if (aladin == 1) {
            double cos_del = Math.cos(c.del * deg_to_rad);
            double distalpha = Math.min(Math.abs(c.al - alphai), 360. - Math.abs(c.al - alphai));
            if (cos_del * (distalpha) * cos_del * (distalpha) + (c.del - deltai) * (c.del - deltai) > 625.0) throw new Exception("Outside the projection");
            delrad = c.del * deg_to_rad;
            alrad = (c.al - alpha) * deg_to_rad;
            double sin_delrad = Math.sin(delrad);
            double cos_delrad = Math.cos(delrad);
            double sin_alrad = Math.sin(alrad);
            double cos_alrad = Math.cos(alrad);
            dr = sin_delrad * sdelz + cos_delrad * cdelz * cos_alrad;
            x_stand = cos_delrad * sin_alrad / dr;
            y_stand = (sin_delrad * cdelz - cos_delrad * sdelz * cos_alrad) / dr;
            x_obj = adxpoly[0] + adxpoly[1] * x_stand + adxpoly[2] * y_stand + adxpoly[3] * x_stand * x_stand + adxpoly[4] * y_stand * y_stand + adxpoly[5] * y_stand * x_stand + adxpoly[6] * x_stand * x_stand * x_stand + adxpoly[7] * y_stand * y_stand * y_stand + adxpoly[8] * y_stand * x_stand * x_stand + adxpoly[9] * y_stand * y_stand * x_stand;
            y_obj = adypoly[0] + adypoly[1] * x_stand + adypoly[2] * y_stand + adypoly[3] * x_stand * x_stand + adypoly[4] * y_stand * y_stand + adypoly[5] * y_stand * x_stand + adypoly[6] * x_stand * x_stand * x_stand + adypoly[7] * y_stand * y_stand * y_stand + adypoly[8] * y_stand * x_stand * x_stand + adypoly[9] * y_stand * y_stand * x_stand;
            x_obj = x_obj * focale + xz;
            y_obj = y_obj * focale + yz;
            c.x = (x_obj * 1000.0 - Xorg) / incX;
            c.y = (y_obj * 1000.0 - Yorg) / incY;
        } else {
            al = c.al;
            del = c.del;
            if ((equinox != 2000.0) && (system != GALACTIC)) {
                Astrocoo ac = new Astrocoo(AF_ICRS, c.al, c.del);
                ac.setPrecision(Astrocoo.MAS + 1);
                ac.convertTo(AF_FK4);
                al = ac.getLon();
                del = ac.getLat();
            }
            if (system == GALACTIC) {
                Astrocoo ac = new Astrocoo(AF_ICRS, c.al, c.del);
                ac.setPrecision(Astrocoo.MAS + 1);
                ac.convertTo(AF_GAL);
                al = ac.getLon();
                del = ac.getLat();
            }
            double dalpha = (al - alphai) * deg_to_rad;
            double cos_del = Math.cos(del * deg_to_rad);
            double sin_del = Math.sin(del * deg_to_rad);
            double sin_dalpha = Math.sin(dalpha);
            double cos_dalpha = Math.cos(dalpha);
            x_tet_phi = cos_del * sin_dalpha;
            y_tet_phi = sin_del * cdelz - cos_del * sdelz * cos_dalpha;
            double phi;
            double tet;
            int goodness = 1;
            switch(proj) {
                case 1:
                case 8:
                case 2:
                    if (dalpha > Math.PI) dalpha = -2 * Math.PI + dalpha;
                    if (dalpha < -Math.PI) dalpha = +2 * Math.PI + dalpha;
                    if ((-sin_del * sdelz) / (cos_del * cdelz) > 1) throw new Exception("Outside the projection"); else if (((-sin_del * sdelz) / (cos_del * cdelz) > -1) && (Math.abs(dalpha) > Math.acos((-sin_del * sdelz) / (cos_del * cdelz)))) throw new Exception("Outside the projection");
                default:
                    break;
            }
            if (goodness == 1) {
                switch(proj) {
                    case 1:
                        x_stand = rad_to_deg * x_tet_phi;
                        y_stand = rad_to_deg * y_tet_phi;
                        break;
                    case 8:
                        x_stand = rad_to_deg * x_tet_phi;
                        if (sdelz == 0) y_stand = rad_to_deg * y_tet_phi; else if (sdelz * sin_del > 0) y_stand = rad_to_deg * y_tet_phi + (cdelz / sdelz) * rad_to_deg * (1 - Math.sqrt(1 - cos_del * cos_del * sin_dalpha * sin_dalpha - sin_del * sin_del * cdelz * cdelz - cos_del * cos_del * sdelz * sdelz * cos_dalpha * cos_dalpha + 2 * sin_del * cos_del * sdelz * cdelz * cos_dalpha)); else {
                            x_stand = 0.0;
                            y_stand = 0.0;
                        }
                        break;
                    case 2:
                        double den = sin_del * sdelz + cos_del * cdelz * cos_dalpha;
                        x_stand = x_tet_phi / den;
                        y_stand = y_tet_phi / den;
                        if ((xyapoly[1] != 0) && (xyapoly[1] != 1) && (aladin == 0)) {
                            double X = xyapoly[0];
                            double Y = xydpoly[0];
                            double dx;
                            double dy;
                            double xx = 0;
                            double yy = 0;
                            int niter = 20;
                            int iter = 0;
                            double m1, m2, m3, m4;
                            while (iter < niter) {
                                iter++;
                                m1 = xyapoly[1] + 2 * xyapoly[3] * xx + xyapoly[4] * yy + 3 * xyapoly[6] * xx * xx + xyapoly[8] * yy * yy + 2 * xyapoly[7] * yy * xx;
                                m2 = xydpoly[1] + 2 * xydpoly[3] * xx + xydpoly[4] * yy + 3 * xydpoly[6] * xx * xx + xydpoly[8] * yy * yy + 2 * xydpoly[7] * yy * xx;
                                m3 = xyapoly[2] + 2 * xyapoly[5] * yy + xyapoly[4] * xx + 3 * xyapoly[9] * yy * yy + 2 * xyapoly[8] * yy * xx + xyapoly[7] * xx * xx;
                                m4 = xydpoly[2] + 2 * xydpoly[5] * yy + xydpoly[4] * xx + 3 * xydpoly[9] * yy * yy + 2 * xydpoly[8] * yy * xx + xydpoly[7] * xx * xx;
                                double det = m1 * m4 - m2 * m3;
                                double tmp = m4 / det;
                                m2 /= -det;
                                m3 /= -det;
                                m4 = m1 / det;
                                m1 = tmp;
                                dx = m1 * (x_stand - X) + m3 * (y_stand - Y);
                                dy = m2 * (x_stand - X) + m4 * (y_stand - Y);
                                xx += dx;
                                yy += dy;
                                X = xyapoly[0] + xyapoly[2] * yy + xyapoly[1] * xx + xyapoly[5] * yy * yy + xyapoly[3] * xx * xx + xyapoly[4] * yy * xx + xyapoly[9] * yy * yy * yy + xyapoly[6] * xx * xx * xx + xyapoly[8] * yy * yy * xx + xyapoly[7] * yy * xx * xx;
                                Y = xydpoly[0] + xydpoly[2] * yy + xydpoly[1] * xx + xydpoly[5] * yy * yy + xydpoly[3] * xx * xx + xydpoly[4] * yy * xx + xydpoly[9] * yy * yy * yy + xydpoly[6] * xx * xx * xx + xydpoly[8] * yy * yy * xx + xydpoly[7] * yy * xx * xx;
                            }
                            x_stand = xx;
                            y_stand = yy;
                        } else {
                            x_stand *= rad_to_deg;
                            y_stand *= rad_to_deg;
                        }
                        break;
                    case 9:
                    case 3:
                        if ((sin_del * cdelz - cos_del * sdelz * cos_dalpha) != 0) phi = Math.atan(-cos_del * sin_dalpha / (sin_del * cdelz - cos_del * sdelz * cos_dalpha)); else if (-cos_del * sin_dalpha < 0) phi = Math.PI / 2; else phi = -Math.PI / 2;
                        if (sin_del * cdelz - cos_del * sdelz * cos_dalpha > 0) phi = Math.PI + phi;
                        tet = Math.asin(sin_del * sdelz + cos_del * cdelz * cos_dalpha);
                        double rteta;
                        if (proj == 9) rteta = adxpoly[1] * (Math.PI / 2 - tet) + adxpoly[3] * (Math.PI / 2 - tet) * (Math.PI / 2 - tet) * (Math.PI / 2 - tet); else rteta = (Math.PI / 2 - tet);
                        x_stand = rteta * Math.sin(phi);
                        y_stand = -rteta * Math.cos(phi);
                        x_stand *= rad_to_deg;
                        y_stand *= rad_to_deg;
                        break;
                    case 4:
                        double cdelp = Math.cos(deltai * deg_to_rad + Math.PI / 2);
                        double sdelp = Math.sin(deltai * deg_to_rad + Math.PI / 2);
                        phi = Math.atan2(cos_del * sin_dalpha, -(sin_del * cdelp - cos_del * sdelp * cos_dalpha));
                        tet = Math.asin(sin_del * sdelp + cos_del * cdelp * cos_dalpha);
                        if (phi > Math.PI) phi = -2 * Math.PI + phi;
                        double alph = Math.sqrt(2 / (1 + Math.cos(tet) * Math.cos(phi / 2.)));
                        x_stand = 2 * alph * Math.cos(tet) * Math.sin(phi / 2);
                        y_stand = alph * Math.sin(tet);
                        x_stand *= rad_to_deg;
                        y_stand *= rad_to_deg;
                        break;
                    case 5:
                        if ((sin_del * cdelz - cos_del * sdelz * cos_dalpha) != 0) phi = Math.atan(-cos_del * sin_dalpha / (sin_del * cdelz - cos_del * sdelz * cos_dalpha)); else if (-cos_del * sin_dalpha < 0) phi = Math.PI / 2; else phi = -Math.PI / 2;
                        if ((sin_del * cdelz - cos_del * sdelz * cos_dalpha > 0) && (Math.abs(Math.sin(phi)) != 1.0)) phi = Math.PI + phi;
                        tet = Math.asin(sin_del * sdelz + cos_del * cdelz * cos_dalpha);
                        double rtet = rad_to_deg * Math.sqrt(2 * (1 - Math.sin(tet)));
                        x_stand = rtet * Math.sin(phi);
                        y_stand = -rtet * Math.cos(phi);
                        break;
                    case 6:
                        den = 1 + sin_del * sdelz + cos_del * cdelz * cos_dalpha;
                        x_stand = 2 * x_tet_phi / den;
                        y_stand = 2 * y_tet_phi / den;
                        x_stand *= rad_to_deg;
                        y_stand *= rad_to_deg;
                        break;
                    case 7:
                        x_stand = al - alphai;
                        y_stand = del - deltai;
                        double xshift = 0.;
                        if (((x_stand + 360.) > Math.min(CD[0][0] * Xcen + CD[0][1] * Ycen, CD[0][0] * (Xcen - xnpix) + CD[0][1] * Ycen)) && ((x_stand + 360.) < Math.max(CD[0][0] * Xcen + CD[0][1] * Ycen, CD[0][0] * (Xcen - xnpix) + CD[0][1] * Ycen))) xshift = 360.;
                        if (((x_stand - 360.) > Math.min(CD[0][0] * Xcen + CD[0][1] * Ycen, CD[0][0] * (Xcen - xnpix) + CD[0][1] * Ycen)) && ((x_stand - 360.) < Math.max(CD[0][0] * Xcen + CD[0][1] * Ycen, CD[0][0] * (Xcen - xnpix) + CD[0][1] * Ycen))) xshift = -360.;
                        x_stand += xshift;
                        break;
                    case 10:
                        x_stand = al - alphai;
                        y_stand = del - deltai;
                        break;
                    default:
                        break;
                }
            }
            c.x = (ID[0][0] * x_stand + ID[0][1] * y_stand) + Xcen;
            c.y = -(ID[1][0] * x_stand + ID[1][1] * y_stand) + ynpix - Ycen;
            if ((xyapoly[1] != 0) && (xyapoly[1] != 1) && (proj == 2) && (aladin == 0) && (xydpoly[2] * ID[1][1] < 0)) {
                c.y = (ID[1][0] * x_stand + ID[1][1] * y_stand) + Ycen;
            }
        }
    }

    /**
	 * @return
	 * @throws Exception
	 */
    public Coord getImgCenter() throws Exception {
        Coord c = new Coord();
        c.x = xnpix / 2;
        c.y = ynpix / 2;
        GetCoord(c);
        return c;
    }

    /**
	 * @return Returns the alpha.
	 */
    public double getAlpha() {
        return alpha;
    }

    /**
	 * @return Returns the cD.
	 */
    public double[][] getCD() {
        return CD;
    }

    /**
	 * @return Returns the delta.
	 */
    public double getDelta() {
        return delta;
    }

    /**
	 * @return Returns the incA.
	 */
    public double getIncA() {
        return incA;
    }

    /**
	 * @return Returns the incD.
	 */
    public double getIncD() {
        return incD;
    }

    /**
	 * @return Returns the incX.
	 */
    public double getIncX() {
        return incX;
    }

    /**
	 * @return Returns the incY.
	 */
    public double getIncY() {
        return incY;
    }

    /**
	 * @return Returns the projection.
	 */
    public String[] getProjection() {
        return projection;
    }

    /**
	 * @return Returns the rota.
	 */
    public double getRota() {
        return rota;
    }

    /**
	 * @return Returns the type1.
	 */
    public String getType1() {
        return type1;
    }

    /**
	 * @return Returns the type2.
	 */
    public String getType2() {
        return type2;
    }

    /**
	 * @return Returns the alphai.
	 */
    public double getAlphai() {
        return alphai;
    }

    /**
	 * @return Returns the deltai.
	 */
    public double getDeltai() {
        return deltai;
    }

    /**
	 * @return Returns the xcen.
	 */
    public double getXcen() {
        return Xcen;
    }

    /**
	 * @return Returns the ycen.
	 */
    public double getYcen() {
        return Ycen;
    }

    /**
	 * @return Returns the widtha.
	 */
    public double getWidtha() {
        return widtha;
    }

    /**
	 * @return Returns the widthd.
	 */
    public double getWidthd() {
        return widthd;
    }

    /**
	 * @return Returns the xnpix.
	 */
    public int getXnpix() {
        return xnpix;
    }

    /**
	 * @return Returns the xnpix.
	 */
    public int getYnpix() {
        return ynpix;
    }

    /**
	 * @return Returns the xorg.
	 */
    public double getXorg() {
        return Xorg;
    }

    public static void main(String[] args) throws Exception {
        Messenger.debug_mode = true;
        Database.init("MUSE");
        Query q = new Query();
        SaadaQLResultSet srs = q.runQuery("Select IMAGE From * In VariabilityCube Limit 1");
        ConfigurationDefaultHandler cdh = null;
        ImageSaada is = null;
        while (srs.next()) {
            is = (ImageSaada) Database.getCache().getObject(srs.getOid());
            cdh = is.getLoaderConfig();
            Image2D img = new Image2D(new File("/data/repository/MUSE/VariabilityCube/IMAGE/inst1.little_var.fits"), null);
            img.loadProductFile(cdh);
            LinkedHashMap<String, AttributeHandler> ahs = img.tableAttributeHandler;
            Image2DCoordinate i2c = new Image2DCoordinate();
            i2c.setImage2DCoordinate(ahs);
            img.setWcsFields();
        }
        srs.close();
    }
}
