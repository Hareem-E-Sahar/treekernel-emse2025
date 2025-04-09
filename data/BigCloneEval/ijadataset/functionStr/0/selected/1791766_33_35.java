public class Test {    public static void copy(Reader reader, Writer writer) throws IOException {
        IOUtils.copy(reader, writer, true);
    }
}