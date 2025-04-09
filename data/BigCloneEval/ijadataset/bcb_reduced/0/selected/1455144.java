package blue.ui.core.orchestra.editor.blueX7;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import blue.orchestra.BlueX7;
import blue.orchestra.blueX7.Operator;

/**
 * <p>
 * Title: blue
 * </p>
 * <p>
 * Description: an object composition environment for csound
 * </p>
 * <p>
 * Copyright: Copyright (c) 2001-2002
 * </p>
 * <p>
 * Company: steven yi music
 * </p>
 * 
 * @author unascribed
 * @version 1.0
 */
public class BlueX7SysexReader {

    public static final int SINGLE = 0;

    public static final int BANK = 1;

    public static final int START_OFFSET = 6;

    public static final int NAME_OFFSET = 118;

    public BlueX7SysexReader() {
    }

    public static int getSysexType(byte[] sysex) {
        if (sysex.length == 4104) {
            return BANK;
        }
        if (sysex.length == 163) {
            return SINGLE;
        }
        return -1;
    }

    public static String[] getNameListFromBank(byte[] sysex) {
        String[] names = new String[32];
        for (int i = 0; i < 32; i++) {
            names[i] = "";
            for (int j = 0; j < 10; j++) {
                names[i] += (char) (sysex[(128 * i) + NAME_OFFSET + START_OFFSET + j]);
            }
        }
        return names;
    }

    public static final void importFromSinglePatch(BlueX7 blueX7, byte[] sysex) {
        for (int i = 0; i < 6; i++) {
            mapOperatorFromSingle(blueX7, sysex, i);
        }
        int offset = START_OFFSET;
        offset += 126;
        blueX7.peg[0].x = sysex[offset++];
        blueX7.peg[1].x = sysex[offset++];
        blueX7.peg[2].x = sysex[offset++];
        blueX7.peg[3].x = sysex[offset++];
        blueX7.peg[0].y = sysex[offset++];
        blueX7.peg[1].y = sysex[offset++];
        blueX7.peg[2].y = sysex[offset++];
        blueX7.peg[3].y = sysex[offset++];
        blueX7.algorithmCommon.algorithm = sysex[offset++] + 1;
        blueX7.algorithmCommon.feedback = sysex[offset++];
        int temp = sysex[offset++];
        for (int i = 0; i < blueX7.operators.length; i++) {
            blueX7.operators[i].sync = temp;
        }
        blueX7.lfo.speed = sysex[offset++];
        blueX7.lfo.delay = sysex[offset++];
        blueX7.lfo.PMD = sysex[offset++];
        blueX7.lfo.AMD = sysex[offset++];
        blueX7.lfo.sync = sysex[offset++];
        blueX7.lfo.wave = sysex[offset++];
        temp = sysex[offset++];
        for (int i = 0; i < blueX7.operators.length; i++) {
            blueX7.operators[i].modulationPitch = temp;
        }
        blueX7.algorithmCommon.keyTranspose = sysex[offset++];
        temp = sysex[offset + 10];
        for (int i = 0; i < blueX7.algorithmCommon.operators.length; i++) {
            blueX7.algorithmCommon.operators[i] = true;
        }
    }

    public static final void importFromBank(BlueX7 blueX7, byte[] sysex, int patchNum) {
        for (int i = 0; i < 6; i++) {
            mapOperatorFromBank(blueX7, sysex, patchNum, i);
        }
        int offset = START_OFFSET;
        offset += patchNum * 128;
        offset += 102;
        blueX7.peg[0].x = sysex[offset++];
        blueX7.peg[1].x = sysex[offset++];
        blueX7.peg[2].x = sysex[offset++];
        blueX7.peg[3].x = sysex[offset++];
        blueX7.peg[0].y = sysex[offset++];
        blueX7.peg[1].y = sysex[offset++];
        blueX7.peg[2].y = sysex[offset++];
        blueX7.peg[3].y = sysex[offset++];
        blueX7.algorithmCommon.algorithm = sysex[offset++] + 1;
        int temp = sysex[offset++];
        int val1 = temp & 7;
        int val2 = (temp & 8) >>> 3;
        blueX7.algorithmCommon.feedback = val1;
        for (int i = 0; i < blueX7.operators.length; i++) {
            blueX7.operators[i].sync = val2;
        }
        blueX7.lfo.speed = sysex[offset++];
        blueX7.lfo.delay = sysex[offset++];
        blueX7.lfo.PMD = sysex[offset++];
        blueX7.lfo.AMD = sysex[offset++];
        temp = sysex[offset++];
        val1 = temp & 1;
        val2 = (temp & 14) >>> 1;
        int val3 = (temp & 112) >>> 4;
        blueX7.lfo.sync = val1;
        blueX7.lfo.wave = val2;
        for (int i = 0; i < blueX7.operators.length; i++) {
            blueX7.operators[i].modulationPitch = val3;
        }
        blueX7.algorithmCommon.keyTranspose = sysex[offset++];
    }

