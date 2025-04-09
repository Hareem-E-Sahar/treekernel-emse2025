public class Test {    public Boolean getHasTitleImg() {
        ChannelExt ext = getChannelExt();
        if (ext != null) {
            return ext.getHasTitleImg();
        } else {
            return null;
        }
    }
}