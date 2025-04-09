public class Test {    @Test(expected = IllegalArgumentException.class)
    public void testBadDigest() {
        sequence = null;
        digester.digest(sequence);
    }
}