public class Test {    public Layer2(BitStream bitstream, Header h, Synthesis filter, int wch) {
        header = h;
        bs = bitstream;
        this.filter = filter;
        nbal = new byte[5][];
        nbal[0] = new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2 };
        nbal[1] = new byte[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2 };
        nbal[2] = new byte[] { 4, 4, 3, 3, 3, 3, 3, 3 };
        nbal[3] = new byte[] { 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 };
        nbal[4] = new byte[] { 4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 };
        factor = new float[] { 2.00000000000000f, 1.58740105196820f, 1.25992104989487f, 1.00000000000000f, 0.79370052598410f, 0.62996052494744f, 0.50000000000000f, 0.39685026299205f, 0.31498026247372f, 0.25000000000000f, 0.19842513149602f, 0.15749013123686f, 0.12500000000000f, 0.09921256574801f, 0.07874506561843f, 0.06250000000000f, 0.04960628287401f, 0.03937253280921f, 0.03125000000000f, 0.02480314143700f, 0.01968626640461f, 0.01562500000000f, 0.01240157071850f, 0.00984313320230f, 0.00781250000000f, 0.00620078535925f, 0.00492156660115f, 0.00390625000000f, 0.00310039267963f, 0.00246078330058f, 0.00195312500000f, 0.00155019633981f, 0.00123039165029f, 0.00097656250000f, 0.00077509816991f, 0.00061519582514f, 0.00048828125000f, 0.00038754908495f, 0.00030759791257f, 0.00024414062500f, 0.00019377454248f, 0.00015379895629f, 0.00012207031250f, 0.00009688727124f, 0.00007689947814f, 0.00006103515625f, 0.00004844363562f, 0.00003844973907f, 0.00003051757813f, 0.00002422181781f, 0.00001922486954f, 0.00001525878906f, 0.00001211090890f, 0.00000961243477f, 0.00000762939453f, 0.00000605545445f, 0.00000480621738f, 0.00000381469727f, 0.00000302772723f, 0.00000240310869f, 0.00000190734863f, 0.00000151386361f, 0.00000120155435f };
        cq_steps = new int[] { 3, 5, 7, 9, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535 };
        cq_C = new float[] { 1.3333333f, 1.6f, 1.1428571f, 1.77777778f, 1.0666667f, 1.0322581f, 1.015873f, 1.007874f, 1.0039216f, 1.0019569f, 1.0009775f, 1.0004885f, 1.0002442f, 1.000122f, 1.000061f, 1.0000305f, 1.00001525902f };
        cq_D = new float[] { 0.5f, 0.5f, 0.25f, 0.5f, 0.125f, 0.0625f, 0.03125f, 0.015625f, 0.0078125f, 0.00390625f, 0.001953125f, 0.0009765625f, 0.00048828125f, 0.00024414063f, 0.00012207031f, 0.00006103516f, 0.00003051758f };
        cq_bits = new byte[] { 5, 7, 3, 10, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        sbquant_offset = new byte[][] { { 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0 }, { 7, 7, 7, 6, 6, 6, 6, 6, 6, 6, 6, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 }, { 5, 5, 2, 2, 2, 2, 2, 2 }, { 5, 5, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 }, { 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } };
        bitalloc_offset = new byte[] { 0, 3, 3, 1, 2, 3, 4, 5 };
        offset_table = new byte[][] { { 0, 1, 16 }, { 0, 1, 2, 3, 4, 5, 16 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 }, { 0, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16 }, { 0, 2, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 } };
        group = new byte[] { 2, 3, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        allocation = new byte[2][32];
        scfsi = new byte[2][32];
        scalefactor = new byte[2][32][3];
        samplecode = new int[3];
        syin = new float[2][3][32];
        nch = header.getChannels();
        int bitrate = header.getBitrate() / nch;
        if (header.getVersion() == Header.MPEG1) if (bitrate <= 48) aidx = (bitrate == 32) ? 3 : 2; else if (bitrate <= 80) aidx = 0; else aidx = (bitrate == 48) ? 0 : 1; else aidx = 4;
        byte[] limit = { 27, 30, 8, 12, 30 };
        sblimit = limit[aidx];
    }
}