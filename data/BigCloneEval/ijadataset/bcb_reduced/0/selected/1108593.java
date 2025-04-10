package net.sourceforge.jaad.aac.syntax;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.ChannelConfiguration;
import net.sourceforge.jaad.aac.DecoderConfig;
import net.sourceforge.jaad.aac.Profile;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.aac.SampleFrequency;
import net.sourceforge.jaad.aac.filterbank.FilterBank;
import net.sourceforge.jaad.aac.tools.LTPrediction;
import net.sourceforge.jaad.aac.sbr2.SBR;
import net.sourceforge.jaad.aac.tools.IS;
import net.sourceforge.jaad.aac.tools.MS;
import java.util.logging.Level;

public class SyntacticElements implements Constants {

    private DecoderConfig config;

    private boolean sbrPresent, psPresent;

    private int bitsRead;

    private final PCE pce;

    private final Element[] elements;

    private final CCE[] cces;

    private final DSE[] dses;

    private final FIL[] fils;

    private int curElem, curCCE, curDSE, curFIL;

    private float[][] data;

    public SyntacticElements(DecoderConfig config) {
        this.config = config;
        pce = new PCE();
        elements = new Element[4 * MAX_ELEMENTS];
        cces = new CCE[MAX_ELEMENTS];
        dses = new DSE[MAX_ELEMENTS];
        fils = new FIL[MAX_ELEMENTS];
        startNewFrame();
    }

    public final void startNewFrame() {
        curElem = 0;
        curCCE = 0;
        curDSE = 0;
        curFIL = 0;
        sbrPresent = false;
        psPresent = false;
        bitsRead = 0;
    }

    public void decode(BitStream in) throws AACException {
        final int start = in.getPosition();
        int type;
        Element prev = null;
        boolean content = true;
        if (!config.getProfile().isErrorResilientProfile()) {
            while (content && (type = in.readBits(3)) != ELEMENT_END) {
                switch(type) {
                    case ELEMENT_SCE:
                    case ELEMENT_LFE:
                        LOGGER.finest("SCE");
                        prev = decodeSCE_LFE(in);
                        break;
                    case ELEMENT_CPE:
                        LOGGER.finest("CPE");
                        prev = decodeCPE(in);
                        break;
                    case ELEMENT_CCE:
                        LOGGER.finest("CCE");
                        decodeCCE(in);
                        prev = null;
                        break;
                    case ELEMENT_DSE:
                        LOGGER.finest("DSE");
                        decodeDSE(in);
                        prev = null;
                        break;
                    case ELEMENT_PCE:
                        LOGGER.finest("PCE");
                        decodePCE(in);
                        prev = null;
                        break;
                    case ELEMENT_FIL:
                        LOGGER.finest("FIL");
                        decodeFIL(in, prev);
                        prev = null;
                        break;
                }
            }
            LOGGER.finest("END");
            content = false;
            prev = null;
        } else {
            switch(config.getChannelConfiguration()) {
                case CHANNEL_CONFIG_MONO:
                    decodeSCE_LFE(in);
                    break;
                case CHANNEL_CONFIG_STEREO:
                    decodeCPE(in);
                    break;
                case CHANNEL_CONFIG_STEREO_PLUS_CENTER:
                    decodeSCE_LFE(in);
                    decodeCPE(in);
                    break;
                case CHANNEL_CONFIG_STEREO_PLUS_CENTER_PLUS_REAR_MONO:
                    decodeSCE_LFE(in);
                    decodeCPE(in);
                    decodeSCE_LFE(in);
                    break;
                case CHANNEL_CONFIG_FIVE:
                    decodeSCE_LFE(in);
                    decodeCPE(in);
                    decodeCPE(in);
                    break;
                case CHANNEL_CONFIG_FIVE_PLUS_ONE:
                    decodeSCE_LFE(in);
                    decodeCPE(in);
                    decodeCPE(in);
                    decodeSCE_LFE(in);
                    break;
                case CHANNEL_CONFIG_SEVEN_PLUS_ONE:
                    decodeSCE_LFE(in);
                    decodeCPE(in);
                    decodeCPE(in);
                    decodeCPE(in);
                    decodeSCE_LFE(in);
                    break;
                default:
                    throw new AACException("unsupported channel configuration for error resilience: " + config.getChannelConfiguration());
            }
        }
        in.byteAlign();
        bitsRead = in.getPosition() - start;
    }

