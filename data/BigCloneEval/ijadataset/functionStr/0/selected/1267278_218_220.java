public class Test {    private void setChannelValue(EEGChannelState state) {
        setChannel(state.getFrequencyType(), getChannelStrength(state.getRangeFrom()));
    }
}