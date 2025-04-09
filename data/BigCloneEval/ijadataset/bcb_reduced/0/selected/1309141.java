package shu.cms.gma.gbd;

import java.io.*;
import shu.cms.colorformat.adapter.*;
import shu.cms.colorspace.depend.*;
import shu.cms.colorspace.independ.*;
import shu.cms.dc.*;
import shu.cms.devicemodel.dc.*;
import shu.cms.profile.*;
import shu.math.*;
import shu.math.lut.*;
import shu.util.*;
import shu.math.array.*;
import shu.cms.devicemodel.dc.dcam.*;

/**
 * <p>Title: Colour Management System</p>
 *
 * <p>Description: </p>
 * �HRGB�ƭȪ��X�z�ʨӭp�������.
 *
 * �o�Ӥ�k���lcd model��G�S��...
 * �]��lcd model(�ר�Omulti matrix����P�_�O�_�����줺�Υ~)
 * �γ\multi matrix���Ӵ��Ӥ�V, ���n��inverse model(�]���w��rgb���T�ʫ���)
 * �Ӹӥ�forward model???
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author cms.shu.edu.tw
 * @version 1.0
 */
public abstract class GamutBoundaryRGBDescriptor extends GamutBoundaryDescriptor implements Serializable {

    public static class D65ThresholdDescriptor extends GamutBoundaryRGBDescriptor implements Serializable {

        protected D65ThresholdDescriptor() {
        }

        private static final long serialVersionUID = 8683452581122892181L;

        protected boolean onLineCalculate = true;

        protected double[] tmpValues = new double[3];

        protected RGB rgb = new RGB(RGB.ColorSpace.unknowRGB, null, RGB.MaxValue.Double1);

        protected double[] whiteXYZValues;

        /**
     * �p��max chroma��, �t���n�p�󦹭�, �~����max chroma
     */
        protected double threshold = .5;

        protected TetrahedralInterpolation ti;

        protected CIELCh.XYZValuesRetriever retriever = null;

        public void setXYZValuesRetriever(CIELCh.XYZValuesRetriever retriever) {
            this.retriever = retriever;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public D65ThresholdDescriptor(ProfileColorSpace profileColorSpace) {
            super(profileColorSpace);
            whiteXYZValues = profileColorSpace.getReferenceWhite().getValues();
        }

        public D65ThresholdDescriptor(ProfileColorSpace profileColorSpace, TetrahedralInterpolation tetrahedralInterpolation) {
            this(profileColorSpace);
            ti = tetrahedralInterpolation;
            onLineCalculate = false;
        }

        protected double[] getXYZValues(CIELCh D65LCh) {
            D65LCh.getValues(tmpValues);
            if (retriever != null) {
                return retriever.getXYZValues(tmpValues);
            } else {
                double[] LabValues = CIELCh.toLabValues(tmpValues);
                double[] XYZValues = CIELab.toXYZValues(LabValues, whiteXYZValues);
                return XYZValues;
            }
        }

        public boolean isOutOfGamut(CIELCh D65LCh) {
            double[] XYZValues = getXYZValues(D65LCh);
            double[] rgbValues = pcs.fromCIEXYZValues(XYZValues);
            rgb.setValues(rgbValues);
            boolean isLegal = rgb.isLegal();
            return !isLegal;
        }

        public CIELCh getBoundaryLCh(final CIELCh D65LCh) {
            CIELCh test = (CIELCh) D65LCh.clone();
            if (test.L == 0 || test.L == 100) {
                return test;
            }
            if (onLineCalculate) {
                while (!isOutOfGamut(test)) {
                    test.C *= 1.5;
                }
                double max = test.C;
                double min = D65LCh.C;
                do {
                    test.C = (max + min) / 2;
                    if (!isOutOfGamut(test)) {
                        min = test.C;
                    } else {
                        max = test.C;
                    }
                } while ((max - min) > threshold);
                test.C = max;
            } else {
                double[] XYZValues = getXYZValues(D65LCh);
                double[] rgbValues = pcs.fromCIEXYZValues(XYZValues);
                rgbValues = RGB.rationalize(rgbValues, RGB.MaxValue.Double1);
                double[] chroma = ti.getValues(rgbValues);
                test.C = chroma[0];
            }
            return test;
        }
    }

