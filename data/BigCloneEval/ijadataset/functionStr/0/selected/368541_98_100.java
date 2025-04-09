public class Test {    public AudioChannel getChannel() throws VLCException {
        return (AudioChannel.elementForValue(this.getChannelNumber()));
    }
}