public class Test {    public ReadableByteChannel openForReading() throws FileNotFoundException {
        return new FileInputStream(file).getChannel();
    }
}