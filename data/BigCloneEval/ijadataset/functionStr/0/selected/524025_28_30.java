public class Test {    public void seek(long position) throws IOException {
        fis.getChannel().position(position);
    }
}