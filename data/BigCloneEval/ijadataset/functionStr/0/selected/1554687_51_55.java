public class Test {    public byte[] berechnen() {
        byte[] hash = md.digest();
        md.reset();
        return hash;
    }
}