    protected static class StepDescriptor extends GamutBoundaryRGBDescriptor {

        public StepDescriptor(ProfileColorSpace profileColorSpace) {
            super(profileColorSpace);
        }

        protected double[] tmpValues = new double[3];

        protected RGB rgb = new RGB(RGB.ColorSpace.unknowRGB, null, RGB.MaxValue.Double1);

        protected double[] tmpValues2 = new double[3];

        protected RGB rgb2 = new RGB(RGB.ColorSpace.unknowRGB, null, RGB.MaxValue.Double1);

        public boolean isOutOfGamut(CIELCh LCh) {
            LCh.getValues(tmpValues);
            double[] rgbValues = pcs.fromPCSCIELChValues(tmpValues);
            rgb.setValues(rgbValues);
            return !rgb.isLegal();
        }

        /**
     * �p����I�ҹ�M�쪺���
     *
     * @param LCh CIELCh
     * @return CIELCh
     */
        public CIELCh getBoundaryLCh(CIELCh LCh) {
            LCh.getValues(tmpValues);
            double[] rgbValues = pcs.fromPCSCIELChValues(tmpValues);
            rgb.setValues(rgbValues);
            if (!rgb.isLegal()) {
                rgb.rationalize();
                return new CIELCh(pcs.toPCSCIELChValues(rgb.getValues()));
            }
            double[] boundary = DoubleArray.copy(tmpValues);
            double step = (tmpValues[1] == 0) ? 5 : tmpValues[1] / 2.;
            double irrationalValue = 0;
            while (true) {
                tmpValues[1] += step;
                rgb.setValues(pcs.fromPCSCIELChValues(tmpValues));
                if (!rgb.isLegalAfter8BitQuantization()) {
                    DoubleArray.copy(tmpValues, tmpValues2);
                    tmpValues2[1] += 1;
                    rgb2.setValues(pcs.fromPCSCIELChValues(tmpValues2));
                    irrationalValue = rgb2.irrationalValueSum() - rgb.irrationalValueSum();
                    if (irrationalValue < 0) {
                        continue;
                    }
                    step = .5;
                    DoubleArray.copy(boundary, tmpValues);
                    while (true) {
                        tmpValues[1] += step;
                        rgb.setValues(pcs.fromPCSCIELChValues(tmpValues));
                        if (!rgb.isLegal()) {
                            return new CIELCh(boundary);
                        } else {
                            DoubleArray.copy(tmpValues, boundary);
                        }
                    }
                } else {
                    DoubleArray.copy(tmpValues, boundary);
                }
            }
        }
    }

    /**
   *
   * <p>Title: Colour Management System</p>
   *
   * <p>Description: a Colour Management System by Java</p>
   * ��M�����ɪ���k
   *
   * <p>Copyright: Copyright (c) 2008</p>
   *
   * <p>Company: skygroup</p>
   *
   * @author skyforce
   * @version 1.0
   */
    public static enum Style {

        Step, D65Threshold
    }

    protected ProfileColorSpace pcs;

    public GamutBoundaryRGBDescriptor(ProfileColorSpace profileColorSpace) {
        this.pcs = profileColorSpace;
    }

    protected GamutBoundaryRGBDescriptor() {
    }

    protected static final int RGB_STEP = 5;

    public static final D65ThresholdDescriptor storeD65ThresholdDescriptor(RGB.ColorSpace rgbColorSpace, String filename) {
        ProfileColorSpace pcs = ProfileColorSpace.Instance.get(rgbColorSpace);
        D65ThresholdDescriptor gbd = getOffLineD65ThresholdInstance(pcs);
        Persistence.writeObject(gbd, filename);
        return gbd;
    }

    public static final D65ThresholdDescriptor storeD65ThresholdDescriptor(RGB.ColorSpace rgbColorSpace, CIELCh.XYZValuesRetriever retriever, String filename) {
        ProfileColorSpace pcs = ProfileColorSpace.Instance.get(rgbColorSpace);
        D65ThresholdDescriptor gbd = getOffLineD65ThresholdInstance(pcs, retriever);
        Persistence.writeObject(gbd, filename);
        return gbd;
    }

