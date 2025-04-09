package shu.cms.lcd.calibrate.measured;

import java.util.*;
import shu.cms.*;
import shu.cms.colorspace.depend.*;
import shu.cms.colorspace.independ.*;
import shu.cms.hvs.gradient.*;
import shu.cms.hvs.gradient.Pattern;
import shu.cms.lcd.*;
import shu.cms.lcd.calibrate.*;
import shu.cms.lcd.calibrate.measured.util.*;
import shu.cms.lcd.calibrate.parameter.*;
import shu.cms.measure.*;
import shu.cms.util.*;
import shu.math.*;
import shu.math.array.*;
import shu.util.log.*;

/**
 * <p>Title: Colour Management System</p>
 *
 * <p>Description: a Colour Management System by Java</p>
 * 1.�w��ե�: ����ڶq��G�P�ؼЭȪ��t��, ����p�t���H�������覡�վ�code, �Ϩ�JNDI����ؼЭ�.
 * �t�׳̧�, �ֳt����ؼЭ�.
 *
 * 2.�̤p�~�t�ե�: �M��A�H�q��v�B�վ��̱���ؼЭ�.
 * �̪�ɶ�, ��O�~�t�̤p.
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: skygroup</p>
 *
 * @author skyforce
 * @version 1.0
 */
public class JNDICalibrator extends MeasuredCalibrator {

    public JNDICalibrator(LCDTarget logoLCDTaget, MeterMeasurement meterMeasurement, ColorProofParameter p, AdjustParameter ap, MeasureParameter mp) {
        this(logoLCDTaget, meterMeasurement, p, ap, mp, RGBBase.Channel.W, null);
    }

    /**
   * �ѬY��MeasuredCalibrator�����@JNDI�ե�
   * @param logoLCDTaget LCDTarget �ؼ�LCDTarget
   * @param measuredCalibrator MeasuredCalibrator �I�s�ӷ���MeasuredCalibrator
   * @param channel Channel �ե��W�D
   * @param originalRampLCDTarget LCDTarget ��l���ե���ramp LCDTarget, �ΨӧP�_
   * smooth�{�ת�
   */
    JNDICalibrator(LCDTarget logoLCDTaget, MeasuredCalibrator measuredCalibrator, RGBBase.Channel channel, LCDTarget originalRampLCDTarget) {
        super(logoLCDTaget, measuredCalibrator);
        init(channel, originalRampLCDTarget);
        this.step = this.maxValue.getStepIn255();
    }

    /**
   * ��l��
   * @param channel Channel
   * @param originalRampLCDTarget LCDTarget
   */
    private void init(RGBBase.Channel channel, LCDTarget originalRampLCDTarget) {
        this.calibratedChannel = channel;
        this.originalRampLCDTarget = originalRampLCDTarget;
    }

    public JNDICalibrator(LCDTarget logoLCDTaget, MeterMeasurement meterMeasurement, ColorProofParameter p, AdjustParameter ap, MeasureParameter mp, RGBBase.Channel channel, LCDTarget originalRampLCDTarget) {
        super(logoLCDTaget, meterMeasurement, p, null, ap, mp);
        init(channel, originalRampLCDTarget);
        this.step = this.maxValue.getStepIn255();
    }

    private double step;

    /**
   * �n�ե����W�D
   */
    private RGBBase.Channel calibratedChannel = RGBBase.Channel.W;

    /**
   * �w��ե����G
   */
    private RGB[] estimateRGBArray;

    /**
   * �̤p�~�t�ե����G
   */
    private RGB[] minDeltaRGBArray;

    /**
   * ��J�ե����G
   */
    private RGB[] compromiseRGBArray;

    /**
   * �O�_�n�i��̤p�~�t�ե�
   */
    private boolean minDeltaCalibrate = true;

    /**
   * �O�_�i��w��ե�
   */
    private boolean estimateCalibrate = false;

    /**
   * �O�_�i���J�ե�, �N�O�b�̤p�����ؼ��I�����p�U, �Ϩ�smooth
   */
    private boolean compromiseCalibrate = false;

    /**
   * �]�w�n�i�檺�ե�
   * @param estimateCalibrate boolean �w��ե�, �O�_�n�i��w��ե�, �ιw��覡��G��,
   *  ��ֹ�ڶq��ɶ��ӷl.
   * @param minDeltaCalibrate boolean �̤p�~�t�ե�, �O�_�n�i�� �̤p�~�t�ե�. ���}�|���
   * ���ɶ��Ԫ�(�w�]�O���}��)
   * @param compromiseCalibrate boolean ���(��J)�ե�
   */
    public void setCalibrate(boolean estimateCalibrate, boolean minDeltaCalibrate, boolean compromiseCalibrate) {
        this.estimateCalibrate = estimateCalibrate;
        this.minDeltaCalibrate = minDeltaCalibrate;
        this.compromiseCalibrate = compromiseCalibrate;
    }

