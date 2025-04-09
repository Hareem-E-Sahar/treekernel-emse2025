public class Test {    public boolean eof() throws IOException {
        return file.length() == fis.getChannel().position();
    }
}