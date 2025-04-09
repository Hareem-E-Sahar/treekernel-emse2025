public class Test {    public ChannelWriter(File f, boolean append) throws IOException {
        this(new FileOutputStream(f, append).getChannel());
    }
}