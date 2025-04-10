public class Test {    public SmafEvent[] getSmafEvents(MidiEvent midiEvent, SmafContext context) throws InvalidSmafDataException {
        ShortMessage shortMessage = (ShortMessage) midiEvent.getMessage();
        int channel = shortMessage.getChannel();
        int data1 = shortMessage.getData1();
        int track = context.retrieveSmafTrack(channel);
        int voice = context.retrieveVoice(channel);
        ProgramChangeMessage changeVoiceMessage = new ProgramChangeMessage();
        changeVoiceMessage.setChannel(voice);
        changeVoiceMessage.setProgram(channel == 9 ? 0 : data1);
        context.setBeforeTick(track, midiEvent.getTick());
        return new SmafEvent[] { new SmafEvent(changeVoiceMessage, midiEvent.getTick()) };
    }
}