    private Element decodeSCE_LFE(BitStream in) throws AACException {
        if (elements[curElem] == null) elements[curElem] = new SCE_LFE(config.getFrameLength());
        ((SCE_LFE) elements[curElem]).decode(in, config);
        curElem++;
        return elements[curElem - 1];
    }

    private Element decodeCPE(BitStream in) throws AACException {
        if (elements[curElem] == null) elements[curElem] = new CPE(config.getFrameLength());
        ((CPE) elements[curElem]).decode(in, config);
        curElem++;
        return elements[curElem - 1];
    }

    private void decodeCCE(BitStream in) throws AACException {
        if (curCCE == MAX_ELEMENTS) throw new AACException("too much CCE elements");
        if (cces[curCCE] == null) cces[curCCE] = new CCE(config.getFrameLength());
        cces[curCCE].decode(in, config);
        curCCE++;
    }

    private void decodeDSE(BitStream in) throws AACException {
        if (curDSE == MAX_ELEMENTS) throw new AACException("too much CCE elements");
        if (dses[curDSE] == null) dses[curDSE] = new DSE();
        dses[curDSE].decode(in);
        curDSE++;
    }

    private void decodePCE(BitStream in) throws AACException {
        pce.decode(in);
        config.setProfile(pce.getProfile());
        config.setSampleFrequency(pce.getSampleFrequency());
        config.setChannelConfiguration(ChannelConfiguration.forInt(pce.getChannelCount()));
    }

    private void decodeFIL(BitStream in, Element prev) throws AACException {
        if (curFIL == MAX_ELEMENTS) throw new AACException("too much FIL elements");
        if (fils[curFIL] == null) fils[curFIL] = new FIL(config.isSBRDownSampled());
        fils[curFIL].decode(in, prev, config.getSampleFrequency());
        curFIL++;
        if (prev != null && prev.isSBRPresent()) {
            sbrPresent = true;
            if (!psPresent && prev.getSBR().isPSUsed()) psPresent = true;
        }
    }

    public void process(FilterBank filterBank) throws AACException {
        final Profile profile = config.getProfile();
        final SampleFrequency sf = config.getSampleFrequency();
        int chs = config.getChannelConfiguration().getChannelCount();
        if (chs == 1 && psPresent) chs++;
        final int mult = sbrPresent ? 2 : 1;
        if (data == null || chs != data.length || (mult * config.getFrameLength()) != data[0].length) data = new float[chs][mult * config.getFrameLength()];
        int channel = 0;
        Element e;
        SCE_LFE scelfe;
        CPE cpe;
        for (int i = 0; i < elements.length && channel < chs; i++) {
            e = elements[i];
            if (e == null) continue;
            if (e instanceof SCE_LFE) {
                scelfe = (SCE_LFE) e;
                channel += processSingle(scelfe, filterBank, channel, profile, sf);
            } else if (e instanceof CPE) {
                cpe = (CPE) e;
                processPair(cpe, filterBank, channel, profile, sf);
                channel += 2;
            } else if (e instanceof CCE) {
                ((CCE) e).process();
                channel++;
            }
        }
    }

