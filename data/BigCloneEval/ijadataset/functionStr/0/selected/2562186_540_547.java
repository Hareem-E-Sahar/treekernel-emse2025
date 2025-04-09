public class Test {    public Boolean getHasContentImg() {
        ChannelExt ext = getChannelExt();
        if (ext != null) {
            return ext.getHasContentImg();
        } else {
            return null;
        }
    }
}