    public static final D65ThresholdDescriptor loadD65ThresholdDescriptor(String filename) {
        return (D65ThresholdDescriptor) Persistence.readObject(filename);
    }

    protected static final D65ThresholdDescriptor getOffLineD65ThresholdInstance(ProfileColorSpace pcs) {
        return getOffLineD65ThresholdInstance(pcs, new CIELabRetriever(pcs.getReferenceWhite().getValues()));
    }

    private static class CIELabRetriever implements CIELCh.XYZValuesRetriever {

        private double[] whiteXYZValues;

        private CIELabRetriever(double[] whiteXYZValues) {
            this.whiteXYZValues = whiteXYZValues;
        }

        public double[] getXYZValues(double[] LChValues) {
            double[] LabValues = CIELCh.toLabValues(LChValues);
            double[] XYZValues = CIELab.toXYZValues(LabValues, whiteXYZValues);
            return XYZValues;
        }

        ;

        public double[] getLChValues(double[] XYZValues) {
            double[] LabValues = CIELab.fromXYZValues(XYZValues, whiteXYZValues);
            double[] LChValues = CIELCh.fromLabValues(LabValues);
            return LChValues;
        }

        ;
    }

    protected static final D65ThresholdDescriptor getOffLineD65ThresholdInstance(ProfileColorSpace pcs, CIELCh.XYZValuesRetriever retriever) {
        D65ThresholdDescriptor onLine = (D65ThresholdDescriptor) getInstance(Style.D65Threshold, pcs);
        onLine.setXYZValuesRetriever(retriever);
        int step = RGB_STEP;
        int grid = (255 / step) + 1;
        int size = (int) Math.pow(grid, 3);
        double[][][] lut = new double[size][2][];
        int index = 0;
        CIELCh LCh = new CIELCh();
        for (int r = 0; r < 256; r += step) {
            for (int g = 0; g < 256; g += step) {
                for (int b = 0; b < 256; b += step) {
                    double[] rgbValues = new double[] { r / 255., g / 255., b / 255. };
                    double[] XYZValues = pcs.toCIEXYZValues(rgbValues);
                    double[] LChValues = retriever.getLChValues(XYZValues);
                    LCh.setValues(LChValues);
                    if (LCh.L == 0) {
                        lut[index][0] = rgbValues;
                        lut[index][1] = new double[] { 0 };
                    } else {
                        CIELCh boundary = onLine.getBoundaryLCh(LCh);
                        lut[index][0] = rgbValues;
                        lut[index][1] = new double[] { boundary.C };
                    }
                    index++;
                }
            }
        }
        CubeTable cubeTable = new CubeTable(lut, new double[] { 0, 0, 0 }, new double[] { 1, 1, 1 }, grid);
        TetrahedralInterpolation ti = new TetrahedralInterpolation(cubeTable);
        return new D65ThresholdDescriptor(pcs, ti);
    }

    public static final GamutBoundaryRGBDescriptor getInstance(Style style, ProfileColorSpace pcs) {
        switch(style) {
            case Step:
                return new StepDescriptor(pcs);
            case D65Threshold:
                return new D65ThresholdDescriptor(pcs);
        }
        return null;
    }

    public static void main(String[] args) {
    }

    public static void DCExample(String[] args) {
        GMBICCProfileAdapter profile = new GMBICCProfileAdapter("Measurement Files/Camera/S5Pro/s5p-f2.icc");
        LightSource.Source lightsource = LightSource.CIE.D65;
        DCChartAdapter chart = new DCChartAdapter(DCTarget.Chart.CC24, lightsource);
        DCTarget target = DCTarget.Instance.get(profile, chart, lightsource, DCTarget.Chart.CC24);
        DCAppearanceModel.Style style = DCAppearanceModel.Style.CIECAM02;
        DCAppearanceModel model = new DCAppearanceModel(target, style, RGB.ColorSpace.sRGB);
        storeD65ThresholdDescriptor(RGB.ColorSpace.sRGB, model.getXYZRetriever(), "sRGB-" + style.name() + ".gbd");
    }
}
