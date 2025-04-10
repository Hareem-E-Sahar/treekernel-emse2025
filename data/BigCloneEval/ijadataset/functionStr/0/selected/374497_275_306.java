public class Test {    protected boolean updateScanVariables(BPM bpm1, BPM bpm2, RfCavity cav, CurrentMonitor bcm) {
        graphScanOn.removeAllGraphData();
        graphScanOff.removeAllGraphData();
        cavAmpPVName = (cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE)).getId();
        cavPhasePVName = cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE).getId();
        BPM1AmpPVName = bpm1.getChannel(BPM.AMP_AVG_HANDLE).getId();
        BPM1PhasePVName = bpm1.getChannel(BPM.PHASE_AVG_HANDLE).getId();
        BPM2AmpPVName = bpm2.getChannel(BPM.AMP_AVG_HANDLE).getId();
        BPM2PhasePVName = bpm2.getChannel(BPM.PHASE_AVG_HANDLE).getId();
        cavAmpRBPVName = cavAmpPVName.replaceFirst("CtlAmpSet", "cavAmpAvg");
        if (bcm != null) BCMPVName = bcm.getId() + ":currentAvg";
        cavAmpRBChan = ChannelFactory.defaultFactory().getChannel(cavAmpRBPVName);
        if (bcm != null) BCMChan = ChannelFactory.defaultFactory().getChannel(BCMPVName);
        scanVariable.setChannel(cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE));
        BPM1PhaseOnMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM1AmpOnMV.setChannel(bpm1.getChannel(BPM.AMP_AVG_HANDLE));
        BPM2PhaseOnMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM2AmpOnMV.setChannel(bpm2.getChannel(BPM.AMP_AVG_HANDLE));
        cavAmpRBOnMV.setChannel(cavAmpRBChan);
        BCMOnMV.setChannel(BCMChan);
        BPM1PhaseOffMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM1AmpOffMV.setChannel(bpm1.getChannel(BPM.AMP_AVG_HANDLE));
        BPM2PhaseOffMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM2AmpOffMV.setChannel(bpm2.getChannel(BPM.AMP_AVG_HANDLE));
        BCMOffMV.setChannel(BCMChan);
        connectChannels(bpm1, bpm2, cav);
        scanControllerOn.setScanVariable(scanVariable);
        scanControllerOff.setScanVariable(scanVariable);
        setPVText();
        setTitles(controller.tuneSet.getCavity().getId());
        if (connectionMap.containsValue(new Boolean(false))) return false; else return true;
    }
}