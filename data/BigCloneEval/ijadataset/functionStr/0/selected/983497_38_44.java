public class Test {    public Object run() {
        try {
            return url_.openStream();
        } catch (Exception e) {
            return null;
        }
    }
}