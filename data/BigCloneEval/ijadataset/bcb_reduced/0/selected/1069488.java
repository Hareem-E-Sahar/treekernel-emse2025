package preproc.laog;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.image.ImageTiler;
import nom.tam.util.BufferedDataOutputStream;

/**
 * Program extracting 'box' around a star from a 'mega' fits file and save it
 * into a new file. The argument of the programs are:
 * @param decal is a ga
 */
public class FistImageExctractor extends Fits {

    int[] decal_;

    FistImageExctractor(String name) throws FitsException {
        super(name);
        decal_ = new int[] { 0, 0 };
    }

    private void setDecal(int nb, int axe) {
        this.decal_[axe] = nb;
    }

    private int[] toPixel(double[] coords) throws FitsException, IOException {
        int[] positionRaAndDec = new int[2];
        positionRaAndDec = positionRaDec();
        int[] res = new int[2];
        double[] coordsOrigin = { getHDU(0).getHeader().getDoubleValue("CRVAL1", 0), getHDU(0).getHeader().getDoubleValue("CRVAL2", 0) };
        double[] coordsPixelOrigin = { getHDU(0).getHeader().getDoubleValue("CRPIX1", 0), getHDU(0).getHeader().getDoubleValue("CRPIX2", 0) };
        double[] scale = { getHDU(0).getHeader().getDoubleValue("CDELT1", 0), this.getHDU(0).getHeader().getDoubleValue("CDELT2", 0) };
        if (coordsOrigin[0] == 0 || coordsOrigin[1] == 0 || coordsPixelOrigin[0] == 0 || coordsPixelOrigin[1] == 0 || scale[0] == 0 || scale[1] == 0) {
            logger_.log(Level.SEVERE, "KeyWords \"CRVAL\" or \"CRPIX\" or \"CDELT\" isn't present in this fits");
            System.exit(-1);
        }
        double ecartx = Math.abs(coords[0]) - Math.abs(coordsOrigin[positionRaAndDec[0]]);
        res[0] = (int) ((ecartx / scale[positionRaAndDec[0]]) + coordsPixelOrigin[positionRaAndDec[0]]);
        double ecarty = Math.abs(coords[1]) - Math.abs(coordsOrigin[positionRaAndDec[1]]);
        res[1] = (int) ((ecarty / scale[positionRaAndDec[1]]) + coordsPixelOrigin[positionRaAndDec[1]]);
        return res;
    }

    public int toPixel(double dim, int axis) throws FitsException, IOException {
        int[] axeRaAndDec = new int[2];
        axeRaAndDec = whichAxeForRaDec();
        int[] positionRaAndDec = new int[2];
        positionRaAndDec = positionRaDec();
        int nb;
        double[] scale = { getHDU(0).getHeader().getDoubleValue("CDELT1", 0), getHDU(0).getHeader().getDoubleValue("CDELT2", 0) };
        if (scale[0] == 0 || scale[1] == 0) {
            logger_.log(Level.SEVERE, "KeyWord \"CDELT\" isn't present in this fits");
            System.exit(-1);
        }
        if (axis == axeRaAndDec[0]) {
            nb = (int) ((dim / 60.0) / Math.abs(scale[positionRaAndDec[0]]));
        } else {
            nb = (int) ((dim / 60.0) / Math.abs(scale[positionRaAndDec[1]]));
        }
        return nb;
    }

