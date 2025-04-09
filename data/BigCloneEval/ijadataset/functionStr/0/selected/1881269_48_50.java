public class Test {    public SequentialTrainingFileReader(URL url) throws IOException {
        this(new DataInputStream(url.openStream()));
    }
}