    public static final void mapOperatorFromSingle(BlueX7 blueX7, byte[] sysex, int operatorNum) {
        Operator op = blueX7.operators[operatorNum];
        int offset = START_OFFSET;
        offset += (5 - operatorNum) * 21;
        op.envelopePoints[0].x = sysex[offset++];
        op.envelopePoints[1].x = sysex[offset++];
        op.envelopePoints[2].x = sysex[offset++];
        op.envelopePoints[3].x = sysex[offset++];
        op.envelopePoints[0].y = sysex[offset++];
        op.envelopePoints[1].y = sysex[offset++];
        op.envelopePoints[2].y = sysex[offset++];
        op.envelopePoints[3].y = sysex[offset++];
        op.breakpoint = sysex[offset++];
        op.depthLeft = sysex[offset++];
        op.depthRight = sysex[offset++];
        op.curveLeft = sysex[offset++];
        op.curveRight = sysex[offset++];
        op.keyboardRateScaling = sysex[offset++];
        op.modulationAmplitude = sysex[offset++];
        op.velocitySensitivity = sysex[offset++];
        op.outputLevel = sysex[offset++];
        op.mode = sysex[offset++];
        op.freqCoarse = sysex[offset++];
        op.freqFine = sysex[offset++];
        op.detune = sysex[offset++] - 7;
    }

    public static final void mapOperatorFromBank(BlueX7 blueX7, byte[] sysex, int patchNum, int operatorNum) {
        Operator op = blueX7.operators[operatorNum];
        int offset = START_OFFSET;
        offset += patchNum * 128;
        offset += (5 - operatorNum) * 17;
        op.envelopePoints[0].x = sysex[offset++];
        op.envelopePoints[1].x = sysex[offset++];
        op.envelopePoints[2].x = sysex[offset++];
        op.envelopePoints[3].x = sysex[offset++];
        op.envelopePoints[0].y = sysex[offset++];
        op.envelopePoints[1].y = sysex[offset++];
        op.envelopePoints[2].y = sysex[offset++];
        op.envelopePoints[3].y = sysex[offset++];
        op.breakpoint = sysex[offset++];
        op.depthLeft = sysex[offset++];
        op.depthRight = sysex[offset++];
        int temp = sysex[offset++];
        int val1 = temp & 3;
        int val2 = (temp & 12) >>> 2;
        op.curveLeft = val2;
        op.curveRight = val1;
        temp = sysex[offset++];
        val1 = temp & 7;
        val2 = (temp & 112) >>> 3;
        op.keyboardRateScaling = val1;
        op.detune = val2 - 7;
        temp = sysex[offset++];
        val1 = temp & 3;
        val2 = (temp & 56) >>> 2;
        op.modulationAmplitude = val1;
        op.velocitySensitivity = val2;
        op.outputLevel = sysex[offset++];
        temp = sysex[offset++];
        val1 = temp & 1;
        val2 = (temp & 62) >>> 1;
        op.mode = val1;
        op.freqCoarse = val2;
        op.freqFine = sysex[offset++];
    }

    public static final byte[] fileToByteArray(File f) {
        try {
            long len = f.length();
            byte[] bytes = new byte[(int) len];
            FileInputStream fin = new FileInputStream(f);
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = fin.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + f.getName());
            }
            fin.close();
            return bytes;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        File f = new File("/home/steven/dx72csnd/dx72csnd/GUITAR1.DX7");
        byte[] sysex = BlueX7SysexReader.fileToByteArray(f);
        if (sysex == null) {
            System.err.println("[error] could not convert file to byte array");
            return;
        }
        System.out.println(BlueX7SysexReader.getSysexType(sysex));
        String[] names = BlueX7SysexReader.getNameListFromBank(sysex);
    }
}