    /**
   * �ˬdcp code�O�_���T
   * �p�G���O��(�]�N�OR/G/B), �N�n�T�wcp code�u�����W�D���ƭ�
   * @return boolean
   */
    protected boolean checkCPCodeRGBArray() {
        if (this.calibratedChannel != RGBBase.Channel.W) {
            RGB[] cpCode = this.getCPCodeRGBArray();
            for (RGB rgb : cpCode) {
                if (!rgb.isBlack() && (rgb.getZeroChannelCount() != 2 || rgb.getMaxChannel() != this.calibratedChannel)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class MeasureResult {

        /**
     * �P�ؼЭȪ�delta JNDI Array
     */
        private double[] deltaArray;

        /**
     * �q��G��LCDTarget
     */
        private LCDTarget measuredLCDTarget;

        private MeasureResult(LCDTarget measuredLCDTarget, double[] deltaArray) {
            this.measuredLCDTarget = measuredLCDTarget;
            this.deltaArray = deltaArray;
        }
    }

    /**
   * ��J, �q��, ødelta �åB�x�s
   * @param rgbArray RGB[]
   * @param title String
   * @param storeFilename String
   * @param plot boolean �O�_�nødelta
   * @param store boolean �O�_�n�x�s
   * @return double[]
   */
    protected MeasureResult loadMeasurePlotAndStore(RGB[] rgbArray, String title, String storeFilename, boolean plot, boolean store) {
        RGB[] cpcodeArray = this.getCPCodeRGBArray();
        RGB white = cpcodeArray[cpcodeArray.length - 1];
        LCDTarget target = this.measure(rgbArray, this.calibratedChannel, white, true, false);
        List<Patch> measurePatchList = target.getPatchList();
        double[] delta = jndi.plotDeltaJNDI(title, plot, measurePatchList);
        MeasureResult result = new MeasureResult(target, delta);
        if (store && storeFilename != null) {
            CalibrateUtils.storeRGBArrayExcel(rgbArray, storeFilename, cp);
        }
        return result;
    }

    public RGB[] _calibrate() {
        this.setChromaticityRelative(false);
        LCDTarget target = initMeasure(this.calibratedChannel, true);
        double[] delta1Array = jndi.calculateDeltaJNDICurve(target);
        jndi.plotDeltaJNDI(delta1Array, "delta");
        RGB[] calibratedRGBArray = null;
        if (estimateCalibrate) {
            estimateRGBArray = estimateCalibrate(target, maxValue);
            delta1Array = loadMeasurePlotAndStore(estimateRGBArray, "1", rootDir + "/" + JNDIEstimate, true, true).deltaArray;
            calibratedRGBArray = Arrays.copyOf(estimateRGBArray, estimateRGBArray.length);
        } else {
            RGB[] cpcodeRGBArray = this.getCPCodeRGBArray();
            calibratedRGBArray = Arrays.copyOf(cpcodeRGBArray, cpcodeRGBArray.length);
        }
        if (minDeltaCalibrate) {
            minDeltaRGBArray = minimumDeltaJNDICalibrate2(calibratedRGBArray, delta1Array);
            if (this.calibratedChannel != RGBBase.Channel.W) {
                IrregularUtil.irregularFix(minDeltaRGBArray, this.calibratedChannel);
            }
            loadMeasurePlotAndStore(minDeltaRGBArray, "2", rootDir + "/" + JNDIMinDelta, true, true);
            calibratedRGBArray = Arrays.copyOf(minDeltaRGBArray, minDeltaRGBArray.length);
        }
        if (compromiseCalibrate) {
            compromiseRGBArray = compromiseCalibrate(calibratedRGBArray);
            loadMeasurePlotAndStore(compromiseRGBArray, "4", rootDir + "/" + JNDICompromise, true, true);
            calibratedRGBArray = Arrays.copyOf(compromiseRGBArray, compromiseRGBArray.length);
        }
        CalibrateUtils.storeRGBArrayExcel(calibratedRGBArray, rootDir + "/" + JNDIFinal, cp);
        return calibratedRGBArray;
    }

    /**
   * �qmeasurePatchList���ͥXLCDTarget��Compromise�ե��ϥ�
   * @param measurePatchList List
   * @return LCDTarget
   */
    protected LCDTarget getCompromiseLCDTarget(List<Patch> measurePatchList) {
        List<Patch> patchList = new ArrayList<Patch>(measurePatchList);
        LCDTarget.Number number = MeasuredUtils.getMeasureNumber(this.calibratedChannel, true, false);
        LCDTarget measureLCDTarget = LCDTarget.Instance.get(patchList, number, this.mm.isDo255InverseMode());
        return measureLCDTarget;
    }

    public List<Patch> getCalibratedPatchList() {
        if (minDeltaCalibrate) {
            if (minDeltaRGBArray == null) {
                throw new IllegalStateException("minDeltaRGBArray == null");
            } else {
                return LCDTargetUtils.getReplacedPatchList(getCalibratedTarget(), this.minDeltaRGBArray);
            }
        } else {
            if (estimateRGBArray == null) {
                throw new IllegalStateException("estimateRGBArray == null");
            } else {
                return LCDTargetUtils.getReplacedPatchList(getCalibratedTarget(), this.estimateRGBArray);
            }
        }
    }

    /**
   * ���JNDI�~�t�̤p��code
   * @param calibratedRGBArray RGB[]
   * @param delta1Array double[]
   * @return RGB[]
   */
    protected RGB[] minimumDeltaJNDICalibrate2(final RGB[] calibratedRGBArray, final double[] delta1Array) {
        MinimumDeltaCalibrator calibrator = new MinimumDeltaCalibrator(calibratedRGBArray, delta1Array);
        RGB[] result = calibrator.calibrate();
        return result;
    }

    protected double getJNDI(CIEXYZ XYZ) {
        return jndi.getJNDI(XYZ);
    }

    /**
   *
   * <p>Title: Colour Management System</p>
   *
   * <p>Description: a Colour Management System by Java</p>
   * �վ��k���^�����O
   *
   * <p>Copyright: Copyright (c) 2008</p>
   *
   * <p>Company: skygroup</p>
   *
   * @author skyforce
   * @version 1.0
   */
    private static class AdjustResult {

        /**
     * �վ㪺���ޭ�
     */
        private int adjustIndex;

        /**
     * �վ㪺��V
     */
        private boolean upAdjust;

        /**
     * �վ�᪺rgb
     */
        private RGB adjustedRGB;

        private AdjustResult(int adjustIndex, boolean upAdjust, RGB adjustedRGB) {
            this.adjustIndex = adjustIndex;
            this.upAdjust = upAdjust;
            this.adjustedRGB = adjustedRGB;
        }

        /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
        public String toString() {
            return "Index(" + adjustIndex + ") rgb:" + adjustedRGB + " Up:" + upAdjust;
        }
    }

    /**
   * �O�_���S�w��pattern
   * @param patternList List �n�ˬd��pattern List
   * @param negativePattern boolean true:�ˬd�t��pattern false:�ˬd����pattern
   * @return boolean
   */
    private static final boolean hasSpecificPattern(List<Pattern> patternList, boolean negativePattern) {
        for (Pattern pattern : patternList) {
            if (pattern.pattern < 0 == negativePattern) {
                return true;
            }
        }
        return false;
    }

    /**
   * ������J�ե�
   */
    private boolean positiveCompromiseCalibrate = true;

    /**
   * �t����J�ե�
   */
    private boolean negativeCompromiseCalibrate = true;

    /**
   * ��J�ե�������
   */
    private int compromiseCalibrateCount;

    /**
   * �qcode�������[�t�׮t������¦, �b�ǫץH�Υ��Ƥ��������
   * @param calibratedRGBArray RGB[]
   * @return RGB[]
   */
    protected RGB[] compromiseCalibrate(final RGB[] calibratedRGBArray) {
        CompromiseCalibrator calibrator = new CompromiseCalibrator(calibratedRGBArray);
        RGB[] result = calibrator.calibrate();
        compromiseCalibrateCount = calibrator.getAdjustCount();
        return result;
    }

    protected static interface CalibratorIF {

        public RGB[] calibrate();

        public int getAdjustCount();
    }

    protected class MinimumDeltaCalibrator implements CalibratorIF {

        private RGB[] calibratedRGBArray;

        private double[] deltaArray;

        protected MinimumDeltaCalibrator(final RGB[] calibratedRGBArray, final double[] deltaArray) {
            this.calibratedRGBArray = calibratedRGBArray;
            this.deltaArray = deltaArray;
        }

        public RGB[] calibrate() {
            final double step = maxValue.getStepIn255();
            final double[] targetJNDIArray = jndi.getTargetJNDICurve();
            final boolean[] calibrated = new boolean[256];
            calibrated[0] = calibrated[255] = true;
            for (int x = 1; x < 255; x++) {
                final int index = x;
                new Thread() {

                    private StringBuilder buf = new StringBuilder();

                    public void run() {
                        double delta = deltaArray[index];
                        if (delta != 0) {
                            buf.append(index + " start adjust (" + calibratedRGBArray[index] + ")\n");
                        } else {
                            calibrated[index] = true;
                        }
                        while (delta != 0) {
                            RGB measureRGB = (RGB) calibratedRGBArray[index].clone();
                            double adjuststep = delta > 0 ? -step : step;
                            measureRGB.addValues(adjuststep);
                            if (calibratedChannel != RGBBase.Channel.W) {
                                measureRGB.reserveValue(calibratedChannel);
                            }
                            if (!measureRGB.isLegal()) {
                                buf.append(index + " abnormal adjust end\n");
                                calibrated[index] = true;
                                break;
                            }
                            Patch p = mi.measure(measureRGB);
                            buf.append(index + " measure " + p + '\n');
                            double JNDI = getJNDI(p.getXYZ());
                            double newdelta = JNDI - targetJNDIArray[index];
                            if (newdelta * delta < 0) {
                                if (Math.abs(delta) > Math.abs(newdelta)) {
                                    calibratedRGBArray[index] = measureRGB;
                                }
                                buf.append(index + " adjust end (" + calibratedRGBArray[index] + ")\n");
                                calibrated[index] = true;
                                break;
                            }
                            delta = newdelta;
                            calibratedRGBArray[index] = measureRGB;
                        }
                        traceDetail(buf.toString());
                    }
                }.start();
            }
            mi.triggerMeasure(new Trigger(calibrated));
            traceDetail("measure end");
            return calibratedRGBArray;
        }

        public int getAdjustCount() {
            return -1;
        }
    }

    /**
   *
   * <p>Title: Colour Management System</p>
   *
   * <p>Description: a Colour Management System by Java</p>
   * smooth�վ�t��k
   *
   * check��index��2:
   * 1.sum of "�W�Xthreshold pattern��pattern"
   * 2.sum of delta a
   * �C���վ�n�ŦX���u��Ǭ�  1.1�n<=�վ�e 2.2�n<=�վ�e, �åB��̤p��
   *
   * �t��k���y�{��:
   * 1. ���score�̤j��pattern, ��code��n
   * 2. ��pattern�� n-1, n, n+1 ���O���վ�,�վ�n�̷�pattern�����A�өw.
   *    �p�G�O+pattern �h�O -,+,-�վ�. �p�G�O-pattern �h�O+,-,+
   * 3. ���O��T�ؽվ�p��index1��index2, ��g�Lindex1������, �Y�q�L, �A�Hindex2����
   *    �ç�̤p��.
   *
   * �B�z����h��:
   * 1. ��B�z����pattern, �]���H���勵pattern�����P��;
   *    ��pattern�O�G�u(��M�W�ܦh), �tpattern�O�t�u(��M���U��).
   *    �G�t�u�q�`�O�@�_�X�{��(�G�u�ǴN�O�t�u).
   * 2. �ˬd�O�_�٦�����pattern, �p�G���N��⥿pattern�B�z��; �B�z���A�ӧ˭tpattern
   * 3. �B�zpattern�ĥΦh������B�z, ���n��U�@�ӵ��W�@��, ��cp code�ɶq�뺡
   * 4. ���F�קK�B�z�ɦ��ҭ���, �C�ӳQ�B�z��pattern���j�n�b3�H�W(�t3), �קK�p��[�t�׮t�ɤ��ۤz�Z
   * 5. �B�z���ǫh�Oindex1�n<= �վ�e, index2�n<= �վ�e �åB��̤p��.
   *
   * <p>Copyright: Copyright (c) 2008</p>
   *
   * <p>Company: skygroup</p>
   *
   * @author skyforce
   * @version 1.0
   */
    protected class CompromiseCalibrator implements CalibratorIF {

        private RGB[] originalRGBArray;

        private RGB[] calibratedRGBArray;

        /**
     * �ΨӦ����վ�L���զX
     */
        private Set<RGB> adjustSet = new HashSet<RGB>();

        /**
     * ��l���վ�e�ҭp�⪺check Index
     */
        private double[] originalCheckIndex;

        /**
     * �p��Ȧs�Ϊ�checkIndex Array
     */
        private double[][] tmpCheckIndexArray;

        /**
     * ��o��l��checking value
     * @return double
     */
        private double getOriginalCheckingValue() {
            if (originalCheckIndex != null) {
                return originalCheckIndex[1];
            } else {
                return -1;
            }
        }

        protected CompromiseCalibrator(RGB[] originalRGBArray) {
            this.originalRGBArray = originalRGBArray;
            loadMeasurePlotAndStore(originalRGBArray, "3", rootDir + "/" + JNDICalibrated, true, true);
            calibratedRGBArray = Arrays.copyOf(originalRGBArray, originalRGBArray.length);
        }

        private boolean hasPositivePattern(GSDFGradientModel.PatternAndScore pas) {
            boolean hasPositivePattern = positiveCompromiseCalibrate && hasSpecificPattern(pas.patternList, false);
            return hasPositivePattern;
        }

        public RGB[] calibrate() {
            GSDFGradientModel.PatternAndScore pas = getPatternAndScore(originalRGBArray);
            originalCheckIndex = getCheckIndex(pas);
            boolean hasPositivePattern = hasPositivePattern(pas);
            boolean skipPositivePattern = false;
            for (adjustCount = 0; !pas.isSmooth(); adjustCount++) {
                List<Pattern> patternList = new ArrayList<Pattern>(pas.patternList);
                Collections.sort(patternList, patternComparator);
                boolean adjusted = false;
                Pattern preAdjustedPattern = null;
                for (Pattern pattern : patternList) {
                    if (((!positiveCompromiseCalibrate || skipPositivePattern) && pattern.pattern > 0) || ((!negativeCompromiseCalibrate || (hasPositivePattern && !skipPositivePattern)) && pattern.pattern < 0)) {
                        continue;
                    }
                    if (preAdjustedPattern != null && (pattern.index - preAdjustedPattern.index) < 3) {
                        continue;
                    }
                    AdjustResult result = getBestAdjustResult(pattern, calibratedRGBArray);
                    if (result != null && !adjustSet.contains(result.adjustedRGB)) {
                        RGB rgb = result.adjustedRGB;
                        calibratedRGBArray[result.adjustIndex] = rgb;
                        preAdjustedPattern = pattern;
                        adjustSet.add(rgb);
                        adjusted = true;
                        Logger.log.trace("[AdjustResult] " + result);
                    }
                }
                if (!adjusted) {
                    if (skipPositivePattern) {
                        Logger.log.trace("Non-calibrated, stop!");
                        break;
                    } else {
                        skipPositivePattern = true;
                    }
                }
                pas = getPatternAndScore(calibratedRGBArray);
                hasPositivePattern = hasPositivePattern(pas);
            }
            return calibratedRGBArray;
        }

        private GSDFGradientModel.PatternAndScore getPatternAndScore(RGB[] measuredRGBArray) {
            loadMeasurePlotAndStore(measuredRGBArray, null, null, false, false);
            LCDTarget target = getMeasuredLCDTarget();
            GSDFGradientModel gm = getGSDFGradientModel(target);
            GSDFGradientModel.PatternAndScore pas = gm.getPatternAndScore();
            return pas;
        }

        public int getAdjustCount() {
            return adjustCount;
        }

        private int adjustCount;

        private static final int AdjustItemCount = 3;

        private static final int MimimumCPCode = 0;

        private static final int MaximumCPCode = 255;

        /**
     * �q�T�ؽվ�覡�����̾A�X���վ�k
     * @param pattern Pattern
     * @param originalRGBArray RGB[]
     * @return AdjustResult
     */
        private AdjustResult getBestAdjustResult(Pattern pattern, final RGB[] originalRGBArray) {
            RGB[] rgbArray1 = Arrays.copyOf(originalRGBArray, originalRGBArray.length);
            RGB[] rgbArray2 = Arrays.copyOf(originalRGBArray, originalRGBArray.length);
            RGB[] rgbArray3 = Arrays.copyOf(originalRGBArray, originalRGBArray.length);
            RGB[][] rgbArray = new RGB[][] { rgbArray1, rgbArray2, rgbArray3 };
            RGB[] adjustRGBArray = new RGB[AdjustItemCount];
            final boolean positivePattern = pattern.pattern > 0;
            final int[] adjustIndexArray = new int[] { pattern.index - 1, pattern.index, pattern.index + 1 };
            final boolean[] adjustUpDirection = new boolean[] { !positivePattern, positivePattern, !positivePattern };
            GSDFGradientModel.PatternAndScore[] pasArray = new GSDFGradientModel.PatternAndScore[AdjustItemCount];
            Logger.log.trace("Candilate: ");
            for (int x = 0; x < AdjustItemCount; x++) {
                RGB[] array = rgbArray[x];
                int adjustIndex = adjustIndexArray[x];
                RGB rgb = (RGB) array[adjustIndex].clone();
                double adjustValue = rgb.getValue(calibratedChannel);
                double adjustStep = adjustUpDirection[x] ? step : -step;
                adjustValue += adjustStep;
                if (adjustValue <= MimimumCPCode || adjustValue >= MaximumCPCode) {
                    continue;
                }
                rgb.setValue(calibratedChannel, adjustValue);
                Logger.log.trace(array[adjustIndex] + "->" + rgb);
                adjustRGBArray[x] = rgb;
                array[adjustIndex] = rgb;
                MeasureResult result = loadMeasurePlotAndStore(array, null, null, false, false);
                GSDFGradientModel gm = getGSDFGradientModel(result.measuredLCDTarget);
                pasArray[x] = gm.getPatternAndScore();
            }
            double[] checkingValue = getCheckingValueArray(pasArray);
            double min = Maths.min(checkingValue);
            if (min < getOriginalCheckingValue()) {
                int minIndex = Maths.minIndex(checkingValue);
                double minCheckingValue = checkingValue[minIndex];
                if (minCheckingValue != Double.MAX_VALUE) {
                    AdjustResult result = new AdjustResult(pattern.index - 1 + minIndex, adjustUpDirection[minIndex], adjustRGBArray[minIndex]);
                    Logger.log.trace("min(" + min + ") < OriginalCheckingValue(" + getOriginalCheckingValue() + ")");
                    return result;
                }
            }
            return null;
        }

        private final GSDFGradientModel getGSDFGradientModel(LCDTarget lcdTarget) {
            GSDFGradientModel gm = new GSDFGradientModel(lcdTarget);
            gm.setImageChannel(calibratedChannel);
            gm.setPatternSign(GSDFGradientModel.PatternSign.Threshold);
            gm.setRecommendThresholdPercent(calibratedChannel);
            gm.setTargetxyYArray(getTargetxyYArray());
            gm.getAllPatternIndex();
            gm.statistics();
            return gm;
        }

        private final double[] getCheckingValueArray(GSDFGradientModel.PatternAndScore[] pasArray) {
            boolean[] check1Pass = getCheck1PassArray(pasArray);
            double[] check2Value = getCheck2ValueArray(pasArray, check1Pass);
            return check2Value;
        }

        private final double[] getCheck2ValueArray(GSDFGradientModel.PatternAndScore[] pasArray, boolean[] check1PassArray) {
            if (pasArray.length != check1PassArray.length) {
                throw new IllegalArgumentException("pasArray.length != check1PassArray.length");
            }
            double[] check2Value = new double[AdjustItemCount];
            int size = pasArray.length;
            for (int x = 0; x < size; x++) {
                boolean check1 = check1PassArray[x];
                if (false == check1) {
                    check2Value[x] = Double.MAX_VALUE;
                } else {
                    double[] checkIndex = tmpCheckIndexArray[x];
                    check2Value[x] = checkIndex[1];
                }
            }
            return check2Value;
        }

        /**
     * �P�_check1�O�_�q�L
     * @param pasArray PatternAndScore[]
     * @return boolean[]
     */
        private final boolean[] getCheck1PassArray(GSDFGradientModel.PatternAndScore[] pasArray) {
            boolean[] check1Pass = new boolean[AdjustItemCount];
            tmpCheckIndexArray = new double[AdjustItemCount][];
            for (int x = 0; x < AdjustItemCount; x++) {
                GSDFGradientModel.PatternAndScore pas = pasArray[x];
                if (pas == null) {
                    check1Pass[x] = false;
                    continue;
                }
                double[] checkIndex = getCheckIndex(pas);
                tmpCheckIndexArray[x] = checkIndex;
                if (checkIndex[0] > originalCheckIndex[0]) {
                    check1Pass[x] = false;
                    continue;
                }
                check1Pass[x] = true;
            }
            return check1Pass;
        }

        /**
     * ��pas�p��XcheckIndex 1 & 2
     * @param pas PatternAndScore
     * @return double[]
     */
        private final double[] getCheckIndex(GSDFGradientModel.PatternAndScore pas) {
            double sumPatternOfOverThreshold = getSumPatternOfOverThreshold(pas);
            double sumOfDeltaAccel = getSumOfDeltaAccel(pas);
            return new double[] { sumPatternOfOverThreshold, sumOfDeltaAccel };
        }

        /**
     * sum of delta a
     * @param pas PatternAndScore
     * @return double
     */
        private final double getSumOfDeltaAccel(GSDFGradientModel.PatternAndScore pas) {
            DoubleArray.abs(pas.deltaAccelArray);
            double sumOfdeltaa = Maths.sum(pas.deltaAccelArray);
            return sumOfdeltaa;
        }

        /**
     * sum of "�W�Xthreshold pattern��pattern"
     * @param pas PatternAndScore
     * @return double
     */
        private final double getSumPatternOfOverThreshold(GSDFGradientModel.PatternAndScore pas) {
            double sumPatternOfOverThreshold = 0;
            List<Pattern> patternList = pas.patternList;
            int size = patternList.size();
            for (int x = 0; x < size; x++) {
                Pattern p = patternList.get(x);
                if (Math.abs(p.overRatio) > 100.) {
                    sumPatternOfOverThreshold += Math.abs(p.pattern);
                }
            }
            return sumPatternOfOverThreshold;
        }

        protected RGB[] getCalibratedResult() {
            return calibratedRGBArray;
        }
    }

    private static PatternComparator patternComparator = new PatternComparator();

    /**
   *
   * <p>Title: Colour Management System</p>
   *
   * <p>Description: a Colour Management System by Java</p>
   * �f�tPatternComparator��ܪ�sort Index
   *
   * <p>Copyright: Copyright (c) 2008</p>
   *
   * <p>Company: skygroup</p>
   *
   * @author skyforce
   * @version 1.0
   */
    private static enum SortBy {

        Pattern, OverRatio, JNDIndex
    }

    /**
   *
   * <p>Title: Colour Management System</p>
   *
   * <p>Description: a Colour Management System by Java</p>
   * pattern���, �ΨӰ�pattern List�ƦC�Ϊ�.
   *
   * <p>Copyright: Copyright (c) 2008</p>
   *
   * <p>Company: skygroup</p>
   *
   * @author skyforce
   * @version 1.0
   */
    protected static class PatternComparator implements Comparator {

        private SortBy sortBy = SortBy.OverRatio;

        private boolean inverseSort = true;

        protected void setInverseSort(boolean inverseSort) {
            this.inverseSort = inverseSort;
        }

        protected void setSortBy(SortBy sortBy) {
            this.sortBy = sortBy;
        }

        /**
     * Compares its two arguments for order.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first
     *   argument is less than, equal to, or greater than the second.
     */
        public int compare(Object o1, Object o2) {
            Pattern p1 = (Pattern) o1;
            Pattern p2 = (Pattern) o2;
            double v1 = getValue(sortBy, p1);
            double v2 = getValue(sortBy, p2);
            return inverseSort ? Double.compare(v2, v1) : Double.compare(v1, v2);
        }

        private static double getValue(SortBy sortBy, Pattern pattern) {
            switch(sortBy) {
                case Pattern:
                    return pattern.pattern;
                case OverRatio:
                    return pattern.overRatio;
                case JNDIndex:
                    return pattern.jndIndex;
                default:
                    return -1;
            }
        }

        /**
     * Indicates whether some other object is &quot;equal to&quot; this
     * comparator.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> only if the specified object is also a
     *   comparator and it imposes the same ordering as this comparator.
     */
        public boolean equals(Object obj) {
            return false;
        }
    }

    /**
   * �p��panel��l�ե��W�D���C�@��code��jndi'
   * @param in256Level boolean �O�_�ȥH256�@�p��,�p�G��true, �h�u��256 level.
   *                           �p�G��false, �h������LCD���ѪR��(�p10bit��1021 level)
   * @return double[]
   */
    protected double[] getOriginalJNDIndexPrimeArray(boolean in256Level) {
        LCDTarget originalRamp = getOriginalRamp();
        LCDTargetInterpolator interp = LCDTargetInterpolator.Instance.get(originalRamp, calibratedChannel);
        GSDFGradientModel gm = new GSDFGradientModel(originalRamp);
        if (true == this.maxValue.integer) {
            RGB.MaxValue maxValue = in256Level ? RGB.MaxValue.Int8Bit : this.maxValue;
            int level = (int) maxValue.max + 1;
            double step = maxValue.getStepIn255();
            double[] jndiArray = new double[level];
            int index = 0;
            for (double code = 0; code <= 255; code += step) {
                double jndi = this.jndi.getJNDI(interp, code, this.calibratedChannel, gm);
                jndiArray[index] = jndi;
                index++;
            }
            double[] jndiPrimeArray = Maths.firstOrderDerivatives(jndiArray);
            return jndiPrimeArray;
        } else {
            throw new IllegalStateException("calibrated maxValue is not integer");
        }
    }

    /**
   * ��l��ramp LCDTarget
   */
    private LCDTarget originalRampLCDTarget;

    /**
   * ��o��l��ramp LCDTarget
   * @return LCDTarget
   */
    protected LCDTarget getOriginalRamp() {
        if (originalRampLCDTarget == null) {
            originalRampLCDTarget = measure(RGBArray.getOriginalRGBArray(), this.calibratedChannel, null, true, true);
        }
        return originalRampLCDTarget;
    }

    protected RGB[] estimateCalibrate(LCDTarget ramp, RGB.MaxValue maxValue) {
        double[] estimateResult = estimate(ramp, maxValue);
        RGB[] result = wcc.getRGBArray(estimateResult, maxValue);
        if (this.calibratedChannel != RGBBase.Channel.W) {
            for (RGB rgb : result) {
                rgb.reserveValue(this.calibratedChannel);
            }
        }
        return result;
    }

    /**
   * �q�q��G, �����X���ؼ�code�̱��񪺹��code.
   * @param code int
   * @param interp LCDTargetInterpolator
   * @param maxValue MaxValue
   * @return double
   */
    protected double getNearestMeasuredCode(int code, LCDTargetInterpolator interp, RGB.MaxValue maxValue) {
        double targetJNDI = jndi.getJNDI(getCalibratedTarget(), code);
        double measureJNDI = jndi.getJNDI(interp, code, this.calibratedChannel);
        double delta = measureJNDI - targetJNDI;
        if (delta == 0) {
            return code;
        }
        double step = delta > 0 ? -maxValue.getStepIn255() : maxValue.getStepIn255();
        double lastdelta = delta;
        for (double newcode = code + step; ; newcode += step) {
            double JNDI = jndi.getJNDI(interp, newcode, this.calibratedChannel);
            double newdelta = JNDI - targetJNDI;
            if (newdelta * delta < 0) {
                if (lastdelta < newdelta) {
                    return newcode - step;
                } else {
                    return newcode;
                }
            }
            lastdelta = newdelta;
            if (newcode > 255) {
                Logger.log.warn("newcode > 255, finish!");
                return 255;
            }
        }
    }

    /**
   * �p��q��ȸ�z�Q�Ȫ��~�t, �M��q�~�t����ե��᪺cp code
   * @param measure LCDTarget �q��쪺LCDTarget
   * @param maxValue MaxValue �ե���bit��
   * @return double[]
   */
    private double[] estimate(LCDTarget measure, RGB.MaxValue maxValue) {
        Interpolation.Algo[] algos = LCDTargetInterpolator.Find.optimumInterpolationType(measure, LCDTargetInterpolator.OptimumType.Max, calibratedChannel);
        LCDTargetInterpolator targetInterp = LCDTargetInterpolator.Instance.get(measure, algos, calibratedChannel);
        double[] origin = new double[256];
        double[] result = new double[256];
        for (int x = 1; x < 255; x++) {
            double nearestCode = getNearestMeasuredCode(x, targetInterp, maxValue);
            nearestCode = nearestCode > 255 ? 255 : nearestCode;
            nearestCode = nearestCode < 0 ? 0 : nearestCode;
            result[x] = nearestCode;
            origin[x] = x;
        }
        result[255] = origin[255] = 255;
        double[] targetGArray = WhiteCodeCalculator.getWhitecodeArray(getCPCodeRGBArray());
        Interpolation interp = new Interpolation(origin, targetGArray);
        double[] estimate = new double[256];
        for (int x = 1; x < 256; x++) {
            if (result[x] <= 0) {
                estimate[x] = targetGArray[x];
            } else {
                estimate[x] = interp.interpolate(result[x], Interpolation.Algo.Linear);
            }
        }
        return estimate;
    }

    /**
   * �O�_�n�i��w��ե�, �ιw��覡��G��, ��ֹ�ڶq��ɶ��ӷl.
   * @param estimateCalibrate boolean
   */
    public void setEstimateCalibrate(boolean estimateCalibrate) {
        this.estimateCalibrate = estimateCalibrate;
    }

    /**
   * ��o�ե��L�{����T
   * @return String
   * @todo M getCalibratedInfomation
   */
    public String getCalibratedInfomation() {
        return null;
    }
}