    private float[][] getBounds(int[] centre, int[] dims, int[] corner) throws FitsException, IOException {
        ImageHDU hdu = (ImageHDU) getHDU(0);
        ImageTiler t = hdu.getTiler();
        float[][] matrice = new float[2][2];
        int[] img;
        boolean redim = false;
        double[] boxDimsOrigin = { getHDU(0).getHeader().getDoubleValue("NAXIS1", 0), getHDU(0).getHeader().getDoubleValue("NAXIS2", 0) };
        if (boxDimsOrigin[0] == 0 || boxDimsOrigin[1] == 0) {
            logger_.log(Level.SEVERE, "KeyWord \"NAXIS\" isn't present in this fits");
            System.exit(-1);
        }
        int[] axeRaAndDec = new int[2];
        axeRaAndDec = whichAxeForRaDec();
        if ((centre[0] > boxDimsOrigin[axeRaAndDec[0]] || centre[0] < 1) || (centre[1] > boxDimsOrigin[axeRaAndDec[1]] || centre[1] < 1)) {
            if (centre[0] > boxDimsOrigin[axeRaAndDec[0]] || centre[0] < 1) {
                logger_.log(Level.SEVERE, "RA out of limit");
            }
            if (centre[1] > boxDimsOrigin[axeRaAndDec[1]] || centre[1] < 1) {
                logger_.log(Level.SEVERE, "DEC out of limit");
            }
            System.exit(-1);
        } else {
            if (corner[0] > boxDimsOrigin[axeRaAndDec[0]]) {
                double tot = dims[0] + corner[0];
                int diff = (int) Math.abs(tot - boxDimsOrigin[axeRaAndDec[0]]);
                this.setDecal(diff, 0);
                corner[0] = (int) boxDimsOrigin[axeRaAndDec[0]];
            } else if (corner[0] < 1) {
                this.setDecal(corner[0], 0);
                corner[0] = 1;
            }
            if (corner[1] > boxDimsOrigin[axeRaAndDec[1]]) {
                double tot = dims[1] + corner[1];
                int diff = (int) Math.abs(tot - boxDimsOrigin[axeRaAndDec[1]]);
                this.setDecal(diff, 1);
                corner[1] = (int) boxDimsOrigin[axeRaAndDec[1]];
            } else if (corner[1] < 1) {
                this.setDecal(corner[1], 1);
                corner[1] = 1;
            }
            if (corner[0] + dims[0] > boxDimsOrigin[axeRaAndDec[0]]) {
                redim = true;
                int tot = (int) dims[0] + corner[0];
                int diff = (int) Math.abs(tot - boxDimsOrigin[axeRaAndDec[0]]);
                dims[0] = dims[0] - diff;
            }
            if (corner[1] + dims[1] > boxDimsOrigin[axeRaAndDec[1]]) {
                redim = true;
                int tot = (int) dims[1] + corner[1];
                int diff = (int) Math.abs(tot - boxDimsOrigin[1]);
                dims[1] = dims[1] - diff;
            }
            if (redim) {
                logger_.log(Level.INFO, "Image was been re-size");
                logger_.log(Level.INFO, "New dimension : " + dims[0] + "X" + dims[1]);
            }
            if (!RaEqualX()) {
                img = (int[]) t.getTile(new int[] { corner[0], corner[1] }, new int[] { dims[0], dims[1] });
                matrice = new float[dims[0]][dims[1]];
            } else {
                img = (int[]) t.getTile(new int[] { corner[1], corner[0] }, new int[] { dims[1], dims[0] });
                matrice = new float[dims[1]][dims[0]];
            }
            int cpt10 = 0;
            int i, j;
            int value;
            for (i = 0; i < matrice.length; i++) {
                for (j = 0; j < matrice[i].length; j++) {
                    if (RaEqualX()) {
                        value = cpt10 * dims[0] + j;
                    } else {
                        value = cpt10 * dims[1] + j;
                    }
                    matrice[i][j] = img[value];
                    if (RaEqualX()) {
                        if (j == dims[0] - 1) {
                            cpt10++;
                        }
                    } else {
                        if (j == dims[1] - 1) {
                            cpt10++;
                        }
                    }
                }
            }
        }
        return matrice;
    }

