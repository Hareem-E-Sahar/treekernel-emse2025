public class Test {    protected void updateScanVariables(BPM bpm1, BPM bpm2, RfCavity cav, CurrentMonitor bcm) {
        graphScan.removeAllGraphData();
        cavAmpPVName = (cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE)).getId();
        cavPhasePVName = cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE).getId();
        BPM1AmpPVName = bpm1.getChannel(BPM.AMP_AVG_HANDLE).getId();
        BPM1PhasePVName = bpm1.getChannel(BPM.PHASE_AVG_HANDLE).getId();
        BPM2AmpPVName = bpm2.getChannel(BPM.AMP_AVG_HANDLE).getId();
        BPM2PhasePVName = bpm2.getChannel(BPM.PHASE_AVG_HANDLE).getId();
        cavAmpRBPVName = cavAmpPVName.replaceFirst("CtlAmpSet", "cavAmpAvg");
        if (bcm != null) BCMPVName = bcm.getId() + ":currentAvg";
        System.out.println("pv = " + cavAmpRBPVName + "   " + BCMPVName);
        cavAmpRBChan = ChannelFactory.defaultFactory().getChannel(cavAmpRBPVName);
        if (bcm != null) BCMChan = ChannelFactory.defaultFactory().getChannel(BCMPVName);
        scanVariable.setChannel(cav.getChannel(RfCavity.CAV_PHASE_SET_HANDLE));
        scanVariableParameter.setChannel(cav.getChannel(RfCavity.CAV_AMP_SET_HANDLE));
        BPM1PhaseMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM1AmpMV.setChannel(bpm1.getChannel(BPM.AMP_AVG_HANDLE));
        BPM2PhaseMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM2AmpMV.setChannel(bpm2.getChannel(BPM.AMP_AVG_HANDLE));
        cavAmpRBMV.setChannel(cavAmpRBChan);
        BCMMV.setChannel(BCMChan);
        BPM1PhaseOffMV.setChannel(bpm1.getChannel(BPM.PHASE_AVG_HANDLE));
        BPM2PhaseOffMV.setChannel(bpm2.getChannel(BPM.PHASE_AVG_HANDLE));
        BCMOffMV.setChannel(BCMChan);
        Channel.flushIO();
        connectChannels(bpm1, bpm2, cav);
        scanController.setScanVariable(scanVariable);
        scanController1D.setScanVariable(scanVariable);
        scanController.setParamVariable(scanVariableParameter);
        setPVText();
    }
}