    private int processSingle(SCE_LFE scelfe, FilterBank filterBank, int channel, Profile profile, SampleFrequency sf) throws AACException {
        final ICStream ics = scelfe.getICStream();
        final ICSInfo info = ics.getInfo();
        final LTPrediction ltp = info.getLTPrediction1();
        final int elementID = scelfe.getElementInstanceTag();
        final float[] iqData = ics.getInvQuantData();
        if (profile.equals(Profile.AAC_MAIN) && info.isICPredictionPresent()) info.getICPrediction().process(ics, iqData, sf);
        if (LTPrediction.isLTPProfile(profile) && info.isLTPrediction1Present()) ltp.process(ics, iqData, filterBank, sf);
        processDependentCoupling(false, elementID, CCE.BEFORE_TNS, iqData, null);
        if (ics.isTNSDataPresent()) ics.getTNS().process(ics, iqData, sf, false);
        processDependentCoupling(false, elementID, CCE.AFTER_TNS, iqData, null);
        filterBank.process(info.getWindowSequence(), info.getWindowShape(ICSInfo.CURRENT), info.getWindowShape(ICSInfo.PREVIOUS), iqData, data[channel], channel);
        if (LTPrediction.isLTPProfile(profile)) ltp.updateState(data[channel], filterBank.getOverlap(channel), profile);
        processIndependentCoupling(false, elementID, data[channel], null);
        if (ics.isGainControlPresent()) ics.getGainControl().process(iqData, info.getWindowShape(ICSInfo.CURRENT), info.getWindowShape(ICSInfo.PREVIOUS), info.getWindowSequence());
        int chs = 1;
        if (sbrPresent) {
            if (data[channel].length == config.getFrameLength()) LOGGER.log(Level.WARNING, "SBR data present, but buffer has normal size!");
            final SBR sbr = scelfe.getSBR();
            if (sbr.isPSUsed()) {
                chs = 2;
                scelfe.getSBR().process(data[channel], data[channel + 1], false);
            } else scelfe.getSBR().process(data[channel], null, false);
        }
        return chs;
    }

    private void processPair(CPE cpe, FilterBank filterBank, int channel, Profile profile, SampleFrequency sf) throws AACException {
        final ICStream ics1 = cpe.getLeftChannel();
        final ICStream ics2 = cpe.getRightChannel();
        final ICSInfo info1 = ics1.getInfo();
        final ICSInfo info2 = ics2.getInfo();
        final LTPrediction ltp1 = info1.getLTPrediction1();
        final LTPrediction ltp2 = cpe.isCommonWindow() ? info1.getLTPrediction2() : info2.getLTPrediction1();
        final int elementID = cpe.getElementInstanceTag();
        final float[] iqData1 = ics1.getInvQuantData();
        final float[] iqData2 = ics2.getInvQuantData();
        if (cpe.isCommonWindow() && cpe.isMSMaskPresent()) MS.process(cpe, iqData1, iqData2);
        if (profile.equals(Profile.AAC_MAIN)) {
            if (info1.isICPredictionPresent()) info1.getICPrediction().process(ics1, iqData1, sf);
            if (info2.isICPredictionPresent()) info2.getICPrediction().process(ics2, iqData2, sf);
        }
        IS.process(cpe, iqData1, iqData2);
        if (LTPrediction.isLTPProfile(profile)) {
            if (info1.isLTPrediction1Present()) ltp1.process(ics1, iqData1, filterBank, sf);
            if (cpe.isCommonWindow() && info1.isLTPrediction2Present()) ltp2.process(ics2, iqData2, filterBank, sf); else if (info2.isLTPrediction1Present()) ltp2.process(ics2, iqData2, filterBank, sf);
        }
        processDependentCoupling(true, elementID, CCE.BEFORE_TNS, iqData1, iqData2);
        if (ics1.isTNSDataPresent()) ics1.getTNS().process(ics1, iqData1, sf, false);
        if (ics2.isTNSDataPresent()) ics2.getTNS().process(ics2, iqData2, sf, false);
        processDependentCoupling(true, elementID, CCE.AFTER_TNS, iqData1, iqData2);
        filterBank.process(info1.getWindowSequence(), info1.getWindowShape(ICSInfo.CURRENT), info1.getWindowShape(ICSInfo.PREVIOUS), iqData1, data[channel], channel);
        filterBank.process(info2.getWindowSequence(), info2.getWindowShape(ICSInfo.CURRENT), info2.getWindowShape(ICSInfo.PREVIOUS), iqData2, data[channel + 1], channel + 1);
        if (LTPrediction.isLTPProfile(profile)) {
            ltp1.updateState(data[channel], filterBank.getOverlap(channel), profile);
            ltp2.updateState(data[channel + 1], filterBank.getOverlap(channel + 1), profile);
        }
        processIndependentCoupling(true, elementID, data[channel], data[channel + 1]);
        if (ics1.isGainControlPresent()) ics1.getGainControl().process(iqData1, info1.getWindowShape(ICSInfo.CURRENT), info1.getWindowShape(ICSInfo.PREVIOUS), info1.getWindowSequence());
        if (ics2.isGainControlPresent()) ics2.getGainControl().process(iqData2, info2.getWindowShape(ICSInfo.CURRENT), info2.getWindowShape(ICSInfo.PREVIOUS), info2.getWindowSequence());
        if (sbrPresent) {
            cpe.getSBR().process(data[channel], data[channel + 1], false);
        }
    }