    public void getBox(double[] coordsDeg, double[] dimDeg, String nomOut) throws FitsException, IOException {
        int[] middle = new int[2];
        int[] corner = new int[2];
        int[] dimPix = new int[2];
        int[] axeRaAndDec = new int[2];
        axeRaAndDec = whichAxeForRaDec();
        System.out.println(axeRaAndDec[0]);
        System.out.println(axeRaAndDec[1]);
        int[] coordsPix = toPixel(coordsDeg);
        dimPix[0] = toPixel(dimDeg[axeRaAndDec[0]], axeRaAndDec[0]);
        dimPix[1] = toPixel(dimDeg[axeRaAndDec[1]], axeRaAndDec[1]);
        middle[0] = (int) (0.5 * dimPix[axeRaAndDec[0]]);
        middle[1] = (int) (0.5 * dimPix[axeRaAndDec[1]]);
        corner[0] = (int) coordsPix[0] - middle[0];
        corner[1] = (int) coordsPix[1] - middle[1];
        logger_.log(Level.FINE, "RA => " + coordsDeg[0]);
        logger_.log(Level.FINE, "DEC => " + coordsDeg[1]);
        logger_.log(Level.FINE, "RA Pix => " + coordsPix[0]);
        logger_.log(Level.FINE, "DEC Pix => " + coordsPix[1]);
        float[][] img = this.getBounds(coordsPix, dimPix, corner);
        nom.tam.fits.Fits fNew = new nom.tam.fits.Fits();
        BasicHDU hh = FitsFactory.HDUFactory(img);
        fNew.addHDU(hh);
        Header hdro = getHDU(0).getHeader();
        Header hdrn = hh.getHeader();
        Iterator itero = hdro.iterator();
        while (itero.hasNext()) {
            nom.tam.fits.HeaderCard hc = (nom.tam.fits.HeaderCard) itero.next();
            if (hc.getKey().equals("BITPIX")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS1")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("SIMPLE")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS2")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("EXTEND")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("CRPIX1")) {
                if (!RaEqualX()) {
                    hdrn.addValue(hc.getKey(), middle[0] - this.decal_[0], "part of " + "");
                } else {
                    hdrn.addValue(hc.getKey(), middle[1] - this.decal_[1], "part of " + "");
                }
            } else if (hc.getKey().equals("CRPIX2")) {
                if (!RaEqualX()) {
                    hdrn.addValue(hc.getKey(), middle[1] - this.decal_[1], "part of " + "");
                } else {
                    hdrn.addValue(hc.getKey(), middle[0] - this.decal_[0], "part of " + "");
                }
            } else if (hc.getKey().equals("CRVAL1")) {
                hdrn.addValue(hc.getKey(), coordsDeg[0], "part of " + "");
            } else if (hc.getKey().equals("CRVAL2")) {
                hdrn.addValue(hc.getKey(), coordsDeg[1], "part of " + "");
            } else if (hc.getKey().equals("RA")) {
                hdrn.addValue(hc.getKey(), coordsDeg[0], "part of " + "");
            } else if (hc.getKey().equals("DEC")) {
                hdrn.addValue(hc.getKey(), coordsDeg[1], "part of " + "");
            } else if (hc.getKey().equals("CDELT1")) {
                hdrn.addValue(hc.getKey(), getHDU(0).getHeader().getDoubleValue("CDELT1"), "part of " + "");
            } else if (hc.getKey().equals("CDELT2")) {
                hdrn.addValue(hc.getKey(), getHDU(0).getHeader().getDoubleValue("CDELT2"), "part of " + "");
            } else {
                if (!hc.getKey().equals("END")) {
                    hdrn.addValue(hc.getKey(), hc.getValue(), hc.getComment());
                }
            }
        }
        FileOutputStream d1 = new FileOutputStream(nomOut);
        BufferedOutputStream d2 = new BufferedOutputStream(d1);
        DataOutputStream d3 = new DataOutputStream(d2);
        BufferedDataOutputStream s = new BufferedDataOutputStream(d3);
        fNew.write(s);
    }

    public void getBox(double[] coordsDeg, int[] dim, String nomOut) throws FitsException, IOException {
        int[] middle = new int[2];
        int[] corner = new int[2];
        int[] axeRaAndDec = new int[2];
        int[] coordsPix = toPixel(coordsDeg);
        axeRaAndDec = whichAxeForRaDec();
        middle[0] = (int) (0.5 * dim[axeRaAndDec[0]]);
        middle[1] = (int) (0.5 * dim[axeRaAndDec[1]]);
        corner[0] = (int) coordsPix[0] - middle[0];
        corner[1] = (int) coordsPix[1] - middle[1];
        logger_.log(Level.FINE, "RA => " + coordsDeg[0]);
        logger_.log(Level.FINE, "DEC => " + coordsDeg[1]);
        logger_.log(Level.FINE, "RA Pix => " + coordsPix[0]);
        logger_.log(Level.FINE, "DEC Pix => " + coordsPix[1]);
        float[][] img = getBounds(coordsPix, dim, corner);
        BasicHDU hh = FitsFactory.HDUFactory(img);
        super.addHDU(hh);
        nom.tam.fits.Fits fNew = new nom.tam.fits.Fits();
        fNew.addHDU(hh);
        Header hdro = getHDU(0).getHeader();
        Header hdrn = hh.getHeader();
        Iterator itero = hdro.iterator();
        while (itero.hasNext()) {
            nom.tam.fits.HeaderCard hc = (nom.tam.fits.HeaderCard) itero.next();
            if (hc.getKey().equals("BITPIX")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS1")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("SIMPLE")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("NAXIS2")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("EXTEND")) {
                hc = (nom.tam.fits.HeaderCard) itero.next();
            } else if (hc.getKey().equals("CRPIX1")) {
                if (!RaEqualX()) {
                    hdrn.addValue(hc.getKey(), middle[0] - this.decal_[0], "part of " + "");
                } else {
                    hdrn.addValue(hc.getKey(), middle[1] - this.decal_[1], "part of " + "");
                }
            } else if (hc.getKey().equals("CRPIX2")) {
                if (!RaEqualX()) {
                    hdrn.addValue(hc.getKey(), middle[1] - this.decal_[1], "part of " + "");
                } else {
                    hdrn.addValue(hc.getKey(), middle[0] - this.decal_[0], "part of " + "");
                }
            } else if (hc.getKey().equals("CRVAL1")) {
                hdrn.addValue(hc.getKey(), coordsDeg[0], "part of " + "");
            } else if (hc.getKey().equals("CRVAL2")) {
                hdrn.addValue(hc.getKey(), coordsDeg[1], "part of " + "");
            } else if (hc.getKey().equals("RA")) {
                hdrn.addValue(hc.getKey(), coordsDeg[0], "part of " + "");
            } else if (hc.getKey().equals("DEC")) {
                hdrn.addValue(hc.getKey(), coordsDeg[1], "part of " + "");
            } else if (hc.getKey().equals("CDELT1")) {
                hdrn.addValue(hc.getKey(), getHDU(0).getHeader().getDoubleValue("CDELT1"), "part of " + "");
            } else if (hc.getKey().equals("CDELT2")) {
                hdrn.addValue(hc.getKey(), getHDU(0).getHeader().getDoubleValue("CDELT2"), "part of " + "");
            } else {
                if (!hc.getKey().equals("END")) {
                    hdrn.addValue(hc.getKey(), hc.getValue(), hc.getComment());
                }
            }
        }
        FileOutputStream d1 = new FileOutputStream(nomOut);
        BufferedOutputStream d2 = new BufferedOutputStream(d1);
        DataOutputStream d3 = new DataOutputStream(d2);
        BufferedDataOutputStream s = new BufferedDataOutputStream(d3);
        fNew.write(s);
    }

    private double[] getCDs() throws FitsException, IOException {
        BasicHDU hdu = getHDU(0);
        Header hdr = hdu.getHeader();
        int[] val;
        int[] position = positionRaDec();
        double ra = hdr.getDoubleValue("PC1_" + position[0], 99999);
        double dec = hdr.getDoubleValue("PC2_" + position[1], 99999);
        if (ra == 99999 || dec == 99999) {
            double incA = hdr.getDoubleValue("CDELT1", 99999);
            double incD = hdr.getDoubleValue("CDELT2", 99999);
            if (incA == 99999 || incD == 99999) {
                logger_.log(Level.SEVERE, "KeyWord \"PC\" isn't present in this fits");
                System.exit(-1);
            }
            double rota1 = hdr.getDoubleValue("CROTA1", 0);
            double rota2 = hdr.getDoubleValue("CROTA2", 0);
            double rota = rota1;
            if (rota1 == 0) {
                rota = rota2;
            } else {
                if (rota2 != 0) {
                    rota = (rota1 + rota2) / 2;
                }
            }
            ra = incA * Math.cos((rota / 180.) * Math.PI);
            dec = incD * Math.cos((rota / 180.) * Math.PI);
        }
        return new double[] { ra, dec };
    }

    private int[] whichAxeForRaDec() throws FitsException, IOException {
        double[] PC = getCDs();
        int[] val;
        if (PC[0] != 0.0 && PC[1] != 0.0) {
            val = new int[] { 0, 1 };
            return val;
        } else {
            val = new int[] { 1, 0 };
            return val;
        }
    }

    private int[] positionRaDec() throws FitsException, IOException {
        BasicHDU hdu = getHDU(0);
        Header hdr = hdu.getHeader();
        String ctype = hdr.getStringValue("CTYPE1");
        if (ctype == null) {
            logger_.log(Level.SEVERE, "KeyWord \"CTYPE\" isn't present in this fits");
            System.exit(-1);
        }
        if (ctype.equals("RA---TAN")) {
            int[] val = new int[] { 0, 1 };
            return val;
        } else {
            int[] val = new int[] { 1, 0 };
            return val;
        }
    }

    private boolean RaEqualX() throws FitsException, IOException {
        BasicHDU hdu = getHDU(0);
        Header hdr = hdu.getHeader();
        String ctype = hdr.getStringValue("CTYPE1");
        if (ctype == null) {
            logger_.log(Level.SEVERE, "KeyWord \"CTYPE\" isn't present in this fits");
            System.exit(-1);
        }
        double[] PC = getCDs();
        if (ctype.equals("RA---TAN")) {
            if (PC[0] != 0.0) {
                return true;
            } else {
                return false;
            }
        } else {
            if (PC[1] != 0.0) {
                return true;
            } else {
                return false;
            }
        }
    }

    private static final Logger logger_ = Logger.getLogger(Fits.class.getName());
}

;
