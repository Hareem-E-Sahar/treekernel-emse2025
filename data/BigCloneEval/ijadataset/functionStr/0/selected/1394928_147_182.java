public class Test {    private void fillHeader() {
        this.jPanel_header.add(new JLabel("Source ID"));
        this.jPanel_header.add(new JLabel(header.getSourceName()));
        this.jPanel_header.add(new JLabel("Start Coordinate"));
        this.jPanel_header.add(new JLabel(header.getStartCoordinate().toString(false)));
        this.jPanel_header.add(new JLabel(""));
        this.jPanel_header.add(new JLabel(header.getStartCoordinate().toString(true)));
        this.jPanel_header.add(new JLabel("Centre Freq Channel 1 (MHz)"));
        this.jPanel_header.add(new JLabel(String.valueOf(header.getCentreFreqFirstChannel())));
        this.jPanel_header.add(new JLabel("Channel Offset (MHz)"));
        this.jPanel_header.add(new JLabel(String.valueOf(header.getChannelOffset())));
        this.jPanel_header.add(new JLabel("Observing time actual/requested"));
        this.jPanel_header.add(new JLabel(header.getActualObsTime() + " / " + header.getRequestedObsTime()));
        this.jPanel_header.add(new JLabel("Start time (UTC)"));
        this.jPanel_header.add(new JLabel(header.getUtc()));
        this.jPanel_header.add(new JLabel("Start Az/El"));
        this.jPanel_header.add(new JLabel(header.getStartAz() + ", " + header.getStartEl()));
        this.jPanel_header.add(new JLabel("End Az/El"));
        this.jPanel_header.add(new JLabel(header.getEndAz() + ", " + header.getEndEl()));
        this.jPanel_header.add(new JLabel("Start PA"));
        this.jPanel_header.add(new JLabel(header.getStartParalacticAngle() + ", " + header.getStartParalacticAngle()));
        this.jPanel_header.add(new JLabel("End PA"));
        this.jPanel_header.add(new JLabel(header.getEndParalacticAngle() + ", " + header.getEndParalacticAngle()));
        this.jPanel_header.add(new JLabel("Telescope"));
        this.jPanel_header.add(new JLabel(header.getTelescopeIdentifyingString()));
        this.jPanel_header.add(new JLabel("Receiver"));
        this.jPanel_header.add(new JLabel(header.getReceiverIdentifyingString()));
        this.jPanel_header.add(new JLabel("Backend"));
        this.jPanel_header.add(new JLabel(header.getBackendIdentifyingString()));
        this.jPanel_header.add(new JLabel("Obs Programme"));
        this.jPanel_header.add(new JLabel(header.getObservingProgramme()));
        this.jPanel_header.add(new JLabel("Observer"));
        this.jPanel_header.add(new JLabel(header.getObserverName()));
        this.jPanel_header.add(new JLabel("Obs Type"));
        this.jPanel_header.add(new JLabel(header.getObservationType()));
    }
}