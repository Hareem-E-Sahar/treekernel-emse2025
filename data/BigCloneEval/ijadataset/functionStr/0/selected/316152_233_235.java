public class Test {    public CobolBytes mapOutput(long position, int size) throws IOException {
        return new CobolBytes(((FileOutputStream) out).getChannel().map(MapMode.READ_WRITE, position, size).array());
    }
}