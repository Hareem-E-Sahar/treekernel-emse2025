public class Test {    public Boolean getStaticChannel() {
        ChannelExt ext = getChannelExt();
        if (ext != null) {
            return ext.getStaticChannel();
        } else {
            return null;
        }
    }
}