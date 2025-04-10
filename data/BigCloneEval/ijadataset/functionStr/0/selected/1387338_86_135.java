public class Test {    public int[] getMPSblmPvs(final String initPvStr, final String orgPvStr) {
        String[] chNames = { "", "", "", "", "", "", "", "", "", "" };
        MPSblmMap = new LinkedHashMap();
        String chName = orgPvStr + ":DbgHVBias";
        chNames[0] = chName;
        Channel ch_1 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgHVBiasRb";
        chNames[1] = chName;
        Channel ch_2 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgHVCurrentRb";
        chNames[2] = chName;
        Channel ch_3 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgAFEFirstStageGainRb";
        chNames[3] = chName;
        Channel ch_4 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgSlowPulseLossRb";
        chNames[4] = chName;
        Channel ch_5 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgMPSPulseLossLimit";
        chNames[5] = chName;
        Channel ch_6 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":DbgMPS600PulsesLossLimit";
        chNames[6] = chName;
        Channel ch_7 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":nom_trip";
        chNames[7] = chName;
        Channel ch_8 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":tune_trip";
        chNames[8] = chName;
        Channel ch_9 = ChannelFactory.defaultFactory().getChannel(chName);
        chName = orgPvStr + ":user_trip";
        chNames[9] = chName;
        Channel ch_10 = ChannelFactory.defaultFactory().getChannel(chName);
        MPSblmPVs_get[0] = getValue(ch_1);
        MPSblmPVs_get[1] = getValue(ch_2);
        MPSblmPVs_get[2] = getValue(ch_3);
        MPSblmPVs_get[3] = getValue(ch_4);
        MPSblmPVs_get[4] = getValue(ch_5);
        MPSblmPVs_get[5] = getValue(ch_6);
        MPSblmPVs_get[6] = getValue(ch_7);
        MPSblmPVs_get[7] = getValue(ch_8);
        MPSblmPVs_get[8] = getValue(ch_9);
        MPSblmPVs_get[9] = getValue(ch_10);
        String str;
        for (int i = 0; i < 10; i++) {
            str = "" + MPSblmPVs_get[i];
            MPSblmMap.put(chNames[i], str);
        }
        return MPSblmPVs_get;
    }
}