    private void processIndependentCoupling(boolean channelPair, int elementID, float[] data1, float[] data2) {
        int index, c, chSelect;
        CCE cce;
        for (int i = 0; i < cces.length; i++) {
            cce = cces[i];
            index = 0;
            if (cce != null && cce.getCouplingPoint() == CCE.AFTER_IMDCT) {
                for (c = 0; c <= cce.getCoupledCount(); c++) {
                    chSelect = cce.getCHSelect(c);
                    if (cce.isChannelPair(c) == channelPair && cce.getIDSelect(c) == elementID) {
                        if (chSelect != 1) {
                            cce.applyIndependentCoupling(index, data1);
                            if (chSelect != 0) index++;
                        }
                        if (chSelect != 2) {
                            cce.applyIndependentCoupling(index, data2);
                            index++;
                        }
                    } else index += 1 + ((chSelect == 3) ? 1 : 0);
                }
            }
        }
    }

    private void processDependentCoupling(boolean channelPair, int elementID, int couplingPoint, float[] data1, float[] data2) {
        int index, c, chSelect;
        CCE cce;
        for (int i = 0; i < cces.length; i++) {
            cce = cces[i];
            index = 0;
            if (cce != null && cce.getCouplingPoint() == couplingPoint) {
                for (c = 0; c <= cce.getCoupledCount(); c++) {
                    chSelect = cce.getCHSelect(c);
                    if (cce.isChannelPair(c) == channelPair && cce.getIDSelect(c) == elementID) {
                        if (chSelect != 1) {
                            cce.applyDependentCoupling(index, data1);
                            if (chSelect != 0) index++;
                        }
                        if (chSelect != 2) {
                            cce.applyDependentCoupling(index, data2);
                            index++;
                        }
                    } else index += 1 + ((chSelect == 3) ? 1 : 0);
                }
            }
        }
    }

    public void sendToOutput(SampleBuffer buffer) {
        final boolean be = buffer.isBigEndian();
        final int chs = data.length;
        final int mult = (sbrPresent) ? 2 : 1;
        final int length = mult * config.getFrameLength();
        final int freq = mult * config.getSampleFrequency().getFrequency();
        byte[] b = buffer.getData();
        if (b.length != chs * length * 2) b = new byte[chs * length * 2];
        float[] cur;
        int i, j, off;
        short s;
        for (i = 0; i < chs; i++) {
            cur = data[i];
            for (j = 0; j < length; j++) {
                s = (short) Math.max(Math.min(Math.round(cur[j]), Short.MAX_VALUE), Short.MIN_VALUE);
                off = (j * chs + i) * 2;
                if (be) {
                    b[off] = (byte) ((s >> 8) & BYTE_MASK);
                    b[off + 1] = (byte) (s & BYTE_MASK);
                } else {
                    b[off + 1] = (byte) ((s >> 8) & BYTE_MASK);
                    b[off] = (byte) (s & BYTE_MASK);
                }
            }
        }
        buffer.setData(b, freq, chs, 16, bitsRead);
    }
}
