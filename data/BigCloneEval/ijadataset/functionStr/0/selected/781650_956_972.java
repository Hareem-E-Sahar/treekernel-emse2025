public class Test {    public static AChannelSelection expandSilence(AChannelSelection s, float silenceThreshold, int minimumWidth) {
        int low = s.getOffset();
        int high = low + s.getLength();
        AChannel ch = s.getChannel();
        low = AOToolkit.getNextLowerNoiseIndex(ch.getSamples(), low, low, silenceThreshold, minimumWidth);
        high = AOToolkit.getNextUpperNoiseIndex(ch.getSamples(), high, ch.getSamples().getLength() - high, silenceThreshold, minimumWidth);
        if (low == -1) {
            low = 0;
        }
        if (high == -1) {
            high = ch.getSamples().getLength() - 1;
        }
        if (low < high) {
            return new AChannelSelection(ch, low, high - low);
        }
        return new AChannelSelection(ch, 0, 0);
    }
}