public class Test {    public String getChannelID() {
        return Dispatch.get(this, "ChannelID").toString();
    }
}