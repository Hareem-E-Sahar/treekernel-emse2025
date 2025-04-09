public class Test {    public BaseMapAdapter(URL url, Rectangle2D bbox) throws IOException, VisADException {
        this(url.openStream(), bbox);
    }
}